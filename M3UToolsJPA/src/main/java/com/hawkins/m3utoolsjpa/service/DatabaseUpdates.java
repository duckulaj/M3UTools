package com.hawkins.m3utoolsjpa.service;

import org.springframework.stereotype.Component;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

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
