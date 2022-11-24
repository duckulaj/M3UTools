package com.hawkins.m3utoolsjpa.data;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.transaction.annotation.Transactional;

public interface M3UItemRepository extends PagingAndSortingRepository<M3UItem, Long> {

	M3UItem findById(long id);

	List<M3UItem> findByGroupTitle(String groupTitle);

	List<M3UItem> findByTvgName(String tvgName);

	Page<M3UItem> findByGroupTitle(String groupTitle, Pageable pageable);

	Page<M3UItem> findByGroupId(Long groupId, Pageable pageable);
	
	@Transactional(readOnly = true)
	@Query("SELECT DISTINCT groupTitle FROM M3UItem WHERE type = ?1")
	List<String> findDistinctByType(String type);


}
