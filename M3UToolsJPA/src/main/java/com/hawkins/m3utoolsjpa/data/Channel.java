package com.hawkins.m3utoolsjpa.data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;


@Entity
public class Channel {

	@GenericGenerator(
	        name = "channelSequenceGenerator",
	        strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
	        parameters = {
	                @Parameter(name = "sequence_name", value = "channelSequence"),
	                @Parameter(name = "initial_value", value = "1000"),
	                @Parameter(name = "increment_size", value = "1")
	        }
	)

	@Id
	@GeneratedValue(generator = "channelSequenceGenerator")
	private Long Id;
	
	private String name;
	private Long groupId;
	private boolean selected;
		
	protected Channel() {}

	public Channel(
			String name,
			Long groupId,
			boolean selected
			) {
	
		this.name = name;
		this.groupId = groupId;
		this.selected = selected;
	}

	public String getName() {
		return name;
	}

	public Long getId() {
		return Id;
	}

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	public Long getGroupId() {
		return groupId;
	}
	
	
}
