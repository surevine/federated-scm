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
	@JsonProperty("id")
	private String id;

	@JsonProperty("namespace")
	private Map namespace;

	@JsonProperty("name_with_namespace")
	private String nameWithNamespace;

	@JsonProperty("ssh_url_to_repo")
	private String sshUrl;

	@JsonProperty("http_url_to_repo")
	private String httpUrl;

	@JsonProperty("path")
	private String path;

	@JsonProperty("path_with_namespace")
	private String pathWithNamespace;

	public String getId() {
		return id;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public void setNamespaceId(final String namespaceId) {
		this.namespaceId = namespaceId;
	}

	public MultivaluedMap<String, String> toMap() {
		final MultivaluedMap<String, String> rtn = new MultivaluedMapImpl<String, String>();

		rtn.put("name", Arrays.asList(name));

		rtn.put("path", Arrays.asList(path));

		if (namespaceId != null) {
			rtn.put("namespace_id", Arrays.asList(namespaceId));
		}

		return rtn;
	}

	public void setNamespace(final Map namespace) {
		this.namespace = namespace;
	}

	public void setNameWithNamespace(final String nameWithNamespace) {
		this.nameWithNamespace = nameWithNamespace;
	}

	public void setSshUrlToRepo(final String sshUrl) {
		this.sshUrl = sshUrl;
	}

	public void setHttpUrlToRepo(final String sshUrl) {
		this.httpUrl = sshUrl;
	}

	public void setPath(final String path) {
		this.path = path;
	}

	public LocalRepoBean asRepoBean() {
		final LocalRepoBean repoBean = new LocalRepoBean();
		repoBean.setCloneSourceURI(sshUrl);
		repoBean.setSlug(path);
		repoBean.setFromGateway(false);
		repoBean.setProjectKey((String) namespace.get("path"));
		return repoBean;
	}

	public String getName() {
		return name;
	}

	public String getPath() {
		return path;
	}

	public Map getNamespace() {
		return namespace;
	}

	public String getNameWithNamespace() {
		return nameWithNamespace;
	}

	public Integer getNamespaceId() {
		return (Integer) namespace.get("id");
	}

	public String getNamespacePath() {
		return (String) namespace.get("path");
	}

	public void setPathWithNamespace(final String pathWithNamespace) {
		this.pathWithNamespace = pathWithNamespace;
	}

	public String getPathWithNamespace() {
		return pathWithNamespace;
	}
}
