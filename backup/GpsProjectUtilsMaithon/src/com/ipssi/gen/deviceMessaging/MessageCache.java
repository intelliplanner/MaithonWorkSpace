package com.ipssi.gen.deviceMessaging;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

import com.ipssi.gen.utils.CacheTrack;
import com.ipssi.gen.utils.DBConnectionPool;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.Pair;
import com.ipssi.gen.utils.Triple;

public class MessageCache {
	
	private static ConcurrentHashMap<Integer, ArrayList<Message>> cache = new ConcurrentHashMap<Integer, ArrayList<Message>>();
	private volatile static boolean g_loadDone = false;
	
	public static Message addMessage(String vehicle, String msg) throws Exception {
		vehicle = vehicle == null ? null : vehicle.replaceAll("[^A-Za-z0-9_]", "").toUpperCase();
		if (vehicle == null || msg == null)
			return null;
		Connection conn = null;
		boolean destroyIt = false;
		Message retval = null;
		try {
			conn = DBConnectionPool.getConnectionFromPoolNonWeb();
			PreparedStatement ps = conn.prepareStatement("select id from vehicle where status=1 and std_name=?");
			ps.setString(1, vehicle);
			ResultSet rs = ps.executeQuery();
			int vehicleId = Misc.getUndefInt();
			if (rs.next()) {
				vehicleId = rs.getInt(1);
			}
			rs.close();
			ps.close();
			retval = addMessage(conn, vehicleId, msg, 0, true, CacheTrack.VehicleSetup.getSetup(vehicleId, conn));
		}
		catch (Exception e) {
			e.printStackTrace();
			destroyIt = true;
			throw e;
		}
		finally {
			if (conn != null)
				DBConnectionPool.returnConnectionToPoolNonWeb(conn,destroyIt);
			retval = null;
		}
		return retval;
	}
	public static Message addMessage(Connection conn, int vehicleId, String msg, int deliveryMode, boolean addToDB, CacheTrack.VehicleSetup vehSetup) throws Exception {
		try {
			//will remove previously SENT but not acknowledged messages as UNACKNOWLEDGED ..
			int todeviceMessageRemoval = vehSetup == null || vehSetup.getDistCalcControl(conn) == null ? 0 : vehSetup.getDistCalcControl(conn).m_todeviceMessageRemoval;
			if (vehSetup == null)
				return null;
			init(conn, null); //incase not initialized then initialize
			if (todeviceMessageRemoval == 1) {
				removeUnsentMessages(conn, vehicleId, null, null);
			}
			Message message = new Message(msg);
			message.setDeliveryMode(deliveryMode);
			if (addToDB) {
				PreparedStatement ps = conn.prepareStatement("insert into vehicle_messages (vehicle_id, message, in_date, status, delivery_mode) values (?,?,?,?,?)");
				ps.setInt(1, vehicleId);
				ps.setString(2, message.getMessage());
				ps.setTimestamp(3, Misc.utilToSqlDate(message.getInDate()));
				ps.setInt(4, message.getStatus().value());
				ps.setInt(5, message.getDeliveryMode());
				ps.executeUpdate();
				ResultSet rs = ps.getGeneratedKeys();
				if (rs.next()) {
					message.setId(rs.getInt(1));
				}
				rs.close();
				ps.close();
			}
			Integer vehIdInt = new Integer(vehicleId);
			ArrayList<Message> msgList = cache.get(vehIdInt);
			if (msgList == null) {
				msgList = new ArrayList<Message>();
				cache.put(vehIdInt, msgList);
			}
			synchronized (msgList) {
				msgList.add(message);
				if (todeviceMessageRemoval == 1) {
					Triple<Boolean, Integer, Message> latest = MessageCache.getLatestSentMessage(msgList);
					if (latest != null && latest.second >= 0)
						MessageCache.removeUnackMessages(conn, vehicleId, latest.third.getLatestTryDate(), null);
				}
			}
			return message;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	public static ArrayList<Message> getMessagesToSend(int vehicleId, Connection conn) throws Exception {
	//	init(conn, null); ... dont load from messageList
		ArrayList<Message> entries = cache.get(vehicleId);
		if (entries == null)
			return null;
		synchronized (entries) {
			if (entries.size() == 0)
				return null;
			ArrayList<Message> retval = new ArrayList<Message>();
			for (Message entry : entries) {
				if (entry.getStatus() == MessageStatus.CREATED)
				retval.add(entry);
			}
			return retval;
		}
	}
	
	public static void updateMessageSentStatus(int vehicleId, Connection conn, ArrayList<Message> sentList) throws Exception {//we are passing the Message Object ... so can just iterate on sentList
		try {
			if (sentList == null || sentList.size() == 0)
				return;
			PreparedStatement ps = conn.prepareStatement("update vehicle_messages set status=1, latest_try_date=? where id=?");
			java.sql.Timestamp now = new java.sql.Timestamp(System.currentTimeMillis());
			for (Message entry:sentList) {
				if (entry.getStatus() == MessageStatus.CREATED || entry.getStatus() == MessageStatus.UNACKNOWLEDGED) {
					entry.setStatus(MessageStatus.SENT);
				}
				ps.setTimestamp(1, entry.getLatestTryDate() == null ? now : Misc.utilToSqlDate(entry.getLatestTryDate()));
				ps.setInt(2, entry.getId());
				ps.addBatch();
			}
			ps.executeBatch();
			ps.close();
			CacheTrack.VehicleSetup vehicleInfo = CacheTrack.VehicleSetup.getSetup(vehicleId, conn);
			int todeviceMessageRemoval = vehicleInfo == null || vehicleInfo.getDistCalcControl(conn) == null ? 0 : vehicleInfo.getDistCalcControl(conn).m_todeviceMessageRemoval;
			if (todeviceMessageRemoval == 0) {
				//remove from list
				ArrayList<Message> msgList = cache.get(vehicleId);
				for (int i=msgList == null ? -1 : msgList.size()-1; i>=0;i--) {
					Message entry = msgList.get(i);
					if (entry.getStatus() == MessageStatus.SENT) {
						msgList.remove(i);
					}
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	public static void updateAcknowledgeMessage(int vehicleId, Connection conn, String message, long atTime, double longitude, double latitude, long recvTime, String posnName) throws Exception {//we are passing the Message Object ... so can just iterate on sentList
		CacheTrack.VehicleSetup vehicleInfo = CacheTrack.VehicleSetup.getSetup(vehicleId, conn);
		int todeviceMessageRemoval = vehicleInfo == null || vehicleInfo.getDistCalcControl(conn) == null ? 0 : vehicleInfo.getDistCalcControl(conn).m_todeviceMessageRemoval;
		if (todeviceMessageRemoval == 0) {
			return;
		}
		//1. will find the latest message sent just before atTime and update the status of that & remove it from list and save to DB and also remove all sent messages before this
		ArrayList<Message> entries = cache.get(vehicleId);
		if (entries == null)
			return;
		synchronized (entries) { 
			Triple<Boolean, Integer, Message> latest = getLatestSentMessage(entries);
	        if (latest == null || latest.second < 0)
	        	return;
	        long now = new Date().getTime();
	        if (Misc.isUndef(atTime))
	        	atTime = now;
	        if (Misc.isUndef(recvTime)) {
	        	recvTime = now;
	        }
	        java.sql.Timestamp ts = Misc.longToSqlDate(atTime);
	        java.sql.Timestamp tsrecv = Misc.longToSqlDate(recvTime);
	        if (posnName == null)
	        	posnName = "Unknown";
        	entries.remove(latest.second.intValue());
        	PreparedStatement ps = conn.prepareStatement("update vehicle_messages set status = ?, acknowledge_date = ?, acknowledge_message=?, longitude=?, latitude=?, receive_time=?, posn_name=? where id=?");
        	ps.setInt(1, MessageStatus.ACKNOWLEDGED.value());
        	ps.setTimestamp(2, ts);
        	ps.setString(3, message);
        	ps.setDouble(4, longitude);
        	ps.setDouble(5, latitude);
        	ps.setTimestamp(6, tsrecv);
        	ps.setString(7, posnName);
        	ps.setInt(8, latest.third.getId());
        	
        	ps.execute();
        	ps.close();
	        if (latest.first) {
	        	removeUnackMessages(conn, vehicleId, latest.third.getLatestTryDate(), ts);
	        }
		}//end of sync block
	}

	public static void saveMessage(int vehicleId, Connection conn, String message, long atTime, double longitude, double latitude, long recvTime, String posnName) throws Exception {//we are passing the Message Object ... so can just iterate on sentList
        long now = new Date().getTime();
        if (Misc.isUndef(atTime))
        	atTime = now;
        if (Misc.isUndef(recvTime)) {
        	recvTime = now;
        }
        java.sql.Timestamp ts = Misc.longToSqlDate(atTime);
        java.sql.Timestamp tsrecv = Misc.longToSqlDate(recvTime);
        if (posnName == null)
        	posnName = "Unknown";
    	PreparedStatement ps = conn.prepareStatement("insert into vehicle_recvd_messages (vehicle_id, record_time, message, longitude, latitude, receive_time, posn_name) values (?,?,?,?,?,?,?) ");
    	ps.setInt(1,vehicleId);
    	ps.setTimestamp(2, ts);
    	ps.setString(3, message);
    	ps.setDouble(4, longitude);
    	ps.setDouble(5, latitude);
    	ps.setTimestamp(6, tsrecv);
    	ps.setString(7, posnName);
    	ps.execute();
    	ps.close();
	}
	
	private static Triple<Boolean, Integer, Message> getLatestSentMessage(ArrayList<Message> entries) {
		if (entries == null)
			return null;
		Message latestEntry = null;
		int index = -1;
		boolean toremoveOlder = false;
        for (int i=entries.size()-1;i>=0;i--) {
        	Message entry = entries.get(i);
        	if (entry.getStatus() == MessageStatus.SENT) {
        		if (latestEntry == null) {
        			latestEntry = entry;
        			index = i;
        		}
        		else {
        			toremoveOlder = true;
        			if (latestEntry.getLatestTryDate() != null && entry.getLatestTryDate() != null && entry.getLatestTryDate().after(latestEntry.getLatestTryDate())) {
        				index = i;
        				latestEntry = entry;
        			}
        		}
        	}
        }
        return new Triple<Boolean, Integer, Message>(toremoveOlder, index, latestEntry);
	}
	
	private static void removeUnsentMessages(Connection conn, int vehicleId, Date beforeThis,  java.sql.Timestamp now) throws Exception {
		try {
    		ArrayList<Message> entries = cache.get(vehicleId);
    		if (entries == null)
    			return;
    		if (now == null)
    			now = new java.sql.Timestamp(System.currentTimeMillis());
    		synchronized (entries) {
	    		PreparedStatement ps = conn.prepareStatement("update vehicle_messages set status = ?, acknowledge_date = ? where id=?");
	    		for (int i=entries.size()-1;i>=0;i--) {
	    			Message entry = entries.get(i);
	    			if ((entry.getStatus() == MessageStatus.CREATED && (beforeThis == null ||  (entry.getInDate() != null && entry.getInDate().before(beforeThis)))) ) {
	    				entries.remove(i);
	    				ps.setInt(1, MessageStatus.UNACKNOWLEDGED.value());
	    				ps.setTimestamp(2, now);
	    				entry.setAcknowledgeDate(now);
	    				ps.setInt(3, entry.getId());
	    				ps.addBatch();
	    			}
	    		}
	    		ps.executeBatch();
	    		ps.close();
    		}//end of sync block
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    		throw e;
    	}
	}
    private static void removeUnackMessages(Connection conn, int vehicleId, Date beforeThis, java.sql.Timestamp now) throws Exception {
    	try {
    		ArrayList<Message> entries = cache.get(vehicleId);
    		if (entries == null)
    			return;
    		if (now == null)
    			now = new java.sql.Timestamp(System.currentTimeMillis());
    		synchronized (entries) {
	    		PreparedStatement ps = conn.prepareStatement("update vehicle_messages set status = ?, acknowledge_date = ? where id=?");
	    		for (int i=entries.size()-1;i>=0;i--) {
	    			Message entry = entries.get(i);
	    			if ((entry.getStatus() == MessageStatus.SENT && (beforeThis == null ||  (entry.getLatestTryDate() != null && entry.getLatestTryDate().before(beforeThis)))) ) {
	    				entries.remove(i);
	    				ps.setInt(1, MessageStatus.UNACKNOWLEDGED.value());
	    				ps.setTimestamp(2, now);
	    				entry.setAcknowledgeDate(now);
	    				ps.setInt(3, entry.getId());
	    				ps.addBatch();
	    			}
	    		}
	    		ps.executeBatch();
	    		ps.close();
    		}//end of sync block
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    		throw e;
    	}
    }

	private static void init(Connection conn, ArrayList<Integer> vehicle) throws Exception {
		if (!g_loadDone) {
			vehicle = null;
		}
		else if (vehicle == null || vehicle.size() == 0) {
			return;
		}
		
		if (vehicle != null) {
			for (Integer v:vehicle) {
				cache.remove(v);
			}
		}
		try {
			StringBuilder query = new StringBuilder("select vehicle_id, vehicle_messages.id, message, in_date, latest_try_date, acknowledge_date, vehicle_messages.status, delivery_mode from vehicle join vehicle_messages on (vehicle.id = vehicle_messages.vehicle_id and vehicle.status in (1)) where  vehicle_messages.status in (0,1) ");
			if (vehicle != null && vehicle.size() != 0) {
				query.append(" and vehicle_id in (");
				Misc.convertInListToStr(vehicle, query);
				query.append(") ");
			}
			query.append(" order by vehicle_id, status desc, in_date ");
			PreparedStatement ps = conn.prepareStatement(query.toString());
			ResultSet rs = ps.executeQuery();
			int prevVehicleId = Misc.getUndefInt();
			ArrayList<Message> prevMessageList = null;
			int prevStatus = Misc.getUndefInt();
			while (rs.next()) {
				int vehicleId = rs.getInt(1);
				if (vehicleId != prevVehicleId) {
					prevMessageList = new ArrayList<Message>();
					cache.put(vehicleId, prevMessageList);
					prevStatus = Misc.getUndefInt();
				}
				int id = rs.getInt(2);
				String msg = rs.getString(2);
				Date inDate = Misc.sqlToUtilDate(rs.getTimestamp(4));
				Date latestTryDate = Misc.sqlToUtilDate(rs.getTimestamp(5));
				Date ackDate = Misc.sqlToUtilDate(rs.getTimestamp(6));
				int status = rs.getInt(7); 
				int deliveryMode = rs.getInt(8);
				Message message = new Message(msg,id, inDate, latestTryDate, ackDate, status, deliveryMode, null);
				synchronized (prevMessageList) {	
					prevMessageList.add(message);
				}
				prevVehicleId = vehicleId;
				prevStatus = status;
			}
			rs.close();
			ps.close();
			g_loadDone = true;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	public static void main(String[] args) {
		int vehicleId = 15420;
		Connection conn = null;
		boolean destroyIt = false;
		try {
			conn = DBConnectionPool.getConnectionFromPoolNonWeb();
			conn.setAutoCommit(true); // for debug
			CacheTrack.VehicleSetup vehSetup = CacheTrack.VehicleSetup.getSetup(vehicleId, conn);
			MessageCache.addMessage(conn, vehicleId, "Test Message 1", 0, true, vehSetup);
			
			ArrayList<Message> tosend = MessageCache.getMessagesToSend(vehicleId, conn);
			for (Message mess:tosend) {
				mess.setStatus(MessageStatus.SENT);
				mess.setLatestTryDate(new Date());
			}
			MessageCache.updateMessageSentStatus(vehicleId, conn, tosend);
			MessageCache.updateAcknowledgeMessage(vehicleId, conn, "resp", Misc.getUndefInt(), 0, 0, Misc.getUndefInt(), null);
			MessageCache.saveMessage(vehicleId, conn, "resp", Misc.getUndefInt(), 0, 0, Misc.getUndefInt(), null);
			MessageCache.addMessage(conn, vehicleId, "Test Message 2", 0, true, vehSetup);
			tosend = MessageCache.getMessagesToSend(vehicleId, conn);
			for (Message mess:tosend) {
				mess.setStatus(MessageStatus.SENT);
				mess.setLatestTryDate(new Date());
			}
			MessageCache.updateMessageSentStatus(vehicleId, conn, tosend);
			MessageCache.updateAcknowledgeMessage(vehicleId, conn, "resp", Misc.getUndefInt(), 0, 0, Misc.getUndefInt(), null);
			MessageCache.saveMessage(vehicleId, conn, "resp", Misc.getUndefInt(), 0, 0, Misc.getUndefInt(), null);
			
			
		} catch (Exception e) {
			e.printStackTrace();
			destroyIt = true;
			;
			// eat it
		} finally {
			try {
				if (conn.getAutoCommit()) { // mysql issue cant commit/rollback if autocommint
					conn.setAutoCommit(false);
				}
				DBConnectionPool.returnConnectionToPoolNonWeb(conn, destroyIt);
			} catch (Exception e) {
				e.printStackTrace();
				// eat it
			}
		}	
	}
}
