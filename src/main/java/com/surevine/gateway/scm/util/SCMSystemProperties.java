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
package com.surevine.gateway.scm.util;

import org.apache.commons.codec.binary.Base64;

/**
 * Bean for SCM system properties
 * @author nick.leaver@surevine.com
 */
public class SCMSystemProperties {
    public static enum SCMType {
        STASH,
        GITLAB,
        UNKNOWN;

        static SCMType getType(final String typeString) {
            if ("stash".equalsIgnoreCase(typeString)) {
                return STASH;
            } else if ("gitlab".equalsIgnoreCase(typeString)) {
                return GITLAB;
            } else {
                return UNKNOWN;
            }
        }
    }

    private SCMType type;
    private String username;
    private String password;
    private String host;
    private String encodedAuth;
    private String authToken;

    SCMSystemProperties(final String type, final String username, final String password, final String host) {
        this.type = SCMType.getType(type);
        this.username = username;
        this.password = password;
        this.host = host;
        this.encodedAuth = "Basic " + Base64.encodeBase64String((username + ":" + password).getBytes());
    }
    
    SCMSystemProperties(final String type, final String username, final String password, final String host, final String authToken) {
    	this(type, username, password, host);
    	this.authToken = authToken;
    }

    public SCMType getType() {
        return type;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getHost() {
        return host;
    }

    public String getBasicAuthHeader() {
        return encodedAuth;
    }
    
    public String getAuthToken() {
    	return authToken;
    }
}
