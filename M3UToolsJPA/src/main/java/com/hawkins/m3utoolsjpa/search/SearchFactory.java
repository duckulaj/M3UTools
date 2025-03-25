
package com.hawkins.m3utoolsjpa.search;

import com.hawkins.m3utoolsjpa.utils.Constants;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SearchFactory {

    public Search createSearch(String searchType) {
        log.info("Creating search for type: {}", searchType);

        return switch (searchType) {
            case Constants.ACTOR_SEARCH -> new SearchByActor();
            case Constants.TITLE_SEARCH -> new SearchByTitle();
            case Constants.YEAR_SEARCH -> new SearchByYear();
            case Constants.GENRE_SEARCH -> new SearchByGenre();
            default -> throw new IllegalArgumentException("Unknown searchType " + searchType);
        };
    }
}
