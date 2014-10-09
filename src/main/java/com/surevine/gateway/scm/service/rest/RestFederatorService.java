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
import com.surevine.gateway.scm.service.SCMFederatorServiceException;
import com.surevine.gateway.scm.service.bean.AcknowledgementBean;
import com.surevine.gateway.scm.util.SpringApplicationContext;
import org.apache.log4j.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.HttpURLConnection;

/**
 * REST proxy for the federator service
 * @author nick.leaver@surevine.com
 */
@Path("/federator")
@Produces("application/json")
@Consumes("application/json")
public class RestFederatorService implements FederatorService {
    private Logger logger = Logger.getLogger(RestFederatorService.class);
    private FederatorService federatorService;

    @POST
    @Path("newSharingPartner")
    @Override
    public void newSharingPartner(@QueryParam("partnerName") final String partnerName,
                                  @QueryParam("projectKey") final String projectKey,
                                  @QueryParam("repositorySlug") final String repositorySlug)
            throws SCMFederatorServiceException {
        // check input
        if (!InputValidator.partnerNameIsValid(partnerName) 
                || !InputValidator.projectKeyIsValid(projectKey)
                || !InputValidator.repoSlugIsValid(repositorySlug)) {
            // one of the params is dirty and we can't use it so reject the request
            throw new WebApplicationException(Response.status(HttpURLConnection.HTTP_BAD_REQUEST)
                    .entity("Received input was invalid")
                    .type(MediaType.TEXT_PLAIN).build());
        }
        
        getImplementation().newSharingPartner(partnerName, projectKey, repositorySlug);
    }

    @POST
    @Path("redistribute")
    @Override
    public void redistribute(@QueryParam("partnerName") final String partnerName,
                             @QueryParam("projectKey") final String projectKey,
                             @QueryParam("repositorySlug") final String repositorySlug)
            throws SCMFederatorServiceException {
        // check input
        if (!InputValidator.partnerNameIsValid(partnerName)
                || !InputValidator.projectKeyIsValid(projectKey)
                || !InputValidator.repoSlugIsValid(repositorySlug)) {
            // one of the params is dirty and we can't use it so reject the request
            throw new WebApplicationException(Response.status(HttpURLConnection.HTTP_BAD_REQUEST)
                    .entity("Received input was invalid")
                    .type(MediaType.TEXT_PLAIN).build());
        }

        getImplementation().redistribute(partnerName, projectKey, repositorySlug);
    }

    @POST
    @Path("sharingPartnerRemoved")
    @Override
    public void sharingPartnerRemoved(@QueryParam("partnerName") final String partnerName,
                                      @QueryParam("projectKey") final String projectKey,
                                      @QueryParam("repositorySlug") final String repositorySlug)
            throws SCMFederatorServiceException {
        // check input
        if (!InputValidator.partnerNameIsValid(partnerName)
                || !InputValidator.projectKeyIsValid(projectKey)
                || !InputValidator.repoSlugIsValid(repositorySlug)) {
            // one of the params is dirty and we can't use it so reject the request
            throw new WebApplicationException(Response.status(HttpURLConnection.HTTP_BAD_REQUEST)
                    .entity("Received input was invalid")
                    .type(MediaType.TEXT_PLAIN).build());
        }

        getImplementation().sharingPartnerRemoved(partnerName, projectKey, repositorySlug);
    }

    @POST
    @Path("processAcknowledgement")
    @Override
    public void processAcknowledgementFile(final AcknowledgementBean acknowledgement)
            throws SCMFederatorServiceException {
        // check input
        if (!InputValidator.acknowledgementBeanIsValid(acknowledgement)) {
            // the bean is dirty so reject the request
            throw new WebApplicationException(Response.status(HttpURLConnection.HTTP_BAD_REQUEST)
                    .entity("Received input was invalid")
                    .type(MediaType.TEXT_PLAIN).build());
        }
        
        getImplementation().processAcknowledgementFile(acknowledgement);
    }

    private FederatorService getImplementation() {
        if (federatorService == null) {
            federatorService = (FederatorService) SpringApplicationContext.getBean("federatorService");
        }
        return federatorService;
    }
}
