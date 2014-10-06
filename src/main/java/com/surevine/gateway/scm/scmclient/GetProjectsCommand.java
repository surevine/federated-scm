package com.surevine.gateway.scm.scmclient;

import com.surevine.gateway.scm.scmclient.bean.ProjectBean;

import java.util.Collection;

/**
 * Gets project information from an SCM system
 * @author nick.leaver@surevine.com
 */
public interface GetProjectsCommand {
    /**
     * Gets a collection of all projects in the SCM system
     * @return a collection of all projects in the SCM system
     */
    public Collection<ProjectBean> getProjects();

    /**
     * Get details of a project in the SCM system
     * @param projectKey the project shortcode for unique project identification
     * @return details of a project in the SCM system
     */
    public ProjectBean getProject(String projectKey);
}
