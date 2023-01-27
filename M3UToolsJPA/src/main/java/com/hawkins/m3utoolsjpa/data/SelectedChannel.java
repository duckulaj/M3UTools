package com.hawkins.m3utoolsjpa.data;

public class SelectedChannel {

    private long id;

    private String tvgName;

    private boolean selected;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTvgName() {
        return tvgName;
    }

    public void setTvgName(String tvgName) {
        this.tvgName = tvgName;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

}
