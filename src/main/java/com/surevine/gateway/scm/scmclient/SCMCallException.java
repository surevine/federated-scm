package com.surevine.gateway.scm.scmclient;

/**
 * Checked exception thrown when there is an issue calling an external SCM system
 * @author nick.leaver@surevine.com
 */
public class SCMCallException extends Exception {
    /**
     * @param call the REST call that failed
     * @param errorMessage the error message
     */
    public SCMCallException(String call, String errorMessage) {
        super(call + ":" + errorMessage);
    }    
}
