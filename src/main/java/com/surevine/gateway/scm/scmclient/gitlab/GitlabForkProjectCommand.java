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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;

import org.apache.log4j.Logger;

import com.surevine.gateway.scm.model.LocalRepoBean;
import com.surevine.gateway.scm.scmclient.ForkRepoCommand;
import com.surevine.gateway.scm.scmclient.SCMCallException;
import com.surevine.gateway.scm.util.PropertyUtil;
import com.surevine.gateway.scm.util.SCMSystemProperties;

/**
 * @author martin.hewitt@surevine.com
 */
public class GitlabForkProjectCommand extends AbstractGitlabCommand implements ForkRepoCommand {

    private static Logger logger = Logger.getLogger(GitlabForkProjectCommand.class);
    private static final String PROJECT_RESOURCE = "/api/v3/projects";
    private static final String GROUP_RESOURCE = "/api/v3/groups";
    private SCMSystemProperties scmSystemProperties;
	
	public GitlabForkProjectCommand() {
        scmSystemProperties = PropertyUtil.getSCMSystemProperties();
	}
	
    @Override
    public LocalRepoBean forkRepo(final String projectKey, final String repositorySlug, final String forkProjectKey) throws SCMCallException {
        // This is going to need to be a two-stage process:
        // 1. Fork the project using POST /projects/fork/:id
        GitlabProjectJSONBean forked = forkProject(projectKey, repositorySlug);
        
        // 2. Transfer the ownership of the project to the group using POST  /groups/:id/projects/:project_id
        changeProjectOwnership(forked, forkProjectKey);
        
        return forked.asRepoBean();
    }

    // Fork the project using POST /projects/fork/:id
    private GitlabProjectJSONBean forkProject(String groupName, String projectName) throws SCMCallException {
        String resource = scmSystemProperties.getHost() + PROJECT_RESOURCE;
        String privateToken = scmSystemProperties.getAuthToken();
        Client client = getClient();

        try {
            resource += "/fork/"+URLEncoder.encode(groupName+"/"+projectName, "UTF-8");
		} catch (UnsupportedEncodingException e) {
            throw new SCMCallException("forkRepo", "Could not build POST URL, UTF-8 encoding not supported");
		}
        
        logger.debug("REST POST call to " + resource);
        
        GitlabProjectJSONBean rtn = null;
        try {
        	rtn = client.target(resource)
        		.queryParam("private_token", privateToken)
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.form(new MultivaluedHashMap()), GitlabProjectJSONBean.class);
        } catch (ProcessingException pe) {
            logger.error("Could not connect to REST service " + resource, pe);
            throw new SCMCallException("createProject", "Could not connect to REST service:" + pe.getMessage());
        } finally {
            client.close();
        }
        
        return rtn;
    }
    
    private void changeProjectOwnership(GitlabProjectJSONBean forked, String forkGroupName) throws SCMCallException {
    	logger.debug(forkGroupName);
    	GitlabGetGroupsCommand getGroup = new GitlabGetGroupsCommand();
    	Integer groupId = getGroup.getProjectsWithIds().get(forkGroupName);
    	
        String resource = scmSystemProperties.getHost() + GROUP_RESOURCE;
        String privateToken = scmSystemProperties.getAuthToken();
        Client client = getClient();

        resource += "/"+groupId+"/projects/"+forked.getId();
        
        logger.debug("REST POST call to " + resource);
        
        String rtn = null;
        try {
        	rtn = client.target(resource)
        		.queryParam("private_token", privateToken)
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.form(new MultivaluedHashMap()), String.class);
        } catch (ProcessingException pe) {
            logger.error("Could not connect to REST service " + resource, pe);
            throw new SCMCallException("createProject", "Could not connect to REST service:" + pe.getMessage());
        } finally {
            client.close();
        }
        logger.debug(rtn);
	}
}
