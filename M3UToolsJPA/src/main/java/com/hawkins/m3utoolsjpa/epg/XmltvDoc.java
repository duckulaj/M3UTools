package com.hawkins.m3utoolsjpa.epg;

import java.util.List;
import java.util.NoSuchElementException;
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
    
    private DownloadProperties dp = DownloadProperties.getInstance();

    public XmltvDoc() {
    }

    public XmltvDoc(List<XmltvChannel> channels, List<XmltvProgramme> programmes) {
        this.channels = channels;
        this.programmes = programmes;
    }

    public List<XmltvChannel> getChannels() {
        return channels;
    }
    
    public XmltvChannel getChannelsByIdAndName(String tvgId, String tvgName) {
    	
    	if (this.channels == null || channels.isEmpty()) return null;
    	
    	try {
	    	XmltvChannel selectedChannel = channels.stream()
	    			.filter(channel -> tvgId.equalsIgnoreCase(channel.getId()))
	    			.filter(channel -> tvgName.equals(StringUtils.cleanTextContent(normalisedDisplayName(channel))))
	    			.parallel()
	    			.unordered()
	    			.findFirst()
	    			.get();
	    	
	    	return selectedChannel;
	    	
    	} catch (NoSuchElementException nsee) {
    		
    		return null;
    	}
    			
        
    }
    
    private String normalisedDisplayName(XmltvChannel channel) {
    	
    	String normalisedName = channel.getDisplayNames().get(0).getText();
    	// normalisedName = StringUtils.removeCountryIdentifierUsingRegExpr(normalisedName, DownloadProperties.getInstance().getCountryRegExpr());
    	normalisedName = RegexUtils.removeCountryIdentifier(Utils.removeFromString(normalisedName, Patterns.STRIP_COUNTRY_IDENTIFIER),dp.getIncludedCountries());
    	return normalisedName;
    }

    public XmltvDoc setChannels(List<XmltvChannel> channels) {
        this.channels = channels;
        return this;
    }

    public List<XmltvProgramme> getProgrammes() {
        return programmes;
    }
    
    public List<XmltvProgramme> getProgrammesById(String tvgId) {
    	
    	if (this.programmes == null) return null;
    	
    	List<XmltvProgramme> selectedProgrammes = programmes.parallelStream()
    		    .filter(programme -> Objects.equals(tvgId, programme.getChannel()))
    		    .collect(Collectors.toList());		
    			
        return selectedProgrammes;
    }


    public XmltvDoc setProgrammes(List<XmltvProgramme> programmes) {
        this.programmes = programmes;
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
