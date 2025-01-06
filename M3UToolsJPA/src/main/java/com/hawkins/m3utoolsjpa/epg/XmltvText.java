
package com.hawkins.m3utoolsjpa.epg;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;

import java.util.Objects;

/**
 * Represents a text element in the XMLTV format.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class XmltvText {
    @JacksonXmlProperty(isAttribute = true, localName = "lang")
    private String language;

    @JacksonXmlText
    private String text;

    /**
     * Default constructor.
     */
    public XmltvText() {
    }

    /**
     * Constructs an XmltvText with the specified text.
     *
     * @param text the text content
     * @throws NullPointerException if text is null
     */
    public XmltvText(String text) {
        this(text, "");
    }

    /**
     * Constructs an XmltvText with the specified text and language.
     *
     * @param text the text content
     * @param language the language of the text
     * @throws NullPointerException if text is null
     */
    public XmltvText(String text, String language) {
        this.text = Objects.requireNonNull(text, "text must not be null");
        this.language = language;
    }

    /**
     * Returns the text content.
     *
     * @return the text content
     */
    public String getText() {
        return text;
    }

    /**
     * Sets the text content.
     *
     * @param text the text content
     * @return the current XmltvText instance
     * @throws NullPointerException if text is null
     */
    public XmltvText setText(String text) {
        this.text = Objects.requireNonNull(text, "text must not be null");
        return this;
    }

    /**
     * Returns the language of the text.
     *
     * @return the language of the text
     */
    public String getLanguage() {
        return language;
    }

    /**
     * Sets the language of the text.
     *
     * @param language the language of the text
     * @return the current XmltvText instance
     */
    public XmltvText setLanguage(String language) {
        this.language = language;
        return this;
    }
}
