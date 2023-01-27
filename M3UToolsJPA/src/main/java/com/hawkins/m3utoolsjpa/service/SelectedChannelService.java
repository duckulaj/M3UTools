package com.hawkins.m3utoolsjpa.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hawkins.m3utoolsjpa.data.M3UItem;
import com.hawkins.m3utoolsjpa.data.M3UItemRepository;
import com.hawkins.m3utoolsjpa.data.SelectedChannel;

@Service
public class SelectedChannelService {

	@Autowired
	M3UItemRepository itemRepository;
	
    public List<SelectedChannel> find(Long groupId) {
    	
    	List<M3UItem> items = new ArrayList<M3UItem>();
    	
    	if (groupId > 0) {
    		items = itemRepository.findTvChannelsByGroup(groupId);
    	} 
    	
    	List<SelectedChannel> channels = new ArrayList<SelectedChannel>();
		
		items.forEach(item -> {
			SelectedChannel channel = new SelectedChannel();
			channel.setId(item.getId());
			channel.setTvgName(item.getTvgName());
			channel.setSelected(item.isSelected());
			channels.add(channel);
		});
		
		return channels;
    }

    public void saveAll(List<SelectedChannel> channels) {
    	
    	channels.forEach(channel -> {
    		itemRepository.updateSelected(channel.getId(), channel.isSelected());
    	});
    }
}
