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
package com.surevine.gateway.scm.service.rest;

import com.surevine.gateway.scm.service.FederatorService;
import com.surevine.gateway.scm.service.bean.AcknowledgementBean;
import com.surevine.gateway.scm.util.SpringApplicationContext;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

/**
 * REST proxy for the federator service
 * @author nick.leaver@surevine.com
 */
@Path("/federator")
@Produces("application/json")
@Consumes("application/json")
public class RestFederatorService implements FederatorService {
    private FederatorService federatorService;

    @POST
    @Path("newSharingPartner")
    @Override
    public void newSharingPartner(@QueryParam("partnerName") final String partnerName,
                                  @QueryParam("projectName") final String projectName) {
        getImplementation().newSharingPartner(partnerName, projectName);
    }

    @POST
    @Path("redistribute")
    @Override
    public void redistribute(@QueryParam("partnerName") final String partnerName,
                             @QueryParam("projectName") final String projectName) {
        getImplementation().redistribute(partnerName, projectName);
    }

    @POST
    @Path("sharingPartnerRemoved")
    @Override
    public void sharingPartnerRemoved(@QueryParam("partnerName") final String partnerName,
                                      @QueryParam("projectName") final String projectName) {
        getImplementation().sharingPartnerRemoved(partnerName, projectName);
    }

    @POST
    @Path("processAcknowledgement")
    @Override
    public void processAcknowledgementFile(final AcknowledgementBean acknowledgement) {
        getImplementation().processAcknowledgementFile(acknowledgement);
    }

    private FederatorService getImplementation() {
        if (federatorService == null) {
            federatorService = (FederatorService) SpringApplicationContext.getBean("federatorService");
        }
        return federatorService;
    }
}
