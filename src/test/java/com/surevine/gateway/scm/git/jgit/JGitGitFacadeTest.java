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
package com.surevine.gateway.scm.git.jgit;

import com.surevine.gateway.scm.TestUtility;
import com.surevine.gateway.scm.scmclient.bean.ProjectBean;
import com.surevine.gateway.scm.scmclient.bean.RepoBean;
import com.surevine.gateway.scm.util.PropertyUtil;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.StoredConfig;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertTrue;

/**
 * Tests the JGit version of the Git facade
 * @author nick.leaver@surevine.com
 */
public class JGitGitFacadeTest {
    private JGitGitFacade underTest = new JGitGitFacade();
    
    @Test
    public void testAlreadyCloned() throws Exception {
        RepoBean testRepo = TestUtility.createTestRepo();
        assertTrue(underTest.repoAlreadyCloned(testRepo));
        TestUtility.destroyTestRepo(testRepo);
    }
    
    @Test
    public void testBundle() throws Exception {
        RepoBean testRepo = TestUtility.createTestRepo();
        Path bundlePath = underTest.bundle(testRepo);
        assertTrue(Files.exists(bundlePath));
        assertTrue(Files.isRegularFile(bundlePath));
        TestUtility.destroyTestRepo(testRepo);
        Files.deleteIfExists(bundlePath);
    }
}
