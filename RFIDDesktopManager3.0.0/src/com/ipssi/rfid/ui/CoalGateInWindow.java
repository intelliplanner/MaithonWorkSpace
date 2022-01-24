
package com.ipssi.rfid.ui;

import java.awt.Color;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;

import morpho.morphosmart.sdk.api.MorphoImage;
import morpho.morphosmart.sdk.demo.ennum.EnumMoveFinger;
import morpho.morphosmart.sdk.demo.trt.MorphoEventHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.ipssi.common.ds.rule.GpsPlusViolations;
import com.ipssi.common.ds.rule.ResultEnum;
import com.ipssi.fingerprint.utils.SynServiceHandler;
import com.ipssi.gen.exception.GenericException;
import com.ipssi.gen.utils.CacheTrack;
import com.ipssi.gen.utils.DBConnectionPool;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.Pair;
import com.ipssi.gen.utils.Triple;
import com.ipssi.rfid.beans.Biometric;
import com.ipssi.rfid.beans.BlockingInstruction;
import com.ipssi.rfid.beans.RFIDHolder;
import com.ipssi.rfid.beans.TPRecord;
import com.ipssi.rfid.beans.TPSQuestionDetail;
import com.ipssi.rfid.beans.TPStep;
import com.ipssi.rfid.beans.Token;
import com.ipssi.rfid.constant.Constant;
import com.ipssi.rfid.constant.Results;
import com.ipssi.rfid.constant.Status;
import com.ipssi.rfid.constant.UIConstant;
import com.ipssi.rfid.constant.Type.WorkStationType;
import com.ipssi.rfid.database.DropDownValues;
import com.ipssi.rfid.database.GateInDao;
import com.ipssi.rfid.db.RFIDMasterDao;
import com.ipssi.rfid.integration.Barrier;
import com.ipssi.rfid.integration.Clock;
import com.ipssi.rfid.integration.DocPrinter;
import com.ipssi.rfid.integration.WaveFormPlayer;
import com.ipssi.rfid.processor.SyncFingerPrint;
import com.ipssi.rfid.processor.TPRBlockManager;
import com.ipssi.rfid.processor.TPRInformation;
import com.ipssi.rfid.processor.TPRUtils;
import com.ipssi.rfid.processor.TokenManager;
import com.ipssi.rfid.processor.Utils;
import com.ipssi.rfid.readers.RFIDDataHandler;
import com.ipssi.rfid.readers.TAGListener;
import com.ipssi.rfid.ui.AutoCompleteCombo.ComboKeyEvent;
import com.scl.loadlibrary.DriverDetail;
import com.scl.loadlibrary.FingurePrintHandler;
import com.scl.loadlibrary.FingurePrintService;
import com.scl.loadlibrary.LoadLibrary;
import com.scl.loadlibrary.MorphoActionI;
import com.scl.loadlibrary.MorphoSmartFunctions;


public class CoalGateInWindow extends javax.swing.JFrame implements MorphoActionI{

	private static final long serialVersionUID = 1L;
	private ButtonGroup sealOk = new ButtonGroup();
    private ButtonGroup tarpaulin = new ButtonGroup();
    private ButtonGroup numberVisible = new ButtonGroup();
    private ButtonGroup tailLightOk = new ButtonGroup();
    private ButtonGroup sideMirrorOk = new ButtonGroup();
    private ButtonGroup leftSideIndicator = new ButtonGroup();
    private ButtonGroup rightSideIndicator = new ButtonGroup();
    private ButtonGroup seatBelt = new ButtonGroup();
    private ButtonGroup headLightOk = new ButtonGroup();
    private ButtonGroup reverseHornOk = new ButtonGroup();
    private ButtonGroup breathlizerOk = new ButtonGroup();
    private ButtonGroup helperOk = new ButtonGroup();
    
    
    //flag
    int contiNue = 1;
    int reEnter = 0;
    String gpsStat = "";
    //app variables    
    private int readerId = 0;
    
    //connections
    //private Connection conn = null;
    //private Connection morphoSyncConn = null;
    //private Connection fingurePrintConn = null;
    //private Connection dbConnectionRFID = null;
    
    //services
    private SyncFingerPrint fingerPrintSyncService = null;
    private FingurePrintService fingurePrintCaptureService = null;
    private RFIDDataHandler rfidHandler = null;
    
    //holder/beans
    private Token token = null;
    private TPRecord tprRecord = null;
    private TPStep tpStep = null;
    private Biometric driverInformation = null;
    private GpsPlusViolations gpv = null;
    private TPRBlockManager tprBlockManager = null;

    private Date entryTime = null;
//    private Date exitTime = null;
    
    private int fitnessOk = Misc.getUndefInt();
    private int roadPermitOk = Misc.getUndefInt();
    private int insuranceOk = Misc.getUndefInt();
    private int polutionOk = Misc.getUndefInt();
    private int driverSrc = Misc.getUndefInt();
    private int isNewVehicle = Misc.getUndefInt();
    

    private boolean isDriverExist = false;
    private boolean isFingerExist = false;
    private boolean isFingerVerified = false;
    private boolean isFingerCaptured = false;
    private boolean isDriverBlacklisted = false;
    
    private boolean isAutoFingerCaptureStart = false;
    
    private boolean isFingerCaptureRunning = false;
    private boolean isMorphoExist = false;
    private boolean isFingerSyncRunning = false;
    private boolean vehicleBlackListed = false;
    private boolean isPaperOk = false;
    private boolean doCheck = false;
    private boolean isTagRead = false;
    private boolean isVehicleExist = false;
    private boolean isTpRecordValid = false;
    private boolean isRequestOverride = false;
    private BlockingInstruction blockInstructionBreathelizer = null;
    
//    private Icon iconArrowUp = new ImageIcon(ImageLoader.load(IMAGE_BASE+"arrow_up.png"));
//	private Icon iconArrowRight = new ImageIcon(ImageLoader.load(IMAGE_BASE+"arrow_right.png"));
//	private Icon iconArrowDown = new ImageIcon(ImageLoader.load(IMAGE_BASE+"arrow_down.png"));
//	private Icon iconArrowLeft = new ImageIcon(ImageLoader.load(IMAGE_BASE+"arrow_left.png"));
//	private Icon gifCapture = new ImageIcon(ImageLoader.load(IMAGE_BASE+"capture.gif"));
//	private Icon gifMaillage = new ImageIcon(ImageLoader.load(IMAGE_BASE+"maillage.gif"));
//	private ImageIcon imgFvpOk = new ImageIcon(ImageLoader.load(IMAGE_BASE+"ok.png"));
//	private ImageIcon imgFvpKo = new ImageIcon(ImageLoader.load(IMAGE_BASE+"ko.png"));
	
    
    private Icon iconArrowUp = new javax.swing.ImageIcon(getClass().getResource(TokenManager.IMAGE_BASE+"arrow_up.png"));
	private Icon iconArrowRight = new javax.swing.ImageIcon(getClass().getResource(TokenManager.IMAGE_BASE+"arrow_right.png"));
	private Icon iconArrowDown = new javax.swing.ImageIcon(getClass().getResource(TokenManager.IMAGE_BASE+"arrow_down.png"));
	private Icon iconArrowLeft = new javax.swing.ImageIcon(getClass().getResource(TokenManager.IMAGE_BASE+"arrow_left.png"));
	private Icon gifCapture = new javax.swing.ImageIcon(getClass().getResource(TokenManager.IMAGE_BASE+"capture.gif"));
	private Icon gifMaillage = new javax.swing.ImageIcon(getClass().getResource(TokenManager.IMAGE_BASE+"maillage.gif"));
	private ImageIcon imgFvpOk = new javax.swing.ImageIcon(getClass().getResource(TokenManager.IMAGE_BASE+"ok.png"));
	private ImageIcon imgFvpKo = new javax.swing.ImageIcon(getClass().getResource(TokenManager.IMAGE_BASE+"ko.png"));
	
	
	private boolean isManualEntry = false;
	private int captureCount = 1;
	private byte[] debugTemplate = null;
//	private Thread monitor;
	
    
    public CoalGateInWindow() throws IOException {
    	Connection conn = null;
    	boolean destroyIt = false;
    	try{
    		initComponents();
    		this.setExtendedState(this.getExtendedState() | this.MAXIMIZED_BOTH);
    		this.setTitle(UIConstant.formTitle);
    		myComponent();
    		conn = DBConnectionPool.getConnectionFromPoolNonWeb();
//    		setTransporterList(conn);
            DropDownValues.setTransporterList(transporter, conn, TokenManager.materialCat);
    		getFocus();
    		try{
    		 isMorphoExist  = TokenManager.morphoDeviceExist == 1 && (TokenManager.useSDK() ?  MorphoSmartFunctions.getMorpho().isConnected() : LoadLibrary.isMorphoConnected() );
    		}catch(Exception ex){
    			ex.printStackTrace();
    		}
    		changeDriverPanel(isMorphoExist);
    		setFingerPrintPanel(false);
    		System.out.println("######### Gate IN Start  ########");
    		start();
    		Clock.startClock("GateIn");
    		  if(TokenManager.isManualEntry.containsKey(TokenManager.currWorkStationId)){
                  int val  = TokenManager.isManualEntry.get(TokenManager.currWorkStationId);
                  if(val == 1){
                	  isManualEntry  = true;
                  }else {
                	  isManualEntry = false;
                       manualEntryButton.setEnabled(false);
                  }
              }else{
            	  isManualEntry = false;
                      manualEntryButton.setEnabled(false);
              }
    		BlockingInstruction bIns = new BlockingInstruction();
    		bIns.setType(com.ipssi.rfid.constant.Type.BlockingInstruction.BLOCK_DUETO_DRUNCK);
    		ArrayList<BlockingInstruction> list = (ArrayList<BlockingInstruction>) RFIDMasterDao.getList(conn, bIns, null);
    		if(list != null && list.size() > 0)
    			blockInstructionBreathelizer = list.get(0);
    		if (isMorphoExist) {
    			syncFingerPrintDataFromServer();
    		}
    	}catch(Exception ex){
    		destroyIt = true;
    		ex.printStackTrace();
    	}finally{
    		try{
    			DBConnectionPool.returnConnectionToPoolNonWeb(conn, destroyIt);
    		}catch(Exception ex){
    			ex.printStackTrace();
    		}
    	}
    	System.out.print("Morpho Device Exist:" + TokenManager.morphoDeviceExist);
    }

    public void start() throws IOException {

        if (rfidHandler == null) {
            rfidHandler = new RFIDDataHandler(1000, readerId, TokenManager.currWorkStationType, TokenManager.currWorkStationId, TokenManager.userId);
            rfidHandler.setTagListener(new TAGListener() {
            	@Override
                public void manageTag(Connection conn ,Token _token, TPRecord tpr, TPStep tps, TPRBlockManager _tprBlockManager) {
            		try {
                        token = _token;
                        tprBlockManager = _tprBlockManager;
                        setTPRecord(conn, tpr);
                        tpStep = tps;
                        Barrier.ChangeSignal();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }

                @Override
                public void showMessage(String message) {
                    JOptionPane.showMessageDialog(null, message);
                }

                @Override
                public void setVehicleName(String text) {
                    // TODO Auto-generated method stub
                    vehicleName.setText(text);
                }

                @Override
                public void clearVehicleName() {
                    vehicleName.setText("NO VEHICLE DETECTED");
                }

                @Override
                public int promptMessage(String message, Object[] options) {
                    return ConfirmationDialog.getDialogBox(new javax.swing.JFrame(), true, options, message);
                }

				@Override
				public void clear(boolean clearToken, Connection conn) {
					// TODO Auto-generated method stub
					clearInput(clearToken, conn);
				}

				@Override
				public int mergeData(long sessionId, String epc, RFIDHolder data) {
					// TODO Auto-generated method stub
					
					if(token == null || token.getLastSeen() != sessionId || data == null)
						return Misc.getUndefInt();
					Connection conn = null;
					boolean destroyIt = false;
					try{
						conn = DBConnectionPool.getConnectionFromPoolNonWeb();
						if(data != null)
							data.setVehicleId(tprRecord.getVehicleId());
						Triple<TPRecord, Integer, Boolean> tprTriplet = TPRInformation.getLatestNonWeb(conn, token.getVehicleId(), data, vehicleName.getText(), TokenManager.currWorkStationType, TokenManager.materialCat);//(conn, vehicleId, data, vehicleName, TokenManager.createNewTPR, workStationType);
						TPRecord tpr = tprTriplet != null ? tprTriplet.first : null;
						if(tpr != null && tpr.getTprStatus() == 0 && tpr.getIsLatest() ==1 && tpr.getStatus() != 100 ){
							tprRecord = tpr;
							vehicleName.setText(tprRecord.getVehicleId(), tprRecord.getVehicleName());
							challanNo.setText(tprRecord.getChallanNo());
							if (tprRecord.getChallanDate() != null) {
								challanDate.setText(UIConstant.displayFormat.format(tprRecord.getChallanDate()));
							}
							lrNo.setText(tprRecord.getLrNo());
							if (tprRecord.getLrDate() != null) {
								lrDate.setText(UIConstant.displayFormat.format(tprRecord.getLrDate()));
							}
							minesId.setText(DropDownValues.getMines(tprRecord.getMinesId(), conn));
							DropDownValues.setComboItem(transporter, tprRecord.getTransporterId());
							gpv = GpsPlusViolations.getGpsPlusViolatins(conn, tprRecord.getVehicleId(), tprRecord.getTprId(),  System.currentTimeMillis(), tprRecord.getMinesId(), tprRecord.getChallanDate() == null ? System.currentTimeMillis() : tprRecord.getChallanDate().getTime());
							Pair<ResultEnum, String>  tatDis = gpv.getTATDistance(conn);
							if(tatDis.first == ResultEnum.GREEN) {
								tatDistance.setForeground(UIConstant.PanelDarkGreen);
								tatDistance.setText(tatDis.second);
							}else if(tatDis.first  == ResultEnum.RED){
								tatDistance.setForeground(UIConstant.noActionPanelColor);
								tatDistance.setText(tatDis.second);
							} else{
								tatDistance.setForeground(UIConstant.PanelYellow);
								tatDistance.setText(tatDis.second);
							} 

							Pair<ResultEnum, String>  tatTime = gpv.getTATTiming(conn);
							if(tatTime.first == ResultEnum.GREEN) {
								tatTiming.setForeground(UIConstant.PanelDarkGreen);
								tatTiming.setText(tatTime.second);
							}else if(tatTime.first  == ResultEnum.RED){
								tatTiming.setForeground(UIConstant.noActionPanelColor);
								tatTiming.setText(tatTime.second);
							} else{
								tatTiming.setForeground(UIConstant.PanelYellow);
								tatTiming.setText(tatTime.second);
							} 

							Pair<ResultEnum, String> gpsSafetyViolation   = gpv.getSafetyViolations(conn) ;
							if(gpsSafetyViolation.first == ResultEnum.GREEN) {
								transitViolation.setForeground(UIConstant.PanelDarkGreen);
								transitViolation.setText(gpsSafetyViolation.second);
							}else if(gpsSafetyViolation.first  == ResultEnum.RED){
								transitViolation.setForeground(UIConstant.noActionPanelColor);
								transitViolation.setText(gpsSafetyViolation.second);
							} else{
								transitViolation.setForeground(UIConstant.PanelYellow);
								transitViolation.setText(gpsSafetyViolation.second);
							} 
							
							
							Pair<ResultEnum, String> getGpsQCViolationsSummary = gpv.getGpsQCViolationsSummary(conn, tprRecord.getLoadGross());
							if(getGpsQCViolationsSummary.first == ResultEnum.GREEN) {
								transitQcViolation.setForeground(UIConstant.PanelDarkGreen);
								transitQcViolation.setText(getGpsQCViolationsSummary.second);
							}else if(getGpsQCViolationsSummary.first  == ResultEnum.RED){
								transitQcViolation.setForeground(UIConstant.noActionPanelColor);
								transitQcViolation.setText(getGpsQCViolationsSummary.second);
							} else{
								transitQcViolation.setForeground(UIConstant.PanelYellow);
								transitQcViolation.setText(getGpsQCViolationsSummary.second);
							}
							Pair<Integer, String> supplier = TPRUtils.getSupplierFromDo(conn, tprRecord.getDoId());
							if(!Misc.isUndef(supplier.first)){
								supplierName.setText(supplier.second);
							}else{
								supplierName.setText("");
							}
							if(tprRecord.getDebugStr() == null){
								tprRecord.setDebugStr("");
							}
							tprRecord.setDebugStr(tprRecord.getDebugStr() + "TPR_Upd_Smart_Read\n");
							System.out.println("TPR Updated By Smart Read");
						} 
					}catch(Exception ex){
						ex.printStackTrace();
						destroyIt = true;
					}finally{
						try {
							DBConnectionPool.returnConnectionToPoolNonWeb(conn,destroyIt);
						} catch (GenericException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					return 1;
				}
            });
        }
        rfidHandler.start();
        if (fingurePrintCaptureService == null) {
            fingurePrintCaptureService = new FingurePrintService(1000, TokenManager.currWorkStationType, TokenManager.currWorkStationId, TokenManager.userId);
            fingurePrintCaptureService.setListener(new FingurePrintHandler() {

				@Override
                public void showMessage(String message) {
                    // TODO Auto-generated method stub
                    photo.setIcon(null);
                	fingerInstruction.setText("No Match Found");
                    WaveFormPlayer.playSoundIn(Status.TPRQuestion.fingerNotMatch);
                    disableFingurePrintCapture();
                }

                @Override
                public int promptMessage(String message, Object[] options) {
                    return 0;
                }

                @Override
               public void onChange(Connection conn, Biometric driverBean, boolean fingerVerified, boolean fingerCaptured) {
                	populateDriver(conn,driverBean, fingerVerified, fingerCaptured);
                	isFingerCaptureRunning = false;
                }
                @Override
                public void statusChange(boolean status) {
                    changeFingerButtonText(status);
                }

				@Override
				public void setDebugTemplate(byte[] data) {
					debugTemplate  = data; 
				}
            },this);
        }
    }

    private void changeFingerButtonText(boolean changeStatus) {
    	setFingerPrintPanel(changeStatus);
    	if (changeStatus) {
            captureButton.setText("Cancel Capture Service");
        } else {
        	captureButton.setText("Capture Finger Print >>>");
            
        }
    }

    private void setTPRecord(String vehicleName) throws IOException {
        if (rfidHandler != null) {
        	
            rfidHandler.getTprecord(vehicleName);
//            Triple<Token, TPRecord, Integer> tpRecord = rfidHandler.getTprecord(conn, vehicleName);
//            if (tpRecord != null) {
//                token = tpRecord.first;
//                setTPRecord(tpRecord.second, tpRecord.third);
//            }
        }
    }

    public void setTPRecord(Connection conn, TPRecord tpr) throws IOException {
        System.out.println("######### Gate IN setTPRecord  ########");
        try {
        	if(TokenManager.isBlockedVehicle){
        		JOptionPane.showMessageDialog(null, "Vehicle '"+tpr.getVehicleName() +"' is Blocked for GATE-IN/GATE-OUT ");
        		return;
        	}
            tprRecord = tpr;
            if (tprRecord != null) {
                if (true) {//blockStatus == Status.VALIDATION.NO_ISSUE) {// TPR Valid
                    toggleVehicle(false);
                	isTpRecordValid = true;
                    isVehicleExist = true;
                    isNewVehicle = Status.VEHICLE.EXISTING_RF;
                    isTagRead = token != null ? token.isReadFromTag() : false;
                    fitnessOk = TPRUtils.isVehicleDocumentComplete(conn, tprRecord.getVehicleId(), Status.TPRQuestion.isFitnessOk, TokenManager.fitnessExpiaryThreshold);
                    roadPermitOk = TPRUtils.isVehicleDocumentComplete(conn, tprRecord.getVehicleId(), Status.TPRQuestion.isRoadPermitOk, TokenManager.roadPermitExpiaryThreshold);
                    insuranceOk = TPRUtils.isVehicleDocumentComplete(conn, tprRecord.getVehicleId(), Status.TPRQuestion.isInsuranceOk, TokenManager.insauranceExpiaryThreshold);
                    polutionOk = TPRUtils.isVehicleDocumentComplete(conn, tprRecord.getVehicleId(), Status.TPRQuestion.isPolutionOk, TokenManager.polutionExpiaryThreshold);
                    isPaperOk = tprBlockManager == null || ( (!tprBlockManager.useForBlocking(Status.TPRQuestion.isFitnessOk) || fitnessOk == Results.Questions.YES) 
                    		    && 
                    		    (!tprBlockManager.useForBlocking(Status.TPRQuestion.isRoadPermitOk) ||  roadPermitOk == Results.Questions.YES) 
                    		    && 
                    		    (!tprBlockManager.useForBlocking(Status.TPRQuestion.isInsuranceOk) || insuranceOk == Results.Questions.YES) 
                    		    && 
                    		    (!tprBlockManager.useForBlocking(Status.TPRQuestion.isPolutionOk) || polutionOk == Results.Questions.YES));
                    if(isPaperOk){
                    	paperValid.setForeground(UIConstant.textFontColor);
                        paperValid.setText("Yes");
                    }else{
                    	paperValid.setForeground(Color.RED);
                        paperValid.setText("No");
                    }
                    if (token == null && tprRecord.getEarliestUnloadGateInEntry() != null) {
                        entryTime = tprRecord.getEarliestUnloadGateInEntry();
                    } else if (token != null && tprRecord.getEarliestUnloadGateInEntry() == null) {
                        if (token.getLastSeen() != Misc.getUndefInt()) {
                            entryTime = new Date(token.getLastSeen());
                        } else {
                            entryTime = new Date();
                        }
                    } else if (token != null && tprRecord.getEarliestUnloadGateInEntry() != null) {
                        if (token.getLastSeen() > Utils.getDateTimeLong(tprRecord.getEarliestUnloadGateInEntry())) {
                            if (token.getLastSeen() != Misc.getUndefInt()) {
                                entryTime = new Date(token.getLastSeen());
                            } else {
                                entryTime = new Date();
                            }
                            System.out.println("token " + entryTime);
                        } else {
                            entryTime = new Date();
                        }
                    } else {
                        entryTime = new Date();
                    }
                    vehicleName.setText(tprRecord.getVehicleId(), tprRecord.getVehicleName());
                    challanNo.setText(tprRecord.getChallanNo());
                    if (tprRecord.getChallanDate() != null) {
                        challanDate.setText(UIConstant.displayFormat.format(tprRecord.getChallanDate()));
                    }
                    lrNo.setText(tprRecord.getLrNo());
                    if (tprRecord.getLrDate() != null) {
                        lrDate.setText(UIConstant.displayFormat.format(tprRecord.getLrDate()));
                    }
                    minesId.setText(DropDownValues.getMines(tprRecord.getMinesId(), conn));
                    if (tprRecord.getTransporterId() != Misc.getUndefInt() && tprRecord.getTransporterId() != 0) {
                        DropDownValues.setComboItem(transporter, tprRecord.getTransporterId());
                        seatBeltPanel.requestFocusInWindow();
                        seatBeltPanel.setBackground(UIConstant.focusPanelColor);
                        playSeatBeltWormVoice();
                    } else {
                        transporter.setSelectedIndex(0);
                        transporter.setFocusable(true);
                        transporter.setEnabled(true);
                        transporter.requestFocusInWindow();
                        setWhiteBackColor();
                    }
                    int selectedtransporter = DropDownValues.getComboSelectedVal(transporter);
            		int selectedMines = tprRecord != null ? tprRecord.getMinesId() : Misc.getUndefInt();
            		int selectedGrade = tprRecord != null ? tprRecord.getMaterialGradeId() : Misc.getUndefInt();
            		int selectedDO = tprRecord != null ? tprRecord.getDoId() : Misc.getUndefInt();
            		Pair<Integer, String> bedAssign = TPRUtils.getBedAllignment(conn, selectedtransporter, selectedMines, selectedGrade,selectedDO);
            		
            		if(bedAssign != null)
            			bedAssigned.setText(bedAssign.second);
                    
                  gpv = GpsPlusViolations.getGpsPlusViolatins(conn, tprRecord.getVehicleId(), tprRecord.getTprId(),  System.currentTimeMillis(), tprRecord.getMinesId(), tprRecord.getChallanDate() == null ? System.currentTimeMillis() : tprRecord.getChallanDate().getTime());
                 
                  Pair<ResultEnum, String>  tatDis = gpv.getTATDistance(conn);
                if(tatDis.first == ResultEnum.GREEN) {
                  tatDistance.setForeground(UIConstant.PanelDarkGreen);
                  tatDistance.setText(tatDis.second);
                }else if(tatDis.first  == ResultEnum.RED){
                  tatDistance.setForeground(UIConstant.noActionPanelColor);
                  tatDistance.setText(tatDis.second);
                } else{
                  tatDistance.setForeground(UIConstant.PanelYellow);
                  tatDistance.setText(tatDis.second);
                } 
                
                 Pair<ResultEnum, String>  tatTime = gpv.getTATTiming(conn);
                if(tatTime.first == ResultEnum.GREEN) {
                  tatTiming.setForeground(UIConstant.PanelDarkGreen);
                  tatTiming.setText(tatTime.second);
                }else if(tatTime.first  == ResultEnum.RED){
                  tatTiming.setForeground(UIConstant.noActionPanelColor);
                  tatTiming.setText(tatTime.second);
                } else{
                  tatTiming.setForeground(UIConstant.PanelYellow);
                  tatTiming.setText(tatTime.second);
                } 
                
              Pair<ResultEnum, String> gpsSafetyViolation   = gpv.getSafetyViolations(conn) ;
                 if(gpsSafetyViolation.first == ResultEnum.GREEN) {
                  transitViolation.setForeground(UIConstant.PanelDarkGreen);
                  transitViolation.setText(gpsSafetyViolation.second);
                  gpsStat = "Ok";
                }else if(gpsSafetyViolation.first  == ResultEnum.RED){
                  transitViolation.setForeground(UIConstant.noActionPanelColor);
                  transitViolation.setText(gpsSafetyViolation.second);
                  gpsStat = "Not Ok";
                } else{
                  transitViolation.setForeground(UIConstant.PanelYellow);
                  transitViolation.setText(gpsSafetyViolation.second);
                  gpsStat = "Ok";
                } 
                tprIdLabel.setText("TPR-ID:"+tprRecord.getTprId());
               Pair<ResultEnum, String> getGpsQCViolationsSummary = gpv.getGpsQCViolationsSummary(conn, tprRecord.getLoadGross());
                  if(getGpsQCViolationsSummary.first == ResultEnum.GREEN) {
                  transitQcViolation.setForeground(UIConstant.PanelDarkGreen);
                  transitQcViolation.setText(getGpsQCViolationsSummary.second);
                }else if(getGpsQCViolationsSummary.first  == ResultEnum.RED){
                  transitQcViolation.setForeground(UIConstant.noActionPanelColor);
                  transitQcViolation.setText(getGpsQCViolationsSummary.second);
                } else{
                  transitQcViolation.setForeground(UIConstant.PanelYellow);
                  transitQcViolation.setText(getGpsQCViolationsSummary.second);
                }
                Pair<Integer, String> supplier = TPRUtils.getSupplierFromDo(conn, tprRecord.getDoId());
                if(!Misc.isUndef(supplier.first)){
                	supplierName.setText(supplier.second);
                }else{
                	supplierName.setText("");
                }
               } 
                setBlockingStatus();
                String gpsViolations = GateInDao.sendDataOnServer(TokenManager.URL,TokenManager.ACTION,tprRecord.getVehicleName());
                String printableString = getPrintableViolations(gpsViolations);
                String blckingTxt  = blocking_reason.getText() + printableString;
                System.out.println("Violations: "+blckingTxt);
                blocking_reason.setText(blckingTxt);
                quickCreate.setEnabled(true);
                scheduleClearScreenTimer();
            } else {
                /*
                 * if (!Utils.isNull(vehicle_name) && !(vehicleName.getText().trim()).equalsIgnoreCase("NO VEHICLE DETECTED")) { JOptionPane.showMessageDialog(null,
                 * "Invalid Vehicle Go to Registration"); } else if (Utils.isNull(vehicle_name)) { JOptionPane.showMessageDialog(null, "Invalid Vehicle Go to Registration"); }
                 */
            	System.out.println("Error No TPR found");
                //JOptionPane.showMessageDialog(null, "Invalid Vehicle Go to Registration");
                isTpRecordValid = false;
                isVehicleExist = false;
            }
//            ThreadMonitor.stop(monitor);
//            ThreadMonitor.start(1000, new InterruptListener(){
//            	@Override 
//            	public void interrupt(){
//            		 //session timeout 
//            	 }
//            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private String getPrintableViolations(String gpsViolations) {
		String retVal = "";
		try {
			JSONArray jsonArr = new JSONArray(gpsViolations);
			if (jsonArr.length() > 0) {
				for (int i = 0; i < jsonArr.length(); i++) {
					JSONObject jobj = jsonArr.getJSONObject(i);

					String violations = jobj.get("GPS_VIOLATIONS") == null ? "" :  jobj.get("GPS_VIOLATIONS").toString(); 
					String gpsTame = jobj.get("GPS_TIME") == null ? "" :  jobj.get("GPS_TIME").toString();
					String gpsLocation = jobj.get("GPS_LOCATION") == null ? "" :  jobj.get("GPS_LOCATION").toString();
				
					violations = violations.length()> 100 ? violations.substring(0, 100) : violations;
					gpsTame = gpsTame.length()> 20 ? gpsTame.substring(0, 20) : gpsTame;
					gpsLocation = gpsLocation.length() > 40 ? gpsLocation.substring(0, 40) : gpsLocation;
					
					retVal = violations
							+ " | " +  gpsTame
							+ " | " + gpsLocation;	
					
					System.out.println("GPS_VIOLATIONS: "+violations);
					System.out.println("GPS_TIME: " + gpsTame);
					System.out.println("GPS_LOCATION: "+gpsLocation);
				}
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return retVal;
	}

	private void getFocus() {
        if (vehicleName.isTextEditable()) {
        	setWhiteBackColor();
            vehicleName.requestFocusInWindow();
        } else {
            seatBeltPanel.requestFocusInWindow();
            seatBeltPanel.setBackground(UIConstant.focusPanelColor);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        username = new javax.swing.JLabel();
        button1 = new java.awt.Button();
        tprIdLabel = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel83 = new javax.swing.JLabel();
        jLabel84 = new javax.swing.JLabel();
        transporter = new javax.swing.JComboBox();
        jLabel14 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        tatDistance = new javax.swing.JLabel();
        minesId = new javax.swing.JLabel();
        transitViolation = new javax.swing.JLabel();
        challanNo = new javax.swing.JLabel();
        transitQcViolation = new javax.swing.JLabel();
        challanDate = new javax.swing.JLabel();
        driverWorkHour = new javax.swing.JLabel();
        lrNo = new javax.swing.JLabel();
        paperValid = new javax.swing.JLabel();
        paperValid.setFont(UIConstant.textFont);
        paperValid.setForeground(UIConstant.textFontColor);
        lrDate = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        tatTiming = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        driverHours = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jLabel89 = new javax.swing.JLabel();
        jLabel97 = new javax.swing.JLabel();
        sideMirrorPanel = new javax.swing.JPanel();
        sideMirrorNo = new javax.swing.JCheckBox();
        sideMirrorYes = new javax.swing.JCheckBox();
        sideMirrorNC = new javax.swing.JCheckBox();
        numberVisiblePanel = new javax.swing.JPanel();
        numberVisibleNo = new javax.swing.JCheckBox();
        numberVisibleYes = new javax.swing.JCheckBox();
        numberVisibleNC = new javax.swing.JCheckBox();
        jLabel88 = new javax.swing.JLabel();
        tarpaulinPanel = new javax.swing.JPanel();
        tarpaulinNo = new javax.swing.JCheckBox();
        tarpaulinYes = new javax.swing.JCheckBox();
        tarpaulinNC = new javax.swing.JCheckBox();
        jLabel86 = new javax.swing.JLabel();
        jLabel85 = new javax.swing.JLabel();
        seatBeltPanel = new javax.swing.JPanel();
        seatBeltNo = new javax.swing.JCheckBox();
        seatBeltYes = new javax.swing.JCheckBox();
        seatBeltNC = new javax.swing.JCheckBox();
        jLabel87 = new javax.swing.JLabel();
        tailLightPanel = new javax.swing.JPanel();
        tailLightNo = new javax.swing.JCheckBox();
        tailLightYes = new javax.swing.JCheckBox();
        tailLightNC = new javax.swing.JCheckBox();
        jLabel90 = new javax.swing.JLabel();
        headLightPanel = new javax.swing.JPanel();
        headLightNo = new javax.swing.JCheckBox();
        headLightYes = new javax.swing.JCheckBox();
        headLightNC = new javax.swing.JCheckBox();
        jLabel99 = new javax.swing.JLabel();
        reverseHornPanel = new javax.swing.JPanel();
        reverseHornNo = new javax.swing.JCheckBox();
        reverseHornYes = new javax.swing.JCheckBox();
        reverseHornNC = new javax.swing.JCheckBox();
        jLabel98 = new javax.swing.JLabel();
        leftSideIndicatorPanel = new javax.swing.JPanel();
        leftSideIndicatorNo = new javax.swing.JCheckBox();
        leftSideIndicatorYes = new javax.swing.JCheckBox();
        leftSideIndicatorNC = new javax.swing.JCheckBox();
        sealOkPanel = new javax.swing.JPanel();
        sealYes = new javax.swing.JCheckBox();
        sealNo = new javax.swing.JCheckBox();
        sealNC = new javax.swing.JCheckBox();
        jLabel101 = new javax.swing.JLabel();
        rightSideIndicatorPanel = new javax.swing.JPanel();
        rightSideIndicatorYes = new javax.swing.JCheckBox();
        rightSideIndicatorNo = new javax.swing.JCheckBox();
        rightSideIndicatorNC = new javax.swing.JCheckBox();
        jPanel7 = new javax.swing.JPanel();
        jLabel23 = new javax.swing.JLabel();
        jLabel24 = new javax.swing.JLabel();
        jLabel25 = new javax.swing.JLabel();
        jLabel26 = new javax.swing.JLabel();
        filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 20), new java.awt.Dimension(0, 20), new java.awt.Dimension(32767, 20));
        breathlizerOkPanel = new javax.swing.JPanel();
        breathlizerNo = new javax.swing.JCheckBox();
        breathlizerYes = new javax.swing.JCheckBox();
        breathlizerNC = new javax.swing.JCheckBox();
        HelperPanel = new javax.swing.JPanel();
        helperNo = new javax.swing.JCheckBox();
        helperYes = new javax.swing.JCheckBox();
        dlNo = new javax.swing.JTextField();
        driverName = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        driverId = new javax.swing.JTextField();
        clear = new javax.swing.JButton();
        manualEntryButton = new javax.swing.JButton();
        quickCreate = new javax.swing.JButton();
        photo = new javax.swing.JLabel();
        jPanel8 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        vehicleName = new AutoCompleteCombo();//new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        overrides = new javax.swing.JTextField();
        jLabel20 = new javax.swing.JLabel();
        supplierName = new javax.swing.JLabel();
        fingerInstruction = new javax.swing.JLabel();
        captureButton = new javax.swing.JButton();
        jLabel10 = new javax.swing.JLabel();
        bedAssigned = new javax.swing.JLabel();
        filler2 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 0));
        filler3 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 0));
        digitalClock = new javax.swing.JLabel();
        fingerImageLabel = new javax.swing.JLabel();
        manualSyncButton = new javax.swing.JButton();
        blocking_reason = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        this.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
            	GateInDao.forceSignut(TokenManager.userId,TokenManager.srcType,Integer.toString(TokenManager.systemId));
            }});
        
        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        jPanel3.setBackground(new java.awt.Color(255, 255, 255));

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/ipssi/rfid/ui/intelliplanner.png"))); // NOI18N

        jLabel7.setFont(UIConstant.subHeadingFont);
        jLabel7.setText("Gate In:Controlled Coal Vehicle Gate Entry");

        jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/ipssi/rfid/ui/cover_01_right_top.gif"))); // NOI18N

        username.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);

        button1.setBackground(new java.awt.Color(255, 255, 255));
        button1.setFocusable(false);
        button1.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        button1.setForeground(new java.awt.Color(0, 102, 153));
        button1.setLabel("Sign Out");
        button1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button1ActionPerformed(evt);
            }
        });
        
        tprIdLabel.setFont(new java.awt.Font("Segoe UI", 1, 14));
//        tprIdLabel.setText("TPR-ID:1234567890");
        
        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(35, 35, 35)
                .addComponent(tprIdLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 207, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(26, 26, 26)
                .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 471, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 78, Short.MAX_VALUE)
                .addComponent(username, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(button1, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(164, 164, 164))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
            		.addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(tprIdLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(jPanel3Layout.createSequentialGroup()
            		.addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(username, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(button1, javax.swing.GroupLayout.DEFAULT_SIZE, 33, Short.MAX_VALUE))))
                .addContainerGap())
        );

        jPanel4.setBackground(new java.awt.Color(255, 255, 255));

        jLabel4.setFont(UIConstant.labelFont);
        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel4.setText("Transporter:");

        jLabel6.setFont(UIConstant.labelFont);
        jLabel6.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel6.setText("TAT Distance[act/exp]:");

        jLabel83.setFont(UIConstant.labelFont);
        jLabel83.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel83.setText("Mines:");

        jLabel84.setFont(UIConstant.labelFont);
        jLabel84.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel84.setText("Paper Validity:");
        //jLabel84.setText("Paper Ok:");

        transporter.setFont(UIConstant.textFont);
        transporter.setMaximumRowCount(10);
        transporter.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Select" }));
        transporter.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 0));
        transporter.setEnabled(false);
        transporter.setFocusable(false);
        transporter.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                transporterMouseClicked(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                transporterMousePressed(evt);
            }
        });
        transporter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                transporterActionPerformed(evt);
            }
        });
        transporter.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                transporterFocusLost(evt);
            }
        });
        transporter.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                transporterKeyPressed(evt);
            }
        });

        jLabel14.setFont(UIConstant.labelFont);
        jLabel14.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel14.setText("Invoice#:");

        jLabel15.setFont(UIConstant.labelFont);
        jLabel15.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel15.setText("Driver Work hours:");

        jLabel16.setFont(UIConstant.labelFont);
        jLabel16.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel16.setText("Transit Safety Violation:");

        jLabel17.setFont(UIConstant.labelFont);
        jLabel17.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel17.setText("Transit QC Violation:");

        jLabel18.setFont(UIConstant.labelFont);
        jLabel18.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel18.setText("LR#:");

        jLabel19.setFont(UIConstant.labelFont);
        jLabel19.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel19.setText("Invoice Date:");

        tatDistance.setFont(UIConstant.textFont);
        tatDistance.setForeground(UIConstant.textFontColor);

        minesId.setFont(UIConstant.textFont);
        minesId.setForeground(UIConstant.textFontColor);

        transitViolation.setFont(UIConstant.textFont); // NOI18N

        transitViolation.setForeground(UIConstant.textFontColor);

        challanNo.setFont(UIConstant.textFont); // NOI18N

        challanNo.setForeground(UIConstant.textFontColor);

        transitQcViolation.setFont(UIConstant.textFont); // NOI18N

        transitQcViolation.setForeground(UIConstant.textFontColor);

        challanDate.setFont(UIConstant.textFont); // NOI18N

        challanDate.setForeground(UIConstant.textFontColor);

        driverWorkHour.setFont(UIConstant.textFont); // NOI18N

        driverWorkHour.setForeground(UIConstant.textFontColor);

        lrNo.setFont(UIConstant.textFont); // NOI18N

        lrNo.setForeground(UIConstant.textFontColor);

        lrDate.setFont(UIConstant.textFont); // NOI18N

        lrDate.setForeground(UIConstant.textFontColor);

        jLabel8.setFont(UIConstant.labelFont);
        jLabel8.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel8.setText("LR Date:");

        jLabel11.setFont(UIConstant.labelFont);
        jLabel11.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel11.setText("TAT Timing[act/exp]:");

        tatTiming.setFont(UIConstant.textFont); // NOI18N

        tatTiming.setForeground(UIConstant.textFontColor);

        jLabel12.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel12.setText("Trips in 24 hrs:");
        jLabel12.setFont(UIConstant.labelFont);

        driverHours.setFont(UIConstant.textFont);
        driverHours.setForeground(UIConstant.textFontColor);

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, 164, Short.MAX_VALUE)
                    .addComponent(jLabel84, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 164, Short.MAX_VALUE)
                    .addComponent(jLabel83, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addComponent(paperValid, javax.swing.GroupLayout.DEFAULT_SIZE, 215, Short.MAX_VALUE)
                                .addComponent(tatDistance, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addComponent(minesId, javax.swing.GroupLayout.PREFERRED_SIZE, 214, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(6, 6, 6)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel14, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel16, javax.swing.GroupLayout.DEFAULT_SIZE, 130, Short.MAX_VALUE)
                            .addComponent(jLabel11, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(transporter, javax.swing.GroupLayout.PREFERRED_SIZE, 217, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel18, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(transitViolation, javax.swing.GroupLayout.DEFAULT_SIZE, 215, Short.MAX_VALUE)
                            .addComponent(challanNo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lrNo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel19, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jLabel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(lrDate, javax.swing.GroupLayout.PREFERRED_SIZE, 258, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(challanDate, javax.swing.GroupLayout.PREFERRED_SIZE, 215, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(18, 18, 18))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jLabel15, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jLabel17, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(transitQcViolation, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 215, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(driverWorkHour, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 215, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(61, 61, 61))))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(tatTiming, javax.swing.GroupLayout.PREFERRED_SIZE, 215, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel12, javax.swing.GroupLayout.DEFAULT_SIZE, 169, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(driverHours, javax.swing.GroupLayout.PREFERRED_SIZE, 215, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(61, 61, 61))))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(3, 3, 3)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(transporter, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabel18, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(3, 3, 3)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel83, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(minesId, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(3, 3, 3)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel84, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(paperValid, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(3, 3, 3)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(tatDistance, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, 30, Short.MAX_VALUE))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel4Layout.createSequentialGroup()
                                        .addComponent(lrDate, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                    .addGroup(jPanel4Layout.createSequentialGroup()
                                        .addComponent(jLabel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addGap(6, 6, 6)))
                                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel19, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel14, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(challanDate, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addComponent(lrNo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(challanNo, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addGap(3, 3, 3)
                                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(jLabel16, javax.swing.GroupLayout.DEFAULT_SIZE, 30, Short.MAX_VALUE)
                                    .addComponent(transitViolation, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addGap(3, 3, 3))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(transitQcViolation, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel17, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel11, javax.swing.GroupLayout.DEFAULT_SIZE, 29, Short.MAX_VALUE)
                            .addComponent(tatTiming, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel12, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(driverHours, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(32, 32, 32)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(driverWorkHour, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel15, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(6, 6, 6))))
        );

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));

        jLabel89.setFont(UIConstant.labelFont);
        jLabel89.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel89.setText("Side Mirror?:");

        jLabel97.setFont(UIConstant.labelFont);
        jLabel97.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel97.setText("Number Visible?:");

        sideMirrorPanel.setBackground(new java.awt.Color(255, 255, 255));
        sideMirrorPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                sideMirrorPanelMouseClicked(evt);
            }
        });
        sideMirrorPanel.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                sideMirrorPanelFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                sideMirrorPanelFocusLost(evt);
            }
        });
        sideMirrorPanel.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                sideMirrorPanelKeyPressed(evt);
            }
        });

        sideMirrorNo.setBackground(new java.awt.Color(255, 255, 255));
        sideMirrorNo.setText("No");
        sideMirrorNo.setFocusable(false);
        sideMirrorNo.setOpaque(false);
        sideMirrorNo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sideMirrorNoActionPerformed(evt);
            }
        });

        sideMirrorYes.setBackground(new java.awt.Color(255, 255, 255));
        sideMirrorYes.setText("Yes");
        sideMirrorYes.setFocusable(false);
        sideMirrorYes.setOpaque(false);
        sideMirrorYes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sideMirrorYesActionPerformed(evt);
            }
        });

        sideMirrorNC.setBackground(new java.awt.Color(255, 255, 255));
        sideMirrorNC.setText("NC");
        sideMirrorNC.setFocusable(false);
        sideMirrorNC.setOpaque(false);
        sideMirrorNC.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sideMirrorNCActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout sideMirrorPanelLayout = new javax.swing.GroupLayout(sideMirrorPanel);
        sideMirrorPanel.setLayout(sideMirrorPanelLayout);
        sideMirrorPanelLayout.setHorizontalGroup(
            sideMirrorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, sideMirrorPanelLayout.createSequentialGroup()
                .addComponent(sideMirrorYes, javax.swing.GroupLayout.DEFAULT_SIZE, 47, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(sideMirrorNo, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(sideMirrorNC, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        sideMirrorPanelLayout.setVerticalGroup(
            sideMirrorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(sideMirrorPanelLayout.createSequentialGroup()
                .addGroup(sideMirrorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(sideMirrorNo, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(sideMirrorYes, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(sideMirrorNC, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, Short.MAX_VALUE))
        );

        numberVisiblePanel.setBackground(new java.awt.Color(255, 255, 255));
        numberVisiblePanel.setName("numberVisiblePanel"); // NOI18N
        numberVisiblePanel.setPreferredSize(new java.awt.Dimension(94, 30));
        numberVisiblePanel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                numberVisiblePanelMouseClicked(evt);
            }
        });
        numberVisiblePanel.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                numberVisiblePanelFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                numberVisiblePanelFocusLost(evt);
            }
        });
        numberVisiblePanel.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                numberVisiblePanelKeyPressed(evt);
            }
        });

        numberVisibleNo.setBackground(new java.awt.Color(255, 255, 255));
        numberVisibleNo.setText("No");
        numberVisibleNo.setFocusable(false);
        numberVisibleNo.setOpaque(false);
        numberVisibleNo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                numberVisibleNoActionPerformed(evt);
            }
        });

        numberVisibleYes.setBackground(new java.awt.Color(255, 255, 255));
        numberVisibleYes.setText("Yes");
        numberVisibleYes.setFocusable(false);
        numberVisibleYes.setOpaque(false);
        numberVisibleYes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                numberVisibleYesActionPerformed(evt);
            }
        });

        numberVisibleNC.setBackground(new java.awt.Color(255, 255, 255));
        numberVisibleNC.setText("NC");
        numberVisibleNC.setFocusable(false);
        numberVisibleNC.setOpaque(false);
        numberVisibleNC.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                numberVisibleNCActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout numberVisiblePanelLayout = new javax.swing.GroupLayout(numberVisiblePanel);
        numberVisiblePanel.setLayout(numberVisiblePanelLayout);
        numberVisiblePanelLayout.setHorizontalGroup(
            numberVisiblePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, numberVisiblePanelLayout.createSequentialGroup()
                .addComponent(numberVisibleYes, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(numberVisibleNo, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(numberVisibleNC, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        numberVisiblePanelLayout.setVerticalGroup(
            numberVisiblePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(numberVisiblePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(numberVisibleNo, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(numberVisibleYes, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(numberVisibleNC, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jLabel88.setFont(UIConstant.labelFont);
        jLabel88.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel88.setText("Tarpaulin?:");

        tarpaulinPanel.setBackground(new java.awt.Color(255, 255, 255));
        tarpaulinPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tarpaulinPanelMouseClicked(evt);
            }
        });
        tarpaulinPanel.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                tarpaulinPanelFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                tarpaulinPanelFocusLost(evt);
            }
        });
        tarpaulinPanel.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                tarpaulinPanelKeyPressed(evt);
            }
        });

        tarpaulinNo.setBackground(new java.awt.Color(255, 255, 255));
        tarpaulinNo.setText("No");
        tarpaulinNo.setFocusable(false);
        tarpaulinNo.setOpaque(false);
        tarpaulinNo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tarpaulinNoActionPerformed(evt);
            }
        });
        tarpaulinNo.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                tarpaulinNoKeyPressed(evt);
            }
        });

        tarpaulinYes.setBackground(new java.awt.Color(255, 255, 255));
        tarpaulinYes.setText("Yes");
        tarpaulinYes.setFocusable(false);
        tarpaulinYes.setOpaque(false);
        tarpaulinYes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tarpaulinYesActionPerformed(evt);
            }
        });
        tarpaulinYes.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                tarpaulinYesKeyPressed(evt);
            }
        });

        tarpaulinNC.setBackground(new java.awt.Color(255, 255, 255));
        tarpaulinNC.setText("NC");
        tarpaulinNC.setFocusable(false);
        tarpaulinNC.setOpaque(false);
        tarpaulinNC.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tarpaulinNCActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout tarpaulinPanelLayout = new javax.swing.GroupLayout(tarpaulinPanel);
        tarpaulinPanel.setLayout(tarpaulinPanelLayout);
        tarpaulinPanelLayout.setHorizontalGroup(
            tarpaulinPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, tarpaulinPanelLayout.createSequentialGroup()
                .addComponent(tarpaulinYes, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tarpaulinNo, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tarpaulinNC, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        tarpaulinPanelLayout.setVerticalGroup(
            tarpaulinPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(tarpaulinPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(tarpaulinNo, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(tarpaulinYes, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(tarpaulinNC))
        );

        jLabel86.setFont(UIConstant.labelFont);
        jLabel86.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel86.setText("Seat Belt Worn?:");

        jLabel85.setFont(UIConstant.labelFont);
        jLabel85.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel85.setText("Seal?:");

        seatBeltPanel.setBackground(new java.awt.Color(255, 255, 255));
        seatBeltPanel.setName("numberVisiblePanel"); // NOI18N
        seatBeltPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                seatBeltPanelMouseClicked(evt);
            }
        });
        seatBeltPanel.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                seatBeltPanelFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                seatBeltPanelFocusLost(evt);
            }
        });
        seatBeltPanel.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                seatBeltPanelKeyPressed(evt);
            }
        });

        seatBeltNo.setBackground(new java.awt.Color(255, 255, 255));
        seatBeltNo.setText("No");
        seatBeltNo.setFocusable(false);
        seatBeltNo.setOpaque(false);
        seatBeltNo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                seatBeltNoActionPerformed(evt);
            }
        });

        seatBeltYes.setBackground(new java.awt.Color(255, 255, 255));
        seatBeltYes.setText("Yes");
        seatBeltYes.setFocusable(false);
        seatBeltYes.setOpaque(false);
        seatBeltYes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                seatBeltYesActionPerformed(evt);
            }
        });
        seatBeltYes.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                seatBeltYesKeyPressed(evt);
            }
        });

        seatBeltNC.setBackground(new java.awt.Color(255, 255, 255));
        seatBeltNC.setText("NC");
        seatBeltNC.setFocusable(false);
        seatBeltNC.setOpaque(false);
        seatBeltNC.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                seatBeltNCActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout seatBeltPanelLayout = new javax.swing.GroupLayout(seatBeltPanel);
        seatBeltPanel.setLayout(seatBeltPanelLayout);
        seatBeltPanelLayout.setHorizontalGroup(
            seatBeltPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, seatBeltPanelLayout.createSequentialGroup()
                .addComponent(seatBeltYes, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(seatBeltNo, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(seatBeltNC, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        seatBeltPanelLayout.setVerticalGroup(
            seatBeltPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(seatBeltPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(seatBeltNo, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(seatBeltYes, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(seatBeltNC))
        );

        jLabel87.setFont(UIConstant.labelFont);
        jLabel87.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel87.setText("Tail Light?:");

        tailLightPanel.setBackground(new java.awt.Color(255, 255, 255));
        tailLightPanel.setPreferredSize(new java.awt.Dimension(94, 30));
        tailLightPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tailLightPanelMouseClicked(evt);
            }
        });
        tailLightPanel.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                tailLightPanelFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                tailLightPanelFocusLost(evt);
            }
        });
        tailLightPanel.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                tailLightPanelKeyPressed(evt);
            }
        });

        tailLightNo.setBackground(new java.awt.Color(255, 255, 255));
        tailLightNo.setText("No");
        tailLightNo.setFocusable(false);
        tailLightNo.setOpaque(false);
        tailLightNo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tailLightNoActionPerformed(evt);
            }
        });

        tailLightYes.setBackground(new java.awt.Color(255, 255, 255));
        tailLightYes.setText("Yes");
        tailLightYes.setFocusable(false);
        tailLightYes.setOpaque(false);
        tailLightYes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tailLightYesActionPerformed(evt);
            }
        });

        tailLightNC.setBackground(new java.awt.Color(255, 255, 255));
        tailLightNC.setText("NC");
        tailLightNC.setFocusable(false);
        tailLightNC.setOpaque(false);
        tailLightNC.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tailLightNCActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout tailLightPanelLayout = new javax.swing.GroupLayout(tailLightPanel);
        tailLightPanel.setLayout(tailLightPanelLayout);
        tailLightPanelLayout.setHorizontalGroup(
            tailLightPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, tailLightPanelLayout.createSequentialGroup()
                .addComponent(tailLightYes, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tailLightNo, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tailLightNC, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        tailLightPanelLayout.setVerticalGroup(
            tailLightPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, tailLightPanelLayout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addGroup(tailLightPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(tailLightNC, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(tailLightNo, javax.swing.GroupLayout.DEFAULT_SIZE, 30, Short.MAX_VALUE)
                    .addComponent(tailLightYes, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );

        jLabel90.setFont(UIConstant.labelFont);
        jLabel90.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel90.setText("Head Light?:");

        headLightPanel.setBackground(new java.awt.Color(255, 255, 255));
        headLightPanel.setName("numberVisiblePanel"); // NOI18N
        headLightPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                headLightPanelMouseClicked(evt);
            }
        });
        headLightPanel.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                headLightPanelFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                headLightPanelFocusLost(evt);
            }
        });
        headLightPanel.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                headLightPanelKeyPressed(evt);
            }
        });

        headLightNo.setBackground(new java.awt.Color(255, 255, 255));
        headLightNo.setText("No");
        headLightNo.setFocusable(false);
        headLightNo.setOpaque(false);
        headLightNo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                headLightNoActionPerformed(evt);
            }
        });

        headLightYes.setBackground(new java.awt.Color(255, 255, 255));
        headLightYes.setText("Yes");
        headLightYes.setFocusable(false);
        headLightYes.setOpaque(false);
        headLightYes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                headLightYesActionPerformed(evt);
            }
        });

        headLightNC.setBackground(new java.awt.Color(255, 255, 255));
        headLightNC.setText("NC");
        headLightNC.setFocusable(false);
        headLightNC.setOpaque(false);
        headLightNC.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                headLightNCActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout headLightPanelLayout = new javax.swing.GroupLayout(headLightPanel);
        headLightPanel.setLayout(headLightPanelLayout);
        headLightPanelLayout.setHorizontalGroup(
            headLightPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, headLightPanelLayout.createSequentialGroup()
                .addComponent(headLightYes, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(headLightNo, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(headLightNC, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        headLightPanelLayout.setVerticalGroup(
            headLightPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(headLightPanelLayout.createSequentialGroup()
                .addGroup(headLightPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(headLightNo, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(headLightYes, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(headLightNC, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, Short.MAX_VALUE))
        );

        jLabel99.setFont(UIConstant.labelFont);
        jLabel99.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel99.setText("Reverse Horn?:");

        reverseHornPanel.setBackground(new java.awt.Color(255, 255, 255));
        reverseHornPanel.setName("numberVisiblePanel"); // NOI18N
        reverseHornPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                reverseHornPanelMouseClicked(evt);
            }
        });
        reverseHornPanel.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                reverseHornPanelFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                reverseHornPanelFocusLost(evt);
            }
        });
        reverseHornPanel.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                reverseHornPanelKeyPressed(evt);
            }
        });

        reverseHornNo.setBackground(new java.awt.Color(255, 255, 255));
        reverseHornNo.setText("No");
        reverseHornNo.setFocusable(false);
        reverseHornNo.setOpaque(false);
        reverseHornNo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                reverseHornNoActionPerformed(evt);
            }
        });

        reverseHornYes.setBackground(new java.awt.Color(255, 255, 255));
        reverseHornYes.setText("Yes");
        reverseHornYes.setFocusable(false);
        reverseHornYes.setOpaque(false);
        reverseHornYes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                reverseHornYesActionPerformed(evt);
            }
        });

        reverseHornNC.setBackground(new java.awt.Color(255, 255, 255));
        reverseHornNC.setText("NC");
        reverseHornNC.setFocusable(false);
        reverseHornNC.setOpaque(false);
        reverseHornNC.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                reverseHornNCActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout reverseHornPanelLayout = new javax.swing.GroupLayout(reverseHornPanel);
        reverseHornPanel.setLayout(reverseHornPanelLayout);
        reverseHornPanelLayout.setHorizontalGroup(
            reverseHornPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, reverseHornPanelLayout.createSequentialGroup()
                .addComponent(reverseHornYes, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 1, Short.MAX_VALUE)
                .addComponent(reverseHornNo, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(reverseHornNC, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        reverseHornPanelLayout.setVerticalGroup(
            reverseHornPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(reverseHornYes, javax.swing.GroupLayout.DEFAULT_SIZE, 30, Short.MAX_VALUE)
            .addComponent(reverseHornNo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(reverseHornNC, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        jLabel98.setFont(UIConstant.labelFont);
        jLabel98.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel98.setText("Left Indicator?:");

        leftSideIndicatorPanel.setBackground(new java.awt.Color(255, 255, 255));
        leftSideIndicatorPanel.setName("numberVisiblePanel"); // NOI18N
        leftSideIndicatorPanel.setPreferredSize(new java.awt.Dimension(94, 30));
        leftSideIndicatorPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                leftSideIndicatorPanelMouseClicked(evt);
            }
        });
        leftSideIndicatorPanel.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                leftSideIndicatorPanelFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                leftSideIndicatorPanelFocusLost(evt);
            }
        });
        leftSideIndicatorPanel.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                leftSideIndicatorPanelKeyPressed(evt);
            }
        });

        leftSideIndicatorNo.setBackground(new java.awt.Color(255, 255, 255));
        leftSideIndicatorNo.setText("No");
        leftSideIndicatorNo.setFocusable(false);
        leftSideIndicatorNo.setOpaque(false);
        leftSideIndicatorNo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                leftSideIndicatorNoActionPerformed(evt);
            }
        });

        leftSideIndicatorYes.setBackground(new java.awt.Color(255, 255, 255));
        leftSideIndicatorYes.setText("Yes");
        leftSideIndicatorYes.setFocusable(false);
        leftSideIndicatorYes.setOpaque(false);
        leftSideIndicatorYes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                leftSideIndicatorYesActionPerformed(evt);
            }
        });

        leftSideIndicatorNC.setBackground(new java.awt.Color(255, 255, 255));
        leftSideIndicatorNC.setText("NC");
        leftSideIndicatorNC.setFocusable(false);
        leftSideIndicatorNC.setOpaque(false);
        leftSideIndicatorNC.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                leftSideIndicatorNCActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout leftSideIndicatorPanelLayout = new javax.swing.GroupLayout(leftSideIndicatorPanel);
        leftSideIndicatorPanel.setLayout(leftSideIndicatorPanelLayout);
        leftSideIndicatorPanelLayout.setHorizontalGroup(
            leftSideIndicatorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, leftSideIndicatorPanelLayout.createSequentialGroup()
                .addComponent(leftSideIndicatorYes, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(leftSideIndicatorNo, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(leftSideIndicatorNC, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        leftSideIndicatorPanelLayout.setVerticalGroup(
            leftSideIndicatorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(leftSideIndicatorYes, javax.swing.GroupLayout.DEFAULT_SIZE, 30, Short.MAX_VALUE)
            .addComponent(leftSideIndicatorNo, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(leftSideIndicatorNC, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        sealOkPanel.setBackground(java.awt.SystemColor.controlLtHighlight);
        sealOkPanel.setName("sealOkPanel"); // NOI18N
        sealOkPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                sealOkPanelMouseClicked(evt);
            }
        });
        sealOkPanel.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                sealOkPanelFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                sealOkPanelFocusLost(evt);
            }
        });
        sealOkPanel.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                sealOkPanelKeyPressed(evt);
            }
        });

        sealYes.setBackground(new java.awt.Color(255, 255, 255));
        sealYes.setText("Yes");
        sealYes.setFocusPainted(false);
        sealYes.setFocusable(false);
        sealYes.setOpaque(false);
        sealYes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sealYesActionPerformed(evt);
            }
        });

        sealNo.setBackground(new java.awt.Color(255, 255, 255));
        sealNo.setText("No");
        sealNo.setFocusable(false);
        sealNo.setOpaque(false);
        sealNo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sealNoActionPerformed(evt);
            }
        });

        sealNC.setBackground(new java.awt.Color(255, 255, 255));
        sealNC.setText("NC");
        sealNC.setFocusable(false);
        sealNC.setOpaque(false);
        sealNC.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sealNCActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout sealOkPanelLayout = new javax.swing.GroupLayout(sealOkPanel);
        sealOkPanel.setLayout(sealOkPanelLayout);
        sealOkPanelLayout.setHorizontalGroup(
            sealOkPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(sealOkPanelLayout.createSequentialGroup()
                .addComponent(sealYes, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(sealNo, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(sealNC, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        sealOkPanelLayout.setVerticalGroup(
            sealOkPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(sealOkPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(sealYes, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(sealNo, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(sealNC, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jLabel101.setFont(UIConstant.labelFont);
        jLabel101.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel101.setText("Right Indicator?:");

        rightSideIndicatorPanel.setBackground(new java.awt.Color(255, 255, 255));
        rightSideIndicatorPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                rightSideIndicatorPanelMouseClicked(evt);
            }
        });
        rightSideIndicatorPanel.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                rightSideIndicatorPanelFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                rightSideIndicatorPanelFocusLost(evt);
            }
        });
        rightSideIndicatorPanel.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                rightSideIndicatorPanelKeyPressed(evt);
            }
        });

        rightSideIndicatorYes.setBackground(new java.awt.Color(255, 255, 255));
        rightSideIndicatorYes.setText("Yes");
        rightSideIndicatorYes.setFocusable(false);
        rightSideIndicatorYes.setOpaque(false);
        rightSideIndicatorYes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rightSideIndicatorYesActionPerformed(evt);
            }
        });

        rightSideIndicatorNo.setBackground(new java.awt.Color(255, 255, 255));
        rightSideIndicatorNo.setText("No");
        rightSideIndicatorNo.setFocusable(false);
        rightSideIndicatorNo.setOpaque(false);
        rightSideIndicatorNo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rightSideIndicatorNoActionPerformed(evt);
            }
        });

        rightSideIndicatorNC.setBackground(new java.awt.Color(255, 255, 255));
        rightSideIndicatorNC.setText("NC");
        rightSideIndicatorNC.setFocusable(false);
        rightSideIndicatorNC.setOpaque(false);
        rightSideIndicatorNC.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rightSideIndicatorNCActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout rightSideIndicatorPanelLayout = new javax.swing.GroupLayout(rightSideIndicatorPanel);
        rightSideIndicatorPanel.setLayout(rightSideIndicatorPanelLayout);
        rightSideIndicatorPanelLayout.setHorizontalGroup(
            rightSideIndicatorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(rightSideIndicatorPanelLayout.createSequentialGroup()
                .addComponent(rightSideIndicatorYes, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(rightSideIndicatorNo, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(rightSideIndicatorNC)
                .addContainerGap(16, Short.MAX_VALUE))
        );
        rightSideIndicatorPanelLayout.setVerticalGroup(
            rightSideIndicatorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(rightSideIndicatorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(rightSideIndicatorYes, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(rightSideIndicatorNo, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(rightSideIndicatorNC, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel88, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel86, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel85, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(tarpaulinPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(seatBeltPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(sealOkPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 40, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel97, javax.swing.GroupLayout.DEFAULT_SIZE, 130, Short.MAX_VALUE)
                    .addComponent(jLabel89, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(sideMirrorPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(numberVisiblePanel, javax.swing.GroupLayout.DEFAULT_SIZE, 144, Short.MAX_VALUE))
                .addGap(40, 40, 40)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel99, javax.swing.GroupLayout.DEFAULT_SIZE, 130, Short.MAX_VALUE)
                    .addComponent(jLabel90, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel87, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(4, 4, 4)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(headLightPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(reverseHornPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(tailLightPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 141, Short.MAX_VALUE))
                .addGap(40, 40, 40)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel98, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel101, javax.swing.GroupLayout.DEFAULT_SIZE, 130, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(leftSideIndicatorPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 146, Short.MAX_VALUE)
                    .addComponent(rightSideIndicatorPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(163, 163, 163))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jLabel86, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(seatBeltPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGap(6, 6, 6)))
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel88, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(tarpaulinPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jLabel85, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addComponent(sealOkPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(numberVisiblePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel97, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel89, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(sideMirrorPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jLabel99, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGap(8, 8, 8))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jLabel98, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(reverseHornPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(leftSideIndicatorPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(rightSideIndicatorPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addComponent(jLabel87, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(tailLightPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jLabel101, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel90, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(headLightPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))))
                .addContainerGap())
        );

        tarpaulinPanel.getAccessibleContext().setAccessibleDescription("");

        jPanel7.setBackground(java.awt.SystemColor.controlLtHighlight);

        jLabel23.setFont(UIConstant.labelFont);
        jLabel23.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel23.setText("DL ID#:");

        jLabel24.setFont(UIConstant.labelFont);
        jLabel24.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel24.setText("Driver Name:");

        jLabel25.setFont(UIConstant.labelFont);
        jLabel25.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel25.setText("Not Driven By Helper?:");

        jLabel26.setFont(UIConstant.labelFont);
        jLabel26.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel26.setText("BreathLyzer Pass?:");

        breathlizerOkPanel.setBackground(new java.awt.Color(255, 255, 255));
        breathlizerOkPanel.setName("numberVisiblePanel"); // NOI18N
        breathlizerOkPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                breathlizerOkPanelMouseClicked(evt);
            }
        });
        breathlizerOkPanel.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                breathlizerOkPanelFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                breathlizerOkPanelFocusLost(evt);
            }
        });
        breathlizerOkPanel.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                breathlizerOkPanelKeyPressed(evt);
            }
        });

        breathlizerNo.setBackground(new java.awt.Color(255, 255, 255));
        breathlizerNo.setText("No");
        breathlizerNo.setFocusable(false);
        breathlizerNo.setOpaque(false);
        breathlizerNo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                breathlizerNoActionPerformed(evt);
            }
        });

        breathlizerYes.setBackground(new java.awt.Color(255, 255, 255));
        breathlizerYes.setText("Yes");
        breathlizerYes.setFocusable(false);
        breathlizerYes.setOpaque(false);
        breathlizerYes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                breathlizerYesActionPerformed(evt);
            }
        });

        breathlizerNC.setBackground(new java.awt.Color(255, 255, 255));
        breathlizerNC.setText("NC");
        breathlizerNC.setFocusable(false);
        breathlizerNC.setOpaque(false);
        breathlizerNC.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                breathlizerNCActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout breathlizerOkPanelLayout = new javax.swing.GroupLayout(breathlizerOkPanel);
        breathlizerOkPanel.setLayout(breathlizerOkPanelLayout);
        breathlizerOkPanelLayout.setHorizontalGroup(
            breathlizerOkPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, breathlizerOkPanelLayout.createSequentialGroup()
                .addComponent(breathlizerYes, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(breathlizerNo, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(breathlizerNC, javax.swing.GroupLayout.DEFAULT_SIZE, 49, Short.MAX_VALUE))
        );
        breathlizerOkPanelLayout.setVerticalGroup(
            breathlizerOkPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(breathlizerOkPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(breathlizerNo, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(breathlizerYes, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(breathlizerNC, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        HelperPanel.setBackground(new java.awt.Color(255, 255, 255));
        HelperPanel.setName("numberVisiblePanel"); // NOI18N
        HelperPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                HelperPanelMouseClicked(evt);
            }
        });
        HelperPanel.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                HelperPanelKeyPressed(evt);
            }
        });

        helperNo.setBackground(new java.awt.Color(255, 255, 255));
        helperNo.setText("No");
        helperNo.setEnabled(false);
        helperNo.setFocusable(false);
        helperNo.setRequestFocusEnabled(false);
        helperNo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                helperNoActionPerformed(evt);
            }
        });

        helperYes.setBackground(new java.awt.Color(255, 255, 255));
        helperYes.setText("Yes");
        helperYes.setEnabled(false);
        helperYes.setFocusable(false);
        helperYes.setRequestFocusEnabled(false);
        helperYes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                helperYesActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout HelperPanelLayout = new javax.swing.GroupLayout(HelperPanel);
        HelperPanel.setLayout(HelperPanelLayout);
        HelperPanelLayout.setHorizontalGroup(
            HelperPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, HelperPanelLayout.createSequentialGroup()
                .addComponent(helperYes, javax.swing.GroupLayout.DEFAULT_SIZE, 47, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(helperNo, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        HelperPanelLayout.setVerticalGroup(
            HelperPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(HelperPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(helperNo, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(helperYes, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        dlNo.setFont(UIConstant.textFont);
        dlNo.setForeground(UIConstant.textFontColor);
        dlNo.setBorder(null);
        dlNo.setFocusable(false);
        dlNo.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                dlNoFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                dlNoFocusLost(evt);
            }
        });
        dlNo.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                dlNoKeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                dlNoKeyReleased(evt);
            }
        });

        driverName.setFont(UIConstant.textFont);
        driverName.setForeground(UIConstant.textFontColor);
        driverName.setEditable(false);
        driverName.setBackground(new java.awt.Color(255, 255, 255));
        driverName.setBorder(null);
        driverName.setFocusable(false);
        driverName.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                driverNameFocusGained(evt);
            }
        });
        driverName.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                driverNameKeyPressed(evt);
            }
        });

        jLabel9.setFont(UIConstant.labelFont);
        jLabel9.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel9.setText("Gate Pass ID:");

        driverId.setFont(UIConstant.textFont);
        driverId.setForeground(UIConstant.textFontColor);
        driverId.setBorder(null);
        driverId.setFocusable(false);
        driverId.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                driverIdActionPerformed(evt);
            }
        });
        driverId.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                driverIdFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                driverIdFocusLost(evt);
            }
        });
        driverId.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                driverIdKeyPressed(evt);
            }
        });

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel24, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel23, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel9, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel25, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel26, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 145, Short.MAX_VALUE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(dlNo, javax.swing.GroupLayout.DEFAULT_SIZE, 215, Short.MAX_VALUE)
                    .addComponent(driverName, javax.swing.GroupLayout.DEFAULT_SIZE, 215, Short.MAX_VALUE)
                    .addComponent(HelperPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(breathlizerOkPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(driverId, javax.swing.GroupLayout.DEFAULT_SIZE, 215, Short.MAX_VALUE))
                .addGap(10, 10, 10)
                .addComponent(filler1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(filler1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel26, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(breathlizerOkPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(3, 3, 3)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(driverId, javax.swing.GroupLayout.DEFAULT_SIZE, 31, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel23, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(dlNo, javax.swing.GroupLayout.DEFAULT_SIZE, 30, Short.MAX_VALUE))
                .addGap(3, 3, 3)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel24, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(driverName, javax.swing.GroupLayout.DEFAULT_SIZE, 30, Short.MAX_VALUE))
                .addGap(3, 3, 3)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel25, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(HelperPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        clear.setFont(UIConstant.buttonFont);
        clear.setText("Clear");
        clear.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearActionPerformed(evt);
            }
        });
        clear.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                clearKeyPressed(evt);
            }
        });

        manualEntryButton.setFont(UIConstant.buttonFont);
        manualEntryButton.setText("Manual Entry");
        manualEntryButton.setEnabled(true);
        manualEntryButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                manualEntryActionPerformed(evt);
            }
        });
        manualEntryButton.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                manualEntryKeyPressed(evt);
            }
        });

        quickCreate.setFont(UIConstant.buttonFont);
        quickCreate.setText("Open Gate");
        quickCreate.setEnabled(false);
        quickCreate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                quickCreateActionPerformed(evt);
            }
        });
        quickCreate.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                quickCreateKeyPressed(evt);
            }
        });

        photo.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        lblCurrentImageInfo = new JLabel("");
        lblCurrentImageInfo.setVisible(false);
		lblCurrentImageInfo.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
		lblInstruction = new JLabel("", SwingConstants.CENTER);
		lblInstruction.setBorder(BorderFactory.createLineBorder(Color.GRAY));
		lblSteps = new JLabel("", SwingConstants.CENTER);
		lblSteps.setBorder(BorderFactory.createLineBorder(Color.GRAY));
		lblSteps.setOpaque(true);
		lblSteps.setBackground(Color.WHITE);
		lblScore = new JLabel("", SwingConstants.CENTER);
		lblScore.setBorder(BorderFactory.createLineBorder(Color.GRAY));

		progressBar = new JProgressBar();
		progressBar.setOrientation(SwingConstants.VERTICAL);
		progressBar.setMaximum(150);
		
        jPanel8.setBackground(new java.awt.Color(255, 255, 255));
        jPanel8.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel3.setFont(UIConstant.vehicleLabel);
        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel3.setText("Vehicle:");
        jPanel8.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(11, 0, 176, 40));

/*        vehicleName.setFont(UIConstant.vehicleLabel);
        vehicleName.setForeground(UIConstant.textFontColor);
        vehicleName.setEditable(false);
        vehicleName.setText(" NO VEHICLE DETECTED");
        vehicleName.setBorder(null);
        vehicleName.setFocusable(false);
        vehicleName.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                vehicleNameMouseClicked(evt);
            }
        });
        vehicleName.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                vehicleNameActionPerformed(evt);
            }
        });
        vehicleName.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                vehicleNameFocusLost(evt);
            }
        });
        vehicleName.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                vehicleNameKeyPressed(evt);
            }
        });*/
        vehicleName.setFont(UIConstant.textFont);
        vehicleName.setMaximumRowCount(10);
        vehicleName.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Select" }));
        vehicleName.setTextBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 0));
        //vehicleName.setEnabled(false);
        vehicleName.setFocusable(false);
        vehicleName.setKeyEvent(new ComboKeyEvent() {
			
			@Override
			public void onKeyPress(KeyEvent evt) {
				if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
				vehicleNameAction();
				}
				
			}
		});
        vehicleName.setText("");
        jPanel8.add(vehicleName, new org.netbeans.lib.awtextra.AbsoluteConstraints(195, 0, 253, 41));

        jLabel5.setFont(UIConstant.labelFont);
        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel5.setText("Override?:");
        jPanel8.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 47, 177, 32));

        overrides.setEditable(false);
        overrides.setBackground(new java.awt.Color(255, 255, 255));
        overrides.setBorder(null);
        overrides.setFocusable(false);
        overrides.setFont(UIConstant.textFont);
        overrides.setForeground(UIConstant.textFontColor);
        jPanel8.add(overrides, new org.netbeans.lib.awtextra.AbsoluteConstraints(191, 44, 212, 31));

        jLabel20.setFont(UIConstant.labelFont);
        jLabel20.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel20.setText("Supplier:");
        jPanel8.add(jLabel20, new org.netbeans.lib.awtextra.AbsoluteConstraints(446, 45, 147, 29));

        lrDate.setFont(UIConstant.textFont); // NOI18N

        lrDate.setForeground(UIConstant.textFontColor);
        supplierName.setFont(UIConstant.textFont);
        jPanel8.add(supplierName, new org.netbeans.lib.awtextra.AbsoluteConstraints(599, 47, 198, 32));

        fingerInstruction.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jPanel8.add(fingerInstruction, new org.netbeans.lib.awtextra.AbsoluteConstraints(1025, 0, 151, 38));

        captureButton.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        captureButton.setText("Capture Finger Print >>>");
        captureButton.setFocusable(false);
        captureButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                captureButtonActionPerformed(evt);
            }
        });
        captureButton.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                captureButtonKeyPressed(evt);
            }
        });
        jPanel8.add(captureButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(727, 0, 232, 41));

        jLabel10.setFont(UIConstant.labelFont);

        jLabel10.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel10.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel10.setText("Bed Assigned:");
        jPanel8.add(jLabel10, new org.netbeans.lib.awtextra.AbsoluteConstraints(803, 47, 204, 32));

        bedAssigned.setFont(UIConstant.textFont); // NOI18N
        bedAssigned.setForeground(UIConstant.textFontColor);
        jPanel8.add(bedAssigned, new org.netbeans.lib.awtextra.AbsoluteConstraints(1013, 49, 272, 30));

        filler2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));
        filler2.setFocusCycleRoot(true);

        filler3.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));

        digitalClock.setFont(UIConstant.textFont); // NOI18N

        digitalClock.setForeground(UIConstant.textFontColor);
        digitalClock.setFont(new java.awt.Font("Segoe UI", 1, 36)); // NOI18N

        manualSyncButton.setText("Manual Sync");
        manualSyncButton.setFocusable(false);
        manualSyncButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        blocking_reason.setFont(UIConstant.textFont);
        blocking_reason.setForeground(UIConstant.noActionPanelColor);

        //for fingerprint
        fingerPrintPanel = new JPanel();
        fingerPrintPanel.setBackground(java.awt.SystemColor.controlLtHighlight);
        
        javax.swing.GroupLayout panel1Layout = new javax.swing.GroupLayout(fingerPrintPanel);
        fingerPrintPanel.setLayout(panel1Layout);
        panel1Layout.setHorizontalGroup(
            panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panel1Layout.createSequentialGroup()
                .addGroup(panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblScore, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lblInstruction, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblSteps, javax.swing.GroupLayout.DEFAULT_SIZE, 220, Short.MAX_VALUE))
                .addGap(10, 10, 10))
            .addComponent(lblCurrentImageInfo, javax.swing.GroupLayout.DEFAULT_SIZE, 220, Short.MAX_VALUE)
        );
        panel1Layout.setVerticalGroup(
            panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panel1Layout.createSequentialGroup()
                .addGroup(panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(panel1Layout.createSequentialGroup()
                        .addGroup(panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                         .addComponent(lblSteps, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                         .addGap(2,2,2)
                        .addComponent(lblInstruction, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(panel1Layout.createSequentialGroup()
                        .addComponent(lblScore, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(2,2,2)
                        .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblCurrentImageInfo, javax.swing.GroupLayout.DEFAULT_SIZE, 30, Short.MAX_VALUE)
                .addContainerGap())
        );
        
        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(blocking_reason, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addComponent(fingerImageLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 122, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(124, 124, 124)
                        .addComponent(filler2, javax.swing.GroupLayout.PREFERRED_SIZE, 1130, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(200, 200, 200)
                        .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(10, 10, 10)
                        .addComponent(photo, javax.swing.GroupLayout.PREFERRED_SIZE, 158, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(10, 10, 10)
                        .addComponent(fingerPrintPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(10, 10, 10)
                        .addComponent(manualSyncButton, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(310, 310, 310)
                        .addComponent(clear, javax.swing.GroupLayout.PREFERRED_SIZE, 131, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(39, 39, 39)
                        .addComponent(manualEntryButton, javax.swing.GroupLayout.PREFERRED_SIZE, 241, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(39, 39, 39)
                        .addComponent(quickCreate, javax.swing.GroupLayout.PREFERRED_SIZE, 231, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(199, 199, 199)
                        .addComponent(digitalClock, javax.swing.GroupLayout.PREFERRED_SIZE, 179, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(123, 123, 123)
                        .addComponent(filler3, javax.swing.GroupLayout.PREFERRED_SIZE, 1131, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(30, 30, 30)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(0, 0, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(fingerImageLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(blocking_reason, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(filler3, javax.swing.GroupLayout.PREFERRED_SIZE, 1, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 101, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(6, 6, 6)
                .addComponent(filler2, javax.swing.GroupLayout.PREFERRED_SIZE, 1, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(photo, javax.swing.GroupLayout.PREFERRED_SIZE, 165, javax.swing.GroupLayout.PREFERRED_SIZE)
                            ))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                    		.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    		.addComponent(fingerPrintPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(47, 47, 47)
                        .addComponent(manualSyncButton, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(clear, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(manualEntryButton, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(quickCreate, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(digitalClock, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void myComponent() {
        
        
        sealOk.add(sealYes);
        sealOk.add(sealNo);
        sealOk.add(sealNC);
        tarpaulin.add(tarpaulinNo);
        tarpaulin.add(tarpaulinYes);
        tarpaulin.add(tarpaulinNC);
        numberVisible.add(numberVisibleYes);
        numberVisible.add(numberVisibleNo);
        numberVisible.add(numberVisibleNC);
        tailLightOk.add(tailLightYes);
        tailLightOk.add(tailLightNo);
        tailLightOk.add(tailLightNC);
        sideMirrorOk.add(sideMirrorNo);
        sideMirrorOk.add(sideMirrorYes);
        sideMirrorOk.add(sideMirrorNC);
        leftSideIndicator.add(leftSideIndicatorNo);
        leftSideIndicator.add(leftSideIndicatorYes);
        leftSideIndicator.add(leftSideIndicatorNC);
        rightSideIndicator.add(rightSideIndicatorNo);
        rightSideIndicator.add(rightSideIndicatorYes);
        rightSideIndicator.add(rightSideIndicatorNC);
        seatBelt.add(seatBeltNo);
        seatBelt.add(seatBeltYes);
        seatBelt.add(seatBeltNC);
        headLightOk.add(headLightYes);
        headLightOk.add(headLightNo);
        headLightOk.add(headLightNC);
        reverseHornOk.add(reverseHornYes);
        reverseHornOk.add(reverseHornNo);
        reverseHornOk.add(reverseHornNC);
        breathlizerOk.add(breathlizerYes);
        breathlizerOk.add(breathlizerNo);
        breathlizerOk.add(breathlizerNC);
        helperOk.add(helperNo);
        helperOk.add(helperYes);
        
    }

    private void quickCreateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_quickCreateActionPerformed
        quickCreateAndSave();
    }//GEN-LAST:event_quickCreateActionPerformed

    private void clearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearActionPerformed
        clearAction();
    }//GEN-LAST:event_clearActionPerformed

    private void sealYesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sealYesActionPerformed
        sealOkAction();
        if (doCheck) {
            checkQuestions();
        }
    }//GEN-LAST:event_sealYesActionPerformed

    private void sealNoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sealNoActionPerformed
        sealOkAction();
        if (doCheck) {
            checkQuestions();
        }
    }//GEN-LAST:event_sealNoActionPerformed

    private void tailLightYesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tailLightYesActionPerformed
        tailLightAction();
        if (doCheck) {
            checkQuestions();
        }
    }//GEN-LAST:event_tailLightYesActionPerformed
    private void tailLightAction() {
        sealOkPanel.setBackground(UIConstant.PanelWhite);
        tarpaulinPanel.setBackground(UIConstant.PanelWhite);
        sideMirrorPanel.setBackground(UIConstant.PanelWhite);
        tailLightPanel.setBackground(UIConstant.PanelWhite);
        numberVisiblePanel.setBackground(UIConstant.PanelWhite);
        leftSideIndicatorPanel.setBackground(UIConstant.PanelWhite);
        seatBeltPanel.setBackground(UIConstant.PanelWhite);
        headLightPanel.setBackground(UIConstant.focusPanelColor);
        headLightPanel.requestFocusInWindow();
        reverseHornPanel.setBackground(UIConstant.PanelWhite);
        breathlizerOkPanel.setBackground(UIConstant.PanelWhite);
    }
    private void tailLightNoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tailLightNoActionPerformed
        tailLightAction();
        if (doCheck) {
            checkQuestions();
        }
    }//GEN-LAST:event_tailLightNoActionPerformed

    private void tarpaulinYesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tarpaulinYesActionPerformed
        tarpaulinAction();
        if (doCheck) {
            checkQuestions();
        }
    }//GEN-LAST:event_tarpaulinYesActionPerformed

    private void tarpaulinNoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tarpaulinNoActionPerformed
        tarpaulinAction();
        if (doCheck) {
            checkQuestions();
        }
    }//GEN-LAST:event_tarpaulinNoActionPerformed

    private void sideMirrorYesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sideMirrorYesActionPerformed
        sideMirrorAction();
        if (doCheck) {
            checkQuestions();
        }
    }//GEN-LAST:event_sideMirrorYesActionPerformed

    private void sideMirrorNoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sideMirrorNoActionPerformed
        // TODO add your handling code here:
        sideMirrorAction();
        if (doCheck) {
            checkQuestions();
        }
    }//GEN-LAST:event_sideMirrorNoActionPerformed

    private void sealOkPanelKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_sealOkPanelKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            numberVisiblePanel.requestFocusInWindow();
            numberVisiblePanel.setBackground(UIConstant.focusPanelColor);
            if (!sealYes.isSelected() && !sealNo.isSelected() && !sealNC.isSelected()) {
                sealOkPanel.setBackground(UIConstant.focusPanelColor);
            } else {
                sealOkPanel.setBackground(UIConstant.PanelWhite);
            }
            if (doCheck) {
                checkQuestions();
            }
        } else if (evt.getKeyCode() == KeyEvent.VK_1 || evt.getKeyCode() == KeyEvent.VK_NUMPAD1) {
            sealOkPanel.setBackground(Color.WHITE);
            sealYes.setSelected(true);
            numberVisiblePanel.setBackground(UIConstant.focusPanelColor);
            numberVisiblePanel.requestFocusInWindow();
            if (doCheck) {
                checkQuestions();
            }
        } else if (evt.getKeyCode() == KeyEvent.VK_2 || evt.getKeyCode() == KeyEvent.VK_NUMPAD2) {
            sealOkPanel.setBackground(Color.WHITE);
            sealNo.setSelected(true);
            numberVisiblePanel.setBackground(UIConstant.focusPanelColor);
            numberVisiblePanel.requestFocusInWindow();
            if (doCheck) {
                checkQuestions();
            }
        } else if (evt.getKeyCode() == KeyEvent.VK_3 || evt.getKeyCode() == KeyEvent.VK_NUMPAD3) {
            sealOkPanel.setBackground(Color.WHITE);
            sealNC.setSelected(true);
            numberVisiblePanel.setBackground(UIConstant.focusPanelColor);
            numberVisiblePanel.requestFocusInWindow();
            if (doCheck) {
                checkQuestions();
            }
        }
    }//GEN-LAST:event_sealOkPanelKeyPressed

    private void tarpaulinPanelKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tarpaulinPanelKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            if (!tarpaulinYes.isSelected() && !tarpaulinNo.isSelected() && !tarpaulinNC.isSelected()) {
                tarpaulinPanel.setBackground(UIConstant.focusPanelColor);
            } else {
                tarpaulinPanel.setBackground(UIConstant.PanelWhite);
            }
            sealOkPanel.requestFocusInWindow();
            sealOkPanel.setBackground(UIConstant.focusPanelColor);
            if (doCheck) {
                checkQuestions();
            }
        } else if (evt.getKeyCode() == KeyEvent.VK_1 || evt.getKeyCode() == KeyEvent.VK_NUMPAD1) {
            tarpaulinYes.setSelected(true);
            tarpaulinPanel.setBackground(Color.WHITE);
            sealOkPanel.setBackground(UIConstant.focusPanelColor);
            sealOkPanel.requestFocusInWindow();
            if (doCheck) {
                checkQuestions();
            }
        } else if (evt.getKeyCode() == KeyEvent.VK_2 || evt.getKeyCode() == KeyEvent.VK_NUMPAD2) {
            tarpaulinPanel.setBackground(Color.WHITE);
            tarpaulinNo.setSelected(true);
            sealOkPanel.setBackground(UIConstant.focusPanelColor);
            sealOkPanel.requestFocusInWindow();
            if (doCheck) {
                checkQuestions();
            }
        } else if (evt.getKeyCode() == KeyEvent.VK_3 || evt.getKeyCode() == KeyEvent.VK_NUMPAD3) {
            tarpaulinPanel.setBackground(Color.WHITE);
            tarpaulinNC.setSelected(true);
            sealOkPanel.setBackground(UIConstant.focusPanelColor);
            sealOkPanel.requestFocusInWindow();
            if (doCheck) {
                checkQuestions();
            }
        }
    }//GEN-LAST:event_tarpaulinPanelKeyPressed

    private void numberVisibleNoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_numberVisibleNoActionPerformed
        numberVisibleAction();
        if (doCheck) {
            checkQuestions();
        }
    }//GEN-LAST:event_numberVisibleNoActionPerformed

    private void numberVisibleYesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_numberVisibleYesActionPerformed
        numberVisibleAction();
        if (doCheck) {
            checkQuestions();
        }
    }//GEN-LAST:event_numberVisibleYesActionPerformed

    private void numberVisiblePanelKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_numberVisiblePanelKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            if (!numberVisibleNo.isSelected() && !numberVisibleYes.isSelected() && !numberVisibleNC.isSelected()) {
                numberVisiblePanel.setBackground(UIConstant.focusPanelColor);
            } else {
                numberVisiblePanel.setBackground(UIConstant.PanelWhite);
            }
            sideMirrorPanel.requestFocusInWindow();
            sideMirrorPanel.setBackground(UIConstant.focusPanelColor);
            if (doCheck) {
                checkQuestions();
            }

        } else if (evt.getKeyCode() == KeyEvent.VK_1 || evt.getKeyCode() == KeyEvent.VK_NUMPAD1) {
            numberVisibleYes.setSelected(true);
            numberVisiblePanel.setBackground(Color.WHITE);
            sideMirrorPanel.setBackground(UIConstant.focusPanelColor);
            sideMirrorPanel.requestFocusInWindow();
            if (doCheck) {
                checkQuestions();
            }
        } else if (evt.getKeyCode() == KeyEvent.VK_2 || evt.getKeyCode() == KeyEvent.VK_NUMPAD2) {
            numberVisibleNo.setSelected(true);
            numberVisiblePanel.setBackground(Color.WHITE);
            sideMirrorPanel.setBackground(UIConstant.focusPanelColor);
            sideMirrorPanel.requestFocusInWindow();
            if (doCheck) {
                checkQuestions();
            }
        } else if (evt.getKeyCode() == KeyEvent.VK_3 || evt.getKeyCode() == KeyEvent.VK_NUMPAD3) {
            numberVisibleNC.setSelected(true);
            numberVisiblePanel.setBackground(Color.WHITE);
            sideMirrorPanel.setBackground(UIConstant.focusPanelColor);
            sideMirrorPanel.requestFocusInWindow();
            if (doCheck) {
                checkQuestions();
            }
        }
    }//GEN-LAST:event_numberVisiblePanelKeyPressed

    private void tarpaulinYesKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tarpaulinYesKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_tarpaulinYesKeyPressed

    private void tarpaulinNoKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tarpaulinNoKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_tarpaulinNoKeyPressed

    private void leftSideIndicatorNoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_leftSideIndicatorNoActionPerformed
        leftSideIndicatorAction();
        if (doCheck) {
            checkQuestions();
        }
    }//GEN-LAST:event_leftSideIndicatorNoActionPerformed

    private void leftSideIndicatorYesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_leftSideIndicatorYesActionPerformed
        leftSideIndicatorAction();
        if (doCheck) {
            checkQuestions();
        }
    }//GEN-LAST:event_leftSideIndicatorYesActionPerformed

    private void leftSideIndicatorPanelKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_leftSideIndicatorPanelKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            if (!leftSideIndicatorYes.isSelected() && !leftSideIndicatorNo.isSelected() && !leftSideIndicatorNC.isSelected()) {
                leftSideIndicatorPanel.setBackground(UIConstant.focusPanelColor);
            } else {
                leftSideIndicatorPanel.setBackground(UIConstant.PanelWhite);
            }
            rightSideIndicatorPanel.requestFocusInWindow();
            rightSideIndicatorPanel.setBackground(UIConstant.focusPanelColor);
            if (doCheck) {
                checkQuestions();
            }
        } else if (evt.getKeyCode() == KeyEvent.VK_1 || evt.getKeyCode() == KeyEvent.VK_NUMPAD1) {
            leftSideIndicatorYes.setSelected(true);
            leftSideIndicatorPanel.setBackground(Color.WHITE);
            rightSideIndicatorPanel.requestFocusInWindow();
            rightSideIndicatorPanel.setBackground(UIConstant.focusPanelColor);
            if (doCheck) {
                checkQuestions();
            }
        } else if (evt.getKeyCode() == KeyEvent.VK_2 || evt.getKeyCode() == KeyEvent.VK_NUMPAD2) {
            leftSideIndicatorNo.setSelected(true);
            leftSideIndicatorPanel.setBackground(Color.WHITE);
            rightSideIndicatorPanel.requestFocusInWindow();
            rightSideIndicatorPanel.setBackground(UIConstant.focusPanelColor);
            if (doCheck) {
                checkQuestions();
            }
        } else if (evt.getKeyCode() == KeyEvent.VK_3 || evt.getKeyCode() == KeyEvent.VK_NUMPAD3) {
            leftSideIndicatorNC.setSelected(true);
            leftSideIndicatorPanel.setBackground(Color.WHITE);
            rightSideIndicatorPanel.requestFocusInWindow();
            rightSideIndicatorPanel.setBackground(UIConstant.focusPanelColor);
            if (doCheck) {
                checkQuestions();
            }
        }

    }//GEN-LAST:event_leftSideIndicatorPanelKeyPressed

    private void reverseHornNoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_reverseHornNoActionPerformed
        reverseHornAction();
        if (doCheck) {
            checkQuestions();
        }
    }//GEN-LAST:event_reverseHornNoActionPerformed

    private void reverseHornYesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_reverseHornYesActionPerformed
        reverseHornAction();
        if (doCheck) {
            checkQuestions();
        }
    }//GEN-LAST:event_reverseHornYesActionPerformed

    private void reverseHornPanelKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_reverseHornPanelKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            if (!reverseHornYes.isSelected() && !reverseHornNo.isSelected() && !reverseHornNC.isSelected()) {
                reverseHornPanel.setBackground(UIConstant.focusPanelColor);
            } else {
                reverseHornPanel.setBackground(UIConstant.PanelWhite);
            }
            tailLightPanel.requestFocusInWindow();
            tailLightPanel.setBackground(UIConstant.focusPanelColor);
            if (doCheck) {
                checkQuestions();
            }
        } else if (evt.getKeyCode() == KeyEvent.VK_1 || evt.getKeyCode() == KeyEvent.VK_NUMPAD1) {
            reverseHornYes.setSelected(true);
            reverseHornPanel.setBackground(Color.WHITE);
            tailLightPanel.requestFocusInWindow();
            tailLightPanel.setBackground(UIConstant.focusPanelColor);
            if (doCheck) {
                checkQuestions();
            }
        } else if (evt.getKeyCode() == KeyEvent.VK_2 || evt.getKeyCode() == KeyEvent.VK_NUMPAD2) {
            reverseHornNo.setSelected(true);
            reverseHornPanel.setBackground(Color.WHITE);
            tailLightPanel.requestFocusInWindow();
            tailLightPanel.setBackground(UIConstant.focusPanelColor);
            if (doCheck) {
                checkQuestions();
            }
        } else if (evt.getKeyCode() == KeyEvent.VK_3 || evt.getKeyCode() == KeyEvent.VK_NUMPAD3) {
            reverseHornNC.setSelected(true);
            reverseHornPanel.setBackground(Color.WHITE);
            tailLightPanel.requestFocusInWindow();
            tailLightPanel.setBackground(UIConstant.focusPanelColor);
            if (doCheck) {
                checkQuestions();
            }
        }

    }//GEN-LAST:event_reverseHornPanelKeyPressed

    private void headLightNoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_headLightNoActionPerformed
        headLightAction();
        if (doCheck) {
            checkQuestions();
        }
    }//GEN-LAST:event_headLightNoActionPerformed

    private void headLightYesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_headLightYesActionPerformed
        headLightAction();
        if (doCheck) {
            checkQuestions();
        }
    }//GEN-LAST:event_headLightYesActionPerformed

    private void headLightPanelKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_headLightPanelKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            if (!headLightYes.isSelected() && !headLightNo.isSelected() && !headLightNC.isSelected()) {
                headLightPanel.setBackground(UIConstant.focusPanelColor);
            } else {
                headLightPanel.setBackground(UIConstant.PanelWhite);
            }
            leftSideIndicatorPanel.requestFocusInWindow();
            leftSideIndicatorPanel.setBackground(UIConstant.focusPanelColor);
            if (doCheck) {
                checkQuestions();
            }
        } else if (evt.getKeyCode() == KeyEvent.VK_1 || evt.getKeyCode() == KeyEvent.VK_NUMPAD1) {
            headLightYes.setSelected(true);
            headLightPanel.setBackground(Color.WHITE);
            leftSideIndicatorPanel.requestFocusInWindow();
            leftSideIndicatorPanel.setBackground(UIConstant.focusPanelColor);
            if (doCheck) {
                checkQuestions();
            }
        } else if (evt.getKeyCode() == KeyEvent.VK_2 || evt.getKeyCode() == KeyEvent.VK_NUMPAD2) {
            headLightNo.setSelected(true);
            headLightPanel.setBackground(Color.WHITE);
            leftSideIndicatorPanel.requestFocusInWindow();
            leftSideIndicatorPanel.setBackground(UIConstant.focusPanelColor);
            if (doCheck) {
                checkQuestions();
            }
        } else if (evt.getKeyCode() == KeyEvent.VK_3 || evt.getKeyCode() == KeyEvent.VK_NUMPAD3) {
            headLightNC.setSelected(true);
            headLightPanel.setBackground(Color.WHITE);
            leftSideIndicatorPanel.requestFocusInWindow();
            leftSideIndicatorPanel.setBackground(UIConstant.focusPanelColor);
            if (doCheck) {
                checkQuestions();
            }
        }

    }//GEN-LAST:event_headLightPanelKeyPressed

    private void seatBeltNoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_seatBeltNoActionPerformed
        seatBeltAction();
       /* if (!Utils.isNull(vehicleName.getText()) && !(vehicleName.getText().trim()).equalsIgnoreCase("NO VEHICLE DETECTED") && !isFingerCaptureRunning && isMorphoExist) {
            fingurePrintCaptureService.start();
            isFingerCaptureRunning = true;
        }*/
        startFingerCaptureService();
    }//GEN-LAST:event_seatBeltNoActionPerformed

    private void seatBeltYesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_seatBeltYesActionPerformed
        seatBeltAction();
       /* if (!Utils.isNull(vehicleName.getText()) && !(vehicleName.getText().trim()).equalsIgnoreCase("NO VEHICLE DETECTED") && !isFingerCaptureRunning) {
            fingurePrintCaptureService.start();
            isFingerCaptureRunning = true;
        }*/
        startFingerCaptureService();
    }//GEN-LAST:event_seatBeltYesActionPerformed

    private void seatBeltPanelKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_seatBeltPanelKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            if (!seatBeltYes.isSelected() && !seatBeltNo.isSelected() && !seatBeltNC.isSelected()) {
                seatBeltPanel.setBackground(UIConstant.focusPanelColor);
            } else {
                seatBeltPanel.setBackground(UIConstant.PanelWhite);
            }
            tarpaulinPanel.requestFocusInWindow();
            tarpaulinPanel.setBackground(UIConstant.focusPanelColor);
            if (doCheck) {
                checkQuestions();
            }
            /*if (!Utils.isNull(vehicleName.getText()) && !(vehicleName.getText().trim()).equalsIgnoreCase("NO VEHICLE DETECTED") && !isFingerCaptureRunning && isMorphoExist) {
                fingurePrintCaptureService.start();
                isFingerCaptureRunning = true;
            }*/
            startFingerCaptureService();
        } else if (evt.getKeyCode() == KeyEvent.VK_1 || evt.getKeyCode() == KeyEvent.VK_NUMPAD1) {
            seatBeltYes.setSelected(true);
            seatBeltPanel.setBackground(Color.WHITE);
            tarpaulinPanel.requestFocus();
            tarpaulinPanel.setBackground(UIConstant.focusPanelColor);
            if (doCheck) {
                checkQuestions();
            }
            /*if (!Utils.isNull(vehicleName.getText()) && !(vehicleName.getText().trim()).equalsIgnoreCase("NO VEHICLE DETECTED") && !isFingerCaptureRunning && isMorphoExist) {
                fingurePrintCaptureService.start();
                isFingerCaptureRunning = true;
            }*/
            startFingerCaptureService();
        } else if (evt.getKeyCode() == KeyEvent.VK_2 || evt.getKeyCode() == KeyEvent.VK_NUMPAD2) {
            seatBeltNo.setSelected(true);
            seatBeltPanel.setBackground(Color.WHITE);
            tarpaulinPanel.requestFocus();
            tarpaulinPanel.setBackground(UIConstant.focusPanelColor);
            if (doCheck) {
                checkQuestions();
            }
            /*if (!Utils.isNull(vehicleName.getText()) && !(vehicleName.getText().trim()).equalsIgnoreCase("NO VEHICLE DETECTED") && !isFingerCaptureRunning && isMorphoExist) {
                fingurePrintCaptureService.start();
                isFingerCaptureRunning = true;
            }*/
            startFingerCaptureService();
        } else if (evt.getKeyCode() == KeyEvent.VK_3 || evt.getKeyCode() == KeyEvent.VK_NUMPAD3) {
            seatBeltNC.setSelected(true);
            seatBeltPanel.setBackground(Color.WHITE);
            tarpaulinPanel.requestFocus();
            tarpaulinPanel.setBackground(UIConstant.focusPanelColor);
            if (doCheck) {
                checkQuestions();
            }
           /* if (!Utils.isNull(vehicleName.getText()) && !(vehicleName.getText().trim()).equalsIgnoreCase("NO VEHICLE DETECTED") && !isFingerCaptureRunning && isMorphoExist) {
                fingurePrintCaptureService.start();
                isFingerCaptureRunning = true;
            }*/
            startFingerCaptureService();
        }


    }//GEN-LAST:event_seatBeltPanelKeyPressed

    private void breathlizerNoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_breathlizerNoActionPerformed
        breathlizerOkPanel.setBackground(UIConstant.PanelWhite);
        setQuestionsBlocking(Status.TPRQuestion.breathLyzerOk, UIConstant.NO);
        /*
        if(tprBlockManager != null && tprBlockManager.useForBlocking(Status.TPRQuestion.breathLyzerOk) && tprRecord != null){
        	enableDenyEntry(true);
        	
        	blocking_reason.setText(RFIDDataProcessor.getTprBlockStatus(tprRecord, blockInstructionBreathelizer, true));
        }*/
        checkQuestions();
    }//GEN-LAST:event_breathlizerNoActionPerformed
    private void enableDenyEntry(boolean show) {
        if (show || vehicleBlackListed) {
        	isRequestOverride = true;
        	overrides.setText("BLOCKED");
        	quickCreate.setText("Request Override");
        } else {
        	isRequestOverride = false;
        	overrides.setText("NOT_BLOCKED");
        	quickCreate.setText("Open Gate");
        }
    }
    private void breathlizerYesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_breathlizerYesActionPerformed
        breathlizerOkPanel.setBackground(UIConstant.PanelWhite);
        setQuestionsBlocking(Status.TPRQuestion.breathLyzerOk, UIConstant.YES);
        /*if(tprBlockManager != null && tprBlockManager.useForBlocking(Status.TPRQuestion.breathLyzerOk)){
        	enableDenyEntry(false);
        	blocking_reason.setText(RFIDDataProcessor.getTprBlockStatus(tprRecord, blockInstructionBreathelizer, false));
        }*/
        checkQuestions();
    }//GEN-LAST:event_breathlizerYesActionPerformed

    private void breathlizerOkPanelKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_breathlizerOkPanelKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            if (!breathlizerNo.isSelected() && !breathlizerYes.isSelected() && !breathlizerNC.isSelected()) {
                breathlizerOkPanel.setBackground(UIConstant.focusPanelColor);
            } else {
                breathlizerOkPanel.setBackground(UIConstant.PanelWhite);
            }
            getWhiteBackGrounds();
            /*if (quickCreate.isEnabled()) {
                quickCreate.requestFocusInWindow();
            } else {
                manualEntryButton.requestFocusInWindow();
            }*/
            quickCreate.requestFocusInWindow();
            checkQuestions();
        } else if (evt.getKeyCode() == KeyEvent.VK_1 || evt.getKeyCode() == KeyEvent.VK_NUMPAD1) {
            breathlizerYes.setSelected(true);
            breathlizerOkPanel.setBackground(Color.WHITE);
            getWhiteBackGrounds();
//            denyEntry.setEnabled(false);
//            quickCreate.setEnabled(true);
//            quickCreate.requestFocusInWindow();
            setQuestionsBlocking(Status.TPRQuestion.breathLyzerOk, UIConstant.YES);
            /*if(tprBlockManager != null && tprBlockManager.useForBlocking(Status.TPRQuestion.breathLyzerOk)){
            	enableDenyEntry(false);
            	blocking_reason.setText(RFIDDataProcessor.getTprBlockStatus(tprRecord, blockInstructionBreathelizer, false));
            }*/
            checkQuestions();
        } else if (evt.getKeyCode() == KeyEvent.VK_2 || evt.getKeyCode() == KeyEvent.VK_NUMPAD2) {
            breathlizerNo.setSelected(true);
            breathlizerOkPanel.setBackground(Color.WHITE);

            getWhiteBackGrounds();
//            denyEntry.setEnabled(true);
//            quickCreate.setEnabled(false);
//            denyEntry.requestFocusInWindow();
            setQuestionsBlocking(Status.TPRQuestion.breathLyzerOk, UIConstant.NO);
            /*if(tprBlockManager != null && tprBlockManager.useForBlocking(Status.TPRQuestion.breathLyzerOk)){
            	enableDenyEntry(true);
            	blocking_reason.setText(RFIDDataProcessor.getTprBlockStatus(tprRecord, blockInstructionBreathelizer, true));
            }*/
            checkQuestions();
        } else if (evt.getKeyCode() == KeyEvent.VK_3 || evt.getKeyCode() == KeyEvent.VK_NUMPAD3) {
            breathlizerNC.setSelected(true);
            breathlizerOkPanel.setBackground(Color.WHITE);

            getWhiteBackGrounds();
//            denyEntry.setEnabled(false);
//            quickCreate.setEnabled(true);
//            quickCreate.requestFocusInWindow();
            setQuestionsBlocking(Status.TPRQuestion.breathLyzerOk, UIConstant.NC);
            /*if(tprBlockManager != null && tprBlockManager.useForBlocking(Status.TPRQuestion.breathLyzerOk)){
            	enableDenyEntry(false);
            	blocking_reason.setText(RFIDDataProcessor.getTprBlockStatus(tprRecord, blockInstructionBreathelizer, false));
            }*/
            checkQuestions();
        }
    }//GEN-LAST:event_breathlizerOkPanelKeyPressed

    private void tailLightPanelKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tailLightPanelKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            if (!tailLightNo.isSelected() && !tailLightYes.isSelected() && !tailLightNC.isSelected()) {
                tailLightPanel.setBackground(UIConstant.focusPanelColor);
            } else {
                tailLightPanel.setBackground(UIConstant.PanelWhite);
            }
            headLightPanel.requestFocusInWindow();
            headLightPanel.setBackground(UIConstant.focusPanelColor);
            if (doCheck) {
                checkQuestions();
            }
        } else if (evt.getKeyCode() == KeyEvent.VK_1 || evt.getKeyCode() == KeyEvent.VK_NUMPAD1) {
            tailLightYes.setSelected(true);
            tailLightPanel.setBackground(Color.WHITE);
            headLightPanel.requestFocusInWindow();
            headLightPanel.setBackground(UIConstant.focusPanelColor);
            if (doCheck) {
                checkQuestions();
            }
        } else if (evt.getKeyCode() == KeyEvent.VK_2 || evt.getKeyCode() == KeyEvent.VK_NUMPAD2) {
            tailLightNo.setSelected(true);
            tailLightPanel.setBackground(Color.WHITE);
            headLightPanel.requestFocusInWindow();
            headLightPanel.setBackground(UIConstant.focusPanelColor);
            if (doCheck) {
                checkQuestions();
            }
        } else if (evt.getKeyCode() == KeyEvent.VK_3 || evt.getKeyCode() == KeyEvent.VK_NUMPAD3) {
            tailLightNC.setSelected(true);
            tailLightPanel.setBackground(Color.WHITE);
            headLightPanel.requestFocusInWindow();
            headLightPanel.setBackground(UIConstant.focusPanelColor);
            if (doCheck) {
                checkQuestions();
            }
        }
    }//GEN-LAST:event_tailLightPanelKeyPressed

    private void sideMirrorPanelKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_sideMirrorPanelKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            if (!sideMirrorYes.isSelected() && !sideMirrorNo.isSelected() && !sideMirrorNC.isSelected()) {
                sideMirrorPanel.setBackground(UIConstant.focusPanelColor);
            } else {
                sideMirrorPanel.setBackground(UIConstant.PanelWhite);
            }
            reverseHornPanel.requestFocusInWindow();
            reverseHornPanel.setBackground(UIConstant.focusPanelColor);
            if (doCheck) {
                checkQuestions();
            }
        } else if (evt.getKeyCode() == KeyEvent.VK_1 || evt.getKeyCode() == KeyEvent.VK_NUMPAD1) {
            sideMirrorYes.setSelected(true);
            sideMirrorPanel.setBackground(Color.WHITE);
            reverseHornPanel.requestFocus();
            reverseHornPanel.setBackground(UIConstant.focusPanelColor);
            if (doCheck) {
                checkQuestions();
            }
        } else if (evt.getKeyCode() == KeyEvent.VK_2 || evt.getKeyCode() == KeyEvent.VK_NUMPAD2) {
            sideMirrorNo.setSelected(true);
            sideMirrorPanel.setBackground(Color.WHITE);
            reverseHornPanel.requestFocus();
            reverseHornPanel.setBackground(UIConstant.focusPanelColor);
            if (doCheck) {
                checkQuestions();
            }
        } else if (evt.getKeyCode() == KeyEvent.VK_3 || evt.getKeyCode() == KeyEvent.VK_NUMPAD3) {
            sideMirrorNC.setSelected(true);
            sideMirrorPanel.setBackground(Color.WHITE);
            reverseHornPanel.requestFocus();
            reverseHornPanel.setBackground(UIConstant.focusPanelColor);
            if (doCheck) {
                checkQuestions();
            }
        }
    }//GEN-LAST:event_sideMirrorPanelKeyPressed

    private void sealOkPanelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_sealOkPanelMouseClicked
        sealOkPanel.requestFocusInWindow();
        sealOkPanel.setBackground(UIConstant.focusPanelColor);
        tarpaulinPanel.setBackground(UIConstant.PanelWhite);
        numberVisiblePanel.setBackground(UIConstant.PanelWhite);
        tailLightPanel.setBackground(UIConstant.PanelWhite);
        sideMirrorPanel.setBackground(UIConstant.PanelWhite);
        leftSideIndicatorPanel.setBackground(UIConstant.PanelWhite);
        rightSideIndicatorPanel.setBackground(UIConstant.PanelWhite);
        seatBeltPanel.setBackground(UIConstant.PanelWhite);
        headLightPanel.setBackground(UIConstant.PanelWhite);
        reverseHornPanel.setBackground(UIConstant.PanelWhite);
        breathlizerOkPanel.setBackground(UIConstant.PanelWhite);
    }//GEN-LAST:event_sealOkPanelMouseClicked

    private void tarpaulinPanelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tarpaulinPanelMouseClicked
        sealOkPanel.setBackground(UIConstant.PanelWhite);
        tarpaulinPanel.requestFocusInWindow();
        tarpaulinPanel.setBackground(UIConstant.focusPanelColor);
        numberVisiblePanel.setBackground(UIConstant.PanelWhite);
        tailLightPanel.setBackground(UIConstant.PanelWhite);
        sideMirrorPanel.setBackground(UIConstant.PanelWhite);
        leftSideIndicatorPanel.setBackground(UIConstant.PanelWhite);
        rightSideIndicatorPanel.setBackground(UIConstant.PanelWhite);
        seatBeltPanel.setBackground(UIConstant.PanelWhite);
        headLightPanel.setBackground(UIConstant.PanelWhite);
        reverseHornPanel.setBackground(UIConstant.PanelWhite);
        breathlizerOkPanel.setBackground(UIConstant.PanelWhite);
    }//GEN-LAST:event_tarpaulinPanelMouseClicked

    private void numberVisiblePanelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_numberVisiblePanelMouseClicked
        sealOkPanel.setBackground(UIConstant.PanelWhite);
        tarpaulinPanel.setBackground(UIConstant.PanelWhite);
        numberVisiblePanel.requestFocusInWindow();
        numberVisiblePanel.setBackground(UIConstant.focusPanelColor);
        tailLightPanel.setBackground(UIConstant.PanelWhite);
        sideMirrorPanel.setBackground(UIConstant.PanelWhite);
        leftSideIndicatorPanel.setBackground(UIConstant.PanelWhite);
        rightSideIndicatorPanel.setBackground(UIConstant.PanelWhite);
        seatBeltPanel.setBackground(UIConstant.PanelWhite);
        headLightPanel.setBackground(UIConstant.PanelWhite);
        reverseHornPanel.setBackground(UIConstant.PanelWhite);
        breathlizerOkPanel.setBackground(UIConstant.PanelWhite);
    }//GEN-LAST:event_numberVisiblePanelMouseClicked

    private void tailLightPanelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tailLightPanelMouseClicked
        sealOkPanel.setBackground(UIConstant.PanelWhite);
        tarpaulinPanel.setBackground(UIConstant.PanelWhite);
        numberVisiblePanel.setBackground(UIConstant.PanelWhite);
        tailLightPanel.requestFocusInWindow();
        tailLightPanel.setBackground(UIConstant.focusPanelColor);
        sideMirrorPanel.setBackground(UIConstant.PanelWhite);
        leftSideIndicatorPanel.setBackground(UIConstant.PanelWhite);
        rightSideIndicatorPanel.setBackground(UIConstant.PanelWhite);
        seatBeltPanel.setBackground(UIConstant.PanelWhite);
        headLightPanel.setBackground(UIConstant.PanelWhite);
        reverseHornPanel.setBackground(UIConstant.PanelWhite);
        breathlizerOkPanel.setBackground(UIConstant.PanelWhite);
    }//GEN-LAST:event_tailLightPanelMouseClicked

    private void sideMirrorPanelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_sideMirrorPanelMouseClicked
        sealOkPanel.setBackground(UIConstant.PanelWhite);
        tarpaulinPanel.setBackground(UIConstant.PanelWhite);
        numberVisiblePanel.setBackground(UIConstant.PanelWhite);
        tailLightPanel.setBackground(UIConstant.PanelWhite);
        sideMirrorPanel.requestFocusInWindow();
        sideMirrorPanel.setBackground(UIConstant.focusPanelColor);
        leftSideIndicatorPanel.setBackground(UIConstant.PanelWhite);
        rightSideIndicatorPanel.setBackground(UIConstant.PanelWhite);
        seatBeltPanel.setBackground(UIConstant.PanelWhite);
        headLightPanel.setBackground(UIConstant.PanelWhite);
        reverseHornPanel.setBackground(UIConstant.PanelWhite);
        breathlizerOkPanel.setBackground(UIConstant.PanelWhite);
    }//GEN-LAST:event_sideMirrorPanelMouseClicked

    private void leftSideIndicatorPanelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_leftSideIndicatorPanelMouseClicked
        sealOkPanel.setBackground(UIConstant.PanelWhite);
        tarpaulinPanel.setBackground(UIConstant.PanelWhite);
        numberVisiblePanel.setBackground(UIConstant.PanelWhite);
        tailLightPanel.setBackground(UIConstant.PanelWhite);
        sideMirrorPanel.setBackground(UIConstant.PanelWhite);
        leftSideIndicatorPanel.requestFocusInWindow();
        leftSideIndicatorPanel.setBackground(UIConstant.focusPanelColor);
        rightSideIndicatorPanel.setBackground(UIConstant.PanelWhite);
        seatBeltPanel.setBackground(UIConstant.PanelWhite);
        headLightPanel.setBackground(UIConstant.PanelWhite);
        reverseHornPanel.setBackground(UIConstant.PanelWhite);
        breathlizerOkPanel.setBackground(UIConstant.PanelWhite);
    }//GEN-LAST:event_leftSideIndicatorPanelMouseClicked

    private void seatBeltPanelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_seatBeltPanelMouseClicked
        sealOkPanel.setBackground(UIConstant.PanelWhite);
        tarpaulinPanel.setBackground(UIConstant.PanelWhite);
        numberVisiblePanel.setBackground(UIConstant.PanelWhite);
        tailLightPanel.setBackground(UIConstant.PanelWhite);
        sideMirrorPanel.setBackground(UIConstant.PanelWhite);
        leftSideIndicatorPanel.setBackground(UIConstant.PanelWhite);
        rightSideIndicatorPanel.setBackground(UIConstant.PanelWhite);
        seatBeltPanel.requestFocusInWindow();
        seatBeltPanel.setBackground(UIConstant.focusPanelColor);
        headLightPanel.setBackground(UIConstant.PanelWhite);
        reverseHornPanel.setBackground(UIConstant.PanelWhite);
        breathlizerOkPanel.setBackground(UIConstant.PanelWhite);
    }//GEN-LAST:event_seatBeltPanelMouseClicked

    private void headLightPanelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_headLightPanelMouseClicked
        sealOkPanel.setBackground(UIConstant.PanelWhite);
        tarpaulinPanel.setBackground(UIConstant.PanelWhite);
        numberVisiblePanel.setBackground(UIConstant.PanelWhite);
        tailLightPanel.setBackground(UIConstant.PanelWhite);
        sideMirrorPanel.setBackground(UIConstant.PanelWhite);
        leftSideIndicatorPanel.setBackground(UIConstant.PanelWhite);
        rightSideIndicatorPanel.setBackground(UIConstant.PanelWhite);
        seatBeltPanel.setBackground(UIConstant.PanelWhite);
        headLightPanel.requestFocusInWindow();
        headLightPanel.setBackground(UIConstant.focusPanelColor);
        reverseHornPanel.setBackground(UIConstant.PanelWhite);
        breathlizerOkPanel.setBackground(UIConstant.PanelWhite);
    }//GEN-LAST:event_headLightPanelMouseClicked

    private void reverseHornPanelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_reverseHornPanelMouseClicked
        sealOkPanel.setBackground(UIConstant.PanelWhite);
        tarpaulinPanel.setBackground(UIConstant.PanelWhite);
        numberVisiblePanel.setBackground(UIConstant.PanelWhite);
        tailLightPanel.setBackground(UIConstant.PanelWhite);
        sideMirrorPanel.setBackground(UIConstant.PanelWhite);
        leftSideIndicatorPanel.setBackground(UIConstant.PanelWhite);
        rightSideIndicatorPanel.setBackground(UIConstant.PanelWhite);
        seatBeltPanel.setBackground(UIConstant.PanelWhite);
        headLightPanel.setBackground(UIConstant.PanelWhite);
        reverseHornPanel.requestFocusInWindow();
        reverseHornPanel.setBackground(UIConstant.focusPanelColor);
        breathlizerOkPanel.setBackground(UIConstant.PanelWhite);
    }//GEN-LAST:event_reverseHornPanelMouseClicked

    private void breathlizerOkPanelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_breathlizerOkPanelMouseClicked
        sealOkPanel.setBackground(UIConstant.PanelWhite);
        tarpaulinPanel.setBackground(UIConstant.PanelWhite);
        numberVisiblePanel.setBackground(UIConstant.PanelWhite);
        tailLightPanel.setBackground(UIConstant.PanelWhite);
        sideMirrorPanel.setBackground(UIConstant.PanelWhite);
        leftSideIndicatorPanel.setBackground(UIConstant.PanelWhite);
        seatBeltPanel.setBackground(UIConstant.PanelWhite);
        headLightPanel.setBackground(UIConstant.PanelWhite);
        reverseHornPanel.setBackground(UIConstant.PanelWhite);
        breathlizerOkPanel.requestFocusInWindow();
        breathlizerOkPanel.setBackground(UIConstant.focusPanelColor);
    }//GEN-LAST:event_breathlizerOkPanelMouseClicked

    private void vehicleNameKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_vehicleNameKeyPressed
          if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            vehicleNameAction();
        }
    }//GEN-LAST:event_vehicleNameKeyPressed

    private void vehicleNameMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_vehicleNameMouseClicked
        // transporter.setBackground(UIConstant.PanelWhite);
        if (vehicleName.isTextEditable()) {
            vehicleName.setFocusable(true);
            vehicleName.requestFocusInWindow();
            vehicleName.setTextBackground(UIConstant.focusPanelColor);
            //transporter.setBackground(Color.WHITE);
            //transporter.setForeground(Color.BLACK);
            sealOkPanel.setBackground(UIConstant.PanelWhite);
            tarpaulinPanel.setBackground(UIConstant.PanelWhite);
            numberVisiblePanel.setBackground(UIConstant.PanelWhite);
            tailLightPanel.setBackground(UIConstant.PanelWhite);
            sideMirrorPanel.setBackground(UIConstant.PanelWhite);
            leftSideIndicatorPanel.setBackground(UIConstant.PanelWhite);
            seatBeltPanel.setBackground(UIConstant.PanelWhite);
            headLightPanel.setBackground(UIConstant.PanelWhite);
            reverseHornPanel.setBackground(UIConstant.PanelWhite);
            breathlizerOkPanel.setBackground(UIConstant.PanelWhite);
        }

    }//GEN-LAST:event_vehicleNameMouseClicked

    private void transporterKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_transporterKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            setWhiteBackColor();
            seatBeltPanel.requestFocusInWindow();
            seatBeltPanel.setBackground(UIConstant.focusPanelColor);
            playSeatBeltWormVoice();
            /*if (!isFingerCaptureRunning && isMorphoExist) {
                fingurePrintCaptureService.start();
                isFingerCaptureRunning = true;
            }*/
            startFingerCaptureService();
        }
    }//GEN-LAST:event_transporterKeyPressed

    private void transporterMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_transporterMouseClicked

        setWhiteBackColor();
        //transporter.setBackground(UIConstant.focusPanelColor);
//        sealOkPanel.setBackground(UIConstant.PanelWhite);
//        tarpaulinPanel.setBackground(UIConstant.PanelWhite);
//        numberVisiblePanel.setBackground(UIConstant.PanelWhite);
//        tailLightPanel.setBackground(UIConstant.PanelWhite);
//        sideMirrorPanel.setBackground(UIConstant.PanelWhite);
//        sideIndicatorPanel.setBackground(UIConstant.PanelWhite);
//        seatBeltPanel.setBackground(UIConstant.PanelWhite);
//        headLightPanel.setBackground(UIConstant.PanelWhite);
//        reverseHornPanel.setBackground(UIConstant.PanelWhite);
//        breathlizerOkPanel.setBackground(UIConstant.PanelWhite);

    }//GEN-LAST:event_transporterMouseClicked

    private void helperNoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_helperNoActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_helperNoActionPerformed

    private void helperYesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_helperYesActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_helperYesActionPerformed

    private void HelperPanelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_HelperPanelMouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_HelperPanelMouseClicked

    private void HelperPanelKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_HelperPanelKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_HelperPanelKeyPressed

    private void transporterMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_transporterMousePressed
        if (vehicleName.isTextEditable()) {
            if (Utils.isNull(vehicleName.getText())) {
                JOptionPane.showMessageDialog(null, "Please Enter Vehicle !!!");
                vehicleName.requestFocusInWindow();
                return;
                // vehicleName.setTextBackground(UIConstant.noActionPanelColor);
            } else {
//                boolean isVehicleExist = false;
//                try {
//                    isVehicleExist = GateInDao.checkVehicleExistInDB(vehicleName.getText());
//                    //alreadyCheck = true;
//                } catch (IOException ex) {
//                    Logger.getLogger(GateIn1st.class.getName()).log(Level.SEVERE, null, ex);
//                }
//                if (!isVehicleExist) {
//                    int contiNue = 1;
//                    int reEnter = 0;
//                    new CheckVehicleDialog(new javax.swing.JFrame(), true);
//                    if (responseVehicleDialog == reEnter) {
//                        vehicleName.setText("");
//                        return;
//                    } else if (responseVehicleDialog == contiNue) {
//                        getFocusOnTransporterControl();
//                    }
//                } else {
//                    getFocusOnTransporterControl();
//                }
            }
        }
    }//GEN-LAST:event_transporterMousePressed

    private void captureButtonActionPerformed(java.awt.event.ActionEvent evt) {
    	if (captureButton.getText().startsWith("Cancel")) {
            if (fingurePrintCaptureService != null) {
                fingurePrintCaptureService.stop();
                if(!isFingerSyncRunning)
                	MorphoSmartFunctions.getMorpho().cancelAllCurrentTask();
               
                disableFingurePrintCapture();
            }
        } else {
        	if(captureCount<3){
        		startFingerCaptureService(true);
        		
        	}else{
        		captureButton.setVisible(false);
        	}
        	//captureFingurePrint();
        }
       
    }//GEN-LAST:event_captureButtonActionPerformed

    private void button1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button1ActionPerformed
        stopRfid();
        if (fingerPrintSyncService != null) {
            fingerPrintSyncService.stop();
        }
        GateInDao.forceSignut(TokenManager.userId,TokenManager.srcType,Integer.toString(TokenManager.systemId));
        this.dispose();
        new LoginWindow().setVisible(true);
    }//GEN-LAST:event_button1ActionPerformed
    
    void stopRfid() {
        if (rfidHandler != null) {
            rfidHandler.stop();
            //RFIDMaster.StopRFIDReaders();
        }

    }
    private void vehicleNameFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_vehicleNameFocusLost

    }//GEN-LAST:event_vehicleNameFocusLost

    private void vehicleNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_vehicleNameActionPerformed
    }//GEN-LAST:event_vehicleNameActionPerformed

    private void manualEntryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_manualEntryActionPerformed
        manualEntryAction();
        // TODO add your handling code here:
    }//GEN-LAST:event_manualEntryActionPerformed

    private void dlNoKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_dlNoKeyPressed
    	Connection conn = null;
    	boolean destroyIt = false;
    	try{// GEN-FIRST:event_dlNoKeyPressed
    		conn = DBConnectionPool.getConnectionFromPoolNonWeb();
    		if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
    			System.out.println("Enter Keyed");
    			if (Utils.isNull(dlNo.getText())) {
    				System.out.println("Enter Keyed null ");
    				JOptionPane.showMessageDialog(null, "Please Enter Driver Lisence Number !!!");
    				return;
    			} else {
    				System.out.println("Enter Keyed not null");
    				driverName.setText("");
    				photo.setIcon(null);
    				helperOk.clearSelection();
    				try {
    					//                        driverInformation = GateInDao.getDriverDetailByDL(dlNo.getText());
    					driverInformation = GateInDao.getDriverDetailByDL(conn, dlNo.getText(), "DLNo");
    					if (driverInformation != null) {
    						driverInformation.setIsFingerCaptured(0);
//    						driverInformation
    						System.out.println("driverInformation is not null ");
    						populateDriver(conn, driverInformation, false, false);
    						driverSrc = Constant.DRIVER_SOURCE_DL_NO;
    						quickCreate.requestFocusInWindow();
    					} else {
    						System.out.println("driverInformation is null ");
    						String msg = "Driver Detail Not Exist, Do you want to Re-Enter?";
    						Object[] options = {"  Yes  ", "  No  "};
    						int responseVehicleDialog = ConfirmationDialog.getDialogBox(new java.awt.Frame(), true, options, msg);
    						System.out.print("responseVehicleDialog : " + responseVehicleDialog);
    						if (responseVehicleDialog == 1) {
    							System.out.print("responseVehicleDialog == 1: " + responseVehicleDialog);
    							System.out.print("driverName : " + driverName);
    							dlNo.setFocusable(false);
    							driverName.setFocusable(true);
    							driverName.setEditable(true);
    							dlNo.setBackground(UIConstant.PanelWhite);

    							driverName.setBackground(UIConstant.focusPanelColor);
    							driverName.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
    							driverName.requestFocusInWindow();
    							dlNo.setFocusable(true);
    						} else {
    							System.out.print("responseVehicleDialog not 1: " + responseVehicleDialog);
    							dlNo.requestFocusInWindow();

    							// JOptionPane.showMessageDialog(null, "responseVehicleDialog != 1: " + responseVehicleDialog);
    							// System.out.print("responseVehicleDialog == 1: " + responseVehicleDialog);
    						}

    					}

    				} catch (IOException ex) {
    					ex.printStackTrace();
    				} catch (Exception ex) {
    					ex.printStackTrace();
    				}

    			}
    		}
    	}catch(Exception ex){
    		ex.printStackTrace();
    		destroyIt = true;
    	}finally{
    		try{
    			DBConnectionPool.returnConnectionToPoolNonWeb(conn, destroyIt);
    		}catch(Exception ex){
    			ex.printStackTrace();
    		}
    	}
    }//GEN-LAST:event_dlNoKeyPressed

    private void dlNoKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_dlNoKeyReleased
        // TODO add your handling code here:
    }//GEN-LAST:event_dlNoKeyReleased

    private void dlNoFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_dlNoFocusGained
        vehicleName.setTextBackground(UIConstant.PanelWhite);
        //transporter.setBackground(UIConstant.PanelWhite);
        driverId.setBackground(UIConstant.PanelWhite);
        dlNo.setBackground(UIConstant.focusPanelColor);
        driverName.setBackground(UIConstant.PanelWhite);
        sealOkPanel.setBackground(UIConstant.PanelWhite);
        tarpaulinPanel.setBackground(UIConstant.PanelWhite);
        numberVisiblePanel.setBackground(UIConstant.PanelWhite);
        tailLightPanel.setBackground(UIConstant.PanelWhite);
        sideMirrorPanel.setBackground(UIConstant.PanelWhite);
        leftSideIndicatorPanel.setBackground(UIConstant.PanelWhite);
        seatBeltPanel.setBackground(UIConstant.PanelWhite);
        headLightPanel.setBackground(UIConstant.PanelWhite);
        reverseHornPanel.setBackground(UIConstant.PanelWhite);
        breathlizerOkPanel.setBackground(UIConstant.PanelWhite);
        driverName.setEditable(false);
        driverName.setFocusable(false);
        driverName.setBorder(null);
        driverName.setText("");
        driverId.setText("");
    }//GEN-LAST:event_dlNoFocusGained

    private void dlNoFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_dlNoFocusLost
        dlNo.setBackground(UIConstant.PanelWhite);
    }//GEN-LAST:event_dlNoFocusLost

    private void reverseHornNCActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_reverseHornNCActionPerformed
        reverseHornAction();
        if (doCheck) {
            checkQuestions();
        }
    }//GEN-LAST:event_reverseHornNCActionPerformed

    private void sideMirrorNCActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sideMirrorNCActionPerformed
        sideMirrorAction();
        if (doCheck) {
            checkQuestions();
        }
    }//GEN-LAST:event_sideMirrorNCActionPerformed

    private void sealNCActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sealNCActionPerformed
        sealOkAction();
        if (doCheck) {
            checkQuestions();
        }
    }//GEN-LAST:event_sealNCActionPerformed

    private void tarpaulinNCActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tarpaulinNCActionPerformed
        tarpaulinAction();
        if (doCheck) {
            checkQuestions();
        }
    }//GEN-LAST:event_tarpaulinNCActionPerformed

    private void numberVisibleNCActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_numberVisibleNCActionPerformed
        numberVisibleAction();
        if (doCheck) {
            checkQuestions();
        }
    }//GEN-LAST:event_numberVisibleNCActionPerformed

    private void tailLightNCActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tailLightNCActionPerformed
        tailLightAction();
        if (doCheck) {
            checkQuestions();
        }
    }//GEN-LAST:event_tailLightNCActionPerformed

    private void leftSideIndicatorNCActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_leftSideIndicatorNCActionPerformed
        leftSideIndicatorAction();
        if (doCheck) {
            checkQuestions();
        }
    }//GEN-LAST:event_leftSideIndicatorNCActionPerformed

    private void seatBeltNCActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_seatBeltNCActionPerformed
        seatBeltAction();
        /*if (!Utils.isNull(vehicleName.getText()) && !(vehicleName.getText().trim()).equalsIgnoreCase("NO VEHICLE DETECTED") && !isFingerCaptureRunning && isMorphoExist) {
            fingurePrintCaptureService.start();
            isFingerCaptureRunning = true;
        }*/
        startFingerCaptureService();
    }//GEN-LAST:event_seatBeltNCActionPerformed

    private void headLightNCActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_headLightNCActionPerformed
        headLightAction();
        if (doCheck) {
            checkQuestions();
        }
    }//GEN-LAST:event_headLightNCActionPerformed

    private void driverNameKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_driverNameKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
        	Connection conn = null;
        	boolean destroyIt = false;
        	try{
        		conn = DBConnectionPool.getConnectionFromPoolNonWeb();
        		if (Utils.isNull(driverName.getText())) {
        			JOptionPane.showMessageDialog(null, "Please Enter Driver Name !!!");
        			return;
        		} else {

        			driverInformation = GateInDao.insertDriverDetailByDL(conn, dlNo.getText(), driverName.getText(), tprRecord.getVehicleId());
        			if (driverInformation != null) {
        				driverInformation.setIsFingerCaptured(0);
        				populateDriver(conn, driverInformation, false, false); 
        				driverSrc = Constant.DRIVER_SOURCE_NEW;
        			} else {
        				isFingerCaptured = false;
        			}
        			quickCreate.requestFocusInWindow();
        		}
        	}catch(Exception ex){
        		ex.printStackTrace();
        		destroyIt = true;
        	}finally{
        		 try{
        			 DBConnectionPool.returnConnectionToPoolNonWeb(conn, destroyIt);
        		 }catch(Exception ex){
        			 ex.printStackTrace();
        		 }
        	}
        }


    }//GEN-LAST:event_driverNameKeyPressed

    private void driverNameFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_driverNameFocusGained
        vehicleName.setTextBackground(UIConstant.PanelWhite);
        //transporter.setBackground(UIConstant.PanelWhite);
        driverName.setBackground(UIConstant.focusPanelColor);
        dlNo.setBackground(UIConstant.PanelWhite);
        sealOkPanel.setBackground(UIConstant.PanelWhite);
        tarpaulinPanel.setBackground(UIConstant.PanelWhite);
        numberVisiblePanel.setBackground(UIConstant.PanelWhite);
        tailLightPanel.setBackground(UIConstant.PanelWhite);
        sideMirrorPanel.setBackground(UIConstant.PanelWhite);
        leftSideIndicatorPanel.setBackground(UIConstant.PanelWhite);
        seatBeltPanel.setBackground(UIConstant.PanelWhite);
        headLightPanel.setBackground(UIConstant.PanelWhite);
        reverseHornPanel.setBackground(UIConstant.PanelWhite);
        breathlizerOkPanel.setBackground(UIConstant.PanelWhite);
    }//GEN-LAST:event_driverNameFocusGained

    private void sealOkPanelFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_sealOkPanelFocusLost
        //Voice.stopPlayer(audios);
        sealOkPanel.setBackground(UIConstant.PanelWhite);
        if (doCheck) {
            checkQuestions();
        }
    }//GEN-LAST:event_sealOkPanelFocusLost

    private void sealOkPanelFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_sealOkPanelFocusGained
//     String voicePath = Status.Voice.getVoicePath(Status.TPRQuestion.sealOk);
//        try {
//            in = new FileInputStream(new File(voicePath));
//            audios = new AudioStream(in);
//            Voice.playQuestion(null, audios);
//        } catch (Exception ex) {
//
//            ex.printStackTrace();
//
//        }
//        WaveFormPlayer.playSoundIn(Status.TPRQuestion.sealOk);
//        WaveFormPlayer.playSoundOut(Status.TPRQuestion.sealOk);
    }//GEN-LAST:event_sealOkPanelFocusGained
    private void playSeatBeltWormVoice() {

//        String voicePath = Status.Voice.getVoicePath(Status.TPRQuestion.seatBeltWorm);
//         
//        try {
//            in = new FileInputStream(new File(voicePath));
//            audios = new AudioStream(in);
//            Voice.playQuestion(null, audios);
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
        WaveFormPlayer.playSoundIn(Status.TPRQuestion.seatBeltWorm);
        WaveFormPlayer.playSoundOut(Status.TPRQuestion.seatBeltWorm);
    }
    private void tarpaulinPanelFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_tarpaulinPanelFocusGained
//        WaveFormPlayer.playSoundIn(Status.TPRQuestion.tarpaulinOk);
//        WaveFormPlayer.playSoundOut(Status.TPRQuestion.tarpaulinOk);
    }//GEN-LAST:event_tarpaulinPanelFocusGained

    private void tarpaulinPanelFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_tarpaulinPanelFocusLost
        //Voice.stopPlayer(audios);
        tarpaulinPanel.setBackground(UIConstant.PanelWhite);
        if (doCheck) {
            checkQuestions();
        }

    }//GEN-LAST:event_tarpaulinPanelFocusLost

    private void numberVisiblePanelFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_numberVisiblePanelFocusGained
        // GEN-FIRST:event_numberVisiblePanelFocusGained
//        String voicePath = Status.Voice.getVoicePath(Status.TPRQuestion.numberVisible);
//        try {
//            in = new FileInputStream(new File(voicePath));
//            audios = new AudioStream(in);
//            Voice.playQuestion(null, audios);
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//        WaveFormPlayer.playSoundIn(Status.TPRQuestion.numberVisible);
//        WaveFormPlayer.playSoundOut(Status.TPRQuestion.numberVisible);
    }//GEN-LAST:event_numberVisiblePanelFocusGained

    private void numberVisiblePanelFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_numberVisiblePanelFocusLost
        //Voice.stopPlayer(audios);
        numberVisiblePanel.setBackground(UIConstant.PanelWhite);
        if (doCheck) {
            checkQuestions();
        }
    }//GEN-LAST:event_numberVisiblePanelFocusLost

    private void tailLightPanelFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_tailLightPanelFocusGained
// GEN-FIRST:event_tailLightPanelFocusGained

//        String voicePath = Status.Voice.getVoicePath(Status.TPRQuestion.tailLightOk);
//        try {
//            in = new FileInputStream(new File(voicePath));
//            audios = new AudioStream(in);
//            Voice.playQuestion(null, audios);
//        } catch (Exception ex) {
//            ex.printStackTrace();
//
//        }

        //WaveFormPlayer.playSoundOut(Status.TPRQuestion.brakeLightOn);
        WaveFormPlayer.playSoundIn(Status.TPRQuestion.pushBrake);

    }//GEN-LAST:event_tailLightPanelFocusGained

    private void tailLightPanelFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_tailLightPanelFocusLost
        //Voice.stopPlayer(audios);
        tailLightPanel.setBackground(UIConstant.PanelWhite);
        if (doCheck) {
            checkQuestions();
        }
    }//GEN-LAST:event_tailLightPanelFocusLost

    private void sideMirrorPanelFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_sideMirrorPanelFocusGained
        // GEN-FIRST:event_sideMirrorPanelFocusGained
//        String voicePath = Status.Voice.getVoicePath(Status.TPRQuestion.sideMirror);
//        try {
//            in = new FileInputStream(new File(voicePath));
//            audios = new AudioStream(in);
//            Voice.playQuestion(null, audios);
//        } catch (Exception ex) {
//            ex.printStackTrace();
//
//        }
//        WaveFormPlayer.playSoundIn(Status.TPRQuestion.sideMirror);
//        WaveFormPlayer.playSoundOut(Status.TPRQuestion.sideMirror);
    }//GEN-LAST:event_sideMirrorPanelFocusGained

    private void sideMirrorPanelFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_sideMirrorPanelFocusLost
        //Voice.stopPlayer(audios);
        sideMirrorPanel.setBackground(UIConstant.PanelWhite);
        if (doCheck) {
            checkQuestions();
        }
    }//GEN-LAST:event_sideMirrorPanelFocusLost

    private void leftSideIndicatorPanelFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_leftSideIndicatorPanelFocusGained
        // GEN-FIRST:event_sideIndicatorPanelFocusGained
//        String voicePath = Status.Voice.getVoicePath(Status.TPRQuestion.sideIndicator);
//        try {
//            in = new FileInputStream(new File(voicePath));
//            audios = new AudioStream(in);
//            Voice.playQuestion(null, audios);
//        } catch (Exception ex) {
//            ex.printStackTrace();
//
//        }
//        WaveFormPlayer.playSoundOut(Status.TPRQuestion.leftSideIndicatorOn);
        WaveFormPlayer.playSoundIn(Status.TPRQuestion.leftSideIndicator);


    }//GEN-LAST:event_leftSideIndicatorPanelFocusGained

    private void seatBeltPanelFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_seatBeltPanelFocusGained
        // GEN-FIRST:event_seatBeltPanelFocusGained
    }//GEN-LAST:event_seatBeltPanelFocusGained

    private void seatBeltPanelFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_seatBeltPanelFocusLost
        //Voice.stopPlayer(audios);
        seatBeltPanel.setBackground(UIConstant.PanelWhite);
    }//GEN-LAST:event_seatBeltPanelFocusLost

    private void leftSideIndicatorPanelFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_leftSideIndicatorPanelFocusLost
        //Voice.stopPlayer(audios);
        leftSideIndicatorPanel.setBackground(UIConstant.PanelWhite);
        if (doCheck) {
            checkQuestions();
        }
    }//GEN-LAST:event_leftSideIndicatorPanelFocusLost

    private void headLightPanelFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_headLightPanelFocusGained
        // GEN-FIRST:event_headLightPanelFocusGained
//        String voicePath = Status.Voice.getVoicePath(Status.TPRQuestion.headLightOk);
//        try {
//            in = new FileInputStream(new File(voicePath));
//            audios = new AudioStream(in);
//            Voice.playQuestion(null, audios);
//        } catch (Exception ex) {
//            ex.printStackTrace();
//
//        }
//        WaveFormPlayer.playSoundOut(Status.TPRQuestion.headLightOn);
        WaveFormPlayer.playSoundIn(Status.TPRQuestion.headLightOk);
    }//GEN-LAST:event_headLightPanelFocusGained

    private void headLightPanelFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_headLightPanelFocusLost
        //Voice.stopPlayer(audios);
        headLightPanel.setBackground(UIConstant.PanelWhite);
        if (doCheck) {
            checkQuestions();
        }
    }//GEN-LAST:event_headLightPanelFocusLost

    private void reverseHornPanelFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_reverseHornPanelFocusGained
        // GEN-FIRST:event_reverseHornPanelFocusGained
//        String voicePath = Status.Voice.getVoicePath(Status.TPRQuestion.reverseHornOk);
//        try {
//            in = new FileInputStream(new File(voicePath));
//            audios = new AudioStream(in);
//            Voice.playQuestion(null, audios);
//        } catch (Exception ex) {
//
//            ex.printStackTrace();
//
//        }
        //WaveFormPlayer.playSound(Status.TPRQuestion.hornPlay);
        WaveFormPlayer.playSoundIn(Status.TPRQuestion.reverseHornOk);
    }//GEN-LAST:event_reverseHornPanelFocusGained

    private void reverseHornPanelFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_reverseHornPanelFocusLost
        //Voice.stopPlayer(audios);
        reverseHornPanel.setBackground(UIConstant.PanelWhite);
        if (doCheck) {
            checkQuestions();
        }
    }//GEN-LAST:event_reverseHornPanelFocusLost

    private void breathlizerOkPanelFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_breathlizerOkPanelFocusGained
        // GEN-FIRST:event_breathlizerOkPanelFocusGained
//        String voicePath = Status.Voice.getVoicePath(Status.TPRQuestion.breathLyzerOk);
//        try {
//            in = new FileInputStream(new File(voicePath));
//            audios = new AudioStream(in);
//            Voice.playQuestion(null, audios);
//        } catch (Exception ex) {
//            ex.printStackTrace();
//
//        }
    }//GEN-LAST:event_breathlizerOkPanelFocusGained

    private void breathlizerOkPanelFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_breathlizerOkPanelFocusLost
        //Voice.stopPlayer(audios);
        breathlizerOkPanel.setBackground(UIConstant.PanelWhite);
        checkQuestions();
    }//GEN-LAST:event_breathlizerOkPanelFocusLost

    private void transporterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_transporterActionPerformed
      
    }//GEN-LAST:event_transporterActionPerformed

    private void captureButtonKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_captureButtonKeyPressed
        // GEN-FIRST:event_captureButtonKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            getWhiteBackGrounds();
            if ((vehicleName.getText().trim()).equalsIgnoreCase("NO VEHICLE DETECTED")) {
                JOptionPane.showMessageDialog(null, "Please Enter Vehicle Name");
                return;
            } else if (Utils.isNull(vehicleName.getText())) {
                JOptionPane.showMessageDialog(null, "Please Enter Vehicle Name");
                return;
            } else if (tprRecord == null) {
                JOptionPane.showMessageDialog(null, "Please Enter valid Vehicle Name");
                return;
            } else {
                try {
                    dlNo.setText("");
                    driverName.setText("");
                    helperOk.clearSelection();
                    photo.setIcon(null);
                    isFingerCaptured = false;
                    isFingerVerified = false;
                    captureFingurePrint();
                }  catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

        }

    }//GEN-LAST:event_captureButtonKeyPressed

    private void quickCreateKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_quickCreateKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            quickCreateAndSave();
        }
    }//GEN-LAST:event_quickCreateKeyPressed

    private void clearKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_clearKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            clearAction();
        }
    }//GEN-LAST:event_clearKeyPressed

    private void rightSideIndicatorPanelFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_rightSideIndicatorPanelFocusGained
//        String voicePath = Status.Voice.getVoicePath(Status.TPRQuestion.sideIndicator);
//        try {
//            in = new FileInputStream(new File(voicePath));
//            audios = new AudioStream(in);
//            Voice.playQuestion(null, audios);
//        } catch (Exception ex) {
//            ex.printStackTrace();
//
//        }
        WaveFormPlayer.playSoundIn(Status.TPRQuestion.rightSideIndicator);
//        WaveFormPlayer.playSoundOut(Status.TPRQuestion.rightSideIndicatorOn);
    }//GEN-LAST:event_rightSideIndicatorPanelFocusGained

    private void rightSideIndicatorPanelFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_rightSideIndicatorPanelFocusLost
//        Voice.stopPlayer(audios);
        WaveFormPlayer.playSoundIn(0);
        leftSideIndicatorPanel.setBackground(UIConstant.PanelWhite);
        if (doCheck) {
            checkQuestions();
        }
    }//GEN-LAST:event_rightSideIndicatorPanelFocusLost

    private void rightSideIndicatorNCActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rightSideIndicatorNCActionPerformed
        rightSideIndicatorAction();
        if (doCheck) {
            checkQuestions();
        }
    }//GEN-LAST:event_rightSideIndicatorNCActionPerformed

    private void rightSideIndicatorPanelKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_rightSideIndicatorPanelKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            if (!rightSideIndicatorYes.isSelected() && !rightSideIndicatorNo.isSelected() && !rightSideIndicatorNC.isSelected()) {
                rightSideIndicatorPanel.setBackground(UIConstant.focusPanelColor);
            } else {
                rightSideIndicatorPanel.setBackground(UIConstant.PanelWhite);
            }
            breathlizerOkPanel.requestFocusInWindow();
            breathlizerOkPanel.setBackground(UIConstant.focusPanelColor);
            if (doCheck) {
                checkQuestions();
            }
        } else if (evt.getKeyCode() == KeyEvent.VK_1 || evt.getKeyCode() == KeyEvent.VK_NUMPAD1) {
            rightSideIndicatorYes.setSelected(true);
            rightSideIndicatorPanel.setBackground(Color.WHITE);
            breathlizerOkPanel.requestFocusInWindow();
            breathlizerOkPanel.setBackground(UIConstant.focusPanelColor);
            if (doCheck) {
                checkQuestions();
            }
        } else if (evt.getKeyCode() == KeyEvent.VK_2 || evt.getKeyCode() == KeyEvent.VK_NUMPAD2) {
            rightSideIndicatorNo.setSelected(true);
            rightSideIndicatorPanel.setBackground(Color.WHITE);
            breathlizerOkPanel.requestFocusInWindow();
            breathlizerOkPanel.setBackground(UIConstant.focusPanelColor);
            if (doCheck) {
                checkQuestions();
            }
        } else if (evt.getKeyCode() == KeyEvent.VK_3 || evt.getKeyCode() == KeyEvent.VK_NUMPAD3) {
            rightSideIndicatorNC.setSelected(true);
            rightSideIndicatorPanel.setBackground(Color.WHITE);
            breathlizerOkPanel.requestFocusInWindow();
            breathlizerOkPanel.setBackground(UIConstant.focusPanelColor);
            if (doCheck) {
                checkQuestions();
            }
        }
    }//GEN-LAST:event_rightSideIndicatorPanelKeyPressed

    private void rightSideIndicatorPanelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_rightSideIndicatorPanelMouseClicked
        sealOkPanel.setBackground(UIConstant.PanelWhite);
        tarpaulinPanel.setBackground(UIConstant.PanelWhite);
        numberVisiblePanel.setBackground(UIConstant.PanelWhite);
        tailLightPanel.setBackground(UIConstant.PanelWhite);
        sideMirrorPanel.setBackground(UIConstant.PanelWhite);

        leftSideIndicatorPanel.setBackground(UIConstant.PanelWhite);
        rightSideIndicatorPanel.setBackground(UIConstant.focusPanelColor);
        rightSideIndicatorPanel.requestFocusInWindow();
        seatBeltPanel.setBackground(UIConstant.PanelWhite);
        headLightPanel.setBackground(UIConstant.PanelWhite);
        reverseHornPanel.setBackground(UIConstant.PanelWhite);
        breathlizerOkPanel.setBackground(UIConstant.PanelWhite);
    }//GEN-LAST:event_rightSideIndicatorPanelMouseClicked

    private void driverIdKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_driverIdKeyPressed
    	Connection conn = null;
    	boolean destroyIt = false;
    	try{
    		conn = DBConnectionPool.getConnectionFromPoolNonWeb();
    		if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
    			if (Utils.isNull(driverId.getText())) {
    				JOptionPane.showMessageDialog(null, "Please Enter Driver Id !!!");
    				return;
    			} else {
    				driverName.setText("");
    				photo.setIcon(null);
    				dlNo.setText("");
    				helperOk.clearSelection();
    				driverInformation = GateInDao.getDriverDetailByDL(conn, driverId.getText(), "DriverId");
    				if (driverInformation != null) {
    					driverInformation.setIsFingerCaptured(0);
    					populateDriver(conn, driverInformation, false, false);
    					driverSrc = Constant.DRIVER_SOURCE_GATE_PASS_ID;
    					quickCreate.requestFocusInWindow();
    					driverName.setText(driverInformation.getDriver_name());
    				} else {
    					String msg = "Driver Detail Not Exist, Do you want to Re-Enter?";
    					Object[] options = {"  Yes  ", "  No  "};//new javax.swing.JFrame()
    					//                        JOptionPane.showMessageDialog(null, "Driver not Exist please enter driver name");
    					int responseVehicleDialog = ConfirmationDialog.getDialogBox(new java.awt.Frame(), true, options, msg);
    					System.out.print("responseVehicleDialog : " + responseVehicleDialog);
    					if (responseVehicleDialog == 1) {
    						isFingerCaptured = false;
    						isFingerVerified = false;
    						driverId.setFocusable(false);
    						dlNo.setFocusable(true);
    						dlNo.setEditable(true);

    						dlNo.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
    						dlNo.requestFocusInWindow();
    						//   dlNo.setFocusable(true);
    						driverId.setText("");
    						driverId.setBackground(UIConstant.PanelWhite);
    						dlNo.setBackground(UIConstant.focusPanelColor);
    					} else {
    						System.out.print("responseVehicleDialog not 1: " + responseVehicleDialog);
    						driverId.requestFocusInWindow();
    						//                            JOptionPane.showMessageDialog(null, "responseVehicleDialog != 1: " + responseVehicleDialog);
    						//                             System.out.print("responseVehicleDialog == 1: " + responseVehicleDialog);
    					}
    				}
    			}
    		}
    	}catch(Exception ex){
    		ex.printStackTrace();
    		destroyIt = true;
    	}finally{
    		try{
   			 DBConnectionPool.returnConnectionToPoolNonWeb(conn, destroyIt);
   		 }catch(Exception ex){
   			 ex.printStackTrace();
   		 }
    	}
    }//GEN-LAST:event_driverIdKeyPressed

    private void driverIdFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_driverIdFocusGained
        vehicleName.setTextBackground(UIConstant.PanelWhite);
        //transporter.setBackground(UIConstant.PanelWhite);
        driverId.setBackground(UIConstant.focusPanelColor);
        dlNo.setBackground(UIConstant.PanelWhite);
        driverName.setBackground(UIConstant.PanelWhite);
        sealOkPanel.setBackground(UIConstant.PanelWhite);
        tarpaulinPanel.setBackground(UIConstant.PanelWhite);
        numberVisiblePanel.setBackground(UIConstant.PanelWhite);
        tailLightPanel.setBackground(UIConstant.PanelWhite);
        sideMirrorPanel.setBackground(UIConstant.PanelWhite);
        leftSideIndicatorPanel.setBackground(UIConstant.PanelWhite);
        seatBeltPanel.setBackground(UIConstant.PanelWhite);
        headLightPanel.setBackground(UIConstant.PanelWhite);
        reverseHornPanel.setBackground(UIConstant.PanelWhite);
        breathlizerOkPanel.setBackground(UIConstant.PanelWhite);
        driverName.setEditable(false);
        driverName.setFocusable(false);
        driverName.setBorder(null);
        driverName.setText("");
        dlNo.setText("");
    }//GEN-LAST:event_driverIdFocusGained

    private void driverIdFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_driverIdFocusLost
        driverId.setBackground(UIConstant.PanelWhite);
    }//GEN-LAST:event_driverIdFocusLost

    private void seatBeltYesKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_seatBeltYesKeyPressed
       // seatBeltYesActionPerformed(evt);
    }//GEN-LAST:event_seatBeltYesKeyPressed

    private void rightSideIndicatorYesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rightSideIndicatorYesActionPerformed
        rightSideIndicatorAction();
        if (doCheck) {
            checkQuestions();
        }
    }//GEN-LAST:event_rightSideIndicatorYesActionPerformed

    private void rightSideIndicatorNoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rightSideIndicatorNoActionPerformed
        rightSideIndicatorAction();
        if (doCheck) {
            checkQuestions();
        }
    }//GEN-LAST:event_rightSideIndicatorNoActionPerformed

    private void breathlizerNCActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_breathlizerNCActionPerformed

        breathlizerOkPanel.setBackground(UIConstant.PanelWhite);
//        captureButton.requestFocusInWindow();
        //manualEntryButton.setEnabled(false);
        setQuestionsBlocking(Status.TPRQuestion.breathLyzerOk, UIConstant.NC);
        /*if(tprBlockManager != null && tprBlockManager.useForBlocking(Status.TPRQuestion.breathLyzerOk)){
        	enableDenyEntry(false);
        	blocking_reason.setText(RFIDDataProcessor.getTprBlockStatus(tprRecord, blockInstructionBreathelizer, false));
        }*/
        //quickCreate.setEnabled(true);
        if(quickCreate.isEnabled())
        	quickCreate.requestFocusInWindow();
        checkQuestions();
    }//GEN-LAST:event_breathlizerNCActionPerformed

    private void manualEntryKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_manualEntryKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            manualEntryAction();
        }
    }//GEN-LAST:event_manualEntryKeyPressed

    private void driverIdActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_driverIdActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_driverIdActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
//        SyncFingerPrintDeviceHelper.loadInFingerPrintDevice(fingurePrintConn,true);
        if (isMorphoExist  && fingerPrintSyncService != null) {
            fingerPrintSyncService.start();
        }
    }//GEN-LAST:event_jButton1ActionPerformed

    private void transporterFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_transporterFocusLost
        Connection conn = null;
        boolean destroyIt = false;
        try{
        	conn = DBConnectionPool.getConnectionFromPoolNonWeb();
        	int selectedtransporter = DropDownValues.getComboSelectedVal(transporter);
    		int selectedMines = tprRecord != null ? tprRecord.getMinesId() : Misc.getUndefInt();
    		int selectedGrade = tprRecord != null ? tprRecord.getMaterialGradeId() : Misc.getUndefInt();
    		int selectedDO = tprRecord != null ? tprRecord.getDoId() : Misc.getUndefInt();
        	Pair<Integer, String> bedAssign = TPRUtils.getBedAllignment(conn, selectedtransporter, selectedMines, selectedGrade,selectedDO);
        	if(bedAssign != null)
        		bedAssigned.setText(bedAssign.second);
        }catch(Exception ex){
        	ex.printStackTrace();
        	destroyIt = true;
        }finally{
        	try{
        		DBConnectionPool.returnConnectionToPoolNonWeb(conn, destroyIt);
        	}catch(Exception ex){
        		ex.printStackTrace();
        	}
        }
    }//GEN-LAST:event_transporterFocusLost

    
    /**
     * @param args the command line arguments
     */
//    public static void main(String args[]) {
//        /* Set the Nimbus look and feel */
//        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
//        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
//         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
//         */
//        try {
//            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
//                if ("Nimbus".equals(info.getName())) {
//                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
//                    break;
//                }
//            }
//        } catch (ClassNotFoundException ex) {
//            java.util.logging.Logger.getLogger(GateIn1st.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//        } catch (InstantiationException ex) {
//            java.util.logging.Logger.getLogger(GateIn1st.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//        } catch (IllegalAccessException ex) {
//            java.util.logging.Logger.getLogger(GateIn1st.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
//            java.util.logging.Logger.getLogger(GateIn1st.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//        }
//        //</editor-fold>
//
//        /* Create and display the form */
//        java.awt.EventQueue.invokeLater(new Runnable() {
//            public void run() {
//                new GateIn1st().setVisible(true);
//            }
//        });
//    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel HelperPanel;
    private javax.swing.JLabel bedAssigned;
    private javax.swing.JLabel blocking_reason;
    private javax.swing.JCheckBox breathlizerNC;
    private javax.swing.JCheckBox breathlizerNo;
    private javax.swing.JPanel breathlizerOkPanel;
    private javax.swing.JCheckBox breathlizerYes;
    private java.awt.Button button1;
    public static javax.swing.JLabel tprIdLabel ;
    public static javax.swing.JButton captureButton;
    public static javax.swing.JLabel challanDate;
    public static javax.swing.JLabel challanNo;
    private javax.swing.JButton clear;
    public static javax.swing.JButton manualEntryButton;
    public static javax.swing.JLabel digitalClock;
    public static javax.swing.JTextField dlNo;
    public static javax.swing.JLabel driverHours;
    public static javax.swing.JTextField driverId;
    public static javax.swing.JTextField driverName;
    private javax.swing.JLabel driverWorkHour;
    private javax.swing.Box.Filler filler1;
    private javax.swing.Box.Filler filler2;
    private javax.swing.Box.Filler filler3;
    public static javax.swing.JLabel fingerImageLabel;
    private javax.swing.JLabel fingerInstruction;
    private javax.swing.JCheckBox headLightNC;
    private javax.swing.JCheckBox headLightNo;
    private javax.swing.JPanel headLightPanel;
    private javax.swing.JCheckBox headLightYes;
    public static javax.swing.JCheckBox helperNo;
    public static javax.swing.JCheckBox helperYes;
    private javax.swing.JButton manualSyncButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel101;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel83;
    private javax.swing.JLabel jLabel84;
    private javax.swing.JLabel jLabel85;
    private javax.swing.JLabel jLabel86;
    private javax.swing.JLabel jLabel87;
    private javax.swing.JLabel jLabel88;
    private javax.swing.JLabel jLabel89;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JLabel jLabel90;
    private javax.swing.JLabel jLabel97;
    private javax.swing.JLabel jLabel98;
    private javax.swing.JLabel jLabel99;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JCheckBox leftSideIndicatorNC;
    private javax.swing.JCheckBox leftSideIndicatorNo;
    private javax.swing.JPanel leftSideIndicatorPanel;
    private javax.swing.JCheckBox leftSideIndicatorYes;
    public static javax.swing.JLabel lrDate;
    public static javax.swing.JLabel lrNo;
    public static javax.swing.JLabel minesId;
    private javax.swing.JCheckBox numberVisibleNC;
    private javax.swing.JCheckBox numberVisibleNo;
    private javax.swing.JPanel numberVisiblePanel;
    private javax.swing.JCheckBox numberVisibleYes;
    private javax.swing.JTextField overrides;
    public static javax.swing.JLabel paperValid;
    public static javax.swing.JLabel photo;
    
    // labels for fingerprint
 	private JLabel lblCurrentImageInfo;
 	private JLabel lblSteps;
 	private JLabel lblScore;
 	private JLabel lblInstruction;
 	private JProgressBar progressBar;
 	private JPanel fingerPrintPanel;
 	
    public static javax.swing.JButton quickCreate;
    private javax.swing.JCheckBox reverseHornNC;
    private javax.swing.JCheckBox reverseHornNo;
    private javax.swing.JPanel reverseHornPanel;
    private javax.swing.JCheckBox reverseHornYes;
    private javax.swing.JCheckBox rightSideIndicatorNC;
    private javax.swing.JCheckBox rightSideIndicatorNo;
    private javax.swing.JPanel rightSideIndicatorPanel;
    private javax.swing.JCheckBox rightSideIndicatorYes;
    private javax.swing.JCheckBox sealNC;
    private javax.swing.JCheckBox sealNo;
    private javax.swing.JPanel sealOkPanel;
    private javax.swing.JCheckBox sealYes;
    private javax.swing.JCheckBox seatBeltNC;
    private javax.swing.JCheckBox seatBeltNo;
    private javax.swing.JPanel seatBeltPanel;
    private javax.swing.JCheckBox seatBeltYes;
    private javax.swing.JCheckBox sideMirrorNC;
    private javax.swing.JCheckBox sideMirrorNo;
    private javax.swing.JPanel sideMirrorPanel;
    private javax.swing.JCheckBox sideMirrorYes;
    public static javax.swing.JLabel supplierName;
    private javax.swing.JCheckBox tailLightNC;
    private javax.swing.JCheckBox tailLightNo;
    private javax.swing.JPanel tailLightPanel;
    private javax.swing.JCheckBox tailLightYes;
    private javax.swing.JCheckBox tarpaulinNC;
    private javax.swing.JCheckBox tarpaulinNo;
    private javax.swing.JPanel tarpaulinPanel;
    private javax.swing.JCheckBox tarpaulinYes;
    private javax.swing.JLabel tatDistance;
    private javax.swing.JLabel tatTiming;
    private javax.swing.JLabel transitQcViolation;
    private javax.swing.JLabel transitViolation;
	private boolean isFingerPrintCaptureRunning = false;
	private boolean manualCaptureFinger = false;
	private Timer timer = null;
    public static javax.swing.JComboBox transporter;
    public static javax.swing.JLabel username;
    public static AutoCompleteCombo vehicleName;
    // End of variables declaration//GEN-END:variables

    private void changeToCreateMode() {
        toggleVehicle(true);

        //transporter.setBackground(UIConstant.PanelWhite);
        sealOkPanel.setBackground(UIConstant.PanelWhite);
        tarpaulinPanel.setBackground(UIConstant.PanelWhite);
        numberVisiblePanel.setBackground(UIConstant.PanelWhite);
        tailLightPanel.setBackground(UIConstant.PanelWhite);
        sideMirrorPanel.setBackground(UIConstant.PanelWhite);
        leftSideIndicatorPanel.setBackground(UIConstant.PanelWhite);
        seatBeltPanel.setBackground(UIConstant.PanelWhite);
        headLightPanel.setBackground(UIConstant.PanelWhite);
        reverseHornPanel.setBackground(UIConstant.PanelWhite);
        breathlizerOkPanel.setBackground(UIConstant.PanelWhite);
    }

    private void getWhiteBackGrounds() {
        vehicleName.setTextBackground(UIConstant.PanelWhite);
        sealOkPanel.setBackground(UIConstant.PanelWhite);
        tarpaulinPanel.setBackground(UIConstant.PanelWhite);
        numberVisiblePanel.setBackground(UIConstant.PanelWhite);
        tailLightPanel.setBackground(UIConstant.PanelWhite);
        sideMirrorPanel.setBackground(UIConstant.PanelWhite);
        leftSideIndicatorPanel.setBackground(UIConstant.PanelWhite);
        seatBeltPanel.setBackground(UIConstant.PanelWhite);
        headLightPanel.setBackground(UIConstant.PanelWhite);
        reverseHornPanel.setBackground(UIConstant.PanelWhite);
        breathlizerOkPanel.setBackground(UIConstant.PanelWhite);
    }

    /*private void toggleVehicle(boolean editable) {
    	if(editable){
    		vehicleName.setEnabled(true);
    		vehicleName.setEditable(true);
    		vehicleName.setFocusable(true);
    		vehicleName.requestFocusInWindow();
    		vehicleName.setTextBackground(UIConstant.focusPanelColor);
    		vehicleName.setText("");
    		//vehicleName.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
    	}else{
    		vehicleName.setEnabled(false);
    		vehicleName.setEditable(false);
    		//vehicleName.setBorder(null);
    		vehicleName.setBackground(Color.WHITE);
    	}
        //  transporter.setFocusable(true);
        // transporter.setEnabled(true);
    }*/
    private void toggleVehicle(boolean editable) {
    	if(editable){
    		//vehicleName.setEnabled(true);
    		vehicleName.setEditable(true);
    		vehicleName.setTextEditable(true);
    		vehicleName.setFocusable(true);
    		vehicleName.requestFocusInWindow();
    		vehicleName.setTextBackground(UIConstant.focusPanelColor);
    		vehicleName.setText("");
    		vehicleName.setTextBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
    	}else{
    		//vehicleName.setEnabled(false);
    		vehicleName.setEditable(false);
    		vehicleName.setTextEditable(false);
    		vehicleName.setTextBorder(null);
    		vehicleName.setBackground(Color.WHITE);
    	}
        //  transporter.setFocusable(true);
        // transporter.setEnabled(true);
    }
    
    private void clearInput(boolean clearToken,Connection conn) {
//    	ThreadMonitor.stop(monitor);
    	if(clearToken){
    		TokenManager.clearWorkstation();
    	}else{
    		if(token != null)
    			TokenManager.returnToken(conn, token);
    	}
    	//services
    	
    	GateInDao.stopClearScreenTimer(timer);
    	
        fingurePrintCaptureService.stop();
        isFingerCaptureRunning = false;
        isFingerPrintCaptureRunning = false;
        //holder/beans
        token = null;
        tprRecord = null;
        tpStep = null;
        driverInformation = null;
        gpv = null;
        tprBlockManager = null;
        
        entryTime = null;
//        exitTime = null;
        
        //questions
        fitnessOk = Misc.getUndefInt();
        roadPermitOk = Misc.getUndefInt();
        insuranceOk = Misc.getUndefInt();
        polutionOk = Misc.getUndefInt();
        driverSrc = Misc.getUndefInt();
        isNewVehicle = Misc.getUndefInt();
        //flags
        isDriverExist = false;
        isFingerExist = false;
        isFingerVerified = false;
        isFingerCaptured = false;
        isDriverBlacklisted = false;
        debugTemplate = null;
        //isMorphoExist = false;
        vehicleBlackListed = false;
        isPaperOk = false;
        doCheck = false;
        isTagRead = false;
        isVehicleExist = false;
        isTpRecordValid = false;
        isRequestOverride = false;
        isAutoFingerCaptureStart = false;
        tprIdLabel.setText("");
        captureCount = 1;
        //reset ui
       
        isFingerCaptureRunning = false; 
        vehicleBlackListed = false;
        
        driverHours.setText("");
        transporter.setFocusable(false);
        transporter.setEnabled(false);
        transporter.setSelectedIndex(0);
        sealOkPanel.setBackground(UIConstant.PanelWhite);
        tarpaulinPanel.setBackground(UIConstant.PanelWhite);
        numberVisiblePanel.setBackground(UIConstant.PanelWhite);
        tailLightPanel.setBackground(UIConstant.PanelWhite);
        sideMirrorPanel.setBackground(UIConstant.PanelWhite);
        leftSideIndicatorPanel.setBackground(UIConstant.PanelWhite);
        seatBeltPanel.setBackground(UIConstant.PanelWhite);
        headLightPanel.setBackground(UIConstant.PanelWhite);
        reverseHornPanel.setBackground(UIConstant.PanelWhite);
        breathlizerOkPanel.setBackground(UIConstant.PanelWhite);
        driverSrc = Misc.getUndefInt();
        photo.setIcon(null);
        blocking_reason.setText("");
        challanNo.setText("");
        lrDate.setText("");
        challanDate.setText("");
        lrNo.setText("");
        minesId.setText("");
        sealOk.clearSelection();
        tarpaulin.clearSelection();
        numberVisible.clearSelection();
        tailLightOk.clearSelection();
        sideMirrorOk.clearSelection();
        leftSideIndicator.clearSelection();
        rightSideIndicator.clearSelection();
        seatBelt.clearSelection();
        headLightOk.clearSelection();
        reverseHornOk.clearSelection();
        breathlizerOk.clearSelection();
        helperOk.clearSelection();
        bedAssigned.setText("");
        transporter.setFocusable(false);
        transporter.setEnabled(false);
        tatDistance.setText("");
        tatTiming.setText("");
        quickCreate.setText("Open Gate");
    	paperValid.setForeground(UIConstant.textFontColor);
        paperValid.setText("");
        transitViolation.setText("");
        transitQcViolation.setText("");
        supplierName.setText("");
        quickCreate.setEnabled(false);
        vehicleName.setText("");
        manualCaptureFinger = false;
        toggleVehicle(false);
        if(isManualEntry){
        	manualEntryButton.setEnabled(true);
        }
        enableDenyEntry(false);
        overrides.setText("");
        if(rfidHandler != null){
        	rfidHandler.stopReadTagData();
        }
        clearDriver();
        if(!isFingerSyncRunning)
        	MorphoSmartFunctions.getMorpho().cancelAllCurrentTask();
        DropDownValues.setTransporterList(transporter, conn, TokenManager.materialCat);
    }

    private void validateInputFields() {
        if (Utils.isNull(vehicleName.getText())) {
            JOptionPane.showMessageDialog(null, "Please Enter Vehcile Name");
            return;
        }

    }
//    private void setValuesForInstruction() {
//      HashMap<String,Boolean> instruction = new HashMap<String,Boolean>();
//       instruction.put("", false);
//    }

    private void updateTPR(Connection conn, int nextWorkStation) throws Exception {
        updateTPR(conn, nextWorkStation,false);
    }

    private void updateTPR(Connection conn, int nextWorkStation,boolean isDeny) throws Exception {
    	java.util.Date curr = new java.util.Date();
    	if(!isDeny){
    		//tprRecord.setVehicleName(vehicleName.getText());
    		tprRecord.setDriverName(driverInformation.getDriver_name());
    		tprRecord.setDlNo(driverInformation.getDl_no());
    		tprRecord.setDriverId(driverInformation.getDriver_id());
    		tprRecord.setPreStepType(TokenManager.currWorkStationType);
    		tprRecord.setPrevTpStep(TokenManager.currWorkStationId);
    		tprRecord.setPreStepDate(curr);
    		tprRecord.setUpdatedBy(TokenManager.userId);
    		tprRecord.setUpdatedOn(curr);
    		if (transporter.isEnabled()) {
    			tprRecord.setTransporterId(DropDownValues.getComboSelectedVal(transporter));
    		}
    		tprRecord.setNextStepType(nextWorkStation);
    		tprRecord.setComboEnd(new Date());
    		if (TokenManager.closeTPR) {
    			tprRecord.setTprStatus(Status.TPR.CLOSE);
    			if (token.getEpcId() != null && token.getEpcId().length() >= 20 && rfidHandler != null) {
    				rfidHandler.clearData(Utils.HexStringToByteArray(token.getEpcId()), 5);
    			}
    		}
    		tprRecord.setLatestUnloadGateInExit(new Date());
    		tprRecord.setDriverSrc(driverSrc);
    		tprRecord.setUnloadGateInName(TokenManager.userName);
    		
    	}
    	if (tprRecord.getComboStart() == null) {
			tprRecord.setComboStart(curr);
		}
    	tprRecord.setIsNewVehicle(isNewVehicle);
    	tprRecord.setEarliestUnloadGateInEntry(entryTime);
    	TPRInformation.insertUpdateTpr(conn, tprRecord);
    	
    }

    private int InsertTPRStep(Connection conn,boolean isDeny,boolean repeatProcess) throws Exception {

        if (tpStep == null || Misc.isUndef(tpStep.getId())) {
            System.out.println("[Manual Creted TpStep]");
            tpStep = new TPStep();
            tpStep.setEntryTime(entryTime);
            tpStep.setExitTime(new Date());
            tpStep.setTprId(tprRecord.getTprId());
            tpStep.setUpdatedBy(TokenManager.userId);
            tpStep.setVehicleId(tprRecord.getVehicleId());
            tpStep.setWorkStationId(TokenManager.currWorkStationId);
            tpStep.setWorkStationType(TokenManager.currWorkStationType);
            tpStep.setUpdatedOn(new Date());
            tpStep.setHasValidRf(isTagRead ? 1 : 0);
            tpStep.setMaterialCat(TokenManager.materialCat);
            if(repeatProcess)
            	tpStep.setSaveStatus(TPStep.REPEAT_PROCESS);
            else	
            	tpStep.setSaveStatus(isDeny ? TPStep.REQUEST_OVERRIDE : TPStep.SAVE_AND_CONTINUE);
            RFIDMasterDao.insert(conn, tpStep,false);
            RFIDMasterDao.insert(conn, tpStep,true);
        } else {
            //tpStep.setTprId(tprRecord.getTprId());
            long currTimeServerMillis = System.currentTimeMillis();
            tpStep.setExitTime(new Date(currTimeServerMillis));
            tpStep.setUpdatedOn(new Date(currTimeServerMillis));
            tpStep.setHasValidRf(isTagRead ? 1 : 0);
            tpStep.setMaterialCat(TokenManager.materialCat);
            if(repeatProcess)
            	tpStep.setSaveStatus(TPStep.REPEAT_PROCESS);
            else
            	tpStep.setSaveStatus(isDeny ? TPStep.REQUEST_OVERRIDE : TPStep.SAVE_AND_CONTINUE);
            RFIDMasterDao.update(conn, tpStep,false);
            RFIDMasterDao.update(conn, tpStep,true);
        }

        return tpStep.getId();
    }

    private boolean InsertTPRQuestionDetails(Connection conn, int stepId) throws Exception {
        HashMap<Integer, Integer> quesAnsList = getQuestionIdList(conn);
        boolean isInsert = true;
        for (Map.Entry<Integer, Integer> entry : quesAnsList.entrySet()) {
            Integer questionId = entry.getKey();
            Integer answerId = entry.getValue();
            GateInDao.updateTPRQuestion(conn, tprRecord.getTprId(), TokenManager.currWorkStationType, questionId, answerId, TokenManager.userId);
        }
        return isInsert;
    }

     private HashMap<Integer, Integer> getQuestionIdList(Connection conn) {
        HashMap<Integer, Integer> quesAnsList = new HashMap<Integer, Integer>();
        
        if (sealYes.isSelected()) {
            quesAnsList.put(Status.TPRQuestion.sealOk, UIConstant.YES);
        } else if (sealNo.isSelected()) {
            quesAnsList.put(Status.TPRQuestion.sealOk, UIConstant.NO);
        } else if(sealNC.isSelected()){
            quesAnsList.put(Status.TPRQuestion.sealOk, UIConstant.NC);
        } else{
        	quesAnsList.put(Status.TPRQuestion.sealOk, Misc.getUndefInt());
        }
        
        if (tarpaulinYes.isSelected()) {
            quesAnsList.put(Status.TPRQuestion.tarpaulinOk, UIConstant.YES);
        } else if (tarpaulinNo.isSelected()) {
            quesAnsList.put(Status.TPRQuestion.tarpaulinOk, UIConstant.NO);
        } else if(tarpaulinNC.isSelected()){
        	quesAnsList.put(Status.TPRQuestion.tarpaulinOk, UIConstant.NC);
        } else {
            quesAnsList.put(Status.TPRQuestion.tarpaulinOk, Misc.getUndefInt());
        }
        
        if (numberVisibleYes.isSelected()) {
            quesAnsList.put(Status.TPRQuestion.numberVisible, UIConstant.YES);
        } else if (numberVisibleNo.isSelected()) {
            quesAnsList.put(Status.TPRQuestion.numberVisible, UIConstant.NO);
        } else if (numberVisibleNC.isSelected()) {
            quesAnsList.put(Status.TPRQuestion.numberVisible, UIConstant.NC);
        } else {
            quesAnsList.put(Status.TPRQuestion.numberVisible, Misc.getUndefInt());
        }
        
        if (tailLightYes.isSelected()) {
            quesAnsList.put(Status.TPRQuestion.tailLightOk, UIConstant.YES);
        } else if (tailLightNo.isSelected()) {
            quesAnsList.put(Status.TPRQuestion.tailLightOk, UIConstant.NO);
        } else if (tailLightNC.isSelected()) {
            quesAnsList.put(Status.TPRQuestion.tailLightOk, UIConstant.NC);
        } else {
            quesAnsList.put(Status.TPRQuestion.tailLightOk, Misc.getUndefInt());
        }
        
        if (sideMirrorYes.isSelected()) {
            quesAnsList.put(Status.TPRQuestion.sideMirror, UIConstant.YES);
        } else if (sideMirrorNo.isSelected()) {
            quesAnsList.put(Status.TPRQuestion.sideMirror, UIConstant.NO);
        } else if (sideMirrorNC.isSelected()) {
            quesAnsList.put(Status.TPRQuestion.sideMirror, UIConstant.NC);
        } else {
            quesAnsList.put(Status.TPRQuestion.sideMirror, Misc.getUndefInt());
        }
        
        if (leftSideIndicatorYes.isSelected()) {
            quesAnsList.put(Status.TPRQuestion.leftSideIndicator, UIConstant.YES);
        } else if (leftSideIndicatorNo.isSelected()) {
            quesAnsList.put(Status.TPRQuestion.leftSideIndicator, UIConstant.NO);
        } else if (leftSideIndicatorNC.isSelected()) {
            quesAnsList.put(Status.TPRQuestion.leftSideIndicator, UIConstant.NC);
        } else {
            quesAnsList.put(Status.TPRQuestion.leftSideIndicator, Misc.getUndefInt());
        }
        
        if (seatBeltYes.isSelected()) {
            quesAnsList.put(Status.TPRQuestion.seatBeltWorm, UIConstant.YES);
        } else if (seatBeltNo.isSelected()) {
            quesAnsList.put(Status.TPRQuestion.seatBeltWorm, UIConstant.NO);
        } else if (seatBeltNC.isSelected()) {
            quesAnsList.put(Status.TPRQuestion.seatBeltWorm, UIConstant.NC);
        } else {
            quesAnsList.put(Status.TPRQuestion.seatBeltWorm, Misc.getUndefInt());
        }
       
        if (headLightYes.isSelected()) {
            quesAnsList.put(Status.TPRQuestion.headLightOk, UIConstant.YES);
        } else if (headLightNo.isSelected()) {
            quesAnsList.put(Status.TPRQuestion.headLightOk, UIConstant.NO);
        } else if (headLightNC.isSelected()) {
            quesAnsList.put(Status.TPRQuestion.headLightOk, UIConstant.NC);
        } else {
            quesAnsList.put(Status.TPRQuestion.headLightOk, Misc.getUndefInt());
        }
        
        if (reverseHornYes.isSelected()) {
            quesAnsList.put(Status.TPRQuestion.reverseHornOk, UIConstant.YES);
        } else if (reverseHornNo.isSelected()) {
            quesAnsList.put(Status.TPRQuestion.reverseHornOk, UIConstant.NO);
        } else if (reverseHornNC.isSelected()) {
            quesAnsList.put(Status.TPRQuestion.reverseHornOk, UIConstant.NC);
        } else {
            quesAnsList.put(Status.TPRQuestion.reverseHornOk, Misc.getUndefInt());
        }
        
        if (breathlizerYes.isSelected()) {
            quesAnsList.put(Status.TPRQuestion.breathLyzerOk, UIConstant.YES);
        } else if (breathlizerNo.isSelected()) {
            quesAnsList.put(Status.TPRQuestion.breathLyzerOk, UIConstant.NO);
        } else if (breathlizerNC.isSelected()) {
            quesAnsList.put(Status.TPRQuestion.breathLyzerOk, UIConstant.NC);
        } else {
            quesAnsList.put(Status.TPRQuestion.breathLyzerOk, Misc.getUndefInt());
        }
        
        if (helperYes.isSelected()) {
            quesAnsList.put(Status.TPRQuestion.drivenByHelper, UIConstant.YES);
        } else if (helperNo.isSelected()) {
            quesAnsList.put(Status.TPRQuestion.drivenByHelper, UIConstant.NO);
        } else {
            quesAnsList.put(Status.TPRQuestion.drivenByHelper, Misc.getUndefInt());
        }
        
        if (rightSideIndicatorYes.isSelected()) {
            quesAnsList.put(Status.TPRQuestion.rightSideIndicator, UIConstant.YES);
        } else if (rightSideIndicatorNo.isSelected()) {
            quesAnsList.put(Status.TPRQuestion.rightSideIndicator, UIConstant.NO);
        } else if (rightSideIndicatorNC.isSelected()) {
            quesAnsList.put(Status.TPRQuestion.rightSideIndicator, UIConstant.NC);
        } else {
            quesAnsList.put(Status.TPRQuestion.rightSideIndicator, Misc.getUndefInt());
        }
        int blockStatus = Misc.getUndefInt();
        if(tprBlockManager != null)
        	blockStatus = tprBlockManager.getBlockStatus();
        if(blockStatus != UIConstant.BLOCKED){
        	quesAnsList.put(Status.TPRQuestion.isTagRead, isTagRead ? Results.Questions.YES : Results.Questions.NO);
        	//if(isFingerCaptured)
        	quesAnsList.put(Status.TPRQuestion.isFingerVerified, isFingerVerified ? Results.Questions.YES : Results.Questions.NO);
        	quesAnsList.put(Status.TPRQuestion.isFingerCaptured, isFingerCaptured ? Results.Questions.YES : Results.Questions.NO);
        	quesAnsList.put(Status.TPRQuestion.isVehicleExist, isVehicleExist ? Results.Questions.YES : Results.Questions.NO);
        	quesAnsList.put(Status.TPRQuestion.isChallanExist, !Utils.isNull(tprRecord.getChallanNo())  ? Results.Questions.YES : Results.Questions.NO);
        	if(!Misc.isUndef(fitnessOk))
        		quesAnsList.put(Status.TPRQuestion.isFitnessOk, fitnessOk);
        	if(!Misc.isUndef(roadPermitOk))
        		quesAnsList.put(Status.TPRQuestion.isRoadPermitOk, roadPermitOk);
        	if(!Misc.isUndef(insuranceOk))
        		quesAnsList.put(Status.TPRQuestion.isInsuranceOk, insuranceOk);
        	if(!Misc.isUndef(polutionOk))
        		quesAnsList.put(Status.TPRQuestion.isPolutionOk, polutionOk);
        	quesAnsList.put(Status.TPRQuestion.isDriverExist, isDriverExist ? Results.Questions.YES : Results.Questions.NO);
        	quesAnsList.put(Status.TPRQuestion.isFingerExist, isFingerExist ? Results.Questions.YES : Results.Questions.NO);
        	quesAnsList.put(Status.TPRQuestion.isDriverBlacklisted, isDriverBlacklisted ? UIConstant.NO : UIConstant.YES);
        	/*Triple<Integer, Long, Long> driverBlockStatus = TPRUtils.getDriverBlockStatus(conn, tprRecord.getDriverId());
        	if(driverBlockStatus != null){
        		long curr = System.currentTimeMillis();
        		boolean isBlock = !Misc.isUndef(driverBlockStatus.first) && !Misc.isUndef(driverBlockStatus.second) && (curr >= driverBlockStatus.second && (curr <= driverBlockStatus.third || Misc.isUndef(driverBlockStatus.third)));
        		
        	}*/
        }
        return quesAnsList;
    }
     
    private void updateCurrentBlocking(Connection conn){
    	if(tprBlockManager == null)
    		return;
    	HashMap<Integer, Integer> quesAnsList = getQuestionIdList(conn);
    	 for (Map.Entry<Integer, Integer> entry : quesAnsList.entrySet()) {
             Integer questionId = entry.getKey();
             Integer answerId = entry.getValue();
             setQuestionsBlocking(questionId,answerId);
         }
    }

    private String getInstruction(Connection conn) {
        StringBuilder instructionString = null;
        if (tprBlockManager != null && tprBlockManager.useForBlocking(Status.TPRQuestion.isFingerCaptured) && !isFingerCaptured) {
        	if(instructionString == null)
        		instructionString = new StringBuilder();
        	else
        		instructionString.append("\n");
            instructionString.append("Finger Not Captured.");
        } 
        if (tprBlockManager != null && tprBlockManager.useForBlocking(Status.TPRQuestion.isFingerVerified) && isFingerCaptured && !isFingerVerified) {
        	if(instructionString == null)
        		instructionString = new StringBuilder();
        	else
        		instructionString.append("\n");
            instructionString.append("Finger Not Verified.");
        }
        if (tprBlockManager != null && tprBlockManager.useForBlocking(Status.TPRQuestion.isDriverExist) && !isDriverExist) {
        	if(instructionString == null)
        		instructionString = new StringBuilder();
        	else
        		instructionString.append("\n");
            instructionString.append("Driver Not Reg.");
        } 
        if (tprBlockManager != null && tprBlockManager.useForBlocking(Status.TPRQuestion.isFingerExist) && !isFingerExist) {
        	if(instructionString == null)
        		instructionString = new StringBuilder();
        	else
        		instructionString.append("\n");
            instructionString.append("Driver Finger Not Reg.");
        }
        if (tprBlockManager != null && tprBlockManager.useForBlocking(Status.TPRQuestion.isDriverBlacklisted) && isDriverBlacklisted) {
        	if(instructionString == null)
        		instructionString = new StringBuilder();
        	else
        		instructionString.append("\n");
            instructionString.append("Driver Blacklisted.");
        }
        if (tprBlockManager != null && tprBlockManager.useForBlocking(Status.TPRQuestion.isTagRead) && !isTagRead ) {
        	if(instructionString == null)
        		instructionString = new StringBuilder();
        	else
        		instructionString.append("\n");
            instructionString.append("RF Not Working.");
        }
        if (tprBlockManager != null && tprBlockManager.useForBlocking(Status.TPRQuestion.isVehicleExist) && !isVehicleExist ) {
        	if(instructionString == null)
        		instructionString = new StringBuilder();
        	else
        		instructionString.append("\n");
            instructionString.append("Vehicle Not Exist.");
        }
        if (tprBlockManager != null && tprBlockManager.useForBlocking(Status.TPRQuestion.breathLyzerOk) && breathlizerNo.isSelected()) {
        	if(instructionString == null)
        		instructionString = new StringBuilder();
        	else
        		instructionString.append("\n");
            instructionString.append("Driver fails Breathlyzer Test.");
        }
        if (tprBlockManager != null && tprBlockManager.useForBlocking(Status.TPRQuestion.isChallanExist) && Utils.isNull(tprRecord.getChallanNo())) {
        	if(instructionString == null)
        		instructionString = new StringBuilder();
        	else
        		instructionString.append("\n");
            instructionString.append("Challan Data Missing.");
        }
        if (tprBlockManager != null && tprBlockManager.useForBlockingByInstructionId(com.ipssi.rfid.constant.Type.BlockingInstruction.BLOCK_DUETO_MULTIPLE_TPR) && tprRecord.getIsMultipleOpenTPR() == 1) {
        	if(instructionString == null)
        		instructionString = new StringBuilder();
        	else
        		instructionString.append("\n");
            instructionString.append("Multiple TPR.");
        }
        if (
        		tprBlockManager != null  && (
                tprBlockManager.useForBlocking(Status.TPRQuestion.isFitnessOk) 
                ||
                tprBlockManager.useForBlocking(Status.TPRQuestion.isRoadPermitOk) 
                ||
                tprBlockManager.useForBlocking(Status.TPRQuestion.isInsuranceOk)
                ||
                tprBlockManager.useForBlocking(Status.TPRQuestion.isPolutionOk)
                )
                && !isPaperOk) {
        	if(instructionString == null)
        		instructionString = new StringBuilder();
        	else
        		instructionString.append("\n");
            instructionString.append("Paper Not Valid.");
        }
        return instructionString == null || instructionString.length() == 0   ? null : instructionString.toString() + "\n GOTO Registration.";
    }

    

    void setWhiteBackColor() {
        vehicleName.setBackground(UIConstant.PanelWhite);
        sealOkPanel.setBackground(UIConstant.PanelWhite);
        tarpaulinPanel.setBackground(UIConstant.PanelWhite);
        numberVisiblePanel.setBackground(UIConstant.PanelWhite);
        tailLightPanel.setBackground(UIConstant.PanelWhite);
        sideMirrorPanel.setBackground(UIConstant.PanelWhite);
        leftSideIndicatorPanel.setBackground(UIConstant.PanelWhite);
        rightSideIndicatorPanel.setBackground(UIConstant.PanelWhite);
        seatBeltPanel.setBackground(UIConstant.PanelWhite);
        headLightPanel.setBackground(UIConstant.PanelWhite);
        reverseHornPanel.setBackground(UIConstant.PanelWhite);
        breathlizerOkPanel.setBackground(UIConstant.PanelWhite);
    }

//    private void setTransporterList(Connection conn) {
//        ArrayList<ComboItem> transporterList = DropDownValues.getTranporterList(conn);
//        for (int i = 0; i < transporterList.size(); i++) {
//            transporter.addItem(transporterList.get(i));
//            transporter.setSelectedIndex(0);
//        }

//    }
    private void quickCreateAndSave() {
    	Connection conn = null;
    	boolean destroyIt = false;
    	try{
    		conn = DBConnectionPool.getConnectionFromPoolNonWeb();
    		if(isRequestOverride){
    			requestOverrideAction();
    			return;
    		}
    		if(vehicleName.isEnabled() && Utils.isNull(vehicleName.getText())) {
    			JOptionPane.showMessageDialog(null, "Please Enter Vehicle Name");
    			vehicleName.requestFocusInWindow();
    			return;
    		} 
    		else if (transporter.isEnabled() && DropDownValues.isNull(transporter)) {
    			JOptionPane.showMessageDialog(null, "Please Select Transporter");
    			setWhiteBackColor();
    			transporter.requestFocusInWindow();
    			return;
    		} else {
    			if (!isTpRecordValid) {
    				JOptionPane.showMessageDialog(null, "Vehicle Not Exist Please go for Registration");
    				return;
    			} else if (!seatBeltNo.isSelected() && !seatBeltYes.isSelected() && !seatBeltNC.isSelected()) {
    				JOptionPane.showMessageDialog(null, "Please Give Answer to All Question");
    				setWhiteBackColor();
    				seatBeltPanel.setBackground(UIConstant.focusPanelColor);
    				seatBeltPanel.requestFocusInWindow();
    			} else if (!tarpaulinNo.isSelected() && !tarpaulinYes.isSelected() && !tarpaulinNC.isSelected()) {
    				JOptionPane.showMessageDialog(null, "Please Give Answer to All Question");
    				setWhiteBackColor();
    				tarpaulinPanel.setBackground(UIConstant.focusPanelColor);
    				tarpaulinPanel.requestFocusInWindow();
    			} else if (!sealNo.isSelected() && !sealYes.isSelected() && !sealNC.isSelected()) {
    				JOptionPane.showMessageDialog(null, "Please Give Answer to All Question");
    				setWhiteBackColor();
    				sealOkPanel.setBackground(UIConstant.focusPanelColor);
    				sealOkPanel.requestFocusInWindow();
    			} else if (!numberVisibleNo.isSelected() && !numberVisibleYes.isSelected() && !numberVisibleNC.isSelected()) {
    				JOptionPane.showMessageDialog(null, "Please Give Answer to All Question");
    				setWhiteBackColor();
    				numberVisiblePanel.setBackground(UIConstant.focusPanelColor);
    				numberVisiblePanel.requestFocusInWindow();
    			} else if (!sideMirrorNo.isSelected() && !sideMirrorYes.isSelected() && !sideMirrorNC.isSelected()) {
    				JOptionPane.showMessageDialog(null, "Please Give Answer to All Question");
    				setWhiteBackColor();
    				sideMirrorPanel.setBackground(UIConstant.focusPanelColor);
    				sideMirrorPanel.requestFocusInWindow();
    			} else if (!reverseHornNo.isSelected() && !reverseHornYes.isSelected() && !reverseHornNC.isSelected()) {
    				JOptionPane.showMessageDialog(null, "Please Give Answer to All Question");
    				setWhiteBackColor();
    				reverseHornPanel.setBackground(UIConstant.focusPanelColor);
    				reverseHornPanel.requestFocusInWindow();
    			} else if (!tailLightNo.isSelected() && !tailLightYes.isSelected() && !tailLightNC.isSelected()) {
    				JOptionPane.showMessageDialog(null, "Please Give Answer to All Question");
    				setWhiteBackColor();
    				tailLightPanel.setBackground(UIConstant.focusPanelColor);
    				tailLightPanel.requestFocusInWindow();
    			} else if (!headLightNo.isSelected() && !headLightYes.isSelected() && !headLightNC.isSelected()) {
    				JOptionPane.showMessageDialog(null, "Please Give Answer to All Question");
    				setWhiteBackColor();
    				headLightPanel.setBackground(UIConstant.focusPanelColor);
    				headLightPanel.requestFocusInWindow();
    			} else if (!leftSideIndicatorNo.isSelected() && !leftSideIndicatorYes.isSelected() && !leftSideIndicatorNC.isSelected()) {
    				JOptionPane.showMessageDialog(null, "Please Give Answer to All Question");
    				setWhiteBackColor();
    				leftSideIndicatorPanel.setBackground(UIConstant.focusPanelColor);
    				leftSideIndicatorPanel.requestFocusInWindow();
    			} else if (!rightSideIndicatorNo.isSelected() && !rightSideIndicatorYes.isSelected() && !rightSideIndicatorNC.isSelected()) {
    				JOptionPane.showMessageDialog(null, "Please Give Answer to All Question");
    				setWhiteBackColor();
    				rightSideIndicatorPanel.setBackground(UIConstant.focusPanelColor);
    				rightSideIndicatorPanel.requestFocusInWindow();
    			} else if (!breathlizerNo.isSelected() && !breathlizerYes.isSelected() && !breathlizerNC.isSelected()) {
    				JOptionPane.showMessageDialog(null, "Please Give Answer to All Question");
    				setWhiteBackColor();
    				breathlizerOkPanel.setBackground(UIConstant.focusPanelColor);
    				breathlizerOkPanel.requestFocusInWindow();
    			} else if (
    					// captureButton.isVisible() 
    					// && 
    					driverInformation == null) {
    				JOptionPane.showMessageDialog(null, "Please Fill Driver Details ");
    				if (driverId.isEditable()) {
    					driverId.requestFocusInWindow();
    				} else if (dlNo.isEditable()) {
    					dlNo.requestFocusInWindow();
    				} else if (driverName.isEditable()) {
    					driverName.requestFocusInWindow();
    				}
    				return;
    			} else if (Utils.isNull(driverName.getText()) || Utils.isNull(dlNo.getText())) {
    				JOptionPane.showMessageDialog(null, "Please Fill Driver Details ");
    				if (driverId.isEditable()) {
    					driverId.requestFocusInWindow();
    				} else if (dlNo.isEditable()) {
    					dlNo.requestFocusInWindow();
    				} else if (driverName.isEditable()) {
    					driverName.requestFocusInWindow();
    				}
    				return;
    			} else {
    				System.out.println("########### Quick Create And Save Start  ##########");
    				Triple<TPRecord, Integer, Boolean> tprTriplet = TPRInformation.getLatestNonWeb(conn, token.getVehicleId(), null, vehicleName.getText(), TokenManager.currWorkStationType, TokenManager.materialCat);//(conn, vehicleId, data, vehicleName, TokenManager.createNewTPR, workStationType);
    				TPRecord tpr = tprTriplet != null ? tprTriplet.first : null;
					boolean repeatProcess = false;
    				if(tpr != null && TPRInformation.isGreaterThanEqualsProcessed(tpr, TokenManager.currWorkStationType, TokenManager.materialCat)/*tpr.getTprId() != tprRecord.getTprId()*/){
    					repeatProcess = true;
    					tprRecord = tpr;
    				}else{
    					repeatProcess = false;
    				}
					String instruction = getInstruction(conn);
    				System.out.println("vehicleName : " + vehicleName.getText());
    				System.out.println("vehicle_Id: " + tprRecord.getVehicleId());
    				int nextWorkstationType =instruction == null ? TokenManager.nextWorkStationType : com.ipssi.rfid.constant.Type.WorkStationType.REGISTRATION; 
    				
    				if(!repeatProcess)
    					updateTPR(conn, nextWorkstationType);
    				
    				int stepId = InsertTPRStep(conn,false,repeatProcess);
    				if (!Misc.isUndef(stepId)) {
    					InsertTPRQuestionDetails(conn, stepId);
    				}
    				if(debugTemplate != null){
    					GateInDao.insertTemplate(conn,tprRecord.getTprId(),debugTemplate);
    				}
    				if(tprBlockManager != null){
    		    		updateCurrentBlocking(conn);
    		    		tprBlockManager.calculateBlocking(conn);
    		    		setBlockingStatus();
    		    		tprBlockManager.setTprBlockStatus(conn, tprRecord.getTprId(),TokenManager.userId);
    		    	}
    				conn.commit();
    				if (true) {
    					System.out.println("########### Print Started  ##########");
    					System.out.println("IsVehicleExist: " + GateInDao.getValue(isVehicleExist) + " isFingerCaptured: "
    							+ "" + GateInDao.getValue(isFingerCaptured) + " isFingerVerified: "
    							+ GateInDao.getValue(isFingerVerified) + " isChallanExist"
    							+ " " + GateInDao.getValue(!Utils.isNull(tprRecord.getChallanNo())) + " isTpRecordValid: "
    							+ "" + GateInDao.getValue(isTpRecordValid) + " isTagRead: " + GateInDao.getValue(isTagRead));
    					System.out.println("## GO To "+com.ipssi.rfid.constant.Type.WorkStationType.getString(nextWorkstationType)+" ##");
    					DocPrinter.print(tprRecord.getVehicleName(), new Date(), tprRecord.getTprId(), DropDownValues.getTransporter(tprRecord.getTransporterId(), conn), tprRecord.getDriverName(), WorkStationType.getString(tprRecord.getNextStepType()), 
    							isVehicleExist, isTagRead, isFingerCaptured, isFingerVerified, !Utils.isNull(tprRecord.getChallanNo()),TokenManager.Printer_Connected,bedAssigned.getText(),nextWorkstationType,isPaperOk, isDriverExist, isFingerExist,isDriverBlacklisted,tprRecord.getIsMultipleOpenTPR() == 1,gpsStat);
//    					JOptionPane.showMessageDialog(null, "Detail Saved");
    					Barrier.openEntryGate();
    					WaveFormPlayer.playSoundIn(Status.TPRQuestion.saveDetail);
    					fingurePrintCaptureService.stop();
    					clearInput(false,conn);
    					getFocus();
    					System.out.println("########### Successfully Saved  ##########");
    				} 
    				if(repeatProcess){
    					JOptionPane.showMessageDialog(null, "This Step has already been completed, Please Confirm next step from control room");
    				}else if (!Utils.isNull(instruction) ) {
    					JOptionPane.showMessageDialog(null, instruction);
    				} else {
    					JOptionPane.showMessageDialog(null, "Go to Weighbridge In");
    				}
    			}
    		}
    	}catch(Exception ex){
    		JOptionPane.showMessageDialog(null, UIConstant.SAVE_FAILER_MESSAGE);
    		ex.printStackTrace();
    		destroyIt = true;
    	}finally{
    		try{
   			 DBConnectionPool.returnConnectionToPoolNonWeb(conn, destroyIt);
   		 }catch(Exception ex){
   			 ex.printStackTrace();
   		 }
    	}

    }


    private void tarpaulinAction() {
        sealOkPanel.setBackground(UIConstant.focusPanelColor);
        sealOkPanel.requestFocusInWindow();
        tarpaulinPanel.setBackground(UIConstant.PanelWhite);
        tailLightPanel.setBackground(UIConstant.PanelWhite);
        numberVisiblePanel.setBackground(UIConstant.PanelWhite);
        sideMirrorPanel.setBackground(UIConstant.PanelWhite);
        leftSideIndicatorPanel.setBackground(UIConstant.PanelWhite);
        rightSideIndicatorPanel.setBackground(UIConstant.PanelWhite);
        seatBeltPanel.setBackground(UIConstant.PanelWhite);
        headLightPanel.setBackground(UIConstant.PanelWhite);
        reverseHornPanel.setBackground(UIConstant.PanelWhite);
        breathlizerOkPanel.setBackground(UIConstant.PanelWhite);
    }

    private void numberVisibleAction() {
        sealOkPanel.setBackground(UIConstant.PanelWhite);
        tarpaulinPanel.setBackground(UIConstant.PanelWhite);
        numberVisiblePanel.setBackground(UIConstant.PanelWhite);
        sideMirrorPanel.requestFocusInWindow();
        sideMirrorPanel.setBackground(UIConstant.focusPanelColor);
        headLightPanel.setBackground(UIConstant.PanelWhite);
        leftSideIndicatorPanel.setBackground(UIConstant.PanelWhite);
        rightSideIndicatorPanel.setBackground(UIConstant.PanelWhite);
        seatBeltPanel.setBackground(UIConstant.PanelWhite);
        tailLightPanel.setBackground(UIConstant.PanelWhite);
        reverseHornPanel.setBackground(UIConstant.PanelWhite);
        breathlizerOkPanel.setBackground(UIConstant.PanelWhite);
    }

    private void headLightAction() {
        sealOkPanel.setBackground(UIConstant.PanelWhite);
        tarpaulinPanel.setBackground(UIConstant.PanelWhite);
        numberVisiblePanel.setBackground(UIConstant.PanelWhite);
        tailLightPanel.setBackground(UIConstant.PanelWhite);
        sideMirrorPanel.setBackground(UIConstant.PanelWhite);
        leftSideIndicatorPanel.setBackground(UIConstant.focusPanelColor);
        leftSideIndicatorPanel.requestFocusInWindow();
        rightSideIndicatorPanel.setBackground(UIConstant.PanelWhite);
        reverseHornPanel.setBackground(UIConstant.PanelWhite);
        headLightPanel.setBackground(UIConstant.PanelWhite);
        breathlizerOkPanel.setBackground(UIConstant.PanelWhite);
        seatBeltPanel.setBackground(UIConstant.PanelWhite);
    }

    private void seatBeltAction() {
        sealOkPanel.setBackground(UIConstant.PanelWhite);
        tarpaulinPanel.setBackground(UIConstant.focusPanelColor);
        tarpaulinPanel.requestFocusInWindow();
        numberVisiblePanel.setBackground(UIConstant.PanelWhite);
        tailLightPanel.setBackground(UIConstant.PanelWhite);
        headLightPanel.setBackground(UIConstant.PanelWhite);
        leftSideIndicatorPanel.setBackground(UIConstant.PanelWhite);
        rightSideIndicatorPanel.setBackground(UIConstant.PanelWhite);
        seatBeltPanel.setBackground(UIConstant.PanelWhite);
//        sideMirrorPanel.requestFocusInWindow();
        sideMirrorPanel.setBackground(UIConstant.PanelWhite);
        reverseHornPanel.setBackground(UIConstant.PanelWhite);
        breathlizerOkPanel.setBackground(UIConstant.PanelWhite);
    }

    private void leftSideIndicatorAction() {
        sealOkPanel.setBackground(UIConstant.PanelWhite);
        tarpaulinPanel.setBackground(UIConstant.PanelWhite);
        numberVisiblePanel.setBackground(UIConstant.PanelWhite);
        tailLightPanel.setBackground(UIConstant.PanelWhite);
        sideMirrorPanel.setBackground(UIConstant.PanelWhite);
        leftSideIndicatorPanel.setBackground(UIConstant.PanelWhite);
        rightSideIndicatorPanel.requestFocusInWindow();
        rightSideIndicatorPanel.setBackground(UIConstant.focusPanelColor);
        headLightPanel.setBackground(UIConstant.PanelWhite);
        seatBeltPanel.setBackground(UIConstant.PanelWhite);
        breathlizerOkPanel.setBackground(UIConstant.PanelWhite);
        reverseHornPanel.setBackground(UIConstant.PanelWhite);
    }

    private void reverseHornAction() {
        seatBeltPanel.setBackground(UIConstant.PanelWhite);
        tarpaulinPanel.setBackground(UIConstant.PanelWhite);
        sealOkPanel.setBackground(UIConstant.PanelWhite);
        numberVisiblePanel.setBackground(UIConstant.PanelWhite);
        sideMirrorPanel.setBackground(UIConstant.PanelWhite);
        reverseHornPanel.setBackground(UIConstant.PanelWhite);
        tailLightPanel.setBackground(UIConstant.focusPanelColor);
        tailLightPanel.requestFocusInWindow();
        headLightPanel.setBackground(UIConstant.PanelWhite);
        leftSideIndicatorPanel.setBackground(UIConstant.PanelWhite);
        rightSideIndicatorPanel.setBackground(UIConstant.PanelWhite);
        breathlizerOkPanel.setBackground(UIConstant.PanelWhite);


    }

    private void clearAction() {
    	Connection conn = null;
    	boolean destroyIt = false;
    	try{
    		conn = DBConnectionPool.getConnectionFromPoolNonWeb();
    		clearInput(true,conn);
    		getFocus();
    	}catch(Exception ex){
    		ex.printStackTrace();
    		destroyIt = true;
    	}finally{
    		try{
    			DBConnectionPool.returnConnectionToPoolNonWeb(conn, destroyIt);
    		}catch(Exception ex){
    			ex.printStackTrace();
    		}
    	}
    }
    
    private void startFingerCaptureService() {
    	startFingerCaptureService(false);
    }
    private void startFingerCaptureService(boolean isManual) {
//        getWhiteBackGrounds();
        
        if ((vehicleName.getText().trim()).equalsIgnoreCase("NO VEHICLE DETECTED")) {
            //JOptionPane.showMessageDialog(null, "Please Enter Vehicle Name");
            return;
        } else if (Utils.isNull(vehicleName.getText())) {
            //JOptionPane.showMessageDialog(null, "Please Enter Vehicle Name");
            return;
        }else if (tprRecord == null) {
            //JOptionPane.showMessageDialog(null, "Please Enter valid Vehicle Name");
            return;
        }else if(isFingerCaptureRunning || isFingerSyncRunning || !isMorphoExist){
        	return;
        }else if(!isAutoFingerCaptureStart || isManual) {
            try {
            	WaveFormPlayer.playSoundOut(Status.TPRQuestion.cleanFinger);
                driverId.setText("");
                dlNo.setText("");
                driverName.setText("");
                helperOk.clearSelection();
                photo.setIcon(null);
                isFingerCaptured = false;
                isFingerVerified = false;
                clearDriver();
                
                MorphoSmartFunctions.getMorpho().cancelAllCurrentTask();
                fingurePrintCaptureService.start();
                isFingerCaptureRunning = true;
                isAutoFingerCaptureStart = true;
                captureCount++;	
            } catch (Exception ex) {
                Logger.getLogger(CoalGateInWindow.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void sealOkAction() {
        // TODO add your handling code here:
        //sealOkPanel.requestFocusInWindow();
        //sealOkPanel.setBackground(UIConstant.focusPanelColor);
        sealOkPanel.setBackground(UIConstant.PanelWhite);
        numberVisiblePanel.setBackground(UIConstant.focusPanelColor);
        numberVisiblePanel.requestFocusInWindow();
        tarpaulinPanel.setBackground(UIConstant.PanelWhite);
        //sealOkPanel.setBackground(UIConstant.focusPanelColor);
//        numberVisiblePanel.setBackground(UIConstant.PanelWhite);
        tailLightPanel.setBackground(UIConstant.PanelWhite);
        sideMirrorPanel.setBackground(UIConstant.PanelWhite);
        leftSideIndicatorPanel.setBackground(UIConstant.PanelWhite);
        rightSideIndicatorPanel.setBackground(UIConstant.PanelWhite);
        seatBeltPanel.setBackground(UIConstant.PanelWhite);
        headLightPanel.setBackground(UIConstant.PanelWhite);
        reverseHornPanel.setBackground(UIConstant.PanelWhite);
        breathlizerOkPanel.setBackground(UIConstant.PanelWhite);
    }

    private void sideMirrorAction() {
        sealOkPanel.setBackground(UIConstant.PanelWhite);
        tarpaulinPanel.setBackground(UIConstant.PanelWhite);
        numberVisiblePanel.setBackground(UIConstant.PanelWhite);
        tailLightPanel.setBackground(UIConstant.PanelWhite);
        sideMirrorPanel.setBackground(UIConstant.PanelWhite);
        leftSideIndicatorPanel.setBackground(UIConstant.PanelWhite);
        rightSideIndicatorPanel.setBackground(UIConstant.PanelWhite);
        seatBeltPanel.setBackground(UIConstant.PanelWhite);
        headLightPanel.setBackground(UIConstant.PanelWhite);
        breathlizerOkPanel.setBackground(UIConstant.PanelWhite);
        reverseHornPanel.setBackground(UIConstant.focusPanelColor);
        reverseHornPanel.requestFocusInWindow();
    }

    private void rightSideIndicatorAction() {
        sealOkPanel.setBackground(UIConstant.PanelWhite);
        tarpaulinPanel.setBackground(UIConstant.PanelWhite);
        numberVisiblePanel.setBackground(UIConstant.PanelWhite);
        tailLightPanel.setBackground(UIConstant.PanelWhite);
        sideMirrorPanel.setBackground(UIConstant.PanelWhite);
        rightSideIndicatorPanel.setBackground(UIConstant.PanelWhite);
        leftSideIndicatorPanel.setBackground(UIConstant.PanelWhite);
        headLightPanel.setBackground(UIConstant.PanelWhite);
        seatBeltPanel.setBackground(UIConstant.PanelWhite);
        breathlizerOkPanel.setBackground(UIConstant.focusPanelColor);
        breathlizerOkPanel.requestFocusInWindow();
        reverseHornPanel.setBackground(UIConstant.PanelWhite);
    }

    private void syncFingerPrintDataFromServer() {
        if (fingerPrintSyncService == null) {
            System.out.println("TokenManager.currWorkStationId : " + TokenManager.currWorkStationId);
            fingerPrintSyncService = new SyncFingerPrint();
//            isDataSyncRunning = true;
            fingerPrintSyncService.setHandler(new SynServiceHandler() {
                @Override
                public void onChange(boolean onChange) {
                    if (onChange) {
                        fingerInstruction.setText("Sync Service Running");
                    } else {
                        fingerInstruction.setText("");
                    }
                    isFingerSyncRunning = onChange;
                    changeDriverPanel(onChange);
                    manualSyncButton.setEnabled(!onChange);
                }

				@Override
				public void notifyText(String msg) {
					// TODO Auto-generated method stub
					fingerInstruction.setText(msg);
				}

				@Override
				public void setDeviceId(String msg) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void setCapacity(int capacity) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void setEnrolled(int enrolled) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void clearingData() {
					// TODO Auto-generated method stub
					fingerInstruction.setText("Clearing Data....");
				}

				@Override
				public void init(String deviceId, int capacity, int enrolled) {
					// TODO Auto-generated method stub
					
				}
            });
            fingerPrintSyncService.start();
        }
    }

    private void checkQuestions() {
        doCheck = true;
        if (!seatBeltNo.isSelected() && !seatBeltYes.isSelected() && !seatBeltNC.isSelected()) {
//                JOptionPane.showMessageDialog(null, "Please Give Answer to All Question");
            setWhiteBackColor();
            seatBeltPanel.setBackground(UIConstant.focusPanelColor);
            seatBeltPanel.requestFocusInWindow();
        } else if (!tarpaulinNo.isSelected() && !tarpaulinYes.isSelected() && !tarpaulinNC.isSelected()) {
//                JOptionPane.showMessageDialog(null, "Please Give Answer to All Question");
            setWhiteBackColor();
            tarpaulinPanel.setBackground(UIConstant.focusPanelColor);
            tarpaulinPanel.requestFocusInWindow();
        } else if (!sealNo.isSelected() && !sealYes.isSelected() && !sealNC.isSelected()) {
//                JOptionPane.showMessageDialog(null, "Please Give Answer to All Question");
            setWhiteBackColor();
            sealOkPanel.setBackground(UIConstant.focusPanelColor);
            sealOkPanel.requestFocusInWindow();
        } else if (!numberVisibleNo.isSelected() && !numberVisibleYes.isSelected() && !numberVisibleNC.isSelected()) {
//                JOptionPane.showMessageDialog(null, "Please Give Answer to All Question");
            setWhiteBackColor();
            numberVisiblePanel.setBackground(UIConstant.focusPanelColor);
            numberVisiblePanel.requestFocusInWindow();
        } else if (!sideMirrorNo.isSelected() && !sideMirrorYes.isSelected() && !sideMirrorNC.isSelected()) {
//                JOptionPane.showMessageDialog(null, "Please Give Answer to All Question");
            setWhiteBackColor();
            sideMirrorPanel.setBackground(UIConstant.focusPanelColor);
            sideMirrorPanel.requestFocusInWindow();
        } else if (!reverseHornNo.isSelected() && !reverseHornYes.isSelected() && !reverseHornNC.isSelected()) {
//                JOptionPane.showMessageDialog(null, "Please Give Answer to All Question");
            setWhiteBackColor();
            reverseHornPanel.setBackground(UIConstant.focusPanelColor);
            reverseHornPanel.requestFocusInWindow();
        } else if (!tailLightNo.isSelected() && !tailLightYes.isSelected() && !tailLightNC.isSelected()) {
//                JOptionPane.showMessageDialog(null, "Please Give Answer to All Question");
            setWhiteBackColor();
            tailLightPanel.setBackground(UIConstant.focusPanelColor);
            tailLightPanel.requestFocusInWindow();
        } else if (!headLightNo.isSelected() && !headLightYes.isSelected() && !headLightNC.isSelected()) {
//                JOptionPane.showMessageDialog(null, "Please Give Answer to All Question");
            setWhiteBackColor();
            headLightPanel.setBackground(UIConstant.focusPanelColor);
            headLightPanel.requestFocusInWindow();
        } else if (!leftSideIndicatorNo.isSelected() && !leftSideIndicatorYes.isSelected() && !leftSideIndicatorNC.isSelected()) {
//                JOptionPane.showMessageDialog(null, "Please Give Answer to All Question");
            setWhiteBackColor();
            leftSideIndicatorPanel.setBackground(UIConstant.focusPanelColor);
            leftSideIndicatorPanel.requestFocusInWindow();
        } else if (!rightSideIndicatorNo.isSelected() && !rightSideIndicatorYes.isSelected() && !rightSideIndicatorNC.isSelected()) {
//                JOptionPane.showMessageDialog(null, "Please Give Answer to All Question");
            setWhiteBackColor();
            rightSideIndicatorPanel.setBackground(UIConstant.focusPanelColor);
            rightSideIndicatorPanel.requestFocusInWindow();
        } else if (!breathlizerNo.isSelected() && !breathlizerYes.isSelected() && !breathlizerNC.isSelected()) {
//                JOptionPane.showMessageDialog(null, "Please Give Answer to All Question");
            setWhiteBackColor();
            breathlizerOkPanel.setBackground(UIConstant.focusPanelColor);
            breathlizerOkPanel.requestFocusInWindow();
        } else if (driverId.isEditable() && driverId.isFocusable() && Utils.isNull(driverId.getText())) {
            driverId.setBackground(UIConstant.focusPanelColor);
            driverId.requestFocusInWindow();
        } else {
            /*if (quickCreate.isEnabled()) {
                quickCreate.requestFocusInWindow();
            } else {
                manualEntryButton.requestFocusInWindow();
            }*/
            quickCreate.requestFocusInWindow();
        }
    }

    private void requestOverrideAction(){
    	Connection conn = null;
    	boolean destroyIt = false;
    	try {
    		conn = DBConnectionPool.getConnectionFromPoolNonWeb();
            updateTPR(conn, com.ipssi.rfid.constant.Type.WorkStationType.REGISTRATION, true);
            int stepId = InsertTPRStep(conn,true,false);
            InsertTPRQuestionDetails(conn, stepId);
           conn.commit();
           fingurePrintCaptureService.stop();
           clearInput(false,conn);
           getFocus();
       } catch (Exception ex) {
           ex.printStackTrace();
           destroyIt = true;
       }finally{
    	   try{
  			 DBConnectionPool.returnConnectionToPoolNonWeb(conn, destroyIt);
  		 }catch(Exception ex){
  			 ex.printStackTrace();
  		 }
       }
    }
    private void manualEntryAction() {
        Connection conn = null;
        boolean destroyIt = false;
    	try{
    		conn = DBConnectionPool.getConnectionFromPoolNonWeb();
        	clearInput(false,conn);
        	if (!vehicleName.isTextEditable()) {
                changeToCreateMode();
                vehicleName.requestFocusInWindow();
            } 
        	manualEntryButton.setEnabled(false);
        }catch(Exception ex){
        	ex.printStackTrace();
        	destroyIt = true;
        }finally{
        	try{
   			 DBConnectionPool.returnConnectionToPoolNonWeb(conn, destroyIt);
   		 }catch(Exception ex){
   			 ex.printStackTrace();
   		 }
        }
    }

    private void changeDriverPanel(boolean isManual) {
        if (!isMorphoExist || isManual) {
            driverId.setEditable(true);
            driverId.setFocusable(true);
            driverId.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
            dlNo.setEditable(true);
            dlNo.setFocusable(true);
            dlNo.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
            captureButton.setVisible(false);
            if (fingurePrintCaptureService != null) {
                fingurePrintCaptureService.stop();
            }
        } else {
            driverId.setEditable(false);
            driverId.setFocusable(false);
            driverId.setBorder(null);
            dlNo.setEditable(false);
            dlNo.setFocusable(false);
            dlNo.setBorder(null);
            captureButton.setVisible(true);
        }
        
    }

    private void clearDriver() {
        if (!isMorphoExist || isFingerSyncRunning ) {
            driverId.setText("");
            driverId.setEditable(true);
            driverId.setFocusable(true);
            driverId.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
            driverId.setBackground(UIConstant.PanelWhite);
            dlNo.setText("");
            dlNo.setEditable(true);
            dlNo.setFocusable(true);
            dlNo.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
            dlNo.setBackground(UIConstant.PanelWhite);
            driverName.setText("");
            driverName.setEditable(false);
            driverName.setFocusable(false);
            driverName.setBackground(UIConstant.PanelWhite);
            driverName.setBorder(null);
            
//        dlNo.setEditable(false);
//        dlNo.setFocusable(false);
//        dlNo.setBorder(null);
            captureButton.setVisible(false);
            
        } else {
            driverId.setText("");
            driverId.setEditable(false);
            driverId.setFocusable(false);
            driverId.setBorder(null);
            driverId.setBackground(UIConstant.PanelWhite);
            dlNo.setText("");
            dlNo.setEditable(false);
            dlNo.setFocusable(false);
            dlNo.setBorder(null);
            dlNo.setBackground(UIConstant.PanelWhite);
            driverName.setText("");
            driverName.setEditable(false);
            driverName.setFocusable(false);
            driverName.setBackground(UIConstant.PanelWhite);
            driverName.setBorder(null);
            captureButton.setVisible(true);
            fingerInstruction.setText("");
        }
        debugTemplate = null;
    }
    
    private void setDriverHour(Connection conn, int driverId){
        Pair<ResultEnum, String>  driverWorkInfo;
        try {
            driverWorkInfo = gpv.getDriverHours(conn, driverId);
               if(driverWorkInfo.first == ResultEnum.GREEN){
            driverHours.setText(driverWorkInfo.second);
            driverHours.setForeground(UIConstant.PanelDarkGreen);
             }else    if(driverWorkInfo.first == ResultEnum.RED){
            driverHours.setText(driverWorkInfo.second);
            driverHours.setForeground(UIConstant.noActionPanelColor);
             }else{
               driverHours.setText(driverWorkInfo.second);
               driverHours.setForeground(UIConstant.PanelYellow);
             }
        } catch (Exception ex) {
            Logger.getLogger(CoalGateInWindow.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
//
//    private String getValue(int tagRead) {
//        String val = "false";
//        if(tagRead == 1){
//            val = "Yes";
//        }
//        return val;
//    }

    private void vehicleNameAction() {
    	Connection conn = null;
    	boolean destroyIt = false;
    	try{
    		conn = DBConnectionPool.getConnectionFromPoolNonWeb();
    		int vehId = Misc.getUndefInt();
    		Pair<Integer, String> vehPair = null;
    		if (!vehicleName.isTextEditable()) {
    			getWhiteBackGrounds();
    			sealOkPanel.requestFocusInWindow();
    			sealOkPanel.setBackground(UIConstant.focusPanelColor);
    		} else if (Utils.isNull(vehicleName.getText())) {
    			JOptionPane.showMessageDialog(null, "Please Enter Vehicle !!!");
    			return;
    			// vehicleName.setTextBackground(UIConstant.noActionPanelColor);
    		} else {
    			String vehName = null;
    			isVehicleExist = false;
    			isNewVehicle = Status.VEHICLE.EXISTING_MANUAL;//
    			vehName = CacheTrack.standardizeName(vehicleName.getText());
    			vehicleName.setText(vehName);
    			vehPair = TPRInformation.getVehicle(conn, null, vehName);
    			if (vehPair != null) {
    				vehId = vehPair.first;
    			}
    			isVehicleExist = !Misc.isUndef(vehId);

    			if (!isVehicleExist) {
    				Object[] options = {"  Re-Enter  ", "  Continue  "};
    				String msg = " Vehicle Not Exist ";
    				int responseVehicleDialog = ConfirmationDialog.getDialogBox(new java.awt.Frame(), true, options, msg);
    				if (responseVehicleDialog == reEnter) {
    					vehicleName.setText("");
    					return;
    				} else if (responseVehicleDialog == contiNue) {
    					try {
    						GateInDao.InsertNewVehicle(conn, vehName, TokenManager.userId);
    						setTPRecord(vehName);
    						getWhiteBackGrounds();
    						transporter.setFocusable(true);
    						transporter.setSelectedIndex(0);
    						transporter.requestFocusInWindow();
    						transporter.setEnabled(true);
    						/*if (!isFingerCaptureRunning && isMorphoExist) {
    							fingurePrintCaptureService.start();
    							isFingerCaptureRunning = true;
    						}*/
    						startFingerCaptureService();
    						isNewVehicle = Status.VEHICLE.NEW_MANUAL;
    						isVehicleExist = false;
    						isTagRead = false;
    					} catch (IOException ex) {
    						ex.printStackTrace();
    					}

    				}
    			} else {
    				getWhiteBackGrounds();
    				setTPRecord(vehName);
    				isTagRead = false;
    				isNewVehicle = Status.VEHICLE.EXISTING_MANUAL;
    				if(tprRecord != null){
    					/*if (!isFingerCaptureRunning && isMorphoExist ) {
    						fingurePrintCaptureService.start();
    						isFingerCaptureRunning = true;
    					}*/
    					startFingerCaptureService();
    					if (transporter.isFocusable()) {
    						transporter.requestFocusInWindow();
    					} else {
    						seatBeltPanel.setBackground(UIConstant.focusPanelColor);
    						seatBeltPanel.requestFocusInWindow();
    					}
    				}
    			}
    		}
    	}catch(Exception ex){
    		ex.printStackTrace();
    		destroyIt = true;
    	}finally{
    		try{
   			 DBConnectionPool.returnConnectionToPoolNonWeb(conn, destroyIt);
   		 }catch(Exception ex){
   			 ex.printStackTrace();
   		 }
    	}
    }
    private void captureFingurePrint(){
    	Connection conn = null;
    	boolean destroyIt = false;
    	if ((vehicleName.getText().trim()).equalsIgnoreCase("NO VEHICLE DETECTED")) {
    		JOptionPane.showMessageDialog(null, "Please Enter Vehicle Name");
    		return;
    	} else if (Utils.isNull(vehicleName.getText())) {
    		JOptionPane.showMessageDialog(null, "Please Enter Vehicle Name");
    		return;
    	} else if (tprRecord == null) {
    		JOptionPane.showMessageDialog(null, "Please Enter valid Vehicle Name");
    		return;
    	}else{
    		try{
    			conn = DBConnectionPool.getConnectionFromPoolNonWeb();
    			Triple<Biometric, Boolean, Boolean> captureResult = null;
    			String msg = "User Not Exist, Do you want to  capture again ?";
    			Object[] options = {"  Recapture  ", "  Continue  "};
    			boolean continueCapture = true;
    			while(continueCapture){
    				setFingerPrintPanel(true);
    				//SwingUtilities.updateComponentTreeUI(this);
    				captureResult = TokenManager.useSDK() ?  DriverDetail.captureFingurePrintSDK(this) : DriverDetail.captureFingurePrint(this) ;
    				if(captureResult == null){
    					JOptionPane.showMessageDialog(null,"Device is not Connected, Please Restart Application and Unplugged Device !!!");
    					setFingerPrintPanel(false);
    					disableFingurePrintCapture();
    					break;
    				}else if(captureResult.first == null){
    					int confirmationVal = ConfirmationDialog.getDialogBox(new javax.swing.JFrame(), true, options, msg);
    					if (confirmationVal == 0) {
    						WaveFormPlayer.playSoundOut(Status.TPRQuestion.tryAgainFinger);
    						continue;
    					} else if (confirmationVal == 1) {
    						WaveFormPlayer.playSoundOut(Status.TPRQuestion.fingerNotMatch);
    						setFingerPrintPanel(false);
    						disableFingurePrintCapture();
    						break;
    					}
    				}else{
    					populateDriver(conn, captureResult.first, captureResult.second, captureResult.third);
    					setFingerPrintPanel(false);
    					break;
    				}
    			}

    		}catch(Exception ex){
    			ex.printStackTrace();
    			destroyIt = true;
    		}finally{
    			try{
    				DBConnectionPool.returnConnectionToPoolNonWeb(conn, destroyIt);
    			}catch(Exception ex){
    				ex.printStackTrace();
    			}
    		}
    	}
    }
    private void populateDriver(Connection conn, Biometric driverBean, boolean fingerVerified, boolean fingerCaptured) {
        // TODO Auto-generated method stub
    	if(driverBean == null){
    		photo.setIcon(null);
    		return;
    	}
    	driverInformation = driverBean;
        dlNo.setText(driverBean.getDl_no());
        driverId.setText(Misc.getPrintableInt(driverBean.getDriver_id()));
        driverName.setText(driverBean.getDriver_name());
        if (driverBean.getPhoto() != null) {
            BufferedImage image = DriverDetail.byteArrayToImage(driverBean.getPhoto());
            photo.setIcon(new ImageIcon(image));
        }
        if (driverBean.getDriver_type() == 1) {
            helperNo.setSelected(true);
        } else {
            helperYes.setSelected(true);
            JOptionPane.showMessageDialog(null, "Please Inform to Driver to take his Seat and then Re-Authenticate !!!");
        }
        if (driverBean.getIsFingerCaptured() == 1) {
        	isFingerCaptured = true;
        } else {
            isFingerCaptured = false;
        }
        this.isFingerCaptured = fingerCaptured;
        this.isFingerVerified = fingerVerified;
        isDriverExist = driverBean.getStatus() == 1;
        isFingerExist = driverBean.getIsFingerInDB() == 1;
        isFingerVerified = fingerVerified;
        Triple<Integer, Long, Long> driverBlockStatus = TPRUtils.getDriverBlockStatus(conn, driverBean.getDriver_id());
    	if(driverBlockStatus != null){
    		long curr = System.currentTimeMillis();
    		isDriverBlacklisted = driverBlockStatus.first == Status.ACTIVE && !Misc.isUndef(driverBlockStatus.second) && (curr >= driverBlockStatus.second && (curr <= driverBlockStatus.third || Misc.isUndef(driverBlockStatus.third)));
    	}
        setDriverHour(conn, driverBean.getDriver_id());
    }
    private void disableFingurePrintCapture(){
    	driverId.setEditable(true);
        driverId.setFocusable(true);
        driverId.requestFocusInWindow();
        driverId.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        dlNo.setEditable(true);
        dlNo.setFocusable(true);
        dlNo.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        photo.setIcon(null);
        isFingerCaptureRunning = false;
        setFingerPrintPanel(false);
        changeFingerButtonText(false);
        captureButton.setVisible(true);
    }
    private void setBlockingStatus(){
    	if(tprBlockManager == null){
    		enableDenyEntry(false);
    		return;
    	}
    	try{
    		int blockStatus = tprBlockManager.getBlockStatus();
    		if (blockStatus == UIConstant.BLOCKED) {
                vehicleBlackListed = true;
                blocking_reason.setText(tprBlockManager.getBlockingReason());
                overrides.setText("BLOCKED");
                quickCreate.requestFocusInWindow();
            }else{
            	vehicleBlackListed = false;
            	overrides.setText("NOT_BLOCKED");
            	blocking_reason.setText("");
            }
    		enableDenyEntry(vehicleBlackListed);
    	}catch(Exception ex){
    		ex.printStackTrace();
    	}
    }
    private void setQuestionsBlocking(int questionId, int answerId){
    	if(Misc.isUndef(questionId))
    		return;
    	if(tprBlockManager != null){
    		TPSQuestionDetail tpsQuestionBean = new TPSQuestionDetail();
    		tpsQuestionBean.setQuestionId(questionId);
    		tpsQuestionBean.setAnswerId(answerId);
    		tprBlockManager.addQuestions(tpsQuestionBean);
    		setBlockingStatus();
    	}
    }
    private void setFingerPrintPanel(boolean status){
		lblScore.setText("");
		progressBar.setValue(0);
		lblSteps.setText("");
		lblInstruction.setText("");
		lblCurrentImageInfo.setText("");
		fingerPrintPanel.setVisible(status);
		manualCaptureFinger = status;
		isFingerPrintCaptureRunning  = status;
		if(debugTemplate != null)
			photo.setIcon(null);
	}
   
	@Override
	public void setInstruction(String intruction) {
		// TODO Auto-generated method stub
		lblInstruction.setText(intruction);
	}

	@Override
	public void setStepsImage(EnumMoveFinger move) {
		// TODO Auto-generated method stub
		if (move == null) {
			lblSteps.setIcon(null);
		} else {
			switch (move) {
			case MOVE_UP:
				lblSteps.setIcon(iconArrowUp);
				break;
			case MOVE_LEFT:
				lblSteps.setIcon(iconArrowLeft);
				break;
			case MOVE_DOWN:
				lblSteps.setIcon(iconArrowDown);
				break;
			case MOVE_RIGHT:
				lblSteps.setIcon(iconArrowRight);
				break;
			}
		}
	}

	@Override
	public void fingerOk() {
		// TODO Auto-generated method stub
		Icon iconOK = new ImageIcon(imgFvpOk.getImage().getScaledInstance(photo.getWidth(), photo.getHeight(), Image.SCALE_AREA_AVERAGING));
		photo.setIcon(iconOK);
	}

	@Override
	public void playVideo(boolean isFingerFvpDetected) {
		// TODO Auto-generated method stub
		if (isFingerFvpDetected)
			photo.setIcon(gifMaillage);
		else
			photo.setIcon(gifCapture);
	}

	@Override
	public void setLiveImage(MorphoImage morphoImage) {
		// TODO Auto-generated method stub
		if(!isFingerPrintCaptureRunning || !manualCaptureFinger )
			return;
		BufferedImage bufferedImage = MorphoEventHandler.toBufferedImage(morphoImage.getImage(), morphoImage.getImageHeader().getNbCol(), morphoImage.getImageHeader().getNbRow());
		ImageIcon image = new ImageIcon(MorphoEventHandler.resizeImage(bufferedImage, photo.getWidth(), photo.getHeight()));
		photo.setIcon(image);
	}

	@Override
	public void setCurrentImageInfo(int nbCol, int nbRow, int resX, int resY, int bitPerPixel) {
		// TODO Auto-generated method stub
		lblCurrentImageInfo.setText("Size: " + nbCol + "*" + nbRow + " pix, Res: " + resX + "*" + resY + " dpi, " + bitPerPixel + " bits/pixels");
	}

	@Override
	public void setScore(short quality) {
		// TODO Auto-generated method stub
		lblScore.setText(String.valueOf(quality));
		progressBar.setValue(quality);
		if (quality < 20)
			progressBar.setForeground(Color.BLUE);
		else
			progressBar.setForeground(Color.GREEN);
	}
	@Override
	public void setBorderColorGreen(short fingerNumber, short step) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setLiveStepImage(MorphoImage morphoImage, short fingerNumber, short step) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setCodeQuality(short codeQuality) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setDetectedQuality(short detectedQuality) {
		// TODO Auto-generated method stub
		
	}
	
	 public void scheduleClearScreenTimer(){
		 try{
			 timer  = new Timer();
			 timer.schedule( new TimerTask() {
				 public void run() {
					 System.out.println("Auto Refress Screen");
					 clearAction();
					// timer.cancel();
				 }
			 }, TokenManager.screenClearInterval * 1000);
		 }catch (Exception e) {
			e.printStackTrace();
		}
	 }
}