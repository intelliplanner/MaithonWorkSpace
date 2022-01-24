package com.ipssi;

import java.util.Date;

import javax.persistence.UniqueConstraint;

import com.ipssi.gen.utils.Misc;
import com.ipssi.rfid.db.Table;
import com.ipssi.rfid.db.Table.Column;
import com.ipssi.rfid.db.Table.GENRATED;
import com.ipssi.rfid.db.Table.KEY;
import com.ipssi.rfid.db.Table.PRIMARY_KEY;
import com.ipssi.rfid.db.Table.ReadOnly;


@Table("mpl_coal_sample_details")
public class CoalSample {

	@KEY
	@GENRATED
	@PRIMARY_KEY
	
	@Column("id")
	private int id = Misc.getUndefInt();
	
	@Column("tpr_id")
	private int tprId = Misc.getUndefInt();

	@Column("rfid_epc")
	private String rfidEPC;
	
	@Column("sample_read")
	private int sampleRead;
	
	@Column("qc_selected")
	private int qcSelected;
	
	@Column("tag_issued")
	private int tagIssued;
	
	@Column("is_latest")
	private int isLatest;

	@Column("identify_sampling_time")
	private Date identifySamplingTime;
	
	@Column("sampling_done_time")
	private Date samplingDoneTime;

	@Column("updated_on")
	private Date updatedOn;
	
	@ReadOnly
	@Column("created_on")
	private Date created_on;
	
	@ReadOnly
	@Column("status")
	private int status;


	@ReadOnly
	@Column("port_node_id")
	private int portNodeId;
	
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getTprId() {
		return tprId;
	}

	public void setTprId(int tprId) {
		this.tprId = tprId;
	}

	public String getRfidEPC() {
		return rfidEPC;
	}

	public void setRfidEPC(String rfidEPC) {
		this.rfidEPC = rfidEPC;
	}

	public int getSampleRead() {
		return sampleRead;
	}

	public void setSampleRead(int sampleRead) {
		this.sampleRead = sampleRead;
	}

	public Date getIdentifySamplingTime() {
		return identifySamplingTime;
	}

	public void setIdentifySamplingTime(Date identifySamplingTime) {
		this.identifySamplingTime = identifySamplingTime;
	}

	public Date getSamplingDoneTime() {
		return samplingDoneTime;
	}

	public void setSamplingDoneTime(Date samplingDoneTime) {
		this.samplingDoneTime = samplingDoneTime;
	}

	public Date getUpdatedOn() {
		return updatedOn;
	}

	public void setUpdatedOn(Date updatedOn) {
		this.updatedOn = updatedOn;
	}

	public Date getCreated_on() {
		return created_on;
	}

	public void setCreated_on(Date created_on) {
		this.created_on = created_on;
	}

	public int getQcSelected() {
		return qcSelected;
	}

	public void setQcSelected(int qcSelected) {
		this.qcSelected = qcSelected;
	}

	public int getTagIssued() {
		return tagIssued;
	}

	public void setTagIssued(int tagIssued) {
		this.tagIssued = tagIssued;
	}


	public int getIsLatest() {
		return isLatest;
	}

	public void setIsLatest(int isLatest) {
		this.isLatest = isLatest;
	}
	
	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}


	public int getPortNodeId() {
		return portNodeId;
	}

	public void setPortNodeId(int portNodeId) {
		this.portNodeId = portNodeId;
	}
}

