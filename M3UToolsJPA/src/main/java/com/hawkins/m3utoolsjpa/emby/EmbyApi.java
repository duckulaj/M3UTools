package com.hawkins.m3utoolsjpa.emby;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
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
public class EmbyApi {



	public static void refreshGuide() {

		DownloadProperties downloadProperties = DownloadProperties.getInstance();



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
			refreshEmbyGuide(refreshGuideUrl);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}



	public static void refreshLibraries() {

		DownloadProperties downloadProperties = DownloadProperties.getInstance();

		String embyApi = downloadProperties.getEmbyApi();
		String embyUrl = downloadProperties.getEmbyUrl();

		String refreshLibraryUrl = embyUrl + "Library/Refresh/?api_key=" + embyApi;

		RestTemplate restTemplate = new RestTemplate();

		try {
			URI uri = new URI(refreshLibraryUrl);

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

	private static void refreshEmbyGuide(String apiUrl) throws IOException {
		// Create a URL object from the API URL

		URL url = null;
		try {
			url = new URI(apiUrl).toURL();
		} catch (MalformedURLException | URISyntaxException e) {
			log.error("Failed to create URL object from API URL: " + apiUrl);
		}
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();

		// Set the request method to POST or GET depending on the API behaviour (GET is generally fine for this request)
		connection.setRequestMethod("POST");

		// Connect to the server
		connection.connect();

		// Get the response code (to check if the request was successful)
		int responseCode = connection.getResponseCode();
		if (responseCode == HttpURLConnection.HTTP_NO_CONTENT) {
			log.info("Live TV guide refresh triggered successfully (HTTP 204 - No Content).");
		} else {
			log.info("Failed to refresh Live TV guide. HTTP Code: " + responseCode);
		}

		// Disconnect when done
		connection.disconnect();
	}
}
