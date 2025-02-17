package com.hawkins.m3utoolsjpa.service;

import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hawkins.m3utoolsjpa.annotations.TrackExecutionTime;
import com.hawkins.m3utoolsjpa.data.M3UGroup;
import com.hawkins.m3utoolsjpa.data.M3UGroupRepository;
import com.hawkins.m3utoolsjpa.data.M3UItem;
import com.hawkins.m3utoolsjpa.data.M3UItemRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaDelete;
import jakarta.transaction.Transactional;

@Service
@Transactional
public class DatabaseService {

	@PersistenceContext
	private EntityManager em;

	@Autowired
	M3UItemRepository itemRepository;

	@Autowired
	M3UGroupRepository groupRepository;

	@TrackExecutionTime
	public void deleteItemsAndGroups() {
		itemRepository.flush();
		groupRepository.flush();

		CriteriaBuilder cb = em.getCriteriaBuilder();

		CriteriaDelete<M3UItem> deleteItems = cb.createCriteriaDelete(M3UItem.class);
		em.createQuery(deleteItems).executeUpdate();

		CriteriaDelete<M3UGroup> deleteGroups = cb.createCriteriaDelete(M3UGroup.class);
		em.createQuery(deleteGroups).executeUpdate();
	}
	
	@TrackExecutionTime
	public void groupsSaveAllAndFlush(Set<M3UGroup> uniqueGroups) {
		groupRepository.saveAllAndFlush(uniqueGroups);
	}
	
	@TrackExecutionTime
	public void itemsSaveAllAndFlush(LinkedList<M3UItem> filteredItems) {
	    int parallelism = Runtime.getRuntime().availableProcessors();
	    try (ForkJoinPool customThreadPool = new ForkJoinPool(parallelism)) {
			try {
			    customThreadPool.submit(() -> 
			        filteredItems.parallelStream()
			                     .collect(Collectors.groupingByConcurrent(item -> filteredItems.indexOf(item) / 1000))
			                     .values()
			                     .parallelStream()
			                     .forEach(batch -> itemRepository.saveAllAndFlush(batch))
			    ).get();
			} catch (Exception e) {
			    e.printStackTrace();
			} finally {
			    customThreadPool.shutdown();
			}
		}
	}

}
