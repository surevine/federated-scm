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
import com.surevine.gateway.scm.git.GitFacade;
import com.surevine.gateway.scm.model.LocalRepoBean;
import com.surevine.gateway.scm.scmclient.SCMCallException;
import com.surevine.gateway.scm.scmclient.SCMCommand;
import com.surevine.gateway.scm.service.SCMFederatorServiceException;
import com.surevine.gateway.scm.util.InputValidator;
import com.surevine.gateway.scm.util.PropertyUtil;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Collection;
import java.util.Map;

/**
 * @author nick.leaver@surevine.com
 * 
 * TODO: Framework code only
 */
public class IncomingProcessorImpl implements IncomingProcessor {
    private Logger logger = Logger.getLogger(IncomingProcessorImpl.class);

    @Override
    public void processIncomingRepository(final Path archivePath) throws SCMFederatorServiceException {
        if (!isTarGz(archivePath)) {
            logger.debug("Not processing " + archivePath + " as it is not a .tar.gz");
            return;
        }

        Collection<Path> extractedFilePaths = extractTarGz(archivePath);
        Path metadataPath = getMetadataFilePath(extractedFilePaths);
        if (metadataPath == null) {
            logger.debug("Not processing " + archivePath + " as it does not contain a metadata file");
            return;
        }
        
        Map<String, String> metadata = MetadataUtil.getMetadataFromPath(metadataPath);
        if (!MetadataUtil.metadataValid(metadata)) {
            logger.debug("Not processing " + archivePath + " as it does not contain all of the required metadata");
            return;
        }
        
        Path extractedGitBundle = getGitBundleFilePath(extractedFilePaths, metadataPath);
        if (!isAGitBundle(extractedGitBundle)) {
            logger.debug("Not processing " + archivePath + " as the .bundle file isn't an actual git bundle");
            return;
        }

        // at this point we have a valid git bundle and some valid metadata so we can start processing
        String partnerName = metadata.get(MetadataUtil.KEY_ORGANISATION);
        String projectKey = metadata.get(MetadataUtil.KEY_PROJECT);
        String repositorySlug = metadata.get(MetadataUtil.KEY_REPO);
        String partnerProjectKey = PropertyUtil.getPartnerProjectKeyString(partnerName, projectKey);
        String partnerProjectForkKey = PropertyUtil.getPartnerForkProjectKeyString(partnerName, projectKey);
        
        // store the bundle in the bundle directory - potentially overwriting the previous version
        Path bundleDestination = Paths.get(PropertyUtil.getRemoteBundleDir(),
                partnerName, projectKey, repositorySlug + ".bundle");

        try {
            logger.debug("Copying received bundle from temporary location " + extractedGitBundle + " to " + bundleDestination);
            if (Files.exists(bundleDestination)) {
                Files.copy(extractedGitBundle, bundleDestination, StandardCopyOption.REPLACE_EXISTING);
            } else {
                Files.createDirectories(bundleDestination.getParent());
                Files.copy(extractedGitBundle, bundleDestination);
            }
        } catch (IOException ioe) {
            throw new SCMFederatorServiceException("Internal error when copying bundle: " + ioe.getMessage());
        }
        
        // only the processing of new repositories is implemented so a simple check to see if a fork and main repo
        // exist allow us to proceed.
        // TODO: logic needs to be worked on here. Identifying whether it's an incoming shared repo, an update to a
        // partner's copy on this site, or an update to a project originated here isn't easy to understand when using
        // just these SCM repo locations and is pretty brittle. Probably need to add metadata to describe these things
        // and simplify all this.
        
        try {
            // a fork exists which means we have received a bundle for this project from this partner previously
            boolean forkRepoExists = SCMCommand.getRepository(partnerProjectForkKey, repositorySlug) != null;
            
            // a main repo exists in the raw project which means this is an incoming change to a repository sourced
            // from this site as the project key should be passed back as-is
            // TODO: What if they have a project and repo with the same name? Do we need metadata to identify the master repository?
            boolean mainRepoExists = SCMCommand.getRepository(projectKey, repositorySlug) != null;
            
            // a partner repo exists which means the received file is an update to a project originating from them but previously shared
            // with this site
            boolean partnerRepoExists = SCMCommand.getRepository(partnerProjectKey, repositorySlug) != null;
            
            if (!forkRepoExists && !mainRepoExists) {
                // newly shared project originating from this partner
                processNewIncomingRepository(bundleDestination, metadata);
            } else if (!forkRepoExists && mainRepoExists) {
                // first update from this partner to a project shared from local SCM system
            } else if (forkRepoExists && partnerRepoExists) {
                // update to a project originating from this partner
            } else if (forkRepoExists && mainRepoExists) {
                // update to a project originating from local SCM system
            }
            
        } catch (SCMCallException e) {
            logger.error("Error while accessing local SCM system", e);
            throw new SCMFederatorServiceException("\"Error while accessing local SCM system: " + e.getMessage());
        }
    }

    private boolean isTarGz(final Path archivePath) {
        return false;
    }

    private Collection<Path> extractTarGz(final Path archivePath) {
        return null;
    }

    private Path getMetadataFilePath(final Collection<Path> paths) {
        return null;
    }

    private Path getGitBundleFilePath(final Collection<Path> extractedFilePaths, final Path metadataPath) {
        return null;
    }

    private boolean isAGitBundle(final Path gitBundlePath) {
        return false;
    }

    private void processNewIncomingRepository(final Path bundleDestination, final Map<String, String> metadata) {
        String partnerName = metadata.get(MetadataUtil.KEY_ORGANISATION);
        String projectKey = metadata.get(MetadataUtil.KEY_PROJECT);
        String repositorySlug = metadata.get(MetadataUtil.KEY_REPO);
        String partnerProjectKey = PropertyUtil.getPartnerProjectKeyString(partnerName, projectKey);
        String partnerProjectForkKey = PropertyUtil.getPartnerForkProjectKeyString(partnerName, projectKey);
        LocalRepoBean repoBean = new LocalRepoBean();
        repoBean.setProjectKey(partnerProjectKey);
        repoBean.setSlug(repositorySlug);
        repoBean.setCloneSourceURI(bundleDestination.toString());
        
        try {
            // create local repository from bundle
            GitFacade.getInstance().clone(repoBean);

            // create project in the SCM system to hold repositories from this partner if it doesn't already exist
            if (!SCMCommand.getProjects().contains(partnerProjectKey)) {
                SCMCommand.createProject(partnerProjectKey);
            }
            
            // check that a repository doesn't already exists where we are planning on creating one in the SCM system
            if (SCMCommand.getRepository(partnerProjectKey, repositorySlug) != null) {
                throw new SCMFederatorServiceException("Repository " + partnerProjectKey + ":"
                        + repositorySlug + " already exists.");
            }
            
            // create a new repository in the SCM system to hold the shared source
            LocalRepoBean createdSCMRepository = SCMCommand.createRepo(partnerProjectKey, repositorySlug);
            
            // push the incoming repository into the new SCM repository
            GitFacade.getInstance().addRemote(repoBean, "scm", createdSCMRepository.getCloneSourceURI());
            GitFacade.getInstance().push(repoBean, "scm");

            // create project in the SCM system to hold update forks from this partner if it doesn't already exist
            if (!SCMCommand.getProjects().contains(partnerProjectForkKey)) {
                SCMCommand.createProject(partnerProjectForkKey);
            }
                        
            // fork the new repository to allow updates to pushed into the fork instead of the master copy in future
            LocalRepoBean forkedRepo = SCMCommand.forkRepo(partnerProjectKey, repositorySlug, partnerProjectForkKey);
            
            // update local repository remote to point at the fork instead for its scm remote
            GitFacade.getInstance().updateRemote(repoBean, "scm", forkedRepo.getCloneSourceURI());
        } catch (Exception e) {
            logger.error("Could not import new repository " + repoBean, e);
        }
    }
}
