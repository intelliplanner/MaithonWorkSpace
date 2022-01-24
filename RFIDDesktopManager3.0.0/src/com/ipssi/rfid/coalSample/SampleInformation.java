/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ipssi.rfid.coalSample;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import com.ipssi.gen.utils.Cache;
import com.ipssi.gen.utils.DimInfo;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.Pair;
import com.ipssi.gen.utils.DimInfo.ValInfo;

/**
 *
 * @author IPSSI
 */
public class SampleInformation {
	private static HashMap<Integer, SampleDefinitionBean> sampleDefinition;
	public synchronized static SampleDefinitionBean getSampleDefinitionByPortNodeId(Connection conn,int portNodeId, boolean toreload) {
		if (toreload)
			sampleDefinition = null;
		SampleDefinitionBean retval = null; 
		if(sampleDefinition == null)
			loadSampleDefinition(conn);
		try{
		if(sampleDefinition != null){
			Cache cache = Cache.getCacheInstance(conn);
			if(!Misc.isUndef(portNodeId)){
				retval = sampleDefinition.get(portNodeId);
			}
		}
		}catch(Exception e){
			e.printStackTrace();
		}
		return retval;
	}
	public static void loadSampleDefinition(Connection conn){
		SampleDefinitionDao sampleDefinitionDao = new SampleDefinitionDao();
		try{
			sampleDefinition = sampleDefinitionDao.getSampleDefinition(conn);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
public static String getDynamicUpdateQuery(ArrayList<DimInfo.ValInfo> valList, HashMap<Integer, SampleParamBean> sampleParamList){
		StringBuilder q = new StringBuilder();
		q.append("update mines_do_details set ");
		boolean paramAdded = false;
		if(valList != null){
			for(DimInfo.ValInfo val : valList){
				SampleParamBean bean = sampleParamList.get(val.m_id);
				if (bean == null)
					continue;
				
				if (paramAdded)
					q.append(",");
				q.append(val.m_name).append("=?");
				paramAdded = true;
			}
		}
		q.append(", updated_on=now() ");
		q.append(" where id = ? ");
		return q.toString();
	}


public static String getDynamicInsertQuery(ArrayList<DimInfo.ValInfo> valList){
	String retval = null;
	String paramList = "updated_on",insertParam = "now()";
	
	if(valList != null){
		for(DimInfo.ValInfo val : valList){
			paramList += "," + val.m_name;
			insertParam += ",?";
			if(val.m_id == 15)
				break;
		}
		retval = " insert into sample_upload_details(";
		retval += paramList + ") values (" + insertParam + ")";
	}
	return retval;
}

public static String getDynamicInsertQuery(ArrayList<DimInfo.ValInfo> valList,String tableName,int port_node_id,int mplLotSampleId, int labId){
	String retval = null;
	String paramList = "port_node_id,status,updated_on,mpl_lot_sample_id,lab_details_id",insertParam = "463,1,now(),"+mplLotSampleId+","+labId;
	
	if(valList != null){
		for(DimInfo.ValInfo val : valList){
		if(val.getOtherProperty("base_table").equalsIgnoreCase("sample_upload_details") && val.m_name != ""){
			paramList += "," + val.m_name;
			insertParam += ",?";
			}
		}
		retval = " insert into "+ tableName +"(";
		retval += paramList + ") values (" + insertParam + ")";
	}
	return retval;
}
public static String getDynamicInsertQueryNew(ArrayList<ValInfo> valList,
		String colList, String tableName) {
	String retval = null;
	String paramList = "updated_on",insertParam = "now()";
	String arrColList[] = colList.split(",");
	if(valList != null){
		for(DimInfo.ValInfo val : valList){
			for (int i = 0; i < arrColList.length; i++) {
				if(val.m_name != "" && val.m_name.equalsIgnoreCase(arrColList[i]) ){
					paramList += "," + val.m_name;
					insertParam += ",?";
					}
				}	
			}
		
		retval = " insert into "+ tableName +"(";
		retval += paramList + ") values (" + insertParam + ")";
	}
	
	return retval;
}

public static 	Pair<String,Integer>  getDynamicColumnList(ArrayList<DimInfo.ValInfo>  valList,String tableName ,String exlColumnName, int columnNo, int isAdpColumnDone){
	Pair<String, Integer> pairVal = null;
	boolean isMatched = false;
	if(valList != null){
		for(DimInfo.ValInfo val : valList){
			String colStr = exlColumnName;
			colStr = colStr == null ? "" : colStr.replaceAll("\\s","");
			String xmlExlKey = val.getOtherProperty("excel_key");
			xmlExlKey = xmlExlKey == null ? "" :  xmlExlKey.replaceAll("\\s","") ;
			boolean isTrue  = (xmlExlKey == "" && colStr == "") ? false : colStr.equalsIgnoreCase(xmlExlKey);
			
//			System.out.println("ExlColumnName: "+ exlColumnName +" ,XmlExlKey: "+xmlExlKey  );
			if(val.getOtherProperty("base_table").equalsIgnoreCase("sample_upload_details") && val.m_name != "" && isTrue){
				if((val.getOtherProperty("excel_key").equalsIgnoreCase("IM %") || val.getOtherProperty("excel_key").equalsIgnoreCase("Ash %") || val.getOtherProperty("excel_key").equalsIgnoreCase("VM %") || val.getOtherProperty("excel_key").equalsIgnoreCase("FC %") || val.getOtherProperty("excel_key").equalsIgnoreCase("GCV (Kcal/kg)"))  && isAdpColumnDone < 5){
					isAdpColumnDone++;
					isMatched = true;
					pairVal = new Pair<String,Integer>(val.m_name,isAdpColumnDone);
					break;
				}else if(isAdpColumnDone == 5 && (val.m_name.equalsIgnoreCase("im_adb") || val.m_name.equalsIgnoreCase("ash_adb") || val.m_name.equalsIgnoreCase("fc_adb") || val.m_name.equalsIgnoreCase("vm_adb") || val.m_name.equalsIgnoreCase("gcv_adb"))){
					continue;
				}else{
					pairVal = new Pair<String,Integer>(val.m_name,isAdpColumnDone);
					isMatched = true;
					break;
				}
			}
		}
		
		System.out.println("ExcelColumnName: "+ exlColumnName +" ,isMatched: "+isMatched  );
	}
	return pairVal ;	
}

public static String getDynamicInsertQueryForCustomerDetails(ArrayList<DimInfo.ValInfo> valList,String tableName,int port_node_id){
	String retval = null;
	String paramList = "updated_on",insertParam = "now()";
	
	if(valList != null){
		for(DimInfo.ValInfo val : valList){
			if(val.getOtherProperty("base_table").equalsIgnoreCase("customer_details") && val.m_name != ""){		
				paramList += "," + val.m_name;
				insertParam += ",?";
			}
		}
		retval = " insert into "+ tableName +"(";
		//retval += paramList + ", sap_code,sn,status,port_node_id,created_on) values (" + insertParam + ",?,?,1,"+port_node_id+",now())";
		retval += paramList + ",status,port_node_id,created_on) values (" + insertParam + ",1,"+port_node_id+",now())";
}
	return retval;
}

public static String getDynamicUpdateQueryForCustomer(ArrayList<DimInfo.ValInfo> valList,String tableName,String whereClauseColumn){
	StringBuilder q = new StringBuilder();
	q.append("update customer_details set ");
	boolean paramAdded = false;
		for(DimInfo.ValInfo val : valList){
			if(val.getOtherProperty("base_table").equalsIgnoreCase("customer_details") && val.m_name != ""){
				if (paramAdded)
					q.append(",");
			
				q.append(val.m_name).append("=?");
				paramAdded = true;
			}
		}
	q.append(" where ");
	q.append(whereClauseColumn).append("=?");
	return q.toString();
}

public static String getDynamicUpdateQuery(ArrayList<DimInfo.ValInfo> valList,String tableName,String whereClauseColumn){
	StringBuilder q = new StringBuilder();
	q.append("update mines_do_details set ");
	boolean paramAdded = false;
		for(DimInfo.ValInfo val : valList){
			if(val.getOtherProperty("base_table").equalsIgnoreCase("mines_do_details") && val.m_name != "" ){
				if (paramAdded)
					q.append(",");
			
				q.append(val.m_name).append("=?");
				paramAdded = true;
			}
		}
	q.append(" where ");
	q.append(whereClauseColumn).append("=?");
	return q.toString();
}

public static String getDynamicUpdateQueryReleaseType(ArrayList<DimInfo.ValInfo> valList,String tableName,String whereClauseColumn){
	StringBuilder q = new StringBuilder();
	q.append("update mines_do_details set ");
	boolean paramAdded = false;
		for(DimInfo.ValInfo val : valList){
			if(val.getOtherProperty("base_table").equalsIgnoreCase("mines_do_details") && val.m_name != ""){
				if(val.m_name.equalsIgnoreCase("do_number"))
					continue;
				
				if (paramAdded)
					q.append(",");
			
				q.append(val.m_name).append("=?");
				paramAdded = true;
			}
		}
	q.append(" where ");
	q.append(whereClauseColumn).append("=?");
	return q.toString();
}

public static void loadMinesDoDetails(Connection conn, boolean toreload) {
	if (toreload)
		SampleMaster.seclMinesDetailsMap = null;

	String fetchMinesDo = "select id,name from mines_details";
	ResultSet rs = null;
	PreparedStatement ps = null;
	
	try {	
		ps = conn.prepareStatement(fetchMinesDo);
		rs = ps.executeQuery();
		while(rs.next()){
			if(SampleMaster.seclMinesDetailsMap  == null)
				SampleMaster.seclMinesDetailsMap = new HashMap<Integer, String>();

			SampleMaster.seclMinesDetailsMap.put(rs.getInt("id"), rs.getString("name"));
		}

	} catch (SQLException sqlEx) {
		sqlEx.printStackTrace();
	} catch (Exception ex) {
		ex.printStackTrace();
	}finally{
		try{
			if(ps != null)
				ps.close();
			if(rs != null)
				rs.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}

}

}
