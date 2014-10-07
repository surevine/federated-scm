package com.surevine.gateway.scm.scmclient.stash;

import com.surevine.gateway.scm.scmclient.SCMCallException;
import com.surevine.gateway.scm.scmclient.bean.ProjectBean;
import com.surevine.gateway.scm.scmclient.bean.RepoBean;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertNotNull;

/**
 * @author nick.leaver@surevine.com
 */
public class StashCreateRepoCommandTest {
    @Test
    public void testCreateAndDeleteRepo() throws SCMCallException {
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

    @Test(expected = SCMCallException.class)
    public void testNoName() throws SCMCallException {
        new StashCreateRepoCommand().createRepo("foo", null);
    }

    @Test(expected = SCMCallException.class)
    public void testNoKey() throws SCMCallException {
        new StashCreateRepoCommand().createRepo(null,"foo");
    }
}
