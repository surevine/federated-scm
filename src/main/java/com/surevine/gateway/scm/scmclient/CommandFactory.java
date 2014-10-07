package com.surevine.gateway.scm.scmclient;

import com.surevine.gateway.scm.scmclient.stash.StashCommandFactory;
import com.surevine.gateway.scm.util.PropertyUtil;

/**
 * Factory for obtaining SCM system commands
 * @author nick.leaver@surevine.com
 */
public abstract class CommandFactory {
    private static CommandFactory commandFactoryImplementation;
    
    private static CommandFactory getInstance() {
        if (commandFactoryImplementation == null) {
            switch (PropertyUtil.getSCMType()) {
                case STASH:
                    commandFactoryImplementation = new StashCommandFactory();
                    break;
                default:
                    break;
            }
        }
        
        return commandFactoryImplementation;
    }

    /**
     * SCM system specific GetProjectsCommand implementation
     * @return SCM system specific GetProjectsCommand implementation
     */
    public abstract GetProjectsCommand getGetProjectsCommand();

    /**
     * SCM system specific CreateProjectCommand implementation
     * @return SCM system specific CreateProjectCommand implementation
     */
    public abstract CreateProjectCommand getCreateProjectCommand();

    /**
     * SCM system specific DeleteProjectCommand implementation.
     * Protected access to restrict to test cleanup. Open up if required.
     * @return SCM system specific DeleteProjectCommand implementation
     */
    protected abstract DeleteProjectCommand getDeleteProjectCommand();

    /**
     * Set a command factory implementation ignoring the configured type in system properties.
     * It's mostly for injecting a mock for testing but may be useful elsewhere.
     * @param commandFactory the command factory implementation.
     */
    static void setCommandFactoryImplementation(CommandFactory commandFactory) {
        commandFactoryImplementation = commandFactory;
    }
}
