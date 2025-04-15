package com.hawkins.m3utoolsjpa.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.hawkins.m3utoolsjpa.data.M3UGroup;
import com.hawkins.m3utoolsjpa.data.M3UGroupRepository;
import com.hawkins.m3utoolsjpa.data.M3UItem;
import com.hawkins.m3utoolsjpa.data.M3UItemRepository;
import com.hawkins.m3utoolsjpa.data.SelectedChannel;
import com.hawkins.m3utoolsjpa.data.SelectedChannelsCreationDto;
import com.hawkins.m3utoolsjpa.service.M3UService;
import com.hawkins.m3utoolsjpa.service.SelectedChannelService;
import com.hawkins.m3utoolsjpa.utils.Constants;

import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
public class HtmxController {

	@Autowired
	private M3UItemRepository m3uItemRepository;

	@Autowired
	private M3UGroupRepository m3uGroupRepository;

	@Autowired
    private SelectedChannelService channelsService;
    
    @Autowired
    private M3UService m3uService;
    
   
	@GetMapping("/m3ugroups")
	public String getM3UGroups(Model model) {
		List<M3UGroup> groups = m3uGroupRepository.findAll();
		log.info("groups: {}", groups);
		model.addAttribute("groups", groups);

		return "m3ugroups";
	}

	@GetMapping("/m3uitems")
	public String getM3UItems(@RequestParam Long groupId, Model model) {
		List<M3UItem> items = m3uItemRepository.findByGroupId(groupId);
		log.info("items count = {}", items.size());
		log.info("items: {}", items);
		model.addAttribute("items", items);
		return "m3uitems :: itemsTable";
	}

	@GetMapping("/tv")
	public String editTvChannels(Model model, @RequestParam(required = false, defaultValue = "-1") Long groupId) {


		List<SelectedChannel> channels = new ArrayList<>();
		channelsService.find(groupId)
		.iterator()
		.forEachRemaining(channels::add);
		
		Collections.sort(channels, Comparator.comparing(SelectedChannel::getTvgName));

		model.addAttribute(Constants.SELECTEDGROUP, m3uService.getSelectedGroup(groupId));
		model.addAttribute("groupId", groupId);
		// model.addAttribute("groups", m3uService.getM3UGroups());
		model.addAttribute("groups", m3uService.getM3UGroupsByType(Constants.LIVE));
		model.addAttribute("form", new SelectedChannelsCreationDto(channels));

		return "channels :: channelsTable";
	}
	
	@PostMapping(value = "/showSelected")
    public String showSelected(Model model, @RequestParam(required = false, defaultValue = "-1") Long groupId) {
    	
    	List<SelectedChannel> channels = new ArrayList<>();
        channelsService.find(true)
            .iterator()
            .forEachRemaining(channels::add);
        
        Collections.sort(channels, Comparator.comparing(SelectedChannel::getTvgName));

        model.addAttribute(Constants.SELECTEDGROUP, m3uService.getSelectedGroup(groupId));
        model.addAttribute("groups", m3uService.getM3UGroups());
        model.addAttribute("groupId", groupId);
        model.addAttribute("form", new SelectedChannelsCreationDto(channels));
        
        
        return "channels :: channelsTable";
    	
    }
}
