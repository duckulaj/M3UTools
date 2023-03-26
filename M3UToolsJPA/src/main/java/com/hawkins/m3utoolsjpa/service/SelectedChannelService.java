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

import jakarta.persistence.NonUniqueResultException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class SelectedChannelService {

	/*
	 * When a channel is selected we need to perform the following actions
	 * 
	 * 1. Set selected to true for the selected items in table M3UItem 2. Check to
	 * see if it exists within the TvChannel table 3. If M3UItem does not exist in
	 * the TvChannel Table add it using the Max ChannelId as the new channel Id 4.
	 * If an M3UItem is now unselected pdate the M3UItem table and remove from
	 * TvChannel table
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
	
	public List<SelectedChannel> find(boolean selected) {
		
		List<TvChannel> channels = new ArrayList<TvChannel>();
		channels = tvChannelRepository.findAll();
		
		List<SelectedChannel> selectedChannels = new ArrayList<SelectedChannel>();

		channels.forEach(channel -> {
			SelectedChannel selectedChannel = new SelectedChannel();
			selectedChannel.setId(channel.getId());
			selectedChannel.setTvgName(channel.getTvgName());
			selectedChannel.setSelected(true);
			selectedChannels.add(selectedChannel);
		});

		return selectedChannels;
	}

	public void saveAll(List<SelectedChannel> channels) {

		channels.forEach(channel -> {
			
			if (channel.isSelected()) log.info("Channel {} IS selected", channel.getTvgName());
			
			itemRepository.updateSelected(channel.getId(), channel.isSelected());
			AddOrUpdateTvChannel(channel);
		});
	}

	private void AddOrUpdateTvChannel(SelectedChannel channel) {

		Long newChannelNumber;

		log.debug("Finding channel(M3UItem) {}", channel.getTvgName());

		try {
			List<M3UItem> items = itemRepository.findListByChannelName(channel.getTvgName());

			if (items.size() > 0) {
				M3UItem thisItem = items.get(0);

				TvChannel tvChannel = tvChannelRepository.findByTvgName(thisItem.getTvgName());

				if (tvChannel == null && channel.isSelected()) {
					if (tvChannelRepository.getMaxChannelNumber() == null) {
						newChannelNumber = 1000L;
					} else {
						newChannelNumber = tvChannelRepository.getMaxChannelNumber() + 1L;
					}

					TvChannel newChannel = new TvChannel(newChannelNumber, newChannelNumber.toString(),
							thisItem.getTvgName(), thisItem.getTvgId(), thisItem.getTvgLogo(), thisItem.getGroupTitle(),
							thisItem.getChannelUri());
					tvChannelRepository.save(newChannel);
					thisItem.setTvgChNo(newChannel.getTvgChNo());
					thisItem.setSelected(true);
					itemRepository.save(thisItem);
				} else {
					if (!channel.isSelected()) {
						if (tvChannel != null) {
							tvChannelRepository.delete(tvChannel);
							thisItem.setTvgChNo("");
							thisItem.setSelected(false);
							itemRepository.save(thisItem);
						}
					}
				}
			}

		} catch (NonUniqueResultException nure) {
			log.info("Found duplicate M3UItem named {}", channel.getTvgName());
		}

	}
}
