/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ipssi.rfid.ui;

import com.ipssi.common.ds.rule.GpsPlusViolations;
import com.ipssi.gen.utils.CacheTrack;
import com.ipssi.gen.utils.DBConnectionPool;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.Pair;
import com.ipssi.gen.utils.Triple;
import com.ipssi.rfid.beans.ComboItem;
import com.ipssi.rfid.beans.ComboItemList;
import com.ipssi.rfid.beans.RFIDHolder;
import com.ipssi.rfid.beans.TPRQCDetail;
import com.ipssi.rfid.beans.TPRecord;
import com.ipssi.rfid.beans.TPStep;
import com.ipssi.rfid.beans.Token;
import com.ipssi.rfid.beans.Vehicle;
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
import com.ipssi.rfid.processor.TokenManager;
import com.ipssi.rfid.processor.Utils;
import com.ipssi.rfid.readers.RFIDDataHandler;
import com.ipssi.rfid.readers.TAGListener;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

/**
 *
 * @author Vi$ky
 */
public class FlyashWeighmentGross extends javax.swing.JFrame {

//    private Connection conn = null;
    boolean isVehicleExist = false;
    private Date entryTime = null;
    private Date exitTime = null;
    Token token = null;
    private TPRecord tprRecord = null;
    private TPStep tpStep = null;
    private boolean isTagRead = false;
    private boolean isTpRecordValid = false;
    private boolean vehicleBlackListed = false;
    private boolean isRequestOverride = false;
    private RFIDDataHandler rfidHandler = null;
    private int readerId = 0;
//    private Connection dbConnectionRFID = null;
    private int contiNue = 1;
    private int reEnter = 0;
    private double captureWeight = Misc.getUndefDouble();
    private TPRQCDetail tprQcDetail = null;
    private WeighBridge weighBridge = null;
    private AutoComplete auto_complete = null;
    private TPRBlockManager tprBlockManager = null;
    private Vehicle vehicleDetail = null;
//    private double Wb_Net_Wt = Misc.getUndefDouble();
    private DisconnectionDialog disconnectionDialog = new DisconnectionDialog("Weigh Bridge Disconnected please check connection.....");
    private ArrayList<Pair<Long, Integer>> readings = null;
    private boolean isManual = false;

    /**
     * captureWeight = Misc.getUndefDouble(); private TPRQCDetail tprQcDetail =
     * null; private WeighBridge weighBridge = null; Creates new form
     * FlyashWeighmentGross
     */
    public FlyashWeighmentGross() throws IOException {
        Connection conn = null;
        boolean destroyIt = false;
        try {
            initComponents();
            labelWeighment.setText(TokenManager.weight_val);
            //flyAshNetWt.setText("Hello");
            this.setExtendedState(this.getExtendedState() | this.MAXIMIZED_BOTH);
            this.setTitle(UIConstant.formTitle);
            conn = DBConnectionPool.getConnectionFromPoolNonWeb();
            DropDownValues.setTransporterList(transporter, conn, TokenManager.materialCat);
            DropDownValues.setConsignList(consignee, conn, 1);
            DropDownValues.setMaterialSubCategory(materialSubCat, conn, TokenManager.materialCat);
            getFocus();
            Clock.startClock("FlyAshWeighmentGross");
            if (TokenManager.isManualEntry.containsKey(TokenManager.currWorkStationId)) {
                int val = TokenManager.isManualEntry.get(TokenManager.currWorkStationId);
                if (val == 1) {
                    isManual = true;
                } else {
                    isManual = false;
                    manualButton.setEnabled(false);
                }
            } else {
                isManual = false;
                manualButton.setEnabled(false);
            }
            start();




            auto_complete = new AutoComplete(vehicleName);
            auto_complete.setKeyEvent(new AutoComplete.ComboKeyEvent() {
                @Override
                public void onKeyPress(KeyEvent evt) {
                    if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                        vehicleNameAction();
                    }

                }
            });
        } catch (Exception ex) {
            destroyIt = true;
            ex.printStackTrace();
        } finally {
            try {
                DBConnectionPool.returnConnectionToPoolNonWeb(conn, destroyIt);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public void start() throws IOException {

        if (rfidHandler == null) {
            rfidHandler = new RFIDDataHandler(1000, readerId, TokenManager.currWorkStationType, TokenManager.currWorkStationId, TokenManager.userId);
            rfidHandler.setTagListener(new TAGListener() {
                @Override
                public void manageTag(Connection conn, Token _token, TPRecord tpr, TPStep tps, TPRBlockManager _tprBlockManager) {
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
                    vehicleName.removeAllItems();
                    vehicleName.addItem(text);
                }

                @Override
                public void clearVehicleName() {
                    vehicleName.removeAllItems();
//                    vehicleName.addItem("NO VEHICLE DETECTED");
                }

                @Override
                public int promptMessage(String message, Object[] options) {
                    return ConfirmationDialog.getDialogBox(new javax.swing.JFrame(), true, options, message);
                }

                @Override
                public void clear(boolean clearToken, Connection conn) {
                    // TODO Auto-generated method stub
                    clearInputs(clearToken, conn);
                }

                @Override
                public int mergeData(long sessionId, String epc, RFIDHolder rfidHolder) {
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
                    System.out.println("[Flyash Gross Reading]:" + str);
                    int val = Misc.getParamAsInt(str);
                    if (!Misc.isUndef(val)) {
                        int currVal = Misc.getParamAsInt(labelWeighment.getText());
                        if (Misc.isUndef(currVal) || (currVal >= val ? currVal - val : val - currVal) >= 10 || val == 0) {
                            labelWeighment.setText(val + "");
                            double net_Wt = GateInDao.calculateNetWt(Misc.getParamAsDouble(labelWeighment.getText())/1000, tprRecord.getLoadTare());
                            //flyAshNetWt.setText(Misc.getPrintableDouble(net_Wt < 16.5 ? net_Wt : 16.5));
                            flyAshNetWt.setText(Misc.getPrintableDouble(net_Wt));
                        }
                        if (TokenManager.isDebugReadings && tprRecord != null && readings != null && (readings.size() <= 0 || readings.get(readings.size() - 1) == null || (readings.get(readings.size() - 1).second != val))) {
                            readings.add(new Pair<Long, Integer>(System.currentTimeMillis(), val));
                        }
                    }
                }

                @Override
                public void showDisconnection() {
                    // TODO Auto-generated method stub
                    java.awt.EventQueue.invokeLater(new Runnable() {
                        public void run() {
                            if (disconnectionDialog != null) {
                                disconnectionDialog.setVisible(true);
                            }
                        }
                    });

                }

                @Override
                public void removeDisconnection() {
                    // TODO Auto-generated method stub
                    java.awt.EventQueue.invokeLater(new Runnable() {
                        public void run() {
                            if (disconnectionDialog != null) {
                                disconnectionDialog.setVisible(false);
                            }
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
        }
    }

    public void setTPRecord(Connection conn, TPRecord tpr) throws IOException {
        System.out.println("######### Gate IN setTPRecord  ########");
        try {
            tprRecord = tpr;
            if (tprRecord != null) {
                if (true) {//blockStatus == Status.VALIDATION.NO_ISSUE) {// TPR Valid
                    vehicleDetail = (Vehicle) RFIDMasterDao.get(conn, Vehicle.class, tprRecord.getVehicleId());
                    
                    long total_days = DropDownValues.getDifferenceBwDate(vehicleDetail.getFlyashTareTime());
                    if(vehicleDetail != null && Misc.isUndef(vehicleDetail.getFlyashTare())){
                    	JOptionPane.showMessageDialog(null, "Please Capture Tare Weight for Next Trip, System Does not have any tare.");
                    }
                    else if (total_days > TokenManager.maxTareDays) {
                        JOptionPane.showMessageDialog(null, "Please Capture Tare Weight for Next Trip");
                    }
                    toggleVehicle(false);
                    isTpRecordValid = true;
                    isVehicleExist = true;
                    isTagRead = token != null ? token.isReadFromTag() : false;

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
                    if (Utils.isNull(tprRecord.getVehicleName())) {
                        vehicleName.removeAllItems();
                        vehicleName.addItem(tprRecord.getVehicleName());
                    } else {
                        vehicleName.removeAllItems();
                        vehicleName.addItem(tprRecord.getVehicleName());
                    }

                    if (tprRecord.getTransporterId() != Misc.getUndefInt() && tprRecord.getTransporterId() != 0) {
                        DropDownValues.setComboItem(transporter, tprRecord.getTransporterId());
                    } else {
                        transporter.setSelectedIndex(0);
                        transporter.setFocusable(true);
                        transporter.setEnabled(true);
                        transporter.requestFocusInWindow();
                        setWhiteBackColor();
                    }
                    tprIdLabel.setText("TPR-ID:"+tprRecord.getTprId());
                    DropDownValues.setComboItemList(consignee, tprRecord.getConsignee());

                    DropDownValues.setComboItem(transporter, tprRecord.getTransporterId());
//                    Pair<Integer, Integer> material_code = DropDownValues.getMaterialCode(tprRecord.getTransporterId());
//                    if (material_code != null) {
                    String materialName = com.ipssi.rfid.constant.Type.TPRMATERIAL.getStr(TokenManager.materialCat);//String materialName = TokenManager.materialCat == 1 ? "STONE" :TokenManager.materialCat == 2 ? "FLYASH" : TokenManager.materialCat == 3 ? "OTHERS" : TokenManager.materialCat == 0 ? "COAL" : "";
                    materialCode.setText(materialName);
//                    }
                    documentNo.setText(tprRecord.getMplReferenceDoc());
//                    consigneeName.setText(tprRecord.getConsigneeName());
                    consigneeDocNo.setText(tprRecord.getConsigneeRefDoc());
//                    materialDesc.setText(tprRecord.getMaterialNotesSecond());
                    mplNotes.setText(tprRecord.getConsignorNotes());// mpl notes
//                    consigneeAddress.setText(tprRecord.getConsigneeAddress());
                    consigneeNotes.setText(tprRecord.getConsigneeNotes());
                    if (tprRecord.getEarliestLoadGateInEntry() != null) {
                        flyashInTime.setText(UIConstant.displayFormat.format(tprRecord.getEarliestLoadGateInEntry()));
                    }
                    DropDownValues.setComboItem(materialSubCat, tprRecord.getMaterial_sub_cat_id());
                    
                    if (tprRecord.getLoadTare() == Misc.getUndefDouble()) {
                    	if(vehicleDetail.getFlyashTare() != Misc.getUndefDouble()){
//                    		tprRecord.setLoadTare(vehicleDetail.getFlyashTare());
                    		flyashTare.setText(Misc.getPrintableDouble(vehicleDetail.getFlyashTare()));
                    	}
                        if (tprRecord.getEarliestLoadWbInEntry() == null && vehicleDetail.getFlyashTareTime() != null) {
                            flyashTareTime.setText(UIConstant.displayFormat.format(vehicleDetail.getFlyashTareTime()));
                        }
                    }else{
                    	if(tprRecord.getLoadTare() != Misc.getUndefDouble())
                    		flyashTare.setText(Misc.getPrintableDouble(tprRecord.getLoadTare()));
                    	if (tprRecord.getEarliestLoadWbInEntry() != null) {
                             flyashTareTime.setText(UIConstant.displayFormat.format(tprRecord.getEarliestLoadWbInEntry()));
                         }
                    }

                    Pair<Long, String> pairVal = GpsPlusViolations.getLatestLocation(conn, tprRecord.getVehicleId());
                    if(pairVal != null){
                    	String location = pairVal.second == null ? "" : pairVal.second;
                    	gpsTime.setText(GateInDao.getLongToDatetime(pairVal.first));
                    	gpsLocation.setText(GateInDao.getString(location, 40));
                    }
                    double net_Wt = GateInDao.calculateNetWt(Misc.getParamAsDouble(labelWeighment.getText())/1000, tprRecord.getLoadTare());
                    flyAshNetWt.setText(Misc.getPrintableDouble(net_Wt));
                    setBlockingStatus();
                    saveButton.setEnabled(true);
                    scheduleClearScreenTimer();
//                    calculateNetWt();
                } else {
                   
                    System.out.println("Error No TPR found");
                    isTpRecordValid = false;
                    isVehicleExist = false;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void setBlockingStatus() {
        if (tprBlockManager == null) {
            enableDenyEntry(false);
            return;
        }
        try {
            int blockStatus = tprBlockManager.getBlockStatus();
            if (blockStatus == UIConstant.BLOCKED) {
                vehicleBlackListed = true;
                blocking_reason.setText(tprBlockManager.getBlockingReason());
                overrides.setText("BLOCKED");
            } else {
                vehicleBlackListed = false;
                overrides.setText("NOT_BLOCKED");
                blocking_reason.setText("");
            }
            enableDenyEntry(vehicleBlackListed);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">                          
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        tprIdLabel = new javax.swing.JLabel();
        username = new javax.swing.JLabel();
        button1 = new java.awt.Button();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        transporter = new javax.swing.JComboBox();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        consigneeDocNo = new javax.swing.JTextField();
        jLabel15 = new javax.swing.JLabel();
        documentNo = new javax.swing.JTextField();
        overrides = new javax.swing.JLabel();
        vehicleName = new javax.swing.JComboBox();
        materialCode = new javax.swing.JLabel();
        consignee = new javax.swing.JComboBox();
        materialSubCat = new javax.swing.JComboBox();
        jLabel4 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        gpsLocation = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        gpsTime = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        jLabel17 = new javax.swing.JLabel();
        consigneeNotes = new javax.swing.JTextField();
        jLabel18 = new javax.swing.JLabel();
        flyAshNetWt = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        mplNotes = new javax.swing.JTextField();
        panel1 = new java.awt.Panel();
        labelWeighment = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        flyashInTime = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        flyashTare = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        flyashTareTime = new javax.swing.JLabel();
        clearButton = new javax.swing.JButton();
        manualButton = new javax.swing.JButton();
        saveButton = new javax.swing.JButton();
        digitalClock = new javax.swing.JLabel();
        blocking_reason = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        this.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
            	GateInDao.forceSignut(TokenManager.userId,TokenManager.srcType,Integer.toString(TokenManager.systemId));
            }});
        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        jPanel3.setBackground(new java.awt.Color(255, 255, 255));

        jLabel7.setFont(UIConstant.subHeadingFont);
        jLabel7.setText("Ash Weighment (Gross)");

//        tprIdLabel.setText("TPR-ID:1234567890");
        tprIdLabel.setFont(new java.awt.Font("Segoe UI", 1, 14));
        
        username.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);

        button1.setLabel("Sign Out");
        button1.setBackground(new java.awt.Color(255, 255, 255));

        button1.setFocusable(false);

        button1.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N

        button1.setForeground(new java.awt.Color(0, 102, 153));
        button1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button1ActionPerformed(evt);
            }
        });

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/ipssi/rfid/ui/cover_01_right_top.gif"))); // NOI18N

        jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/ipssi/rfid/ui/intelliplanner.png"))); // NOI18N

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 232, javax.swing.GroupLayout.PREFERRED_SIZE)
//                .addGap(292, 292, 292)
                .addGap(64, 64, 64)
                .addComponent(tprIdLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 213, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(30, 30, 30)
                .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 285, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 85, Short.MAX_VALUE)
                .addComponent(username, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(6, 6, 6)
                .addComponent(button1, javax.swing.GroupLayout.PREFERRED_SIZE, 96, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel1)
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(button1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
            .addComponent(username, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, 54, Short.MAX_VALUE)
            .addComponent(jLabel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(tprIdLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        jPanel5.setBackground(new java.awt.Color(255, 255, 255));
        jPanel5.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel8.setFont(UIConstant.labelFont);
        jLabel8.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel8.setText("Vehicle:");
        jPanel5.add(jLabel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 178, 41));

        jLabel9.setFont(UIConstant.labelFont);
        jLabel9.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel9.setText("LR#:");//Consignee Ref Doc #:
        jPanel5.add(jLabel9, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 265, 178, 30));

        jLabel11.setFont(UIConstant.labelFont);
        jLabel11.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel11.setText("Consignee:");
        jPanel5.add(jLabel11, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 229, 178, 30));

        transporter.setFont(UIConstant.textFont);
        transporter.addItem(new ComboItem(0, "Select"));
        transporter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                transporterActionPerformed(evt);
            }
        });
        transporter.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                transporterKeyPressed(evt);
            }
        });
        jPanel5.add(transporter, new org.netbeans.lib.awtextra.AbsoluteConstraints(184, 47, 215, 30));

        jLabel12.setFont(UIConstant.labelFont);
        jLabel12.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel12.setText("Transporter:");
        jPanel5.add(jLabel12, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 47, 178, 30));

        jLabel13.setFont(UIConstant.labelFont);
        jLabel13.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel13.setText("Material Code:");
        jPanel5.add(jLabel13, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 121, 178, 30));

        jLabel14.setFont(UIConstant.labelFont);
        jLabel14.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel14.setText("Blocked:");
        jPanel5.add(jLabel14, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 83, 178, 30));

        consigneeDocNo.setFont(UIConstant.textFont);
        consigneeDocNo.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        consigneeDocNo.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                consigneeDocNoFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                consigneeDocNoFocusLost(evt);
            }
        });
        consigneeDocNo.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                consigneeDocNoKeyPressed(evt);
            }
        });
        jPanel5.add(consigneeDocNo, new org.netbeans.lib.awtextra.AbsoluteConstraints(184, 265, 215, 30));

        jLabel15.setFont(UIConstant.labelFont);
        jLabel15.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel15.setText("Order#:");
        //MPL Ref Document #
        jPanel5.add(jLabel15, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 193, 178, 30));

        documentNo.setFont(UIConstant.textFont);
        documentNo.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        documentNo.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                documentNoFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                documentNoFocusLost(evt);
            }
        });
        documentNo.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                documentNoKeyPressed(evt);
            }
        });
        jPanel5.add(documentNo, new org.netbeans.lib.awtextra.AbsoluteConstraints(184, 193, 215, 30));

        overrides.setFont(UIConstant.textFont);
        jPanel5.add(overrides, new org.netbeans.lib.awtextra.AbsoluteConstraints(184, 83, 215, 30));

        vehicleName.setFont(UIConstant.textFont);
        vehicleName.setAutoscrolls(true);
        vehicleName.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        vehicleName.setEnabled(false);
        vehicleName.setEditable(false);
        vehicleName.removeAllItems();
        vehicleName.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                vehicleNameFocusGained(evt);
            }
        });
        vehicleName.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                vehicleNameKeyPressed(evt);
            }
        });
        jPanel5.add(vehicleName, new org.netbeans.lib.awtextra.AbsoluteConstraints(184, 0, 249, 41));

        materialCode.setFont(UIConstant.textFont);
        jPanel5.add(materialCode, new org.netbeans.lib.awtextra.AbsoluteConstraints(184, 121, 215, 30));

        consignee.setFont(UIConstant.textFont);
        consignee.addItem(new ComboItemList(0,"Select",""));
        consignee.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                consigneeFocusLost(evt);
            }
        });
        consignee.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                consigneeKeyPressed(evt);
            }
        });
        jPanel5.add(consignee, new org.netbeans.lib.awtextra.AbsoluteConstraints(184, 229, 213, 30));

        materialSubCat.setFont(UIConstant.textFont);
        materialSubCat.addItem(new ComboItem(0, "Select"));
        materialSubCat.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                materialSubCatFocusGained(evt);
            }
        });
        materialSubCat.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                materialSubCatKeyPressed(evt);
            }
        });
        jPanel5.add(materialSubCat, new org.netbeans.lib.awtextra.AbsoluteConstraints(184, 157, 215, 30));

        jLabel4.setFont(UIConstant.labelFont);
        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel4.setText("Material Sub Category:");
        jPanel5.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 157, 180, 30));

        jLabel10.setFont(UIConstant.labelFont);
        jLabel10.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel10.setText("GPS Location:");
        jPanel5.add(jLabel10, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 301, 178, 30));

        gpsLocation.setFont(UIConstant.textFont);
        jPanel5.add(gpsLocation, new org.netbeans.lib.awtextra.AbsoluteConstraints(184, 301, 334, 30));

        jLabel16.setFont(UIConstant.labelFont);
        jLabel16.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel16.setText("GPS Time:");
        jPanel5.add(jLabel16, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 337, 178, 30));

        gpsTime.setFont(UIConstant.textFont);
        jPanel5.add(gpsTime, new org.netbeans.lib.awtextra.AbsoluteConstraints(184, 337, 334, 30));

        jPanel4.setBackground(new java.awt.Color(255, 255, 255));
        jPanel4.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel17.setFont(UIConstant.labelFont);
        jLabel17.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel17.setText("Net Wt:");
        jPanel4.add(jLabel17, new org.netbeans.lib.awtextra.AbsoluteConstraints(22, 200, 184, 30));

        consigneeNotes.setFont(UIConstant.textFont);
        consigneeNotes.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        consigneeNotes.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                consigneeNotesFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                consigneeNotesFocusLost(evt);
            }
        });
        consigneeNotes.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                consigneeNotesKeyPressed(evt);
            }
        });
        jPanel4.add(consigneeNotes, new org.netbeans.lib.awtextra.AbsoluteConstraints(210, 270, 217, 30));

        jLabel18.setFont(UIConstant.labelFont);
        jLabel18.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel18.setText("Consignee Notes:");
        jPanel4.add(jLabel18, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 270, 184, 30));

        flyAshNetWt.setFont(UIConstant.textFont);
        
        flyAshNetWt.setBorder(null);
//        materialDesc.addActionListener(new java.awt.event.ActionListener() {
//            public void actionPerformed(java.awt.event.ActionEvent evt) {
//                materialDescActionPerformed(evt);
//            }
//        });
        flyAshNetWt.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                materialDescFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                materialDescFocusLost(evt);
            }
        });
        flyAshNetWt.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                materialDescKeyPressed(evt);
            }
        });
        jPanel4.add(flyAshNetWt, new org.netbeans.lib.awtextra.AbsoluteConstraints(210, 202, 215, 27));

        jLabel19.setFont(UIConstant.labelFont);
        jLabel19.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel19.setText("MPL Notes:");
        jPanel4.add(jLabel19, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 230, 184, 30));

        mplNotes.setFont(UIConstant.textFont);
        mplNotes.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        mplNotes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mplNotesActionPerformed(evt);
            }
        });
        mplNotes.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                mplNotesFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                mplNotesFocusLost(evt);
            }
        });
        mplNotes.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                mplNotesKeyPressed(evt);
            }
        });
        jPanel4.add(mplNotes, new org.netbeans.lib.awtextra.AbsoluteConstraints(210, 237, 217, 27));

        panel1.setBackground(new java.awt.Color(0, 0, 0));

        labelWeighment.setBackground(new java.awt.Color(0, 0, 0));
        labelWeighment.setFont(UIConstant.headingFont);
        labelWeighment.setForeground(new java.awt.Color(255, 255, 255));
        labelWeighment.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        //        labelWeighment.setText("25100");

        javax.swing.GroupLayout panel1Layout = new javax.swing.GroupLayout(panel1);
        panel1.setLayout(panel1Layout);
        panel1Layout.setHorizontalGroup(
            panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(labelWeighment, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 366, Short.MAX_VALUE)
        );
        panel1Layout.setVerticalGroup(
            panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(labelWeighment, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        jPanel4.add(panel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(61, 0, -1, -1));

        jLabel3.setFont(UIConstant.labelFont);
        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel3.setText("Flyash In Time:");
        jPanel4.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(22, 175, 182, 30));

        flyashInTime.setFont(UIConstant.textFont);
        jPanel4.add(flyashInTime, new org.netbeans.lib.awtextra.AbsoluteConstraints(210, 175, 215, 27));

        jLabel5.setFont(UIConstant.labelFont);
        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel5.setText("Flyash Tare Weight:");
        jPanel4.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(22, 115, 182, 29));

        flyashTare.setFont(UIConstant.textFont);
        jPanel4.add(flyashTare, new org.netbeans.lib.awtextra.AbsoluteConstraints(210, 115, 217, 29));

        jLabel6.setFont(UIConstant.labelFont);
        jLabel6.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel6.setText("Flyash Tare Time:");
        jPanel4.add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(22, 150, 182, 26));

        flyashTareTime.setFont(UIConstant.textFont);
        jPanel4.add(flyashTareTime, new org.netbeans.lib.awtextra.AbsoluteConstraints(210, 150, 217, 26));

        clearButton.setFont(UIConstant.buttonFont);
        clearButton.setText("Clear");
        clearButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearButtonActionPerformed(evt);
            }
        });
        clearButton.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                clearButtonFocusGained(evt);
            }
        });
        clearButton.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                clearButtonKeyPressed(evt);
            }
        });

        manualButton.setFont(UIConstant.buttonFont);
        manualButton.setText("Manual Entry");
        manualButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                manualButtonActionPerformed(evt);
            }
        });
        manualButton.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                manualButtonFocusGained(evt);
            }
        });
        manualButton.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                manualButtonKeyPressed(evt);
            }
        });

        saveButton.setFont(UIConstant.buttonFont);
        saveButton.setText("Save And Open Gate");
        saveButton.setEnabled(false);
        saveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveButtonActionPerformed(evt);
            }
        });
        saveButton.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                saveButtonFocusGained(evt);
            }
        });
        saveButton.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                saveButtonKeyPressed(evt);
            }
        });

        digitalClock.setFont(UIConstant.textFont); // NOI18N

        digitalClock.setForeground(UIConstant.textFontColor);
        digitalClock.setFont(new java.awt.Font("Segoe UI", 1, 36));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGap(0, 14, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addComponent(clearButton, javax.swing.GroupLayout.PREFERRED_SIZE, 132, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(43, 43, 43)
                        .addComponent(manualButton, javax.swing.GroupLayout.PREFERRED_SIZE, 184, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(33, 33, 33)
                        .addComponent(saveButton, javax.swing.GroupLayout.PREFERRED_SIZE, 224, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(462, 462, 462))
                    .addComponent(jPanel3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(digitalClock, javax.swing.GroupLayout.PREFERRED_SIZE, 179, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(103, 103, 103)
                        .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(blocking_reason, javax.swing.GroupLayout.PREFERRED_SIZE, 1269, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(blocking_reason, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(16, 16, 16)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(41, 41, 41)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(saveButton, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(manualButton, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(clearButton, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 97, Short.MAX_VALUE)
                .addComponent(digitalClock, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(112, 112, 112))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 24, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>                        

    private void materialDescActionPerformed(java.awt.event.ActionEvent evt) {                                             
        // TODO add your handling code here:
    }                                            

    private void mplNotesActionPerformed(java.awt.event.ActionEvent evt) {                                         
        // TODO add your handling code here:
    }                                        

    private void button1ActionPerformed(java.awt.event.ActionEvent evt) {                                        
        stopRfid();
        GateInDao.forceSignut(TokenManager.userId,TokenManager.srcType,Integer.toString(TokenManager.systemId));
        this.dispose();
        new LoginWindow().setVisible(true);

    }                                       
    private void stopRfid() {
        if (rfidHandler != null) {
            rfidHandler.stop();
        }
    }
    private void transporterActionPerformed(java.awt.event.ActionEvent evt) {                                            
//        int selectedtransporter = DropDownValues.getComboSelectedVal(transporter);
//        
//        Pair<Integer, Integer> material_code = DropDownValues.getMaterialCode(selectedtransporter);
//        if (material_code != null && transporter.getSelectedIndex() != 0) {
//            String materialName = material_code.second == 1 ? "STONE" : material_code.second == 2 ? "FLYASH" : material_code.second == 3 ? "OTHERS" : "COAL";
//            materialCode.setText(materialName);
//        }else{
//            materialCode.setText("");
//        }
    }                                           

    private void transporterKeyPressed(java.awt.event.KeyEvent evt) {                                       
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            setWhiteBackColor();
            materialSubCat.requestFocusInWindow();
        }
    }                                      

    private void clearButtonActionPerformed(java.awt.event.ActionEvent evt) {                                            
        clearAction();
    }                                           

    private void documentNoKeyPressed(java.awt.event.KeyEvent evt) {                                      
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            setWhiteBackColor();
            consignee.requestFocusInWindow();
        }
    }                                     

    private void consigneeDocNoKeyPressed(java.awt.event.KeyEvent evt) {                                          
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            setWhiteBackColor();
            mplNotes.requestFocusInWindow();
        }
    }                                         

    private void materialDescKeyPressed(java.awt.event.KeyEvent evt) {                                        
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            setWhiteBackColor();
            mplNotes.requestFocusInWindow();
        }
    }                                       

    private void mplNotesKeyPressed(java.awt.event.KeyEvent evt) {                                    
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            setWhiteBackColor();
            consigneeNotes.requestFocusInWindow();
        }
    }                                   

    private void consigneeNotesKeyPressed(java.awt.event.KeyEvent evt) {                                          
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            setWhiteBackColor();
            setFocusOnButton();
        }
    }                                         

    private void saveButtonActionPerformed(java.awt.event.ActionEvent evt) {                                           
        saveButtonAction();
    }                                          

    private void documentNoFocusGained(java.awt.event.FocusEvent evt) {                                       
        setWhiteBackColor();
        documentNo.setBackground(UIConstant.focusPanelColor);
    }                                      

    private void consigneeDocNoFocusGained(java.awt.event.FocusEvent evt) {                                           
        setWhiteBackColor();
        consigneeDocNo.setBackground(UIConstant.focusPanelColor);        // TODO add your handling code here:
    }                                          

    private void materialDescFocusGained(java.awt.event.FocusEvent evt) {                                         
        setWhiteBackColor();
        flyAshNetWt.setBackground(UIConstant.focusPanelColor);        // TODO add your handling code here:
    }                                        

    private void mplNotesFocusGained(java.awt.event.FocusEvent evt) {                                     
        setWhiteBackColor();
        mplNotes.setBackground(UIConstant.focusPanelColor);
    }                                    

    private void consigneeNotesFocusGained(java.awt.event.FocusEvent evt) {                                           
        setWhiteBackColor();
        consigneeNotes.setBackground(UIConstant.focusPanelColor);
    }                                          

    private void documentNoFocusLost(java.awt.event.FocusEvent evt) {                                     
        setWhiteBackColor();        // TODO add your handling code here:
    }                                    

    private void consigneeNotesFocusLost(java.awt.event.FocusEvent evt) {                                         
        setWhiteBackColor();        // TODO add your handling code here:
    }                                        

    private void consigneeDocNoFocusLost(java.awt.event.FocusEvent evt) {                                         
        setWhiteBackColor();        // TODO add your handling code here:
    }                                        

    private void materialDescFocusLost(java.awt.event.FocusEvent evt) {                                       
        setWhiteBackColor();        // TODO add your handling code here:
    }                                      

    private void mplNotesFocusLost(java.awt.event.FocusEvent evt) {                                   
        setWhiteBackColor();        // TODO add your handling code here:
    }                                  

    private void clearButtonKeyPressed(java.awt.event.KeyEvent evt) {                                       
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            clearAction();
        }
    }                                      

    private void manualButtonKeyPressed(java.awt.event.KeyEvent evt) {                                        
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            manualButtonAction();
        }
    }                                       

    private void manualButtonActionPerformed(java.awt.event.ActionEvent evt) {                                             
        manualButtonAction();
    }                                            

    private void clearButtonFocusGained(java.awt.event.FocusEvent evt) {                                        
        setWhiteBackColor();        // TODO add your handling code here:
    }                                       

    private void manualButtonFocusGained(java.awt.event.FocusEvent evt) {                                         
        setWhiteBackColor();        // TODO add your handling code here:
    }                                        

    private void saveButtonFocusGained(java.awt.event.FocusEvent evt) {                                       
        setWhiteBackColor();        // TODO add your handling code here:
    }                                      

    private void saveButtonKeyPressed(java.awt.event.KeyEvent evt) {                                      
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            saveButtonAction();
        }        // TODO add your handling code here:
    }                                     

    private void vehicleNameKeyPressed(java.awt.event.KeyEvent evt) {                                       
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            vehicleNameAction();
        }
    }                                      

    private void vehicleNameFocusGained(java.awt.event.FocusEvent evt) {                                        
        setWhiteBackColor();
        vehicleName.setBackground(UIConstant.focusPanelColor);
    }                                       

    private void consigneeKeyPressed(java.awt.event.KeyEvent evt) {                                     
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {

            consigneeDocNo.requestFocusInWindow();

        }
    }                                    

    private void consigneeFocusLost(java.awt.event.FocusEvent evt) {                                    
//       String address = ((ComboItemList) consignee.getSelectedItem()).getAddress();
//       consigneeAddress.setText(address);
    }                                   

    private void materialSubCatFocusGained(java.awt.event.FocusEvent evt) {                                           
        setWhiteBackColor();
    }                                          

    private void materialSubCatKeyPressed(java.awt.event.KeyEvent evt) {                                          
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            documentNo.requestFocusInWindow();
        }
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
            java.util.logging.Logger.getLogger(FlyashWeighmentGross.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(FlyashWeighmentGross.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(FlyashWeighmentGross.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(FlyashWeighmentGross.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    new FlyashWeighmentGross().setVisible(true);
                } catch (IOException ex) {
                    Logger.getLogger(FlyashWeighmentGross.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
    }
    // Variables declaration - do not modify                     
    private javax.swing.JLabel blocking_reason;
    private java.awt.Button button1;
    private javax.swing.JButton clearButton;
    private javax.swing.JComboBox consignee;
    private javax.swing.JTextField consigneeDocNo;
    private javax.swing.JTextField consigneeNotes;
    public static javax.swing.JLabel digitalClock;
    private javax.swing.JTextField documentNo;
    private javax.swing.JLabel flyashInTime;
    private javax.swing.JLabel flyashTare;
    private javax.swing.JLabel flyashTareTime;
    private javax.swing.JLabel gpsLocation;
    private javax.swing.JLabel gpsTime;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel tprIdLabel;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JLabel labelWeighment;
    private javax.swing.JButton manualButton;
    private javax.swing.JLabel materialCode;
    private javax.swing.JLabel flyAshNetWt;
    private javax.swing.JComboBox materialSubCat;
    private javax.swing.JTextField mplNotes;
    private javax.swing.JLabel overrides;
    private java.awt.Panel panel1;
    private javax.swing.JButton saveButton;
    private javax.swing.JComboBox transporter;
    public static javax.swing.JLabel username;
    private javax.swing.JComboBox vehicleName;
    // End of variables declaration                   
	private Timer timer = null;

//    private void initConnection() {
//        try {
//            conn = DBConnectionPool.getConnectionFromPoolNonWeb();
//            dbConnectionRFID = DBConnectionPool.getConnectionFromPoolNonWeb();
//        } catch (GenericException ex) {
//            // Logger.getLogger(GateIn1st.class.getName()).log(Level.SEVERE, null, ex);
//            ex.printStackTrace();
//        }
//
//    }
    private void getFocus() {
        if (vehicleName.isEditable()) {
            vehicleName.requestFocusInWindow();
        } else {
            transporter.requestFocusInWindow();
        }
    }

    private void setWhiteBackColor() {
        vehicleName.setBackground(UIConstant.PanelWhite);
        documentNo.setBackground(UIConstant.PanelWhite);
//        consignee.setBackground(UIConstant.PanelWhite);
//        consigneeAddress.setBackground(UIConstant.PanelWhite);
        consigneeDocNo.setBackground(UIConstant.PanelWhite);
        consigneeNotes.setBackground(UIConstant.PanelWhite);
//        consigneeName.setBackground(UIConstant.PanelWhite);
        flyAshNetWt.setBackground(UIConstant.PanelWhite);
        mplNotes.setBackground(UIConstant.PanelWhite);
    }

    private void clearAction() {
        Connection conn = null;
        boolean destroyIt = false;
        try {
            conn = DBConnectionPool.getConnectionFromPoolNonWeb();
            setWhiteBackColor();
            clearInputs(true, conn);
            toggleVehicle(false);
            getFocus();
        } catch (Exception ex) {
            ex.printStackTrace();
            destroyIt = true;
        } finally {
            try {
                DBConnectionPool.returnConnectionToPoolNonWeb(conn, destroyIt);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void manualButtonAction() {
        Connection conn = null;
        boolean destroyIt = false;
        try {
            conn = DBConnectionPool.getConnectionFromPoolNonWeb();
            if (!vehicleName.isEditable()) {
                clearInputs(false, conn);
                toggleVehicle(true);
            } else {
                vehicleName.requestFocusInWindow();
            }
            manualButton.setEnabled(false);
        } catch (Exception ex) {
            ex.printStackTrace();
            destroyIt = true;
        } finally {
            try {
                DBConnectionPool.returnConnectionToPoolNonWeb(conn, destroyIt);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

    }

    private void toggleVehicle(boolean editable) {
        if (editable) {
            vehicleName.setEnabled(true);
            vehicleName.setEditable(true);
            vehicleName.removeAllItems();
            vehicleName.setFocusable(true);
            vehicleName.requestFocusInWindow();
        } else {
            vehicleName.setEnabled(false);
            vehicleName.setEditable(false);
            vehicleName.setFocusable(false);
            vehicleName.removeAllItems();
//            vehicleName.addItem("NO VEHICLE DETECTED");
        }
    }

    private void clearInputs(boolean clearToken, Connection conn) {
        if (clearToken) {
            TokenManager.clearWorkstation();
        } else {
            if (token != null) {
                TokenManager.returnToken(conn, token);
            }
        }
        GateInDao.stopClearScreenTimer(timer);
        tprIdLabel.setText("");
        flyashTareTime.setText("");
        flyashTare.setText("");
        materialSubCat.setSelectedIndex(0);
        flyashInTime.setText("");
        transporter.setSelectedIndex(0);
        materialCode.setText("");
        documentNo.setText("");
        consignee.setSelectedIndex(0);
//        consigneeAddress.setText("");
        consigneeDocNo.setText("");
        consigneeNotes.setText("");
//        consigneeName.setText("");
        flyAshNetWt.setText("");
//        Wb_Net_Wt = Misc.getUndefDouble();
        mplNotes.setText("");
        overrides.setText("");
        blocking_reason.setText("");
        gpsLocation.setText("");
        gpsTime.setText("");
        vehicleDetail = null;
        tprRecord = null;
        entryTime = null;
        exitTime = null;
        tpStep = null;
        token = null;
        isTagRead = false;
        vehicleBlackListed = false;
        isVehicleExist = false;
        isRequestOverride = false;
        captureWeight = Misc.getUndefDouble();
        tprQcDetail = null;
        weighBridge = null;

        tprBlockManager = null;
        saveButton.setEnabled(false);
        if (isManual) {
            manualButton.setEnabled(true);
        }
    }

    private void setFocusOnButton() {
        if (saveButton.isEnabled()) {
            saveButton.requestFocusInWindow();
        } else if (manualButton.isEnabled()) {
            manualButton.requestFocusInWindow();
        } else {
            clearButton.requestFocusInWindow();
        }
    }

    private void enableDenyEntry(boolean show) {
        if (show || vehicleBlackListed) {
            isRequestOverride = true;
            overrides.setText("BLOCKED");
            saveButton.setText("Request Override");
        } else {
            isRequestOverride = false;
            overrides.setText("NOT_BLOCKED");
            saveButton.setText("Save And Open Gate");
        }
    }

    private void vehicleNameAction() {
        Connection conn = null;
        boolean destroyIt = false;
        try {
            conn = DBConnectionPool.getConnectionFromPoolNonWeb();
            int vehId = Misc.getUndefInt();
            Pair<Integer, String> vehPair = null;
            if (!vehicleName.isEnabled()) {
                setWhiteBackColor();
                transporter.requestFocusInWindow();
                transporter.setBackground(UIConstant.focusPanelColor);
            } else if (vehicleName.getItemCount() == 0 && Utils.isNull(vehicleName.getEditor().getItem().toString())) {
                JOptionPane.showMessageDialog(null, "Please Enter Vehicle !!!");
                return;
                // vehicleName.setBackground(UIConstant.noActionPanelColor);
            } else {

                String vehName = null;
                try {
                    isVehicleExist = false;
                    String std_name = null;
                   if (vehicleName.isEditable() || vehicleName.getItemCount() == 0) {
                        std_name = vehicleName.getEditor().getItem().toString();
                    } else {
                        std_name = vehicleName.getSelectedItem().toString();
                    }

                    vehName = CacheTrack.standardizeName(std_name);
                    vehicleName.removeAllItems();
                    vehicleName.addItem(vehName);
                    vehPair = TPRInformation.getVehicle(conn, null, vehName);
                    if (vehPair != null) {
                        vehId = vehPair.first;
                    }
                    isVehicleExist = !Misc.isUndef(vehId);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                if (!isVehicleExist) {
                    Object[] options = {"  Re-Enter  ", "  Continue  "};
                    String msg = " Vehicle Not Exist ";
                    int responseVehicleDialog = ConfirmationDialog.getDialogBox(new java.awt.Frame(), true, options, msg);
                    if (responseVehicleDialog == reEnter) {
                        vehicleName.removeAllItems();
//                    vehicleName.setText("");
                        return;
                    } else if (responseVehicleDialog == contiNue) {
                        try {
                            GateInDao.InsertNewVehicle(conn, vehName, TokenManager.userId);
                            setTPRecord(vehName);
                            setWhiteBackColor();
                            transporter.setFocusable(true);
                            transporter.setSelectedIndex(0);
                            transporter.requestFocusInWindow();
                            transporter.setEnabled(true);
                            isVehicleExist = false;
                            isTagRead = false;
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }

                    }
                } else {
                    try {
                        setWhiteBackColor();
                        setTPRecord(vehName);
                        isTagRead = false;
                        if (transporter.isFocusable()) {
                            transporter.requestFocusInWindow();
                        } else {
                            transporter.setBackground(UIConstant.focusPanelColor);
                            transporter.requestFocusInWindow();
                        }
//                        transporter.requestFocusInWindow();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            destroyIt = true;
        } finally {
            try {
                DBConnectionPool.returnConnectionToPoolNonWeb(conn, destroyIt);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

     private void saveButtonAction() {
        Connection conn = null;
        boolean destroyIt = false;
        try {
            conn = DBConnectionPool.getConnectionFromPoolNonWeb();
            if (isRequestOverride) {
                requestOverrideAction();
                return;
            }
            if (vehicleName.isEnabled() && Utils.isNull(vehicleName.getSelectedItem().toString())) {
                JOptionPane.showMessageDialog(null, "Please Enter Vehicle Name");
                vehicleName.requestFocusInWindow();
                return;
            } else if (DropDownValues.isNull(transporter)) {//mines.getSelectedIndex() == 0) {
                JOptionPane.showMessageDialog(null, "Please Select Transporter");
                transporter.requestFocusInWindow();
                return;
            } else if (DropDownValues.isNull(materialSubCat)) {//mines.getSelectedIndex() == 0) {
                JOptionPane.showMessageDialog(null, "Please Select Material Sub Category");
                materialSubCat.requestFocusInWindow();
                return;
            } //        else if (DropDownValues.isNull(materialCode)) {//mines.getSelectedIndex() == 0) {
            //            JOptionPane.showMessageDialog(null, "Please Select Material Code");
            //            materialCode.requestFocusInWindow();
            //            return;
            //        }
//            else if (Utils.isNull(documentNo.getText())) {
//                JOptionPane.showMessageDialog(null, "Please Enter MPL Ref Doc No");
//                documentNo.requestFocusInWindow();
//                return;
//            } 
            else if (consignee.getSelectedItem().toString().equalsIgnoreCase("Select")) {//mines.getSelectedIndex() == 0) {
                JOptionPane.showMessageDialog(null, "Please Select consignee");
                consignee.requestFocusInWindow();
                return;
            }
            
            //            else if (Utils.isNull(consigneeName.getText())) {
            //                JOptionPane.showMessageDialog(null, "Please Enter Consignee Name");
            //                consigneeName.requestFocusInWindow();
            //                return;
            //            }
            else if (Utils.isNull(consigneeDocNo.getText())) {
                JOptionPane.showMessageDialog(null, "Please Enter LR No.");
                consigneeDocNo.requestFocusInWindow();
                return;
            }
//            else if (Utils.isNull(materialDesc.getText())) {
//                JOptionPane.showMessageDialog(null, "Please Enter Material Desc");
//                materialDesc.requestFocusInWindow();
//                return;
//            }
//            else if (Utils.isNull(mplNotes.getText())) {
//                JOptionPane.showMessageDialog(null, "Please Enter MPL Notes");
//                mplNotes.requestFocusInWindow();
//                return;
//            } 
            //            else if (Utils.isNull(consigneeAddress.getText())) {
            //                JOptionPane.showMessageDialog(null, "Please Enter Consignee Address");
            //                consigneeAddress.requestFocusInWindow();
            //                return;
            //            }
//            else if (Utils.isNull(consigneeNotes.getText())) {
//                JOptionPane.showMessageDialog(null, "Please Enter Consignee Notes");
//                consigneeNotes.requestFocusInWindow();
//                return;
//            }
            
            else {
            	boolean isLrNoExist = GateInDao.isLrExist(conn, tprRecord.getTprId(), consigneeDocNo.getText(),TokenManager.materialCat);
                if(isLrNoExist){
                	 JOptionPane.showMessageDialog(null, "Duplicate LR No");
                	return;
                }
                captureWeight = Misc.getParamAsDouble(labelWeighment.getText());
                if(captureWeight < TokenManager.min_weight || captureWeight > TokenManager.max_weight){
//                if(captureWeight < 15000.0 || captureWeight > 60000.0){
        			JOptionPane.showMessageDialog(null, "Captured Weight is not in limits (15.00-60.00 MT).Please capture properly");
        			return;
        		}else{
        			captureWeight = captureWeight/1000;
        		}
//                int responseVehicleDialog = JOptionPane.showConfirmDialog(this, "Vehicle Name: " + vehicleName.getSelectedItem().toString() + "\nTransporter: " + transporter.getSelectedItem().toString() + "\nCaptured Weight: " + Misc.printDouble(captureWeight/1000), UIConstant.dialogTitle, JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
                String[] options = {"Yes", "No"};

                int responseVehicleDialog = JOptionPane.showOptionDialog(new javax.swing.JFrame(),
        				"Vehicle Name: " + vehicleName.getSelectedItem().toString() + "\nTransporter: " + transporter.getSelectedItem().toString() + "\nCaptured Weight: " + Misc.printDouble(captureWeight,false),
                        "Confirm",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.INFORMATION_MESSAGE,
                        null, //do not use a custom Icon
                        options, //the titles of buttons
                        options[1]);
              System.out.print("##### Confirmation Value :#####" + responseVehicleDialog);
                if (responseVehicleDialog == 1 || responseVehicleDialog == -1) {
                    return;
                } else {
                	Triple<TPRecord, Integer, Boolean> tprTriplet = TPRInformation.getLatestNonWeb(conn, token.getVehicleId(), null, vehicleName.getSelectedItem().toString(), TokenManager.currWorkStationType, TokenManager.materialCat);//(conn, vehicleId, data, vehicleName, TokenManager.createNewTPR, workStationType);
    				TPRecord tpr = tprTriplet != null ? tprTriplet.first : null;
    				boolean repeatProcess = false;
    				if(tpr != null && TPRInformation.isGreaterThanEqualsProcessed(tpr, TokenManager.currWorkStationType, TokenManager.materialCat)/*tpr.getTprId() != tprRecord.getTprId()*/){
    					repeatProcess = true;
    					tprRecord = tpr;
    				}else{
    					repeatProcess = false;
    				}
                    try {
                        boolean isInsert = false;
                        boolean isUpdateTpr = false;
                        int stepId = Misc.getUndefInt();
                        
                        if(!repeatProcess)
                        	isUpdateTpr = updateTPR(conn);
                            
                        stepId = InsertTPRStep(conn, false,repeatProcess);

                        if (stepId != Misc.getUndefInt()) {
                            InsertQCDetail(conn, stepId);
                        }
                        GateInDao.insertReadings(conn, tprRecord.getTprId(), readings);
                        conn.commit();
                        if (true) {
                        	if(repeatProcess){
    	    					JOptionPane.showMessageDialog(null, "This Step has already been completed, Please Confirm next step from control room");
    	    				}else
    	    					JOptionPane.showMessageDialog(null, "Detail Saved");
                            String materialSubCatValue = DropDownValues.getComboSelectedText(materialSubCat).equalsIgnoreCase("Select") ? "" : DropDownValues.getComboSelectedText(materialSubCat);
                            if(TokenManager.Weightment_Printer_Connected == 1){
                            	new FlyAshGrossSlip(this, true, tprRecord,materialSubCatValue).setVisible(true);
                            }
                            Barrier.openEntryGate();
                            clearInputs(false, conn);
                            toggleVehicle(false);
                            getFocus();
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }

                }

            }
        } catch (Exception ex) {
            ex.printStackTrace();
            destroyIt = true;
        } finally {
            try {
                DBConnectionPool.returnConnectionToPoolNonWeb(conn, destroyIt);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
//        ComboItem combo = (ComboItem) transporter.getSelectedItem();
//        System.out.println("name: " + combo.getLabel() + "value: " + combo.getValue());
    }

    private void requestOverrideAction() {
        Connection conn = null;
        boolean destroyIt = false;
        try {
            conn = DBConnectionPool.getConnectionFromPoolNonWeb();
            updateTPR(conn, true);
            int stepId = InsertTPRStep(conn, true,false);
            conn.commit();
            clearInputs(false, conn);
            toggleVehicle(false);
            getFocus();
        } catch (Exception ex) {
            ex.printStackTrace();
            destroyIt = true;
        } finally {
            try {
                DBConnectionPool.returnConnectionToPoolNonWeb(conn, destroyIt);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private boolean updateTPR(Connection conn) throws Exception {
        return updateTPR(conn, false);
    }

        private boolean updateTPR(Connection conn, boolean isDeny) throws Exception {
        boolean isUpdate = false;
        if (!isDeny) {
            tprRecord.setTransporterId(DropDownValues.getComboSelectedVal(transporter));
//            tprRecord.setMaterialCodeId(DropDownValues.getComboSelectedVal(materialCode));
//            tprRecord.setConsignee(DropDownValues.getComboSelectedVal(consignee));
            tprRecord.setConsignee(((ComboItemList) consignee.getSelectedItem()).getValue());
            tprRecord.setMplReferenceDoc(documentNo.getText());
            
            tprRecord.setMaterial_sub_cat_id(DropDownValues.getComboSelectedVal(materialSubCat));

            tprRecord.setConsigneeName(((ComboItemList) consignee.getSelectedItem()).getLabel());
            tprRecord.setConsigneeRefDoc(consigneeDocNo.getText());
//            tprRecord.setMaterialNotesSecond(materialDesc.getText());
            tprRecord.setConsignorNotes(mplNotes.getText());// mpl notes
            tprRecord.setConsigneeAddress(((ComboItemList) consignee.getSelectedItem()).getAddress());
            tprRecord.setConsigneeNotes(consigneeNotes.getText());
            tprRecord.setLoadGross(captureWeight);
            tprRecord.setLoadTare(Misc.getParamAsDouble(flyashTare.getText()));
            tprRecord.setUpdatedOn(new Date());
            tprRecord.setPreStepType(TokenManager.currWorkStationType);
            tprRecord.setNextStepType(TokenManager.nextWorkStationType);
            tprRecord.setLoadFlyashGrossName(TokenManager.userName);
            tprRecord.setUpdatedBy(TokenManager.userId);
            if (tprRecord.getComboStart() == null) {
                tprRecord.setComboStart(new Date());
            }
            tprRecord.setPreStepDate(new Date());
            tprRecord.setComboEnd(new Date());
            if (TokenManager.closeTPR) {
                tprRecord.setTprStatus(Status.TPR.CLOSE);
                if (token.getEpcId() != null && token.getEpcId().length() >= 20 && rfidHandler != null) {
                    rfidHandler.clearData(Utils.HexStringToByteArray(token.getEpcId()), 5);
                }
            }
            tprRecord.setLatestLoadWbOutExit(new Date());
        }
        tprRecord.setEarliestLoadWbOutEntry(entryTime);
        TPRInformation.insertUpdateTpr(conn, tprRecord);
        if (tprBlockManager != null) {
            tprBlockManager.setTprBlockStatus(conn, tprRecord.getTprId(), TokenManager.userId);
        }
        isUpdate = true;
        return isUpdate;
    }

    private int InsertTPRStep(Connection conn, boolean isDeny,boolean repeatProcess) throws Exception {
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
            tpStep.setGrossWt(tprRecord.getLoadGross());
            if(repeatProcess)
            	tpStep.setSaveStatus(TPStep.REPEAT_PROCESS);
            else
            	tpStep.setSaveStatus(isDeny ? TPStep.REQUEST_OVERRIDE : TPStep.SAVE_AND_CONTINUE);
            RFIDMasterDao.insert(conn, tpStep, false);
            RFIDMasterDao.insert(conn, tpStep, true);
        } else {
            long currTimeServerMillis = System.currentTimeMillis();
            tpStep.setExitTime(new Date(currTimeServerMillis));
            tpStep.setUpdatedOn(new Date(currTimeServerMillis));
            tpStep.setMaterialCat(TokenManager.materialCat);
            if(repeatProcess)
            	tpStep.setSaveStatus(TPStep.REPEAT_PROCESS);
            else
            	tpStep.setSaveStatus(isDeny ? TPStep.REQUEST_OVERRIDE : TPStep.SAVE_AND_CONTINUE);
            tpStep.setHasValidRf(isTagRead ? 1 : 0);
            RFIDMasterDao.update(conn, tpStep, false);
            RFIDMasterDao.update(conn, tpStep, true);
        }
        return tpStep.getId();
    }

    private boolean InsertQCDetail(Connection conn, int step_Id) throws Exception {
        boolean isInserted = false;
        tprQcDetail = new TPRQCDetail();
        tprQcDetail.setTprId(tprRecord.getTprId());
        tprQcDetail.setTpsId(step_Id);
        tprQcDetail.setUpdatedBy(TokenManager.userId);
        //tprBean.setUpdatedOn(new Date());
        tprQcDetail.setCreatedOn(new Date());
        isInserted = RFIDMasterDao.insert(conn, tprQcDetail);
        return isInserted;
    }

//    private void calculateNetWt() {
//        if (tprRecord != null && tprRecord.getLoadTare() != Misc.getUndefDouble() && !Utils.isNull(labelWeighment.getText())) {
//            double mplGross = Double.valueOf(labelWeighment.getText());
//            if (!Misc.isUndef(mplGross)) {
//            	if(mplGross > 5000.0)
//            		mplGross = (mplGross/1000);
//            		System.out.print("calculateNetWt() : FlyAsh Tare: "+tprRecord.getLoadTare()+" FlyAsh Gross: "+mplGross);
//                	Wb_Net_Wt = mplGross - tprRecord.getLoadTare();
//        		
//            }
//        }
//    }
    public void scheduleClearScreenTimer(){
		 try{
			 timer    = new Timer();
			 timer.schedule( new TimerTask() {
				 public void run() {
					 System.out.println("Auto Refress Screen");
					 clearAction();
					 //timer.cancel();
				 }
			 }, TokenManager.screenClearInterval * 1000);
		 }catch (Exception e) {
			e.printStackTrace();
		}
	 }
}
