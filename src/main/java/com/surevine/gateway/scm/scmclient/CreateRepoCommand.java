package com.surevine.gateway.scm.scmclient;

import com.surevine.gateway.scm.scmclient.bean.RepoBean;

/**
 * @author nick.leaver@surevine.com
 */
public interface CreateRepoCommand {
    /**
     * Creates a repo in the specified project
     * @param projectKey the project
     * @param name the name of the repo
     * @return a RepoBean populated with extra information from the SCM system
     * @throws SCMCallException
     */
    public RepoBean createRepo(String projectKey, String name) throws SCMCallException;
}
