package com.surevine.gateway.scm.scmclient.stash;

import com.surevine.gateway.scm.scmclient.CommandFactory;
import com.surevine.gateway.scm.scmclient.CreateProjectCommand;
import com.surevine.gateway.scm.scmclient.DeleteProjectCommand;
import com.surevine.gateway.scm.scmclient.GetProjectsCommand;

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
}
