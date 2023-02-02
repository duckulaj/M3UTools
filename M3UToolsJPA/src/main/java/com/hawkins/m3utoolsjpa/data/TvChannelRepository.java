package com.hawkins.m3utoolsjpa.data;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TvChannelRepository extends PagingAndSortingRepository<TvChannel, Long> {

	TvChannel findById(String id);
	
	List<TvChannel> findAll(Sort sort);


}
