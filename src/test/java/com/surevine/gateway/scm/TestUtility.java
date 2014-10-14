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
package com.surevine.gateway.scm;

import com.surevine.gateway.scm.scmclient.bean.ProjectBean;
import com.surevine.gateway.scm.scmclient.bean.RepoBean;
import com.surevine.gateway.scm.util.PropertyUtil;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.StoredConfig;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @author nick.leaver@surevine.com
 */
public final class TestUtility {
    private TestUtility() {
        // no-op
    }
    
    public static RepoBean createTestRepo() throws Exception {
        String projectKey = "test_" + UUID.randomUUID().toString();
        String repoSlug = "testRepo";
        String repoURL = "ssh://fake_url";
        Path repoPath = Paths.get(PropertyUtil.getGitDir(), projectKey, repoSlug);
        Files.createDirectories(repoPath);
        Repository repo = new FileRepository(repoPath.resolve(".git").toFile());
        repo.create();
        StoredConfig config = repo.getConfig();
        config.setString("remote", "origin", "url", repoURL);
        config.save();

        RepoBean repoBean = new RepoBean();
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
        return repoBean;
    }
    
    public static void destroyTestRepo(final RepoBean repoBean) throws Exception {
        if (repoBean.getProject().getKey().startsWith("test_")) {
            Path repoTopLevelDir = Paths.get(PropertyUtil.getGitDir(), repoBean.getProject().getKey());
            FileUtils.deleteDirectory(repoTopLevelDir.toFile());
        }
    } 
}
