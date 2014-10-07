package com.surevine.gateway.scm.scmclient;

import com.surevine.gateway.scm.scmclient.bean.ProjectBean;
import com.surevine.gateway.scm.scmclient.bean.RepoBean;

import java.util.Collection;
import java.util.Map;

/**
 * Gets repository information from an SCM system
 * @author nick.leaver@surevine.com
 */
public interface GetRepoCommand {
    /**
     * Gets all repositories under a project
     * @param projectKey the project key
     * @return all repositories in the project
     * @throws SCMCallException
     */
    public Collection<RepoBean> getRepositories(String projectKey);

    /**
     * Gets a single repo from a project
     * @param projectKey the owning project key
     * @param repositorySlug the repository slug
     * @return the repository info
     * @throws SCMCallException
     */
    public RepoBean getRepository(String projectKey, String repositorySlug);

    /**
     * Gets all repositories in the SCM system.
     * Warning: this is probably expensive.
     * @return a mapping of projects to repositories
     * @throws SCMCallException
     */
    public Map<ProjectBean, Collection<RepoBean>> getAllRepositories() throws SCMCallException;
}
