package com.kaavya.xsd;

import java.io.Serializable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

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