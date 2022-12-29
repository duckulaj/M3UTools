package com.hawkins.m3utoolsjpa.component;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.hawkins.m3utoolsjpa.data.M3UGroupRepository;
import com.hawkins.m3utoolsjpa.data.M3UItemRepository;
import com.hawkins.m3utoolsjpa.epg.EpgReader;
import com.hawkins.m3utoolsjpa.m3u.M3UtoStrm;
import com.hawkins.m3utoolsjpa.properties.DownloadProperties;
import com.hawkins.m3utoolsjpa.service.M3UService;
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

	@Scheduled(cron = "0 1 1 * * ?") // 1.01am
	public void resetM3UFile() {

		m3uService.resetM3UFile();
	}


	@Scheduled(cron = "0 1 5 * * ?") // 5.01am
	public void createStreams() {

		M3UtoStrm.convertM3UtoStream(itemRepository);
		log.info("Scheduled Task createStreams) completed at {}", Utils.printNow());
	}

	@Scheduled(fixedRate = 3, timeUnit = TimeUnit.HOURS)
	public void reloadEPG() {

		String epgFile = DownloadProperties.getInstance().getEpgFileName();
		
		EpgReader.changeLocalTime(epgFile);
		log.info("Scheduled Task reloadEPG completed at {}", Utils.printNow());
	}
	
	
}