/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.scl.loadlibrary;


/**
 *
 * @author Morpho
 */
public class LoadLibrary {

    public static String LoadingErrorMessage = null;
    public static Object lock = new Object();
    private static LoadLibrary auth = null;

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

//    public LoadLibrary(){
//        //Do nothing
//    }
    private LoadLibrary() {
        //Do nothing
    }

    public static LoadLibrary getBiometricDevice() {
        if (auth == null) {
            auth = new LoadLibrary();
        }
        return auth;
    }
    public static boolean isMorphoConnected(){
    	boolean retval = false;
    	try{
    		synchronized (lock) {
    			LoadLibrary auth = getBiometricDevice();
    			retval = auth.isDeviceConnected();
			}
    	}catch(Exception ex){
    		ex.printStackTrace();
    	}
    	return retval;
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
    
    
    public static void main(String[] str){
    	LoadLibrary auth = getBiometricDevice();
    	synchronized (auth) {
    		auth.captureFPData(0);
    		byte[] data = auth.getCapturedTemplate();
    		System.out.println();
		}
    }
}
