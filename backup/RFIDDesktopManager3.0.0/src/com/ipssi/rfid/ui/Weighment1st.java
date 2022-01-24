/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ipssi.rfid.ui;

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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerDateModel;

/**
 *
 * @author Vi$ky
 */
public class Weighment1st extends javax.swing.JFrame {

    private JSpinner.DateEditor de = null;
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
    private TPRBlockManager tprBlockManager = null;
    private int readerId = 0;
    private int contiNue = 1;
    private int reEnter = 0;
    private TPRQCDetail tprQcDetail = null;
    private double captureWeight = Misc.getUndefDouble();
    private int material_type = 1;
    private AutoComplete auto_complete = null;
    private WeighBridge weighBridge = null;
//    private double Wb_Net_Wt = Misc.getUndefDouble();
    private DisconnectionDialog disconnectionDialog = new DisconnectionDialog("Weigh Bridge Disconnected please check connection.....");
    private ArrayList<Pair<Long, Integer>> readings = null;
    private boolean isManual = false;

    /**
     * Creates new form Weighment1st
     */
    public Weighment1st() throws IOException {
        Connection conn = null;
        boolean destroyIt = false;
        try {
            conn = DBConnectionPool.getConnectionFromPoolNonWeb();
            initComponents();
            labelWeighment.setText(TokenManager.weight_val);
            jLabel23.setVisible(false);
            materialDescription.setVisible(false);
            this.setExtendedState(this.getExtendedState()
                    | Weighment1st.MAXIMIZED_BOTH);
            this.setTitle(UIConstant.formTitle);
            DropDownValues.setTransporterList(transporter, conn, TokenManager.materialCat);
            DropDownValues.setConsignList(consignor, conn, 0);
            DropDownValues.setConsignList(consignee, conn, 1);
            getFocus();
            Clock.startClock("FirstWeighment");
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
                    clearInputs(conn, clearToken);
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
                    System.out.println("[Weighment First Reading]:" + str);
                    int val = Misc.getParamAsInt(str);
                    if (!Misc.isUndef(val)) {
                        int currVal = Misc.getParamAsInt(labelWeighment.getText());
                        if (Misc.isUndef(currVal) || (currVal >= val ? currVal - val : val - currVal) >= 10 || val == 0) {
                            labelWeighment.setText(val + "");
//                            try {
//                                calculateNetWt();
//                            } catch (Exception ex) {
//                                ex.printStackTrace();
//                            }
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
                    DropDownValues.setComboItem(consignee, tprRecord.getConsignee());
                    DropDownValues.setComboItem(consignor, tprRecord.getConsignorId());

                    String materialName = com.ipssi.rfid.constant.Type.TPRMATERIAL.getStr(TokenManager.materialCat);
                    materialCode.setText(materialName);

                    if (Utils.isNull(tprRecord.getVehicleName())) {
                        vehicleName.removeAllItems();
                        vehicleName.addItem(tprRecord.getVehicleName());
                    } else {
                        vehicleName.removeAllItems();
                        vehicleName.addItem(tprRecord.getVehicleName());
                    }

                }
                setBlockingStatus();
                saveButton.setEnabled(true);
//                calculateNetWt();
            } else {
                /*if (!Utils.isNull(vehicle_name)) {
                 JOptionPane.showMessageDialog(null, "Invalid Vehicle Go to Registration");

                 }*/
                JOptionPane.showMessageDialog(null, "Invalid Vehicle Go to Registration");
                isTpRecordValid = false;
                isVehicleExist = false;
                //clearInputs();
                //    getFocus();

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
        jLabel1 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        username = new javax.swing.JLabel();
        button1 = new java.awt.Button();
        jLabel3 = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        lrNo = new javax.swing.JTextField();
        consignorRefDocument = new javax.swing.JTextField();
        jLabel15 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        consigneeRefDoc = new javax.swing.JTextField();
        jLabel18 = new javax.swing.JLabel();
        consignor = new javax.swing.JComboBox();
        jLabel20 = new javax.swing.JLabel();
        Date dt = new Date() ;
        dt.setTime(dt.getTime() - 1000 * 60 * 30 );
        SpinnerDateModel sm= new SpinnerDateModel(dt,null,null,Calendar.DATE);
        lrDate = new javax.swing.JSpinner(sm);
        transporter = new javax.swing.JComboBox();
        materialCode = new javax.swing.JLabel();
        vehicleName = new javax.swing.JComboBox();
        consignee = new javax.swing.JComboBox();
        jPanel6 = new javax.swing.JPanel();
        jLabel22 = new javax.swing.JLabel();
        consignorNotes = new javax.swing.JTextField();
        consigneeNotes = new javax.swing.JTextField();
        jLabel19 = new javax.swing.JLabel();
        jLabel23 = new javax.swing.JLabel();
        otherMaterialDesc = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        materialDescription = new javax.swing.JTextField();
        clearButton = new javax.swing.JButton();
        manualButton = new javax.swing.JButton();
        saveButton = new javax.swing.JButton();
        blocking_reason = new javax.swing.JLabel();
        digitalClock = new javax.swing.JLabel();
        panel1 = new java.awt.Panel();
        labelWeighment = new javax.swing.JLabel();
        consigneeText = new javax.swing.JTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        jPanel2.setBackground(java.awt.SystemColor.controlLtHighlight);

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/ipssi/rfid/ui/intelliplanner.png"))); // NOI18N

        jLabel6.setFont(UIConstant.subHeadingFont);
        jLabel6.setText("Other Weighment(Gross)");

        username.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);

        button1.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N

        button1.setForeground(new java.awt.Color(0, 102, 153));
        button1.setFocusable(false);
        button1.setLabel("Sign Out");
        button1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button1ActionPerformed(evt);
            }
        });

        jLabel3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/ipssi/rfid/ui/cover_01_right_top.gif"))); // NOI18N

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
                jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel2Layout.createSequentialGroup()
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(291, 291, 291)
                    .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 306, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                    .addComponent(username, javax.swing.GroupLayout.DEFAULT_SIZE, 242, Short.MAX_VALUE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(button1, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 151, javax.swing.GroupLayout.PREFERRED_SIZE))
            );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 54, Short.MAX_VALUE)
            .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(username, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(button1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        jPanel5.setBackground(new java.awt.Color(255, 255, 255));

        jLabel7.setFont(UIConstant.labelFont);
        jLabel7.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel7.setText("Vehicle:");

        jLabel8.setFont(UIConstant.labelFont);
        jLabel8.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel8.setText("Transporter:");

        jLabel9.setFont(UIConstant.labelFont);
        jLabel9.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel9.setText("LR Date:");

        jLabel10.setFont(UIConstant.labelFont);
        jLabel10.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel10.setText("LR #:");

        jLabel11.setFont(UIConstant.labelFont);
        jLabel11.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel11.setText("Material Code:");

        lrNo.setFont(UIConstant.textFont);
        lrNo.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        lrNo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                lrNoActionPerformed(evt);
            }
        });
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

        consignorRefDocument.setFont(UIConstant.textFont);
        consignorRefDocument.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        consignorRefDocument.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                consignorRefDocumentFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                consignorRefDocumentFocusLost(evt);
            }
        });
        consignorRefDocument.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                consignorRefDocumentKeyPressed(evt);
            }
        });

        jLabel15.setFont(UIConstant.labelFont);
        jLabel15.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel15.setText("Bill#:");//Consignor Ref Document 

        jLabel16.setFont(UIConstant.labelFont);
        jLabel16.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel16.setText("Consignee:");

        consigneeRefDoc.setFont(UIConstant.textFont);
        consigneeRefDoc.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        consigneeRefDoc.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                consigneeRefDocFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                consigneeRefDocFocusLost(evt);
            }
        });
        consigneeRefDoc.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                consigneeRefDocKeyPressed(evt);
            }
        });

        jLabel18.setFont(UIConstant.labelFont);
        jLabel18.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel18.setText("PO#:");//

        consignor.setFont(UIConstant.textFont);
        consignor.addItem(new ComboItemList(0,"Select",""));
        consignor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                consignorActionPerformed(evt);
            }
        });
        consignor.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                consignorFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                consignorFocusLost(evt);
            }
        });
        consignor.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                consignorKeyPressed(evt);
            }
        });

        jLabel20.setFont(UIConstant.labelFont);
        jLabel20.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel20.setText("Consignor:");

        de = new JSpinner.DateEditor(lrDate,"dd/MM/yyyy HH:mm");

        lrDate.setEditor(de);
        lrDate.setFont(UIConstant.textFont);
        lrDate.setForeground(UIConstant.textFontColor);
        lrDate.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                lrDateFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                lrDateFocusLost(evt);
            }
        });
        lrDate.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                lrDateKeyPressed(evt);
            }
        });

        transporter.setFont(UIConstant.textFont);
        transporter.addItem(new ComboItem(0,"Select"));
        transporter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                transporterActionPerformed(evt);
            }
        });
        transporter.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                transporterFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                transporterFocusLost(evt);
            }
        });
        transporter.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                transporterKeyPressed(evt);
            }
        });

        materialCode.setFont(UIConstant.textFont);

        vehicleName.setFont(UIConstant.textFont);
        vehicleName.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        vehicleName.setEnabled(false);
        vehicleName.setEditable(false);
        vehicleName.removeAllItems();

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

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 225, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(4, 4, 4)
                        .addComponent(vehicleName, javax.swing.GroupLayout.PREFERRED_SIZE, 216, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 225, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(5, 5, 5)
                        .addComponent(transporter, javax.swing.GroupLayout.PREFERRED_SIZE, 215, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 225, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(5, 5, 5)
                        .addComponent(lrNo, javax.swing.GroupLayout.PREFERRED_SIZE, 215, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 225, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(5, 5, 5)
                        .addComponent(lrDate, javax.swing.GroupLayout.PREFERRED_SIZE, 215, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, 225, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(6, 6, 6)
                        .addComponent(materialCode, javax.swing.GroupLayout.PREFERRED_SIZE, 214, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(jLabel20, javax.swing.GroupLayout.PREFERRED_SIZE, 225, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(6, 6, 6)
                        .addComponent(consignor, javax.swing.GroupLayout.PREFERRED_SIZE, 217, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(jLabel15, javax.swing.GroupLayout.PREFERRED_SIZE, 229, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(4, 4, 4)
                        .addComponent(consignorRefDocument, javax.swing.GroupLayout.PREFERRED_SIZE, 215, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(jLabel18, javax.swing.GroupLayout.PREFERRED_SIZE, 229, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(4, 4, 4)
                        .addComponent(consigneeRefDoc, javax.swing.GroupLayout.PREFERRED_SIZE, 215, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(jLabel16, javax.swing.GroupLayout.PREFERRED_SIZE, 229, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(consignee, javax.swing.GroupLayout.PREFERRED_SIZE, 217, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGap(2, 2, 2)
                        .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(vehicleName, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(6, 6, 6)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(transporter, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(6, 6, 6)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lrNo, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(6, 6, 6)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lrDate, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(6, 6, 6)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(materialCode, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(6, 6, 6)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel20, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(consignor, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel15, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(consignorRefDocument, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(6, 6, 6)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel16, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGap(1, 1, 1)
                        .addComponent(consignee, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel18, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(consigneeRefDoc, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        jPanel6.setBackground(new java.awt.Color(255, 255, 255));

        jLabel22.setFont(UIConstant.labelFont);
        jLabel22.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel22.setText("Consignor Notes:");

        consignorNotes.setFont(UIConstant.textFont);
        consignorNotes.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        consignorNotes.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                consignorNotesFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                consignorNotesFocusLost(evt);
            }
        });
        consignorNotes.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                consignorNotesKeyPressed(evt);
            }
        });

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

        jLabel19.setFont(UIConstant.labelFont);
        jLabel19.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel19.setText("Consignee Notes:");

        jLabel23.setFont(UIConstant.labelFont);
        jLabel23.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel23.setText("Material Description:");

        otherMaterialDesc.setFont(UIConstant.textFont);
        otherMaterialDesc.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        otherMaterialDesc.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                otherMaterialDescFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                otherMaterialDescFocusLost(evt);
            }
        });
        otherMaterialDesc.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                otherMaterialDescKeyPressed(evt);
            }
        });

        jLabel2.setFont(UIConstant.labelFont);
        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel2.setText("Other Material Name:");

        materialDescription.setFont(UIConstant.textFont);
        materialDescription.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        materialDescription.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                materialDescriptionFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                materialDescriptionFocusLost(evt);
            }
        });
        materialDescription.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                materialDescriptionKeyPressed(evt);
            }
        });

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addComponent(jLabel23, javax.swing.GroupLayout.PREFERRED_SIZE, 193, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(materialDescription, javax.swing.GroupLayout.PREFERRED_SIZE, 215, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 203, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(4, 4, 4)
                        .addComponent(otherMaterialDesc, javax.swing.GroupLayout.PREFERRED_SIZE, 215, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addComponent(jLabel22, javax.swing.GroupLayout.PREFERRED_SIZE, 193, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(4, 4, 4)
                        .addComponent(consignorNotes, javax.swing.GroupLayout.PREFERRED_SIZE, 215, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addGap(37, 37, 37)
                        .addComponent(jLabel19, javax.swing.GroupLayout.PREFERRED_SIZE, 166, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(4, 4, 4)
                        .addComponent(consigneeNotes, javax.swing.GroupLayout.PREFERRED_SIZE, 215, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(0, 0, Short.MAX_VALUE))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGap(17, 17, 17)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(otherMaterialDesc, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel23, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(materialDescription, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel22, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(consignorNotes, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel19, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(consigneeNotes, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        clearButton.setFont(UIConstant.buttonFont);
        clearButton.setText("Clear");
        clearButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearButtonActionPerformed(evt);
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
        saveButton.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                saveButtonKeyPressed(evt);
            }
        });

        digitalClock.setFont(UIConstant.textFont); // NOI18N
        digitalClock.setForeground(UIConstant.textFontColor);
        digitalClock.setFont(new java.awt.Font("Segoe UI", 1, 36));

        panel1.setBackground(new java.awt.Color(0, 0, 0));

        labelWeighment.setFont(UIConstant.headingFont);
        labelWeighment.setForeground(new java.awt.Color(255, 255, 255));
        labelWeighment.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
//        labelWeighment.setText("3100");
        javax.swing.GroupLayout panel1Layout = new javax.swing.GroupLayout(panel1);
        panel1.setLayout(panel1Layout);
        panel1Layout.setHorizontalGroup(
            panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panel1Layout.createSequentialGroup()
                .addComponent(labelWeighment, javax.swing.GroupLayout.PREFERRED_SIZE, 336, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        panel1Layout.setVerticalGroup(
            panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(labelWeighment, javax.swing.GroupLayout.DEFAULT_SIZE, 85, Short.MAX_VALUE)
        );

        consigneeText.setFont(UIConstant.textFont);
        consigneeText.setEditable(false);
        consigneeText.setBackground(new java.awt.Color(255, 255, 255));
        consigneeText.setBorder(null);
        consigneeText.setFocusable(false);
        consigneeText.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                consigneeTextFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                consigneeTextFocusLost(evt);
            }
        });
        consigneeText.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                consigneeTextKeyPressed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(blocking_reason, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(digitalClock, javax.swing.GroupLayout.PREFERRED_SIZE, 171, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(19, 19, 19))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(331, 331, 331)
                        .addComponent(clearButton, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(37, 37, 37)
                        .addComponent(manualButton, javax.swing.GroupLayout.PREFERRED_SIZE, 183, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(36, 36, 36)
                        .addComponent(saveButton, javax.swing.GroupLayout.PREFERRED_SIZE, 221, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(126, 126, 126)
                        .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(consigneeText, javax.swing.GroupLayout.PREFERRED_SIZE, 118, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(189, 189, 189)
                                .addComponent(panel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addContainerGap(153, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(1, 1, 1)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(blocking_reason, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(panel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(consigneeText, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(124, 124, 124))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(51, 51, 51)
                                .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(52, 52, 52)))
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(manualButton, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(saveButton, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(clearButton, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 19, Short.MAX_VALUE)
                .addComponent(digitalClock, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(34, 34, 34))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>                        

    private void lrNoKeyPressed(java.awt.event.KeyEvent evt) {                                
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            lrDate.requestFocusInWindow();
//            lrDate.getEditor().getCursor();
        }
    }                               

    private void lrDateKeyPressed(java.awt.event.KeyEvent evt) {                                  
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            consignor.requestFocusInWindow();
        }
    }                                 

    private void consignorRefDocumentKeyPressed(java.awt.event.KeyEvent evt) {                                                
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            consignee.requestFocusInWindow();
        }
    }                                               

    private void lrNoActionPerformed(java.awt.event.ActionEvent evt) {                                     
        // TODO add your handling code here:
    }                                    

    private void lrNoFocusGained(java.awt.event.FocusEvent evt) {                                 
        lrNo.setBackground(UIConstant.focusPanelColor);
    }                                

    private void lrDateFocusGained(java.awt.event.FocusEvent evt) {                                   
        lrDate.setBackground(UIConstant.focusPanelColor);
    }                                  

    private void consignorRefDocumentFocusGained(java.awt.event.FocusEvent evt) {                                                 
        consignorRefDocument.setBackground(UIConstant.focusPanelColor);
    }                                                

    private void consigneeRefDocFocusGained(java.awt.event.FocusEvent evt) {                                            
        consigneeRefDoc.setBackground(UIConstant.focusPanelColor);
    }                                           

    private void otherMaterialDescFocusGained(java.awt.event.FocusEvent evt) {                                              
        otherMaterialDesc.setBackground(UIConstant.focusPanelColor);        // TODO add your handling code here:
    }                                             

    private void consigneeRefDocKeyPressed(java.awt.event.KeyEvent evt) {                                           
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            otherMaterialDesc.requestFocusInWindow();
        }
    }                                          

    private void otherMaterialDescKeyPressed(java.awt.event.KeyEvent evt) {                                             
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            consignorNotes.requestFocusInWindow();
        }
    }                                            

    private void consignorNotesKeyPressed(java.awt.event.KeyEvent evt) {                                          
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            consigneeNotes.requestFocusInWindow();
        }
    }                                         

    private void consigneeNotesKeyPressed(java.awt.event.KeyEvent evt) {                                          
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            setFocusOnButton();
        }
    }                                         

    private void saveButtonActionPerformed(java.awt.event.ActionEvent evt) {                                           
        saveButtonAction();
    }                                          

    private void button1ActionPerformed(java.awt.event.ActionEvent evt) {                                        
        this.dispose();
        new LoginWindow().setVisible(true);
    }                                       

    private void transporterKeyPressed(java.awt.event.KeyEvent evt) {                                       
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            lrNo.requestFocusInWindow();
        }
    }                                      

    private void transporterFocusGained(java.awt.event.FocusEvent evt) {                                        
        setWhiteBackground();
    }                                       

    private void transporterFocusLost(java.awt.event.FocusEvent evt) {                                      
        setWhiteBackground();
    }                                     

    private void lrNoFocusLost(java.awt.event.FocusEvent evt) {                               
        setWhiteBackground();
    }                              

    private void lrDateFocusLost(java.awt.event.FocusEvent evt) {                                 
        setWhiteBackground();
    }                                

    private void consignorRefDocumentFocusLost(java.awt.event.FocusEvent evt) {                                               
        setWhiteBackground();        // TODO add your handling code here:
    }                                              

    private void consigneeRefDocFocusLost(java.awt.event.FocusEvent evt) {                                          
        setWhiteBackground();        // TODO add your handling code here:
    }                                         

    private void otherMaterialDescFocusLost(java.awt.event.FocusEvent evt) {                                            
        setWhiteBackground();
// TODO add your handling code here:
    }                                           

    private void consignorNotesFocusGained(java.awt.event.FocusEvent evt) {                                           
        consignorNotes.setBackground(UIConstant.focusPanelColor);        // TODO add your handling code here:
    }                                          

    private void consignorNotesFocusLost(java.awt.event.FocusEvent evt) {                                         
        setWhiteBackground();
// TODO add your handling code here:
    }                                        

    private void consigneeNotesFocusGained(java.awt.event.FocusEvent evt) {                                           
        consigneeNotes.setBackground(UIConstant.focusPanelColor);        // TODO add your handling code here:
    }                                          

    private void consigneeNotesFocusLost(java.awt.event.FocusEvent evt) {                                         
        setWhiteBackground();// TODO add your handling code here:
    }                                        

    private void clearButtonActionPerformed(java.awt.event.ActionEvent evt) {                                            
        clearAction();        // TODO add your handling code here:
    }                                           

    private void manualButtonActionPerformed(java.awt.event.ActionEvent evt) {                                             
        manualButtonAction();
    }                                            

    private void manualButtonKeyPressed(java.awt.event.KeyEvent evt) {                                        
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            manualButtonAction();
        }        // TODO add your handling code here:
    }                                       

    private void saveButtonKeyPressed(java.awt.event.KeyEvent evt) {                                      
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            saveButtonAction();
        }        // TODO add your handling code here:
    }                                     

    private void clearButtonKeyPressed(java.awt.event.KeyEvent evt) {                                       
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            clearAction();
        }        // TODO add your handling code here:
    }                                      

    private void consignorKeyPressed(java.awt.event.KeyEvent evt) {                                     
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            consignorRefDocument.requestFocusInWindow();
        }
    }                                    

    private void consignorFocusGained(java.awt.event.FocusEvent evt) {                                      
        setWhiteBackground();
    }                                     

    private void transporterActionPerformed(java.awt.event.ActionEvent evt) {                                            
//        int selectedtransporter = DropDownValues.getComboSelectedVal(transporter);
//        Pair<Integer, Integer> material_code = DropDownValues.getMaterialCode(selectedtransporter);
//        if (material_code != null && transporter.getSelectedIndex() != 0) {
//            String materialName = material_code.second == 1 ? "STONE" : material_code.second == 2 ? "FLYASH" : material_code.second == 3 ? "OTHERS" : "COAL";
//            materialCode.setText(materialName);
//        }else{
//            materialCode.setText("");
//        }
    }                                           

    private void consignorActionPerformed(java.awt.event.ActionEvent evt) {                                          
        // TODO add your handling code here:
    }                                         

    private void consigneeTextKeyPressed(java.awt.event.KeyEvent evt) {                                         
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            consigneeRefDoc.requestFocusInWindow();
        }
    }                                        

    private void consigneeTextFocusGained(java.awt.event.FocusEvent evt) {                                          
        consigneeText.setBackground(UIConstant.focusPanelColor);
    }                                         

    private void consigneeTextFocusLost(java.awt.event.FocusEvent evt) {                                        
        setWhiteBackground();
    }                                       

    private void materialDescriptionKeyPressed(java.awt.event.KeyEvent evt) {                                               
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            consignorNotes.requestFocusInWindow();
        }
    }                                              

    private void materialDescriptionFocusGained(java.awt.event.FocusEvent evt) {                                                
        materialDescription.setBackground(UIConstant.focusPanelColor);
    }                                               

    private void consigneeKeyPressed(java.awt.event.KeyEvent evt) {                                     
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            if (consignee.getSelectedItem().toString().equalsIgnoreCase("Other")) {
                consigneeText.requestFocusInWindow();
            } else {
                consigneeRefDoc.requestFocusInWindow();
            }
        }
    }                                    

    private void consigneeFocusLost(java.awt.event.FocusEvent evt) {                                    
        // TODO add your handling code here:
//        String address = ((ComboItemList) consignee.getSelectedItem()).getAddress();
//       consigneeAddress.setText(address);
    }                                   

    private void consigneeFocusGained(java.awt.event.FocusEvent evt) {                                      
        setWhiteBackground();
    }                                     

    private void consigneeActionPerformed(java.awt.event.ActionEvent evt) {                                          
    }                                         

    private void consignorFocusLost(java.awt.event.FocusEvent evt) {                                    
//       String address = ((ComboItemList) consignor.getSelectedItem()).getAddress();
//       consignorAddress.setText(address);
    }                                   

    private void materialDescriptionFocusLost(java.awt.event.FocusEvent evt) {                                              
        setWhiteBackground();
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
//        try {
//            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
//                if ("Nimbus".equals(info.getName())) {
//                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
//                    break;
//                }
//            }
//        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
//            java.util.logging.Logger.getLogger(Weighment1st.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    new Weighment1st().setVisible(true);
                } catch (IOException ex) {
                    Logger.getLogger(Weighment1st.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
    }
    // Variables declaration - do not modify                     
    private javax.swing.JLabel blocking_reason;
    private java.awt.Button button1;
    private javax.swing.JButton clearButton;
    private javax.swing.JComboBox consignee;
    private javax.swing.JTextField consigneeNotes;
    private javax.swing.JTextField consigneeRefDoc;
    private javax.swing.JTextField consigneeText;
    private javax.swing.JComboBox consignor;
    private javax.swing.JTextField consignorNotes;
    private javax.swing.JTextField consignorRefDocument;
    public static javax.swing.JLabel digitalClock;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JLabel labelWeighment;
    private javax.swing.JSpinner lrDate;
    private javax.swing.JTextField lrNo;
    private javax.swing.JButton manualButton;
    private javax.swing.JLabel materialCode;
    private javax.swing.JTextField materialDescription;
    private javax.swing.JTextField otherMaterialDesc;
    private java.awt.Panel panel1;
    private javax.swing.JButton saveButton;
    private javax.swing.JComboBox transporter;
    public static javax.swing.JLabel username;
    private javax.swing.JComboBox vehicleName;
    // End of variables declaration                   

    private void getFocus() {
        if (vehicleName.isEditable()) {
            vehicleName.requestFocusInWindow();
        } else {
            transporter.requestFocusInWindow();
        }
    }

    private void setWhiteBackground() {
        vehicleName.setBackground(UIConstant.PanelWhite);
        lrNo.setBackground(UIConstant.PanelWhite);
//        consigneeName.setBackground(UIConstant.PanelWhite);
        consignorRefDocument.setBackground(UIConstant.PanelWhite);
//        consignorName.setBackground(UIConstant.PanelWhite);
        consigneeRefDoc.setBackground(UIConstant.PanelWhite);
        otherMaterialDesc.setBackground(UIConstant.PanelWhite);
//        consignorAddress.setBackground(UIConstant.PanelWhite);
//        consigneeAddress.setBackground(UIConstant.PanelWhite);
        consignorNotes.setBackground(UIConstant.PanelWhite);
        consigneeNotes.setBackground(UIConstant.PanelWhite);
        consigneeText.setBackground(UIConstant.PanelWhite);
        materialDescription.setBackground(UIConstant.PanelWhite);
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

//    private void clearAction() {
//        setWhiteBackground();
//        clearInputs(true);
//        toggleVehicle(false);
//        getFocus();
//    }
    private void clearAction() {
        Connection conn = null;
        boolean destroyIt = false;
        try {
            conn = DBConnectionPool.getConnectionFromPoolNonWeb();
            setWhiteBackground();
            clearInputs(conn, true);
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

    private void clearInputs(Connection conn, boolean clearToken) {
        if (clearToken) {
            TokenManager.clearWorkstation();
        } else {
            if (token != null) {
                TokenManager.returnToken(conn, token);
            }
        }

        transporter.setSelectedIndex(0);
        materialCode.setText("");
        consignee.setSelectedIndex(0);
        consignor.setSelectedIndex(0);
        
        lrNo.setText("");
//        consigneeName.setText("");
        consignorRefDocument.setText("");
//        consignorName.setText("");
        consigneeRefDoc.setText("");
        otherMaterialDesc.setText("");
//        consignorAddress.setText("");
//        consigneeAddress.setText("");
        consignorNotes.setText("");
        consigneeNotes.setText("");
        consigneeTextChange(false);
        blocking_reason.setText("");
//        materialDescription.setText("");
//        Wb_Net_Wt = Misc.getUndefDouble();
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

        saveButton.setEnabled(false);
        if(isManual)
        manualButton.setEnabled(true);
    }

    private void toggleVehicle(boolean editable) {
        if (editable) {
            vehicleName.setEditable(true);
            vehicleName.setEnabled(true);
            vehicleName.setFocusable(true);
            vehicleName.setBackground(UIConstant.focusPanelColor);
            vehicleName.removeAllItems();
//            vehicleName.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
            vehicleName.requestFocusInWindow();
        } else {
            vehicleName.setFocusable(false);
            vehicleName.setEditable(false);
            vehicleName.setEnabled(false);
//              vehicleName.setBorder(null);
            vehicleName.removeAllItems();
//            vehicleName.addItem("NO VEHICLE DETECTED");
        }
    }

    private void enableDenyEntry(boolean show) {
        if (show || vehicleBlackListed) {
            isRequestOverride = true;
//            overrides.setText("BLOCKED");
            saveButton.setText("Request Override");
        } else {
            isRequestOverride = false;
//            overrides.setText("NOT_BLOCKED");
            saveButton.setText("Save And Open Gate");
        }
    }

    private void manualButtonAction() {
        Connection conn = null;
        boolean destroyIt = false;
        try {
            conn = DBConnectionPool.getConnectionFromPoolNonWeb();
            if (!vehicleName.isEditable()) {
                clearInputs(conn, false);
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
            } else if (transporter.getSelectedItem().toString().equalsIgnoreCase("Select")) {//mines.getSelectedIndex() == 0) {
                JOptionPane.showMessageDialog(null, "Please Select Transporter");
                transporter.requestFocusInWindow();
                return;
            }else if (isSpinnerDateNull(lrDate.getValue())) {
                JOptionPane.showMessageDialog(null, "Please Select Challan Date");
                lrDate.requestFocusInWindow();
                return;
            } 
          
             //        else if (DropDownValues.isNull(materialCode)) {//mines.getSelectedIndex() == 0) {
            //            JOptionPane.showMessageDialog(null, "Please Select Material Code");
            //            materialCode.requestFocusInWindow();
            //            return;
            //        }
            else if (consignor.getSelectedItem().toString().equalsIgnoreCase("Select")) {//mines.getSelectedIndex() == 0) {
                JOptionPane.showMessageDialog(null, "Please Select Consignor");
                consignor.requestFocusInWindow();
                return;
            } //            else if (Utils.isNull(consignorName.getText())) {
            //                JOptionPane.showMessageDialog(null, "Please Enter Consignor Name");
            //                consignorName.requestFocusInWindow();
            //                return;
            //            } 
            else if (Utils.isNull(consignorRefDocument.getText())) {
                JOptionPane.showMessageDialog(null, "Please Enter consignorRefDocument");
                consignorRefDocument.requestFocusInWindow();
                return;
            }
            else if (consignee.getSelectedItem().toString().equalsIgnoreCase("Select")) {//mines.getSelectedIndex() == 0) {
                JOptionPane.showMessageDialog(null, "Please Select consignee");
                consignee.requestFocusInWindow();
                return;
            } //            else if (Utils.isNull(consigneeName.getText())) {
            //                JOptionPane.showMessageDialog(null, "Please Enter consigneeName");
            //                consigneeName.requestFocusInWindow();
            //                return;
            //            }
//            else if (Utils.isNull(consigneeRefDoc.getText())) {
//                JOptionPane.showMessageDialog(null, "Please Enter consigneeRefDoc");
//                consigneeRefDoc.requestFocusInWindow();
//                return;
//            } 
            else if (Utils.isNull(otherMaterialDesc.getText())) {
                JOptionPane.showMessageDialog(null, "Please Enter Material Description");
                otherMaterialDesc.requestFocusInWindow();
                return;
            } //            else if (Utils.isNull(consignorAddress.getText())) {
            //                JOptionPane.showMessageDialog(null, "Please Enter Consignor Address");
            //                consignorAddress.requestFocusInWindow();
            //                return;
            //            }
//            else if (Utils.isNull(consignorNotes.getText())) {
//                JOptionPane.showMessageDialog(null, "Please Enter Consignor Notes");
//                consignorNotes.requestFocusInWindow();
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
            	if (!Utils.isNull(lrNo.getText())) {
                	boolean isLrNoExist = GateInDao.isStoneLrNoExist(conn,tprRecord.getTprId(), lrNo.getText(),TokenManager.materialCat);
                    if(isLrNoExist){
                        JOptionPane.showMessageDialog(null, "Duplicate LrNo");
                    	return;
                    }
                }
                captureWeight = Misc.getParamAsDouble(labelWeighment.getText());
                if(captureWeight < 5000.0 || captureWeight > 60000.0){
        			JOptionPane.showMessageDialog(null, "Captured Weight is not in limits (5.00-60.00 MT).Please capture properly");
        			return;
        		}else{
        			captureWeight = captureWeight/1000;
        		}
//                int responseVehicleDialog = JOptionPane.showConfirmDialog(this, "Vehicle Name: " + vehicleName.getSelectedItem().toString() + "\nTransporter: " + transporter.getSelectedItem().toString() + "\nCaptured Weight: " + Misc.printDouble(Misc.getParamAsDouble(labelWeighment.getText())/1000), UIConstant.dialogTitle, JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
                String[] options = {"Yes", "No"};

                int responseVehicleDialog = JOptionPane.showOptionDialog(new javax.swing.JFrame(),
        				"Vehicle Name: " + vehicleName.getSelectedItem().toString() + "\nTransporter: " + transporter.getSelectedItem().toString() + "\nCaptured Weight: " + Misc.printDouble((captureWeight),false),
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
                        GateInDao.insertReadings(conn, tprRecord.getTprId(), readings);
                        conn.commit();
                        if (true) {
                            JOptionPane.showMessageDialog(null, "Detail Saved");
                            if(TokenManager.Weightment_Printer_Connected == 1){
                            	new WeightmentFirstSlip(this, true, tprRecord).setVisible(true);
                            }
                            Barrier.openEntryGate();
                            clearInputs(conn, false);
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
            int stepId = InsertTPRStep(conn, true);
//            if (!Misc.isUndef(stepId)) {
//                InsertTPRQuestionDetails(stepId);
//            }
            conn.commit();
            clearInputs(conn, false);
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
            tprRecord.setLrNo(lrNo.getText());
            Date lrD = (Date) lrDate.getValue();
            tprRecord.setLrDate(lrD);
//            tprRecord.setMaterialCodeId(DropDownValues.getComboSelectedVal(materialCode));
            tprRecord.setConsignorId(((ComboItemList) consignor.getSelectedItem()).getValue());
            tprRecord.setConsignorName(((ComboItemList) consignor.getSelectedItem()).getLabel());
            tprRecord.setConsignorAddress(((ComboItemList) consignor.getSelectedItem()).getAddress());
            tprRecord.setConsignorRefDoc(consignorRefDocument.getText());

            tprRecord.setConsignee(((ComboItemList) consignee.getSelectedItem()).getValue());
            tprRecord.setConsigneeName(((ComboItemList) consignee.getSelectedItem()).getLabel());
            tprRecord.setConsigneeAddress(((ComboItemList) consignee.getSelectedItem()).getAddress());

            tprRecord.setConsigneeRefDoc(consigneeRefDoc.getText());
//            tprRecord.setMaterialNotesFirst(materialDescription.getText());
            tprRecord.setConsignorNotes(consignorNotes.getText());


            tprRecord.setConsigneeNotes(consigneeNotes.getText());

            tprRecord.setOtherMaterialDescription(otherMaterialDesc.getText());

//            tprRecord.setUnloadTare(captureWeight);
            tprRecord.setUnloadGross(captureWeight);

            tprRecord.setUpdatedOn(new Date());
            tprRecord.setPreStepType(TokenManager.currWorkStationType);
            tprRecord.setNextStepType(TokenManager.nextWorkStationType);
            tprRecord.setEarliestLoadWbInEntry(entryTime);
            tprRecord.setUpdatedBy(TokenManager.userId);
            tprRecord.setLoadYardInName(TokenManager.userName);
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
            tpStep.setMaterialCat(TokenManager.materialCat);
            tpStep.setHasValidRf(isTagRead ? 1 : 0);
            tpStep.setSaveStatus(isDeny ? TPStep.REQUEST_OVERRIDE : TPStep.SAVE_AND_CONTINUE);
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

    private boolean isSpinnerDateNull(Object value) {
        String sdf = UIConstant.displayFormat.format(value);
        if (Utils.isNull(sdf)) {
            return true;
        }
        return false;
    }

    private void vehicleNameAction() {

        Connection conn = null;
        boolean destroyIt = false;
        try {
            conn = DBConnectionPool.getConnectionFromPoolNonWeb();
            int vehId = Misc.getUndefInt();
            Pair<Integer, String> vehPair = null;
            if (!vehicleName.isEditable()) {
                setWhiteBackground();
                transporter.requestFocusInWindow();
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
                        vehicleName.addItem(vehName);
                        return;
                    } else if (responseVehicleDialog == contiNue) {
                        try {
                            GateInDao.InsertNewVehicle(conn, vehName, TokenManager.userId);
                            setTPRecord(vehName);
                            setWhiteBackground();
                            transporter.requestFocusInWindow();
                            isVehicleExist = false;
                            isTagRead = false;
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }

                    }
                } else {
                    try {
                        setWhiteBackground();
                        setTPRecord(vehName);
                        isTagRead = false;
                        transporter.requestFocusInWindow();
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

    void consigneeTextChange(boolean doEditable) {
        if (doEditable) {
            consigneeText.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
            consigneeText.setFocusable(true);
            consigneeText.setEditable(true);
            consigneeText.setText("");
        } else {
            consigneeText.setBorder(null);
            consigneeText.setFocusable(false);
            consigneeText.setEditable(false);
            consigneeText.setText("");
        }
    }

    
//    private void calculateNetWt() {
//        if (tprRecord != null && tprRecord.getUnloadTare() != Misc.getUndefDouble() && !Utils.isNull(labelWeighment.getText())) {
//            double mplGross = Double.valueOf(labelWeighment.getText());
//            if (!Misc.isUndef(mplGross)) {
//            	if(mplGross >= 5000.0)
//            		mplGross = (mplGross/1000);
//            	System.out.print("  calculateNetWt() : Weighment Tare: "+tprRecord.getUnloadTare()+" Weighment Gross: "+mplGross);
//                	Wb_Net_Wt = mplGross - (tprRecord.getUnloadTare() / 1000);
//        		
//            }
//        }
//    }
    
}
