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

@Service
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
		
		CriteriaBuilder cbItems = em.getCriteriaBuilder();
		
		CriteriaDelete<M3UItem> deleteItems = cbItems.createCriteriaDelete(M3UItem.class);
		
		this.em.createQuery(deleteItems).executeUpdate();
		
		groupRepository.flush();
		
		CriteriaBuilder cbGroups = em.getCriteriaBuilder();
		
		CriteriaDelete<M3UGroup> deleteGroups = cbGroups.createCriteriaDelete(M3UGroup.class);
		
		this.em.createQuery(deleteGroups).executeUpdate();
	}
}
