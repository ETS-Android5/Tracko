package com.kaavya.xsd;

import java.io.Serializable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class FindResponse implements Serializable{

	@SerializedName("error")
	@Expose
	private boolean error;
	@SerializedName("errorDescription")
	@Expose
	private String errorDescription;
	@SerializedName("name")
	@Expose
	private String name;
	@SerializedName("userId")
	@Expose
	private String userId;
	@SerializedName("lat")
	@Expose
	private double lat;
	@SerializedName("lon")
	@Expose
	private double lon;

	public boolean isError() {
		return error;
	}

	public void setError(boolean error) {
		this.error = error;
	}

	public String getErrorDescription() {
		return errorDescription;
	}

	public void setErrorDescription(String errorDescription) {
		this.errorDescription = errorDescription;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public double getLat() {
		return lat;
	}

	public void setLat(double lat) {
		this.lat = lat;
	}

	public double getLon() {
		return lon;
	}

	public void setLon(double lon) {
		this.lon = lon;
	}

}