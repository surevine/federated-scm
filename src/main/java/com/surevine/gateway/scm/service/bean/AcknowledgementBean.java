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

    public void setReceivedCommitHashes(List<String> receivedCommitHashes) {
        this.receivedCommitHashes = receivedCommitHashes;
    }

    public String getPartnerName() {
        return partnerName;
    }

    public void setPartnerName(String partnerName) {
        this.partnerName = partnerName;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }
    
    public String toString() {
        return "partner:" + partnerName + " project:" + projectName + " commits:" + receivedCommitHashes;
    }
}
