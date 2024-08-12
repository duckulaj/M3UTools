package com.hawkins.m3utoolsjpa.data;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

@Entity
@Table(name = "TVCHANNEL", 
	indexes = {@Index(name = "IDX_TVCHANNEL_GROUPID", columnList = "groupId"),
			@Index(name = "IDX_TVCHANNEL_TVGID", columnList = "tvgId"),
	}
)
public class TvChannel {
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	@Column(name = "id", nullable = false)
	private Long id;
	public Long channelID;
	public Long groupId;
	public String tvgChNo;
	public String tvgName;
	public String tvgId;
	public String tvgLogo;
	public String groupTitle;
	public String tvgUrl;
	
	
	
	public TvChannel() {
		super();
	}

	public TvChannel(Long channelID, Long groupId, String tvgChNo, String tvgName, String tvgId, String tvgLogo, String groupTitle,
			String tvgUrl) {
		super();
		this.channelID = channelID;
		this.groupId = groupId;
		this.tvgChNo = tvgChNo;
		this.tvgName = tvgName;
		this.tvgId = tvgId;
		this.tvgLogo = tvgLogo;
		this.groupTitle = groupTitle;
		this.tvgUrl = tvgUrl;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	
	public Long getChannelID() {
		return channelID;
	}

	public void setChannelID(Long channelID) {
		this.channelID = channelID;
	}

	public Long getGroupId() {
		return groupId;
	}

	public void setGroupId(Long groupId) {
		this.groupId = groupId;
	}

	public String getTvgChNo() {
		return tvgChNo;
	}

	public void setTvgChNo(String tvgChNo) {
		this.tvgChNo = tvgChNo;
	}

	public String getTvgName() {
		return tvgName;
	}

	public void setTvgName(String tvgName) {
		this.tvgName = tvgName;
	}

	public String getTvgId() {
		return tvgId;
	}

	public void setTvgId(String tvgId) {
		this.tvgId = tvgId;
	}

	public String getTvgLogo() {
		return tvgLogo;
	}

	public void setTvgLogo(String tvgLogo) {
		this.tvgLogo = tvgLogo;
	}

	public String getGroupTitle() {
		return groupTitle;
	}

	public void setGroupTitle(String groupTitle) {
		this.groupTitle = groupTitle;
	}

	public String getTvgUrl() {
		return tvgUrl;
	}

	public void setTvgUrl(String tvgUrl) {
		this.tvgUrl = tvgUrl;
	}

	@Override
	public String toString() {
		return "TvChannel [channelID=" + channelID + ", groupId=" + groupId + ", tvgChNo=" + tvgChNo + ", tvgName=" + tvgName + ", tvgId="
				+ tvgId + ", tvgLogo=" + tvgLogo + ", groupTitle=" + groupTitle + ", tvgUrl=" + tvgUrl + "]";
	}
	
	
	
}
