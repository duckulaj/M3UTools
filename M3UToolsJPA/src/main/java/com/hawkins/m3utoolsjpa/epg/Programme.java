
package com.hawkins.m3utoolsjpa.epg;

import java.util.Objects;

/**
 * Represents a TV programme in the XMLTV format.
 */
public class Programme {

    private String title;
    private String lang;
    private String desc;
    private String date;
    private String start;
    private String stop;
    private String channel;

    /**
     * Default constructor.
     */
    public Programme() {
    }

    /**
     * Returns the title of the programme.
     *
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the title of the programme.
     *
     * @param title the title
     * @throws NullPointerException if title is null
     */
    public void setTitle(String title) {
        this.title = Objects.requireNonNull(title, "title must not be null");
    }

    /**
     * Returns the language of the programme.
     *
     * @return the language
     */
    public String getLang() {
        return lang;
    }

    /**
     * Sets the language of the programme.
     *
     * @param lang the language
     * @throws NullPointerException if lang is null
     */
    public void setLang(String lang) {
        this.lang = Objects.requireNonNull(lang, "lang must not be null");
    }

    /**
     * Returns the description of the programme.
     *
     * @return the description
     */
    public String getDesc() {
        return desc;
    }

    /**
     * Sets the description of the programme.
     *
     * @param desc the description
     * @throws NullPointerException if desc is null
     */
    public void setDesc(String desc) {
        this.desc = Objects.requireNonNull(desc, "desc must not be null");
    }

    /**
     * Returns the date of the programme.
     *
     * @return the date
     */
    public String getDate() {
        return date;
    }

    /**
     * Sets the date of the programme.
     *
     * @param date the date
     * @throws NullPointerException if date is null
     */
    public void setDate(String date) {
        this.date = Objects.requireNonNull(date, "date must not be null");
    }

    /**
     * Returns the start time of the programme.
     *
     * @return the start time
     */
    public String getStart() {
        return start;
    }

    /**
     * Sets the start time of the programme.
     *
     * @param start the start time
     * @throws NullPointerException if start is null
     */
    public void setStart(String start) {
        this.start = Objects.requireNonNull(start, "start must not be null");
    }

    /**
     * Returns the stop time of the programme.
     *
     * @return the stop time
     */
    public String getStop() {
        return stop;
    }

    /**
     * Sets the stop time of the programme.
     *
     * @param stop the stop time
     * @throws NullPointerException if stop is null
     */
    public void setStop(String stop) {
        this.stop = Objects.requireNonNull(stop, "stop must not be null");
    }

    /**
     * Returns the channel of the programme.
     *
     * @return the channel
     */
    public String getChannel() {
        return channel;
    }

    /**
     * Sets the channel of the programme.
     *
     * @param channel the channel
     * @throws NullPointerException if channel is null
     */
    public void setChannel(String channel) {
        this.channel = Objects.requireNonNull(channel, "channel must not be null");
    }
}
