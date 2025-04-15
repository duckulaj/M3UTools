
package com.hawkins.m3utoolsjpa.epg;

import java.util.Objects;

/**
 * Represents a video element in the XMLTV format.
 */
public class XmltvVideo {

    private String quality;

    /**
     * Default constructor.
     */
    public XmltvVideo() {
    }

    /**
     * Constructs an XmltvVideo with the specified quality.
     *
     * @param quality the video quality
     * @throws NullPointerException if quality is null
     */
    public XmltvVideo(String quality) {
        this.quality = Objects.requireNonNull(quality, "quality must not be null");
    }

    /**
     * Returns the video quality.
     *
     * @return the video quality
     */
    public String getQuality() {
        return quality;
    }

    /**
     * Sets the video quality.
     *
     * @param quality the video quality
     * @return the current XmltvVideo instance
     * @throws NullPointerException if quality is null
     */
    public XmltvVideo setQuality(String quality) {
        this.quality = Objects.requireNonNull(quality, "quality must not be null");
        return this;
    }
}
