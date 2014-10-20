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
package com.surevine.gateway.scm.gatewayclient;

/**
 * @author nick.leaver@surevine.com
 */
public final class SharedRepoIdentification {
    private String projectKey;
    private String repoSlug;
    
    public SharedRepoIdentification() {
        // no-op
    }

    public SharedRepoIdentification(String projectKey, String repoSlug) {
        this.projectKey = projectKey;
        this.repoSlug = repoSlug;
    }

    public String getProjectKey() {
        return projectKey;
    }

    void setProjectKey(final String projectKey) {
        this.projectKey = projectKey;
    }

    public String getRepoSlug() {
        return repoSlug;
    }

    void setRepoSlug(final String repoSlug) {
        this.repoSlug = repoSlug;
    }
    
    public String toString() {
        return projectKey + ":" + repoSlug;
    }
}
