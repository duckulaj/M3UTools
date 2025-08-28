package com.hawkins.m3utoolsjpa.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.springframework.util.StopWatch;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FileDownloader {
    private static final int DEFAULT_BUFFER_SIZE = 16384;
    private static final int DEFAULT_THREAD_POOL_SIZE = 4;

    /**
     * Downloads a file in parallel segments using HTTP range requests.
     * Falls back to sequential download if content length is unknown.
     *
     * @param fileUrl        the URL of the file to download
     * @param outputFilePath the local file path to write to
     * @param segmentSize    the size of each segment in bytes
     * @throws IOException              if an I/O error occurs
     * @throws InterruptedException     if interrupted while waiting for threads
     */
    public static void downloadFileInSegments(String fileUrl, String outputFilePath, int segmentSize) throws IOException, InterruptedException {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start("downloadFileInSegments");

        URL url;
        try {
            url = new URI(fileUrl).toURL();
        } catch (MalformedURLException | URISyntaxException e) {
            log.error("Error in downloadFileInSegments: {}", e.getMessage(), e);
            throw new IOException("Invalid URL: " + fileUrl, e);
        }

        int contentLength;
        HttpURLConnection connection = null;
        try  {
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("HEAD");
            connection.setRequestProperty("User-Agent", "M3UToolsJPA-Downloader/1.0");
            connection.connect();
            contentLength = connection.getContentLength();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

        if (contentLength <= 0) {
            log.info("Content length could not be determined. Downloading file sequentially.");
            HttpURLConnection seqConn = (HttpURLConnection) url.openConnection();
            seqConn.setRequestProperty("User-Agent", "M3UToolsJPA-Downloader/1.0");
            try (InputStream inputStream = seqConn.getInputStream();
                 RandomAccessFile outputFile = new RandomAccessFile(outputFilePath, "rw")) {
                byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputFile.write(buffer, 0, bytesRead);
                }
            } finally {
                seqConn.disconnect();
            }
            stopWatch.stop();
            log.info("Time taken for sequential download: {} ms", stopWatch.getTotalTimeMillis());
            return;
        }

        // Ensure file exists and is empty
        Path outPath = Path.of(outputFilePath);
        Files.deleteIfExists(outPath);
        Files.createFile(outPath);

        List<Future<Boolean>> futures = new ArrayList<>();
        List<String> failedSegments = Collections.synchronizedList(new ArrayList<>());
        int threadPoolSize = Math.max(2, Math.min(DEFAULT_THREAD_POOL_SIZE, contentLength / segmentSize));
        ExecutorService executor = Executors.newFixedThreadPool(threadPoolSize);

        try (RandomAccessFile outputFile = new RandomAccessFile(outputFilePath, "rw")) {
            outputFile.setLength(contentLength);
            for (int i = 0; i < contentLength; i += segmentSize) {
                int rangeStart = i;
                int rangeEnd = Math.min(i + segmentSize - 1, contentLength - 1);
                futures.add(executor.submit(new SegmentTask(fileUrl, outputFile, rangeStart, rangeEnd, failedSegments)));
            }
            executor.shutdown();
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } finally {
            if (!executor.isShutdown()) executor.shutdownNow();
        }

        // Check for failures
        for (Future<Boolean> future : futures) {
            try {
                future.get();
            } catch (ExecutionException e) {
                log.error("Segment download failed: {}", e.getCause().getMessage(), e.getCause());
            }
        }
        if (!failedSegments.isEmpty()) {
            log.error("Failed segments: {}", failedSegments);
        }

        stopWatch.stop();
        log.info("Time taken for downloadFileInSegments: {} ms", stopWatch.getTotalTimeMillis());
    }

    /**
     * Downloads a specific segment of the file.
     *
     * @param fileUrl    the URL of the file
     * @param outputFile the RandomAccessFile to write to
     * @param rangeStart the start byte (inclusive)
     * @param rangeEnd   the end byte (inclusive)
     * @throws IOException if an I/O error occurs
     */
    public static void downloadSegment(String fileUrl, RandomAccessFile outputFile, int rangeStart, int rangeEnd) throws IOException {
        URL url;
        try {
            url = new URI(fileUrl).toURL();
        } catch (MalformedURLException | URISyntaxException e) {
            log.error("Error in downloadSegment: {}", e.getMessage(), e);
            throw new IOException("Invalid URL: " + fileUrl, e);
        }
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Range", "bytes=" + rangeStart + "-" + rangeEnd);
            connection.setRequestProperty("User-Agent", "M3UToolsJPA-Downloader/1.0");
            try (InputStream inputStream = connection.getInputStream()) {
                byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
                int bytesRead;
                synchronized (outputFile) {
                    outputFile.seek(rangeStart);
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputFile.write(buffer, 0, bytesRead);
                    }
                }
            }
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    /**
     * Callable task for downloading a file segment.
     */
    private static class SegmentTask implements Callable<Boolean> {
        private final String fileUrl;
        private final RandomAccessFile outputFile;
        private final int rangeStart;
        private final int rangeEnd;
        private final List<String> failedSegments;

        SegmentTask(String fileUrl, RandomAccessFile outputFile, int rangeStart, int rangeEnd, List<String> failedSegments) {
            this.fileUrl = fileUrl;
            this.outputFile = outputFile;
            this.rangeStart = rangeStart;
            this.rangeEnd = rangeEnd;
            this.failedSegments = failedSegments;
        }

        @Override
        public Boolean call() {
            try {
                downloadSegment(fileUrl, outputFile, rangeStart, rangeEnd);
                return true;
            } catch (IOException e) {
                failedSegments.add(rangeStart + "-" + rangeEnd);
                log.error("Failed to download segment {}-{}: {}", rangeStart, rangeEnd, e.getMessage());
                return false;
            }
        }
    }

    
}