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
public class StashDeleteProjectCommandTest {
    /**
     * Calls the actual configured Stash instance and therefore makes no assumptions about 
     * the return. Fails on unexpected exception only.
     */
    @Test
    public void testDelete() throws SCMCallException {
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
}
