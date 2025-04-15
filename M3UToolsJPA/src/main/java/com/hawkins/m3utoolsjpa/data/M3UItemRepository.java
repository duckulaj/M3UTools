
package com.hawkins.m3utoolsjpa.data;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.hawkins.m3utoolsjpa.utils.Constants;

public interface M3UItemRepository extends JpaRepository<M3UItem, Long> {

    String FIND_BY_TYPE_AND_CHANNEL_NAME = "SELECT m0 FROM M3UItem m0 WHERE type = ?1 AND channelName LIKE ?2";
    String FIND_ALL_BY_TYPE = "SELECT m0 FROM M3UItem m0 WHERE type = ?1";
    String FIND_LIST_BY_CHANNEL_NAME = "SELECT m0 FROM M3UItem m0 WHERE channelName LIKE ?1";
    String FIND_TV_CHANNELS = "SELECT m0 FROM M3UItem m0 WHERE type = '" + Constants.LIVE + "'";
    String FIND_TV_CHANNELS_BY_GROUP = "SELECT m0 FROM M3UItem m0 WHERE type = '" + Constants.LIVE + "' AND groupId = ?1";
    String FIND_TV_CHANNELS_BY_SELECTED = "SELECT m0 FROM M3UItem m0 WHERE type = '" + Constants.LIVE + "' AND selected = ?1";
    String FIND_TV_CHANNELS_BY_SELECTED_AND_GROUP = "SELECT m0 FROM M3UItem m0 WHERE type = '" + Constants.LIVE + "' AND selected = ?1 AND groupId = ?2";

    Optional<M3UItem> findById(long id);

    Optional<M3UItem> findByChannelName(String channelName);

    List<M3UItem> findByGroupTitle(String groupTitle);

    List<M3UItem> findByTypeOrderByTvgName(String type);

    List<M3UItem> findByTvgName(String tvgName);

    List<M3UItem> findByTvgId(String tvgId);

    List<M3UItem> findByTvgIdAndTvgName(String tvgId, String tvgName);

    Page<M3UItem> findByGroupTitle(String groupTitle, Pageable pageable);

    Page<M3UItem> findByGroupId(Long groupId, Pageable pageable);

    List<M3UItem> findByGroupId(Long tvgId);

    @Transactional(readOnly = true)
    @Query(FIND_BY_TYPE_AND_CHANNEL_NAME)
    List<M3UItem> findByChannelName(String type, String channelName);

    @Transactional(readOnly = true)
    @Query(FIND_ALL_BY_TYPE)
    List<M3UItem> findAllByType(String type);

    @Transactional(readOnly = true)
    @Query(FIND_LIST_BY_CHANNEL_NAME)
    List<M3UItem> findListByChannelName(String channelName);

    @Transactional(readOnly = true)
    @Query(FIND_TV_CHANNELS)
    Page<M3UItem> findTvChannels(Pageable pageable);

    @Transactional(readOnly = true)
    @Query(FIND_TV_CHANNELS)
    List<M3UItem> findTvChannels();

    @Transactional(readOnly = true)
    @Query(FIND_TV_CHANNELS_BY_GROUP)
    Page<M3UItem> findTvChannelsByGroup(Long groupId, Pageable pageable);

    @Transactional(readOnly = true)
    @Query(FIND_TV_CHANNELS_BY_GROUP)
    List<M3UItem> findTvChannelsByGroup(Long groupId);

    @Transactional(readOnly = true)
    @Query(FIND_TV_CHANNELS_BY_SELECTED)
    List<M3UItem> findTvChannelsBySelected(boolean selected);

    @Transactional(readOnly = true)
    @Query(FIND_TV_CHANNELS_BY_SELECTED_AND_GROUP)
    List<M3UItem> findTvChannelsBySelectedAndGroup(boolean selected, Long groupId);

    @Transactional
    @Modifying
    @Query("update M3UItem m0 set m0.selected = :selected where m0.tvgId = :tvgId and m0.groupId = :groupId and m0.tvgName = :tvgName")
    void updateSelected(@Param(value = "tvgId") String tvgId, @Param(value = "groupId") String groupId, @Param(value = "tvgName") String tvgName, @Param(value = "selected") boolean selected);

    @Transactional
    @Modifying
    @Query("update M3UItem m0 set m0.tvgChNo = :tvgChNo where m0.id = :id")
    void updateTvgChNo(@Param(value = "id") long id, @Param(value = "tvgChNo") String tvgChNo);
}
