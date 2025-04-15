
package com.hawkins.m3utoolsjpa.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Service;

import com.hawkins.m3utoolsjpa.annotations.TrackExecutionTime;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class FileDownloaderService {

    @TrackExecutionTime
    public void downloadFileInSegments(String fileUrl, String outputFilePath, int segmentSize) throws IOException, InterruptedException {
        URL url;
        try {
            url = new URI(fileUrl).toURL();
        } catch (MalformedURLException | URISyntaxException e) {
            log.error("Invalid URL: " + e.getMessage());
            throw new IOException("Invalid URL", e);
        }

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("HEAD");
        connection.connect();

        int contentLength = connection.getContentLength();
        connection.disconnect();

        if (contentLength == -1) {
            throw new IOException("Could not retrieve file size");
        }

        try (RandomAccessFile outputFile = new RandomAccessFile(outputFilePath, "rw")) {
            outputFile.setLength(contentLength);

            int threadCount = Math.min(10, (contentLength + segmentSize - 1) / segmentSize);
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);

            for (int i = 0; i < contentLength; i += segmentSize) {
                int rangeStart = i;
                int rangeEnd = Math.min(i + segmentSize - 1, contentLength - 1);
                executor.submit(() -> {
                    try {
                        downloadSegment(fileUrl, outputFile, rangeStart, rangeEnd);
                    } catch (IOException e) {
                        log.error("Error in downloadSegment: " + e.getMessage());
                    }
                });
            }

            executor.shutdown();
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        }
    }

    private static void downloadSegment(String fileUrl, RandomAccessFile outputFile, int rangeStart, int rangeEnd) throws IOException {
        URL url;
        try {
            url = new URI(fileUrl).toURL();
        } catch (MalformedURLException | URISyntaxException e) {
            log.error("Invalid URL: " + e.getMessage());
            throw new IOException("Invalid URL", e);
        }

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Range", "bytes=" + rangeStart + "-" + rangeEnd);

        try (InputStream inputStream = connection.getInputStream()) {
            outputFile.seek(rangeStart);

            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputFile.write(buffer, 0, bytesRead);
            }
        } finally {
            connection.disconnect();
        }
    }
}
