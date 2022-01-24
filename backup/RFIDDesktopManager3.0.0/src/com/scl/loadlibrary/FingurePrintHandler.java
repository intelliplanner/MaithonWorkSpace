package com.scl.loadlibrary;

import java.sql.Connection;

import com.ipssi.rfid.beans.Biometric;

public interface FingurePrintHandler {
	void onChange(Connection conn, Biometric driver, boolean isFingerVerified, boolean isFingerCaptured);
	int promptMessage(String message,Object[] options);
	void showMessage(String message);
        void statusChange(boolean status);
        void setDebugTemplate(byte[] data);
}
