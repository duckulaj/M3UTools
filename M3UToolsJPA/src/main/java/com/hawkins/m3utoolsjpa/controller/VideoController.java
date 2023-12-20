package com.hawkins.m3utoolsjpa.controller;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.hawkins.m3utoolsjpa.data.M3UItemRepository;
import com.hawkins.m3utoolsjpa.regex.Patterns;
import com.hawkins.m3utoolsjpa.service.StreamingService;
import com.hawkins.m3utoolsjpa.utils.NetUtils;
import com.hawkins.m3utoolsjpa.utils.Utils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
public class VideoController {

	@Autowired
	M3UItemRepository m3uItemRepository;

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
	public final ResponseEntity<InputStreamResource> retrieveResource(@RequestParam String streamUrl) throws Exception {

		HttpURLConnection con = (HttpURLConnection) new URI(streamUrl).toURL().openConnection(); 
	    InputStream targetStream = con.getInputStream();

	    HttpHeaders headers = new HttpHeaders();
	    headers.setContentType(MediaType.valueOf("video/mp4"));
	    headers.set("Accept-Ranges", "bytes");
	    headers.set("Expires", "0");
	    headers.set("Cache-Control", "no-cache, no-store");
	    headers.set("Connection", "keep-alive");
	    headers.set("Content-Transfer-Encoding", "binary");

	    return new ResponseEntity<>(new InputStreamResource(targetStream), headers, HttpStatus.OK);

	}


}