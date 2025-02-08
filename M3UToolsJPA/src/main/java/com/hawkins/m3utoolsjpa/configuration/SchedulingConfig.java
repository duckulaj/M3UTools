package com.hawkins.m3utoolsjpa.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import com.hawkins.m3utoolsjpa.service.ApplicationService;
import com.hawkins.m3utoolsjpa.service.EpgService;
import com.hawkins.m3utoolsjpa.service.M3UtoStrmService;
import com.hawkins.m3utoolsjpa.utils.Utils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@EnableScheduling
public class SchedulingConfig {

	@Autowired
	M3UtoStrmService m3UtoStrm;

	@Autowired
	ApplicationService  applicationService;

	@Autowired
	EpgService epgService;

	
    @Value("${task.schedule.cron}")
    private String scheduleCron;  // Injected from both application.properties and config.properties

    @Bean
    Runnable myScheduledTask() {
        return new Runnable() {
            @Override
            public void run() {
            	applicationService.runAtStartup();
        		log.info("Scheduled Task updateApplication completed at {}", Utils.printNow());
        		
        		m3UtoStrm.convertM3UtoStream();
        		log.info("Scheduled Task createStreams) completed at {}", Utils.printNow());
        		
        		epgService.readEPG();
        		log.info("Scheduled Task updateApplication completed at {}", Utils.printNow());
            }
        };
    }

    @Scheduled(cron = "${task.schedule.cron}")
    public void runScheduledTask() {
        log.info("Running scheduled task at interval: " + scheduleCron);
        
        applicationService.runAtStartup();
		log.info("Scheduled Task updateApplication completed at {}", Utils.printNow());
		
		m3UtoStrm.convertM3UtoStream();
		log.info("Scheduled Task createStreams) completed at {}", Utils.printNow());
		
		epgService.readEPG();
		log.info("Scheduled Task updateApplication completed at {}", Utils.printNow());
    }
}
