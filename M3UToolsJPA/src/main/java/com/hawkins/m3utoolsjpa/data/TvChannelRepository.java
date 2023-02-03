package com.hawkins.m3utoolsjpa.data;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.hawkins.m3utoolsjpa.utils.Constants;

@Repository
public interface TvChannelRepository extends PagingAndSortingRepository<TvChannel, Long> {

	TvChannel findById(String id);
	
	TvChannel findByTvgName(String tvgName);
	
	List<TvChannel> findAll(Sort sort);

	@Transactional(readOnly = true)
	@Query("SELECT MAX(m0.channelID) FROM TvChannel m0")
	Long getMaxChannelNumber();
}
