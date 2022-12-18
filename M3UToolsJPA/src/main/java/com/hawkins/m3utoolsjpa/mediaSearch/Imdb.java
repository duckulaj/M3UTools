package com.hawkins.m3utoolsjpa.mediaSearch;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.hawkins.m3utoolsjpa.utils.Constants;
import com.hawkins.m3utoolsjpa.utils.Utils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Imdb extends MediaSearchHelper implements Runnable {
	
	private String url = "";
	private String api = "";
	private String movieURL = "";
	private String personURL = "";
	private String discoverURL = "";
	

	public static Imdb thisInstance = null;
	
	public Imdb() {
		
		Properties props = Utils.readProperties(Constants.CONFIGPROPERTIES);
		
		this.setApi(props.getProperty("Imdb.apikey"));
		this.setUrl(props.getProperty("Imdb.searchURL"));
		this.setMovieURL(props.getProperty("Imdb.searchMovieURL"));
		this.setPersonURL(props.getProperty("Imdb.searchPersonURL"));
		this.setDiscoverURL(props.getProperty("Imdb.discoverURL"));
		
	}
	
	public static synchronized Imdb getInstance()
	{
		if (Imdb.thisInstance == null)
		{
			Imdb.thisInstance = new Imdb();
		}

		return Imdb.thisInstance;
	}
		
	@Override
	public void run() {
		throw new UnsupportedOperationException();
	}
	
	public JsonObject searchMovieDbByActor(String filter) {

		Imdb imdb = Imdb.getInstance();
		String searchPersonURL = imdb.getPersonURL();
		String api = imdb.getApi();
		JsonObject obj = new JsonObject();

		try {

			Map<String, String> parameters = new HashMap<>();
			parameters.put("api_key", api);
			parameters.put("query", filter);

			URL url = new URL(searchPersonURL + "?" + MediaSearchHelper.getParamsString(parameters));
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("GET");
			con.setRequestProperty("Content-Type", "application/json");

			JsonObject jsonObject = (JsonObject)JsonParser.parseReader(
					new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8));

			obj = jsonObject;

		} catch (Exception e) {
			log.info(e.getMessage());
		}

		return obj;
	}
	
	public static JsonObject searchMovieDbByYear(String filter) {

		Imdb imdb = Imdb.getInstance();
		String discoverURL = imdb.getDiscoverURL();
		String api = imdb.getApi();
		JsonObject obj = new JsonObject();

		try {

			Map<String, String> parameters = new HashMap<>();
			parameters.put("api_key", api);
			parameters.put("language", "en-GB");
			parameters.put("region", "GB");
			parameters.put("release_date.gte", filter + "-01-01");
			parameters.put("release_date.lte", filter + "-12-31");

			URL url = new URL(discoverURL + "?" + MediaSearchHelper.getParamsString(parameters));
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("GET");
			con.setRequestProperty("Content-Type", "application/json");

			JsonObject jsonObject = (JsonObject)JsonParser.parseReader(
					new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8));

			obj = jsonObject;

		} catch (Exception e) {
			log.info(e.getMessage());
		}

		return obj;
	}


	public String getUrl() {
		return url;
	}

	public void setUrl(String mdURL) {
		this.url = mdURL;
	}

	public String getApi() {
		return api;
	}

	public void setApi(String mdAPI) {
		this.api = mdAPI;
	}

	public String getMovieURL() {
		return movieURL;
	}

	public void setMovieURL(String movieURL) {
		this.movieURL = movieURL;
	}

	public String getPersonURL() {
		return personURL;
	}

	public void setPersonURL(String personURL) {
		this.personURL = personURL;
	}

	public String getDiscoverURL() {
		return discoverURL;
	}

	public void setDiscoverURL(String discoverURL) {
		this.discoverURL = discoverURL;
	}
	
	

	
	
	
	

}
