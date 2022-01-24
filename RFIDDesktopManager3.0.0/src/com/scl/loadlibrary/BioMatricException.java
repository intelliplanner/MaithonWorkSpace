/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.scl.loadlibrary;

/**
 *
 * @author Vi$ky
 */
public class BioMatricException {

    public static String getException(int exceptionCode) {
        String errorMsg = "";
         if (exceptionCode == -1) {
            errorMsg = "Device Not Connected Please Connect And Restart Application";
        }
         else if (exceptionCode == -1) {
            errorMsg = "Communication protocol error";
        } else if (exceptionCode == -2) {
            errorMsg = "";
        } else if (exceptionCode == -3) {
            errorMsg = "Device Not Connected";
        } else if (exceptionCode == -4) {
            errorMsg = "";
        } else if (exceptionCode == -5) {
            errorMsg = "";
        } else if (exceptionCode == -6) {
            errorMsg = "";
        } else if (exceptionCode == -7) {
            errorMsg = "";
        } else if (exceptionCode == -8) {
            errorMsg = "Finger Print Not Exist";//Authentication or Identification failed 
        } else if (exceptionCode == -9) {
            errorMsg = "";
        } else if (exceptionCode == -10) {
            errorMsg = "";
        } else if (exceptionCode == -11) {
            errorMsg = "Morpho database is empty";
        } else if (exceptionCode == -12) {
            errorMsg = " User has already Exist or Enrolled!!! ";
        } else if (exceptionCode == -13) {
            errorMsg = " Morpho specified base does not exist ";
        } else if (exceptionCode == -14) {
            errorMsg = "";
        } else if (exceptionCode == -15) {
            errorMsg = "";
        } else if (exceptionCode == -16) {
            errorMsg = "";
        } else if (exceptionCode == -17) {
            errorMsg = "Please Put Your Finger on Scanner Device!!!";
        } else if (exceptionCode == -18) {
            errorMsg = "";
        } else if (exceptionCode == -19) {
            errorMsg = "Please Put Your Finger on Scanner Device!!!";
        } else if (exceptionCode == -20) {
            errorMsg = "";
        } else if (exceptionCode == -21) {
            errorMsg = "";
        } else if (exceptionCode == -22) {
            errorMsg = "";
        } else if (exceptionCode == -23) {
            errorMsg = "";
        } else if (exceptionCode == -24) {
            errorMsg = "";
        } else if (exceptionCode == -25) {
            errorMsg = "";
        } else if (exceptionCode == -26) {
            errorMsg = "";
        } else if (exceptionCode == -27) {
            errorMsg = "";
        } else if (exceptionCode == -28) {
            errorMsg = "";
        } else if (exceptionCode == -29) {
            errorMsg = "";
        } else if (exceptionCode == -23) {
            errorMsg = "";
        } else if (exceptionCode == -30) {
            errorMsg = "User Id already exists !!!";
        } else {
            errorMsg = "check Log File";
        }
        return errorMsg;

    }
}
