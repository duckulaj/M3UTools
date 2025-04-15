
package com.hawkins.m3utoolsjpa.epg;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a TV channel in the XMLTV format.
 */
public class Channel {

    private String displayName;
    private String iconSrc;
    private String id;
    private List<Programme> programmes;

    /**
     * Default constructor.
     */
    public Channel() {
        this.programmes = new ArrayList<>();
    }

    /**
     * Returns the display name of the channel.
     *
     * @return the display name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Sets the display name of the channel.
     *
     * @param displayName the display name
     * @throws NullPointerException if displayName is null
     */
    public void setDisplayName(String displayName) {
        this.displayName = Objects.requireNonNull(displayName, "displayName must not be null");
    }

    /**
     * Returns the icon source of the channel.
     *
     * @return the icon source
     */
    public String getIconSrc() {
        return iconSrc;
    }

    /**
     * Sets the icon source of the channel.
     *
     * @param iconSrc the icon source
     * @throws NullPointerException if iconSrc is null
     */
    public void setIconSrc(String iconSrc) {
        this.iconSrc = Objects.requireNonNull(iconSrc, "iconSrc must not be null");
    }

    /**
     * Returns the ID of the channel.
     *
     * @return the ID
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the ID of the channel.
     *
     * @param id the ID
     * @throws NullPointerException if id is null
     */
    public void setId(String id) {
        this.id = Objects.requireNonNull(id, "id must not be null");
    }

    /**
     * Returns the list of programmes for the channel.
     *
     * @return the list of programmes
     */
    public List<Programme> getProgrammes() {
        return programmes;
    }

    /**
     * Sets the list of programmes for the channel.
     *
     * @param programmes the list of programmes
     * @throws NullPointerException if programmes is null
     */
    public void setProgrammes(List<Programme> programmes) {
        this.programmes = Objects.requireNonNull(programmes, "programmes must not be null");
    }

    /**
     * Adds a programme to the list of programmes for the channel.
     *
     * @param programme the programme to add
     * @throws NullPointerException if programme is null
     */
    public void addProgramme(Programme programme) {
        this.programmes.add(Objects.requireNonNull(programme, "programme must not be null"));
    }
}
