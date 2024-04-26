package com.hawkins.m3utoolsjpa.service;

import java.io.FileOutputStream;
import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.api.client.googleapis.media.MediaHttpDownloader;
import com.google.api.client.googleapis.media.MediaHttpDownloaderProgressListener;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.hawkins.m3utoolsjpa.data.M3UItemRepository;
import com.hawkins.m3utoolsjpa.utils.Utils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class DownloadService {

	@Autowired
	M3UItemRepository itemRepository;
	
	public void downloadToserver(String name) {
		
		
		try {
			// Long contentSize = 0L;
	        String url = null;
			
			url = Utils.getURLFromName(name, itemRepository);
			log.info("Downloading {}", name);
			// Long contentSize = NetUtils.getContentSizeFromUrl(url);
			
			HttpTransport httpTransport = new NetHttpTransport();
	        HttpRequestInitializer httpInitializer = null;
	        
	        MediaHttpDownloader downloader = new MediaHttpDownloader(httpTransport, httpInitializer);
	        String fileType = url.substring(url.lastIndexOf("."));
	        FileOutputStream fileOutputStream = new FileOutputStream(name + fileType);
	
	        MediaHttpDownloaderProgressListener downloadProgressListener = new MediaHttpDownloaderProgressListener() {
	
	            @Override
	            public void progressChanged(MediaHttpDownloader downloader) throws IOException {
	                switch(downloader.getDownloadState()) {
	                    case MEDIA_IN_PROGRESS:
	                        System.out.println("Download in progress");
	                        System.out.println("Download percentage: " + downloader.getProgress());
	                        System.out.println("Download percentage: " + downloader.getNumBytesDownloaded());
	                        break;
	                    //  been successfully downloaded.
	                    case MEDIA_COMPLETE:
	                        System.out.println("Download Completed!");
	                        break;
	                    //  not started yet.
	                    case NOT_STARTED:
	                        System.out.println("Download Not Started!");
	                        break;
	                }
	            }
	        };
	        downloader.setProgressListener(downloadProgressListener);
	        downloader.setChunkSize(MediaHttpDownloader.MAXIMUM_CHUNK_SIZE);
	        downloader.download(new GenericUrl(url), fileOutputStream);
	
	        fileOutputStream.close();
	        System.out.println("File downloaded successfully!");
	        
		} catch (IOException ioe) {
			log.info("Error occurred downloading {}", name);
		}
	}
}
