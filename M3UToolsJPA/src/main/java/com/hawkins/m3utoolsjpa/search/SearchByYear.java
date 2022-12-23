package com.hawkins.m3utoolsjpa.search;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.hawkins.m3utoolsjpa.data.M3UItem;
import com.hawkins.m3utoolsjpa.data.M3UItemRepository;
import com.hawkins.m3utoolsjpa.utils.Utils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SearchByYear implements Search {

	@Override
	public List<M3UItem> search(String year, M3UItemRepository itemRepository) {

		MovieDb movieDb = MovieDb.getInstance();
		String discoverURL = movieDb.getDiscoverURL();
		String api = movieDb.getApi();
		JsonObject obj = new JsonObject();

		try {

			Map<String, String> parameters = new HashMap<>();
			parameters.put("api_key", api);
			parameters.put("language", "en-GB");
			parameters.put("region", "GB");
			parameters.put("release_date.gte", year + "-01-01");
			parameters.put("release_date.lte", year + "-12-31");

			URL url = new URL(discoverURL + "?" + Utils.getParamsString(parameters));
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("GET");
			con.setRequestProperty("Content-Type", "application/json");

			JsonObject jsonObject = (JsonObject)JsonParser.parseReader(
					new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8));

			obj = jsonObject;

		} catch (Exception e) {
			log.info(e.getMessage());
		}

		return JsonToList.convertJsonToList(obj, itemRepository);

	}

}
