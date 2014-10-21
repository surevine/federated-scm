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

import com.surevine.gateway.scm.model.LocalRepoBean;
import com.surevine.gateway.scm.util.PropertyUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * @author nick.leaver@surevine.com
 */
public final class MetadataUtil {
    public static final String KEY_SOURCE = "source";
    public static final String VALUE_SOURCE = "SCM";
    public static final String KEY_LIMIT_DISTRIBUTION_TO = "limit_distribution_to";
    public static final String KEY_PROJECT = "project";
    public static final String KEY_REPO = "repo";
    public static final String KEY_DISTRIBUTION_TYPE = "distribution_type";
    public static final String KEY_ORGANISATION = "source_organisation";
    public static final String VALUE_SINGLE_DISTRIBUTION = "SINGLE_DISTRIBUTION";
    public static final String VALUE_DISTRIBUTE_TO_ALL_PERMITTED = "DISTRIBUTE_TO_ALL_PERMITTED";
    
    private MetadataUtil() {
        // no-op
    }
        
    public static Map<String, String> getSinglePartnerMetadata(final LocalRepoBean repoBean, final String partner) {
        Map<String, String> metadataMap = generateMetadata(repoBean, VALUE_SINGLE_DISTRIBUTION);
        metadataMap.put(KEY_LIMIT_DISTRIBUTION_TO, partner);
        return metadataMap;
    }
    
    public static Map<String, String> getMetadata(final LocalRepoBean repoBean) {
        return generateMetadata(repoBean, VALUE_DISTRIBUTE_TO_ALL_PERMITTED);
    }
    
    private static Map<String, String> generateMetadata(final LocalRepoBean repoBean, final String distributionType) {
        Map<String, String> metadataMap = new HashMap<String, String>();
        metadataMap.put(KEY_SOURCE, VALUE_SOURCE);
        metadataMap.put(KEY_PROJECT, repoBean.getProjectKey());
        metadataMap.put(KEY_REPO, repoBean.getSlug());
        metadataMap.put(KEY_DISTRIBUTION_TYPE, distributionType);
        metadataMap.put(KEY_ORGANISATION, PropertyUtil.getOrgName());
        return metadataMap;
    }
}
