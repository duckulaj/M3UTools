package com.hawkins.m3utoolsjpa.controller;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
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

		return new ModelAndView("streaming", model);


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
	    headers.set("range", headers.getRange().get(0).toString());

	    return new ResponseEntity<>(new InputStreamResource(targetStream), headers, HttpStatus.OK);

	}
	
	

}