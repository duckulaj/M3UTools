
package com.hawkins.m3utoolsjpa.search;

import java.util.List;

import com.hawkins.m3utoolsjpa.data.M3UItem;
import com.hawkins.m3utoolsjpa.data.M3UItemRepository;
import com.hawkins.m3utoolsjpa.utils.Constants;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SearchByTitle implements Search {

    @Override
    public List<M3UItem> search(String title, M3UItemRepository itemRepository) {
        List<M3UItem> searchResults = null;
        try {
            searchResults = itemRepository.findByChannelName(Constants.MOVIE, "%" + title + "%");
        } catch (Exception e) {
            log.error("Error searching by title: {}", e.getMessage());
        }
        return searchResults;
    }
}
