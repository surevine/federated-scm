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
import org.apache.log4j.Logger;
import org.junit.Test;

import java.util.Collection;
import java.util.Random;

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

        createProjectCommand.createProject(randomProjectKey);

        StashGetProjectsCommand getProjectsCommand = new StashGetProjectsCommand();
        Collection<String> projects = getProjectsCommand.getProjects();
        assertTrue(projects.contains(randomProjectKey));
        
        for (String projectKey:projects) {
            logger.debug(projectKey);
        }

        StashDeleteProjectCommand deleteProjectCommand = new StashDeleteProjectCommand();
        deleteProjectCommand.deleteProject(randomProjectKey);
    }
}
