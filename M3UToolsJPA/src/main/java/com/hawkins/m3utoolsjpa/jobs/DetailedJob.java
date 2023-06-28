package com.hawkins.m3utoolsjpa.jobs;

interface DetailedJob extends Runnable {
    int getProgress();

    String getState();

    String getJobName();
}
