/*
 * Copyright (C) 2008-2014 Surevine Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/
package com.surevine.gateway.scm.git;

import com.surevine.gateway.scm.git.jgit.JGitGitFacade;
import com.surevine.gateway.scm.model.LocalRepoBean;

import java.nio.file.Path;
import java.util.Map;

/**
 * @author nick.leaver@surevine.com
 */
public abstract class GitFacade {
    private static GitFacade instance;

    protected GitFacade() {
        // no-op
    }

    /**
     * Clone a repo into the configured git repository directory
     * @param repoBean Information about the remote Repo
     * @throws GitException
     */
    public abstract void clone(LocalRepoBean repoBean) throws GitException;

    /**
     * Adds a remote to a local repo
     * @param repoBean the details of the repo
     * @param remoteName the remote name
     * @param remoteURL the remote URL
     * @throws GitException
     */
    public abstract void addRemote(LocalRepoBean repoBean, String remoteName, String remoteURL) throws GitException;

    /**
     * Updates a remote on a local repo
     * @param repoBean the details of the repo
     * @param remoteName the remote name
     * @param remoteURL the remote URL
     * @throws GitException
     */
    public abstract void updateRemote(LocalRepoBean repoBean, String remoteName, String remoteURL) throws GitException;

    /**
     * Gets the remotes configured for a repository
     * @param repoBean the repo bean
     * @return the remotes configured for a repository
     * @throws GitException
     */
    public abstract Map<String, String> getRemotes(LocalRepoBean repoBean) throws GitException;

    /**
     * Fetch from a remote
     * @param repoBean Information about the remote Repo
     * @param remoteName the name of the remote - will use origin if null
     * @return true if the fetch resulted in an update to the local repository, false if there were no changes
     * @throws GitException
     */
    public abstract boolean fetch(LocalRepoBean repoBean, String remoteName) throws GitException;

    /**
     * Pushes a repo to a remote
     * @param repoBean the repo bean
     * @param remoteName the name of the remote
     * @throws GitException if something went wrong
     */
    public abstract void push(LocalRepoBean repoBean, String remoteName) throws GitException;

    /**
     * Tags a repository
     * @param repoBean the repository info
     * @param tag the tag name
     * @throws GitException
     */
    public abstract void tag(LocalRepoBean repoBean, String tag) throws GitException;

    /**
     * Bundles a repository
     * @param repoBean the repository information
     * @return the Path to the bundle file
     */
    public abstract Path bundle(LocalRepoBean repoBean) throws GitException;

    /**
     * Determines if a repo has already been cloned into the scm federator working directory
     * @param repoBean Information about the remote Repo
     * @return true if ${git.repodir}/projectKey/repoSlug already exists and has a remote called origin set to repoURI
     */
    public abstract boolean repoAlreadyCloned(LocalRepoBean repoBean) throws GitException;

    /**
     * Determines whether a repo is empty (no files)
     * @param repoBean repo to check
     * @return
     */
    public abstract boolean isRepoEmpty(LocalRepoBean repoBean) throws GitException;

    /**
     * Set the Git implementation
     * @param instance the git implementation
     */
    public static void setInstance(final GitFacade instance) {
        GitFacade.instance = instance;
    }

    /**
     * Get Git implementation
     * @return the git implementation
     */
    public static GitFacade getInstance() {
        if (instance == null) {
            instance = new JGitGitFacade();
        }
        return instance;
    }
}
