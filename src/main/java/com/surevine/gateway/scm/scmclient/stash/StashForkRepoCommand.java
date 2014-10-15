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

import com.surevine.gateway.scm.model.RepoBean;
import com.surevine.gateway.scm.scmclient.ForkRepoCommand;
import com.surevine.gateway.scm.scmclient.SCMCallException;
import com.surevine.gateway.scm.util.PropertyUtil;
import com.surevine.gateway.scm.util.SCMSystemProperties;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import java.util.HashMap;

/**
 * @author nick.leaver@surevine.com
 */
public class StashForkRepoCommand implements ForkRepoCommand {
    private static Logger logger = Logger.getLogger(StashForkRepoCommand.class);
    private static final String RESOURCE = "/rest/api/1.0/projects/%s/repos/%s";
    private SCMSystemProperties scmSystemProperties;

    StashForkRepoCommand() {
        scmSystemProperties = PropertyUtil.getSCMSystemProperties();
    }
    
    @Override
    public RepoBean forkRepo(final String projectKey, final String repositorySlug, final String forkProjectKey)
            throws SCMCallException {
        HashMap<String, String> projectMap = new HashMap<String, String>();
        projectMap.put("key", forkProjectKey);
        JSONObject payload = new JSONObject().put("project", projectMap);

        String resource = scmSystemProperties.getHost() + RESOURCE;
        logger.debug("REST call to " + resource);

        Client client = ClientBuilder.newClient();
        StashRepoJSONBean response = client.target(resource)
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", scmSystemProperties.getBasicAuthHeader())
                .post(Entity.json(payload), StashRepoJSONBean.class);

        client.close();
        return response.asRepoBean();
    }
}
