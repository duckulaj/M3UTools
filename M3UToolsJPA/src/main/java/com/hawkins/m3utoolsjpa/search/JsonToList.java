package com.hawkins.m3utoolsjpa.search;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.hawkins.m3utoolsjpa.data.M3UItem;
import com.hawkins.m3utoolsjpa.data.M3UItemRepository;
import com.hawkins.m3utoolsjpa.regex.Patterns;
import com.hawkins.m3utoolsjpa.utils.Constants;
import com.hawkins.m3utoolsjpa.utils.Utils;

public class JsonToList {

	public static List<M3UItem> convertJsonToList(JsonObject jsonObj, M3UItemRepository itemRepository,
			String searchType) {

		/*
		 * This List will hold the matched items in the database and returned to the
		 * search results page
		 */
		List<M3UItem> foundItems = new ArrayList<M3UItem>();

		LinkedList<String> m3uitems = new LinkedList<String>();

		if (!jsonObj.isJsonNull()) {

			JsonArray results = (JsonArray) jsonObj.get("results");
			Iterator<JsonElement> resultsIterator = results.iterator();

			/*
			 * Within the resultant Json Object we are interested in the "known_for" section
			 * This holds all the information we require
			 */

			while (resultsIterator.hasNext()) {
				JsonObject result = resultsIterator.next().getAsJsonObject();

				switch (searchType) {
				case Constants.ACTOR_SEARCH:

					JsonElement known_for_department = result.getAsJsonPrimitive("known_for_department");

					if (known_for_department.getAsString().equalsIgnoreCase("Acting")) {

						JsonArray knownfor = (JsonArray) result.get("known_for");
						Iterator<JsonElement> knownforIt = knownfor.iterator();

						while (knownforIt.hasNext()) {
							JsonObject movie = knownforIt.next().getAsJsonObject();

							String mediatype = movie.get("media_type").getAsString();

							if (mediatype.equalsIgnoreCase("tv")) {
								m3uitems.add(movie.get("name").getAsString());
							} else if (mediatype.equalsIgnoreCase("movie")) {
								m3uitems.add(movie.get("title").getAsString());
							}

						}
					}

					break;

				case Constants.YEAR_SEARCH:

					m3uitems.add(result.get("title").getAsString());

					break;
				default:
					break;
				}

			}
		}

		/*
		 * If we have any matches from the Json results returned from the search
		 * provider let's see if we can find them in our database
		 */

		if (m3uitems.size() > 0) {

			for (String m3uItem : m3uitems) {

				List<M3UItem> filmList = itemRepository.findByChannelName(Constants.MOVIE, "%" + m3uItem + "%");
				ListIterator<M3UItem> iFilters = filmList.listIterator();

				while (iFilters.hasNext()) {
					M3UItem m3uItemFromDb = iFilters.next();
					m3uItemFromDb.setSearch(Utils.normaliseSearch(m3uItemFromDb.getChannelName()));
					foundItems.add(m3uItemFromDb);
				}

			}

		}

		return foundItems;
	}
}
