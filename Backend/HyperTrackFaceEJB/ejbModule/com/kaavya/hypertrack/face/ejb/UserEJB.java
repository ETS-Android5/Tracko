package com.kaavya.hypertrack.face.ejb;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import com.kaavya.azure.face.exceptions.DeviceNotFoundException;
import com.kaavya.hypertrack.face.exceptions.ECSUserInvalidCredentialException;
import com.kaavya.hypertrack.face.exceptions.ECSUserInvalidDataException;
import com.kaavya.hypertrack.face.exceptions.ECSUserInvalidDeviceException;
import com.kaavya.hypertrack.face.exceptions.ECSUserValidationException;
import com.kaavya.hypertrack.face.jpa.UserEntity;
import com.kaavya.hypertrack.face.pojo.UserPojo;

@Stateless
@LocalBean
public class UserEJB {
	@PersistenceContext(unitName = "HyperTrackFaceJPA")
	private EntityManager em;

	public void register(String username, String deviceId, String name, byte[]photo) throws ECSUserInvalidDataException
	{
		if (username == null || deviceId == null || name == null || photo == null)
			throw new ECSUserInvalidDataException();
		
		username = username.trim();
		UserEntity entity = em.find(UserEntity.class, username);
		if (entity != null)
		{
			em.remove(entity);
		}
		
		entity = new UserEntity();
		entity.setDeviceId(deviceId);
		entity.setName(name);
		entity.setPhoto(photo);
		entity.setUserId(username);

		em.persist(entity);
	}

	
	public UserPojo validateCredential(String username, String deviceId)
			throws ECSUserInvalidCredentialException, ECSUserInvalidDeviceException{

		if (username == null || deviceId == null)
			throw new ECSUserInvalidCredentialException();

		username = username.trim();
		UserEntity entity = em.find(UserEntity.class, username);
		if (entity == null)
		{
			throw new ECSUserInvalidCredentialException();
		}
		
		if(entity.getDeviceId().compareToIgnoreCase(deviceId) != 0)
			throw new ECSUserInvalidDeviceException();
		return convert(entity);
	}
	
	public UserPojo read(String username) throws DeviceNotFoundException{
		username = username.trim();
		UserEntity entity = em.find(UserEntity.class, username);
		if (entity == null)
		{
			throw new DeviceNotFoundException();
		}
		return convert(entity);
	}

	public List<UserPojo> readAll()
	{
		Query query = em.createNamedQuery("UserEntity.READ_ALL");

		List<UserEntity> listRecords = query.getResultList();
		List<UserPojo>  lstPojos = new ArrayList<UserPojo>();
		for(UserEntity entity : listRecords)
		{
			lstPojos.add(convert(entity));
		}
		return lstPojos;
	}
	
	private UserPojo convert(UserEntity entity)
	{
		return new UserPojo(entity.getUserId(), entity.getDeviceId(), entity.getName(), entity.getPhoto());
	}
}