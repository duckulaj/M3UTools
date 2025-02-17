package com.hawkins.m3utoolsjpa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.repository.config.BootstrapMode;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@ComponentScan(basePackages = {"com.hawkins.m3utoolsjpa"})
@EnableAsync
@EnableJpaRepositories(bootstrapMode = BootstrapMode.DEFAULT)
@EnableScheduling
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class M3UToolsJpaApplication {

	@Bean
   RestTemplate restTemplateBean() { 
        return new RestTemplate(); 
    } 
	
	public static void main(String[] args) {
		SpringApplication.run(M3UToolsJpaApplication.class, args);
	}

}
