package com.hawkins.m3utoolsjpa.service;

import java.util.LinkedList;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.hawkins.m3utoolsjpa.data.M3UGroup;
import com.hawkins.m3utoolsjpa.data.M3UItem;
import com.hawkins.m3utoolsjpa.properties.DownloadProperties;
import com.hawkins.m3utoolsjpa.utils.Constants;
import com.hawkins.m3utoolsjpa.utils.ParserUtils;

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
	
	DownloadProperties dp = DownloadProperties.getInstance();
	String[] includedCountries = dp.getIncludedCountries();
	
	public LinkedList<M3UItem> parseM3UFile() {
		log.info("Parsing M3U File");
		
		ParserUtils.loadM3UFileFromUrl(dp.getStreamChannels(), Constants.M3U_FILE);
		LinkedList<M3UItem> m3uItems = ParserUtils.parse();
		log.info("Number of M3UItems: {}", m3uItems.size());
		
		Set<M3UGroup> uniqueGroups = ParserUtils.extractUniqueTvgGroups(m3uItems);
		log.info("Number of unique tvg-groups: {}", uniqueGroups.size());
		
		uniqueGroups = ParserUtils.removeGroupsNotInIncludedCountries(uniqueGroups, includedCountries);
		log.info("Number of unique tvg-groups after removing groups not in includedCountries: {}", uniqueGroups.size());
		
		LinkedList<M3UItem> filteredItems = ParserUtils.createM3UItemsListIfGroupExists(uniqueGroups, m3uItems);
		log.info("Number of M3UItems after filtering: {}", filteredItems.size());
		
		return filteredItems;
	}
	
		
}
