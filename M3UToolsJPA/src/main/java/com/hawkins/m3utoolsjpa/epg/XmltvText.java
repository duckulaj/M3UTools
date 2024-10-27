package com.hawkins.m3utoolsjpa.epg;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;

@JsonIgnoreProperties(ignoreUnknown = true)
public class XmltvText {
    @JacksonXmlProperty(isAttribute = true, localName = "lang")
    private String language;

    @JacksonXmlText
    private String text;

    public XmltvText() {
    }

    public XmltvText(String text) {
        this(text, "");
    }

    public XmltvText(String text, String language) {
        this.text = text;
        this.language = language;
    }

    public String getText() {
        return text;
    }

    public XmltvText setText(String text) {
        this.text = text;
        return this;
    }

    public String getLanguage() {
        return language;
    }

    public XmltvText setLanguage(String language) {
        this.language = language;
        return this;
    }
}
