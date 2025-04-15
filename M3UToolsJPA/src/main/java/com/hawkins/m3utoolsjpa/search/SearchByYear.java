
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
public class SearchByYear implements Search {

    @Override
    public List<M3UItem> search(String year, M3UItemRepository itemRepository) {
        JsonObject jsonResponse = fetchYearData(year);
        return JsonToList.convertJsonToList(jsonResponse, itemRepository, Constants.YEAR_SEARCH);
    }

    private JsonObject fetchYearData(String year) {
        MovieDb movieDb = MovieDb.getInstance();
        JsonObject jsonResponse = new JsonObject();

        try {
            Map<String, String> parameters = new HashMap<>();
            parameters.put("api_key", movieDb.getApi());
            parameters.put("language", "en-GB");
            parameters.put("region", "GB");
            parameters.put("primary_release_year", year);

            URL url = new URI(movieDb.getDiscoverURL() + "?" + Utils.getParamsString(parameters)).toURL();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-Type", "application/json");

            jsonResponse = JsonParser.parseReader(
                new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8)).getAsJsonObject();

        } catch (Exception e) {
            log.error("Error fetching year data: {}", e.getMessage());
        }

        return jsonResponse;
    }
}
