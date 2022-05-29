package com.kaavya.hypertrack.face.pojo;

public class UserPojo {
	private String userId;
	private String deviceId;
	private String name;
	private byte[]photo;
	public UserPojo(String userId, String deviceId, String name, byte[] photo) {
		super();
		this.userId = userId;
		this.deviceId = deviceId;
		this.name = name;
		this.photo = photo;
	}
	public String getUserId() {
		return userId;
	}
	public String getDeviceId() {
		return deviceId;
	}
	public String getName() {
		return name;
	}
	public byte[] getPhoto() {
		return photo;
	}
}