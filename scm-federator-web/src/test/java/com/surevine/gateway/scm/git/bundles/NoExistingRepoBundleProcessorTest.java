package com.surevine.gateway.scm.git.bundles;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.surevine.gateway.scm.gatewayclient.MetadataUtil;
import com.surevine.gateway.scm.model.LocalRepoBean;
import com.surevine.gateway.scm.scmclient.SCMCommand;
import com.surevine.gateway.scm.service.SCMFederatorServiceException;
import com.surevine.gateway.scm.util.PropertyUtil;

@RunWith(PowerMockRunner.class)
@PrepareForTest(SCMCommand.class) 
public class NoExistingRepoBundleProcessorTest {
	
	private Path getGoodBundle() {
		String path = getClass().getResource("/good.bundle").getPath();
		File archiveFile = new File(path);
		return archiveFile.toPath();
	}
	
	private Map<String, String> getTestMetadata() {
		Map<String, String> metadata = new HashMap<String, String>();
		metadata.put(MetadataUtil.KEY_ORGANISATION, "test");
		metadata.put(MetadataUtil.KEY_PROJECT, "project");
		metadata.put(MetadataUtil.KEY_REPO, "repo");
		return metadata;
	}
	
	@Test()
	public void testShouldThrowExceptionIfNothingProvided() {
		PartnerProjectBundleProcessor underTest = new PartnerProjectBundleProcessor();
		
		try {
			underTest.processBundle();
		} catch (Exception e) {
			assertThat(e, instanceOf(BundleProcessingException.class));
		}
	}
	
	@Test
	public void testShouldThrowExceptionIfNoBundleProvided() {
		PartnerProjectBundleProcessor underTest = new PartnerProjectBundleProcessor();
		underTest.setBundleMetadata(getTestMetadata());
		
		try {
			underTest.processBundle();
		} catch (Exception e) {
			assertThat(e, instanceOf(BundleProcessingException.class));
		}
	}
	
	@Test
	public void testShouldThrowExceptionIfNoMetadataProvided() {
		PartnerProjectBundleProcessor underTest = new PartnerProjectBundleProcessor();
		underTest.setBundleLocation(getGoodBundle());
		
		try {
			underTest.processBundle();
		} catch (Exception e) {
			assertThat(e, instanceOf(BundleProcessingException.class));
		}
	}
	
	@Test
	public void testShouldCreateCorrectBean() throws Exception {
		Map<String, String> metadata = getTestMetadata();
		Path bundle = getGoodBundle();
		
		PartnerProjectBundleProcessor underTest = new PartnerProjectBundleProcessor();
		underTest.setBundleLocation(bundle);
		underTest.setBundleMetadata(metadata);
		
		String partnerName = metadata.get(MetadataUtil.KEY_ORGANISATION);
		String projectKey = metadata.get(MetadataUtil.KEY_PROJECT);
		String repositorySlug = metadata.get(MetadataUtil.KEY_REPO);
		
		String partnerProjectKey = PropertyUtil.getPartnerProjectKeyString(partnerName, projectKey);

        LocalRepoBean repoBean = new LocalRepoBean();
        repoBean.setProjectKey(partnerProjectKey);
        repoBean.setSlug(repositorySlug);
        repoBean.setCloneSourceURI(bundle.toString());
        repoBean.setFromGateway(true);
        repoBean.setSourcePartner(partnerName);
        
        assertEquals(repoBean.toString(), underTest.getRepoForBundle().toString());
	}
	
	@Test
	public void testShouldCreateProjectIfNoPartnerProject() throws Exception {
		Map<String, String> metadata = getTestMetadata();
		
		PartnerProjectBundleProcessor underTest = new PartnerProjectBundleProcessor();
		underTest.setBundleLocation(getGoodBundle());
		underTest.setBundleMetadata(metadata);

		String partnerName = metadata.get(MetadataUtil.KEY_ORGANISATION);
        String projectKey = metadata.get(MetadataUtil.KEY_PROJECT);
        String repositorySlug = metadata.get(MetadataUtil.KEY_REPO);
        
        String partnerProjectKey = PropertyUtil.getPartnerProjectKeyString(partnerName, projectKey);
		
        PowerMockito.mockStatic(SCMCommand.class);
        Mockito.when(SCMCommand.getProjects()).thenReturn(new ArrayList<String>());
		Mockito.when(SCMCommand.getRepository(projectKey, repositorySlug)).thenReturn(new LocalRepoBean());
		
		underTest.getPrimaryRepo();
		
		PowerMockito.verifyStatic(Mockito.times(1));
		SCMCommand.createProject(partnerProjectKey);
	}
	
	@Test
	public void testShouldThrowExceptionIfRepoExists() throws Exception {
		Map<String, String> metadata = getTestMetadata();
		
		PartnerProjectBundleProcessor underTest = new PartnerProjectBundleProcessor();
		underTest.setBundleLocation(getGoodBundle());
		underTest.setBundleMetadata(metadata);

		String partnerName = metadata.get(MetadataUtil.KEY_ORGANISATION);
        String projectKey = metadata.get(MetadataUtil.KEY_PROJECT);
        String repositorySlug = metadata.get(MetadataUtil.KEY_REPO);
		
        PowerMockito.mockStatic(SCMCommand.class);
        Mockito.when(SCMCommand.getProjects()).thenReturn(new ArrayList<String>());
		Mockito.when(SCMCommand.getRepository(projectKey, repositorySlug)).thenReturn(new LocalRepoBean());
		
		try {
			underTest.getPrimaryRepo();
		} catch ( Exception e ) {
			assertThat(e, instanceOf(SCMFederatorServiceException.class));
		}
	}
}
