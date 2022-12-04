package com.hawkins.m3utoolsjpa.service;

import java.util.LinkedList;
import java.util.concurrent.Future;

import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestParam;

import com.hawkins.dmanager.DManagerApp;
import com.hawkins.dmanager.util.Utils;
import com.hawkins.m3utoolsjpa.job.DownloadJob;
import com.hawkins.m3utoolsjpa.utils.Constants;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DownloadService {

	private boolean stop = false;
	
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
}
