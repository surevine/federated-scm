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
package com.surevine.gateway.scm.api.impl;

import com.surevine.gateway.scm.api.IncomingProcessor;
import com.surevine.gateway.scm.gatewayclient.MetadataUtil;
import com.surevine.gateway.scm.git.GitFacade;
import com.surevine.gateway.scm.model.RepoBean;
import com.surevine.gateway.scm.scmclient.GetRepoCommand;
import com.surevine.gateway.scm.scmclient.SCMCommandFactory;
import com.surevine.gateway.scm.service.SCMFederatorServiceException;
import com.surevine.gateway.scm.util.InputValidator;
import com.surevine.gateway.scm.util.PropertyUtil;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;

/**
 * @author nick.leaver@surevine.com
 */
public class IncomingProcessorImpl implements IncomingProcessor {
    private Logger logger = Logger.getLogger(IncomingProcessorImpl.class);
    
    @Override
    public void processIncomingRepository(final Path tmpBundlePath, final Map<String, String> metadata)
            throws SCMFederatorServiceException {
        if (MetadataUtil.VALUE_SOURCE.equals(metadata.get(MetadataUtil.KEY_SOURCE))) {
            String partnerName = metadata.get(MetadataUtil.KEY_ORGANISATION);
            String projectKey = metadata.get(MetadataUtil.KEY_PROJECT);
            String repositorySlug = metadata.get(MetadataUtil.KEY_REPO);
            

            if (!InputValidator.partnerNameIsValid(partnerName)
                    || !InputValidator.projectKeyIsValid(projectKey)
                    || !InputValidator.repoSlugIsValid(repositorySlug)) {
                // one of the params is invalid and we can't use it so do not try and process the repository
                throw new SCMFederatorServiceException("Could not process file " + tmpBundlePath.getFileName()
                        + ": missing or invalid metadata");
            }

            // we have a path to an incoming file and it has correct metadata to indicate it's a git repository
            // so process the file
            logger.info("Processing incoming bundle " + partnerName + ":" + projectKey + ":" + repositorySlug);

            // store the bundle in the right place - potentially overwriting the previous version
            Path bundleDestination = Paths.get(PropertyUtil.getRemoteBundleDir(),
                    partnerName, projectKey, repositorySlug + ".bundle");

            try {
                if (Files.exists(bundleDestination)) {
                    logger.info("Overwriting existing bundle at " + bundleDestination);
                    Files.copy(tmpBundlePath, bundleDestination, StandardCopyOption.REPLACE_EXISTING);
                } else {
                    Files.createDirectories(bundleDestination.getParent());
                    Files.copy(tmpBundlePath, bundleDestination);
                }
            } catch (IOException ioe) {
                logger.error("Could not write incoming bundle to disk: " + bundleDestination, ioe);
            }

            String scmProjectKey = PropertyUtil.getPartnerProjectKeyString(partnerName, projectKey);
            String scmProjectForkKey = PropertyUtil.getPartnerForkProjectKeyString(partnerName, projectKey);
            GetRepoCommand getRepoCommand = SCMCommandFactory.getInstance().getGetRepoCommand();
            
            boolean mainRepoExists = getRepoCommand.getRepository(scmProjectKey, repositorySlug) != null;
            boolean forkRepoExists = getRepoCommand.getRepository(scmProjectForkKey, repositorySlug) != null;
            
            if (mainRepoExists && forkRepoExists) {
                processUpdate(bundleDestination, metadata);
            } else {
                processNewIncomingRepository(bundleDestination, metadata);
            }
        }
    }
    
    private void processNewIncomingRepository(final Path bundleDestination, final Map<String, String> metadata) {
        String partnerName = metadata.get(MetadataUtil.KEY_ORGANISATION);
        String projectKey = metadata.get(MetadataUtil.KEY_PROJECT);
        String repositorySlug = metadata.get(MetadataUtil.KEY_REPO);
        String scmProjectKey = PropertyUtil.getPartnerProjectKeyString(partnerName, projectKey);
        String scmProjectForkKey = PropertyUtil.getPartnerForkProjectKeyString(partnerName, projectKey);
        RepoBean repoBean = new RepoBean();
        repoBean.setProjectKey(scmProjectKey);
        repoBean.setSlug(repositorySlug);
        repoBean.setCloneURL(bundleDestination.toString());
        
        try {
            // create local repository from bundle
            GitFacade.getInstance().clone(repoBean);

            // create project in the SCM system to hold repositories from this partner if it doesn't already exist
            if (!SCMCommandFactory.getInstance().getGetProjectsCommand()
                    .getProjects().contains(scmProjectKey)) {
                SCMCommandFactory.getInstance().getCreateProjectCommand().createProject(scmProjectKey);
            }
            
            // check that a repository doesn't already exists where we are planning on creating one in the SCM system
            if (SCMCommandFactory.getInstance().getGetRepoCommand()
                    .getRepository(scmProjectKey, repositorySlug) != null) {
                throw new SCMFederatorServiceException("Repository " + scmProjectKey + ":"
                        + repositorySlug + " already exists.");
            }
            
            // create a new repository in the SCM system to hold the shared source
            RepoBean createdSCMRepository = SCMCommandFactory.getInstance().getCreateRepoCommand()
                    .createRepo(scmProjectKey, repositorySlug);
            
            // push the incoming repository into the new SCM repository
            GitFacade.getInstance().addRemote(repoBean, "scm", createdSCMRepository.getCloneURL());
            GitFacade.getInstance().push(repoBean, "scm");

            // create project in the SCM system to hold update forks from this partner if it doesn't already exist
            if (!SCMCommandFactory.getInstance().getGetProjectsCommand()
                    .getProjects().contains(scmProjectForkKey)) {
                SCMCommandFactory.getInstance().getCreateProjectCommand().createProject(scmProjectForkKey);
            }
                        
            // fork the new repository to allow updates to pushed into the fork instead of the master copy in future
            RepoBean forkedRepo = SCMCommandFactory.getInstance().getForkRepoCommand()
                    .forkRepo(scmProjectKey, repositorySlug, scmProjectForkKey);
            
            // update local repository remote to point at the fork instead for its scm remote
            GitFacade.getInstance().updateRemote(repoBean, "scm", forkedRepo.getCloneURL());
        } catch (Exception e) {
            logger.error("Could not import new repository " + repoBean, e);
        }
    }

    private void processUpdate(final Path path, final Map<String, String> metadata) {
        // todo
    }
}
