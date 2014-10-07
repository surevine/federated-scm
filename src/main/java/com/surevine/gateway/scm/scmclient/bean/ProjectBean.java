package com.surevine.gateway.scm.scmclient.bean;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

/**
 * Bean for Project information
 * @author nick.leaver@surevine.com
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProjectBean {
    private String id;
    private String name;
    private String key;
    private String description;
    
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

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "ProjectBean{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", key='" + key + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
