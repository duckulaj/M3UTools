package com.hawkins.m3utoolsjpa.search;

import com.hawkins.m3utoolsjpa.utils.Constants;

public class SearchFactory {

	public Search createSearch(String searchType) {
		
		switch (searchType ) {
		case Constants.ACTOR_SEARCH:
			
			return new SearchByActor();
			
		case Constants.TITLE_SEARCH:
			
			return new SearchByTitle();
			
		case Constants.YEAR_SEARCH:
			
			return new SearchByYear();
		
		default :
			throw new IllegalArgumentException("Unknown searchType " + searchType);
		}
	}
}
