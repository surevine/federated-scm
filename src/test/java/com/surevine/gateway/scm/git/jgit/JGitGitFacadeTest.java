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
    private static String tmpRepoRootPath = PropertyUtil.getGitDir();
    private static String projectKey = "testproject";
    private static String repoSlug = "testrepo";
    private static String repoURL = "ssh://fake_url";
    private JGitGitFacade underTest = new JGitGitFacade();
    private RepoBean repoBean;
    
    @Before
    public void setup() throws Exception {
        Path repoPath = Paths.get(tmpRepoRootPath, projectKey, repoSlug);
        Files.createDirectories(repoPath);
        Repository repo = new FileRepository(repoPath.resolve(".git").toFile());
        repo.create();
        StoredConfig config = repo.getConfig();
        config.setString("remote", "origin", "url", repoURL);
        config.save();

        repoBean = new RepoBean();
        ProjectBean projectBean = new ProjectBean();
        projectBean.setKey(projectKey);
        repoBean.setProject(projectBean);
        repoBean.setSlug(repoSlug);

        Map<String, List<RepoBean.Link>> links = new HashMap<String, List<RepoBean.Link>>();
        RepoBean.Link link = new RepoBean.Link();
        link.setName("ssh");
        link.setHref(repoURL);
        links.put("clone", Arrays.asList(link));
        repoBean.setLinks(links);

        Git git = new Git(repo);
        
        for (int i = 0; i < 3; i++) {
            String filename = "newfile" + i + ".txt";
            Files.write(repoPath.resolve(filename), Arrays.asList("Hello World"), StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            git.add().addFilepattern(filename).call();
            git.commit().setMessage("Added " + filename).call();
        }
        
        repo.close();
    }
    
    @After
    public void removeTestRepo() throws Exception {
        FileUtils.deleteDirectory(Paths.get(tmpRepoRootPath, projectKey).toFile());
    }
    
    @Test
    public void testAlreadyCloned() throws Exception {
        assertTrue(underTest.repoAlreadyCloned(repoBean));
    }
    
    @Test
    public void testBundle() throws Exception {
        //underTest.bundle(repoBean, null);
    }
}
