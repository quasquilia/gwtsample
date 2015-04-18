package com.guarascio.gwtsample.dao;

import java.util.Date;

import javax.persistence.Embeddable;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

@Embeddable
public class UpdateInfo {
	
	private Date dateLastUpdate = new Date();
	private String userLastUpdate;

	public Date getDateLastUpdate() {
		return dateLastUpdate;
	}
	
	public void setDateLastUpdate(Date dateLastUpdate) {
		this.dateLastUpdate = dateLastUpdate;
	}
	
	public String getUserLastUpdate() {
		if (userLastUpdate == null) {
			UserService userService = UserServiceFactory.getUserService();
			User user = userService.getCurrentUser();
			userLastUpdate = user.getUserId();
		}
		return userLastUpdate;
	}
	
	public void setUserLastUpdate(String userLastUpdate) {
		this.userLastUpdate = userLastUpdate;
	}
	
}