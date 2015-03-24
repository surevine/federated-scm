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

import static org.junit.Assert.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.Test;

import com.surevine.gateway.scm.git.GitFacade;
import com.surevine.gateway.scm.git.jgit.TestUtility;
import com.surevine.gateway.scm.model.LocalRepoBean;

/**
 * @author nick.leaver@surevine.com
 */
public class GatewayPackageTest {
	@Test
	public void testGatewayPackageFileCreation() throws Exception {
		final LocalRepoBean repo = TestUtility.createTestRepo();
		final GitFacade git = GitFacade.getInstance();
		final Path bundlePath = git.bundle(repo);
		final GatewayPackage gatewayPackage = new GatewayPackage(bundlePath, MetadataUtil.getSinglePartnerMetadata(
				repo, repo.getSourcePartner()));
		gatewayPackage.createArchive();

		assertTrue(Files.exists(gatewayPackage.getArchive()));
		TestUtility.destroyTestRepo(repo);
		Files.delete(gatewayPackage.getArchive());
	}
}
