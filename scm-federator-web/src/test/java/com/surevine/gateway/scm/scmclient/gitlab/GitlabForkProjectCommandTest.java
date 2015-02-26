package com.surevine.gateway.scm.scmclient.gitlab;

import static org.junit.Assert.assertNotNull;

import java.util.Random;

import org.junit.Test;


public class GitlabForkProjectCommandTest {
	
	@Test
	public void testForkProject() throws Exception {
		
        GitlabCreateGroupCommand createProjectCommand = new GitlabCreateGroupCommand();
        GitlabDeleteGroupCommand deleteProjectCommand = new GitlabDeleteGroupCommand();
        
        GitlabCreateProjectCommand createRepoCommand = new GitlabCreateProjectCommand();
        GitlabDeleteProjectCommand deleteRepoCommand = new GitlabDeleteProjectCommand();
        
        GitlabForkProjectCommand forkRepoCommand = new GitlabForkProjectCommand();
        GitlabGetProjectCommand getRepoCommand = new GitlabGetProjectCommand();
        
        String randomProject1 = "PRJ-1-" + new Random().nextInt();
        String randomProject2 = "PRJ-2-" + new Random().nextInt();
        String repoSlug = "testrepository";
        
        createProjectCommand.createProject(randomProject1);
        createProjectCommand.createProject(randomProject2);
        createRepoCommand.createRepo(randomProject1, repoSlug);
        forkRepoCommand.forkRepo(randomProject1, repoSlug, randomProject2);
        
        assertNotNull(getRepoCommand.getRepository(randomProject1, repoSlug));
        assertNotNull(getRepoCommand.getRepository(randomProject2, repoSlug));
        
        deleteRepoCommand.deleteRepo(randomProject1, repoSlug);
        deleteRepoCommand.deleteRepo(randomProject2, repoSlug);
        deleteProjectCommand.deleteProject(randomProject1);
        deleteProjectCommand.deleteProject(randomProject2);
	}
}
