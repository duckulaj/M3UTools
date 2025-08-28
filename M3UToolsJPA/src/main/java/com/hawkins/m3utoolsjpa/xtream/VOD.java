package com.hawkins.m3utoolsjpa.xtream;

import java.util.List;

public class VOD {
    private int num;
    private String name;
    private String stream_type;
    private int stream_id;
    private String stream_icon;
    private String rating;
    private double rating_5based;
    private String tmdb;
    private String trailer;
    private String added;
    private int is_adult;
    private String category_id;
    private List<Integer> category_ids;
    private String container_extension;
    private String custom_sid;
    private String direct_source;

    public VOD() {}

    // Getters and setters
    public int getNum() { return num; }
    public void setNum(int num) { this.num = num; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getStream_type() { return stream_type; }
    public void setStream_type(String stream_type) { this.stream_type = stream_type; }

    public int getStream_id() { return stream_id; }
    public void setStream_id(int stream_id) { this.stream_id = stream_id; }

    public String getStream_icon() { return stream_icon; }
    public void setStream_icon(String stream_icon) { this.stream_icon = stream_icon; }

    public String getRating() { return rating; }
    public void setRating(String rating) { this.rating = rating; }

    public double getRating_5based() { return rating_5based; }
    public void setRating_5based(double rating_5based) { this.rating_5based = rating_5based; }

    public String getTmdb() { return tmdb; }
    public void setTmdb(String tmdb) { this.tmdb = tmdb; }

    public String getTrailer() { return trailer; }
    public void setTrailer(String trailer) { this.trailer = trailer; }

    public String getAdded() { return added; }
    public void setAdded(String added) { this.added = added; }

    public int getIs_adult() { return is_adult; }
    public void setIs_adult(int is_adult) { this.is_adult = is_adult; }

    public String getCategory_id() { return category_id; }
    public void setCategory_id(String category_id) { this.category_id = category_id; }

    public List<Integer> getCategory_ids() { return category_ids; }
    public void setCategory_ids(List<Integer> category_ids) { this.category_ids = category_ids; }

    public String getContainer_extension() { return container_extension; }
    public void setContainer_extension(String container_extension) { this.container_extension = container_extension; }

    public String getCustom_sid() { return custom_sid; }
    public void setCustom_sid(String custom_sid) { this.custom_sid = custom_sid; }

    public String getDirect_source() { return direct_source; }
    public void setDirect_source(String direct_source) { this.direct_source = direct_source; }
}
