package com.hawkins.m3utoolsjpa.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
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

    @Autowired
    private M3UItemRepository itemRepository;

    @Autowired
    private TvChannelRepository tvChannelRepository;

    // Fetch selected channels based on groupId
    public List<SelectedChannel> find(Long groupId) {
        return groupId > 0
                ? itemRepository.findTvChannelsByGroup(groupId).stream()
                        .filter(item -> StringUtils.isNotBlank(item.getTvgId()))
                        .map(this::convertToSelectedChannel)
                        .collect(Collectors.toList())
                : List.of();
    }

    // Fetch all channels based on the selection status
    public List<SelectedChannel> find(boolean selected) {
        return tvChannelRepository.findAll().stream()
                .filter(channel -> StringUtils.isNotBlank(channel.getTvgId()))
                .map(channel -> convertToSelectedChannel(channel, selected))
                .collect(Collectors.toList());
    }

    // Save all selected channels to the database
    public void saveAll(List<SelectedChannel> channels) {
        channels.forEach(channel -> {
            if (channel.isSelected()) {
                log.debug("Channel {} IS selected", channel.getTvgName());
            }

            if (StringUtils.isNotBlank(channel.getTvgId())) {
                itemRepository.updateSelected(channel.getTvgId(), channel.getGroupId(),
                        channel.getTvgName(), channel.isSelected());
                addOrUpdateTvChannel(channel);
            }
        });
    }

 // Add or update TvChannel based on selected M3UItem
    private void addOrUpdateTvChannel(SelectedChannel channel) {
        log.debug("Finding channel(M3UItem) {}", channel.getTvgId());

        try {
            List<M3UItem> items = itemRepository.findByTvgIdAndTvgName(channel.getTvgId(), channel.getTvgName());

            if (items.isEmpty()) {
                return;
            }

            M3UItem thisItem = items.get(0);
            Optional<TvChannel> tvChannelOpt = Optional.ofNullable(tvChannelRepository.findByTvgName(thisItem.getTvgName()));

            if (tvChannelOpt.isEmpty() && channel.isSelected()) {
                // Use Optional to handle null values safely
                Long newChannelNumber = Optional.ofNullable(tvChannelRepository.getMaxChannelNumber())
                        .map(num -> num + 1) // Increment the max channel number
                        .orElse(1000L); // Default to 1000 if max channel number is null

                TvChannel newChannel = new TvChannel(newChannelNumber, thisItem.getGroupId(), newChannelNumber.toString(),
                        thisItem.getTvgName(), thisItem.getTvgId(), thisItem.getTvgLogo(), thisItem.getGroupTitle(),
                        thisItem.getChannelUri());
                tvChannelRepository.save(newChannel);

            } else if (!channel.isSelected()) {
                tvChannelOpt.ifPresent(tvChannel -> {
                    itemRepository.updateTvgChNo(thisItem.getId(), "");
                    itemRepository.updateSelected(tvChannel.getTvgId(), String.valueOf(tvChannel.getGroupId()), tvChannel.getTvgName(), false);
                    tvChannelRepository.delete(tvChannel);
                });
            }

        } catch (NonUniqueResultException e) {
            log.warn("Found duplicate M3UItem named {}", channel.getTvgName(), e);
        }
    }

    // Helper method to convert M3UItem to SelectedChannel
    private SelectedChannel convertToSelectedChannel(M3UItem item) {
        SelectedChannel channel = new SelectedChannel();
        channel.setId(item.getId());
        channel.setGroupId(String.valueOf(item.getGroupId()));
        channel.setTvgName(item.getTvgName());
        channel.setTvgId(item.getTvgId());
        channel.setSelected(item.isSelected());
        return channel;
    }

    // Helper method to convert TvChannel to SelectedChannel with selected flag
    private SelectedChannel convertToSelectedChannel(TvChannel channel, boolean selected) {
        SelectedChannel selectedChannel = new SelectedChannel();
        selectedChannel.setId(channel.getId());
        selectedChannel.setGroupId(String.valueOf(channel.getGroupId()));
        selectedChannel.setTvgName(channel.getTvgName());
        selectedChannel.setTvgId(channel.getTvgId());
        selectedChannel.setSelected(selected);
        return selectedChannel;
    }
}
