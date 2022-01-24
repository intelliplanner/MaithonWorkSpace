/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ipssi.fingerprint.utils;

/**
 *
 * @author Vi$ky
 */
public interface SynServiceHandler {
    void onChange(boolean  onChange);
    void notifyText(String msg);
    void setDeviceId(String deviceId);
    void setCapacity(int capacity);
    void setEnrolled(int enrolled);
    void clearingData();
    void init(String deviceId,int capacity,int enrolled);
}
