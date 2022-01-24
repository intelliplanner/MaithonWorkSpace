package com.ipssi.rfid.processor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import com.ipssi.gen.exception.GenericException;
import com.ipssi.gen.utils.CacheTrack;
import com.ipssi.gen.utils.DBConnectionPool;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.Pair;
import com.ipssi.gen.utils.Triple;
import com.ipssi.rfid.beans.MaterialProcessSeqBean;
import com.ipssi.rfid.beans.RFIDHolder;
import com.ipssi.rfid.beans.TPRecord;
import com.ipssi.rfid.beans.TPStep;
import com.ipssi.rfid.beans.Vehicle;
import com.ipssi.rfid.constant.Status;
import com.ipssi.rfid.constant.Status.TPR;
import com.ipssi.rfid.constant.Type;
import com.ipssi.rfid.db.Criteria;
import com.ipssi.rfid.db.RFIDMasterDao;

public class TPRInformation {
	
	
	private static int sameStationTprThreshold = 150*60*1000;
	private static final int newLatestTprWebThreshold = 60*60*1000;
	//private static final boolean traceToDB = true;
	
	
	private static final ArrayList<Pair<Integer, Integer>> processList = new ArrayList<Pair<Integer,Integer>>();
	static{
		processList.add(new Pair<Integer, Integer>(Type.WorkStationType.GATE_IN_TYPE, Type.WorkStationType.WEIGH_BRIDGE_OUT_TYPE));
		processList.add(new Pair<Integer, Integer>(Type.WorkStationType.STONE_TARE_WT_TYPE, Type.WorkStationType.STONE_GROSS_WT_TYPE));
		processList.add(new Pair<Integer, Integer>(Type.WorkStationType.FLY_ASH_IN_TYPE, Type.WorkStationType.FLY_ASH_GROSS_WT_TYPE));
		processList.add(new Pair<Integer, Integer>(Type.WorkStationType.FIRST_WEIGHTMENT_TYPE, Type.WorkStationType.SECOND_WEIGHTMENT_TYPE));
	}

	public static void setSameStationTprThresholdMinutes(int minutes){
		if(!Misc.isUndef(minutes))
			sameStationTprThreshold = minutes*60*1000;
	}
	public static long getSameStationTprThresholdMinutes(){
			return sameStationTprThreshold; 
	}

	public static Pair<Integer, String> getVehicle(Connection conn, String epcId, String vehicleName) {
		ArrayList<Object> list = null;
		try {
			Vehicle veh = new Vehicle();
			veh.setStatus(1);
			if(epcId != null && epcId.length() > 0 && !epcId.equalsIgnoreCase("E000000000000000000000E0")){
				veh.setEpcId(epcId);
				list = (ArrayList<Object>) RFIDMasterDao.select(conn, veh);
				if (list != null && list.size() > 0) {
					return new Pair<Integer, String>(((Vehicle) list.get(0)).getId(),((Vehicle) list.get(0)).getVehicleName());
				}
				veh.setEpcId(null);
			}
			if(vehicleName != null && vehicleName.length() > 0){
				veh.setStdName(CacheTrack.standardizeName(vehicleName));
				//veh.setVehicleName(vehicleName);
				list = (ArrayList<Object>) RFIDMasterDao.select(conn, veh);
				if (list != null && list.size() > 0) {
					return new Pair<Integer, String>(((Vehicle) list.get(0)).getId(),((Vehicle) list.get(0)).getVehicleName());
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}
	/*public static Triple<TPRecord, Integer, Boolean> getLatestTPR(Connection conn, String vehicleName, RFIDHolder holder,boolean create) {
		return getLatestTPR(conn, vehicleName, holder, create,false,Misc.getUndefInt());
	}
	public static Triple<TPRecord, Integer, Boolean> getLatestTPR(Connection conn, String vehicleName, RFIDHolder holder,boolean create,boolean isWeb) {
		return getLatestTPR(conn, vehicleName, holder, create,isWeb,Misc.getUndefInt());
	}
	public static Triple<TPRecord, Integer, Boolean> getLatestTPR(Connection conn, String vehicleName, RFIDHolder holder,boolean create, boolean isWeb, int workstationTypeId) {
		return getLatestTPR(conn, vehicleName, holder, create, isWeb, workstationTypeId, 0);
	}*/
	public static Triple<TPRecord, Integer, Boolean> getLatestNonWeb(Connection conn, String vehicleName, RFIDHolder holder, int workstationTypeId, int materialCat) throws Exception {
		int vehId = Misc.getUndefInt();
		Pair<Integer, String> vehPair = null;
		try {
			if (holder != null && !Misc.isUndef(holder.getVehicleId())) {
				vehId = holder.getVehicleId();
				System.out.println("HOLDER INFORMATION  : "+vehId);
			} else {
				vehPair = getVehicle(conn, (holder != null ? holder.getEpcId() : null), vehicleName); //CacheTrack.VehicleSetup.getSetupByStdName(stdName, conn);
				if(vehPair != null){
					vehId = vehPair.first;
					vehicleName = vehPair.second;
				}
				System.out.println("GetVehicle INFORMATION  : "+vehId);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			throw ex;
		}
		return getLatestNonWeb(conn, vehId, holder, vehicleName, workstationTypeId, materialCat); //(conn, vehId, holder,vehicleName,create,isWeb,workstationTypeId);
	}

	public static TPRecord createTpr(Connection conn, int vehicleId, RFIDHolder holder,String vehicleName,int isLatest,boolean isWeb, boolean isDataUseful, boolean updateOnlyRFEntry, int materialCat) throws Exception{
		return updateTpr(conn, vehicleId, holder, vehicleName, new TPRecord(), isLatest,isWeb, isDataUseful, updateOnlyRFEntry, materialCat);//(conn, vehicleId, holder, vehicleName, new TPRecord(), isLatest, isDataUseful);
	}
	private static TPRecord updateTpr(Connection conn, int vehicleId, RFIDHolder holder,String vehicleName,TPRecord tpr,int isLatest,boolean isWeb,boolean isDataUseful,boolean updateOnlyRFEntry, int materialCat) throws Exception{
		TPRecord retval = null;
//		try{
			retval = tpr;
			if(Misc.isUndef(tpr.getTprId())){
				retval.setVehicleId(vehicleId);
				retval.setVehicleName(vehicleName);
				retval.setTprCreateDate(new Date());
				retval.setTprStatus(TPR.OPEN);
				if(!Misc.isUndef(isLatest))
					retval.setLatest(isLatest);
				MaterialProcessSeqBean materialProcessSeqBean = new MaterialProcessSeqBean();
				materialProcessSeqBean.setMaterialType(materialCat);
				materialProcessSeqBean.setStatus(1);
				ArrayList<MaterialProcessSeqBean> matProcessSeqList = (ArrayList<MaterialProcessSeqBean>) RFIDMasterDao.getList(conn, materialProcessSeqBean, null); 
				int nextWorkStationType = 1;
				for(int i=0,is=matProcessSeqList == null ? 0 : matProcessSeqList.size(); i<is; i++){
					if(matProcessSeqList.get(i).getSeq() == 1){
						nextWorkStationType = matProcessSeqList.get(i).getWorkstationType();
						break;
					}
				}
				retval.setNextStepType(nextWorkStationType);
			}
			if (isDataUseful) {
				if(!updateOnlyRFEntry){
					retval.setTransporterId(holder.getTransporterId());
					retval.setMinesId(holder.getMinesId());
					retval.setMaterialGradeId(holder.getGrade());
					//retval.setVehicleName(vehicleName);//
					retval.setChallanDate(holder.getDatetime());
					retval.setChallanNo(holder.getChallanId());
					retval.setLrDate(holder.getLrDate());
					retval.setLrNo(holder.getLRID());
					retval.setLoadTare(holder.getLoadTare());
					retval.setLoadGross(holder.getLoadGross());
					retval.setDoId(holder.getDoId());
				}
				//update rf fields also
				retval.setRfVehicleName(holder.getVehicleName());
				retval.setRfVehicleId(holder.getVehicleId());
				retval.setRfTransporterId(holder.getTransporterId());
				retval.setRfMinesId(holder.getMinesId());
				retval.setRfGrade(holder.getGrade());
				retval.setRfChallanDate(holder.getDatetime());
				retval.setRfChallanId(holder.getChallanId());
				retval.setRfLRDate(holder.getLrDate());
				retval.setRfLRId(holder.getLRID());
				retval.setRfLoadTare(holder.getLoadTare());
				retval.setRfLoadGross(holder.getLoadGross());
				retval.setRfDeviceId(holder.getDeviceId());
				retval.setRfDOId(holder.getDoId());
				retval.setRfRecordId(holder.getId());
				retval.setRfRecordKey(holder.getRecordKey());
				retval.setM_trip_id(holder.getId());
				retval.setIsMergedWithHHTpr(1);
				retval.setHhDeviceId(holder.getDeviceId());
				if(isWeb)
					retval.setHhTprMergedTime(new Date());
				else
					retval.setRfCardDataMergeTime(new Date());
				retval.setSrcDeviceLogId(holder.getGeneratedId());
				//tpr.set
			}
			if(Misc.isUndef(retval.getMaterialCat())){
				if(!Misc.isUndef(materialCat)){
					retval.setMaterialCat(materialCat);
				}else if(holder != null && !Misc.isUndef(holder.getMaterial())){
					retval.setMaterialCat(holder.getMaterial());
				}else{
					retval.setMaterialCat(Type.TPRMATERIAL.COAL);
				}
			}
			/*if(retval.getTprId() <= 0){
				MaterialProcessSeqBean materialProcessSeqBean = new MaterialProcessSeqBean();
				materialProcessSeqBean.setMaterialType(materialCat);
				materialProcessSeqBean.setStatus(1);
				ArrayList<MaterialProcessSeqBean> matProcessSeqList = (ArrayList<MaterialProcessSeqBean>) RFIDMasterDao.getList(conn, materialProcessSeqBean, null); 
				int nextWorkStationType = 1;
				for(int i=0,is=matProcessSeqList == null ? 0 : matProcessSeqList.size(); i<is; i++){
					if(matProcessSeqList.get(i).getSeq() == 1){
						nextWorkStationType = matProcessSeqList.get(i).getWorkstationType();
						break;
					}
				}
				retval.setNextStepType(nextWorkStationType);
			}*/
			if(Misc.isUndef(retval.getTprId()))//review
				retval.setRfAlsoOnCard(holder == null ? 0 : (isDataUseful ? 1 : 2));//0-no RF Data ,1-useful rf data ,2-invalid rf data
		/*}catch(Exception ex){
			ex.printStackTrace();
		}*/
		return retval;
	}
	/*synchronized public static Triple<TPRecord, Integer, Boolean> getLatestTPROld(Connection conn, int vehicleId, RFIDHolder holder,String vehicleName,boolean create,boolean isWeb, int workstationTypeId, int materialCat) {
		StringBuilder sb = new StringBuilder();
		TPRecord tpr = null;
		TPRecord latestTpr = null;
		int status = Status.VALIDATION.NO_ISSUE;
		boolean isHHSync = false;
		ArrayList<Object> list = null;
		boolean isFound = false;
		int isLatest = Misc.getUndefInt();
		boolean isDataUseful = false;
		boolean fillEmptyTPR = false;
		boolean isSameStationProcessing = false;
		sb.append("\n@@@@GET Latest TPR["+vehicleId+","+vehicleName+"]").append("\n");
		try {
			if (!Misc.isUndef(vehicleId)) {
				if(holder != null){
					isDataUseful = holder.isDataUseful(conn, isWeb) == RFIDHolder.RF_DATA_USEABLE;
					sb.append("[Holder KEY]:"+holder.getRecordKey()).append("\n");
					sb.append("[Holder DATA VALIDITY]:"+isDataUseful).append("\n");
				}
				tpr = new TPRecord();
				tpr.setVehicleId(vehicleId);
				tpr.setTprStatus(TPR.OPEN);
				//tpr.setMaterialCat(materialCat);
				Criteria cr = new Criteria(TPRecord.class);
				cr.setOrderByClause("coalesce(tp_record.challan_date,tp_record.tpr_create_date)");
				cr.setDesc(true);
				list = (ArrayList<Object>) RFIDMasterDao.select(conn, tpr,cr);
				sb.append("Record Found:"+(list == null ? 0 : list.size())).append("\n");
				for(int i=0,is=list == null ? 0 : list.size();i<is;i++){
					TPRecord tprEntry = (TPRecord) list.get(i);
					if(tprEntry.isLatest() == 1){
						latestTpr = tprEntry;
						sb.append("[Latest Found]:"+latestTpr.getTprId()).append("\n");
					}
					if(isDataUseful){
						RFIDHolder tempRfHolder = tprEntry.getHolderRFData();
						RFIDHolder tempManualHolder = tprEntry.getHolderManualData();
						sb.append("[Match With Existing TPR]:"+tprEntry.getTprId()).append("\n");
						if(tempRfHolder != null ){
							sb.append("@@[TPR RF DATA]@@").append("\n");
							sb.append("[TPR RF KEY]:"+tempRfHolder.getRecordKey()).append("\n");
							sb.append("[TPR RF Match]:"+tempRfHolder.equalsIgnoreChallanNumber(holder)).append("\n");
							sb.append(tempRfHolder.toString());
							if(!tempRfHolder.getRecordKey().equalsIgnoreCase(holder.getRecordKey())){
//								if(tempRfHolder.equalsIgnoreChallanNumber(holder)){
								if(tempRfHolder.isMergeable(holder)){
									isFound = true;
									//update rf and manual part of tpr only
									updateTpr(conn, vehicleId, holder, vehicleName, tprEntry, isLatest,isWeb, isDataUseful,true,materialCat);
								}
							}else{
								isFound = true;
							}
							sb.append("[TPR RF DATA Merge]:"+isFound).append("\n");
						}else if(tempManualHolder != null){
							sb.append("@@[TPR Manual DATA]@@").append("\n");
							sb.append("[TPR Manual KEY]:"+tempManualHolder.getRecordKey()).append("\n");
							sb.append("[TPR Manual Match]:"+tempManualHolder.equalsIgnoreChallanNumber(holder)).append("\n");
							sb.append(tempManualHolder.toString());
							//tempManualHolder.printData();
							//if(tempManualHolder.equalsIgnoreChallanNumber(holder) ){
							if(tempManualHolder.isMergeable(holder)){
								isFound = true;
								//update rf part of tpr only
								updateTpr(conn, vehicleId, holder, vehicleName, tprEntry, isLatest,isWeb, isDataUseful,true,materialCat);
							}
							sb.append("[TPR Manual DATA Merge]:"+isFound).append("\n");
						}else if(latestTpr != null){
							//System.out.println("[TPR Challan Data is not present]:");
							long rfTime = holder != null && holder.getDatetime() != null ? holder.getDatetime().getTime() : Misc.getUndefInt();
							long latestTprTime = latestTpr != null ?  (latestTpr.getChallanDate() != null ?  latestTpr.getChallanDate().getTime() : (latestTpr.getEarliestUnloadGateInEntry() != null ? latestTpr.getEarliestUnloadGateInEntry().getTime() : Misc.getUndefInt())) : Misc.getUndefInt();
							if(!Misc.isUndef(rfTime) && !Misc.isUndef(latestTprTime) && (latestTprTime - rfTime) >= 0 ){
								isFound = true;
								fillEmptyTPR = true;
							}
							sb.append("[Empty TPR Found]:"+fillEmptyTPR).append("\n");
						}
					}
				}//end of searching for existing open record
				sb.append("[Latest TPR]:"+latestTpr).append("\n");
				sb.append("@@@[toCreateNew]@@@").append("\n");
				sb.append("workstationTypeId:"+workstationTypeId).append("\n");
				sb.append("create:"+create).append("\n");
				sb.append("isDataUseful:"+isDataUseful).append("\n");
				sb.append("isFound:"+isFound).append("\n");
				sb.append("isWeb:"+isWeb).append("\n");
				isSameStationProcessing = isSameWorkStation(latestTpr, workstationTypeId);
				boolean toCreateNew = isWeb ? create && isDataUseful && !isFound
						: (
						  (
					      (latestTpr == null && workstationTypeId != Type.WorkStationType.GATE_OUT_TYPE ) 
					      || 
					      (!isSameStationProcessing 
//					    		  && isGreaterThanEqualsProcessedOld(latestTpr, workstationTypeId);
					    		  && isGreaterThanEqualsProcessed(latestTpr, workstationTypeId, materialCat))
					     )
						||(isDataUseful && !isFound) //in case latest is null, has no plant in, tag has undelivered challan ... we record this
						                              //but if this is older than latest then the data on tag will NOT become latest
                        ||(workstationTypeId != Type.WorkStationType.GATE_OUT_TYPE && workstationTypeId != Type.WorkStationType.REGISTRATION && latestTpr != null && latestTpr.getMaterialCat() != materialCat)
					   )
					   ;
				sb.append("toCreateNew:"+toCreateNew).append("\n");
				if (toCreateNew) {
					TPRecord newTpr = createTpr(conn, vehicleId, holder, vehicleName, 0,isWeb, isDataUseful,false,materialCat);
					sb.append("[New TPR Created]").append("\n");
					boolean toMakeLatest = latestTpr == null || !isWeb;
					if (!toMakeLatest && isDataUseful) {
						long newTprTime = holder != null && holder.getDatetime() != null ? holder.getDatetime().getTime() : Misc.getUndefInt();
						long latestTprTime = latestTpr != null ?  (latestTpr.getChallanDate() != null ?  latestTpr.getChallanDate().getTime() : (latestTpr.getEarliestUnloadGateInEntry() != null ? latestTpr.getEarliestUnloadGateInEntry().getTime() : Misc.getUndefInt())) : Misc.getUndefInt();
						if(!Misc.isUndef(newTprTime) && !Misc.isUndef(latestTprTime) && (newTprTime - latestTprTime) >= (newLatestTprWebThreshold) )
							toMakeLatest = true;
					}
					if (toMakeLatest) {
						newTpr.setLatest(1);
						latestTpr = newTpr;
					}
					sb.append(newTpr.toString());
				}else{//to avoid empty tpr filling when creating new one 
					if(fillEmptyTPR && latestTpr != null){
						sb.append("@@@[Fill Empty TPR]@@@").append("\n");
						updateTpr(conn, vehicleId, holder, vehicleName, latestTpr, latestTpr.isLatest(),isWeb, isDataUseful,false,materialCat);
					}
				}
			}//if proper vehicleId
		} catch (Exception ex) {
			ex.printStackTrace();
		}finally{
			if(sb != null)
				System.out.println(sb.toString());
		}
		
		return new Triple<TPRecord, Integer, Boolean>(latestTpr, status, isSameStationProcessing);
	}*/
	synchronized public static TPRecord getLatestTPRForView(Connection conn,int vehicleId){
		TPRecord latestTpr = null;
		try{
			TPRecord tpr = new TPRecord();
			tpr.setVehicleId(vehicleId);
			tpr.setTprStatus(TPR.OPEN);
			tpr.setStatus(Status.ACTIVE);
			Criteria cr = new Criteria(TPRecord.class);
			cr.setOrderByClause("coalesce(tp_record.combo_end,tp_record.combo_start,tp_record.tpr_create_date,tp_record.challan_date)");
			cr.setDesc(true);
			ArrayList<Object> list = (ArrayList<Object>) RFIDMasterDao.select(conn, tpr,cr);
			for(int i=0,is=list == null ? 0 : list.size();i<is;i++){
				TPRecord tprEntry = (TPRecord) list.get(i);
				if(tprEntry.isLatest() == 1){
					latestTpr = tprEntry;
					break;
				}
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return latestTpr;
	}
	
	synchronized public static TPRecord getTPRForHHWeb(Connection conn, int vehicleId, RFIDHolder holder,String vehicleName) throws Exception{
		StringBuilder sb = new StringBuilder();
		TPRecord tpr = null;
		TPRecord latestTpr = null;
		ArrayList<Object> list = null;
		boolean isFound = false;
		boolean isDataUseful = false;
		TPRecord tprAgainstHHLog = null;
		int holdlerDataValidity = Misc.getUndefInt();
		sb.append("Get_Mergable_Tpr_against_HH["+vehicleId+","+vehicleName+"]").append("\n");
		try {
			if (!Misc.isUndef(vehicleId)) {
				if(holder != null){
					holdlerDataValidity = holder.isDataUseful(conn, true);
					isDataUseful = holdlerDataValidity == RFIDHolder.RF_DATA_USEABLE;
					sb.append("HH_log["+holder.getGeneratedId()+"]:"+holder.getRecordKey()+","+holder.getChallanId()+","+isDataUseful).append("\n");
				}
				tpr = new TPRecord();
				tpr.setVehicleId(vehicleId);
				tpr.setTprStatus(TPR.OPEN);
				tpr.setStatus(Status.ACTIVE);
				Criteria cr = new Criteria(TPRecord.class);
				cr.setOrderByClause("coalesce(tp_record.combo_end,tp_record.combo_start,tp_record.tpr_create_date,tp_record.challan_date)");
				cr.setDesc(true);
				list = (ArrayList<Object>) RFIDMasterDao.select(conn, tpr,cr);
				for(int i=0,is=list == null ? 0 : list.size();i<is;i++){
					TPRecord tprEntry = (TPRecord) list.get(i);
					if(tprEntry.isLatest() == 1){
						latestTpr = tprEntry;
						break;
					}
				}
				sb.append("latest:"+(latestTpr == null ? null : "("+latestTpr.getTprId()+","+latestTpr.getMinesId()+","+latestTpr.getChallanNo()+")")+"\n");
				Pair<Integer, TPRecord> mergePair = mergeWithExisting(conn, latestTpr, vehicleId, vehicleName, holder, isDataUseful, true, Misc.getUndefInt(), sb);
				isFound = mergePair.second != null ;//mergePair.first == Status.TPR_MERGE_STATUS.MERGED;
				if(isFound){
					tprAgainstHHLog = mergePair.second;
				}
				boolean toCreateNew = isDataUseful && !isFound;
				sb.append("toCreateNew:"+toCreateNew).append("\n");
				if (toCreateNew) {
					TPRecord newTpr = createTpr(conn, vehicleId, holder, vehicleName, 0, true, (isDataUseful && !isFound),false,Type.TPRMATERIAL.COAL);
					sb.append("New_TPR_Created").append("\n");
					long lastLatestTime = Misc.getUndefInt();
					if(latestTpr == null){
						lastLatestTime = getLastLatestTime(conn, vehicleId);
						sb.append("Get_lastLatestTime:"+lastLatestTime).append("\n");
					}
					boolean toMakeLatest = latestTpr == null && Misc.isUndef(lastLatestTime);
					//sb.append("Make_Latest"+toMakeLatest).append("\n");
					if (!toMakeLatest && isDataUseful) {
						long newTprTime = holder != null && holder.getDatetime() != null ? holder.getDatetime().getTime() : Misc.getUndefInt();
						long latestTprTime = latestTpr == null ? lastLatestTime : latestTpr.getLastProcessedTime(); //(  latestTpr.getComboEnd() != null ? latestTpr.getComboEnd().getTime() : (latestTpr.getChallanDate() != null ?  latestTpr.getChallanDate().getTime()  : Misc.getUndefInt())) ;
						sb.append("HH_Log["+holder.getGeneratedId()+"]:"+newTprTime).append("\n");
						sb.append("Curr_TPR["+(latestTpr != null ? latestTpr.getTprId() : null) +"]:"+latestTprTime).append("\n");
						if(!Misc.isUndef(newTprTime) && !Misc.isUndef(latestTprTime) && (newTprTime - latestTprTime) >= (newLatestTprWebThreshold) )
							toMakeLatest = true;
						
					}
					sb.append("Make_Latest:"+toMakeLatest).append("\n");
					if (toMakeLatest) {
						newTpr.setLatest(1);
						latestTpr = newTpr;
					}
					tprAgainstHHLog = newTpr;
					//sb.append(newTpr.toString());
				}else{// 
					if(latestTpr != null && mergePair.first == Status.TPR_MERGE_STATUS.FILL_BOTH_CURRENT || mergePair.first == Status.TPR_MERGE_STATUS.FILL_RHS_CURRENT){
						updateTpr(conn, vehicleId, holder, vehicleName, latestTpr, latestTpr.isLatest(),true, isDataUseful,mergePair.first == Status.TPR_MERGE_STATUS.FILL_RHS_CURRENT,Type.TPRMATERIAL.COAL);
						tprAgainstHHLog = latestTpr;
						sb.append("Merge_With_latest:"+latestTpr.getTprId()).append("\n");
					}
				}
				
				if(tprAgainstHHLog == null ){//SHOULD NOT GET HERE
					if(holder != null){
						sb.append("[BUG]exec_unreachable_code:"+holder.getVehicleId()+","+holder.getChallanId());
						holder.printData();
						tprAgainstHHLog = createTpr(conn, vehicleId, holder, vehicleName, 0, true, isDataUseful,false,Type.TPRMATERIAL.COAL);
						if(tprAgainstHHLog != null)
							tprAgainstHHLog.setStatus(100);
					}
				}
				if(tprAgainstHHLog != null){
					tprAgainstHHLog.setDebugStr(sb.toString());
				}
			}
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}finally{
			if(sb != null)
				System.out.println(sb.toString());
		}
		
		return tprAgainstHHLog;
	}
	synchronized public static Triple<TPRecord, Integer, Boolean> getLatestNonWeb(Connection conn, int vehicleId, RFIDHolder holder,String vehicleName, int workstationTypeId, int materialCat) throws Exception{
		StringBuilder sb = new StringBuilder();
		TPRecord tpr = null;
		TPRecord latestTpr = null;
		int status = Status.VALIDATION.NO_ISSUE;
		ArrayList<Object> list = null;
		boolean isFound = false;
		boolean isDataUseful = false;
		boolean isSameStationProcessing = false;
		int holdlerDataValidity = Misc.getUndefInt();
		sb.append("Get_Latest["+vehicleId+","+vehicleName+","+Type.WorkStationType.getString(workstationTypeId)+","+materialCat+"]").append("\n");
		int isMultiple = Misc.getUndefInt();
		int openTPRCout = 0;
		try {
			if (!Misc.isUndef(vehicleId)) {
				if(holder != null){
					holdlerDataValidity = holder.isDataUseful(conn, false);
					isDataUseful = holdlerDataValidity == RFIDHolder.RF_DATA_USEABLE;
					sb.append("Card_Data:"+holder.getRecordKey()+","+holder.getChallanId()+","+isDataUseful).append("\n");
				}else{
					sb.append("Card_Data_Is_Null\n");
				}
				tpr = new TPRecord();
				tpr.setVehicleId(vehicleId);
				tpr.setTprStatus(TPR.OPEN);
				tpr.setStatus(Status.ACTIVE);
				Criteria cr = new Criteria(TPRecord.class);
				cr.setOrderByClause("coalesce(tp_record.combo_end,tp_record.combo_start,tp_record.tpr_create_date,tp_record.challan_date)");
				cr.setDesc(true);
				list = (ArrayList<Object>) RFIDMasterDao.select(conn, tpr,cr);
				for(int i=0,is=list == null ? 0 : list.size();i<is;i++){
					TPRecord tprEntry = (TPRecord) list.get(i);
					if(workstationTypeId == Type.WorkStationType.GATE_IN_TYPE || workstationTypeId == Type.WorkStationType.FLY_ASH_IN_TYPE || workstationTypeId == Type.WorkStationType.REGISTRATION)
						openTPRCout++;
					else if(tprEntry.getComboStart() != null)
						openTPRCout++;
					if(tprEntry.isLatest() == 1 && latestTpr == null){
						latestTpr = tprEntry;
						//break;
					}
				}
				
				isSameStationProcessing = isSameWorkStation(latestTpr, workstationTypeId);
				sb.append("isSameStationProcessing:"+isSameStationProcessing+"\n");
				boolean toCreateNew = 
							(
								(
										(latestTpr == null && workstationTypeId != Type.WorkStationType.GATE_OUT_TYPE ) 
										|| 
										(!isSameStationProcessing 
												//					    		  && isGreaterThanEqualsProcessedOld(latestTpr, workstationTypeId);
												&& isGreaterThanEqualsProcessed(latestTpr, workstationTypeId, materialCat))
										)
										||(
												workstationTypeId != Type.WorkStationType.GATE_OUT_TYPE 
												&& 
												workstationTypeId != Type.WorkStationType.REGISTRATION 
												&& 
												latestTpr != null 
												&& 
												(
														!Misc.isUndef(latestTpr.getMaterialCat()) 
														&& !Misc.isUndef(materialCat) 
														&& latestTpr.getMaterialCat() != materialCat ))
								)
								;
				sb.append("toCreateNew:"+toCreateNew).append("\n");
				if (toCreateNew) {
					//isDataUseful passed as false so that holder is not merged right now - instead will be merged with mergedProc depe on TAT
					TPRecord newTpr = createTpr(conn, vehicleId, holder, vehicleName, 0, false, false,false,materialCat);
					openTPRCout++;
					
					//boolean toMakeLatest = latestTpr == null || !isWeb;
					boolean toMakeLatest = true;//latestTpr == null || holder == null;
					
					if (toMakeLatest) {
						newTpr.setLatest(1);
						latestTpr = newTpr;
					}
					sb.append("New_Latest_Tpr_Obj_Created\n");
					//sb.append(newTpr.toString());
				}
				sb.append("latest:"+(latestTpr == null ? null : "("+latestTpr.getTprId()+","+latestTpr.getMinesId()+","+latestTpr.getChallanNo()+","+(latestTpr != null ? latestTpr.getComboStart() : Misc.getUndefInt())+")")+"\n");
				Pair<Integer, TPRecord> mergePair = mergeWithExisting(conn, latestTpr, vehicleId, vehicleName, holder, isDataUseful, false, materialCat, sb);
				isFound = mergePair.second != null ;//mergePair.first == Status.TPR_MERGE_STATUS.MERGED;
				if(isFound &&  mergePair.second.getTprId() != latestTpr.getTprId()){
					sb.append("Card_Merge_With_Non_Latest_DB_Commit").append("\n");
					insertUpdateTpr(conn, mergePair.second);
					conn.commit();
				}else if(mergePair.first == Status.TPR_MERGE_STATUS.CREATE_NEW){
					TPRecord newTpr = createTpr(conn, vehicleId, holder, vehicleName, 0, false, true,false,materialCat);
					boolean toMakeLatest = false;
					if (!toMakeLatest && isDataUseful) {
						long newTprTime = holder != null && holder.getDatetime() != null ? holder.getDatetime().getTime() : Misc.getUndefInt();
						long latestTprTime = latestTpr != null && latestTpr.getChallanDate() != null  ? latestTpr.getChallanDate().getTime() : Misc.getUndefInt(); //(  latestTpr.getComboEnd() != null ? latestTpr.getComboEnd().getTime() : (latestTpr.getChallanDate() != null ?  latestTpr.getChallanDate().getTime()  : Misc.getUndefInt())) ;
						sb.append("Card["+holder.getGeneratedId()+"]:"+newTprTime).append("\n");
						sb.append("Latest_TPR["+(latestTpr != null ? latestTpr.getTprId() : null) +"]:"+latestTprTime).append("\n");
						if(!Misc.isUndef(newTprTime) &&(Misc.isUndef(latestTprTime) || (newTprTime - latestTprTime) >= 1000 ))
							toMakeLatest = true;
					}
					sb.append("Make_Latest:"+toMakeLatest).append("\n");
					if (toMakeLatest) {
						newTpr.setLatest(1);
						latestTpr = newTpr;
					}else{
						insertUpdateTpr(conn, newTpr);
						conn.commit();
					}
					sb.append("Create_New_TPR_Against_Card_Data:"+newTpr.getTprId()+"\n");
				}
				if(mergePair.first == Status.TPR_MERGE_STATUS.FILL_BOTH_CURRENT || mergePair.first == Status.TPR_MERGE_STATUS.FILL_RHS_CURRENT){
					updateTpr(conn, vehicleId, holder, vehicleName, latestTpr, latestTpr.isLatest(),false, isDataUseful,mergePair.first == Status.TPR_MERGE_STATUS.FILL_RHS_CURRENT,materialCat);
					sb.append("Card_Merge_With_Latest").append("\n");
				}
			}
			
			if(latestTpr != null){
				isMultiple = openTPRCout  > 1 ? 1 : Misc.getUndefInt();
				sb.append("Multi_Open:"+(openTPRCout  > 1 )).append("\n");
				latestTpr.setIsMultipleOpenTPR(isMultiple);
				latestTpr.changeWtInTons();
				latestTpr.setDebugStr(sb.toString());
			}
			
		} catch (Exception ex) {
			ex.printStackTrace();
			throw ex;
		}finally{
			if(sb != null)
				System.out.println(sb.toString());
		}
		
		return new Triple<TPRecord, Integer, Boolean>(latestTpr, status, isSameStationProcessing);
	}
	private static Pair<Integer,TPRecord> mergeWithExisting(Connection conn,TPRecord latestTPR,int vehicleId, String vehicleName,RFIDHolder holder,boolean isDataUseful,boolean isWeb, int materialCat,StringBuilder sb){
		if(holder == null || Misc.isUndef(holder.getVehicleId()) || !isDataUseful)
			return new Pair<Integer, TPRecord>(Misc.getUndefInt(), null);
		try{

			//check in rhs
			TPRecord rightMatchedTPRId = holder.getMatchingRightTprId(conn);
			if(rightMatchedTPRId != null && !Misc.isUndef(rightMatchedTPRId.getTprId())){
				sb.append("Merge_RHS_key["+rightMatchedTPRId.getTprId()+","+rightMatchedTPRId.getVehicleId()+"]\n");
				return new Pair<Integer, TPRecord>(Status.TPR_MERGE_STATUS.MERGED, rightMatchedTPRId);
			}else{
				sb.append("Not_Merge_RHS\n");
			}

			//check in lhs
			TPRecord leftMatchedTPRId = holder.getMatchingLeftTprId(conn);
			if(leftMatchedTPRId != null && !Misc.isUndef(leftMatchedTPRId.getTprId()) ){
				sb.append("Merge_LHS_CM["+leftMatchedTPRId.getTprId()+","+leftMatchedTPRId.getVehicleId()+"]\n");
				//if rhs empty
				if(leftMatchedTPRId.isRightEmpty()){
					sb.append("RHS_EMPTY["+leftMatchedTPRId.getTprId()+"]\n");
					updateTpr(conn, vehicleId, holder, vehicleName, leftMatchedTPRId, Misc.getUndefInt(),isWeb, isDataUseful,true,materialCat);
					return new Pair<Integer, TPRecord>(Status.TPR_MERGE_STATUS.MERGED, leftMatchedTPRId);
				}else{//holder == lhs but holder != rhs
					sb.append("RHS_NON_EMPTY_Unmergable["+leftMatchedTPRId.getTprId()+"]\n");
					TPRecord errorTPR = createTpr(conn, vehicleId, holder, vehicleName, Misc.getUndefInt(), isWeb, isDataUseful, false, materialCat);
					errorTPR.setStatus(100);//UNMEREGABLE
					return new Pair<Integer, TPRecord>(Status.TPR_MERGE_STATUS.UNMEREGABLE, errorTPR);
				}
			}else{
				sb.append("Not_Merge_LHS\n");
			}
			if(latestTPR != null && latestTPR.isEmpty() 
					&& (latestTPR.getMaterialCat() == holder.getMaterial()) 
					&& (
						(
							latestTPR.getComboStart() != null 
							&& holder.isAllowedToFillTprRHS(conn, holder.getMinesId(), latestTPR.getComboStart().getTime(),sb)
						)
						|| !isWeb) //in case of newly created tpr having card data.
					){
				sb.append("Merge_Latest_FULL_EMPTY["+latestTPR.getTprId()+"]\n");
				return new Pair<Integer, TPRecord>(Status.TPR_MERGE_STATUS.FILL_BOTH_CURRENT, latestTPR);//in case of non web, we want the update to latestTPR to happen upon save
			}else{
				sb.append("Not_FULL_Fill_Latest\n");
			}
			//check if it is merged with latestTPR that is left fill, rt empty and holder from card
			if(!isWeb && latestTPR != null 
					&& (latestTPR.getMaterialCat() == holder.getMaterial())
					&& latestTPR.isRightEmpty()  
					&& !latestTPR.isLeftEmpty() 
					&& latestTPR.getComboStart() != null
					&& holder.isAllowedToFillTprRHS(conn, latestTPR.getMinesId(), latestTPR.getComboStart().getTime(),sb)){
				sb.append("Merge_Latest_RHS_EMPTY_NON_WEB["+latestTPR.getTprId()+"]\n");
				return new Pair<Integer, TPRecord>(Status.TPR_MERGE_STATUS.FILL_RHS_CURRENT, latestTPR);////in case of non web, we want the update to latestTPR to happen upon save
			}else{
				sb.append("Not_RHS_Fill_Latest\n");
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
		sb.append("Not_Mergable_Create_New\n");
		return new Pair<Integer, TPRecord>(Status.TPR_MERGE_STATUS.CREATE_NEW, null);
	}
	public static void insertUpdateTpr(Connection conn,TPRecord tpr) throws Exception{
		try{
			if(tpr == null)
				return;
			//change wt. in tons
			tpr.changeWtInTons();
			if(tpr.getTprId() <= 0){
				if(tpr.isLatest() == 1){
					clearLatestTprForVehicle(conn, tpr.getVehicleId());
				}
				RFIDMasterDao.insert(conn, tpr,false);
				RFIDMasterDao.insert(conn, tpr,true);
				System.out.println("[NEW TPR INSERTED]:"+tpr != null ? tpr.getTprId() : Misc.getUndefInt());
			}
			else{
				RFIDMasterDao.update(conn, tpr,false);
				RFIDMasterDao.update(conn, tpr,true);
				System.out.println("[TPR Updated]:"+tpr != null ? tpr.getTprId() : Misc.getUndefInt());
			}
			if(tpr.getDebugStr() != null){
				saveTraceToDB(conn, tpr.getTprId(),tpr.getVehicleId() , tpr.getDebugStr());
			}
			
		}catch(Exception ex){
			ex.printStackTrace();
			throw ex;
		}
	}
	private static void clearLatestTprForVehicle(Connection conn,int vehicleId) throws Exception{
		PreparedStatement ps = null;
		try{
			ps = conn.prepareStatement("update tp_record set is_latest=0 where tpr_status = 0 and vehicle_id=?");
			Misc.setParamInt(ps, vehicleId, 1);
			System.out.println("[Query]:"+ps.toString());
			ps.executeUpdate();
			ps.clearParameters();
			ps = conn.prepareStatement("update tp_record_apprvd set is_latest=0 where tpr_status = 0 and vehicle_id=?");
			Misc.setParamInt(ps, vehicleId, 1);
			ps.executeUpdate();
		}catch(Exception ex){
			ex.printStackTrace();
			throw ex;
		}finally{
			try{
				if(ps != null){
					ps.close();
				}
			}catch(Exception ex){
				ex.printStackTrace();
			}
		}
	}
	synchronized public static TPStep getTpStep(Connection conn, TPRecord tpr, int workStationType, int workStationId, int updatedBy) {
		TPStep retval = null;
//		try {
			retval = new TPStep();
			retval.setTprId(tpr.getTprId());
			retval.setVehicleId(tpr.getVehicleId());
			retval.setEntryTime(new Date());
			retval.setWorkStationType(workStationType);
			retval.setWorkStationId(workStationId);
			retval.setUpdatedBy(updatedBy);
		/*}catch(Exception ex){
			ex.printStackTrace();
		}*/
		return retval;
	}
	public static void main(String[] arg) throws GenericException {
		boolean destroyIt = false;
		Connection conn = null;
		Pair<Integer, String> vehPair = null;
		int vehicleId = 23661;
		try {
      
			conn = DBConnectionPool.getConnectionFromPoolNonWeb();
		    
			if(false){
				RFIDHolder cardData = new RFIDHolder();
				RFIDHolder manualData = new RFIDHolder();
				manualData.setRefTPRId(79085);
				manualData.setMinesId(2);
				manualData.setChallanId("20178321");
				manualData.setVehicleId(23517);
				int id = manualData.getConflictingTPRId(conn);


				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				Date d = sdf.parse("2016-03-12 13:14:11");
				System.out.println(d.getTime());

				if(true)
					return;
			}
			RFIDHolder hh1 = new RFIDHolder(vehicleId,"JH10AM4297", 1, "2016-03-18 08:30", "ch2201", "lr2201", 1, 1,true);
			RFIDHolder hh2 = new RFIDHolder(vehicleId,"JH10AM4297", 1, "2016-03-18 15:30", "ch2202", "lr2202", 1, 1,true);
			RFIDHolder hh3 = new RFIDHolder(vehicleId,"JH10AM4297", 1, "2016-03-18 18:30", "ch2203", "lr2203", 1, 1,true);
			RFIDHolder hh4 = new RFIDHolder(vehicleId,"JH10AM4297", 1, "2016-03-18 20:30", "ch2204", "lr2204", 1, 1,true);
			RFIDHolder hh5 = new RFIDHolder(vehicleId,"JH10AM4297", 1, "2016-03-18 22:30", "ch2205", "lr2205", 1, 1,true);
			
			ArrayList<RFIDHolder> webData = new ArrayList<RFIDHolder>();
			//webData.add(hh1);
			webData.add(hh2);
			webData.add(hh3);
			webData.add(hh4);
			webData.add(hh5);
			
			//first trip
			Triple<TPRecord, Integer, Boolean> tpr1 = getLatestNonWeb(conn, vehicleId, null, "", Type.WorkStationType.GATE_IN_TYPE, Type.TPRMATERIAL.COAL);
			if(tpr1 !=null ){//gateIn
				TPRecord gIn = tpr1.first;
				gIn.setEarliestUnloadGateInEntry(hh1.getDateFromStr("2016-03-18 10:25"));
				gIn.setLatestUnloadGateInExit(hh1.getDateFromStr("2016-03-18 10:26"));
				gIn.setComboStart(hh1.getDateFromStr("2016-03-18 10:25"));
				//gIn.setTprCreateDate(hh1.getDateFromStr("2016-03-18 10:25"));
				gIn.setTprStatus(0);
				insertUpdateTpr(conn, gIn);
				conn.commit();
			}
			
			TPRecord tpr_hh1 =TPRInformation.getTPRForHHWeb(conn, vehicleId,hh1,hh1.getVehicleName());
			if(tpr_hh1!=null&&hh1!=null)
			{
				TPRInformation.insertUpdateTpr(conn, tpr_hh1);
				conn.commit();
			}
			tpr1 = getLatestNonWeb(conn, vehicleId, hh2, "", Type.WorkStationType.WEIGH_BRIDGE_IN_TYPE, Type.TPRMATERIAL.COAL);
			if(tpr1 !=null ){//wbIn
				TPRecord wIn = tpr1.first;
				//wIn.setChallanNo("ch2201");
				//wIn.setChallanDate(hh1.getDateFromStr("2016-03-18 09:45"));
				//wIn.setLrNo("lr2201");
				wIn.setMinesId(1);
				wIn.setTransporterId(1);
				wIn.setMaterialGradeId(1);
				wIn.setTprStatus(2);
				wIn.setEarliestUnloadWbInEntry(hh1.getDateFromStr("2016-03-18 10:28"));
				wIn.setLatestUnloadWbInExit(hh1.getDateFromStr("2016-03-18 10:30"));
				wIn.setComboEnd(hh1.getDateFromStr("2016-03-18 10:30"));
				insertUpdateTpr(conn, wIn);
				conn.commit();
			}
			//second trip
			tpr1 = getLatestNonWeb(conn, vehicleId, null, "", Type.WorkStationType.GATE_IN_TYPE, Type.TPRMATERIAL.COAL);
			if(tpr1 !=null ){//gateIn
				TPRecord gIn = tpr1.first;
				gIn.setEarliestUnloadGateInEntry(hh1.getDateFromStr("2016-03-18 12:25"));
				gIn.setLatestUnloadGateInExit(hh1.getDateFromStr("2016-03-18 12:26"));
				gIn.setComboStart(hh1.getDateFromStr("2016-03-18 12:25"));
				//gIn.setTprCreateDate(hh1.getDateFromStr("2016-03-18 12:25"));
				gIn.setTprStatus(0);
				insertUpdateTpr(conn, gIn);
				conn.commit();
			}
			tpr1 = getLatestNonWeb(conn, vehicleId, hh2, "", Type.WorkStationType.WEIGH_BRIDGE_IN_TYPE, Type.TPRMATERIAL.COAL);
			if(tpr1 !=null ){//wbIn
				TPRecord wIn = tpr1.first;
			/*	wIn.setChallanNo("ch2202");
				wIn.setChallanDate(hh1.getDateFromStr("2016-03-18 11:45"));
				wIn.setLrNo("lr2202");*/
				wIn.setMinesId(1);
				wIn.setTransporterId(1);
				wIn.setMaterialGradeId(1);
				//wIn.setTprStatus(2);
				wIn.setEarliestUnloadWbInEntry(hh1.getDateFromStr("2016-03-18 12:28"));
				wIn.setLatestUnloadWbInExit(hh1.getDateFromStr("2016-03-18 12:30"));
				wIn.setComboEnd(hh1.getDateFromStr("2016-03-18 12:30"));
				insertUpdateTpr(conn, wIn);
				conn.commit();
			}
			for(RFIDHolder holder : webData){
				TPRecord tpr =TPRInformation.getTPRForHHWeb(conn, vehicleId,holder,holder.getVehicleName());
				if(tpr!=null&&holder!=null)
				{
					TPRInformation.insertUpdateTpr(conn, tpr);
					conn.commit();
				}
			}
			
			if(true)
				return;
			
			tpr1 = getLatestNonWeb(conn, vehicleId, null, "", Type.WorkStationType.WEIGH_BRIDGE_IN_TYPE, Type.TPRMATERIAL.COAL);
			if(tpr1 !=null ){
				TPRecord yIn = tpr1.first;
				yIn.setEarliestUnloadYardInEntry(new Date());
				yIn.setLatestUnloadYardInExit(new Date());
			}
			tpr1 = getLatestNonWeb(conn, vehicleId, null, "", Type.WorkStationType.WEIGH_BRIDGE_IN_TYPE, Type.TPRMATERIAL.COAL);
			if(tpr1 !=null ){
				TPRecord wIn = tpr1.first;
				wIn.setEarliestUnloadWbInEntry(new Date());
				wIn.setLatestUnloadWbInExit(new Date());
			}
			tpr1 = getLatestNonWeb(conn, vehicleId, null, "",Type.WorkStationType.WEIGH_BRIDGE_IN_TYPE, Type.TPRMATERIAL.COAL);
			if(tpr1 !=null ){
				TPRecord wIn = tpr1.first;
				wIn.setEarliestUnloadWbInEntry(new Date());
				wIn.setLatestUnloadWbInExit(new Date());
			}
			tpr1 = getLatestNonWeb(conn, vehicleId, null, "", Type.WorkStationType.WEIGH_BRIDGE_IN_TYPE, Type.TPRMATERIAL.COAL);
			if(tpr1 !=null ){
				TPRecord wIn = tpr1.first;
				wIn.setEarliestUnloadWbInEntry(new Date());
				wIn.setLatestUnloadWbInExit(new Date());
			}
			tpr1 = getLatestNonWeb(conn, vehicleId, null, "", Type.WorkStationType.WEIGH_BRIDGE_IN_TYPE, Type.TPRMATERIAL.COAL);
			if(tpr1 !=null ){
				TPRecord wIn = tpr1.first;
				wIn.setEarliestUnloadWbInEntry(new Date());
				wIn.setLatestUnloadWbInExit(new Date());
			}
			
			
			Pair<Integer, String> bed = TPRUtils.getBedAllignment(conn, 1, 2,3,4 );
			System.out.println(vehicleId);
		} catch (Exception ex) {
			ex.printStackTrace();
			destroyIt = true;
		} finally {
			DBConnectionPool.returnConnectionToPoolNonWeb(conn, destroyIt);
		}
	}
	public static boolean isSameWorkStation(TPRecord tpr, int workStationType){
		if(tpr == null || Misc.isUndef(workStationType) || tpr.getPreStepType() != workStationType)
			return false;
		long currMillis = System.currentTimeMillis();
		long lastProcessedTime = tpr.getWorkStationTime(workStationType);
		return !Misc.isUndef(lastProcessedTime) && ((currMillis - lastProcessedTime) < sameStationTprThreshold);
	}
	
	public static boolean isGreaterThanEqualsProcessed(TPRecord tpr, int workStationType, int materialCat){
		if(tpr == null || Misc.isUndef(workStationType))
			return false;
		if(workStationType == Type.WorkStationType.GATE_OUT_TYPE)
			materialCat = tpr.getMaterialCat();
        if(materialCat < 0 || materialCat > 3)
        	return false;
		Pair<Integer, Integer> startEnd = processList.get(materialCat);
		if(workStationType < startEnd.first || workStationType > startEnd.second)
			return false;
		for(int i=workStationType;i <= startEnd.second;i++){
			if(!Misc.isUndef(tpr.getWorkStationTime(i))){
				return true;
			}
		}
		return false;
	}
	private static boolean isGreaterThanEqualsProcessedOld(TPRecord latestTpr,int workstationTypeId){
		return 
			      //(!isSameStationProcessing && !Misc.isUndef(lastWorkstationProcessedTime))
			      (
			    		  //create 
			    		  //&& 
			     (
			       (
				        //no latest exist - use this for in plant processing 
//			    		latestTpr.getEarliestUnloadGateInEntry()  != null
			    		latestTpr.getLatestUnloadGateInExit()  != null
			            && 
			      		workstationTypeId <= Type.WorkStationType.GATE_IN_TYPE
			       )
			       ||
			       (
//						latestTpr.getEarliestUnloadWbInEntry() != null
						latestTpr.getLatestUnloadWbInExit() != null
						&& 
						workstationTypeId <= Type.WorkStationType.WEIGH_BRIDGE_IN_TYPE
			       )
			       ||
			       (
//						latestTpr.getEarliestUnloadYardInEntry() != null
			    		latestTpr.getLatestUnloadYardInExit() != null
						&& 
						workstationTypeId <= Type.WorkStationType.YARD_IN_TYPE
			        )
			        ||
			        (
//						latestTpr.getEarliestUnloadYardOutEntry() != null
						latestTpr.getLatestUnloadYardOutExit() != null
						&& 
						workstationTypeId <= Type.WorkStationType.YARD_OUT_TYPE
			        )
			        ||
			        (
//						latestTpr.getEarliestUnloadWbOutEntry() != null
						latestTpr.getLatestUnloadWbOutExit() != null
						&& 
						workstationTypeId <= Type.WorkStationType.WEIGH_BRIDGE_OUT_TYPE
			        )
			        ||
			        (
//						latestTpr.getEarliestUnloadGateOutEntry() != null
			        	latestTpr.getLatestUnloadGateOutExit() != null
						&& 
						workstationTypeId < Type.WorkStationType.GATE_OUT_TYPE
			        )
			        ||
			        (
//						latestTpr.getEarliestUnloadGateOutEntry() != null
			        	latestTpr.getLatestUnloadGateOutExit() != null
						&& 
						workstationTypeId < Type.WorkStationType.GATE_OUT_TYPE
			        )
			        ||
			        (
//						latestTpr.getEarliestUnloadGateOutEntry() != null
			        	latestTpr.getLatestLoadGateInExit() != null
						&& 
						workstationTypeId > Type.WorkStationType.GATE_IN_TYPE && workstationTypeId < Type.WorkStationType.FLY_ASH_IN_TYPE
			        )
			        ||
			        (
//						latestTpr.getEarliestUnloadGateOutEntry() != null
			        	latestTpr.getLatestLoadWbInExit() != null
						&& 
						workstationTypeId > Type.WorkStationType.GATE_IN_TYPE && workstationTypeId < Type.WorkStationType.FLY_ASH_TARE_WT_TYPE
			        )
			        ||
			        (
//						latestTpr.getEarliestUnloadGateOutEntry() != null
			        	latestTpr.getLatestLoadWbOutExit() != null
						&& 
						workstationTypeId > Type.WorkStationType.GATE_IN_TYPE && workstationTypeId < Type.WorkStationType.FLY_ASH_GROSS_WT_TYPE
			        )
			     )
			     );
	}
	public static boolean closeTPR(Connection conn,int tprId) throws Exception{
		if(conn == null || Misc.isUndef(tprId))
			return false;
		try{
			RFIDMasterDao.executeQuery(conn, "update tp_record set tpr_status=2 where tpr_id="+tprId);
		}catch(Exception ex){
			ex.printStackTrace();
			throw ex;
		}
		return true;
	}
	private static void saveTraceToDB(Connection conn,int tprId,int vehicleId, String trace){
		PreparedStatement ps = null;
		ResultSet rs = null;
		try{
			ps = conn.prepareStatement("select flag from merge_debug_flag");
			rs = ps.executeQuery();
			boolean insert = false;
			if(rs.next()){
				insert = Misc.getRsetInt(rs, 1) == 1;
			}
			if(insert){
				ps.clearParameters();
				ps = null;
				ps = conn.prepareStatement("insert into merge_process_log(tpr_id,vehicle_id,trace,updated_on) values (?,?,?,now())");
				Misc.setParamInt(ps, tprId, 1);
				Misc.setParamInt(ps, tprId, 2);
				ps.setString(3, trace);
				ps.execute();
			}
			Misc.closePS(ps);
			Misc.closeRS(rs);
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	private static long getLastLatestTime(Connection conn,int vehicleId){
		PreparedStatement ps = null;
		ResultSet rs = null;
		long retval = Misc.getUndefInt();
		try{
			ps = conn.prepareStatement("select coalesce(tp_record.combo_end,tp_record.combo_start,tp_record.tpr_create_date,tp_record.challan_date) last_time from tp_record where vehicle_id=? and is_latest=1 and status=1 order by coalesce(tp_record.combo_end,tp_record.combo_start,tp_record.tpr_create_date,tp_record.challan_date) desc  limit 1");
			Misc.setParamInt(ps, vehicleId, 1);
			rs = ps.executeQuery();
			if(rs.next()){
				retval = Misc.getDateInLong(rs, 1);
			}
			Misc.closePS(ps);
			Misc.closeRS(rs);
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return retval;
	}
}
