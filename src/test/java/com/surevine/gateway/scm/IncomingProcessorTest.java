package com.surevine.gateway.scm;

import java.io.File;
import java.net.URL;
import java.io.IOException;

import org.junit.Test;

import static org.junit.Assert.*;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Iterator;

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
	public void testShouldExpandValidArchive() throws Exception {
		Path archivePath = getGoodArchive();
		Collection<Path> extracted = underTest.extractTarGz(archivePath);
		assertEquals(2, extracted.size());
	}
}
