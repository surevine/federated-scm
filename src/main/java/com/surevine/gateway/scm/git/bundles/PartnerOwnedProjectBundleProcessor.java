package com.surevine.gateway.scm.git.bundles;

import java.nio.file.Path;
import java.util.Map;

import org.apache.log4j.Logger;

import com.surevine.gateway.scm.IncomingProcessorImpl;
import com.surevine.gateway.scm.gatewayclient.MetadataUtil;
import com.surevine.gateway.scm.git.GitFacade;
import com.surevine.gateway.scm.model.LocalRepoBean;
import com.surevine.gateway.scm.scmclient.SCMCallException;
import com.surevine.gateway.scm.scmclient.SCMCommand;
import com.surevine.gateway.scm.service.SCMFederatorServiceException;
import com.surevine.gateway.scm.util.PropertyUtil;

public class PartnerOwnedProjectBundleProcessor extends BundleProcessor {

    private Logger logger = Logger.getLogger(PartnerOwnedProjectBundleProcessor.class);
	private String partnerName;
	private String projectKey;
	private String repositorySlug;
	private String partnerProjectKey;
	private String partnerProjectForkKey;
	
	private Boolean repoWasCreated = false;
	private Boolean forkWasCreated = false;
    
	public PartnerOwnedProjectBundleProcessor() {
		super();
	}
	
	@Override
	public void setBundleMetadata(Map<String, String> metadata) {
		super.setBundleMetadata(metadata);
		
		partnerName = metadata.get(MetadataUtil.KEY_ORGANISATION).toLowerCase();
		projectKey = metadata.get(MetadataUtil.KEY_PROJECT).toLowerCase();
		repositorySlug = metadata.get(MetadataUtil.KEY_REPO).toLowerCase();
		
		partnerProjectKey = PropertyUtil
				.getPartnerProjectKeyString(partnerName, projectKey)
				.toLowerCase();
		
		partnerProjectForkKey = PropertyUtil
				.getPartnerForkProjectKeyString(partnerName, projectKey)
				.toLowerCase();
	}
	
	public LocalRepoBean getRepoForBundle() {
        LocalRepoBean repoBean = new LocalRepoBean();
        repoBean.setProjectKey(partnerProjectKey);
        repoBean.setSlug(repositorySlug);
        repoBean.setCloneSourceURI(bundle.toString());
        repoBean.setFromGateway(true);
        repoBean.setSourcePartner(partnerName);
        
        return repoBean;
	}
	
	public void ensureRepositories() {
		//
	}
	
	private LocalRepoBean getForkedRepo() throws SCMCallException {
        if (!SCMCommand.getProjects().contains(partnerProjectForkKey)) {
        	logger.debug("Creating fork "+partnerProjectForkKey);
            SCMCommand.createProject(partnerProjectForkKey);
            forkWasCreated = true;
        }
        
        LocalRepoBean forkedRepo = SCMCommand.getRepository(partnerProjectForkKey, repositorySlug);
        if ( forkedRepo == null ) {
	        // fork the new repository to allow updates to pushed into the fork instead of the master copy in future
	        logger.debug("Creating forked repo: "+partnerProjectKey+" "+repositorySlug+" "+partnerProjectForkKey);
	        forkedRepo = SCMCommand.forkRepo(partnerProjectKey, repositorySlug, partnerProjectForkKey);
        }
        
        return forkedRepo;
	}
	
	public LocalRepoBean getPartnerRepo() throws SCMCallException {
        // create project in the SCM system to hold repositories from this partner if it doesn't already exist
        if (!SCMCommand.getProjects().contains(partnerProjectKey)) {
        	logger.debug("Creating repo "+partnerProjectKey);
            SCMCommand.createProject(partnerProjectKey);
        } else {
        	logger.debug(partnerProjectKey+" exists, not creating");
        }
        
        // check that a repository doesn't already exists where we are planning on creating one in the SCM system
        LocalRepoBean partnerRepository = SCMCommand.getRepository(partnerProjectKey, repositorySlug);
        if (partnerRepository == null) {
        	logger.debug("Creating parter repo "+partnerProjectKey+", "+repositorySlug);
        	partnerRepository = SCMCommand.createRepo(partnerProjectKey, repositorySlug);
            repoWasCreated = true;
        } else {
        	logger.debug(partnerRepository+" exists, not creating");
        }
        
        return partnerRepository;
	}

	@Override
	public void processBundle() throws SCMCallException, BundleProcessingException {
		Map<String, String> metadata = getMetadata();
		Path bundleDestination = getBundleLocation();
		
		if ( metadata == null || bundleDestination == null ){
			throw new BundleProcessingException("Bundle path and metadata both required");
		}
        
        LocalRepoBean repoBean = getRepoForBundle();
        
        try {
        	repoBean.emptyRepoDirectory();
        	
        	logger.debug("Cloning from localRepoBean");
            // create local repository from bundle
            GitFacade.getInstance().clone(repoBean);
            
            // create a new repository in the SCM system to hold the shared source
            LocalRepoBean partnerRepo = getPartnerRepo();

            GitFacade.getInstance().addRemote(repoBean, "scm", partnerRepo.getCloneSourceURI());
            
            if ( repoWasCreated ) {
            	logger.debug("Repo was created, so pushing");
	            // push the incoming repository into the new SCM repository
	            GitFacade.getInstance().push(repoBean, "scm");
            } else {
            	logger.debug("Repo not created, not doing anything");
            }

            // create project in the SCM system to hold update forks from this partner if it doesn't already exist
            LocalRepoBean forkedRepo = getForkedRepo();
            logger.debug("Got forked repo");
            
            // update local repository remote to point at the fork instead for its scm remote
            GitFacade.getInstance().updateRemote(repoBean, "scm", forkedRepo.getCloneSourceURI());
            logger.debug("Updated remote to "+forkedRepo.getCloneSourceURI().toString());
            
            if ( !repoWasCreated && !forkWasCreated) {
            	logger.debug("Repo existing, as did fork, so pushing to fork and creating MR");
	            GitFacade.getInstance().push(repoBean, "scm");
	    		SCMCommand.createMergeRequest(forkedRepo, partnerRepo);
            }
            
        } catch (Exception e) {
            logger.error("Could not import new repository " + repoBean, e);
        }
	}
}
