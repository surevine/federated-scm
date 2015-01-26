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
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataOutput;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.TimeUnit;

/**
 * @author nick.leaver@surevine.com
 */
public class GatewayClient {
    private static Logger logger = Logger.getLogger(GatewayClient.class);
    private static final String USE_MOCK_KEY = "fedscm.mock.gatewayclient";
    private static GatewayClient instance;

    protected GatewayClient() {
        // no-op
    }

    public void sendToGateway(final GatewayPackage gatewayPackage) {
        String gatewayServiceURL = PropertyUtil.getGatewayURL();
        Client client = new ResteasyClientBuilder()
                .establishConnectionTimeout(30, TimeUnit.SECONDS)
                .socketTimeout(30, TimeUnit.SECONDS)
                .build();
        try {
            String filename = gatewayPackage.getMetadata().get(MetadataUtil.KEY_FILENAME);
            MultipartFormDataOutput mfdo = new MultipartFormDataOutput();
            mfdo.addFormData("filename", filename, MediaType.TEXT_PLAIN_TYPE);
            mfdo.addFormData("file", Files.readAllBytes(gatewayPackage.getArchive()), MediaType.APPLICATION_OCTET_STREAM_TYPE);
            GenericEntity<MultipartFormDataOutput> entity = new GenericEntity<MultipartFormDataOutput>(mfdo) {
            };
            client.target(gatewayServiceURL).request().post(Entity.entity(entity, MediaType.MULTIPART_FORM_DATA_TYPE));
            logger.info(gatewayPackage.getArchive().getFileName() + " sent to the gateway as " + filename);
        } catch (IOException e) {
            logger.error("Failed to send " + gatewayPackage.getArchive().toString() + " to the gateway", e);
        }
    }

    public static GatewayClient getInstance() {
        if (instance == null) {
            boolean useMock;
            try {
            	useMock = PropertyUtil.getBooleanProperty(USE_MOCK_KEY);
            } catch ( Exception e ) {
            	useMock = false;
            }
            instance = (useMock) ? new MockFileWriterGatewayClient() : new GatewayClient();
        }
        return instance;
    }
}
