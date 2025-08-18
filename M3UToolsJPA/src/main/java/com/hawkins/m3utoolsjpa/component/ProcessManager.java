package com.hawkins.m3utoolsjpa.component;

import java.util.concurrent.ConcurrentHashMap;

public class ProcessManager {
    private static final ConcurrentHashMap<String, Process> processMap = new ConcurrentHashMap<>();

    public static void addProcess(String sessionId, Process process) {
        processMap.put(sessionId, process);
    }

    public static void terminateProcess(String sessionId) {
        Process process = processMap.remove(sessionId);
        if (process != null && process.isAlive()) {
            process.destroy();
        }
    }

    public static void terminateAllProcesses() {
        processMap.forEach((p, process) -> {
            if (process.isAlive()) {
                process.destroy();
            }
        });
        processMap.clear();
    }
}
