package com.hawkins.m3utoolsjpa.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.hawkins.m3utoolsjpa.data.M3UGroup;
import com.hawkins.m3utoolsjpa.data.M3UGroupRepository;
import com.hawkins.m3utoolsjpa.data.M3UItem;
import com.hawkins.m3utoolsjpa.data.M3UItemRepository;

import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
public class HtmxController {

	@Autowired
	private M3UItemRepository m3uItemRepository;
	
	@Autowired
	private M3UGroupRepository m3uGroupRepository;
	
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
}
