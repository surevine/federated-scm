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

import com.surevine.gateway.scm.model.LocalRepoBean;
import com.surevine.gateway.scm.scmclient.GetRepoCommand;
import com.surevine.gateway.scm.scmclient.SCMCallException;
import com.surevine.gateway.scm.util.PropertyUtil;
import com.surevine.gateway.scm.util.SCMSystemProperties;
import org.apache.log4j.Logger;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.MediaType;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * @author nick.leaver@surevine.com
 */
public class StashGetRepoCommand extends AbstractStashCommand implements GetRepoCommand {
    private static final Logger LOGGER = Logger.getLogger(StashGetRepoCommand.class);
    private static final String ALL_RESOURCE = "/rest/api/1.0/projects/%s/repos?limit=10000";
    private static final String SINGLE_RESOURCE = "/rest/api/1.0/projects/%s/repos/%s";
    private SCMSystemProperties scmSystemProperties;

    StashGetRepoCommand() {
        scmSystemProperties = PropertyUtil.getSCMSystemProperties();
    }

    @Override
    public Collection<LocalRepoBean> getRepositories(String projectKey) throws SCMCallException {
    	projectKey = projectKey.toUpperCase();
        HashSet<LocalRepoBean> repositories = new HashSet<LocalRepoBean>();

        Client client = getClient();
        String resource = scmSystemProperties.getHost() + String.format(ALL_RESOURCE, projectKey);
        LOGGER.debug("REST call to " + resource);

        PagedRepoResult response = null;
        try {
            response = client.target(resource)
                    .request(MediaType.APPLICATION_JSON)
                    .header("Authorization", scmSystemProperties.getBasicAuthHeader())
                    .get(PagedRepoResult.class);
        } catch (ProcessingException pe) {
            LOGGER.error("Could not connect to REST service " + resource, pe);
            throw new SCMCallException("createRepo", "Could not connect to REST service:" + pe.getMessage());
        } finally {
            client.close();
        }

        for (StashRepoJSONBean stashRepoJSONBean:response.getValues()) {
            repositories.add(stashRepoJSONBean.asRepoBean());
        }

        return repositories;
    }

    @Override
    public LocalRepoBean getRepository(String projectKey, String repositorySlug) throws SCMCallException {
    	projectKey = projectKey.toUpperCase();
    	repositorySlug = repositorySlug.toLowerCase();
        Client client = getClient();
        String resource = scmSystemProperties.getHost() + String.format(SINGLE_RESOURCE, projectKey, repositorySlug);
        LOGGER.debug("REST call to " + resource);

        StashRepoJSONBean response = null;

        try {
            response = client.target(resource)
                    .request(MediaType.APPLICATION_JSON)
                    .header("Authorization", scmSystemProperties.getBasicAuthHeader())
                    .get(StashRepoJSONBean.class);
        } catch (NotFoundException nfe) {
            // no-op - acceptable response and will return a null object
        } catch (ProcessingException pe) {
            LOGGER.error("Could not connect to REST service " + resource, pe);
            throw new SCMCallException("createRepo", "Could not connect to REST service:" + pe.getMessage());
        } finally {
            client.close();
        }

        return (response != null) ? response.asRepoBean() : null;
    }

    @Override
    public Map<String, Collection<LocalRepoBean>> getAllRepositories() throws SCMCallException {
        Map<String, Collection<LocalRepoBean>> repositories = new HashMap<String, Collection<LocalRepoBean>>();
        LOGGER.debug("Getting all repositories from Stash");
        int pCount = 0;
        int rCount = 0;

        StashGetProjectsCommand getProjectsCommand = new StashGetProjectsCommand();
        Collection<String> projects = getProjectsCommand.getProjects();
        if (projects.size() > 0) {
            pCount = projects.size();
        }

        for (String projectKey:projects) {
            Collection<LocalRepoBean> repos = getRepositories(projectKey);
            if (repos.size() > 0) {
                repositories.put(projectKey, repos);
                rCount += repos.size();
            }
        }

        LOGGER.debug("Retrieved " + rCount + " repositories from " + pCount + " projects");
        return repositories;
    }

    /**
     * Private wrapper for holding paging results
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class PagedRepoResult {
        private List<StashRepoJSONBean> values;

        public List<StashRepoJSONBean> getValues() {
            return values;
        }

        public void setValues(final List<StashRepoJSONBean> values) {
            this.values = values;
        }
    }
}
