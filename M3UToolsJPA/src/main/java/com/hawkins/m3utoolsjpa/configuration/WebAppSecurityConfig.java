package com.hawkins.m3utoolsjpa.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.HeaderWriterLogoutHandler;
import org.springframework.security.web.header.writers.ClearSiteDataHeaderWriter;
import org.springframework.security.web.header.writers.ClearSiteDataHeaderWriter.Directive;

@Configuration
@EnableWebSecurity
public class WebAppSecurityConfig {

	@Bean
    SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
		
		HeaderWriterLogoutHandler clearSiteData = new HeaderWriterLogoutHandler(new ClearSiteDataHeaderWriter(Directive.ALL));
				    
        http
            .authorizeHttpRequests(authorizeRequests ->
                authorizeRequests
                    .requestMatchers("/webjars/**", "/css/**", "/js/**").permitAll()
                    .anyRequest().authenticated()
                    
            )
            .logout((logout) -> logout
                    .logoutSuccessUrl("/login")
                    .addLogoutHandler(clearSiteData)
                    .permitAll()
             )
            .formLogin(form -> form
            		.loginProcessingUrl("/loginPage.html")
            		.permitAll()
            )
            .httpBasic(Customizer.withDefaults())
            .csrf(csrf -> csrf.disable())
            .headers(headers -> {
                headers
                    .httpStrictTransportSecurity(Customizer.withDefaults())
                    .xssProtection(Customizer.withDefaults());
                    
            });
        return http.build();
    }

    @Bean
    UserDetailsService userDetailsService() {
        UserDetails user = User.withUsername("user")
        	.password("{bcrypt}$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG")
            .roles("USER")
            .build();
        UserDetails admin = User.withUsername("admin")
            	.password("{bcrypt}$2a$10$3/KYlinLpd7SLc9fFdKpjebmh3GQiwACk7IAW/nLDvyui5Oo2qKUu")
                .roles("ADMIN")
                .build();
        UserDetails jonathan = User.withUsername("jonathan")
            	.password("{bcrypt}$2a$10$N9AMwnocjm57hi54yeRzvOXCHt/UauJwD9LNSQZQUZg4XuYVyAXfO")
                .roles("ADMIN")
                .build();
        return new InMemoryUserDetailsManager(user, admin, jonathan);
    }
    
}
