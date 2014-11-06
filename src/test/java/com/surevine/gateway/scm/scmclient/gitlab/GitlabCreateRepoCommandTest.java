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
package com.surevine.gateway.scm.scmclient.gitlab;

import com.surevine.gateway.scm.model.LocalRepoBean;
import com.surevine.gateway.scm.scmclient.SCMCallException;
import org.apache.log4j.Logger;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertNotNull;

/**
 * @author nick.leaver@surevine.com
 */
public class GitlabCreateRepoCommandTest {
    private static Logger logger = Logger.getLogger(GitlabCreateRepoCommandTest.class);
    
    @Test
    public void testCreateAndDeleteRepo() throws SCMCallException {
        GitlabCreateProjectCommand createRepoCommand = new GitlabCreateProjectCommand();
        
        // create a dummy project
        GitlabCreateGroupCommand createProjectCommand = new GitlabCreateGroupCommand();
        String randomProjectKey = "PRJ" + new Random().nextInt();
        createProjectCommand.createProject(randomProjectKey);
        
        // create a repo
        String randomRepoName = "REP" + new Random().nextInt();
        LocalRepoBean response = createRepoCommand.createRepo(randomProjectKey,randomRepoName);
        logger.debug(response);
        GitlabGetProjectCommand getRepoCommand = new GitlabGetProjectCommand();
        assertNotNull(getRepoCommand.getRepository(randomProjectKey, response.getSlug()));

        // clean up dummy project and repo
        GitlabDeleteProjectCommand deleteRepoCommand = new GitlabDeleteProjectCommand();
        deleteRepoCommand.deleteRepo(randomProjectKey, response.getSlug());
        
        GitlabDeleteGroupCommand deleteProjectCommand = new GitlabDeleteGroupCommand();
        deleteProjectCommand.deleteProject(randomProjectKey);
    }

//    @Test(expected = SCMCallException.class)
//    public void testNoName() throws SCMCallException {
//        new StashCreateRepoCommand().createRepo("foo", null);
//    }
//
//    @Test(expected = SCMCallException.class)
//    public void testNoKey() throws SCMCallException {
//        new StashCreateRepoCommand().createRepo(null,"foo");
//    }
}
