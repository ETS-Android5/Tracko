package com.kaavya.hypertrack.sample;

import java.util.Base64;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ListDevices {

	public static void main(String[] args) throws Exception {

		// GET DEVICE LIST
		OkHttpClient client = new OkHttpClient();

		  String accountId = "pb0nQ6n94peizAPQd9eRgXxiMpw";
		  String secretKey = "m86ZqsTpUJJS4I_lbVtJbauEOhkfwCqCeKTb5Gv7ujbUGBVjjqk5Xg";
		  String authString = "Basic " +
		    Base64.getEncoder().encodeToString(
		      String.format("%s:%s", accountId, secretKey)
		        .getBytes()
		    );

		Request request = new Request.Builder()
		   .url("https://v3.api.hypertrack.com/devices/")
		   .get()
		   .addHeader("Authorization", authString)
		   .build();

		Response response = client.newCall(request).execute();
		System.out.println(response.body().string());
	}
}