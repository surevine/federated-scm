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
import com.surevine.gateway.scm.scmclient.bean.ProjectBean;
import com.surevine.gateway.scm.scmclient.bean.RepoBean;
import org.apache.log4j.Logger;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertNotNull;

/**
 * @author nick.leaver@surevine.com
 */
public class StashCreateRepoCommandTest {
    private static Logger logger = Logger.getLogger(StashCreateProjectCommandTest.class);
    
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
        logger.debug(response);
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
