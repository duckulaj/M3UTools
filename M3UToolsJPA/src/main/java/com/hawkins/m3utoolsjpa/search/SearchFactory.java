package com.hawkins.m3utoolsjpa.search;

import com.hawkins.m3utoolsjpa.utils.Constants;

public class SearchFactory {

	public Search createSearch(String searchType) {
		
		Search thisSearchType = null;
		
		switch (searchType ) {
			case Constants.ACTOR_SEARCH -> thisSearchType =  new SearchByActor();
			case Constants.TITLE_SEARCH -> thisSearchType = new SearchByTitle();
			case Constants.YEAR_SEARCH -> thisSearchType = new SearchByYear();
			case Constants.GENRE_SEARCH -> thisSearchType = new SearchByGenre();
			default -> throw new IllegalArgumentException("Unknown searchType " + searchType);
		}
		
		return thisSearchType;
	}
}
