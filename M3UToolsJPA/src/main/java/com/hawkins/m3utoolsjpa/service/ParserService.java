package com.hawkins.m3utoolsjpa.service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
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
	
	@Autowired
	XtreamParserUtilsService xtreamParserUtilsService;
	
	DownloadProperties dp = DownloadProperties.getInstance();
	String[] includedCountries = dp.getIncludedCountries();
	
	
	@TrackExecutionTime
	public Set<M3UItem> parseM3UFile() throws DownloadFailureException {
		log.info("Parsing M3U File");
		
		
		
		// Set<M3UItem> m3uItems = parserUtilsService.parse();
		
		Set<M3UItem> m3uItems =  xtreamParserUtilsService.parse();
		log.info("Number of M3UItems: {}", m3uItems.size());
		
		Set<M3UGroup> m3uGroups = xtreamParserUtilsService.extractUniqueTvgGroups(m3uItems);
		log.info("Number of unique M3UGroups: {}", m3uGroups.size());
		
		groupRepository.saveAllAndFlush(m3uGroups);
		log.info("Filtered M3UGroups saved to the database.");
		
		// Retrieve groups that now have an id
		List<M3UGroup> savedM3uGroups = groupRepository.findAll();
		
		m3uGroups.clear();
		
		// convert List to Set
		m3uGroups = Set.copyOf(savedM3uGroups);
		
		// Now filter M3UItems to include only those whose group is in filteredGroups
		Set<M3UItem> filteredItems = xtreamParserUtilsService.createM3UItemsListIfGroupExists(m3uGroups, m3uItems);
		log.info("Number of filtered M3UItems: {}", filteredItems.size());
		
		// Clear existing items in the database before saving new ones
		itemRepository.deleteAll();
		log.info("Existing M3UItems cleared from the database.");
		
		Set<M3UItem> newItems = new HashSet<>();
			
		// Save filtered items to the database
		
		for (M3UItem item : filteredItems) {
			
			try {
				Optional<M3UGroup> group = groupRepository.findById(item.getGroupId());
				if (group.isPresent()) {
					item.setGroupId(group.get().getId());
					log.debug("Saving M3UItem: {} with Group ID: {}", item.getChannelName(), item.getGroupId());
					newItems.add(item);
				} else {
					log.warn("No matching group found for item: {}", item.getChannelName());
				}
			} catch (Exception e) {
				log.error("Error finding group for item: {}", item.getChannelName(), e);
			}
			
		}
		
		databaseService.itemsSaveAllAndFlush(newItems);
		return newItems;
				
	}
	
		
}
