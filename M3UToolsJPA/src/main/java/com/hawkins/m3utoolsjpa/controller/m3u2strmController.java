package com.hawkins.m3utoolsjpa.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.hawkins.m3utoolsjpa.data.M3UGroupRepository;
import com.hawkins.m3utoolsjpa.data.M3UItemRepository;
import com.hawkins.m3utoolsjpa.m3u.M3UGroupSelected;
import com.hawkins.m3utoolsjpa.m3u.M3UtoStrm;
import com.hawkins.m3utoolsjpa.service.M3UService;
import com.hawkins.m3utoolsjpa.utils.Constants;

import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
public class m3u2strmController {

	@Autowired
	M3UItemRepository itemRepository;
	
	@Autowired 
	M3UGroupRepository groupRepository;
	
	@ModelAttribute
	public void initValues(Model model) {
		
	}

	@GetMapping("/convertToStream") public String convertM3UtoStream(Model model) {
		
		log.info("Starting convertM3UtoStream()");
		
		M3UtoStrm.convertM3UtoStream(itemRepository);
		
		
		model.addAttribute("groups", M3UService.getM3UGroups(groupRepository));
		model.addAttribute(Constants.SELECTEDGROUP, new M3UGroupSelected());
		model.addAttribute(Constants.SEARCHFILTER, new String());
		return Constants.ITEMS;
	}
}
