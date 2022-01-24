/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.scl.loadlibrary;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

import com.ipssi.gen.utils.Misc;
import com.ipssi.rfid.beans.Biometric;

/**
 *
 * @author Vi$ky
 */
public class DriverDetailFlyAsh {

    private static byte[] capturedTemplate;
    private static LoadLibrary auth = null;
    private static boolean isDriverLoaded = false;
    private static int confirmationVal = Misc.getUndefInt();
    private static String msg = "User Not Exist, Do you want to  capture again ?";
    private static Object[] options = {"  Recapture  ", "  Continue  "};
    
    public static void checkDriverExistance(Connection conn) throws Exception {
        //int enrollmentId = Misc.getUndefInt();
//        if (!isDriverLoaded || !auth.isDeviceConnected()) {
//            auth = new LoadLibrary();
//            isDriverLoaded = true;
//        }
       synchronized (LoadLibrary.lock) {
    	if (!isDriverLoaded || !auth.isDeviceConnected()) {
            auth = LoadLibrary.getBiometricDevice();
            isDriverLoaded = true;
        }
        boolean deviceConnected = auth.isDeviceConnected();
        System.out.println("deviceConnected" + deviceConnected);
        if (!deviceConnected) {
            JOptionPane.showMessageDialog(null,
                    "Device is not Connected, Please Restart Application and Unplugged Device !!!");
            return;
        }
        boolean continueCapture = true;
        
        while (continueCapture) {/*
                
            GateInFlyAshWindow.dlNo.setText("");
            GateInFlyAshWindow.driverName.setText("");
            GateInFlyAshWindow.helperOk.clearSelection();
            GateInFlyAshWindow.photo.setIcon(null);
            int enrollmentId = Misc.getUndefInt();
            String userId = "";
            enrollmentId = auth.identify(20);
            userId = auth.getUserId();
            System.out.println("userid : " + userId + " enrollmentId : " + enrollmentId);
            //  continueCapture = false;
            if (enrollmentId == 0) {//finger Exist
                continueCapture = false;
                GateInFlyAshWindow.isFingerVerified = true;
                GateInFlyAshWindow.isFingerCaptured = true;

                String splitId[] = userId.split("_");

                //driverBean = GateInDao.getDriverDetail(splitId[0]);
                GateInFlyAshWindow.driverInformation = GateInDao.getDriverDetail(conn,splitId[0]);
                if (GateInFlyAshWindow.driverInformation != null) {
                        DriverDetailFlyAsh.showDriverDetail(GateInFlyAshWindow.driverInformation);
                        GateInFlyAshWindow.quickCreate.requestFocusInWindow();
                        GateInFlyAshWindow.driverName.setText(GateInFlyAshWindow.driverInformation .getDriver_name());
                    
                } else {
                    confirmationVal = ConfirmationDialog.getDialogBox(new javax.swing.JFrame(), true, options, msg);
                    if (confirmationVal == 0) {
                        continueCapture = true;
                    } else if (confirmationVal == 1) {
                        continueCapture = false;
                        
                        GateInFlyAshWindow.driverId.setEditable(true);
                        GateInFlyAshWindow.driverId.setFocusable(true);
                        GateInFlyAshWindow.driverId.requestFocusInWindow();
                        GateInFlyAshWindow.driverId.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
                        
                        GateInFlyAshWindow.dlNo.setEditable(true);
                        GateInFlyAshWindow.dlNo.setFocusable(true);
//                        GateInFlyAshWindow.dlNo.requestFocusInWindow();
                        GateInFlyAshWindow.dlNo.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
                        GateInFlyAshWindow.captureButton.setVisible(false);
                        return;
                    }
                }
            } else if (enrollmentId == -8) {// finger print not exist
                GateInFlyAshWindow.isFingerVerified = false;
                confirmationVal = ConfirmationDialog.getDialogBox(new javax.swing.JFrame(), true, options, msg);
                if (confirmationVal == 0) {
                    continueCapture = true;
                } else if (confirmationVal == 1) {
                    continueCapture = false;
                   
                     GateInFlyAshWindow.driverId.setEditable(true);
                        GateInFlyAshWindow.driverId.setFocusable(true);
                        GateInFlyAshWindow.driverId.requestFocusInWindow();
                        GateInFlyAshWindow.driverId.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
                        
                    
                    GateInFlyAshWindow.dlNo.setEditable(true);
                    GateInFlyAshWindow.dlNo.setFocusable(true);
//                    GateInFlyAshWindow.dlNo.requestFocusInWindow();
                    GateInFlyAshWindow.dlNo.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
                    GateInFlyAshWindow.captureButton.setVisible(false);
                    return;
                }

            } else {
                String exceptionMsg = BioMatricException.getException(enrollmentId);
                JOptionPane.showMessageDialog(null, exceptionMsg);
                System.out.println(" \t\n Error_Code : " + enrollmentId + " Exception : " + exceptionMsg);
                continueCapture = false;
                if (enrollmentId == -11) {
                    
                     GateInFlyAshWindow.driverId.setEditable(true);
                        GateInFlyAshWindow.driverId.setFocusable(true);
                        GateInFlyAshWindow.driverId.requestFocusInWindow();
                        GateInFlyAshWindow.driverId.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
                        
                    
                    GateInFlyAshWindow.dlNo.setEditable(true);
                    GateInFlyAshWindow.dlNo.setFocusable(true);
//                    GateInFlyAshWindow.dlNo.requestFocusInWindow();
                    GateInFlyAshWindow.dlNo.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
                    GateInFlyAshWindow.captureButton.setVisible(false);
                }
            }

        */}
    }
    }
    public static BufferedImage byteArrayToImage(byte[] bytes) {

        BufferedImage bufferedImage = null;
        try {
            InputStream inputStream = new ByteArrayInputStream(bytes);
            bufferedImage = ImageIO.read(inputStream);
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
        return bufferedImage;
    }
    public static void showDriverDetail(Biometric driverBean) throws Exception {/*
        GateInFlyAshWindow.dlNo.setText(driverBean.getDl_no());
        GateInFlyAshWindow.driverId.setText(Integer.toString(driverBean.getDriver_id()));
        GateInFlyAshWindow.driverName.setText(driverBean.getDriver_name());
        if (driverBean.getPhoto() != null) {
            BufferedImage image = byteArrayToImage(driverBean.getPhoto());
            GateInFlyAshWindow.photo.setIcon(new ImageIcon(image));
        }
        if (driverBean.getDriver_type() == 1) {
            GateInFlyAshWindow.helperNo.setSelected(true);
        } else {
            GateInFlyAshWindow.helperYes.setSelected(true);
            JOptionPane.showMessageDialog(null, "Please Inform to Driver to take his Seat and then Re-Authenticate !!!");
        }
        if (driverBean.getIsFingerCaptured() == 1) {
            GateInFlyAshWindow.isFingerCaptured = true;
        } else {
            GateInFlyAshWindow.isFingerCaptured = false;
        }
    */}
}
