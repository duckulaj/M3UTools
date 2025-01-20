
package com.hawkins.m3utoolsjpa.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import com.hawkins.m3utoolsjpa.exception.M3UItemsNotFoundException;
import com.hawkins.m3utoolsjpa.utils.Utils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ApplicationService {

    @Autowired
    private EpgService epgService;

    @Autowired
    private M3UService m3uService;

    public void runAtStartup() {
        // Update the database with the latest m3u information
        StopWatch swUpdateDatabase = new StopWatch();
        try {
            swUpdateDatabase.start();
            log.info("Running m3uService.resetDatabase() at {}", Utils.printNow());
            m3uService.resetDatabase();
        } catch (M3UItemsNotFoundException e) {
            log.error("Error resetting database", e);
        } finally {
            swUpdateDatabase.stop();
            log.info("m3uService.resetDatabase() took {}ms", swUpdateDatabase.getTotalTimeMillis());
        }

        // Create the epg file with latest programmes for the updated m3u items
        StopWatch swReadEPG = new StopWatch();
        try {
            swReadEPG.start();
            log.info("Running epgService.readEPG() at {}", Utils.printNow());
            epgService.readEPG();
        } finally {
            swReadEPG.stop();
            log.info("epgService.readEPG() took {}ms", swReadEPG.getTotalTimeMillis());
        }
    }
}
