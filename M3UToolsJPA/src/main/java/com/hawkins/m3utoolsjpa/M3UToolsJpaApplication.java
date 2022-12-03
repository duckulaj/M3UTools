package com.hawkins.m3utoolsjpa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.repository.config.BootstrapMode;


@SpringBootApplication
@EnableJpaRepositories(bootstrapMode = BootstrapMode.DEFAULT)
public class M3UToolsJpaApplication {

	public static void main(String[] args) {
		SpringApplication.run(M3UToolsJpaApplication.class, args);
	}

}
