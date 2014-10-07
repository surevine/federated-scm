package com.surevine.gateway.scm.scmclient.stash;

import com.surevine.gateway.scm.scmclient.CreateRepoCommand;
import com.surevine.gateway.scm.scmclient.SCMCallException;
import com.surevine.gateway.scm.scmclient.bean.RepoBean;
import com.surevine.gateway.scm.util.PropertyUtil;
import com.surevine.gateway.scm.util.SCMSystemProperties;
import org.apache.log4j.Logger;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;

/**
 * @author nick.leaver@surevine.com
 */
public class StashCreateRepoCommand implements CreateRepoCommand {
    private static Logger logger = Logger.getLogger(StashCreateRepoCommand.class);
    private static final String RESOURCE = "/rest/api/1.0/projects/%s/repos";
    private SCMSystemProperties scmSystemProperties;

    StashCreateRepoCommand() {
        scmSystemProperties = PropertyUtil.getSCMSystemProperties();
    }
    
    @Override
    public RepoBean createRepo(String projectKey, String name) throws SCMCallException {
        if (projectKey == null || projectKey.isEmpty()) {
            throw new SCMCallException("createRepo", "No project key was provided");
        } else if (name == null || name.isEmpty()) {
            throw new SCMCallException("createRepo", "No repo name was provided");
        }

        Client client = ClientBuilder.newClient();
        String resource = scmSystemProperties.getHost() + String.format(RESOURCE, projectKey);
        logger.debug("REST call to " + resource);
        
        RepoBean args = new RepoBean();
        args.setName(name);

        RepoBean response = client.target(resource)
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", scmSystemProperties.getBasicAuthHeader())
                .post(Entity.json(args), RepoBean.class);
        
        client.close();

        return response;
    }
}
