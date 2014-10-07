package com.surevine.gateway.scm.scmclient;

import com.surevine.gateway.scm.scmclient.bean.ProjectBean;

/**
 * @author nick.leaver@surevine.com
 */
public interface CreateProjectCommand {
    /**
     * Creates a new project in the SCM system
     * @param projectBean the project details
     * @return The ProjectBean with any additional information from the SCM system (probably the ID)
     * @throws com.surevine.gateway.scm.scmclient.SCMCallException
     */
    public ProjectBean createProject(ProjectBean projectBean) throws SCMCallException;
}
