package com.hawkins.m3utoolsjpa.xtream;

public class VODCategory {
    private String category_id;
    private String category_name;
    private String parent_id;

    public VODCategory() {}

    public String getCategory_id() {
        return category_id;
    }

    public void setCategory_id(String category_id) {
        this.category_id = category_id;
    }

    public String getCategory_name() {
        return category_name;
    }

    public void setCategory_name(String category_name) {
        this.category_name = category_name;
    }

    public String getParent_id() {
        return parent_id;
    }

    public void setParent_id(String parent_id) {
        this.parent_id = parent_id;
    }

    @Override
    public String toString() {
        return "VODCategory{" +
                "category_id='" + category_id + '\'' +
                ", category_name='" + category_name + '\'' +
                ", parent_id='" + parent_id + '\'' +
                '}';
    }
}
