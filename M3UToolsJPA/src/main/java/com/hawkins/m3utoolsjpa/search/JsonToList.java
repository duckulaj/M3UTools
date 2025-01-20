
package com.hawkins.m3utoolsjpa.search;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.hawkins.m3utoolsjpa.data.M3UItem;
import com.hawkins.m3utoolsjpa.data.M3UItemRepository;
import com.hawkins.m3utoolsjpa.utils.Constants;
import com.hawkins.m3utoolsjpa.utils.Utils;

public class JsonToList {

    public static List<M3UItem> convertJsonToList(JsonObject jsonObj, M3UItemRepository itemRepository, String searchType) {
        List<M3UItem> foundItems = new ArrayList<>();
        LinkedList<String> m3uitems = new LinkedList<>();

        if (!jsonObj.isJsonNull()) {
            JsonArray results = jsonObj.getAsJsonArray("results");

            for (JsonElement resultElement : results) {
                JsonObject result = resultElement.getAsJsonObject();

                switch (searchType) {
                    case Constants.ACTOR_SEARCH:
                        processActorSearch(result, m3uitems);
                        break;
                    case Constants.YEAR_SEARCH:
                    case Constants.GENRE_SEARCH:
                        m3uitems.add(result.get("title").getAsString());
                        break;
                    default:
                        break;
                }
            }
        }

        if (!m3uitems.isEmpty()) {
            for (String m3uItem : m3uitems) {
                List<M3UItem> filmList = itemRepository.findByChannelName(Constants.MOVIE, "%" + m3uItem + "%");
                for (M3UItem m3uItemFromDb : filmList) {
                    m3uItemFromDb.setSearch(Utils.normaliseSearch(m3uItemFromDb.getChannelName()));
                    foundItems.add(m3uItemFromDb);
                }
            }
        }

        Collections.sort(foundItems, Comparator.comparing(M3UItem::getSearch));
        return foundItems;
    }

    private static void processActorSearch(JsonObject result, LinkedList<String> m3uitems) {
        JsonElement knownForDepartment = result.getAsJsonPrimitive("known_for_department");

        if (knownForDepartment.getAsString().equalsIgnoreCase("Acting")) {
            JsonArray knownFor = result.getAsJsonArray("known_for");

            for (JsonElement movieElement : knownFor) {
                JsonObject movie = movieElement.getAsJsonObject();
                String mediaType = movie.get("media_type").getAsString();

                if (mediaType.equalsIgnoreCase("tv")) {
                    m3uitems.add(movie.get("name").getAsString());
                } else if (mediaType.equalsIgnoreCase("movie")) {
                    m3uitems.add(movie.get("title").getAsString());
                }
            }
        }
    }
}
