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

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * @author nick.leaver@surevine.com
 */
public class MockGatewayConfigServiceFacade extends GatewayConfigServiceFacade {

    private static final Logger LOGGER = Logger.getLogger(MockGatewayConfigServiceFacade.class);

    @Override
    public List<SharedRepoIdentification> getSharedRepositories() {
        List<SharedRepoIdentification> sharedRepositories = new ArrayList<SharedRepoIdentification>();

        try {
            InputStream mockConfigIS = getClass().getClassLoader().getResourceAsStream("mock_project_config.json");
            JSONObject jsonObject = new JSONObject(IOUtils.toString(mockConfigIS, Charset.forName("UTF-8")));
            for (Object key:jsonObject.keySet()) {
                String project = key.toString();
                JSONArray repos = jsonObject.getJSONArray(project);
                for (int i = 0; i < repos.length(); i++) {
                    String repo = repos.getString(i);
                    LOGGER.debug("Shared repository configuration loaded: " + project + ":" + repo);
                    sharedRepositories.add(new SharedRepoIdentification(project, repo));
                }
            }
        } catch (IOException e) {
            LOGGER.error("Could not read the mock gateway config", e);
        }
        return sharedRepositories;
    }
}
