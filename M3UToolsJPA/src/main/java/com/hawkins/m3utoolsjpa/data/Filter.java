package com.hawkins.m3utoolsjpa.data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

@Entity
public class Filter {

	@GenericGenerator(
	        name = "filterSequenceGenerator",
	        strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
	        parameters = {
	                @Parameter(name = "sequence_name", value = "filterSequence"),
	                @Parameter(name = "initial_value", value = "1"),
	                @Parameter(name = "increment_size", value = "1")
	        }
	)

	@Id
	@GeneratedValue(generator = "itemSequenceGenerator")
	private Long Id;
	private String name;
	private String description;
	private Long groupId;
	private String include;
	private String exclude;
		
	protected Filter() {}

	public Filter(
			Long id, 
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
