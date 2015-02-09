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
package com.surevine.gateway.scm.scmclient.gitlab;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import org.apache.log4j.Logger;
import org.jboss.resteasy.specimpl.MultivaluedMapImpl;

import com.surevine.gateway.scm.scmclient.CreateProjectCommand;
import com.surevine.gateway.scm.scmclient.SCMCallException;
import com.surevine.gateway.scm.util.PropertyUtil;
import com.surevine.gateway.scm.util.SCMSystemProperties;

/**
 * This being Gitlab, what we're actually doing is creating a
 * group, not a project. A Gitlab 'group' is a Stash 'project',
 * and a Gitlab 'project' is a Stash 'repo'
 *
 * API documentation for this entity's endpoint is here: http://doc.gitlab.com/ce/api/groups.html
 *
 * @author martin.hewitt@surevine.com
 */
public class GitlabCreateGroupCommand extends AbstractGitlabCommand implements CreateProjectCommand {

    private static final Logger LOGGER = Logger.getLogger(GitlabCreateGroupCommand.class);
    private static final String RESOURCE = "/api/v3/groups";
    private static final String USER_RESOURCE = "/api/v3/user";
    private SCMSystemProperties scmSystemProperties;

    GitlabCreateGroupCommand() {
        scmSystemProperties = PropertyUtil.getSCMSystemProperties();
    }

    @Override
    public void createProject(final String projectKey) throws SCMCallException {
    	GitlabGroupJSONBean projectBean = createGroup(projectKey);
    	addUserToGroup(projectBean);
    }

    private GitlabGroupJSONBean createGroup(String projectKey) throws SCMCallException {
        String resource = scmSystemProperties.getHost() + RESOURCE;
        String privateToken = scmSystemProperties.getAuthToken();
        Client client = getClient();
        LOGGER.debug("REST POST call to " + resource);

        projectKey = projectKey.toLowerCase();

        GitlabGroupJSONBean projectBean = new GitlabGroupJSONBean();
        projectBean.setName(projectKey);
        projectBean.setPath(projectKey);

        try {
        	projectBean = client.target(resource)
        		.queryParam("private_token", privateToken)
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.form(projectBean.toMap()), GitlabGroupJSONBean.class);
        } catch (ProcessingException pe) {
            LOGGER.error("Could not connect to REST service " + resource, pe);
            throw new SCMCallException("createProject", "Could not connect to REST service:" + pe.getMessage());
        } finally {
            client.close();
        }

        return projectBean;
    }

    private void addUserToGroup(GitlabGroupJSONBean projectBean) throws SCMCallException {
        String resource = scmSystemProperties.getHost() + RESOURCE+"/"+projectBean.getId()+"/members";
        String privateToken = scmSystemProperties.getAuthToken();
        Client client = getClient();
        LOGGER.debug("REST GET call to " + resource);

        GitlabGetUserCommand getUser = new GitlabGetUserCommand();
        GitlabUserJSONBean user = getUser.getAuthorizedUser();

        MultivaluedMap<String, String> data = new MultivaluedMapImpl<String, String>();
        data.putSingle("id", projectBean.getId());
        data.putSingle("user_id", user.getId());
        data.putSingle("access_level", "50");

        try {
        	client.target(resource)
        		.queryParam("private_token", privateToken)
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.form(data));
        } catch (ProcessingException pe) {
            LOGGER.error("Could not connect to REST service " + resource, pe);
            throw new SCMCallException("createProject", "Could not add user to project:" + pe.getMessage());
        } finally {
            client.close();
        }
    }
}
