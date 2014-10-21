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

/**
 * @author nick.leaver@surevine.com
 */
public interface SCMCommandFactory {
    /**
     * SCM system specific ForkRepoCommand implementation
     * @return SCM system specific ForkRepoCommand implementation
     */
    ForkRepoCommand getForkRepoCommandImpl();

    /**
     * SCM system specific GetProjectsCommand implementation
     * @return SCM system specific GetProjectsCommand implementation
     */
    GetProjectsCommand getGetProjectsCommandImpl();

    /**
     * SCM system specific CreateProjectCommand implementation
     * @return SCM system specific CreateProjectCommand implementation
     */
    CreateProjectCommand getCreateProjectCommandImpl();

    /**
     * SCM system specific DeleteProjectCommand implementation.
     * Protected access to restrict to test cleanup. Open up if required.
     * @return SCM system specific DeleteProjectCommand implementation
     */
    DeleteProjectCommand getDeleteProjectCommandImpl();

    /**
     * SCM system specific GetRepoCommand implementation
     * @return SCM system specific GetRepoCommand implementation
     */
    GetRepoCommand getGetRepoCommandImpl();

    /**
     * SCM system specific CreateRepoCommand implementation
     * @return SCM system specific CreateRepoCommand implementation
     */
    CreateRepoCommand getCreateRepoCommandImpl();

    /**
     * SCM system specific DeleteRepoCommand implementation.
     * Protected access to restrict to test cleanup. Open up if required.
     * @return SCM system specific DeleteRepoCommand implementation
     */
    DeleteRepoCommand getDeleteRepoCommandImpl();
}
