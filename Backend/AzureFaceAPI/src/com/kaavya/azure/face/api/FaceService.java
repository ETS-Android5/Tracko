package com.kaavya.azure.face.api;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.kaavya.azure.face.exceptions.ECSFaceDetectException;
import com.kaavya.azure.face.exceptions.ECSFaceIdentifyException;
import com.kaavya.azure.face.exceptions.ECSFaceRateLimitException;
import com.kaavya.azure.face.exceptions.FaceNotFoundException;
import com.kaavya.azure.face.exceptions.FaceTrainingFailedException;
import com.kaavya.azure.face.exceptions.FaceTrainingPendingException;
import com.kaavya.azure.face.utils.PersonData;
import com.microsoft.azure.cognitiveservices.vision.faceapi.FaceAPI;
import com.microsoft.azure.cognitiveservices.vision.faceapi.FaceAPIManager;
import com.microsoft.azure.cognitiveservices.vision.faceapi.models.APIErrorException;
import com.microsoft.azure.cognitiveservices.vision.faceapi.models.AddPersonFaceFromStreamOptionalParameter;
import com.microsoft.azure.cognitiveservices.vision.faceapi.models.AzureRegions;
import com.microsoft.azure.cognitiveservices.vision.faceapi.models.CreatePersonGroupPersonsOptionalParameter;
import com.microsoft.azure.cognitiveservices.vision.faceapi.models.CreatePersonGroupsOptionalParameter;
import com.microsoft.azure.cognitiveservices.vision.faceapi.models.DetectWithStreamOptionalParameter;
import com.microsoft.azure.cognitiveservices.vision.faceapi.models.DetectWithUrlOptionalParameter;
import com.microsoft.azure.cognitiveservices.vision.faceapi.models.DetectedFace;
import com.microsoft.azure.cognitiveservices.vision.faceapi.models.FaceAttributeType;
import com.microsoft.azure.cognitiveservices.vision.faceapi.models.FaceAttributes;
import com.microsoft.azure.cognitiveservices.vision.faceapi.models.IdentifyResult;
import com.microsoft.azure.cognitiveservices.vision.faceapi.models.Person;
import com.microsoft.azure.cognitiveservices.vision.faceapi.models.TrainingStatus;
import com.microsoft.azure.cognitiveservices.vision.faceapi.models.TrainingStatusType;


public class FaceService {

	 // Used for the Identify 
    final String PERSON_GROUP_ID = "person_group"; // can be any lowercase, 0-9, "-", or "_" character.
    // Used for the Face List 
    final String FACE_LIST_ID = "person_group_list";
    
    final AzureRegions REGION = AzureRegions.EASTUS2;
    
    private String KEY = "PASTE_YOUR_FACE_SUBSCRIPTION_KEY_HERE";
    private FaceAPI client = null;
    
    private Hashtable<UUID, String> htUsers = null;
    
	public FaceService(String key) {
		this.KEY = key;
		client = FaceAPIManager.authenticate(REGION, KEY);
		
	}

	public void addPersons(List<PersonData> lstpersons) throws FaceTrainingFailedException {

		deleteAll();		 
		client.personGroups().create(PERSON_GROUP_ID, new CreatePersonGroupsOptionalParameter().withName(PERSON_GROUP_ID));
		htUsers = new Hashtable<UUID, String>();
		for(PersonData personData : lstpersons)
		{
			Person person = client.personGroupPersons().create(PERSON_GROUP_ID, 
                    new CreatePersonGroupPersonsOptionalParameter().withName(personData.getName()));
			client.personGroupPersons().addPersonFaceFromStream(PERSON_GROUP_ID, person.personId(), personData.getPhoto(),null);
			
			System.out.println("ADDED: " + person.personId().toString());
			
			htUsers.put(person.personId(), personData.getName());
			
		}
		client.personGroups().train(PERSON_GROUP_ID);
		
		 // Wait until the training is completed.
        while(true) 
        {
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) { e.printStackTrace(); }
            
            // Get training status
            TrainingStatus status = client.personGroups().getTrainingStatus(PERSON_GROUP_ID);
            if (status.status() == TrainingStatusType.SUCCEEDED) 
            {
                System.out.println("Training status: " + status.status());
                break;
            }
            
            System.out.println("Training status: " + status.status());
            if(status.status() == TrainingStatusType.FAILED)
            	throw new FaceTrainingFailedException();
        }
        return;
	}

	public void addPerson(String name, byte[]photo) throws FaceTrainingPendingException, FaceTrainingFailedException {
		
		
		
        if(htUsers.size() >0 &&  client.personGroups().getTrainingStatus(PERSON_GROUP_ID).status() != TrainingStatusType.SUCCEEDED)
        	throw new FaceTrainingPendingException();

        
        Enumeration<UUID> enumeration = htUsers.keys();
        
        // iterate using enumeration object
        while(enumeration.hasMoreElements()) {
 
            UUID key = enumeration.nextElement();
            if(htUsers.get(key).compareTo(name) == 0)
            {
            	client.personGroupPersons().delete(PERSON_GROUP_ID, key);
            	htUsers.remove(key);
            	break;
            }
        }
        
        Person person = client.personGroupPersons().create(PERSON_GROUP_ID, 
                    new CreatePersonGroupPersonsOptionalParameter().withName(name));
		client.personGroupPersons().addPersonFaceFromStream(PERSON_GROUP_ID, person.personId(), photo,null);
		
		System.out.println("ADDED: " + person.personId().toString());
		
		htUsers.put(person.personId(), name);
			
		client.personGroups().train(PERSON_GROUP_ID);

		// Wait until the training is completed.
        while(true) 
        {
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) { e.printStackTrace(); }
            
            // Get training status
            TrainingStatus status = client.personGroups().getTrainingStatus(PERSON_GROUP_ID);
            if (status.status() == TrainingStatusType.SUCCEEDED) 
            {
                System.out.println("Training status: " + status.status());
                break;
            }
            
            System.out.println("Training status: " + status.status());
            if(status.status() == TrainingStatusType.FAILED)
            	throw new FaceTrainingFailedException();
        }
        return;
	}
	
	public String detect(byte[]photo) throws FaceTrainingPendingException, FaceNotFoundException, ECSFaceRateLimitException, ECSFaceDetectException, ECSFaceIdentifyException 
	{
		
//		try {
//			Files.write(Paths.get("/root/face_new.jpg"), photo);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}

        if(client.personGroups().getTrainingStatus(PERSON_GROUP_ID).status() != TrainingStatusType.SUCCEEDED)
        	throw new FaceTrainingPendingException();
        
		List<UUID> detectedFaces = detectFaces(photo);
		if(detectedFaces.size() == 0)
		{
			throw new FaceNotFoundException();
		}
		// Identifies which faces in group photo are in our person group. 
		
		try
		{
	        List<IdentifyResult> identifyResults = client.faces().identify(PERSON_GROUP_ID, detectedFaces, null);
	        if(identifyResults.size() == 0)
	        {
	        	throw new FaceNotFoundException();
	        }
	        return htUsers.get(identifyResults.get(0).candidates().get(0).personId());
        }catch(APIErrorException ex)
        {
        	if(ex.getMessage().contains("429"))
        		throw new ECSFaceRateLimitException();
        	else
        		throw new ECSFaceIdentifyException();
        }

        //        System.out.println("TOTAL FOUND: " + identifyResults.size());
//		System.out.println("FOUND: " + identifyResults.get(0).faceId());
//		System.out.println("FOUND: " + identifyResults.get(0).candidates().get(0).personId());

    	
	}
	
	public List<UUID> detectFaces(byte[] image) throws ECSFaceRateLimitException, ECSFaceDetectException 
	{
        // Create face IDs list
        List<DetectedFace> facesList = null;
        
        try
        {
        	facesList = client.faces().detectWithStream(image, new DetectWithStreamOptionalParameter().withReturnFaceId(true));
        }catch(APIErrorException ex)
        {
        	if(ex.getMessage().contains("429"))
        		throw new ECSFaceRateLimitException();
        	else
        		throw new ECSFaceDetectException();
        }
        // Get face(s) UUID(s)
        List<UUID> faceUuids = new ArrayList<>();
        for (DetectedFace face : facesList) {
            faceUuids.add(face.faceId());
        }
        return faceUuids;
    }
	
	public void deleteAll()
	{
		try
		{
			client.personGroups().delete(PERSON_GROUP_ID); // Delete existing group
			//client.faceLists().delete(FACE_LIST_ID);
		}catch(APIErrorException ex)
		{
			ex.printStackTrace();
		}
	}
	
	public static void main(String[] args) throws Exception
	{
		List<PersonData> lstPersons = new ArrayList<>();
		
		lstPersons.add(new PersonData("Prabu", readFile("P:\\FaceCompareImages\\prabu1.jpg")));
		lstPersons.add(new PersonData("Ramki", readFile("P:\\FaceCompareImages\\ramki1.jpg")));
		//lstPersons.add(new PersonData("Sanjeev", readFile("P:\\FaceCompareImages\\sanjeev.png")));

		FaceService service =  new  FaceService("d4fa3f0a6c7f464785b68911dbd65866");
		service.addPersons(lstPersons);
		service.addPerson("Sanjeev", readFile("P:\\FaceCompareImages\\sanjeev.png"));

		String name = service.detect(readFile("P:\\FaceCompareImages\\ramki3.jpg"));
		System.out.println("Detected Person : " + name);
	}
	
	private static byte[]readFile(String filename) throws IOException
	{
		return Files.readAllBytes(Paths.get(filename));
	}
}
