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

import com.surevine.gateway.scm.git.GitFacade;
import com.surevine.gateway.scm.git.GitException;
import com.surevine.gateway.scm.scmclient.bean.RepoBean;
import com.surevine.gateway.scm.util.PropertyUtil;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullCommand;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author nick.leaver@surevine.com
 */
public class JGitGitFacade extends GitFacade {
    private Path gitDirectoryPath;
    
    public JGitGitFacade() {
        this.gitDirectoryPath = Paths.get(PropertyUtil.getGitDir());
    }
    
    @Override
    public void clone(final RepoBean repoBean) throws GitException {
        CloneCommand cloneCommand = new CloneCommand();
        cloneCommand.setDirectory(getRepoPath(repoBean).toFile());
        cloneCommand.setURI(repoBean.getRepoCloneURL());
        try {
            cloneCommand.call();
        } catch (Exception e) {
            throw new GitException(e);
        }
    }

    @Override
    public void pull(final RepoBean repoBean) throws GitException {
        try {
            Path gitPath = getRepoGitDirPath(repoBean);
            Repository repository = FileRepositoryBuilder.create(gitPath.toFile());
            Git git = new org.eclipse.jgit.api.Git(repository);
            PullCommand pullCommand = git.pull();
            pullCommand.setRemote("origin");
            pullCommand.call();
        } catch (Exception e) {
            throw new GitException(e);
        }
    }

    @Override
    public boolean repoAlreadyCloned(final RepoBean repoBean) {
        boolean alreadyCloned = false;
        Path repoDirectoryPath = getRepoPath(repoBean);
        if (Files.exists(repoDirectoryPath)) {
            // a directory exists in the correct location
            // TODO check for a remote called "origin" and compare with the provided repository location
            // for now - return true
            return true;
        }
        return alreadyCloned;
    }
    
    private Path getRepoPath(final RepoBean repoBean) {
        return gitDirectoryPath.resolve(Paths.get(repoBean.getProject().getKey(), repoBean.getSlug()));
    }
    
    private Path getRepoGitDirPath(final RepoBean repoBean) {
        return getRepoPath(repoBean).resolve(".git");
    }
}
