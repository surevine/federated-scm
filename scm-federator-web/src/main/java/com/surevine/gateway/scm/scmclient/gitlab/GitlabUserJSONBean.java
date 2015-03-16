package com.surevine.gateway.scm.scmclient.gitlab;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GitlabUserJSONBean {
	
	private String id;
	private String username;
	
	public void setId(String id) {
		this.id = id;
	}
	
	public void setUsername(String username) {
		this.username = username;
	}
	
	public String getId() {
		return id;
	}
	
	public String getUsername() {
		return username;
	}
}
