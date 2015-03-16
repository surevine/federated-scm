package com.surevine.gateway.scm.scmclient.gitlab;

import static org.junit.Assert.*;

import org.junit.Test;

public class GitlabGetProjectCommandTest {
	
	@Test
	public void testShouldReturnNullIfNoProject() throws Exception {
		GitlabGetProjectCommand cmd = new GitlabGetProjectCommand();
		
		assertEquals(null, cmd.getRepository("test", "something"));
	}

}
