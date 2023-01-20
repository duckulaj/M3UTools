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

		String query = "ALTER SEQUENCE CHANNEL_SEQUENCE START WITH 1001";

		entityManager.createNativeQuery(query).executeUpdate();
	}
}
