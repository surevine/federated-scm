package com.surevine.gateway.scm;

import java.io.File;
import java.net.URL;
import java.io.IOException;

import org.junit.Test;

import com.surevine.gateway.scm.util.PropertyUtil;

import static org.junit.Assert.*;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;

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
}
