
package com.hawkins.m3utoolsjpa.epg;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

/**
 * Represents a channel in the XMLTV format.
 */
public class XmltvChannel {
    @JacksonXmlProperty(isAttribute = true, localName = "id")
    private String id;

    @JacksonXmlProperty(localName = "display-name")
    private List<XmltvText> displayNames;

    @JacksonXmlProperty(localName = "icon")
    private XmltvIcon icon;

    /**
     * Default constructor.
     */
    public XmltvChannel() {
    }

    /**
     * Constructs an XmltvChannel with the specified id, display names, and icon.
     *
     * @param id the channel id
     * @param displayNames the list of display names
     * @param icon the channel icon
     * @throws NullPointerException if any of the parameters are null
     */
    public XmltvChannel(String id, List<XmltvText> displayNames, XmltvIcon icon) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.displayNames = Objects.requireNonNull(displayNames, "displayNames must not be null");
        this.icon = Objects.requireNonNull(icon, "icon must not be null");
    }

    /**
     * Constructs an XmltvChannel with the specified id, display name, and icon.
     *
     * @param id the channel id
     * @param displayName the display name
     * @param icon the channel icon
     * @throws NullPointerException if any of the parameters are null
     */
    public XmltvChannel(String id, XmltvText displayName, XmltvIcon icon) {
        this(id, List.of(Objects.requireNonNull(displayName, "displayName must not be null")), icon);
    }

    /**
     * Returns the channel id.
     *
     * @return the channel id
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the channel id.
     *
     * @param id the channel id
     * @return the current XmltvChannel instance
     */
    public XmltvChannel setId(String id) {
        this.id = id;
        return this;
    }

    /**
     * Returns the list of display names.
     *
     * @return an unmodifiable list of display names
     */
    public List<XmltvText> getDisplayNames() {
        return Collections.unmodifiableList(displayNames);
    }

    /**
     * Sets the list of display names.
     *
     * @param displayNames the list of display names
     * @return the current XmltvChannel instance
     */
    public XmltvChannel setDisplayNames(List<XmltvText> displayNames) {
        this.displayNames = displayNames;
        return this;
    }

    /**
     * Returns the channel icon.
     *
     * @return the channel icon
     */
    public XmltvIcon getIcon() {
        return icon;
    }

    /**
     * Sets the channel icon.
     *
     * @param icon the channel icon
     * @return the current XmltvChannel instance
     */
    public XmltvChannel setIcon(XmltvIcon icon) {
        this.icon = icon;
        return this;
    }
}
