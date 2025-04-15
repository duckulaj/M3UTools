
package com.hawkins.m3utoolsjpa.epg;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a collection of TV programmes.
 */
public class Programmes {

    private List<Programme> programmes;

    /**
     * Default constructor.
     * Initializes the programmes list.
     */
    public Programmes() {
        this.programmes = new ArrayList<>();
    }

    /**
     * Returns the list of programmes.
     *
     * @return the list of programmes
     */
    public List<Programme> getProgrammes() {
        return programmes;
    }

    /**
     * Sets the list of programmes.
     *
     * @param programmes the list of programmes
     * @throws NullPointerException if programmes is null
     */
    public void setProgrammes(List<Programme> programmes) {
        this.programmes = Objects.requireNonNull(programmes, "programmes must not be null");
    }

    /**
     * Adds a programme to the list of programmes.
     *
     * @param programme the programme to add
     * @throws NullPointerException if programme is null
     */
    public void addProgramme(Programme programme) {
        this.programmes.add(Objects.requireNonNull(programme, "programme must not be null"));
    }

    /**
     * Returns the list of programmes for the specified channel.
     *
     * @param channel the channel
     * @return the list of programmes for the specified channel
     * @throws NullPointerException if channel is null
     */
    public List<Programme> getProgrammesByChannel(String channel) {
        Objects.requireNonNull(channel, "channel must not be null");
        List<Programme> channelProgrammes = new ArrayList<>();

        for (Programme programme : programmes) {
            if (programme.getChannel().equals(channel)) {
                channelProgrammes.add(programme);
            }
        }

        return channelProgrammes;
    }
}
