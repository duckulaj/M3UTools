package com.hawkins.m3utoolsjpa.test.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;


public class UrlRangeTest {

	@Disabled
	@Test
	void testRanges() {

		String url = "http://dome13667.cdngold.me:80/movie/ba201db620/21ffd018d2/183690.mp4";

		RestTemplate httpClient = new RestTemplate();

		{
			HttpHeaders requestHeaders = new HttpHeaders();

			MultiValueMap<String, String> postParameters = new LinkedMultiValueMap<String, String>();

			HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<MultiValueMap<String, String>>(
					postParameters, requestHeaders);

			ResponseEntity<byte[]> response = httpClient.exchange(url, HttpMethod.GET, requestEntity, byte[].class);

			System.out.println(response);

			assertTrue(response.getHeaders().containsKey("Accept-ranges"), "The server doesn't accept ranges");
			// assertTrue(response.getHeaders().get("Accept-ranges").contains("bytes"), "The server doesn't accept ranges with bytes");
		}

		long fileLength = 0;

		{
			// Range: bytes=0-

			HttpHeaders requestHeaders = new HttpHeaders();
			requestHeaders.set("Range", "bytes=0-10000");

			MultiValueMap<String, String> postParameters = new LinkedMultiValueMap<String, String>();

			HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<MultiValueMap<String, String>>(
					postParameters, requestHeaders);

			ResponseEntity<byte[]> response = httpClient.exchange(url, HttpMethod.GET, requestEntity, byte[].class);

			System.out.println(response);

			assertEquals(response.getStatusCode(), HttpStatus.PARTIAL_CONTENT, "The server doesn't respond with http status code 206 to a request with ranges");

			fileLength = Long.parseLong(response.getHeaders().get("Content-Length").get(0));




			// HttpHeaders requestHeaders = new HttpHeaders();

			long firstByte = fileLength - 3000;
			long lastByte = fileLength - 1;
			long numBytes = lastByte - firstByte + 1;

			requestHeaders.set("Range", "bytes=" + firstByte + "-" + lastByte);

			// MultiValueMap<String, String> postParameters = new LinkedMultiValueMap<String, String>();

			// HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<MultiValueMap<String, String>>(postParameters, requestHeaders);


			ResponseEntity<byte[]> newResponse = httpClient.exchange(url, HttpMethod.GET, requestEntity, byte[].class);

			System.out.println(newResponse);

			assertEquals(HttpStatus.PARTIAL_CONTENT, response.getStatusCode(),"The server doesn't respond with http status code 206 to a request with ranges");

			long responseContentLength = Long.parseLong(response.getHeaders().get("Content-Length").get(0));
			assertEquals(numBytes, responseContentLength, "The server doesn't send the requested bytes");

			assertEquals(responseContentLength, response.getBody().length, "The server doesn't send the requested bytes");

		}
	}}