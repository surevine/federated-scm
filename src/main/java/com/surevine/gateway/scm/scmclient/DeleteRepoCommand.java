package com.surevine.gateway.scm.scmclient;

/**
 * @author nick.leaver@surevine.com
 */
public interface DeleteRepoCommand {
    /**
     * Deletes a repository from a project
     * @param projectKey The project key
     * @param repoSlug the repo slug
     * @throws SCMCallException
     */
    public void deleteRepo(String projectKey, String repoSlug) throws SCMCallException;
}
