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
package com.surevine.gateway.scm.util;

import org.apache.log4j.Logger;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ResourceBundle;

/**
 * External properties access
 * @author nick.leaver@surevine.com
 */
public final class PropertyUtil {
    private static Logger logger = Logger.getLogger(PropertyUtil.class);
    private static final String PROP_TEMP_DIR = "fedscm.temp.dir";
    private static final String PROP_REMOTE_BUNDLE_DIR = "fedscm.bundle.dir";
    private static final String PROP_VERSION = "fedscm.version";
    private static final String PROP_EXPORT_INTERVAL = "fedscm.export.interval";
    private static final String PROP_ORG_NAME = "fedscm.org.name";
    private static final String PROP_SCM_TYPE = "scm.type";
    private static final String PROP_SCM_USERNAME = "scm.auth.username";
    private static final String PROP_SCM_PASSWORD = "scm.auth.password";
    private static final String PROP_SCM_HOSTNAME = "scm.hostname";
    private static final String PROP_GIT_REPODIR = "fedscm.git.dir";
    private static final String PROP_GATEWAY_SERVICE_URL = "gateway.serviceURL";
    private static final String PROP_GATEWAY_IMPORT_DIR = "gateway.incoming.dir";
    private static final String PROP_PARTNER_PROJECT_KEY = "scm.import.project.key";
    private static final String PROP_PARTNER_FORK_PROJECT_KEY = "scm.import.project.fork";
    private static ResourceBundle bundle;

    private PropertyUtil() {
    }

    public static String getProperty(final String key) {
        checkInit();
        return bundle.getString(key);
    }
    
    public static String getOrgName() {
        return getProperty(PROP_ORG_NAME);
    }
    
    public static String getPartnerProjectKeyString(final String partnerName) {
        return getProperty(String.format(PROP_PARTNER_PROJECT_KEY, partnerName));
    }

    public static String getPartnerForkProjectKeyString(final String partnerName) {
        return getProperty(String.format(PROP_PARTNER_FORK_PROJECT_KEY, partnerName));
    }
    
    public static String getGatewayURL() {
        return getProperty(PROP_GATEWAY_SERVICE_URL);
    }

    public static long getExportInterval() {
        return Long.parseLong(getProperty(PROP_EXPORT_INTERVAL));
    }

    public static String getGatewayImportDir() {
        return getProperty(PROP_GATEWAY_IMPORT_DIR);
    }

    public static String getTempDir() {
        String tmpDir = getProperty(PROP_TEMP_DIR);
        try {
            Files.createDirectories(Paths.get(tmpDir));
        } catch (Exception e) {
            logger.error(e);
        }
        return tmpDir;
    }

    public static String getRemoteBundleDir() {
        String bundleDir = getProperty(PROP_REMOTE_BUNDLE_DIR);
        try {
            Files.createDirectories(Paths.get(bundleDir));
        } catch (Exception e) {
            logger.error(e);
        }
        return bundleDir;
    }

    public static String getSystemVersion() {
        return getProperty(PROP_VERSION);
    }
    
    public static String getGitDir() {
        String gitDir = getProperty(PROP_GIT_REPODIR);
        Path gitDirPath = Paths.get(gitDir);
        try {
            Files.createDirectories(gitDirPath);
            Files.createDirectories(gitDirPath.resolve("local_scm"));
            Files.createDirectories(gitDirPath.resolve("from_gateway"));
        } catch (Exception e) {
            logger.error(e);
        }
        return gitDir;
    }

    public static SCMSystemProperties getSCMSystemProperties() {
        String scmType = getProperty(PROP_SCM_TYPE);
        String scmUsername = getProperty(PROP_SCM_USERNAME);
        String scmPassword = getProperty(PROP_SCM_PASSWORD);
        String scmHostname = getProperty(PROP_SCM_HOSTNAME);
        return new SCMSystemProperties(scmType, scmUsername, scmPassword, scmHostname);
    }

    public static SCMSystemProperties.SCMType getSCMType() {
        return SCMSystemProperties.SCMType.getType(getProperty(PROP_SCM_TYPE));
    }

    private static void checkInit() {
        if (bundle == null) {
            bundle = ResourceBundle.getBundle("federated-scm");
            logger.info("SCM federator properties loaded:");
            for (String key : bundle.keySet()) {
                if (!key.toLowerCase().contains("password")) {
                    logger.info("\t" + key + ":" + bundle.getString(key));
                }
            }
        }
    }
}
