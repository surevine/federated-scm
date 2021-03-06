package com.surevine.gateway.scm.git.bundles;

import org.apache.log4j.Logger;

import com.surevine.gateway.scm.model.LocalRepoBean;
import com.surevine.gateway.scm.scmclient.SCMCallException;
import com.surevine.gateway.scm.scmclient.SCMCommand;

public class LocalProjectBundleProcessor extends BundleProcessor {

	private static final Logger LOGGER = Logger.getLogger(LocalProjectBundleProcessor.class);

	public LocalProjectBundleProcessor() {
		super();
	}

	@Override
	public LocalRepoBean getForkedRepo() throws SCMCallException {
		LOGGER.debug("Checking if " + partnerProjectForkKey + " exists");
		if (!SCMCommand.projectExists(partnerProjectForkKey)) {
			LOGGER.debug("Creating fork " + partnerProjectForkKey);
			SCMCommand.createProject(partnerProjectForkKey);
			forkWasCreated = true;
		}

		LocalRepoBean forkedRepo = SCMCommand.getRepository(partnerProjectForkKey, repositorySlug);
		if (forkedRepo == null) {
			// fork the new repository to allow updates to pushed into the fork instead of the master copy in future
			LOGGER.debug("Creating forked repo: " + projectKey + " " + repositorySlug + " " + partnerProjectForkKey);
			forkedRepo = SCMCommand.forkRepo(projectKey, repositorySlug, partnerProjectForkKey);
		}

		return forkedRepo;
	}

	@Override
	public LocalRepoBean getPrimaryRepo() throws SCMCallException, BundleProcessingException {
		// this repo should exist, otherwise we wouldn't be here, but let's double-check
		if (!SCMCommand.projectExists(projectKey)) {
			throw new BundleProcessingException("Using LocalProjectBundleProcessor, but no project exists");
		}

		final LocalRepoBean localRepository = SCMCommand.getRepository(projectKey, repositorySlug);
		if (localRepository == null) {
			throw new BundleProcessingException("Using LocalProjectBundleProcessor, but no repo exists");
		}

		return localRepository;
	}
}
