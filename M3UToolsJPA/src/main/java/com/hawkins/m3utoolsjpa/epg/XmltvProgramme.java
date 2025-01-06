
package com.hawkins.m3utoolsjpa.epg;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.util.Objects;
import java.util.Optional;

/**
 * Represents a programme in the XMLTV format.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class XmltvProgramme {

    @JacksonXmlProperty(isAttribute = true)
    private String channel;

    @JacksonXmlProperty(isAttribute = true)
    private String start;

    @JacksonXmlProperty(isAttribute = true)
    private String stop;

    private XmltvText category;

    private XmltvText title;

    private XmltvText desc;

    private XmltvRating rating;

    private XmltvIcon icon;

    private String credits;

    private XmltvVideo video;

    private String date;

    /**
     * Default constructor.
     */
    public XmltvProgramme() {
    }

    /**
     * Constructs an XmltvProgramme with the specified channel, start, and stop times.
     *
     * @param channel the channel id
     * @param start the start time
     * @param stop the stop time
     * @throws NullPointerException if any of the parameters are null
     */
    public XmltvProgramme(String channel, String start, String stop) {
        this.channel = Objects.requireNonNull(channel, "channel must not be null");
        this.start = Objects.requireNonNull(start, "start must not be null");
        this.stop = Objects.requireNonNull(stop, "stop must not be null");
    }

    /**
     * Copy constructor.
     *
     * @param p the XmltvProgramme to copy
     */
    public XmltvProgramme(XmltvProgramme p) {
        this(p.channel, p.start, p.stop);
        this.category = p.category;
        this.title = p.title;
        this.desc = p.desc;
        this.rating = p.rating;
        this.icon = p.icon;
        this.credits = p.credits;
        this.video = p.video;
        this.date = p.date;
    }

    /**
     * Creates a copy of this XmltvProgramme.
     *
     * @return a new XmltvProgramme instance with the same properties
     */
    public XmltvProgramme copy() {
        return new XmltvProgramme(this);
    }

    /**
     * Returns the channel id.
     *
     * @return the channel id
     */
    public String getChannel() {
        return channel;
    }

    /**
     * Sets the channel id.
     *
     * @param channel the channel id
     * @return the current XmltvProgramme instance
     */
    public XmltvProgramme setChannel(String channel) {
        this.channel = Objects.requireNonNull(channel, "channel must not be null");
        return this;
    }

    /**
     * Returns the start time.
     *
     * @return the start time
     */
    public String getStart() {
        return start;
    }

    /**
     * Sets the start time.
     *
     * @param start the start time
     * @return the current XmltvProgramme instance
     */
    public XmltvProgramme setStart(String start) {
        this.start = Objects.requireNonNull(start, "start must not be null");
        return this;
    }

    /**
     * Returns the stop time.
     *
     * @return the stop time
     */
    public String getStop() {
        return stop;
    }

    /**
     * Sets the stop time.
     *
     * @param stop the stop time
     * @return the current XmltvProgramme instance
     */
    public XmltvProgramme setStop(String stop) {
        this.stop = Objects.requireNonNull(stop, "stop must not be null");
        return this;
    }

    /**
     * Returns the category.
     *
     * @return an Optional containing the category, or an empty Optional if not set
     */
    public Optional<XmltvText> getCategory() {
        return Optional.ofNullable(category);
    }

    /**
     * Sets the category.
     *
     * @param category the category
     * @return the current XmltvProgramme instance
     */
    public XmltvProgramme setCategory(XmltvText category) {
        this.category = category;
        return this;
    }

    /**
     * Returns the title.
     *
     * @return an Optional containing the title, or an empty Optional if not set
     */
    public Optional<XmltvText> getTitle() {
        return Optional.ofNullable(title);
    }

    /**
     * Sets the title.
     *
     * @param title the title
     * @return the current XmltvProgramme instance
     */
    public XmltvProgramme setTitle(XmltvText title) {
        this.title = title;
        return this;
    }

    /**
     * Returns the description.
     *
     * @return an Optional containing the description, or an empty Optional if not set
     */
    public Optional<XmltvText> getDesc() {
        return Optional.ofNullable(desc);
    }

    /**
     * Sets the description.
     *
     * @param desc the description
     * @return the current XmltvProgramme instance
     */
    public XmltvProgramme setDesc(XmltvText desc) {
        this.desc = desc;
        return this;
    }

    /**
     * Returns the rating.
     *
     * @return an Optional containing the rating, or an empty Optional if not set
     */
    public Optional<XmltvRating> getRating() {
        return Optional.ofNullable(rating);
    }

    /**
     * Sets the rating.
     *
     * @param rating the rating
     * @return the current XmltvProgramme instance
     */
    public XmltvProgramme setRating(XmltvRating rating) {
        this.rating = rating;
        return this;
    }

    /**
     * Returns the icon.
     *
     * @return an Optional containing the icon, or an empty Optional if not set
     */
    public Optional<XmltvIcon> getIcon() {
        return Optional.ofNullable(icon);
    }

    /**
     * Sets the icon.
     *
     * @param icon the icon
     * @return the current XmltvProgramme instance
     */
    public XmltvProgramme setIcon(XmltvIcon icon) {
        this.icon = icon;
        return this;
    }

    /**
     * Returns the credits.
     *
     * @return an Optional containing the credits, or an empty Optional if not set
     */
    public Optional<String> getCredits() {
        return Optional.ofNullable(credits);
    }

    /**
     * Sets the credits.
     *
     * @param credits the credits
     * @return the current XmltvProgramme instance
     */
    public XmltvProgramme setCredits(String credits) {
        this.credits = credits;
        return this;
    }

    /**
     * Returns the date.
     *
     * @return an Optional containing the date, or an empty Optional if not set
     */
    public Optional<String> getDate() {
        return Optional.ofNullable(date);
    }

    /**
     * Sets the date.
     *
     * @param date the date
     * @return the current XmltvProgramme instance
     */
    public XmltvProgramme setDate(String date) {
        this.date = date;
        return this;
    }

    /**
     * Returns the video information.
     *
     * @return an Optional containing the video information, or an empty Optional if not set
     */
    public Optional<XmltvVideo> getVideo() {
        return Optional.ofNullable(video);
    }

    /**
     * Sets the video information.
     *
     * @param video the video information
     * @return the current XmltvProgramme instance
     */
    public XmltvProgramme setVideo(XmltvVideo video) {
        this.video = video;
        return this;
    }
}
