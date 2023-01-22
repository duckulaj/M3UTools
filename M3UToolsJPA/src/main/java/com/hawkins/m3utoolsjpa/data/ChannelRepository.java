package com.hawkins.m3utoolsjpa.data;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChannelRepository extends PagingAndSortingRepository<Channel, Long> {

	
	Channel findById(long id);

	Channel findByName(String name);
	
	Channel findById(long id, Sort sort);
	
	List<Channel> findBySelected(boolean selected);
}
