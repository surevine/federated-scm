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

import com.surevine.gateway.scm.model.LocalRepoBean;
import com.surevine.gateway.scm.scmclient.CreateRepoCommand;
import com.surevine.gateway.scm.scmclient.SCMCallException;
import com.surevine.gateway.scm.util.PropertyUtil;
import com.surevine.gateway.scm.util.SCMSystemProperties;

/**
 * This being Gitlab, what we're actually doing is creating a
 * project, not a repo. A Gitlab 'group' is a Stash 'project',
 * and a Gitlab 'project' is a Stash 'repo'
 *
 * API documentation for this entity's endpoint is here: http://doc.gitlab.com/ce/api/projects.html
 *
 * @author martin.hewitt@surevine.com
 */
public class GitlabCreateProjectCommand extends AbstractGitlabCommand implements CreateRepoCommand {

	private static final Logger LOGGER = Logger.getLogger(GitlabCreateProjectCommand.class);
	private static final String RESOURCE = "/api/v3/projects";
	private final SCMSystemProperties scmSystemProperties;

	GitlabCreateProjectCommand() {
		scmSystemProperties = PropertyUtil.getSCMSystemProperties();
	}

	@Override
	public LocalRepoBean createRepo(final String projectKey, final String name) throws SCMCallException {
		if (projectKey == null || projectKey.isEmpty()) {
			throw new SCMCallException("createRepo", "No project key provided");
		}

		final GitlabGetGroupsCommand projectCmd = new GitlabGetGroupsCommand();
		final List<GitlabGroupJSONBean> projects = projectCmd.getProjectObjects();

		GitlabGroupJSONBean project = null;

		for (final GitlabGroupJSONBean projectEntry : projects) {
			if (projectEntry.getName().equals(projectKey)) {
				project = projectEntry;
			}
		}

		if (project == null) {
			throw new SCMCallException("createRepo", "No project found with the key provided");
		}

		return createRepo(project, name);
	}

	public LocalRepoBean createRepo(final GitlabGroupJSONBean project, final String name) throws SCMCallException {
		final String resource = scmSystemProperties.getHost() + RESOURCE;
		final String privateToken = scmSystemProperties.getAuthToken();
		final Client client = getClient();
		LOGGER.debug("REST POST call to " + resource);

		final GitlabProjectJSONBean projectBean = new GitlabProjectJSONBean();
		projectBean.setName(name);
		projectBean.setPath(name);
		projectBean.setNamespaceId(project.getId());

		GitlabProjectJSONBean createdBean = null;
		try {
			createdBean = client.target(resource).queryParam("private_token", privateToken)
					.request(MediaType.APPLICATION_JSON)
					.post(Entity.form(projectBean.toMap()), GitlabProjectJSONBean.class);
		} catch (final ProcessingException pe) {
			LOGGER.error("Could not connect to REST service " + resource, pe);
			throw new SCMCallException("createProject", "Could not connect to REST service:" + pe.getMessage());
		} finally {
			client.close();
		}

		return createdBean.asRepoBean();
	}
}