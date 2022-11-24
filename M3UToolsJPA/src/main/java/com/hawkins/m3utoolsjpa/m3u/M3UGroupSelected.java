package com.hawkins.m3utoolsjpa.m3u;

public class M3UGroupSelected {

	private Long id;
	private String name;
	private String type;

	public M3UGroupSelected() {
		
	}
	
	public M3UGroupSelected(
			Long id,
			String name,
			String type
			) {

		this.id = id;
		this.name = name;
		this.type = type;
	}

	public Long getId() {
		return id;
	}
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
	}

}
