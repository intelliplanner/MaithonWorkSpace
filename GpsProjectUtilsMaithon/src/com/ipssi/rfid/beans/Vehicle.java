package com.ipssi.rfid.beans;

import java.util.Date;

import com.ipssi.gen.utils.Misc;
import com.ipssi.rfid.db.Table;
import com.ipssi.rfid.db.Table.Column;
import com.ipssi.rfid.db.Table.GENRATED;
import com.ipssi.rfid.db.Table.KEY;
import com.ipssi.rfid.db.Table.PRIMARY_KEY;

@Table("vehicle")
public class Vehicle {

    @KEY
    @GENRATED
    @PRIMARY_KEY
    @Column("id")
    private int id = Misc.getUndefInt();
    @Column("name")
    private String vehicleName;
    @Column("rfid_epc")
    private String epcId;
    @Column("std_name")
    private String stdName;
    @Column("tare")
    private double avgTare = Misc.getUndefDouble();
    @Column("gross")
    private double avgGross = Misc.getUndefDouble();
    @Column("status")
    private int status = Misc.getUndefInt();
    @Column("rfid_temp_status")
    private int rfidTempStatus = Misc.getUndefInt();
    @Column("rfid_issue_date")
    private Date rfid_issue_date;
    
    @Column("last_epc")
    private String lastEPC;
    @Column("customer_id")
    private int customerId = Misc.getUndefInt();
    
    @Column("flyash_tare")
    private double flyashTare = Misc.getUndefDouble();
    
    public double getFlyashTare() {
		return flyashTare;
	}


	public void setFlyashTare(double flyashTare) {
		this.flyashTare = flyashTare;
	}


	public Date getFlyashTareTime() {
		return flyashTareTime;
	}


	public void setFlyashTareTime(Date flyashTareTime) {
		this.flyashTareTime = flyashTareTime;
	}
	@Column("flyash_tare_time")
    private Date flyashTareTime;
    
    
    public Date getRfid_issue_date() {
        return rfid_issue_date;
    }

    
    public void setRfid_issue_date(Date rfid_issue_date) {
        this.rfid_issue_date = rfid_issue_date;
    }
    public int getRfidTempStatus() {
        return rfidTempStatus;
    }

    public void setRfidTempStatus(int rfidTempStatus) {
        this.rfidTempStatus = rfidTempStatus;
    }
    private int transporterId;
    
    private int minesId;
    
    private Date createdOn;
    
    private Date updatedOn;
    
    private int updatedBy;
    
    @Column("stone_tare_time")
    private Date stoneTareTime;
    
    @Column("stone_tare")
    private double stoneTare = Misc.getUndefDouble();
    
    public int getId() {
        return id;
    }

    public String getVehicleName() {
        return vehicleName;
    }

    public String getEpcId() {
        return epcId;
    }

    public double getAvgTare() {
        return avgTare;
    }

    public double getAvgGross() {
        return avgGross;
    }

    public int getStatus() {
        return status;
    }

    public int getTransporterId() {
        return transporterId;
    }

    public int getMinesId() {
        return minesId;
    }

    public Date getCreatedOn() {
        return createdOn;
    }

    public Date getUpdatedOn() {
        return updatedOn;
    }

    public int getUpdatedBy() {
        return updatedBy;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setVehicleName(String vehicleName) {
        this.vehicleName = vehicleName;
    }

    public void setEpcId(String epcId) {
        this.epcId = epcId;
    }

    public void setAvgTare(double avgTare) {
        this.avgTare = avgTare;
    }

    public void setAvgGross(double avgGross) {
        this.avgGross = avgGross;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setTransporterId(int transporterId) {
        this.transporterId = transporterId;
    }

    public void setMinesId(int minesId) {
        this.minesId = minesId;
    }

    public void setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
    }

    public void setUpdatedOn(Date updatedOn) {
        this.updatedOn = updatedOn;
    }

    public void setUpdatedBy(int updatedBy) {
        this.updatedBy = updatedBy;
    }

    public String getStdName() {
        return stdName;
    }

    public void setStdName(String stdName) {
        this.stdName = stdName;
    }

	public String getLastEPC() {
		return lastEPC;
	}

	public void setLastEPC(String lastEPC) {
		this.lastEPC = lastEPC;
	}

	public int getCustomerId() {
		return customerId;
	}

	public void setCustomerId(int customerId) {
		this.customerId = customerId;
	}


	public Date getStoneTareTime() {
		return stoneTareTime;
	}


	public void setStoneTareTime(Date stoneTareTime) {
		this.stoneTareTime = stoneTareTime;
	}


	public void setStoneTare(double stoneTare) {
		this.stoneTare = stoneTare;
	}


	public double getStoneTare() {
		return stoneTare;
	}
}
