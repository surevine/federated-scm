package com.surevine.gateway.scm.scmclient.stash;

import java.util.HashMap;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;

import com.surevine.gateway.scm.model.LocalRepoBean;
import com.surevine.gateway.scm.scmclient.CreateMergeRequestCommand;
import com.surevine.gateway.scm.scmclient.SCMCallException;
import com.surevine.gateway.scm.util.PropertyUtil;
import com.surevine.gateway.scm.util.SCMSystemProperties;

public class StashCreateMergeRequestCommand extends AbstractStashCommand implements CreateMergeRequestCommand {

    private static final Logger LOGGER = Logger.getLogger(StashCreateMergeRequestCommand.class);
    private static final String RESOURCE = "/rest/api/1.0/projects/";
    private SCMSystemProperties scmSystemProperties;

    StashCreateMergeRequestCommand() {
        scmSystemProperties = PropertyUtil.getSCMSystemProperties();
    }

	@Override
	public void createMergeRequest(LocalRepoBean from, LocalRepoBean to) throws SCMCallException {
		StashMergeRequestJSONBean mrBean = new StashMergeRequestJSONBean();
		mrBean.setTitle("Automated Gateway merge request");

		HashMap<String, Object> fromRef = refFromRepoBean(from);
		mrBean.setFromRef(fromRef);

		HashMap<String, Object> toRef = refFromRepoBean(to);
		mrBean.setToRef(toRef);

	    ///rest/api/1.0/projects/{projectKey}/repos/{repositorySlug}/pull-requests
	    String resource = scmSystemProperties.getHost()+
	    		RESOURCE+
	    		to.getProjectKey().toUpperCase()+
	    		"/repos/"+
	    		to.getSlug()+
	    		"/pull-requests";

        Client client = getClient();

        LOGGER.debug("REST POST call to " + resource);
        try {
        	client.target(resource)
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", scmSystemProperties.getBasicAuthHeader())
                .post(Entity.json(mrBean), String.class);
        } catch ( ClientErrorException e ) {
            LOGGER.error("Client error: "+e.getMessage());
        } catch (ProcessingException pe) {
            LOGGER.error("Could not connect to REST service " + resource, pe);
            throw new SCMCallException("createMergeRequest", "Could not connect to REST service:" + pe.getMessage());
        } finally {
            client.close();
        }
	}

	private HashMap<String, Object> refFromRepoBean(LocalRepoBean from) {

		HashMap<String, Object> rtn = new HashMap<String, Object>();
		rtn.put("id", "refs/heads/master");

		HashMap<String, Object> fromRepo = new HashMap<String, Object>();
		fromRepo.put("slug", from.getSlug());
		fromRepo.put("name", null);

		HashMap<String, Object> fromProject = new HashMap<String, Object>();
		fromProject.put("key", from.getProjectKey());

		fromRepo.put("project", fromProject);
		rtn.put("repository", fromRepo);

		return rtn;
	}

	/*
	{
	    "title": "Talking Nerdy",
	    "description": "It’s a kludge, but put the tuple from the database in the cache.",
	    "state": "OPEN",
	    "open": true,
	    "closed": false,
	    "fromRef": {
	        "id": "refs/heads/feature-ABC-123",
	        "repository": {
	            "slug": "my-repo",
	            "name": null,
	            "project": {
	                "key": "PRJ"
	            }
	        }
	    },
	    "toRef": {
	        "id": "refs/heads/master",
	        "repository": {
	            "slug": "my-repo",
	            "name": null,
	            "project": {
	                "key": "PRJ"
	            }
	        }
	    },
	    "locked": false,
	    "reviewers": [
	        {
	            "user": {
	                "name": "charlie"
	            }
	        }
	    ]
	}
	*/

}