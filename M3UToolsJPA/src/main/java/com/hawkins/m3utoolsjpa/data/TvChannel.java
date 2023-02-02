package com.hawkins.m3utoolsjpa.data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

@Entity
public class TvChannel {
	
	@GenericGenerator(
	        name = "tvChannelSequenceGenerator",
	        strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
	        parameters = {
	                @Parameter(name = "sequence_name", value = "tvChannelSequence"),
	                @Parameter(name = "initial_value", value = "1"),
	                @Parameter(name = "increment_size", value = "3"),
	                @Parameter(name = "optimizer", value = "hilo")
	        }
	)
	@Id
	@GeneratedValue(generator = "tvChannelSequenceGenerator")
	private Long id;
	public String channelID;
	public String tvgChNo;
	public String tvgName;
	public String tvgId;
	public String tvgLogo;
	public String groupTitle;
	public String tvgUrl;
	
	public TvChannel(String channelID, String tvgChNo, String tvgName, String tvgId, String tvgLogo, String groupTitle,
			String tvgUrl) {
		super();
		this.channelID = channelID;
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
	
	public String getChannelID() {
		return channelID;
	}

	public void setChannelID(String channelID) {
		this.channelID = channelID;
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
		return "TvChannel [channelID=" + channelID + ", tvgChNo=" + tvgChNo + ", tvgName=" + tvgName + ", tvgId="
				+ tvgId + ", tvgLogo=" + tvgLogo + ", groupTitle=" + groupTitle + ", tvgUrl=" + tvgUrl + "]";
	}
	
	
	
}
