package com.hawkins.m3utoolsjpa.m3u;

public class M3UGenre {

	public String id;
	public String name;

	public M3UGenre() {
		
	}
	
	public M3UGenre(String id, String name) {
		super();
		this.id = id;
		this.name = name;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	
}
