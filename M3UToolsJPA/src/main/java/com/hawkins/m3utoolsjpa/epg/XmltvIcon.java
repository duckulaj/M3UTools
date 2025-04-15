
package com.hawkins.m3utoolsjpa.epg;

import java.util.Objects;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

/**
 * Represents an icon in the XMLTV format.
 */
public class XmltvIcon {
    @JacksonXmlProperty(isAttribute = true)
    private String src;

    @JacksonXmlProperty(isAttribute = true)
    private String height;

    @JacksonXmlProperty(isAttribute = true)
    private String width;

    /**
     * Constructs an XmltvIcon with the specified source, height, and width.
     *
     * @param src the source URL of the icon
     * @param height the height of the icon
     * @param width the width of the icon
     * @throws NullPointerException if src is null
     */
    public XmltvIcon(String src, String height, String width) {
        this.src = Objects.requireNonNull(src, "src must not be null");
        this.height = height;
        this.width = width;
    }

    /**
     * Default constructor.
     */
    public XmltvIcon() {
    }

    /**
     * Constructs an XmltvIcon with the specified source.
     *
     * @param src the source URL of the icon
     * @throws NullPointerException if src is null
     */
    public XmltvIcon(String src) {
        this.src = Objects.requireNonNull(src, "src must not be null");
    }

    /**
     * Returns the source URL of the icon.
     *
     * @return the source URL of the icon
     */
    public String getSrc() {
        return src;
    }

    /**
     * Sets the source URL of the icon.
     *
     * @param src the source URL of the icon
     * @return the current XmltvIcon instance
     */
    public XmltvIcon setSrc(String src) {
        this.src = src;
        return this;
    }

    /**
     * Returns the height of the icon.
     *
     * @return the height of the icon
     */
    public String getHeight() {
        return height;
    }

    /**
     * Sets the height of the icon.
     *
     * @param height the height of the icon
     * @return the current XmltvIcon instance
     */
    public XmltvIcon setHeight(String height) {
        this.height = height;
        return this;
    }

    /**
     * Returns the width of the icon.
     *
     * @return the width of the icon
     */
    public String getWidth() {
        return width;
    }

    /**
     * Sets the width of the icon.
     *
     * @param width the width of the icon
     * @return the current XmltvIcon instance
     */
    public XmltvIcon setWidth(String width) {
        this.width = width;
        return this;
    }
}
