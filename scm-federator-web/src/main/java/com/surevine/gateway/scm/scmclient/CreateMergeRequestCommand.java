package com.surevine.gateway.scm.scmclient;

import com.surevine.gateway.scm.model.LocalRepoBean;

public interface CreateMergeRequestCommand {
	
	public void createMergeRequest(LocalRepoBean from, LocalRepoBean to ) throws SCMCallException;
	
}
