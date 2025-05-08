package com.hawkins.m3utoolsjpa.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;

import com.hawkins.m3utoolsjpa.component.ProcessManager;

@RestController
public class NavigationController {

    @PostMapping("/navigation/handle")
    public void handleNavigation() {
        String sessionId = RequestContextHolder.currentRequestAttributes().getSessionId();
        ProcessManager.terminateProcess(sessionId);
    }
}
