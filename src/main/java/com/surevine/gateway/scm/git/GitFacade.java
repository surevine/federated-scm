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
import com.surevine.gateway.scm.model.RepoBean;

import java.nio.file.Path;

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
    public abstract void clone(RepoBean repoBean) throws GitException;

    /**
     * Pull a remote into a git repository
     * @param repoBean Information about the remote Repo
     * @return true if the pull resulted in an update to the local repository, false if there were no changes
     * @throws GitException
     */
    public abstract boolean pull(RepoBean repoBean) throws GitException;

    /**
     * Pushes a repo to a remote
     * @param repoBean the repo bean
     * @return true if the push succeeded
     * @throws GitException if something went wrong
     */
    public abstract boolean push(RepoBean repoBean) throws GitException;

    /**
     * Tags a repository
     * @param repoBean the repository info
     * @param tag the tag name
     * @throws GitException
     */
    public abstract void tag(RepoBean repoBean, String tag) throws GitException;

    /**
     * Bundles a repository
     * @param repoBean the repository information
     * @return the Path to the bundle file
     */
    public abstract Path bundle(RepoBean repoBean) throws GitException;
    
    /**
     * Determines if a repo has already been cloned into the scm federator working directory
     * @param repoBean Information about the remote Repo
     * @return true if ${git.repodir}/projectKey/repoSlug already exists and has a remote called origin set to repoURI
     */
    public abstract boolean repoAlreadyCloned(RepoBean repoBean) throws GitException;

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
