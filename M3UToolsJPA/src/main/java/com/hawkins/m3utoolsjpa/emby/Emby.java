package com.hawkins.m3utoolsjpa.emby;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestTemplate;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.hawkins.m3utoolsjpa.properties.DownloadProperties;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Emby {
	
	

	public static void refreshGuide() {
		
		DownloadProperties downloadProperties = DownloadProperties.getInstance();
				
		if (downloadProperties.isEmbyInstalled()) {
	
			String refreshGuideId = "";
			String embyApi = downloadProperties.getEmbyApi();
			String embyUrl = downloadProperties.getEmbyUrl();
			
			RestTemplate restTemplate = new RestTemplate();
			
			String scheduledTasksUrl = embyUrl + "ScheduledTasks?api_key=" + embyApi;
			String response = restTemplate.getForObject(scheduledTasksUrl, String.class);
						
			JsonArray scheduledTasksArray = new Gson().fromJson(response, JsonArray.class);
			
			for (Iterator<JsonElement> iterator = scheduledTasksArray.iterator(); iterator.hasNext();) {
				JsonObject type = (JsonObject) iterator.next();
				
				if (type.get("Key").getAsString().equals("RefreshGuide")) {
					refreshGuideId = type.get("Id").getAsString();
					break;
				}
			}
			
			    
		    String refreshGuideUrl = embyUrl + "ScheduledTasks/Running/" + refreshGuideId + "?api_key=" + embyApi;
			
	        try {
				URI uri = new URI(refreshGuideUrl);
				
				HttpHeaders headers = new HttpHeaders();   
		        headers.set("X-COM-LOCATION", "UK");     
		        
		        HttpEntity<String> request = new HttpEntity<>(refreshGuideId, headers);
		        
		        String result = restTemplate.postForObject(uri, request, String.class);
		        
		        log.info(result);
		        
		        		        
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
	public static void refreshLibraries() {
		
		DownloadProperties downloadProperties = DownloadProperties.getInstance();
		
		String embyApi = downloadProperties.getEmbyApi();
		String embyUrl = downloadProperties.getEmbyUrl();
		
		String refreshGuideUrl = embyUrl + "Library/Refresh/?api_key=" + embyApi;
		
		RestTemplate restTemplate = new RestTemplate();
		
        try {
			URI uri = new URI(refreshGuideUrl);
			
			HttpHeaders headers = new HttpHeaders();   
	        headers.set("X-COM-LOCATION", "UK");     
	        
	        HttpEntity<String> request = new HttpEntity<>(headers);
	        
	        String result = restTemplate.postForObject(uri, request, String.class);
	        
	        log.info(result);
	        
	        		        
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
}
