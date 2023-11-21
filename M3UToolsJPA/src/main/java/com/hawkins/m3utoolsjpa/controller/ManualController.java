package com.hawkins.m3utoolsjpa.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.hawkins.m3utoolsjpa.emby.EmbyApi;
import com.hawkins.m3utoolsjpa.service.CompletableFutureService;
import com.hawkins.m3utoolsjpa.service.EpgService;
import com.hawkins.m3utoolsjpa.service.M3UService;
import com.hawkins.m3utoolsjpa.utils.LoggerUtils;
import com.hawkins.m3utoolsjpa.utils.Utils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
public class ManualController {

	@Autowired
	M3UService m3uService;
	
	@Autowired
	EpgService epgService;
	
	@Autowired
	CompletableFutureService completableFutureService;
	
	/*
	 * This controller exists to allow the invocation on controller methods that
	 * have no graphical trigger.
	 */
	
	@GetMapping("/updateEmbyLibraries")
	public ModelAndView updateEmbyLibraries(ModelMap model) {

		EmbyApi.refreshLibraries();
		
		log.info("updateEmbyLibraries(Manual) completed at {}", Utils.printNow());
		
		return new ModelAndView("forward:/", model);
	}
	
	@GetMapping("/reloadEPG")
	public ModelAndView reloadEPG(ModelMap model) {

		epgService.readEPG();
		
		return new ModelAndView("forward:/", model);
	}
	
	@GetMapping("writeM3U")
	public ModelAndView writeM3U(ModelMap model) {
		
		m3uService.writeTvChannelsM3U();
		
		return new ModelAndView("forward:/", model);
	}
	
	@GetMapping("readEPG")
	public ModelAndView readEPG(ModelMap model) {
		
		epgService.readEPG();
		// epgService.readEPGUsingSax();
		
		return new ModelAndView("forward:/", model);
	}

	@GetMapping("completableFuture")
	public ModelAndView completableFuture(ModelMap model) {
		
		completableFutureService.writeEPG();
		
		return new ModelAndView("forward:/", model);
	}
	
	@GetMapping("setLogLevel")
	public ModelAndView setLogLevel(ModelMap model, @RequestParam(required = true, defaultValue = "info") String level) {
		
		LoggerUtils.updateLogLevel(level);
		model.addAttribute("currentLevel", LoggerUtils.getCurrentLogLevel());
		
		return new ModelAndView("forward:/viewLog", model);
	}
	
	
}
