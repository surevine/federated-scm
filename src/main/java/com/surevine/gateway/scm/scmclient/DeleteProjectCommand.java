package com.surevine.gateway.scm.scmclient;

/**
 * @author nick.leaver@surevine.com
 */
public interface DeleteProjectCommand {
    /**
     * Deletes a project - use with care
     * @param projectKey the project key
     * @throws SCMCallException
     */
    public void deleteProject(String projectKey) throws SCMCallException;
}
