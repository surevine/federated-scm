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
import com.surevine.gateway.scm.model.ProjectBean;
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
public class StashCreateProjectCommand implements CreateProjectCommand {
    private static Logger logger = Logger.getLogger(StashCreateProjectCommand.class);
    private static final String RESOURCE = "/rest/api/1.0/projects/";
    private SCMSystemProperties scmSystemProperties;

    StashCreateProjectCommand() {
        scmSystemProperties = PropertyUtil.getSCMSystemProperties();
    }

    @Override
    public ProjectBean createProject(final ProjectBean projectBean) throws SCMCallException {
        validate(projectBean);
        Client client = ClientBuilder.newClient();

        String resource = scmSystemProperties.getHost() + RESOURCE;
        logger.debug("REST call to " + resource);

        ProjectBean response = client.target(resource)
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", scmSystemProperties.getBasicAuthHeader())
                .post(Entity.json(projectBean), ProjectBean.class);

        client.close();

        return response;
    }

    private void validate(final ProjectBean projectBean) throws SCMCallException {
        if (projectBean.getName() == null || projectBean.getName().isEmpty()) {
            throw new SCMCallException("createProject", "Supplied project bean did not have a name");
        } else if (projectBean.getKey() == null || projectBean.getKey().isEmpty()) {
            throw new SCMCallException("createProject", "Supplied project bean did not have a name");
        } else if (projectBean.getId() != null && !projectBean.getId().isEmpty()) {
            throw new SCMCallException("createProject", "Supplied project bean already had an ID set");
        }
    }
}
