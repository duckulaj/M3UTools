
package com.hawkins.m3utoolsjpa.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import com.hawkins.m3utoolsjpa.exception.DownloadFailureException;
import com.hawkins.m3utoolsjpa.exception.M3UItemsNotFoundException;
import com.hawkins.m3utoolsjpa.utils.Utils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ApplicationService {

    @Autowired
    private M3UService m3uService;

    public void runAtStartup() {
        // Update the database with the latest m3u information
        StopWatch swUpdateDatabase = new StopWatch();
        try {
            swUpdateDatabase.start();
            log.info("Running m3uService.resetDatabase() at {}", Utils.printNow());
            try {
				m3uService.resetDatabase();
			} catch (DownloadFailureException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        } catch (M3UItemsNotFoundException e) {
            log.error("Error resetting database", e);
        } finally {
            swUpdateDatabase.stop();
            log.info("m3uService.resetDatabase() took {}ms", swUpdateDatabase.getTotalTimeMillis());
        }

  
    }
}
