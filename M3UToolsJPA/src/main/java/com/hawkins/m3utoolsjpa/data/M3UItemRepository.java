package com.hawkins.m3utoolsjpa.data;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.hawkins.m3utoolsjpa.utils.Constants;

@Repository
public interface M3UItemRepository extends PagingAndSortingRepository<M3UItem, Long> {

	M3UItem findById(long id);
	
	M3UItem findByChannelName(String channelName);

	List<M3UItem> findByGroupTitle(String groupTitle);
	
	List<M3UItem> findByType(String type);

	List<M3UItem> findByTvgName(String tvgName);
	
	M3UItem findByTvgNameDistinct(String tvgName);

	Page<M3UItem> findByGroupTitle(String groupTitle, Pageable pageable);

	Page<M3UItem> findByGroupId(Long groupId, Pageable pageable);
		
	@Transactional(readOnly = true)
	@Query("SELECT m0 FROM M3UItem m0 WHERE type = ?1 AND channelName LIKE ?2")
	List<M3UItem> findByChannelName(String type, String channelName);
	
	@Transactional(readOnly = true)
	@Query("SELECT m0 FROM M3UItem m0 WHERE type = '" +  Constants.LIVE + "'")
	Page<M3UItem> findTvChannels(Pageable pageable);
	
	@Transactional(readOnly = true)
	@Query("SELECT m0 FROM M3UItem m0 WHERE type = '" +  Constants.LIVE + "'")
	List<M3UItem> findTvChannels();
	
	@Transactional(readOnly = true)
	@Query("SELECT m0 FROM M3UItem m0 WHERE type = '" +  Constants.LIVE + "'" + " AND groupId = ?1")
	Page<M3UItem> findTvChannelsByGroup(Long groupId, Pageable pageable);
	
	@Transactional(readOnly = true)
	@Query("SELECT m0 FROM M3UItem m0 WHERE type = '" +  Constants.LIVE + "'" + " AND groupId = ?1")
	List<M3UItem> findTvChannelsByGroup(Long groupId);
	
	@Transactional(readOnly = true)
	@Query("SELECT m0 FROM M3UItem m0 WHERE type = '" +  Constants.LIVE + "'" + " AND selected = ?1")
	List<M3UItem> findTvChannelsBySelected(boolean selected);
	
	@Transactional(readOnly = true)
	@Query("SELECT m0 FROM M3UItem m0 WHERE type = '" +  Constants.LIVE + "'" + " AND selected = ?1 AND groupId = ?2")
	List<M3UItem> findTvChannelsBySelectedAndGroup(boolean selected, Long groupId);
	
	@Transactional
	@Modifying
	@Query("update M3UItem m0 set m0.selected = :selected where m0.id = :id")
	void updateSelected(@Param(value = "id") long id, @Param(value = "selected") boolean selected);
	
	@Transactional
	@Modifying
	@Query("update M3UItem m0 set m0.tvgChNo = :tvgChNo where m0.id = :id")
	void updateTvgChNo(@Param(value = "id") long id, @Param(value = "tvgChNo") String tvgChNo);

}
