package com.hawkins.m3utoolsjpa.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import com.hawkins.m3utoolsjpa.emby.Emby;

@Controller
public class ManualController {

	/*
	 * This controller exists to allow the invocation on controller methods that
	 * have no graphical trigger.
	 */
	
	@GetMapping("/updateEmbyLibraries")
	public ModelAndView updateEmbyLibraries(ModelMap model) {

		Emby.refreshLibraries();
		
		return new ModelAndView("forward:/", model);
	}
}
