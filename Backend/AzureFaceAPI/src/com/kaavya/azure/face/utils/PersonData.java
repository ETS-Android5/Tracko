package com.kaavya.azure.face.utils;

import java.util.UUID;

public class PersonData {
	private String name;
	private byte[]photo;
	public PersonData(String name, byte[] photo) {
		super();
		this.name = name;
		this.photo = photo;
	}
	
	public String getName() {
		return name;
	}
	
	public byte[] getPhoto() {
		return photo;
	}
}
