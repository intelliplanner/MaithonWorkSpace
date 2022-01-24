/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ipssi.rfid.constant;

import java.util.Date;

/**
 *
 * @author Vi$ky
 */
public class DriverDetailBean {

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getDriver_name() {
        return driver_name;
    }

    public void setDriver_name(String driver_name) {
        this.driver_name = driver_name;
    }

    public String getDriver_uid() {
        return driver_uid;
    }

    public void setDriver_uid(String driver_uid) {
        this.driver_uid = driver_uid;
    }

    public String getDriver_dl_number() {
        return driver_dl_number;
    }

    public void setDriver_dl_number(String driver_dl_number) {
        this.driver_dl_number = driver_dl_number;
    }

    public String getInfo3() {
        return info3;
    }

    public void setInfo3(String info3) {
        this.info3 = info3;
    }

    public String getDriver_mobile_one() {
        return driver_mobile_one;
    }

    public void setDriver_mobile_one(String driver_mobile_one) {
        this.driver_mobile_one = driver_mobile_one;
    }

    public String getDriver_address_one() {
        return driver_address_one;
    }

    public void setDriver_address_one(String driver_address_one) {
        this.driver_address_one = driver_address_one;
    }

    public String getDriver_address_two() {
        return driver_address_two;
    }

    public void setDriver_address_two(String driver_address_two) {
        this.driver_address_two = driver_address_two;
    }

    public String getDriver_insurance_one() {
        return driver_insurance_one;
    }

    public void setDriver_insurance_one(String driver_insurance_one) {
        this.driver_insurance_one = driver_insurance_one;
    }

    public String getDriver_insurance_two() {
        return driver_insurance_two;
    }

    public void setDriver_insurance_two(String driver_insurance_two) {
        this.driver_insurance_two = driver_insurance_two;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getProvided_uid() {
        return provided_uid;
    }

    public void setProvided_uid(String provided_uid) {
        this.provided_uid = provided_uid;
    }

    public String getInfo1() {
        return info1;
    }

    public void setInfo1(String info1) {
        this.info1 = info1;
    }

    public String getInfo2() {
        return info2;
    }

    public void setInfo2(String info2) {
        this.info2 = info2;
    }

    public Date getDdt_training_date() {
        return ddt_training_date;
    }

    public void setDdt_training_date(Date ddt_training_date) {
        this.ddt_training_date = ddt_training_date;
    }

    public String getDriver_mobile_two() {
        return driver_mobile_two;
    }

    public void setDriver_mobile_two(String driver_mobile_two) {
        this.driver_mobile_two = driver_mobile_two;
    }

    public Date getDl_expiry_date() {
        return dl_expiry_date;
    }

    public void setDl_expiry_date(Date dl_expiry_date) {
        this.dl_expiry_date = dl_expiry_date;
    }

    public Date getDdt_training_expiry_date() {
        return ddt_training_expiry_date;
    }

    public void setDdt_training_expiry_date(Date ddt_training_expiry_date) {
        this.ddt_training_expiry_date = ddt_training_expiry_date;
    }

    public Date getInsurance_one_date() {
        return insurance_one_date;
    }

    public void setInsurance_one_date(Date insurance_one_date) {
        this.insurance_one_date = insurance_one_date;
    }

    public Date getInsurance_two_date() {
        return insurance_two_date;
    }

    public void setInsurance_two_date(Date insurance_two_date) {
        this.insurance_two_date = insurance_two_date;
    }

    public Date getDriver_dob() {
        return driver_dob;
    }

    public void setDriver_dob(Date driver_dob) {
        this.driver_dob = driver_dob;
    }

    public int getOrg_id() {
        return org_id;
    }

    public void setOrg_id(int org_id) {
        this.org_id = org_id;
    }

    public int getGuid_type() {
        return guid_type;
    }

    public void setGuid_type(int guid_type) {
        this.guid_type = guid_type;
    }

    public Date getCreated_on() {
        return created_on;
    }

    public void setCreated_on(Date created_on) {
        this.created_on = created_on;
    }

    public int getIs_finger_Captured() {
        return is_finger_Captured;
    }

    public void setIs_finger_Captured(int is_finger_Captured) {
        this.is_finger_Captured = is_finger_Captured;
    }

    int type;
    String driver_name;
    String driver_uid;
    String driver_dl_number;
    String info3;
    String driver_mobile_one;
    String driver_address_one;
    String driver_address_two;
    String driver_insurance_one;
    String driver_insurance_two;
    int status;
    String provided_uid;
    String info1;
    String info2;
    Date ddt_training_date;
    String driver_mobile_two;
    Date dl_expiry_date;
    Date ddt_training_expiry_date;
    Date insurance_one_date;
    Date insurance_two_date;
    Date driver_dob;
    int org_id;
    int guid_type;
    Date created_on;
    int is_finger_Captured;
    
}
