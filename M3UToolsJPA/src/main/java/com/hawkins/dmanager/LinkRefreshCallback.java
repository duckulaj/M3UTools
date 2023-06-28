package com.hawkins.dmanager;

import com.hawkins.m3utoolsjpa.downloaders.metadata.HttpMetadata;

public interface LinkRefreshCallback {
	public String getId();

	public boolean isValidLink(HttpMetadata metadata);
}
