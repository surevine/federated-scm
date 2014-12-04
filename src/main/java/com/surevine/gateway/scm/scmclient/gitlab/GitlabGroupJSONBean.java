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
package com.surevine.gateway.scm.scmclient.gitlab;

import java.util.Arrays;

import javax.ws.rs.core.MultivaluedMap;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.jboss.resteasy.specimpl.MultivaluedMapImpl;

/**
 * Bean for easily working with group information with the Gitlab API
 * 
 * API documentation for this entity is: http://doc.gitlab.com/ce/api/projects.html
 * 
 * @author martin.hewitt@surevine.com
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class GitlabGroupJSONBean {
	
    private String id;
    private String name;
    private String path;
    private String ownerId;
	private String namespaceId;
    
    public GitlabGroupJSONBean() {
    	//
    }

    public String getId() {
        return id;
    }
    
    public Integer getIdInt() {
    	return Integer.parseInt(getId());
    }

    void setId(final String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    void setName(final String name) {
        this.name = name.toLowerCase();
    }
    
    public String getPath() {
    	return path;
    }
    
    public void setPath(String path) {
    	this.path = path;
    }
    
    public String getOwnerId() {
    	return ownerId;
    }
    
    public void setOwnerId(String ownerId) {
    	this.ownerId = ownerId;
    }

	public MultivaluedMap<String, String> toMap() {
		MultivaluedMap<String, String> rtn = new MultivaluedMapImpl<String, String>();
		
		rtn.put("name", Arrays.asList(name));
		
		if ( path != null ) {
			rtn.put("path", Arrays.asList(path));
		}
		
		if ( id != null && id.length() > 0 ) {
			rtn.put("id", Arrays.asList(id));
		}

		if ( ownerId != null && ownerId.length() > 0 ) {
			rtn.put("ownerId", Arrays.asList(ownerId));
		}
		
		return rtn;
	}
}
