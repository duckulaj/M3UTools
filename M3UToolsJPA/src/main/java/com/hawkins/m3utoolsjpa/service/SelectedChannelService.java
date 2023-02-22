package com.hawkins.m3utoolsjpa.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hawkins.m3utoolsjpa.data.M3UItem;
import com.hawkins.m3utoolsjpa.data.M3UItemRepository;
import com.hawkins.m3utoolsjpa.data.SelectedChannel;
import com.hawkins.m3utoolsjpa.data.TvChannel;
import com.hawkins.m3utoolsjpa.data.TvChannelRepository;

@Service
public class SelectedChannelService {
	
	/*
	 * When a channel is selected we need to perform the following actions
	 * 
	 * 1. Set selected to true for the selected items in table M3UItem
	 * 2. Check to see if it exists within the TvChannel table
	 * 3. If M3UItem does not exist in the TvChannel Table add it using the Max ChannelId as the new channel Id
	 * 4. If an M3UItem is now unselected pdate the M3UItem table and remove from TvChannel table
	 */

	@Autowired
	M3UItemRepository itemRepository;
	
	@Autowired
	TvChannelRepository tvChannelRepository;
	
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
    		AddOrUpdateTvChannel(channel);
    	});
    }
    
    private void AddOrUpdateTvChannel(SelectedChannel channel) {
    	
    	Long newChannelNumber;
    	
    	M3UItem thisItem = itemRepository.findByChannelName(channel.getTvgName());
    	
    	TvChannel tvChannel = tvChannelRepository.findByTvgName(thisItem.getTvgName());
    	
    	if (tvChannel == null && channel.isSelected()) {
    		 if (tvChannelRepository.getMaxChannelNumber() == null) {
    			 newChannelNumber = 1000L;
    		 } else {
    			 newChannelNumber = tvChannelRepository.getMaxChannelNumber() + 1L;
    		 }
    		 
    		TvChannel newChannel = new TvChannel(newChannelNumber, newChannelNumber.toString(), thisItem.getTvgName(), thisItem.getTvgId(), thisItem.getTvgLogo(), thisItem.getGroupTitle(), thisItem.getChannelUri());
    		tvChannelRepository.save(newChannel);
    		thisItem.setTvgChNo(newChannel.getTvgChNo());
    		itemRepository.save(thisItem);
    	} else {
    		if (!channel.isSelected()) {
    			if (tvChannel != null) {
    				tvChannelRepository.delete(tvChannel);
    				thisItem.setTvgChNo("");
    				itemRepository.save(thisItem);
    			}
    		}
    	}
    }
}