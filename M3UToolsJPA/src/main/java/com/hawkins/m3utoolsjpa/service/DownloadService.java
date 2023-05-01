package com.hawkins.m3utoolsjpa.service;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestParam;

import com.hawkins.dmanager.DManagerApp;
import com.hawkins.m3utoolsjpa.data.M3UItemRepository;
import com.hawkins.m3utoolsjpa.m3u.M3UDownloadItem;
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
	private SimpMessagingTemplate template;


	@Autowired
	public DownloadService() {
		this.fileStorageLocation = Paths.get(DownloadProperties.getInstance().getDownloadPath()).toAbsolutePath().normalize();

		try {
			Files.createDirectories(this.fileStorageLocation);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public boolean doWork(Runnable startDownload) {

		log.info("Got runnable {}", startDownload);


		startDownload.run();

		return true;

	}
	public String interruptJob(Model model, @RequestParam String name) {

		DManagerApp.getInstance().pauseDownload(name); 
		DownloadJob job = Utils.findJobByName(new LinkedList<DownloadJob>(), name);

		if (job != null) job.stop();

		return Constants.STATUS; 
	}

	public Resource loadFileAsResource(String fileName) {
		try {

			Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
			Resource resource = new UrlResource(filePath.toUri());

			if(resource.exists()) {
				return resource;
			} else {

				log.info("{} not found", fileName);
			}
		} catch (MalformedURLException ex) {
			ex.printStackTrace();
		}

		return null;
	}

	public DownloadJob createDownloadJob(String name) {

		DownloadProperties	downloadProperties = DownloadProperties.getInstance();


		if (log.isDebugEnabled()) {
			log.debug("download {}", name);
		}

		try {
			URL url = Utils.getFinalLocation(Utils.getURLFromName(name, itemRepository));

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

			name = name.substring(name.lastIndexOf("|") + 1).trim();
			M3UDownloadItem downloadItem = new M3UDownloadItem();
			downloadItem.setUrl(url);
			downloadItem.setName(
					downloadProperties.getDownloadPath() + name + "." + Utils.getFileExtension(url.toString()));
			downloadItem.setFilmName(name);
			downloadItem.setSearchPhrase("");
			downloadItem.setSize(length);

			DownloadJob downloadJob = new DownloadJob(downloadItem.getUrl().toString(), "Job-" + name, downloadItem.getName(), template);

			downloadJob.setFileName(name + "." + Utils.getFileExtension(url.toString()));
			downloadJob.setFolder(downloadProperties.getDownloadPath());
			
			return downloadJob;
			
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
}
