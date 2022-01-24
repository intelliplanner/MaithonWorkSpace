package com.ipssi.rfid.beans;

import java.util.Date;

public class Mines {

	private int id;
	private String name;
	private Date createdOn;                         
	private Date updatedOn;  
	private int updatedBy;
	public int getId() {
		return id;
	}
	public String getName() {
		return name;
	}
	public Date getCreatedOn() {
		return createdOn;
	}
	public Date getUpdatedOn() {
		return updatedOn;
	}
	public int getUpdatedBy() {
		return updatedBy;
	}
	public void setId(int id) {
		this.id = id;
	}
	public void setName(String name) {
		this.name = name;
	}
	public void setCreatedOn(Date createdOn) {
		this.createdOn = createdOn;
	}
	public void setUpdatedOn(Date updatedOn) {
		this.updatedOn = updatedOn;
	}
	public void setUpdatedBy(int updatedBy) {
		this.updatedBy = updatedBy;
	}  

}
