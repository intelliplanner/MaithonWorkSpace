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
import com.ipssi.rfid.beans.ComboItem;
import com.ipssi.rfid.beans.ComboItemList;
import com.ipssi.rfid.beans.RFIDHolder;
import com.ipssi.rfid.beans.TPRQCDetail;
import com.ipssi.rfid.beans.TPRecord;
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
import com.ipssi.rfid.processor.TPRInformation;
import com.ipssi.rfid.processor.TokenManager;
import com.ipssi.rfid.processor.Utils;
import com.ipssi.rfid.readers.RFIDDataHandler;
import com.ipssi.rfid.readers.TAGListener;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

/**
 *
 * @author Vi$ky
 */
public class FlyashWeighmentTare extends javax.swing.JFrame {

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
    private int contiNue = 1;
    private int reEnter = 0;
    private TPRQCDetail tprQcDetail = null;
    private double captureWeight = Misc.getUndefDouble();
    private WeighBridge weighBridge = null;
    private AutoComplete auto_complete = null;
    private TPRBlockManager tprBlockManager = null;
    private DisconnectionDialog disconnectionDialog = new DisconnectionDialog("Weigh Bridge Disconnected please check connection.....");
    private ArrayList<Pair<Long, Integer>> readings = null;
    private  boolean isManual = false;

    /**
     * Creates new form FlyashWeighmentTare
     */
    public FlyashWeighmentTare() throws IOException {
        Connection conn = null;
        boolean destroyIt = false;
        try {
            conn = DBConnectionPool.getConnectionFromPoolNonWeb();
            initComponents();
            labelWeighment.setText(TokenManager.weight_val);
            jLabel16.setVisible(false);
            materialDesc.setVisible(false);
            this.setExtendedState(this.getExtendedState() | this.MAXIMIZED_BOTH);
            this.setTitle(UIConstant.formTitle);
            DropDownValues.setTransporterList(transporter, conn, TokenManager.materialCat);
            DropDownValues.setConsignList(consignee, conn, 1);
            DropDownValues.setMaterialSubCategory(materialSubCat, conn, TokenManager.materialCat);
            getFocus();
            Clock.startClock("FlyAshWeighmentTare");
            if(TokenManager.isManualEntry.containsKey(TokenManager.currWorkStationId)){
                int val  = TokenManager.isManualEntry.get(TokenManager.currWorkStationId);
                if(val == 1){
                     isManual = true;
                }else {
                     isManual = false;
                     manualButton.setEnabled(false);
                }
            }else{
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
                    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                }
            });
        }
        rfidHandler.start();
        if (weighBridge == null) {
            weighBridge = new WeighBridge();
            weighBridge.setListener(new WeighBridgeListener() {
                @Override
                public void changeValue(String str) {
                    System.out.println("[Flyash Tare Reading]:" + str);
                    int val = Misc.getParamAsInt(str);
                    if (!Misc.isUndef(val)) {
                        int currVal = Misc.getParamAsInt(labelWeighment.getText());
                        if (Misc.isUndef(currVal) || (currVal >= val ? currVal - val : val - currVal) >= 10 || val == 0) {
                            labelWeighment.setText(val + "");
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
                    }else {
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

                    DropDownValues.setComboItemList(consignee, tprRecord.getConsignee());
                    String materialName = com.ipssi.rfid.constant.Type.TPRMATERIAL.getStr(TokenManager.materialCat);//String materialName = TokenManager.materialCat == 1 ? "STONE" : TokenManager.materialCat == 2 ? "FLYASH" : TokenManager.materialCat == 3 ? "OTHERS" :  TokenManager.materialCat == 0 ? "COAL" : "";
                    materialCode.setText(materialName);

                    Pair<Long, String> pairVal = GpsPlusViolations.getLatestLocation(conn, tprRecord.getVehicleId());
                    if(pairVal != null){
                    	String location = pairVal.second == null ? "" : pairVal.second;
                    	gpsTime.setText(GateInDao.getLongToDatetime(pairVal.first));
                    	gpsLocation.setText(GateInDao.getString(location, 40));
                    }

                    
                    setBlockingStatus();
                    saveButton.setEnabled(true);
//                    calculateNetWt();
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
            }
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
        jPanel2 = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        button1 = new java.awt.Button();
        username = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        transporter = new javax.swing.JComboBox();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        consigneeDocNo = new javax.swing.JTextField();
        jLabel14 = new javax.swing.JLabel();
        override = new javax.swing.JLabel();
        documentNo = new javax.swing.JTextField();
        vehicleName = new javax.swing.JComboBox();
        materialCode = new javax.swing.JLabel();
        consignee = new javax.swing.JComboBox();
        jLabel3 = new javax.swing.JLabel();
        materialSubCat = new javax.swing.JComboBox();
        jPanel3 = new javax.swing.JPanel();
        jLabel16 = new javax.swing.JLabel();
        consigneeNotes = new javax.swing.JTextField();
        jLabel17 = new javax.swing.JLabel();
        materialDesc = new javax.swing.JTextField();
        jLabel18 = new javax.swing.JLabel();
        mplNotes = new javax.swing.JTextField();
        panel1 = new java.awt.Panel();
        labelWeighment = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        gpsTime = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        gpsLocation = new javax.swing.JLabel();
        clearButton = new javax.swing.JButton();
        manualButton = new javax.swing.JButton();
        saveButton = new javax.swing.JButton();
        blocking_reason = new javax.swing.JLabel();
        digitalClock = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(java.awt.SystemColor.controlLtHighlight);

        jPanel2.setBackground(java.awt.SystemColor.controlLtHighlight);

        jLabel6.setFont(UIConstant.subHeadingFont);
        jLabel6.setText("Ash Weighment (Tare)");

        button1.setBackground(new java.awt.Color(255, 255, 255));

        button1.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N

        button1.setForeground(new java.awt.Color(0, 102, 153));
        button1.setFocusable(false);
        button1.setLabel("Sign Out");
        button1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button1ActionPerformed(evt);
            }
        });

        username.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/ipssi/rfid/ui/cover_01_right_top.gif"))); // NOI18N

        jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/ipssi/rfid/ui/intelliplanner.png"))); // NOI18N

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 267, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(85, 85, 85)
                .addComponent(username, javax.swing.GroupLayout.PREFERRED_SIZE, 174, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(button1, javax.swing.GroupLayout.PREFERRED_SIZE, 104, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel1))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, 49, Short.MAX_VALUE)
                    .addComponent(username, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(button1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addGap(0, 0, 0))
        );

        jPanel5.setBackground(new java.awt.Color(255, 255, 255));

        jLabel7.setFont(UIConstant.labelFont);
        jLabel7.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel7.setText("Vehicle:");

        jLabel8.setFont(UIConstant.labelFont);
        jLabel8.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel8.setText("LR#:");

        jLabel10.setFont(UIConstant.labelFont);
        jLabel10.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel10.setText("Consignee:");

        transporter.setFont(UIConstant.textFont);
        transporter.addItem(new ComboItem(0, "Select"));
        transporter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                transporterActionPerformed(evt);
            }
        });
        transporter.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                transporterFocusGained(evt);
            }
        });
        transporter.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                transporterKeyPressed(evt);
            }
        });

        jLabel11.setFont(UIConstant.labelFont);
        jLabel11.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel11.setText("Transporter:");

        jLabel12.setFont(UIConstant.labelFont);
        jLabel12.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel12.setText("Material Code:");

        jLabel13.setFont(UIConstant.labelFont);
        jLabel13.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel13.setText("Blocked:");

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

        jLabel14.setFont(UIConstant.labelFont);
        jLabel14.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel14.setText("Order#:");

        override.setFont(UIConstant.textFont);

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

        vehicleName.setFont(UIConstant.textFont);
        vehicleName.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        vehicleName.setEnabled(false);
        vehicleName.setEditable(false);
        vehicleName.removeAllItems();

        materialCode.setFont(UIConstant.textFont);

        consignee.setFont(UIConstant.textFont);
        consignee.addItem(new ComboItemList(0,"Select",""));
        consignee.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                consigneeActionPerformed(evt);
            }
        });
        consignee.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                consigneeFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                consigneeFocusLost(evt);
            }
        });
        consignee.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                consigneeKeyPressed(evt);
            }
        });

        jLabel3.setFont(UIConstant.labelFont);
        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel3.setText("Material Sub Category:");

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

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 226, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(4, 4, 4)
                .addComponent(vehicleName, javax.swing.GroupLayout.PREFERRED_SIZE, 249, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, 226, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(4, 4, 4)
                .addComponent(transporter, javax.swing.GroupLayout.PREFERRED_SIZE, 215, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, 226, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(4, 4, 4)
                .addComponent(override, javax.swing.GroupLayout.PREFERRED_SIZE, 215, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, 226, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(4, 4, 4)
                .addComponent(materialCode, javax.swing.GroupLayout.PREFERRED_SIZE, 215, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 216, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(4, 4, 4)
                        .addComponent(consignee, javax.swing.GroupLayout.PREFERRED_SIZE, 215, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 216, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(4, 4, 4)
                        .addComponent(consigneeDocNo, javax.swing.GroupLayout.PREFERRED_SIZE, 215, javax.swing.GroupLayout.PREFERRED_SIZE))))
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel14, javax.swing.GroupLayout.PREFERRED_SIZE, 226, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addGap(4, 4, 4)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(documentNo)
                    .addComponent(materialSubCat, 0, 215, Short.MAX_VALUE)))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(vehicleName, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(6, 6, 6)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(transporter, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(6, 6, 6)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(override, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(7, 7, 7)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGap(1, 1, 1)
                        .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(materialCode, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(materialSubCat, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel14, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(documentNo, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(6, 6, 6)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(consignee, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(consigneeDocNo, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        jPanel3.setBackground(new java.awt.Color(255, 255, 255));

        jLabel16.setFont(UIConstant.labelFont);
        jLabel16.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel16.setText("Material Description:");

        consigneeNotes.setFont(UIConstant.textFont);
        consigneeNotes.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        consigneeNotes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                consigneeNotesActionPerformed(evt);
            }
        });
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

        jLabel17.setFont(UIConstant.labelFont);
        jLabel17.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel17.setText("Consignee Notes:");

        materialDesc.setFont(UIConstant.textFont);
        materialDesc.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        materialDesc.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                materialDescActionPerformed(evt);
            }
        });
        materialDesc.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                materialDescFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                materialDescFocusLost(evt);
            }
        });
        materialDesc.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                materialDescKeyPressed(evt);
            }
        });

        jLabel18.setFont(UIConstant.labelFont);
        jLabel18.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel18.setText("MPL Notes:");

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
            public void keyReleased(java.awt.event.KeyEvent evt) {
                mplNotesKeyReleased(evt);
            }
        });

        panel1.setBackground(new java.awt.Color(0, 0, 0));

        labelWeighment.setBackground(new java.awt.Color(0, 0, 0));
        labelWeighment.setFont(UIConstant.headingFont);
        labelWeighment.setForeground(new java.awt.Color(255, 255, 255));
        labelWeighment.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        //        labelWeighment.setText("12500");

        javax.swing.GroupLayout panel1Layout = new javax.swing.GroupLayout(panel1);
        panel1.setLayout(panel1Layout);
        panel1Layout.setHorizontalGroup(
            panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panel1Layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(labelWeighment, javax.swing.GroupLayout.PREFERRED_SIZE, 334, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        panel1Layout.setVerticalGroup(
            panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(labelWeighment, javax.swing.GroupLayout.PREFERRED_SIZE, 86, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        jLabel4.setFont(UIConstant.labelFont);
        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel4.setText("GPS Location:");

        gpsTime.setFont(UIConstant.textFont);

        jLabel9.setFont(UIConstant.labelFont);
        jLabel9.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel9.setText("GPS Time:");

        gpsLocation.setFont(UIConstant.textFont);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(75, 75, 75)
                .addComponent(panel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addComponent(jLabel16, javax.swing.GroupLayout.PREFERRED_SIZE, 172, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(4, 4, 4)
                .addComponent(materialDesc, javax.swing.GroupLayout.PREFERRED_SIZE, 215, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addComponent(jLabel18, javax.swing.GroupLayout.PREFERRED_SIZE, 172, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(4, 4, 4)
                .addComponent(mplNotes, javax.swing.GroupLayout.PREFERRED_SIZE, 215, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel17, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 172, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, 180, Short.MAX_VALUE))
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(4, 4, 4)
                        .addComponent(consigneeNotes, javax.swing.GroupLayout.PREFERRED_SIZE, 215, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(gpsTime, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(gpsLocation, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(panel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(71, 71, 71)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel16, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(materialDesc, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(6, 6, 6)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel18, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(mplNotes, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel17, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(consigneeNotes, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, 30, Short.MAX_VALUE)
                    .addComponent(gpsLocation, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(gpsTime, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

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
            public void focusLost(java.awt.event.FocusEvent evt) {
                saveButtonFocusLost(evt);
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
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(192, 192, 192)
                                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(337, 337, 337)
                                .addComponent(clearButton, javax.swing.GroupLayout.PREFERRED_SIZE, 155, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(30, 30, 30)
                                .addComponent(manualButton, javax.swing.GroupLayout.PREFERRED_SIZE, 176, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(32, 32, 32)
                                .addComponent(saveButton, javax.swing.GroupLayout.PREFERRED_SIZE, 194, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 230, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(digitalClock, javax.swing.GroupLayout.PREFERRED_SIZE, 178, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(blocking_reason, javax.swing.GroupLayout.PREFERRED_SIZE, 1062, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(blocking_reason, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(48, 48, 48)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(clearButton, javax.swing.GroupLayout.DEFAULT_SIZE, 42, Short.MAX_VALUE)
                    .addComponent(manualButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(saveButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 66, Short.MAX_VALUE)
                .addComponent(digitalClock, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
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
        this.dispose();

        new LoginWindow().setVisible(true);

    }                                       

    private void clearButtonActionPerformed(java.awt.event.ActionEvent evt) {                                            
        clearAction();
    }                                           

    private void manualButtonActionPerformed(java.awt.event.ActionEvent evt) {                                             
        manualButtonAction();
    }                                            

    private void manualButtonKeyPressed(java.awt.event.KeyEvent evt) {                                        
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            manualButtonAction();
        }
    }                                       

    private void consigneeDocNoFocusGained(java.awt.event.FocusEvent evt) {                                           
        setWhiteBackColor();
        consigneeDocNo.setBackground(UIConstant.focusPanelColor);
    }                                          

    private void consigneeDocNoFocusLost(java.awt.event.FocusEvent evt) {                                         
        setWhiteBackColor();
    }                                        

    private void materialDescFocusGained(java.awt.event.FocusEvent evt) {                                         
        setWhiteBackColor();
        materialDesc.setBackground(UIConstant.focusPanelColor);
    }                                        

    private void materialDescFocusLost(java.awt.event.FocusEvent evt) {                                       
        setWhiteBackColor();
    }                                      

    private void mplNotesFocusGained(java.awt.event.FocusEvent evt) {                                     
        setWhiteBackColor();
        mplNotes.setBackground(UIConstant.focusPanelColor);
    }                                    

    private void mplNotesFocusLost(java.awt.event.FocusEvent evt) {                                   
        setWhiteBackColor();
    }                                  

    private void consigneeNotesActionPerformed(java.awt.event.ActionEvent evt) {                                               
        // TODO add your handling code here:
    }                                              

    private void consigneeNotesFocusGained(java.awt.event.FocusEvent evt) {                                           
        setWhiteBackColor();
        consigneeNotes.setBackground(UIConstant.focusPanelColor);
    }                                          

    private void consigneeNotesFocusLost(java.awt.event.FocusEvent evt) {                                         
        setWhiteBackColor();
    }                                        

    private void transporterKeyPressed(java.awt.event.KeyEvent evt) {                                       
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            setWhiteBackColor();
            materialSubCat.requestFocusInWindow();
        }
    }                                      

    private void consigneeNotesKeyPressed(java.awt.event.KeyEvent evt) {                                          
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            setWhiteBackColor();
            setFocusOnButton();
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

    private void mplNotesKeyReleased(java.awt.event.KeyEvent evt) {                                     
        // TODO add your handling code here:
    }                                    

    private void mplNotesKeyPressed(java.awt.event.KeyEvent evt) {                                    
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            setWhiteBackColor();
            consigneeNotes.requestFocusInWindow();
        }
    }                                   

    private void transporterFocusGained(java.awt.event.FocusEvent evt) {                                        
        setWhiteBackColor();        // TODO add your handling code here:
    }                                       

    private void clearButtonFocusGained(java.awt.event.FocusEvent evt) {                                        
        setWhiteBackColor();        // TODO add your handling code here:
    }                                       

    private void manualButtonFocusGained(java.awt.event.FocusEvent evt) {                                         
        setWhiteBackColor();        // TODO add your handling code here:
    }                                        

    private void saveButtonFocusLost(java.awt.event.FocusEvent evt) {                                     
        // TODO add your handling code here:
    }                                    

    private void saveButtonFocusGained(java.awt.event.FocusEvent evt) {                                       
        setWhiteBackColor();        // TODO add your handling code here:
    }                                      

    private void clearButtonKeyPressed(java.awt.event.KeyEvent evt) {                                       
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            clearAction();
        }      // TODO add your handling code here:
    }                                      

    private void saveButtonKeyPressed(java.awt.event.KeyEvent evt) {                                      
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            saveButtonAction();
        }        // TODO add your handling code here:
    }                                     

    private void saveButtonActionPerformed(java.awt.event.ActionEvent evt) {                                           
        saveButtonAction();        // TODO add your handling code here:
    }                                          

    private void documentNoFocusGained(java.awt.event.FocusEvent evt) {                                       
        setWhiteBackColor();
        documentNo.setBackground(UIConstant.focusPanelColor);
    }                                      

    private void documentNoFocusLost(java.awt.event.FocusEvent evt) {                                     
        setWhiteBackColor();
    }                                    

    private void documentNoKeyPressed(java.awt.event.KeyEvent evt) {                                      
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            consignee.requestFocusInWindow();
        }
    }                                     

    private void transporterActionPerformed(java.awt.event.ActionEvent evt) {                                            
//        int selectedtransporter = DropDownValues.getComboSelectedVal(transporter);
//
//        Pair<Integer, Integer> material_code = DropDownValues.getMaterialCode(selectedtransporter);
//        if (material_code != null && transporter.getSelectedIndex() != 0) {
//            String materialName = material_code.second == 1 ? "STONE" : material_code.second == 2 ? "FLYASH" : material_code.second == 3 ? "OTHERS" : "COAL";
//            materialCode.setText(materialName);
//        } else {
//            materialCode.setText("");
//        }
    }                                           

    private void consigneeActionPerformed(java.awt.event.ActionEvent evt) {                                          
//        if (consignee.getSelectedItem().toString().equalsIgnoreCase("Other")) {
//            consigneeTextChange(true);
//        } else {
//            consigneeTextChange(false);
//        }
    }                                         

    private void consigneeFocusGained(java.awt.event.FocusEvent evt) {                                      
//        setWhiteBackground();
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
            java.util.logging.Logger.getLogger(FlyashWeighmentTare.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(FlyashWeighmentTare.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(FlyashWeighmentTare.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(FlyashWeighmentTare.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    new FlyashWeighmentTare().setVisible(true);
                } catch (IOException ex) {
                    Logger.getLogger(FlyashWeighmentTare.class.getName()).log(Level.SEVERE, null, ex);
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
    private javax.swing.JLabel gpsLocation;
    private javax.swing.JLabel gpsTime;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JLabel labelWeighment;
    private javax.swing.JButton manualButton;
    private javax.swing.JLabel materialCode;
    private javax.swing.JTextField materialDesc;
    private javax.swing.JComboBox materialSubCat;
    private javax.swing.JTextField mplNotes;
    private javax.swing.JLabel override;
    private java.awt.Panel panel1;
    private javax.swing.JButton saveButton;
    private javax.swing.JComboBox transporter;
    public static javax.swing.JLabel username;
    private javax.swing.JComboBox vehicleName;
    // End of variables declaration                   

    private void setWhiteBackColor() {
        vehicleName.setBackground(UIConstant.PanelWhite);
//        consignee.setBackground(UIConstant.PanelWhite);
//        consigneeAddress.setBackground(UIConstant.PanelWhite);
        consigneeDocNo.setBackground(UIConstant.PanelWhite);
        consigneeNotes.setBackground(UIConstant.PanelWhite);
//        consigneeName.setBackground(UIConstant.PanelWhite);
        mplNotes.setBackground(UIConstant.PanelWhite);
        materialDesc.setBackground(UIConstant.PanelWhite);
        documentNo.setBackground(UIConstant.PanelWhite);
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

    private void clearInputs(boolean clearToken, Connection conn) {
        if (clearToken) {
            TokenManager.clearWorkstation();
        } else {
            if (token != null) {
                TokenManager.returnToken(conn, token);
            }
        }
        override.setText("");
        consignee.setSelectedIndex(0);
        materialSubCat.setSelectedIndex(0);
//        consigneeAddress.setText("");
        consigneeDocNo.setText("");
        consigneeNotes.setText("");
//        consigneeName.setText("");
//        materialDesc.setText("");
        mplNotes.setText("");

        documentNo.setText("");
        materialCode.setText("");
        transporter.setSelectedIndex(0);
        blocking_reason.setText("");
        
        gpsLocation.setText("");
        gpsTime.setText("");
        
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
        if(isManual)
            manualButton.setEnabled(true);
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
        //  transporter.setFocusable(true);
        // transporter.setEnabled(true);
    }

    private void getFocus() {
        if (vehicleName.isEditable()) {
            vehicleName.requestFocusInWindow();
        } else {
            transporter.requestFocusInWindow();
        }
    }

    private void enableDenyEntry(boolean show) {
        if (show || vehicleBlackListed) {
            isRequestOverride = true;
            override.setText("BLOCKED");
            saveButton.setText("Request Override");
        } else {
            isRequestOverride = false;
            override.setText("NOT_BLOCKED");
            saveButton.setText("Save And Open Gate");
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

    private void vehicleNameAction() {
        Connection conn = null;
        boolean destroyIt = false;
        try {
            conn = DBConnectionPool.getConnectionFromPoolNonWeb();
            int vehId = Misc.getUndefInt();
            Pair<Integer, String> vehPair = null;
            if (!vehicleName.isEditable()) {
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
//                    vehicleName.addItem("");
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

    private void setFocusOnButton() {
        if (saveButton.isEnabled()) {
            saveButton.requestFocusInWindow();
        } else if (manualButton.isEnabled()) {
            manualButton.requestFocusInWindow();
        } else {
            clearButton.requestFocusInWindow();
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
//                JOptionPane.showMessageDialog(null, "Please Enter Dispatch Permit No");
//                documentNo.requestFocusInWindow();
//                return;
//            }
            else if (consignee.getSelectedItem().toString().equalsIgnoreCase("Select")) {//mines.getSelectedIndex() == 0) {
                JOptionPane.showMessageDialog(null, "Please Select consignee");
                consignee.requestFocusInWindow();
                return;
            } //            else if (Utils.isNull(consigneeName.getText())) {
            //                JOptionPane.showMessageDialog(null, "Please Enter Consignee Name");
            //                consigneeName.requestFocusInWindow();
            //                return;
            //            }
//            else if (Utils.isNull(consigneeDocNo.getText())) {
//                JOptionPane.showMessageDialog(null, "Please Enter Consignee Doc No");
//                consigneeDocNo.requestFocusInWindow();
//                return;
//            }
            
            
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
            	if(!Utils.isNull(consigneeDocNo.getText())){
                	boolean isLrNoExist = GateInDao.isLrExist(conn, tprRecord.getTprId(), consigneeDocNo.getText(),TokenManager.materialCat);
                	if(isLrNoExist){
                		JOptionPane.showMessageDialog(null, "Duplicate LR No");
                		return;
                	}
                } 
                captureWeight = Misc.getParamAsDouble(labelWeighment.getText());
                if(captureWeight < 8000.0 || captureWeight > 14999.0){
        			JOptionPane.showMessageDialog(null, "Captured Weight is not in limits (8.00-14.99 MT).Please capture properly");
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
                    try {
                        boolean isInsert = false;
                        boolean isUpdateTpr = false;
                        int stepId = Misc.getUndefInt();

                        isUpdateTpr = updateTPR(conn);
                        if (isUpdateTpr) {
                            stepId = InsertTPRStep(conn, false);
                        }
                        if (stepId != Misc.getUndefInt()) {
                            InsertQCDetatl(conn, stepId);
//                        InsertTPRQuestionDetails(stepId);
                        }
                        updateVehicle(conn);
//                        TPRBlockStatusHelper.allowCurrentStep(conn, tprRecord != null ? tprRecord.getVehicleId() : Misc.getUndefInt(), tprRecord, Misc.getUndefInt(), TokenManager.currWorkStationType, TokenManager.userId, true, true);
                        GateInDao.insertReadings(conn, tprRecord.getTprId(), readings);
                        conn.commit();
                        if (true) {
                            JOptionPane.showMessageDialog(null, "Detail Saved");
                            String materialSubCatValue = DropDownValues.getComboSelectedText(materialSubCat).equalsIgnoreCase("Select") ? "" : DropDownValues.getComboSelectedText(materialSubCat);
                            if(TokenManager.Weightment_Printer_Connected == 1){
                            	new FlyAshGrossSlip(this, true, tprRecord, materialSubCatValue).setVisible(true);
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
            destroyIt = true;
            ex.printStackTrace();
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
            int stepId = InsertTPRStep(conn, true);
//            TPRBlockStatusHelper.allowCurrentStep(conn, tprRecord.getVehicleId(), tprRecord, TokenManager.currWorkStationId, TokenManager.currWorkStationType, TokenManager.userId, true, true);
            conn.commit();
            clearInputs(false, conn);
            toggleVehicle(false);
            getFocus();
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

    private boolean updateTPR(Connection conn) throws Exception {
        return updateTPR(conn, false);
    }

    private boolean updateTPR(Connection conn, boolean isDeny) throws Exception {
        
        boolean isUpdate = false;
        if (!isDeny) {
            tprRecord.setTransporterId(DropDownValues.getComboSelectedVal(transporter));
//            tprRecord.setMaterialCodeId(DropDownValues.getComboSelectedVal(materialCode));
            tprRecord.setMplReferenceDoc(documentNo.getText());
//            tprRecord.setConsignee(DropDownValues.getComboSelectedVal(consignee));
            tprRecord.setMaterial_sub_cat_id(DropDownValues.getComboSelectedVal(materialSubCat));
            tprRecord.setConsignee(((ComboItemList) consignee.getSelectedItem()).getValue());
            tprRecord.setConsigneeName(((ComboItemList) consignee.getSelectedItem()).getLabel());
            tprRecord.setConsigneeRefDoc(consigneeDocNo.getText());
//            tprRecord.setMaterialNotesFirst(materialDesc.getText());
            tprRecord.setConsignorNotes(mplNotes.getText());// mpl notes
            tprRecord.setConsigneeAddress(((ComboItemList) consignee.getSelectedItem()).getAddress());
            tprRecord.setConsigneeNotes(consigneeNotes.getText());
            tprRecord.setLoadTare(captureWeight);
            tprRecord.setUpdatedOn(new Date());
            tprRecord.setPreStepType(TokenManager.currWorkStationType);
            tprRecord.setNextStepType(TokenManager.nextWorkStationType);
            tprRecord.setLoadFlyashTareName(TokenManager.userName);
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
            tprRecord.setLatestLoadWbInExit(new Date());
        }
        tprRecord.setEarliestLoadWbInEntry(entryTime);
        TPRInformation.insertUpdateTpr(conn, tprRecord);
        if (tprBlockManager != null) {
            tprBlockManager.setTprBlockStatus(conn, tprRecord.getTprId(), TokenManager.userId);
        }
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
            tpStep.setHasValidRf(isTagRead ? 1 : 0);
            tpStep.setMaterialCat(TokenManager.materialCat);
            tpStep.setSaveStatus(isDeny ? TPStep.REQUEST_OVERRIDE : TPStep.SAVE_AND_CONTINUE);
            RFIDMasterDao.insert(conn, tpStep, false);
            RFIDMasterDao.insert(conn, tpStep, true);

        } else {
            long currTimeServerMillis = System.currentTimeMillis();
            tpStep.setExitTime(new Date(currTimeServerMillis));
            tpStep.setUpdatedOn(new Date(currTimeServerMillis));
            tpStep.setHasValidRf(isTagRead ? 1 : 0);
            tpStep.setMaterialCat(TokenManager.materialCat);
            tpStep.setSaveStatus(isDeny ? TPStep.REQUEST_OVERRIDE : TPStep.SAVE_AND_CONTINUE);
//            tpStep.setMarkForQc(markQcVal);

            tpStep.setHasValidRf(isTagRead ? 1 : 0);
            RFIDMasterDao.update(conn, tpStep, false);
            RFIDMasterDao.update(conn, tpStep, true);
        }
        return tpStep.getId();
    }

    private boolean InsertQCDetatl(Connection conn, int step_Id) throws Exception {
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
//                overrides.setText("BLOCKED");
            } else {
                vehicleBlackListed = false;
//                overrides.setText("NOT_BLOCKED");
                blocking_reason.setText("");
            }
            enableDenyEntry(vehicleBlackListed);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

 private void updateVehicle(Connection conn) {
        PreparedStatement ps = null;
        System.out.print("test");
        int parameterIndex = 1;
        String now = UIConstant.requireFormat.format(new Date());
//        Date now = new java.sql.Timestamp((new Date()).getTime());
        String query = "UPDATE vehicle SET flyash_tare = ?, flyash_tare_time = ? WHERE id = ?";
        if(entryTime != null){
        	now = UIConstant.requireFormat.format(entryTime);
        }
        
        try {
            ps = conn.prepareStatement(query);
            ps.setDouble(parameterIndex++, captureWeight);
            ps.setString( parameterIndex++, now);
            ps.setInt(parameterIndex++, tprRecord.getVehicleId());
            ps.executeUpdate();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (ps != null) {
                    ps.close();
                }
            } catch (SQLException ex) {
                Logger.getLogger(FlyashWeighmentTare.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }
//    private void calculateNetWt() {
//        if (tprRecord != null && tprRecord.getLoadGross() != Misc.getUndefDouble() && !Utils.isNull(labelWeighment.getText())) {
//            double mplTare = Double.valueOf(labelWeighment.getText());
//            if (!Misc.isUndef(mplTare)) {
//            	if(mplTare > 5000.0)
//            		mplTare = (mplTare/1000);
//            		System.out.print("calculateNetWt() : FlyAsh Tare: "+mplTare+" FlyAsh Gross: "+ tprRecord.getLoadGross());
//                	Wb_Net_Wt = (tprRecord.getLoadGross() / 1000) - mplTare;
//            }
//        }
//    }
}
