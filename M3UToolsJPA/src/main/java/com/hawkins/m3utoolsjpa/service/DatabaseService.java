package com.hawkins.m3utoolsjpa.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hawkins.m3utoolsjpa.data.M3UGroup;
import com.hawkins.m3utoolsjpa.data.M3UGroupRepository;
import com.hawkins.m3utoolsjpa.data.M3UItem;
import com.hawkins.m3utoolsjpa.data.M3UItemRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaDelete;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@Transactional
public class DatabaseService {

	@PersistenceContext
	private EntityManager em;
	
	@Autowired
	M3UItemRepository itemRepository;
	
	@Autowired
	M3UGroupRepository groupRepository;
	
	public void DeleteItemsAndGroups() {
		
		itemRepository.flush();
		
		CriteriaBuilder cb = em.getCriteriaBuilder();
		
		CriteriaDelete<M3UItem> deleteItems = cb.createCriteriaDelete(M3UItem.class);
		
		// Root ei = deleteItems.from(M3UItem.class);
		
		this.em.createQuery(deleteItems).executeUpdate();
		
		groupRepository.flush();
		
		CriteriaDelete<M3UGroup> deleteGroups = cb.createCriteriaDelete(M3UGroup.class);
		
		// Root eg = deleteGroups.from(M3UGroup.class);
		
		this.em.createQuery(deleteGroups).executeUpdate();
	}
}
