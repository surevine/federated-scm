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
package com.surevine.gateway.scm.scmclient.stash;

import com.surevine.gateway.scm.scmclient.SCMCallException;
import com.surevine.gateway.scm.model.ProjectBean;
import com.surevine.gateway.scm.model.RepoBean;
import org.junit.Test;

import java.util.Collection;
import java.util.Map;
import java.util.Random;

import static org.junit.Assert.*;

/**
 * @author nick.leaver@surevine.com
 */
public class StashGetRepoCommandTest {
    @Test
    public void testGetRepos() throws SCMCallException {
        StashCreateRepoCommand createRepoCommand = new StashCreateRepoCommand();

        // create a dummy project
        StashCreateProjectCommand createProjectCommand = new StashCreateProjectCommand();
        String randomProjectKey = "PRJ" + new Random().nextInt();
        ProjectBean projectBean = new ProjectBean();
        projectBean.setKey(randomProjectKey);
        projectBean.setName(randomProjectKey);
        createProjectCommand.createProject(projectBean);

        // create a couple of repo
        String randomRepoName1 = "REP" + new Random().nextInt();
        String randomRepoName2 = "REP" + new Random().nextInt();
        String slug1 = createRepoCommand.createRepo(randomProjectKey,randomRepoName1).getSlug();
        String slug2 = createRepoCommand.createRepo(randomProjectKey,randomRepoName2).getSlug();
        
        StashGetRepoCommand getRepoCommand = new StashGetRepoCommand();
        Collection<RepoBean> repos = getRepoCommand.getRepositories(randomProjectKey);
        assertEquals(2, repos.size());
        
        // clean up dummy project and repos
        StashDeleteRepoCommand deleteRepoCommand = new StashDeleteRepoCommand();
        deleteRepoCommand.deleteRepo(randomProjectKey, slug1);
        deleteRepoCommand.deleteRepo(randomProjectKey, slug2);
        StashDeleteProjectCommand deleteProjectCommand = new StashDeleteProjectCommand();
        deleteProjectCommand.deleteProject(randomProjectKey);
    }

    @Test
    public void testGetReposForAllProjects() throws SCMCallException {
        StashCreateRepoCommand createRepoCommand = new StashCreateRepoCommand();

        // create a dummy project
        StashCreateProjectCommand createProjectCommand = new StashCreateProjectCommand();
        String randomProjectKey1 = "PRJ" + new Random().nextInt();
        ProjectBean projectBean1 = new ProjectBean();
        projectBean1.setKey(randomProjectKey1);
        projectBean1.setName(randomProjectKey1);
        createProjectCommand.createProject(projectBean1);
        String randomProjectKey2 = "PRJ" + new Random().nextInt();
        ProjectBean projectBean2 = new ProjectBean();
        projectBean2.setKey(randomProjectKey2);
        projectBean2.setName(randomProjectKey2);
        createProjectCommand.createProject(projectBean2);

        // create some repos
        String randomRepoName1 = "REP" + new Random().nextInt();
        String randomRepoName2 = "REP" + new Random().nextInt();
        String randomRepoName3 = "REP" + new Random().nextInt();
        String randomRepoName4 = "REP" + new Random().nextInt();
        String slug1 = createRepoCommand.createRepo(randomProjectKey1,randomRepoName1).getSlug();
        String slug2 = createRepoCommand.createRepo(randomProjectKey1,randomRepoName2).getSlug();
        String slug3 = createRepoCommand.createRepo(randomProjectKey2,randomRepoName3).getSlug();
        String slug4 = createRepoCommand.createRepo(randomProjectKey2,randomRepoName4).getSlug();

        StashGetRepoCommand getRepoCommand = new StashGetRepoCommand();
        Map<ProjectBean,Collection<RepoBean>> repos = getRepoCommand.getAllRepositories();
        
        assertTrue(repos.size() >= 2);

        // clean up dummy project and repos
        StashDeleteRepoCommand deleteRepoCommand = new StashDeleteRepoCommand();
        deleteRepoCommand.deleteRepo(randomProjectKey1, slug1);
        deleteRepoCommand.deleteRepo(randomProjectKey1, slug2);
        deleteRepoCommand.deleteRepo(randomProjectKey2, slug3);
        deleteRepoCommand.deleteRepo(randomProjectKey2, slug4);
        
        StashDeleteProjectCommand deleteProjectCommand = new StashDeleteProjectCommand();
        deleteProjectCommand.deleteProject(randomProjectKey1);
        deleteProjectCommand.deleteProject(randomProjectKey2);
    }

    @Test
    public void testGetRepo() throws SCMCallException {
        StashCreateRepoCommand createRepoCommand = new StashCreateRepoCommand();

        // create a dummy project
        StashCreateProjectCommand createProjectCommand = new StashCreateProjectCommand();
        String randomProjectKey = "PRJ" + new Random().nextInt();
        ProjectBean projectBean = new ProjectBean();
        projectBean.setKey(randomProjectKey);
        projectBean.setName(randomProjectKey);
        createProjectCommand.createProject(projectBean);

        // create a repo
        String randomRepoName = "REP" + new Random().nextInt();
        RepoBean response = createRepoCommand.createRepo(randomProjectKey,randomRepoName);
        StashGetRepoCommand getRepoCommand = new StashGetRepoCommand();
        assertNotNull(getRepoCommand.getRepository(randomProjectKey,response.getSlug()));

        // clean up dummy project and repo
        StashDeleteRepoCommand deleteRepoCommand = new StashDeleteRepoCommand();
        deleteRepoCommand.deleteRepo(randomProjectKey, response.getSlug());
        StashDeleteProjectCommand deleteProjectCommand = new StashDeleteProjectCommand();
        deleteProjectCommand.deleteProject(randomProjectKey);
    }

    @Test
    public void testGetRepoThatDoesntExist() {
        StashGetProjectsCommand getProjectsCommand = new StashGetProjectsCommand();
        ProjectBean project = getProjectsCommand.getProject("" + new Random().nextInt());

        assertNull(project);
    }
}
