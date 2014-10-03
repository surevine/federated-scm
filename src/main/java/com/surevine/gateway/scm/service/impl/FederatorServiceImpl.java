package com.surevine.gateway.scm.service.impl;

import com.surevine.gateway.scm.service.FederatorService;
import com.surevine.gateway.scm.service.bean.AcknowledgementBean;
import org.apache.log4j.Logger;

/**
 * Implementation of Federator Service
 * @author nick.leaver@surevine.com
 */
public class FederatorServiceImpl implements FederatorService {
    private static final Logger logger = Logger.getLogger(FederatorServiceImpl.class);
    
    @Override
    public void newSharingPartner(String partnerName, String projectName) {
        logger.debug("New sharing partner notification: " + partnerName + " for project " + projectName);
    }

    @Override
    public void redistribute(String partnerName, String projectName) {
        logger.debug("Redistribution request: " + partnerName + " for project " + projectName);
    }

    @Override
    public void sharingPartnerRemoved(String partnerName, String projectName) {
        logger.debug("Removed sharing partner notification: " + partnerName + " for project " + projectName);
    }

    @Override
    public void processAcknowledgementFile(AcknowledgementBean acknowledgement) {
        logger.debug("Received acknowledgement:" + acknowledgement);
    }
}
