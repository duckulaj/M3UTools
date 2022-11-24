package com.hawkins.m3utoolsjpa.data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

@Entity
public class M3UGroup {

	@GenericGenerator(
	        name = "groupSequenceGenerator",
	        strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
	        parameters = {
	                @Parameter(name = "sequence_name", value = "groupSequence"),
	                @Parameter(name = "initial_value", value = "1"),
	                @Parameter(name = "increment_size", value = "1")
	        }
	)

	@Id
	@GeneratedValue(generator = "groupSequenceGenerator")
	private Long id;
	private String name;
	private String type;

	protected M3UGroup() {}

	public M3UGroup(
			String name,
			String type
			) {

		this.name = name;
		this.type = type;


	}
	
	

	@Override
	public String toString() {
		return String.format(
				"M3UGroup[id=%d, name='%s', type='%s']",
				id, name, type);
	}

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

}
