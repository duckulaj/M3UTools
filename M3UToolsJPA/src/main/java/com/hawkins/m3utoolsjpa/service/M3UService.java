package com.hawkins.m3utoolsjpa.service;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.collections4.IteratorUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import com.hawkins.m3utoolsjpa.data.Filter;
import com.hawkins.m3utoolsjpa.data.FilterRepository;
import com.hawkins.m3utoolsjpa.data.M3UGroup;
import com.hawkins.m3utoolsjpa.data.M3UGroupRepository;
import com.hawkins.m3utoolsjpa.data.M3UItem;
import com.hawkins.m3utoolsjpa.data.M3UItemRepository;
import com.hawkins.m3utoolsjpa.m3u.M3UGroupSelected;
import com.hawkins.m3utoolsjpa.parser.Parser;
import com.hawkins.m3utoolsjpa.properties.ConfigProperty;
import com.hawkins.m3utoolsjpa.properties.DownloadProperties;
import com.hawkins.m3utoolsjpa.properties.OrderedProperties;
import com.hawkins.m3utoolsjpa.search.Search;
import com.hawkins.m3utoolsjpa.search.SearchFactory;
import com.hawkins.m3utoolsjpa.utils.Constants;
import com.hawkins.m3utoolsjpa.utils.Utils;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class M3UService {

	@Autowired
	M3UItemRepository itemRepository;

	@Autowired
	M3UGroupRepository groupRepository;

	@Autowired
	FilterRepository filterRepository;
	
	@Autowired
	DatabaseUpdates databaseUpdates;
	
	public void resetDatabase() {
			

		StopWatch sw = new org.springframework.util.StopWatch();
		sw.start();

		LinkedList<M3UItem> items = Parser.parse();
		
		if (items.size() > 0) {
		
			itemRepository.deleteAll(itemRepository.findAll());
	
			groupRepository.deleteAll(groupRepository.findAll());
		
			for (M3UItem item : items) {
	
				M3UGroup group = groupRepository.findByName(item.getGroupTitle());
				if (group == null) {
					M3UGroup newGroup = groupRepository.save(new M3UGroup(item.getGroupTitle(), item.getType()));
					item.setGroupId(newGroup.getId());
				} else {
					item.setGroupId(group.getId());
				}
			}
	
			itemRepository.saveAll(items);
			
			log.info("Saved {} M3UItem(s)", items.size());
		}
		sw.stop();

		log.info("Total time in milliseconds for all tasks : " + sw.getTotalTimeMillis());
		log.info("resetDatabase completed");
	}

	public Page<M3UItem> findAllPageable(Pageable pageable) {

		return itemRepository.findAll(pageable);
	}

	public List<M3UItem> getM3UItems() {

		return IteratorUtils.toList(itemRepository.findAll(Sort.by(Sort.Direction.ASC, "tvgName")).iterator());

	}

	public List<M3UGroup> getM3UGroups() {

		return IteratorUtils.toList(groupRepository.findAll(Sort.by(Sort.Direction.ASC, "name")).iterator());

	}
	
	public List<M3UGroup> getM3UGroupsByType(String type) {

		return IteratorUtils.toList(groupRepository.findByType(type, Sort.by(Sort.Direction.ASC, "name")).iterator());

	}

	public List<M3UItem> getM3UItemsByGroupTitle(String groupTitle) {

		return IteratorUtils.toList(itemRepository.findByGroupTitle(groupTitle).iterator());

	}

	public List<M3UItem> getM3UItemsByType(String type) {

		return IteratorUtils.toList(itemRepository.findByType(type).iterator());

	}

	public Page<M3UItem> getM3UItemsByGroupTitle(String groupTitle, Pageable pageable) {

		return itemRepository.findByGroupTitle(groupTitle, pageable);

	}
	
	public List<M3UItem> getSelectedTvChannels() {
		
		return IteratorUtils.toList(itemRepository.findTvChannelsBySelected(true).iterator());
	}

	public static OrderedProperties getOrderProperties() {

		OrderedProperties properties = new OrderedProperties();
		try {
			properties.load(new FileReader(Utils.getPropertyFile(Constants.CONFIGPROPERTIES)));
			properties = OrderedProperties.copyOf(properties);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return properties;

	}

	public static Set<Entry<String, String>> getOrderedPropertiesEntrySet() {

		OrderedProperties properties = new OrderedProperties();
		try {
			properties.load(new FileReader(Utils.getPropertyFile(Constants.CONFIGPROPERTIES)));
					properties = OrderedProperties.copyOf(properties);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return properties.entrySet();
	}

	public static void updateProperty(ConfigProperty configProperty) {

		log.info(configProperty.toString());
		DownloadProperties.getInstance().updateProperty(configProperty);
	}

	public Page<M3UItem> getPageableItems(Long groupId, int page, int size) {

		Pageable paging = PageRequest.of(page - 1, size, Sort.by("tvgName"));

		Page<M3UItem> pageItems;
		if (groupId == null || groupId == 0) {
			pageItems = itemRepository.findAll(paging);
		} else {
			pageItems = itemRepository.findByGroupId(groupId, paging);
		}

		return pageItems;
	}
	
	public Page<M3UItem> getPageableTvChannels(Long groupId, int page, int size) {

		Pageable paging = PageRequest.of(page - 1, size, Sort.by("tvgName"));

		Page<M3UItem> pageItems;
		if (groupId == null || groupId == 0 || groupId == -1) {
			pageItems = itemRepository.findTvChannels(paging);
		} else {
			pageItems = itemRepository.findTvChannelsByGroup(groupId, paging);
		}

		return pageItems;
	}

	public M3UGroupSelected getSelectedGroup(Long groupId) {

		Optional<M3UGroup> foundGroup = groupRepository.findById(groupId);
		if (foundGroup.isPresent()) {
			M3UGroupSelected selectedGroup = new M3UGroupSelected(foundGroup.get().getId(), foundGroup.get().getName(),
					foundGroup.get().getType());

			return selectedGroup;
		}

		return new M3UGroupSelected();

	}
	
	public void resetM3UFile() {

		if (log.isDebugEnabled()) {
			log.debug("The time is now {}", Utils.printNow());
		}

		DownloadProperties downloadProperties = DownloadProperties.getInstance();

		Utils.copyUrlToFile(downloadProperties.getChannels(), downloadProperties.getFullM3U());
		if (log.isDebugEnabled()) {
			log.debug("Reloaded m3u file at {}", Utils.printNow());
		}
	}

	public static String getConfigFileName() {

		return Utils.getPropertyFile(Constants.CONFIGPROPERTIES).getAbsolutePath();

	}

	public List<M3UItem> searchMedia(String searchType, String criteria) {

		List<M3UItem> searchResults = new ArrayList<M3UItem>();

		if (criteria != null && criteria.length() > 0) {
			SearchFactory searchFactory = new SearchFactory();
			Search search = searchFactory.createSearch(searchType);
			searchResults = search.search(criteria, itemRepository);
		}

		return searchResults;
	}
	
	public List<Filter> getFilters() {

		return IteratorUtils.toList(filterRepository.findAll(Sort.by(Sort.Direction.ASC, "name")).iterator());
		
	}
	
	public void saveFilter(Filter filter) {
		
		filterRepository.save(filter);
		
	}
}
