package com.kaavya.hypertrack.face.jpa;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

@Entity
@Table(name="user_master")
@NamedQueries({
	  @NamedQuery(name ="UserEntity.READ_ALL" ,query = "SELECT o FROM UserEntity o ")
})
public class UserEntity {
	@Id
	@Column(name="userId", length=128)
	private String userId;
	
	@Column(name="deviceId", length=128)
	private String deviceId;

	@Column(name="name", length=128)
	private String name;

	@Lob
	@Column(name="photo", length=1048576)
	private byte[]photo;

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public byte[] getPhoto() {
		return photo;
	}

	public void setPhoto(byte[] photo) {
		this.photo = photo;
	}
}