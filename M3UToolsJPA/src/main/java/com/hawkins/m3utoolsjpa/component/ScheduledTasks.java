package com.hawkins.m3utoolsjpa.component;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.hawkins.m3utoolsjpa.service.ApplicationService;
import com.hawkins.m3utoolsjpa.service.M3UtoStrmService;
import com.hawkins.m3utoolsjpa.utils.Utils;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ScheduledTasks {
	
	@Autowired
	M3UtoStrmService m3UtoStrm;
	
	@Autowired
	ApplicationService  applicationService;

	@Scheduled(cron = "0 1 */2 * *") // 1.00am every other day
	public void createStreams() {

		m3UtoStrm.convertM3UtoStream();
		log.info("Scheduled Task createStreams) completed at {}", Utils.printNow());
	}

		
	@Scheduled(cron = "0 0 0/6 ? * *") // Every four hours 
	public void updateApplication() {

		applicationService.runAtStartup();
		log.info("Scheduled Task updateApplication completed at {}", Utils.printNow());
	}

	
	
}