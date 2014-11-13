package com.surevine.gateway.scm.scmclient.gitlab;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;

import com.surevine.gateway.scm.scmclient.SCMCallException;
import com.surevine.gateway.scm.util.PropertyUtil;
import com.surevine.gateway.scm.util.SCMSystemProperties;

public class GitlabGetUserCommand extends AbstractGitlabCommand {

    private static Logger logger = Logger.getLogger(GitlabGetUserCommand.class);
    private static final String RESOURCE = "/api/v3/user";
    private SCMSystemProperties scmSystemProperties;
    
	GitlabGetUserCommand() {
        scmSystemProperties = PropertyUtil.getSCMSystemProperties();
	}
	
	public GitlabUserJSONBean getAuthorizedUser() throws SCMCallException {

        String resource = scmSystemProperties.getHost() + RESOURCE;
        String privateToken = scmSystemProperties.getAuthToken();
        Client client = getClient();
        logger.debug("REST GET call to " + resource);
        
        
        GitlabUserJSONBean user = null;

        try {
        	user = client.target(resource)
        		.queryParam("private_token", privateToken)
                .request(MediaType.APPLICATION_JSON)
                .get(GitlabUserJSONBean.class);
        } catch (ProcessingException pe) {
            logger.error("Could not connect to REST service " + resource, pe);
            throw new SCMCallException("createProject", "Could not get logged in user:" + pe.getMessage());
        } finally {
            client.close();
        }
        
        return user;
	}

}
