package com.hawkins.m3utoolsjpa;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import com.hawkins.m3utoolsjpa.service.ApplicationService;

@Component
public class StartupApplicationListener implements ApplicationListener<ContextRefreshedEvent> {

	@Autowired
	ApplicationService applicationService;

	@Override 
	public void onApplicationEvent(ContextRefreshedEvent event) {

		applicationService.runAtStartup();
	}
}

