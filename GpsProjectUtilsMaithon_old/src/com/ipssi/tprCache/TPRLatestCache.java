package com.ipssi.tprCache;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.concurrent.ConcurrentHashMap;

import com.ipssi.gen.utils.DBConnectionPool;
import com.ipssi.gen.utils.Misc;

public class TPRLatestCache {
	private static ConcurrentHashMap<Integer, TPRLatestCache> g_latestTPRCache = new ConcurrentHashMap<Integer, TPRLatestCache>();
	public static TPRLatestCache getLatest(int vehicleId) {
		Connection conn = null;
		boolean destroyIt = false;
		try {
			if (g_latestTPRCache == null || g_latestTPRCache.size() == 0 ) {
				conn = DBConnectionPool.getConnectionFromPoolNonWeb();
				load(conn);
			}
			return g_latestTPRCache.get(vehicleId);
		}
		catch (Exception e) {
			e.printStackTrace();
			//eat it
		}
		finally {
			if (conn != null) {
				try {
					DBConnectionPool.returnConnectionToPoolNonWeb(conn, destroyIt);
				}
				catch (Exception e) {
					
				}
			}
		}
		return null;
	}
	private int vehicleId;
	private int tprId;
	private int fromMinesId;
	private int toDestId;
	private int materialId;
	private int tprStatus;
	private long lgin;
	private long lgout;
	private long ugin;
	private long ugout;
	private long challanDate;
	private long rfChallanDate;
	private long comboStart;
	private long comboEnd;
	private String challanNo;
	private String rfChallanNo;
	private String lrNo;
	private String rfLrNo;
	private String doNumber;
	private int driverId;
	private String transporter;
	private String rfTransporter;
	private int rfMinesId;
	
	public String getTransporter() {
		return transporter;
	}
	public void setTransporter(String transporter) {
		this.transporter = transporter;
	}
	public String getRfTransporter() {
		return rfTransporter;
	}
	public void setRfTransporter(String rfTransporter) {
		this.rfTransporter = rfTransporter;
	}
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Veh:").append(vehicleId).append(" TPRId:").append(tprId).append(" Mat:").append(materialId).append(" TPR Status:").append(tprStatus)
		.append(" Mines:").append(fromMinesId).append(" Combo:").append(Misc.longToUtilDate(comboStart));
		return sb.toString();
	}
	
	public String getChallanNo() {
		return challanNo;
	}
	public void setChallanNo(String challanNo) {
		this.challanNo = challanNo;
	}
	public String getRfChallanNo() {
		return rfChallanNo;
	}
	public void setRfChallanNo(String rfChallanNo) {
		this.rfChallanNo = rfChallanNo;
	}
	public String getLrNo() {
		return lrNo;
	}
	public void setLrNo(String lrNo) {
		this.lrNo = lrNo;
	}
	public String getRfLrNo() {
		return rfLrNo;
	}
	public void setRfLrNo(String rfLrNo) {
		this.rfLrNo = rfLrNo;
	}
	public int getDriverId() {
		return driverId;
	}
	public void setDriverId(int driverId) {
		this.driverId = driverId;
	}
	public static void load(Connection conn) throws Exception {
		try {
			String q = "select ctp.vehicle_id, tp_record.tpr_id, tp_record.tpr_status, tp_record.mines_id, mines_details.name mines_name, tp_record.combo_start, tp_record.challan_date, tp_record.combo_end, tp_record.material_cat, tp_record.earliest_load_gate_in_in, tp_record.latest_load_gate_out_out, tp_record.earliest_unload_gate_in_in, tp_record.latest_unload_gate_out_out, tp_record.challan_date, tp_record.rf_challan_date, tp_record.plant_id, tp_record.challan_no,tp_record.rf_challan_id,tp_record.lr_no,tp_record.rf_lr_id,tp_record.driver_id,tp_record.rf_mines_id,do_rr_details.do_rr_number,mtrans.name transporter, rftrans.name rf_transporter"+
	 " from current_vehicle_tpr ctp left outer join tp_record on (ctp.tpr_id = tp_record.tpr_id) left outer join mines_details on (mines_details.id = tp_record.mines_id) left outer join do_rr_details on (tp_record.do_id = do_rr_details.id) left outer join transporter_details mtrans on (tp_record.transporter_id = mtrans.id) left outer join transporter_details rftrans on (tp_record.rf_transporter_id = rftrans.id)";
			System.out.println("[TPRBUILDCACHE] "+Thread.currentThread().getId()+" Building TPR Cache");
			PreparedStatement ps = conn.prepareStatement(q);
			ResultSet rs = ps.executeQuery();
			//select ctp.vehicle_id, tp_record.tpr_id, tp_record.tpr_status, tp_record.mines_id, mines_details.name mines_name, tp_record.combo_start, tp_record.challan_date, tp_record.combo_end, tp_record.material_cat, tp_record.earliest_load_gate_in, tp_record.earliest_load_gate_out, tp_record.earliest_unload_gate_in, tp_record.earliest_unload_gate_out, tp_record.challan_date, tp_record.rf_challan_date 
			while (rs.next()) {
				TPRLatestCache item = new TPRLatestCache(Misc.getRsetInt(rs, "vehicle_id"), Misc.getRsetInt(rs, "tpr_id"), Misc.getRsetInt(rs, "mines_id"),
						Misc.getRsetInt(rs, "plant_id"), Misc.getRsetInt(rs, "material_cat"), Misc.getRsetInt(rs, "tpr_status"), Misc.sqlToLong(rs.getTimestamp("challan_date")), Misc.sqlToLong(rs.getTimestamp("rf_challan_date")), Misc.sqlToLong(rs.getTimestamp("earliest_load_gate_in_in")), Misc.sqlToLong(rs.getTimestamp("latest_load_gate_out_out")), Misc.sqlToLong(rs.getTimestamp("earliest_unload_gate_in_in")),
						Misc.sqlToLong(rs.getTimestamp("latest_unload_gate_out_out")), Misc.sqlToLong(rs.getTimestamp("combo_start")),rs.getString("challan_no"),rs.getString("rf_challan_id"),rs.getString("lr_no"),rs.getString("rf_lr_id"),Misc.getRsetInt(rs, "driver_id"),rs.getString("do_rr_number"),rs.getString("transporter"),rs.getString("rf_transporter"),Misc.getRsetInt(rs, "rf_mines_id"), Misc.sqlToLong(rs.getTimestamp("combo_end")));
				g_latestTPRCache.put(item.getVehicleId(), item);
				System.out.println(item.toString());
			}
			System.out.println("[TPRBUILDCACHE] "+Thread.currentThread().getId()+" Done Building TPR Cache");
			rs = Misc.closeRS(rs);
			ps = Misc.closePS(ps);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	public int getVehicleId() {
		return vehicleId;
	}
	public void setVehicleId(int vehicleId) {
		this.vehicleId = vehicleId;
	}
	public int getTprId() {
		return tprId;
	}
	public void setTprId(int tprId) {
		this.tprId = tprId;
	}
	public int getFromMinesId() {
		return fromMinesId;
	}
	public void setFromMinesId(int fromMinesId) {
		this.fromMinesId = fromMinesId;
	}
	public int getToDestId() {
		return toDestId;
	}
	public void setToDestId(int toDestId) {
		this.toDestId = toDestId;
	}
	public long getLgin() {
		return lgin;
	}
	public void setLgin(long lgin) {
		this.lgin = lgin;
	}
	public long getLgout() {
		return lgout;
	}
	public void setLgout(long lgout) {
		this.lgout = lgout;
	}
	public long getUgin() {
		return ugin;
	}
	public void setUgin(long ugin) {
		this.ugin = ugin;
	}
	public long getUgout() {
		return ugout;
	}
	public void setUgout(long ugout) {
		this.ugout = ugout;
	}
	public int getMaterialId() {
		return materialId;
	}
	public void setMaterialId(int materialId) {
		this.materialId = materialId;
	}
	
	public TPRLatestCache(int vehicleId, int tprId, int fromMinesId,
			int toDestId, int materialId, int tprStatus, long challanDate, long rfChallanDate, long lgin, long lgout, long ugin,
			long ugout, long comboStart,String challanNo, String rfChallanNo, String lrNo, String rfLrNo, int driverId, String doNumber, String transporter, String rfTransporter,int rfMinesIdlong ,long comboEnd) {
		super();
		this.tprStatus = tprStatus;
		this.vehicleId = vehicleId;
		this.tprId = tprId;
		this.fromMinesId = fromMinesId;
		this.toDestId = toDestId;
		this.materialId = materialId;
		this.lgin = lgin;
		this.lgout = lgout;
		this.ugin = ugin;
		this.ugout = ugout;
		this.comboStart = comboStart;
		this.challanNo = challanNo;
		this.rfChallanNo = rfChallanNo;
		this.lrNo = lrNo;
		this.rfLrNo = rfLrNo;
		this.driverId = driverId;
		this.doNumber = doNumber;
		this.transporter = transporter;
		this.rfTransporter = rfTransporter;
		this.rfMinesId=rfMinesIdlong;
		this.comboEnd=comboEnd;
	}	
	
	public int getTprStatus() {
		return tprStatus;
	}
	public void setTprStatus(int tprStatus) {
		this.tprStatus = tprStatus;
	}
	public long getChallanDate() {
		return challanDate;
	}
	public void setChallanDate(long challanDate) {
		this.challanDate = challanDate;
	}
	public long getRfChallanDate() {
		return rfChallanDate;
	}
	public void setRfChallanDate(long rfChallanDate) {
		this.rfChallanDate = rfChallanDate;
	}
	public long getComboStart() {
		return comboStart;
	}
	public void setComboStart(long comboStart) {
		this.comboStart = comboStart;
	}
	public String getDoNumber() {
		return doNumber;
	}
	public void setDoNumber(String doNumber) {
		this.doNumber = doNumber;
	}
	public void setRfMinesId(int rfMinesId) {
		this.rfMinesId = rfMinesId;
	}
	public int getRfMinesId() {
		return rfMinesId;
	}
	public void setComboEnd(long comboEnd) {
		this.comboEnd = comboEnd;
	}
	public long getComboEnd() {
		return comboEnd;
	}
	
}
