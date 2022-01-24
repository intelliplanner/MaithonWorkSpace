package com.ipssi.rfid.processor;

import java.sql.Connection;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.Pair;
import com.ipssi.rfid.beans.Token;
import com.ipssi.rfid.connection.ConfigUtility;
import com.ipssi.rfid.constant.Status;
import com.ipssi.rfid.constant.Status.Workstate;
import com.ipssi.rfid.constant.Type;
import java.util.HashMap;

public class TokenManager {
	public static final int NORMAL = 0;
	public static final int STRICT_WITH_STEP_BLOCKING = 1;
	public static final int STRICT_WITH_PAPER_BLOCKING = 2;
	public static final int STRICT_WITH_WEB_BLOCKING = 3;
	private static ConcurrentHashMap<String, Token> rfidTokenMap = new ConcurrentHashMap<String, Token>();
	private static Object locker = new Object();
	public static long refreshInterval = 10 * 1000;
	private static long minTokenGap = 30 * 60 * 1000;
	private static String lastEpc;
	private static Token lastToken = null;
	private static long updatedOn = Misc.getUndefInt();
	private static int readerId = Misc.getUndefInt();
	private static int status = Workstate.IDLE;
	public static int nextWorkStationType = Misc.getUndefInt();
	public static int prevWorkStationType = Misc.getUndefInt();
	public static int currWorkStationType = Misc.getUndefInt();
	public static int currWorkStationId = Misc.getUndefInt();
	public static int morphoDeviceExist = Misc.getUndefInt();
	public static int morphoAPIType = Misc.getUndefInt();
    public static long maxTareDays = Misc.getUndefInt();
	public static int Printer_Connected = Misc.getUndefInt();
	public static int Weightment_Printer_Connected = 1;
	public static boolean createNewTPR = false;
	public static boolean closeTPR = false;
	public static int portNodeId = 463;
	public static int applicationMode = NORMAL;
	public static long fitnessExpiaryThreshold;
	public static long roadPermitExpiaryThreshold;
	public static long insauranceExpiaryThreshold;
	public static long polutionExpiaryThreshold;
	public static int userId = Misc.getUndefInt();
	public static String userName = "";
	public static int nextStationNumber = 5;
	private static long threshold = 15*24*60*60*1000;
	public static int noOfStation = 2;
	public static int lastStationCount = 0;
	public static int materialCat = Type.TPRMATERIAL.COAL;
	public static String weight_val = "";
	public static long morphoSyncFreq = 30*60*1000;
	public static long clockSyncFreq = 30*60*1000;
	public static int maxTagDataRetry = 1;
	public static boolean useSmartRFRead = false;
	public static boolean syncClock = false;
	public static String systemDateFormat = null;
	public static boolean isDebug = false;
	public static boolean isDebugReadings = false;
	public static long weighBridgeTimeout = 10*1000;
	public static boolean forceManual = false;
    public static HashMap<Integer,Integer> isManualEntry  =  new HashMap<Integer,Integer>();
    public static boolean checkSyncReg = false;
                
//	 HashMap<Integer,String> hm=new HashMap<Integer,String>();  
                
	public static boolean isTokenAvailable(Connection conn, String epcId) {
		return isTokenAvailable(conn, epcId,null);
	}
	public static boolean isTokenAvailable(Connection conn, String epcId, String vehicleName) {
		boolean retval = false;
		if (Utils.isNull(epcId) && !Utils.isNull(vehicleName)) {
			epcId = vehicleName;
		}
		if(Utils.isNull(epcId))
			return false;
		if (status != Workstate.BUSY) {
			if (!Utils.isNull(epcId)) {
				//new 
				long lastUpdatedOn = rfidTokenMap.containsKey(epcId) ? rfidTokenMap.get(epcId).getLastProcessed() : Misc.getUndefInt();
				long t = (System.currentTimeMillis() - lastUpdatedOn);
				if (Misc.isUndef(lastUpdatedOn) || t >= minTokenGap || status == Workstate.CLEAR) {
					retval = true;
				}
				//end new
				//start prev
				/*long lastUpdatedOn = rfidTokenMap.containsKey(epcId) ? rfidTokenMap.get(epcId).getLastProcessed() : updatedOn;
				if (!epcId.equalsIgnoreCase(lastEpc)) {
					long t = (System.currentTimeMillis() - lastUpdatedOn);
					if (Misc.isUndef(lastUpdatedOn) || t >= refreshInterval || status == Workstate.CLEAR) {
						retval = true;
					}
				} else {
					if (!Misc.isUndef(lastUpdatedOn)) {
						long t = (System.currentTimeMillis() - lastUpdatedOn);
						if (status == Workstate.CLEAR || t >= minTokenGap) {
							retval = true;
						}
					} else {
						retval = true;
					}
				}*/
				//end prev
			}
		} else {
			//LoggerNew.Write("[RFID TOKEN]:" + epcId + ",workstation busy");
		}
		return retval;
	}
	public static Token createToken(Connection conn, String epcId) {
		return createToken(conn, epcId, null);
	}
	public static Token createToken(Connection conn, String epcId,String vehicleName) {
		Token token = null;
		long issueTimeStamp = System.currentTimeMillis();
		int vehicleId = Misc.getUndefInt();
		Pair<Integer, String> vehiclePair = null;
		boolean readVehicleFromTag = true;
		try {
			synchronized (locker) {
				if (isTokenAvailable(conn,vehicleName, epcId)) {
					vehiclePair = TPRInformation.getVehicle(conn, epcId, vehicleName);
					if (Utils.isNull(epcId) && !Utils.isNull(vehicleName)) {
						epcId = vehicleName;
						readVehicleFromTag = false;
					}
					if(Utils.isNull(epcId))
						return null;
					if(vehiclePair != null){
						vehicleId = vehiclePair.first;
						vehicleName = vehiclePair.second;
					}
					System.out.println("TOKEN : " +vehicleId);
					token = rfidTokenMap.get(epcId);
					if (token == null) {//first token for that vehicle
						token = new Token(vehicleId, epcId);
						token.setVehicleName(vehicleName);
						rfidTokenMap.put(epcId, token);
					}
					token.setVehicleId(vehicleId);
					token.setVehicleName(vehicleName);
					token.setEpcId(epcId);
					token.setStatus(Status.Token.ASSIGNED);
					token.setLastSeen(issueTimeStamp);
					token.setReadFromTag(readVehicleFromTag);
					status = Status.Workstate.BUSY;
					lastEpc = token.getEpcId();
					updatedOn = issueTimeStamp;
					lastToken = token;
					System.out.println("[RFID TOKEN ISSUE]:" + epcId);
				}

			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return token;
	}

	public static void returnToken(Connection conn, Token token) {
		try{
			synchronized (locker) {
				status = Workstate.IDLE;
				if (token == null || Utils.isNull(token.getEpcId())) {
					return;
				}
				if (rfidTokenMap.get(token.getEpcId()) == null) {
					rfidTokenMap.put(token.getEpcId(), token);
					rfidTokenMap.get(token.getEpcId()).setLastSeen(System.currentTimeMillis());
				}
				rfidTokenMap.get(token.getEpcId()).setLastProcessed(System.currentTimeMillis());
				rfidTokenMap.get(token.getEpcId()).setStatus(Status.Token.PROCESSED);
				System.out.println("[RFID TOKEN Return]:" + token.getEpcId());
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}

	public static void clearWorkstation() {
		synchronized (locker) {
			status = Workstate.CLEAR;
			if (!Utils.isNull(lastEpc) && rfidTokenMap.get(lastEpc) != null) {
				//rfidTokenMap.get(lastEpc).setLastProcessed(System.currentTimeMillis());
				rfidTokenMap.get(lastEpc).setStatus(Status.Token.PROCESSED);
			}
			System.out.println("[RFID TOKEN Clear]:" + lastEpc);
		}
	}
	public static void initSystemConfig(){
		try{
			Properties config = ConfigUtility.getSystemConfiguration();
			if(config == null)
				return;
			syncClock = Misc.getParamAsInt(config.getProperty("SYNC_CLOCK")) == 1;
			systemDateFormat = Misc.getParamAsString(config.getProperty("SYSTEM_DATE_FORMAT"),"dd-MM-yyyy HH:mm:ss");
			clockSyncFreq  = Misc.getParamAsInt(config.getProperty("CLOCK_SYNC_FREQ"));
			if(clockSyncFreq > 0)
				clockSyncFreq *=  1000;
			else
				clockSyncFreq	= 30 * 60 * 1000;
			isDebug   = Misc.getParamAsInt(config.getProperty("DEBUG")) == 1;
			forceManual = Misc.getParamAsInt(config.getProperty("FORCE_MANUAL")) == 1;
			checkSyncReg = Misc.getParamAsInt(config.getProperty("CHECK_SYNC_REG")) == 1;
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	public static void initConfig(String sufix,int workStationType){
		try{
			Properties config = ConfigUtility.getWorkStationConfiguration(sufix,workStationType);
			if(config != null){
				nextWorkStationType = Misc.getParamAsInt(config.getProperty("NEXT_WORK_STATION_TYPE"));
				prevWorkStationType = Misc.getParamAsInt(config.getProperty("PREV_WORK_STATION_TYPE"));
				currWorkStationType = Misc.getParamAsInt(config.getProperty("WORK_STATION_TYPE"));
				currWorkStationId = Misc.getParamAsInt(config.getProperty("WORK_STATION_ID"));
				morphoDeviceExist  = Misc.getParamAsInt(config.getProperty("MORPHO_DEVICE_EXIST"));
				morphoAPIType = Misc.getParamAsInt(config.getProperty("MORPHO_API_TYPE"));	
				morphoSyncFreq  = Misc.getParamAsInt(config.getProperty("MORPHO_SYNC_FREQ"));
				Printer_Connected  = Misc.getParamAsInt(config.getProperty("PRINTER_CONNECTED"));
				Weightment_Printer_Connected = Misc.getParamAsInt(config.getProperty("WEIGHMENT_PRINTER_CONNECTED"));
				refreshInterval = Misc.getParamAsLong(config.getProperty("REFRESH_INTERVAL"));
				weighBridgeTimeout = Misc.getParamAsLong(config.getProperty("WB_TIMEOUT"));
				maxTareDays =   Misc.getParamAsLong(config.getProperty("MAXIMUM_TARE_DAYS"));
				weight_val = config.getProperty("WEIGHT");
				if(refreshInterval > 0)
					refreshInterval *= 1000;
				else
					refreshInterval	= 10 * 1000;
				if(morphoSyncFreq > 0)
					morphoSyncFreq *=  1000;
				else
					morphoSyncFreq	= 30 * 60 * 1000;
				if(weighBridgeTimeout > 0)
					weighBridgeTimeout *=  1000;
				else
					weighBridgeTimeout	= 10 * 1000;
				
				minTokenGap = Misc.getParamAsInt(config.getProperty("MIN_TOKEN_GAP"));
				if(minTokenGap > 0)
					minTokenGap *= 1000;
				else
					minTokenGap = 30 * 60 * 1000;
				maxTagDataRetry = Misc.getParamAsInt(config.getProperty("MAX_TAG_DATA_RETRY"));
				createNewTPR = Misc.getParamAsInt(config.getProperty("CREATE_NEW_TRIP")) == 1;
				closeTPR = Misc.getParamAsInt(config.getProperty("CLOSE_TRIP")) == 1;
				portNodeId = Misc.getParamAsInt(config.getProperty("PORT_NODE_ID"),463);
				applicationMode = Misc.getParamAsInt(config.getProperty("APPLICATION_MODE"),0);
				useSmartRFRead = Misc.getParamAsInt(config.getProperty("USE_SMART_RF_READ")) == 1;
                                                                
				nextStationNumber = 5;
				fitnessExpiaryThreshold = threshold;
				roadPermitExpiaryThreshold = threshold;
				insauranceExpiaryThreshold = threshold;
				polutionExpiaryThreshold = threshold;
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	public static boolean isLastProcessedByUser(String epcId, int vehicleId){
		Token token =  rfidTokenMap.get(epcId);
		return ( token != null 
				&& (
						(
								token.getEpcId() != null && token.getEpcId().equalsIgnoreCase(epcId)) 
								|| (token.getVehicleId() == vehicleId)
						) 
						&& (token.getLastProcessed() > 0  && (System.currentTimeMillis() - token.getLastProcessed()) < minTokenGap )
				);
	}
	public static String getNextStationSuffix(){
		TokenManager.nextStationNumber = TokenManager.nextStationNumber++ % TokenManager.noOfStation;
		return TokenManager.nextStationNumber+"";
	}
	public static boolean useSDK(){
		return morphoAPIType == 1;
	}
}
