package com.surevine.gateway.scm.scmclient.stash;

import com.surevine.gateway.scm.scmclient.DeleteRepoCommand;
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
public class StashDeleteRepoCommand implements DeleteRepoCommand {
    private static Logger logger = Logger.getLogger(StashCreateProjectCommand.class);
    private static final String RESOURCE = "/rest/api/1.0/projects/%s/repos/%s";
    private SCMSystemProperties scmSystemProperties;

    StashDeleteRepoCommand() {
        scmSystemProperties = PropertyUtil.getSCMSystemProperties();
    }

    @Override
    public void deleteRepo(String projectKey, String repoSlug) throws SCMCallException {
        if (projectKey == null || projectKey.isEmpty()) {
            throw new SCMCallException("deleteProject", "No project key provided");
        } else if (repoSlug == null || repoSlug.isEmpty()) {
            throw new SCMCallException("deleteProject", "No repo slug provided");
        }

        Client client = ClientBuilder.newClient();
        String resource = scmSystemProperties.getHost() + String.format(RESOURCE,projectKey,repoSlug);
        logger.debug("REST call to " + resource);

        client.target(resource)
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", scmSystemProperties.getBasicAuthHeader())
                .delete();
        
        client.close();
        
    }
}
