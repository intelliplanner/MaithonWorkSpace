package com.scl.loadlibrary;

import morpho.morphosmart.sdk.api.MorphoImage;
import morpho.morphosmart.sdk.demo.ennum.EnumMoveFinger;

public interface MorphoActionI {
	void setBorderColorGreen(short fingerNumber, short step);
	
	void setInstruction(String intruction);

	void setStepsImage(EnumMoveFinger move);

	void fingerOk();

	void playVideo(boolean isFingerFvpDetected);

	void setLiveImage(MorphoImage morphoImage);
	
	void setLiveStepImage(MorphoImage morphoImage, short fingerNumber, short step);

	void setCurrentImageInfo(int nbCol, int nbRow, int resX, int resY, int bitPerPixel);

	void setScore(short quality);
	
	void setCodeQuality(short codeQuality);
	
	void setDetectedQuality(short detectedQuality);
}
