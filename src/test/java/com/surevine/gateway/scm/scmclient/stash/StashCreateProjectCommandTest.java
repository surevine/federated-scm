package com.surevine.gateway.scm.scmclient.stash;

import com.surevine.gateway.scm.scmclient.SCMCallException;
import com.surevine.gateway.scm.scmclient.bean.ProjectBean;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * @author nick.leaver@surevine.com
 */
public class StashCreateProjectCommandTest {
    @Test
    public void testCreateAndDeleteProject() throws SCMCallException {
        StashCreateProjectCommand createProjectCommand = new StashCreateProjectCommand();

        String randomProjectKey = "PRJ" + new Random().nextInt();
        ProjectBean projectBean = new ProjectBean();
        projectBean.setKey(randomProjectKey);
        projectBean.setName(randomProjectKey);
        
        createProjectCommand.createProject(projectBean);
        
        StashGetProjectsCommand getProjectsCommand = new StashGetProjectsCommand();
        assertNotNull(getProjectsCommand.getProject(randomProjectKey));

        StashDeleteProjectCommand deleteProjectCommand = new StashDeleteProjectCommand();
        deleteProjectCommand.deleteProject(randomProjectKey);

        assertNull(getProjectsCommand.getProject(randomProjectKey));
    }

    @Test(expected = SCMCallException.class)
    public void testNoName() throws SCMCallException {
        StashCreateProjectCommand createProjectCommand = new StashCreateProjectCommand();
        ProjectBean projectBean = new ProjectBean();
        projectBean.setKey("key");
        
        new StashCreateProjectCommand().createProject(projectBean);
    }

    @Test(expected = SCMCallException.class)
    public void testNoKey() throws SCMCallException {
        StashCreateProjectCommand createProjectCommand = new StashCreateProjectCommand();
        ProjectBean projectBean = new ProjectBean();
        projectBean.setName("name");

        new StashCreateProjectCommand().createProject(projectBean);
    }

    @Test(expected = SCMCallException.class)
    public void testExistingID() throws SCMCallException {
        StashCreateProjectCommand createProjectCommand = new StashCreateProjectCommand();
        ProjectBean projectBean = new ProjectBean();
        projectBean.setName("name");
        projectBean.setKey("key");
        projectBean.setId("shouldn't be a key here");

        new StashCreateProjectCommand().createProject(projectBean);
    }
}
