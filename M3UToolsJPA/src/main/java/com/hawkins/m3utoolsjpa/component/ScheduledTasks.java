package com.hawkins.m3utoolsjpa.component;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.hawkins.m3utoolsjpa.data.M3UGroupRepository;
import com.hawkins.m3utoolsjpa.data.M3UItemRepository;
import com.hawkins.m3utoolsjpa.service.ApplicationService;
import com.hawkins.m3utoolsjpa.service.M3UService;
import com.hawkins.m3utoolsjpa.service.M3UtoStrmService;
import com.hawkins.m3utoolsjpa.utils.Utils;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ScheduledTasks {
	
	@Autowired
	M3UItemRepository itemRepository;

	@Autowired 
	M3UGroupRepository groupRepository;
	
	@Autowired
	M3UService m3uService;
	
	@Autowired
	M3UtoStrmService m3UtoStrm;
	
	@Autowired
	ApplicationService  applicationService;

	@Scheduled(cron = "0 1 1 * * ?") // 1.01am
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