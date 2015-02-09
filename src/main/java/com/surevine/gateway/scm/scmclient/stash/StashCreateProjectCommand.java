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

import com.surevine.gateway.scm.scmclient.CreateProjectCommand;
import com.surevine.gateway.scm.scmclient.SCMCallException;
import com.surevine.gateway.scm.util.PropertyUtil;
import com.surevine.gateway.scm.util.SCMSystemProperties;
import org.apache.log4j.Logger;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;

/**
 * @author nick.leaver@surevine.com
 */
public class StashCreateProjectCommand extends AbstractStashCommand implements CreateProjectCommand {
    private static final Logger LOGGER = Logger.getLogger(StashCreateProjectCommand.class);
    private static final String RESOURCE = "/rest/api/1.0/projects/";
    private SCMSystemProperties scmSystemProperties;

    StashCreateProjectCommand() {
        scmSystemProperties = PropertyUtil.getSCMSystemProperties();
    }

    @Override
    public void createProject(String projectKey) throws SCMCallException {
        Client client = getClient();
        StashProjectJSONBean projectBean = new StashProjectJSONBean();
        projectBean.setName(projectKey);
        projectBean.setKey(projectKey.toUpperCase());
        projectBean.setDescription(projectKey);

        String resource = scmSystemProperties.getHost() + RESOURCE;
        LOGGER.debug("REST call to " + resource);

        try {
        client.target(resource)
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", scmSystemProperties.getBasicAuthHeader())
                .post(Entity.json(projectBean), StashProjectJSONBean.class);
        } catch (ProcessingException pe) {
            LOGGER.error("Could not connect to REST service " + resource, pe);
            throw new SCMCallException("createProject", "Could not connect to REST service:" + pe.getMessage());
        } finally {
            client.close();
        }
    }
}
