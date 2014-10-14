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
package com.surevine.gateway.scm.scmclient.stash;

import com.surevine.gateway.scm.scmclient.CommandFactory;
import com.surevine.gateway.scm.scmclient.CreateProjectCommand;
import com.surevine.gateway.scm.scmclient.CreateRepoCommand;
import com.surevine.gateway.scm.scmclient.DeleteProjectCommand;
import com.surevine.gateway.scm.scmclient.DeleteRepoCommand;
import com.surevine.gateway.scm.scmclient.ForkRepoCommand;
import com.surevine.gateway.scm.scmclient.GetProjectsCommand;
import com.surevine.gateway.scm.scmclient.GetRepoCommand;

/**
 * Stash command factory
 * @author nick.leaver@surevine.com
 */
public class StashCommandFactory extends CommandFactory {
    @Override
    public ForkRepoCommand getForkRepoCommand() {
        return null;
    }

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
