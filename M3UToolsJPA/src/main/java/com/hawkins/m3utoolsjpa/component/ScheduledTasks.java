package com.hawkins.m3utoolsjpa.component;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.hawkins.m3utoolsjpa.service.ApplicationService;
import com.hawkins.m3utoolsjpa.service.EpgService;
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

	@Autowired
	EpgService epgService;

	@Scheduled(cron = "0 0 2 * * ?") // 02:00 AM every day
	public void createStreams() {

		m3UtoStrm.convertM3UtoStream();
		log.info("Scheduled Task createStreams) completed at {}", Utils.printNow());
	}



	@Scheduled(cron = "0 0 1 * * ?") // 01:00 AM every day
	public void updateApplication() {

		applicationService.runAtStartup();
		log.info("Scheduled Task updateApplication completed at {}", Utils.printNow());
	}

	@Scheduled(cron = "0 0 5 * * ?") // 01:00 AM every day
	public void reloadEPG() {

		epgService.readEPG();
		log.info("Scheduled Task updateApplication completed at {}", Utils.printNow());
	}


}