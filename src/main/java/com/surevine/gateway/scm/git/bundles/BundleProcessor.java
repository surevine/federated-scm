package com.surevine.gateway.scm.git.bundles;

import java.nio.file.Path;
import java.util.Map;

import com.surevine.gateway.scm.scmclient.SCMCallException;

public abstract class BundleProcessor {
	
	private Path bundle;
	private Map<String, String> metadata;
	
	public BundleProcessor() {
		//
	}

	public  void setBundleLocation(Path bundleLocation) {
		bundle = bundleLocation;
	}
	
	public void setBundleMetadata(Map<String, String> bundleMetadata) {
		metadata = bundleMetadata;
	}
	
	public Map<String, String> getMetadata() {
		return metadata;
	}
	
	public Path getBundleLocation() {
		return bundle;
	}
	
	public abstract void processBundle() throws SCMCallException;
}
