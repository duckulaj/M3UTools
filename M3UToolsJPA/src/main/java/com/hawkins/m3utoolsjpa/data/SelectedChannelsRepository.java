package com.hawkins.m3utoolsjpa.data;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.transaction.annotation.Transactional;

public interface SelectedChannelsRepository extends PagingAndSortingRepository<SelectedTvChannels, Long> {

	SelectedTvChannels findById(long id);

	List<SelectedTvChannels> findByChannelId(Long channelId);
	
	List<SelectedTvChannels> findByChannelName(String channelName);
}
