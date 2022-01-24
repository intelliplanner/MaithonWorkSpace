/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ipssi.rfid.beans;

import com.ipssi.gen.utils.Misc;

/**
 *
 * @author Vi$ky
 */
public class Biometric {

    private int driver_id;
    private String driver_name;
    private byte[] photo;
    private String dl_no;
    private int driver_type;
    private int isFingerInDB = Misc.getUndefInt();
    private int status = Misc.getUndefInt();
    
    
    public int getIsFingerCaptured() {
        return isFingerCaptured;
    }

    public void setIsFingerCaptured(int isFingerCaptured) {
        this.isFingerCaptured = isFingerCaptured;
    }
    private int isFingerCaptured;
    public int getDriver_id() {
        return driver_id;
    }

    public void setDriver_id(int driver_id) {
        this.driver_id = driver_id;
    }

    public String getDriver_name() {
        return driver_name;
    }

    public void setDriver_name(String driver_name) {
        this.driver_name = driver_name;
    }

    public byte[] getPhoto() {
        return photo;
    }

    public void setPhoto(byte[] photo) {
        this.photo = photo;
    }

    public String getDl_no() {
        return dl_no;
    }

    public void setDl_no(String dl_no) {
        this.dl_no = dl_no;
    }

    public int getDriver_type() {
        return driver_type;
    }

    public void setDriver_type(int driver_type) {
        this.driver_type = driver_type;
    }

	public int getIsFingerInDB() {
		return isFingerInDB;
	}

	public int getStatus() {
		return status;
	}

	public void setIsFingerInDB(int isFingerInDB) {
		this.isFingerInDB = isFingerInDB;
	}

	public void setStatus(int status) {
		this.status = status;
	}
}
