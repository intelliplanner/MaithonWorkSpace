/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.scl.loadlibrary;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import com.ipssi.gen.utils.DBConnectionPool;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.Pair;
import com.ipssi.gen.utils.Triple;
import com.ipssi.rfid.beans.Biometric;
import com.ipssi.rfid.constant.Constant;
import com.ipssi.rfid.constant.Status;
import com.ipssi.rfid.database.GateInDao;
import com.ipssi.rfid.integration.WaveFormPlayer;
import com.ipssi.rfid.ui.ConfirmationDialog;
import com.ipssi.rfid.ui.CoalGateInWindow;
import java.sql.Connection;
import java.sql.SQLException;

;

/**
 *
 * @author Vi$ky
 */
public class DriverDetail {

    private static byte[] capturedTemplate;
    private static LoadLibrary auth = null;
    private static boolean isDriverLoaded = false;
    private static int confirmationVal = Misc.getUndefInt();
    private static String msg = "User Not Exist, Do you want to  capture again ?";
    private static Object[] options = {"  Recapture  ", "  Continue  "};
//    private static int fingerVoice = 64;

    public static Triple<Biometric,Boolean,Boolean > captureFingurePrint(MorphoActionI morphoActionI) throws SQLException {
        boolean isFingerCaptured = false;
        boolean isFingerVerified = false;
        Biometric driverInformation = null;
        Connection conn = null;
        boolean destroyIt = false;
        try{
        	conn = DBConnectionPool.getConnectionFromPoolNonWeb();
        	synchronized (LoadLibrary.lock) {
        		if (!isDriverLoaded || !auth.isDeviceConnected()) {
        			auth = LoadLibrary.getBiometricDevice();
        			isDriverLoaded = true;
        		}
        		boolean deviceConnected = auth.isDeviceConnected();
        		System.out.println("deviceConnected" + deviceConnected);
        		if (!deviceConnected) {
        			return null;
        		}
        		WaveFormPlayer.playSoundIn(Status.TPRQuestion.startFingerCapturing);
        		boolean continueCapture = true;
        		int noOfTimes = 0;
        		while (continueCapture) {
        			noOfTimes++;
        			int enrollmentId = Misc.getUndefInt();
        			String userId = "";
        			// play voice
        			enrollmentId = auth.identify(20);
        			userId = auth.getUserId();
        			System.out.println("userid : " + userId + " enrollmentId : " + enrollmentId);
        			isFingerCaptured = true;
        			if (enrollmentId == 0) {//finger Exist
        				continueCapture = false;
        				isFingerVerified = true;
        				String splitId[] = userId.split("_");
        				driverInformation = GateInDao.getDriverDetail(conn, splitId[0]);
        			} else if (enrollmentId == -8) {// finger print not exist
        				isFingerVerified = false;
        				WaveFormPlayer.playSoundOut(Status.TPRQuestion.fingerNotMatch);
        				isFingerVerified = false;
        				confirmationVal = ConfirmationDialog.getDialogBox(new javax.swing.JFrame(), true, options, msg);
        				if (confirmationVal == 0) {
        					// clean finger msg
        					WaveFormPlayer.playSoundOut(Status.TPRQuestion.tryAgainFinger);
        					continueCapture = true;
        				} else if (confirmationVal == 1) {
        					WaveFormPlayer.playSoundOut(Status.TPRQuestion.fingerNotMatch);
        					continueCapture = false;
        					break;
        				}

        			} else {
        				WaveFormPlayer.playSoundOut(Status.TPRQuestion.fingerNotMatch);
        				String exceptionMsg = BioMatricException.getException(enrollmentId);
        				System.out.println(" \t\n Error_Code : " + enrollmentId + " Exception : " + exceptionMsg);
        				continueCapture = false;
        				break;
        			}

        		}
        	}
        }catch(Exception ex){
        	ex.printStackTrace();
        	destroyIt = true;
        }finally{
        	try{
   			 DBConnectionPool.returnConnectionToPoolNonWeb(conn, destroyIt);
   		 }catch(Exception ex){
   			 ex.printStackTrace();
   		 }
        }
        return new Triple<Biometric, Boolean, Boolean>(driverInformation, isFingerVerified, isFingerCaptured);
    }

    public static BufferedImage byteArrayToImage(byte[] bytes) {

        BufferedImage bufferedImage = null;
        try {
            InputStream inputStream = new ByteArrayInputStream(bytes);
            bufferedImage = ImageIO.read(inputStream);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return bufferedImage;
    }

    public static void showDriverDetail(Biometric driverBean) throws Exception {
        /*CoalGateInWindow.dlNo.setText(driverBean.getDl_no());
        CoalGateInWindow.driverId.setText(Misc.getPrintableInt(driverBean.getDriver_id()));
        CoalGateInWindow.driverName.setText(driverBean.getDriver_name());
        if (driverBean.getPhoto() != null) {
            BufferedImage image = byteArrayToImage(driverBean.getPhoto());
            CoalGateInWindow.photo.setIcon(new ImageIcon(image));
        }
        if (driverBean.getDriver_type() == 1) {
            CoalGateInWindow.helperNo.setSelected(true);
        } else {
//            JOptionPane.showMessageDialog(null, "Please Inform to Driver to take his Seat and then Re-Authenticate !!!");
            CoalGateInWindow.helperYes.setSelected(true);
        }
        if (driverBean.getIsFingerCaptured() == 1) {
            CoalGateInWindow.isFingerCaptured = true;
        } else {
            CoalGateInWindow.isFingerCaptured = false;
        }*/
    }
    public static Triple<Biometric,Boolean,Boolean > captureFingurePrintSDK(MorphoActionI morphoActionI) throws SQLException {
        boolean isFingerCaptured = false;
        boolean isFingerVerified = false;
        Biometric driverInformation = null;
        Connection conn = null;
        boolean destroyIt = false;
        try{
        	conn = DBConnectionPool.getConnectionFromPoolNonWeb();
        	boolean deviceConnected = MorphoSmartFunctions.getMorpho().isConnected();
        	System.out.println("deviceConnected" + deviceConnected);
        	if (!deviceConnected) {
        		return null;
        	}
        	MorphoSmartFunctions morpho = null;
        	synchronized (MorphoSmartFunctions.lock) {
        		morpho = MorphoSmartFunctions.getMorpho();
        		WaveFormPlayer.playSoundIn(Status.TPRQuestion.startFingerCapturing);
        		boolean continueCapture = true;
        		while (continueCapture) {
        			int enrollmentId = Misc.getUndefInt();
        			// play voice
        			Pair<Integer, Integer> identify = morpho.identify(morphoActionI);
        			enrollmentId = identify.first;
        			int userId = identify.second;
        			System.out.println("userid : " + userId + " enrollmentId : " + enrollmentId);
        			isFingerCaptured = true;
        			if (enrollmentId == 0 && !Misc.isUndef(userId)) {//finger Exist
        				continueCapture = false;
        				isFingerVerified = true;
        				driverInformation = GateInDao.getDriverDetail(conn, userId+"");
        			} else if (enrollmentId == -8) {// finger print not exist
        				isFingerVerified = false;
        				WaveFormPlayer.playSoundOut(Status.TPRQuestion.fingerNotMatch);
        				isFingerVerified = false;
        				confirmationVal = ConfirmationDialog.getDialogBox(new javax.swing.JFrame(), true, options, msg);
        				if (confirmationVal == 0) {
        					// clean finger msg
        					WaveFormPlayer.playSoundOut(Status.TPRQuestion.tryAgainFinger);
        					continueCapture = true;
        				} else if (confirmationVal == 1) {
        					WaveFormPlayer.playSoundOut(Status.TPRQuestion.fingerNotMatch);
        					continueCapture = false;
        					break;
        				}

        			} else {
        				WaveFormPlayer.playSoundOut(Status.TPRQuestion.fingerNotMatch);
        				String exceptionMsg = BioMatricException.getException(enrollmentId);
        				System.out.println(" \t\n Error_Code : " + enrollmentId + " Exception : " + exceptionMsg);
        				continueCapture = false;
        				break;
        			}

        		}
        	}
        }catch(Exception ex){
        	ex.printStackTrace();
        	destroyIt = true;
        }finally{
        	try{
   			 DBConnectionPool.returnConnectionToPoolNonWeb(conn, destroyIt);
   		 }catch(Exception ex){
   			 ex.printStackTrace();
   		 }
        }
        return new Triple<Biometric, Boolean, Boolean>(driverInformation, isFingerVerified, isFingerCaptured);
    }
    
    
    private  void setDriverHours() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
