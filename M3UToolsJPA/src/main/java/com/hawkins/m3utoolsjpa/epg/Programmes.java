package com.hawkins.m3utoolsjpa.epg;

import java.util.ArrayList;
import java.util.List;

public class Programmes {

	public List<Programme> programmes;
	
	public Programmes() {
		
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
	
	public List<Programme> getProgrammesByChannel(String channel) {
		
		List<Programme> channelProgrammes = new ArrayList<Programme>();
		
		for (Programme programme : programmes) {
			if (programme.getChannel().equals(channel) ) {
				channelProgrammes.add(programme);
			}
		}
		
		return channelProgrammes;
		
	}
}
