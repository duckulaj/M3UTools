package com.hawkins.m3utoolsjpa.epg;

import java.util.List;

public class EPG {

	private List<Channel> channels;

	public EPG(List<Channel> channels) {
		super();
		this.channels = channels;
	}

	public List<Channel> getChannels() {
		return channels;
	}

	public void setChannels(List<Channel> channels) {
		this.channels = channels;
	}
	
	
}
