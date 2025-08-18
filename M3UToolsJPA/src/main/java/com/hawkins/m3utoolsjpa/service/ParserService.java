package com.hawkins.m3utoolsjpa.service;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hawkins.m3utoolsjpa.annotations.TrackExecutionTime;
import com.hawkins.m3utoolsjpa.data.M3UGroup;
import com.hawkins.m3utoolsjpa.data.M3UGroupRepository;
import com.hawkins.m3utoolsjpa.data.M3UItem;
import com.hawkins.m3utoolsjpa.data.M3UItemRepository;
import com.hawkins.m3utoolsjpa.exception.DownloadFailureException;
import com.hawkins.m3utoolsjpa.properties.DownloadProperties;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ParserService {

		/*
		 * 1. Load m3uFile from url
		 * 2. Extract M3UItems from m3u file
		 * 3. Create List of Unique M3UGroups
		 * 4. Remove groups that are not in the includedCountries property
		 * 5. Create list of M3Uitems associated with those groups defined in step 4
		 */
	
	@Autowired
	M3UGroupRepository groupRepository;
	
	@Autowired
	M3UItemRepository itemRepository;
	
	@Autowired
	DatabaseService databaseService;
		
	@Autowired
	ParserUtilsService parserUtilsService;
	
	DownloadProperties dp = DownloadProperties.getInstance();
	String[] includedCountries = dp.getIncludedCountries();
	
	
	@TrackExecutionTime
	public Set<M3UItem> parseM3UFile() throws DownloadFailureException {
		log.info("Parsing M3U File");
		
		
		
		Set<M3UItem> m3uItems = parserUtilsService.parse();
		log.info("Number of M3UItems: {}", m3uItems.size());
		
		Set<M3UGroup> uniqueGroups = parserUtilsService.extractUniqueTvgGroups(m3uItems);
		log.info("Number of unique tvg-groups: {}", uniqueGroups.size());
		
		databaseService.deleteItemsAndGroups();
		databaseService.groupsSaveAllAndFlush(uniqueGroups);
		// uniqueGroups = ParserUtils.removeGroupsNotInIncludedCountries(uniqueGroups, includedCountries);
		// log.info("Number of unique tvg-groups after removing groups not in includedCountries: {}", uniqueGroups.size());
		
		Set<M3UItem> filteredItems = parserUtilsService.createM3UItemsListIfGroupExists(uniqueGroups, m3uItems);
		log.info("Number of M3UItems after filtering: {}", filteredItems.size());
		
		
		databaseService.itemsSaveAllAndFlush(filteredItems);
		
		
		return filteredItems;
				
	}
	
		
}
