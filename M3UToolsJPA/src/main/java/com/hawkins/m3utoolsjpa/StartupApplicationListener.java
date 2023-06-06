package com.hawkins.m3utoolsjpa;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

@Component
public class StartupApplicationListener implements ApplicationListener<ContextRefreshedEvent> {

	// @Autowired
	// ApplicationService applicationService;

	@Override 
	public void onApplicationEvent(ContextRefreshedEvent event) {

		// applicationService.runAtStartup();
	}
}

