package com.hawkins.m3utoolsjpa.controller;

import java.net.URI;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.hawkins.m3utoolsjpa.data.M3UItemRepository;
import com.hawkins.m3utoolsjpa.utils.Utils;

import jakarta.servlet.http.HttpServletRequest;

@RestController
public class StreamingController {

	@Autowired
	M3UItemRepository m3uItemRepository;
	
	private final RestTemplate restTemplate;

	public StreamingController(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}

	@GetMapping("streamVideo")
	public ResponseEntity<Resource> streamVideo(@RequestParam String streamName,
			@RequestHeader(value = "Range", required = false) String rangeHeader) {
		try {
			String videoUrl = Utils.getURLFromName(streamName, m3uItemRepository);

			HttpHeaders headers = new HttpHeaders();
			if (rangeHeader != null && !rangeHeader.isEmpty()) {
				headers.set("Range", rangeHeader);
			}

			HttpEntity<Void> httpEntity = new HttpEntity<>(headers);

			ResponseEntity<Resource> responseEntity = restTemplate.exchange(
					URI.create(videoUrl), HttpMethod.GET, httpEntity, Resource.class);

			HttpHeaders responseHeaders = new HttpHeaders();
			responseHeaders.setContentType(MediaType.valueOf("video/mp4"));
			responseHeaders.setContentLength(responseEntity.getHeaders().getContentLength());

			return new ResponseEntity<>(responseEntity.getBody(), responseHeaders, HttpStatus.PARTIAL_CONTENT);
		} catch (HttpClientErrorException.NotFound e) {
			return ResponseEntity.notFound().build();
		}
	}

	private String getRangeHeader(HttpServletRequest request) {
		String rangeHeader = request.getHeader(HttpHeaders.RANGE);
		return rangeHeader != null ? rangeHeader.replace("bytes=", "") : "";
	}
}
