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
package com.surevine.gateway.scm.scmclient.gitlab;

import com.surevine.gateway.scm.scmclient.CreateProjectCommand;
import com.surevine.gateway.scm.scmclient.CreateRepoCommand;
import com.surevine.gateway.scm.scmclient.DeleteProjectCommand;
import com.surevine.gateway.scm.scmclient.DeleteRepoCommand;
import com.surevine.gateway.scm.scmclient.ForkRepoCommand;
import com.surevine.gateway.scm.scmclient.GetProjectsCommand;
import com.surevine.gateway.scm.scmclient.GetRepoCommand;
import com.surevine.gateway.scm.scmclient.SCMCommandFactory;


/**
 * Gitlab SCM command factory
 * 
 * This being Gitlab, the terms are different.
 * A Gitlab 'group' is a Stash 'project'
 * A Gitlab 'project' is a Stash 'repo'
 */
public class GitlabCommandFactory implements SCMCommandFactory {
    @Override
    public ForkRepoCommand getForkRepoCommandImpl() {
        return new GitlabForkProjectCommand();
    }

    @Override
    public GetProjectsCommand getGetProjectsCommandImpl() {
        return new GitlabGetGroupsCommand();
    }

    @Override
    public CreateProjectCommand getCreateProjectCommandImpl() {
        return new GitlabCreateGroupCommand();
    }

    @Override
    public DeleteProjectCommand getDeleteProjectCommandImpl() {
        return new GitlabDeleteGroupCommand();
    }

    @Override
    public GetRepoCommand getGetRepoCommandImpl() {
        return new GitlabGetProjectCommand();
    }

    @Override
    public CreateRepoCommand getCreateRepoCommandImpl() {
        return new GitlabCreateProjectCommand();
    }

    @Override
    public DeleteRepoCommand getDeleteRepoCommandImpl() {
        return new GitlabDeleteProjectCommand();
    }
}
