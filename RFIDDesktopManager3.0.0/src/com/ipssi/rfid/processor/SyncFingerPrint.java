/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ipssi.rfid.processor;

import java.sql.Connection;

import com.ipssi.fingerprint.utils.SynServiceHandler;
import com.ipssi.fingerprint.utils.SyncFingerPrintDeviceHelper;
import com.ipssi.gen.utils.DBConnectionPool;

/**
 *
 * @author Vi$ky
 */
public class SyncFingerPrint implements Runnable {

//    private Connection db_conn = null;
    private SynServiceHandler handler = null;
    private boolean isRunning = false;
    private Object lock = new Object();
    Thread mThread = null;
    private long refreshRate = TokenManager.morphoSyncFreq;//10*60*1000;
    
    private boolean singleFullSyncOnly = false;
    public void setSingleFullSyncOnly(boolean singleFullSyncOnly){
    	this.singleFullSyncOnly = singleFullSyncOnly;
    }
    public SyncFingerPrint() {
//        db_conn = conn;
        //startClock();
    }
    public SyncFingerPrint(long refreshRate) {
//        db_conn = conn;
        this.refreshRate = refreshRate;
        //startClock();
    }
    public SyncFingerPrint(long refreshRate,SynServiceHandler handler) {
//        db_conn = conn;
        this.refreshRate = refreshRate;
        this.handler = handler;
        //startClock();
    }
    public void setRefresh(long refreshRate){
    	this.refreshRate = refreshRate;
    }
    public void setHandler(SynServiceHandler handler){
        this.handler = handler;
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
					conn = DBConnectionPool.getConnectionFromPoolNonWeb();
					if(!TokenManager.useSDK())
						SyncFingerPrintDeviceHelper.loadInFingerPrintDevice(conn,handler);
					else
						SyncFingerPrintDeviceHelper.loadInFingerPrintDeviceSDK(conn,false,handler,singleFullSyncOnly);
					if(singleFullSyncOnly){
						System.out.println("Exited after fullSyncOnly");
						break;
					}
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

}
