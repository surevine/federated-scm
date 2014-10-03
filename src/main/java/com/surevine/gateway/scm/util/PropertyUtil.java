package com.surevine.gateway.scm.util;

import org.apache.log4j.Logger;

import java.util.ResourceBundle;

/**
 * External properties access
 * @author nick.leaver@surevine.com
 */
public class PropertyUtil {
    private static final Logger logger = Logger.getLogger(PropertyUtil.class);
    private static final String PROP_REDIS_HOSTNAME = "redis.hostname";
    private static final String PROP_REDIS_NAMESPACE = "redis.namespace";
    private static final String PROP_GATEWAY_IMPORT_DIR = "gateway.import.dir";
    private static final String PROP_GATEWAY_EXPORT_DIR = "gateway.export.dir";
    private static final String PROP_VERSION = "fedscm.version";
    private static final String PROP_EXPORT_INTERVAL = "fedscm.export.interval";
    
    private static ResourceBundle bundle;
    
    public static String getProperty(final String key) {        
        checkInit();
        return bundle.getString(key);
    }
    
    public static long getExportInterval() {
        return Long.parseLong(getProperty(PROP_EXPORT_INTERVAL));
    }
    
    public static String getRedisHostname() {
        return getProperty(PROP_REDIS_HOSTNAME);
    }
    
    public static String getPropRedisNamespace() {
        return getProperty(PROP_REDIS_NAMESPACE);
    }
    
    public static String getGatewayImportDir() {
        return getProperty(PROP_GATEWAY_IMPORT_DIR);
    }

    public static String getGatewayExportDir() {
        return getProperty(PROP_GATEWAY_EXPORT_DIR);
    }

    public static String getSystemVersion() {
        return getProperty(PROP_VERSION);
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