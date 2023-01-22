package com.hawkins.m3utoolsjpa.m3u;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.hawkins.m3utoolsjpa.data.Channel;
import com.hawkins.m3utoolsjpa.data.ChannelRepository;
import com.hawkins.m3utoolsjpa.data.M3UGroupRepository;
import com.hawkins.m3utoolsjpa.data.M3UItem;
import com.hawkins.m3utoolsjpa.data.M3UItemRepository;
import com.hawkins.m3utoolsjpa.utils.Constants;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class M3UChannel {

	@Autowired
	M3UGroupRepository groupRepository;
	
	public static void populateTVChannels(M3UItemRepository m3uItemRepository, ChannelRepository channelRepository) {
		
		List<M3UItem> tvChannels = m3uItemRepository.findByType(Constants.LIVE);
		
		List<Channel> channels = new ArrayList<Channel>();
		
		for (M3UItem channel : tvChannels) {
			Long channelId = 
		    channels.add(new Channel(channel.getChannelName(), channel.getGroupTitle(), false)); 
		}
		
		if (channels.size()> 0) {
			channelRepository.deleteAll();
			channelRepository.saveAll(channels);
			
		}
		
		log.info("Saved {} channels", channels.size());
		 
	}
}
