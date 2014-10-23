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
package com.surevine.gateway.scm.gatewayclient;

import com.surevine.gateway.scm.util.PropertyUtil;
import org.apache.log4j.Logger;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.ws.rs.client.Client;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Facade over access to the configured shared repositories in the gateway management component
 * @author nick.leaver@surevine.com
 */
public class GatewayConfigServiceFacade {
    private static final String USE_MOCK_KEY = "fedscm.mock.gatewayconfig";
    private static GatewayConfigServiceFacade instance;
    private static Logger logger = Logger.getLogger(GatewayConfigServiceFacade.class);
    
    protected GatewayConfigServiceFacade() {
        // external instantiation protection
    }
    
    public List<SharedRepoIdentification> getSharedRepositories() {
        String gatewayConfigServiceURL = PropertyUtil.getProjectConfigServiceURL();
        List<SharedRepoIdentification> sharedRepositories = new ArrayList<SharedRepoIdentification>();
        Client client = new ResteasyClientBuilder()
                .establishConnectionTimeout(6, TimeUnit.SECONDS)
                .socketTimeout(6, TimeUnit.SECONDS)
                .build();
        logger.info("Attempting to retrieve project sharing configuration from " + gatewayConfigServiceURL);
        String jsonResponse = client.target(gatewayConfigServiceURL).request().get(String.class);
        if (jsonResponse != null && jsonResponse.length() > 0) {
            JSONArray jsonArray = new JSONArray(jsonResponse);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject projectConfigurationObject = jsonArray.getJSONObject(i);
                String projectKey = projectConfigurationObject.getString("projectKey");
                String repositorySlug = projectConfigurationObject.getString("repositorySlug");
                
                logger.debug("Shared repository configuration loaded: " + projectKey + ":" + repositorySlug);
                sharedRepositories.add(new SharedRepoIdentification(projectKey, repositorySlug));
            }
        }

        return sharedRepositories;
    }
    
    public static GatewayConfigServiceFacade getInstance() {
        if (instance == null) {
            boolean useMock = PropertyUtil.getBooleanProperty(USE_MOCK_KEY);
            instance = (useMock) ? new MockGatewayConfigServiceFacade() : new GatewayConfigServiceFacade();
        }
        return instance;
    }
    
    public static void setInstance(final GatewayConfigServiceFacade newInstance) {
        instance = newInstance;
    }
}
