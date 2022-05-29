package com.kaavya.hypertrack.face.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Base64;

import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.kaavya.azure.face.exceptions.FaceTrainingFailedException;
import com.kaavya.azure.face.exceptions.FaceTrainingPendingException;
import com.kaavya.hypertrack.face.ejb.StartupBean;
import com.kaavya.hypertrack.face.ejb.UserEJB;
import com.kaavya.hypertrack.face.exceptions.ECSUserInvalidDataException;
import com.kaavya.hypertrack.face.json.RegisterRequest;
import com.kaavya.hypertrack.face.json.RegisterResponse;

/**
 * Servlet implementation class HyperTrackFaceService
 */
@WebServlet("/Register")
public class RegisterService extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
	
	@EJB
	private StartupBean ejbStartup;
	
	@EJB
	private UserEJB ejbUser;
    /**
     * @see HttpServlet#HttpServlet()
     */
    public RegisterService() {
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
		System.out.println("*** INSIDE REGISTER SERVICE ***");

		response.setContentType("application/json");

		String jsonData = null;
		jsonData = getBody(request);
		
		RegisterRequest req = null;
		try
		{
			req = new Gson().fromJson(jsonData, RegisterRequest.class);
		}catch(JsonSyntaxException ex)
		{
			RegisterResponse res = new RegisterResponse();
			res.setError(true);
			res.setErrorDescription("Invalid request");
			response.getWriter().write(new Gson().toJson(res));
			response.getWriter().flush();
			return;
		}

		if(req.getDeviceId() == null)
		{
			RegisterResponse res = new RegisterResponse();
			res.setError(true);
			res.setErrorDescription("Device Id cannot be null");
			response.getWriter().write(new Gson().toJson(res));
			response.getWriter().flush();
			return;
		}
		
		if(req.getName() == null)
		{
			RegisterResponse res = new RegisterResponse();
			res.setError(true);
			res.setErrorDescription("Name cannot be null");
			response.getWriter().write(new Gson().toJson(res));
			response.getWriter().flush();
			return;
		}
		
		if(req.getPhoto() == null)
		{
			RegisterResponse res = new RegisterResponse();
			res.setError(true);
			res.setErrorDescription("Photo cannot be null");
			response.getWriter().write(new Gson().toJson(res));
			response.getWriter().flush();
			return;
		}
		
		if(req.getUserId() == null)
		{
			RegisterResponse res = new RegisterResponse();
			res.setError(true);
			res.setErrorDescription("User Id cannot be null");
			response.getWriter().write(new Gson().toJson(res));
			response.getWriter().flush();
			return;
		}

		try {
			ejbUser.register(req.getUserId(), req.getDeviceId(), req.getName(), Base64.getDecoder().decode(req.getPhoto()));
		} catch (ECSUserInvalidDataException e) {
			e.printStackTrace();
			RegisterResponse res = new RegisterResponse();
			res.setError(true);
			res.setErrorDescription("Error registering user - Invalid data");
			response.getWriter().write(new Gson().toJson(res));
			response.getWriter().flush();
			return;
		}
		try {
			ejbStartup.getFaceService().addPerson(req.getUserId(), Base64.getDecoder().decode(req.getPhoto()));
		} catch (FaceTrainingPendingException e) {
			RegisterResponse res = new RegisterResponse();
			res.setError(true);
			res.setErrorDescription("Error registering user - Training pending");
			response.getWriter().write(new Gson().toJson(res));
			response.getWriter().flush();
			return;
		} catch (FaceTrainingFailedException e) {
			RegisterResponse res = new RegisterResponse();
			res.setError(true);
			res.setErrorDescription("Error registering user - Training failed");
			response.getWriter().write(new Gson().toJson(res));
			response.getWriter().flush();
			return;
		}
		
		RegisterResponse res = new RegisterResponse();
		res.setError(false);
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
