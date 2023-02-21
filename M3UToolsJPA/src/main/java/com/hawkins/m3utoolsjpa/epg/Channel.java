package com.hawkins.m3utoolsjpa.epg;

import java.util.List;

public class Channel {

	private String display_name;
	private String icon_src;
	private String id;
	private List<Programme> programmes;
	
	public Channel() {
		
	}
	
	public String getDisplay_name() {
		return display_name;
	}

	public void setDisplay_name(String display_name) {
		this.display_name = display_name;
	}

	public String getIcon_src() {
		return icon_src;
	}

	public void setIcon_src(String icon_src) {
		this.icon_src = icon_src;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public List<Programme> getProgrammes() {
		return programmes;
	}

	public void setProgrammes(List<Programme> programmes) {
		this.programmes = programmes;
	}

	public void addProgramme(Programme programme) {
		this.programmes.add(programme);
	}
	
	
}
