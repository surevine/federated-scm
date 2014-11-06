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

import java.util.ArrayList;
import java.util.Collection;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;

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
public class GitlabCreateProjectCommand extends AbstractGitlabCommand implements CreateProjectCommand {

    private static Logger logger = Logger.getLogger(GitlabCreateProjectCommand.class);
    private static final String RESOURCE = "/api/v3/groups";
    private SCMSystemProperties scmSystemProperties;

    GitlabCreateProjectCommand() {
        scmSystemProperties = PropertyUtil.getSCMSystemProperties();
    }

    @Override
    public void createProject(final String projectKey) throws SCMCallException {
        String resource = scmSystemProperties.getHost() + RESOURCE;
        String privateToken = scmSystemProperties.getAuthToken();
        Client client = getClient();
        logger.debug("REST call to " + resource);

        GitlabProjectJSONBean projectBean = new GitlabProjectJSONBean();
        projectBean.setName(projectKey);
        projectBean.setPath(projectKey);

        try {
        	client.target(resource)
        		.queryParam("private_token", privateToken)
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.form(projectBean.toMap()), GitlabProjectJSONBean.class);
        } catch (ProcessingException pe) {
            logger.error("Could not connect to REST service " + resource, pe);
            throw new SCMCallException("createProject", "Could not connect to REST service:" + pe.getMessage());
        } finally {
            client.close();
        }
    }
}
