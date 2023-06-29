package com.hawkins.m3utoolsjpa.service;

import java.awt.print.Book;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.collections4.IteratorUtils;
import org.apache.commons.collections4.properties.SortedProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import com.hawkins.m3utoolsjpa.data.TvChannel;
import com.hawkins.m3utoolsjpa.data.TvChannelRepository;
import com.hawkins.m3utoolsjpa.exception.M3UItemsNotFoundException;
import com.hawkins.m3utoolsjpa.m3u.M3UGroupSelected;
import com.hawkins.m3utoolsjpa.properties.ConfigProperty;
import com.hawkins.m3utoolsjpa.properties.DownloadProperties;
import com.hawkins.m3utoolsjpa.properties.OrderedProperties;
import com.hawkins.m3utoolsjpa.search.Search;
import com.hawkins.m3utoolsjpa.search.SearchFactory;
import com.hawkins.m3utoolsjpa.utils.Constants;
import com.hawkins.m3utoolsjpa.utils.Utils;
import com.hawkins.m3utoolsjpa.utils.Writer;

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
	TvChannelRepository channelRepository;
	
	@Autowired
	CompletableFutureService completableFutureService;
	
	@Value("${spring.jpa.properties.hibernate.jdbc.batch_size}")
    private static int batchSize;
	
	public void resetDatabase() throws M3UItemsNotFoundException {
			
		StopWatch sw = new org.springframework.util.StopWatch();
		sw.start();

		List<M3UItem> items = completableFutureService.reloadDatabase();
		
		if (items == null) {
			throw new M3UItemsNotFoundException("No items found from M3UParser, an error occured connecting to streaming service or content is malformed");
		}
		
		if (items.size() > 0) {
		
			for (M3UItem item : items) {
	
				M3UGroup group = groupRepository.findByName(item.getGroupTitle());
				if (group == null) {
					M3UGroup newGroup = groupRepository.save(new M3UGroup(item.getGroupTitle(), item.getType()));
					item.setGroupId(newGroup.getId());
				} else {
					item.setGroupId(group.getId());
				}
			}
	
			StopWatch swSave = new org.springframework.util.StopWatch();
			swSave.start();
			itemRepository.saveAllAndFlush(items);
			// saveAllUsingBatch(items);
			
			swSave.stop();
			log.info("Saved {} M3UItem(s) in {} milliseconds", items.size(), swSave.getTotalTimeMillis());
		}
		
		/*
		 * It is possible that we will have selected TV Channels. Now that the M3UItem and M3UGroup has been rebuilt
		 * we need to go through any existing channels and update M3UItem.selected and M3UItem.tvgChNo
		 */
		
		Iterable<TvChannel> tvChannels = channelRepository.findAll(); 
		log.info("Found {} selected TvChannels", IterableUtils.size(tvChannels));
		
		for (TvChannel tvChannel : tvChannels) {
			List<M3UItem> theseItems = itemRepository.findByTvgIdAndTvgName(tvChannel.getTvgId(), tvChannel.getTvgName());
			for (M3UItem item : theseItems) {
				if (item != null) {
					item.setSelected(true);
					item.setTvgChNo(tvChannel.getTvgChNo());
					itemRepository.save(item);
				}
			}
		}
		
		sw.stop();

		log.info("Total time in milliseconds for all tasks : " + sw.getTotalTimeMillis());
		log.info("resetDatabase completed");
		
		writeTvChannelsM3U();

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

		return IteratorUtils.toList(itemRepository.findByTypeOrderByTvgName(type).iterator());

	}

	public Page<M3UItem> getM3UItemsByGroupTitle(String groupTitle, Pageable pageable) {

		return itemRepository.findByGroupTitle(groupTitle, pageable);

	}
	
	public List<M3UItem> getSelectedTvChannels() {
		
		return IteratorUtils.toList(itemRepository.findTvChannelsBySelected(true).iterator());
	}

	public static OrderedProperties getOrderProperties() {

		SortedProperties sortedProperties = new SortedProperties();
		OrderedProperties properties = new OrderedProperties();
		try {
			properties.load(new FileReader(Utils.getPropertyFile(Constants.CONFIGPROPERTIES)));
			properties = OrderedProperties.copyOf(properties);
			sortedProperties.load(new FileReader(Utils.getPropertyFile(Constants.CONFIGPROPERTIES)));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return properties;

	}

	public static Set<Entry<String, String>> getOrderedPropertiesEntrySet() {

		OrderedProperties properties = new OrderedProperties();
		try {
			properties.loadFromXML(new FileInputStream(Utils.getPropertyFile(Constants.CONFIGPROPERTIES)));
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
		 

		/*
		 * if (foundGroup != null) { M3UGroupSelected selectedGroup = new
		 * M3UGroupSelected(foundGroup.getId(), foundGroup.getName(),
		 * foundGroup.getType());
		 * 
		 * return selectedGroup; }
		 */

		return new M3UGroupSelected();

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
	
	public List<TvChannel> getTvChannels() {

		return IteratorUtils.toList(channelRepository.findAll(Sort.by(Sort.Direction.ASC, "channelId")).iterator());

	}
	
	public void writeTvChannelsM3U() {
		
		// DownloadProperties dp = DownloadProperties.getInstance();
				
		// String outputFile = dp.getFileWatcherLocation() + "/M3UToolsJPA.m3u";
		String outputFile = "./M3UToolsJPA.m3u";
		
		List<M3UItem> channels = getSelectedTvChannels();
		
		
		try {
			BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(outputFile));
			Writer.write(channels, bos);
			bos.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		log.info("Updated m3u file written to {}", outputFile);
		
		
	}
	
	public void saveAllUsingBatch(List<M3UItem> items) {
		
		batchSize = 1000;
		int totalObjects = items.size();
		for (int i = 0; i < totalObjects; i += batchSize) {
	        if( i+ batchSize > totalObjects){
	            List<M3UItem> items1 = items.subList(i, totalObjects - 1);
	            itemRepository.saveAll(items1);
	            break;
	        }
	        List<M3UItem> items1 = items.subList(i, i + batchSize);
	        itemRepository.saveAll(items1);
	    }

	}

}
