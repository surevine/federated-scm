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
package com.surevine.gateway.scm.api;

import com.surevine.gateway.scm.service.SCMFederatorServiceException;

/**
 * SCM Federator Service
 * @author nick.leaver@surevine.com
 */
public interface Distributor {
    /**
     * Causes the SCM federator to package a repository and distribute to a specific partner via the Gateway.
     * @param partnerName The name of the partner. Must match a Gateway management endpoint.
     * @param projectKey The project key of the project. Must match the key in the SCM system.
     * @param repositorySlug The repository slug for the shared repository.
     * @throws com.surevine.gateway.scm.service.SCMFederatorServiceException
     */
    void distributeToSingleDestination(final String partnerName, final String projectKey, final String repositorySlug)
            throws SCMFederatorServiceException;

    /**
     * Fire and forget method to package and distribute all repositories configured for sharing 
     * in the gateway.
     * No exceptions are thrown from this method. Any errors with any configured repository will be logged internally.
     */
    void distributeAll();
}
