package com.hawkins.m3utoolsjpa.search;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.hawkins.m3utoolsjpa.data.M3UItem;
import com.hawkins.m3utoolsjpa.data.M3UItemRepository;
import com.hawkins.m3utoolsjpa.utils.Constants;
import com.hawkins.m3utoolsjpa.utils.Utils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SearchByGenre implements Search {@Override
	
	public List<M3UItem> search(String genre, M3UItemRepository itemRepository) {
		
	MovieDb movieDb = MovieDb.getInstance();
	String searchDiscoverURL = movieDb.getDiscoverURL();
	String api = movieDb.getApi();
	JsonObject obj = new JsonObject();

	try {

		Map<String, String> parameters = new HashMap<>();
		parameters.put("api_key", api);
		parameters.put("with_genres", genre);

		URL url = new URI(searchDiscoverURL + "?" + Utils.getParamsString(parameters)).toURL();
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("GET");
		con.setRequestProperty("Content-Type", "application/json");

		JsonObject jsonObject = (JsonObject)JsonParser.parseReader(
				new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8));

		obj = jsonObject;

	} catch (Exception e) {
		log.info(e.getMessage());
	}

	return JsonToList.convertJsonToList(obj, itemRepository, Constants.GENRE_SEARCH);

	}
	

}
