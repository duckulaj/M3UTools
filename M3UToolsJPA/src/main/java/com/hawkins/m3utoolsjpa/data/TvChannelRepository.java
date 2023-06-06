package com.hawkins.m3utoolsjpa.data;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;


public interface TvChannelRepository extends JpaRepository<TvChannel, Long> {

	Optional<TvChannel> findById(Long id);
	
	TvChannel findByTvgName(String tvgName);
	
	List<TvChannel> findAll(Sort sort);
	
	List<TvChannel> findAll();
	
	List<TvChannel> findByGroupId(Long groupId);
	

	@Transactional(readOnly = true)
	@Query("SELECT MAX(m0.channelID) FROM TvChannel m0")
	Long getMaxChannelNumber();
}
