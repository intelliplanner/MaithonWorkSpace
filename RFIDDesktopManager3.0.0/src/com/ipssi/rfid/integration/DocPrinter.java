package com.ipssi.rfid.integration;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintException;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.SimpleDoc;

import com.ipssi.gen.utils.Misc;
import com.ipssi.rfid.beans.Token;
import com.ipssi.rfid.constant.Type;
import com.ipssi.rfid.constant.UIConstant;
import com.ipssi.rfid.database.GateInDao;
import com.ipssi.rfid.processor.TokenManager;
import com.ipssi.rfid.processor.Utils;
import javax.print.attribute.PrintServiceAttributeSet;
import javax.print.attribute.standard.PrinterState;


public class DocPrinter {
	private static SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm");
	static String stationNoLabel;
	private static String getPrintStr(String vehicleName, Date date, int tprId,String mines,String transporter,String driver,String instruction) {
		TokenManager.nextStationNumber = TokenManager.nextStationNumber++ % TokenManager.noOfStation;
		//stationNoLabel = TokenManager.nextStationNumber;
		stationNoLabel = TokenManager.getNextStationSuffix();
		
		return
				"Trip Date   : "+(date == null ? "" : sdf.format(date))+"  \n"+
				"Trip ID     : "+(Misc.isUndef(tprId) ? "" : ""+tprId)+"  \n"+
				"Vehicle     : "+(Utils.isNull(vehicleName) ? "" : vehicleName)+"  \n"+
				"Mines       : "+(Utils.isNull(mines) ? "" : mines)+"  \n"+
				"Transporter : "+(Utils.isNull(transporter) ?  "" : transporter)+"  \n"+
				"Driver      : "+(Utils.isNull(driver) ? "" : driver)+"  \n"+
				"=============================\n"+
				"         Instruction         \n"+
				"=============================\n"+
				"Go to "+(Utils.isNull(instruction) ? "" : (instruction + " " + stationNoLabel))+"          \n"+
				"=============================\n"+
				"\n"+
				"\n";
	}
	
	public static void print(String vehicleName, Date date, int tprId,String mines,String transporter,String driver,String instruction,int printerConnected,String bedAssign) throws PrintException {
		/*TokenManager.nextStationNumber = TokenManager.nextStationNumber++ % TokenManager.noOfStation;
		stationNoLabel = TokenManager.nextStationNumber;*/
		/*stationNoLabel = TokenManager.getNextStationSuffix();
		String printOnSlip = "Trip Date   : "+(date == null ? "" : sdf.format(date))+"  \n"+
				"Trip ID     : "+(Misc.isUndef(tprId) ? "" : ""+tprId)+"  \n"+
				"Vehicle     : "+(Utils.isNull(vehicleName) ? "" : vehicleName)+"  \n"+
				"Mines       : "+(Utils.isNull(mines) ? "" : mines)+"  \n"+
				"Transporter : "+(Utils.isNull(transporter) ?  "" : transporter)+"  \n"+
				"Bed Assign : "+(Utils.isNull(bedAssign) ?  "" : bedAssign)+"  \n"+
				"Driver      : "+(Utils.isNull(driver) ? "" : driver)+"  \n"+
				"=============================\n"+
				"         Instruction         \n"+
				"=============================\n"+
				"Go to "+(Utils.isNull(instruction) ? "" : (instruction + " " + stationNoLabel))+"          \n"+
				"=============================\n"+
				"\n"+
				"\n";
		System.out.println("Print On Slip : "+printOnSlip);*/

		if(printerConnected != 1){
			System.out.println("########### Printer Not Connected ##########");
			return;
		}


		try{
			DocFlavor flavor = DocFlavor.BYTE_ARRAY.AUTOSENSE;
			PrintService defaultService = PrintServiceLookup.lookupDefaultPrintService();
			PrintServiceAttributeSet attr = defaultService.getAttributes();
			PrinterState state =  (PrinterState) attr.get(PrinterState.class);
			boolean isConnected = state != null;
			String printerNAme = defaultService.getName();
			byte[] bytes;
			bytes = getPrintStr(vehicleName, date, tprId, mines, transporter, driver, instruction).getBytes();
			Doc doc = new SimpleDoc(bytes,flavor,null);
			DocPrintJob job = defaultService.createPrintJob();

			job.print(doc, null);
		}catch(Exception ex){
			ex.printStackTrace();
		}
		System.out.println("###########  Successfully  Printed  ##########");
	}


	public static void print(String vehicleName, Date date, int tprId, String transporter, String driverName, String instruction, 
			boolean vehicleExist, boolean tagRead, boolean fingerCaptured, boolean fingerVerified,boolean challanExist,
			int printerConnected,String bedAssigned, int nextWorkstation, boolean isPaperOk, boolean isDriverExist, 
			boolean isFingerExist,boolean isDriverBlacklisted, boolean isMultipleTPR,String gpsStatas) {
		String nextStationLabel = Type.WorkStationType.getString(nextWorkstation);
		if(nextWorkstation != Type.WorkStationType.REGISTRATION){
//			TokenManager.nextStationNumber = TokenManager.nextStationNumber + (TokenManager.lastStationCount++ % TokenManager.noOfStation);
			TokenManager.lastStationCount = (++TokenManager.lastStationCount) % TokenManager.noOfStation;
			nextStationLabel += " "+ (TokenManager.nextStationNumber + TokenManager.lastStationCount);
		}
		String printOnSlip = 
				"Trip Date        : "+(date == null ? "" : sdf.format(date))+"  \n"+
				"Trip ID          : "+(Misc.isUndef(tprId) ? "" : ""+tprId)+"  \n"+
				"Vehicle          : "+(Utils.isNull(vehicleName) ? "" : vehicleName)+"  \n"+
				"Transporter      : "+(Utils.isNull(transporter) ?  "" : transporter)+"  \n"+
				"Bed Assign       : "+(Utils.isNull(bedAssigned) ?  "" : bedAssigned)+"  \n"+
				"Driver           : "+(Utils.isNull(driverName) ? "" : driverName)+"  \n"+
				"Vehicle Reg Rq   : "+ GateInDao.getValue(!vehicleExist)+"  \n"+                                    
				"Tag Reg Rq       : "+ GateInDao.getValue(!tagRead)+"  \n"+
				"Driver Reg Rq    : "+ GateInDao.getValue(!isDriverExist || !isFingerExist)+"  \n"+
			  //"FP Enroll Rq     : "+ GateInDao.getValue(!isFingerExist)+"  \n"+
				"FP Capture       : "+ GateInDao.getValue(fingerCaptured)+"  \n"+
				"FP Match         : "+ GateInDao.getValue(fingerVerified)+"  \n"+
				"Mines Entry Done : "+ GateInDao.getValue(challanExist)+"  \n"+
				"Multiple Open TPR: "+ GateInDao.getValue(isMultipleTPR)+"  \n"+
				"Paper Ok         : "+ GateInDao.getValue(isPaperOk)+"  \n"+
				"GPS Ok           : "+(Utils.isNull(gpsStatas) ?  "" : gpsStatas)+"  \n"+
				"Driver Blacklist : "+ GateInDao.getValue(isDriverBlacklisted)+"  \n"+
				"=============================\n"+
				"         Instruction         \n"+
				"=============================\n"+
				"Go to "+nextStationLabel+"          \n"+
				"=============================\n"+
				"\n"+
				"\n";            
		System.out.println("@#@Print On Slip@#@");
		System.out.println(printOnSlip);

		if(printerConnected != 1){
			System.out.println("########### Printer Not Connected ##########");
			return;
		}

		try{
			DocFlavor flavor = DocFlavor.BYTE_ARRAY.AUTOSENSE;
			PrintService defaultService = PrintServiceLookup.lookupDefaultPrintService();
			byte[] bytes;
			//bytes = getPrintStr(vehicleName, date, tprId, transporter, driverName, instruction,vehicleExist, tagRead, fingerCaptured, fingerVerified,challanExist).getBytes();
			bytes = printOnSlip.getBytes();
			Doc doc = new SimpleDoc(bytes,flavor,null);
			DocPrintJob job = defaultService.createPrintJob();
			job.print(doc, null);
		}catch(Exception ex){
			ex.printStackTrace();
		}
		System.out.println("###########  Successfully  Printed  ##########");
	}


}