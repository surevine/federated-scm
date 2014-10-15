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
package com.surevine.gateway.scm.model;

import com.surevine.gateway.scm.util.PropertyUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Bean for information related to an existing, or future, local git repository
 * @author nick.leaver@surevine.com
 */
public class RepoBean {
    private String slug;
    private boolean remote;
    private String sourcePartner;
    private String projectKey;
    private String cloneURL;

    public boolean isRemote() {
        return remote;
    }

    public void setRemote(boolean remote) {
        this.remote = remote;
    }

    public String getSourcePartner() {
        return sourcePartner;
    }

    public void setSourcePartner(String sourcePartner) {
        this.sourcePartner = sourcePartner;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(final String slug) {
        this.slug = slug;
    }

    public String getCloneURL() {
        return cloneURL;
    }

    public void setCloneURL(String cloneURL) {
        this.cloneURL = cloneURL;
    }

    public String getProjectKey() {
        return projectKey;
    }

    public void setProjectKey(String projectKey) {
        this.projectKey = projectKey;
    }

    /**
     * Gets the root directory of this repoBean
     * @return a Path to the correct location for this repo's working directory - may or may not exist
     */
    public Path getRepoDirectory() {
        Path repoDir;
        if (isRemote()) {
            String sourcePartner = getSourcePartner();
            repoDir = Paths.get(PropertyUtil.getGitDir(), "from_gateway", sourcePartner,
                    projectKey, getSlug());
        } else {
            repoDir = Paths.get(PropertyUtil.getGitDir(), "local_scm", projectKey, getSlug());
        }

        try {
            Files.createDirectories(repoDir);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return repoDir;
    }

    /**
     * Gets the .git directory for this repo bean
     * @return a Path to the .git directory location for this repository - may or may not exist
     */
    public Path getGitConfigDirectory() {
        return getRepoDirectory().resolve(".git");
    }

    @Override
    public String toString() {
        return "RepoBean{" 
                + "slug='" + slug + '\''
                + ", remote=" + remote
                + ", sourcePartner='" + sourcePartner + '\''
                + ", projectKey='" + projectKey + '\''
                + ", cloneURL='" + cloneURL + '\''
                + '}';
    }
}
