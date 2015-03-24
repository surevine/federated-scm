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

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

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
 * @author nick.leaver@surevine.com
 */
public class StashGetProjectsCommand extends AbstractStashCommand implements GetProjectsCommand {
	private static final Logger LOGGER = Logger.getLogger(StashGetProjectsCommand.class);
	private static final String ALL_RESOURCE = "/rest/api/1.0/projects?limit=10000";
	private final SCMSystemProperties scmSystemProperties;

	StashGetProjectsCommand() {
		scmSystemProperties = PropertyUtil.getSCMSystemProperties();
	}

	@Override
	public Collection<String> getProjects() throws SCMCallException {
		final HashSet<String> projectKeys = new HashSet<String>();
		final String resource = scmSystemProperties.getHost() + ALL_RESOURCE;
		final Client client = getClient();
		LOGGER.debug("REST call to " + resource);

		PagedProjectResult response = null;
		try {
			response = client.target(resource).request(MediaType.APPLICATION_JSON)
					.header("Authorization", scmSystemProperties.getBasicAuthHeader()).get(PagedProjectResult.class);
		} catch (final ProcessingException pe) {
			LOGGER.error("Could not connect to REST service " + resource, pe);
			throw new SCMCallException("getProjects", "Could not connect to REST service:" + pe.getMessage());
		} finally {
			client.close();
		}

		for (final StashProjectJSONBean stashProjectJSONBean : response.getValues()) {
			projectKeys.add(stashProjectJSONBean.getKey());
		}

		return projectKeys;
	}

	@Override
	public boolean projectExists(final String project) throws SCMCallException {
		final Collection<String> projects = getProjects();
		for (final String loadedProject : projects) {
			if (loadedProject.equalsIgnoreCase(project)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Private wrapper for holding paging results
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	private static class PagedProjectResult {
		private List<StashProjectJSONBean> values;

		public List<StashProjectJSONBean> getValues() {
			return values;
		}

		public void setValues(final List<StashProjectJSONBean> values) {
			this.values = values;
		}
	}
}
