package com.hawkins.m3utoolsjpa.job;

interface DetailedJob extends Runnable {
    int getProgress();

    String getState();

    String getJobName();
}
