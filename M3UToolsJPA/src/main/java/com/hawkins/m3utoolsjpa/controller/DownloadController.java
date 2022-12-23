package com.hawkins.m3utoolsjpa.controller;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.LinkedList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.simp.SimpMessagingTemplate;
// import org.springframework.mobile.device.Device;
// import org.springframework.mobile.device.site.SitePreference;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.hawkins.dmanager.properties.DmProperties;
import com.hawkins.dmanager.util.Utils;
import com.hawkins.m3utoolsjpa.data.M3UItemRepository;
import com.hawkins.m3utoolsjpa.job.DownloadJob;
import com.hawkins.m3utoolsjpa.m3u.M3UDownloadItem;
import com.hawkins.m3utoolsjpa.m3u.M3UGroupSelected;
import com.hawkins.m3utoolsjpa.properties.DownloadProperties;
import com.hawkins.m3utoolsjpa.search.MovieDb;
import com.hawkins.m3utoolsjpa.service.DownloadService;
import com.hawkins.m3utoolsjpa.service.M3UService;
import com.hawkins.m3utoolsjpa.utils.Constants;

import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
public class DownloadController {

	
	
	@Qualifier("taskExecutor")
	@Autowired
	private ThreadPoolTaskExecutor myExecutor;

	@Autowired
	private SimpMessagingTemplate template;

	private int jobNumber;
	private LinkedList<DownloadJob> myDownloadList = new LinkedList<DownloadJob>();

	@Autowired
	private DownloadService downloadService;

	@Autowired
	M3UService m3uService;
	
	@Autowired
	M3UItemRepository m3uItemRepository;
	
	@Autowired
	DownloadController(DownloadService downloadService) {
		this.downloadService = downloadService;
	}
	
	DownloadProperties downloadProperties = DownloadProperties.getInstance();
	DmProperties dmProperties = DmProperties.getInstance();
	
	@PostMapping(value = "/download", params = { "name" })
	public String download(Model model, @RequestParam String name, HttpServletResponse response,
			HttpServletRequest request) {

		if (downloadProperties == null) {
			downloadProperties = DownloadProperties.getInstance();
		}
		
		if (log.isDebugEnabled()) {
			log.debug("download {}", name);
		}

		try {
			URL url = Utils.getFinalLocation(Utils.getURLFromName(name, m3uItemRepository));

			URLConnection u = url.openConnection();

			long length = 0L;
			try {
				length = Long.parseLong(u.getHeaderField("Content-Length"));
			} catch (NumberFormatException nfe) {
				log.debug(nfe.getMessage());
			}
			String type = u.getHeaderField("Content-Type");
			String lengthString = Utils.format(length, 2);

			if (log.isDebugEnabled()) {
				log.debug("File of type {} is {}", type, lengthString);
			}

			M3UDownloadItem downloadItem = new M3UDownloadItem();
			downloadItem.setUrl(url);
			downloadItem.setName(
					downloadProperties.getDownloadPath() + name + "." + Utils.getFileExtension(url.toString()));
			downloadItem.setFilmName(name);
			downloadItem.setSearchPhrase("");
			downloadItem.setSize(length);

			jobNumber ++;
			DownloadJob downloadJob = new DownloadJob(downloadItem.getUrl().toString(), "Job-" + jobNumber, downloadItem.getName(), template);
			downloadJob.setFileName(name + "." + Utils.getFileExtension(url.toString()));
			downloadJob.setFolder(downloadProperties.getDownloadPath());
			
			downloadService.doWork(downloadJob);
			
			model.addAttribute(Constants.MOVIEDB, MovieDb.getInstance());
			model.addAttribute(Constants.SELECTEDGROUP, new M3UGroupSelected());

		} catch (IOException ioe) {
			log.info(ioe.getMessage());
		} catch (Exception e) {
			log.info(e.getMessage());
		}

		return Constants.STATUS;
	}

	@GetMapping(value = "interrupt", params = { "name" }) 
	public String interruptJob(Model model, @RequestParam String name) {
	  
		downloadService.interruptJob(model, name);
	  return Constants.STATUS; 
	}
}
