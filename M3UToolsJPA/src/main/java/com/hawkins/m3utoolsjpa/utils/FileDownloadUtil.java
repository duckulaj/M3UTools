package com.hawkins.m3utoolsjpa.utils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.springframework.util.MimeTypeUtils;

import jakarta.servlet.http.HttpServletResponse;

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
        try (var in = new BufferedInputStream(file.openStream())) {

            // get MIME type of the file

            if (mimeType == null) {
                // set to binary type if MIME mapping not found
                mimeType = MimeTypeUtils.APPLICATION_OCTET_STREAM.getType();
            }

            // set content attributes for the response
            response.setContentType(mimeType);
            response.setContentLength(contentSize.intValue());

            // This will download the file to the user's computer
            response.setHeader("Content-Disposition", "attachment; filename=" + originalFileName);

            in.transferTo(response.getOutputStream());
            IOUtils.copyLarge(in, response.getOutputStream());

            response.getOutputStream().flush();
        }
    }

}