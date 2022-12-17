package com.hawkins.m3utoolsjpa.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import com.hawkins.m3utoolsjpa.component.ScheduledTasks;
import com.hawkins.m3utoolsjpa.emby.Emby;
import com.hawkins.m3utoolsjpa.epg.EpgReader;
import com.hawkins.m3utoolsjpa.properties.DownloadProperties;
import com.hawkins.m3utoolsjpa.service.M3UService;
import com.hawkins.m3utoolsjpa.utils.Utils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
public class ManualController {

	@Autowired
	M3UService m3uService;
	/*
	 * This controller exists to allow the invocation on controller methods that
	 * have no graphical trigger.
	 */
	
	@GetMapping("/updateEmbyLibraries")
	public ModelAndView updateEmbyLibraries(ModelMap model) {

		Emby.refreshLibraries();
		
		log.info("updateEmbyLibraries(Manual) completed at {}", Utils.printNow());
		
		return new ModelAndView("forward:/", model);
	}
	
	@GetMapping("/resetM3UFile")
	public ModelAndView resetM3UFile(ModelMap model) {

		m3uService.resetM3UFile();
		
		log.info("resetM3UFile(Manual) completed at {}", Utils.printNow());
		
		return new ModelAndView("forward:/", model);
	}
	
	@GetMapping("/reloadEPG")
	public ModelAndView reloadEPG(ModelMap model) {

		String epgFile = DownloadProperties.getInstance().getEpgFileName();
		
		EpgReader.changeLocalTime(epgFile);
		
		log.info("reloadEPG(Manual) completed at {}", Utils.printNow());
		
		return new ModelAndView("forward:/", model);
	}
}
