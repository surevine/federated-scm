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

import java.util.ArrayList;
import java.util.List;

/**
 * Facade over access to the configured shared repositories in the gateway management component
 * @author nick.leaver@surevine.com
 */
public class GatewayConfigServiceFacade {
    private static GatewayConfigServiceFacade instance;
    
    GatewayConfigServiceFacade() {
        // external instantiation protection
    }
    
    public List<SharedRepoIdentification> getSharedRepositories() {
        List<SharedRepoIdentification> sharedRepositories = new ArrayList<SharedRepoIdentification>();
        // TODO
        // connect to gateway configuration service
        // retrieve list of shared repositories
        // populate sharedRepositories list
        return sharedRepositories;
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
