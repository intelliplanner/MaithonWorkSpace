/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ipssi.rfid.constant;

/**
 *
 * @author Vi$ky
 */
public class Type {

    public static class Reader {

        public static final int IN = 0;
        public static final int OUT = 1;
    }

    public static class WorkStationType {

        public static final int GATE_IN_TYPE = 1;
        public static final int REGISTRATION = 2;
        public static final int WEIGH_BRIDGE_IN_TYPE = 3;
        public static final int YARD_IN_TYPE = 4;
        public static final int YARD_OUT_TYPE = 5;
        public static final int WEIGH_BRIDGE_OUT_TYPE = 6;
        public static final int GATE_OUT_TYPE = 7;
        public static final int GPS_REPAIRED = 8;
        public static final int FLY_ASH_IN_TYPE = 9;
        public static final int FLY_ASH_TARE_WT_TYPE = 10;
        public static final int FLY_ASH_GROSS_WT_TYPE = 11;
        public static final int STONE_TARE_WT_TYPE  = 12;
        public static final int STONE_GROSS_WT_TYPE = 13;
        public static final int FIRST_WEIGHTMENT_TYPE  = 14;
        public static final int SECOND_WEIGHTMENT_TYPE  = 15;
        
        public static String getString(int key){
        	String retval = null;
        	switch(key){
        	case GATE_IN_TYPE : retval = "Coal Gate In"; break;
            case WEIGH_BRIDGE_IN_TYPE : retval = "Coal WB In"; break;
            case YARD_IN_TYPE : retval = "Coal Yard In"; break;
            case YARD_OUT_TYPE : retval = "Coal Yard Out"; break;
            case WEIGH_BRIDGE_OUT_TYPE : retval = "Coal WB Out"; break;
            case GATE_OUT_TYPE : retval = "Coal Gate Out"; break;
            case REGISTRATION : retval = "Registration"; break;
            case GPS_REPAIRED : retval = "GPS Repair Center"; break;
            case FLY_ASH_IN_TYPE : retval = "FlyAsh Gate In"; break;
            case FLY_ASH_GROSS_WT_TYPE : retval = "FlyAsh WB Gross"; break;
            case FLY_ASH_TARE_WT_TYPE : retval = "FlyAsh WB Tare"; break;
            case STONE_GROSS_WT_TYPE : retval = "Stone Gross"; break;
            case STONE_TARE_WT_TYPE  : retval = "Stone Tare"; break;
            case FIRST_WEIGHTMENT_TYPE  : retval = "First Weighment"; break;
            case SECOND_WEIGHTMENT_TYPE  : retval = "Second Weighment"; break;
        	}
        	return retval;
        }
        
    }
    public static class BlockingInstruction {
    	public static final int BLOCK_DUETO_BLACKLIST = 1;
    	public static final int BLOCK_DUETO_NEXT_STEP = 1001;
    	

    	public static final int BLOCK_DUETO_STEP_JUMP = 2001;
    	public static final int BLOCK_DUETO_MULTIPLE_TPR = 2002;
    	
    	public static final int BLOCK_DUETO_QC = 3001;
        public static final int BLOCK_DUETO_GPS = 3002;
        public static final int BLOCK_DUETO_DRUNCK = 3003;
        public static final int BLOCK_DUETO_HEADLIGHT = 3004;
        public static final int BLOCK_DUETO_BACKLIGHT = 3005;
        public static final int BLOCK_DUETO_DOC_INCOMPLETE = 3006;
        
        public static final int BLOCK_DUETO_TAG_NOT_READ= 4001;
		public static final int BLOCK_DUETO_FINGER_NOT_VERIFIED = 4002;
		public static final int BLOCK_DUETO_FINGER_NOT_CAPTURED = 4003;
		public static final int BLOCK_DUETO_VEHICLE_NOT_EXIST = 4004;
		public static final int BLOCK_DUETO_CHALLAN_NOT_EXIST = 4005;
        
		public static final int BLOCK_DUETO_FITNESS_EXPIRED = 4006;
		public static final int BLOCK_DUETO_ROAD_PERMIT_EXPIRED = 4007;
		public static final int BLOCK_DUETO_INSURANCE_EXPIRED = 4008;
		public static final int BLOCK_DUETO_POLUTION_EXPIRED = 4009;
		public static final int BLOCK_DUETO_NOT_INFORMED_GPS_VENDOR = 4010;
		public static final int BLOCK_DUETO_DRIVER_NOT_EXIST = 4011;
		public static final int BLOCK_DUETO_FINGER_NOT_EXIST = 4012;
		public static final int BLOCK_DUETO_DRIVER_BLACKLISTED = 4013;
		public static String getBlockingStr(int id){
        	switch(id){
        	case BLOCK_DUETO_BLACKLIST: return "Vehicle Blacklisted";
        	case BLOCK_DUETO_DOC_INCOMPLETE: return "Document Incomplete";
        	case BLOCK_DUETO_NEXT_STEP: return "Step Block";
        	case BLOCK_DUETO_STEP_JUMP: return "Step Jump";
        	case BLOCK_DUETO_MULTIPLE_TPR : return "Multiple Open TPR";
        	case BLOCK_DUETO_QC: return "QC Not Done";
        	case BLOCK_DUETO_GPS: return "GPS Not Ok";
        	case BLOCK_DUETO_DRUNCK: return "Driver Found Drunck";
        	case BLOCK_DUETO_HEADLIGHT: return "Headlight Not Ok";
        	case BLOCK_DUETO_BACKLIGHT: return "Backlight Not Ok";
        	case BLOCK_DUETO_TAG_NOT_READ: return "Tag Not Working";
        	case BLOCK_DUETO_FINGER_NOT_VERIFIED: return "Finger Not Verified";
        	case BLOCK_DUETO_FINGER_NOT_CAPTURED: return "Finger Not Captured";
        	case BLOCK_DUETO_VEHICLE_NOT_EXIST: return "Vehicle Not Exist";
        	case BLOCK_DUETO_CHALLAN_NOT_EXIST: return "Challan Not Exist";
        	
        	case BLOCK_DUETO_FITNESS_EXPIRED: return "Fitness Expired";
        	case BLOCK_DUETO_ROAD_PERMIT_EXPIRED: return "Road Permit Expired";
        	case BLOCK_DUETO_INSURANCE_EXPIRED: return "Insurance Expired";
        	case BLOCK_DUETO_POLUTION_EXPIRED: return "Polution Expired";
        	case BLOCK_DUETO_NOT_INFORMED_GPS_VENDOR: return "Not Informed GPS Vendor";
        	case BLOCK_DUETO_DRIVER_NOT_EXIST: return "Driver Reg. Not Done";
        	case BLOCK_DUETO_FINGER_NOT_EXIST: return "Driver Finger Not Reg.";
        	case BLOCK_DUETO_DRIVER_BLACKLISTED: return "Driver Blacklisted";
        	default : return "NA";
        	}
        }
    }
    public static class TPRMATERIAL {
    	//(0=coal, 1=stone, 2=flyash, 3=other)
        public static final int COAL = 0;	
        public static final int  STONE = 1;
        public static final int  FLYASH = 2;
        public static final int  OTHERS = 3;
        public static String getStr(int id){
        	switch(id){
        	case COAL: return "COAL";
        	case STONE: return "STONE";
        	case FLYASH: return "FLYASH";
        	case OTHERS: return "OTHERS";
        	default : return "NA";
        	}
        }
    }
}
