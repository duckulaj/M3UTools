package com.hawkins.m3utoolsjpa.data;

import java.util.Objects;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;

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
	@JsonProperty("category_name")
	private String name;
	private String type;
	@JsonProperty("category_id")
	private String categoryid;

	protected M3UGroup() {}

	public M3UGroup(
			String name,
			String type,
			String categoryid
			) {

		this.name = name;
		this.type = type;
		this.categoryid = categoryid;

	}
	
	

	@Override
	public String toString() {
		return "M3UGroup[id=%d, name='%s', type='%s', externalId='%s']".formatted(
                id, name, type, categoryid);
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
	
	public String getCategoryId() {
		return categoryid;
	}
	
	public void setCategoryId(String externalId) {
		this.categoryid = externalId;
	}

	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        M3UGroup m3UGroup = (M3UGroup) o;
        return Objects.equals(name, m3UGroup.name) && Objects.equals(type, m3UGroup.type) && Objects.equals(categoryid, m3UGroup.categoryid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, type, categoryid);
    }
}
