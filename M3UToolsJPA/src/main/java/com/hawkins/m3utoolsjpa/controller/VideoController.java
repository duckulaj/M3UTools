package com.hawkins.m3utoolsjpa.controller;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.ModelAndView;

import com.hawkins.m3utoolsjpa.data.M3UItemRepository;
import com.hawkins.m3utoolsjpa.regex.Patterns;
import com.hawkins.m3utoolsjpa.service.StreamingService;
import com.hawkins.m3utoolsjpa.utils.NetUtils;
import com.hawkins.m3utoolsjpa.utils.Utils;

@Controller
public class VideoController {

	@Autowired
	M3UItemRepository m3uItemRepository;
	
	@Autowired
	StreamingService service;

	@GetMapping(value ="stream", params = { "name" })
	public ModelAndView stream(ModelMap model, @RequestParam String name) {

		URL url = null;

		try {
			url = new URI(Utils.getURLFromName(name, m3uItemRepository)).toURL();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		model.addAttribute("streamUrl", url);
		model.addAttribute("filmTitle", Utils.removeFromString(name, Patterns.STRIP_COUNTRY_IDENTIFIER));

		return new ModelAndView("stream", model);


	}


	@GetMapping(value = "media")
	@ResponseBody
	public final ResponseEntity<InputStreamResource> retrieveResource(@RequestParam String streamUrl, @RequestHeader HttpHeaders headers) throws Exception {

		// HttpHeaders headers = new HttpHeaders();
		// InputStream targetStream = service.getInputStream(streamUrl, headers);
		
		NetUtils.printHeaders(headers);
		
		HttpURLConnection con = (HttpURLConnection) new URI(streamUrl).toURL().openConnection(); 
	    InputStream targetStream = con.getInputStream();

	    headers.setContentType(MediaType.valueOf("video/mp4"));
	    headers.set("Accept-Ranges", "bytes");
	    headers.set("Expires", "0");
	    headers.set("Cache-Control", "no-cache, no-store");
	    headers.set("Connection", "keep-alive");
	    headers.set("Content-Transfer-Encoding", "binary");
	    // headers.setContentLength(NetUtils.getContentSizeFromUrl(streamUrl));
	    // headers.set("range", "bytes=0-100000");

	    return new ResponseEntity<>(new InputStreamResource(targetStream), headers, HttpStatus.OK);

	}
	
	@GetMapping(value = "restTemplateMedia")
	@ResponseBody
	public final ResponseEntity<Resource> restTemplateMedia(@RequestParam String streamUrl, @RequestHeader HttpHeaders headers) {
		
		// request url
		// String url = "https://jsonplaceholder.typicode.com/posts/{id}";

		// create an instance of RestTemplate
		RestTemplate restTemplate = new RestTemplate();

		// create headers
		// HttpHeaders headers = new HttpHeaders();

		// set `Content-Type` and `Accept` headers
		headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
		headers.setAccept(Collections.singletonList(MediaType.APPLICATION_OCTET_STREAM));

		// example of custom header
		headers.set("X-Request-Source", "Desktop");

		// build the request
		HttpEntity request = new HttpEntity(headers);

		// make an HTTP GET request with headers
		/* ResponseEntity<Byte> response = restTemplate.exchange(
		        streamUrl,
		        HttpMethod.GET,
		        request,
		        Byte.class,
		        1
		);
		
		// check response
		if (response.getStatusCode() == HttpStatus.OK) {
		    System.out.println("Request Successful.");

		} else {
		    System.out.println("Request Failed");
		    System.out.println(response.getStatusCode());
		}
		*/
		ResponseExtractor<ResponseEntity<byte[]>> responseExtractor = restTemplate.responseEntityExtractor(byte[].class);
		ResponseEntity<byte[]> results = restTemplate.execute(streamUrl, HttpMethod.GET, null, responseExtractor);

		ByteArrayResource byteArrayResource = new ByteArrayResource(results.getBody());
		return new ResponseEntity<Resource>(byteArrayResource, null, HttpStatus.OK);
	}


}