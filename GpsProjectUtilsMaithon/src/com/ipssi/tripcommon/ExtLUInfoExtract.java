package com.ipssi.tripcommon;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import com.ipssi.RegionTest.RegionTest;
import com.ipssi.RegionTest.RegionTest.RegionTestHelper;
import com.ipssi.gen.utils.Misc;
import com.ipssi.processor.utils.GpsData;
public class ExtLUInfoExtract extends LUInfoExtract  {
    public static class WBInfo {
    	private long wb1In = Misc.getUndefInt();
    	private long wb2In = Misc.getUndefInt();
    	private long wb3In = Misc.getUndefInt();
    	private long wb1Out = Misc.getUndefInt();
    	private long wb2Out = Misc.getUndefInt();
    	private long wb3Out = Misc.getUndefInt();
    	private int wb1Id = Misc.getUndefInt();
    	private int wb2Id = Misc.getUndefInt();
    	private int wb3Id = Misc.getUndefInt();
    	public void copy(WBInfo rhs) {
    		if (rhs == null) {
    			wb1In =  Misc.getUndefInt();
    			wb2In =  Misc.getUndefInt();
    			wb3In =  Misc.getUndefInt();
    			wb1Out =  Misc.getUndefInt();
    			wb2Out =  Misc.getUndefInt();
    			wb3Out =  Misc.getUndefInt();
    			wb1Id =  Misc.getUndefInt();
    			wb2Id = Misc.getUndefInt();
    			wb3Id = Misc.getUndefInt();
    		}
    		else {
    			wb1In =  rhs.wb1In;
    			wb2In =  rhs.wb2In;
    			wb3In =  rhs.wb3In;
    			wb1Out = rhs.wb1Out;
    			wb2Out = rhs.wb2Out;
    			wb3Out = rhs.wb3Out;
    			wb1Id = rhs.wb1Id;
    			wb2Id = rhs.wb2Id;
    			wb3Id = rhs.wb3Id;
    		}
    	}
    	public boolean equals(WBInfo rhs) {
        	return (
        			rhs != null &&
        	((!Misc.isUndef(wb1In) && !Misc.isUndef(rhs.wb1In) && wb1In == rhs.wb1In) || (Misc.isUndef(wb1In) && Misc.isUndef(rhs.wb1In))) &&
        	((!Misc.isUndef(wb2In) && !Misc.isUndef(rhs.wb2In) && wb2In == rhs.wb2In) || (Misc.isUndef(wb2In) && Misc.isUndef(rhs.wb2In))) &&
        	((!Misc.isUndef(wb3In) && !Misc.isUndef(rhs.wb3In) && wb3In == rhs.wb3In) || (Misc.isUndef(wb3In) && Misc.isUndef(rhs.wb3In))) &&
        	((!Misc.isUndef(wb1Out) && !Misc.isUndef(rhs.wb1Out) && wb1Out == rhs.wb1Out) || (Misc.isUndef(wb1Out) && Misc.isUndef(rhs.wb1Out))) &&
        	((!Misc.isUndef(wb2Out) && !Misc.isUndef(rhs.wb2Out) && wb2Out == rhs.wb2Out) || (Misc.isUndef(wb2Out) && Misc.isUndef(rhs.wb2Out))) &&
        	((!Misc.isUndef(wb3Out) && !Misc.isUndef(rhs.wb3Out) && wb3Out == rhs.wb3Out) || (Misc.isUndef(wb3Out) && Misc.isUndef(rhs.wb3Out))) &&
        	(wb1Id == rhs.wb1Id) &&
        	(wb2Id == rhs.wb2Id) &&
        	(wb3Id == rhs.wb3Id)
        	)
        	;    	
        }

    	public String toString() {
	    	return	
	    	   "WB1:"+ExtLUInfoExtract.getRegionName(wb1Id)+"WB1in:"+ExtLUInfoExtract.dbgFormat(wb1In)+"WB1out:"+dbgFormat(wb1Out)
	    	+"WB2:"+ExtLUInfoExtract.getRegionName(wb2Id)+"WB2in:"+ExtLUInfoExtract.dbgFormat(wb2In)+"WB2out:"+dbgFormat(wb2Out)
	    	+"WB3:"+ExtLUInfoExtract.getRegionName(wb3Id)+"WB3in:"+ExtLUInfoExtract.dbgFormat(wb3In)+"WB3out:"+dbgFormat(wb3Out)
	    	;
    	}
		public long getWb1In() {
			return wb1In;
		}
		public void setWb1In(long wb1In) {
			this.wb1In = wb1In;
		}
		public long getWb2In() {
			return wb2In;
		}
		public void setWb2In(long wb2In) {
			this.wb2In = wb2In;
		}
		public long getWb3In() {
			return wb3In;
		}
		public void setWb3In(long wb3In) {
			this.wb3In = wb3In;
		}
		public long getWb1Out() {
			return wb1Out;
		}
		public void setWb1Out(long wb1Out) {
			this.wb1Out = wb1Out;
		}
		public long getWb2Out() {
			return wb2Out;
		}
		public void setWb2Out(long wb2Out) {
			this.wb2Out = wb2Out;
		}
		public long getWb3Out() {
			return wb3Out;
		}
		public void setWb3Out(long wb3Out) {
			this.wb3Out = wb3Out;
		}
		public int getWb1Id() {
			return wb1Id;
		}
		public void setWb1Id(int wb1Id) {
			this.wb1Id = wb1Id;
		}
		public int getWb2Id() {
			return wb2Id;
		}
		public void setWb2Id(int wb2Id) {
			this.wb2Id = wb2Id;
		}
		public int getWb3Id() {
			return wb3Id;
		}
		public void setWb3Id(int wb3Id) {
			this.wb3Id = wb3Id;
		}
    }
	private long gateIn = Misc.getUndefInt();
	private long areaIn = Misc.getUndefInt();
	private long areaOut = Misc.getUndefInt();
	private long gateOut = Misc.getUndefInt();
	private int areaId = Misc.getUndefInt();
	private int tripId = Misc.getUndefInt();
	private int materialId = Misc.getUndefInt();
	private WBInfo wbInfo = null;
	private ArrayList<Integer> alternateMaterialList = null;
	
    
    public void clear() {
    	super.clear();
    	gateIn = Misc.getUndefInt();
    	areaIn = Misc.getUndefInt();
    	areaOut = Misc.getUndefInt();
    	gateOut = Misc.getUndefInt();
    	materialId = Misc.getUndefInt();
    	tripId = Misc.getUndefInt();
    	wbInfo = null;
    }
    
    private static SimpleDateFormat dbgFormatter = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
    private static String dbgFormat(Date dt) {
    	return dt == null ? "null" : dbgFormatter.format(dt);
    }
    public static String dbgFormat(long dt) {
    	Date tempDt = null;
    	if(!Misc.isUndef(dt))
    		tempDt = new Date(dt);
    	return tempDt == null ? "null" : dbgFormatter.format(tempDt);
    }
    private static String getRegionName(int regionId) {
    	RegionTestHelper rt = null;
    	try {
    		rt = RegionTest.getRegionInfo(regionId, null);
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    		//eat it;
    	}
    	return rt == null ? "null" : rt.region.m_name;
    	//RegionTest.g_regionInfos.get(regionId) != null ? RegionTest.g_regionInfos.get(regionId).region.m_name : "(null)";
    }
    public String toString() {
    	synchronized (dbgFormatter) {
    		
    		return 
    		//2014_06_20 		+ "Parkin:"+dbgFormat(parkIn)+"ParkOut:"+dbgFormat(parkOut)    		
    		//2014_06_20 	+"Park:"+getRegionName(parkId)
    		super.toString()
    			+ "Gin:"+dbgFormat(gateIn)
    			+ (wbInfo == null ? "" : wbInfo.toString())
    		+ "Area:"+getRegionName(areaId)+"Ain:"+dbgFormat(areaIn)+"event:"
    		+"Aout:"+dbgFormat(areaOut)
    		+"Gout:"+dbgFormat(gateOut)
    		;
    	}
    }
    
    public boolean equals(LUInfoExtract rhsPassed) {
    	if (rhsPassed == null || !(rhsPassed instanceof ExtLUInfoExtract))
    		return false;
    	ExtLUInfoExtract rhs = (ExtLUInfoExtract) rhsPassed;
    		return  
    	super.equals(rhs) &&
    	((!Misc.isUndef(gateIn) && !Misc.isUndef(rhs.gateIn) && gateIn == rhs.gateIn) || (Misc.isUndef(gateIn) && Misc.isUndef(rhs.gateIn))) &&
    	((!Misc.isUndef(areaIn) && !Misc.isUndef(rhs.areaIn) && areaIn == rhs.areaIn) || (Misc.isUndef(areaIn) && Misc.isUndef(rhs.areaIn))) &&
    	((!Misc.isUndef(areaOut) && !Misc.isUndef(rhs.areaOut) && areaOut == rhs.areaOut) || (Misc.isUndef(areaOut) && Misc.isUndef(rhs.areaOut))) &&
    	((!Misc.isUndef(gateOut) && !Misc.isUndef(rhs.gateOut) && gateOut == rhs.gateOut) || (Misc.isUndef(gateOut) && Misc.isUndef(rhs.gateOut))) &&
    	(areaId == rhs.areaId) && 
    	((wbInfo == null && rhs.wbInfo == null) || (wbInfo != null && rhs.wbInfo != null && wbInfo.equals(rhs.wbInfo)))
    	
    	;    	
    }

    
    public void copy(LUInfoExtract rhsPassed) {
    	if (rhsPassed == null)
    		return;
    	//super.copy(rhsPassed);
    	if (rhsPassed instanceof ExtLUInfoExtract) {
    		ExtLUInfoExtract rhs = (ExtLUInfoExtract) rhsPassed;
	    	gateIn = rhs.gateIn;
	    	areaIn = rhs.areaIn;
	    	areaOut = rhs.areaOut;
	    	gateOut = rhs.gateOut;
	    	areaId = rhs.areaId;
	    	tripId = rhs.tripId;
	    	materialId = rhs.materialId;
	    	if (rhs.wbInfo == null)
	    		this.wbInfo = null;
	    	else {
	    		if (this.wbInfo == null)
	    			this.wbInfo = new WBInfo();
	    		this.wbInfo.copy(rhs.wbInfo);
	    	}    	
    	}
    	else {
    		this.clear();
    		//super.copy(rhsPassed);
    	}
    	super.copy(rhsPassed);
    	
    }
    
    public String getState(){
    	if(!Misc.isUndef(getWaitOut())){
    		return "waitOut";
    	}else if(!Misc.isUndef(gateOut)){
    		return "gateOut";
    	}else if(!Misc.isUndef(areaOut)){
    		return "areaOut";
    	}else if(!Misc.isUndef(areaIn)){
    		return "areaIn";
    	}else if(!Misc.isUndef(gateIn)){
    		return "gateIn";
    	}else if(!Misc.isUndef(getWaitIn())){
    		return "waitIn";
    	}
	    else if (!Misc.isUndef(getWb1In())){
			return "WB1In";
		}
	    else if (!Misc.isUndef(getWb2In())){
			return "WB2In";
		}
	    else if (!Misc.isUndef(getWb3In())){
			return "WB3In";
		}
    	else {
    		return "";
    	}
    }
    public long getLatestEventDateTime(){
    	if(!Misc.isUndef(getWaitOut())){
    		return getWaitOut();
    	}else if(!Misc.isUndef(gateOut)){
    		return gateOut;
    	}
    	else if (!Misc.isUndef(getWb3Out())){
    		return getWb3Out();
    	}
    	else if (!Misc.isUndef(getWb3In())){
    		return getWb3In();
    	}
    	else if (!Misc.isUndef(getWb2Out())){
    		return getWb2Out();
    	}
    	else if (!Misc.isUndef(getWb2In())){
    		return getWb2In();
    	}
    	else if(!Misc.isUndef(areaOut)){
    		return areaOut;
    	}
    	else if(!Misc.isUndef(areaIn)){
    		return areaIn;
    	}
    	else if (!Misc.isUndef(getWb1Out())){
    		return getWb1Out();
    	}
    	else if (!Misc.isUndef(getWb1In())){
    		return getWb1In();
    	}
    	else if(!Misc.isUndef(gateIn)){
    		return gateIn;
    	}
    	else if(!Misc.isUndef(getWaitIn())){
    		return getWaitIn();
    	}else {
    		return Misc.getUndefInt();
    	}
    }
    public long getEarliestEventDateTime(){
    	if(!Misc.isUndef(getWaitIn())){
    		return getWaitIn();
    	}else if(!Misc.isUndef(gateIn)){
    		return gateIn;
    	}
    	else if (!Misc.isUndef(getWb1In())){
        		return getWb1In();
    	}
    	else if (!Misc.isUndef(getWb1Out())){
    		return getWb1Out();
    	}
    	else if(!Misc.isUndef(areaIn)){
    		return areaIn;
    	}
    	else if(!Misc.isUndef(areaOut)){
    		return areaOut;
    	} 
    	else if (!Misc.isUndef(getWb2In())){
    		return getWb2In();
    	}
    	else if (!Misc.isUndef(getWb2Out())){
    		return getWb2Out();
    	}
    	else if (!Misc.isUndef(getWb3In())){
    		return getWb3In();
    	}
    	else if (!Misc.isUndef(getWb3Out())){
    		return getWb3Out();
    	}

    	else if(!Misc.isUndef(gateOut)){
    		return gateOut;
    	}
    	else if(!Misc.isUndef(getWaitOut())){
    		return getWaitOut();
    	}else {
    		return Misc.getUndefInt();
    	}
    }
	public boolean isCalcComplete() {
		return true;
	}
	public long getGateIn() {
		return gateIn;
	}
	public void setGateIn(long gateIn) {
		this.gateIn = gateIn;
	}
	public long getAreaIn() {
		return areaIn;
	}
	public void setAreaIn(long areaIn) {
		this.areaIn = areaIn;
	}
	public long getAreaOut() {
		return areaOut;
	}
	public void setAreaOut(long areaOut) {
		this.areaOut = areaOut;
	}
	public long getGateOut() {
		return gateOut;
	}
	public void setGateOut(long gateOut) {
		this.gateOut = gateOut;
	}
	public int getAreaId() {
		return areaId;
	}
	public void setAreaId(int areaId) {
		this.areaId = areaId;
	}
	public int getTripId() {
		return tripId;
	}
	public void setTripId(int tripId) {
		this.tripId = tripId;
	}
	public int getMaterialId() {
		return materialId;
	}
	public void setMaterialId(int materialId) {
		this.materialId = materialId;
	}
	public long getWb1In() {
		return wbInfo == null ? Misc.getUndefInt() : wbInfo.getWb1In();
	}
	public void setWb1In(long wb1In) {
		if (wb1In > 0) {
			if (wbInfo == null )
				wbInfo = new WBInfo();
			wbInfo.setWb1In(wb1In);
		}
	}
	public long getWb2In() {
		return wbInfo == null ? Misc.getUndefInt() : wbInfo.getWb2In();
	}
	public void setWb2In(long wb2In) {
		if (wb2In > 0) {
			if (wbInfo == null )
				wbInfo = new WBInfo();
			wbInfo.setWb2In(wb2In);
		}
	}
	public long getWb3In() {
		return wbInfo == null ? Misc.getUndefInt() : wbInfo.getWb3In();
	}
	public void setWb3In(long wb3In) {
		if (wb3In > 0) {
			if (wbInfo == null )
				wbInfo = new WBInfo();
			wbInfo.setWb3In(wb3In);
		}
	}
	public int getWb1Id() {
		return wbInfo == null ? Misc.getUndefInt() : wbInfo.getWb1Id();
	}
	public void setWb1Id(int wb1Id) {
		if (wb1Id > 0) {
			if (wbInfo == null )
				wbInfo = new WBInfo();
			wbInfo.setWb1Id(wb1Id);
		}
	}
	public int getWb2Id() {
		return wbInfo == null ? Misc.getUndefInt() : wbInfo.getWb2Id();
	}
	public void setWb2Id(int wb2Id) {
		if (wb2Id > 0) {
			if (wbInfo == null )
				wbInfo = new WBInfo();
			wbInfo.setWb2Id(wb2Id);
		}
	}
	public int getWb3Id() {
		return wbInfo == null ? Misc.getUndefInt() : wbInfo.getWb3Id();
	}
	public void setWb3Id(int wb3Id) {
		if (wb3Id > 0) {
			if (wbInfo == null )
				wbInfo = new WBInfo();
			wbInfo.setWb3Id(wb3Id);
		}
	}
	public long getWb1Out() {
		return wbInfo == null ? Misc.getUndefInt() : wbInfo.getWb1Out();
	}
	public void setWb1Out(long wb1Out) {
		if (wb1Out > 0) {
			if (wbInfo == null )
				wbInfo = new WBInfo();
			wbInfo.setWb1Out(wb1Out);
		}
	}
	public long getWb2Out() {
		return wbInfo == null ? Misc.getUndefInt() : wbInfo.getWb2Out();
	}
	public void setWb2Out(long wb2Out) {
		if (wb2Out > 0) {
			if (wbInfo == null )
				wbInfo = new WBInfo();
			wbInfo.setWb2Out(wb2Out);
		}
	}
	public long getWb3Out() {
		return wbInfo == null ? Misc.getUndefInt() : wbInfo.getWb3Out();
	}
	public void setWb3Out(long wb3Out) {
		if (wb3Out > 0) {
			if (wbInfo == null )
				wbInfo = new WBInfo();
			wbInfo.setWb3Out(wb3Out);
		}
	}


	public WBInfo getWbInfo() {
		return wbInfo;
	}
	public void setWbInfo(WBInfo wbInfo) {
		this.wbInfo = wbInfo;
	}
	public ArrayList<Integer> getAlternateMaterialList() {
		return alternateMaterialList;
	}
	public void setAlternateMaterialList(ArrayList<Integer> alternateMaterialList) {
		this.alternateMaterialList = alternateMaterialList;
	}
}
