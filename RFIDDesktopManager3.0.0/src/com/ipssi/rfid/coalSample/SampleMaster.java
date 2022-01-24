/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ipssi.rfid.coalSample;

import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.ipssi.fingerprint.utils.SampleUtils;
import com.ipssi.gen.utils.Cache;
import com.ipssi.gen.utils.DimInfo;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.SessionManager;
import com.ipssi.gen.utils.Triple;
import com.ipssi.rfid.processor.Utils;


/**
 *
 * @author IPSSI
 */
public class SampleMaster {
	public static SessionManager m_session = null;


	public  static HashMap<Integer, String> seclMinesDetailsMap = new HashMap<Integer, String>();



	public SampleMaster(SessionManager m_session) {
		this.m_session = m_session;
	}


	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

//	public static Triple<Integer, Integer, StringBuilder>  processContentCSV(Connection conn, CsvReader data, int portNodeId, SampleDefinitionBean challanDef, HashMap<Integer, SampleParamBean> sampleParamList, ArrayList<DimInfo.ValInfo> valList) throws Exception {
//		PreparedStatement psInsert = null;
//		PreparedStatement psUpdate = null;
//		PreparedStatement psSelect = null;
//		PreparedStatement psUpdPrior = null;
//		String query = null;
//		int rowCount = 0;
//		StringBuilder error = new StringBuilder();
//		int processCount = 0;
//		int errorCount = 0;
//		try {
//			if(data != null && challanDef.getCsvDelemeter() != null && challanDef.getCsvDelemeter().length() > 0)
//				data.setDelimiter(challanDef.getCsvDelemeter().charAt(0));
//			if(valList != null )//&& challanParamList != null)
//				query = SampleInformation.getDynamicInsertQuery(valList);	
//			System.out.println("[Sample Master]-INSERT-"+query);
//			psInsert = conn.prepareStatement(query);
//			String updateQuery = SampleInformation.getDynamicUpdateQuery(valList, sampleParamList);
//			System.out.println("[Sample Master]-UPDATE-"+updateQuery);
//			psUpdate = conn.prepareStatement(updateQuery);
//			psSelect = conn.prepareStatement("select id from challan_details where vehicle_id=? and challan_date=?");
//			psUpdPrior = conn.prepareStatement("update challan_details set trip_status=2 where vehicle_id=? and challan_date < ? and (trip_status is null or trip_status=1)");
//			ArrayList<String> row = new ArrayList<String>();
//			HashMap<Integer, Integer> vehicleUpdated = new HashMap<Integer, Integer>();
//			while (readRowFromCSV(data, row)) {
//				try {
//					if (isEmptyRow(row))
//						continue;
//					rowCount++;
//					if (rowCount <= challanDef.getStartRow())
//						continue;
//					if (data.getColumnCount() < 2)
//						continue;
//				}
//				catch (Exception e) {
//					e.printStackTrace();
//					//eat it
//				}
//			}
//			if (processCount >0) {
//				psInsert.executeBatch();
//				psUpdate.executeBatch();
//				psUpdPrior.executeBatch();
//			}
//
//			/*if (!conn.getAutoCommit())*/
//			conn.commit();
//			sendUpdMsg(vehicleUpdated);
//		} 
//		catch (SQLException e) {
//			e.printStackTrace();
//			throw e;
//		} 
//		finally {
//			try {
//				if (psInsert != null) {
//					psInsert.close();
//				}
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//			try {
//				if (psUpdate != null) {
//					psUpdate.close();
//				}
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//			try {
//				if (psSelect != null) {
//					psSelect.close();
//				}
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//			try {
//				if (psUpdPrior != null)
//					psUpdPrior.close();
//			}
//			catch (Exception e) {
//				e.printStackTrace();
//			}
//		}
//		error.append("<br/>").append(processCount).append(" Rows successfully added<br/>").append(errorCount).append(" Rows had error");
//		return new Triple<Integer, Integer, StringBuilder>(processCount, errorCount, error) ;
//	}

	public static Triple<Integer, Integer, StringBuilder> processContentXLS2003(Connection conn, FileInputStream data, int portNodeId, SampleDefinitionBean doDef, HashMap<Integer, SampleParamBean> doParamList, ArrayList<DimInfo.ValInfo> valList,int userId,boolean isUpdate,int lotSampleId,int labId) throws Exception {
		PreparedStatement psInsert = null;
		String query = null;
		StringBuilder error = new StringBuilder();
		int rowCount = 0;
		int processCount = 0;
		int errorCount = 0;
		int updateCount = 0;
		int insertCount = 0;

		try {
			if(valList != null && doParamList != null)
				query = SampleInformation.getDynamicInsertQuery(valList);	

			System.out.println("[Query: "+query+"]");
			psInsert = conn.prepareStatement(query);

			HSSFWorkbook wb = new HSSFWorkbook(data);
			HSSFSheet sheet = wb.getSheetAt(0);
			int excelRows = sheet.getPhysicalNumberOfRows();
			ArrayList<String> row = new ArrayList<String>();
			int rowStart =  doDef.getStartRow() - 3; //0;
			for(int i = rowStart ; i <= excelRows; i++) {
				try {

					HSSFRow excelRow = sheet.getRow(i);
					if (excelRow == null)
						continue;
					row.clear();
					boolean seenText = false;
					for (int j = 0, js = doDef.getTotalColumn(); j < js; j++) {
						HSSFCell cell = excelRow.getCell(j);
						if(cell != null)
							cell.setCellType(Cell.CELL_TYPE_STRING);
						String cellStr = cell == null ? null : cell.getStringCellValue();

						if (cellStr != null)
							cellStr = cellStr.replace("\r<br/>", "").trim();
						
						
						if (seenText || cell != null) {
							/*if(cell.getCellType() == cell.CELL_TYPE_NUMERIC)
								cellStr = cell.getNumericCellValue() +"";*/
							row.add(cellStr);
							seenText = true;
						}
					}
					if (isEmptyRow(row))
						continue;
					rowCount++;
					if (rowCount <= doDef.getStartRow())
						continue;
					rowCount++;
					if (rowCount <= doDef.getStartRow())
						continue;

					processCount = processSingleRow(conn, row, i+1, doDef, doParamList, valList, portNodeId, psInsert, error,userId,false);
					if(processCount  == 1)
						insertCount++;
					else if(processCount  == 2)
						updateCount++;

				}
				catch (Exception e) {
					e.printStackTrace();
					errorCount++;
					//error.append("<br/> At Row").append(row);
					//eat it
				}
			}//each row
			System.out.println("[Query: "+ psInsert.toString()+"]");

			if (!conn.getAutoCommit())
				conn.commit();
		} 
		catch (SQLException e) {
			e.printStackTrace();
			throw e;
		} 
		finally {
			try {
				if (psInsert != null) {
					psInsert.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		error.append("<br>").append(insertCount).append(" Rows successfully added, ").append(updateCount).append(" Rows successfully updated, ").append(errorCount).append(" Rows had error");

		return new Triple<Integer, Integer, StringBuilder>(processCount, errorCount, error) ;

	}

	public static Triple<Integer, Integer, StringBuilder> processContentXLS2007(Connection conn, FileInputStream data, int portNodeId, SampleDefinitionBean doDef, HashMap<Integer,SampleParamBean> sampleParamList, ArrayList<DimInfo.ValInfo> valList, int userId,boolean doUpdateDo,int mplLotSampleIds,int labId) throws Exception {
		PreparedStatement psInsert = null;
		String query = null;
		StringBuilder error = new StringBuilder();
		int rowCount = 0;
		int processCount = 0;
		int errorCount = 0;
		int existCount = 0;
		int insertCount = 0;
		try {
			if(valList == null || sampleParamList == null)
				return null;
			query =  SampleInformation.getDynamicInsertQuery(valList,"sample_upload_details",portNodeId,mplLotSampleIds,labId);
			psInsert = conn.prepareStatement(query);
			System.out.println("[1st Query: "+query+"]");
			//psInsert = conn.prepareStatement(query);
			XSSFWorkbook wb = new XSSFWorkbook(data);
			XSSFSheet sheet = wb.getSheetAt(0);
			int excelRows = sheet.getPhysicalNumberOfRows();
			ArrayList<String> row = new ArrayList<String>();
			int rowStart =  0;//doDef.getStartRow() - 3; //0;
			rowCount  = doDef.getStartRow();
			rowStart = doDef.getStartRow();
			for(int i = rowStart ; i <= excelRows; i++) {
				System.out.println("Row Pos: "+i);
				try {
					XSSFRow excelRow = sheet.getRow(i);
					if (excelRow == null)
						continue;
					row.clear();
					boolean seenText = false;
					for (int j = 0, js = doDef.getTotalColumn(); j <= js; j++) {
						XSSFCell cell = excelRow.getCell(j);
						if(cell != null)
							cell.setCellType(Cell.CELL_TYPE_STRING);
						String cellStr = cell == null ? null : cell.getStringCellValue();
						if (cellStr != null)
							cellStr = cellStr.replace("\r<br/>", "").trim();
						if ((seenText || cell != null) && !Utils.isNull(cellStr)) {
							row.add(cellStr);
							seenText = true;
						}
					}
					rowCount++;
					if (isEmptyRow(row))
						continue;
					if (rowCount <= doDef.getStartRow())
						continue;
					
				//	query =  SampleInformation.getDynamicInsertQueryNew(valList,paramList,"sample_upload_details");
					
					processCount = processSingleRow(conn, row, i+1, doDef, sampleParamList, valList, portNodeId, psInsert, error,userId, doUpdateDo);
					if(processCount  == 1)
						insertCount++;
//					else if(processCount  == 2)
//						updateCount++;
					else if(processCount  == 3)
						existCount++;
					else
						errorCount++;
					
					break;
				}
				catch (Exception e) {
					e.printStackTrace();
					errorCount++;
					//eat it
				}
			}

			if (!conn.getAutoCommit())
				conn.commit();
		} catch (SQLException e) {
			e.printStackTrace();
			throw e;
		} finally {
			try {
				if (psInsert != null) {
					psInsert.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			
		}
		error.append("<br>").append(insertCount).append(" Rows successfully added, ").append(errorCount).append(" Rows had error");

		return new Triple<Integer, Integer, StringBuilder>(processCount, errorCount, error) ;
	}

	private static int processSingleRow(Connection conn, ArrayList<String> data, int rowCount, SampleDefinitionBean doDef, HashMap<Integer, SampleParamBean> doParamList, ArrayList<DimInfo.ValInfo> valList, int portNodeId,PreparedStatement psInsert,StringBuilder error,int userId,boolean doUpdate) {
		int count = 0;
		int rowInsert = 1;
		int pos = 1;
		int lessRow = doDef.getStartRow();
		try {
			if (rowCount < doDef.getStartRow())
				return Misc.getUndefInt();
			if (data.size() < 2)
				return Misc.getUndefInt();

			for(DimInfo.ValInfo val : valList){
				String fieldVal = null;
				Object retval = null;
				int contentType = Cache.STRING_TYPE;
				String secValStr = null;
				int secColDatatype = Cache.STRING_TYPE;
				if(!val.getOtherProperty("base_table").equalsIgnoreCase("sample_upload_details") || val.m_name == "") 
					continue;
				contentType = Misc.getParamAsInt(val.getOtherProperty("col_data_type"));

				// secValStr = data.get(val.m_id -1 );
				if(val.m_id <= doDef.getTotalColumn())
					fieldVal = data.get(val.m_id -1 );
			
				retval = addParameter(pos, fieldVal, contentType, psInsert,secValStr,secColDatatype);
				pos++;
				}
			}catch (Exception e) {
				// TODO: handle exception
			}
			

			System.out.println(psInsert.toString());
		
			try {
				psInsert.executeUpdate();
				error.append("</br>Inserted Row ").append(rowCount-lessRow).append("</br>");
				ResultSet rs = psInsert.getGeneratedKeys();
				if (rs.next()){
					count = rowInsert;
				}
				Misc.closeRS(rs);

				if (!conn.getAutoCommit())
					conn.commit();

			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}finally{
			}	
			return  count;
	}



	private static Object addParameter(int pos,String val,int contentType,PreparedStatement ps,String secValStr,int secColDatatype){
		try{
			Object retval = null;

			if(contentType == Cache.INTEGER_TYPE) {
				retval = new Integer((int) Math.round(Misc.getParamAsDouble(val, 0.0)));
				//retval = mergeColValue(retval, secValStr, secColDatatype);
				Misc.setParamInt(ps, ((Integer)retval).intValue(), pos);
			}
			if(contentType == Cache.NUMBER_TYPE) {
				retval = new Double(Misc.getParamAsDouble(val));
				//retval = mergeColValue(retval, secValStr, secColDatatype);
				Misc.setParamDouble(ps, ((Double)retval).doubleValue(), pos);
			}
			if(contentType == Cache.DATE_TYPE || contentType == 7) {//if date type
				retval = SampleUtils.getDateFromStrDo(val);

				//retval = mergeColValue(retval, secValStr, secColDatatype);
				ps.setTimestamp(pos, Misc.utilToSqlDate((java.util.Date) retval));
			}
			if(contentType == Cache.STRING_TYPE) {
				retval = val;
				//retval = mergeColValue(retval, secValStr, secColDatatype);
				ps.setString(pos, val);
			}
			return retval;
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}
	private static Object mergeColValue(Object colValue,String secValStr,int secColDatatype){
		if(colValue == null || secValStr == null || secValStr.length() <= 0)
			return colValue;
		if(secColDatatype == 8){// 8-time
			return new java.util.Date(((java.util.Date) colValue).getTime() + SampleUtils.getTimeFromStr(secValStr, 0));
		}
		else if(secColDatatype == Cache.STRING_TYPE){
			return colValue + "," + secValStr;
		}
		else if(secColDatatype == Cache.INTEGER_TYPE ){
			return ((Double)colValue).intValue() + Misc.getParamAsInt(secValStr);
		}
		else if(secColDatatype == Cache.NUMBER_TYPE ){
			return ((Double)colValue).doubleValue() + Misc.getParamAsDouble(secValStr);
		}
		return colValue;
	}
	private static void sendUpdMsg(HashMap<Integer, Integer> vehicleUpdated) {
		Set<Integer> keys = vehicleUpdated.keySet();
		ArrayList<Integer> list = new ArrayList<Integer>();
		for (Integer key:keys) {
			list.add(key);
		}
		//if (list.size() > 0)
		//		TripProcessorGateway.updateChallan(list, Misc.getServerName(), false);
	}
	private static boolean isEmptyRow(ArrayList<String> row) {
		for (int i=0,is=row.size();i<is;i++) {
			if (row.get(i) != null && row.get(i).length() > 0)
				return false;
		}
		return true;
	}
//	private static boolean readRowFromCSV(CsvReader data, ArrayList<String> row) throws Exception {
//		boolean hasNext = data.readRecord();
//		row.clear();
//		if (hasNext) {
//			boolean seenText = false;
//			for (int i=0,is=data.getColumnCount(); i<is; i++) {
//				String str = data.get(i);
//				if (str != null)
//					str = str.trim();
//				if (str.length() == 0)
//					str = null;
//				if (seenText || str != null) {
//					row.add(data.get(i));
//					seenText = true;
//				}
//			}
//		}
//		return hasNext;
//	}



}
