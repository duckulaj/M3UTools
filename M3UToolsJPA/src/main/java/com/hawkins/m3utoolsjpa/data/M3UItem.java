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
import lombok.Data;

@Entity
@Data
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

}
