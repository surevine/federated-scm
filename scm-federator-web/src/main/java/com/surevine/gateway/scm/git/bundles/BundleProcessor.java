package com.surevine.gateway.scm.git.bundles;

import java.nio.file.Path;
import java.util.Map;

import org.apache.log4j.Logger;

import com.surevine.gateway.scm.gatewayclient.MetadataUtil;
import com.surevine.gateway.scm.git.GitFacade;
import com.surevine.gateway.scm.model.LocalRepoBean;
import com.surevine.gateway.scm.scmclient.SCMCallException;
import com.surevine.gateway.scm.scmclient.SCMCommand;
import com.surevine.gateway.scm.util.PropertyUtil;

public abstract class BundleProcessor {

	private static final Logger LOGGER = Logger.getLogger(BundleProcessor.class);

	protected Path bundle;
	protected Map<String, String> metadata;
	protected String partnerName;
	protected String projectKey;
	protected String repositorySlug;
	protected String partnerProjectKey;
	protected String partnerProjectForkKey;

	protected boolean repoWasCreated = false;
	protected boolean forkWasCreated = false;

	public BundleProcessor() {
		//
	}

	public void setBundleLocation(final Path bundleLocation) {
		bundle = bundleLocation;
	}

	public Map<String, String> getMetadata() {
		return metadata;
	}

	public Path getBundleLocation() {
		return bundle;
	}

	public void setBundleMetadata(final Map<String, String> metadata) {
		this.metadata = metadata;
		LOGGER.debug(metadata.toString());

		partnerName = metadata.get(MetadataUtil.KEY_ORGANISATION);
		projectKey = metadata.get(MetadataUtil.KEY_PROJECT);
		repositorySlug = metadata.get(MetadataUtil.KEY_REPO);

		LOGGER.debug(partnerName + " - " + projectKey + " - " + repositorySlug);

		partnerProjectKey = PropertyUtil.getPartnerProjectKeyString(partnerName, projectKey);

		partnerProjectForkKey = PropertyUtil.getPartnerForkProjectKeyString(partnerName, projectKey);
	}

	public LocalRepoBean getRepoForBundle() {
		final LocalRepoBean repoBean = new LocalRepoBean();
		repoBean.setProjectKey(partnerProjectKey);
		repoBean.setSlug(repositorySlug);
		repoBean.setCloneSourceURI(bundle.toString());
		repoBean.setFromGateway(true);
		repoBean.setSourcePartner(partnerName);

		return repoBean;
	}

	public abstract LocalRepoBean getPrimaryRepo() throws SCMCallException, BundleProcessingException;

	public abstract LocalRepoBean getForkedRepo() throws SCMCallException, BundleProcessingException;

	public void processBundle() throws SCMCallException, BundleProcessingException {
		final Map<String, String> metadata = getMetadata();
		final Path bundleDestination = getBundleLocation();

		if (metadata == null || bundleDestination == null) {
			throw new BundleProcessingException("Bundle path and metadata both required");
		}

		final LocalRepoBean repoBean = getRepoForBundle();

		try {
			repoBean.emptyRepoDirectory();

			LOGGER.debug("Cloning from localRepoBean");
			// create local repository from bundle
			GitFacade.getInstance().clone(repoBean);

			// create a new repository in the SCM system to hold the shared source
			final LocalRepoBean primaryRepo = getPrimaryRepo();

			LOGGER.debug("Adding `scm` remote at " + primaryRepo.getCloneSourceURI());
			GitFacade.getInstance().addRemote(repoBean, "scm", primaryRepo.getCloneSourceURI());

			if (repoWasCreated) {
				LOGGER.debug("Repo was created, so pushing");
				// push the incoming repository into the new SCM repository
				GitFacade.getInstance().push(repoBean, "scm");
			} else {
				LOGGER.debug("Repo not created, not doing anything");
			}

			// create project in the SCM system to hold update forks from this partner if it doesn't already exist
			final LocalRepoBean forkedRepo = getForkedRepo();
			LOGGER.debug("Got forked repo");

			LOGGER.debug("Updating `scm` remote to " + forkedRepo.getCloneSourceURI());
			// update local repository remote to point at the fork instead for its scm remote
			GitFacade.getInstance().updateRemote(repoBean, "scm", forkedRepo.getCloneSourceURI());
			LOGGER.debug("Updated remote to " + forkedRepo.getCloneSourceURI().toString());

			LOGGER.debug("Pushing to fork and creating MR");
			GitFacade.getInstance().push(repoBean, "scm");
			SCMCommand.createMergeRequest(forkedRepo, primaryRepo);

		} catch (final Exception e) {
			LOGGER.error("Could not import new repository " + repoBean, e);
		}
	}
}
