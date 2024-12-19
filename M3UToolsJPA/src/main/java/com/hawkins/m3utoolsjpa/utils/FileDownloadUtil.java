package com.hawkins.m3utoolsjpa.utils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URL;

import org.springframework.util.MimeTypeUtils;

import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class FileDownloadUtil {

    private FileDownloadUtil() {
    }

    public static void downloadFile(HttpServletResponse response, URL file, String originalFileName) throws IOException {
        handle(response, file, originalFileName, null, 0L);
    }

    public static void downloadFile(HttpServletResponse response, URL file, String originalFileName, String mimeType, Long contentSize) throws IOException {
        handle(response, file, originalFileName, mimeType, contentSize);
    }


    private static void handle(HttpServletResponse response, URL file, String originalFileName, String mimeType, Long contentSize) throws IOException {
        if (file == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "File not found or invalid URL");
            return;
        }

        // Default mime type to application/octet-stream if not provided
        if (mimeType == null || mimeType.isEmpty()) {
            mimeType = MimeTypeUtils.APPLICATION_OCTET_STREAM.getType();
        }

        // Validate content size
        if (contentSize == null || contentSize <= 0) {
            contentSize = file.openConnection().getContentLengthLong();
        }

        // Set MIME type and content length headers
        response.setContentType(mimeType);
        response.setContentLengthLong(contentSize); // Use setContentLengthLong for large files

        // Set the Content-Disposition header to indicate an attachment
        response.setHeader("Content-Disposition", "attachment; filename=\"" + originalFileName + "\"");

        // Stream the file content to the response output stream
        try (var in = new BufferedInputStream(file.openStream())) {
            // Copy the file content directly to the output stream
            in.transferTo(response.getOutputStream());

            // Ensure the output stream is flushed after transfer
            response.getOutputStream().flush();
        } catch (IOException e) {
            log.info("Error streaming file to response", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error processing file");
        }
    }


}