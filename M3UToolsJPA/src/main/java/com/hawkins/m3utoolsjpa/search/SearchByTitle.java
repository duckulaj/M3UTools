package com.hawkins.m3utoolsjpa.search;

import java.util.ArrayList;
import java.util.List;

import com.hawkins.m3utoolsjpa.data.M3UItem;
import com.hawkins.m3utoolsjpa.data.M3UItemRepository;

public class SearchByTitle implements Search {

	@Override
	public List<M3UItem> search(String title, M3UItemRepository itemRepository) {
		
		List<M3UItem>searchResults = new ArrayList<M3UItem>();
		
		return searchResults;
		
	}

}
