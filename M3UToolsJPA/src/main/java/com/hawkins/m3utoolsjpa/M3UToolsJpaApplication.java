package com.hawkins.m3utoolsjpa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.repository.config.BootstrapMode;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ComponentScan(basePackages = {"com.hawkins.m3utoolsjpa"})
@EnableAsync
@EnableJpaRepositories(bootstrapMode = BootstrapMode.DEFAULT)
@EnableScheduling
public class M3UToolsJpaApplication {

	public static void main(String[] args) {
		SpringApplication.run(M3UToolsJpaApplication.class, args);
	}

}
