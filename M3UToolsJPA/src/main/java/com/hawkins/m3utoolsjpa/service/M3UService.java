package com.hawkins.m3utoolsjpa.service;

import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.collections4.IteratorUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import com.hawkins.m3utoolsjpa.data.M3UGroup;
import com.hawkins.m3utoolsjpa.data.M3UGroupRepository;
import com.hawkins.m3utoolsjpa.data.M3UItem;
import com.hawkins.m3utoolsjpa.data.M3UItemRepository;
import com.hawkins.m3utoolsjpa.parser.Parser;
import com.hawkins.m3utoolsjpa.properties.ConfigProperty;
import com.hawkins.m3utoolsjpa.properties.DownloadProperties;
import com.hawkins.m3utoolsjpa.properties.OrderedProperties;
import com.hawkins.m3utoolsjpa.utils.Constants;
import com.hawkins.m3utoolsjpa.utils.Utils;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class M3UService {


	public void resetDatabase(M3UItemRepository itemRepository, M3UGroupRepository groupRepository) {

		StopWatch sw = new org.springframework.util.StopWatch();
		sw.start();

		itemRepository.deleteAll(itemRepository.findAll());

		groupRepository.deleteAll(groupRepository.findAll());

		LinkedList<M3UItem> items = Parser.parse();

		for (M3UItem item : items) {

			M3UGroup group = groupRepository.findByName(item.getGroupTitle());
			if (group == null) {
				M3UGroup newGroup = groupRepository.save(new M3UGroup(item.getGroupTitle(), item.getType()));
				item.setGroupId(newGroup.getId());
			} else {
				item.setGroupId(group.getId());
			}
		}


		itemRepository.saveAll(items); log.info("Saved {} M3UItem(s)", items.size());
		
		sw.stop();

		log.info("Total time in milliseconds for all tasks : " + sw.getTotalTimeMillis());
		log.info("resetDatabase completed");
	}

	public static Page<M3UItem> findAllPageable(Pageable pageable, M3UItemRepository itemRepository) {

		return itemRepository.findAll(pageable); 
	}

	public static List<M3UItem> getM3UItems(M3UItemRepository itemRepository) {

		return IteratorUtils.toList(itemRepository.findAll(Sort.by(Sort.Direction.ASC, "tvgName")).iterator());

	}

	public static List<M3UGroup> getM3UGroups(M3UGroupRepository groupRepository) {

		return IteratorUtils.toList(groupRepository.findAll(Sort.by(Sort.Direction.ASC, "name")).iterator());

	}

	public static List<M3UItem> getM3UItemsByGroupTitle(M3UItemRepository itemRepository, String groupTitle) {

		return IteratorUtils.toList(itemRepository.findByGroupTitle(groupTitle).iterator());

	}

	public static Page<M3UItem> getM3UItemsByGroupTitle(M3UItemRepository itemRepository, String groupTitle, Pageable pageable) {

		return itemRepository.findByGroupTitle(groupTitle, pageable);

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
}
