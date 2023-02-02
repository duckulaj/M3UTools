package com.hawkins.m3utoolsjpa.data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

@Entity
public class M3UItem {
	
	@GenericGenerator(
	        name = "itemSequenceGenerator",
	        strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
	        parameters = {
	                @Parameter(name = "sequence_name", value = "itemSequence"),
	                @Parameter(name = "initial_value", value = "1"),
	                @Parameter(name = "increment_size", value = "3"),
	                @Parameter(name = "optimizer", value = "hilo")
	        }
	)
	
	@ManyToOne
    @JoinColumn(name="groupId", nullable=false, insertable=false, updatable=false)
    private M3UGroup group;

	@Id
	@GeneratedValue(generator = "itemSequenceGenerator")
	private Long id;
	private String duration;
	private String groupTitle;
	private Long groupId;
	private String tvgId;
	private String tvgName;
	
	@Lob
	private String tvgLogo;
	private String tvgShift;
	private String radio;
			
	@Lob
	private String channelUri;
	
	private String channelName;
	private String type;
	private String search;

	private boolean selected;
	
	protected M3UItem() {}

	public M3UItem(
			String duration,
			String groupTitle,
			Long groupId,
			String tvgId,
			String tvgName,
			String tvgLogo,
			String tvgShift,
			String radio,
			String channelUri,
			String channelName,
			String type,
			String search,
			Boolean selected
			) {

		this.duration = duration;
		this.groupTitle = groupTitle;
		this.groupId = groupId;
		this.tvgId = tvgId;
		this.tvgName = tvgName;
		this.tvgLogo = tvgLogo;
		this.tvgShift = tvgShift;
		this.radio = radio;
		this.channelUri = channelUri;
		this.channelName = channelName;
		this.type = type;
		this.search = search;
		this.selected = selected;

	}

	@Override
	public String toString() {
		return String.format(
				"M3UItem[id=%d, groupId='%s', groupTitle='%s']",
				id, groupId, groupTitle);
	}

	public Long getId() {
		return id;
	}

	public String getDuration() {
		return duration;
	}

	public String getGroupTitle() {
		return groupTitle;
	}
	
	public Long getGroupId() {
		return groupId;
	}
	
	public void setGroupId(Long groupId) {
		this.groupId = groupId;
	}

	public String getTvgId() {
		return tvgId;
	}

	public String getTvgName() {
		return tvgName;
	}

	public String getTvgLogo() {
		return tvgLogo;
	}

	public String getTvgShift() {
		return tvgShift;
	}

	public String getRadio() {
		return radio;
	}

	public String getChannelUri() {
		return channelUri;
	}
	
	public void setChannelUri(String channelUri) {
		this.channelUri = channelUri;
	}

	public String getChannelName() {
		return channelName;
	}

	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
	}

	public String getSearch() {
		return search;
	}
	
	public void setSearch(String search) {
		this.search = search;
	}

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	

}
