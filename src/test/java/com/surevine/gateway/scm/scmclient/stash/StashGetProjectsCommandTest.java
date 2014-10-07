package com.surevine.gateway.scm.scmclient.stash;

import com.surevine.gateway.scm.scmclient.SCMCallException;
import com.surevine.gateway.scm.scmclient.bean.ProjectBean;
import org.apache.log4j.Logger;
import org.junit.Test;

import java.util.Collection;
import java.util.Random;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * @author nick.leaver@surevine.com
 */
public class StashGetProjectsCommandTest {
    private static Logger logger = Logger.getLogger(StashGetProjectsCommandTest.class);

    @Test
    public void testGetProjects() throws SCMCallException {
        StashCreateProjectCommand createProjectCommand = new StashCreateProjectCommand();

        String randomProjectKey = "PRJ" + new Random().nextInt();
        ProjectBean projectBean = new ProjectBean();
        projectBean.setKey(randomProjectKey);
        projectBean.setName(randomProjectKey);

        createProjectCommand.createProject(projectBean);

        StashGetProjectsCommand getProjectsCommand = new StashGetProjectsCommand();
        Collection<ProjectBean> projects = getProjectsCommand.getProjects();
        assertTrue(projects.size() > 0);
        
        for (ProjectBean b:projects) {
            logger.debug(b);
        }

        StashDeleteProjectCommand deleteProjectCommand = new StashDeleteProjectCommand();
        deleteProjectCommand.deleteProject(randomProjectKey);
    }

    @Test
    public void testGetProject() throws SCMCallException {
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

    @Test
    public void testGetProjectThatDoesntExist() {
        StashGetProjectsCommand getProjectsCommand = new StashGetProjectsCommand();
        ProjectBean project = getProjectsCommand.getProject("" + new Random().nextInt());
        
        assertNull(project);
    }
}
