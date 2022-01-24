package com.scl.loadlibrary;

import java.sql.Connection;

import com.ipssi.gen.utils.DBConnectionPool;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.Pair;
import com.ipssi.rfid.beans.Biometric;
import com.ipssi.rfid.constant.Status;
import com.ipssi.rfid.database.GateInDao;
import com.ipssi.rfid.integration.WaveFormPlayer;
import com.ipssi.rfid.processor.TokenManager;
import com.ipssi.rfid.ui.CoalGateInWindow;
import javax.swing.Icon;

public class FingurePrintService implements Runnable {
    
    Thread mThread = null;
    int refrehTime;
    private Object obj = new Object();
    private int readerId = 0;
    private FingurePrintHandler handler = null;
    private boolean isRunning = false;
//    private Connection conn = null;
    private int workStationType;
    private int workStationTypeId;
    private int userId;
    MorphoActionI morphoActionI = null;
    private  int fingerVoice = Status.TPRQuestion.startFingerCapturing;
    public FingurePrintService(int refrehTime, int workStationType, int workStationTypeId, int userId) {
        this.refrehTime = refrehTime;
//        this.conn = conn;
        this.workStationType = workStationType;
        this.workStationTypeId = workStationTypeId;
        this.userId = userId;
    }
    
    public void setListener(FingurePrintHandler handler,MorphoActionI morphoActionI) {
        this.handler = handler;
        this.morphoActionI = morphoActionI;
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
        synchronized (obj) {
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
    LoadLibrary auth = null;
    
    @Override
    public void run() {
        if(TokenManager.useSDK())
    	try {
    		if(TokenManager.useSDK())
    			identifySDK();
    		else
    			identifyOld();
    		
    	} catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (handler != null) {
                handler.statusChange(false);
            }
        }
    }
    
    private void identifySDK(){

        int retry = 0;
        while (isRunning) {
        	boolean destroyIt = false;
        	Connection conn = null;
        	try {
        		conn = DBConnectionPool.getConnectionFromPoolNonWeb();
        		boolean isFingerVerified = false;
        		boolean isFingerCaptured = false;
        		if (handler != null) {
        			handler.statusChange(true);
        		}
        		boolean deviceConnected = MorphoSmartFunctions.getMorpho().isConnected();
        		System.out.println("deviceConnected" + deviceConnected);
        		if (!deviceConnected) {
        			System.out.println("Device is not Connected, Please Restart Application and Unplugged Device !!!");
        			isRunning = false;
        			break;
        		}
        		MorphoSmartFunctions morpho = null;
        		synchronized (MorphoSmartFunctions.lock) {
        			morpho = MorphoSmartFunctions.getMorpho();
        			if(retry == 0){
        				WaveFormPlayer.playSoundIn(fingerVoice);
        			}
        			int enrollmentId = Misc.getUndefInt();
        			Pair<Integer, Integer> identify = morpho.identify(morphoActionI);
        			enrollmentId = identify.first;
        			int userId = identify.second;System.out.println("userid : " + userId + " enrollmentId : " + enrollmentId);
        			if (enrollmentId == 0 && !Misc.isUndef(userId)) {//finger Exist
        				Biometric driverInformation = GateInDao.getDriverDetail(conn, userId+"");
        				if (driverInformation != null) {
        					isFingerCaptured = true;
        					isFingerVerified = true;
        					if (handler != null) {
        						handler.onChange(conn, driverInformation, isFingerVerified, isFingerCaptured);
        					}
        					WaveFormPlayer.playSoundIn(Status.TPRQuestion.thankYou);
        					isRunning = false;
        					/*if (driverInformation.getIsFingerCaptured() == 1) {
                                isFingerCaptured = true;
                            } else {
                                isFingerCaptured = false;
                            }
                            if (handler != null) {
                                handler.onChange(conn, driverInformation, isFingerVerified, isFingerCaptured);
                            }
                            isRunning = false;*/
        					break;
        				} else {
        					if (retry < 2) {
        						WaveFormPlayer.playSoundIn(Status.TPRQuestion.tryAgainFinger);
        					}
        					retry++;
        				}
        			} else if (enrollmentId == -8) {// finger print not exist
        				if (retry < 2) {
        					WaveFormPlayer.playSoundIn(Status.TPRQuestion.tryAgainFinger);
        				}
        				retry++;
        			} else if (enrollmentId == -11) {
        				WaveFormPlayer.playSoundIn(Status.TPRQuestion.fingerNotMatch);
        				CoalGateInWindow.driverId.setEditable(true);
        				CoalGateInWindow.driverId.setFocusable(true);
        				//                    driverId.requestFocusInWindow();
        				CoalGateInWindow.driverId.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        				CoalGateInWindow.dlNo.setEditable(true);
        				CoalGateInWindow.dlNo.setFocusable(true);
        				//                    CoalGateInWindow.dlNo.requestFocusInWindow();
        				CoalGateInWindow.dlNo.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        				isRunning = false;
        				break; 
        			} else if (enrollmentId == -19) {
        			} else {
        				String exceptionMsg = BioMatricException.getException(enrollmentId);
        				System.out.println(" \t\n Error_Code : " + enrollmentId + " Exception : " + exceptionMsg);
        				isRunning = false;
        				break;

        			}
        			if (retry > 2) {//save current template for debug
        				byte[] template = null;
        				int count = 0;
        				while(count < 2){
        					template = morpho.getTemplate();
        					if(template != null){
        						//insert into database
        						if(handler != null)
        							handler.setDebugTemplate(template);
        						break;
        					}
        				}
        				//Pair<Integer, Integer> capture = morpho.capture(morphoActionI);
        			}
        		}
        	} catch (Exception ex) {
            	destroyIt = true;
                ex.printStackTrace();
            } finally {
                if (retry > 2) {
                    if (handler != null) {
                    	handler.showMessage("No Match Found");
                    }
                    isRunning = false;
                    break;
                }
                try{
                	DBConnectionPool.returnConnectionToPoolNonWeb(conn, destroyIt);
                }catch(Exception ex){
                	ex.printStackTrace();
                }
            }
        }
    
    }
    
    private void identifyOld(){
        int retry = 0;
        while (isRunning) {
        	boolean destroyIt = false;
        	Connection conn = null;
            try {
            	conn = DBConnectionPool.getConnectionFromPoolNonWeb();
            	boolean isFingerVerified = false;
            	boolean isFingerCaptured = false;
                synchronized (LoadLibrary.lock) {
                    if (handler != null) {
                        handler.statusChange(true);
                    }
                    if (auth == null || !auth.isDeviceConnected()) {
                        auth = LoadLibrary.getBiometricDevice();
                    }
                    boolean deviceConnected = auth.isDeviceConnected();
                    System.out.println("deviceConnected" + deviceConnected);
                    if (!deviceConnected) {
                        System.out.println("Device is not Connected, Please Restart Application and Unplugged Device !!!");
                        isRunning = false;
                        break;
                    }
                    if(retry == 0){
                    WaveFormPlayer.playSoundIn(fingerVoice);
                    }
                    int enrollmentId = Misc.getUndefInt();
                    String userId = "";
                    // play voice
                    enrollmentId = auth.identify(20);
                    userId = auth.getUserId();
//                    byte[] getCaptureImage = auth.getCapturedImage();
//                    CoalGateInWindow.fingerImageLabel.setIcon((Icon) (FingerPrintAction.getImage(getCaptureImage) == null ? "" : FingerPrintAction.getImage(getCaptureImage)));
                    System.out.println("userid : " + userId + " enrollmentId : " + enrollmentId);
                    if (enrollmentId == 0) {//finger Exist
                        String splitId[] = userId.split("_");
                        isFingerVerified = true;
                        
                        Biometric driverInformation = GateInDao.getDriverDetail(conn, splitId[0]);
                        if (driverInformation != null) {
                            WaveFormPlayer.playSoundIn(Status.TPRQuestion.thankYou);
                            if (driverInformation.getIsFingerCaptured() == 1) {
                                isFingerCaptured = true;
                            } else {
                                isFingerCaptured = false;
                            }
                            if (handler != null) {
                                handler.onChange(conn, driverInformation, isFingerVerified, isFingerCaptured);
                            }
                            isRunning = false;
                            break;
                        } else {
                            if (retry < 2) {
                                WaveFormPlayer.playSoundIn(Status.TPRQuestion.tryAgainFinger);
                            }
                            retry++;
                        }
                    } else if (enrollmentId == -8) {// finger print not exist
                        if (retry < 2) {
                            WaveFormPlayer.playSoundIn(Status.TPRQuestion.tryAgainFinger);
                        }
                        retry++;
                    } else if (enrollmentId == -11) {
                        WaveFormPlayer.playSoundIn(Status.TPRQuestion.fingerNotMatch);
                        CoalGateInWindow.driverId.setEditable(true);
                        CoalGateInWindow.driverId.setFocusable(true);
//                    driverId.requestFocusInWindow();
                        CoalGateInWindow.driverId.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
                        
                        CoalGateInWindow.dlNo.setEditable(true);
                        CoalGateInWindow.dlNo.setFocusable(true);
//                    CoalGateInWindow.dlNo.requestFocusInWindow();
                        CoalGateInWindow.dlNo.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
                        
                    } else if (enrollmentId == -19) {
                    } else {
                        String exceptionMsg = BioMatricException.getException(enrollmentId);
                        System.out.println(" \t\n Error_Code : " + enrollmentId + " Exception : " + exceptionMsg);
                        isRunning = false;
                        break;
                        
                    }
                }
            } catch (Exception ex) {
            	destroyIt = true;
                ex.printStackTrace();
            } finally {
                if (retry > 2) {
                    if (handler != null) {
                        handler.showMessage("No Match Found");
                    }
                    isRunning = false;
                    break;
                }
                try{
                	DBConnectionPool.returnConnectionToPoolNonWeb(conn, destroyIt);
                }catch(Exception ex){
                	ex.printStackTrace();
                }
            }
        }
    
    }
    
    
    public static void main(String[] args) {
        // TODO Auto-generated method stub
    }
}
