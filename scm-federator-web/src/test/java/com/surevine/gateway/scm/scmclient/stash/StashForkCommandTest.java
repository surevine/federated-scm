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

import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertNotNull;

/**
 * @author nick.leaver@surevine.com
 */
public class StashForkCommandTest {
    @Test
    public void testFork() throws Exception {
        StashCreateProjectCommand createProjectCommand = new StashCreateProjectCommand();
        StashDeleteProjectCommand deleteProjectCommand = new StashDeleteProjectCommand();
        StashCreateRepoCommand createRepoCommand = new StashCreateRepoCommand();
        StashDeleteRepoCommand deleteRepoCommand = new StashDeleteRepoCommand();
        StashForkRepoCommand forkRepoCommand = new StashForkRepoCommand();
        StashGetRepoCommand getRepoCommand = new StashGetRepoCommand();
        
        String randomProject1 = "PRJ" + new Random().nextInt();
        String randomProject2 = "PRJ" + new Random().nextInt();
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
