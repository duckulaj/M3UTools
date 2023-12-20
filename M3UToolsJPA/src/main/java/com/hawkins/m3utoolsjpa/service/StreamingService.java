package com.hawkins.m3utoolsjpa.service;

import java.io.IOException;
import java.net.MalformedURLException;

import org.springframework.core.io.UrlResource;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRange;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.hawkins.m3utoolsjpa.utils.NetUtils;

@Service
public class StreamingService {

	private static final long ChunkSize = 1000000L;

	public ResourceRegion streamVideo(String streamUrl, HttpHeaders headers) {

		UrlResource video = null;
		try {
			video = new UrlResource("url:" + streamUrl);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ResourceRegion region = null;
		try {
			region = resourceRegion(video, headers);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return region;
		
	}

	private ResourceRegion resourceRegion(UrlResource video, HttpHeaders headers) throws IOException {
		long contentLength = NetUtils.getContentSizeFromUrl(video.getURL());
		HttpRange range = headers.getRange().stream().findFirst().orElse(null);
		if (range != null) {
			long start = range.getRangeStart(contentLength);
			long end = range.getRangeEnd(contentLength);
			long rangeLength = Math.min(ChunkSize, end - start + 1);
			return new ResourceRegion(video, start, rangeLength);
		} else {
			long rangeLength = Math.min(ChunkSize, contentLength);
			return new ResourceRegion(video, 0, rangeLength);
		}
	}
}