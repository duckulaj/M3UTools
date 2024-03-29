package com.hawkins.m3utoolsjpa.search;

import java.util.List;

import com.hawkins.m3utoolsjpa.data.M3UItem;
import com.hawkins.m3utoolsjpa.data.M3UItemRepository;
import com.hawkins.m3utoolsjpa.utils.Constants;

public class SearchByTitle implements Search {

	@Override
	public List<M3UItem> search(String title, M3UItemRepository itemRepository) {
		
		List<M3UItem>searchResults = itemRepository.findByChannelName(Constants.MOVIE, "%" + title + "%");
		
		return searchResults;
		
	}

}
