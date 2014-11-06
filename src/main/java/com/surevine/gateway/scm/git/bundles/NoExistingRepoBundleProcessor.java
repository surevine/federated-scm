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

public class NoExistingRepoBundleProcessor extends BundleProcessor {

    private Logger logger = Logger.getLogger(NoExistingRepoBundleProcessor.class);
    
	public NoExistingRepoBundleProcessor() {
		super();
	}
	
	public LocalRepoBean getRepoForBundle() {
		String partnerName = metadata.get(MetadataUtil.KEY_ORGANISATION);
		String projectKey = metadata.get(MetadataUtil.KEY_PROJECT);
		String repositorySlug = metadata.get(MetadataUtil.KEY_REPO);
		
		String partnerProjectKey = PropertyUtil.getPartnerProjectKeyString(partnerName, projectKey);

        LocalRepoBean repoBean = new LocalRepoBean();
        repoBean.setProjectKey(partnerProjectKey);
        repoBean.setSlug(repositorySlug);
        repoBean.setCloneSourceURI(bundle.toString());
        
        return repoBean;
	}

	@Override
	public void processBundle() throws SCMCallException, BundleProcessingException {
		Map<String, String> metadata = getMetadata();
		Path bundleDestination = getBundleLocation();
		
		if ( metadata == null || bundleDestination == null ){
			throw new BundleProcessingException("Bundle path and metadata both required");
		}
		
        String partnerName = metadata.get(MetadataUtil.KEY_ORGANISATION);
        String projectKey = metadata.get(MetadataUtil.KEY_PROJECT);
        String repositorySlug = metadata.get(MetadataUtil.KEY_REPO);
        
        String partnerProjectKey = PropertyUtil.getPartnerProjectKeyString(partnerName, projectKey);
        String partnerProjectForkKey = PropertyUtil.getPartnerForkProjectKeyString(partnerName, projectKey);
        
        LocalRepoBean repoBean = getRepoForBundle();
        
        try {
            // create local repository from bundle
            GitFacade.getInstance().clone(repoBean);
            
            createProjectAndVerifyRepo(partnerProjectKey, repositorySlug);
            
            // create a new repository in the SCM system to hold the shared source
            LocalRepoBean createdSCMRepository = SCMCommand.createRepo(partnerProjectKey, repositorySlug);
            
            // push the incoming repository into the new SCM repository
            GitFacade.getInstance().addRemote(repoBean, "scm", createdSCMRepository.getCloneSourceURI());
            GitFacade.getInstance().push(repoBean, "scm");

            // create project in the SCM system to hold update forks from this partner if it doesn't already exist
            if (!SCMCommand.getProjects().contains(partnerProjectForkKey)) {
                SCMCommand.createProject(partnerProjectForkKey);
            }
                        
            // fork the new repository to allow updates to pushed into the fork instead of the master copy in future
            LocalRepoBean forkedRepo = SCMCommand.forkRepo(partnerProjectKey, repositorySlug, partnerProjectForkKey);
            
            // update local repository remote to point at the fork instead for its scm remote
            GitFacade.getInstance().updateRemote(repoBean, "scm", forkedRepo.getCloneSourceURI());
        } catch (Exception e) {
            logger.error("Could not import new repository " + repoBean, e);
        }
	}
	
	public void createProjectAndVerifyRepo(String partnerProjectKey, String repositorySlug) throws SCMFederatorServiceException, SCMCallException {
        // create project in the SCM system to hold repositories from this partner if it doesn't already exist
        if (!SCMCommand.getProjects().contains(partnerProjectKey)) {
            SCMCommand.createProject(partnerProjectKey);
        }
        
        // check that a repository doesn't already exists where we are planning on creating one in the SCM system
        if (SCMCommand.getRepository(partnerProjectKey, repositorySlug) != null) {
            throw new SCMFederatorServiceException("Repository " + partnerProjectKey + ":"
                    + repositorySlug + " already exists.");
        }
	}
	
}
