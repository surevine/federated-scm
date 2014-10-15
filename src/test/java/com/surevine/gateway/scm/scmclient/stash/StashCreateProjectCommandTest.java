/*
 * Copyright (C) 2008-2014 Surevine Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/
package com.surevine.gateway.scm.scmclient.stash;

import com.surevine.gateway.scm.scmclient.SCMCallException;
import com.surevine.gateway.scm.model.ProjectBean;
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
