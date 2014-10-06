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
        
        static SCMType getType(String typeString) {
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

    SCMSystemProperties(String type, String username, String password, String host) {
        this.type = SCMType.getType(type);
        this.username = username;
        this.password = password;
        this.host = host;
        this.encodedAuth = "Basic " + Base64.encodeBase64String((username + ":" + password).getBytes());
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
}
