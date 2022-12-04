package com.hawkins.dmanager;

import com.hawkins.M3UToolsJPA.downloaders.metadata.HttpMetadata;

public interface LinkRefreshCallback {
	public String getId();

	public boolean isValidLink(HttpMetadata metadata);
}
