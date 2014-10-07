package com.surevine.gateway.scm.scmclient.stash;

import com.surevine.gateway.scm.scmclient.SCMCallException;
import com.surevine.gateway.scm.scmclient.bean.ProjectBean;
import org.apache.log4j.Logger;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertNotNull;

/**
 * @author nick.leaver@surevine.com
 */
public class StashCreateProjectCommandTest {
    private static Logger logger = Logger.getLogger(StashCreateProjectCommandTest.class);
    /**
     * Calls the actual configured Stash instance and therefore makes no assumptions about 
     * the return. Fails on unexpected exception only.
     */
    @Test
    public void testCreateProject() throws SCMCallException {
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
    }

    /**
     * Checks that an exception is thrown if there is no name present
     * @throws SCMCallException
     */
    @Test(expected = SCMCallException.class)
    public void testNoName() throws SCMCallException {
        StashCreateProjectCommand createProjectCommand = new StashCreateProjectCommand();
        ProjectBean projectBean = new ProjectBean();
        projectBean.setKey("key");
        
        new StashCreateProjectCommand().createProject(projectBean);
    }

    /**
     * Checks that an exception is thrown if there is no key present
     * @throws SCMCallException
     */
    @Test(expected = SCMCallException.class)
    public void testNoKey() throws SCMCallException {
        StashCreateProjectCommand createProjectCommand = new StashCreateProjectCommand();
        ProjectBean projectBean = new ProjectBean();
        projectBean.setName("name");

        new StashCreateProjectCommand().createProject(projectBean);
    }

    /**
     * Checks that an exception is thrown if there is an ID already in the bean
     * @throws SCMCallException
     */
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
