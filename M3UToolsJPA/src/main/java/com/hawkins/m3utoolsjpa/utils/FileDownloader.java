package com.hawkins.m3utoolsjpa.utils;

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

import org.springframework.util.StopWatch;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FileDownloader {

    public static void downloadFileInSegments(String fileUrl, String outputFilePath, int segmentSize) throws IOException, InterruptedException {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start("downloadFileInSegments");

        // Connect to the URL and get file info (like file size)
        URL url = null;
        try {
            url = new URI(fileUrl).toURL();
        } catch (MalformedURLException | URISyntaxException e) {
            log.info("Error in downloadFileInSegments: " + e.getMessage());
        }
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("HEAD");
        connection.connect();

        int contentLength = connection.getContentLength();  // Total file size
        connection.disconnect();

        if (contentLength == -1) {
            throw new IOException("Could not retrieve file size");
        }

        // Open RandomAccessFile to write the downloaded file in segments
        try (RandomAccessFile outputFile = new RandomAccessFile(outputFilePath, "rw")) {
            outputFile.setLength(contentLength);  // Set the length of the output file

            // Create a thread pool
            ExecutorService executor = Executors.newFixedThreadPool(10);

            // Download file in segments
            for (int i = 0; i < contentLength; i += segmentSize) {
                int rangeStart = i;
                int rangeEnd = Math.min(i + segmentSize - 1, contentLength - 1);
                executor.submit(() -> {
                    try {
                        downloadSegment(fileUrl, outputFile, rangeStart, rangeEnd);
                    } catch (IOException e) {
                        log.info("Error in downloadSegment: " + e.getMessage());
                    }
                });
            }

            // Shutdown the executor and wait for all tasks to complete
            executor.shutdown();
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        }

        stopWatch.stop();
        log.info("Time taken for downloadFileInSegments: {} ms", stopWatch.getTotalTimeMillis());
    }

    // Download a specific segment of the file
    public static void downloadSegment(String fileUrl, RandomAccessFile outputFile, int rangeStart, int rangeEnd) throws IOException {
        // Connect to the URL
        URL url = null;
        try {
            url = new URI(fileUrl).toURL();
        } catch (MalformedURLException | URISyntaxException e) {
            log.info("Error in downloadSegment: " + e.getMessage());
        }
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Range", "bytes=" + rangeStart + "-" + rangeEnd);

        // Get the input stream of the response
        try (InputStream inputStream = connection.getInputStream()) {
            outputFile.seek(rangeStart);  // Move to the correct position in the file

            byte[] buffer = new byte[8192];  // Use a larger buffer size
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputFile.write(buffer, 0, bytesRead);
            }
        }

        connection.disconnect();
    }
}
