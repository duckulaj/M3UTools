package com.hawkins.m3utoolsjpa.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import com.hawkins.m3utoolsjpa.service.EpgService;

@Controller
public class epgController {
	
	@Autowired
	EpgService epgService;
	
	@GetMapping("readEPG")
	public ModelAndView readEPG(ModelMap model) {
		
		epgService.readEPG();
		// epgService.readEPGUsingSax();
		
		return new ModelAndView("forward:/", model);
	}
	
	@GetMapping("/reloadEPG")
	public ModelAndView reloadEPG(ModelMap model) {

		epgService.readEPG();
		
		return new ModelAndView("forward:/", model);
	}
	
	@GetMapping("/showEPG")
	public ModelAndView showEPG(ModelMap model) {
		
		// model.addAttribute("channels", epgService.getEPGJson());
		
		return new ModelAndView("epg", model);
		
		
	}

}
