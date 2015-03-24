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
import com.surevine.gateway.scm.model.LocalRepoBean;
import com.surevine.gateway.scm.util.PropertyUtil;
import com.surevine.gateway.scm.util.StringUtil;
import org.apache.log4j.Logger;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.FetchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.api.TagCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Config;
import org.eclipse.jgit.lib.NullProgressMonitor;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.FetchResult;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.RemoteRefUpdate;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author nick.leaver@surevine.com
 */
public class JGitGitFacade extends GitFacade {

    private static final Logger LOGGER = Logger.getLogger(JGitGitFacade.class);

    @Override
    public void push(final LocalRepoBean repoBean, final String remoteName) throws GitException {
        try {
            FileRepositoryBuilder builder = new FileRepositoryBuilder();
            Repository repository = builder.setGitDir(repoBean.getRepoDirectory().toFile()).findGitDir().build();
            Git git = new org.eclipse.jgit.api.Git(repository);

            // dry run the push - if the push cannot be done automatically we have no way to recover so we just log
            // and throw an exception
            PushCommand dryRunPushCommand = git.push();
            dryRunPushCommand.setRemote(remoteName);
            dryRunPushCommand.setDryRun(true);
            Iterable<PushResult> dryRunResult = dryRunPushCommand.call();
            for (PushResult result:dryRunResult) {
                for (RemoteRefUpdate remoteRefUpdate:result.getRemoteUpdates()) {
                    switch (remoteRefUpdate.getStatus()) {
                        case OK:
                        case UP_TO_DATE:
                            continue;
                        default:
                            throw new GitException("During a dry run of a push one of the updates would have failed "
                                    + "so the push was aborted for repo " + repoBean + " to remote "
                                    + dryRunPushCommand.getRemote());
                    }
                }
            }

            // if we get to this point the dry run was OK so try the real thing
            PushCommand realPushCommand = git.push();
            realPushCommand.setRemote(remoteName);
            Iterable<PushResult> pushResults = realPushCommand.call();
            for (PushResult result:pushResults) {
                for (RemoteRefUpdate remoteRefUpdate:result.getRemoteUpdates()) {
                    switch (remoteRefUpdate.getStatus()) {
                        case OK:
                        case UP_TO_DATE:
                            continue;
                        default:
                            throw new GitException("Push failed for " + repoBean + " to remote "
                                    + dryRunPushCommand.getRemote());
                    }
                }
            }
        } catch (GitAPIException | IOException e) {
            LOGGER.error(e);
            throw new GitException(e);
        }
    }

    @Override
    public Map<String, String> getRemotes(final LocalRepoBean repoBean) throws GitException {
        Map<String, String> remotes = new HashMap<String, String>();
        try {
            FileRepositoryBuilder builder = new FileRepositoryBuilder();
            Repository repository = builder.setGitDir(repoBean.getGitConfigDirectory().toFile()).findGitDir().build();
            Git git = new org.eclipse.jgit.api.Git(repository);
            StoredConfig config = git.getRepository().getConfig();
            Set<String> remoteNames = config.getSubsections("remote");
            for (String remoteName:remoteNames) {
                remotes.put(remoteName, config.getString("remote", remoteName, "url"));
            }
        } catch (IOException e) {
            LOGGER.error(e);
            throw new GitException(e);
        }
        return remotes;
    }

    @Override
    public void addRemote(final LocalRepoBean repoBean, final String remoteName, final String remoteURL) throws GitException {
        try {
            FileRepositoryBuilder builder = new FileRepositoryBuilder();
            Repository repository = builder.setGitDir(repoBean.getGitConfigDirectory().toFile()).findGitDir().build();
            Git git = new org.eclipse.jgit.api.Git(repository);
            StoredConfig config = git.getRepository().getConfig();
            config.setString("remote", remoteName, "url", remoteURL);
            config.setString("remote", remoteName, "fetch", String.format("+refs/heads/*:refs/remotes/%s/*", remoteName));
            config.save();

        } catch (IOException e) {
            LOGGER.error(e);
            throw new GitException(e);
        }
    }

    @Override
    public void updateRemote(final LocalRepoBean repoBean, final String remoteName, final String remoteURL) throws GitException {
        // same process as adding for jgit
        addRemote(repoBean, remoteName, remoteURL);
    }

    @Override
    public void clone(final LocalRepoBean repoBean) throws GitException {
        CloneCommand cloneCommand = new CloneCommand();
        cloneCommand.setDirectory(repoBean.getRepoDirectory().toFile());
        cloneCommand.setURI(repoBean.getCloneSourceURI());
        cloneCommand.setBare(repoBean.isLocalBare());
        try {
            cloneCommand.call();
        } catch (GitAPIException e) {
            LOGGER.error(e);
            throw new GitException(e);
        }
    }

    @Override
    public boolean fetch(final LocalRepoBean repoBean, final String remoteName) throws GitException {
        String remoteToPull = (remoteName != null) ? remoteName : "origin";

        FetchResult result;
        try {
            FileRepositoryBuilder builder = new FileRepositoryBuilder();
            Repository repository = builder.setGitDir(repoBean.getGitConfigDirectory().toFile()).findGitDir().build();
            Git git = new org.eclipse.jgit.api.Git(repository);
            FetchCommand fetchCommand = git.fetch();
            fetchCommand.setRemote(remoteToPull);
            fetchCommand.setRefSpecs(new RefSpec("+refs/heads/*:refs/heads/*"));
            result = fetchCommand.call();
            repository.close();
        } catch (GitAPIException | IOException e) {
            LOGGER.error(e);
            throw new GitException(e);
        }

        boolean hadUpdates = !result.getTrackingRefUpdates().isEmpty();
        return hadUpdates;
    }

    @Override
    public void tag(final LocalRepoBean repoBean, final String tag) throws GitException {
        try {
            FileRepositoryBuilder builder = new FileRepositoryBuilder();
            Repository repository = builder.setGitDir(repoBean.getGitConfigDirectory().toFile()).findGitDir().build();
            Git git = new org.eclipse.jgit.api.Git(repository);
            TagCommand tagCommand = git.tag();
            tagCommand.setName(tag);
            tagCommand.call();
            repository.close();
        } catch (GitAPIException | IOException e) {
            LOGGER.error(e);
            throw new GitException(e);
        }
    }

    @Override
    public Path bundle(final LocalRepoBean repoBean) throws GitException {
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
        } catch (IOException e) {
            throw new GitException(e);
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException ioe) {
                    LOGGER.error(ioe);
                }
            }
        }
    }

    @Override
    public boolean repoAlreadyCloned(final LocalRepoBean repoBean) throws GitException {
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
                alreadyCloned = originURL != null && repoBean.getCloneSourceURI() != null
                        && originURL.equals(repoBean.getCloneSourceURI());
                repository.close();
            } catch (IOException e) {
                LOGGER.error(e);
                throw new GitException(e);
            }
        }
        return alreadyCloned;
    }

    @Override
    public boolean isRepoEmpty(LocalRepoBean repoBean) throws GitException {
    	FileRepositoryBuilder builder = new FileRepositoryBuilder();
    	Repository repository = null;
    	try {
    		repository = builder.setGitDir(repoBean.getRepoDirectory().toFile()).findGitDir().build();
    		return repository == null ||
    				repository.getRef("HEAD") == null ||
    				repository.getRef("HEAD").getObjectId() == null;
    	} catch(IOException e) {
    		throw new GitException("Error detecting whether repository is empty.", e);
    	}
    }


}
