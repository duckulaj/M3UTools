package com.hawkins.m3utoolsjpa.xtream;

public class Episode {
    private String id;
    private String title;
    private String container_extension;
    private String plot;
    private String airdate;
    private String duration;
    private String direct_source;
    private String season;

    public Episode() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContainer_extension() { return container_extension; }
    public void setContainer_extension(String container_extension) { this.container_extension = container_extension; }

    public String getPlot() { return plot; }
    public void setPlot(String plot) { this.plot = plot; }

    public String getAirdate() { return airdate; }
    public void setAirdate(String airdate) { this.airdate = airdate; }

    public String getDuration() { return duration; }
    public void setDuration(String duration) { this.duration = duration; }

    public String getDirect_source() { return direct_source; }
    public void setDirect_source(String direct_source) { this.direct_source = direct_source; }

    public String getSeason() { return season; }
    public void setSeason(String season) { this.season = season; }
}