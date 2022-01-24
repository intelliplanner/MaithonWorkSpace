package com.ipssi.common.ds.trip;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.ipssi.RegionTest.RegionTest.RegionTestHelper;
import com.ipssi.gen.utils.Cache;
import com.ipssi.gen.utils.CacheTrack;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.Pair;
import com.ipssi.gen.utils.StopDirControl;
import com.ipssi.geometry.Point;
import com.ipssi.mapguideutils.RTreeSearch;
import com.ipssi.processor.utils.GpsData;
import com.ipssi.common.ds.trip.OpStationBean;
import com.ipssi.common.ds.trip.VehicleControlling;
import com.ipssi.common.ds.trip.TripInfoConstants;

public class ThreadContextCache {
	//Note on usage current: 2013-01-08
	//Calling functions could be getRegionsContaining - this will get list of fixed regions that contain the point
	//                                                getMovingOpStationContaing(opStationBean) - it will get if OpstationBean when centered contains the point
	//                                                getMovingOpStationContaining() .. this willget arraylist of opstationBean when centered containing the point
	// All of these calls will check if cached lon/lat is same as new pt and if so will return previously evaluated result else will reevaluate. Internally the first function fills
	// ArrayList<RegionTestHelper>
	
	// HOWEVER getMovignOpstationContaining behaves a bit differently - if opStationBean is given and if ArrayList<SimpleMoving> has not been fully evaluated, it will evaluate 
	//                   for that opStationBean (center and evaluate) and partially populate ArrayList<SimpleMoving>
	// if no Bean is given then will evaluate for all relevant opStations
	//To help with above bookkeeping - what has been evaluated we keep following flags:
	//searchResultValid - the ArrayList<RegionHelper> has been evaluated for the lon/lat  
	//movingResultValid - the ArrayList<SimpleRegion> has been evaluate for some OpStation for the lon/lat
	//movingResultFullyEvaluated - the ArrayList<SimpleRegion> has been evaluated for ALL OpStation of the lon/lat
	//movingSingleOpStationChecked - the opstationId for which ArrayList<SimpleRegion> been evaluated 
	//when getMovingOpStationContaining(opStationBean) is called the mvoingResultValid is true but the movingSingleOpStationChecked is different from opStationBean
	//then we assume that we have to evaluate for all opstation
	public static class SimpleMoving {
		private OpStationBean opstationBean;
		private Point center;
		public SimpleMoving(OpStationBean opstationBean, Point center) {
			this.opstationBean = opstationBean;
			this.center = center;
		}
		public OpStationBean getOpstationBean() {
			return opstationBean;
		}
		public void setOpstationBean(OpStationBean opstationBean) {
			this.opstationBean = opstationBean;
		}
		public Point getCenter() {
			return center;
		}
		public void setCenter(Point center) {
			this.center = center;
		}
	}	
	private double lon = Misc.getUndefDouble();
	private double lat = Misc.getUndefDouble();
	private ArrayList<RegionTestHelper> searchResult = null;
	private boolean searchResultValid = false; //will be set to true if searchResult has been evaluated at lon/lat 

	
	private ArrayList<SimpleMoving> movingResult = null;
	private boolean movingFullyEvaluated = false;
	private int movingSingleOpStationChecked = Misc.getUndefInt();
	private boolean movingResultValid = false;
	public boolean checkAndResetValidity(Point pt) {
		return checkAndResetValidity(pt.getLongitude(), pt.getLatitude());
	}
	public boolean checkAndResetValidity(double x, double y) {
		if (!Misc.isEqual(x, this.lon) || !Misc.isEqual(y, this.lat)) {
			searchResultValid = false;
			movingResultValid = false;
			movingFullyEvaluated = false;
			searchResult = null;
			movingResult = null;
			movingSingleOpStationChecked = Misc.getUndefInt();
			this.lon = x;
			this.lat = y;
			return false;
		}
		return true;
	}

	public boolean isPointInRegion(Point pt, int regionId) {
		ArrayList<RegionTestHelper> result = getRegionsContaining(pt);
		for (int i=0,is=result == null ? null : result.size();i<is;i++) {
			RegionTestHelper rth  = result.get(i);
			if (rth.region.id  == regionId)
				return true;
		}
		return false;
	}
	public static Pair<Boolean, ArrayList<RegionTestHelper>> isInOpStationAndAllRegIn(Connection conn, double lon, double lat, int vehiclePortNodeId, ArrayList<Integer> optypes, int vehicleId, long dt) throws Exception {
		VehicleControlling vehicleControlling = NewProfileCache.getOrCreateControlling(vehicleId);
		if (optypes == null)
			optypes = new ArrayList<Integer>();
		if (optypes.size() == 0) {
			optypes.add(TripInfoConstants.LOAD);
			optypes.add(TripInfoConstants.UNLOAD);
			optypes.add(TripInfoConstants.HYBRID_ALL);
			optypes.add(TripInfoConstants.HYBRID_LU);
			optypes.add(TripInfoConstants.HYBRID_UL);
			optypes.add(TripInfoConstants.HYBRID_UL_ALWAY);
			optypes.add(TripInfoConstants.HYBRID_NONE);
			optypes.add(TripInfoConstants.PREFERRED_LOAD_LOWPRIORITY);
			optypes.add(TripInfoConstants.PREFERRED_UNLOAD_LOWPRIORITY);
			optypes.add(TripInfoConstants.PREFERRED_LOAD_HIPRIORITY);
			optypes.add(TripInfoConstants.PREFERRED_UNLOAD_HIPRIORITY);
		}
		ArrayList<RegionTestHelper> result = RTreeSearch.getContainingRegions(new Point(lon, lat));
		if (result == null)
			result = new ArrayList<RegionTestHelper>(); //so as to know nothing found

		Cache cache = Cache.getCacheInstance(conn);
		for (int i=0,is=result == null ? null : result.size();i<is;i++) {
			RegionTestHelper rth  = result.get(i);
			ArrayList<Integer> oplist = TripInfoCacheHelper.getOpListForWait(rth.region.id);
			for (int j=0,js = oplist == null ? 0 : oplist.size(); j<js;j++) {
				int opstationId = oplist.get(j);
				OpStationBean opStation = TripInfoCacheHelper.getOpStation(opstationId);
				if (opStation == null)
					continue;
				//check if matching type and then check if belongs from opstation point of view ..
				int opstationType  = opStation.getOpStationType(conn, cache, vehiclePortNodeId, vehicleControlling);
				boolean matchingType = false;
				for (int k=0, ks=optypes.size();k<ks;k++) {
					if (optypes.get(k) == opstationType && (dt <= 0 || opStation.isValidByDate(dt))) {
						return new Pair<Boolean, ArrayList<RegionTestHelper>> (true, result);
					}
				}
			}//for each opstation that are mapped to waitareaid o
		}//for each region that contain the point
		return new Pair<Boolean, ArrayList<RegionTestHelper>> (false, result);
	}
	
	public ArrayList<OpStationBean> getFixedOpstationsContaining(Point pt, ArrayList<Integer> optypes, int vehiclePortNodeId, Connection conn, VehicleControlling vehicleControlling, long dt) throws Exception {	
		ArrayList<RegionTestHelper> result = getRegionsContaining(pt);
		ArrayList<OpStationBean> retval = null;
		Cache cache = Cache.getCacheInstance(conn);
		for (int i=0,is=result == null ? null : result.size();i<is;i++) {
			RegionTestHelper rth  = result.get(i);
			ArrayList<Integer> oplist = TripInfoCacheHelper.getOpListForWait(rth.region.id);
			for (int j=0,js = oplist == null ? 0 : oplist.size(); j<js;j++) {
				int opstationId = oplist.get(j);
				OpStationBean opStation = TripInfoCacheHelper.getOpStation(opstationId);
				if (opStation == null)
					continue;
				//check if matching type and then check if belongs from opstation point of view ..
				int opstationType  = opStation.getOpStationType(conn, cache, vehiclePortNodeId, vehicleControlling);
				boolean matchingType = false;
				for (int k=0, ks=optypes.size();k<ks;k++) {
					if (optypes.get(k) == opstationType) {
						matchingType = true;
						break;
					}
				}
				if (matchingType && dt > 0) 
					matchingType = opStation.isValidByDate(dt);
				if (matchingType) {
					if (retval == null) {
						retval = new ArrayList<OpStationBean>();
					}
					retval.add(opStation);
				}//if type was matching ... 
			}//for each opstation that are mapped to waitareaid o
		}//for each region that contain the point
		return retval;
	}

	public ArrayList<RegionTestHelper> getRegionsContaining(Point pt)  {
		if (!checkAndResetValidity(pt) || !searchResultValid) {
			//do search
			try {
				searchResult = RTreeSearch.getContainingRegions(pt);
				searchResultValid = true;
			}
			catch (Exception e) {
				e.printStackTrace();
				//eat it
			}
			this.lon = pt.getLongitude();
			this.lat = pt.getLatitude();
		}
		return searchResult;
	}
	public OpStationBean getNearestMovingOpStationContaining(Connection conn, int ownerOrgId, GpsData data, VehicleControlling vehicleControlling, CacheTrack.VehicleSetup vehSetup) {
		ArrayList<SimpleMoving> movingList = getMovingOpStationContaining(conn, ownerOrgId, data, vehicleControlling, vehSetup);
		double mindist = 0;
		OpStationBean retval = null;
		for (int i=0,is = movingList == null ? 0 : movingList.size(); i<is;i++) {
			SimpleMoving movingInfo = movingList.get(i);
			OpStationBean bean = movingInfo.getOpstationBean();
			if (bean == null)
				continue;
			double d = movingInfo.center.distance(data.getPoint());
			if (retval == null || d < mindist) {
				retval = bean;
				mindist = d;
			}
		}
		return retval;
	}
	
	public ArrayList<SimpleMoving> getMovingOpStationContaining(Connection conn, int ownerOrgId, GpsData data, VehicleControlling vehicleControlling, CacheTrack.VehicleSetup vehSetup) {
		try {
			
			if (!checkAndResetValidity(data.getPoint()) || !movingResultValid || !movingFullyEvaluated) {
				//HACK currently moving assumed to be of Type LOAD
				Cache cache = Cache.getCacheInstance(conn);
				List<OpStationBean> opslist = TripInfoCacheHelper.getOpStationsForVehicleIgnoreBelonging(conn, ownerOrgId, TripInfoConstants.LOAD, 1, vehicleControlling);
				StopDirControl stopDirControl = vehicleControlling.getStopDirControl(conn, vehSetup);
				ArrayList<Integer> stopRegionList = new ArrayList<Integer>();
				stopRegionList.add(TripInfoConstants.REST_AREA_REGION);
				Collections.sort(opslist);
				for (OpStationBean op : opslist) {
					if (op.getOpStationId() != this.movingSingleOpStationChecked) {
						try {
							op.tryLock();
							GpsData centerPt = op.positionOpStationAtTime(conn, data, true, stopDirControl);
							Point center = centerPt == null ? null : centerPt.getPoint();
							double dbgD = center == null ? Misc.getUndefDouble() : centerPt.distance(data.getLongitude(), data.getLatitude());
							if (center != null && op.isPointLinkedWait(conn, data.getPoint(), center, stopDirControl)) {
								ArrayList<OpStationBean> restOps = this.getFixedOpstationsContaining(center, stopRegionList, ownerOrgId, conn, vehicleControlling, data.getGps_Record_Time());
								if (restOps == null || restOps.size() == 0)
									addMovingResult(op, center);
							}
						}
						catch (Exception e1) {
							//eat it
						}
						finally {
							op.unlock();
						}
					}
				}
				movingFullyEvaluated = true;
				movingResultValid = true;
			}
			return this.movingResult;
		}
		catch (Exception e) {
			
		}
		return null;
	}
	
	public SimpleMoving getMovingOpStationContaining(Connection conn, int ownerOrgId, GpsData data, OpStationBean op, VehicleControlling vehicleControlling, CacheTrack.VehicleSetup vehSetup) {
		try {
			boolean isOldPt = checkAndResetValidity(data.getPoint());
			boolean lookupFromMovingResult = false;
			if ( isOldPt && movingResultValid && movingFullyEvaluated) {
				lookupFromMovingResult = true;
			}
			else if (isOldPt && movingResultValid && op.getOpStationId() == this.movingSingleOpStationChecked) {
				lookupFromMovingResult = true;
			}
			else if (isOldPt && !movingResultValid && Misc.isUndef(this.movingSingleOpStationChecked)) {
				try {
					StopDirControl stopDirControl = vehicleControlling.getStopDirControl(conn, vehSetup);
					op.tryLock();
					
					GpsData centerPt = op.positionOpStationAtTime(conn, data, true, stopDirControl);
					Point center = centerPt == null ? null : centerPt.getPoint();
					if (centerPt != null && op.isPointLinkedWait(conn, data.getPoint(), center, stopDirControl)) {
						addMovingResult(op, center);
					}
				}
				catch (Exception e1) {
					//eat it
				}
				finally {
					op.unlock();
				}
				movingResultValid = true;
				movingSingleOpStationChecked = op.getOpStationId(); 
				lookupFromMovingResult = true;
			}
			if (!lookupFromMovingResult) {
				getMovingOpStationContaining(conn, ownerOrgId, data, vehicleControlling, vehSetup);
			}
			for (int i=0,is=movingResult == null ? 0 : movingResult.size(); i<is; i++) {
				if (movingResult.get(i).getOpstationBean().getOpStationId() == op.getOpStationId())
					return movingResult.get(i);
			}
			return null;
			
		}
		catch (Exception e) {
			
		}
		return null;
	}
	
	public void addMovingResult(OpStationBean bean, Point center) {
		if (movingResult == null)
			movingResult = new ArrayList<SimpleMoving>();
		movingResult.add(new SimpleMoving(bean, center));
	}
}
