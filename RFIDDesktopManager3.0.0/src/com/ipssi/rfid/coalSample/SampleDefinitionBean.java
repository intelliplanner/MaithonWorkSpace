/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ipssi.rfid.coalSample;

import java.util.HashMap;

/**
 *
 * @author IPSSI
 */
public class SampleDefinitionBean {
	private int id;
	private int portNodeId;
	public String getServerMailPass() {
		return serverMailPass;
	}
	public void setServerMailPass(String serverMailPass) {
		this.serverMailPass = serverMailPass;
	}
	private String name;
	private String subject;
	private String fileName;
	private int totalColumn;
	private int startRow;
	private String serverMailId;
	private String serverMailPass;
	private String fromMail;
	private int listener;
	private String csvDelemeter;
	private String charset;
	public int getListener() {
		return listener;
	}
	public void setListener(int listener) {
		this.listener = listener;
	}
	
	private HashMap<Integer,SampleParamBean> sampleParamList;
	public HashMap<Integer,SampleParamBean> getSampleParamList() {
		return sampleParamList;
	}
	public void setSampleParamList(HashMap<Integer,SampleParamBean> sampleParamList) {
		this.sampleParamList = sampleParamList;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}

	public int getPortNodeId() {
		return portNodeId;
	}

	public void setPortNodeId(int portNodeId) {
		this.portNodeId = portNodeId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public int getTotalColumn() {
		return totalColumn;
	}

	public void setTotalColumn(int totalColumn) {
		this.totalColumn = totalColumn;
	}

	public int getStartRow() {
		return startRow;
	}

	public void setStartRow(int startRow) {
		this.startRow = startRow;
	}

	public String getServerMailId() {
		return serverMailId;
	}

	public void setServerMailId(String serverMailId) {
		this.serverMailId = serverMailId;
	}

	public String getFromMail() {
		return fromMail;
	}

	public void setFromMail(String fromMail) {
		this.fromMail = fromMail;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	public String getCsvDelemeter() {
		return csvDelemeter;
	}
	public void setCsvDelemeter(String csvDelemeter) {
		this.csvDelemeter = csvDelemeter;
	}
	public String getCharset() {
		return charset;
	}
	public void setCharset(String charset) {
		this.charset = charset;
	}

}
