package com.hawkins.m3utoolsjpa.service;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hawkins.m3utoolsjpa.data.M3UGroup;
import com.hawkins.m3utoolsjpa.data.M3UGroupRepository;
import com.hawkins.m3utoolsjpa.properties.DownloadProperties;
import com.hawkins.m3utoolsjpa.redis.M3UGroupRedisService;
import com.hawkins.m3utoolsjpa.xtream.XtreamCodes;
import com.hawkins.m3utoolsjpa.xtream.XtreamCredentials;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class XtreamService {
	
    private static final DownloadProperties dp = DownloadProperties.getInstance();
    private static final XtreamCredentials xtreamCredentials = XtreamCredentials.getInstance();
    
    @Autowired
    M3UGroupRepository m3UGroupRepository;
    
    @Autowired
	M3UGroupRedisService m3UGroupRedisService;
    
    @Autowired
	DatabaseService databaseService;
    
    public void GetXtreamData() {
		
		/*
		 * TODO: Implement Xtream data fetching logic here.
		 * This might involve making HTTP requests to an Xtream API,
		 * parsing the response, and storing the data as needed.
		 * 
		 * 1. Get a list of categories from the Xtream service.
		 * 2. Get all live items from the Xtream service.
		 * 3. Get all VOD items from the Xtream service.
		 * 4. Get all series items from the Xtream service.
		 * 5. Get all catchup items from the Xtream service.
		 * 6. Process and store the fetched data as required.
		 * 7. Handle any exceptions or errors that may occur during the process.
		 * 8. Log relevant information for debugging and monitoring purposes.
		 * 
		 */
		
		try {
			String liveCategoriesJson = XtreamCodes.getCategoriesJson("live");
			String movieCategoriesJson = XtreamCodes.getCategoriesJson("movie");
			String seriesCategoriesJson = XtreamCodes.getCategoriesJson("series");
			
			 Set<M3UGroup> liveGroups = XtreamCodes.getGroupsFromJson(liveCategoriesJson);
			for (M3UGroup group : liveGroups) {
				group.setType("live");
			}
			 Set<M3UGroup> movieGroups = XtreamCodes.getGroupsFromJson(movieCategoriesJson);
			 for (M3UGroup group : movieGroups) {
					group.setType("movie");
			 }
			 Set<M3UGroup> seriesGroups = XtreamCodes.getGroupsFromJson(seriesCategoriesJson);
			 for (M3UGroup group : seriesGroups) {
                    group.setType("series");
             }
			 
			 databaseService.groupsSaveAllAndFlush(liveGroups);
			 databaseService.groupsSaveAllAndFlush(movieGroups);	
			 databaseService.groupsSaveAllAndFlush(seriesGroups);
			 
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		XtreamCodes.getXtreamCodesItems();

		m3UGroupRedisService.deleteAll();
		for (M3UGroup group : m3UGroupRepository.findAll()) {
			m3UGroupRedisService.save(group);
		}
		
		XtreamCodes.convertAllJsonToSingleM3U(new File("./live.json"), new File("./movie.json"), new File("./series.json") ,new File(dp.getFullM3U()));
		
		log.info("Xtream data fetch not implemented yet.");
	}

}