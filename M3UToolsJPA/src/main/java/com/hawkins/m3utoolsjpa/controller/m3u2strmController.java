package com.hawkins.m3utoolsjpa.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import com.hawkins.m3utoolsjpa.data.M3UItemRepository;
import com.hawkins.m3utoolsjpa.service.M3UtoStrm;

import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
public class m3u2strmController {

	@Autowired
	M3UItemRepository itemRepository;
	
	@Autowired
	M3UtoStrm m3UtoStrm;
	
	@GetMapping("/convertToStream") 
	public ModelAndView convertM3UtoStream(ModelMap model) {
		
		log.info("Starting convertM3UtoStream()");
		
		m3UtoStrm.convertM3UtoStream();
		
		return new ModelAndView("forward:/", model);
	}
}
