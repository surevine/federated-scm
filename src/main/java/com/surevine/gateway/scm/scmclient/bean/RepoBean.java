package com.surevine.gateway.scm.scmclient.bean;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

/**
 * Bean for repo information
 * @author nick.leaver@surevine.com
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class RepoBean {
    private String id;
    private String name;
    private String slug;
    private String scmId = "git"; 
    private ProjectBean project;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getScmId() {
        return scmId;
    }

    public void setScmId(String scmId) {
        this.scmId = scmId;
    }

    public ProjectBean getProject() {
        return project;
    }

    public void setProject(ProjectBean project) {
        this.project = project;
    }

    @Override
    public String toString() {
        return "RepoBean{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", slug='" + slug + '\'' +
                ", scmId='" + scmId + '\'' +
                ", project=" + project +
                '}';
    }
}
