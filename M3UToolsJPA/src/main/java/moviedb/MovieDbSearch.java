package moviedb;

import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.hawkins.m3utoolsjpa.data.M3UItem;
import com.hawkins.m3utoolsjpa.data.M3UItemRepository;
import com.hawkins.m3utoolsjpa.utils.MovieDb;
import com.hawkins.m3utoolsjpa.utils.Utils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MovieDbSearch {

	@Autowired
	M3UItemRepository itemRepository;

	public static JsonObject searchMovieDbByActor(String filter) {

		MovieDb movieDb = MovieDb.getInstance();
		String searchPersonURL = movieDb.getPersonURL();
		String api = movieDb.getApi();
		JsonObject obj = new JsonObject();

		try {

			Map<String, String> parameters = new HashMap<>();
			parameters.put("api_key", api);
			parameters.put("query", filter);

			URL url = new URL(searchPersonURL + "?" + getParamsString(parameters));
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

			URL url = new URL(discoverURL + "?" + getParamsString(parameters));
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

	private static String getParamsString(Map<String, String> params) 
			throws UnsupportedEncodingException{
		StringBuilder result = new StringBuilder();

		for (Map.Entry<String, String> entry : params.entrySet()) {
			result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
			result.append("=");
			result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
			result.append("&");
		}

		String resultString = result.toString();
		return resultString.length() > 0
				? resultString.substring(0, resultString.length() - 1)
						: resultString;
	}

	public List<M3UItem> searchplayList(String filter) {

		List<M3UItem> foundItems = itemRepository.findByTvgName(filter);

		return foundItems;
	}

	public LinkedList<M3UItem> searchplayListByActor(String filter) {

		JsonObject obj = searchMovieDbByActor(filter);
		JsonArray actors = (JsonArray) obj.get("results"); 

		if (log.isDebugEnabled()) {
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			log.debug(gson.toJson(actors));
		}

		Iterator<JsonElement> actorsIt = actors.iterator();

		List<String> movies = new ArrayList<>();
		while (actorsIt.hasNext()) {
			JsonObject actor = actorsIt.next().getAsJsonObject();

			JsonArray knownfor = (JsonArray) actor.get("known_for");

			if (log.isDebugEnabled()) {
				Gson gson = new GsonBuilder().setPrettyPrinting().create();
				log.debug(gson.toJson(knownfor));
			}

			Iterator<JsonElement> knownforIt = knownfor.iterator();

			while (knownforIt.hasNext()) {
				JsonObject movie = knownforIt.next().getAsJsonObject();

				String mediatype = movie.get("media_type").getAsString();

				if (mediatype.equalsIgnoreCase("tv")) {
					movies.add(movie.get("name").getAsString());
				} else if (mediatype.equalsIgnoreCase("movie")) {
					movies.add(movie.get("title").getAsString());
				}

			}


		}

		LinkedList<M3UItem> foundItems = new LinkedList<M3UItem>();

		if (movies.size() > 0) {

			for (String movie : movies) {
				List<M3UItem> items = itemRepository.findByTvgName(movie);
				if (items.size() > 0) {
					for (M3UItem item : items) {
						foundItems.add(item);
					}
				}
			}
		}

		return foundItems;
	}

	public LinkedList<M3UItem> searchplayListByYear(String filter) {

		JsonObject obj = searchMovieDbByYear(filter);
		JsonArray moviesForYear = (JsonArray) obj.get("results"); 

		if (log.isDebugEnabled()) {
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			log.debug(gson.toJson(moviesForYear));
		}

		Iterator<JsonElement> moviesIt = moviesForYear.iterator();

		LinkedList<String> movies = new LinkedList<String>();
		while (moviesIt.hasNext()) {
			JsonObject movie = moviesIt.next().getAsJsonObject();

			movies.add(movie.get("title").getAsString());
		}

		LinkedList<M3UItem> foundItems = new LinkedList<M3UItem>();

		if (movies.size() > 0) {

			for (String movie : movies) {
				List<M3UItem> items = itemRepository.findByTvgName(movie);
				if (items.size() > 0) {
					for (M3UItem item : items) {
						foundItems.add(item);
					}
				}
			}
		}
		return foundItems;
	}

}
