
package com.hawkins.m3utoolsjpa.utils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NetUtils {
    public static byte[] getBytes(String str) {
        return str.getBytes();
    }

    public static final String readLine(InputStream in) throws IOException {
        StringBuffer buf = new StringBuffer();
        while (true) {
            int x = in.read();
            if (x == -1)
                throw new IOException("Unexpected EOF while reading header line");
            if (x == '\n')
                return buf.toString();
            if (x != '\r')
                buf.append((char) x);
        }
    }

    public static final int getResponseCode(String statusLine) {
        String arr[] = statusLine.split(" ");
        if (arr.length < 2)
            return 400;
        return Integer.parseInt(arr[1]);
    }

    public static void skipRemainingStream(InputStream inStream, long length) throws IOException {
        byte buf[] = new byte[8192];
        if (length > 0) {
            while (length > 0) {
                int r = (int) (length > buf.length ? buf.length : length);
                int x = inStream.read(buf, 0, r);
                if (x == -1)
                    break;
                length -= x;
            }
        } else {
            while (true) {
                int x = inStream.read(buf);
                if (x == -1)
                    break;
            }
        }
    }

    public static String getNameFromContentDisposition(String header) {
        try {
            if (header == null)
                return null;
            header = header.toLowerCase();
            if (header.startsWith("attachment")) {
                String arr[] = header.split(";");
                for (String str : arr) {
                    str = str.trim();
                    if (str.toLowerCase().startsWith("filename")) {
                        int index = str.indexOf('=');
                        return str.substring(index + 1).replace("\"", "").trim();
                    }
                }
            }
        } catch (Exception e) {
            log.debug("Error parsing content disposition header: {}", e.getMessage());
        }
        return null;
    }

    public static String getCleanContentType(String contentType) {
        if (contentType == null || contentType.length() < 1)
            return contentType;
        int index = contentType.indexOf(";");
        if (index > 0) {
            contentType = contentType.substring(0, index).trim().toLowerCase();
        }
        return contentType;
    }

    public static String getContentTypeFromUrl(String streamUrl) {
        try {
            URL url = new URI(streamUrl).toURL();
            return getContentTypeFromUrl(url);
        } catch (MalformedURLException | URISyntaxException e) {
            log.debug("Invalid URL: {}", e.getMessage());
            return null;
        }
    }

    public static String getContentTypeFromUrl(URL url) {
        if (url == null) {
            return null;
        }
        try (InputStream input = new BufferedInputStream(url.openStream())) {
            String contentType = URLConnection.guessContentTypeFromStream(input);
            return contentType != null ? contentType : MediaType.APPLICATION_OCTET_STREAM_VALUE;
        } catch (IOException e) {
            log.debug("Failed to identify content type from URL: {}", e.getMessage());
            return MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }
    }

    public static Long getContentSizeFromUrl(String streamUrl) {
        try {
            URL url = new URI(streamUrl).toURL();
            return getContentSizeFromUrl(url);
        } catch (MalformedURLException | URISyntaxException e) {
            log.debug("Invalid URL: {}", e.getMessage());
            return 0L;
		} catch (IllegalArgumentException e) {
			log.debug("URI is not absolute: {}", e.getMessage());
			return 0L;
		}
    }

    public static Long getContentSizeFromUrl(URL url) {
        if (url == null) {
            return 0L;
        }
        try {
            URLConnection connection = url.openConnection();
            return connection.getContentLengthLong();
        } catch (IOException e) {
            log.debug("Failed to identify content size from URL: {}", e.getMessage());
            return 0L;
        }
    }

    public static void printHeaders(HttpHeaders headers) {
        headers.forEach((key, value) -> {
            log.info(String.format("Header '%s' = %s", key, value));
        });
    }
}
