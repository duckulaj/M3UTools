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
import com.hawkins.m3utoolsjpa.utils.Constants;
import com.hawkins.m3utoolsjpa.xtream.XtreamCodes;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class XtreamService {

    @Autowired
    M3UItemRepository m3UItemRepository;
    
    @Autowired
    M3UGroupRepository m3UGroupRepository;

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

            // Write each categories JSON to file
            writeJsonToFile(liveCategoriesJson, "./liveCategories.json");
            writeJsonToFile(movieCategoriesJson, "./movieCategories.json");
            writeJsonToFile(seriesCategoriesJson, "./seriesCategories.json");

            m3UItemRepository.deleteAll();
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
        Set<M3UGroup> groups = xtreamCodes.getGroupsFromJson(categoriesJson, type);
        
        databaseService.groupsSaveAllAndFlush(groups);
    }

    /**
     * Helper method to write JSON string to a file
     */
    private void writeJsonToFile(String json, String filePath) throws IOException {
        java.nio.file.Files.write(java.nio.file.Paths.get(filePath), json.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }
}