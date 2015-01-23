package com.surevine.gateway.scm.scmclient.gitlab;

import java.util.concurrent.TimeUnit;

import javax.ws.rs.client.Client;

import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;

public abstract class AbstractGitlabCommand {
    public Client getClient() {
        return new ResteasyClientBuilder()
                .establishConnectionTimeout(30, TimeUnit.SECONDS)
                .socketTimeout(30, TimeUnit.SECONDS)
                .build();
    }
}
