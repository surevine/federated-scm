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

import java.util.Collection;
import java.util.Map;

import com.surevine.gateway.scm.model.LocalRepoBean;
import com.surevine.gateway.scm.scmclient.gitlab.GitlabCommandFactory;
import com.surevine.gateway.scm.scmclient.stash.StashCommandFactory;
import com.surevine.gateway.scm.util.PropertyUtil;

/**
 * SCM system commands
 * 
 * @author nick.leaver@surevine.com
 */
public abstract class SCMCommand {

	private static SCMCommandFactory commandFactoryImplementation;

	private static SCMCommandFactory getCommandFactory() {
		if (commandFactoryImplementation == null) {
			switch (PropertyUtil.getSCMType()) {
				case STASH:
					commandFactoryImplementation = new StashCommandFactory();
					break;
				case GITLAB:
					commandFactoryImplementation = new GitlabCommandFactory();
					break;
				default:
					break;
			}
		}
		return commandFactoryImplementation;
	}

	/**
	 * Forks a repository in an SCM system
	 * 
	 * @param projectKey
	 *            the existing project (or group) key
	 * @param repositorySlug
	 *            the repository slug
	 * @param forkProjectKey
	 *            the target project/group key
	 * @throws SCMCallException
	 */
	public static LocalRepoBean forkRepo(final String projectKey, final String repositorySlug,
			final String forkProjectKey) throws SCMCallException {
		return getCommandFactory().getForkRepoCommandImpl().forkRepo(projectKey, repositorySlug, forkProjectKey);
	}

	/**
	 * Gets a collection of all projects in the SCM system
	 * 
	 * @return a collection of all projects in the SCM system
	 * @throws SCMCallException
	 */
	public static Collection<String> getProjects() throws SCMCallException {
		return getCommandFactory().getGetProjectsCommandImpl().getProjects();
	}

	/**
	 * Creates a new project in the SCM system
	 * 
	 * @param projectKey
	 *            the project key
	 * @throws SCMCallException
	 */
	public static void createProject(final String projectKey) throws SCMCallException {
		getCommandFactory().getCreateProjectCommandImpl().createProject(projectKey);
	}

	/**
	 * Deletes a project - use with care
	 * 
	 * @param projectKey
	 *            the project key
	 * @throws SCMCallException
	 */
	public static void deleteProject(final String projectKey) throws SCMCallException {
		getCommandFactory().getDeleteProjectCommandImpl().deleteProject(projectKey);
	}

	/**
	 * Gets all repositories under a project
	 * 
	 * @param projectKey
	 *            the project key
	 * @return all repositories in the project
	 * @throws SCMCallException
	 */
	public static Collection<LocalRepoBean> getRepositories(final String projectKey) throws SCMCallException {
		return getCommandFactory().getGetRepoCommandImpl().getRepositories(projectKey);
	}

	/**
	 * Gets a single repo from a project
	 * 
	 * @param projectKey
	 *            the owning project key
	 * @param repositorySlug
	 *            the repository slug
	 * @return the repository info
	 * @throws SCMCallException
	 */
	public static LocalRepoBean getRepository(final String projectKey, final String repositorySlug)
			throws SCMCallException {
		return getCommandFactory().getGetRepoCommandImpl().getRepository(projectKey, repositorySlug);
	}

	/**
	 * Gets all repositories in the SCM system.
	 * Warning: this is probably expensive.
	 * 
	 * @return a mapping of projects to repositories
	 * @throws SCMCallException
	 */
	public static Map<String, Collection<LocalRepoBean>> getAllRepositories() throws SCMCallException {
		return getCommandFactory().getGetRepoCommandImpl().getAllRepositories();
	}

	/**
	 * Creates a repo in the specified project
	 * 
	 * @param projectKey
	 *            the project
	 * @param name
	 *            the name of the repo
	 * @return a RepoBean populated with extra information from the SCM system
	 * @throws SCMCallException
	 */
	public static LocalRepoBean createRepo(final String projectKey, final String name) throws SCMCallException {
		return getCommandFactory().getCreateRepoCommandImpl().createRepo(projectKey, name);
	}

	/**
	 * Deletes a repository from a project
	 * 
	 * @param projectKey
	 *            The project key
	 * @param repoSlug
	 *            the repo slug
	 * @throws SCMCallException
	 */
	public static void deleteRepo(final String projectKey, final String repoSlug) throws SCMCallException {
		getCommandFactory().getDeleteRepoCommandImpl().deleteRepo(projectKey, repoSlug);
	}

	/**
	 * Deletes a repository from a project
	 * 
	 * @param LocalRepoBean
	 *            The source repository
	 * @param LocalRepoBean
	 *            The destination repository
	 * @throws SCMCallException
	 */
	public static void createMergeRequest(final LocalRepoBean source, final LocalRepoBean destination)
			throws SCMCallException {
		getCommandFactory().getMergeRequestCommandImpl().createMergeRequest(source, destination);
	}

	/**
	 * Set a command factory implementation ignoring the configured type in system properties.
	 * It's mostly for injecting a mock for testing but may be useful elsewhere.
	 * 
	 * @param commandFactory
	 *            the command factory implementation.
	 */
	static void setCommandFactoryImplementation(final SCMCommandFactory commandFactory) {
		commandFactoryImplementation = commandFactory;
	}
}
