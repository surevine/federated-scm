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
package com.surevine.gateway.scm.service.impl;

import com.surevine.gateway.scm.service.FederatorService;
import com.surevine.gateway.scm.service.bean.AcknowledgementBean;
import org.apache.log4j.Logger;

/**
 * Implementation of Federator Service
 * @author nick.leaver@surevine.com
 */
public class FederatorServiceImpl implements FederatorService {
    private static Logger logger = Logger.getLogger(FederatorServiceImpl.class);

    @Override
    public void newSharingPartner(String partnerName, String projectKey, String repositorySlug) {
        logger.debug("New sharing partner notification: " + partnerName + " for repository " + projectKey + ":" + repositorySlug);
    }

    @Override
    public void redistribute(String partnerName, String projectKey, String repositorySlug) {
        logger.debug("Redistribution request: " + partnerName + " for repository " + projectKey + ":" + repositorySlug);
    }

    @Override
    public void sharingPartnerRemoved(String partnerName, String projectKey, String repositorySlug) {
        logger.debug("Removed sharing partner notification: " + partnerName + " for repository " + projectKey + ":" + repositorySlug);
    }

    @Override
    public void processAcknowledgementFile(final AcknowledgementBean acknowledgement) {
        logger.debug("Received acknowledgement:" + acknowledgement);
    }
}
