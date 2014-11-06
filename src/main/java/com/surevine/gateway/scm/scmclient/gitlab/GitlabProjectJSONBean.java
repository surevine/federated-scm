package com.surevine.gateway.scm.scmclient.gitlab;

import java.util.Arrays;
import java.util.Map;

import javax.ws.rs.core.MultivaluedMap;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.jboss.resteasy.specimpl.MultivaluedMapImpl;

import com.surevine.gateway.scm.model.LocalRepoBean;

/**
 * Bean for easily working with project information with the Gitlab API
 * 
 * API documentation for this entity is: http://doc.gitlab.com/ce/api/groups.html
 * 
 * @author martin.hewitt@surevine.com
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class GitlabProjectJSONBean {
	
	private String name;
	@JsonProperty("namespace_id")
	private String namespaceId;
	private String id;
	
	@JsonProperty("namespace")
	private Map namespace;
	
	@JsonProperty("ssh_url_to_repo")
	private String sshUrl;
	
	@JsonProperty("http_url_to_repo")
	private String httpUrl;
	
	@JsonProperty("path")
	private String path;

	public void setName(String name) {
		this.name = name;
	}

	public void setNamespaceId(String namespaceId) {
		this.namespaceId = namespaceId;
	}

	public MultivaluedMap<String, String> toMap() {
		MultivaluedMap<String, String> rtn = new MultivaluedMapImpl<String, String>();
		
		rtn.put("name", Arrays.asList(name));
		
		if ( namespaceId != null ) {
			rtn.put("namespace_id", Arrays.asList(namespaceId));
		}
		
		return rtn;
	}
	
	public void setNamespace(Map namespace) {
		this.namespace = namespace;
	}
	
	public void setSshUrlToRepo(String sshUrl) {
		this.sshUrl = sshUrl;
	}
	
	public void setHttpUrlToRepo(String sshUrl) {
		this.httpUrl = sshUrl;
	}
	
	public void setPathWithNamespace(String path) {
		this.path = path;
	}

    public LocalRepoBean asRepoBean() {
        LocalRepoBean repoBean = new LocalRepoBean();
        repoBean.setCloneSourceURI(sshUrl);
        repoBean.setSlug(path);
        repoBean.setFromGateway(false);
        repoBean.setProjectKey((String)namespace.get("path"));
        return repoBean;
    }

	public Map getNamespace() {
		return namespace;
	}
	
	public Integer getNamespaceId() {
		return (Integer) namespace.get("id");
	}
	
	public String getNamespacePath() {
		return (String) namespace.get("path");
	}
}
