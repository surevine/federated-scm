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
import org.eclipse.jgit.api.PullCommand;
import org.eclipse.jgit.api.TagCommand;
import org.eclipse.jgit.lib.Config;
import org.eclipse.jgit.lib.NullProgressMonitor;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.revwalk.filter.RevFilter;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.BundleWriter;

import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;

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
    public void pull(final RepoBean repoBean) throws GitException {
        try {
            Path gitPath = getRepoGitDirectory(repoBean);
            FileRepositoryBuilder builder = new FileRepositoryBuilder();
            Repository repository = builder.setGitDir(gitPath.toFile()).findGitDir().build();
            Git git = new org.eclipse.jgit.api.Git(repository);
            PullCommand pullCommand = git.pull();
            pullCommand.setRemote("origin"); // always assume origin is the remote we want here
            pullCommand.call();
            repository.close();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new GitException(e);
        }
    }

    @Override
    public void tag(RepoBean repoBean, String tag) throws GitException {
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
    public void bundle(RepoBean repoBean, String baseTagName) throws GitException {
        try {
            Path gitPath = getRepoGitDirectory(repoBean);
            FileRepositoryBuilder builder = new FileRepositoryBuilder();
            Repository repository = builder.setGitDir(gitPath.toFile()).findGitDir().build();
            Git git = new Git(repository);

            BundleWriter bundleWriter = new BundleWriter(repository);

            RevWalk walk = new RevWalk(repository);
            RevCommit baseCommit = null;

            if (baseTagName != null && !baseTagName.isEmpty()) {
                List<Ref> tags = git.tagList().call();
                ObjectId tagObject = null;
                for (Ref tag:tags) {
                    if (tag.getName().equals(baseTagName)) {
                        tagObject = tag.getObjectId();
                    }
                }
                
                if (tagObject != null) {
                    baseCommit = walk.parseCommit(tagObject);
                } else {
                    throw new GitException("The provided baseline tag " + baseTagName + " does not exist in the repo");
                }
            } else {
                Iterable<RevCommit> commits = git.log().setMaxCount(1).call();
                RevCommit firstCommit = commits.iterator().next();
                if (firstCommit != null) {
                    logger.debug("Bundling from first commit:" + firstCommit);
                    baseCommit = walk.parseCommit(firstCommit);
                }
            }
            
            walk.markStart(baseCommit);            

            Iterator<RevCommit> interestedCommits = walk.iterator();
            while (interestedCommits.hasNext()) {
                RevCommit revCommit = interestedCommits.next();

                Ref ref = repository.getRef(revCommit.getName());
                bundleWriter.include(ref);
            }
            
            String fileName = StringUtil.cleanStringForFilePath(repoBean.getProject().getKey()
                    + "_" + repoBean.getSlug()) + ".bundle";
            
            Path outputPath = Paths.get(PropertyUtil.getGatewayExportDir(), fileName);
            OutputStream outputStream = Files.newOutputStream(outputPath);
            bundleWriter.writeBundle(NullProgressMonitor.INSTANCE, outputStream);

            repository.close();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new GitException(e);
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
