
package com.hawkins.m3utoolsjpa.epg;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.hawkins.m3utoolsjpa.properties.DownloadProperties;
import com.hawkins.m3utoolsjpa.regex.Patterns;
import com.hawkins.m3utoolsjpa.regex.RegexUtils;
import com.hawkins.m3utoolsjpa.utils.StringUtils;
import com.hawkins.m3utoolsjpa.utils.Utils;

@JacksonXmlRootElement(localName = "tv")
public class XmltvDoc {
    @JacksonXmlProperty(isAttribute = true, localName = "generator-info-name")
    private String generatorName;

    @JacksonXmlProperty(isAttribute = true, localName = "generator-info-url")
    private String generatorUrl;

    @JacksonXmlProperty(isAttribute = true, localName = "source-info-url")
    private String sourceInfoUrl;

    @JacksonXmlProperty(isAttribute = true, localName = "source-info-name")
    private String sourceInfoName;

    @JacksonXmlProperty(isAttribute = true, localName = "source-info-logo")
    private String sourceInfoLogo;

    @JacksonXmlProperty(localName = "channel")
    private List<XmltvChannel> channels;

    @JacksonXmlProperty(localName = "programme")
    private List<XmltvProgramme> programmes;

    // private DownloadProperties dp = DownloadProperties.getInstance();

    public XmltvDoc() {
    }

    public XmltvDoc(List<XmltvChannel> channels, List<XmltvProgramme> programmes) {
        this.channels = Objects.requireNonNull(channels, "channels must not be null");
        this.programmes = Objects.requireNonNull(programmes, "programmes must not be null");
    }

    public List<XmltvChannel> getChannels() {
        return Collections.unmodifiableList(channels);
    }

    public XmltvChannel getChannelsByIdAndName(String tvgId, String tvgName) {
        if (this.channels == null || channels.isEmpty()) return null;

        return channels.stream()
            .filter(channel -> tvgId.equalsIgnoreCase(channel.getId()))
            .filter(channel -> tvgName.equals(StringUtils.cleanTextContent(normalisedDisplayName(channel))))
            .parallel()
            .unordered()
            .findFirst()
            .orElse(null);
    }

    private String normalisedDisplayName(XmltvChannel channel) {
        String normalisedName = channel.getDisplayNames().get(0).getText();
        normalisedName = RegexUtils.removeCountryIdentifier(Utils.removeFromString(normalisedName, Patterns.STRIP_COUNTRY_IDENTIFIER), DownloadProperties.getInstance().getIncludedCountries());
        return normalisedName;
    }

    public XmltvDoc setChannels(List<XmltvChannel> channels) {
        this.channels = Collections.unmodifiableList(Objects.requireNonNull(channels, "channels must not be null"));
        return this;
    }

    public List<XmltvProgramme> getProgrammes() {
        return Collections.unmodifiableList(programmes);
    }

    public List<XmltvProgramme> getProgrammesById(String tvgId) {
        if (this.programmes == null) return null;

        return programmes.parallelStream()
            .filter(programme -> Objects.equals(tvgId, programme.getChannel()))
            .collect(Collectors.toList());
    }

    public XmltvDoc setProgrammes(List<XmltvProgramme> programmes) {
        this.programmes = Collections.unmodifiableList(Objects.requireNonNull(programmes, "programmes must not be null"));
        return this;
    }

    public String getGeneratorName() {
        return generatorName;
    }

    public XmltvDoc setGeneratorName(String generatorName) {
        this.generatorName = generatorName;
        return this;
    }

    public String getGeneratorUrl() {
        return generatorUrl;
    }

    public XmltvDoc setGeneratorUrl(String generatorUrl) {
        this.generatorUrl = generatorUrl;
        return this;
    }

    public String getSourceInfoUrl() {
        return sourceInfoUrl;
    }

    public XmltvDoc setSourceInfoUrl(String sourceInfoUrl) {
        this.sourceInfoUrl = sourceInfoUrl;
        return this;
    }

    public String getSourceInfoName() {
        return sourceInfoName;
    }

    public XmltvDoc setSourceInfoName(String sourceInfoName) {
        this.sourceInfoName = sourceInfoName;
        return this;
    }

    public String getSourceInfoLogo() {
        return sourceInfoLogo;
    }

    public XmltvDoc setSourceInfoLogo(String sourceInfoLogo) {
        this.sourceInfoLogo = sourceInfoLogo;
        return this;
    }
}
