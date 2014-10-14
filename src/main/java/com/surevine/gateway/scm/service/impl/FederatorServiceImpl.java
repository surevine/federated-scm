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
package com.surevine.gateway.scm.service.impl;

import com.surevine.gateway.scm.gatewayclient.GatewayPackage;
import com.surevine.gateway.scm.gatewayclient.GatewayUtil;
import com.surevine.gateway.scm.git.GitFacade;
import com.surevine.gateway.scm.scmclient.CommandFactory;
import com.surevine.gateway.scm.scmclient.bean.RepoBean;
import com.surevine.gateway.scm.service.FederatorService;
import com.surevine.gateway.scm.service.SCMFederatorServiceException;
import org.apache.log4j.Logger;

import java.nio.file.Path;
import java.util.Map;

/**
 * Implementation of Federator Service
 * @author nick.leaver@surevine.com
 */
public class FederatorServiceImpl implements FederatorService {
    private static Logger logger = Logger.getLogger(FederatorServiceImpl.class);

    @Override
    public void processIncomingRepository(final Path path, final Map<String, String> metadata) 
            throws SCMFederatorServiceException {
        // TODO
        // examine metadata for project key and repo name
        // determine if we have dealt with the repository before
        // if we have seen it before update incoming fork repository and create merge requests
        // if we haven't seen it before create new repo in SCM system and create fork from the repo for future changes
    }

    @Override
    public void distribute(final String partnerName, final String projectKey, final String repositorySlug) 
            throws SCMFederatorServiceException {
        logger.debug("Distributing to partner: " + partnerName + " repository "
                + projectKey + ":" + repositorySlug);
        RepoBean repo = CommandFactory.getInstance().getGetRepoCommand().getRepository(projectKey, repositorySlug);
        distribute(repo, GatewayUtil.getSinglePartnerMetadata(repo, partnerName));        
    }

    @Override
    public void distribute(final String projectKey, final String repositorySlug) throws SCMFederatorServiceException {
        logger.debug("Distributing repository " + projectKey + ":" + repositorySlug);
        RepoBean repo = CommandFactory.getInstance().getGetRepoCommand().getRepository(projectKey, repositorySlug);
        distribute(repo, GatewayUtil.getMetadata(repo));
    }

    /**
     * Distributes a repository via the gateway with the provided metadata map
     * @param repo The repository to distribute.
     * @param metadata the metadata to be send with the distribution
     */
    private void distribute(final RepoBean repo, final Map<String, String> metadata)
            throws SCMFederatorServiceException {
        if (repo ==  null) {
            throw new SCMFederatorServiceException("The repository information for  " + repo.getProject().getKey() + ":"
                    + repo.getSlug() + " could not be retrieved from the SCM system.");
        }

        GitFacade gitFacade = GitFacade.getInstance();

        try {
            boolean alreadyCloned = gitFacade.repoAlreadyCloned(repo);

            if (alreadyCloned) {
                gitFacade.pull(repo);
            } else {
                gitFacade.clone(repo);
            }

            Path bundlePath = gitFacade.bundle(repo);
            
            GatewayPackage gatewayPackage = new GatewayPackage(bundlePath, metadata);
            gatewayPackage.createArchive();
            GatewayUtil.sendToGateway(gatewayPackage);
            
        } catch (Exception e) {
            logger.error(e);
            throw new SCMFederatorServiceException(e.getMessage());
        }
    }
}
