package com.surevine.gateway.scm.scmclient.gitlab;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;

import com.surevine.gateway.scm.model.LocalRepoBean;
import com.surevine.gateway.scm.scmclient.DeleteRepoCommand;
import com.surevine.gateway.scm.scmclient.SCMCallException;
import com.surevine.gateway.scm.util.PropertyUtil;
import com.surevine.gateway.scm.util.SCMSystemProperties;

public class GitlabDeleteProjectCommand extends AbstractGitlabCommand implements DeleteRepoCommand {

    private static final Logger LOGGER = Logger.getLogger(GitlabDeleteProjectCommand.class);
    private static final String RESOURCE = "/api/v3/projects/";
    private SCMSystemProperties scmSystemProperties;

    GitlabDeleteProjectCommand() {
        scmSystemProperties = PropertyUtil.getSCMSystemProperties();
    }

	public void deleteRepo(String projectKey, String repoSlug) throws SCMCallException {

        String resource = scmSystemProperties.getHost() + RESOURCE;
        try {
			resource += URLEncoder.encode(projectKey+"/"+repoSlug, "UTF-8");
		} catch (UnsupportedEncodingException e) {
            throw new SCMCallException("deleteRepo", "Could not build DELETE URL, UTF-8 encoding not supported");
		}

        String privateToken = scmSystemProperties.getAuthToken();
        Client client = getClient();
        LOGGER.debug("REST call to " + resource);

        GitlabProjectJSONBean createdBean = null;
        try {
        	client.target(resource)
        		.queryParam("private_token", privateToken)
                .request(MediaType.APPLICATION_JSON)
                .delete();
        } catch (ProcessingException pe) {
            LOGGER.error("Could not connect to REST service " + resource, pe);
            throw new SCMCallException("deleteRepo", "Could not connect to REST service:" + pe.getMessage());
        } finally {
            client.close();
        }
	}
}
