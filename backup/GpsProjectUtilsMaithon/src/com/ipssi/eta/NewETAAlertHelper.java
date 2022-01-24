package com.ipssi.eta;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import java.sql.ResultSet;

import com.ipssi.gen.utils.Cache;
import com.ipssi.gen.utils.CacheTrack;
import com.ipssi.gen.utils.DBConnectionPool;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.Pair;
import com.ipssi.gen.utils.StopDirControl;
import com.ipssi.gen.utils.Value;
import com.ipssi.gen.utils.VehicleExtendedInfo;
import com.ipssi.processor.utils.GpsData;
import com.ipssi.report.cache.CacheValue;
import com.ipssi.userNameUtils.CDHEmailInfo;
import com.ipssi.userNameUtils.IdInfo;
import com.ipssi.communicator.dto.CommunicatorQueueSender;
import com.ipssi.cache.NewVehicleData;
import com.ipssi.cache.VehicleDataInfo;
import com.ipssi.common.ds.trip.ChallanInfo;
import com.ipssi.communicator.dto.CommunicatorDTO;

public class NewETAAlertHelper {
	public static boolean g_alertSimulateMode = false;//DEBUG13 false in prod
	public static String helperGetCleanDate(SimpleDateFormat sdf, long dt) {
		return dt <= 0 ? "N/A" : sdf.format(Misc.longToUtilDate(dt));
	}
	
	public static String helperGetCleanString(String s) {
		if (s != null) {
			s = s.trim();
			if (s.length() == 0)
				s = null;
		}
		return s == null ? "N/A" : s;
	}
	public static boolean handleSendAlertAndEvent(Connection conn, int vehicleId, NewVehicleETA vehicleETA, NewETAforSrcDestItem specificETA, ChallanInfo challanInfo
	,boolean doChallanOnly, int eventTy, int flexParam, long nowTime, GpsData latestPosn
	,StopDirControl stopDirControl, VehicleDataInfo vdf
	,boolean sendAlert, CacheTrack.VehicleSetup vehSetup
	)  {
		//returns true if succ
		boolean retval = true;
		try {
			vehicleETA.setAlertSentThisTime(true);
			StringBuilder traceStr = NewVehicleETA.toTrace(vehicleId) ? new StringBuilder() : null;
			SrcDestInfo srcDestInfo = SrcDestInfo.getSrcDestInfo(conn, specificETA == null ? Misc.getUndefInt() : specificETA.getSrcDestId());
			if (srcDestInfo == null)
				return true;//nothing to send
			//createAlertDB
			if (traceStr != null) {
				traceStr.append("[ETA:").append(vehicleId).append(" Send Alert now DB:")
				.append("Ty:").append(eventTy).append(",Flex:").append(flexParam);
				System.out.println(traceStr);
				traceStr.setLength(0);
			}
			retval = createAlertDB(conn, vehicleId, vehicleETA, specificETA, challanInfo, eventTy, flexParam, nowTime, latestPosn) && retval;
			//get String to send
			if (!conn.getAutoCommit())
				conn.commit();
			if (srcDestInfo == null || !sendAlert)
				return retval;
			String format = srcDestInfo.getAlertFormat(eventTy);
			if (format == null)
				return retval;
			String pattern = null;
			SimpleDateFormat sdf = new SimpleDateFormat(Misc.G_DEFAULT_DATE_FORMAT_HHMM);
			
			
			//get uniquelist of addresses/messages
			double toLon = specificETA == null ? Misc.getUndefDouble() : specificETA.getDestLon();
			double toLat = specificETA == null ? Misc.getUndefDouble() : specificETA.getDestLat();
			 
			ArrayList<Integer> emailList = new ArrayList<Integer>();
			ArrayList<Integer> phoneList = new ArrayList<Integer>();
			String otherEmail = null;
			String otherPhone = null;
			ArrayList<SrcDestInfo.AlertSetting> alertList = srcDestInfo.getAlertSettingCalc(conn, eventTy);
			double flexKM = (double)flexParam/1000.0;
			double flexHr = (double)flexParam/(60.0);
			if (flexKM < 0)
				flexKM = 0;
			if (flexHr < 0)
				flexHr = 0;
			int refcdhId = Misc.getUndefInt();
			String refDestItemCode = null;
			boolean calcCDH = false;
			CDHEmailInfo emailPhoneSrcDestSpecificFromNonChallan = null;
			
			for (int i=0,is=alertList == null ? 0 : alertList.size(); i<is; i++) {
				SrcDestInfo.AlertSetting alertSetting = alertList.get(i);
				boolean ofInterest =  false;
				if (SrcDestInfo.isDistBasedAlert(eventTy))
					ofInterest = Misc.isEqual(flexKM, alertSetting.getDist(),0.1,0.05);
				else if (SrcDestInfo.isTimeBasedAlert(eventTy))
					ofInterest = Misc.isEqual(flexHr, alertSetting.getDist(),0.01,0.01);
				else 
					ofInterest = true;
				boolean checkFromCDH = false;
				if (ofInterest) {
					if (doChallanOnly && (challanInfo == null || alertSetting.getContactId() >0)) {
						ofInterest = false;
					}
					else if (alertSetting.getContactId() <=0) {
						checkFromCDH = true;
					}
				}
				if (alertSetting.getAlertType() != 0 && alertSetting.getAlertType() != 1) {
					ofInterest = false;
				}
				if (ofInterest && checkFromCDH) {
					if (!calcCDH) {
						if (challanInfo != null) {
							IdInfo destIdInfo = challanInfo.getIdInfo();
							if (destIdInfo != null) {
								refcdhId = destIdInfo.getId();
							}
							if (challanInfo.getTextInfo() != null) {
								refDestItemCode = challanInfo.getTextInfo().getAddressItemCode();
							}
						}
						emailPhoneSrcDestSpecificFromNonChallan = CDHEmailInfo.getCDHInfo(conn, Misc.getUndefInt(), vehicleETA.getCurrToOpStationId(), 481, refDestItemCode, refcdhId, toLon, toLat, Misc.getUndefDouble());
						calcCDH = true;
					}
					if (emailPhoneSrcDestSpecificFromNonChallan == null && challanInfo == null)
						ofInterest = false;
				}
				if (!ofInterest)
					continue;
				if (alertSetting.getAlertType() == 0) { //sms
					if (alertSetting.getContactId() <= 0) {
						if (emailPhoneSrcDestSpecificFromNonChallan != null)
							otherPhone = emailPhoneSrcDestSpecificFromNonChallan.getPhone();
						String temp = challanInfo == null ? null : challanInfo.getAlertPhone(conn, stopDirControl);
						if (otherPhone == null || otherPhone.length() == 0)
							otherPhone = temp;
						else if (temp != null && temp.length() != 0) {
							otherPhone += ";" + temp;
						}
					}
					else 
						phoneList = helperAddToList(phoneList, alertSetting.getContactId());
				}
				if (alertSetting.getAlertType() == 1) { //email
					if (alertSetting.getContactId() <= 0) {
						if (emailPhoneSrcDestSpecificFromNonChallan != null)
							otherEmail = emailPhoneSrcDestSpecificFromNonChallan.getEmail();
						String temp = challanInfo == null ? null : challanInfo.getAlertMailId(conn, stopDirControl);
						if (otherEmail == null || otherEmail.length() == 0)
							otherEmail = temp;
						else if (temp != null && temp.length() != 0) {
							otherEmail += ";" + temp;
						}
					}
					else 
						emailList = helperAddToList(emailList, alertSetting.getContactId());
				}
			}
			Pair<ArrayList<String>, ArrayList<String>> emailPhoneToSend = getEmailPhoneForAlert(conn, emailList, phoneList, otherPhone, otherEmail)	;
			ArrayList<String> emailToSend = emailPhoneToSend.first;
			ArrayList<String> phoneToSend = emailPhoneToSend.second;
			if (emailToSend.size() == 0 && phoneToSend.size() == 0)
				return retval;
			
			//when ... now figure out text and then send
			
			pattern = "%vehicleId";
			if (format.indexOf(pattern) >= 0) {
				String replacement = vehSetup.m_name;
				
				replacement = helperGetCleanString(replacement);
				format = format.replaceAll(pattern, replacement);
			}
			pattern = "%sentTime";
			if (format.indexOf(pattern) >= 0) {
				String replacement = helperGetCleanDate(sdf,nowTime);
				format = format.replaceAll(pattern, replacement);
			}
			
			pattern = "%consignor";
			if (format.indexOf(pattern) >= 0) {
				String replacement = vehicleETA.getFrom(conn,(byte)1);
				replacement = helperGetCleanString(replacement);
				format = format.replaceAll(pattern, replacement);
			}
			
			pattern = "%dest_intime"; //other wise dest replaces!!
			if (format.indexOf(pattern) >= 0) {
				String replacement = helperGetCleanDate(sdf,vehicleETA.getCurrToInTime());
				replacement = helperGetCleanString(replacement);
				format = format.replaceAll(pattern, replacement);
			}
			pattern = "%dest";
			if (format.indexOf(pattern) >= 0) {
				String replacement = vehicleETA.getTo(conn, (byte)0);
				replacement = helperGetCleanString(replacement);
				format = format.replaceAll(pattern, replacement);
			}
			pattern = "%from";
			if (format.indexOf(pattern) >= 0) {
				String replacement = vehicleETA.getFrom(conn,(byte)0);
				format = format.replaceAll(pattern, replacement);
			}
			pattern = "%src";
			if (format.indexOf(pattern) >= 0) {
				String replacement = vehicleETA.getFrom(conn,(byte)0);
				replacement = helperGetCleanString(replacement);
				format = format.replaceAll(pattern, replacement);
			}
			pattern = "%to";
			if (format.indexOf(pattern) >= 0) {
				String replacement = vehicleETA.getTo(conn, (byte)0);
				replacement = helperGetCleanString(replacement);
				format = format.replaceAll(pattern, replacement);
			}
			pattern = "%consignee";
			if (format.indexOf(pattern) >= 0) {
				String replacement = vehicleETA.getTo(conn, (byte)1);
				replacement = helperGetCleanString(replacement);
				format = format.replaceAll(pattern, replacement);
			}
			pattern = "%curr_eta";
			if (format.indexOf(pattern) >= 0) {
				String replacement = helperGetCleanDate(sdf,vehicleETA.getCurrETA(specificETA));
				format = format.replaceAll(pattern, replacement);
			}
			pattern = "%est_eta";
			if (format.indexOf(pattern) >= 0) {
				String replacement = helperGetCleanDate(sdf,vehicleETA.getEstETA(specificETA));
				format = format.replaceAll(pattern, replacement);
			}
			pattern = "%intermediate";
			if (format.indexOf(pattern) >= 0) {
				ArrayList<SrcDestInfo.WayPoint> wplist = srcDestInfo.getWaypoints();
				String replacement = wplist == null || flexParam < 0 || flexParam >= wplist.size() ? "N/A" : wplist.get(flexParam).getName();
				replacement = helperGetCleanString(replacement);
				format = format.replaceAll(pattern, replacement);
			}
			
			
			pattern = "%location";
			if (format.indexOf(pattern) >= 0) {
				Value  val = CacheValue.getValueInternal(conn, vehicleId, 20167, vehSetup, vdf);
				String replacement = val == null ? "N/A" : val.toString();
				replacement = helperGetCleanString(replacement);
				format = format.replaceAll(pattern, replacement);
			}
			
			pattern = "%intime";
			if (format.indexOf(pattern) >= 0) {
				String replacement = helperGetCleanDate(sdf,vehicleETA.getCurrFromOpStationInTime());
				format = format.replaceAll(pattern, replacement);
			}
			pattern = "%time";
			if (format.indexOf(pattern) >= 0) {
				Value  val = CacheValue.getValueInternal(conn, vehicleId, 20173, vehSetup, vdf);
				String replacement = val == null ? "N/A" : sdf.format(val.getDateVal());
				format = format.replaceAll(pattern, replacement);
			}
			pattern = "%prop";
			if (format.indexOf(pattern) >= 0) {
				String replacement = "N/A";
				if (flexParam >= 0 && SrcDestInfo.isDistBasedAlert(eventTy))
					replacement = Double.toString(flexKM);
				else if (SrcDestInfo.isTimeBasedAlert(eventTy))
					replacement = Double.toString(flexHr);
				replacement = helperGetCleanString(replacement);
				format = format.replaceAll(pattern, replacement);
			}
			pattern = "%route";
			if (format.indexOf(pattern) >= 0) {
				String replacement = srcDestInfo == null ? null : srcDestInfo.getName();
				replacement = helperGetCleanString(replacement);
				format = format.replaceAll(pattern, replacement);
			}
			if (format.indexOf("%field") >= 0 || format.indexOf("%misc") >= 0) {
				VehicleExtendedInfo vehicleExt = VehicleExtendedInfo.getVehicleExtended(conn, vehSetup.m_vehicleId);
				if (vehicleExt != null) {
					pattern = "%fieldone";
					if (format.indexOf(pattern) >= 0) {
						String replacement = vehicleExt.getFieldone();
						replacement = helperGetCleanString(replacement);
						format = format.replaceAll(pattern, replacement);
					}
					pattern = "%fieldtwo";
					if (format.indexOf(pattern) >= 0) {
						String replacement = vehicleExt.getFieldtwo();
						replacement = helperGetCleanString(replacement);
						format = format.replaceAll(pattern, replacement);
					}
					pattern = "%fieldthree";
					if (format.indexOf(pattern) >= 0) {
						String replacement = vehicleExt.getFieldthree();
						replacement = helperGetCleanString(replacement);
						format = format.replaceAll(pattern, replacement);
					}
					pattern = "%fieldfour";
					if (format.indexOf(pattern) >= 0) {
						String replacement = vehicleExt.getFieldfour();
						replacement = helperGetCleanString(replacement);
						format = format.replaceAll(pattern, replacement);
					}
					pattern = "%fieldfive";
					if (format.indexOf(pattern) >= 0) {
						String replacement = vehicleExt.getFieldfive();
						replacement = helperGetCleanString(replacement);
						format = format.replaceAll(pattern, replacement);
					}
					pattern = "%fieldsix";
					if (format.indexOf(pattern) >= 0) {
						String replacement = vehicleExt.getFieldsix();
						replacement = helperGetCleanString(replacement);
						format = format.replaceAll(pattern, replacement);
					}
					pattern = "%fieldseven";
					if (format.indexOf(pattern) >= 0) {
						String replacement = vehicleExt.getFieldseven();
						replacement = helperGetCleanString(replacement);
						format = format.replaceAll(pattern, replacement);
					}
					pattern = "%fieldeight";
					if (format.indexOf(pattern) >= 0) {
						String replacement = vehicleExt.getFieldeight();
						replacement = helperGetCleanString(replacement);
						format = format.replaceAll(pattern, replacement);
					}
					pattern = "%misc";
					if (format.indexOf(pattern) >= 0) {
						String replacement = vehicleExt.getMiscellaneous();
						replacement = helperGetCleanString(replacement);
						format = format.replaceAll(pattern, replacement);
					}
				}
			}
			pattern = "%misc";
			if (format.indexOf(pattern) >= 0) {
				Cache cache = Cache.getCacheInstance(conn);
				String replacement = cache.getPortName(conn, vehSetup.m_ownerOrgId);
				replacement = helperGetCleanString(replacement);
				format = format.replaceAll(pattern, replacement);
			}
			
			//whew ... now send it
			if (traceStr != null) {
				traceStr.append("[ETA:").append(vehicleId).append(" Sending Alert:")
				.append("Ty:").append(eventTy).append(",Flex:").append(flexParam)
				.append(",Emails:").append(emailToSend).append(",Phones:").append(phoneToSend)
				.append(",Send Info:").append(format)
				;
				System.out.println(traceStr);
				traceStr.setLength(0);
			}
			for (int i=0,is=emailToSend == null ? 0 : emailToSend.size(); i<is;i++) {
				CommunicatorDTO commDTO = new CommunicatorDTO();
				
				commDTO.setNotificationType(2);
				commDTO.setTo(emailToSend.get(i));
				commDTO.setBody(format);
				commDTO.setForceSend(true);
				commDTO.setVehicleId(vehicleId);
				commDTO.setAlertIndex(flexParam);
				commDTO.setRuleId(eventTy);
				commDTO.setSubject("ETA Alerts");
				commDTO.setEngineEventId(Misc.getUndefInt());
				
				try {
					if (!g_alertSimulateMode)
						CommunicatorQueueSender.send(commDTO);
					else
						System.out.println("[ALERT SIM]:To:"+commDTO.getTo()+" Body:"+commDTO.getBody());
				} 
				catch (Exception e) {
					System.out.println(e);
					e.printStackTrace();
				}
			}
			
			for (int i=0,is=phoneToSend == null ? 0 : phoneToSend.size(); i<is;i++) {
				CommunicatorDTO commDTO = new CommunicatorDTO();
				
				commDTO.setNotificationType(1);
				commDTO.setTo(phoneToSend.get(i));
				commDTO.setBody(format.substring(0,160));
				
				commDTO.setForceSend(true);
				commDTO.setVehicleId(vehicleId);
				commDTO.setAlertIndex(flexParam);
				commDTO.setRuleId(eventTy);
				commDTO.setSubject("ETA Alerts");
				commDTO.setEngineEventId(Misc.getUndefInt());
				
				try {
					if (!g_alertSimulateMode)
						CommunicatorQueueSender.send(commDTO);
					else
						System.out.println("[ALERT SIM]:To:"+commDTO.getTo()+" Body:"+commDTO.getBody());
				} 
				catch (Exception e) {
					System.out.println(e);
					e.printStackTrace();
				}
			}
		}
		catch (Exception e) {
			retval = false;
			e.printStackTrace();
			//eat it
		}
		return retval;
	}
	
	public static Pair<ArrayList<String>, ArrayList<String>> getEmailPhoneForAlert(Connection conn, ArrayList<Integer> emailList, ArrayList<Integer> phoneList, String otherPhone, String otherEmail) {
		ArrayList<String> email = new ArrayList<String> ();
		ArrayList<String> phone = new ArrayList<String>();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			if (otherEmail != null)
				otherEmail = otherEmail.trim();
			if (otherPhone != null)
				otherPhone = otherPhone.trim();
			if (otherEmail != null && otherEmail.length() == 0)
				otherEmail = null;
			if (otherPhone != null && otherPhone.length() == 0)
				otherPhone = null;
			if (otherEmail != null) {
				String[] list = otherEmail.split("[;,| ]");
				for (int i=0,is = list==null ? 0 : list.length;i<is;i++) {
					String s = list[i];
					if (s == null)
						continue;
					s = s.trim();
					if (s.length() <= 1)
						continue;
					if (email.indexOf(s) >= 0)
						continue;
					email.add(s);
				}
			}
			if (otherPhone != null) {
				String[] list = otherPhone.split("[;,| ]");
				for (int i=0,is = list==null ? 0 : list.length;i<is;i++) {
					String s = list[i];
					if (s == null)
						continue;
					s = s.trim();
					if (s.startsWith("+91"))
						s = s.substring(3);
					if (s.startsWith("0"))
						s = s.substring(1);
					s = s.trim();
					if (s.length() <= 1)
						continue;
					if (phone.indexOf(s) >= 0)
						continue;
					phone.add(s);
				}
			}
			if (emailList.size() > 0 || phoneList.size() > 0) {
				StringBuilder sb = new StringBuilder();
				sb.append("select id, email, mobile,phone from customer_contacts where id in (");
				if (emailList.size() > 0) {
					Misc.convertInListToStr(emailList, sb);
					if (phoneList.size() > 0)
						sb.append(",");
				}
				if (phoneList.size() > 0) {
					Misc.convertInListToStr(phoneList, sb);
				}
				sb.append(") ");
				ps = conn.prepareStatement(sb.toString());
				rs = ps.executeQuery();
				while (rs.next()) {
					int id = rs.getInt(1);
					String em = rs.getString(2);
					String ph = rs.getString(3);
					String mo = rs.getString(4);
					if (em != null)
						em = em.trim();
					if (ph != null)
						ph = ph.trim();
					if (mo !=null)
						mo = mo.trim();
					if (em != null && em.length() == 0)
						em = null;
					if (ph != null && ph.length() == 0)
						ph = null;
					if (mo != null && mo.length() == 0)
						mo = null;
					if (ph != null && ph.startsWith("+91"))
						ph = ph.substring(3);
					if (mo != null && mo.startsWith("+91"))
						mo = mo.substring(3);
					if (ph != null && ph.startsWith("0"))
						ph = ph.substring(1);
					if (mo != null && mo.startsWith("0"))
						mo = mo.substring(1);
					if (mo != null)
						mo = mo.trim();
					if (ph != null)
						ph = ph.trim();
					if ("".equals(mo))
						mo = null;
					if ("".equals(ph))
						ph = null;
					if (mo == null)
						mo = ph;
					if (phoneList != null && phoneList.indexOf(id) >= 0 && phone.indexOf(mo) < 0) {
						phone.add(mo);
					}
					if (emailList != null && emailList.indexOf(id) >= 0 && email.indexOf(em) < 0) {
						email.add(em);
					}
				}
				rs = Misc.closeRS(rs);
				ps = Misc.closePS(ps);
			}	
		}
		catch (Exception e) {
			e.printStackTrace();
			//eat it
		}
		finally {
			rs = Misc.closeRS(rs);
			ps = Misc.closePS(ps);
		}
		return new Pair<ArrayList<String>, ArrayList<String>>(email, phone);
	}
	
	private static ArrayList<Integer> helperAddToList(ArrayList<Integer> theList, int item) {
		boolean found = false;
		for (int i=0,is = theList == null ? 0 : theList.size(); i<is; i++) {
			if (theList.get(i) == item) {
				found = true;
				break;
			}
		}
		if (!found) {
			if (theList == null)
				theList = new ArrayList<Integer>();
			theList.add(item);
		}
		return theList;
	}
	public static boolean createAlertDB(Connection conn, int vehicleId, NewVehicleETA vehicleETA, NewETAforSrcDestItem specificETA, ChallanInfo challanInfo
			,int eventTy, int flexParam, long nowTime, GpsData latestPosn
			)  {//returns true if succ else false
		boolean retval = true;
		PreparedStatement ps = null;
		try {
			final String insertQ = "insert into eta_alerts_new(vehicle_id, created_on, trip_from_station_id, trip_from_in, trip_from_out "+
			" ,trip_to_station_id, trip_to_station_in, trip_to_station_out, challan_from_loc, challan_to_loc, challan_date, challan_id "+
			", src_dest_id, est_to_location_lon, est_to_location_lat, transit_dist, transit_time "+
			", event_type_id, event_flex_param, curr_eta, est_eta, latest_grt) "+
			" values ("+
			"?,?,?,?,?,"+
			"?,?,?,?,?,?,?,"+
			"?,?,?,?,?,"+
			"?,?,?,?,?"+
			")"
			;
			ps = conn.prepareStatement(insertQ);
			int colIndex = 1;
			//"insert into eta_alerts_new(vehicle_id, created_on, trip_from_station_id, trip_from_in, trip_from_out "+
			Misc.setParamInt(ps, vehicleId, colIndex++);
			ps.setTimestamp(colIndex++, Misc.longToSqlDate(nowTime));
			Misc.setParamInt(ps, vehicleETA.getCurrFromOpStationId(), colIndex++);
			ps.setTimestamp(colIndex++, Misc.longToSqlDate(vehicleETA.getCurrFromOpStationInTime()));
			ps.setTimestamp(colIndex++, Misc.longToSqlDate(vehicleETA.getCurrFroOpStationOutTime()));
			//" ,trip_to_station_id, trip_to_station_in, trip_to_station_out, challan_from_loc, challan_to_loc, challan_date, challan_id "+
			Misc.setParamInt(ps, vehicleETA.getCurrToOpStationId(), colIndex++);
			ps.setTimestamp(colIndex++, Misc.longToSqlDate(vehicleETA.getCurrToInTime()));
			ps.setTimestamp(colIndex++, Misc.longToSqlDate(vehicleETA.getCurrToOutTime()));
			ps.setString(colIndex++, challanInfo == null ? null : challanInfo.getFromLoc());
			ps.setString(colIndex++, challanInfo == null ? null : challanInfo.getToLoc());
			ps.setTimestamp(colIndex++, Misc.longToSqlDate(challanInfo == null ? 0 : challanInfo.getChallanDate()));
			Misc.setParamInt(ps, challanInfo == null ? Misc.getUndefInt() : challanInfo.getId(), colIndex++);
			//", src_dest_id, est_to_location_lon, est_to_location_lat, transit_dist, transit_time, base_transit_dist, base_transit_time "+
			Misc.setParamInt(ps, vehicleETA.getCurrPossibleSrcDestList() == null || vehicleETA.getCurrPossibleSrcDestList().size() == 0 ? Misc.getUndefInt() : vehicleETA.getCurrPossibleSrcDestList().get(0).first, colIndex++);
			Misc.setParamDouble(ps, specificETA == null ? Misc.getUndefDouble() : specificETA.getDestLon(), colIndex++);
			Misc.setParamDouble(ps, specificETA == null ? Misc.getUndefDouble() : specificETA.getDestLat(), colIndex++);
			Misc.setParamDouble(ps, specificETA == null ? Misc.getUndefDouble() : specificETA.getTransitDist(), colIndex++);
			Misc.setParamDouble(ps, specificETA == null ? Misc.getUndefDouble() : specificETA.getTransitTime(), colIndex++);
			//", event_type_id, event_flex_param, curr_eta, est_eta) "+
			Misc.setParamInt(ps, eventTy, colIndex++);
			Misc.setParamInt(ps, flexParam, colIndex++);
			ps.setTimestamp(colIndex++, Misc.longToSqlDate(specificETA == null ? 0 : vehicleETA.getCurrETA(specificETA)));
			ps.setTimestamp(colIndex++, Misc.longToSqlDate(specificETA == null ? 0 : vehicleETA.getEstETA(specificETA)));
			ps.setTimestamp(colIndex++, Misc.longToSqlDate(latestPosn == null ? 0 : latestPosn.getGps_Record_Time()));
			ps.execute();
			ps = Misc.closePS(ps);
		}
		catch (Exception e) {
			retval = false;
			e.printStackTrace();
			//eat it
		}
		finally {
			ps = Misc.closePS(ps);
		}
		return retval;

	}
	
	public static void main(String[] args) throws Exception {
		Connection conn = null;
		try {
			conn = DBConnectionPool.getConnectionFromPoolNonWeb();
			CDHEmailInfo test1 = CDHEmailInfo.getCDHInfo(conn, Misc.getUndefInt(), 3363147, 481, null, Misc.getUndefInt(), Misc.getUndefDouble(), Misc.getUndefDouble(), Misc.getUndefDouble());
			System.out.println(test1);
			test1 = CDHEmailInfo.getCDHInfo(conn, Misc.getUndefInt(), 123, 481, null, Misc.getUndefInt(),  Misc.getUndefDouble(), Misc.getUndefDouble(), Misc.getUndefDouble());
			System.out.println(test1);
			test1 = CDHEmailInfo.getCDHInfo(conn, Misc.getUndefInt(), Misc.getUndefInt(), 481, null, Misc.getUndefInt(),  76.1177632212639, 28.7795553304385, Misc.getUndefDouble());
			System.out.println(test1);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			if (conn != null)
				DBConnectionPool.returnConnectionToPoolNonWeb(conn);
		}
//		Tester.callMain(args);
	}
}
