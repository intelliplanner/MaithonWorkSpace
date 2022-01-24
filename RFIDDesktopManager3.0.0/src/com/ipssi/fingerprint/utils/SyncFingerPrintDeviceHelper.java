package com.ipssi.fingerprint.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import morpho.morphosmart.sdk.api.MorphoSmartSDK;

import com.ipssi.gen.exception.GenericException;
import com.ipssi.gen.utils.DBConnectionPool;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.Pair;
import com.ipssi.rfid.processor.Utils;
import com.scl.loadlibrary.DriverBean;
import com.scl.loadlibrary.LoadLibrary;
import com.scl.loadlibrary.MorphoSmartFunctions;


public class SyncFingerPrintDeviceHelper {
	private static String path = "C:" + File.separator + "ipssi" + File.separator + "properties" + File.separator + "load_fingure_print.txt";
	private static String morphoDeviceId = "";
	private static int morphoDeviceCapacity = Misc.getUndefInt();
	private static int morphoDeviceEnroll = Misc.getUndefInt();
	
	public static boolean checkSyncAll(){ 
		FileInputStream fis = null;
		boolean retval = false;
		byte[] b = null;
		File  file = null;
		try{
			file = new File(path);
			if (!file.exists()) {
				file.getParentFile().mkdirs();
				file.createNewFile();
				FileOutputStream cfos = new FileOutputStream(path);
				cfos.write("0".getBytes()[0]);
				cfos.close();
			}else{
				fis = new FileInputStream(path);
				int content;
				b = new byte[fis.available()];
				int i=0;
				while ((content = fis.read()) != -1) {
					b[i++] = (byte)content;
				}
				String s = new String(b);
				retval = Misc.getParamAsInt(s) == 1;
				if(fis != null)
					fis.close();
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return retval;
	}
	public static boolean updateFileStatus(int status){
		boolean retval = false;
		File  file = null;
		try{
			file = new File(path);
			if (!file.exists()) {
				file.getParentFile().mkdirs();
				file.createNewFile();
			}
			FileOutputStream cfos = new FileOutputStream(path);
			cfos.write((status+"").getBytes()[0]);
			cfos.close();
			retval = true;
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return retval;
	}
	
	public static Pair<ArrayList<DriverBean>, ArrayList<DriverBean>>  getDriverDataToSyncNew(Connection conn, int morphoId,long lastReadTimeStamp, boolean syncAll){

		PreparedStatement ps = null;
		ResultSet rs = null;
		ArrayList<DriverBean> driverBeanListForEnroll = null;
		ArrayList<DriverBean> driverBeanListForRemove = null;
		String SELECT = " SELECT driver_details.id, driver_name, driver_details.status, capture_template_first," +
				" capture_template_second, capture_template_third, capture_template_fourth, capture_template_fifth, " +
				" capture_template_sixth, capture_template_seventh, capture_template_eighth, capture_template_ninth, " +
				" capture_template_tenth, driver_details.status, driver_dl_number, now() read_timestamp " 
				; 
		String FROM = " driver_details ";
		String WHERE = "1=1";//" driver_details.status in (1) and driver_details.driver_name is not null and driver_details.driver_dl_number is not null and (capture_template_first is not null or capture_template_second is not null)";
		boolean syncPartial = !syncAll && !(Misc.isUndef(lastReadTimeStamp));
		try {
			if(syncPartial){
				WHERE += " and template_updated_on > ? " 
						+ " and driver_details.id in (Select distinct(driver_id)  from tp_record where tpr_create_date > DATE_SUB(NOW(),INTERVAL 30 DAY))";
			}
			System.out.println("QUERY FOR SELECT DRIVER DETAILS: "+SELECT + " FROM " + FROM + " where " + WHERE );
			ps = conn.prepareStatement(SELECT + " FROM " + FROM + " where " + WHERE );
			if(syncPartial){
				ps.setTimestamp(1, new Timestamp(lastReadTimeStamp));
			}
			rs = ps.executeQuery();
			while(rs.next()){
				DriverBean db = new DriverBean();
				db.id = Misc.getRsetInt(rs,1);
				db.driver_name = rs.getString(2);
				db.status = Misc.getRsetInt(rs, 3);
				ArrayList <byte[]> byteArrList = null;
				for (int i = 4; i <= 13; i++) {
					byte[] obj = (byte[]) rs.getObject(i);
					if(obj == null || obj.length <= 0)
						continue;
					if(byteArrList == null )
						byteArrList = new ArrayList<byte[]>();
						byteArrList.add(obj);
				}
				db.capture_template = byteArrList;
				db.dl_no = rs.getString("driver_dl_number");
				db.readTimestamp = Misc.getDateInLong(rs, "read_timestamp");
				if(db.status != 1 || (db.capture_template == null)){
					if(driverBeanListForRemove == null)
						driverBeanListForRemove = new ArrayList<DriverBean>();
					driverBeanListForRemove.add(db);
				}
				else{
					if(driverBeanListForEnroll == null)
						driverBeanListForEnroll = new ArrayList<DriverBean>();
					driverBeanListForEnroll.add(db);
				}
			}
			if(rs != null)
				rs.close();
			if(ps != null)
				ps.close();
		}catch(Exception e){
			e.printStackTrace();
			//throw new GenericException(e);
		}
		return new Pair<ArrayList<DriverBean>, ArrayList<DriverBean>>(driverBeanListForEnroll, driverBeanListForRemove);

	}
	public static Pair<ArrayList<DriverBean>, ArrayList<DriverBean>>  getDriverDataToSync(Connection conn, int morphoId, boolean syncAll){

		PreparedStatement ps = null;
		ResultSet rs = null;
		ArrayList<DriverBean> driverBeanListForEnroll = null;
		ArrayList<DriverBean> driverBeanListForRemove = null;
		String SELECT = " SELECT driver_details.id, driver_name, driver_details.status, capture_template_first," +
				" capture_template_second, capture_template_third, capture_template_fourth, capture_template_fifth, " +
				" capture_template_sixth, capture_template_seventh, capture_template_eighth, capture_template_ninth, " +
				" capture_template_tenth, driver_details.status, driver_dl_number, now() read_timestamp " 
				; 
		String FROM = " driver_details ";
		String WHERE = "1=1";//" driver_details.status in (1) and driver_details.driver_name is not null and driver_details.driver_dl_number is not null and (capture_template_first is not null or capture_template_second is not null)";

		try {
			if(!syncAll && !Misc.isUndef(morphoId)){
				FROM  += " left outer join morpho_sync_status on (driver_details.id = morpho_sync_status.driver_id and morpho_sync_status.morpho_id="+morphoId+")";
				SELECT += ", morpho_sync_status.task ";
				WHERE += " and (morpho_sync_status.status=0 or morpho_sync_status.status is null)";
			}
			System.out.println("QUERY FOR SELECT DRIVER DETAILS: "+SELECT + " FROM " + FROM + " where " + WHERE +(!syncAll && !Misc.isUndef(morphoId) ? "  order by morpho_sync_status.task desc" : "") );
			ps = conn.prepareStatement(SELECT + " FROM " + FROM + " where " + WHERE + (!syncAll && !Misc.isUndef(morphoId) ? "  order by morpho_sync_status.task desc" : "") );
			rs = ps.executeQuery();
			while(rs.next()){
				DriverBean db = new DriverBean();
				db.id = Misc.getRsetInt(rs,1);
				db.driver_name = rs.getString(2);
				db.status = Misc.getRsetInt(rs, 3);
				ArrayList <byte[]> byteArrList = null;
				for (int i = 4; i <= 13; i++) {
					byte[] obj = (byte[]) rs.getObject(i);
					if(obj == null || obj.length <= 0)
						continue;
					if(byteArrList == null )
						byteArrList = new ArrayList<byte[]>();
						byteArrList.add(obj);
				}
				db.capture_template = byteArrList;
				db.dl_no = rs.getString("driver_dl_number");
				db.readTimestamp = Misc.getDateInLong(rs, "read_timestamp");
				if(!syncAll && !Misc.isUndef(morphoId))
					db.task = Misc.getRsetInt(rs, "task", DriverBean.INSERT);
				if(db.status != 1 || (db.capture_template == null)){
					if(driverBeanListForRemove == null)
						driverBeanListForRemove = new ArrayList<DriverBean>();
					driverBeanListForRemove.add(db);
				}
				else{
					if(driverBeanListForEnroll == null)
						driverBeanListForEnroll = new ArrayList<DriverBean>();
					driverBeanListForEnroll.add(db);
				}
			}
			if(rs != null)
				rs.close();
			if(ps != null)
				ps.close();
		}catch(Exception e){
			e.printStackTrace();
			//throw new GenericException(e);
		}
		return new Pair<ArrayList<DriverBean>, ArrayList<DriverBean>>(driverBeanListForEnroll, driverBeanListForRemove);

	}
	public static void loadInFingerPrintDevice (Connection conn,SynServiceHandler handler){
		//            boolean syncAll = checkSyncAll();
		loadInFingerPrintDevice(conn,false,handler);
		//	    if(syncAll)
		//		updateFileStatus(0);

	}
	public static void loadInFingerPrintDevice (Connection conn,boolean syncAll){
		//            boolean syncAll = checkSyncAll();
		loadInFingerPrintDevice(conn,syncAll,null);
		//	    if(syncAll)
		//		updateFileStatus(0);

	}
	
	public static ArrayList<DriverBean> checkDB (Connection conn,boolean syncAll,SynServiceHandler handler,ArrayList<DriverBean> driverBeanList){
		ArrayList<DriverBean> failedDriverList = new ArrayList<DriverBean>();
		try {
			//First create an object of any name of LoadLibrary Class  
			//System.out.println("Loading All the active driver details ");
			String morphoDeviceId = null;
			synchronized (LoadLibrary.lock) {
				LoadLibrary auth = LoadLibrary.getBiometricDevice(); 
				int enrollResult = Misc.getUndefInt();
				int id = Misc.getUndefInt();
				String dlNo = "";
				int status = Misc.getUndefInt();
				boolean deviceConnected = auth.isDeviceConnected();
				int morphoId = Misc.getUndefInt();
				if(deviceConnected){
					String morphoDeviceInfo = auth.getDeviceInfo();
					String[] morphoDeviceInfoPair = null;
					if(!Utils.isNull(morphoDeviceInfo)){
						morphoDeviceInfoPair = auth.getDeviceInfo().split("-");
					}
					if(morphoDeviceInfoPair != null && morphoDeviceInfo.length() > 1)
						morphoDeviceId = auth.getDeviceInfo().split("-")[1];
				//	System.out.println("Morpho device Info:"+ morphoDeviceId);
					//System.out.println("Is deviceConnected : " + deviceConnected);
					int count = 0;
					int m_count = 1;
					morphoId = isExist(conn, morphoDeviceId);
					if(driverBeanList == null || driverBeanList.size() == 0){
						Pair<ArrayList<DriverBean>, ArrayList<DriverBean>> driverBeanListPair = getDriverDataToSync(conn, morphoId,true); 
						driverBeanList = driverBeanListPair == null ? null : driverBeanListPair.first;
					}
					if(Misc.isUndef(morphoId))
						morphoId = insertMorphoDevice(conn, morphoDeviceId);
					for (int k=0,ks=driverBeanList==null? 0 : driverBeanList.size();k<ks;k++) {
						long t = 0;
						long st = System.currentTimeMillis();
						
						if(k==0){
							if(handler != null ){
								handler.onChange(true);
								handler.notifyText("Record found: "+ks);
							}
							if(syncAll){
								auth.deleteAll();//debug
							}
						}
						DriverBean driverBean = driverBeanList.get(k);
						id = driverBean.id;
						if(!Utils.isNull(driverBean.dl_no))
							dlNo = driverBean.dl_no;
						status = driverBean.status;
						//System.out.println("Enroll For id= "+id+" Driver Name= "+driverBean.driver_name);
						ArrayList <byte[]> byteArrList = driverBean.capture_template;
						boolean fpUpdated = false;
						if(!syncAll){
							clearAllregisteredUserFingers(id,auth);
						}
						int enrollStatus = Misc.getUndefInt();
						int[] enrollResultarray = getEmptyIntArray(10);
						for (int i = 0,is = byteArrList == null ? null : byteArrList.size(); i < is; i++) {
							byte[] dbTemplate = byteArrList.get(i);   
							if(dbTemplate == null || dbTemplate.length <= 0)
								continue;
							enrollResult = Misc.getUndefInt();
							String newId = ""+id+"_"+(i+1);
							
							if (status == 1 && driverBean.task != DriverBean.DELETE){
								enrollResult = auth.enrollUser(newId, "", "", dbTemplate, dbTemplate.length);
								count++;	
							}
							enrollResultarray[i] = enrollResult;
							if(enrollResult != 0){
								boolean isFound = false;
								for (int j = 0,js = failedDriverList == null ? 0 : failedDriverList.size(); j < js; j++) {
									if(id == driverBean.id){
										isFound = true;
									}
								}
								if(!isFound)
									failedDriverList.add(driverBean);
							}
							if(enrollResult == 0 || enrollResult == -12 || driverBean.task == DriverBean.DELETE)//0-success -12-already enrolled
								fpUpdated = true;
							if((status == 2 || status == 0) && i == 9)
								fpUpdated = true;
							//fpUpdated = true;
							//System.out.println("Enroll count "+(enrollResult == 0 ? m_count++ : m_count)+" Result "+ enrollResult +" for id= "+ id + " : newId= " +newId+" : driver name= " + driverBean.driver_name+"Driver name= "+dlNo + " : capture_template_" + (i+1) +"= "+ enrollResult);
						}
						if(true){//fpUpdated){
							updateDriverDetails(conn, id, morphoId,DriverBean.INSERT,1, driverBean.readTimestamp,null,enrollResultarray);
							if(handler != null){
								t = System.currentTimeMillis() - st;
								String timeRemaining = formatTimeInterval((ks-k-1)*t/(60*1000));
//								handler.notifyText(timeRemaining);
								handler.notifyText("Sync:"+(k+1)+"/"+ks + "(" + timeRemaining+")");
							}
						}
						if(conn != null)
							conn.commit();
						
						//System.out.println("Enroll Time "+t);
					}
					//System.out.println("Total Enrollment = "+ (count -1));

					//System.out.println("Loaded All the active driver details in Finger Print Device ");

				}else{

				} 
			}
		}catch (UnsatisfiedLinkError e) {
			e.printStackTrace();
			System.out.println("Error in Loading Library" + e);
		} catch (Exception ex) {
			ex.printStackTrace();
			System.out.println("Error in Loading Library" + ex);
		}finally{
			if(handler != null){
				handler.onChange(false);
			}

		}
		return failedDriverList;
	}
	public static void loadInFingerPrintDevice (Connection conn,boolean syncAll,SynServiceHandler handler){
		try {
			//First create an object of any name of LoadLibrary Class  
			System.out.println("Loading All the active driver details ");
			String morphoDeviceId = null;
			synchronized (LoadLibrary.lock) {
				LoadLibrary auth = LoadLibrary.getBiometricDevice(); 
				int enrollResult = Misc.getUndefInt();
				int id = Misc.getUndefInt();
				String dlNo = "";
				int status = Misc.getUndefInt();
				boolean deviceConnected = auth.isDeviceConnected();
				int morphoId = Misc.getUndefInt();
				if(deviceConnected){
					String morphoDeviceInfo = auth.getDeviceInfo();
					String[] morphoDeviceInfoPair = null;
					if(!Utils.isNull(morphoDeviceInfo)){
						morphoDeviceInfoPair = auth.getDeviceInfo().split("-");
					}
					if(morphoDeviceInfoPair != null && morphoDeviceInfo.length() > 1)
						morphoDeviceId = auth.getDeviceInfo().split("-")[1];
					System.out.println("Morpho device Info:"+ morphoDeviceId);
					System.out.println("Is deviceConnected : " + deviceConnected);
					int count = 0;
					int m_count = 1;
					morphoId = isExist(conn, morphoDeviceId);
					Pair<ArrayList<DriverBean>, ArrayList<DriverBean>> driverBeanListPair = getDriverDataToSync(conn, morphoId,syncAll); 
					ArrayList<DriverBean> driverBeanList = driverBeanListPair == null ? null : driverBeanListPair.first;
					if(Misc.isUndef(morphoId))
						morphoId = insertMorphoDevice(conn, morphoDeviceId);
					for (int k=0,ks=driverBeanList==null? 0 : driverBeanList.size();k<ks;k++) {
						long t = 0;
						long st = System.currentTimeMillis();
						if(handler != null && k==0){
							handler.onChange(true);
							handler.notifyText("Record found: "+ks);
							if(syncAll){
								auth.deleteAll();//debug
							}
						}
						DriverBean driverBean = driverBeanList.get(k);
						id = driverBean.id;
						if(!Utils.isNull(driverBean.dl_no))
							dlNo = driverBean.dl_no;
						status = driverBean.status;
						//System.out.println("Enroll For id= "+id+" Driver Name= "+driverBean.driver_name);
						ArrayList <byte[]> byteArrList = driverBean.capture_template;
						boolean fpUpdated = false;
						if(!syncAll){
							clearAllregisteredUserFingers(id,auth);
						}
						int enrollStatus = Misc.getUndefInt();
						int[] enrollResultArray = getEmptyIntArray(10);
						for (int i = 0,is = byteArrList == null ? null : byteArrList.size(); i < is; i++) {
							byte[] dbTemplate = byteArrList.get(i);   
							if(dbTemplate == null || dbTemplate.length <= 0)
								continue;
							enrollResult = Misc.getUndefInt();
							String newId = ""+id+"_"+(i+1);
							
							if (status == 1 && driverBean.task != DriverBean.DELETE){
								enrollResult = auth.enrollUser(newId, "", "", dbTemplate, dbTemplate.length);
								count++;	
							}
							if(enrollResult == 0 || enrollResult == -12 || driverBean.task == DriverBean.DELETE)//0-success -12-already enrolled
								fpUpdated = true;
							if((status == 2 || status == 0) && i == 9)
								fpUpdated = true;
							//fpUpdated = true;
							System.out.println("Enroll count "+(enrollResult == 0 ? m_count++ : m_count)+" Result "+ enrollResult +" for id= "+ id + " : newId= " +newId+" : driver name= " + driverBean.driver_name+"Driver name= "+dlNo + " : capture_template_" + (i+1) +"= "+ enrollResult);
						}
						if(true){//fpUpdated){
							updateDriverDetails(conn, id, morphoId,DriverBean.INSERT,1,driverBean.readTimestamp,null,enrollResultArray);
							if(handler != null){
								t = System.currentTimeMillis() - st;
								String timeRemaining = formatTimeInterval((ks-k-1)*t/(60*1000));
//								handler.notifyText(timeRemaining);
								handler.notifyText("Sync:"+(k+1)+"/"+ks + "(" + timeRemaining+")");
							}
						}
						if(conn != null)
							conn.commit();
						
						System.out.println("Enroll Time "+t);
					}
					System.out.println("Total Enrollment = "+ (count -1));

					System.out.println("Loaded All the active driver details in Finger Print Device ");

				}else{

				} 
			}
		}catch (UnsatisfiedLinkError e) {
			e.printStackTrace();
			System.out.println("Error in Loading Library" + e);
		} catch (Exception ex) {
			ex.printStackTrace();
			System.out.println("Error in Loading Library" + ex);
		}finally{
			if(handler != null){
				handler.onChange(false);
			}

		}
	}
	public static void loadInFingerPrintDeviceSDK (Connection conn,boolean syncAll,SynServiceHandler handler,boolean singleFullSync){
		try {
			//First create an object of any name of LoadLibrary Class  
			System.out.println("Loading All the active driver details ");
			int id = Misc.getUndefInt();
			int morphoId = Misc.getUndefInt();
			MorphoSmartFunctions morpho = null;
			
				morpho = MorphoSmartFunctions.getMorpho();
				System.out.println("Is deviceConnected : " + morpho.isConnected());
				if(morpho.isConnected()){
					morphoDeviceId = morpho.getDeviceId();
					if(Utils.isNull(morphoDeviceId))
						return;
					
					System.out.println("Morpho device Info:"+ morphoDeviceId);
					int count = 0;
					morphoId = isExist(conn, morphoDeviceId);
					morphoDeviceEnroll = morpho.getTotalEnrolled();
					morphoDeviceCapacity = morpho.getCapacity();
					
					Pair<Long, Integer> morphoSyncStatus = getMorphoSyncStatus(conn, morphoId);
					long lastReadTimeStamp = morphoSyncStatus == null ? Misc.getUndefInt() : morphoSyncStatus.first;
					int syncAllFlag = morphoSyncStatus == null ? Misc.getUndefInt() : morphoSyncStatus.second;
					syncAll = morphoDeviceEnroll == 0 || singleFullSync || syncAllFlag == 1 || Misc.isUndef(lastReadTimeStamp);
					Pair<ArrayList<DriverBean>, ArrayList<DriverBean>> driverBeanListPair = getDriverDataToSyncNew(conn, morphoId,lastReadTimeStamp,syncAll);
			synchronized (MorphoSmartFunctions.lock) {
					if(handler != null){
						handler.onChange(true);
						handler.setDeviceId(morphoDeviceId);
						handler.setCapacity(morphoDeviceCapacity);
						handler.setEnrolled(morphoDeviceEnroll);
					}
					ArrayList<DriverBean> driverBeanListForEnroll = driverBeanListPair == null ? null : driverBeanListPair.first;
					ArrayList<DriverBean> driverBeanListForRemove = driverBeanListPair == null ? null : driverBeanListPair.second;
					if(Misc.isUndef(morphoId))
						morphoId = insertMorphoDevice(conn, morphoDeviceId);
					/*for (int k=0,ks=driverBeanList==null? 0 : driverBeanList.size();k<ks;k++) {
						if(driverBeanList.get(k).status != 1 || (driverBeanList.get(k).capture_template == null)){
							if(driverBeanListForRemove == null)
								driverBeanListForRemove = new ArrayList<DriverBean>();
							driverBeanListForRemove.add(driverBeanList.get(k));
						}
						else{
							if(driverBeanListForEnroll == null)
								driverBeanListForEnroll = new ArrayList<DriverBean>();
							driverBeanListForEnroll.add(driverBeanList.get(k));
						}
					}*/
					if(!syncAll){
						for (int k=0,ks=driverBeanListForRemove==null? 0 : driverBeanListForRemove.size();k<ks;k++) {
							if(handler != null && k==0){
								/*String dots = "";
							for(int i=0;i<(k%10);i++)
								dots += ".";
							handler.notifyText("Clear Records"+dots);*/
								handler.clearingData();
							}
							int[] enrollStatusList =   getEmptyIntArray(10);
							if(!syncAll)
								enrollStatusList[0] = morpho.remove(driverBeanListForRemove.get(k).id);
							/*try{
								updateDriverDetails(conn, driverBeanListForRemove.get(k).id, morphoId,DriverBean.INSERT,1,driverBeanListForRemove.get(k).readTimestamp,null,getEmptyIntArray(10));
								conn.commit();
							}catch(Exception ex){
								ex.printStackTrace();
							}*/
						}
					}
					boolean updateLastRead = false;
					for (int k=0,ks=driverBeanListForEnroll==null? 0 : driverBeanListForEnroll.size();k<ks;k++) {
						long t = 0;
						long st = System.currentTimeMillis();
						if(handler != null && k==0){
							handler.notifyText("Record found: "+ks);
						}
						DriverBean driverBean = driverBeanListForEnroll.get(k);
						id = driverBean.id;
						if(!updateLastRead && !Misc.isUndef(driverBean.readTimestamp)){
							lastReadTimeStamp = driverBean.readTimestamp;
							updateLastRead = true;
						}
						ArrayList <byte[]> byteArrList = driverBean.capture_template;
						int[] enrollStatusList =   getEmptyIntArray(10);
						enrollStatusList[0] = morpho.enroll(id+"", byteArrList);
						if(enrollStatusList[0] == MorphoSmartSDK.MORPHOERR_PROTOCOLE)
							return;
						System.out.println("["+k+"]driver enroll result:("+driverBean.id+","+enrollStatusList[0]+")");
						if(true){//fpUpdated){
							/*try{	
								updateDriverDetails(conn, id, morphoId,DriverBean.INSERT,1,driverBean.readTimestamp,null,enrollStatusList);
							}catch(Exception ex){
								ex.printStackTrace();
							}*/
							if(handler != null){
								t = System.currentTimeMillis() - st;
								String timeRemaining = formatTimeInterval((ks-k-1)*t/(60*1000));
								handler.notifyText("Sync:"+(k+1)+"/"+ks + "(" + timeRemaining+")");
							}
						}
						if(conn != null)
							conn.commit();
						System.out.println("Enroll Time "+t);
					}
					morphoDeviceEnroll = morpho.getTotalEnrolled();
					syncAllFlag = 0;
					
					updateMorphoDeviceStatus(conn, morphoId, morphoDeviceEnroll,lastReadTimeStamp,syncAllFlag);
					System.out.println("Total Enrollment = "+ (count -1));
					System.out.println("Loaded All the active driver details in Finger Print Device ");
				}
			}
		}catch (Exception ex) {
			ex.printStackTrace();
			System.out.println("Error in Loading Library" + ex);
		}finally{
			if(handler != null){
				handler.onChange(false);
			}

		}
	} 
	public static void TestSmartSDK (Connection conn){
		try {
			System.out.println("Loading All the active driver details ");
			String morphoDeviceId = null;
			int enrollResult = Misc.getUndefInt();
			int id = Misc.getUndefInt();
			boolean deviceConnected = true;//auth.isDeviceConnected();
			int morphoId = Misc.getUndefInt();
			if(deviceConnected){
				morphoDeviceId = "867687";
				morphoId = isExist(conn, morphoDeviceId);
				Pair<ArrayList<DriverBean>, ArrayList<DriverBean>> driverBeanListPair = getDriverDataToSync(conn, morphoId,true); 
				ArrayList<DriverBean> driverBeanList = driverBeanListPair == null ? null : driverBeanListPair.first;
				if(Misc.isUndef(morphoId))
					morphoId = insertMorphoDevice(conn, morphoDeviceId);
				//MorphoSmartFunctions.removeAll();
				for (int k=0,ks=driverBeanList==null? 0 : driverBeanList.size();k<ks;k++) {
					long t = 0;
					long st = System.currentTimeMillis();
					DriverBean driverBean = driverBeanList.get(k);
					id = driverBean.id;
					if(id == 18 || id== 4906){
						//enrollResult = MorphoSmartFunctions.identifyUser(id+"",driverBean.capture_template);
					}else{
						continue;
					}
					//enrollResult = MorphoSmartFunctions.enroll(id+"", driverBean.capture_template);
					System.out.println("["+k+"]driver enroll result:("+driverBean.id+","+enrollResult+")");
				}
			}else{

			} 
		}catch (UnsatisfiedLinkError e) {
			e.printStackTrace();
			System.out.println("Error in Loading Library" + e);
		} catch (Exception ex) {
			ex.printStackTrace();
			System.out.println("Error in Loading Library" + ex);
		}finally{
		}
	}
	
	private static void clearAllregisteredUserFingers(int id, LoadLibrary auth) {
		// TODO Auto-generated method stub
		int enrollResult = Misc.getUndefInt();
		for(int i=0; i<10; i++){
			String newId = ""+id+"_"+(i+1);
			enrollResult = auth.deleteUserById(newId);
			//System.out.println("deleted for fp_id =" + newId + " Result: " + enrollResult);
		}
	}
	public static int isExist(Connection conn,String morphoDeviceId){

		int retval = Misc.getUndefInt();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try{
			if(!Utils.isNull(morphoDeviceId)){
				ps = conn.prepareStatement("select id from morpho_device where morpho_device_id like '%"+morphoDeviceId+"%'");
				rs = ps.executeQuery();
				while(rs.next()){
					retval = Misc.getRsetInt(rs, "id");
				}
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}finally{
			try{
				if(rs != null)
					rs.close();
				if(ps != null)
					ps.close();

			}catch(Exception ex){
				ex.printStackTrace();
			}
		}
		return retval;
	}
	private static int isRecordExist(Connection conn,int morphoId,int driverId){
		int retval = Misc.getUndefInt();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try{
			if(!Misc.isUndef(morphoId) && !Misc.isUndef(morphoId)){
				ps = conn.prepareStatement("select id from morpho_sync_status where morpho_id=? and driver_id=?");
				Misc.setParamInt(ps, morphoId, 1);
				Misc.setParamInt(ps, driverId, 2);
				rs = ps.executeQuery();
				while(rs.next()){
					retval = Misc.getRsetInt(rs, "id");
				}
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}finally{
			try{
				if(rs != null)
					rs.close();
				if(ps != null)
					ps.close();

			}catch(Exception ex){
				ex.printStackTrace();
			}
		}
		return retval;
	}
	private static ArrayList<Integer> getMorphoDevices(Connection conn){
		ArrayList<Integer> retval = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try{
			ps = conn.prepareStatement("select id from morpho_device");
			rs = ps.executeQuery();
			while(rs.next()){
				if(retval == null)
					retval = new ArrayList<Integer>();
				retval.add(Misc.getRsetInt(rs, "id"));
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}finally{
			try{
				if(rs != null)
					rs.close();
				if(ps != null)
					ps.close();
			}catch(Exception ex){
				ex.printStackTrace();
			}
		}
		return retval;
	}
	public static void updateDriverSyncStatus(Connection conn, int driverId,int task,int status,long readTimeStamp){
		updateDriverSyncStatus(conn, driverId, task, status, readTimeStamp, null);
	}
	public static void updateDriverSyncStatus(Connection conn, int driverId,int task,int status,long readTimeStamp, StringBuilder sb){
		ArrayList<Integer> deviceList = null;
		try{
			deviceList = getMorphoDevices(conn);
			if(deviceList != null && deviceList.size() > 0){
				for(Integer morphoId : deviceList)
					updateDriverDetails(conn, driverId, morphoId,task,status,readTimeStamp,sb);
			}

		}catch(Exception ex){
			ex.printStackTrace();
		}finally{

		}
	}
	public static int[] getEmptyIntArray(int size){
		if(size <= 0)
			return null;
		int[] array = new int[size];
		for (int i = 0; i < size; i++) {
			array[i] = Misc.getUndefInt();
		}
		return array;
	}
	public static void updateDriverDetails(Connection conn, int driverId, int morphoId,int taskId,int status,long readTimeStamp) throws Exception{
		updateDriverDetails(conn, driverId, morphoId, taskId, status,readTimeStamp, null);
	}
	public static void updateDriverDetails(Connection conn, int driverId, int morphoId,int taskId,int status, long readTimeStamp, StringBuilder sb) throws Exception{
		updateDriverDetails(conn, driverId, morphoId, taskId, status,readTimeStamp, null, getEmptyIntArray(10));
	}
	public static void updateDriverDetails(Connection conn, int driverId, int morphoId,int taskId,int status,long readTimeStamp, StringBuilder sb,int[] enrollResult) throws Exception{
		int recordId = Misc.getUndefInt();
		PreparedStatement ps = null;

		try{
			if(!Misc.isUndef(morphoId) && !Misc.isUndef(morphoId)){
				recordId = isRecordExist(conn, morphoId, driverId);
				if(Misc.isUndef(recordId)){//create
					ps = conn.prepareStatement("insert into morpho_sync_status(morpho_id,driver_id,task,status,finger_1_enroll,finger_2_enroll,finger_3_enroll,finger_4_enroll,finger_5_enroll,finger_6_enroll,finger_7_enroll,finger_8_enroll,finger_9_enroll,finger_10_enroll,updated_on) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,now())");
					Misc.setParamInt(ps, morphoId, 1);
					Misc.setParamInt(ps, driverId, 2);
					Misc.setParamInt(ps, taskId, 3);
					Misc.setParamInt(ps, status, 4);
					Misc.setParamInt(ps, enrollResult[0], 5);
					Misc.setParamInt(ps, enrollResult[1], 6);
					Misc.setParamInt(ps, enrollResult[2], 7);
					Misc.setParamInt(ps, enrollResult[3], 8);
					Misc.setParamInt(ps, enrollResult[4], 9);
					Misc.setParamInt(ps, enrollResult[5], 10);
					Misc.setParamInt(ps, enrollResult[6], 11);
					Misc.setParamInt(ps, enrollResult[7], 12);
					Misc.setParamInt(ps, enrollResult[8], 13);
					Misc.setParamInt(ps, enrollResult[9], 14);
					ps.executeUpdate();
					conn.commit();
				}else{//update
					ps = conn.prepareStatement("update morpho_sync_status set status = ?, task= ?, finger_1_enroll=?,finger_2_enroll=?,finger_3_enroll=?,finger_4_enroll=?,finger_5_enroll=?,finger_6_enroll=?,finger_7_enroll=?,finger_8_enroll=?,finger_9_enroll=?,finger_10_enroll=?,updated_on=now() where morpho_id=? and driver_id=?"+(!Misc.isUndef(readTimeStamp) ? " and updated_on<=?" : ""));
					Misc.setParamInt(ps, status, 1);
					Misc.setParamInt(ps, taskId, 2);
					Misc.setParamInt(ps, enrollResult[0], 3);
					Misc.setParamInt(ps, enrollResult[1], 4);
					Misc.setParamInt(ps, enrollResult[2], 5);
					Misc.setParamInt(ps, enrollResult[3], 6);
					Misc.setParamInt(ps, enrollResult[4], 7);
					Misc.setParamInt(ps, enrollResult[5], 8);
					Misc.setParamInt(ps, enrollResult[6], 9);
					Misc.setParamInt(ps, enrollResult[7], 10);
					Misc.setParamInt(ps, enrollResult[8], 11);
					Misc.setParamInt(ps, enrollResult[9], 12);
					Misc.setParamInt(ps, morphoId, 13);
					Misc.setParamInt(ps, driverId, 14);
					if(!Misc.isUndef(readTimeStamp))
						ps.setTimestamp(15, new Timestamp(readTimeStamp));
					ps.executeUpdate();
					conn.commit();
				}
				if(sb != null)
					sb.append("\nupdateDriverDetails : " + ps.toString());
			}

		}catch(Exception ex){
			ex.printStackTrace();
			throw ex;
		}finally{
			try{
				if(ps != null)
					ps.close();

			}catch(Exception ex){
				ex.printStackTrace();
			}
		}
	}
	public static int insertMorphoDevice(Connection conn, String morphoDeviceId){
		int retval = Misc.getUndefInt();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try{
			ps = conn.prepareStatement("insert into morpho_device(morpho_device_id) values (?)");
			ps.setString(1, morphoDeviceId);
			ps.executeUpdate();
			rs = ps.getGeneratedKeys();
			if(rs.next())
				retval = Misc.getRsetInt(rs, 1);
		}catch(Exception ex){
			ex.printStackTrace();
		}finally{
			try{
				if(rs != null)
					rs.close();
				if(ps != null)
					ps.close();

			}catch(Exception ex){
				ex.printStackTrace();
			}
		}
		return retval;
	}
	public static int updateMorphoDeviceStatus(Connection conn, int morphoId,int enrollCount,long readTimeStamp,int syncFlag){
		int retval = Misc.getUndefInt();
		PreparedStatement ps = null;
		try{
			ps = conn.prepareStatement("update morpho_device set enroll_count=?,last_read_time=?,full_sync=? where id=?");
			Misc.setParamInt(ps, enrollCount, 1);
			ps.setTimestamp(2, new Timestamp(readTimeStamp));
			Misc.setParamInt(ps, syncFlag, 3);
			Misc.setParamInt(ps, morphoId, 4);
			ps.executeUpdate();
		}catch(Exception ex){
			ex.printStackTrace();
		}finally{
			Misc.closePS(ps);
		}
		return retval;
	}
	public static int deleteMorphoSyncStatus(Connection conn, int morphoDeviceId){
		int retval = Misc.getUndefInt();
		PreparedStatement ps = null;
		try{
			ps = conn.prepareStatement("delete from morpho_sync_status where morpho_id=?");
			Misc.setParamInt(ps, morphoDeviceId, 1);
			ps.executeUpdate();
		}catch(Exception ex){
			ex.printStackTrace();
		}finally{
			try{
				if(ps != null)
					ps.close();
			}catch(Exception ex){
				ex.printStackTrace();
			}
		}
		return retval;
	}

	public static void syncAllDriverToDevice(Connection conn){
		try{
			boolean syncAll = checkSyncAll();
			if(syncAll){
				System.out.println("Morpho sync all entries start");
				loadInFingerPrintDevice(conn,syncAll);
				updateFileStatus(0);
				System.out.println("Morpho sync all entries end");
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	public static void main(String[] args) {
		Connection conn = null;
		try {
			conn = DBConnectionPool.getConnectionFromPoolNonWeb();
			long n=0;
			Pair<ArrayList<DriverBean>, ArrayList<DriverBean>> t =  getDriverDataToSyncNew(conn,1,n, false);

			
//			loadInFingerPrintDeviceSDK(conn, false, new SynServiceHandler() {
//				
//				@Override
//				public void onChange(boolean onChange) {
//					// TODO Auto-generated method stub
//					
//				}
//				
//				@Override
//				public void notifyText(String msg) {
//					// TODO Auto-generated method stub
//					
//				}
//
//				@Override
//				public void setDeviceId(String msg) {
//					// TODO Auto-generated method stub
//					
//				}
//
//				@Override
//				public void setCapacity(int capacity) {
//					// TODO Auto-generated method stub
//					
//				}
//
//				@Override
//				public void setEnrolled(int enrolled) {
//					// TODO Auto-generated method stub
//					
//				}
//
//				@Override
//				public void clearingData() {
//					// TODO Auto-generated method stub
//					
//				}
//
//				@Override
//				public void init(String deviceId, int capacity, int enrolled) {
//					// TODO Auto-generated method stub
//					
//				}
//			},false);
			//TestSmartSDK(conn);
			/*boolean syncAll = checkSyncAll();
			//loadInFingerPrintDevice(_dbConnection,true);
			ArrayList<DriverBean> failedDrivers = checkDB(conn, true, null, null);
			ArrayList<DriverBean> failedDrivers2 = checkDB(conn, true, null, failedDrivers);
			System.out.println();
			if(syncAll)
				updateFileStatus(0);*/
		} catch (Exception ex) {
			Logger.getLogger(DriverBean.class.getName()).log(Level.SEVERE, null, ex);
		}
		finally{
			try {
				DBConnectionPool.returnConnectionToPoolNonWeb(conn);
			} catch (GenericException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	private static String formatTimeInterval(long num) {
		long hr = num/60;
		long min = num - hr*60;
		long days = hr/24;
		hr = hr - days*24;
		if (hr == 0 && days == 0)
			return Long.toString(num)+"m";
		else if (days == 0) {
			return Long.toString(hr)+"h:"+Long.toString(min)+"m";
		}
		else {
			return Long.toString(days)+"d:"+Long.toString(hr)+"h:"+Long.toString(min)+"m";
		}			
	}
	public static Pair<Long, Integer> getMorphoSyncStatus(Connection conn, int morphoId){
		PreparedStatement ps = null;
		ResultSet rs = null;
		Pair<Long, Integer> retval = null;
		try {
			ps = conn.prepareStatement("select last_read_time, full_sync from morpho_device where id=? ");
			Misc.setParamInt(ps, morphoId, 1);
			rs = ps.executeQuery();
			if(rs.next()){
			    Timestamp ts = rs.getTimestamp(1);
				retval = new Pair<Long, Integer>(ts != null ? ts.getTime() : Misc.getUndefInt(), Misc.getRsetInt(rs, 2) );
			}
			if(rs != null)
				rs.close();
			if(ps != null)
				ps.close();
		}catch(Exception e){
			e.printStackTrace();
			//throw new GenericException(e);
		}
		return retval;

	}
}
