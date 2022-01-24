package com.ipssi.rfid.ui;

import java.awt.AWTKeyStroke;
import java.awt.Color;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ButtonGroup;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerDateModel;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.ipssi.common.ds.rule.GpsPlusViolations;
import com.ipssi.common.ds.rule.ResultEnum;
import com.ipssi.gen.utils.DBConnectionPool;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.Pair;
import com.ipssi.gen.utils.Triple;
import com.ipssi.rfid.beans.ComboItem;
import com.ipssi.rfid.beans.RFIDHolder;
import com.ipssi.rfid.beans.RegistrationStatus;
import com.ipssi.rfid.beans.TPRQCDetail;
import com.ipssi.rfid.beans.TPRecord;
import com.ipssi.rfid.beans.TPSQuestionDetail;
import com.ipssi.rfid.beans.TPStep;
import com.ipssi.rfid.beans.Token;
import com.ipssi.rfid.constant.Status;
import com.ipssi.rfid.constant.UIConstant;
import com.ipssi.rfid.database.DropDownValues;
import com.ipssi.rfid.database.GateInDao;
import com.ipssi.rfid.db.RFIDMasterDao;
import com.ipssi.rfid.integration.Barrier;
import com.ipssi.rfid.integration.Clock;
import com.ipssi.rfid.integration.WeighBridge;
import com.ipssi.rfid.integration.WeighBridgeListener;
import com.ipssi.rfid.processor.TPRBlockManager;
import com.ipssi.rfid.processor.TPRBlockStatusHelper;
import com.ipssi.rfid.processor.TPRInformation;
import com.ipssi.rfid.processor.TPRUtils;
import com.ipssi.rfid.processor.TokenManager;
import com.ipssi.rfid.processor.Utils;
import com.ipssi.rfid.readers.RFIDDataHandler;
import com.ipssi.rfid.readers.TAGListener;
import com.ipssi.rfid.ui.AutoCompleteCombo.ComboKeyEvent;

public class CoalWeighBridgeInWindow extends javax.swing.JFrame {
	
	private static final long serialVersionUID = 1L;
	//flag
    int contiNue = 1;
    int reEnter = 0;

    //date format
    DateFormat inFormat = new SimpleDateFormat("MMM dd, yyyy hh:mm:ss aa");
    DateFormat outFormat = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");
    
    private int readerId = 0;
    private RFIDDataHandler rfidHandler = null;
    private WeighBridge weighBridge = null;
    
    private ButtonGroup markQc = new ButtonGroup();
    private ButtonGroup paperChallan = new ButtonGroup();
    boolean isVehicleExist = false;
    private Date entryTime = null;
    private Date exitTime = null;

    Token token = null;
    private TPRecord tprRecord = null;
    private TPStep tpStep = null;
    private TPRQCDetail tprQcDetail = null;
    private GpsPlusViolations gpv = null;
    
    private boolean isTagRead = false;
    private boolean isTpRecordValid = false;
    private boolean vehicleBlackListed = false;
    private boolean isRequestOverride = false;
    private double captureWeight = Misc.getUndefDouble();
    private int markForQCVal = Misc.getUndefInt();
    private long challanDateTime = Misc.getUndefInt();
    
    JSpinner.DateEditor de = null;
    private ArrayList<Pair> doList = null;
    String markForQcReason = null;
	private TPRBlockManager tprBlockManager = null;
    private ArrayList<Pair<Long, Integer>> readings = null;
    private DisconnectionDialog disconnectionDialog = new DisconnectionDialog("Weigh Bridge Disconnected please check connection.....");
	private boolean isManual = false;
    
	/**
     * Creates new form CoalWeighBridgeInWindow
     */
    public CoalWeighBridgeInWindow() throws IOException {
        initComponents();
//        gpsLocation.setText("12345678901234567890123456789012345678901234567890");
//        gpsTime.setText("asdfghjkl;wertyuiod");
        labelWeighment.setText(TokenManager.weight_val);
        this.setExtendedState(this.getExtendedState()
                | this.MAXIMIZED_BOTH);
        this.setTitle(UIConstant.formTitle);
        myComponent();
        UIManager.put("Button.defaultButtonFollowsFocus", Boolean.TRUE);
        Clock.startClock("WbIn");
        setMinesList();
        getFocus();
        calculateGrossShort();
        if(TokenManager.isManualEntry.containsKey(TokenManager.currWorkStationId)){
            int val  = TokenManager.isManualEntry.get(TokenManager.currWorkStationId);
            if(val == 1){
                 isManual   = true;
            }else {
                 isManual = false;
                 manualEntryButton.setEnabled(false);
            }
        }else{
                isManual = false;
                manualEntryButton.setEnabled(false);
        }
        start();
//        labelWeighment.setText("22500");
    }

    public void start() throws IOException {

        if (rfidHandler == null) {
            rfidHandler = new RFIDDataHandler(1000, readerId, TokenManager.currWorkStationType, TokenManager.currWorkStationId, TokenManager.userId);
            rfidHandler.setTagListener(new TAGListener() {
            	@Override
                public void manageTag(Connection conn ,Token _token, TPRecord tpr, TPStep tps, TPRBlockManager _tprBlockManager) {
            		try {
                        token = _token;
                        tprBlockManager  = _tprBlockManager;
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
					clearInput(conn, clearToken);
				}

				@Override
				public int mergeData(long sessionId, String epc, RFIDHolder rfidHolder) {
					// TODO Auto-generated method stub
					return 0;
				}
            });
        }
        rfidHandler.start();
        if (weighBridge == null) {
            weighBridge = new WeighBridge();
            weighBridge.setListener(new WeighBridgeListener() {
                @Override
                public void changeValue(String str) {
                	System.out.println("[WB Reading]:" + str);
                    int val = Misc.getParamAsInt(str);
                    if (!Misc.isUndef(val)) {
                        int currVal = Misc.getParamAsInt(labelWeighment.getText());
                        if (Misc.isUndef(currVal) || (currVal >= val ? currVal - val : val - currVal) >= 10 || val == 0) {
                            labelWeighment.setText(val + "");
                            calculateGrossShort();
                        }
                        if(TokenManager.isDebugReadings && tprRecord != null && readings != null && (readings.size() <= 0 || readings.get(readings.size()-1) == null ||(readings.get(readings.size()-1).second != val) )){
                        	readings.add(new Pair<Long, Integer>(System.currentTimeMillis(),val));
                        }
                    }
                }

				@Override
				public void showDisconnection() {
					// TODO Auto-generated method stub
					java.awt.EventQueue.invokeLater(new Runnable() {
			            public void run() {
			            	if(disconnectionDialog != null)
			            		disconnectionDialog.setVisible(true);
			            }
			        });
					
				}

				@Override
				public void removeDisconnection() {
					// TODO Auto-generated method stub
					java.awt.EventQueue.invokeLater(new Runnable() {
			            public void run() {
			            	if(disconnectionDialog != null)
			            		disconnectionDialog.setVisible(false);
			            }
			        });
				}
            });
        }
        weighBridge.startWeighBridge();
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
        System.out.println("######### Weigh bridge In setTPRecord  ########");
        try {
            tprRecord = tpr;
            if (tprRecord != null) {
                System.out.println("TPR Record Create");
                if (true) {//blockStatus == Status.VALIDATION.NO_ISSUE) {// TPR Valid
                	toggleVehicle(false);
                    isTpRecordValid = true;
                    isTagRead = token != null ? token.isReadFromTag() : false;
                    if (token == null && tprRecord.getEarliestUnloadWbInEntry() != null) {
                        System.out.println("Entry Time 1st");
                        entryTime = tprRecord.getEarliestUnloadWbInEntry();

                    } else if (token != null && tprRecord.getEarliestUnloadWbInEntry() == null) {
                        System.out.println("Entry Time 2nd :" + token.getLastSeen());
                        if (token.getLastSeen() != Misc.getUndefInt()) {
                            entryTime = new Date(token.getLastSeen());
                        } else {
                            entryTime = new Date();
                        }
                    } else if (token != null && tprRecord.getEarliestUnloadWbInEntry() != null) {
                        if (token.getLastSeen() > Utils.getDateTimeLong(tprRecord.getEarliestUnloadWbInEntry())) {
                            System.out.println("Entry Time 3rd :" + token.getLastSeen());
                            if (token.getLastSeen() != Misc.getUndefInt()) {
                                entryTime = new Date(token.getLastSeen());
                            } else {
                                entryTime = new Date();
                            }
                        } else {
                            entryTime = new Date();
                        }
                    } else {
                        entryTime = new Date();
                    }
                    System.out.println("Entry Time :" + entryTime);


                    vehicleName.setText(tprRecord.getVehicleId(), tprRecord.getVehicleName());
                    challanNo.setText(tprRecord.getChallanNo());
                    challanDateTime = tprRecord.getChallanDate() != null ? tprRecord.getChallanDate().getTime() : Misc.getUndefInt();
                    if (tprRecord.getLrDate() != null) {
                        lrDate.setValue(tprRecord.getLrDate());
                    }
                    if (tprRecord.getEarliestUnloadGateInEntry() != null) {
                        gateInTime.setText(UIConstant.displayFormat.format(tprRecord.getEarliestUnloadGateInEntry()));
                    }

                    lrNo.setText(tprRecord.getLrNo());
                    tareWt.setText(Misc.getPrintableDouble(tprRecord.getLoadTare()));
                    grossWt.setText(Misc.getPrintableDouble(tprRecord.getLoadGross()));
                    /*if (tprRecord.getTprId() != Misc.getUndefInt()) {
                        gpv = GpsPlusViolations.getGpsPlusViolatins(conn, tprRecord.getVehicleId(), tprRecord.getTprId(), System.currentTimeMillis(), tprRecord.getMinesId(), tprRecord.getChallanDate() == null ? System.currentTimeMillis() : tprRecord.getChallanDate().getTime());
                    } else {
                        gpv = GpsPlusViolations.getGpsPlusViolatins(conn, tprRecord.getVehicleId(), tprRecord.getTprId(), System.currentTimeMillis(), Misc.getUndefInt(), System.currentTimeMillis());
                    }*/
                    /*gpv = GpsPlusViolations.getGpsPlusViolatins(conn, tprRecord.getVehicleId(), tprRecord.getTprId(), System.currentTimeMillis(), DropDownValues.getComboSelectedVal(mines), challanDate.getValue() == null ? System.currentTimeMillis() : ((Date)challanDate.getValue()).getTime());
                    Pair<ResultEnum, String> gpsIsTracking = gpv.getGpsIsTracking(conn);
                    if (gpsIsTracking.first == ResultEnum.GREEN) {
                    	gpsStatus.setForeground(UIConstant.PanelDarkGreen);
                    	gpsStatus.setText(gpsIsTracking.second);
                        
                    } else if (gpsIsTracking.first == ResultEnum.RED) {
                    	gpsStatus.setForeground(UIConstant.noActionPanelColor);
                    	gpsStatus.setText(gpsIsTracking.second);
                        
                    } else {
                    	gpsStatus.setForeground(UIConstant.PanelYellow);
                    	gpsStatus.setText(gpsIsTracking.second);
                    }
                    Pair<ResultEnum, String> gpsViolationStatus = gpv.getGpsQCViolationsSummary(conn);
                    if (gpsViolationStatus.first == ResultEnum.GREEN) {
                    	gpsViolation.setForeground(UIConstant.PanelDarkGreen);
                    	gpsViolation.setText(gpsViolationStatus.second);
                        
                    } else if (gpsViolationStatus.first == ResultEnum.RED) {
                    	gpsViolation.setForeground(UIConstant.noActionPanelColor);
                    	gpsViolation.setText(gpsViolationStatus.second);
                        
                    } else {
                    	gpsViolation.setForeground(UIConstant.PanelYellow);
                    	gpsViolation.setText(gpsViolationStatus.second);
                    }*/
                    DropDownValues.setComboItem(mines, tprRecord.getMinesId());
                    setDoRrList(conn,tprRecord.getDoId(), tprRecord.getMinesId() == Misc.getUndefInt() ? 0 : tprRecord.getMinesId());
                    setGradeList(conn,tprRecord.getMaterialGradeId(), tprRecord.getDoId() == Misc.getUndefInt() ? 0 : tprRecord.getDoId());
                    setTransporterList(conn,tprRecord.getTransporterId(), tprRecord.getDoId() == Misc.getUndefInt() ? 0 : tprRecord.getDoId());
                    
                    int selectedtransporter = DropDownValues.getComboSelectedVal(transporter);
            		int selectedMines = DropDownValues.getComboSelectedVal(mines);
            		int selectedGrade = DropDownValues.getComboSelectedVal(grade);
            		int selectedDO = DropDownValues.getComboSelectedVal(doNo);
            		Pair<Integer, String> bedAssign = TPRUtils.getBedAllignment(conn, selectedtransporter, selectedMines, selectedGrade, selectedDO);
            		if(bedAssign != null)
            			bedAssigned.setText(bedAssign.second);
            		
                    /*Pair<ResultEnum, String> getMarkQC = gpv.getMarkForQC(conn, Misc.getParamAsDouble(labelWeighment.getText()), tprRecord.getLoadGross());
                    if (getMarkQC.first == ResultEnum.GREEN) {
                        markForQC.setForeground(UIConstant.PanelDarkGreen);
                        markForQC.setText(getMarkQC.second);
                        markForQCVal = UIConstant.NO;
                    } else if (getMarkQC.first == ResultEnum.RED) {
                        markForQC.setForeground(UIConstant.noActionPanelColor);
                        markForQC.setText(getMarkQC.second);
                        markForQCVal = UIConstant.YES;
                    } else {
                        markForQC.setForeground(UIConstant.PanelYellow);
                        markForQC.setText(getMarkQC.second);
                        markForQCVal = UIConstant.NC;
                    }
                    Pair<ResultEnum, ArrayList<String>> getGpsQCViolationDetails = gpv.getGpsQCViolationsDetailed(conn, tprRecord.getLoadGross());
                    if (getGpsQCViolationDetails.first == ResultEnum.GREEN) {
                        listOfViolation.setForeground(UIConstant.PanelDarkGreen);
                        for (String s : getGpsQCViolationDetails.second) {
                            listOfViolation.setText("<html>" + s + "<br></html>");
                        }
                    } else if (getGpsQCViolationDetails.first == ResultEnum.RED) {
                        listOfViolation.setForeground(UIConstant.noActionPanelColor);
                        for (String s : getGpsQCViolationDetails.second) {
                            listOfViolation.setText("<html>" + s + "<br></html>");
                        }
                    } else {
                        listOfViolation.setForeground(UIConstant.PanelYellow);
                        for (String s : getGpsQCViolationDetails.second) {
                            listOfViolation.setText("<html>" + s + "<br></html>");
                        }
                    }*/
            		
                    if (tprRecord.getChallanDate() != null) {
                        challanDate.setValue(tprRecord.getChallanDate());
                        paperChallanPanel.requestFocusInWindow();
                        paperChallanPanel.setBackground(UIConstant.focusPanelColor);
                    } else {
                        paperChallanNo.setSelected(true);
                        paperChallanNoActionPerformed();
                        paperChallanPanel.setBackground(UIConstant.PanelWhite);
                        mines.requestFocusInWindow();
                    }
                    Pair<Integer, String> supplierDetails = TPRUtils.getSupplierFromDo(conn, tprRecord.getDoId());
                    if(!Misc.isUndef(supplierDetails.first)){
                    	supplier.setText(supplierDetails.second);
                    }else{
                    	supplier.setText("");
                    }
                    
                    if((tprRecord.getMinesId() != Misc.getUndefInt() || tprRecord.getMinesId() != 0) && (tprRecord.getMaterialGradeId() != Misc.getUndefInt() || tprRecord.getMaterialGradeId() != 0) ){
                    	if(tprRecord.getDoId() == 0 || tprRecord.getDoId() == Misc.getUndefInt()){
                    	paperChallanNo.setSelected(true);
                    	mines.requestFocusInWindow();
                    	}
                    }
                    

                } 
                Pair<Long, String> pairVal = GpsPlusViolations.getLatestLocation(conn, tprRecord.getVehicleId());
                if(pairVal != null){
                	String location = pairVal.second == null ? "" : pairVal.second;
                	gpsTime.setText(GateInDao.getLongToDatetime(pairVal.first));
                	gpsLocation.setText(GateInDao.getString(location, 40));
                }

                setMarkForQcAndGPSVoilation(conn, false);
                calculateGrossShort();
                setBlockingStatus();
                saveAndOpen.setEnabled(true);
                readings = new ArrayList<Pair<Long,Integer>>();
            } else {
                JOptionPane.showMessageDialog(null, "Invalid Vehicle Go to Registration");
                isTpRecordValid = false;
                isVehicleExist = false;
                if (tprRecord != null && tprRecord.getChallanDate() != null) {
                    paperChallanPanel.requestFocusInWindow();
                    paperChallanPanel.setBackground(UIConstant.focusPanelColor);
                }
            }
        } catch(Exception ex){
    		ex.printStackTrace();
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
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel22 = new javax.swing.JLabel();
        username = new javax.swing.JLabel();
        jButtonSignOut = new java.awt.Button();
        jPanel3 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        paperChallanPanel = new javax.swing.JPanel();
        paperChallanNo = new javax.swing.JCheckBox();
        paperChallanYes = new javax.swing.JCheckBox();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        vehicleName = new AutoCompleteCombo();
        lrNo = new javax.swing.JTextField();
        challanNo = new javax.swing.JTextField();
        grossWt = new javax.swing.JTextField();
        tareWt = new javax.swing.JTextField();
        roadPermitNo = new javax.swing.JTextField();
        mines = new javax.swing.JComboBox();
        grade = new javax.swing.JComboBox();
        doNo = new javax.swing.JComboBox();
        overrides = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        mineralPermitNo = new javax.swing.JTextField();
        jLabel23 = new javax.swing.JLabel();
        jLabel24 = new javax.swing.JLabel();
        supplier = new javax.swing.JTextField();
        jLabel25 = new javax.swing.JLabel();
        gateInTime = new javax.swing.JTextField();
        transporter = new javax.swing.JComboBox();
        Date dt = new Date() ;
        dt.setTime(dt.getTime() - 1000 * 60 * 30 );
        SpinnerDateModel sm= new SpinnerDateModel(dt,null,null,Calendar.DATE);
        challanDate = new javax.swing.JSpinner(sm);
        Date date = new Date() ;
        date.setTime(date.getTime() - 1000 * 60 * 30 );
        SpinnerDateModel sdm= new SpinnerDateModel(date,null,null,Calendar.DATE);
        lrDate = new javax.swing.JSpinner(sdm);
        jLabel27 = new javax.swing.JLabel();
        jLabel28 = new javax.swing.JLabel();
        gpsLocation = new javax.swing.JLabel();
        gpsTime = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();
        jLabelGrossShort = new javax.swing.JLabel();
        jLabelGpsStatus = new javax.swing.JLabel();
        jLabelGpsViolation = new javax.swing.JLabel();
        jLabelQCMark = new javax.swing.JLabel();
        jLabelListOfViolation = new javax.swing.JLabel();
        grossShort = new javax.swing.JTextField();
        gpsStatus = new javax.swing.JLabel();
        gpsViolation = new javax.swing.JLabel();
        jLabelBedAssigned = new javax.swing.JLabel();
        bedAssigned = new javax.swing.JLabel();
        listOfViolation = new javax.swing.JTextArea();
        markForQC = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        saveAndOpen = new javax.swing.JButton();
        jPanel6 = new javax.swing.JPanel();
        labelWeighment = new javax.swing.JLabel();
        digitalClock = new javax.swing.JLabel();
        manualEntryButton = new javax.swing.JButton();
        blocking_reason = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/ipssi/rfid/ui/intelliplanner.png"))); // NOI18N

        jLabel3.setFont(UIConstant.subHeadingFont);
        jLabel3.setText("Weigh Bridge - In");

        jLabel22.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/ipssi/rfid/ui/cover_01_right_top.gif"))); // NOI18N

        username.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);

        jButtonSignOut.setBackground(new java.awt.Color(255, 255, 255));
        jButtonSignOut.setFocusable(false);
        jButtonSignOut.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jButtonSignOut.setForeground(new java.awt.Color(0, 102, 153));
        jButtonSignOut.setLabel("Sign Out");
        jButtonSignOut.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(363, 363, 363)
                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 288, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(username, javax.swing.GroupLayout.PREFERRED_SIZE, 112, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonSignOut, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel22, javax.swing.GroupLayout.PREFERRED_SIZE, 162, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jLabel22, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(5, 5, 5)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(username, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButtonSignOut, javax.swing.GroupLayout.DEFAULT_SIZE, 43, Short.MAX_VALUE)))
        );

        jPanel3.setBackground(java.awt.SystemColor.controlLtHighlight);

        jLabel2.setFont(UIConstant.labelFont);
        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel2.setText("Vehicle:");

        jLabel4.setFont(UIConstant.labelFont);
        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel4.setText("Overrides?");

        jLabel5.setFont(UIConstant.labelFont);
        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel5.setText("Mines:");

        jLabel6.setFont(UIConstant.labelFont);
        jLabel6.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel6.setText("LR #:");

        jLabel7.setFont(UIConstant.labelFont);
        jLabel7.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel7.setText("Challan Date:");

        jPanel4.setBackground(new java.awt.Color(255, 255, 255));

        jLabel8.setFont(UIConstant.labelFont);
        jLabel8.setText("<html>Paper<br>Challan<br>Matches<br>System?</html>");

        paperChallanPanel.setBackground(new java.awt.Color(255, 255, 255));
        paperChallanPanel.setName("numberVisiblePanel"); // NOI18N
        paperChallanPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                paperChallanPanelMouseClicked(evt);
            }
        });
        paperChallanPanel.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                paperChallanPanelKeyPressed(evt);
            }
        });

        paperChallanNo.setBackground(new java.awt.Color(255, 255, 255));
        paperChallanNo.setText("No");
        paperChallanNo.setFocusable(false);
        paperChallanNo.setOpaque(false);
        paperChallanNo.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                paperChallanNoMouseClicked(evt);
            }
        });
        paperChallanNo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                paperChallanNoActionPerformed(evt);
            }
        });

        paperChallanYes.setBackground(new java.awt.Color(255, 255, 255));
        paperChallanYes.setText("Yes");
        paperChallanYes.setFocusable(false);
        paperChallanYes.setOpaque(false);
        paperChallanYes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                paperChallanYesActionPerformed(evt);
            }
        });
        paperChallanYes.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                paperChallanYesKeyPressed(evt);
            }
        });

        javax.swing.GroupLayout paperChallanPanelLayout = new javax.swing.GroupLayout(paperChallanPanel);
        paperChallanPanel.setLayout(paperChallanPanelLayout);
        paperChallanPanelLayout.setHorizontalGroup(
            paperChallanPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, paperChallanPanelLayout.createSequentialGroup()
                .addComponent(paperChallanYes, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(paperChallanNo, javax.swing.GroupLayout.DEFAULT_SIZE, 45, Short.MAX_VALUE))
        );
        paperChallanPanelLayout.setVerticalGroup(
            paperChallanPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(paperChallanPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(paperChallanNo, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(paperChallanYes, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(paperChallanPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 18, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(paperChallanPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabel9.setFont(UIConstant.labelFont);
        jLabel9.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel9.setText("Challan #:");

        jLabel10.setFont(UIConstant.labelFont);
        jLabel10.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel10.setText("DO / RR #:");

        jLabel11.setFont(UIConstant.labelFont);
        jLabel11.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel11.setText("Grade:");

        jLabel12.setFont(UIConstant.labelFont);
        jLabel12.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel12.setText("Transporter:");

        jLabel13.setFont(UIConstant.labelFont);
        jLabel13.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel13.setText("Party Gross Wt:");

        jLabel14.setFont(UIConstant.labelFont);
        jLabel14.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel14.setText("Party Tare Wt:");

        jLabel15.setFont(UIConstant.labelFont);
        jLabel15.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel15.setText("Road Permit #:");

        /*vehicleName.setFont(UIConstant.vehicleLabel);
        vehicleName.setForeground(UIConstant.textFontColor);
        vehicleName.setEditable(false);
        vehicleName.setText("NO VEHICLE DETECTED");
        vehicleName.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        vehicleName.setFocusable(false);
        vehicleName.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                vehicleNameMouseClicked(evt);
            }
        });
        vehicleName.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                vehicleNameFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                vehicleNameFocusLost(evt);
            }
        });
        vehicleName.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                vehicleNameKeyPressed(evt);
            }
        });
*/
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
        lrNo.setFont(UIConstant.textFont);
        lrNo.setForeground(UIConstant.textFontColor);
        lrNo.setEditable(false);
        lrNo.setBorder(null);
        lrNo.setFocusable(false);
        lrNo.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                lrNoFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                lrNoFocusLost(evt);
            }
        });
        lrNo.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                lrNoKeyPressed(evt);
            }
        });

        challanNo.setFont(UIConstant.textFont);
        challanNo.setForeground(UIConstant.textFontColor);
        challanNo.setEditable(false);
        challanNo.setBorder(null);
        challanNo.setFocusable(false);
        challanNo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                challanNoActionPerformed(evt);
            }
        });
        challanNo.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                challanNoFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                challanNoFocusLost(evt);
            }
        });
        challanNo.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                challanNoKeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                challanNoKeyReleased(evt);
            }
        });

        grossWt.setFont(UIConstant.textFont);
        grossWt.setForeground(UIConstant.textFontColor);
        grossWt.setEditable(false);
        grossWt.setBorder(null);
        grossWt.setFocusable(false);
        grossWt.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                grossWtFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
            	refreshGPSAndVoilations(false);
            	grossWtFocusLost(evt);
            }
        });
        grossWt.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                grossWtKeyPressed(evt);
            }
        });

        tareWt.setFont(UIConstant.textFont);
        tareWt.setForeground(UIConstant.textFontColor);
        tareWt.setEditable(false);
        tareWt.setBorder(null);
        tareWt.setFocusable(false);
        tareWt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tareWtActionPerformed(evt);
            }
        });
        tareWt.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                tareWtFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
            	//refreshGPSAndVoilations(false);
            }
        });
        tareWt.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                tareWtKeyPressed(evt);
            }
        });

        roadPermitNo.setFont(UIConstant.textFont);
        roadPermitNo.setForeground(UIConstant.textFontColor);
        roadPermitNo.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        roadPermitNo.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                roadPermitNoFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                roadPermitNoFocusLost(evt);
            }
        });
        roadPermitNo.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                roadPermitNoKeyPressed(evt);
            }
        });

        mines.setFont(UIConstant.textFont);
        mines.setMaximumRowCount(10);
        mines.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Select" }));
        mines.setEnabled(false);
        mines.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
            	minesRefresh();
            	minesActionPerformed(evt);
            }
        });
        mines.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                //minesFocusLost(evt);
            }
        });
        mines.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
            	if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            		minesKeyPressed(evt);
            		minesRefresh();
            	}
            }
        });

        grade.setFont(UIConstant.textFont);
        grade.addItem(new ComboItem(0, "Select"));
        grade.setMaximumRowCount(10);
        grade.setEnabled(false);
        grade.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                gradeKeyPressed(evt);
            }
        });
        grade.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                //refreshGPSAndVoilations(true);
            }
        });

        doNo.setFont(UIConstant.textFont);
        doNo.setMaximumRowCount(10);
        doNo.addItem(new ComboItem(0, "Select"));
        doNo.setEnabled(false);
        doNo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                //doNoActionPerformed(evt);
                doNoFocusLost(null);
            }
        });
        doNo.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                //doNoFocusLost(evt);
            }
        });
        doNo.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                doNoKeyPressed(evt);
            }
        });

        overrides.setFont(UIConstant.textFont);
        overrides.setForeground(UIConstant.textFontColor);

        jLabel16.setFont(UIConstant.labelFont);
        jLabel16.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel16.setText("Mineral Permit #:");

        mineralPermitNo.setFont(UIConstant.textFont);
        mineralPermitNo.setForeground(UIConstant.textFontColor);
        mineralPermitNo.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        mineralPermitNo.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                mineralPermitNoFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                mineralPermitNoFocusLost(evt);
            }
        });
        mineralPermitNo.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                mineralPermitNoKeyPressed(evt);
            }
        });

        jLabel23.setFont(UIConstant.labelFont);
        jLabel23.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel23.setText("LR Date:");

        jLabel24.setFont(UIConstant.labelFont);
        jLabel24.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel24.setText("Supplier:");

        grossWt.setFont(UIConstant.textFont);
        grossWt.setForeground(UIConstant.textFontColor);
        supplier.setFont(UIConstant.textFont);
        supplier.setEditable(false);
        supplier.setBorder(null);
        supplier.setFocusable(false);
        supplier.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                supplierFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                supplierFocusLost(evt);
            }
        });
        supplier.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                supplierKeyPressed(evt);
            }
        });

        jLabel25.setFont(UIConstant.labelFont);
        jLabel25.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel25.setText("Gate In Time :");

        gateInTime.setFont(UIConstant.textFont);
        gateInTime.setForeground(UIConstant.textFontColor);
        gateInTime.setEditable(false);
        gateInTime.setBorder(null);
        gateInTime.setFocusable(false);
        gateInTime.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                gateInTimeFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                gateInTimeFocusLost(evt);
            }
        });
        gateInTime.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                gateInTimeKeyPressed(evt);
            }
        });

        transporter.setFont(UIConstant.textFont);
        transporter.setMaximumRowCount(10);
        transporter.addItem(new ComboItem(0, "Select"));
        transporter.setEnabled(false);
        transporter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                transporterActionPerformed();
            }
        });
        transporter.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
            	transporterActionPerformed();
            }
        });
        transporter.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                transporterKeyPressed(evt);
            }
        });

        de = new JSpinner.DateEditor(challanDate,"dd/MM/yyyy HH:mm");
        challanDate.setEditor(de);
        challanDate.setEnabled(false);
        challanDate.setFocusTraversalKeysEnabled(true);
        challanDate.setFont(UIConstant.textFont);
        challanDate.setForeground(UIConstant.textFontColor);
        challanDate.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                challanDateFocusGained(evt);
            }
        });
        challanDate.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                challanDateKeyPressed(evt);
            }
        });
        ChangeListener changeListener = new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
            	if(challanDate.getValue() != null && (Misc.isUndef(challanDateTime) || Math.abs((challanDateTime - ((Date)challanDate.getValue()).getTime()) ) > 60000)){
            		challanDateTime = ((Date)challanDate.getValue()).getTime();
            		refreshGPSAndVoilations(true);
            	}
            }
        };
        challanDate.addChangeListener(changeListener);
        
        JSpinner.DateEditor dedit = new JSpinner.DateEditor(lrDate,"dd/MM/yyyy HH:mm");
        lrDate.setEditor(dedit);
        lrDate.setEnabled(false);
        lrDate.setFont(UIConstant.textFont);
        lrDate.setForeground(UIConstant.textFontColor);
        lrDate.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                lrDateFocusGained(evt);
            }
        });
        lrDate.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                lrDateKeyPressed(evt);
            }
        });

        jLabel27.setFont(UIConstant.labelFont);
        jLabel27.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel27.setText("GPS Location:");

        jLabel28.setFont(UIConstant.labelFont);
        jLabel28.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel28.setText("GPS Time:");

        gpsLocation.setFont(UIConstant.textFont);
        gpsLocation.setForeground(UIConstant.textFontColor);

        gpsTime.setFont(UIConstant.textFont);
        gpsTime.setForeground(UIConstant.textFontColor);
        
        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
                jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel3Layout.createSequentialGroup()
                    .addGap(18, 18, 18)
                    .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                    .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(jLabel27, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel13, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel12, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel15, javax.swing.GroupLayout.DEFAULT_SIZE, 120, Short.MAX_VALUE)
                        .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, 120, Short.MAX_VALUE)
                        .addComponent(jLabel28, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel3Layout.createSequentialGroup()
                            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addGroup(jPanel3Layout.createSequentialGroup()
                                    .addComponent(vehicleName, javax.swing.GroupLayout.PREFERRED_SIZE, 264, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(jLabel25, javax.swing.GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE))
                                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel3Layout.createSequentialGroup()
                                    .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addComponent(overrides, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(roadPermitNo)
                                        .addComponent(lrNo)
                                        .addComponent(challanNo)
                                        .addComponent(grossWt, javax.swing.GroupLayout.DEFAULT_SIZE, 211, Short.MAX_VALUE)
                                        .addComponent(mines, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(transporter, 0, 211, Short.MAX_VALUE))
                                    .addGap(14, 14, 14)
                                    .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jLabel14, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(jLabel24, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(jLabel23, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(jLabel11, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(jLabel10, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(jLabel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(jLabel16, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 143, Short.MAX_VALUE))))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(tareWt)
                                    .addComponent(mineralPermitNo, javax.swing.GroupLayout.DEFAULT_SIZE, 215, Short.MAX_VALUE)
                                    .addComponent(grade, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addComponent(doNo, 0, 215, Short.MAX_VALUE)
                                .addComponent(gateInTime, javax.swing.GroupLayout.DEFAULT_SIZE, 215, Short.MAX_VALUE)
                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addComponent(lrDate, javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(challanDate, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 215, Short.MAX_VALUE))
                                .addComponent(supplier)))
                        .addGroup(jPanel3Layout.createSequentialGroup()
                            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(gpsTime, javax.swing.GroupLayout.PREFERRED_SIZE, 210, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(gpsLocation, javax.swing.GroupLayout.PREFERRED_SIZE, 442, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGap(0, 0, Short.MAX_VALUE)))
                    .addContainerGap())
            );
            jPanel3Layout.setVerticalGroup(
                jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel3Layout.createSequentialGroup()
                    .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel3Layout.createSequentialGroup()
                            .addContainerGap()
                            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel25, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(gateInTime, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(vehicleName, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, 30, Short.MAX_VALUE)
                            .addComponent(overrides, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel24, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(supplier, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(mines, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(doNo, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel3Layout.createSequentialGroup()
                            .addGap(3, 3, 3)
                            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(grade, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(transporter, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGap(3, 3, 3)
                            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(challanDate, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(challanNo, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(lrNo)
                                    .addComponent(jLabel23, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addComponent(lrDate, javax.swing.GroupLayout.DEFAULT_SIZE, 30, Short.MAX_VALUE))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(grossWt, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabel14, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(tareWt, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jLabel16, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel15, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(roadPermitNo, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(mineralPermitNo, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(gpsLocation, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jLabel27, javax.swing.GroupLayout.DEFAULT_SIZE, 29, Short.MAX_VALUE))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(jLabel28, javax.swing.GroupLayout.DEFAULT_SIZE, 29, Short.MAX_VALUE)
                                .addComponent(gpsTime, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(199, 199, 199))))
            );

        jPanel5.setBackground(new java.awt.Color(255, 255, 255));

        jLabelGrossShort.setFont(UIConstant.labelFont);
        jLabelGrossShort.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabelGrossShort.setText("Gross Short:");

        jLabelGpsStatus.setFont(UIConstant.labelFont);
        jLabelGpsStatus.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabelGpsStatus.setText("GPS Status:");

        jLabelGpsViolation.setFont(UIConstant.labelFont);
        jLabelGpsViolation.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabelGpsViolation.setText("GPS Violations:");

        jLabelQCMark.setFont(UIConstant.labelFont);
        jLabelQCMark.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabelQCMark.setText("Mark for QC:");

        jLabelListOfViolation.setFont(UIConstant.labelFont);
        jLabelListOfViolation.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabelListOfViolation.setText("List of Violations:");

        grossShort.setFont(UIConstant.textFont);
        grossShort.setForeground(UIConstant.textFontColor);
        grossShort.setEditable(false);
        grossShort.setBackground(new java.awt.Color(255, 255, 255));
        grossShort.setBorder(null);
        grossShort.setFocusable(false);
        grossShort.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                grossShortKeyPressed(evt);
            }
        });

        gpsStatus.setFont(UIConstant.textFont);
        gpsStatus.setForeground(UIConstant.textFontColor);

        gpsViolation.setFont(UIConstant.textFont);
        gpsViolation.setForeground(UIConstant.textFontColor);

        jLabelBedAssigned.setFont(UIConstant.labelFont);
        jLabelBedAssigned.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabelBedAssigned.setText("Bed Assigned:");

        bedAssigned.setFont(UIConstant.textFont);
        bedAssigned.setForeground(UIConstant.textFontColor);

        listOfViolation.setFont(UIConstant.textFontSmall);
        listOfViolation.setEditable(false);
        listOfViolation.setLineWrap(true);
        listOfViolation.setWrapStyleWord(true);
        listOfViolation.setFocusable(false);
        JScrollPane listOfViolationScroll = new JScrollPane (listOfViolation);
        listOfViolationScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        //listOfViolation.setText("sdjkvbjbsjkv bkjsvbskjvbkj ");
//        listOfViolation.setBackground(Color.GRAY);
//        listOfViolation.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        markForQC.setFont(UIConstant.textFont);
        markForQC.setForeground(UIConstant.textFontColor);

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jLabelBedAssigned, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabelQCMark, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 129, Short.MAX_VALUE)
                    .addComponent(jLabelGpsViolation, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabelGpsStatus, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabelGrossShort, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabelListOfViolation, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    //.addComponent(listOfViolation, javax.swing.GroupLayout.DEFAULT_SIZE, 297, Short.MAX_VALUE)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(grossShort, javax.swing.GroupLayout.DEFAULT_SIZE, 225, Short.MAX_VALUE)
                            .addComponent(gpsStatus, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(bedAssigned, javax.swing.GroupLayout.DEFAULT_SIZE, 225, Short.MAX_VALUE)
                            .addComponent(gpsViolation, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(markForQC, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
                .addComponent(listOfViolationScroll, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGap(2, 2, 2)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabelGrossShort, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(grossShort, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(gpsStatus, javax.swing.GroupLayout.DEFAULT_SIZE, 30, Short.MAX_VALUE)
                    .addComponent(jLabelGpsStatus, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabelBedAssigned, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(bedAssigned, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabelGpsViolation, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(gpsViolation, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabelQCMark, javax.swing.GroupLayout.DEFAULT_SIZE, 32, Short.MAX_VALUE)
                    .addComponent(markForQC, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGap(5, 5, 5)
                        .addComponent(jLabelListOfViolation, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    /*.addGroup(jPanel5Layout.createSequentialGroup()
                        .addGap(3, 3, 3)*/
              .addComponent(listOfViolationScroll, javax.swing.GroupLayout.PREFERRED_SIZE, 165, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jButton1.setFont(UIConstant.textFont);
        jButton1.setText("Clear");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        jButton1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jButton1KeyPressed(evt);
            }
        });

        saveAndOpen.setFont(UIConstant.textFont);
        saveAndOpen.setText("Save And Open Gate");
        saveAndOpen.setEnabled(false);
        saveAndOpen.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveAndOpenActionPerformed(evt);
            }
        });
        saveAndOpen.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                saveAndOpenKeyPressed(evt);
            }
        });

        jPanel6.setBackground(new java.awt.Color(0, 0, 0));

        labelWeighment.setFont(UIConstant.vehicleLabel);
        labelWeighment.setBackground(new java.awt.Color(0, 0, 0));
        labelWeighment.setFont(new java.awt.Font("Segoe UI", 1, 48)); // NOI18N
        labelWeighment.setForeground(new java.awt.Color(255, 255, 255));
        labelWeighment.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(labelWeighment, javax.swing.GroupLayout.DEFAULT_SIZE, 371, Short.MAX_VALUE)
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(labelWeighment, javax.swing.GroupLayout.DEFAULT_SIZE, 74, Short.MAX_VALUE)
        );

        digitalClock.setFont(UIConstant.textFont); // NOI18N

        digitalClock.setForeground(UIConstant.textFontColor);
        digitalClock.setFont(new java.awt.Font("Segoe UI", 1, 36)); // NOI18N

        manualEntryButton.setFont(UIConstant.textFont);
        manualEntryButton.setText("Manual Entry");
        //manualEntry.setEnabled(false);
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

        blocking_reason.setFont(UIConstant.textFont);
        blocking_reason.setForeground(UIConstant.noActionPanelColor);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 131, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(75, 75, 75)
                        .addComponent(manualEntryButton, javax.swing.GroupLayout.PREFERRED_SIZE, 229, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(47, 47, 47))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(61, 91, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(saveAndOpen, javax.swing.GroupLayout.PREFERRED_SIZE, 231, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 221, Short.MAX_VALUE))
                            .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addContainerGap())))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(digitalClock, javax.swing.GroupLayout.PREFERRED_SIZE, 179, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(34, 34, 34))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(blocking_reason, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(91, 91, 91))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(blocking_reason, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(saveAndOpen, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(manualEntryButton, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(digitalClock, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(32, 32, 32))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(0, 0, 0))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void button1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button1ActionPerformed
        stopRfid();
        this.dispose();
        try {
            new LoginWindow().setVisible(true);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }//GEN-LAST:event_button1ActionPerformed

    private void paperChallanNoMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_paperChallanNoMouseClicked
        paperChallanNoActionPerformed();
    }//GEN-LAST:event_paperChallanNoMouseClicked

    private void paperChallanNoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_paperChallanNoActionPerformed
        paperChallanNoActionPerformed();

    }//GEN-LAST:event_paperChallanNoActionPerformed

    private void paperChallanYesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_paperChallanYesActionPerformed
        setWhiteBackColor();
        // grossWt.setEnabled(true);
        if (vehicleName.isTextEditable() && Utils.isNull(vehicleName.getText())) {
            vehicleName.setTextBackground(UIConstant.focusPanelColor);
            vehicleName.requestFocusInWindow();
        } else {
            roadPermitNo.setBackground(UIConstant.focusPanelColor);
            roadPermitNo.requestFocusInWindow();
        }

        transporter.setEnabled(false);
        lrNo.setEditable(false);
        lrNo.setFocusable(false);
        lrNo.setBorder(null);
        lrDate.setEnabled(false);
        challanDate.setEnabled(false);

        //challanDate.setDate(null);
        challanNo.setEditable(false);
        challanNo.setFocusable(false);
        //challanNo.setText("");
        challanNo.setBorder(null);
        grade.setEnabled(false);
        mines.setEnabled(false);
        doNo.setEnabled(false);

        grossWt.setEditable(false);
        grossWt.setFocusable(false);
        grossWt.setBorder(null);

        tareWt.setEditable(false);
        tareWt.setFocusable(false);
        tareWt.setBorder(null);

//        saveAndOpen.setEnabled(true);
//        manualEntry.setEnabled(true);

    }//GEN-LAST:event_paperChallanYesActionPerformed

    private void paperChallanYesKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_paperChallanYesKeyPressed
        paperChallanYesActionPerformed();
    }//GEN-LAST:event_paperChallanYesKeyPressed

    private void paperChallanPanelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_paperChallanPanelMouseClicked
        setWhiteBackColor();
        paperChallanPanel.requestFocusInWindow();
        paperChallanPanel.setBackground(UIConstant.focusPanelColor);
    }//GEN-LAST:event_paperChallanPanelMouseClicked

    private void paperChallanPanelKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_paperChallanPanelKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            setWhiteBackColor();
            if (mines.isEnabled()) {
                mines.requestFocusInWindow();
            } else {
                roadPermitNo.setBackground(UIConstant.focusPanelColor);
                roadPermitNo.requestFocusInWindow();
            }

        } else if (evt.getKeyCode() == KeyEvent.VK_1) {
            paperChallanYes.setSelected(true);
            setWhiteBackColor();
            if (Utils.isNull(vehicleName.getText()) && vehicleName.isTextEditable()) {
                vehicleName.setTextBackground(UIConstant.focusPanelColor);
                vehicleName.requestFocusInWindow();
            } else {
                roadPermitNo.setBackground(UIConstant.focusPanelColor);
                roadPermitNo.requestFocusInWindow();
            }

            lrNo.setEditable(false);
            lrNo.setFocusable(false);
            //lrNo.setText("");
            lrNo.setBorder(null);
            lrDate.setEnabled(false);
            challanDate.setEnabled(false);
            //challanDate.setDate(null);
            challanNo.setEditable(false);
            challanNo.setFocusable(false);
            // challanNo.setText("");
            challanNo.setBorder(null);
            grade.setEnabled(false);
            mines.setEnabled(false);
            doNo.setEnabled(false);
            //  transporter.requestFocusInWindow();
            grossWt.setEditable(true);
            grossWt.setFocusable(true);
            grossWt.setBorder(null);
            tareWt.setEditable(false);
            tareWt.setFocusable(false);
            tareWt.setBorder(null);
            //            transporter.setSelectedIndex(0);
            //            mines.setSelectedIndex(0);
            //            grade.setSelectedIndex(0);
            //            doNo.setSelectedIndex(0);
//            saveAndOpen.setEnabled(true);
//            manualEntry.setEnabled(true);
        } else if (evt.getKeyCode() == KeyEvent.VK_2) {
            mines.setEnabled(true);
            mines.setFocusable(true);
            paperChallanNo.setSelected(true);
            setWhiteBackColor();
            if (vehicleName.isTextEditable() && Utils.isNull(vehicleName.getText())) {
                vehicleName.setTextBackground(UIConstant.focusPanelColor);
                vehicleName.requestFocusInWindow();
            } else {
                mines.requestFocusInWindow();
            }
            //saveAndOpen.setEnabled(false);
            lrNo.setEditable(true);
            lrNo.setFocusable(true);
            //lrNo.setText("");
            lrNo.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
            lrDate.setEnabled(true);
            challanDate.setEnabled(true);
            //challanDate.setDate(null);
            challanNo.setEditable(true);
            challanNo.setFocusable(true);
            // challanNo.setText("");
            challanNo.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
            grade.setEnabled(true);
            transporter.setEnabled(true);
            doNo.setEnabled(true);

            grossWt.setEditable(true);
            grossWt.setFocusable(true);
            grossWt.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

            tareWt.setEditable(true);
            tareWt.setFocusable(true);
            tareWt.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
//            manualEntry.setEnabled(false);
        }
    }//GEN-LAST:event_paperChallanPanelKeyPressed

    private void vehicleNameMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_vehicleNameMouseClicked
        setWhiteBackColor();
        vehicleName.setTextBackground(UIConstant.focusPanelColor);
    }//GEN-LAST:event_vehicleNameMouseClicked

    private void vehicleNameFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_vehicleNameFocusGained
        setWhiteBackColor();
        vehicleName.setTextBackground(UIConstant.focusPanelColor);
    }//GEN-LAST:event_vehicleNameFocusGained

    private void vehicleNameFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_vehicleNameFocusLost
        setWhiteBackColor();
    }//GEN-LAST:event_vehicleNameFocusLost

    private void vehicleNameKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_vehicleNameKeyPressed

        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            vehicleNameAction();
        }
    }//GEN-LAST:event_vehicleNameKeyPressed

    private void lrNoFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_lrNoFocusGained
        setWhiteBackColor();
        lrNo.setBackground(UIConstant.focusPanelColor);
    }//GEN-LAST:event_lrNoFocusGained

    private void lrNoFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_lrNoFocusLost
        lrNo.setBackground(UIConstant.PanelWhite);
    }//GEN-LAST:event_lrNoFocusLost

    private void lrNoKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_lrNoKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            setWhiteBackColor();
            lrDate.requestFocusInWindow();
            //  challanDate.setBackground(UIConstant.focusPanelColor);
        }
    }//GEN-LAST:event_lrNoKeyPressed

    private void challanNoFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_challanNoFocusGained
        setWhiteBackColor();
        challanNo.setBackground(UIConstant.focusPanelColor);
    }//GEN-LAST:event_challanNoFocusGained

    private void challanNoKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_challanNoKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            setWhiteBackColor();

            challanDate.requestFocusInWindow();

//            mines.requestFocusInWindow();
            //  mines.setBackground(UIConstant.focusPanelColor);
        }
    }//GEN-LAST:event_challanNoKeyPressed

    private void grossWtFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_grossWtFocusGained
        setWhiteBackColor();
        grossWt.setBackground(UIConstant.focusPanelColor);
    }//GEN-LAST:event_grossWtFocusGained

    private void grossWtFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_grossWtFocusLost
        calculateGrossShort();
    }//GEN-LAST:event_grossWtFocusLost

    private void grossWtKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_grossWtKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            setWhiteBackColor();
            tareWt.requestFocusInWindow();
            tareWt.setBackground(UIConstant.focusPanelColor);
        }
    }//GEN-LAST:event_grossWtKeyPressed

    private void tareWtActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tareWtActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tareWtActionPerformed

    private void tareWtFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_tareWtFocusGained
        setWhiteBackColor();
        tareWt.setBackground(UIConstant.focusPanelColor);
    }//GEN-LAST:event_tareWtFocusGained

    private void tareWtKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tareWtKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            setWhiteBackColor();
            roadPermitNo.requestFocusInWindow();
            roadPermitNo.setBackground(UIConstant.focusPanelColor);
        }
    }//GEN-LAST:event_tareWtKeyPressed

    private void roadPermitNoFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_roadPermitNoFocusGained
        setWhiteBackColor();
        roadPermitNo.setBackground(UIConstant.focusPanelColor);
    }//GEN-LAST:event_roadPermitNoFocusGained

    private void roadPermitNoFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_roadPermitNoFocusLost
        //  changeQuickCreateToSave();
    }//GEN-LAST:event_roadPermitNoFocusLost

    private void roadPermitNoKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_roadPermitNoKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            setWhiteBackColor();
            mineralPermitNo.setBackground(UIConstant.focusPanelColor);
            mineralPermitNo.requestFocusInWindow();

        }
    }//GEN-LAST:event_roadPermitNoKeyPressed

    private void transporterKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_transporterKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            setWhiteBackColor();
            grade.requestFocusInWindow();
            // mines.setBackground(UIConstant.focusPanelColor);
        }
    }//GEN-LAST:event_transporterKeyPressed

    private void minesKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_minesKeyPressed
    	setWhiteBackColor();
        doNo.requestFocusInWindow();
    }//GEN-LAST:event_minesKeyPressed

    private void gradeKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_gradeKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            setWhiteBackColor();
            challanNo.requestFocusInWindow();
            challanNo.setBackground(UIConstant.focusPanelColor);
//            doNo.requestFocusInWindow();
            // challanDate.setBackground(UIConstant.focusPanelColor);
        }
    }//GEN-LAST:event_gradeKeyPressed

    private void mineralPermitNoFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_mineralPermitNoFocusGained
        setWhiteBackColor();
        mineralPermitNo.setBackground(UIConstant.focusPanelColor);
    }//GEN-LAST:event_mineralPermitNoFocusGained

    private void mineralPermitNoFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_mineralPermitNoFocusLost
        setWhiteBackColor();
    }//GEN-LAST:event_mineralPermitNoFocusLost

    private void mineralPermitNoKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_mineralPermitNoKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            setWhiteBackColor();
            /*if (manualEntryButton.isEnabled()) {
                manualEntryButton.requestFocusInWindow();
            } else {
                saveAndOpen.requestFocusInWindow();
            }*/
            saveAndOpen.requestFocusInWindow();
        }
    }//GEN-LAST:event_mineralPermitNoKeyPressed

    private void grossShortKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_grossShortKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            gpsStatus.requestFocusInWindow();
            //doNo.setBackground(UIConstant.focusPanelColor);
        }
    }//GEN-LAST:event_grossShortKeyPressed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        clearAction();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton1KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jButton1KeyPressed
        clearAction();
    }//GEN-LAST:event_jButton1KeyPressed

    private void manualEntryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_manualEntryActionPerformed
        manualEntryAction();
    }//GEN-LAST:event_manualEntryActionPerformed

    private void saveAndOpenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveAndOpenActionPerformed
        quickCreateAndOpen();
    }//GEN-LAST:event_saveAndOpenActionPerformed

    private void saveAndOpenKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_saveAndOpenKeyPressed
        quickCreateAndOpen();
    }//GEN-LAST:event_saveAndOpenKeyPressed

    private void challanNoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_challanNoActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_challanNoActionPerformed

    private void supplierFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_supplierFocusGained
        // TODO add your handling code here:
    }//GEN-LAST:event_supplierFocusGained

    private void supplierFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_supplierFocusLost
        // TODO add your handling code here:
    }//GEN-LAST:event_supplierFocusLost

    private void supplierKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_supplierKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_supplierKeyPressed

    private void gateInTimeFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_gateInTimeFocusGained
        // TODO add your handling code here:
    }//GEN-LAST:event_gateInTimeFocusGained

    private void gateInTimeFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_gateInTimeFocusLost
        // TODO add your handling code here:
    }//GEN-LAST:event_gateInTimeFocusLost

    private void gateInTimeKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_gateInTimeKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_gateInTimeKeyPressed

    private void doNoKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_doNoKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            setWhiteBackColor();
//            setTransporterList(0);
//            setGradeList(0);
            transporter.setFocusable(true);
            transporter.requestFocusInWindow();
        }
    }//GEN-LAST:event_doNoKeyPressed

    private void manualEntryKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_manualEntryKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            manualEntryAction();
        }
    }//GEN-LAST:event_manualEntryKeyPressed

    private void transporterActionPerformed() {//GEN-FIRST:event_transporterActionPerformed
    	Connection conn = null;
    	boolean destroyIt = false;
    	try{
    		conn = DBConnectionPool.getConnectionFromPoolNonWeb();
    		int selectedtransporter = DropDownValues.getComboSelectedVal(transporter);
    		int selectedMines = DropDownValues.getComboSelectedVal(mines);
    		int selectedGrade = DropDownValues.getComboSelectedVal(grade);
    		int selectedDO = DropDownValues.getComboSelectedVal(doNo);
    		Pair<Integer, String> bedAssign = TPRUtils.getBedAllignment(conn, selectedtransporter, selectedMines, selectedGrade, selectedDO);
    		if(bedAssign != null)
    			bedAssigned.setText(bedAssign.second);
    		//setMarkForQcAndGPSVoilation(conn, true);
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
    }//GEN-LAST:event_transporterActionPerformed
    
    private void refreshGPSAndVoilations(boolean createNew) {//GEN-FIRST:event_transporterActionPerformed
    	if(tprRecord == null)
    		return;
    	Connection conn = null;
    	boolean destroyIt = false;
    	try{
    		conn = DBConnectionPool.getConnectionFromPoolNonWeb();
    		setMarkForQcAndGPSVoilation(conn, true);
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

    private void minesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_minesActionPerformed
    }//GEN-LAST:event_minesActionPerformed

    private void doNoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_doNoActionPerformed
    }//GEN-LAST:event_doNoActionPerformed
    private void challanNoFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_challanNoFocusLost
        // TODO add your handling code here:
    }//GEN-LAST:event_challanNoFocusLost

    private void challanDateFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_challanDateFocusGained
        setWhiteBackColor();
        challanDate.setBackground(UIConstant.focusPanelColor);
    }//GEN-LAST:event_challanDateFocusGained

    private void challanNoKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_challanNoKeyReleased
    }//GEN-LAST:event_challanNoKeyReleased

    private void challanDateKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_challanDateKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
        	lrNo.requestFocusInWindow();
        }
    }//GEN-LAST:event_challanDateKeyPressed

    private void lrDateKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_lrDateKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            grossWt.requestFocusInWindow();
        }
    }//GEN-LAST:event_lrDateKeyPressed

    private void lrDateFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_lrDateFocusGained
        setWhiteBackColor();
        lrDate.setBackground(UIConstant.focusPanelColor);
    }//GEN-LAST:event_lrDateFocusGained

    private void doNoFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_doNoFocusLost
    	Pair<Integer, String> supplierDetails;
    	Connection conn = null;
    	boolean destroyIt = false;
    	try {
    		conn = DBConnectionPool.getConnectionFromPoolNonWeb();
    		supplierDetails = TPRUtils.getSupplierFromDo(conn, DropDownValues.getComboSelectedVal(doNo));
    		if(!Misc.isUndef(supplierDetails.first)){
    			supplier.setText(supplierDetails.second);
    		}else{
    			supplier.setText("");
    		}
    		setTransporterList(conn, DropDownValues.getComboSelectedVal(transporter), DropDownValues.getComboSelectedVal(doNo));
    		transporterActionPerformed();
    		setGradeList(conn, DropDownValues.getComboSelectedVal(grade), DropDownValues.getComboSelectedVal(doNo));
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
    }//GEN-LAST:event_doNoFocusLost

    private void minesRefresh() {//GEN-FIRST:event_minesFocusLost
    	if(tprRecord == null)
    		return;
    	Connection conn = null;
    	boolean destroyIt = false;
    	try{
    		conn = DBConnectionPool.getConnectionFromPoolNonWeb();
    		setMarkForQcAndGPSVoilation(conn, true);
    		setDoRrList(conn,DropDownValues.getComboSelectedVal(doNo), DropDownValues.getComboSelectedVal(mines));         // TODO add your handling code here:
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
    }//GEN-LAST:event_minesFocusLost
    void setWhiteBackColor() {
        vehicleName.setBackground(UIConstant.PanelWhite);
        lrNo.setBackground(UIConstant.PanelWhite);
        challanNo.setBackground(UIConstant.PanelWhite);
        challanDate.setBackground(UIConstant.PanelWhite);
        grossWt.setBackground(UIConstant.PanelWhite);
        tareWt.setBackground(UIConstant.PanelWhite);
        roadPermitNo.setBackground(UIConstant.PanelWhite);
        grossShort.setBackground(UIConstant.PanelWhite);
        paperChallanPanel.setBackground(UIConstant.PanelWhite);
        mineralPermitNo.setBackground(UIConstant.PanelWhite);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(CoalWeighBridgeInWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(CoalWeighBridgeInWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(CoalWeighBridgeInWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(CoalWeighBridgeInWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    new CoalWeighBridgeInWindow().setVisible(true);
                } catch (IOException ex) {
                    Logger.getLogger(CoalWeighBridgeInWindow.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel bedAssigned;
    private javax.swing.JLabel blocking_reason;
    private java.awt.Button jButtonSignOut;
    private javax.swing.JSpinner challanDate;
    private javax.swing.JTextField challanNo;
    public static javax.swing.JLabel digitalClock;
    private javax.swing.JComboBox doNo;
    private javax.swing.JTextField gateInTime;
    private javax.swing.JLabel gpsLocation;
    private javax.swing.JLabel gpsStatus;
    private javax.swing.JLabel gpsTime;
    private javax.swing.JLabel gpsViolation;
    private javax.swing.JComboBox grade;
    private javax.swing.JTextField grossShort;
    private javax.swing.JTextField grossWt;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabelGrossShort;
    private javax.swing.JLabel jLabelGpsStatus;
    private javax.swing.JLabel jLabelGpsViolation;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabelQCMark;
    private javax.swing.JLabel jLabelListOfViolation;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabelBedAssigned;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JLabel labelWeighment;
    private javax.swing.JTextArea listOfViolation;
    private javax.swing.JSpinner lrDate;
    private javax.swing.JTextField lrNo;
    private javax.swing.JLabel markForQC;
    private javax.swing.JTextField mineralPermitNo;
    private javax.swing.JComboBox mines;
    private javax.swing.JLabel overrides;
    private javax.swing.JCheckBox paperChallanNo;
    private javax.swing.JPanel paperChallanPanel;
    private javax.swing.JCheckBox paperChallanYes;
    private javax.swing.JTextField roadPermitNo;
    private javax.swing.JButton saveAndOpen;
    private javax.swing.JButton manualEntryButton;
    private javax.swing.JTextField supplier;
    private javax.swing.JTextField tareWt;
    private javax.swing.JComboBox transporter;
    public static javax.swing.JLabel username;
    private AutoCompleteCombo vehicleName;
    // End of variables declaration//GEN-END:variables

    private void myComponent() {
//        markQc.add(markQcNo);
//        markQc.add(markQcYes);

        paperChallan.add(paperChallanYes);
        paperChallan.add(paperChallanNo);
    }

    private void quickCreateAndOpen() {
        setWhiteBackColor();
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
        	} else if (DropDownValues.isNull(mines)){//mines.getSelectedIndex() == 0) {
        		JOptionPane.showMessageDialog(null, "Please Select Mines");
        		mines.requestFocusInWindow();
        		return;
        	} else if (DropDownValues.isNull(doNo)){//doNo.getSelectedIndex() == 0) {
        		JOptionPane.showMessageDialog(null, "Please Select Do Number");
        		doNo.requestFocusInWindow();
        		return;
        	} else if (DropDownValues.isNull(transporter)){//transporter.getSelectedIndex() == 0) {
        		JOptionPane.showMessageDialog(null, "Please Select Transporter");
        		transporter.requestFocusInWindow();
        		return;
        	} else if (DropDownValues.isNull(grade)){//grade.getSelectedIndex() == 0) {
        		JOptionPane.showMessageDialog(null, "Please Select Grade");
        		grade.requestFocusInWindow();
        		return;
        	} else if (isSpinnerDateNull(challanDate.getValue())) {
        		JOptionPane.showMessageDialog(null, "Please Select Challan Date");
        		challanDate.requestFocusInWindow();
        		return;
        	} else if (!paperChallanYes.isSelected() && !paperChallanNo.isSelected()) {
        		JOptionPane.showMessageDialog(null, "Please Confirm that Paper Challan is Match Or Not");
        		paperChallanPanel.requestFocusInWindow();
        		paperChallanPanel.setBackground(UIConstant.focusPanelColor);
        		return;
        	} else if (!Utils.isNumericDigit(grossWt.getText()) || Misc.getParamAsDouble(grossWt.getText()) > 30.00 || Misc.getParamAsDouble(grossWt.getText()) < 15.00) {
        		JOptionPane.showMessageDialog(null, "Please Enter Valid Gross Wt");
        		grossWt.setText("");
        		grossWt.requestFocusInWindow();
        		grossWt.setBackground(UIConstant.focusPanelColor);
        		return;
        	} else if (!Utils.isNumericDigit(tareWt.getText()) || Misc.getParamAsDouble(tareWt.getText()) > 14.99 || Misc.getParamAsDouble(tareWt.getText()) < 8.00) {
        		JOptionPane.showMessageDialog(null, "Please Enter Valid Tare Wt");
        		tareWt.setText("");
        		tareWt.requestFocusInWindow();
        		tareWt.setBackground(UIConstant.focusPanelColor);
        		return;
        	} else if (Utils.isNull(roadPermitNo.getText())) {
        		JOptionPane.showMessageDialog(null, "Please Enter Dispatch Permit No");
        		roadPermitNo.requestFocusInWindow();
        		roadPermitNo.setBackground(UIConstant.focusPanelColor);
        		return;
        	} else if (Utils.isNull(mineralPermitNo.getText())) {
        		JOptionPane.showMessageDialog(null, "Please Enter Mineral Permit No");
        		mineralPermitNo.requestFocusInWindow();
        		mineralPermitNo.setBackground(UIConstant.focusPanelColor);
        		return;
        	}
        	if (Utils.isNull(challanNo.getText())) {
        		JOptionPane.showMessageDialog(null, "Please provide valid challan No.");
        		challanNo.requestFocusInWindow();
        		challanNo.setBackground(UIConstant.focusPanelColor);
        		return;
        	}
        	if (Utils.isNull(lrNo.getText())) {
        		JOptionPane.showMessageDialog(null, "Please provide valid LR No.");
        		lrNo.requestFocusInWindow();
        		lrNo.setBackground(UIConstant.focusPanelColor);
        		return;
        	}
        	if (isSpinnerDateNull(lrDate.getValue())) {
        		JOptionPane.showMessageDialog(null, "Please provide valid LR Date");
        		lrDate.requestFocusInWindow();
        		lrDate.setBackground(UIConstant.focusPanelColor);
        		return;
        	} else{
        		captureWeight = Misc.getParamAsDouble(labelWeighment.getText());
        		if(captureWeight < 15000.0 || captureWeight > 30000.0){
        			JOptionPane.showMessageDialog(null, "Captured Weight is not in limits (15.00-30.00 MT).Please capture properly");
        			return;
        		}else{
        			captureWeight = captureWeight/1000;
        		}
        		String[] options = {"Yes", "No"};
//        		int responseVehicleDialog = ConfirmationDialog.getDialogBox(new javax.swing.JFrame(), true, options,  "Vehicle Name: " + vehicleName.getText() + "\nTransporter: " + transporter.getSelectedItem().toString() + "\nCaptured Weight: " + Misc.printDouble((captureWeight/1000),false));
        		int responseVehicleDialog = JOptionPane.showOptionDialog(new javax.swing.JFrame(),
        				"Vehicle Name: " + vehicleName.getText() + "\nTransporter: " + transporter.getSelectedItem().toString() + "\nCaptured Weight: " + Misc.printDouble(captureWeight,false),
                        "Confirm",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.INFORMATION_MESSAGE,
                        null, //do not use a custom Icon
                        options, //the titles of buttons
                        options[1]);
        		//int responseVehicleDialog = JOptionPane.showConfirmDialog(this, "Vehicle Name: " + vehicleName.getText() + "\nTransporter: " + transporter.getSelectedItem().toString() + "\nCaptured Weight: " + Misc.printDouble((captureWeight/1000),false), UIConstant.dialogTitle, JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
//                int responseVehicleDialog = UIConstant.showConfirmDialog(null, "Vehicle Name: " + vehicleName.getText() + "\nTransporter: " + transporter.getSelectedItem().toString() + "\nCaptured Weight: " + Misc.printDouble((captureWeight/1000),false));
//        		int responseVehicleDialog = ConfirmDialog.showDialog(this, "Vehicle Name: " + vehicleName.getText() + "\nTransporter: " + transporter.getSelectedItem().toString() + "\nCaptured Weight: " + Misc.printDouble((captureWeight/1000),false));
                System.out.print("##### Confirmation Value :#####" + responseVehicleDialog);
                if (responseVehicleDialog != JOptionPane.YES_OPTION) {//no_option is yes in our context
                    return;
                } else {
        			boolean isUpdateTpr = false;
        			int stepId = Misc.getUndefInt();
        			isUpdateTpr = updateTPR(conn);
        			if (isUpdateTpr) {
        				stepId = InsertTPRStep(conn,false);
        				if (stepId != Misc.getUndefInt()) {
        					//InsertQCDetatl(conn, stepId);
        					InsertTPRQuestionDetails(conn, stepId);
        				}
        				GateInDao.insertReadings(conn, tprRecord.getTprId(), readings);
        				conn.commit();
        				if (true) {
        					JOptionPane.showMessageDialog(null, "Detail Saved");
        					new PrintData(this, true, tprRecord, "", "").setVisible(true);
        					Barrier.openEntryGate();
        					clearInput(conn, false);
        					getFocus();
        				} 
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

    private void getFocus() {
        if (vehicleName.isTextEditable()) {
            setWhiteBackColor();
            vehicleName.requestFocusInWindow();
            vehicleName.setTextBackground(UIConstant.focusPanelColor);
        } else {
            setWhiteBackColor();
            paperChallanPanel.requestFocusInWindow();
            paperChallanPanel.setBackground(UIConstant.focusPanelColor);
            //grade.setBackground(UIConstant.focusPanelColor);
        }
    }

    void changeQuickCreateToSave() {

        if (tprRecord != null) {
            saveAndOpen.setText("Save And Open Gate");
        } 
        /*else {
            saveAndOpen.setText("Manual Entry");
        }*/
    }

    private boolean updateTPR(Connection conn) throws Exception {
        return updateTPR(conn, false);
    }

    private boolean updateTPR(Connection conn, boolean isDeny) throws Exception {
    	boolean isUpdate = false;
    	RFIDHolder prevData = tprRecord.getHolderManualData();
    	if(!isDeny){
    		//tprRecord.setVehicleName(vehicleName.getText());
    		if (transporter.isEnabled()) {
    			tprRecord.setTransporterId(DropDownValues.getComboSelectedVal(transporter));
    		}
    		tprRecord.setLoadGross(Misc.getParamAsDouble(grossWt.getText()));
    		tprRecord.setLoadTare(Misc.getParamAsDouble(tareWt.getText()));
    		tprRecord.setLrNo(lrNo.getText());
    		tprRecord.setChallanNo(challanNo.getText());
    		ComboItem comboVal = null;
    		if (mines.isEnabled()) {
    			tprRecord.setMinesId(DropDownValues.getComboSelectedVal(mines));
    		}
    		if (grade.isEnabled()) {
    			tprRecord.setMaterialGradeId(DropDownValues.getComboSelectedVal(grade));
    		}
    		if (doNo.isEnabled()) {
    			tprRecord.setDoId(DropDownValues.getComboSelectedVal(doNo));
    		}
    		tprRecord.setDispatchPermitNo(roadPermitNo.getText());
    		tprRecord.setMaterialDescription(mineralPermitNo.getText());
    		tprRecord.setMarkForQCReason(markForQcReason);
    		Date lrD = (Date) lrDate.getValue();
    		tprRecord.setLrDate(lrD);
    		tprRecord.setMarkForQC(markForQCVal);
    		Date chD = (Date) challanDate.getValue();
    		tprRecord.setChallanDate(chD);
    		tprRecord.setUnloadWbInName(TokenManager.userName);
    		tprRecord.setUnloadGross(captureWeight);
    		tprRecord.setUpdatedOn(new Date());
    		tprRecord.setPreStepType(TokenManager.currWorkStationType);
    		tprRecord.setNextStepType(TokenManager.nextWorkStationType);
    		tprRecord.setUpdatedBy(TokenManager.userId);
    		tprRecord.setPreStepDate(new Date());
    		tprRecord.setComboEnd(new Date());
    		if (TokenManager.closeTPR) {
    			tprRecord.setTprStatus(Status.TPR.CLOSE);
    			if (token.getEpcId() != null && token.getEpcId().length() >= 20 && rfidHandler != null) {
    				rfidHandler.clearData(Utils.HexStringToByteArray(token.getEpcId()), 5);
    			}
    		}
    		if (tprRecord.getComboStart() == null) {
    			tprRecord.setComboStart(new Date());
    		}
    		tprRecord.setLatestUnloadWbInExit(new Date());
    		RFIDHolder currData = tprRecord.getHolderManualData();
    		tprRecord.setChallanDataEditAtWb(currData != null && !currData.isMatched(prevData) ? 1 : Misc.getUndefInt());
    	}
    	tprRecord.setEarliestUnloadWbInEntry(entryTime);
    	if(!isDeny){
    		RFIDHolder manualDataHolder = tprRecord.getHolderManualData();
    		int matchingTPRId = manualDataHolder == null ? Misc.getUndefInt() :  manualDataHolder.getConflictingTPRId(conn);
    		if(!Misc.isUndef(matchingTPRId)){
    			JOptionPane.showMessageDialog(null, "Challan and mines match with existing tpr id "+matchingTPRId);
    			return false;
    		}
    	}
    	TPRInformation.insertUpdateTpr(conn, tprRecord);
    	if(tprBlockManager != null)
        	tprBlockManager.setTprBlockStatus(conn, tprRecord.getTprId(),TokenManager.userId);
    	isUpdate = true;
    	return isUpdate;
    }

    private int InsertTPRStep(Connection conn, boolean isDeny) throws Exception {
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
            tpStep.setShortWt(Misc.getParamAsDouble(grossShort.getText()));
            tpStep.setMineralChallanNo(mineralPermitNo.getText());
            tpStep.setDispatchPermitNo(roadPermitNo.getText());
            //tpsBean.setTareWt(Wt(Misc.getParamAsDouble(tareWt.getText()));
            tpStep.setGrossWt(Misc.getParamAsDouble(grossWt.getText()));
            tpStep.setSaveStatus(isDeny ? TPStep.REQUEST_OVERRIDE : TPStep.SAVE_AND_CONTINUE);

//            tpStep.setMarkForQc(markQcVal);

            tpStep.setHasValidRf(isTagRead ? 1 : 0);
            tpStep.setMaterialCat(TokenManager.materialCat);
            RFIDMasterDao.insert(conn, tpStep,false);
            RFIDMasterDao.insert(conn, tpStep,true);
        } else {
            long currTimeServerMillis = System.currentTimeMillis();
            tpStep.setExitTime(new Date(currTimeServerMillis));
            tpStep.setUpdatedOn(new Date(currTimeServerMillis));
            tpStep.setShortWt(Misc.getParamAsDouble(grossShort.getText()));
            tpStep.setMineralChallanNo(mineralPermitNo.getText());
            tpStep.setDispatchPermitNo(roadPermitNo.getText());
            tpStep.setMaterialCat(TokenManager.materialCat);
            //tpsBean.setTareWt(Wt(Misc.getParamAsDouble(tareWt.getText()));
            tpStep.setGrossWt(Misc.getParamAsDouble(grossWt.getText()));
            tpStep.setSaveStatus(isDeny ? TPStep.REQUEST_OVERRIDE : TPStep.SAVE_AND_CONTINUE);
//            tpStep.setMarkForQc(markQcVal);

            tpStep.setHasValidRf(isTagRead ? 1 : 0);
            RFIDMasterDao.update(conn, tpStep,false);
            RFIDMasterDao.update(conn, tpStep,true);
        }

        return tpStep.getId();
    }
    private boolean InsertTPRQuestionDetails(Connection conn, int stepId) throws Exception {
        HashMap<Integer, Integer> quesAnsList = getQuestionIdList();
        boolean isInsert = false;
        for (Map.Entry<Integer, Integer> entry : quesAnsList.entrySet()) {
            Integer questionId = entry.getKey();
            Integer answerId = entry.getValue();
            GateInDao.updateTPRQuestion(conn, tprRecord.getTprId(), TokenManager.currWorkStationType, questionId, answerId, TokenManager.userId);
        }
        return isInsert;
    }
    

    private HashMap<Integer, Integer> getQuestionIdList() {
        HashMap<Integer, Integer> quesAnsList = new HashMap<Integer, Integer>();
        if (paperChallanYes.isSelected()) {
            quesAnsList.put(Status.TPRQuestion.paperChallan, UIConstant.YES);
        } else if (paperChallanNo.isSelected()) {
            quesAnsList.put(Status.TPRQuestion.paperChallan, UIConstant.NO);
        } else {
        	quesAnsList.put(Status.TPRQuestion.paperChallan, Misc.getUndefInt());
        }
        	
        //quesAnsList.put(Status.TPRQuestion.isChallanExist, !Utils.isNull(tprRecord.getChallanNo())  ? Results.Questions.YES : Results.Questions.NO);
        return quesAnsList;
    }
    
    private void clearInput(Connection conn, boolean clearToken) {
    	if(clearToken){
    		TokenManager.clearWorkstation();
    	}else{
    		if(token != null)
    			TokenManager.returnToken(conn, token);
    	}
    	tprRecord = null;
    	overrides.setText("");
        //lrDate.setValue(getDate());
        //challanDate.setValue(getDate());
        challanNo.setText("");
        // labelWeighment.setText("");
        grossWt.setText("");
        tareWt.setText("");
        gateInTime.setText("");
        roadPermitNo.setText("");
        grossShort.setText("");
        gpsStatus.setText("");
        gpsViolation.setText("");
        mines.setSelectedIndex(0);
        doNo.removeAllItems();
        doNo.addItem(new ComboItem(0, "Select"));
        doNo.setSelectedIndex(0);
        doList = null;
        transporter.removeAllItems();
        transporter.addItem(new ComboItem(0, "Select"));
        transporter.setSelectedIndex(0);
        grade.removeAllItems();
        grade.addItem(new ComboItem(0, "Select"));
        grade.setSelectedIndex(0);
        paperChallan.clearSelection();
        mineralPermitNo.setText("");
        
        
        blocking_reason.setText("");
        vehicleName.setText("");
        transporter.setFocusable(false);
        transporter.setEnabled(false);
        transporter.setSelectedIndex(0);
        lrNo.setEditable(false);
        lrNo.setFocusable(false);
        lrNo.setBorder(null);
        lrNo.setText("");
        challanDate.setEnabled(false);
        lrDate.setEnabled(false);
        challanNo.setEditable(false);
        challanNo.setFocusable(false);
        challanNo.setBorder(null);
        grade.setEnabled(false);
        mines.setEnabled(false);
        doNo.setEnabled(false);
        grossWt.setEditable(false);
        grossWt.setFocusable(false);
        grossWt.setBorder(null);
        tareWt.setEditable(false);
        tareWt.setFocusable(false);
        tareWt.setBorder(null);
        listOfViolation.setText("");
        saveAndOpen.setText("Save And Open Gate");
        saveAndOpen.setEnabled(false);
        if(isManual)
        	manualEntryButton.setEnabled(true);
        isVehicleExist = false;
        entryTime = null;
        exitTime = null;
        token = null;
        
        tprBlockManager = null;
        tpStep = null;
        tprQcDetail = null;
        gpv = null;
        isTagRead = false;
        isTpRecordValid = false;
        vehicleBlackListed = false;
        isRequestOverride = false;
        captureWeight = Misc.getUndefDouble();
        doList = null;
        supplier.setText("");
        markForQCVal = Misc.getUndefInt();
        toggleVehicle(false);
        bedAssigned.setText("");
        markForQC.setText("");
        markForQcReason = null;
        overrides.setText("");
        challanDateTime = Misc.getUndefInt();
        readings = null;
        gpsTime.setText("");
        gpsLocation.setText("");
        setMinesList();
    }



    void stopRfid() {
        try {
            if (rfidHandler != null) {
                rfidHandler.stop();
            }
            if (weighBridge != null) {
                weighBridge.stopWeighBridge();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    void calculateGrossShort() {
        if (!Utils.isNull(labelWeighment.getText()) && !Utils.isNull(grossWt.getText())) {
            double mplGross = Double.valueOf(labelWeighment.getText());
            double partyGross = Double.valueOf(grossWt.getText());
            if(!Misc.isUndef(mplGross) && !Misc.isUndef(partyGross)){
            	if(mplGross > 8000)
        			mplGross = mplGross/1000;
            	double gross_short = partyGross - mplGross;
                grossShort.setText(Misc.getPrintableDouble(gross_short));
            }
        }
    }

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
    		vehicleName.setTextBackground(Color.WHITE);
    	}
        //  transporter.setFocusable(true);
        // transporter.setEnabled(true);
    }

//    private void setTransporterList() {
//    	Connection conn = null;
//    	boolean destroyIt = false;
//    	try{
//    		conn = DBConnectionPool.getConnectionFromPoolNonWeb();
//    		ArrayList<ComboItem> transporterList = DropDownValues.getTranporterList(conn);
//    		for (int i = 0; i < transporterList.size(); i++) {
//    			transporter.addItem(transporterList.get(i));
//    			transporter.setSelectedIndex(0);
//    		}
//    	}catch(Exception ex){
//    		ex.printStackTrace();
//    		destroyIt = true;
//    	}finally{
//    		try{
//    			DBConnectionPool.returnConnectionToPoolNonWeb(conn, destroyIt);
//    		}catch(Exception ex){
//    			ex.printStackTrace();
//    		}
//    	}
//    }

    private void setMinesList() {
    	Connection conn = null;
    	boolean destroyIt = false;
    	try{
    		conn = DBConnectionPool.getConnectionFromPoolNonWeb();
    		mines.removeAllItems();
    		mines.getItemCount();
    		mines.addItem(new ComboItem(Misc.getUndefInt(), "select"));
    		ArrayList<ComboItem> minesList = DropDownValues.getMinesList(conn);
    		for (int i = 0; i < minesList.size(); i++) {
    			mines.addItem(minesList.get(i));
    		}
    		mines.setSelectedIndex(0);
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

    private void setGardeList() {
    	Connection conn = null;
    	boolean destroyIt = false;
    	try{
    		conn = DBConnectionPool.getConnectionFromPoolNonWeb();
    		ArrayList<ComboItem> gradeList = DropDownValues.getGradeList(conn);
    		for (int i = 0; i < gradeList.size(); i++) {
    			grade.addItem(gradeList.get(i));
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

    private void setDoRrList() {
    	Connection conn = null;
    	boolean destroyIt = false;
    	try{
    		doNo.removeAllItems();
    		doNo.getItemCount();
    		conn = DBConnectionPool.getConnectionFromPoolNonWeb();
    		ArrayList<ComboItem> doRrdList = DropDownValues.getDoRrList(conn);
    		for (int i = 0; i < doRrdList.size(); i++) {
    			doNo.addItem(doRrdList.get(i));
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

    private boolean InsertQCDetatl(Connection conn, int step_Id) throws Exception {
        boolean isInserted = false;
        tprQcDetail = new TPRQCDetail();
        tprQcDetail.setTprId(tprRecord.getTprId());
        tprQcDetail.setTpsId(step_Id);

//        tprQcDetail.setStatus(markQcVal);

        tprQcDetail.setUpdatedBy(TokenManager.userId);
        //tprBean.setUpdatedOn(new Date());
        tprQcDetail.setCreatedOn(new Date());
        isInserted = RFIDMasterDao.insert(conn, tprQcDetail,false);
        isInserted = RFIDMasterDao.insert(conn, tprQcDetail,true);
        return isInserted;
    }

    private void paperChallanNoActionPerformed() {
        setWhiteBackColor();
        // grossWt.setEnabled(true);
        transporter.setEnabled(true);
        transporter.setFocusable(true);
        if (vehicleName.isTextEditable() && Utils.isNull(vehicleName.getText())) {
            vehicleName.setTextBackground(UIConstant.focusPanelColor);
            vehicleName.requestFocusInWindow();
        } else {
            mines.requestFocusInWindow();
        }

        lrNo.setEditable(true);
        lrNo.setFocusable(true);
        //lrNo.setText("");
        lrNo.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        lrDate.setEnabled(true);
        challanDate.setEnabled(true);
        //challanDate.setDate(null);
        challanNo.setEditable(true);
        challanNo.setFocusable(true);
        //challanNo.setText("");
        challanNo.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        grade.setEnabled(true);
        mines.setEnabled(true);
        doNo.setEnabled(true);

        grossWt.setEditable(true);
        grossWt.setFocusable(true);
        grossWt.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        tareWt.setEditable(true);
        tareWt.setFocusable(true);
        tareWt.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        //saveAndOpen.setEnabled(false);
//        manualEntry.setEnabled(false);
        mines.requestFocusInWindow();
    }

    private void clearAction() {
    	Connection conn = null;
    	boolean destroyIt = false;
    	try{
    		conn = DBConnectionPool.getConnectionFromPoolNonWeb();
    		setWhiteBackColor();
    		clearInput(conn, true);
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

    private void paperChallanYesActionPerformed() {
        paperChallanPanel.setBackground(Color.WHITE);
        // grossWt.setEnabled(true);
        transporter.setEnabled(false);
        grossWt.requestFocusInWindow();
        lrNo.setEditable(false);
        lrNo.setFocusable(false);
        lrNo.setBorder(null);

        challanDate.setEnabled(false);
        //challanDate.setDate(null);
        challanNo.setEditable(false);
        challanNo.setFocusable(false);
        //challanNo.setText("");
        challanNo.setBorder(null);
        grade.setEnabled(false);
        mines.setEnabled(false);
        doNo.setEnabled(false);
        saveAndOpen.setEnabled(true);
        if(isManual)
        	manualEntryButton.setEnabled(true);
    }

    private void requestOverrideAction() {
    	Connection conn = null;
    	boolean destroyIt = false;
        try {
        	conn = DBConnectionPool.getConnectionFromPoolNonWeb();
            updateTPR(conn, true);
            int stepId = InsertTPRStep(conn,true);
            InsertTPRQuestionDetails(conn, stepId);
            conn.commit();
            clearInput(conn, false);
            getFocus();
        } catch(Exception ex){
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

    /*private void enableDenyEntry(boolean show) {
        if (show || vehicleBlackListed) {
            manualEntry.setEnabled(true);
            saveAndOpen.setEnabled(false);
//            manualEntry.requestFocusInWindow();
        } else {
            manualEntry.setEnabled(false);
            saveAndOpen.setEnabled(true);
//            saveAndOpen.requestFocusInWindow();
        }
    }*/
    private void enableDenyEntry(boolean show) {
        if (show || vehicleBlackListed) {
            /*manualEntryButton.setEnabled(true);
            quickCreate.setEnabled(false);*/
        	isRequestOverride = true;
        	saveAndOpen.setText("Request Override");
        } else {
        	isRequestOverride = false;
        	saveAndOpen.setText("Save And Open Gate");
            /*manualEntryButton.setEnabled(false);
            quickCreate.setEnabled(true);*/
        }
    }

    private String getDateValue(Object value) {
        String sdf = UIConstant.displayFormat.format(value);
        if (Utils.isNull(sdf)) {
            sdf = UIConstant.displayFormat.format(new Date());
        }
        return sdf;
    }

    private boolean isSpinnerDateNull(Object value) {
        String sdf = UIConstant.displayFormat.format(value);
        if (Utils.isNull(sdf)) {
            return true;
        }
        return false;
    }

    private int getIntFromDouble(String val) {
        int intVal = Misc.getUndefInt();
        if (!Utils.isNull(val)) {
            Double d = new Double(val);
            intVal = d.intValue();
        }
        return intVal;
    }

    private Date getDate() {
        Date dt = new Date();
        dt.setTime(dt.getTime() - 1000 * 60 * 30);
        return dt;

    }

    private void vehicleNameAction() {
    	Connection conn = null;
    	boolean destroyIt = false;
    	try{
    		conn = DBConnectionPool.getConnectionFromPoolNonWeb();
    		int vehId = Misc.getUndefInt();
    		if (!vehicleName.isTextEditable()) {
    			setWhiteBackColor();
    			JOptionPane.showMessageDialog(null, "Please Enter Vehicle !!!");
    			vehicleName.setTextBackground(UIConstant.focusPanelColor);
    			return;
    		} else if (Utils.isNull(vehicleName.getText())) {
    			JOptionPane.showMessageDialog(null, "Please Enter Vehicle !!!");
    			return;
    		} else {
    			Pair<Integer, String> vehPair = TPRInformation.getVehicle(conn, null, vehicleName.getText());
    			if (vehPair != null) {
    				vehId = vehPair.first;
    			}
    			if (vehId != Misc.getUndefInt()) {
    				isVehicleExist = true;
    			}
    			if (!isVehicleExist) {
    				JOptionPane.showMessageDialog(null, "Invalid Vehicle,please enter valid vehicle");
    				/*
    				Object[] options = {"  Re-Enter  ", "  Continue  "};
    				String msg = "Invalid Vehicle Please Go to Registration Office";
    				// new CheckVehicleDialog(new javax.swing.JFrame(), true);
    				int responseVehicleDialog = ConfirmationDialog.getDialogBox(new javax.swing.JFrame(), true, options, msg);
    				if (responseVehicleDialog == reEnter) {
    					setWhiteBackColor();
    					vehicleName.setText("");
    					vehicleName.setTextBackground(UIConstant.focusPanelColor);
    					return;
    				} else if (responseVehicleDialog == contiNue) {
    					setWhiteBackColor();
    					vehicleName.setText("");
    					vehicleName.setTextBackground(UIConstant.focusPanelColor);
    					return;

    				}
    				 */
    				} else {
    				setWhiteBackColor();
    				setTPRecord(vehicleName.getText());
    				//changeQuickCreateToSave();
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
    private void setTransporterList(Connection conn, int selectedIndex, int selectedDo) {
    	try{
    		if(tprRecord == null)
    			return;
    		transporter.removeAllItems();
    		transporter.getItemCount();
    		ArrayList<ComboItem> getTransporter = DropDownValues.getTransporterList(conn,TokenManager.materialCat, selectedDo);
    		for (int i = 0; i < getTransporter.size(); i++) {
    			ComboItem item = getTransporter.get(i);
    			transporter.addItem(item);
    			if (item.getValue() == selectedIndex) {
    				transporter.setSelectedIndex(i);
    			}
    		}
    	}catch(Exception ex){
    		ex.printStackTrace();
    	}
    }


    private void setGradeList(Connection conn, int selectedIndex, int selectedDo) {
    	try{
    		if(tprRecord == null)
    			return;
    		grade.removeAllItems();
    		ArrayList<Pair> getGradeList = DropDownValues.getGradeList(conn, selectedDo);
    		for (int i = 0; i < getGradeList.size(); i++) {
    			Pair<Integer, String> pairVal = getGradeList.get(i);
    			grade.addItem(new ComboItem(pairVal.first, pairVal.second));
    			if (selectedIndex == pairVal.first) {
    				grade.setSelectedIndex(i);
    			}
    		}
    	}catch(Exception ex){
    		ex.printStackTrace();
    	}
    }

    private void setDoRrList(Connection conn, int selectedDo, int minesIndex) {
    	try{
    		int selectedmines = minesIndex;
    		doNo.removeAllItems();
    		doList = DropDownValues.getDoRrNumber(conn, selectedmines);
    		for (int i = 0; i < doList.size(); i++) {
    			Pair<Integer, String> pairVal = doList.get(i);
    			doNo.addItem(new ComboItem(pairVal.first, pairVal.second));
    			if (selectedDo == pairVal.first) {
    				doNo.setSelectedIndex(i);
    			}
    		}
//    		doNo.addItem(new ComboItem(Misc.getUndefInt(), "Temp DO"));
    	}catch(Exception ex){
    		ex.printStackTrace();
    	}
    }
    private void manualEntryAction() {
        Connection conn = null;
        boolean destroyIt = false;
    	try{
    		conn = DBConnectionPool.getConnectionFromPoolNonWeb();
        	clearInput(conn, false);
        	if (!vehicleName.isTextEditable()) {
                toggleVehicle(true);
                vehicleName.requestFocusInWindow();
            } 
        	manualEntryButton.setEnabled(false);
        }catch(Exception ex){
    		ex.printStackTrace();
    	}finally{
    		try{
    			DBConnectionPool.returnConnectionToPoolNonWeb(conn, destroyIt);
    		}catch(Exception ex){
    			ex.printStackTrace();
    		}
    	}
    }
    private void setMarkForQcAndGPSVoilation(Connection conn,boolean createObject){
    	try{
    		if(tprRecord == null)
    			return;
    		if(gpv == null || createObject){
    			//gpv = GpsPlusViolations.getGpsPlusViolatins(conn, tprRecord.getVehicleId(), tprRecord.getTprId(), System.currentTimeMillis(), tprRecord.getMinesId(), tprRecord.getChallanDate() == null ? System.currentTimeMillis() : tprRecord.getChallanDate().getTime());
    			gpv = GpsPlusViolations.getGpsPlusViolatins(conn, tprRecord.getVehicleId(), tprRecord.getTprId(), System.currentTimeMillis(), DropDownValues.getComboSelectedVal(mines), 
    					//challanDate.getValue() == null ? System.currentTimeMillis() : ((Date)challanDate.getValue()).getTime()
    					challanDateTime
    					);
    		}
    		if(gpv == null)
    			return;
    		double unloadGross = Misc.getParamAsDouble(labelWeighment.getText());
    		if(unloadGross > 5000.0)
    			unloadGross = (unloadGross/1000);
    		double partyGross = Misc.getParamAsDouble(grossWt.getText());
    		if(partyGross > 5000.0)
    			partyGross = (partyGross/1000);
    		Pair<ResultEnum, String> getMarkQC = gpv.getMarkForQC(conn, unloadGross, partyGross);
    		markForQcReason = getMarkQC.second;
    		if (getMarkQC.first == ResultEnum.GREEN) {
                markForQC.setForeground(UIConstant.PanelDarkGreen);
                markForQC.setText(getMarkQC.second);
                markForQCVal = UIConstant.NO;
            } else if (getMarkQC.first == ResultEnum.RED) {
                markForQC.setForeground(UIConstant.noActionPanelColor);
                markForQC.setText(getMarkQC.second);
                markForQCVal = UIConstant.YES;
            } else {
                markForQC.setForeground(UIConstant.PanelYellow);
                markForQC.setText(getMarkQC.second);
                markForQCVal = UIConstant.NC;
            }
    		Pair<ResultEnum, ArrayList<String>> getGpsQCViolationDetails = gpv.getGpsQCViolationsDetailed(conn, Misc.getParamAsDouble(grossWt.getText()));
            if (getGpsQCViolationDetails.first == ResultEnum.GREEN) {
                listOfViolation.setForeground(UIConstant.PanelDarkGreen);
                for (String s : getGpsQCViolationDetails.second) {
                    listOfViolation.setText(s);
                }
            } else if (getGpsQCViolationDetails.first == ResultEnum.RED) {
                listOfViolation.setForeground(UIConstant.noActionPanelColor);
                for (String s : getGpsQCViolationDetails.second) {
                    listOfViolation.setText(s);
                }
            } else {
                listOfViolation.setForeground(UIConstant.PanelYellow);
                for (String s : getGpsQCViolationDetails.second) {
                    listOfViolation.setText(s);
                }
            }
            Pair<ResultEnum, String> gpsIsTracking = gpv.getGpsIsTracking(conn);
            if (gpsIsTracking.first == ResultEnum.GREEN) {
            	gpsStatus.setForeground(UIConstant.PanelDarkGreen);
            	gpsStatus.setText(gpsIsTracking.second);
                
            } else if (gpsIsTracking.first == ResultEnum.RED) {
            	gpsStatus.setForeground(UIConstant.noActionPanelColor);
            	gpsStatus.setText(gpsIsTracking.second);
                
            } else {
            	gpsStatus.setForeground(UIConstant.PanelYellow);
            	gpsStatus.setText(gpsIsTracking.second);
            }
            Pair<ResultEnum, String> gpsViolationStatus = gpv.getGpsQCViolationsSummary(conn);
            if (gpsViolationStatus.first == ResultEnum.GREEN) {
            	gpsViolation.setForeground(UIConstant.PanelDarkGreen);
            	gpsViolation.setText(gpsViolationStatus.second);
                
            } else if (gpsViolationStatus.first == ResultEnum.RED) {
            	gpsViolation.setForeground(UIConstant.noActionPanelColor);
            	gpsViolation.setText(gpsViolationStatus.second);
                
            } else {
            	gpsViolation.setForeground(UIConstant.PanelYellow);
            	gpsViolation.setText(gpsViolationStatus.second);
            }
            long srcTime = gpv.getSrcTime();
           // updateChallanDate(srcTime);
            if(tprRecord != null && tprRecord.getChallanDate() == null && !Misc.isUndef(srcTime) && challanDateTime != srcTime){
            	System.out.println("WB Challan Time Calculated:"+new Date(srcTime));
            	challanDate.setValue(new Date(srcTime));
            	lrDate.setValue(new Date(srcTime));
            	challanDateTime = srcTime;
            }
    	}catch(Exception ex){
    		ex.printStackTrace();
    	}
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
                saveAndOpen.requestFocusInWindow();
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
    private void setQuetionsBlocking(int questionId, int answerId){
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
    private void updateChallanDate(long srcTime){
    	try{
    		if(tprRecord != null && tprRecord.getChallanDate() == null && !Misc.isUndef(srcTime) ){
            	System.out.println("WB Challan Time Calculated:"+new Date(srcTime));
            	long manualChallanDatePart = Misc.getUndefInt();
            	long srcDatePart = Misc.getUndefInt();
            	long calculatedChallanDate = srcTime / (24*60*60*1000);
            	if(challanDate.getValue() != null){
            		manualChallanDatePart = (((Date)challanDate.getValue()).getTime() / (24*60*60*1000));
            		srcDatePart = srcTime / (24*60*60*1000);
            		if(manualChallanDatePart > srcDatePart)
            			calculatedChallanDate = (manualChallanDatePart*24*60*60*1000) - (60*1000) - 330*60*1000;
            		else
            			calculatedChallanDate = (manualChallanDatePart*24*60*60*1000) + (60*1000) - 330*60*1000;
            		if(calculatedChallanDate !=  ((Date)challanDate.getValue()).getTime()){
            			challanDateTime = calculatedChallanDate;
            			challanDate.setValue(new Date(calculatedChallanDate));
            			lrDate.setValue(new Date(calculatedChallanDate));
            		}
            	}
            	;
            }
    	}catch(Exception ex){
    		ex.printStackTrace();
    	}
    }
}
