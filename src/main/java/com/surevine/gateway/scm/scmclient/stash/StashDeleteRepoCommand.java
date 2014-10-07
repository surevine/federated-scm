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
    public void deleteRepo(final String projectKey, final String repoSlug) throws SCMCallException {
        if (projectKey == null || projectKey.isEmpty()) {
            throw new SCMCallException("deleteProject", "No project key provided");
        } else if (repoSlug == null || repoSlug.isEmpty()) {
            throw new SCMCallException("deleteProject", "No repo slug provided");
        }

        Client client = ClientBuilder.newClient();
        String resource = scmSystemProperties.getHost() + String.format(RESOURCE, projectKey, repoSlug);
        logger.debug("REST call to " + resource);

        client.target(resource)
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", scmSystemProperties.getBasicAuthHeader())
                .delete();

        client.close();
    }
}
