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
