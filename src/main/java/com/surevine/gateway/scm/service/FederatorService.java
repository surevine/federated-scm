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
     * Informs the SCM Federator that a project is being shared with a new partner. Causes the SCM federator to package
     * the project repository and distribute to the new partner via the Gateway.
     * @param partnerName The name of the partner. Must match a Gateway management endpoint.
     * @param projectName The name of the project. Must match the name in the SCM system.
     */
    void newSharingPartner(String partnerName, String projectName);

    /**
     * Causes the SCM Federator to redistribute an entire repository to a sharing partner.
     * @param partnerName The name of the partner. Must match a Gateway management endpoint.
     * @param projectName The name of the project. Must match the name in the SCM system.
     */
    void redistribute(String partnerName, String projectName);

    /**
     * Informs the SCM Federator that a project is no longer being shared with a partner.
     * @param partnerName The name of the partner. Must match a Gateway management endpoint.
     * @param projectName The name of the project. Must match the name in the SCM system.
     */
    void sharingPartnerRemoved(String partnerName, String projectName);

    /**
     * Causes the SCM Federator to process a received acknowledgement file received from a sharing partner.
     * @param acknowledgement The acknowledgement.
     */
    void processAcknowledgementFile(AcknowledgementBean acknowledgement);
}
