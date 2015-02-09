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

import com.surevine.gateway.scm.Distributor;
import com.surevine.gateway.scm.DistributorImpl;
import com.surevine.gateway.scm.IncomingProcessor;
import com.surevine.gateway.scm.IncomingProcessorImpl;
import com.surevine.gateway.scm.util.InputValidator;
import com.surevine.gateway.scm.util.PropertyUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.xml.ws.WebServiceException;

import org.apache.log4j.Logger;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * REST proxy for the federator service
 * @author nick.leaver@surevine.com
 */
@Path("/federator")
@Produces("application/json")
public class RestFederatorService {

	private static final Logger LOGGER = Logger.getLogger(RestFederatorService.class);

    private Distributor distributionService;

    public static final String BUNDLE_FORM_FIELD = "bundle";

    /**
     * Causes the SCM federator to attempt to distribute the named repository to the specified destination.
     * @param partnerName the name of the partner / gateway destination the repo should be sent to
     * @param projectKey the project (SCM group) key
     * @param repositorySlug the repository slug (short name) as it appears in the SCM system
     * @return a HTTP response appropriate to the success or failure of the call
     * @throws SCMFederatorServiceException if the specified repository could not be distributed
     */
    @POST
    @Path("distribute")
    @Consumes("application/json")
    public Response distributeToSingleDestination(@QueryParam("destination") final String partnerName,
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

        getDistributionService().distributeToSingleDestination(partnerName, projectKey, repositorySlug);
        return Response.ok(MediaType.APPLICATION_JSON).build();
    }

    /**
     * Causes the SCM federator to attempt to distribute all shared repositories via the gateway.
     * No service errors are returned from this call. Any errors with individual repositories will be logged.
     */
    @GET
    @Path("distributeAll")
    @Consumes("application/json")
    public Response distributeAll() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                getDistributionService().distributeAll();
            }
        }).start();
        return Response.ok(MediaType.APPLICATION_JSON).build();
    }

    private Distributor getDistributionService() {
        if (distributionService == null) {
            distributionService = new DistributorImpl();
        }
        return distributionService;
    }

    @POST
    @Path("incoming")
    @Consumes("multipart/form-data")
    /**
     * An HTTP method to begin the import of a bundle with associated metdata. Expects a multipart
     * form, with the `bundle` field the file input containing the Git bundle
     */
    public Response incomingBundle(MultipartFormDataInput input) {

    	try {
    		java.nio.file.Path bundlePath = importBundle(input);
    		HashMap<String, String> metadata = importMetadata(input);

    		IncomingProcessor incoming = new IncomingProcessorImpl();
    		incoming.processIncomingRepository(bundlePath, metadata);

    		bundlePath.toFile().delete();

		} catch (IOException e) {
			return Response.status(400).entity(e.getMessage()).build();
		} catch (SCMFederatorServiceException e) {
			return Response.status(500).entity(e.getMessage()).build();
		}

    	return Response.ok().build();
    }

    private java.nio.file.Path importBundle(MultipartFormDataInput input) throws IOException {

		Map<String, List<InputPart>> uploadForm = input.getFormDataMap();

    	// Process the file first
		List<InputPart> inputParts = uploadForm.get(BUNDLE_FORM_FIELD);
		if ( inputParts == null ) {
			throw new WebApplicationException(Response.status(HttpURLConnection.HTTP_BAD_REQUEST)
                .entity("Received input was invalid")
                .type(MediaType.TEXT_PLAIN).build());
		}
    	java.nio.file.Path file;

		for (InputPart inputPart : inputParts) {
			file = generateFilename();

			InputStream inputStream = inputPart.getBody(InputStream.class, null);
			Files.copy(inputStream, file, StandardCopyOption.REPLACE_EXISTING);

			return file;
		}
		return null;
    }

    private HashMap<String, String> importMetadata(MultipartFormDataInput input) throws IOException {
		Map<String, List<InputPart>> uploadForm = input.getFormDataMap();
		HashMap<String, String> rtn = new HashMap<String, String>();

		for (String key : uploadForm.keySet() ) {
			if ( key.equals(BUNDLE_FORM_FIELD) ) {
				continue;
			}

			rtn.put(key, uploadForm.get(key).get(0).getBodyAsString());
		}

		LOGGER.debug(rtn.toString());

		return rtn;
    }

    private java.nio.file.Path generateFilename() {
    	String filename = UUID.randomUUID().toString();
    	String filepath = PropertyUtil.getTempDir()+"/"+filename+".bundle";
    	LOGGER.debug(filepath);
    	return new File(filepath).toPath();
    }
}
