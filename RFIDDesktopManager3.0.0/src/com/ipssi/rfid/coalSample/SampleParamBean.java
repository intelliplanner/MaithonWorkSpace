/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ipssi.rfid.coalSample;

/**
 *
 * @author IPSSI
 */
public class SampleParamBean {
    private int id;
	private int paramId;
	private int paramPos;
	private String notes;
	public int getSecColPos() {
		return secColPos;
	}
	public void setSecColPos(int secColPos) {
		this.secColPos = secColPos;
	}
	private int dataType;
	private int secColPos;
	private int secColDataType;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getParamId() {
		return paramId;
	}
	public void setParamId(int paramId) {
		this.paramId = paramId;
	}
	public int getParamPos() {
		return paramPos;
	}
	public int getSecColDataType() {
		return secColDataType;
	}
	public void setSecColDataType(int secColDataType) {
		this.secColDataType = secColDataType;
	}
	public void setParamPos(int paramPos) {
		this.paramPos = paramPos;
	}
	public String getNotes() {
		return notes;
	}
	public void setNotes(String notes) {
		this.notes = notes;
	}
	public int getDataType() {
		return dataType;
	}
	public void setDataType(int dataType) {
		this.dataType = dataType;
	}	

}
