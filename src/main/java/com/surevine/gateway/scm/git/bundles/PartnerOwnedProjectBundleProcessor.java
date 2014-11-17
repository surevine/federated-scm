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
		
		partnerName = metadata.get(MetadataUtil.KEY_ORGANISATION);
		projectKey = metadata.get(MetadataUtil.KEY_PROJECT);
		repositorySlug = metadata.get(MetadataUtil.KEY_REPO);
		
		partnerProjectKey = PropertyUtil.getPartnerProjectKeyString(partnerName, projectKey);
		partnerProjectForkKey = PropertyUtil.getPartnerForkProjectKeyString(partnerName, projectKey);
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
            SCMCommand.createProject(partnerProjectForkKey);
            forkWasCreated = true;
        }
        
        LocalRepoBean forkedRepo = SCMCommand.getRepository(partnerProjectForkKey, repositorySlug);
        if ( forkedRepo == null ) {
	        // fork the new repository to allow updates to pushed into the fork instead of the master copy in future
	        logger.debug(partnerProjectKey+" "+repositorySlug+" "+partnerProjectForkKey);
	        forkedRepo = SCMCommand.forkRepo(partnerProjectKey, repositorySlug, partnerProjectForkKey);
        }
        
        return forkedRepo;
	}
	
	public LocalRepoBean getPartnerRepo() throws SCMCallException {
        // create project in the SCM system to hold repositories from this partner if it doesn't already exist
        if (!SCMCommand.getProjects().contains(partnerProjectKey)) {
            SCMCommand.createProject(partnerProjectKey);
            repoWasCreated = true;
        }
        
        // check that a repository doesn't already exists where we are planning on creating one in the SCM system
        LocalRepoBean partnerRepository = SCMCommand.getRepository(partnerProjectKey, repositorySlug);
        if (partnerRepository == null) {
        	partnerRepository = SCMCommand.createRepo(partnerProjectKey, repositorySlug);
        }
        
        return partnerRepository;
	}

	@Override
	public void processBundle() throws SCMCallException, BundleProcessingException {
		
		String something = "nothing";
		something += "hello";
		
		Map<String, String> metadata = getMetadata();
		Path bundleDestination = getBundleLocation();
		
		if ( metadata == null || bundleDestination == null ){
			throw new BundleProcessingException("Bundle path and metadata both required");
		}
		
		logger.debug(something);
        
        LocalRepoBean repoBean = getRepoForBundle();
        
        try {
        	repoBean.emptyRepoDirectory();
        	
            // create local repository from bundle
            GitFacade.getInstance().clone(repoBean);
            
            // create a new repository in the SCM system to hold the shared source
            LocalRepoBean parterRepo = getPartnerRepo();
            
            if ( repoWasCreated ) {
	            // push the incoming repository into the new SCM repository
	            GitFacade.getInstance().addRemote(repoBean, "scm", parterRepo.getCloneSourceURI());
	            GitFacade.getInstance().push(repoBean, "scm");
            }

            // create project in the SCM system to hold update forks from this partner if it doesn't already exist
            LocalRepoBean forkedRepo = getForkedRepo();
            
            if ( !repoWasCreated && !forkWasCreated ) {
	            // push the incoming repository into the sync repository
	            GitFacade.getInstance().addRemote(repoBean, "scm", forkedRepo.getCloneSourceURI());
	            GitFacade.getInstance().push(repoBean, "scm");
            }
            
            // update local repository remote to point at the fork instead for its scm remote
            logger.debug(GitFacade.getInstance().getRemotes(repoBean).toString());
            GitFacade.getInstance().updateRemote(repoBean, "scm", forkedRepo.getCloneSourceURI());
            
            if ( !repoWasCreated && !forkWasCreated) {
            	createMergeRequest();
            }
            
        } catch (Exception e) {
            logger.error("Could not import new repository " + repoBean, e);
        }
	}
	
	private void createMergeRequest() {
		// look to see if tips of partnerRepo and partnerForkRepo are different
		
		// if they are, create a MR between the two
	}
}
