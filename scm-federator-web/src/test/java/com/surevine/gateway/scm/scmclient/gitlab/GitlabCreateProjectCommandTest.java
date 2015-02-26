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
		GitlabCreateGroupCommand createProjectCommand = new GitlabCreateGroupCommand();

		String randomProjectKey = "PRJ" + new Random().nextInt();

		createProjectCommand.createProject(randomProjectKey);

		GitlabGetGroupsCommand getProjectsCommand = new GitlabGetGroupsCommand();
		assertTrue(getProjectsCommand.getProjects().contains(randomProjectKey));

		GitlabDeleteGroupCommand deleteProjectCommand = new GitlabDeleteGroupCommand();
		deleteProjectCommand.deleteProject(randomProjectKey);

		assertFalse(getProjectsCommand.getProjects().contains(randomProjectKey));
	}

}
