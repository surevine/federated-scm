package com.surevine.gateway.scm.service.rest;

import com.surevine.gateway.scm.service.FederatorService;
import com.surevine.gateway.scm.service.bean.AcknowledgementBean;
import com.surevine.gateway.scm.util.SpringApplicationContext;
import org.apache.log4j.Logger;

import javax.ws.rs.*;

/**
 * REST proxy for the federator service
 * @author nick.leaver@surevine.com
 */
@Path("/federator")
@Produces("application/json")
@Consumes("application/json")
public class RestFederatorService implements FederatorService {
    private static final Logger logger = Logger.getLogger(RestFederatorService.class);
    private FederatorService federatorService;
    
    @POST
    @Path("newSharingPartner")
    @Override
    public void newSharingPartner(@QueryParam("partnerName") String partnerName, @QueryParam("projectName") String projectName) {
        getImplementation().newSharingPartner(partnerName, projectName);
    }

    @POST
    @Path("redistribute")
    @Override
    public void redistribute(@QueryParam("partnerName") String partnerName, @QueryParam("projectName") String projectName) {
        getImplementation().redistribute(partnerName, projectName);
    }

    @POST
    @Path("sharingPartnerRemoved")
    @Override
    public void sharingPartnerRemoved(@QueryParam("partnerName") String partnerName, @QueryParam("projectName") String projectName) {
        getImplementation().sharingPartnerRemoved(partnerName, projectName);
    }

    @POST
    @Path("processAcknowledgement")
    @Override
    public void processAcknowledgementFile(AcknowledgementBean acknowledgement) {
        getImplementation().processAcknowledgementFile(acknowledgement);
    }

    private FederatorService getImplementation() {
        if (federatorService == null) {
            federatorService = (FederatorService) SpringApplicationContext.getBean("federatorService");
        }
        return federatorService;
    }
}
