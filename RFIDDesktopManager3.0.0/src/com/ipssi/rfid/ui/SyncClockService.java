package com.ipssi.rfid.ui;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import com.ipssi.gen.utils.DBConnectionPool;
import com.ipssi.gen.utils.Misc;
import com.ipssi.rfid.processor.TokenManager;
import com.sun.org.apache.bcel.internal.generic.NEW;

public class SyncClockService implements Runnable{

	private boolean isRunning = false;
	private Object lock = new Object();
	Thread mThread = null;
	private long refreshRate = TokenManager.clockSyncFreq;//10*60*1000;
	public SyncClockService() {

	}
	public void start() {
		stop();
		if (mThread == null) {
			mThread = new Thread(this);
			isRunning = true;
			mThread.start();
		} else {
			isRunning = true;
		}

	}

	public void stop() {
		synchronized (lock) {
			try {
				if (mThread != null) {
					//					mThread.stop();
					mThread = null;
				}

				isRunning = false;

			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}
	@Override
	public void run() {
		try{
			while(isRunning){
				Connection conn = null;
				boolean destroyIt = false;
				try{
					syncSystemTime();
					//if(!LoginWindow.isTimeSync)
						
					
						
					
				}catch(Exception ex){
					ex.printStackTrace();
					destroyIt = true;
				}finally{
					try{
						DBConnectionPool.returnConnectionToPoolNonWeb(conn, destroyIt);
						Thread.sleep(refreshRate);
					}catch(Exception ex){
						ex.printStackTrace();
					}
				}
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	private void syncSystemTime() throws Exception{
		PreparedStatement ps = null;
		ResultSet rs = null;
		Connection conn = null;
		String value = null;
		long dateLong = Misc.getUndefInt();
		try{

			conn = DBConnectionPool.getConnectionFromPoolNonWeb();
			SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
			SimpleDateFormat IST  = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
			IST.setTimeZone(TimeZone.getTimeZone("IST"));
			SimpleDateFormat MYSQL  = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			MYSQL.setTimeZone(TimeZone.getTimeZone("IST"));
			SimpleDateFormat DEF =  new SimpleDateFormat(TokenManager.systemDateFormat);//dd-MM-yyyy HH:mm:ss
			DEF.setTimeZone(TimeZone.getDefault());
			String currentDateStr = MYSQL.format(new Date());
			//    		ps = conn.prepareStatement("select now(),DATE_FORMAT(NOW(),'%m-%d-%Y %H:%i:%s')");

			ps = conn.prepareStatement("select now(),DATE_FORMAT(NOW(),'%m-%d-%Y %H:%i:%s') from singleton where timestampdiff(minute,now(),\""+currentDateStr+"\") > 2 or timestampdiff(minute,now(),\""+currentDateStr+"\") < 2");
			rs = ps.executeQuery();
			boolean updateDateTime = false;
			if(rs.next()){
				dateLong = Misc.getDateInLong(rs, 1);
				value = rs.getString(2);//"2014-12-12 00:26:14";
				updateDateTime = true;
			}
			if(!updateDateTime)
				return;
			System.out.println("Date Using Long"+sdf.format(new Date(dateLong)));
			System.out.println("Date Current Date"+sdf.format(new Date(System.currentTimeMillis())));
			Date d = IST.parse(value);
			System.out.println("Server Date Str");
			System.out.println("Date Using IST FORMATER"+IST.format(d));
			System.out.println("Date Using Local FORMATTER"+DEF.format(d));


			/*Locale currentLocale = Locale.getDefault();
    		DateFormat SYSFormatter = DateFormat.getDateTimeInstance(
                    DateFormat.SHORT, 
                    DateFormat.SHORT, 
                    currentLocale);*/

			value = DEF.format(d);
			//value = SYSFormatter.format(d);
			System.out.println("Final Date time short String"+value);
			final Process dateProcess = Runtime.getRuntime().exec("cmd /c date "+value.substring(0, value.lastIndexOf(' ')));
//			dateProcess.waitFor();
//			dateProcess.exitValue();
			final Process timeProcess = Runtime.getRuntime().exec("cmd /c time "+value.substring(value.lastIndexOf(' ')+1));
//			timeProcess.waitFor();
//			timeProcess.exitValue();
			//final Process timeZoneProcess = Runtime.getRuntime().exec("cmd /c tzutil /s \"India Standard Time\"");
//			timeZoneProcess.waitFor();
//			timeZoneProcess.exitValue();
			TimeZone.setDefault(TimeZone.getTimeZone("IST"));

			Calendar now = Calendar.getInstance();
			System.out.println(now.getTimeZone());
			System.out.println(now.getTime());
			LoginWindow.isTimeSync = true;
		}catch(Exception ex){
			ex.printStackTrace();
			LoginWindow.isTimeSync = false;
			throw ex;
		}finally{
			try{
				Misc.closeRS(rs);
				Misc.closePS(ps);
				DBConnectionPool.returnConnectionToPoolNonWeb(conn);
			}catch(Exception ex){
				ex.printStackTrace();
			}
		}
	}

public static void main(String s[]) {
	// TODO Auto-generated method stub
	timeSync();
}

private  static void timeSync(){

	PreparedStatement ps = null;
	ResultSet rs = null;
	Connection conn = null;
	String value = null;
	long dateLong = Misc.getUndefInt();
	try{

		conn = DBConnectionPool.getConnectionFromPoolNonWeb();
		SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
		SimpleDateFormat IST  = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
		IST.setTimeZone(TimeZone.getTimeZone("IST"));
		SimpleDateFormat MYSQL  = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		MYSQL.setTimeZone(TimeZone.getTimeZone("IST"));
		SimpleDateFormat DEF =  new SimpleDateFormat(TokenManager.systemDateFormat);//dd-MM-yyyy HH:mm:ss
		DEF.setTimeZone(TimeZone.getDefault());
		String currentDateStr = MYSQL.format(new Date());
		//    		ps = conn.prepareStatement("select now(),DATE_FORMAT(NOW(),'%m-%d-%Y %H:%i:%s')");

		ps = conn.prepareStatement("select now(),DATE_FORMAT(NOW(),'%m-%d-%Y %H:%i:%s') from singleton where timestampdiff(minute,now(),\""+currentDateStr+"\") > 2 or timestampdiff(minute,now(),\""+currentDateStr+"\") < 2");
		rs = ps.executeQuery();
		boolean updateDateTime = false;
		if(rs.next()){
			dateLong = Misc.getDateInLong(rs, 1);
			value = rs.getString(2);//"2014-12-12 00:26:14";
			updateDateTime = true;
		}
		if(!updateDateTime)
			return;
		System.out.println("Date Using Long"+sdf.format(new Date(dateLong)));
		System.out.println("Date Current Date"+sdf.format(new Date(System.currentTimeMillis())));
		Date d = IST.parse(value);
		System.out.println("Server Date Str");
		System.out.println("Date Using IST FORMATER"+IST.format(d));
		System.out.println("Date Using Local FORMATTER"+DEF.format(d));


		/*Locale currentLocale = Locale.getDefault();
		DateFormat SYSFormatter = DateFormat.getDateTimeInstance(
                DateFormat.SHORT, 
                DateFormat.SHORT, 
                currentLocale);*/

		value = DEF.format(d);
		//value = SYSFormatter.format(d);
		System.out.println("Final Date time short String"+value);
		final Process dateProcess = Runtime.getRuntime().exec("cmd /c date "+value.substring(0, value.lastIndexOf(' ')));
//		dateProcess.waitFor();
//		dateProcess.exitValue();
		final Process timeProcess = Runtime.getRuntime().exec("cmd /c time "+value.substring(value.lastIndexOf(' ')+1));
//		timeProcess.waitFor();
//		timeProcess.exitValue();
		//final Process timeZoneProcess = Runtime.getRuntime().exec("cmd /c tzutil /s \"India Standard Time\"");
//		timeZoneProcess.waitFor();
//		timeZoneProcess.exitValue();
		TimeZone.setDefault(TimeZone.getTimeZone("IST"));

		Calendar now = Calendar.getInstance();
		System.out.println(now.getTimeZone());
		System.out.println(now.getTime());
	}catch(Exception ex){
		ex.printStackTrace();
	}finally{
		try{
			Misc.closeRS(rs);
			Misc.closePS(ps);
			DBConnectionPool.returnConnectionToPoolNonWeb(conn);
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}

	
}

}
