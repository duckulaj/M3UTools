package com.hawkins.m3utoolsjpa.controller;

import java.net.URL;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
// import org.springframework.mobile.device.Device;
// import org.springframework.mobile.device.site.SitePreference;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.hawkins.dmanager.properties.DmProperties;
import com.hawkins.m3utoolsjpa.data.M3UItemRepository;
import com.hawkins.m3utoolsjpa.m3u.M3UGroupSelected;
import com.hawkins.m3utoolsjpa.properties.DownloadProperties;
import com.hawkins.m3utoolsjpa.search.MovieDb;
import com.hawkins.m3utoolsjpa.service.DownloadService;
import com.hawkins.m3utoolsjpa.service.M3UService;
import com.hawkins.m3utoolsjpa.utils.Constants;
import com.hawkins.m3utoolsjpa.utils.FileDownloadUtil;
import com.hawkins.m3utoolsjpa.utils.NetUtils;
import com.hawkins.m3utoolsjpa.utils.Utils;
import com.hawkins.m3uttoolsjpa.jobs.DownloadJob;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
public class DownloadController {

	
	
	@Qualifier("taskExecutor")
	@Autowired
	private ThreadPoolTaskExecutor myExecutor;

	@Autowired
	private DownloadService downloadService;

	@Autowired
	M3UService m3uService;
	
	@Autowired
	M3UItemRepository m3uItemRepository;
	
	DownloadController(DownloadService downloadService) {
		this.downloadService = downloadService;
	}
	
	DownloadProperties downloadProperties = DownloadProperties.getInstance();
	DmProperties dmProperties = DmProperties.getInstance();
	
	@PostMapping(value = "/download", params = { "name" })
	public String download(Model model, @RequestParam String name, HttpServletResponse response,
			HttpServletRequest request) {

		DownloadJob downloadJob = downloadService.createDownloadJob(name);
		
		CompletableFuture<Boolean> downloadCompleted = CompletableFuture.supplyAsync(() -> 

				downloadService.doWork(downloadJob)

					);
			
		model.addAttribute(Constants.MOVIEDB, MovieDb.getInstance());
		model.addAttribute(Constants.SELECTEDGROUP, new M3UGroupSelected());

		return Constants.STATUS;
	}

	@GetMapping(value = "interrupt", params = { "name" }) 
	public String interruptJob(Model model, @RequestParam String name) {
	  
		downloadService.interruptJob(model, name);
	  return Constants.STATUS; 
	}
	
	@GetMapping(value ="downloadLocal", params = { "name" })
    // public ResponseEntity<Resource> downloadLocal(@RequestParam String name, HttpServletRequest request, HttpServletResponse response) {
		public void downloadLocal(@RequestParam String name, HttpServletRequest request, HttpServletResponse response) {
		// Resource resource = downloadService.loadFileAsResource(name);
		
		
		String savedFileName = name;
		// remove any file extension
		name = name.substring(0, name.lastIndexOf("."));
		
		
		String contentType = null;
        Long contentSize = 0L;
        Resource resource  = null;
        
        try {
            // URL url = resource.getURL();
        	URL url = new URL(Utils.getURLFromName(name, m3uItemRepository));
        	resource = new UrlResource(url.toURI());
        	contentType = NetUtils.getContentTypeFromUrl(url);
            contentSize = NetUtils.getContentSizeFromUrl(url);
            
            FileDownloadUtil.downloadFile(response, url, savedFileName, contentType, contentSize);
        } catch (Exception ex) {
            log.info("Could not determine file type.");
        }

        // Fallback to the default content type if type could not be determined
        if(contentType == null) {
            contentType = "application/octet-stream";
        }
        
        HttpHeaders headers = new HttpHeaders();
        // headers.add(HttpHeaders.CONTENT_TYPE, contentType);
        headers.add(HttpHeaders.CONTENT_TYPE, "application/x-download");
        
        // headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"");
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + savedFileName + "\"");
        headers.add(HttpHeaders.CONTENT_LENGTH, contentSize.toString()); 
        headers.add("Pragma", "no-cache");
        headers.add("Cache-Control", "no-cache");
        // return ResponseEntity.ok().headers(headers).body(resource);

        
    }
	
	@GetMapping(value ="downloadDirect", params = { "name" })
	public String downloadDirect(@RequestParam String name, HttpServletRequest request, HttpServletResponse response) {

		String savedFileName = name;
		// remove any file extension
		// name = name.substring(0, name.lastIndexOf("."));
				
		String contentType = null;
                
        try {
           	URL url = new URL(Utils.getURLFromName(name, m3uItemRepository));
           	String fileExtension = url.toString().substring(url.toString().lastIndexOf("."));
           	savedFileName = savedFileName + fileExtension;
            contentType = NetUtils.getContentTypeFromUrl(url);
            
            FileDownloadUtil.downloadFile(response, url, savedFileName, contentType, NetUtils.getContentSizeFromUrl(url));
        } catch (Exception ex) {
            log.info("Could not determine file type.");
        }

        // Fallback to the default content type if type could not be determined
		/*
		 * if(contentType == null) { contentType = "application/octet-stream"; }
		 * 
		 * HttpHeaders headers = new HttpHeaders();
		 * headers.add(HttpHeaders.CONTENT_TYPE, "application/x-download");
		 * headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" +
		 * savedFileName + "\""); headers.add(HttpHeaders.CONTENT_LENGTH,
		 * contentSize.toString()); headers.add("Pragma", "no-cache");
		 * headers.add("Cache-Control", "no-cache");
		 */
        
        return Constants.ITEMS;
                
    }
	
	
}
