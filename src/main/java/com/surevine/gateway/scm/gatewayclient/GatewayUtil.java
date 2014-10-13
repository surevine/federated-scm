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

import com.surevine.gateway.scm.scmclient.bean.RepoBean;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * @author nick.leaver@surevine.com
 */
public final class GatewayUtil {
    private static final String KEY_SOURCE = "source";
    private static final String VALUE_SOURCE = "SCM";
    private static final String KEY_LIMIT_DISTRIBUTION_TO = "limit_distribution_to";
    private static final String KEY_PROJECT = "project";
    private static final String KEY_REPO = "repo";
    private static final String KEY_DISTRIBUTION_TYPE = "repo";
    private static final String VALUE_SINGLE_DISTRIBUTION = "SINGLE_DISTRIBUTION";
    private static final String VALUE_DISTRIBUTE_TO_ALL_PERMITTED = "DISTRIBUTE_TO_ALL_PERMITTED";
    
    private GatewayUtil() {
        // no-op
    }
    
    public static void sendFileToGateway(final Path bundleFile, final Map<String, String> metadata) {
        // TODO
        // check bundleFile
        // check metadata
        // create JSON tmp file .metadata.json
        // create tar.gz containing bundleFile and .metadata.json
        // submit to the Gateway rest service
    }
    
    public static Map<String, String> getSinglePartnerMetadata(final RepoBean repoBean, final String partner) {
        Map<String, String> metadataMap = generateMetadata(repoBean, VALUE_SINGLE_DISTRIBUTION);
        metadataMap.put(KEY_LIMIT_DISTRIBUTION_TO, partner);
        return metadataMap;
    }
    
    public static Map<String, String> getMetadata(final RepoBean repoBean) {
        return generateMetadata(repoBean, VALUE_DISTRIBUTE_TO_ALL_PERMITTED);
    }
    
    private static Map<String, String> generateMetadata(final RepoBean repoBean, final String distributionType) {
        Map<String, String> metadataMap = new HashMap<String, String>();
        metadataMap.put(KEY_SOURCE, VALUE_SOURCE);
        metadataMap.put(KEY_PROJECT, repoBean.getProject().getKey());
        metadataMap.put(KEY_REPO, repoBean.getSlug());
        metadataMap.put(KEY_DISTRIBUTION_TYPE, distributionType);
        return metadataMap;
    }
}
