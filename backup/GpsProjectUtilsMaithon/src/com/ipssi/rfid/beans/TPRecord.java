package com.ipssi.rfid.beans;

import java.util.ArrayList;
import java.util.Date;

import com.ipssi.gen.utils.Misc;
import com.ipssi.rfid.constant.Status;
import com.ipssi.rfid.constant.Type;
import com.ipssi.rfid.db.Table;
import com.ipssi.rfid.db.Table.Column;
import com.ipssi.rfid.db.Table.GENRATED;
import com.ipssi.rfid.db.Table.KEY;
import com.ipssi.rfid.db.Table.PRIMARY_KEY;
import com.ipssi.rfid.db.Table.ReadOnly;

@Table("tp_record")
public class TPRecord {

    @KEY
    @GENRATED
    @PRIMARY_KEY
    @Column("tpr_id")
    private int tprId = Misc.getUndefInt();
   
    @Column("vehicle_id")
    private int vehicleId = Misc.getUndefInt();
    @Column("material_code_id")
    private int materialCodeId = Misc.getUndefInt();
    @Column("mpl_ref_doc")
    private int mplRefDoc = Misc.getUndefInt();
    
    @Column("consignee_ref_doc")
    private String consigneeRefDoc;
    @Column("material_description")
    private String materialDescription ;
    @Column("consignee_address")
    private String consigneeAddress ;
    @Column("consignee_notes")
    private String consigneeNotes ;
    @Column("vehicle_name")
    private String vehicleName;
    @Column("consignor_name")
    private String consignorName;
    @Column("consignor_ref_doc")
    private String consignorRefDoc;
    @Column("consignor_address")
    private String consignorAddress;
    @Column("consignor_notes")
    private String consignorNotes;
    @Column("transporter_id")
    private int transporterId = Misc.getUndefInt();
    @Column("carrying_transporter_id")
    private int carryingTransporterId = Misc.getUndefInt();
    @Column("consignor_id")
    private int consignorId = Misc.getUndefInt();
    @Column("do_id")
    private int doId = Misc.getUndefInt();
    @Column("material_grade_id")
    private int materialGradeId = Misc.getUndefInt();
    @Column("plant_id")
    private int plantId = Misc.getUndefInt();
    @Column("mines_id")
    private int minesId = Misc.getUndefInt();
    @Column("hh_device_id")
    private int hhDeviceId = Misc.getUndefInt();
    @Column("challan_no")
    private String challanNo;
    @Column("challan_date")
    private Date challanDate;
    
    @ReadOnly
    @Column("tpr_create_date")
    private Date tprCreateDate;
    
    @Column("lr_no")
    private String lrNo;
    @Column("dispatch_permit_no")
    private String dispatchPermitNo;
    @Column("load_tare")
    private double loadTare = Misc.getUndefDouble();
    @Column("load_gross")
    private double loadGross = Misc.getUndefDouble();
    @Column("unload_tare")
    private double unloadTare = Misc.getUndefDouble();
    @Column("unload_gross")
    private double unloadGross = Misc.getUndefDouble();
    @Column("earliest_load_gate_in_in")
    private Date earliestLoadGateInEntry;
    @Column("latest_load_gate_in_out")
    private Date latestLoadGateInExit;
    @Column("load_gate_in_name")
    private String loadGateInName;
    @Column("earliest_load_wb_in_in")
    private Date earliestLoadWbInEntry;
    @Column("latest_load_wb_in_out")
    private Date latestLoadWbInExit;
    @Column("load_wb_in_name")
    private String loadWbInName;
    @Column("earliest_load_yard_in_in")
    private Date earliestLoadYardInEntry;
    @Column("latest_load_yard_in_out")
    private Date latestLoadYardInExit;
    @Column("load_yard_in_name")
    private String loadYardInName;
    @Column("earliest_load_yard_out_in")
    private Date earliestLoadYardOutEntry;
    @Column("latest_load_yard_out_out")
    private Date latestLoadYardOutExit;
    @Column("earliest_load_wb_out_in")
    private Date earliestLoadWbOutEntry;
    @Column("latest_load_wb_out_out")
    private Date latestLoadWbOutExit;
    @Column("load_wb_out_name")
    private String loadWbOutName;
    @Column("earliest_load_gate_out_in")
    private Date earliestLoadGateOutEntry;
    @Column("latest_load_gate_out_out")
    private Date latestLoadGateOutExit;
    @Column("load_gate_out_name")
    private String loadGateOutName;
    @Column("earliest_unload_gate_in_in")
    private Date earliestUnloadGateInEntry;
    @Column("latest_unload_gate_in_out")
    private Date latestUnloadGateInExit;
    @Column("unload_gate_in_name")
    private String unloadGateInName;
    @Column("earliest_unload_wb_in_in")
    private Date earliestUnloadWbInEntry;
    @Column("latest_unload_wb_in_out")
    private Date latestUnloadWbInExit;
    @Column("unload_wb_in_name")
    private String unloadWbInName;
    @Column("earliest_unload_yard_in_in")
    private Date earliestUnloadYardInEntry;
    @Column("latest_unload_yard_in_out")
    private Date latestUnloadYardInExit;
    @Column("unload_yard_in_name")
    private String unloadYardInName;
    @Column("earliest_unload_yard_out_in")
    private Date earliestUnloadYardOutEntry;
    @Column("latest_unload_yard_out_out")
    private Date latestUnloadYardOutExit;
    @Column("earliest_unload_wb_out_in")
    private Date earliestUnloadWbOutEntry;
    @Column("latest_unload_wb_out_out")
    private Date latestUnloadWbOutExit;
    @Column("unload_wb_out_name")
    private String unloadWbOutName;
    @Column("earliest_unload_gate_out_in")
    private Date earliestUnloadGateOutEntry;
    @Column("latest_unload_gate_out_out")
    private Date latestUnloadGateOutExit;
    @Column("unload_gate_out_name")
    private String unloadGateOutName;
    @Column("bed_assigned")
    private String bedAssigned;
    @Column("driver_id")
    private int driverId = Misc.getUndefInt();
    @Column("dl_no")
    private String dlNo;
    @Column("driver_name")
    private String driverName;
    @Column("mines_trip_id")
    private int minesTripId = Misc.getUndefInt();
    @Column("is_merged_with_hh_tpr")
    private int isMergedWithHHTpr = Misc.getUndefInt();
    @Column("hh_tpr_merged_time")
    private Date hhTprMergedTime;
    @Column("old_trip_id")
    private int oldTripId = Misc.getUndefInt();
    @Column("tpr_status")
    private int tprStatus = Misc.getUndefInt();
    @Column("status_reason")
    private String statusReason;
    @Column("updated_on")
    private Date updatedOn;
    @Column("user_by")
    private int updatedBy = Misc.getUndefInt();
    @Column("prev_tp_step")
    private int prevTpStep = Misc.getUndefInt();
    @Column("next_tp_step")
    private int nextTpStep = Misc.getUndefInt();
    @Column("confirm_time")
    private Date confirmTime;
    @Column("combo_start")
    private Date comboStart;
    @Column("combo_end")
    private Date comboEnd;
    @Column("next_trip_id")
    private int nextTripId = Misc.getUndefInt();
    @Column("m_trip_id")
    private int m_trip_id = Misc.getUndefInt();
    @Column("lr_date")
    private Date lrDate;
    
    //rfid fields
    @Column("rf_vehicle_name")
    private String rfVehicleName;
    @Column("rf_vehicle_id")
    private int rfVehicleId = Misc.getUndefInt();
    @Column("rf_lr_date")
    private Date rfLRDate;
    @Column("rf_transporter_id")
    private int rfTransporterId = Misc.getUndefInt();
    @Column("rf_mines_id")
    private int rfMinesId = Misc.getUndefInt();
    @Column("rf_grade")
    private int rfGrade = Misc.getUndefInt();
    @Column("rf_challan_date")
    private Date rfChallanDate;
    @Column("rf_challan_id")
    private String rfChallanId;
    @Column("rf_lr_id")
    private String rfLRId;
    @Column("rf_load_tare")
    private double rfLoadTare = Misc.getUndefDouble();
    @Column("rf_load_gross")
    private double rfLoadGross = Misc.getUndefDouble();
    @Column("rf_device_id")
    private int rfDeviceId = Misc.getUndefInt();
    @Column("rf_do_id")
    private int rfDOId = Misc.getUndefInt();
    @Column("rf_record_id")
    private int rfRecordId = Misc.getUndefInt();
    @Column("rf_record_key")
    private String rfRecordKey;
    @Column("consignee_id")
    private int consignee = Misc.getUndefInt(); 
    @Column("consignee_name")
    private String consigneeName;
    
    @Column("mark_for_qc")
    private int markForQC = Misc.getUndefInt();
    @Column("permit_no")
    private String permitNo;
    @Column("pre_step_type")
    private int preStepType = Misc.getUndefInt();
    @Column("pre_step_date")
    private Date preStepDate;
    @Column("blocked_step_type")
    private int blockedStepType = Misc.getUndefInt();
    @Column("blocked_step_date")
    private Date blockedStepDate;
    @Column("next_step_type")
    private int nextStepType = Misc.getUndefInt();
    @Column("blocked_step_id")
    private int blockedStepId = Misc.getUndefInt();
    @Column("is_latest")
    private int isLatest  = Misc.getUndefInt();
    @Column("driver_src")
    private int driverSrc = Misc.getUndefInt();
    @Column("vehicle_src")
    private int vehicleSrc = Misc.getUndefInt();
    /*@Column("create_status")
    private int createStatus = Misc.getUndefInt();*/
    @Column("mark_for_gps")
    private int markForGPS = Misc.getUndefInt();
    
    @Column("mark_for_qc_reason")
    private String markForQCReason;
    
    @Column("supplier_id")
    private int supplierId = Misc.getUndefInt();
    
    @Column("is_new_vehicle")
    private int isNewVehicle = Misc.getUndefInt();
    
    @Column("src_device_log_id")
    private int srcDeviceLogId = Misc.getUndefInt();
    
    @Column("rf_also_on_card")
    private int rfAlsoOnCard = Misc.getUndefInt();
    
    @Column("material_cat")
    private int materialCat = Misc.getUndefInt();
    
    @Column("stone_lift_area_id")
    private int stoneLiftAreaId = Misc.getUndefInt();
    
    @Column("stone_of_transporter_id")
    private int stoneOfTransporterId = Misc.getUndefInt();
    
    @Column("earliest_reg_in")
    private Date earliestRegIn;
    
    @Column("latest_reg_out")
    private Date latestRegOut;
    
    
    @Column("challan_data_edit_at_reg")
    private int challanDataEditAtReg = Misc.getUndefInt();
    
    @Column("challan_data_edit_at_wb")
    private int challanDataEditAtWb = Misc.getUndefInt();
    
    @Column("challan_data_edit_at_preaudit")
    private int challanDataEditAtPreaudit = Misc.getUndefInt();
    
    @Column("challan_data_edit_at_audit")
    private int challanDataEditAtAudit = Misc.getUndefInt();
    
    @Column("material_notes_first")
    private String materialNotesFirst ;
    
    @Column("material_notes_second")
    private String materialNotesSecond ;
    
    @Column("mpl_reference_doc")
    private String mplReferenceDoc  ;
    
    @Column("rf_card_data_merge_time")
    private Date rfCardDataMergeTime;
    
    @Column("other_material_description")
    private String otherMaterialDescription ;
    
    
	@Column("status")
    private int status=Status.ACTIVE;
    
	
	@Column("unload_yard_out_name")
    private String unloadYardOutName;
	
	@Column("load_yard_out_name")
    private String loadYardOutName;
	
	@Column("load_flyash_tare_name")
    private String loadFlyashTareName;
	
	@Column("load_flyash_gross_name")
    private String loadFlyashGrossName;
	
	
    String debugStr;
    
    
    private int isMultipleOpenTPR = 0;
    
    
    private ArrayList<TPRBlockEntry> blockingEntries = null;
    
    private int tprCreateType = Misc.getUndefInt();
    
    public int getConsignee() {
		return consignee;
	}

	public void setConsignee(int consignee) {
		this.consignee = consignee;
	}

	public String getConsigneeName() {
		return consigneeName;
	}

	public void setConsigneeName(String consigneeName) {
		this.consigneeName = consigneeName;
	}

	public int getM_trip_id() {
        return m_trip_id;
    }

    public void setM_trip_id(int m_trip_id) {
        this.m_trip_id = m_trip_id;
    }

    public int getIs_rfid_trip_close() {
        return is_rfid_trip_close;
    }

    public void setIs_rfid_trip_close(int is_rfid_trip_close) {
        this.is_rfid_trip_close = is_rfid_trip_close;
    }
    @Column("is_rfid_trip_close")
    private int is_rfid_trip_close = Misc.getUndefInt();

    @Column("material_sub_cat_id")
    private int material_sub_cat_id = Misc.getUndefInt();
    
    
//	@JOIN(parentCol="tpr_id",childCol="tpr_id", entity="tp_step")
//	private TPStep nextStep = new TPStep();
    public int getTprId() {
        return tprId;
    }

    public int getVehicleId() {
        return vehicleId;
    }

    public String getVehicleName() {
        return vehicleName;
    }

    public int getTransporterId() {
        return transporterId;
    }

    public int getDoId() {
        return doId;
    }

    public int getMaterialGradeId() {
        return materialGradeId;
    }

    public int getPlantId() {
        return plantId;
    }

    public int getMinesId() {
        return minesId;
    }

    public int getHhDeviceId() {
        return hhDeviceId;
    }

    public String getChallanNo() {
        return challanNo;
    }

    public Date getChallanDate() {
        return challanDate;
    }

    public Date getTprCreateDate() {
        return tprCreateDate;
    }

    public String getLrNo() {
        return lrNo;
    }

    public String getDispatchPermitNo() {
        return dispatchPermitNo;
    }

    public double getLoadTare() {
        return loadTare;
    }

    public double getLoadGross() {
        return loadGross;
    }

    public double getUnloadTare() {
        return unloadTare;
    }

    public double getUnloadGross() {
        return unloadGross;
    }

    public Date getEarliestLoadGateInEntry() {
        return earliestLoadGateInEntry;
    }

    public Date getLatestLoadGateInExit() {
        return latestLoadGateInExit;
    }

    public String getLoadGateInName() {
        return loadGateInName;
    }

    public Date getEarliestLoadWbInEntry() {
        return earliestLoadWbInEntry;
    }

    public Date getLatestLoadWbInExit() {
        return latestLoadWbInExit;
    }

    public String getLoadWbInName() {
        return loadWbInName;
    }

    public Date getEarliestLoadYardInEntry() {
        return earliestLoadYardInEntry;
    }

    public Date getLatestLoadYardInExit() {
        return latestLoadYardInExit;
    }

    public String getLoadYardInName() {
        return loadYardInName;
    }

    public Date getEarliestLoadYardOutEntry() {
        return earliestLoadYardOutEntry;
    }

    public Date getLatestLoadYardOutExit() {
        return latestLoadYardOutExit;
    }

    public Date getEarliestLoadWbOutEntry() {
        return earliestLoadWbOutEntry;
    }

    public Date getLatestLoadWbOutExit() {
        return latestLoadWbOutExit;
    }

    public String getLoadWbOutName() {
        return loadWbOutName;
    }

    public Date getEarliestLoadGateOutEntry() {
        return earliestLoadGateOutEntry;
    }

    public Date getLatestLoadGateOutExit() {
        return latestLoadGateOutExit;
    }

    public String getLoadGateOutName() {
        return loadGateOutName;
    }

    public Date getEarliestUnloadGateInEntry() {
        return earliestUnloadGateInEntry;
    }

    public Date getLatestUnloadGateInExit() {
        return latestUnloadGateInExit;
    }

    public String getUnloadGateInName() {
        return unloadGateInName;
    }

    public Date getEarliestUnloadWbInEntry() {
        return earliestUnloadWbInEntry;
    }

    public Date getLatestUnloadWbInExit() {
        return latestUnloadWbInExit;
    }

    public String getUnloadWbInName() {
        return unloadWbInName;
    }

    public Date getEarliestUnloadYardInEntry() {
        return earliestUnloadYardInEntry;
    }

    public Date getLatestUnloadYardInExit() {
        return latestUnloadYardInExit;
    }

    public String getUnloadYardInName() {
        return unloadYardInName;
    }

    public Date getEarliestUnloadYardOutEntry() {
        return earliestUnloadYardOutEntry;
    }

    public Date getLatestUnloadYardOutExit() {
        return latestUnloadYardOutExit;
    }

    public Date getEarliestUnloadWbOutEntry() {
        return earliestUnloadWbOutEntry;
    }

    public Date getLatestUnloadWbOutExit() {
        return latestUnloadWbOutExit;
    }

    public String getUnloadWbOutName() {
        return unloadWbOutName;
    }

    public Date getEarliestUnloadGateOutEntry() {
        return earliestUnloadGateOutEntry;
    }

    public Date getLatestUnloadGateOutExit() {
        return latestUnloadGateOutExit;
    }

    public String getUnloadGateOutName() {
        return unloadGateOutName;
    }

    public String getBedAssigned() {
        return bedAssigned;
    }

    public int getDriverId() {
        return driverId;
    }

    public String getDlNo() {
        return dlNo;
    }

    public String getDriverName() {
        return driverName;
    }

    public int getMinesTripId() {
        return minesTripId;
    }

    public int getIsMergedWithHHTpr() {
        return isMergedWithHHTpr;
    }

    public Date getHhTprMergedTime() {
        return hhTprMergedTime;
    }

    public int getOldTripId() {
        return oldTripId;
    }

    public int getTprStatus() {
        return tprStatus;
    }

    public String getStatusReason() {
        return statusReason;
    }

    public Date getUpdatedOn() {
        return updatedOn;
    }

    public int getUpdatedBy() {
        return updatedBy;
    }

    public int getPrevTpStep() {
        return prevTpStep;
    }

    public int getNextTpStep() {
        return nextTpStep;
    }

    public Date getConfirmTime() {
        return confirmTime;
    }

    public Date getComboStart() {
        return comboStart;
    }

    public Date getComboEnd() {
        return comboEnd;
    }

    public int getNextTripId() {
        return nextTripId;
    }

    public void setTprId(int tprId) {
        this.tprId = tprId;
    }

    public void setVehicleId(int vehicleId) {
        this.vehicleId = vehicleId;
    }

    public void setVehicleName(String vehicleName) {
        this.vehicleName = vehicleName;
    }

    public void setTransporterId(int transporterId) {
        this.transporterId = transporterId;
    }

    public void setDoId(int doId) {
        this.doId = doId;
    }

    public void setMaterialGradeId(int materialGradeId) {
        this.materialGradeId = materialGradeId;
    }

    public void setPlantId(int plantId) {
        this.plantId = plantId;
    }

    public void setMinesId(int minesId) {
        this.minesId = minesId;
    }

    public void setHhDeviceId(int hhDeviceId) {
        this.hhDeviceId = hhDeviceId;
    }

    public void setChallanNo(String challanNo) {
        this.challanNo = challanNo;
    }

    public void setChallanDate(Date challanDate) {
        this.challanDate = challanDate;
    }

    public void setTprCreateDate(Date tprCreateDate) {
        this.tprCreateDate = tprCreateDate;
    }

    public void setLrNo(String lrNo) {
        this.lrNo = lrNo;
    }

    public void setDispatchPermitNo(String dispatchPermitNo) {
        this.dispatchPermitNo = dispatchPermitNo;
    }

    public void setLoadTare(double loadTare) {
        this.loadTare = loadTare;
    }

    public void setLoadGross(double loadGross) {
        this.loadGross = loadGross;
    }

    public void setUnloadTare(double unloadTare) {
        this.unloadTare = unloadTare;
    }

    public void setUnloadGross(double unloadGross) {
        this.unloadGross = unloadGross;
    }

    public void setEarliestLoadGateInEntry(Date earliestLoadGateInEntry) {
        this.earliestLoadGateInEntry = earliestLoadGateInEntry;
    }

    public void setLatestLoadGateInExit(Date latestLoadGateInExit) {
        this.latestLoadGateInExit = latestLoadGateInExit;
    }

    public void setLoadGateInName(String loadGateInName) {
        this.loadGateInName = loadGateInName;
    }

    public void setEarliestLoadWbInEntry(Date earliestLoadWbInEntry) {
        this.earliestLoadWbInEntry = earliestLoadWbInEntry;
    }

    public void setLatestLoadWbInExit(Date latestLoadWbInExit) {
        this.latestLoadWbInExit = latestLoadWbInExit;
    }

    public void setLoadWbInName(String loadWbInName) {
        this.loadWbInName = loadWbInName;
    }

    public void setEarliestLoadYardInEntry(Date earliestLoadYardInEntry) {
        this.earliestLoadYardInEntry = earliestLoadYardInEntry;
    }

    public void setLatestLoadYardInExit(Date latestLoadYardInExit) {
        this.latestLoadYardInExit = latestLoadYardInExit;
    }

    public void setLoadYardInName(String loadYardInName) {
        this.loadYardInName = loadYardInName;
    }

    public void setEarliestLoadYardOutEntry(Date earliestLoadYardOutEntry) {
        this.earliestLoadYardOutEntry = earliestLoadYardOutEntry;
    }

    public void setLatestLoadYardOutExit(Date latestLoadYardOutExit) {
        this.latestLoadYardOutExit = latestLoadYardOutExit;
    }

    public void setEarliestLoadWbOutEntry(Date earliestLoadWbOutEntry) {
        this.earliestLoadWbOutEntry = earliestLoadWbOutEntry;
    }

    public void setLatestLoadWbOutExit(Date latestLoadWbOutExit) {
        this.latestLoadWbOutExit = latestLoadWbOutExit;
    }

    public void setLoadWbOutName(String loadWbOutName) {
        this.loadWbOutName = loadWbOutName;
    }

    public void setEarliestLoadGateOutEntry(Date earliestLoadGateOutEntry) {
        this.earliestLoadGateOutEntry = earliestLoadGateOutEntry;
    }

    public void setLatestLoadGateOutExit(Date latestLoadGateOutExit) {
        this.latestLoadGateOutExit = latestLoadGateOutExit;
    }

    public void setLoadGateOutName(String loadGateOutName) {
        this.loadGateOutName = loadGateOutName;
    }

    public void setEarliestUnloadGateInEntry(Date earliestUnloadGateInEntry) {
        this.earliestUnloadGateInEntry = earliestUnloadGateInEntry;
    }

    public void setLatestUnloadGateInExit(Date latestUnloadGateInExit) {
        this.latestUnloadGateInExit = latestUnloadGateInExit;
    }

    public void setUnloadGateInName(String unloadGateInName) {
        this.unloadGateInName = unloadGateInName;
    }

    public void setEarliestUnloadWbInEntry(Date earliestUnloadWbInEntry) {
        this.earliestUnloadWbInEntry = earliestUnloadWbInEntry;
    }

    public void setLatestUnloadWbInExit(Date latestUnloadWbInExit) {
        this.latestUnloadWbInExit = latestUnloadWbInExit;
    }

    public void setUnloadWbInName(String unloadWbInName) {
        this.unloadWbInName = unloadWbInName;
    }

    public void setEarliestUnloadYardInEntry(Date earliestUnloadYardInEntry) {
        this.earliestUnloadYardInEntry = earliestUnloadYardInEntry;
    }

    public void setLatestUnloadYardInExit(Date latestUnloadYardInExit) {
        this.latestUnloadYardInExit = latestUnloadYardInExit;
    }

    public void setUnloadYardInName(String unloadYardInName) {
        this.unloadYardInName = unloadYardInName;
    }

    public void setEarliestUnloadYardOutEntry(Date earliestUnloadYardOutEntry) {
        this.earliestUnloadYardOutEntry = earliestUnloadYardOutEntry;
    }

    public void setLatestUnloadYardOutExit(Date latestUnloadYardOutExit) {
        this.latestUnloadYardOutExit = latestUnloadYardOutExit;
    }

    public void setEarliestUnloadWbOutEntry(Date earliestUnloadWbOutEntry) {
        this.earliestUnloadWbOutEntry = earliestUnloadWbOutEntry;
    }

    public void setLatestUnloadWbOutExit(Date latestUnloadWbOutExit) {
        this.latestUnloadWbOutExit = latestUnloadWbOutExit;
    }

    public void setUnloadWbOutName(String unloadWbOutName) {
        this.unloadWbOutName = unloadWbOutName;
    }

    public void setEarliestUnloadGateOutEntry(Date earliestUnloadGateOutEntry) {
        this.earliestUnloadGateOutEntry = earliestUnloadGateOutEntry;
    }

    public void setLatestUnloadGateOutExit(Date latestUnloadGateOutExit) {
        this.latestUnloadGateOutExit = latestUnloadGateOutExit;
    }

    public void setUnloadGateOutName(String unloadGateOutName) {
        this.unloadGateOutName = unloadGateOutName;
    }

    public void setBedAssigned(String bedAssigned) {
        this.bedAssigned = bedAssigned;
    }

    public void setDriverId(int driverId) {
        this.driverId = driverId;
    }

    public void setDlNo(String dlNo) {
        this.dlNo = dlNo;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    public void setMinesTripId(int minesTripId) {
        this.minesTripId = minesTripId;
    }

    public void setIsMergedWithHHTpr(int isMergedWithHHTpr) {
        this.isMergedWithHHTpr = isMergedWithHHTpr;
    }

    public void setHhTprMergedTime(Date hhTprMergedTime) {
        this.hhTprMergedTime = hhTprMergedTime;
    }

    public void setOldTripId(int oldTripId) {
        this.oldTripId = oldTripId;
    }

    public void setTprStatus(int tprStatus) {
        this.tprStatus = tprStatus;
    }

    public void setStatusReason(String statusReason) {
        this.statusReason = statusReason;
    }

    public void setUpdatedOn(Date updatedOn) {
        this.updatedOn = updatedOn;
    }

    public void setUpdatedBy(int updatedBy) {
        this.updatedBy = updatedBy;
    }

    public void setPrevTpStep(int prevTpStep) {
        this.prevTpStep = prevTpStep;
    }

    public void setNextTpStep(int nextTpStep) {
        this.nextTpStep = nextTpStep;
    }

    public void setConfirmTime(Date confirmTime) {
        this.confirmTime = confirmTime;
    }

    public void setComboStart(Date comboStart) {
        this.comboStart = comboStart;
    }

    public void setComboEnd(Date comboEnd) {
        this.comboEnd = comboEnd;
    }

    public void setNextTripId(int nextTripId) {
        this.nextTripId = nextTripId;
    }

	public int getRfTransporterId() {
		return rfTransporterId;
	}

	public int getRfMinesId() {
		return rfMinesId;
	}

	public int getRfGrade() {
		return rfGrade;
	}

	public Date getRfChallanDate() {
		return rfChallanDate;
	}

	public String getRfChallanId() {
		return rfChallanId;
	}

	public String getRfLRId() {
		return rfLRId;
	}

	public double getRfLoadTare() {
		return rfLoadTare;
	}

	public double getRfLoadGross() {
		return rfLoadGross;
	}

	public int getRfDeviceId() {
		return rfDeviceId;
	}

	public int getRfDOId() {
		return rfDOId;
	}

	public int getRfRecordId() {
		return rfRecordId;
	}

	public String getRfRecordKey() {
		return rfRecordKey;
	}

	public void setRfTransporterId(int rfTransporterId) {
		this.rfTransporterId = rfTransporterId;
	}

	public void setRfMinesId(int rfMinesId) {
		this.rfMinesId = rfMinesId;
	}

	public void setRfGrade(int rfGrade) {
		this.rfGrade = rfGrade;
	}

	public void setRfChallanDate(Date rfChallanDate) {
		this.rfChallanDate = rfChallanDate;
	}

	public void setRfChallanId(String rfChallanId) {
		this.rfChallanId = rfChallanId;
	}

	public void setRfLRId(String rfLRId) {
		this.rfLRId = rfLRId;
	}

	public void setRfLoadTare(double rfLoadTare) {
		this.rfLoadTare = rfLoadTare;
	}

	public void setRfLoadGross(double rfLoadGross) {
		this.rfLoadGross = rfLoadGross;
	}

	public void setRfDeviceId(int rfDeviceId) {
		this.rfDeviceId = rfDeviceId;
	}

	public void setRfDOId(int rfDOId) {
		this.rfDOId = rfDOId;
	}

	public void setRfRecordId(int rfRecordId) {
		this.rfRecordId = rfRecordId;
	}

	public void setRfRecordKey(String rfRecordKey) {
		this.rfRecordKey = rfRecordKey;
	}

	public Date getLrDate() {
		return lrDate;
	}

	public Date getRfLRDate() {
		return rfLRDate;
	}

	public void setLrDate(Date lrDate) {
		this.lrDate = lrDate;
	}

	public void setRfLRDate(Date rfLRDate) {
		this.rfLRDate = rfLRDate;
	}

	public void setMaterialCodeId(int materialCodeId) {
		this.materialCodeId = materialCodeId;
	}

	public int getMaterialCodeId() {
		return materialCodeId;
	}

	public void setMplRefDoc(int mplRefDoc) {
		this.mplRefDoc = mplRefDoc;
	}

	public int getMplRefDoc() {
		return mplRefDoc;
	}



	public void setConsigneeRefDoc(String consigneeRefDoc) {
		this.consigneeRefDoc = consigneeRefDoc;
	}

	public String getConsigneeRefDoc() {
		return consigneeRefDoc;
	}

	public void setMaterialDescription(String materialDescription) {
		this.materialDescription = materialDescription;
	}

	public String getMaterialDescription() {
		return materialDescription;
	}

	public void setConsigneeAddress(String consigneeAddress) {
		this.consigneeAddress = consigneeAddress;
	}

	public String getConsigneeAddress() {
		return consigneeAddress;
	}

	public void setConsigneeNotes(String consigneeNotes) {
		this.consigneeNotes = consigneeNotes;
	}

	public String getConsigneeNotes() {
		return consigneeNotes;
	}

	public void setCarryingTransporterId(int carryingTransporterId) {
		this.carryingTransporterId = carryingTransporterId;
	}

	public int getCarryingTransporterId() {
		return carryingTransporterId;
	}

	public void setConsignorId(int consignorId) {
		this.consignorId = consignorId;
	}

	public int getConsignorId() {
		return consignorId;
	}

	public void setConsignorName(String consignorName) {
		this.consignorName = consignorName;
	}

	public String getConsignorName() {
		return consignorName;
	}

	public void setConsignorRefDoc(String consignorRefDoc) {
		this.consignorRefDoc = consignorRefDoc;
	}

	public String getConsignorRefDoc() {
		return consignorRefDoc;
	}

	public void setConsignorAddress(String consignorAddress) {
		this.consignorAddress = consignorAddress;
	}

	public String getConsignorAddress() {
		return consignorAddress;
	}

	public void setConsignorNotes(String consignorNotes) {
		this.consignorNotes = consignorNotes;
	}

	public String getConsignorNotes() {
		return consignorNotes;
	}
	
	public RFIDHolder getHolderRFData(){
		RFIDHolder retval = null;
		if(this.rfChallanDate != null){
			retval = new RFIDHolder();
			retval.setRefTPRId(tprId);
			retval.setVehicleId(rfVehicleId);
			retval.setVehicleName(rfVehicleName);
			retval.setTransporterId(rfTransporterId);
        	retval.setMinesId(rfMinesId);
        	retval.setGrade(rfGrade);
        	retval.setDatetime(rfChallanDate);
        	retval.setChallanId(rfChallanId);
        	retval.setLrDate(rfLRDate);
        	retval.setLRID(rfLRId);
        	retval.setLoadTare((int)Math.round(rfLoadTare));
        	retval.setLoadGross((int)Math.round(rfLoadGross));
        	retval.setDeviceId(rfDeviceId);
        	retval.setDoId(rfDOId);
        	retval.setId(rfRecordId);
        	retval.setMaterial(materialCat);
		}
		return retval;
	}
	public RFIDHolder getHolderManualData(){
		RFIDHolder retval = null;
		if(this.challanDate != null){
			retval = new RFIDHolder();
			retval.setRefTPRId(tprId);
			retval.setVehicleId(vehicleId);
			retval.setVehicleName(vehicleName);
			retval.setTransporterId(transporterId);
        	retval.setMinesId(minesId);
        	retval.setGrade(materialGradeId);
        	retval.setDatetime(challanDate);
        	retval.setChallanId(challanNo);
        	retval.setLrDate(lrDate);
        	retval.setLRID(lrNo);
        	retval.setLoadTare((int)Math.round(loadTare));
        	retval.setLoadGross((int)Math.round(loadGross));
        	retval.setDoId(doId);
        	retval.setMaterial(materialCat);
		}
		return retval;
	}

	public int getMarkForQC() {
		return markForQC;
	}

	public String getPermitNo() {
		return permitNo;
	}

	public int getPreStepType() {
		return preStepType;
	}

	public Date getPreStepDate() {
		return preStepDate;
	}

	public int getBlockedStepType() {
		return blockedStepType;
	}

	public Date getBlockedStepDate() {
		return blockedStepDate;
	}

	public void setMarkForQC(int markForQC) {
		this.markForQC = markForQC;
	}

	public void setPermitNo(String permitNo) {
		this.permitNo = permitNo;
	}

	public void setPreStepType(int preStepType) {
		this.preStepType = preStepType;
	}

	public void setPreStepDate(Date preStepDate) {
		this.preStepDate = preStepDate;
	}

	public void setBlockedStepType(int blockedStepType) {
		this.blockedStepType = blockedStepType;
	}

	public void setBlockedStepDate(Date blockedStepDate) {
		this.blockedStepDate = blockedStepDate;
	}

	public int getNextStepType() {
		return nextStepType;
	}

	public int getBlockedStepId() {
		return blockedStepId;
	}

	public void setNextStepType(int nextStepType) {
		this.nextStepType = nextStepType;
	}

	public void setBlockedStepId(int blockedStepId) {
		this.blockedStepId = blockedStepId;
	}

	public int isLatest() {
		return isLatest;
	}

	public void setLatest(int isLatest) {
		this.isLatest = isLatest;
	}
	public String toString(){
		StringBuilder retval = new StringBuilder();
		retval.append("\n@@@[TPR Details]@@@\n");
		retval.append("[\n");
		retval.append("TprId : ").append(tprId).append("\n");
		retval.append("VehicleName : ").append(vehicleName).append("\n");
		retval.append("VehicleId : ").append(vehicleId).append("\n");
		retval.append("ChallanNo : ").append(challanNo).append("\n");
		retval.append("LrNo : ").append(lrNo).append("\n");
		retval.append("ChallanDate : ").append(challanDate).append("\n");
		retval.append("MinesId : ").append(minesId).append("\n");
		retval.append("TransporterId : ").append(transporterId).append("\n");
		retval.append("DoId : ").append(doId).append("\n");
		retval.append("GradeId : ").append(materialGradeId).append("\n");
		retval.append("IsLatest : ").append(isLatest).append("\n");
		retval.append("TprStatus : ").append(tprStatus).append("\n");
		retval.append("LatestUnloadGateInExit:"+getLatestUnloadGateInExit()).append("\n");
		retval.append("LatestUnloadWbInExit:"+getLatestUnloadWbInExit()).append("\n");
		retval.append("LatestUnloadYardInExit:"+getLatestUnloadYardInExit()).append("\n");
		retval.append("LatestUnloadYardOutExit:"+getLatestUnloadYardOutExit()).append("\n");
		retval.append("LatestUnloadWbOutExit:"+getLatestUnloadWbOutExit()).append("\n");
		retval.append("LatestUnloadGateOutExit:"+getLatestUnloadGateOutExit()).append("\n");
		retval.append("MarkForQC:"+getMarkForQC()).append("\n");
		retval.append("MarkForGPS:"+getMarkForGPS()).append("\n");
		retval.append("TprCreateDate : ").append(tprCreateDate).append("\n");
		retval.append("ComboStart:"+getComboStart()).append("\n");
		retval.append("ComboEnd:"+getComboEnd()).append("\n");
		retval.append("]\n");
		return retval.toString();
	}
	public void print(){
		System.out.println(toString());
	}

	public int getIsLatest() {
		return isLatest;
	}

	public int getDriverSrc() {
		return driverSrc;
	}

	public int getVehicleSrc() {
		return vehicleSrc;
	}

	public void setIsLatest(int isLatest) {
		this.isLatest = isLatest;
	}

	public void setDriverSrc(int driverSrc) {
		this.driverSrc = driverSrc;
	}

	public void setVehicleSrc(int vehicleSrc) {
		this.vehicleSrc = vehicleSrc;
	}

	public int getMarkForGPS() {
		return markForGPS;
	}

	public void setMarkForGPS(int markForGPS) {
		this.markForGPS = markForGPS;
	}

	public int getSupplierId() {
		return supplierId;
	}

	public void setSupplierId(int supplierId) {
		this.supplierId = supplierId;
	}

	public ArrayList<TPRBlockEntry> getBlockingEntries() {
		return blockingEntries;
	}

	public void setBlockingEntries(ArrayList<TPRBlockEntry> blockingEntries) {
		this.blockingEntries = blockingEntries;
	}

	public String getMarkForQCReason() {
		return markForQCReason;
	}

	public void setMarkForQCReason(String markForQCReason) {
		this.markForQCReason = markForQCReason;
	}

	public int getIsNewVehicle() {
		return isNewVehicle;
	}

	public int getSrcDeviceLogId() {
		return srcDeviceLogId;
	}

	public int getRfAlsoOnCard() {
		return rfAlsoOnCard;
	}

	public int getMaterialCat() {
		return materialCat;
	}

	public int getStoneLiftAreaId() {
		return stoneLiftAreaId;
	}

	public int getStoneOfTransporterId() {
		return stoneOfTransporterId;
	}

	public void setIsNewVehicle(int isNewVehicle) {
		this.isNewVehicle = isNewVehicle;
	}

	public void setSrcDeviceLogId(int srcDeviceLogId) {
		this.srcDeviceLogId = srcDeviceLogId;
	}

	public void setRfAlsoOnCard(int rfAlsoOnCard) {
		this.rfAlsoOnCard = rfAlsoOnCard;
	}

	public void setMaterialCat(int materialCat) {
		this.materialCat = materialCat;
	}

	public void setStoneLiftAreaId(int stoneLiftAreaId) {
		this.stoneLiftAreaId = stoneLiftAreaId;
	}

	public void setStoneOfTransporterId(int stoneOfTransporterId) {
		this.stoneOfTransporterId = stoneOfTransporterId;
	}

	public Date getEarliestRegIn() {
		return earliestRegIn;
	}

	public Date getLatestRegOut() {
		return latestRegOut;
	}

	public void setEarliestRegIn(Date earliestRegIn) {
		this.earliestRegIn = earliestRegIn;
	}

	public void setLatestRegOut(Date latestRegOut) {
		this.latestRegOut = latestRegOut;
	}

	public int getChallanDataEditAtReg() {
		return challanDataEditAtReg;
	}

	public int getChallanDataEditAtWb() {
		return challanDataEditAtWb;
	}

	public int getChallanDataEditAtPreaudit() {
		return challanDataEditAtPreaudit;
	}

	public int getChallanDataEditAtAudit() {
		return challanDataEditAtAudit;
	}

	public void setChallanDataEditAtReg(int challanDataEditAtReg) {
		this.challanDataEditAtReg = challanDataEditAtReg;
	}

	public void setChallanDataEditAtWb(int challanDataEditAtWb) {
		this.challanDataEditAtWb = challanDataEditAtWb;
	}

	public void setChallanDataEditAtPreaudit(int challanDataEditAtPreaudit) {
		this.challanDataEditAtPreaudit = challanDataEditAtPreaudit;
	}

	public void setChallanDataEditAtAudit(int challanDataEditAtAudit) {
		this.challanDataEditAtAudit = challanDataEditAtAudit;
	}
	
	public boolean isEquals(int vehicleId, int doId, int minesId, int materialGradeId, int transporterId, String challanNo, String lrNo, Date challanDate, Date lrDate, double loadTare, double loadGross){
		return 
				vehicleId == this.vehicleId
				&& doId == this.doId 
		        && minesId == this.minesId
		        && materialGradeId ==  this.materialGradeId
		        && transporterId == this.transporterId
		        && challanNo == this.challanNo
		        && lrNo == this.lrNo
		        && loadTare == this.loadTare
				&& ((challanDate == null ? Misc.getUndefInt() : challanDate.getTime()) == (this.challanDate == null ? Misc.getUndefInt() : this.challanDate.getTime() ))
		        && ((lrDate == null ? Misc.getUndefInt() : lrDate.getTime()) == (this.lrDate == null ? Misc.getUndefInt() : this.lrDate.getTime() ))
		        && loadGross == this.loadGross;
	}
	
	public boolean isEquals(TPRecord tpRecord){
		return isEquals(tpRecord.getVehicleId(), tpRecord.getDoId(), tpRecord.getMinesId(), tpRecord.getMaterialGradeId(), tpRecord.getTransporterId(), tpRecord.getChallanNo(), tpRecord.getLrNo(), tpRecord.getChallanDate(), tpRecord.getLrDate(), tpRecord.getLoadTare(), tpRecord.getLoadGross());
	}
	
	public long getWorkStationTime(int workStationType){
		if(Misc.isUndef(workStationType) )
			return Misc.getUndefInt();
		Date lastPrcoessed =  null;
		switch (workStationType) {
		case Type.WorkStationType.GATE_IN_TYPE:
			lastPrcoessed = latestUnloadGateInExit;
			break;
		case Type.WorkStationType.REGISTRATION:
			lastPrcoessed = latestRegOut;
			break;
		case Type.WorkStationType.WEIGH_BRIDGE_IN_TYPE:
			lastPrcoessed = latestUnloadWbInExit;
			break;
		case Type.WorkStationType.YARD_IN_TYPE:
			lastPrcoessed = latestUnloadYardInExit;
			break;
		case Type.WorkStationType.YARD_OUT_TYPE:
			lastPrcoessed = latestUnloadYardOutExit;
			break;
		case Type.WorkStationType.WEIGH_BRIDGE_OUT_TYPE:
			lastPrcoessed = latestUnloadWbOutExit;
			break;
		case Type.WorkStationType.GATE_OUT_TYPE:
			lastPrcoessed = latestUnloadGateOutExit;
			break;
		case Type.WorkStationType.FLY_ASH_IN_TYPE:
			lastPrcoessed = latestLoadGateInExit;
			break;
		case Type.WorkStationType.FLY_ASH_TARE_WT_TYPE:
			lastPrcoessed = latestLoadWbInExit;
			break;
		case Type.WorkStationType.FLY_ASH_GROSS_WT_TYPE:
			lastPrcoessed = latestLoadWbOutExit;
			break;
		case Type.WorkStationType.STONE_TARE_WT_TYPE:
			lastPrcoessed = latestLoadWbInExit;
			break;
		case Type.WorkStationType.STONE_GROSS_WT_TYPE:
			lastPrcoessed = latestLoadWbOutExit;
			break;
		case Type.WorkStationType.FIRST_WEIGHTMENT_TYPE:
			lastPrcoessed = latestLoadWbInExit;
			break;
		case Type.WorkStationType.SECOND_WEIGHTMENT_TYPE:
			lastPrcoessed = latestLoadWbOutExit;
			break;
		default:
			break;
		}
		return lastPrcoessed != null ? lastPrcoessed.getTime() : Misc.getUndefInt();
	}
	
	public void changeWtInkg(){
		//change wt. in tons
		if(Misc.isUndef(this.getLoadGross()) && this.getLoadGross() < 50)
			this.setLoadGross(this.getLoadGross()*1000);
		
		if(Misc.isUndef(this.getLoadTare()) && this.getLoadTare() < 50)
			this.setLoadTare(this.getLoadTare()*1000);
		
		if(Misc.isUndef(this.getUnloadGross()) && this.getUnloadGross() < 50)
			this.setUnloadGross(this.getUnloadGross()*1000);
		
		if(Misc.isUndef(this.getUnloadTare()) && this.getUnloadTare() < 50)
			this.setUnloadTare(this.getUnloadTare()*1000);
		
		if(Misc.isUndef(this.getRfLoadGross()) && this.getRfLoadGross() < 50)
			this.setRfLoadGross(this.getRfLoadGross()*1000);
		
		if(Misc.isUndef(this.getRfLoadTare()) && this.getRfLoadTare() < 50)
			this.setRfLoadTare(this.getRfLoadTare()*1000);
	}
	public void changeWtInTons(){
		if(this.getLoadGross() > 8000)
			this.setLoadGross(this.getLoadGross()/1000);
		
		if(this.getLoadTare() > 8000)
			this.setLoadTare(this.getLoadTare()/1000);
		
		if(this.getUnloadGross() > 8000)
			this.setUnloadGross(this.getUnloadGross()/1000);
		
		if(this.getUnloadTare() > 8000)
			this.setUnloadTare(this.getUnloadTare()/1000);
		
		if(this.getRfLoadGross() > 8000)
			this.setRfLoadGross(this.getRfLoadGross()/1000);
		
		if(this.getRfLoadTare() > 8000)
			this.setRfLoadTare(this.getRfLoadTare()/1000);
	}

	public String getMaterialNotesFirst() {
		return materialNotesFirst;
	}

	public String getMaterialNotesSecond() {
		return materialNotesSecond;
	}

	public void setMaterialNotesFirst(String materialNotesFirst) {
		this.materialNotesFirst = materialNotesFirst;
	}

	public void setMaterialNotesSecond(String materialNotesSecond) {
		this.materialNotesSecond = materialNotesSecond;
	}

	public String getMplReferenceDoc() {
		return mplReferenceDoc;
	}

	public void setMplReferenceDoc(String mplReferenceDoc) {
		this.mplReferenceDoc = mplReferenceDoc;
	}

	public Date getRfCardDataMergeTime() {
		return rfCardDataMergeTime;
	}

	public void setRfCardDataMergeTime(Date rfCardDataMergeTime) {
		this.rfCardDataMergeTime = rfCardDataMergeTime;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public int getIsMultipleOpenTPR() {
		return isMultipleOpenTPR;
	}

	public void setIsMultipleOpenTPR(int isMultipleOpenTPR) {
		this.isMultipleOpenTPR = isMultipleOpenTPR;
	}

	public String getRfVehicleName() {
		return rfVehicleName;
	}

	public int getRfVehicleId() {
		return rfVehicleId;
	}

	public void setRfVehicleName(String rfVehicleName) {
		this.rfVehicleName = rfVehicleName;
	}

	public void setRfVehicleId(int rfVehicleId) {
		this.rfVehicleId = rfVehicleId;
	}
	public boolean isEmpty(){
		return isLeftEmpty() && isRightEmpty();
	}
	public boolean isLeftEmpty(){
		return challanDate == null;
	}
	public boolean isRightEmpty(){
		return rfChallanDate == null;
	}
	
	public String getOtherMaterialDescription() {
		return otherMaterialDescription;
	}

	public void setOtherMaterialDescription(String otherMaterialDescription) {
		this.otherMaterialDescription = otherMaterialDescription;
	}

	public String getDebugStr() {
		return debugStr;
	}

	public void setDebugStr(String debugStr) {
		this.debugStr = debugStr;
	}
	public long getLastProcessedTime(){
		Date lastProcessedDate = null;
		if(comboEnd != null){
			lastProcessedDate = comboEnd;
		}else if(comboStart != null){
			lastProcessedDate = comboStart;
		}/*else if(tprCreateDate != null){
			lastProcessedDate = tprCreateDate;
		}*/else if(challanDate != null){
			lastProcessedDate = challanDate;
		}
		return lastProcessedDate != null ? lastProcessedDate.getTime() : Misc.getUndefInt();
	}

	public int getMaterial_sub_cat_id() {
		return material_sub_cat_id;
	}

	public void setMaterial_sub_cat_id(int material_sub_cat_id) {
		this.material_sub_cat_id = material_sub_cat_id;
	}

	public int getTprCreateType() {
		return tprCreateType;
	}

	public void setTprCreateType(int tprCreateType) {
		this.tprCreateType = tprCreateType;
	}

	public String getUnloadYardOutName() {
		return unloadYardOutName;
	}

	public void setUnloadYardOutName(String unloadYardOutName) {
		this.unloadYardOutName = unloadYardOutName;
	}

	public String getLoadYardOutName() {
		return loadYardOutName;
	}

	public void setLoadYardOutName(String loadYardOutName) {
		this.loadYardOutName = loadYardOutName;
	}

	public String getLoadFlyashTareName() {
		return loadFlyashTareName;
	}

	public void setLoadFlyashTareName(String loadFlyashTareName) {
		this.loadFlyashTareName = loadFlyashTareName;
	}

	public String getLoadFlyashGrossName() {
		return loadFlyashGrossName;
	}

	public void setLoadFlyashGrossName(String loadFlyashGrossName) {
		this.loadFlyashGrossName = loadFlyashGrossName;
	}
}
