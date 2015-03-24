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

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.StoredConfig;

import com.surevine.gateway.scm.model.LocalRepoBean;
import com.surevine.gateway.scm.util.PropertyUtil;

/**
 * @author nick.leaver@surevine.com
 */
public final class TestUtility {
	private TestUtility() {
		// no-op
	}

	public static LocalRepoBean createBareClone(final LocalRepoBean nonBareSource) throws Exception {
		final String projectKey = nonBareSource.getProjectKey();
		final String repoSlug = nonBareSource.getSlug() + "_bare";
		final String cloneURL = nonBareSource.getRepoDirectory().toString();
		final Path repoPath = Paths.get(PropertyUtil.getGitDir(), "local_scm", projectKey, repoSlug);
		Files.createDirectories(repoPath.getParent());

		final CloneCommand cloneCommand = new CloneCommand();
		cloneCommand.setBare(true).setCloneAllBranches(true).setURI(cloneURL).call();

		final LocalRepoBean repoBean = new LocalRepoBean();
		repoBean.setProjectKey(projectKey);
		repoBean.setSlug(repoSlug);
		repoBean.setLocalBare(true);
		return repoBean;
	}

	public static LocalRepoBean createTestRepoMultipleBranches() throws Exception {
		final String projectKey = "test_" + UUID.randomUUID().toString();
		final String repoSlug = "testRepo";
		final String remoteURL = "ssh://fake_url";
		final Path repoPath = Paths.get(PropertyUtil.getGitDir(), "local_scm", projectKey, repoSlug);
		Files.createDirectories(repoPath);
		final Repository repo = new FileRepository(repoPath.resolve(".git").toFile());
		repo.create();
		final StoredConfig config = repo.getConfig();
		config.setString("remote", "origin", "url", remoteURL);
		config.save();

		final LocalRepoBean repoBean = new LocalRepoBean();
		repoBean.setProjectKey(projectKey);
		repoBean.setSlug(repoSlug);
		repoBean.setLocalBare(false);

		final Git git = new Git(repo);

		// add some files to some branches
		for (int i = 0; i < 3; i++) {
			final String filename = "newfile" + i + ".txt";
			Files.write(repoPath.resolve(filename), Arrays.asList("Hello World"), StandardCharsets.UTF_8,
					StandardOpenOption.CREATE, StandardOpenOption.APPEND);
			git.add().addFilepattern(filename).call();
			git.commit().setMessage("Added " + filename).call();
		}

		git.checkout().setName("branch1").setCreateBranch(true).call();
		for (int i = 0; i < 3; i++) {
			final String filename = "branch1" + i + ".txt";
			Files.write(repoPath.resolve(filename), Arrays.asList("Hello World"), StandardCharsets.UTF_8,
					StandardOpenOption.CREATE, StandardOpenOption.APPEND);
			git.add().addFilepattern(filename).call();
			git.commit().setMessage("Added " + filename).call();
		}

		git.checkout().setName("master").call();
		git.checkout().setName("branch2").setCreateBranch(true).call();
		for (int i = 0; i < 3; i++) {
			final String filename = "branch2" + i + ".txt";
			Files.write(repoPath.resolve(filename), Arrays.asList("Hello World"), StandardCharsets.UTF_8,
					StandardOpenOption.CREATE, StandardOpenOption.APPEND);
			git.add().addFilepattern(filename).call();
			git.commit().setMessage("Added " + filename).call();
		}

		repo.close();
		return repoBean;
	}

	public static LocalRepoBean createTestRepo() throws Exception {
		final String projectKey = "test_" + UUID.randomUUID().toString();
		final String repoSlug = "testRepo";
		final String remoteURL = "ssh://fake_url";
		final Path repoPath = Paths.get(PropertyUtil.getGitDir(), "local_scm", projectKey, repoSlug);
		Files.createDirectories(repoPath);
		final Repository repo = new FileRepository(repoPath.resolve(".git").toFile());
		repo.create();
		final StoredConfig config = repo.getConfig();
		config.setString("remote", "origin", "url", remoteURL);
		config.save();

		final LocalRepoBean repoBean = new LocalRepoBean();
		repoBean.setProjectKey(projectKey);
		repoBean.setSlug(repoSlug);
		repoBean.setLocalBare(false);
		repoBean.setSourcePartner("partner");

		final Git git = new Git(repo);

		for (int i = 0; i < 3; i++) {
			final String filename = "newfile" + i + ".txt";
			Files.write(repoPath.resolve(filename), Arrays.asList("Hello World"), StandardCharsets.UTF_8,
					StandardOpenOption.CREATE, StandardOpenOption.APPEND);
			git.add().addFilepattern(filename).call();
			git.commit().setMessage("Added " + filename).call();
		}

		git.checkout().setName("master").call();

		repo.close();
		return repoBean;
	}

	public static void destroyTestRepo(final LocalRepoBean repoBean) throws Exception {
		final Path parent = repoBean.getRepoDirectory().getParent();
		if (parent.getFileName().toString().startsWith("test_")) {
			FileUtils.deleteDirectory(repoBean.getRepoDirectory().getParent().toFile());
		}
	}
}
