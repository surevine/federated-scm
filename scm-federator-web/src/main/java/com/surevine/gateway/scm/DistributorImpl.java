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

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.log4j.Logger;

import com.surevine.community.transfermodel.federation.FederationConfiguration;
import com.surevine.gateway.scm.gatewayclient.GatewayClient;
import com.surevine.gateway.scm.gatewayclient.GatewayConfigServiceFacade;
import com.surevine.gateway.scm.gatewayclient.GatewayPackage;
import com.surevine.gateway.scm.gatewayclient.MetadataUtil;
import com.surevine.gateway.scm.git.GitException;
import com.surevine.gateway.scm.git.GitFacade;
import com.surevine.gateway.scm.model.LocalRepoBean;
import com.surevine.gateway.scm.scmclient.SCMCallException;
import com.surevine.gateway.scm.scmclient.SCMCommand;
import com.surevine.gateway.scm.service.SCMFederatorServiceException;

/**
 * Implementation of Distribution Service
 *
 * @author nick.leaver@surevine.com
 */
public class DistributorImpl implements Distributor {

	private static final Logger LOGGER = Logger.getLogger(DistributorImpl.class);

	@Override
	public void distributeToSingleDestination(final String partnerName, final String projectKey,
			final String repositorySlug) throws SCMFederatorServiceException {
		LOGGER.info("Attempting to distribute to partner: " + partnerName + " repository " + projectKey + ":"
				+ repositorySlug);
		try {

			// try to load the repository with the given values - will find if this federator is the repository owner
			// side
			LocalRepoBean repo = SCMCommand.getRepository(projectKey, repositorySlug);

			// if the repository was not found, try to load it with the partner name as a prefix - this will match
			// if we are sending back to the owner of the originally shared repository
			if (repo == null) {
				final String partnerPrefixedProjectKey = partnerName + "_" + projectKey;
				LOGGER.warn("Could not find repository: " + projectKey + "/" + repositorySlug
						+ " - we are not the master of this repository. Attempting to load: "
						+ partnerPrefixedProjectKey + "/" + repositorySlug);

				repo = SCMCommand.getRepository(partnerPrefixedProjectKey, repositorySlug);

				// if the repo is still null, the repository really doesn't exist
				if (repo == null) {
					LOGGER.error("Could not find repository: " + partnerPrefixedProjectKey + "/" + repositorySlug
							+ ". Configuration should be checked.");

					throw new SCMFederatorServiceException("Neither " + projectKey + "/" + repositorySlug + " nor "
							+ partnerPrefixedProjectKey + "/" + repositorySlug + " exist in the SCM system");
				} else {
					LOGGER.info("Found partner shared repository, now distributing back to partner: " + partnerName
							+ " repository " + projectKey + ":" + repositorySlug);
				}
			}

			distribute(repo, MetadataUtil.getSinglePartnerMetadata(repo, partnerName), true);
		} catch (GitException | SCMCallException | CompressorException | ArchiveException | IOException e) {
			throw new SCMFederatorServiceException("Could not distribute " + projectKey + ":" + repositorySlug
					+ " due to internal error: " + e.getMessage());
		}
	}

	@Override
	public void distributeAll() {
		final List<FederationConfiguration> currentSharedRepositories = GatewayConfigServiceFacade.getInstance()
				.getSharedRepositories();

		if (currentSharedRepositories.size() > 0) {
			for (final FederationConfiguration share : currentSharedRepositories) {
				if (share != null && share.getRepository() != null && share.getRepository().getIdentifier() != null
						&& share.getPartner() != null && share.getPartner().getName() != null) {

					final String partnerName = share.getPartner().getSourceKey();
					final String repositoryIdentifier = share.getRepository().getIdentifier();
					final String[] projectKeyParts = repositoryIdentifier.split("/");

					final String projectKey = projectKeyParts[0];
					final String repositorySlug = projectKeyParts[1];

					try {
						distributeToSingleDestination(partnerName, projectKey, repositorySlug);
					} catch (final SCMFederatorServiceException e) {
						LOGGER.info("Failed distribution of " + projectKey + ":" + repositorySlug + " due to error: "
								+ e.getMessage());
					}
				}
			}
		}
	}

	/**
	 * Distributes a repository via the gateway with the provided metadata map
	 *
	 * @param repo
	 *            The repository to distribute.
	 * @param metadata
	 *            the metadata to be send with the distribution
	 * @param sendEvenIfNoUpdates
	 *            if true the repo will be distributed even if there are no new updates since last export
	 */
	private void distribute(final LocalRepoBean repo, final Map<String, String> metadata,
			final boolean sendEvenIfNoUpdates) throws SCMFederatorServiceException, GitException, CompressorException,
			ArchiveException, IOException {
		if (repo == null) {
			throw new SCMFederatorServiceException(
					"The repository information for could not be retrieved from the SCM system.");
		}

		final GitFacade gitFacade = GitFacade.getInstance();

		final boolean alreadyCloned = gitFacade.repoAlreadyCloned(repo);
		boolean hadUpdates;

		if (alreadyCloned) {
			hadUpdates = gitFacade.fetch(repo, "origin");
		} else {
			gitFacade.clone(repo);
			hadUpdates = true;
		}

		if (sendEvenIfNoUpdates || hadUpdates) {
			final Path bundlePath = gitFacade.bundle(repo);
			final GatewayPackage gatewayPackage = new GatewayPackage(bundlePath, metadata);
			gatewayPackage.createArchive();
			GatewayClient.getInstance().sendToGateway(gatewayPackage);
		} else {
			LOGGER.info("Skipping distribution of " + repo.getProjectKey() + ":" + repo.getSlug()
					+ " due to no updates since last distribution");
		}
	}
}
