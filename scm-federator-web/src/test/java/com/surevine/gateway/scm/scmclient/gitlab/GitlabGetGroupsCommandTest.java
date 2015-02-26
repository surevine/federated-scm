package com.surevine.gateway.scm.scmclient.gitlab;

import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.Random;

import org.apache.log4j.Logger;
import org.junit.Test;

import com.surevine.gateway.scm.scmclient.SCMCallException;

public class GitlabGetGroupsCommandTest {
    private static Logger logger = Logger.getLogger(GitlabGetGroupsCommandTest.class);

    @Test
    public void testGetProjects() throws SCMCallException {
        GitlabCreateGroupCommand createProjectCommand = new GitlabCreateGroupCommand();

        String randomProjectKey = "PRJ" + new Random().nextInt();

        createProjectCommand.createProject(randomProjectKey);

        GitlabGetGroupsCommand getProjectsCommand = new GitlabGetGroupsCommand();
        Collection<String> projects = getProjectsCommand.getProjects();
        assertTrue(projects.contains(randomProjectKey));
        
        for (String projectKey:projects) {
            logger.debug(projectKey);
        }

        GitlabDeleteGroupCommand deleteProjectCommand = new GitlabDeleteGroupCommand();
        deleteProjectCommand.deleteProject(randomProjectKey);
    }

}
