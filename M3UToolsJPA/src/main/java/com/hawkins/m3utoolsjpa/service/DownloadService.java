package com.hawkins.m3utoolsjpa.service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

import org.springframework.stereotype.Service;

import com.google.api.client.googleapis.media.MediaHttpDownloader;
import com.google.api.client.googleapis.media.MediaHttpDownloaderProgressListener;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.hawkins.m3utoolsjpa.data.M3UItemRepository;
import com.hawkins.m3utoolsjpa.utils.Utils;

import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class DownloadService {

    private final M3UItemRepository itemRepository;

    public DownloadService(M3UItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    public void downloadToServer(String name) {
        String url = null;
        try {
            url = Utils.getURLFromName(name, itemRepository);
            log.info("Starting download for: {}", name);

            HttpTransport httpTransport = new NetHttpTransport();
            HttpRequestInitializer httpInitializer = null;

            MediaHttpDownloader downloader = new MediaHttpDownloader(httpTransport, httpInitializer);
            downloader.setChunkSize(MediaHttpDownloader.MAXIMUM_CHUNK_SIZE);

            String fileType = url.substring(url.lastIndexOf("."));
            try (FileOutputStream fileOutputStream = new FileOutputStream(name + fileType)) {
                downloader.setProgressListener(new MediaHttpDownloaderProgressListener() {
                    @Override
                    public void progressChanged(MediaHttpDownloader downloader) throws IOException {
                        switch (downloader.getDownloadState()) {
                            case MEDIA_IN_PROGRESS:
                                log.info("Download in progress: {}% ({} bytes)", 
                                    downloader.getProgress() * 100, downloader.getNumBytesDownloaded());
                                break;
                            case MEDIA_COMPLETE:
                                log.info("Download completed for: {}", name);
                                break;
                            case NOT_STARTED:
                                log.info("Download not started for: {}", name);
                                break;
                        }
                    }
                });

                downloader.download(new GenericUrl(url), fileOutputStream);
                log.info("File downloaded successfully: {}", name + fileType);
            }
        } catch (IOException e) {
            log.error("Error occurred while downloading {}: {}", name, e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error occurred for {}: {}", name, e.getMessage(), e);
        }
    }
    
    public void downloadToClient(String name, HttpServletResponse response) {
        String fileUrl = null;
        try {
            // Retrieve the file URL from the repository
            fileUrl = Utils.getURLFromName(name, itemRepository);
            log.info("Preparing to download file: {}", name);

            // Open a connection to the file URL
            URL url = new URI(fileUrl).toURL();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            // Get the file type and size
            String fileType = connection.getContentType();
            int contentLength = connection.getContentLength();

            // Set response headers
            response.setContentType(fileType);
            response.setHeader("Content-Disposition", "attachment; filename=\"" + name + "\"");
            response.setContentLength(contentLength);

            // Stream the file content to the client
            try (InputStream inputStream = connection.getInputStream()) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    response.getOutputStream().write(buffer, 0, bytesRead);
                }
                response.getOutputStream().flush();
            }

            log.info("File successfully downloaded: {}", name);
        } catch (IOException e) {
            log.error("Error occurred while downloading file {}: {}", name, e.getMessage(), e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            log.error("Unexpected error occurred for file {}: {}", name, e.getMessage(), e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

}
