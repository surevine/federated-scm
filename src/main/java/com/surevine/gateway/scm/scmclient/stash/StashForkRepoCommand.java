/*
 * Copyright (C) 2008-2014 Surevine Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/
package com.surevine.gateway.scm.scmclient.stash;

import com.surevine.gateway.scm.model.LocalRepoBean;
import com.surevine.gateway.scm.scmclient.ForkRepoCommand;
import com.surevine.gateway.scm.scmclient.SCMCallException;
import com.surevine.gateway.scm.util.PropertyUtil;
import com.surevine.gateway.scm.util.SCMSystemProperties;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import java.util.HashMap;

/**
 * @author nick.leaver@surevine.com
 */
public class StashForkRepoCommand extends AbstractStashCommand implements ForkRepoCommand {
    private static final Logger LOGGER = Logger.getLogger(StashForkRepoCommand.class);
    private static final String RESOURCE = "/rest/api/1.0/projects/%s/repos/%s";
    private SCMSystemProperties scmSystemProperties;

    StashForkRepoCommand() {
        scmSystemProperties = PropertyUtil.getSCMSystemProperties();
    }

    @Override
    public LocalRepoBean forkRepo(String projectKey, String repositorySlug, String forkProjectKey)
            throws SCMCallException {
    	projectKey = projectKey.toUpperCase();
        repositorySlug = repositorySlug.toLowerCase();
        forkProjectKey = forkProjectKey.toUpperCase();

        HashMap<String, String> projectMap = new HashMap<String, String>();
        projectMap.put("key", forkProjectKey);
        JSONObject payload = new JSONObject().put("project", projectMap);

        String resource = scmSystemProperties.getHost() + String.format(RESOURCE, projectKey, repositorySlug);
        LOGGER.debug("REST call to " + resource);

        Client client = getClient();
        StashRepoJSONBean response = null;
        try {
            response = client.target(resource)
                    .request(MediaType.APPLICATION_JSON)
                    .header("Authorization", scmSystemProperties.getBasicAuthHeader())
                    .post(Entity.json(payload.toString()), StashRepoJSONBean.class);
        } catch (ProcessingException pe) {
            LOGGER.error("Could not connect to REST service " + resource, pe);
            throw new SCMCallException("forkRepo", "Could not connect to REST service:" + pe.getMessage());
        } finally {
            client.close();
        }

        return response.asRepoBean();
    }
}
