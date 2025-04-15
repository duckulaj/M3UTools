package com.hawkins.m3utoolsjpa.data;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;


@Entity
public class Filter {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	@Column(name = "id", nullable = false)
	private Long Id;
	
	@NotBlank(message = "Name is mandatory")
	private String name;
	private String description;
	
	@NotNull(message = "Group is mandatory")
	private Long groupId;
	private String include;
	private String exclude;
		
	protected Filter() {}

	public Filter(
			String name, 
			String description, 
			Long groupId, 
			String include, 
			String exclude
			) {
	
		this.name = name;
		this.description = description;
		this.groupId = groupId;
		this.include = include;
		this.exclude = exclude;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Long getGroupId() {
		return groupId;
	}

	public void setGroupId(Long groupId) {
		this.groupId = groupId;
	}

	public String getInclude() {
		return include;
	}

	public void setInclude(String include) {
		this.include = include;
	}

	public String getExclude() {
		return exclude;
	}

	public void setExclude(String exclude) {
		this.exclude = exclude;
	}

	public Long getId() {
		return Id;
	}
	
	
}
