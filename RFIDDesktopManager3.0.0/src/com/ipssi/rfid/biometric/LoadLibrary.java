/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ipssi.rfid.biometric;


/**
 *
 * @author Morpho
 */
public class LoadLibrary {
    public static String LoadingErrorMessage = null;
    
    static {
        try {
             System.out.println("Loading Library");
             System.loadLibrary("FingerprintSensorAPI");
             System.out.println("Library Loaded Successfully...");
             LoadingErrorMessage = "Libraries Loaded Successfully.";
        } catch (UnsatisfiedLinkError e) {
            e.printStackTrace();
            LoadingErrorMessage = "Error in Loading Library";
        } catch (Exception ex) {
            LoadingErrorMessage = "Error in Loading Library";
            ex.printStackTrace();
        }
    }

    public LoadLibrary(){
        //Do nothing
    }

    public native boolean isDeviceConnected();
    public native int captureFPData(int timeOut);
    public native byte[] getCapturedTemplate();
    public native int enrollUser(String userId, String firstName, String lastName, byte[] templateData, int length);
    public native int identify(int timeOut);
    public native int deleteUserById(String id);
    public native String getUserId();
    public native String getFirstName();
    public native String getLastName();
    public native String getDeviceMake();
    public native String getDeviceInfo();
    public native String getDeviceModel();
    public native byte[] getCapturedImage();
    public native byte[] createImage(byte[] rawimage, int imagelength);
    public native int deleteAll();
}
