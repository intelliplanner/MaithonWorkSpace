package com.ipssi.morphoTest;

import java.sql.PreparedStatement;
import java.util.ArrayList;

import morpho.morphosmart.sdk.api.MorphoSmartSDK;

import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.Pair;
import com.ipssi.rfid.processor.Utils;
import com.mysql.jdbc.Connection;
import com.scl.loadlibrary.DriverBean;
import com.scl.loadlibrary.MorphoSmartFunctions;

public class MorphoInstrument {
	String morphoId;
	ArrayList<MorphoBean> morphoDb;

	public static void enrollUser(Connection conn, String userId,
			byte[] template1, byte[] template2) throws Exception {
		System.out.println("############ Enroll User Into DB ###########");
		PreparedStatement ps = null;
		try {
			ps = conn
					.prepareStatement("Insert into morpho_db_test(driver_id,template1,template2) values (?, ?, ?)");
			Misc.setParamInt(ps, userId, 1);
			ps.setObject(2, template1);
			ps.setObject(3, template2);
			ps.executeUpdate();
		} catch (Exception ex) {
			throw ex;
		} finally {
			Misc.closePS(ps);
		}
	}

	public static void identifyUser() throws Exception {

	}

	public static void deleteUser(Connection conn, int driverId)
			throws Exception {
		PreparedStatement ps = null;
		try {
			ps = conn.prepareStatement("Delete from morpho_db_test where driver_id=?");
			Misc.setParamInt(ps, driverId, 1);
			ps.execute();
		} catch (Exception ex) {
			throw ex;
		} finally {
			Misc.closePS(ps);
		}
	}

	public static void removeAllUser(Connection conn) throws Exception {
		PreparedStatement ps = null;
		try {
			ps = conn.prepareStatement("Delete from morpho_db_test");
			ps.execute();
		} catch (Exception ex) {
			throw ex;
		} finally {
			Misc.closePS(ps);
		}
	}

}
