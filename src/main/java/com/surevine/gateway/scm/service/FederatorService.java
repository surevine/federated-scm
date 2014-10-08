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
package com.surevine.gateway.scm.service;

import com.surevine.gateway.scm.service.bean.AcknowledgementBean;

/**
 * SCM Federator Service
 * @author nick.leaver@surevine.com
 */
public interface FederatorService {
    /**
     * Informs the SCM Federator that a repository is being shared with a new partner. Causes the SCM federator to 
     * package the repository and distribute to the new partner via the Gateway.
     * @param partnerName The name of the partner. Must match a Gateway management endpoint.
     * @param projectKey The project key of the project. Must match the key in the SCM system.
     * @param repositorySlug The repository slug for the shared repository.
     */
    void newSharingPartner(String partnerName, String projectKey, String repositorySlug)
            throws SCMFederatorServiceException;

    /**
     * Causes the SCM Federator to redistribute an entire repository to a sharing partner.
     * @param partnerName The name of the partner. Must match a Gateway management endpoint.
     * @param projectKey The project key of the project. Must match the key in the SCM system.
     * @param repositorySlug The repository slug for the shared repository.
     */
    void redistribute(String partnerName, String projectKey, String repositorySlug) throws SCMFederatorServiceException;

    /**
     * Informs the SCM Federator that a project is no longer being shared with a partner.
     * @param partnerName The name of the partner. Must match a Gateway management endpoint.
     * @param projectKey The project key of the project. Must match the key in the SCM system.
     * @param repositorySlug The repository slug for the shared repository.
     */
    void sharingPartnerRemoved(String partnerName, String projectKey, String repositorySlug)
            throws SCMFederatorServiceException;

    /**
     * Causes the SCM Federator to process a received acknowledgement file received from a sharing partner.
     * @param acknowledgement The acknowledgement.
     */
    void processAcknowledgementFile(AcknowledgementBean acknowledgement) throws SCMFederatorServiceException;
}
