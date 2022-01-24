package com.ipssi.common.ds.trip;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;

import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.Pair;
import com.ipssi.gen.utils.StopDirControl;
import com.ipssi.mapguideutils.RTreeSearch;
import com.ipssi.userNameUtils.CDHEmailInfo;
import com.ipssi.userNameUtils.IdInfo;
import com.ipssi.userNameUtils.TextInfo;
import com.ipssi.userNameUtils.Utils;

public class ChallanInfo implements Comparable {
	public final static String GET_CHALLAN_DATA_SEL = "select ch.vehicle_id,  ch.port_node_id, ch.challan_date, ch.updated_on, ch.chd_id, ch.consignee, ch.dest_code, ch.dest_addr_1, ch.dest_addr_2, ch.dest_addr_3, ch.dest_addr_4, ch.dest_city, ch.dest_state, ch.material_id, ch.trip_info_id, ch.wt_by_qty, ch.chd_src_id, ch.consignor, ch.src_code, ch.src_addr_1, ch.src_addr_2, ch.src_addr_3, ch.src_addr_4, ch.src_city, ch.src_state, ch.invoice_distkm, ch.trip_status, ch.load_status, ch.from_location, ch.to_location, ch.alert_mail_id, ch.alert_phone, ch.delivery_date, ch.id, ch.gr_no_ "
		+",ch.bill_party, ch.container_1_no, ch.container_2_no,  ch.tripsheet_no_, ch.driver, ch.mrs_contact_details, ch.load_gross, ch.unload_gross, ch.load_tare, ch.unload_tare, ch.load_wb_date, ch.unload_wb_date, ch.from_station_id, ch.to_station_id, ch.orig_eta, ch.curr_eta, ch.at_cust_arrival, ch.challan_load_flipped, ch.auth_time_min ";

	//public final static String GET_TPR_DATA_SEL = //TODO 
	//	"select tp_record.vehicle_id,  tp_record.port_node_id, (case when tp_record.material_cat = 0 then coalesce(tp_record.latest_unload_gate_in_out, tp_record.latest_unload_wb_in_out, tp_record.challan_date)" +
	//	"   else tp_record.combo_start end) ch_date "+//TODO for other types of challans
	//	", tp_record.updated_on, 

	public static class AddnlInfo {
		private String billParty;
		private String container1;
		private String container2;
		private String  tripSheetNumber;
		private String driver;
		private String mrsContact;
		private double loadGross;
		private double unloadGross;
		private double loadTare;
		private double unloadTare;
		private long loadWBDate;
		private long unloadWBDate;
		public String getBillParty() {
			return billParty;
		}
		public void setBillParty(String billParty) {
			this.billParty = billParty;
		}
		public String getContainer1() {
			return container1;
		}
		public void setContainer1(String container1) {
			this.container1 = container1;
		}
		public String getContainer2() {
			return container2;
		}
		public void setContainer2(String container2) {
			this.container2 = container2;
		}
		public String getTripSheetNumber() {
			return tripSheetNumber;
		}
		public void setTripSheetNumber(String tripSheetNumber) {
			this.tripSheetNumber = tripSheetNumber;
		}
		public String getDriver() {
			return driver;
		}
		public void setDriver(String driver) {
			this.driver = driver;
		}
		public String getMrsContact() {
			return mrsContact;
		}
		public void setMrsContact(String mrsContact) {
			this.mrsContact = mrsContact;
		}
		public double getLoadGross() {
			return loadGross;
		}
		public void setLoadGross(double loadGross) {
			this.loadGross = loadGross;
		}
		public double getUnloadGross() {
			return unloadGross;
		}
		public void setUnloadGross(double unloadGross) {
			this.unloadGross = unloadGross;
		}
		public double getLoadTare() {
			return loadTare;
		}
		public void setLoadTare(double loadTare) {
			this.loadTare = loadTare;
		}
		public double getUnloadTare() {
			return unloadTare;
		}
		public void setUnloadTare(double unloadTare) {
			this.unloadTare = unloadTare;
		}
		public long getLoadWBDate() {
			return loadWBDate;
		}
		public void setLoadWBDate(long loadWBDate) {
			this.loadWBDate = loadWBDate;
		}
		public long getUnloadWBDate() {
			return unloadWBDate;
		}
		public void setUnloadWBDate(long unloadWBDate) {
			this.unloadWBDate = unloadWBDate;
		}
	}
	private long challanDate = Misc.getUndefInt();
	private int materialId = Misc.getUndefInt();
	private int tripId = Misc.getUndefInt();
	private int vehicleId = Misc.getUndefInt();
	private int portNodeId = Misc.getUndefInt();
	private int cdhId = Misc.getUndefInt();
	private long challanRecvTime = Misc.getUndefInt();
	private TextInfo textInfo = null;
	private IdInfo idInfo = null;
	private int srcCdhId = Misc.getUndefInt();
	private TextInfo srcTextInfo = null;
	private IdInfo srcIdInfo = null;
	private double invoiceDistKM = Misc.getUndefDouble();
	private double authTimeMin = Misc.getUndefDouble();
	private int tripStatus = 1;
	private int loadStatus = 1;
	private double qty = Misc.getUndefDouble();
	// Trip Alert related mail and phone
	private String alertMailId = null;
	private String alertPhone = null;
	
	private String fromLoc = null;
	private String toLoc = null;
	private long deliveryDate = 0;
	private int id = Misc.getUndefInt();
	public String grNo = null;
	private AddnlInfo addnlInfo = null;
	private int seq = 0;
	private boolean givePrefToDelivery = false;
	private boolean popFromCDHDone = false;
	private int fromStationId = Misc.getUndefInt();
	private int toStationId = Misc.getUndefInt();
	private long origETA = Misc.getUndefInt();
	private long currETA = Misc.getUndefInt();
	private long atCustSite = Misc.getUndefInt();
	private boolean challanLoadFlipped = false;
	private byte source = 0; //
	public static byte G_SRC_CHALLAN = 0;
	public static byte G_SRC_TPR = 1;
	public static byte G_SRC_RFID = 2; //NOT IMPLEMENTED
	public String toString() {
		return "Trip:" + tripId +" Date:"+(new Date(challanDate))+" CdhId:"+cdhId+" IdInfo:"+idInfo+" Dest Addr:"+textInfo+" SrcCdhId:"+srcCdhId+" SrcIdInfo:"+srcIdInfo+" Src Addr:"+srcTextInfo+ " InvoiceKM:"+invoiceDistKM+" TripStatus:"+tripStatus+" LoadStatus:"+loadStatus;
	}
	public static ArrayList<Pair<Integer, ChallanInfo>> read(Connection conn, ArrayList<Integer> challanId) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		ArrayList<Pair<Integer, ChallanInfo>> retval = new ArrayList<Pair<Integer, ChallanInfo>>();
		try {
			if (challanId == null || challanId.size() == 0)
				return null;
			StringBuilder sb = new StringBuilder();
			sb.append(ChallanInfo.GET_CHALLAN_DATA_SEL + " from challan_details ch where ch.id in (");
			Misc.convertInListToStr(challanId, sb);
			sb.append(") order by ch.vehicle_id, ch.challan_date, ch.id");
			ps = conn.prepareStatement(sb.toString());
			
			rs = ps.executeQuery();
			while (rs.next()) {
				int vehicleId = rs.getInt(1);
				ChallanInfo ch = read(conn, rs);
				retval.add(new Pair<Integer, ChallanInfo>(vehicleId, ch));
			}
			rs = Misc.closeRS(rs);
			ps = Misc.closePS(ps);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			rs = Misc.closeRS(rs);
			ps = Misc.closePS(ps);
		}
		return retval;
	}
	public static ChallanInfo read(Connection conn, ResultSet rs) {
//		public final static String GET_CHALLAN_DATA_SEL = "select ch.vehicle_id,  ch.port_node_id, ch.challan_date, ch.challan_rec_date, ch.chd_id, ch.consignee, ch.dest_code, ch.dest_addr_1, ch.dest_addr_2, ch.dest_addr_3, ch.dest_addr_4, ch.dest_city, ch.dest_state, ch.material_id, ch.trip_info_id, ch.wt_by_qty, ch.chd_src_id, ch.consignor, ch.src_code, ch.src_addr_1, ch.src_addr_2, ch.src_addr_3, ch.src_addr_4, ch.src_city, ch.src_state, ch.invoice_distkm, ch.trip_status, ch.load_status, ch.from_location, ch.to_location, ch.alert_mail_id, ch.alert_phone, ch.delivery_date, ch.id, ch.gr_no_ "
//		+",ch.bill_party, ch.container_1_no, ch.container_2_no,  ch.tripsheet_no_, ch.driver, ch.mrs_contact_details, ch.load_gross, ch.unload_gross, ch.load_tare, ch.unload_tare, ch.load_wb_date, ch.unload_wb_date ";
		ChallanInfo retval = null;
		try {
			int vehicleId = Misc.getRsetInt(rs, 1);
			int portNodeId = Misc.getRsetInt(rs, 2);
			long challanDate = Misc.sqlToLong(rs.getTimestamp(3));
			long challanRecvTime = Misc.sqlToLong(rs.getTimestamp(4));
			int cdhId = Misc.getRsetInt(rs,5);
			String custName = Misc.getRsetString(rs, 6,null);
			String destCode = Misc.getRsetString(rs, 7,null);
			String destAddress1 = Misc.getRsetString(rs, 8,null);
			String destAddress2 = Misc.getRsetString(rs, 9,null);
			String destAddress3 = Misc.getRsetString(rs, 10,null);
			String destAddress4 = Misc.getRsetString(rs, 11,null);
			String destCity = Misc.getRsetString(rs, 12,null);
			String destState = Misc.getRsetString(rs, 13,null);
			int materialId = Misc.getRsetInt(rs, 14);
			int tripId = Misc.getRsetInt(rs, 15);
			double wt = Misc.getRsetDouble(rs, 16);
			
			int src_cdhId = Misc.getRsetInt(rs, 17);
			String src_custName = Misc.getRsetString(rs, 18,null);
			String src_destCode = Misc.getRsetString(rs, 19,null);
			String src_destAddress1 = Misc.getRsetString(rs, 20,null);
			String src_destAddress2 = Misc.getRsetString(rs, 21,null);
			String src_destAddress3 = Misc.getRsetString(rs, 22,null);
			String src_destAddress4 = Misc.getRsetString(rs, 23,null);
			String src_destCity = Misc.getRsetString(rs, 24,null);
			String src_destState = Misc.getRsetString(rs, 25,null);
			double invoiceDistKM = Misc.getRsetDouble(rs, 26);
			int tripStatus = Misc.getRsetInt(rs, 27, 1);
			int loadStatus = Misc.getRsetInt(rs, 28, 1);
			String fromLoc = Misc.getRsetString(rs,29,null);
			String toLoc = Misc.getRsetString(rs,30,null);
			String alertMailId = Misc.getRsetString(rs,31,null);
			String alertPhone = Misc.getRsetString(rs,32,null);
			if (destAddress1 == null)
				destAddress1 = toLoc;
			if (destCity == null || destCity.length() == 0)
				destCity = toLoc;
			//if (destCity != null && destAddress1 != null && destAddress1.indexOf(destCity) >= 0)
			//	destCity = null;
			if (toLoc == null) {
				if (destAddress1 != null)
					toLoc = destAddress1;
				if (destCity != null) {
					if (toLoc != null)
						toLoc += ","+destCity;
					else
						toLoc = destCity;
				}
			}
			TextInfo textInfo = new TextInfo();
			
			if (destCode != null)
				destCode = destCode.trim().toUpperCase();
			if (destCity != null)
				destCity = destCity.trim().toUpperCase();
			if (destState != null)
				destState = destState.trim().toUpperCase();
			if (custName != null)
				custName = custName.trim().toUpperCase();
			textInfo.setAddressItemCode(destCode);
			textInfo.setLine(destAddress1, 0);
			textInfo.setLine(destAddress2, 1);
			textInfo.setLine(destAddress2, 2);
			textInfo.setLine(destAddress4, 3);
			textInfo.setCity(destCity);
			textInfo.setState(destState);
			textInfo.setCustName(custName);
	//		Utils.doSpecialProcessing(conn, 481,1,textInfo, destAddress1, destAddress2, destAddress3, destAddress4, destCity);
			
			TextInfo srcTextInfo = new TextInfo();
			if (src_destAddress1 == null)
				src_destAddress1 = fromLoc;
			if (src_destCity == null || src_destCity.length() == 0)
				src_destCity = fromLoc;
			//if (src_destCity != null && src_destAddress1 != null && src_destAddress1.indexOf(src_destCity) >= 0)
				//src_destCity = null;
			if (src_destCode != null)
				destCode = destCode.trim().toUpperCase();
			if (src_destCity != null)
				src_destCity = src_destCity.trim().toUpperCase();
			if (src_destState != null)
				src_destState = src_destState.trim().toUpperCase();
			if (src_custName != null)
				src_custName = src_custName.trim().toUpperCase();
			if (fromLoc == null) {
				if (src_destAddress1 != null)
					fromLoc = src_destAddress1;
				if (src_destCity != null) {
					if (fromLoc != null)
						fromLoc += ","+src_destAddress1;
					else
						fromLoc = src_destAddress1;
				}
			}
			
			srcTextInfo.setAddressItemCode(src_destCode);
			srcTextInfo.setCity(src_destCity);
			srcTextInfo.setState(src_destState);
			srcTextInfo.setCustName(src_custName);
	//		Utils.doSpecialProcessing(conn, 481,1,srcTextInfo, src_destAddress1, src_destAddress2, src_destAddress3, src_destAddress4, src_destCity);
			srcTextInfo.setLine(src_destAddress1, 0);
			srcTextInfo.setLine(src_destAddress2, 1);
			srcTextInfo.setLine(src_destAddress2, 2);
			srcTextInfo.setLine(src_destAddress4, 3);
			retval = new ChallanInfo(challanDate);
			retval.setVehicleId(vehicleId);
			retval.setChallanRecvTime(challanRecvTime);
			retval.setCdhId(cdhId);
			retval.setPortNodeId(portNodeId);
			retval.setTextInfo(textInfo);
			retval.setMaterialId(materialId);
			retval.setTripId(tripId);
			retval.setQty(wt);
			retval.setSrcCdhId(src_cdhId);
			retval.setSrcTextInfo(srcTextInfo);
			retval.setInvoiceDistKM(invoiceDistKM);
			retval.setTripStatus(tripStatus);
			retval.setLoadStatus(loadStatus);
			retval.setAlertMailId(alertMailId);
			retval.setAlertPhone(alertPhone);
			retval.setFromLoc(fromLoc);
			retval.setToLoc(toLoc);
			retval.setDeliveryDate(Misc.getRsetLong(rs, "delivery_date"));
			retval.setId(Misc.getRsetInt(rs, "id"));
			retval.setGrNo(rs.getString("gr_no_"));
			
			retval.setBillParty(rs.getString("bill_party"));
			retval.setContainer1(rs.getString("container_1_no"));
			retval.setContainer2(rs.getString("container_2_no"));
			retval.setTripSheetNumber(rs.getString("tripsheet_no_"));
			retval.setDriver(rs.getString("driver"));
			retval.setMrsContact(rs.getString("mrs_contact_details"));
			retval.setLoadGross(Misc.getRsetDouble(rs,"load_gross"));
			retval.setLoadTare(Misc.getRsetDouble(rs,"load_tare"));
			retval.setUnloadGross(Misc.getRsetDouble(rs,"unload_gross"));
			retval.setUnloadTare(Misc.getRsetDouble(rs,"unload_tare"));
			retval.setLoadWBDate(Misc.sqlToLong(rs.getTimestamp("load_wb_date")));
			retval.setUnloadWBDate(Misc.sqlToLong(rs.getTimestamp("unload_wb_date")));
			retval.setFromStationId(Misc.getRsetInt(rs, "from_station_id"));
			retval.setToStationId(Misc.getRsetInt(rs, "to_station_id"));
			retval.setOrigETA(Misc.sqlToLong(rs.getTimestamp("orig_eta")));
			retval.setCurrETA(Misc.sqlToLong(rs.getTimestamp("curr_eta")));
			retval.setAtCustSite(Misc.sqlToLong(rs.getTimestamp("at_cust_arrival")));
			retval.setChallanLoadFlipped(1 == rs.getInt("challan_load_flipped"));
			retval.setAuthTimeMin(Misc.getRsetDouble(rs, "auth_time_min"));
		}
		catch (Exception e) {
			e.printStackTrace();
			//eat it
		}
		return retval;
	}
	
	public ChallanInfo(Date challanDate) {
		this.challanDate = challanDate == null ? Misc.getUndefInt() : challanDate.getTime();
	}
	public ChallanInfo(long challanDate) {
		this.challanDate = challanDate;
	}
	public ChallanInfo(Date challanDate, int materialId, int tripId, double qty) {
		super();
		this.challanDate = challanDate == null ? Misc.getUndefInt() : challanDate.getTime();
		this.materialId = materialId;
		this.qty = qty;
		this.tripId = tripId;
	}
	public ChallanInfo(long challanDate, int materialId, int tripId, double qty) {
		super();
		this.challanDate = challanDate;
		this.materialId = materialId;
		this.qty = qty;
		this.tripId = tripId;
	}
	private static int signum(long a, long b) {
		long t = a-b;
		return t < 0 ? -1 : t > 0 ? 1 : 0;
	}
	private static int signum(int a, int b) {
		int t = a-b;
		return t < 0 ? -1 : t > 0 ? 1 : 0;
	}
	public int compareTo(Object obj) {		
		ChallanInfo p = (ChallanInfo)obj;
		int retval = 0;
		retval = signum(this.challanDate, p.challanDate);
		/*
		if (givePrefToDelivery) {
			retval = this.deliveryDate > 0 && p.deliveryDate > 0 ? signum(this.deliveryDate, p.deliveryDate) : 0;
			if (retval == 0) {
				retval = this.challanDate > 0 && p.challanDate > 0 ? signum(this.challanDate, p.challanDate) : 0;
			}
		}
		else {
			retval = this.challanDate > 0 && p.challanDate > 0 ? signum(this.challanDate, p.challanDate) : 0;
			if (retval == 0) {
				retval = this.deliveryDate > 0 && p.deliveryDate > 0 ? signum(this.deliveryDate, p.deliveryDate) : 0;	
			}
		}
		if (retval == 0) {
			retval = signum(this.seq, p.seq);
		}
		if (retval == 0)
			retval = this.grNo != null && p.grNo != null ? this.grNo.compareTo(p.grNo) : 0;
			*/
		return retval;
	}

	public long getChallanDate() {
		return challanDate;
	}

	public void setChallanDate(long challanDate) {
		this.challanDate = challanDate;
	}

	public int getMaterialId() {
		return materialId;
	}

	public void setMaterialId(int materialId) {
		this.materialId = materialId;
	}
	public void setTripId(int tripId) {
		this.tripId = tripId;
	}
	public int getTripId() {
		return tripId;
	}
	public void setQty(double qty) {
		this.qty = qty;
	}
	public double getQty() {
		return qty;
	}
	public int getVehicleId() {
		return vehicleId;
	}
	public void setVehicleId(int vehicleId) {
		this.vehicleId = vehicleId;
	}
	public int getPortNodeId() {
		return portNodeId;
	}
	public void setPortNodeId(int portNodeId) {
		this.portNodeId = portNodeId;
	}
	public int getCdhId() {
		return cdhId;
	}
	public void setCdhId(int cdhId) {
		this.cdhId = cdhId;
	}
	public long getChallanRecvTime() {
		return challanRecvTime > 0 ? challanRecvTime : this.challanDate;
	}
	public void setChallanRecvTime(long challanRecvTime) {
		this.challanRecvTime = challanRecvTime;
	}
	public TextInfo getTextInfo() {
		return textInfo;
	}
	public void setTextInfo(TextInfo textInfo) {
		this.textInfo = textInfo;
	}
	public static IdInfo getIdInforForOp(int opstationId) {
		OpStationBean bean = TripInfoCacheHelper.getOpStation(opstationId);
		double lon = bean == null ? Misc.getUndefDouble() : (bean.getLowerX()+bean.getUpperX())/2.0;
		double lat = bean == null ? Misc.getUndefDouble() : (bean.getLowerY()+bean.getUpperY())/2.0;
		if (!Misc.isUndef(lon) && !Misc.isUndef(lat)) {
			IdInfo idInfo = new IdInfo();
			idInfo.setDestIdType((byte)3);
			idInfo.setDestId(opstationId);
			idInfo.setMatchQuality((byte) 4);
			idInfo.setLongitude(lon);
			idInfo.setLatitude(lat);
			return idInfo;
		}
		else {
			return null;
		}
		
	}
	public IdInfo getIdInfoWithCalc(Connection conn, boolean nullTextIfDone, StopDirControl stopDirControl) {
		nullTextIfDone = false; //dont forget the textInfo
		if (idInfo == null && !Misc.isUndef(toStationId)) {
			idInfo = getIdInforForOp(toStationId);
			return idInfo;
		}
		if (idInfo == null && textInfo != null) {
			idInfo = Utils.getIdInfo(textInfo, portNodeId, this.cdhId, true, conn, stopDirControl);
			//reclaim space ...
			if (nullTextIfDone)
				textInfo = null;
		}
		return idInfo == null || idInfo.getLongitude() <= 0 ? null : idInfo;
			
	}
	/*
	public void populateAlertInfo(Connection conn, StopDirControl stopDirControl) {
		
	}
	*/
	
	public void populateAlertInfo(Connection conn,  StopDirControl stopDirControl)  {
		if (!popFromCDHDone) {
			PreparedStatement updChallanBack = null;
			try {
				updChallanBack = conn.prepareStatement("update challan_details set invoice_distkm=?, auth_time_min=? where id=?");
				boolean challanToUpd = false;
				CDHEmailInfo.loadCDH(conn, Misc.getUndefInt(), Misc.getUndefInt(), false);
				IdInfo destIdInfo = this.getIdInfoWithCalc(conn,false, stopDirControl);
				IdInfo srcIdInfo = this.getSrcIdInfoWithCalc(conn, false, stopDirControl);
				int portNodeId = this.getPortNodeId();
				int fromOpId = srcIdInfo != null && srcIdInfo.getDestIdType() == 3 ? srcIdInfo.getDestId() : Misc.getUndefInt();
				if (destIdInfo != null && destIdInfo.getLongitude() > 0) {
					Pair<CDHEmailInfo, Double> emailInfo = RTreeSearch.getNearestCDHLM(conn, destIdInfo.getLongitude(), destIdInfo.getLatitude(), null, destIdInfo.getId(), fromOpId, portNodeId, Misc.getUndefDouble());
					if (emailInfo != null && emailInfo.first != null) {
						String email = emailInfo.first.getEmail();
						String phone = emailInfo.first.getPhone();
						double authDist = emailInfo.first.getAuthDistKM();
						double authTime = emailInfo.first.getAuthTimeKM();
						if (email != null && email.length() != 0) {
							if (this.alertMailId == null || this.alertMailId.length() == 0)
								this.alertMailId = email;
						}
						if (phone != null && phone.length() != 0) {
							if (this.alertPhone == null || this.alertPhone.length() == 0)
								this.alertPhone = phone;
						}
						if (authDist > 0.001 && (stopDirControl.challanIgnDistProvided == 1 || Misc.isUndef(this.invoiceDistKM) || this.invoiceDistKM < 0.001)) {
							this.invoiceDistKM = authDist;
							challanToUpd = true;
						}
						
						if (authTime > 0.001 && (stopDirControl.challanIgnDistProvided == 1 || Misc.isUndef(this.authTimeMin) || this.authTimeMin < 0.001)) {
							this.authTimeMin = authTime;
							challanToUpd = true;
						}
						Misc.setParamDouble(updChallanBack, invoiceDistKM, 1);
						Misc.setParamDouble(updChallanBack, authTimeMin, 2);
						Misc.setParamInt(updChallanBack, this.getId(), 3);
						if (challanToUpd) {
							updChallanBack.executeUpdate();
						}
					}
				}
				popFromCDHDone = true;
			}
			catch (Exception e) {
				e.printStackTrace();
				//eat it
			}
			finally {
				updChallanBack = Misc.closePS(updChallanBack);
			}
		}
	}
	
	public IdInfo getSrcIdInfoWithCalc(Connection conn, boolean nullTextIfDone, StopDirControl stopDirControl)  {
		nullTextIfDone = false;//DEBUG13 ... needed for central caching
		if (srcIdInfo == null && !Misc.isUndef(fromStationId)) {
			srcIdInfo = getIdInforForOp(fromStationId);
			return srcIdInfo;
		}
		if (srcIdInfo == null && srcTextInfo != null) {
			srcIdInfo = Utils.getIdInfo(srcTextInfo, portNodeId, this.srcCdhId, false, conn, stopDirControl);
			//reclaim space ...
			if (nullTextIfDone) {
				
				srcTextInfo = null;
			}
		}
		return srcIdInfo == null || srcIdInfo.getLongitude() <= 0 ? null : srcIdInfo;
	}
	public IdInfo getIdInfo() {
		return idInfo == null || idInfo.getLongitude() <= 0 ? null : idInfo;
	}
	public void setIdInfo(IdInfo idInfo) {
		this.idInfo = idInfo;
	}
	public int getSrcCdhId() {
		return srcCdhId;
	}
	public void setSrcCdhId(int srcCdhId) {
		this.srcCdhId = srcCdhId;
	}
	public TextInfo getSrcTextInfo() {
		return srcTextInfo;
	}
	public void setSrcTextInfo(TextInfo srcTextInfo) {
		this.srcTextInfo = srcTextInfo;
	}
	public IdInfo getSrcIdInfo() {
		return srcIdInfo == null || srcIdInfo.getLongitude() <= 0 ? null : srcIdInfo;
	}
	public void setSrcIdInfo(IdInfo srcIdInfo) {
		this.srcIdInfo = srcIdInfo;
	}
	public double getSimpleInvoiceDistKM() {
		return invoiceDistKM;
	}
	public double getInvoiceDistKM(Connection conn, StopDirControl stopDirControl) {
		this.populateAlertInfo(conn, stopDirControl);
		return invoiceDistKM;
	}
	public void setInvoiceDistKM(double invoiceDistKM) {
		this.invoiceDistKM = invoiceDistKM;
	}
	public int getTripStatus() {
		return tripStatus;
	}
	public void setTripStatus(int tripStatus) {
		this.tripStatus = tripStatus;
	}
	public int getLoadStatus() {
		return loadStatus;
	}
	public void setLoadStatus(int loadStatus) {
		this.loadStatus = loadStatus;
	}
	public String getAlertMailId(Connection conn, StopDirControl stopDirControl) {
		this.populateAlertInfo(conn, stopDirControl);
		if (alertMailId == null || alertMailId.length() == 0) {
			try {
				IdInfo destId = this.getIdInfoWithCalc(conn, false, stopDirControl);
				if (destId != null) {
					String am = destId.getAlertMailId();
					if (am != null)
						alertMailId = am;
				}
				IdInfo srcId = this.getSrcIdInfoWithCalc(conn, false, stopDirControl);
				if (srcId != null) {
					String am = srcId.getAlertMailId();
					if (am != null)
						alertMailId = am;
				}
			}
			catch (Exception e) {
				e.printStackTrace();
				//eat it
			}
		}
		return alertMailId;
	}
	public void setAlertMailId(String alertMailId) {
		this.alertMailId = alertMailId;
	}
	public String getAlertPhone(Connection conn, StopDirControl stopDirControl) {
		this.populateAlertInfo(conn, stopDirControl);
		try {
			if (alertPhone == null || alertPhone.length() == 0) {
				IdInfo destId = this.getIdInfoWithCalc(conn, false, stopDirControl);
				if (destId != null) {
					String am = destId.getAlertPhone();
					if (am != null)
						alertPhone = am;
				}
				IdInfo srcId = this.getSrcIdInfoWithCalc(conn, false, stopDirControl);
				if (srcId != null) {
					String am = srcId.getAlertPhone();
					if (am != null)
						alertPhone = am;
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			//eat it
		}
		return alertPhone;
	}
	public void setAlertPhone(String alertPhone) {
		this.alertPhone = alertPhone;
	}
	public String getFromLoc() {
		return fromLoc;
	}
	public void setFromLoc(String fromLoc) {
		this.fromLoc = fromLoc;
	}
	public String getToLoc() {
		return toLoc;
	}
	public void setToLoc(String toLoc) {
		this.toLoc = toLoc;
	}

	public long getDeliveryDate() {
		return deliveryDate;
	}

	public void setDeliveryDate(long deliveryDate) {
		this.deliveryDate = deliveryDate;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getGrNo() {
		return grNo;
	}

	public void setGrNo(String grNo) {
		this.grNo = grNo;
	}
	
	public String getBillParty() {
		return addnlInfo == null ? null : addnlInfo.billParty;
	}
	public void setBillParty(String billParty) {
		if (billParty != null || addnlInfo != null) {
			if (addnlInfo == null)
				addnlInfo = new AddnlInfo();
			addnlInfo.billParty = billParty;
		}
	}
	public String getContainer1() {
		return addnlInfo == null ? null : addnlInfo.container1;
	}
	public void setContainer1(String container1) {
		if (container1 != null || addnlInfo != null) {
			if (addnlInfo == null)
				addnlInfo = new AddnlInfo();
			addnlInfo.container1 = container1;
		}
	}
	public String getContainer2() {
		return addnlInfo == null ? null : addnlInfo.container2;
	}
	public void setContainer2(String container2) {
		if (container2 != null || addnlInfo != null) {
			if (addnlInfo == null)
				addnlInfo = new AddnlInfo();
			addnlInfo.container2 = container2;
		}
	}
	public String getTripSheetNumber() {
		return addnlInfo == null ? null : addnlInfo.tripSheetNumber;
	}
	public void setTripSheetNumber(String tripSheetNumber) {
		if (tripSheetNumber != null || addnlInfo != null) {
			if (addnlInfo == null)
				addnlInfo = new AddnlInfo();
			addnlInfo.tripSheetNumber = tripSheetNumber;
		}
	}
	public String getDriver() {
		return addnlInfo == null ? null : addnlInfo.driver;
	}
	public void setDriver(String driver) {
		if (driver != null || addnlInfo != null) {
			if (addnlInfo == null)
				addnlInfo = new AddnlInfo();
			addnlInfo.driver = driver;
		}
	}
	public String getMrsContact() {
		return addnlInfo == null ? null : addnlInfo.mrsContact;
	}
	public void setMrsContact(String mrsContact) {
		if (mrsContact != null || addnlInfo != null) {
			if (addnlInfo == null)
				addnlInfo = new AddnlInfo();
			addnlInfo.mrsContact = mrsContact;
		}
	}
	public double getLoadGross() {
		return addnlInfo == null ? Misc.getUndefDouble() : addnlInfo.loadGross;
	}
	public void setLoadGross(double wt) {
		if (!Misc.isUndef(wt) || addnlInfo != null) {
			if (addnlInfo == null)
				addnlInfo = new AddnlInfo();
			addnlInfo.loadGross = wt;
		}
	}
	public double getLoadTare() {
		return addnlInfo == null ? Misc.getUndefDouble() : addnlInfo.loadTare;
	}
	public void setLoadTare(double wt) {
		if (!Misc.isUndef(wt) || addnlInfo != null) {
			if (addnlInfo == null)
				addnlInfo = new AddnlInfo();
			addnlInfo.loadTare = wt;
		}
	}
	public double getUnloadGross() {
		return addnlInfo == null ? Misc.getUndefDouble() : addnlInfo.unloadGross;
	}
	public void setUnloadGross(double wt) {
		if (!Misc.isUndef(wt) || addnlInfo != null) {
			if (addnlInfo == null)
				addnlInfo = new AddnlInfo();
			addnlInfo.unloadGross = wt;
		}
	}
	public double getUnloadTare() {
		return addnlInfo == null ? Misc.getUndefDouble() : addnlInfo.unloadTare;
	}
	public void setUnloadTare(double wt) {
		if (!Misc.isUndef(wt) || addnlInfo != null) {
			if (addnlInfo == null)
				addnlInfo = new AddnlInfo();
			addnlInfo.unloadTare = wt;
		}
	}
	public long getLoadWBDate() {
		return addnlInfo == null ? Misc.getUndefInt() : addnlInfo.loadWBDate;
	}
	public void setLoadWBDate(long dt) {
		if (dt > 0 || addnlInfo != null) {
			if (addnlInfo == null)
				addnlInfo = new AddnlInfo();
			addnlInfo.loadWBDate = dt;
		}
	}
	public long getUnloadWBDate() {
		return addnlInfo == null ? Misc.getUndefInt() : addnlInfo.unloadWBDate;
	}
	public void setUnloadWBDate(long dt) {
		if (dt > 0 || addnlInfo != null) {
			if (addnlInfo == null)
				addnlInfo = new AddnlInfo();
			addnlInfo.unloadWBDate = dt;
		}
	}

	public int getSeq() {
		return seq;
	}

	public void setSeq(int seq) {
		this.seq = seq;
	}

	public boolean isGivePrefToDelivery() {
		return givePrefToDelivery;
	}

	public void setGivePrefToDelivery(boolean givePrefToDelivery) {
		this.givePrefToDelivery = givePrefToDelivery;
	}

	public double getAuthTimeMin(Connection conn, StopDirControl stopDirControl) {
		this.populateAlertInfo(conn, stopDirControl);
		return authTimeMin;
	}

	public void setAuthTimeMin(double authTimeMin) {
		this.authTimeMin = authTimeMin;
	}

	public int getFromStationId() {
		return fromStationId;
	}

	public void setFromStationId(int fromStationId) {
		this.fromStationId = fromStationId;
	}

	public int getToStationId() {
		return toStationId;
	}

	public void setToStationId(int toStationId) {
		this.toStationId = toStationId;
	}

	public long getOrigETA() {
		return origETA;
	}

	public void setOrigETA(long origETA) {
		this.origETA = origETA;
	}

	public long getCurrETA() {
		return currETA;
	}

	public void setCurrETA(long currETA) {
		this.currETA = currETA;
	}

	public long getAtCustSite() {
		return atCustSite;
	}

	public void setAtCustSite(long atCustSite) {
		this.atCustSite = atCustSite;
	}
	public boolean isChallanLoadFlipped() {
		return challanLoadFlipped;
	}
	public void setChallanLoadFlipped(boolean challanLoadFlipped) {
		this.challanLoadFlipped = challanLoadFlipped;
	}
	
}
