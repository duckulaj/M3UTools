package com.hawkins.m3utoolsjpa.data;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

@Data
@Entity
public class XtreamCategory {

	@Id
	@JsonProperty("category_id")
	private Long categoryId;
	@JsonProperty("category_name")
	private String categoryName;
	@JsonProperty("parent_id")
	private String parentId;
	private String type;
	
	public XtreamCategory() {
		// Default constructor
	}
	
	public XtreamCategory(Long category_id, String category_name, String parent_id, String type) {
		this.categoryId = category_id;
		this.categoryName = category_name;
		this.parentId = parent_id;
		this.type = type;
	}
}
