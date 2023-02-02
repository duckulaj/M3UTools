package com.hawkins.m3utoolsjpa.service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import org.springframework.stereotype.Component;

@Component
public class DatabaseUpdates {

	@PersistenceContext
	private EntityManager entityManager;

	@Transactional
	public void resetChannelSequence() {

		String query = "DROP SEQUENCE ITEM_SEQUENCE";
		entityManager.createNativeQuery(query).executeUpdate();
		
		query = "CREATE SEQUENCE ITEM_SEQUENCE START WITH 1001";
		entityManager.createNativeQuery(query).executeUpdate();
	}
}
