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

import com.surevine.gateway.scm.scmclient.bean.ProjectBean;

/**
 * @author nick.leaver@surevine.com
 */
public interface CreateProjectCommand {
    /**
     * Creates a new project in the SCM system
     * @param projectBean the project details
     * @return The ProjectBean with any additional information from the SCM system (probably the ID)
     * @throws com.surevine.gateway.scm.scmclient.SCMCallException
     */
    ProjectBean createProject(ProjectBean projectBean) throws SCMCallException;
}
