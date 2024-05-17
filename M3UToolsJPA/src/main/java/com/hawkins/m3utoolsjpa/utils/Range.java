package com.hawkins.m3utoolsjpa.utils;

import java.io.IOException;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

public class Range {
    private long start;
    private long end;
    private long size;
    private Resource resource;

    public Range(long start, long end, long size, Resource resource) {
        this.start = start;
        this.end = end;
        this.size = size;
        this.resource = resource;
    }

    public static Range parse(String rangeHeader, long fileSize, Resource resource) {
        if (rangeHeader == null || !rangeHeader.startsWith("bytes=")) {
            return new Range(0, fileSize - 1, fileSize, resource);
        }

        String[] ranges = rangeHeader.substring("bytes=".length()).split("-");
        long start = Long.parseLong(ranges[0]);
        long end = ranges.length > 1 ? Long.parseLong(ranges[1]) : fileSize - 1;

        return new Range(start, end, fileSize, null);
    }

    public String getContentRangeHeader() {
        return "bytes " + start + "-" + end + "/" + size;
    }

    public Resource getResource() {
        if (resource == null) {
            try {
                
                byte[] data = resource.getContentAsByteArray();
                return new ByteArrayResource(data);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
        return resource;
    }
}
