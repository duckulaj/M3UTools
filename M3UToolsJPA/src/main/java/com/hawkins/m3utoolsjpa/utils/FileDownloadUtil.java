
package com.hawkins.m3utoolsjpa.utils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URL;

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
        try (var in = new BufferedInputStream(file.openStream());
             var out = response.getOutputStream()) {

            if (mimeType == null) {
                mimeType = MimeTypeUtils.APPLICATION_OCTET_STREAM.getType();
            }

            response.setContentType(mimeType);
            response.setContentLength(contentSize.intValue());
            response.setHeader("Content-Disposition", "attachment; filename=" + originalFileName);

            in.transferTo(out);
            out.flush();
        }
    }
}
