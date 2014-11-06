package com.surevine.gateway.scm.scmclient.gitlab;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Random;

import org.junit.Test;

public class GitlabCreateProjectCommandTest {

	@Test
	public void testCreateAndDeleteProject() throws Exception {

		GitlabCreateProjectCommand createProjectCommand = new GitlabCreateProjectCommand();

		String randomProjectKey = "PRJ" + new Random().nextInt();

		createProjectCommand.createProject(randomProjectKey);

//		GitlabGetProjectsCommand getProjectsCommand = new GitlabGetProjectsCommand();
//		assertTrue(getProjectsCommand.getProjects().contains(randomProjectKey));
//
//		GitlabDeleteProjectCommand deleteProjectCommand = new GitlabDeleteProjectCommand();
//		deleteProjectCommand.deleteProject(randomProjectKey);
//
//		assertFalse(getProjectsCommand.getProjects().contains(randomProjectKey));
	}

}
