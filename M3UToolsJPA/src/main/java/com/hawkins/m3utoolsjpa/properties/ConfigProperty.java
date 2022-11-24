package com.hawkins.m3utoolsjpa.properties;

public class ConfigProperty {
	
	public String key;
	public String value;

	public ConfigProperty() {
		
	}
	public ConfigProperty (String key, String value) {
		this.key = key;
		this.value=value;
	}
	
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	
	@Override
	public String toString() {
		return "ConfigProperty [key=" + key + ", value=" + value + "]";
	}
	
}
