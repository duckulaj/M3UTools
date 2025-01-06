
package com.hawkins.m3utoolsjpa.epg;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import java.util.Objects;

/**
 * Represents a rating in the XMLTV format.
 */
public class XmltvRating {
    @JacksonXmlProperty(isAttribute = true)
    private String system;

    private String value;

    /**
     * Default constructor.
     */
    public XmltvRating() {
    }

    /**
     * Constructs an XmltvRating with the specified system and value.
     *
     * @param system the rating system
     * @param value the rating value
     * @throws NullPointerException if system or value is null
     */
    public XmltvRating(String system, String value) {
        this.system = Objects.requireNonNull(system, "system must not be null");
        this.value = Objects.requireNonNull(value, "value must not be null");
    }

    /**
     * Returns the rating system.
     *
     * @return the rating system
     */
    public String getSystem() {
        return system;
    }

    /**
     * Sets the rating system.
     *
     * @param system the rating system
     * @return the current XmltvRating instance
     */
    public XmltvRating setSystem(String system) {
        this.system = Objects.requireNonNull(system, "system must not be null");
        return this;
    }

    /**
     * Returns the rating value.
     *
     * @return the rating value
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the rating value.
     *
     * @param value the rating value
     * @return the current XmltvRating instance
     */
    public XmltvRating setValue(String value) {
        this.value = Objects.requireNonNull(value, "value must not be null");
        return this;
    }
}
