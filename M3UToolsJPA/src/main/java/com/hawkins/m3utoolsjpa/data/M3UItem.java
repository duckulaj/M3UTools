package com.hawkins.m3utoolsjpa.data;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "M3UITEM", indexes = {@Index(name = "IDX_M3UGROUP_ID", columnList = "groupId" ), 
		@Index(name = "IDX_M3UITEM_TVGID", columnList = "tvgId")}
)
public class M3UItem {

	
	
	
	@ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="groupId", nullable=false, insertable=false, updatable=false)
    private M3UGroup group;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	@Column(name = "id", nullable = false)
	private Long id;
	private String duration;
	private String groupTitle;
	private Long groupId;
	private String tvgId;
	private String tvgName;
	private String tvgChNo;
	
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
			String tvgChNo,
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
		this.tvgChNo = tvgChNo;
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
		return "M3UItem [group=" + group + ", id=" + id + ", duration=" + duration + ", groupTitle=" + groupTitle
				+ ", groupId=" + groupId + ", tvgId=" + tvgId + ", tvgName=" + tvgName + ", tvgChNo=" + tvgChNo
				+ ", tvgLogo=" + tvgLogo + ", tvgShift=" + tvgShift + ", radio=" + radio + ", channelUri=" + channelUri
				+ ", channelName=" + channelName + ", type=" + type + ", search=" + search + ", selected=" + selected
				+ "]";
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
	
	public String getTvgChNo() {
		return tvgChNo;
	}

	public void setTvgChNo(String tvgChNo) {
		this.tvgChNo = tvgChNo;
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

	public void setChannelName(String channelName) {
		this.channelName = channelName;
	}

	public void setTvgName(String tvgName) {
		this.tvgName = tvgName;
	}

	

}
