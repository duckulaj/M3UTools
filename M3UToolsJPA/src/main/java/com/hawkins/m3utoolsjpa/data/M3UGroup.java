package com.hawkins.m3utoolsjpa.data;

import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

@Entity
public class M3UGroup {

	
	@OneToMany(mappedBy = "group")
	private Set<M3UItem> items;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	@Column(name = "id", nullable = false)
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
		return "M3UGroup[id=%d, name='%s', type='%s']".formatted(
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
