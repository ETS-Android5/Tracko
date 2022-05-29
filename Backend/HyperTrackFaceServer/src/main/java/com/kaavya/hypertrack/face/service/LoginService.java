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
import com.kaavya.hypertrack.face.exceptions.ECSUserInvalidCredentialException;
import com.kaavya.hypertrack.face.exceptions.ECSUserInvalidDataException;
import com.kaavya.hypertrack.face.exceptions.ECSUserInvalidDeviceException;
import com.kaavya.hypertrack.face.json.LoginRequest;
import com.kaavya.hypertrack.face.json.LoginResponse;
import com.kaavya.hypertrack.face.json.RegisterRequest;
import com.kaavya.hypertrack.face.json.RegisterResponse;

/**
 * Servlet implementation class HyperTrackFaceService
 */
@WebServlet("/Login")
public class LoginService extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	@EJB
	private StartupBean ejbStartup;
	
	@EJB
	private UserEJB ejbUser;
    /**
     * @see HttpServlet#HttpServlet()
     */
    public LoginService() {
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
		
		LoginRequest req = null;
		try
		{
			req = new Gson().fromJson(jsonData, LoginRequest.class);
		}catch(JsonSyntaxException ex)
		{
			LoginResponse res = new LoginResponse();
			res.setError(true);
			res.setErrorDescription("Invalid request");
			response.getWriter().write(new Gson().toJson(res));
			response.getWriter().flush();
			return;
		}

		if(req.getDeviceId() == null)
		{
			LoginResponse res = new LoginResponse();
			res.setError(true);
			res.setErrorDescription("Device Id cannot be null");
			response.getWriter().write(new Gson().toJson(res));
			response.getWriter().flush();
			return;
		}
		
		try {
			ejbUser.validateCredential(req.getUserId(), req.getDeviceId());
		} catch (ECSUserInvalidCredentialException e) {
			e.printStackTrace();
			LoginResponse res = new LoginResponse();
			res.setError(true);
			res.setErrorDescription("Invalid credentials");
			response.getWriter().write(new Gson().toJson(res));
			response.getWriter().flush();
			return;
		}catch (ECSUserInvalidDeviceException e) {
			e.printStackTrace();
			LoginResponse res = new LoginResponse();
			res.setError(true);
			res.setErrorDescription("Invalid device");
			response.getWriter().write(new Gson().toJson(res));
			response.getWriter().flush();
			return;
		}
		
		LoginResponse res = new LoginResponse();
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