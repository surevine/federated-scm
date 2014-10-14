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

import com.surevine.gateway.scm.git.GitException;
import com.surevine.gateway.scm.git.GitFacade;
import com.surevine.gateway.scm.scmclient.bean.RepoBean;
import com.surevine.gateway.scm.util.PropertyUtil;
import com.surevine.gateway.scm.util.StringUtil;
import org.apache.log4j.Logger;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.api.PullCommand;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.RebaseResult;
import org.eclipse.jgit.api.TagCommand;
import org.eclipse.jgit.lib.Config;
import org.eclipse.jgit.lib.NullProgressMonitor;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

/**
 * @author nick.leaver@surevine.com
 */
public class JGitGitFacade extends GitFacade {
    private static Logger logger = Logger.getLogger(JGitGitFacade.class);
    private Path gitDirectoryPath;
    
    public JGitGitFacade() {
        this.gitDirectoryPath = Paths.get(PropertyUtil.getGitDir());
    }

    @Override
    public boolean push(final RepoBean repoBean) throws GitException {
        throw new UnsupportedOperationException("Not yet implemented");        
    }

    @Override
    public void clone(final RepoBean repoBean) throws GitException {
        CloneCommand cloneCommand = new CloneCommand();
        cloneCommand.setDirectory(getRepoRootDirectory(repoBean).toFile());
        cloneCommand.setURI(repoBean.getRepoCloneURL());
        try {
            cloneCommand.call();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new GitException(e);
        }
    }

    @Override
    public boolean pull(final RepoBean repoBean) throws GitException {
        PullResult result;
        try {
            Path gitPath = getRepoGitDirectory(repoBean);
            FileRepositoryBuilder builder = new FileRepositoryBuilder();
            Repository repository = builder.setGitDir(gitPath.toFile()).findGitDir().build();
            Git git = new org.eclipse.jgit.api.Git(repository);
            PullCommand pullCommand = git.pull();
            pullCommand.setRemote("origin"); // always assume origin is the remote we want here
            result = pullCommand.call();
            repository.close();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new GitException(e);
        }
        
        boolean hadUpdates = false;            
        
        if (result != null && result.isSuccessful()) {
            if (result.getMergeResult() != null) {
                hadUpdates = !MergeResult.MergeStatus.ALREADY_UP_TO_DATE
                        .equals(result.getMergeResult().getMergeStatus());
            } else {
                hadUpdates = !RebaseResult.Status.UP_TO_DATE.equals(result.getRebaseResult().getStatus());
            }
        }
        
        return hadUpdates;
    }

    @Override
    public void tag(final RepoBean repoBean, final String tag) throws GitException {
        try {
            Path gitPath = getRepoGitDirectory(repoBean);
            FileRepositoryBuilder builder = new FileRepositoryBuilder();
            Repository repository = builder.setGitDir(gitPath.toFile()).findGitDir().build();
            Git git = new org.eclipse.jgit.api.Git(repository);
            TagCommand tagCommand = git.tag();
            tagCommand.setName(tag);
            tagCommand.call();
            repository.close();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new GitException(e);
        }
    }

    @Override
    public Path bundle(final RepoBean repoBean) throws GitException {
        OutputStream outputStream = null;
        try {
            Path gitPath = getRepoGitDirectory(repoBean);
            FileRepositoryBuilder builder = new FileRepositoryBuilder();
            Repository repository = builder.setGitDir(gitPath.toFile()).findGitDir().build();
            BundleWriter bundleWriter = new BundleWriter(repository);
            String fileName = StringUtil.cleanStringForFilePath(repoBean.getProject().getKey()
                    + "_" + repoBean.getSlug()) + ".bundle";
            Path outputPath = Paths.get(PropertyUtil.getTempDir(), fileName);
            outputStream = Files.newOutputStream(outputPath);
            
            Map<String, Ref> refMap = repository.getAllRefs();
            for (Ref ref:refMap.values()) {
                bundleWriter.include(ref);
            }

            bundleWriter.writeBundle(NullProgressMonitor.INSTANCE, outputStream);
            outputStream.close();
            repository.close();
            return outputPath;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new GitException(e);
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException ioe) {
                    logger.error(ioe);
                }
            }
        }
    }

    @Override
    public boolean repoAlreadyCloned(final RepoBean repoBean) throws GitException {
        boolean alreadyCloned = false;
        
        // get the location of where the git repository should be (/configured_repo_dir/project_key/repo_slug)
        Path repoDirectoryPath = getRepoRootDirectory(repoBean);
        
        // if the enclosing directory exists then examine the repository to check it's the right one
        if (Files.exists(repoDirectoryPath)) {
            try {
                // get the .git directory for this repository - jgit needs to use this
                Path gitPath = getRepoGitDirectory(repoBean);
                
                // load the repository into jgit
                FileRepositoryBuilder builder = new FileRepositoryBuilder();
                Repository repository = builder.setGitDir(gitPath.toFile()).findGitDir().build();
                
                // examine the repository configuration and confirm whether it has a remote named "origin"
                // that points to the clone URL in the argument repo information. If it does the repo has 
                // already been cloned.
                Config storedConfig = repository.getConfig();
                String originURL = storedConfig.getString("remote", "origin", "url");        
                alreadyCloned = originURL != null && repoBean.getRepoCloneURL() != null
                        && originURL.equals(repoBean.getRepoCloneURL());
                repository.close();
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                throw new GitException(e);
            }
        }
        return alreadyCloned;
    }

    /**
     * Gets the enclosing directory of a git repository
     * @param repoBean the repo information
     * @return a Path to the directory
     */
    private Path getRepoRootDirectory(final RepoBean repoBean) {
        return gitDirectoryPath.resolve(Paths.get(repoBean.getProject().getKey(), repoBean.getSlug()));
    }

    /**
     * Gets the .git directory for a repository
     * @param repoBean the repo information
     * @return a Path to the .git directory
     */
    private Path getRepoGitDirectory(final RepoBean repoBean) {
        return getRepoRootDirectory(repoBean).resolve(".git");
    }
}
