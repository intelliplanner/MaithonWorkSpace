package com.ipssi.rfid.beans;

import java.util.Date;

public class Transporter {
	private int id;
	private String name;
	private String refPONumber;
	private String refPOLineItem;
	private double loadAllocation;
	private Date createdOn;                         
	private Date updatedOn;  
	private int updatedBy;
	public int getId() {
		return id;
	}
	public String getName() {
		return name;
	}
	public String getRefPONumber() {
		return refPONumber;
	}
	public String getRefPOLineItem() {
		return refPOLineItem;
	}
	public double getLoadAllocation() {
		return loadAllocation;
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
	public void setRefPONumber(String refPONumber) {
		this.refPONumber = refPONumber;
	}
	public void setRefPOLineItem(String refPOLineItem) {
		this.refPOLineItem = refPOLineItem;
	}
	public void setLoadAllocation(double loadAllocation) {
		this.loadAllocation = loadAllocation;
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
