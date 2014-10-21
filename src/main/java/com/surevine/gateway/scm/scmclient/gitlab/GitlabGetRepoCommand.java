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

import com.surevine.gateway.scm.model.LocalRepoBean;
import com.surevine.gateway.scm.scmclient.GetRepoCommand;
import com.surevine.gateway.scm.scmclient.SCMCallException;

import java.util.Collection;
import java.util.Map;

/**
 * @author nick.leaver@surevine.com
 */
public class GitlabGetRepoCommand implements GetRepoCommand {
    @Override
    public Collection<LocalRepoBean> getRepositories(final String projectKey) {
        return null;
    }

    @Override
    public LocalRepoBean getRepository(final String projectKey, final String repositorySlug) {
        return null;
    }

    @Override
    public Map<String, Collection<LocalRepoBean>> getAllRepositories() throws SCMCallException {
        return null;
    }
}
