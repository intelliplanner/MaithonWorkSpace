package com.ipssi.rfid.controller;

public interface ControllerI {
	void clearInputs();
	boolean save();
	void hideControls(boolean isTrue);
	void enableControls(boolean isTrue);
	void getFocus();
}
