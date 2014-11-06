package com.surevine.gateway.scm.scmclient.gitlab;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Random;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.surevine.gateway.scm.util.PropertyUtil;

@RunWith(PowerMockRunner.class)
@PrepareForTest(PropertyUtil.class)
@PowerMockIgnore("javax.net.ssl.*")
public class GitlabCreateProjectCommandTest {

	@PrepareForTest(PropertyUtil.class)
	@Test
	public void testCreateAndDeleteProject() throws Exception {
		GitlabCreateProjectCommand createProjectCommand = new GitlabCreateProjectCommand();

		String randomProjectKey = "PRJ" + new Random().nextInt();

		createProjectCommand.createProject(randomProjectKey);

		GitlabGetProjectsCommand getProjectsCommand = new GitlabGetProjectsCommand();
		assertTrue(getProjectsCommand.getProjects().contains(randomProjectKey));

		GitlabDeleteProjectCommand deleteProjectCommand = new GitlabDeleteProjectCommand();
		deleteProjectCommand.deleteProject(randomProjectKey);

		assertFalse(getProjectsCommand.getProjects().contains(randomProjectKey));
	}

}
