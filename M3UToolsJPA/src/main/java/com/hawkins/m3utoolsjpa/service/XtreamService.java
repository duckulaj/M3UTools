package com.hawkins.m3utoolsjpa.service;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hawkins.m3utoolsjpa.data.M3UGroup;
import com.hawkins.m3utoolsjpa.data.M3UGroupRepository;
import com.hawkins.m3utoolsjpa.data.M3UItemRepository;
import com.hawkins.m3utoolsjpa.exception.DownloadFailureException;
import com.hawkins.m3utoolsjpa.properties.DownloadProperties;
import com.hawkins.m3utoolsjpa.redis.M3UGroupRedisService;
import com.hawkins.m3utoolsjpa.redis.M3UItemCacheInitializer;
import com.hawkins.m3utoolsjpa.redis.M3UItemRedisService;
import com.hawkins.m3utoolsjpa.utils.Constants;
import com.hawkins.m3utoolsjpa.xtream.XtreamCodes;
import com.hawkins.m3utoolsjpa.xtream.XtreamCredentials;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class XtreamService {

    

    
    private static final DownloadProperties dp = DownloadProperties.getInstance();
    private static final XtreamCredentials xtreamCredentials = XtreamCredentials.getInstance();

    @Autowired
    M3UItemRepository m3UItemRepository;
    
    @Autowired
    M3UItemRedisService m3UItemRedisService;
    
    @Autowired
    M3UItemCacheInitializer m3UItemCacheInitializer;
    
    @Autowired
    M3UGroupRepository m3UGroupRepository;

    @Autowired
    M3UGroupRedisService m3UGroupRedisService;

    @Autowired
    DatabaseService databaseService;
    
    @Autowired
    ParserService parserService;

    @Autowired
    XtreamCodes xtreamCodes;

    XtreamService() {
        
    }

    /**
     * Fetches Xtream data (categories and items), processes and stores them in the database and Redis cache.
     * Logs progress and handles errors gracefully.
     */
    public void getXtreamData() {
        try {
            // Fetch categories JSON for each type
            String liveCategoriesJson = XtreamCodes.getCategoriesJson("live");
            String movieCategoriesJson = XtreamCodes.getCategoriesJson("movie");
            String seriesCategoriesJson = XtreamCodes.getCategoriesJson("series");

            m3UItemRedisService.deleteAll();
            m3UItemRepository.deleteAll();
            m3UGroupRedisService.deleteAll();
            m3UGroupRepository.deleteAll();
            
            // Process and save groups for each type
            saveGroupsWithType(liveCategoriesJson, "live");
            saveGroupsWithType(movieCategoriesJson, "movie");
            saveGroupsWithType(seriesCategoriesJson, "series");
        } catch (IOException e) {
            log.error("Error fetching or processing Xtream categories", e);
            return;
        }

        // Fetch Xtream items (side effect method)
        XtreamCodes.getXtreamCodesItems();

        // Refresh Redis cache
        
        for (M3UGroup group : m3UGroupRepository.findAll()) {
            m3UGroupRedisService.save(group);
        }

        // Convert all JSON to a single M3U file
        xtreamCodes.convertAllJsonToSingleM3U(
            new File("./live.json"),
            new File("./movie.json"),
            new File("./series.json"),
            new File(Constants.M3U_FILE)
        );
        
        try {
			parserService.parseM3UFile();
		} catch (DownloadFailureException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        log.info("Xtream data fetch and processing completed.");
    }

    /**
     * Helper method to parse groups from JSON, set their type, and save them.
     */
    private void saveGroupsWithType(String categoriesJson, String type) {
        Set<M3UGroup> groups = XtreamCodes.getGroupsFromJson(categoriesJson, type);
        for (M3UGroup group : groups) {
            group.setType(type);
        }
        databaseService.groupsSaveAllAndFlush(groups);
    }
}