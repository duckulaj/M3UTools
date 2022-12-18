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
public class MovieDb extends MediaSearchHelper implements Runnable {
	
	private String url = "";
	private String api = "";
	private String movieURL = "";
	private String personURL = "";
	private String discoverURL = "";
	

	public static MovieDb thisInstance = null;
	
	public MovieDb() {
		
		Properties props = Utils.readProperties(Constants.CONFIGPROPERTIES);
		
		this.setApi(props.getProperty("moviedb.apikey"));
		this.setUrl(props.getProperty("moviedb.searchURL"));
		this.setMovieURL(props.getProperty("moviedb.searchMovieURL"));
		this.setPersonURL(props.getProperty("moviedb.searchPersonURL"));
		this.setDiscoverURL(props.getProperty("moviedb.discoverURL"));
		
	}
	
	public static synchronized MovieDb getInstance()
	{
		if (MovieDb.thisInstance == null)
		{
			MovieDb.thisInstance = new MovieDb();
		}

		return MovieDb.thisInstance;
	}
		
	@Override
	public void run() {
		throw new UnsupportedOperationException();
	}
	
	public JsonObject searchMovieDbByActor(String filter) {

		MovieDb movieDb = MovieDb.getInstance();
		String searchPersonURL = movieDb.getPersonURL();
		String api = movieDb.getApi();
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

		MovieDb movieDb = MovieDb.getInstance();
		String discoverURL = movieDb.getDiscoverURL();
		String api = movieDb.getApi();
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
