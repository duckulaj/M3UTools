package com.hawkins.m3utoolsjpa.search;

import java.util.List;

import com.hawkins.m3utoolsjpa.data.M3UItem;
import com.hawkins.m3utoolsjpa.data.M3UItemRepository;

public interface Search {

	public List<M3UItem> search(String criteria, M3UItemRepository itemRepository);

}
