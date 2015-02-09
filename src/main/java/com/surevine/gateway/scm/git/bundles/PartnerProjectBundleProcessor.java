package com.surevine.gateway.scm.git.bundles;


import org.apache.log4j.Logger;

import com.surevine.gateway.scm.model.LocalRepoBean;
import com.surevine.gateway.scm.scmclient.SCMCallException;
import com.surevine.gateway.scm.scmclient.SCMCommand;

public class PartnerProjectBundleProcessor extends BundleProcessor {

    private static final Logger LOGGER = Logger.getLogger(PartnerProjectBundleProcessor.class);

	public PartnerProjectBundleProcessor() {
		super();
	}

	public LocalRepoBean getForkedRepo() throws SCMCallException {
        if (!SCMCommand.getProjects().contains(partnerProjectForkKey)) {
        	LOGGER.debug("Creating fork "+partnerProjectForkKey);
            SCMCommand.createProject(partnerProjectForkKey);
            forkWasCreated = true;
        }

        LocalRepoBean forkedRepo = SCMCommand.getRepository(partnerProjectForkKey, repositorySlug);
        if ( forkedRepo == null ) {
	        // fork the new repository to allow updates to pushed into the fork instead of the master copy in future
	        LOGGER.debug("Creating forked repo: "+partnerProjectKey+" "+repositorySlug+" "+partnerProjectForkKey);
	        forkedRepo = SCMCommand.forkRepo(partnerProjectKey, repositorySlug, partnerProjectForkKey);
        }

        return forkedRepo;
	}

	public LocalRepoBean getPrimaryRepo() throws SCMCallException {
        // create project in the SCM system to hold repositories from this partner if it doesn't already exist
        if (!SCMCommand.getProjects().contains(partnerProjectKey)) {
        	LOGGER.debug("Creating repo "+partnerProjectKey);
            SCMCommand.createProject(partnerProjectKey);
        } else {
        	LOGGER.debug(partnerProjectKey+" exists, not creating");
        }

        // check that a repository doesn't already exists where we are planning on creating one in the SCM system
        LocalRepoBean partnerRepository = SCMCommand.getRepository(partnerProjectKey, repositorySlug);
        if (partnerRepository == null) {
        	LOGGER.debug("Creating parter repo "+partnerProjectKey+", "+repositorySlug);
        	partnerRepository = SCMCommand.createRepo(partnerProjectKey, repositorySlug);
            repoWasCreated = true;
        } else {
        	LOGGER.debug(partnerRepository+" exists, not creating");
        }

        return partnerRepository;
	}
}
