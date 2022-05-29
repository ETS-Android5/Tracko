package com.kaavya.hypertrack.face.ejb;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Singleton;
import javax.ejb.Startup;

import com.google.gson.Gson;
import com.kaavya.azure.face.api.FaceService;
import com.kaavya.azure.face.exceptions.FaceTrainingFailedException;
import com.kaavya.azure.face.exceptions.HyperTrackInvalidResponseException;
import com.kaavya.azure.face.utils.PersonData;
import com.kaavya.hypertrack.face.pojo.UserPojo;
import com.kaavya.hypertrack.json.DeviceList;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@Singleton
@Startup
@LocalBean
@ConcurrencyManagement(ConcurrencyManagementType.CONTAINER)
public class StartupBean {
	
	private static final  String AZURE_FACE_KEY="d4fa3f0a6c7f464785b68911dbd65866";
	private static final String HYPERTRACK_ACCOUNT_ID="pb0nQ6n94peizAPQd9eRgXxiMpw";
	private static final String HYPERTRACK_SECRET_KEY="m86ZqsTpUJJS4I_lbVtJbauEOhkfwCqCeKTb5Gv7ujbUGBVjjqk5Xg";
	
	private FaceService faceService;

	@EJB
	private UserEJB ejbUser;
	
	@PostConstruct
    public void initialize() {
		faceService = new FaceService(AZURE_FACE_KEY);
		List<UserPojo> lstUsers = ejbUser.readAll();
		
		List<PersonData> lstPersons = new ArrayList<>();
		for(UserPojo pojoUser : lstUsers)
		{
			PersonData person = new PersonData(pojoUser.getUserId(), pojoUser.getPhoto());
			lstPersons.add(person);
		}
		
		try {
			faceService.addPersons(lstPersons);
		} catch (FaceTrainingFailedException e) {
			e.printStackTrace();
		}
	}
	
	public FaceService getFaceService()
	{
		return faceService;
	}
	
	public DeviceList getDeviceList() throws IOException, HyperTrackInvalidResponseException
	{
		// GET DEVICE LIST
		OkHttpClient client = new OkHttpClient();

		  String accountId = HYPERTRACK_ACCOUNT_ID;
		  String secretKey = HYPERTRACK_SECRET_KEY;
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
		if(response.isSuccessful() == false)
			throw new HyperTrackInvalidResponseException();
		
		String jsonResponse = response.body().string();
		
		if(jsonResponse.contains("\"data\"") == false)
			throw new HyperTrackInvalidResponseException();

		return new Gson().fromJson(jsonResponse, DeviceList.class);
	}
}