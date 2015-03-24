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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import com.surevine.gateway.scm.scmclient.GetProjectsCommand;
import com.surevine.gateway.scm.scmclient.SCMCallException;
import com.surevine.gateway.scm.util.PropertyUtil;
import com.surevine.gateway.scm.util.SCMSystemProperties;

/**
 * This being Gitlab, what we're actually doing is retrieving
 * groups, not projects. A Gitlab 'group' is a Stash 'project',
 * and a Gitlab 'project' is a Stash 'repo'
 *
 * API documentation for this entity's endpoint is here: http://doc.gitlab.com/ce/api/groups.html
 *
 * @author martin.hewitt@surevine.com
 */
public class GitlabGetGroupsCommand extends AbstractGitlabCommand implements GetProjectsCommand {
	private static final Logger LOGGER = Logger.getLogger(GitlabGetGroupsCommand.class);
	private static final String ALL_RESOURCE = "/api/v3/groups";
	private final SCMSystemProperties scmSystemProperties;

	GitlabGetGroupsCommand() {
		scmSystemProperties = PropertyUtil.getSCMSystemProperties();
	}

	@Override
	public Collection<String> getProjects() throws SCMCallException {
		final List<GitlabGroupJSONBean> projects = getProjectObjects();
		final ArrayList<String> projectKeys = new ArrayList<String>();

		if (projects.size() > 0) {
			for (final GitlabGroupJSONBean projectBean : projects) {
				projectKeys.add(projectBean.getPath().toLowerCase());
			}
		}

		return projectKeys;
	}

	public Map<String, Integer> getProjectsWithIds() throws SCMCallException {
		final Map<String, Integer> rtn = new HashMap<String, Integer>();
		final List<GitlabGroupJSONBean> projects = getProjectObjects();

		if (projects.size() > 0) {
			for (final GitlabGroupJSONBean projectBean : projects) {
				rtn.put(projectBean.getPath(), projectBean.getIdInt());
			}
		}

		return rtn;
	}

	public List<GitlabGroupJSONBean> getProjectObjects() throws SCMCallException {
		final String resource = scmSystemProperties.getHost() + ALL_RESOURCE;
		final String privateToken = scmSystemProperties.getAuthToken();
		final Client client = getClient();
		LOGGER.debug("REST call to " + resource);

		PagedProjectResult response = null;
		try {
			response = client.target(resource).queryParam("private_token", privateToken)
					.request(MediaType.APPLICATION_JSON).get(PagedProjectResult.class);
		} catch (final ProcessingException pe) {
			LOGGER.error("Could not connect to REST service " + resource, pe);
			throw new SCMCallException("getProjects", "Could not connect to REST service:" + pe.getMessage());
		} finally {
			client.close();
		}

		return response;
	}

	/**
	 * Private wrapper for typing response results
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	private static class PagedProjectResult extends ArrayList<GitlabGroupJSONBean> {
	}
}
