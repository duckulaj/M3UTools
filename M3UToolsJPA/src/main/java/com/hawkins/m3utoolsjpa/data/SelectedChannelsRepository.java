package com.hawkins.m3utoolsjpa.data;

import java.util.List;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SelectedChannelsRepository extends PagingAndSortingRepository<SelectedTvChannels, Long> {

	SelectedTvChannels findById(long id);

	List<SelectedTvChannels> findByChannelId(Long channelId);
	
	List<SelectedTvChannels> findByChannelName(String channelName);
}
