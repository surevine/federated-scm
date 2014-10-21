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

import com.surevine.gateway.scm.gatewayclient.GatewayClient;
import com.surevine.gateway.scm.gatewayclient.GatewayConfigServiceFacade;
import com.surevine.gateway.scm.gatewayclient.GatewayPackage;
import com.surevine.gateway.scm.gatewayclient.MetadataUtil;
import com.surevine.gateway.scm.gatewayclient.SharedRepoIdentification;
import com.surevine.gateway.scm.git.GitFacade;
import com.surevine.gateway.scm.model.LocalRepoBean;
import com.surevine.gateway.scm.scmclient.SCMCommandFactory;
import com.surevine.gateway.scm.service.SCMFederatorServiceException;
import org.apache.log4j.Logger;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * Implementation of Distribution Service
 * @author nick.leaver@surevine.com
 */
public class DistributorImpl implements Distributor {
    private static Logger logger = Logger.getLogger(DistributorImpl.class);

    @Override
    public void distributeToSingleDestination(final String partnerName, final String projectKey, final String repositorySlug) 
            throws SCMFederatorServiceException {
        logger.info("Distributing to partner: " + partnerName + " repository "
                + projectKey + ":" + repositorySlug);
        LocalRepoBean repo = SCMCommandFactory.getInstance().getGetRepoCommand().getRepository(projectKey, repositorySlug);
        
        if (repo == null) {
            logger.error("Could not distribute repository:" + projectKey + ":" + repositorySlug
                    + " does not exist in the SCM system");
            throw new SCMFederatorServiceException(projectKey + ":" + repositorySlug
                    + " does not exist in the SCM system");
        }
        
        try {
            distribute(repo, MetadataUtil.getSinglePartnerMetadata(repo, partnerName), true);
        } catch (Exception e) {
            throw new SCMFederatorServiceException("Could not distribute " + repo.getProjectKey() + ":" + repo.getSlug()
                    + " due to internal error: " + e.getMessage());
        }
    }

    @Override
    public void distributeAll() {
        List<SharedRepoIdentification> currentSharedRepositories
                = GatewayConfigServiceFacade.getInstance().getSharedRepositories();

        if (currentSharedRepositories.size() > 0) {
            for (SharedRepoIdentification repoShare:currentSharedRepositories) {
                if (repoShare != null && repoShare.getProjectKey() != null
                    && repoShare.getRepoSlug() != null) {
                    
                    String projectKey = repoShare.getProjectKey();
                    String repositorySlug = repoShare.getRepoSlug();
                    
                    logger.info("Distributing repository " + projectKey + ":" + repositorySlug);
                    LocalRepoBean repo = SCMCommandFactory.getInstance().getGetRepoCommand().getRepository(projectKey, repositorySlug);

                    if (repo == null) {
                        logger.info("Skipping distribution of " + projectKey + ":" + repositorySlug
                                + " because the repository does not exist in the SCM system");
                    } else {
                        try {
                            distribute(repo, MetadataUtil.getMetadata(repo), false);
                        } catch (Exception e) {
                            logger.info("Skipping distribution of " + projectKey + ":" + repositorySlug + " due to error: " + e.getMessage());
                        }
                    }
                }
            }
        }
    }

    /**
     * Distributes a repository via the gateway with the provided metadata map
     * @param repo The repository to distribute.
     * @param metadata the metadata to be send with the distribution
     * @param sendEvenIfNoUpdates if true the repo will be distributed even if there are no new updates since last export
     */
    private void distribute(final LocalRepoBean repo, final Map<String, String> metadata, final boolean sendEvenIfNoUpdates)
            throws Exception {
        if (repo ==  null) {
            throw new SCMFederatorServiceException("The repository information for  " + repo.getProjectKey() + ":"
                    + repo.getSlug() + " could not be retrieved from the SCM system.");
        }

        GitFacade gitFacade = GitFacade.getInstance();

        boolean alreadyCloned = gitFacade.repoAlreadyCloned(repo);
        boolean hadUpdates;

        if (alreadyCloned) {
            hadUpdates = gitFacade.fetch(repo, "origin");
        } else {
            gitFacade.clone(repo);
            hadUpdates = true;
        }

        if (sendEvenIfNoUpdates || hadUpdates) {
            Path bundlePath = gitFacade.bundle(repo);
            GatewayPackage gatewayPackage = new GatewayPackage(bundlePath, metadata);
            gatewayPackage.createArchive();
            GatewayClient.getInstance().sendToGateway(gatewayPackage);
        } else {
            logger.info("Skipping distribution of " + repo.getProjectKey() + ":" + repo.getSlug() + " due to no updates since last distribution");
        }            
    }
}
