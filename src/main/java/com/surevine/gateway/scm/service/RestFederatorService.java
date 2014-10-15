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

import com.surevine.gateway.scm.api.Distributor;
import com.surevine.gateway.scm.api.impl.DistributorImpl;
import com.surevine.gateway.scm.util.InputValidator;

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
public class RestFederatorService {
    private Distributor distributionService;

    @POST
    @Path("distribute")
    public Response distribute(@QueryParam("destination") final String partnerName,
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

        getDistributionService().distribute(partnerName, projectKey, repositorySlug);
        return Response.ok(MediaType.APPLICATION_JSON).build();
    }

    private Distributor getDistributionService() {
        if (distributionService == null) {
            distributionService = new DistributorImpl();
        }
        return distributionService;
    }
}
