package com.hawkins.m3utoolsjpa.service;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.collections4.IteratorUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.hawkins.m3utoolsjpa.annotations.TrackExecutionTime;
import com.hawkins.m3utoolsjpa.data.Filter;
import com.hawkins.m3utoolsjpa.data.FilterRepository;
import com.hawkins.m3utoolsjpa.data.M3UGroup;
import com.hawkins.m3utoolsjpa.data.M3UGroupRepository;
import com.hawkins.m3utoolsjpa.data.M3UItem;
import com.hawkins.m3utoolsjpa.data.M3UItemRepository;
import com.hawkins.m3utoolsjpa.data.TvChannel;
import com.hawkins.m3utoolsjpa.data.TvChannelRepository;
import com.hawkins.m3utoolsjpa.exception.DownloadFailureException;
import com.hawkins.m3utoolsjpa.exception.M3UItemsNotFoundException;
import com.hawkins.m3utoolsjpa.m3u.M3UGenre;
import com.hawkins.m3utoolsjpa.m3u.M3UGroupSelected;
import com.hawkins.m3utoolsjpa.properties.DownloadProperties;
import com.hawkins.m3utoolsjpa.redis.M3UGroupRedisService;
import com.hawkins.m3utoolsjpa.redis.M3UItemRedisService;
import com.hawkins.m3utoolsjpa.search.MovieDb;
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
	
	@Autowired
	PropertiesService propertiesService;
	
	@Autowired
	private M3UGroupRedisService m3UGroupRedisService;
	
	@Autowired
	private M3UItemRedisService m3UItemRedisService;
	
	@Autowired
	private XtreamService xtreamService;
	
	@TrackExecutionTime
	public void resetDatabase() throws M3UItemsNotFoundException, DownloadFailureException {

		// completableFutureService.reloadDatabase();
		xtreamService.getXtreamData();
				/*
		 * It is possible that we will have selected TV Channels. Now that the M3UItem and M3UGroup has been rebuilt
		 * we need to go through any existing channels and update M3UItem.selected and M3UItem.tvgChNo
		 */

		Iterable<TvChannel> tvChannels = channelRepository.findAll(); 
		log.info("Found {} selected TvChannels", IterableUtils.size(tvChannels));

		List<M3UItem> selectedItems = new ArrayList<M3UItem>();

		for (TvChannel tvChannel : tvChannels) {
			List<M3UItem> theseItems = itemRepository.findByTvgIdAndTvgName(tvChannel.getTvgId(), tvChannel.getTvgName());

			for (M3UItem item : theseItems) {
				if (item != null) {
					item.setSelected(true);
					item.setTvgChNo(tvChannel.getTvgChNo());
					// itemRepository.save(item);
					selectedItems.add(item);
				}
			}
		}

		if (selectedItems.size() > 0) {
			log.debug("Saving {} selected items", selectedItems.size());
			itemRepository.saveAll(selectedItems);
		}
		
		log.info("resetDatabase completed");

		writeTvChannelsM3U();

	}

	public Page<M3UItem> findAllPageable(Pageable pageable) {

		return itemRepository.findAll(pageable);
	}

	public List<M3UItem> getM3UItems() {
        List<M3UItem> cachedItems = m3UItemRedisService.findAll();
        if (cachedItems != null && !cachedItems.isEmpty()) {
            log.info("Reading from cache");
            return cachedItems;
        }
        return IteratorUtils.toList(itemRepository.findAll(Sort.by(Sort.Direction.ASC, "tvgName")).iterator());

	}

	public List<M3UGroup> getM3UGroups() {
        List<M3UGroup> cachedGroups = m3UGroupRedisService.findAll();
        if (cachedGroups != null && !cachedGroups.isEmpty()) {
            log.info("Reading from cache");
            return cachedGroups;
        }
        return IteratorUtils.toList(groupRepository.findAll(Sort.by(Sort.Direction.ASC, "name")).iterator());
    }

	public List<M3UGroup> getM3UGroupsByType(String type) {

		return IteratorUtils.toList(groupRepository.findByType(type, Sort.by(Sort.Direction.ASC, "name")).iterator());

	}

	public List<M3UItem> getM3UItemsByGroupTitle(String groupTitle) {
        List<M3UItem> cachedItems = m3UItemRedisService.findAll();
        if (cachedItems != null && !cachedItems.isEmpty()) {
            log.info("Reading from cache");
            return cachedItems.stream().filter(item -> groupTitle.equals(item.getGroupTitle())).collect(Collectors.toList());
        }
        return IteratorUtils.toList(itemRepository.findByGroupTitle(groupTitle).iterator());

	}

	public List<M3UItem> getM3UItemsByType(String type) {
        List<M3UItem> cachedItems = m3UItemRedisService.findAll();
        if (cachedItems != null && !cachedItems.isEmpty()) {
            log.info("Reading from cache");
            return cachedItems.stream().filter(item -> type.equals(item.getType())).collect(Collectors.toList());
        }
        return IteratorUtils.toList(itemRepository.findAllByType(type).iterator());

	}

	public Page<M3UItem> getM3UItemsByGroupTitle(String groupTitle, Pageable pageable) {

		return itemRepository.findByGroupTitle(groupTitle, pageable);

	}

	private List<M3UItem> getSelectedTvChannels() {

		return IteratorUtils.toList(itemRepository.findTvChannelsBySelected(true).iterator());
	}

	
	public Page<M3UItem> getPageableItems(Long groupId, int page, int size) {

		Pageable paging = PageRequest.of(page - 1, size, Sort.by("tvgName"));

		Page<M3UItem> pageItems;
		if (groupId == null || groupId == 0) {
			pageItems = itemRepository.findAll(paging);
		} else {
			pageItems = itemRepository.findByGroupId(groupId, paging);
		}

		if (pageItems.getSize() > 0) {
			for (M3UItem item : pageItems) {
				item.setSearch(Utils.normaliseSearch(item.getSearch()));
			}
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

		return new M3UGroupSelected(null, null, null);

	}

	public static String getConfigFileName() {

		return new File(DownloadProperties.getInstance().getFileName()).getAbsolutePath();
		

	}

	public List<M3UItem> searchMedia(String searchType, String criteria) {

		List<M3UItem> searchResults = new ArrayList<M3UItem>();

		int genreId = 0;

		// If the searchType evaluates to an integer it means that a genre was selected from the search.html form
		try {
			genreId = Integer.parseInt(searchType);
			searchType = Constants.GENRE_SEARCH;
			criteria = String.valueOf(genreId);
		} catch (NumberFormatException nfe) {
			genreId = 0;
		}

		if (criteria != null && criteria.length() > 0) {
			SearchFactory searchFactory = new SearchFactory();
			Search search = searchFactory.createSearch(searchType);
			searchResults = search.search(criteria, itemRepository);

			if (searchResults.size() > 0) {
				for (M3UItem item : searchResults) {
					item.setSearch(Utils.normaliseSearch(item.getSearch()));
				}
			}
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

	@TrackExecutionTime
	public void writeTvChannelsM3U() {

		String outputFile = "./M3UToolsJPA.m3u";

		List<M3UItem> channels = getSelectedTvChannels();


		try {
			BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(outputFile));
			Writer.write(channels, bos);
			bos.close();
		} catch (FileNotFoundException e) {
			log.info("FileNotFoundException - {}", e.getMessage());
		} catch (IOException e) {
			log.info("IOException - {}", e.getMessage());

		}

		log.info("Updated m3u file written to {}", outputFile);


	}

	public List<M3UGenre> getGenres() {

		MovieDb movieDb = MovieDb.getInstance();
		String genreURL = movieDb.getGenreURL();
		String api = movieDb.getApi();
		JsonObject obj = new JsonObject();

		try {

			Map<String, String> parameters = new HashMap<>();
			parameters.put("api_key", api);

			URL url = new URI(genreURL + "?" + Utils.getParamsString(parameters)).toURL();
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("GET");
			con.setRequestProperty("Content-Type", "application/json");

			JsonObject jsonObject = (JsonObject)JsonParser.parseReader(
					new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8));

			obj = jsonObject;

		} catch (Exception e) {
			log.info(e.getMessage());
		}

		ObjectMapper objectMapper = new ObjectMapper();

		List<M3UGenre> list = null;
		try {
			String jsonArray = obj.getAsJsonArray("genres").toString();
			TypeReference<List<M3UGenre>> typeReference = new TypeReference<List<M3UGenre>>() {};

			list = objectMapper.readValue(jsonArray, typeReference);
		} catch (JsonMappingException e) {
			log.info("JsonMappingException - {}", e.getMessage());
		} catch (JsonProcessingException e) {
			log.info("JsonProcessingException - {}", e.getMessage());			}

		return list;
	}





}