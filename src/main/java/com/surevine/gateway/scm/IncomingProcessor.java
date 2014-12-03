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
package com.surevine.gateway.scm;

import com.surevine.gateway.scm.service.SCMFederatorServiceException;

import java.nio.file.Path;
import java.util.Map;

/**
 * Processed incoming files
 * @author nick.leaver@surevine.com
 */
public interface IncomingProcessor {
    /**
     * Causes the SCM federator to process an incoming SCM update file
     * @param archivePath the path to the received file
     * @throws com.surevine.gateway.scm.service.SCMFederatorServiceException
     */
    void processIncomingRepository(Path archivePath) throws SCMFederatorServiceException;
    
    /**
     * Causes the SCM federator to process an incoming SCM update file
     * @param bundlePath the path to the received file
     * @param metadata A map of parsed/extracted metadata, contents of .metadata.json from the gateway bundle
     * @throws com.surevine.gateway.scm.service.SCMFederatorServiceException
     */
    void processIncomingRepository(Path bundlePath, Map<String, String> metadata) throws SCMFederatorServiceException;
}
