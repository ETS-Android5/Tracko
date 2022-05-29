package com.kaavya.hypertrack.face.json;

import java.io.Serializable;

import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Generated("jsonschema2pojo")
public class RegisterRequest implements Serializable{

	@SerializedName("userId")
	@Expose
	private String userId;
	@SerializedName("deviceId")
	@Expose
	private String deviceId;
	@SerializedName("name")
	@Expose
	private String name;
	@SerializedName("photo")
	@Expose
	private String photo;

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

	public String getPhoto() {
		return photo;
	}

	public void setPhoto(String photo) {
		this.photo = photo;
	}

}