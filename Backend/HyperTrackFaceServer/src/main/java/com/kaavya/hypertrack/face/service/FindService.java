package com.kaavya.hypertrack.face.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.kaavya.azure.face.exceptions.DeviceNotFoundException;
import com.kaavya.azure.face.exceptions.ECSFaceDetectException;
import com.kaavya.azure.face.exceptions.ECSFaceIdentifyException;
import com.kaavya.azure.face.exceptions.ECSFaceRateLimitException;
import com.kaavya.azure.face.exceptions.FaceNotFoundException;
import com.kaavya.azure.face.exceptions.FaceTrainingFailedException;
import com.kaavya.azure.face.exceptions.FaceTrainingPendingException;
import com.kaavya.azure.face.exceptions.HyperTrackInvalidResponseException;
import com.kaavya.hypertrack.face.ejb.StartupBean;
import com.kaavya.hypertrack.face.ejb.UserEJB;
import com.kaavya.hypertrack.face.exceptions.ECSUserInvalidCredentialException;
import com.kaavya.hypertrack.face.exceptions.ECSUserInvalidDataException;
import com.kaavya.hypertrack.face.exceptions.ECSUserInvalidDeviceException;
import com.kaavya.hypertrack.face.json.FindRequest;
import com.kaavya.hypertrack.face.json.FindResponse;
import com.kaavya.hypertrack.face.json.LoginRequest;
import com.kaavya.hypertrack.face.json.LoginResponse;
import com.kaavya.hypertrack.face.json.RegisterRequest;
import com.kaavya.hypertrack.face.json.RegisterResponse;
import com.kaavya.hypertrack.face.pojo.UserPojo;
import com.kaavya.hypertrack.json.Datum;
import com.kaavya.hypertrack.json.DeviceList;

/**
 * Servlet implementation class HyperTrackFaceService
 */
@WebServlet("/Find")
public class FindService extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	@EJB
	private StartupBean ejbStartup;
	
	@EJB
	private UserEJB ejbUser;
	
	
	private UserPojo pojoUser = null;

    /**
     * @see HttpServlet#HttpServlet()
     */
    public FindService() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.getWriter().append("Served at: ").append(request.getContextPath());
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		System.out.println("*** INSIDE FIND SERVICE ***");

		response.setContentType("application/json");

		String jsonData = null;
		jsonData = getBody(request);
		
		FindRequest req = null;
		try
		{
			req = new Gson().fromJson(jsonData, FindRequest.class);
		}catch(JsonSyntaxException ex)
		{
			FindResponse res = new FindResponse();
			res.setError(true);
			res.setErrorDescription("Invalid request");
			response.getWriter().write(new Gson().toJson(res));
			response.getWriter().flush();
			return;
		}

		if(req.getPhoto() == null)
		{
			FindResponse res = new FindResponse();
			res.setError(true);
			res.setErrorDescription("Photo cannot be null");
			response.getWriter().write(new Gson().toJson(res));
			response.getWriter().flush();
			return;
		}
		
		String userId = null;
		
		try {
				userId = ejbStartup.getFaceService().detect(Base64.getDecoder().decode(req.getPhoto()));
		} catch (FaceTrainingPendingException e2) {
			FindResponse res = new FindResponse();
			res.setError(true);
			res.setErrorDescription("Face training still pending");
			response.getWriter().write(new Gson().toJson(res));
			response.getWriter().flush();
			return;
		} catch (FaceNotFoundException e2) {
			FindResponse res = new FindResponse();
			res.setError(true);
			res.setErrorDescription("User not registered");
			response.getWriter().write(new Gson().toJson(res));
			response.getWriter().flush();
			return;
		} catch (ECSFaceRateLimitException e) {
			FindResponse res = new FindResponse();
			res.setError(true);
			res.setErrorDescription("Rate limit reached. Try after sometime");
			response.getWriter().write(new Gson().toJson(res));
			response.getWriter().flush();
			return;		
		} catch (ECSFaceDetectException e) {
			e.printStackTrace();
			FindResponse res = new FindResponse();
			res.setError(true);
			res.setErrorDescription("Error detecting face");
			response.getWriter().write(new Gson().toJson(res));
			response.getWriter().flush();
			return;		

		} catch (ECSFaceIdentifyException e) {
			e.printStackTrace();
			FindResponse res = new FindResponse();
			res.setError(true);
			res.setErrorDescription("Error identifying face");
			response.getWriter().write(new Gson().toJson(res));
			response.getWriter().flush();
			return;		
		}
		
		try {
			pojoUser = ejbUser.read(userId);
		} catch (DeviceNotFoundException e1) {
			FindResponse res = new FindResponse();
			res.setError(true);
			res.setErrorDescription("User unregistered");
			response.getWriter().write(new Gson().toJson(res));
			response.getWriter().flush();
			return;
		}

		DeviceList deviceList = null;
		try {
			deviceList = ejbStartup.getDeviceList();
		} catch (IOException | HyperTrackInvalidResponseException e) {
			e.printStackTrace();
			FindResponse res = new FindResponse();
			res.setError(true);
			res.setErrorDescription("Error retrieving devices");
			response.getWriter().write(new Gson().toJson(res));
			response.getWriter().flush();
			return;
		}
		
		List<Datum> reqResult = deviceList.getData().stream()
			     .filter(item -> item.getDeviceId().compareTo(pojoUser.getDeviceId()) == 0)
			     .collect(Collectors.toList());	

		if(reqResult.isEmpty())
		{
			FindResponse res = new FindResponse();
			res.setError(true);
			res.setErrorDescription(String.format("User %s could not be tracked", pojoUser.getName()));
			response.getWriter().write(new Gson().toJson(res));
			response.getWriter().flush();
			return;
		}
		
		if(reqResult.get(0).getDeviceStatus().getValue().compareTo("active") != 0)
		{
			FindResponse res = new FindResponse();
			res.setError(true);
			res.setErrorDescription(String.format("User %s is not active", pojoUser.getName()));
			response.getWriter().write(new Gson().toJson(res));
			response.getWriter().flush();
			return;
		}
		
		FindResponse res = new FindResponse();
		res.setError(false);
		res.setLat(reqResult.get(0).getLocation().getGeometry().getCoordinates().get(1));
		res.setLon(reqResult.get(0).getLocation().getGeometry().getCoordinates().get(0));
		res.setName(pojoUser.getName());
		res.setUserId(pojoUser.getUserId());
		response.getWriter().write(new Gson().toJson(res));
		response.getWriter().flush();
		return;
	}
	
	private String getBody(HttpServletRequest request) throws IOException {
	    String body = null;
	    StringBuilder stringBuilder = new StringBuilder();
	    BufferedReader bufferedReader = null;

	    try {
	        InputStream inputStream = request.getInputStream();
	        if (inputStream != null) 
	        {
	            bufferedReader = new BufferedReader(new InputStreamReader(inputStream,"UTF-8"));
	            char[] charBuffer = new char[128];
	            int bytesRead = -1;
	            while ((bytesRead = bufferedReader.read(charBuffer)) > 0) 
	            {
	                stringBuilder.append(charBuffer, 0, bytesRead);
	            }
	        } 
	        else 
	        {
	            stringBuilder.append("");
	        }
	    } catch (IOException ex) {
	        throw ex;
	    } finally 
	    {
	        if (bufferedReader != null) 
	        {
	            try {
	                bufferedReader.close();
	            } catch (IOException ex) {
	                throw ex;
	            }
	        }
	    }
	    body = stringBuilder.toString();
	    return body;
	}
}