package com.hawkins.m3utoolsjpa.utils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;

import org.apache.commons.io.IOUtils;
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
				throw new IOException(
						"Unexpected EOF while reading header line");
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

	
	
	public static void skipRemainingStream(InputStream inStream, long length)
			throws IOException {
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
				for (int i = 0; i < arr.length; i++) {
					String str = arr[i].trim();
					if (str.toLowerCase().startsWith("filename")) {
						int index = str.indexOf('=');
						return str.substring(index + 1).replace("\"", "")
								.trim();
					}
				}
			}
		} catch (Exception e) {
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
		
		URL url = null;
		try {
			url = new URI(streamUrl).toURL();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return getContentTypeFromUrl(url);
		
	}
	
	public static String getContentTypeFromUrl(URL url) {
	    if (url == null) {
	        return null;
	    }// w ww  .j  a va 2 s . c om
	    String contentType;
	    
	    try {
	        URLConnection connection = url.openConnection();
	        contentType = URLConnection.guessContentTypeFromStream(new BufferedInputStream(connection.getInputStream()));
	            if (contentType == null) {
	                contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
	            }
	       
	    } catch (IOException e) {
	        log.debug("Failed to identify content type from URL: {}", e.getMessage());
	        contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
	    } finally {
	    
	    }
	    return contentType;
	}
	
	public static Long getContentSizeFromUrl(String streamUrl) {
		
		URL url = null;
		try {
			url = new URI(streamUrl).toURL();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return getContentSizeFromUrl(url);
	}
	
	public static Long getContentSizeFromUrl(URL url) {
	    if (url == null) {
	        log.warn("URL is null. Cannot determine content size.");
	        return null;
	    }

	    Long contentSize = null;
	    
	    try {
	        URLConnection connection = url.openConnection();
	        
	        // Get content length (some servers may not provide this)
	        contentSize = connection.getContentLengthLong();
	        
	        if (contentSize == -1) {
	            log.warn("Content length is unavailable for URL: {}", url);
	            return null;  // If the content length is unavailable
	        }

	        log.debug("Content size from headers: {}", contentSize);
	    } catch (IOException e) {
	        log.error("Failed to retrieve content size for URL: {}. Error: {}", url, e.getMessage(), e);
	        return null;  // Return null when there is an error
	    }

	    return contentSize;
	}


	public static void printHeaders(HttpHeaders headers) {
		
		headers.forEach((key, value) -> {
	        log.info(String.format("Header '%s' = %s", key, value));
	    });
		
	}
}
