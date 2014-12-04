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

import com.surevine.gateway.scm.model.LocalRepoBean;
import com.surevine.gateway.scm.scmclient.GetRepoCommand;
import com.surevine.gateway.scm.scmclient.SCMCallException;
import com.surevine.gateway.scm.util.PropertyUtil;
import com.surevine.gateway.scm.util.SCMSystemProperties;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;


/**
 * This being Gitlab, what we're actually doing is retrieving
 * projects, not repos. A Gitlab 'group' is a Stash 'project',
 * and a Gitlab 'project' is a Stash 'repo'
 * 
 * API documentation for this entity's endpoint is here: http://doc.gitlab.com/ce/api/projects.html
 * 
 * @author martin.hewitt@surevine.com
 */
public class GitlabGetProjectCommand extends AbstractGitlabCommand implements GetRepoCommand {
    private static Logger logger = Logger.getLogger(GitlabGetProjectCommand.class);
    private static final String RESOURCE = "/api/v3/projects";
    private SCMSystemProperties scmSystemProperties;

    GitlabGetProjectCommand() {
        scmSystemProperties = PropertyUtil.getSCMSystemProperties();
    }
	
    @Override
    // 								 getProjects(String namespace) throws SCMCallException {
    public Collection<LocalRepoBean> getRepositories(final String projectKey) throws SCMCallException {
    	List<GitlabProjectJSONBean> projects = getAllProjects();
    	Collection<LocalRepoBean> rtn = new ArrayList<LocalRepoBean>();
    	
    	for ( GitlabProjectJSONBean project : projects ) {
    		if ( project.getNamespacePath().equals(projectKey) ) {
    			rtn.add(project.asRepoBean());
    		}
    	}
    	
        return rtn;
    }

    @Override
    // 					 getProject(String namespace, String projectName) throws SCMCallException {
    public LocalRepoBean getRepository(final String projectKey, final String repositorySlug) throws SCMCallException {
    	GitlabProjectJSONBean rtn = getProject(projectKey, repositorySlug);
    	if ( rtn == null ) {
    		return null;
    	}
    	
    	return rtn.asRepoBean();
    }
    
    public GitlabProjectJSONBean getProject(final String projectKey, final String repositorySlug) throws SCMCallException {
    	
    	if ( projectKey == null || repositorySlug == null ) {
    		throw new SCMCallException("getProject", "Project key and repo slug required");
    	}
    	
    	if ( projectKey.contains("%s")) {
    		throw new SCMCallException("getProject", "Project key has arrived without replacement");
    	}
    	
    	String projectPath = projectKey+"/"+repositorySlug;
    	projectPath = projectPath.toLowerCase();
    	for ( GitlabProjectJSONBean bean : getAllProjects() ) {
    		if ( bean.getPathWithNamespace().equals(projectPath) ) {
    			return bean;
    		}
    	}
    	
    	return null;
    }

    @Override
    // 					 						  getAllProjects() throws SCMCallException {
    public Map<String, Collection<LocalRepoBean>> getAllRepositories() throws SCMCallException {
    	Map<String, Collection<LocalRepoBean>> rtn = new HashMap<String, Collection<LocalRepoBean>>();
    	
    	LocalRepoBean thisBean;
    	String thisNamespace;
    	for ( GitlabProjectJSONBean project : getAllProjects() ) {
    		thisBean = project.asRepoBean();
    		thisNamespace = project.getNamespacePath();
    		if ( !rtn.containsKey(thisNamespace) ) {
    			rtn.put(thisNamespace, new ArrayList<LocalRepoBean>());
    		}
    		
			rtn.get(thisNamespace).add(thisBean);
    	}
    	
    	return rtn;
    }
    
    private List<GitlabProjectJSONBean> getAllProjects() throws SCMCallException {
        String resource = scmSystemProperties.getHost() + RESOURCE;
        String privateToken = scmSystemProperties.getAuthToken();
        Client client = getClient();
        logger.debug("REST GET call to " + resource);

        PagedProjectResult response = null;
        try {
            response = client.target(resource)
            		.queryParam("private_token", privateToken)
                    .request(MediaType.APPLICATION_JSON)
                    .get(PagedProjectResult.class);
        } catch (ProcessingException pe) {
            logger.error("Could not connect to REST service " + resource, pe);
            throw new SCMCallException("getAllProjects", "Could not connect to REST service:" + pe.getMessage());
        } finally {
            client.close();
        }
        
        return response;
    }

    /**
     * Private wrapper for typing response results
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class PagedProjectResult
    	extends ArrayList<GitlabProjectJSONBean> {
    }
}
