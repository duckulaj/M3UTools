package com.hawkins.m3utoolsjpa.mediaSearch;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.google.gson.JsonObject;
import com.hawkins.m3utoolsjpa.data.M3UItem;
import com.hawkins.m3utoolsjpa.data.M3UItemRepository;

public abstract class MediaSearchHelper extends HelperObject {

	@Autowired
	M3UItemRepository itemRepository;
	
	private static MediaSearchHelper thisMediaSearchHelper = null;
	
	public static MediaSearchHelper getsearchHelper(String SearchEngine) {
		
		if (SearchEngine.equals("MovieDb")) {
			thisMediaSearchHelper=  MovieDb.getInstance();
		} else if (SearchEngine.equals("Imdb")) {
			thisMediaSearchHelper = Imdb.getInstance();
		}
		
		return thisMediaSearchHelper;
	}
	
	public JsonObject searchByActor(String actor) {
		
		return thisMediaSearchHelper.searchByActor(actor);
		
	}
	
	public JsonObject searchByYear(String year) {
		
		return thisMediaSearchHelper.searchByYear(year);
		
	}
	
	public static String getParamsString(Map<String, String> params) 
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

}
