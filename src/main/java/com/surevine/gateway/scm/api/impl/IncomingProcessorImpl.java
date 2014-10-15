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
    public void processIncomingRepository(final Path path, final Map<String, String> metadata)
            throws SCMFederatorServiceException {
        if (MetadataUtil.VALUE_SOURCE.equals(metadata.get(MetadataUtil.KEY_SOURCE))) {
            String partnerName = metadata.get(MetadataUtil.KEY_ORGANISATION);
            String projectKey = metadata.get(MetadataUtil.KEY_PROJECT);
            String repositorySlug = metadata.get(MetadataUtil.KEY_REPO);
            String scmProjectKey = PropertyUtil.getPartnerProjectKeyString(partnerName);
            String scmProjectForkKey = PropertyUtil.getPartnerForkProjectKeyString(partnerName);

            if (!InputValidator.partnerNameIsValid(partnerName)
                    || !InputValidator.projectKeyIsValid(projectKey)
                    || !InputValidator.repoSlugIsValid(repositorySlug)) {
                // one of the params is invalid and we can't use it so do not try and process the repository
                throw new SCMFederatorServiceException("Could not process file " + path.getFileName()
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
                    Files.copy(path, bundleDestination, StandardCopyOption.REPLACE_EXISTING);
                } else {
                    Files.createDirectories(bundleDestination.getParent());
                    Files.copy(path, bundleDestination);
                }
            } catch (IOException ioe) {
                logger.error("Could not write incoming bundle to disk: " + bundleDestination, ioe);
            }

            // at this point the bundle is in the right place at ${bundle_directory}/$partner/$project/$repo.bundle
            // now check if there's an existing working copy of the repository that has the correct remotes.
            // if there is then we need to pull from the bundle, push into the SCM fork, and create merge requests,
            // if there isn't then we create a new repository from the bundle, push to a new repository in the SCM
            // system, and fork the SCM repository for future incoming updates

            RepoBean repoBean = new RepoBean();
            repoBean.setRemote(true);
            repoBean.setSourcePartner(partnerName);
            repoBean.setSlug(repositorySlug);
            repoBean.setProjectKey(projectKey);
            
            Path repositoryDirectory = repoBean.getRepoDirectory();
            
            if (Files.exists(repositoryDirectory)) {
                // TODO: Process an update
            } else {
                // New incoming repository
                try {
                    // create local repository from bundle
                    GitFacade.getInstance().clone(repoBean);

                    // create SCM repository
                    if (SCMCommandFactory.getInstance().getGetProjectsCommand()
                            .getProjects().contains(scmProjectKey)) {
                        SCMCommandFactory.getInstance().getCreateProjectCommand().createProject(scmProjectKey);
                    }
                    
                    if (SCMCommandFactory.getInstance().getGetRepoCommand()
                            .getRepository(scmProjectKey, repositorySlug) != null) {
                        throw new SCMFederatorServiceException("Repository " + scmProjectKey + ":"
                                + repositorySlug + " already exists.");
                    }
                    
                    RepoBean createdSCMRepository = SCMCommandFactory.getInstance().getCreateRepoCommand()
                            .createRepo(scmProjectKey, repositorySlug);
                    
                    // stash derives the slug from the repository name provided - we provided an existing slug as 
                    // the name when we created the repo so it should match but if it doesn't, fail here
                    if (createdSCMRepository.getSlug() == null || 
                            createdSCMRepository.getSlug().equals(repositorySlug)) {
                        throw new SCMFederatorServiceException("Repository slug on new repo doesn't match incoming");
                    }
                    
                    // fork the new repository
                    RepoBean forkedRepo = SCMCommandFactory.getInstance().getForkRepoCommand()
                            .forkRepo(scmProjectKey, repositorySlug, scmProjectForkKey);
                    
                    // push into the fork
                    GitFacade.getInstance().addRemote(repoBean, "scm", forkedRepo.getCloneURL());
                    GitFacade.getInstance().push(repoBean, "scm");
                    

                } catch (Exception e) {
                    logger.error("Could not import new repository " + repoBean, e);
                }
                
                // - create SCM repository and push from local repository
                // - fork SCM repository and set as a remote on local repository
                
            }
        }
    }
}
