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

import com.surevine.gateway.scm.model.RepoBean;
import com.surevine.gateway.scm.util.PropertyUtil;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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
        testRepo.setCloneURL("ssh://fake_url");
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
        TestUtility.destroyTestRepo(testRepo);
    }
    
    @Test
    public void testBundleMultipleBranches() throws Exception {
        RepoBean repoWithBranches = TestUtility.createTestRepoMultipleBranches();
        Path bundlePath = underTest.bundle(repoWithBranches);
        assertTrue(Files.exists(bundlePath));
        assertTrue(Files.isRegularFile(bundlePath));
        
        RepoBean fromBundle = new RepoBean();
        fromBundle.setCloneURL(bundlePath.toString());
        fromBundle.setProjectKey(repoWithBranches.getProjectKey());
        fromBundle.setSlug(repoWithBranches.getSlug() + "_from_bundle");
        fromBundle.setLocalBare(true);
        
        underTest.clone(fromBundle);

        FileRepositoryBuilder builder = new FileRepositoryBuilder();

        Repository originalRepo = builder.setGitDir(repoWithBranches.getGitConfigDirectory().toFile()).findGitDir().build();
        Git original = new Git(originalRepo);
        List<Ref> originalBranches = original.branchList().call();
        

        Repository fromBundleRepo = builder.setGitDir(fromBundle.getGitConfigDirectory().toFile()).findGitDir().build();
        Git fromBundleGit = new Git(fromBundleRepo);
        List<Ref> fromBundleBraches = fromBundleGit.branchList().call();
        
        int matches = 0;
        for (Ref branchRef:originalBranches) {
            for (Ref fromBundleRef:fromBundleBraches) {
                if (branchRef.getName().equals(fromBundleRef.getName())) {
                    matches++;
                    continue;
                }
            }
        }
        
        assertEquals(matches, originalBranches.size());
        
        TestUtility.destroyTestRepo(fromBundle);
        TestUtility.destroyTestRepo(repoWithBranches);
        Files.deleteIfExists(bundlePath);
    }
        
    @Test
    public void testAddRemote() throws Exception {
        RepoBean testRepo = TestUtility.createTestRepo();
        underTest.addRemote(testRepo, "my_remote", "ssh://my_remote_url");

        Map<String, String> remotes = underTest.getRemotes(testRepo);
        assertTrue(remotes.containsKey("my_remote") && remotes.containsValue("ssh://my_remote_url"));
        TestUtility.destroyTestRepo(testRepo);
    }

    @Test
    public void testUpdateRemote() throws Exception {
        RepoBean testRepo = TestUtility.createTestRepo();
        underTest.addRemote(testRepo, "my_remote", "ssh://my_remote_url");
        underTest.updateRemote(testRepo, "my_remote", "ssh://updated_remote_url");
        Map<String, String> remotes = underTest.getRemotes(testRepo);
        assertTrue(remotes.containsKey("my_remote") && remotes.containsValue("ssh://updated_remote_url"));
        TestUtility.destroyTestRepo(testRepo);
    }
    
    @Test
    public void testFetch() throws Exception {
        RepoBean sourceRepoBean = TestUtility.createTestRepo();
        String sourceDirectory = sourceRepoBean.getRepoDirectory().toString();
        
        RepoBean targetRepoBean = new RepoBean();
        targetRepoBean.setLocalBare(false);
        targetRepoBean.setProjectKey(sourceRepoBean.getProjectKey());
        targetRepoBean.setSlug(sourceRepoBean.getSlug() + "_clone");
        targetRepoBean.setCloneURL(sourceDirectory);
        underTest.clone(targetRepoBean);
        underTest.addRemote(targetRepoBean, "source_repo", sourceDirectory);
        Map<String, String> remotes = underTest.getRemotes(targetRepoBean);
        
        assertTrue(remotes.containsKey("source_repo") && remotes.containsValue(sourceDirectory));
        assertFalse(underTest.fetch(targetRepoBean, "source_repo"));

        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        Repository sourceRepository = builder.setGitDir(sourceRepoBean.getGitConfigDirectory().toFile()).findGitDir().build();
        
        String filename = "should_be_in_both.txt";
        Files.write(sourceRepoBean.getRepoDirectory().resolve(filename), Arrays.asList("Hello World"), StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        Git git = new Git(sourceRepository);
        git.add().addFilepattern(filename).call();
        git.commit().setMessage("Added " + filename).call();
        
        assertTrue(underTest.fetch(targetRepoBean, "source_repo"));
        
        TestUtility.destroyTestRepo(sourceRepoBean);
        TestUtility.destroyTestRepo(targetRepoBean);
    }
}
