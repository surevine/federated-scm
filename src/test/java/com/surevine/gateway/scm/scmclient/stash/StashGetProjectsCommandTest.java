package com.surevine.gateway.scm.scmclient.stash;

import com.surevine.gateway.scm.scmclient.bean.ProjectBean;
import org.apache.log4j.Logger;
import org.junit.Test;

import java.util.Collection;

/**
 * @author nick.leaver@surevine.com
 */
public class StashGetProjectsCommandTest {
    private static Logger logger = Logger.getLogger(StashGetProjectsCommandTest.class);
    /**
     * Calls the actual configured Stash instance and therefore makes no assumptions about 
     * the return. Fails on exception only.
     */
    @Test
    public void testGetProjects() {
        StashGetProjectsCommand getProjectsCommand = new StashGetProjectsCommand();
        Collection<ProjectBean> projects = getProjectsCommand.getProjects();
        for (ProjectBean b:projects) {
            logger.debug(b);
        }
    }

    /**
     * Calls the actual configured Stash instance and therefore makes no assumptions about 
     * the return. Fails on exception only. Needs getProjects() to work.
     */
    @Test
    public void testGetProject() {
        StashGetProjectsCommand getProjectsCommand = new StashGetProjectsCommand();
        Collection<ProjectBean> projects = getProjectsCommand.getProjects();
        if (projects.size() > 0) {
            String anActualKey = projects.iterator().next().getKey();
            ProjectBean projectBean = getProjectsCommand.getProject(anActualKey);
            logger.debug(projectBean);
        }
    }
}
