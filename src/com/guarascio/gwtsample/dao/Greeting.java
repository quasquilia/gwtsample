package com.guarascio.gwtsample.dao;

import java.util.Date;

public class Greeting {
	private String userName;
	private String message;
	private Date date = new Date();
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	@Override
	public String toString() {
		return date.toString() + " - " + userName +": " + message;
	}
}
