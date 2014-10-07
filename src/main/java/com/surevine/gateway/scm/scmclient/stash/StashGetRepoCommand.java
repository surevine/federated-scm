package com.surevine.gateway.scm.scmclient.stash;

import com.surevine.gateway.scm.scmclient.GetRepoCommand;
import com.surevine.gateway.scm.scmclient.SCMCallException;
import com.surevine.gateway.scm.scmclient.bean.ProjectBean;
import com.surevine.gateway.scm.scmclient.bean.RepoBean;
import com.surevine.gateway.scm.util.PropertyUtil;
import com.surevine.gateway.scm.util.SCMSystemProperties;
import org.apache.log4j.Logger;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author nick.leaver@surevine.com
 */
public class StashGetRepoCommand implements GetRepoCommand {
    private static Logger logger = Logger.getLogger(StashGetRepoCommand.class);
    private static final String ALL_RESOURCE = "/rest/api/1.0/projects/%s/repos?limit=10000";
    private static final String SINGLE_RESOURCE = "/rest/api/1.0/projects/%s/repos/%s";
    private SCMSystemProperties scmSystemProperties;

    StashGetRepoCommand() {
        scmSystemProperties = PropertyUtil.getSCMSystemProperties();
    }
    
    @Override
    public Collection<RepoBean> getRepositories(String projectKey) {
        Client client = ClientBuilder.newClient();
        String resource = scmSystemProperties.getHost() + String.format(ALL_RESOURCE, projectKey);
        logger.debug("REST call to " + resource);

        PagedRepoResult response = client.target(resource)
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", scmSystemProperties.getBasicAuthHeader())
                .get(PagedRepoResult.class);

        client.close();
        
        return response.getValues();
    }

    @Override
    public RepoBean getRepository(String projectKey, String repositorySlug) {
        Client client = ClientBuilder.newClient();
        String resource = scmSystemProperties.getHost() + String.format(SINGLE_RESOURCE, projectKey, repositorySlug);
        logger.debug("REST call to " + resource);

        RepoBean response = null;

        try {
            response = client.target(resource)
                    .request(MediaType.APPLICATION_JSON)
                    .header("Authorization", scmSystemProperties.getBasicAuthHeader())
                    .get(RepoBean.class);
        } catch (NotFoundException nfe) {
            // no-op - acceptable response and will return a null object
        }
        
        client.close();

        return response;
    }

    @Override
    public Map<ProjectBean, Collection<RepoBean>> getAllRepositories() throws SCMCallException {
        logger.debug("Getting all repositories from Stash");
        int pCount = 0;
        int rCount = 0;
        
        StashGetProjectsCommand getProjectsCommand = new StashGetProjectsCommand();
        Collection<ProjectBean> projects = getProjectsCommand.getProjects();
        if (projects.size() > 0) {
            pCount = projects.size();
        }
        
        HashMap<ProjectBean, Collection<RepoBean>> repositories = new HashMap<ProjectBean, Collection<RepoBean>>();
        
        for (ProjectBean p:projects) {
            Collection<RepoBean> repos = getRepositories(p.getKey());
            if (repos.size() > 0) {
                repositories.put(p,repos);
                rCount+=repos.size();
            }
        }
        
        logger.debug("Retrieved " + rCount + " repositories from " + pCount + " projects");
        return repositories;
    }

    /**
     * Private wrapper for holding paging results
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class PagedRepoResult {
        private List<RepoBean> values;

        public List<RepoBean> getValues() {
            return values;
        }

        public void setValues(List<RepoBean> values) {
            this.values = values;
        }
    }
}
