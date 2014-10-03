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
     * 
     * @param partnerName The name of the partner. Must match a Gateway management endpoint.
     * @param projectName The name of the project. Must match the name in the SCM system.
     */
    public void newSharingPartner(String partnerName, String projectName);

    /**
     * Causes the SCM Federator to redistribute an entire repository to a sharing partner.
     * 
     * @param partnerName The name of the partner. Must match a Gateway management endpoint.
     * @param projectName The name of the project. Must match the name in the SCM system.
     */
    public void redistribute(String partnerName, String projectName);

    /**
     * Informs the SCM Federator that a project is no longer being shared with a partner.
     * 
     * @param partnerName The name of the partner. Must match a Gateway management endpoint.
     * @param projectName The name of the project. Must match the name in the SCM system.
     */
    public void sharingPartnerRemoved(String partnerName, String projectName);

    /**
     * Causes the SCM Federator to process a received acknowledgement file received from a sharing partner.
     * 
     * @param acknowledgement The acknowledgement.
     */
    public void processAcknowledgementFile(AcknowledgementBean acknowledgement);
    
}
