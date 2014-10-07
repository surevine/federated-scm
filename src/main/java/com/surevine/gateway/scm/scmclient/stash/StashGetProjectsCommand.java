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

import com.surevine.gateway.scm.scmclient.GetProjectsCommand;
import com.surevine.gateway.scm.scmclient.bean.ProjectBean;
import com.surevine.gateway.scm.util.PropertyUtil;
import com.surevine.gateway.scm.util.SCMSystemProperties;
import org.apache.log4j.Logger;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import java.util.Collection;
import java.util.List;

/**
 * @author nick.leaver@surevine.com
 */
public class StashGetProjectsCommand implements GetProjectsCommand {
    private static Logger logger = Logger.getLogger(StashGetProjectsCommand.class);
    private static final String ALL_RESOURCE = "/rest/api/1.0/projects?limit=10000";
    private static final String SINGLE_RESOURCE = "/rest/api/1.0/projects/";
    private SCMSystemProperties scmSystemProperties;

    StashGetProjectsCommand() {
        scmSystemProperties = PropertyUtil.getSCMSystemProperties();
    }

    @Override
    public Collection<ProjectBean> getProjects() {
        String resource = scmSystemProperties.getHost() + ALL_RESOURCE;
        Client client = ClientBuilder.newClient();
        logger.debug("REST call to " + resource);

        PagedProjectResult response = client.target(resource)
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", scmSystemProperties.getBasicAuthHeader())
                .get(PagedProjectResult.class);
        
        client.close();
               
        return response.getValues();
    }

    @Override
    public ProjectBean getProject(final String projectKey) {
        String resource = scmSystemProperties.getHost() + SINGLE_RESOURCE + projectKey;
        Client client = ClientBuilder.newClient();
        logger.debug("REST call to " + resource);
        
        ProjectBean response = null;
        
        try {
            response = client.target(resource)
                    .request(MediaType.APPLICATION_JSON)
                    .header("Authorization", scmSystemProperties.getBasicAuthHeader())
                    .get(ProjectBean.class);
        } catch (NotFoundException nfe) {
            // no-op - acceptable response and will return a null object
        }
        
        client.close();
        
        return response;
    }

    /**
     * Private wrapper for holding paging results
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class PagedProjectResult {
        private List<ProjectBean> values;

        public List<ProjectBean> getValues() {
            return values;
        }

        public void setValues(final List<ProjectBean> values) {
            this.values = values;
        }
    }
}
