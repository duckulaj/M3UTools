package com.hawkins.m3uttoolsjpa.jobs;

interface DetailedJob extends Runnable {
    int getProgress();

    String getState();

    String getJobName();
}
