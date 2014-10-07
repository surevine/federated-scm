package com.surevine.gateway.scm.scmclient.stash;

import com.surevine.gateway.scm.scmclient.DeleteProjectCommand;
import com.surevine.gateway.scm.scmclient.SCMCallException;
import com.surevine.gateway.scm.util.PropertyUtil;
import com.surevine.gateway.scm.util.SCMSystemProperties;
import org.apache.log4j.Logger;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;

/**
 * @author nick.leaver@surevine.com
 */
public class StashDeleteProjectCommand implements DeleteProjectCommand {
    private static Logger logger = Logger.getLogger(StashCreateProjectCommand.class);
    private static final String RESOURCE = "/rest/api/1.0/projects/";
    private SCMSystemProperties scmSystemProperties;
    private Client client;

    StashDeleteProjectCommand() {
        scmSystemProperties = PropertyUtil.getSCMSystemProperties();
        client = ClientBuilder.newClient();
    }

    @Override
    public void deleteProject(String projectKey) throws SCMCallException {
        if (projectKey == null || projectKey.isEmpty()) {
            throw new SCMCallException("deleteProject", "No project key provided");
        }
        
        String resource = scmSystemProperties.getHost() + RESOURCE + projectKey;
        logger.debug("REST call to " + resource);

        client.target(resource)
            .request(MediaType.APPLICATION_JSON)
            .header("Authorization", scmSystemProperties.getBasicAuthHeader())
            .delete();
    }
}
