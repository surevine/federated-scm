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
package com.surevine.gateway.scm.scmclient.bean;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

/**
 * Bean for repo information
 * @author nick.leaver@surevine.com
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class RepoBean {
    private String id;
    private String name;
    private String slug;
    private String scmId = "git";
    private ProjectBean project;

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(final String slug) {
        this.slug = slug;
    }

    public String getScmId() {
        return scmId;
    }

    public void setScmId(final String scmId) {
        this.scmId = scmId;
    }

    public ProjectBean getProject() {
        return project;
    }

    public void setProject(final ProjectBean project) {
        this.project = project;
    }

    @Override
    public String toString() {
        return "RepoBean{"
                + "id='" + id + '\''
                + ", name='" + name + '\''
                + ", slug='" + slug + '\''
                + ", scmId='" + scmId + '\''
                + ", project=" + project
                + '}';
    }
}
