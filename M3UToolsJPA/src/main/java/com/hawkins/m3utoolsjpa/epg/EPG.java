
package com.hawkins.m3utoolsjpa.epg;

import java.util.List;
import java.util.Objects;

/**
 * Represents an Electronic Program Guide (EPG) containing a list of channels.
 */
public class EPG {

    private List<Channel> channels;

    /**
     * Constructs an EPG with the specified list of channels.
     *
     * @param channels the list of channels
     * @throws NullPointerException if channels is null
     */
    public EPG(List<Channel> channels) {
        this.channels = Objects.requireNonNull(channels, "channels must not be null");
    }

    /**
     * Returns the list of channels.
     *
     * @return the list of channels
     */
    public List<Channel> getChannels() {
        return channels;
    }

    /**
     * Sets the list of channels.
     *
     * @param channels the list of channels
     * @throws NullPointerException if channels is null
     */
    public void setChannels(List<Channel> channels) {
        this.channels = Objects.requireNonNull(channels, "channels must not be null");
    }
}
