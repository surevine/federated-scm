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

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author nick.leaver@surevine.com
 */
public class MockFileWriterGatewayClient extends GatewayClient {
    private static final Logger LOGGER = Logger.getLogger(MockFileWriterGatewayClient.class);
    private Path tmpOutputDir;

    protected MockFileWriterGatewayClient() {
        tmpOutputDir = Paths.get(PropertyUtil.getTempDir(), "gateway_mock_outputs");
        try {
            Files.createDirectories(tmpOutputDir);
        } catch (Exception e) {
            // drop exception - mock class
        }
    }

    @Override
    public void sendToGateway(final GatewayPackage gatewayPackage) {
        Path archivePath = gatewayPackage.getArchive();
        try {
            Files.copy(archivePath, tmpOutputDir.resolve(archivePath.getFileName().toString()));
            LOGGER.info("Copied distributable " + archivePath.getFileName() + " to " + tmpOutputDir);
        } catch (Exception e) {
            // drop exception - mock class
        }
    }
}
