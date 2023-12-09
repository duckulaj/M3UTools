package com.hawkins.m3utoolsjpa.controller;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.ModelAndView;

import com.hawkins.m3utoolsjpa.data.M3UItemRepository;
import com.hawkins.m3utoolsjpa.regex.Patterns;
import com.hawkins.m3utoolsjpa.utils.Utils;

import jakarta.servlet.http.HttpServletResponse;
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
	
	@RequestMapping("/media")
    public void media(ModelMap model, @RequestParam("streamUrl") String streamUrl, HttpServletResponse response, @RequestHeader HttpHeaders headers) {
        log.info("Calling /media...");
        
        RestTemplate restTemplate = new RestTemplate();
            	
        headers.set("Accept-Ranges", "bytes");
    	
        try {
        restTemplate.execute(
                URI.create(streamUrl),
                HttpMethod.GET,
                (ClientHttpRequest request) -> {},
                responseExtractor -> {
                    //response.setContentType("multipart/x-mixed-replace; boundary=BoundaryString");
                    response.setContentType("video/mp4");
                    IOUtils.copy(responseExtractor.getBody(), response.getOutputStream());
                    return null;
                }
        );
        }
        catch (Exception e) {
        	log.info("An arror occured - {}", e.getMessage());
        }
    }
	
	
}