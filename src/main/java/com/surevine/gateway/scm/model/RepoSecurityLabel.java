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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * @author nick.leaver@surevine.com
 */
public class RepoSecurityLabel {
    private String classification;
    private String decorator;
    private List<String> groups;

    public RepoSecurityLabel() {
        this.groups = new ArrayList<>();

        // TODO: Remove this once a proper security label service is in place
        this.classification = "no_classification";
        this.decorator = "no_decorator";
        this.groups.add("group1");
        this.groups.add("group2");
    }

    public RepoSecurityLabel(final String classification, final String decorator, final String ... groups) {
        this.groups = new ArrayList<>();
        this.classification = classification;
        this.decorator = decorator;
        this.groups.addAll(Arrays.asList(groups));
    }

    public String getClassification() {
        return classification;
    }

    public void setClassification(final String classification) {
        this.classification = classification;
    }

    public String getDecorator() {
        return decorator;
    }

    public void setDecorator(final String decorator) {
        this.decorator = decorator;
    }

    public List<String> getGroups() {
        return groups;
    }

    public void setGroups(final List<String> groups) {
        this.groups = groups;
    }

    /**
     * Gets groups as a comma separated string
     * @return groups as a comma separated string
     */
    public String getGroupString() {
        String groupString = null;
        if (groups != null && groups.size() > 0) {
        	StringBuilder groupBuilder = new StringBuilder();
            Iterator<String> groupIterator = groups.iterator();
            if (groupIterator.hasNext()) {
                groupBuilder.append(groupIterator.next());
            }

            while (groupIterator.hasNext()) {
                groupBuilder.append(",").append(groupIterator.next());
            }

            groupString = groupBuilder.toString();
        }
        return groupString;
    }
}
