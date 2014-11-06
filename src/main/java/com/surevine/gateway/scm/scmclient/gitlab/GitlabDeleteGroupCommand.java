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

import java.util.List;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;

import com.surevine.gateway.scm.scmclient.CreateProjectCommand;
import com.surevine.gateway.scm.scmclient.DeleteProjectCommand;
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
public class GitlabDeleteGroupCommand extends AbstractGitlabCommand implements DeleteProjectCommand {

    private static Logger logger = Logger.getLogger(GitlabDeleteGroupCommand.class);
    private static final String RESOURCE = "/api/v3/groups/";
    private SCMSystemProperties scmSystemProperties;

    GitlabDeleteGroupCommand() {
        scmSystemProperties = PropertyUtil.getSCMSystemProperties();
    }

    @Override
    public void deleteProject (final String projectKey) throws SCMCallException {
        if (projectKey == null || projectKey.isEmpty()) {
            throw new SCMCallException("deleteProject", "No project key provided");
        }
        GitlabGetGroupsCommand projectCmd = new GitlabGetGroupsCommand();
        List<GitlabGroupJSONBean> projects = projectCmd.getProjectObjects();
        
        Integer projectId = null;
        
        for ( GitlabGroupJSONBean project : projects ) {
        	if ( project.getName().equals(projectKey) ) {
        		projectId = project.getIdInt();
        	}
        }
        
        if ( projectId == null ) {
        	throw new SCMCallException("deleteProject", "No project found with the key provided");
        }
        
        deleteProject(projectId);
    }

    public void deleteProject (int projectId) throws SCMCallException {
        String resource = scmSystemProperties.getHost() + RESOURCE + projectId;
        
        String privateToken = scmSystemProperties.getAuthToken();
        Client client = getClient();
        logger.debug("REST call to " + resource);

        try {
        	client.target(resource)
        		.queryParam("private_token", privateToken)
                .request(MediaType.APPLICATION_JSON)
                .delete();
        } catch (ProcessingException pe) {
            logger.error("Could not connect to REST service " + resource, pe);
            throw new SCMCallException("createProject", "Could not connect to REST service:" + pe.getMessage());
        } finally {
            client.close();
        }
    }
}
