package com.hawkins.m3utoolsjpa.service;

import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Future;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestParam;

import com.hawkins.dmanager.DManagerApp;
import com.hawkins.m3utoolsjpa.data.M3UItem;
import com.hawkins.m3utoolsjpa.data.M3UItemRepository;
import com.hawkins.m3utoolsjpa.properties.DownloadProperties;
import com.hawkins.m3utoolsjpa.utils.Constants;
import com.hawkins.m3utoolsjpa.utils.Utils;
import com.hawkins.m3uttoolsjpa.jobs.DownloadJob;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DownloadService {

	private boolean stop = false;
	
	private final Path fileStorageLocation;
	
	@Autowired M3UItemRepository itemRepository;
	
	@Autowired
    public DownloadService() {
        this.fileStorageLocation = Paths.get(DownloadProperties.getInstance().getDownloadPath()).toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
	
	@Async
    public Future<Boolean> doWork(Runnable startDownload) {
        
		if (log.isDebugEnabled()) {
			log.debug("Got runnable {}", startDownload);
		}
	
        startDownload.run();
        
        stop = true;
        return new AsyncResult<>(stop);
    }
	
	public String interruptJob(Model model, @RequestParam String name) {
  	  
  	  DManagerApp.getInstance().pauseDownload(name); 
  	  DownloadJob job = Utils.findJobByName(new LinkedList<DownloadJob>(), name);
  	  
  	  if (job != null) job.stop();
  	  
  	  return Constants.STATUS; 
  	}
	
	public Resource loadFileAsResource(String fileName) {
        try {
        	
        	List<M3UItem> m3uItems = itemRepository.findByTvgName(fileName);
        	
        	M3UItem thisItem;
        	if (m3uItems.size() > 0) {
        		thisItem = m3uItems.get(0);
        		String itemURL = thisItem.getChannelUri();
        		fileName = Utils.normaliseName(fileName);
        		Utils.copyUrlToFile(itemURL, fileName);
        	}
        	
        	
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if(resource.exists()) {
                return resource;
            } else {
            	
                log.info("File not found " + fileName);
            }
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
        }
        
        return null;
    }
}
