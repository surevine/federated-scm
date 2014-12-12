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
import com.surevine.gateway.scm.service.SCMFederatorServiceException;
import com.surevine.gateway.scm.util.InputValidator;
import com.surevine.gateway.scm.util.PropertyUtil;
import com.surevine.gateway.scm.util.StringUtil;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * @author nick.leaver@surevine.com
 */
public final class MetadataUtil {
    private static Logger logger = Logger.getLogger(MetadataUtil.class);
    public static final String KEY_SOURCE = "source_type";
    public static final String VALUE_SOURCE = "SCM";
    public static final String KEY_LIMIT_DISTRIBUTION_TO = "limit_distribution_to";
    public static final String KEY_PROJECT = "project";
    public static final String KEY_REPO = "repo";
    public static final String KEY_DISTRIBUTION_TYPE = "distribution_type";
    public static final String KEY_ORGANISATION = "source_organisation";
    public static final String VALUE_SINGLE_DISTRIBUTION = "single_distribution";
    public static final String VALUE_DISTRIBUTE_TO_ALL_PERMITTED = "distribute_to_all_permitted";
    public static final String KEY_FILENAME = "name";
    public static final String KEY_CLASSIFICATION = "classification";
    public static final String KEY_DECORATOR = "decorator";
    public static final String KEY_GROUPS = "groups";
    
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
    
    public static String deriveFilenameFromMetadata(final Map<String, String> metadata) {
        StringBuilder sb = new StringBuilder();
        sb.append("scm_")
                .append(metadata.get(MetadataUtil.KEY_ORGANISATION))
                .append("_").append(metadata.get(MetadataUtil.KEY_PROJECT))
                .append("_").append(metadata.get(MetadataUtil.KEY_REPO));
        return StringUtil.cleanStringForFilePath(sb.toString()) + ".tar.gz";
    }
    
    private static Map<String, String> generateMetadata(final LocalRepoBean repoBean, final String distributionType) {
        Map<String, String> metadataMap = new HashMap<>();
        String organisationName = PropertyUtil.getOrgName().toLowerCase();
        String projectKey = repoBean.getProjectKey().toLowerCase();
    	projectKey = projectKey.replace(organisationName+"_", "");
        
        logger.debug(organisationName+", "+projectKey+", "+repoBean.getProjectKey()+", "+PropertyUtil.getOrgName());
        
        metadataMap.put(KEY_SOURCE, VALUE_SOURCE);
        metadataMap.put(KEY_PROJECT, projectKey);
        metadataMap.put(KEY_REPO, repoBean.getSlug());
        metadataMap.put(KEY_DISTRIBUTION_TYPE, distributionType);
        metadataMap.put(KEY_ORGANISATION, organisationName);
        metadataMap.put(KEY_CLASSIFICATION, repoBean.getRepoSecurityLabel().getClassification());
        metadataMap.put(KEY_DECORATOR, repoBean.getRepoSecurityLabel().getDecorator());
        metadataMap.put(KEY_GROUPS, repoBean.getRepoSecurityLabel().getGroupString());
        
        String filename = deriveFilenameFromMetadata(metadataMap);
        metadataMap.put(KEY_FILENAME, filename);
        return metadataMap;
    }

    public static Map<String, String> getMetadataFromPath(final Path metadataPath) {
        Map<String, String> metadata = new HashMap<>();
        try {
            JSONObject jsonObject = new JSONObject(new String(Files.readAllBytes(metadataPath), Charset.forName("UTF-8")));
            for (Object key:jsonObject.keySet()) {
                try {
                    String keyString = (String) key;
                    String value = jsonObject.getString(keyString);
                    if (value != null && !value.isEmpty()) {
                        metadata.put(keyString, value);
                    }
                } catch (JSONException jsonException) {
                    // no-op - skip as the value is not a string and metadata entries are expected to be String:String
                }
            }
        } catch (IOException e) {
            logger.error("Could not read the mock gateway config", e);
        }
        return metadata;
    }

    public static boolean metadataValid(final Map<String, String> metadata) {
        boolean containsRequiredKeys = metadata.containsKey(KEY_SOURCE)
                && metadata.containsKey(KEY_PROJECT)
                && metadata.containsKey(KEY_REPO)
                && metadata.containsKey(KEY_DISTRIBUTION_TYPE)
                && metadata.containsKey(KEY_ORGANISATION)
                && metadata.containsKey(KEY_CLASSIFICATION)
                && metadata.containsKey(KEY_DECORATOR)
                && metadata.containsKey(KEY_GROUPS)
                && metadata.containsKey(KEY_FILENAME);
        
        boolean valuesAreValid = InputValidator.partnerNameIsValid(metadata.get(MetadataUtil.KEY_ORGANISATION))
                && InputValidator.projectKeyIsValid(metadata.get(MetadataUtil.KEY_PROJECT))
                && InputValidator.repoSlugIsValid(metadata.get(MetadataUtil.KEY_REPO));
        
        return containsRequiredKeys && valuesAreValid;
    }
}
