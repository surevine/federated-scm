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
     * @throws com.surevine.gateway.scm.scmclient.SCMCallException
     */
    public Collection<ProjectBean> getProjects() throws SCMCallException;

    /**
     * Get details of a project in the SCM system
     * @param projectKey the project shortcode for unique project identification
     * @return details of a project in the SCM system
     * @throws com.surevine.gateway.scm.scmclient.SCMCallException
     */
    public ProjectBean getProject(String projectKey) throws SCMCallException;
}
