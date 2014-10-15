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
import com.surevine.gateway.scm.model.RepoBean;
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

    @Override
    public boolean push(final RepoBean repoBean, final String remoteName) throws GitException {
        throw new UnsupportedOperationException("Not yet implemented");        
    }

    @Override
    public void addRemote(final RepoBean repoBean, final String remoteName, final String remoteURL) throws GitException {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void clone(final RepoBean repoBean) throws GitException {
        CloneCommand cloneCommand = new CloneCommand();
        cloneCommand.setDirectory(repoBean.getRepoDirectory().toFile());
        cloneCommand.setURI(repoBean.getCloneURL());
        try {
            cloneCommand.call();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new GitException(e);
        }
    }

    @Override
    public boolean pull(final RepoBean repoBean, final String remoteName) throws GitException {
        String remoteToPull = (remoteName == null) ? remoteName : "origin";
        PullResult result;
        try {
            FileRepositoryBuilder builder = new FileRepositoryBuilder();
            Repository repository = builder.setGitDir(repoBean.getRepoDirectory().toFile()).findGitDir().build();
            Git git = new org.eclipse.jgit.api.Git(repository);
            PullCommand pullCommand = git.pull();
            pullCommand.setRemote(remoteToPull);
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
            FileRepositoryBuilder builder = new FileRepositoryBuilder();
            Repository repository = builder.setGitDir(repoBean.getGitConfigDirectory().toFile()).findGitDir().build();
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
            FileRepositoryBuilder builder = new FileRepositoryBuilder();
            Repository repository = builder.setGitDir(repoBean.getGitConfigDirectory().toFile()).findGitDir().build();
            BundleWriter bundleWriter = new BundleWriter(repository);
            String fileName = StringUtil.cleanStringForFilePath(repoBean.getProjectKey()
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
        
        // if the enclosing directory exists then examine the repository to check it's the right one
        if (Files.exists(repoBean.getRepoDirectory())) {
            try {
                // load the repository into jgit
                FileRepositoryBuilder builder = new FileRepositoryBuilder();
                Repository repository = 
                        builder.setGitDir(repoBean.getGitConfigDirectory().toFile()).findGitDir().build();
                
                // examine the repository configuration and confirm whether it has a remote named "origin"
                // that points to the clone URL in the argument repo information. If it does the repo has 
                // already been cloned.
                Config storedConfig = repository.getConfig();
                String originURL = storedConfig.getString("remote", "origin", "url");        
                alreadyCloned = originURL != null && repoBean.getCloneURL() != null
                        && originURL.equals(repoBean.getCloneURL());
                repository.close();
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                throw new GitException(e);
            }
        }
        return alreadyCloned;
    }

    
}
