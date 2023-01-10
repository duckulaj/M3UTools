package com.hawkins.m3utoolsjpa.data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

@Entity
public class SelectedTvChannels {

	@GenericGenerator(
	        name = "TVChannelsSequenceGenerator",
	        strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
	        parameters = {
	                @Parameter(name = "sequence_name", value = "TVChannelsSequence"),
	                @Parameter(name = "initial_value", value = "1"),
	                @Parameter(name = "increment_size", value = "1")
	        }
	)

	@Id
	@GeneratedValue(generator = "TVChannelsSequenceGenerator")
	private Long id;
	private Long channelId;
	private String channelName;
	
	protected SelectedTvChannels() {}
	
	public SelectedTvChannels(
			Long channelId, String channelName) {
		this.channelId = channelId;
		this.channelName= channelName;
	}
	
	@Override
	public String toString() {
		return String.format(
				"M3USelectedTvChannels[id=%d, channelId='%d', channelName='%s']",
				id, channelId, channelName);
	}

	public Long getId() {
		return id;
	}
	
	public Long getChannelId() {
		return channelId;
	}
	
	public String getChannelName() {
		return channelName;
	}
}
