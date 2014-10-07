package com.surevine.gateway.scm.scmclient.stash;

import com.surevine.gateway.scm.scmclient.*;

/**
 * Stash command factory
 * @author nick.leaver@surevine.com
 */
public class StashCommandFactory extends CommandFactory {

    @Override
    public GetProjectsCommand getGetProjectsCommand() {
        return new StashGetProjectsCommand();
    }

    @Override
    public CreateProjectCommand getCreateProjectCommand() {
        return new StashCreateProjectCommand();
    }
    
    @Override
    protected DeleteProjectCommand getDeleteProjectCommand() {
        return new StashDeleteProjectCommand();
    }

    @Override
    public GetRepoCommand getGetRepoCommand() {
        return new StashGetRepoCommand();
    }

    @Override
    public CreateRepoCommand getCreateRepoCommand() {
        return new StashCreateRepoCommand();
    }

    @Override
    protected DeleteRepoCommand getDeleteRepoCommand() {
        return new StashDeleteRepoCommand();
    }
}
