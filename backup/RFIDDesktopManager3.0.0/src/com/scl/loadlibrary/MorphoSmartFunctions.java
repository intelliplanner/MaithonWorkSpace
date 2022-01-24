package com.scl.loadlibrary;

import java.sql.Connection;
import java.util.ArrayList;

import morpho.morphosmart.sdk.api.IMorphoEventHandler;
import morpho.morphosmart.sdk.api.MorphoCallbackCommand;
import morpho.morphosmart.sdk.api.MorphoCompressAlgo;
import morpho.morphosmart.sdk.api.MorphoDatabase;
import morpho.morphosmart.sdk.api.MorphoDatabase.MorphoTypeDeletion;
import morpho.morphosmart.sdk.api.MorphoDevice;
import morpho.morphosmart.sdk.api.MorphoFAR;
import morpho.morphosmart.sdk.api.MorphoFVPTemplateType;
import morpho.morphosmart.sdk.api.MorphoImage;
import morpho.morphosmart.sdk.api.MorphoSmartSDK;
import morpho.morphosmart.sdk.api.MorphoTemplateEnvelope;
import morpho.morphosmart.sdk.api.MorphoTemplateList;
import morpho.morphosmart.sdk.api.MorphoTemplateType;
import morpho.morphosmart.sdk.api.MorphoUser;
import morpho.morphosmart.sdk.demo.dialog.DialogUtils;
import morpho.morphosmart.sdk.demo.ennum.EnrollmentType;
import morpho.morphosmart.sdk.demo.ennum.EnumMoveFinger;
import morpho.morphosmart.sdk.demo.ennum.LatentDetection;
import morpho.morphosmart.sdk.demo.ennum.MorphoUserFields;
import morpho.morphosmart.sdk.demo.ennum.TemplateFVPType;
import morpho.morphosmart.sdk.demo.ennum.TemplateType;
import morpho.morphosmart.sdk.demo.trt.ErrorsMgt;

import com.ipssi.fingerprint.utils.SyncFingerPrintDeviceHelper;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.Pair;
import com.ipssi.gen.utils.Triple;

public class MorphoSmartFunctions {  
	static MorphoDevice morphoDevice = null;    
	static MorphoDatabase morphoDatabase = null;
	private static boolean isConnected = false; 
	static MorphoTemplateType tempType = MorphoTemplateType.MORPHO_PK_COMP;
	static int index = 0;
	public static Object lock = new Object();
	private static final int taskTimeOut = 20;//seconds
	private static final int identifyTimeout = 0;//seconds
	private static MorphoSmartFunctions morpho = null;
	static{
		try    {       
			System.loadLibrary("MorphoSmartSDKJavaWrapper");       
			if (System.getProperty("os.name").startsWith("Windows"))       {          
				// load MSOSECU library on Windows ONLY          
				System.loadLibrary("MSOSECUJavaWrapper"); 
				init();
			}    
		}    catch (UnsatisfiedLinkError e)    {       
			DialogUtils.showErrorMessage("Load Library Error", "Native code library failed to load." + " See the chapter on Dynamic Linking Problems in the SWIG Java documentation for help.\nError: " + e.getMessage()+"\n"+System.getProperty("java.library.path"));
		} 
	}
	public static MorphoSmartFunctions getMorpho() {
	        if (morpho == null) {
	        	morpho =  new MorphoSmartFunctions();
	        }
	        return morpho;
	    }
	 private MorphoSmartFunctions(){
	 }
	 private static void init(){
		 morphoDevice = new MorphoDevice();    
		 morphoDatabase = new MorphoDatabase();
		 try{
			 int ret = morphoDevice.openDevice(-1);    
			 // Get a database instance    
			 if(ret == MorphoSmartSDK.MORPHO_OK)    
			 {      
				 ret = morphoDevice.getDatabase((short) 0, "", morphoDatabase);    
				 isConnected  = ret == MorphoSmartSDK.MORPHO_OK;
			 }else{
				 isConnected = false;
			 }

		 }catch(Exception ex){
			 ex.printStackTrace();
		 }
	 }
	 public int remove(int userId){
		 int ret = Misc.getUndefInt();
		 if(!isConnected())
			 return ret;
		 try{
			 MorphoUser user = new MorphoUser();
			 ret = morphoDatabase.getUser(userId+"", user);
			 if(ret == MorphoSmartSDK.MORPHO_OK)
				 ret = user.dbDelete();
		 }catch(Exception ex){
			 ex.printStackTrace();
		 }
		 return ret;
	 }
	public int getCapacity(){
		int retval = Misc.getUndefInt();
		int ret = Misc.getUndefInt();
		int[] totalCount = new int[1];
		if(!isConnected())
			return ret;
		try{
			ret = morphoDatabase.getNbTotalRecord(totalCount);
			if(ret == MorphoSmartSDK.MORPHO_OK && totalCount != null && totalCount.length > 0)
				retval = totalCount[0];
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return retval;
	}
	public int getTotalEnrolled(){
		int retval = Misc.getUndefInt();
		int ret = Misc.getUndefInt();
		int[] totalCount = new int[1];
		if(!isConnected())
			return ret;
		try{
			ret = morphoDatabase.getNbUsedRecord(totalCount);
			if(ret == MorphoSmartSDK.MORPHO_OK && totalCount != null && totalCount.length > 0)
				retval = totalCount[0];
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return retval;
	}
	public int getFreeSpace(){
		int retval = Misc.getUndefInt();
		int ret = Misc.getUndefInt();
		int[] totalCount = new int[1];
		if(!isConnected())
			return ret;
		try{
			ret = morphoDatabase.getNbFreeRecord(totalCount);
			if(ret == MorphoSmartSDK.MORPHO_OK && totalCount != null && totalCount.length > 0)
				retval = totalCount[0];
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return retval;
	}
	public int removeAll(){
		int ret = Misc.getUndefInt();
		try{
			ret = isConnected() ? morphoDatabase.dbDelete(MorphoTypeDeletion.MORPHO_ERASE_BASE) : Misc.getUndefInt();
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return ret;
	}
	public boolean isConnected(){
		return isConnected; 
	}
	public String  getDeviceId(){
		if(!isConnected)
			return null;
		String retval = null;
		String[] secuSerialNumber = { "" };
		byte[] secuConfig = { 0 };
		MorphoFAR[] secuFar = { MorphoFAR.MORPHO_FAR_0 };
		long[] secuMinMSL = { 0 };
		int ret = morphoDevice.getSecuConfig(secuSerialNumber, secuConfig, secuFar, secuMinMSL);
		if(secuSerialNumber != null && secuSerialNumber.length > 0 && secuSerialNumber[0] != null && secuSerialNumber[0].length() > 0){
			String[] deviceIdPair = secuSerialNumber[0].split("-");
			if(deviceIdPair != null && deviceIdPair.length > 1){
				retval = deviceIdPair[1];
			}
		}
		return retval; 
	}
	public int getMorphoDBDeviceId(Connection conn){
		return SyncFingerPrintDeviceHelper.isExist(conn, getDeviceId());
	}
	public int enroll(String userId,ArrayList<byte[]> fingerPrintTemplateList) 
	{   
		int ret = Misc.getUndefInt();
		// Loading MorphoSDK and MSOSECU libraries    
		if(!isConnected || fingerPrintTemplateList == null || fingerPrintTemplateList.size() <= 0 )
			return ret;
		try{
			int is=fingerPrintTemplateList == null ? 0 : (fingerPrintTemplateList.size() <= 2 ? fingerPrintTemplateList.size() : 2); 
			if(is <= 0 || (fingerPrintTemplateList.get(0) == null))
				return ret;
			MorphoUser user = new MorphoUser();
			ret = morphoDatabase.getUser(userId, user);
			if(ret == MorphoSmartSDK.MORPHO_OK)
				ret = user.dbDelete();
			ret = morphoDatabase.getUser(userId, user);
			ret = user.putField(MorphoUserFields.ID_FIELD_INDEX.getValue(), userId);
			ret = user.putField(MorphoUserFields.FIRSTNAME_FIELD_INDEX.getValue(), "");
			ret = user.putField(MorphoUserFields.LASTTNAME_FIELD_INDEX.getValue(), "");
			/*if(index >= MorphoTemplateType.values().length)
			return Misc.getUndefInt();
		tempType = MorphoTemplateType.values()[index++];*/
			int fingerCount = 0;
			for(int i=0;i<is;i++){
				byte[] template = fingerPrintTemplateList.get(i);
				if(template == null)
					continue;
				short[] indexTemplate = { (short)fingerCount++ };
				ret = user.putTemplate(
						MorphoTemplateType.MORPHO_PK_ISO_FMR, //tempType,//MorphoTemplateType.MORPHO_PK_COMP, 
						template,
						(short) 0xFF, 
						(short) 0,
						indexTemplate
						);
			}
			user.setNoCheckOnTemplateForDBStore(true);
			user.setTemplateUpdateMask(0);
			ret = user.dbStore();
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return ret; 
	}
	public  Pair<String, String> identifyUser(String userId, ArrayList<byte[]> fingerPrintTemplateList){
		int is=fingerPrintTemplateList == null ? 0 : (fingerPrintTemplateList.size() <= 2 ? fingerPrintTemplateList.size() : 2); 
		int ret = Misc.getUndefInt();
		String firstMatch = null;
		String secondMatch = null;
		if(is <= 0)
			return new Pair<String, String>(firstMatch, secondMatch);
		int fingerCount = 0;
		MorphoTemplateList templateList1 = new MorphoTemplateList();
		MorphoTemplateList templateList2 = new MorphoTemplateList();
		for(int i=0;i<is;i++){
			byte[] template = fingerPrintTemplateList.get(i);
			if(template == null)
				continue;
			short[] indexTemplate = { (short)fingerCount++ };
			if(i==0){
				ret = templateList1.putTemplate(
						MorphoTemplateType.MORPHO_PK_ISO_FMR, //tempType,//MorphoTemplateType.MORPHO_PK_COMP, 
						fingerPrintTemplateList.get(i),
						(short) 0xFF, 
						(short) 0,
						indexTemplate
						);
			}else{
				ret = templateList2.putTemplate(
						MorphoTemplateType.MORPHO_PK_ISO_FMR, //tempType,//MorphoTemplateType.MORPHO_PK_COMP, 
						fingerPrintTemplateList.get(i),
						(short) 0xFF, 
						(short) 0,
						indexTemplate
						);
			}
			if(fingerCount == 2)
				break;
		}
		MorphoUser f1Users = new MorphoUser();
		MorphoUser f2Users = new MorphoUser();
		long[] matchingScore = new long[1];
		short[] matchedFingerIndex = new short[1];

		ArrayList<String> fields1 = new ArrayList<String>();
		ArrayList<String> fields2 = new ArrayList<String>();
		ret = morphoDatabase.identifyMatch(MorphoFAR.MORPHO_FAR_5, templateList1, f1Users, matchingScore, matchedFingerIndex);
		if(f1Users != null && ret == MorphoSmartSDK.MORPHO_OK){
			f1Users.getField(0, fields1);
			if(fields1 != null && fields1.size() > 0)
				firstMatch = fields1.get(0);
		}
		if(fingerCount > 1){
			ret = morphoDatabase.identifyMatch(MorphoFAR.MORPHO_FAR_5, templateList2, f2Users, matchingScore, matchedFingerIndex);
			if(f2Users != null && ret == MorphoSmartSDK.MORPHO_OK){
				f2Users.getField(0, fields2);
				if(fields2 != null && fields2.size() > 0)
					secondMatch = fields2.get(0);
			}
		}
		System.out.println("Identify Result["+userId+"]:("+ret+","+firstMatch+","+secondMatch+")");
		return new Pair<String, String>(firstMatch, secondMatch);
	}
	public  Pair<Integer, Integer> identify(MorphoActionI morphoActionI){
		int userId = Misc.getUndefInt();
		int status = Misc.getUndefInt();
		MorphoUser morphoUser = new MorphoUser();
		if(!isConnected())
			return new Pair<Integer, Integer>(status, userId);
		IMorphoEventHandler callback = new MorphoEventHandler(morphoActionI);
		int callbackMask = MorphoCallbackCommand.MORPHO_CALLBACK_IMAGE_CMD.swigValue() | MorphoCallbackCommand.MORPHO_CALLBACK_ENROLLMENT_CMD.swigValue() | MorphoCallbackCommand.MORPHO_CALLBACK_COMMAND_CMD.swigValue() | MorphoCallbackCommand.MORPHO_CALLBACK_CODEQUALITY.swigValue() | MorphoCallbackCommand.MORPHO_CALLBACK_DETECTQUALITY.swigValue();
		int ret = morphoDatabase.identify(identifyTimeout,                      // infinite timeout 
				MorphoFAR.MORPHO_FAR_5, // recommended value 
				callbackMask,                      // no asynchronous messages
				callback,                   // no callback
				morphoUser,             // user instance
				null,                      
				null,                      
				MorphoSmartSDK.MORPHO_DEFAULT_CODER,
				MorphoSmartSDK.MORPHO_VERIF_DETECT_MODE,
				0,
				1                    
				);    
		status = ret;
		if(ret == MorphoSmartSDK.MORPHO_OK)    {      
			ArrayList<String> dataField = new ArrayList<String>();      
			ret = morphoUser.getField(                                 
					MorphoUserFields.ID_FIELD_INDEX.getValue(),
					dataField
					);    
			if(dataField != null && dataField.size() > 0){
				userId = Misc.getParamAsInt(dataField.get(0));
			}
		}
		return new Pair<Integer, Integer>(status, userId);
	}

	public int cancelAllCurrentTask(){
		int ret = Misc.getUndefInt();
		try{
			ret = isConnected() ? morphoDevice.cancelLiveAcquisition() : Misc.getUndefInt();
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return ret;
	}
	public static void main(String[] arg){
		byte[] data = getMorpho().getTemplate();
		System.out.println();
		//getMorpho().removeAll();
	}
	public Triple<Integer, byte[], byte[]> capture(MorphoActionI morphoActionI){
		int captureResult = Misc.getUndefInt();
		byte[] templateOne = null;
		byte[] templateTwo = null;
		if(!isConnected())
			return new Triple<Integer, byte[], byte[]>(captureResult, templateOne, templateTwo);
			try{
				int ret = Misc.getUndefInt();
				int timeout = taskTimeOut;
				short advancedSecurityLevelsRequired = 0;
				short fingerNumber = 2;
				MorphoTemplateType templateType = MorphoTemplateType.MORPHO_PK_ISO_FMR;
				MorphoFVPTemplateType templateFVPType = MorphoFVPTemplateType.MORPHO_NO_PK_FVP;
				int maxSizeTemplate = 255;
				short enrolType = (short) EnrollmentType.THREE_ACQUISITIONS.getValue();
				long callbackMask = MorphoCallbackCommand.MORPHO_CALLBACK_IMAGE_CMD.swigValue() | MorphoCallbackCommand.MORPHO_CALLBACK_ENROLLMENT_CMD.swigValue() | MorphoCallbackCommand.MORPHO_CALLBACK_COMMAND_CMD.swigValue() | MorphoCallbackCommand.MORPHO_CALLBACK_CODEQUALITY.swigValue() | MorphoCallbackCommand.MORPHO_CALLBACK_DETECTQUALITY.swigValue();
				MorphoTemplateList templateList = new MorphoTemplateList();
				MorphoTemplateEnvelope typEnvelop = MorphoTemplateEnvelope.MORPHO_RAW_TEMPLATE;
				byte[] applicationData = null;
				short latentDetection = (short)LatentDetection.LATENT_DETECT_DISABLE.getValue();
				int coderChoice = 0;
				long detectModeChoice = MorphoSmartSDK.MORPHO_ENROLL_DETECT_MODE;
				short acquisitionThreshold = 100;
				int securityLevel = MorphoSmartSDK.FFD_SECURITY_LEVEL_DEFAULT_HOST;;
				morphoDevice.setSecurityLevel(securityLevel);
				TemplateType templateFp = TemplateType.MORPHO_PK_ISO_FMR;
				TemplateFVPType templateFvp = TemplateFVPType.MORPHO_NO_PK_FVP;
				IMorphoEventHandler callback = new MorphoEventHandler(morphoActionI);

				captureResult = morphoDevice.capture(
						timeout, 
						acquisitionThreshold, 
						advancedSecurityLevelsRequired, 
						fingerNumber, 
						templateType, 
						templateFVPType, 
						maxSizeTemplate, 
						enrolType, 
						callbackMask, 
						callback, 
						templateList, 
						typEnvelop, 
						applicationData, 
						latentDetection, 
						coderChoice, 
						detectModeChoice, 
						(short) 0, 
						MorphoCompressAlgo.MORPHO_NO_COMPRESS, 
						(short) 0);
				if (captureResult != MorphoSmartSDK.MORPHO_OK) {
					String message1 = "Capture Failed";
					String message2 = "";
					String message3 = "";
					String message4 = "";
					switch (ret) {
					case MorphoSmartSDK.MORPHOERR_NO_HIT:
						message2 = "Bad Capture Sequence.";
						break;
					case MorphoSmartSDK.MORPHOERR_CMDE_ABORTED:
						message2 = "Command aborted by user.";
						break;
					case MorphoSmartSDK.MORPHOERR_TIMEOUT:
						message2 = "Timeout has expired.";
						message3 = "Command aborted.";
						break;
					case MorphoSmartSDK.MORPHOERR_FFD:
						message2 = "False finger detected !!!";
						break;
					case MorphoSmartSDK.MORPHOERR_MOIST_FINGER:
						message2 = "Finger too moist !!!";
						break;
					case MorphoSmartSDK.MORPHOERR_BADPARAMETER:
						message2 = "One or more input parameters are out of range";
						break;
					case MorphoSmartSDK.MORPHOERR_LICENSE_MISSING:
						message2 = "A required license is missing.";
						break;
					case MorphoSmartSDK.MORPHOERR_ADVANCED_SECURITY_LEVEL_NOT_AVAILABLE:
						message2 = "Cannot make a multimodal template compatible with advanced security levels.";
						break;
					case MorphoSmartSDK.MORPHOERR_ADVANCED_SECURITY_LEVEL_MISMATCH:
						message2 = "Failed to make a multimodal template compatible with advanced security levels.";
						break;
					case MorphoSmartSDK.MORPHOERR_BAD_FINAL_FINGER_PRINT_QUALITY:
						message2 = "Failed to reach the fingerprint quality threshold.";
						break;
					default:
						message2 = "An error occured while calling";
						message3 = "MorphoDevice.capture() function";
						message4 = ErrorsMgt.convertSDKError(ret);
						break;
					}
					//new DialogResultWindow(null, message1, message2, message3, message4, "").setVisible(true);
				} else {
					short[] nbTemplate = { 0 };
					templateList.getNbTemplate(nbTemplate);
					MorphoTemplateType[] typTemplate = { MorphoTemplateType.MORPHO_PK_ISO_FMR };
					ArrayList<Byte> dataTemplate = new ArrayList<Byte>();
					short[] pkFpQuality = { 0 };
					short[] dataIndex = { 0 };
					ret = templateList.getTemplate((short) 0, typTemplate, dataTemplate, pkFpQuality, dataIndex);
					if(ret == MorphoSmartSDK.MORPHO_OK){
						templateOne = FingerPrintAction.toByteArray(dataTemplate);
					}
					dataTemplate.clear();
					ret = templateList.getTemplate((short) 1, typTemplate, dataTemplate, pkFpQuality, dataIndex);
					if(ret == MorphoSmartSDK.MORPHO_OK){
						templateTwo = FingerPrintAction.toByteArray(dataTemplate);
					}
				}
			}catch(Exception ex){
			ex.printStackTrace();
		}
		return new Triple<Integer, byte[], byte[]>(captureResult, templateOne, templateTwo);
	}
	public byte[] getTemplate(){
		int captureResult = Misc.getUndefInt();
		byte[] template = null;
		if(!isConnected())
			return null;
			try{
				int ret = Misc.getUndefInt();
				int timeout = taskTimeOut;
				short advancedSecurityLevelsRequired = 0;
				short fingerNumber = 1;
				MorphoTemplateType templateType = MorphoTemplateType.MORPHO_PK_ISO_FMR;
				MorphoFVPTemplateType templateFVPType = MorphoFVPTemplateType.MORPHO_NO_PK_FVP;
				int maxSizeTemplate = 255;
				short enrolType = (short) EnrollmentType.ONE_ACQUISITIONS.getValue();
				long callbackMask = MorphoCallbackCommand.MORPHO_CALLBACK_IMAGE_CMD.swigValue() | MorphoCallbackCommand.MORPHO_CALLBACK_ENROLLMENT_CMD.swigValue() | MorphoCallbackCommand.MORPHO_CALLBACK_COMMAND_CMD.swigValue() | MorphoCallbackCommand.MORPHO_CALLBACK_CODEQUALITY.swigValue() | MorphoCallbackCommand.MORPHO_CALLBACK_DETECTQUALITY.swigValue();
				MorphoTemplateList templateList = new MorphoTemplateList();
				MorphoTemplateEnvelope typEnvelop = MorphoTemplateEnvelope.MORPHO_RAW_TEMPLATE;
				byte[] applicationData = null;
				short latentDetection = (short)LatentDetection.LATENT_DETECT_DISABLE.getValue();
				int coderChoice = 0;
				long detectModeChoice = MorphoSmartSDK.MORPHO_ENROLL_DETECT_MODE;
				short acquisitionThreshold = 50;
				int securityLevel = MorphoSmartSDK.FFD_SECURITY_LEVEL_DEFAULT_HOST;;
				morphoDevice.setSecurityLevel(securityLevel);
				TemplateType templateFp = TemplateType.MORPHO_PK_ISO_FMR;
				TemplateFVPType templateFvp = TemplateFVPType.MORPHO_NO_PK_FVP;
				IMorphoEventHandler callback = new MorphoEventHandler(new MorphoActionI() {
				
					@Override
					public void setStepsImage(EnumMoveFinger move) {
						// TODO Auto-generated method stub
				
					}
				
					@Override
					public void setScore(short quality) {
						// TODO Auto-generated method stub
				
					}
				
					@Override
					public void setLiveStepImage(MorphoImage morphoImage, short fingerNumber,
							short step) {
						// TODO Auto-generated method stub
				
					}
				
					@Override
					public void setLiveImage(MorphoImage morphoImage) {
						// TODO Auto-generated method stub
				
					}
				
					@Override
					public void setInstruction(String intruction) {
						// TODO Auto-generated method stub
				
					}
				
					@Override
					public void setDetectedQuality(short detectedQuality) {
						// TODO Auto-generated method stub
					}
				
					@Override
					public void setCurrentImageInfo(int nbCol, int nbRow, int resX, int resY,
							int bitPerPixel) {
						// TODO Auto-generated method stub
				
					}
				
					@Override
					public void setCodeQuality(short codeQuality) {
						// TODO Auto-generated method stub
				
					}
				
					@Override
					public void setBorderColorGreen(short fingerNumber, short step) {
						// TODO Auto-generated method stub
				
					}
				
					@Override
					public void playVideo(boolean isFingerFvpDetected) {
						// TODO Auto-generated method stub
				
					}
				
					@Override
					public void fingerOk() {
						// TODO Auto-generated method stub
				
					}
				});

				captureResult = morphoDevice.capture(
						timeout, 
						acquisitionThreshold, 
						advancedSecurityLevelsRequired, 
						fingerNumber, 
						templateType, 
						templateFVPType, 
						maxSizeTemplate, 
						enrolType, 
						callbackMask, 
						callback, 
						templateList, 
						typEnvelop, 
						applicationData, 
						latentDetection, 
						coderChoice, 
						detectModeChoice, 
						(short) 0, 
						MorphoCompressAlgo.MORPHO_NO_COMPRESS, 
						(short) 0);
				if (captureResult != MorphoSmartSDK.MORPHO_OK) {
					String message1 = "Capture Failed";
					String message2 = "";
					String message3 = "";
					String message4 = "";
					switch (ret) {
					case MorphoSmartSDK.MORPHOERR_NO_HIT:
						message2 = "Bad Capture Sequence.";
						break;
					case MorphoSmartSDK.MORPHOERR_CMDE_ABORTED:
						message2 = "Command aborted by user.";
						break;
					case MorphoSmartSDK.MORPHOERR_TIMEOUT:
						message2 = "Timeout has expired.";
						message3 = "Command aborted.";
						break;
					case MorphoSmartSDK.MORPHOERR_FFD:
						message2 = "False finger detected !!!";
						break;
					case MorphoSmartSDK.MORPHOERR_MOIST_FINGER:
						message2 = "Finger too moist !!!";
						break;
					case MorphoSmartSDK.MORPHOERR_BADPARAMETER:
						message2 = "One or more input parameters are out of range";
						break;
					case MorphoSmartSDK.MORPHOERR_LICENSE_MISSING:
						message2 = "A required license is missing.";
						break;
					case MorphoSmartSDK.MORPHOERR_ADVANCED_SECURITY_LEVEL_NOT_AVAILABLE:
						message2 = "Cannot make a multimodal template compatible with advanced security levels.";
						break;
					case MorphoSmartSDK.MORPHOERR_ADVANCED_SECURITY_LEVEL_MISMATCH:
						message2 = "Failed to make a multimodal template compatible with advanced security levels.";
						break;
					case MorphoSmartSDK.MORPHOERR_BAD_FINAL_FINGER_PRINT_QUALITY:
						message2 = "Failed to reach the fingerprint quality threshold.";
						break;
					default:
						message2 = "An error occured while calling";
						message3 = "MorphoDevice.capture() function";
						message4 = ErrorsMgt.convertSDKError(ret);
						break;
					}
					//new DialogResultWindow(null, message1, message2, message3, message4, "").setVisible(true);
				} else {
					short[] nbTemplate = { 0 };
					templateList.getNbTemplate(nbTemplate);
					MorphoTemplateType[] typTemplate = { MorphoTemplateType.MORPHO_PK_ISO_FMR };
					ArrayList<Byte> dataTemplate = new ArrayList<Byte>();
					short[] pkFpQuality = { 0 };
					short[] dataIndex = { 0 };
					ret = templateList.getTemplate((short) 0, typTemplate, dataTemplate, pkFpQuality, dataIndex);
					if(ret == MorphoSmartSDK.MORPHO_OK){
						template = FingerPrintAction.toByteArray(dataTemplate);
					}
					dataTemplate.clear();
				}
			}catch(Exception ex){
			ex.printStackTrace();
		}
		return template;
	}

}
