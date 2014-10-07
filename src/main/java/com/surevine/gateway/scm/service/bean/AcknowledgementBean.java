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
package com.surevine.gateway.scm.service.bean;

import java.util.List;

/**
 * Contains acknowledgement of receipt of some commits
 * @author nick.leaver@surevine.com
 */
public class AcknowledgementBean {
    private String projectName;
    private String partnerName;
    private List<String> receivedCommitHashes;

    public List<String> getReceivedCommitHashes() {
        return receivedCommitHashes;
    }

    public void setReceivedCommitHashes(final List<String> receivedCommitHashes) {
        this.receivedCommitHashes = receivedCommitHashes;
    }

    public String getPartnerName() {
        return partnerName;
    }

    public void setPartnerName(final String partnerName) {
        this.partnerName = partnerName;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(final String projectName) {
        this.projectName = projectName;
    }

    public String toString() {
        return "partner:" + partnerName + " project:" + projectName + " commits:" + receivedCommitHashes;
    }
}
