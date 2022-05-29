package com.kaavya.hypertrack.face.json;

import java.io.Serializable;

import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Generated("jsonschema2pojo")
public class RegisterResponse implements Serializable{

	@SerializedName("error")
	@Expose
	private boolean error;
	@SerializedName("errorDescription")
	@Expose
	private String errorDescription;

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

}