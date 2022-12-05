package com.hawkins.dmanager;

import com.hawkins.m3Utoolsjpa.downloaders.metadata.HttpMetadata;

public interface LinkRefreshCallback {
	public String getId();

	public boolean isValidLink(HttpMetadata metadata);
}
