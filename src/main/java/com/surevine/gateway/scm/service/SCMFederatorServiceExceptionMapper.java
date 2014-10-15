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

import com.surevine.gateway.scm.service.SCMFederatorServiceException;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.net.HttpURLConnection;

/**
 * @author nick.leaver@surevine.com
 */
@Provider
public class SCMFederatorServiceExceptionMapper implements ExceptionMapper<SCMFederatorServiceException> {
    @Override
    public Response toResponse(final SCMFederatorServiceException exception) {
        return Response.status(HttpURLConnection.HTTP_BAD_REQUEST)
                .entity(exception.getUserMessage())
                .type(MediaType.TEXT_PLAIN).build();
    }
}
