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
package com.surevine.gateway.scm.scmclient;

import com.surevine.gateway.scm.model.LocalRepoBean;

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
    Collection<LocalRepoBean> getRepositories(String projectKey) throws SCMCallException;

    /**
     * Gets a single repo from a project
     * @param projectKey the owning project key
     * @param repositorySlug the repository slug
     * @return the repository info
     * @throws SCMCallException
     */
    LocalRepoBean getRepository(String projectKey, String repositorySlug) throws SCMCallException;

    /**
     * Gets all repositories in the SCM system.
     * Warning: this is probably expensive.
     * @return a mapping of projects to repositories
     * @throws SCMCallException
     */
    Map<String, Collection<LocalRepoBean>> getAllRepositories() throws SCMCallException;
}
