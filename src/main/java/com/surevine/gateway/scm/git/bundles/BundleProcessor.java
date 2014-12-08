package com.surevine.gateway.scm.git.bundles;

import java.nio.file.Path;
import java.util.Map;

import org.apache.log4j.Logger;

import com.surevine.gateway.scm.gatewayclient.MetadataUtil;
import com.surevine.gateway.scm.git.GitFacade;
import com.surevine.gateway.scm.model.LocalRepoBean;
import com.surevine.gateway.scm.scmclient.SCMCallException;
import com.surevine.gateway.scm.scmclient.SCMCommand;
import com.surevine.gateway.scm.service.SCMFederatorServiceException;
import com.surevine.gateway.scm.util.PropertyUtil;

public abstract class BundleProcessor {

    private Logger logger = Logger.getLogger(BundleProcessor.class);
    
	protected Path bundle;
	protected Map<String, String> metadata;
	protected String partnerName;
	protected String projectKey;
	protected String repositorySlug;
	protected String partnerProjectKey;
	protected String partnerProjectForkKey;
	protected String localForkKey;
	
	protected boolean repoWasCreated = false;
	protected boolean forkWasCreated = false;

	
	public BundleProcessor() {
		//
	}

	public  void setBundleLocation(Path bundleLocation) {
		bundle = bundleLocation;
	}
	
	public Map<String, String> getMetadata() {
		return metadata;
	}
	
	public Path getBundleLocation() {
		return bundle;
	}
	
	public void setBundleMetadata(Map<String, String> metadata) {
		this.metadata = metadata;
		
		partnerName = metadata.get(MetadataUtil.KEY_ORGANISATION).toLowerCase();
		projectKey = metadata.get(MetadataUtil.KEY_PROJECT).toLowerCase();
		repositorySlug = metadata.get(MetadataUtil.KEY_REPO).toLowerCase();
		
		partnerProjectKey = PropertyUtil
				.getPartnerProjectKeyString(partnerName, projectKey)
				.toLowerCase();
		
		partnerProjectForkKey = PropertyUtil
				.getPartnerForkProjectKeyString(partnerName, projectKey)
				.toLowerCase();
		
		localForkKey = projectKey+"_sync";
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
	
	public abstract LocalRepoBean getPrimaryRepo() throws SCMCallException, BundleProcessingException;
	public abstract LocalRepoBean getForkedRepo() throws SCMCallException, BundleProcessingException;

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
            LocalRepoBean primaryRepo = getPrimaryRepo();

            GitFacade.getInstance().addRemote(repoBean, "scm", primaryRepo.getCloneSourceURI());
            
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
            
        	logger.debug("Pushing to fork and creating MR");
            GitFacade.getInstance().push(repoBean, "scm");
    		SCMCommand.createMergeRequest(forkedRepo, primaryRepo);
    		
        } catch (Exception e) {
            logger.error("Could not import new repository " + repoBean, e);
        }
	}
}
