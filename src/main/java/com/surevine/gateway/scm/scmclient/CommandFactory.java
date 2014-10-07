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
     * SCM system specific GetRepoCommand implementation
     * @return SCM system specific GetRepoCommand implementation
     */
    public abstract GetRepoCommand getGetRepoCommand();

    /**
     * SCM system specific CreateRepoCommand implementation
     * @return SCM system specific CreateRepoCommand implementation
     */
    public abstract CreateRepoCommand getCreateRepoCommand();

    /**
     * SCM system specific DeleteRepoCommand implementation.
     * Protected access to restrict to test cleanup. Open up if required.
     * @return SCM system specific DeleteRepoCommand implementation
     */
    protected abstract DeleteRepoCommand getDeleteRepoCommand();

    /**
     * Set a command factory implementation ignoring the configured type in system properties.
     * It's mostly for injecting a mock for testing but may be useful elsewhere.
     * @param commandFactory the command factory implementation.
     */
    static void setCommandFactoryImplementation(final CommandFactory commandFactory) {
        commandFactoryImplementation = commandFactory;
    }
}
