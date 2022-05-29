package com.kaavya.hypertrack.face.json;

import java.io.Serializable;

import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Generated("jsonschema2pojo")
public class FindRequest  implements Serializable{

	@SerializedName("photo")
	@Expose
	private String photo;

	public String getPhoto() {
		return photo;
	}

	public void setPhoto(String photo) {
		this.photo = photo;
	}
}