/*
 * Copyright (C) 2008-2014 Surevine Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/
package com.surevine.gateway.scm;

import com.surevine.gateway.scm.gatewayclient.MetadataUtil;
import com.surevine.gateway.scm.git.bundles.BundleProcessingException;
import com.surevine.gateway.scm.git.bundles.BundleProcessor;
import com.surevine.gateway.scm.git.bundles.LocalProjectBundleProcessor;
import com.surevine.gateway.scm.git.bundles.NoBundleProcessorAvailableException;
import com.surevine.gateway.scm.git.bundles.PartnerProjectBundleProcessor;
import com.surevine.gateway.scm.scmclient.SCMCallException;
import com.surevine.gateway.scm.scmclient.SCMCommand;
import com.surevine.gateway.scm.service.SCMFederatorServiceException;
import com.surevine.gateway.scm.util.PropertyUtil;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author nick.leaver@surevine.com
 *
 * TODO: Framework code only
 */
public class IncomingProcessorImpl implements IncomingProcessor {
    private static final Logger LOGGER = Logger.getLogger(IncomingProcessorImpl.class);
    private List<File> createdFiles = new ArrayList<File>();

    @Override
    public void processIncomingRepository(final Path archivePath) throws SCMFederatorServiceException {
        if (!isTarGz(archivePath)) {
            LOGGER.debug("Not processing " + archivePath + " as it is not a .tar.gz");
            return;
        }

        registerCreatedFile(archivePath.toFile());

        LOGGER.debug(archivePath+" being processed as a potential git bundle");

        Path metadataPath = null;
        Collection<Path> extractedFilePaths = null;
        try {
        	LOGGER.debug("Extracting "+archivePath);
        	extractedFilePaths = extractTarGz(archivePath);
        	if ( extractedFilePaths == null ) {
        		LOGGER.debug(archivePath+" did not have the right contents");
        		return;
        	}

            metadataPath = getMetadataFilePath(extractedFilePaths);
        	LOGGER.debug("Extracted "+archivePath+", metadata path is "+metadataPath);
        } catch ( IOException e ) {
            LOGGER.debug("Error when expanding " + archivePath + ": "+e.getMessage());
            return;
        }

        LOGGER.debug(archivePath+" expanded, reading metadata");

        if (metadataPath == null) {
            LOGGER.debug("Not processing " + archivePath + " as it does not contain a metadata file");
            return;
        }

        Map<String, String> metadata = MetadataUtil.getMetadataFromPath(metadataPath);
        if (!MetadataUtil.metadataValid(metadata)) {
            LOGGER.debug("Not processing " + archivePath + " as it does not contain all of the required metadata");
            return;
        }

        LOGGER.debug(archivePath+" metadata read");

        Path extractedGitBundle = getGitBundleFilePath(extractedFilePaths);

        LOGGER.debug(archivePath+" bundle copied, sending for processing");

        processIncomingRepository(extractedGitBundle, metadata);
    }

    @Override
    public void processIncomingRepository(final Path extractedGitBundle, final Map<String, String> metadata) throws SCMFederatorServiceException {

    	// Strip local organisation name from projectKey to prevent duplicate repository from being created
    	sanitiseProjectKey(metadata);

        Path bundleDestination;
        try {
        	bundleDestination = copyBundle(extractedGitBundle, metadata);
        } catch ( IOException ioe ) {
            throw new SCMFederatorServiceException("Internal error when copying bundle: " + ioe.getMessage());
        }

        // at this point we have a valid git bundle and some valid metadata so we can start processing
        try {
            BundleProcessor processor = getAppropriateBundleProcessor(bundleDestination, metadata);
        	processor.processBundle();
        } catch (SCMCallException e) {
            LOGGER.error("Error while accessing local SCM system", e);
            throw new SCMFederatorServiceException("\"Error while accessing local SCM system: " + e.getMessage());
        } catch (NoBundleProcessorAvailableException e) {
            LOGGER.error("No bundle processor available for the determined repository state", e);
            throw new SCMFederatorServiceException("\"Error while unpacking bundle: No processor available");
		} catch (BundleProcessingException e) {
			LOGGER.error("Error while processing bundle: "+e.getMessage());
            throw new SCMFederatorServiceException("\"Error while unpacking bundle: "+e.getMessage());
		} finally {
			clearCreatedFiles();
		}
    }

    // only the processing of new repositories is implemented so a simple check to see if a fork and main repo
    // exist allow us to proceed.
    // TODO: logic needs to be worked on here. Identifying whether it's an incoming shared repo, an update to a
    // partner's copy on this site, or an update to a project originated here isn't easy to understand when using
    // just these SCM repo locations and is pretty brittle. Probably need to add metadata to describe these things
    // and simplify all this.
    public BundleProcessor getAppropriateBundleProcessor(Path bundleDestination, Map<String, String> metadata) throws SCMCallException, NoBundleProcessorAvailableException {

        String projectKey = metadata.get(MetadataUtil.KEY_PROJECT);
        String repositorySlug = metadata.get(MetadataUtil.KEY_REPO);

        BundleProcessor rtn = null;

        // a main repo exists in the raw project which means this is an incoming change to a repository sourced
        // from this site as the project key should be passed back as-is
        // TODO: What if they have a project and repo with the same name? Do we need metadata to identify the master repository?
        boolean mainRepoExists = SCMCommand.getRepository(projectKey, repositorySlug) != null;

        /**
         * For a given repo: `project/repo` and a partner `partner`
         */
        if ( !mainRepoExists ) {
        	rtn = new PartnerProjectBundleProcessor();
        } else {
        	rtn = new LocalProjectBundleProcessor();
        }

        rtn.setBundleLocation(bundleDestination);
        rtn.setBundleMetadata(metadata);

        return rtn;
    }

    public Path buildBundleDestination(Map<String, String> metadata) {
        String partnerName = metadata.get(MetadataUtil.KEY_ORGANISATION);
        String projectKey = metadata.get(MetadataUtil.KEY_PROJECT);
        String repositorySlug = metadata.get(MetadataUtil.KEY_REPO);

        return Paths.get(PropertyUtil.getRemoteBundleDir(),
                partnerName, projectKey, repositorySlug + ".bundle");
    }

    public Path copyBundle(Path extractedGitBundle, Map<String, String> metadata ) throws IOException {

        Path bundleDestination = buildBundleDestination(metadata);

        LOGGER.debug("Copying received bundle from temporary location " + extractedGitBundle + " to " + bundleDestination);
        if (Files.exists(bundleDestination)) {
            Files.copy(extractedGitBundle, bundleDestination, StandardCopyOption.REPLACE_EXISTING);
        } else {
            Files.createDirectories(bundleDestination.getParent());
            Files.copy(extractedGitBundle, bundleDestination);
        }

        registerCreatedFile(extractedGitBundle.toFile());
        registerCreatedFile(bundleDestination.toFile());

        return bundleDestination;
    }

    public TarArchiveInputStream openTarGz(final Path archivePath) throws FileNotFoundException, IOException {
    	File file = archivePath.toFile();

    	return new TarArchiveInputStream(
    		new GzipCompressorInputStream(
    			new BufferedInputStream(
					new FileInputStream(file)
    			)
			)
		);
    }

    public boolean isTarGz(final Path archivePath) {
    	try {
    		openTarGz(archivePath);
	    	return true;
    	} catch ( Exception e ) {
    		LOGGER.debug("Verification of "+archivePath.getName(archivePath.getNameCount() - 1)+" failed: "+e.getMessage());
    		return false;
    	}
    }

    public boolean tarGzHasExpectedContents(final Path archivePath) {
    	try {
	    	TarArchiveInputStream archive = openTarGz(archivePath);
	    	return tarGzHasExpectedContents(archive);
    	} catch ( IOException e ) {
    		return false;
    	}
    }

    public boolean tarGzHasExpectedContents(final TarArchiveInputStream archive){
    	Boolean hasMetaData = false;
    	Boolean hasBundle = false;
    	Integer fileCount = 0;

        TarArchiveEntry entry = null;

        try {
	    	while ( (entry = archive.getNextTarEntry()) != null ) {
	    		if (".metadata.json".equals(entry.getName())) {
	    			hasMetaData = true;
	    		} else if ( entry.getName().contains(".bundle") ) {
	    			hasBundle = true;
	    		}
	    		fileCount++;
	    	}
	    	archive.reset();
	    	return hasMetaData && hasBundle && fileCount == 2;
        } catch ( IOException e ) {
        	return false;
        }
    }

    public Path getTmpExtractionPath(Path archivePath) {
    	String tmpDir = PropertyUtil.getTempDir();
    	String filename = archivePath.getName(archivePath.getNameCount() - 1).toString();
    	tmpDir += "/"+filename.substring(0, filename.indexOf('.'));
    	return new File(tmpDir).toPath();
    }

    public Collection<Path> extractTarGz(final Path archivePath) throws FileNotFoundException, IOException {
    	TarArchiveInputStream archive = openTarGz(archivePath);

    	if (!tarGzHasExpectedContents(archive)) {
    		LOGGER.debug(archivePath.toString()+" does not have the correct contents - exactly one .bundle and exactly one .metadata.json");
    		return null;
    	}

    	// We need to re-open the archive as the Iterator implementation
    	// has #reset as a no-op
    	archive.close();
    	archive = openTarGz(archivePath);

    	Path tmpDir = getTmpExtractionPath(archivePath);
        Files.createDirectories(tmpDir);

    	Collection<Path> extractedFiles = new ArrayList<Path>();
		LOGGER.debug("Extracting "+archivePath.toString()+" to "+tmpDir);

		File tmp = tmpDir.toFile();

		TarArchiveEntry entry = archive.getNextTarEntry();
		while (entry != null) {
			final File outputFile = new File(tmp, entry.getName());

			if (!entry.isFile()) {
				continue;
			}

			LOGGER.debug("Creating output file "+outputFile.getAbsolutePath());
			registerCreatedFile(outputFile);

			final OutputStream outputFileStream = new FileOutputStream(outputFile);
			IOUtils.copy(archive, outputFileStream);
			outputFileStream.close();

			extractedFiles.add(outputFile.toPath());
			entry = archive.getNextTarEntry();
		}
	    archive.close();

		return extractedFiles;
    }

    private void registerCreatedFile(File outputFile) {
    	LOGGER.debug("Registering "+outputFile.getAbsolutePath()+" for eventual cleanup");
    	createdFiles.add(outputFile);
	}

    private void clearCreatedFiles() {
    	LOGGER.debug("Cleaning up "+createdFiles.size()+" created file(s)");
    	for ( File file : createdFiles ) {
    		if ( file.exists() ) {
    	    	LOGGER.debug("Deleting "+file.getAbsolutePath());
    			file.delete();
    		}
    	}
    }

	private Path findFileEndsWith(final Collection<Path> paths, String endsWith) {
    	for ( Path entry : paths ) {
    		String lastPath = entry.getName(entry.getNameCount() - 1).toString();
    		if ( StringUtils.endsWith(lastPath, endsWith) ) {
    			return entry;
    		}
    	}
        return null;
    }

	/**
	 * Strips local organisation name from projectKey of incoming repo (if present).
	 * Prevents duplicates of local repo's being produced following shares back
	 * from partner.
	 *
	 * @param metadata Sanitised metadata
	 */
	private void sanitiseProjectKey(final Map<String, String> metadata) {
		String organisationName = PropertyUtil.getOrgName().toLowerCase();
    	String projectKey = metadata.get(MetadataUtil.KEY_PROJECT);

    	// If local organisation name is present it indicates that the project is mastered locally
    	// (and this is a share back from partner)
    	if(projectKey.startsWith(organisationName+"_")) {
    		metadata.put(MetadataUtil.KEY_PROJECT, projectKey.replace(organisationName+"_", ""));
    	}
	}

    public Path getMetadataFilePath(final Collection<Path> paths) {
    	return findFileEndsWith(paths, ".metadata.json");
    }

    public Path getGitBundleFilePath(final Collection<Path> extractedFilePaths) {
    	return findFileEndsWith(extractedFilePaths, ".bundle");
    }
}
