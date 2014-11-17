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
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Bean for information related to an existing, or future, git repository stored in the local filesystem
 * @author nick.leaver@surevine.com
 */
public class LocalRepoBean {
    private String slug;
    private boolean fromGateway;
    private String sourcePartner;
    private String projectKey;
    private String cloneSourceURI;
    private boolean localBare = true;
    private RepoSecurityLabel repoSecurityLabel = new RepoSecurityLabel();

    /**
     * Returns true if this repo was, or should be, created bare
     * @return true if this repo was, or should be, created bare
     */
    public boolean isLocalBare() {
        return localBare;
    }

    /**
     * Sets whether this repo is / should be bare
     * @param localBare if true the local is / should be bare
     */
    public void setLocalBare(final boolean localBare) {
        this.localBare = localBare;
    }

    /**
     * Returns true if this local repository is the result of an incoming repository from the gateway
     * @return true if this local repository is the result of an incoming repository from the gateway
     */
    public boolean isFromGateway() {
        return fromGateway;
    }

    /**
     * Sets whether this repo is from an incoming gateway bundle or not
     * @param fromGateway true if from the gateway
     */
    public void setFromGateway(final boolean fromGateway) {
        this.fromGateway = fromGateway;
    }

    /**
     * Repositories received across the gateway will have a source partner name in their metadata.
     * @return the name of the source partner for this repository. Probably null if this isn't from the gateway.
     */
    public String getSourcePartner() {
        return sourcePartner;
    }

    /**
     * Sets the source parner name for repositories received from the gateway
     * @param sourcePartner the source partner name
     */
    public void setSourcePartner(final String sourcePartner) {
        this.sourcePartner = sourcePartner;
    }

    /**
     * Gets the repository slug for this repository. Should relate to the SCM system slug of this repository,
     * which should be the same on both the partner and local SCM systems. Forms part of the filesystem location of 
     * the local repo.
     * @return the repository slug
     */
    public String getSlug() {
        return slug;
    }

    /**
     * Sets the repository slug
     * @param slug the repository slug
     */
    public void setSlug(final String slug) {
        this.slug = slug;
    }

    /**
     * Gets the URI of the repository the local repository was, or will be, cloned from (if any)
     * @return the URI of the repository the local repository was, or will be, cloned from (if any)
     */
    public String getCloneSourceURI() {
        return cloneSourceURI;
    }

    /**
     * Sets the clone URI for this local repository
     * @param cloneSourceURI /Users/nickl/projects/tpsc/code/federated-scm/src/main/java/com/surevine/gateway/scm/model/LocalRepoBean.java
     */
    public void setCloneSourceURI(final String cloneSourceURI) {
        this.cloneSourceURI = cloneSourceURI;
    }

    /**
     * Gets the project / group / namespace key for this repository. Should relate to the SCM system project of this repository,
     * which should be the same on both the partner and local SCM systems. Forms part of the filesystem location of 
     * the local repo.
     * @return the projecty / group / namespace of this repository
     */
    public String getProjectKey() {
        return projectKey;
    }

    /**
     * Sets the project / group / namespace of this repo
     * @param projectKey the project / group / namespace of this repo
     */
    public void setProjectKey(final String projectKey) {
        this.projectKey = projectKey;
    }

    /**
     * Return true if this local repository exists in the filesystem. Naive check at the moment which simply tests
     * for the presence of a directory in the correct location based on the properties of this bean.
     * @return true if the repo directory exists
     */
    public boolean repoDirectoryExists() {
        return Files.exists(getRepoDirectory());
    }

    /**
     * Gets the root directory of this repoBean.
     * Repos related to repositories cloned from the local SCM are stored at ${fedscm.git.dir}/local_scm/{projectKey}/{slug}
     * Repos related to repositories cloned from incoming partner bundles are stored at ${fedscm.git.dir}/from_gateway/{sourcePartner}/{projectKey}/{slug}
     * 
     * @return a Path to the correct location for this repo's working directory - may or may not exist
     */
    public Path getRepoDirectory() {
        Path repoDir;
        if (isFromGateway()) {
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
        if (isLocalBare()) {
            return getRepoDirectory();
        } else {
            return getRepoDirectory().resolve(".git");
        }
    }

    /**
     * Attempts to delete the local repository directory. Fails silently as there's nothing the system can do to 
     * recover and not deleting the directory shouldn't affect the usual operation of the system.
     */
    public void deleteRepoDirectory() {
        Path localDirectory = getRepoDirectory();
        if (localDirectory != null && Files.exists(localDirectory) && localDirectory.startsWith(Paths.get(PropertyUtil.getGitDir()))) {
            // check that it's a directory inside the system configured git directory before deleting
            try {
                FileUtils.deleteDirectory(localDirectory.toFile());
            } catch (IOException e) {
                // drop the exception
            }
        }
    }
    
    public void emptyRepoDirectory() {
        Path localDirectory = getRepoDirectory();
        try {
        	FileUtils.cleanDirectory(localDirectory.toFile());
        } catch ( IOException e ) {
            // drop the exception
        }
    }

    /**
     * Gets the security label for this repo
     * @return the security label for this repo
     */
    public RepoSecurityLabel getRepoSecurityLabel() {
        return repoSecurityLabel;
    }

    /**
     * Sets a security label for this repo
     * @param repoSecurityLabel a new security label
     */
    public void setRepoSecurityLabel(final RepoSecurityLabel repoSecurityLabel) {
        this.repoSecurityLabel = repoSecurityLabel;
    }

    @Override
    public String toString() {
        return "RepoBean{" 
                + "slug='" + slug + '\''
                + ", fromGateway=" + fromGateway
                + ", sourcePartner='" + sourcePartner + '\''
                + ", projectKey='" + projectKey + '\''
                + ", cloneSourceURI='" + cloneSourceURI + '\''
                + '}';
    }
}
