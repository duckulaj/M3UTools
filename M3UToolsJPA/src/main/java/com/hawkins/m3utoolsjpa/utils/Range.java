
package com.hawkins.m3utoolsjpa.utils;

import java.io.IOException;
import java.util.Optional;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

public class Range {
    private final long start;
    private final long end;
    private final long size;
    private final Optional<Resource> resource;

    public Range(long start, long end, long size, Resource resource) {
        this.start = start;
        this.end = end;
        this.size = size;
        this.resource = Optional.ofNullable(resource);
    }

    public static Range parse(String rangeHeader, long fileSize, Resource resource) {
        if (rangeHeader == null || !rangeHeader.startsWith("bytes=")) {
            return new Range(0, fileSize - 1, fileSize, resource);
        }

        try {
            String[] ranges = rangeHeader.substring("bytes=".length()).split("-");
            long start = Long.parseLong(ranges[0]);
            long end = (ranges.length > 1 && !ranges[1].isEmpty()) ? Long.parseLong(ranges[1]) : fileSize - 1;
            return new Range(start, end, fileSize, resource);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid range format", e);
        }
    }

    public String getContentRangeHeader() {
        return "bytes " + start + "-" + end + "/" + size;
    }

    public Resource getResource() {
        return resource.orElseGet(() -> {
            try {
                byte[] data = resource.get().getContentAsByteArray();
                return new ByteArrayResource(data);
            } catch (IOException e) {
                throw new RuntimeException("Failed to get content as byte array", e);
            }
        });
    }
}
