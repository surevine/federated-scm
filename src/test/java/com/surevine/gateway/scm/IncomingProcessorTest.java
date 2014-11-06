package com.surevine.gateway.scm;

import java.io.File;
import java.net.URL;
import java.io.IOException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.surevine.gateway.scm.gatewayclient.MetadataUtil;
import com.surevine.gateway.scm.git.bundles.BundleProcessor;
import com.surevine.gateway.scm.git.bundles.NoExistingRepoBundleProcessor;
import com.surevine.gateway.scm.scmclient.SCMCommand;
import com.surevine.gateway.scm.util.PropertyUtil;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.instanceOf;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RunWith(PowerMockRunner.class)
@PrepareForTest(SCMCommand.class) 
public class IncomingProcessorTest {

	private IncomingProcessorImpl underTest = new IncomingProcessorImpl();
	
	private Path getGoodArchive() {
		String path = getClass().getResource("/good.tar.gz").getPath();
		File archiveFile = new File(path);
		return archiveFile.toPath();
	}
	
	private Path getBadArchive() {
		String path = getClass().getResource("/bad.tar.gz").getPath();
		File archiveFile = new File(path);
		return archiveFile.toPath();
	}
	
	private Path getGoodBundle() {
		String path = getClass().getResource("/good.bundle").getPath();
		File archiveFile = new File(path);
		return archiveFile.toPath();
	}
	
	private Path getBadBundle() {
		String path = getClass().getResource("/bad.bundle").getPath();
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
	
	@Test(expected=IOException.class)
	public void testShouldNotProcessCorruptArchive() throws Exception {
		Path archivePath = getBadArchive();
		underTest.openTarGz(archivePath);
	}
	
	@Test
	public void testShouldProcessValidArchive() throws Exception {
		Path archivePath = getGoodArchive();
		underTest.openTarGz(archivePath);
	}
	
	@Test
	public void testShouldShowInvalidArchive() throws Exception {
		Path archivePath = getBadArchive();
		Boolean isValid = underTest.isTarGz(archivePath);
		assertEquals(false, isValid);
	}
	
	@Test
	public void testShouldShowValidArchive() throws Exception {
		Path archivePath = getGoodArchive();
		Boolean isValid = underTest.isTarGz(archivePath);
		assertEquals(true, isValid);
	}
	
	@Test
	public void testShouldNotContainCorrectContents() throws Exception {
		Path archivePath = getBadArchive();
		Boolean isCorrect = underTest.tarGzHasExpectedContents(archivePath);
		assertEquals(false, isCorrect);
	}
	
	@Test
	public void testShouldContainCorrectContents() throws Exception {
		Path archivePath = getGoodArchive();
		Boolean isCorrect = underTest.tarGzHasExpectedContents(archivePath);
		assertEquals(true, isCorrect);
	}
	
	@Test
	public void testShouldCreateCorrectExtractionPath() throws Exception {
		Path archivePath = getGoodArchive();
		Path extractPath = underTest.getTmpExtractionPath(archivePath);
		
		String expectedPath = PropertyUtil.getTempDir()+"/good";
		
		assertEquals(expectedPath, extractPath.toString());
	}
	
	@Test
	public void testShouldExpandValidArchive() throws Exception {
		Path archivePath = getGoodArchive();
		Collection<Path> extracted = underTest.extractTarGz(archivePath);
		assertEquals(2, extracted.size());
	}
	
	@Test
	public void testShouldNotPickOutMetadataPath() throws Exception {
		Collection<Path> paths = new ArrayList<Path>();
		paths.add(new File("file://something/but/not/metadata").toPath());
		paths.add(new File("file://another/junk/path").toPath());
		paths.add(new File("file://more/junk/paths/metadata.json").toPath());
		
		Path metadataPath = underTest.getMetadataFilePath(paths);
		assertEquals(null, metadataPath);
	}
	
	@Test
	public void testShouldPickOutMetadataPath() throws Exception {
		Path validPath = new File("file://the/file/path/with/.metadata.json").toPath();
		
		Collection<Path> paths = new ArrayList<Path>();
		paths.add(new File("file://something/but/not/metadata").toPath());
		paths.add(validPath);
		paths.add(new File("file://another/junk/path").toPath());
		
		Path metadataPath = underTest.getMetadataFilePath(paths);
		assertEquals(validPath, metadataPath);
	}
	
	@Test
	public void testShouldNotPickOutBundlePath() throws Exception {
		Collection<Path> paths = new ArrayList<Path>();
		paths.add(new File("file://something/but/not/a/bundle").toPath());
		paths.add(new File("file://another/junk/path").toPath());
		paths.add(new File("file://more/junk/paths/metadata.json").toPath());
		
		Path bundlePath = underTest.getGitBundleFilePath(paths);
		assertEquals(null, bundlePath);
	}
	
	@Test
	public void testShouldPickOutBundlePath() throws Exception {
		Path validPath = new File("file://the/file/path/with/git.bundle").toPath();
		
		Collection<Path> paths = new ArrayList<Path>();
		paths.add(new File("file://something/but/not/metadata").toPath());
		paths.add(validPath);
		paths.add(new File("file://another/junk/path").toPath());
		
		Path bundlePath = underTest.getGitBundleFilePath(paths);
		assertEquals(validPath, bundlePath);
	}
	
	@Test
	public void testShouldBuildCorrectBundlePath() throws Exception {
		Map<String, String> metadata = getTestMetadata();
		
		Path bundlePath = underTest.buildBundleDestination(metadata);
		
		Path expectedPath = Paths.get(PropertyUtil.getRemoteBundleDir(),
                "test", "project", "repo" + ".bundle");
		
		assertEquals(expectedPath.toString(), bundlePath.toString());
	}
	
	@Test
	public void testShouldCopyBundleCorrectly() throws Exception {
		Path goodBundle = getGoodBundle();
		Map<String, String> metadata = getTestMetadata();
		
		underTest.copyBundle(goodBundle, metadata);

		Path expectedPath = Paths.get(PropertyUtil.getRemoteBundleDir(),
                "test", "project", "repo" + ".bundle");
		
		assertEquals(true, expectedPath.toFile().exists());
	}

	@PrepareForTest(SCMCommand.class)
	@Test
	public void testShouldCreateCorrectProcessor() throws Exception {
		Map<String, String> metadata = getTestMetadata();

        String partnerName = metadata.get(MetadataUtil.KEY_ORGANISATION);
        String projectKey = metadata.get(MetadataUtil.KEY_PROJECT);
        String repositorySlug = metadata.get(MetadataUtil.KEY_REPO);
        String partnerProjectKey = PropertyUtil.getPartnerProjectKeyString(partnerName, projectKey);
        String partnerProjectForkKey = PropertyUtil.getPartnerForkProjectKeyString(partnerName, projectKey);
		
		PowerMockito.mockStatic(SCMCommand.class);
		
		Mockito.when(SCMCommand.getRepository(partnerProjectForkKey, repositorySlug)).thenReturn(null);
		Mockito.when(SCMCommand.getRepository(projectKey, repositorySlug)).thenReturn(null);
		Mockito.when(SCMCommand.getRepository(partnerProjectKey, repositorySlug)).thenReturn(null);
		
		BundleProcessor processor = underTest.getAppropriateBundleProcessor(getGoodBundle(), metadata);
		
		assertThat(processor, instanceOf(NoExistingRepoBundleProcessor.class));
	}
}
