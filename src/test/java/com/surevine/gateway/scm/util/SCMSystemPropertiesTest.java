package com.surevine.gateway.scm.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author nick.leaver@surevine.com
 */
public class SCMSystemPropertiesTest {
    @Test
    public void testAuthEncoding() {
        String username = "username";
        String password = "password";
        String correctAuthHeader = "Basic dXNlcm5hbWU6cGFzc3dvcmQ=";
        
        SCMSystemProperties props = new SCMSystemProperties("stash", username, password, null);
        
        assertEquals(correctAuthHeader, props.getBasicAuthHeader());
    }
    
    @Test
    public void checkStringTypeMapping() {
        assertEquals(SCMSystemProperties.SCMType.STASH, SCMSystemProperties.SCMType.getType("stash"));
        assertEquals(SCMSystemProperties.SCMType.GITLAB, SCMSystemProperties.SCMType.getType("gitlab"));
        assertEquals(SCMSystemProperties.SCMType.UNKNOWN, SCMSystemProperties.SCMType.getType("anything else"));
    }
}
