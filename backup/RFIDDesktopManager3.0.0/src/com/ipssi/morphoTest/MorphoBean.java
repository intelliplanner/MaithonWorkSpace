package com.ipssi.morphoTest;

import java.util.ArrayList;

public class MorphoBean {
	private int driverId;
	private ArrayList<byte[]> templateFirst;
	private ArrayList<byte[]> templateSecond;

	public int getDriverId() {
		return driverId;
	}

	public void setDriverId(int driverId) {
		this.driverId = driverId;
	}

	public ArrayList<byte[]> getTemplateFirst() {
		return templateFirst;
	}

	public void setTemplateFirst(ArrayList<byte[]> templateFirst) {
		this.templateFirst = templateFirst;
	}

	public ArrayList<byte[]> getTemplateSecond() {
		return templateSecond;
	}

	public void setTemplateSecond(ArrayList<byte[]> templateSecond) {
		this.templateSecond = templateSecond;
	}

}
