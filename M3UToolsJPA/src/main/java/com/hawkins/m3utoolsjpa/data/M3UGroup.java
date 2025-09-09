package com.hawkins.m3utoolsjpa.data;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class M3UGroup {

	public M3UGroup() {
		// Default constructor
	}
	
	public M3UGroup(Long id, String name, String type, String categoryid) {
		this.id = id;
		this.name = name;
		this.type = type;
		this.categoryid = categoryid;
	}
	
	public M3UGroup(String name, String type, String categoryid) {
        this.name = name;
        this.type = type;
        this.categoryid = categoryid;
    }
	
	

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	@Column(name = "id", nullable = false)
	private Long id;

	@jakarta.persistence.Version
	private Long version;

	private String name;
	private String type;
	private String categoryid;

}