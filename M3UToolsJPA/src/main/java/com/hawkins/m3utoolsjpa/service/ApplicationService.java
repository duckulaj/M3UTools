package com.hawkins.m3utoolsjpa.service;

import java.io.File;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import com.hawkins.m3utoolsjpa.exception.M3UItemsNotFoundException;
import com.hawkins.m3utoolsjpa.utils.Constants;
import com.hawkins.m3utoolsjpa.utils.Utils;

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
				
				log.info("Running m3uService.resetDatabase() at {}", Utils.printNow());
				try {
					// File m3uFile = new File(Constants.M3U_FILE);
					// if (m3uFile.exists()) m3uFile.delete();
					m3uService.resetDatabase();
				} catch (M3UItemsNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				swUpdateDatabase.stop();
				log.info("m3uService.resetDatabase() took {}ms", swUpdateDatabase.getTotalTimeMillis());
				
				  // Create the epg file with latest programmes for the updated m3u items
				  
				  StopWatch swReadEPG = new org.springframework.util.StopWatch();
				  swReadEPG.start();
				  
				  log.info("Running epgService.readEPG() at {}", Utils.printNow()); 
				  // File epgFile = new File(Constants.EPG_XML); 
				  // if (epgFile.exists()) epgFile.delete(); 
				  epgService.readEPG();
				  
				  swReadEPG.stop(); log.info("epgService.readEPG() took {}ms",
				  swReadEPG.getTotalTimeMillis());
				 
		
		
	}
}
