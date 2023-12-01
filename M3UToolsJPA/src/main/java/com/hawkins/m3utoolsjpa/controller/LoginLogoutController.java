package com.hawkins.m3utoolsjpa.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.PostMapping;

import com.hawkins.m3utoolsjpa.utils.Constants;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class LoginLogoutController {

	SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();

	@PostMapping("/logout")
	public String performLogout(Authentication authentication, HttpServletRequest request, HttpServletResponse response) {
	    // .. perform logout
	    this.logoutHandler.logout(request, response, authentication);
	    return "redirect:/login";
	}
	
	@PostMapping("/login")
	public String performLogin() {
		return "loginPage.html";
	}
	
	@PostMapping("/error")
	public String error() {
		
		return Constants.ITEMS;
	}
}
