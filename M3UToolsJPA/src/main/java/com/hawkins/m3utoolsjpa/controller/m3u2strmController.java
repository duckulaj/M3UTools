package com.hawkins.m3utoolsjpa.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import com.hawkins.m3utoolsjpa.data.M3UItemRepository;
import com.hawkins.m3utoolsjpa.m3u.M3UtoStrm;

import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
public class m3u2strmController {

	@Autowired
	M3UItemRepository itemRepository;
	
	@GetMapping("/convertToStream") 
	public ModelAndView convertM3UtoStream(ModelMap model) {
		
		log.info("Starting convertM3UtoStream()");
		
		M3UtoStrm.convertM3UtoStream(itemRepository);
		
		return new ModelAndView("forward:/", model);
	}
}
