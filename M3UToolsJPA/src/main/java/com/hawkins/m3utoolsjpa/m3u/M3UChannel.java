package com.hawkins.m3utoolsjpa.m3u;

import java.util.ArrayList;
import java.util.List;

import com.hawkins.m3utoolsjpa.data.Channel;
import com.hawkins.m3utoolsjpa.data.M3UItemRepository;
import com.hawkins.m3utoolsjpa.service.ChannelRepository;
import com.hawkins.m3utoolsjpa.utils.Constants;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class M3UChannel {

	
	public static void populateTVChannels(M3UItemRepository m3uItemRepository, ChannelRepository channelRepository) {
		
		List<String> channelNames = m3uItemRepository.findDistinctChannelNameByType(Constants.LIVE);
		
		List<Channel> channels = new ArrayList<Channel>();
		
		for (String channelName : channelNames) {
		    channels.add(new Channel(channelName)); 
		}
		
		if (channels.size()> 0) {
			channelRepository.deleteAll();
			channelRepository.saveAll(channels);
			
		}
		
		log.info("Saved {} channels", channels.size());
		 
	}
}
