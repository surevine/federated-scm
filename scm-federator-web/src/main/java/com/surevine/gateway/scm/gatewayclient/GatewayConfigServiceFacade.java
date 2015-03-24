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

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.client.Client;

import org.apache.log4j.Logger;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;

import com.surevine.community.transfermodel.federation.FederationConfiguration;
import com.surevine.community.transfermodel.federation.RepositoryType;
import com.surevine.gateway.scm.util.PropertyUtil;

/**
 * Facade over access to the configured shared repositories in the gateway
 * management component
 *
 * @author nick.leaver@surevine.com
 */
public class GatewayConfigServiceFacade {
	private static final Logger LOGGER = Logger.getLogger(GatewayConfigServiceFacade.class);
	private static GatewayConfigServiceFacade instance;

	protected GatewayConfigServiceFacade() {
		// external instantiation protection
	}

	public List<FederationConfiguration> getSharedRepositories() {
		final String managementUrl = PropertyUtil.getProjectConfigServiceURL();

		LOGGER.info("Attempting to retrieve project sharing configuration from " + managementUrl);

		// call the management console to get the list of shares to be exported
		final Client client = new ResteasyClientBuilder().establishConnectionTimeout(6, TimeUnit.SECONDS)
				.socketTimeout(6, TimeUnit.SECONDS).build();

		final FederationConfiguration[] configurations = client.target(managementUrl)
				.queryParam("repoType", RepositoryType.SCM.name()).request().get(FederationConfiguration[].class);
		for (final FederationConfiguration configuration : configurations) {
			final String repositoryIdentifier = configuration.getRepository().getIdentifier();
			final String partnerIdentifier = configuration.getPartner().getSourceKey();

			LOGGER.debug("Shared repository configuration loaded: " + repositoryIdentifier + " shared with "
					+ partnerIdentifier);
		}

		return Arrays.asList(configurations);
	}

	public static GatewayConfigServiceFacade getInstance() {
		if (instance == null) {
			instance = new GatewayConfigServiceFacade();
		}
		return instance;
	}

	public static void setInstance(final GatewayConfigServiceFacade newInstance) {
		instance = newInstance;
	}
}
