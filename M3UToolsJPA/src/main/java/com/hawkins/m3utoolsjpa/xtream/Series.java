package com.hawkins.m3utoolsjpa.xtream;

import java.util.List;

public class Series {
    private int num;
    private String name;
    private int series_id;
    private String cover;
    private String plot;
    private String cast;
    private String director;
    private String genre;
    private String releaseDate;
    private String release_date;
    private String last_modified;
    private String rating;
    private String rating_5based;
    private List<String> backdrop_path;
    private String youtube_trailer;
    private String tmdb;
    private String episode_run_time;
    private String category_id;
    private List<Integer> category_ids;

    public Series() {}

    // Getters and setters
    public int getNum() { return num; }
    public void setNum(int num) { this.num = num; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getSeries_id() { return series_id; }
    public void setSeries_id(int series_id) { this.series_id = series_id; }

    public String getCover() { return cover; }
    public void setCover(String cover) { this.cover = cover; }

    public String getPlot() { return plot; }
    public void setPlot(String plot) { this.plot = plot; }

    public String getCast() { return cast; }
    public void setCast(String cast) { this.cast = cast; }

    public String getDirector() { return director; }
    public void setDirector(String director) { this.director = director; }

    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }

    public String getReleaseDate() { return releaseDate; }
    public void setReleaseDate(String releaseDate) { this.releaseDate = releaseDate; }

    public String getRelease_date() { return release_date; }
    public void setRelease_date(String release_date) { this.release_date = release_date; }

    public String getLast_modified() { return last_modified; }
    public void setLast_modified(String last_modified) { this.last_modified = last_modified; }

    public String getRating() { return rating; }
    public void setRating(String rating) { this.rating = rating; }

    public String getRating_5based() { return rating_5based; }
    public void setRating_5based(String rating_5based) { this.rating_5based = rating_5based; }

    public List<String> getBackdrop_path() { return backdrop_path; }
    public void setBackdrop_path(List<String> backdrop_path) { this.backdrop_path = backdrop_path; }

    public String getYoutube_trailer() { return youtube_trailer; }
    public void setYoutube_trailer(String youtube_trailer) { this.youtube_trailer = youtube_trailer; }

    public String getTmdb() { return tmdb; }
    public void setTmdb(String tmdb) { this.tmdb = tmdb; }

    public String getEpisode_run_time() { return episode_run_time; }
    public void setEpisode_run_time(String episode_run_time) { this.episode_run_time = episode_run_time; }

    public String getCategory_id() { return category_id; }
    public void setCategory_id(String category_id) { this.category_id = category_id; }

    public List<Integer> getCategory_ids() { return category_ids; }
    public void setCategory_ids(List<Integer> category_ids) { this.category_ids = category_ids; }
}
