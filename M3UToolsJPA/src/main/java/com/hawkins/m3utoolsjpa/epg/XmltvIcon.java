package com.hawkins.m3utoolsjpa.epg;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public class XmltvIcon {
    @JacksonXmlProperty(isAttribute = true)
    String src;

    @JacksonXmlProperty(isAttribute = true)
    String height;
    
    @JacksonXmlProperty(isAttribute = true)
    String width;
    
    public XmltvIcon() {
    }

    public XmltvIcon(String src) {
        this.src = src;
    }

    public String getSrc() {
        return src;
    }

    public XmltvIcon setSrc(String src) {
        this.src = src;
        return this;
    }
    
    public String getHeight() {
        return height;
    }

    public XmltvIcon setHeight(String height) {
        this.height = height;
        return this;
    }

    public String getWidth() {
        return width;
    }

    public XmltvIcon setWidth(String width) {
        this.width = width;
        return this;
    }

}
