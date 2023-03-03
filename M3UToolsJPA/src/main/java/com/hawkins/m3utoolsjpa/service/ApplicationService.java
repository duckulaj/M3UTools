package com.hawkins.m3utoolsjpa.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ApplicationService {

	@Autowired
	EpgService epgService;
	
	@Autowired
	M3UService m3uService;
	
	
	public void runAtStartup() {
		
				// Update the database with the latest m3u information
				
				StopWatch swUpdateDatabase = new org.springframework.util.StopWatch();
				swUpdateDatabase.start();
				
				log.info("Running m3uService.resetDatabase() at startup");
				m3uService.resetDatabase();
				
				swUpdateDatabase.stop();
				log.info("m3uService.resetDatabase() took {}ms", swUpdateDatabase.getTotalTimeMillis());
				
				// Create the epg file with latest programmes for the updated m3u items
				
				StopWatch swReadEPG = new org.springframework.util.StopWatch();
				swReadEPG.start();
				
				log.info("Running epgService.readEPG() at startup");
				epgService.readEPG();
				
				swReadEPG.stop();
				log.info("epgService.readEPG() took {}ms", swReadEPG.getTotalTimeMillis());

		
		
	}
}
