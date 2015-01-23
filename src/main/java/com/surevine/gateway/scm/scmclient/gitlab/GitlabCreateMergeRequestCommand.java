package com.surevine.gateway.scm.scmclient.gitlab;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.StatusType;

import org.apache.log4j.Logger;
import org.jboss.resteasy.specimpl.MultivaluedMapImpl;

import com.surevine.gateway.scm.model.LocalRepoBean;
import com.surevine.gateway.scm.scmclient.CreateMergeRequestCommand;
import com.surevine.gateway.scm.scmclient.SCMCallException;
import com.surevine.gateway.scm.util.PropertyUtil;
import com.surevine.gateway.scm.util.SCMSystemProperties;

public class GitlabCreateMergeRequestCommand extends AbstractGitlabCommand implements CreateMergeRequestCommand {

    private static Logger logger = Logger.getLogger(GitlabCreateMergeRequestCommand.class);
    private static final String PROJECT_RESOURCE = "/api/v3/projects";
    private SCMSystemProperties scmSystemProperties;
	
	public GitlabCreateMergeRequestCommand() {
        scmSystemProperties = PropertyUtil.getSCMSystemProperties();
	}
	
	
	public void createMergeRequest(LocalRepoBean from, LocalRepoBean to ) throws SCMCallException{
        String resource = scmSystemProperties.getHost() + PROJECT_RESOURCE;
        String privateToken = scmSystemProperties.getAuthToken();
        Client client = getClient();
        
        GitlabGetProjectCommand getProject = new GitlabGetProjectCommand();
        
        GitlabProjectJSONBean srcProject = getProject.getProject(from.getProjectKey(), from.getSlug());
        GitlabProjectJSONBean destProject = getProject.getProject(to.getProjectKey(), to.getSlug());
        
        if (srcProject == null || destProject == null) {
        	throw new SCMCallException("createMergeRequest", "Missing one or other project");
        }
        
//        #   id (required)            - The ID of a project - this will be the source of the merge request
//        #   source_branch (required) - The source branch
//        #   target_branch (required) - The target branch
//        #   target_project           - The target project of the merge request defaults to the :id of the project
//        #   assignee_id              - Assignee user ID
//        #   title (required)         - Title of MR
        
        resource += "/"+srcProject.getId()+"/merge_requests";
        
        MultivaluedMap<String, String> payload = new MultivaluedMapImpl<String, String>();
        payload.putSingle("id", srcProject.getId());
        payload.putSingle("source_branch", "master");
        payload.putSingle("target_branch", "master");
        payload.putSingle("target_project_id", destProject.getId());
        payload.putSingle("title", "Automated Gateway merge request");
        
        logger.debug("REST POST call to " + resource);
        logger.debug(payload.toString());
        
        String rtn = null;
        try {
        	rtn = client.target(resource)
        		.queryParam("private_token", privateToken)
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.form(payload), String.class);
        	logger.debug(rtn);
        } catch (ClientErrorException e ) {
        	logger.error("Exception when connecting to rest service: "+e.getMessage());
        	Response response = e.getResponse();
        	if ( response != null ) {
	        	logger.error(response.getEntity().toString());
	        	StatusType t = response.getStatusInfo();
	        	if ( t != null ) {
	        		logger.error(t.getStatusCode()+": "+t.getReasonPhrase());
	        	}
        	}
            throw new SCMCallException("createProject", "Error received from REST service: " + e.getMessage());
        } catch (ProcessingException pe) {
            logger.error("Could not connect to REST service " + resource, pe);
            throw new SCMCallException("createProject", "Could not connect to REST service: " + pe.getMessage());
        } finally {
            client.close();
        }
	}
}
