/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ipssi.rfid.ui;

import com.ipssi.gen.utils.CacheTrack;
import com.ipssi.gen.utils.DBConnectionPool;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.Pair;
import com.ipssi.gen.utils.Triple;
import com.ipssi.rfid.beans.ComboItem;
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

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.FileNotFoundException;
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
public class StoneWeighmentGross extends javax.swing.JFrame {

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
    private AutoComplete auto_complete = null;
    private TPRBlockManager tprBlockManager = null;
    private WeighBridge weighBridge = null;
//    private double Wb_Net_Wt = Misc.getUndefDouble();
    private DisconnectionDialog disconnectionDialog = new DisconnectionDialog("Weigh Bridge Disconnected please check connection.....");
    private ArrayList<Pair<Long, Integer>> readings = null;
    private  boolean isManual = false;
	private Vehicle vehicleDetail = null;

    /**
     * Creates new form StoneWeighmentGross
     */
    public StoneWeighmentGross() throws IOException {
        Connection conn = null;
        boolean destroyIt = false;
        try {
            conn = DBConnectionPool.getConnectionFromPoolNonWeb();
            initComponents();
            labelWeighment.setText(TokenManager.weight_val);
            //jLabel12.setVisible(false);
            //jLabel20.setVisible(false);
            //stoneTareWt.setText("22100");
            //stoneTareTime.setText("24-02-2016 22:22");
            this.setExtendedState(this.getExtendedState()
                    | this.MAXIMIZED_BOTH);
            this.setTitle(UIConstant.formTitle);

            DropDownValues.setTransporterList(carryingTransporter, conn, TokenManager.materialCat);
            DropDownValues.setTransporterList(coalTransporter, conn,com.ipssi.rfid.constant.Type.TPRMATERIAL.COAL);
//            DropDownValues.setMaterialSubCategory(materialSubCategory, conn, TokenManager.materialCat);
            getFocus();
            setHidden(false);
            Clock.startClock("StoneWeighmentGross");
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

    private void setHidden(boolean isTrue) {
		materialSubCategory.setVisible(isTrue);
		materialSubCategoryLbl.setVisible(isTrue);
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
                	System.out.println("[Stone Gross Reading]:" + str);
                    int val = Misc.getParamAsInt(str);
                    if (!Misc.isUndef(val)) {
                        int currVal = Misc.getParamAsInt(labelWeighment.getText());
                        if (Misc.isUndef(currVal) || (currVal >= val ? currVal - val : val - currVal) >= 10 || val == 0) {
                            labelWeighment.setText(val + "");
                            double stone_grossWt = Misc.getParamAsDouble(labelWeighment.getText())/1000;
//                            stoneNetWtText.setText(Misc.getPrintableDouble(GateInDao.calculateNetWt(stone_grossWt, tprRecord.getLoadTare())));
                            stoneNetWtText.setText(Misc.getPrintableDouble(GateInDao.calculateNetWt(stone_grossWt, Misc.getParamAsDouble(stoneTareWt.getText()))));
                            
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
        }
    }

    public void setTPRecord(Connection conn, TPRecord tpr) throws IOException {
        System.out.println("######### Gate IN setTPRecord  ########");
        try {
            tprRecord = tpr;
            if (tprRecord != null) {
                System.out.println("TPR Record Create");
                if (true) {//blockStatus == Status.VALIDATION.NO_ISSUE) {// TPR Valid
                	vehicleDetail  = (Vehicle) RFIDMasterDao.get(conn, Vehicle.class, tprRecord.getVehicleId());
                	 long total_days = DropDownValues.getDifferenceBwDate(vehicleDetail.getStoneTareTime());
                     if(vehicleDetail != null && Misc.isUndef(vehicleDetail.getStoneTare())){
                       	JOptionPane.showMessageDialog(null, "Please Capture Tare Weight for Next Trip, System Does not have any tare.");
                       }else if (total_days > TokenManager.maxTareDays) {
                         JOptionPane.showMessageDialog(null, "Please Capture Tare Weight for Next Trip");
                     }
                     if (tprRecord.getLoadTare() == Misc.getUndefDouble()) {
                     	if(vehicleDetail.getStoneTare() != Misc.getUndefDouble()){
//                     		tprRecord.setLoadTare(vehicleDetail.getFlyashTare());
                     		stoneTareWt.setText(Misc.getPrintableDouble(vehicleDetail.getStoneTare()));
                     		stoneNetWtText.setText(Misc.getPrintableDouble(GateInDao.calculateNetWt(Misc.getParamAsDouble(labelWeighment.getText())/1000, vehicleDetail.getStoneTare())));
                     	}
                         if (tprRecord.getEarliestLoadWbInEntry() == null && vehicleDetail.getStoneTareTime() != null) {
                        	 stoneTareTime.setText(UIConstant.displayFormat.format(vehicleDetail.getStoneTareTime()));
                         }
                     }else{
                     	if(tprRecord.getLoadTare() != Misc.getUndefDouble())
                     		stoneTareWt.setText(Misc.getPrintableDouble(tprRecord.getLoadTare()));
                     	stoneNetWtText.setText(Misc.getPrintableDouble(GateInDao.calculateNetWt(Misc.getParamAsDouble(labelWeighment.getText())/1000, tprRecord.getLoadTare())));
                     	if (tprRecord.getEarliestLoadWbInEntry() != null) {
                              stoneTareTime.setText(UIConstant.displayFormat.format(tprRecord.getEarliestLoadWbInEntry()));
                          }
                     }   
               

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
                    
                     tprIdLabel.setText("TPR-ID:"+tprRecord.getTprId());
                     DropDownValues.setComboItem(carryingTransporter, tprRecord.getCarryingTransporterId());
                     DropDownValues.setComboItem(coalTransporter, tprRecord.getTransporterId());
//                     DropDownValues.setBedList(bedNo,tprRecord.getTransporterId());
                     DropDownValues.setComboItem(bedNo, tprRecord.getMplRefDoc());
                     lrNo.setText(tprRecord.getLrNo());
                      
                      
                    if (Utils.isNull(tprRecord.getVehicleName())) {
                        vehicleName.removeAllItems();
                        vehicleName.addItem(tprRecord.getVehicleName());
                    } else {
                        vehicleName.removeAllItems();
                        vehicleName.addItem(tprRecord.getVehicleName());
                    }

                }
//                double stone_grossWt = Misc.getParamAsDouble(labelWeighment.getText())/1000;
//                stoneNetWtText.setText(Misc.getPrintableDouble(GateInDao.calculateNetWt(stone_grossWt, tprRecord.getLoadTare())));
               
                setBlockingStatus();
                saveButton.setEnabled(true);
                scheduleClearScreenTimer();
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
        jLabel2 = new javax.swing.JLabel();
        tprIdLabel = new javax.swing.JLabel();
        button1 = new java.awt.Button();
        jPanel5 = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        carryingTransporter = new javax.swing.JComboBox();
        materialSubCategory = new javax.swing.JComboBox();
        coalTransporter = new javax.swing.JComboBox();
        bedNo = new javax.swing.JComboBox();
        vehicleName = new javax.swing.JComboBox();
        jLabel3 = new javax.swing.JLabel();
        lrNo = new javax.swing.JTextField();
        jPanel3 = new javax.swing.JPanel();
        jLabel12 = new javax.swing.JLabel();
        jLabel20 = new javax.swing.JLabel();
        stoneTareWt = new javax.swing.JLabel();
        stoneTareTime = new javax.swing.JLabel();
        panel1 = new java.awt.Panel();
        labelWeighment = new javax.swing.JLabel();
        clearButton = new javax.swing.JButton();
        saveButton = new javax.swing.JButton();
        manualButton = new javax.swing.JButton();
        blocking_reason = new javax.swing.JLabel();
        digitalClock = new javax.swing.JLabel();
        stoneNetWtlbl = new javax.swing.JLabel();
        stoneNetWtText = new javax.swing.JLabel();
        stoneTareLabel = new javax.swing.JLabel();
        stoneTareText = new javax.swing.JLabel();
        materialSubCategoryLbl = new javax.swing.JLabel();
        
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        this.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
            	GateInDao.forceSignut(TokenManager.userId,TokenManager.srcType,Integer.toString(TokenManager.systemId));
        }});
        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        jPanel2.setBackground(java.awt.SystemColor.controlLtHighlight);

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/ipssi/rfid/ui/intelliplanner.png"))); // NOI18N

        jLabel6.setFont(UIConstant.subHeadingFont);
        jLabel6.setText("Stone Weighment (Gross)");

        username.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);

        tprIdLabel.setFont(new java.awt.Font("Segoe UI", 1, 14));
//        tprIdLabel.setText("123456789012345");
        
        
        jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/ipssi/rfid/ui/cover_01_right_top.gif"))); // NOI18N

        button1.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        button1.setFocusable(false);
        button1.setForeground(new java.awt.Color(0, 102, 153));
        button1.setLabel("Sign Out");
        button1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(30,30,30)
                .addComponent(tprIdLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 213, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(30,30, 30)
                .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 314, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(username, javax.swing.GroupLayout.PREFERRED_SIZE, 314, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(button1, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 52, Short.MAX_VALUE)
            .addComponent(username, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jLabel6, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(tprIdLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanel2Layout.createSequentialGroup()
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
        jLabel8.setText("Carrying Transporter:");

        jLabel9.setFont(UIConstant.labelFont);
        jLabel9.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel9.setText("Stone Source:");// Bed#

        jLabel10.setFont(UIConstant.labelFont);
        jLabel10.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel10.setText("Coal Transporter:");

        materialSubCategory.setFont(UIConstant.textFont);
        materialSubCategory.addItem(new ComboItem(0, "Select"));
        materialSubCategory.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
            	materialSubCategoryKeyPressed(evt);
            }
            private void materialSubCategoryKeyPressed(KeyEvent evt) {
            	  if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                      setWhiteBackColor();
                      lrNo.requestFocusInWindow();
                  }
        	}
        });
      
      

        
        carryingTransporter.setFont(UIConstant.textFont);
        carryingTransporter.addItem(new ComboItem(0, "Select"));
        carryingTransporter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                carryingTransporterActionPerformed(evt);
            }
        });
        carryingTransporter.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                carryingTransporterKeyPressed(evt);
            }
        });

        coalTransporter.setFont(UIConstant.textFont);
        coalTransporter.addItem(new ComboItem(0, "Select"));
        coalTransporter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                coalTransporterActionPerformed(evt);
            }
        });
        coalTransporter.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                coalTransporterKeyPressed(evt);
            }
        });

        bedNo.setFont(UIConstant.textFont);
        bedNo.addItem(new ComboItem(0, "Select"));
        bedNo.addItem(new ComboItem(-10, "Bed"));
        bedNo.addItem(new ComboItem(-20, "Module"));
        bedNo.addItem(new ComboItem(-30, "Conveyor"));
        bedNo.addItem(new ComboItem(-40, "Mill"));
        bedNo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bedNoActionPerformed(evt);
            }
        });
        bedNo.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                bedNoKeyPressed(evt);
            }
        });

        vehicleName.setFont(UIConstant.textFont);
        vehicleName.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        vehicleName.setEnabled(false);
        vehicleName.setEditable(false);
        vehicleName.removeAllItems();

        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel3.setFont(UIConstant.labelFont);
        jLabel3.setText("Challan#:");//LR#

        lrNo.setFont(UIConstant.textFont);
        lrNo.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
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

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 177, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(4, 4, 4)
                        .addComponent(vehicleName, javax.swing.GroupLayout.PREFERRED_SIZE, 254, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 177, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(4, 4, 4)
                        .addComponent(carryingTransporter, javax.swing.GroupLayout.PREFERRED_SIZE, 238, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 177, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(4, 4, 4)
                        .addComponent(coalTransporter, javax.swing.GroupLayout.PREFERRED_SIZE, 238, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addGroup(jPanel5Layout.createSequentialGroup()
                            .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 174, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(6, 6, 6)
                            .addComponent(lrNo, javax.swing.GroupLayout.PREFERRED_SIZE, 239, javax.swing.GroupLayout.PREFERRED_SIZE))
                            
                        .addGroup(jPanel5Layout.createSequentialGroup()
                            .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 177, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(4, 4, 4)
                            .addComponent(bedNo, javax.swing.GroupLayout.PREFERRED_SIZE, 238, javax.swing.GroupLayout.PREFERRED_SIZE))
                            
                            .addGroup(jPanel5Layout.createSequentialGroup()
                            		 .addComponent(materialSubCategoryLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE)
                                     .addGap(4, 4, 4)
                                    .addComponent(materialSubCategory, javax.swing.GroupLayout.PREFERRED_SIZE, 238, javax.swing.GroupLayout.PREFERRED_SIZE)))))
               
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGap(11, 11, 11)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(vehicleName, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(6, 6, 6)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(carryingTransporter, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(6, 6, 6)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(coalTransporter, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(6, 6, 6)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(bedNo, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                		.addComponent(materialSubCategoryLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                		.addComponent(materialSubCategory, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lrNo, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        jPanel3.setBackground(new java.awt.Color(255, 255, 255));

        jLabel12.setFont(UIConstant.labelFont);
        jLabel12.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        //jLabel12.setText("1st Wt:");
        jLabel12.setText("Stone Tare Wt:");
        
        stoneNetWtlbl.setFont(UIConstant.labelFont);
        stoneNetWtlbl.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        stoneNetWtlbl.setText("Net Wt:");
        
        stoneNetWtText.setFont(UIConstant.labelFont);
        stoneNetWtText.setForeground(UIConstant.labeltextFontColor);
        stoneNetWtText.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
//        stoneNetWtText.setText("10.222");
        
        materialSubCategoryLbl.setFont(UIConstant.labelFont);
        materialSubCategoryLbl.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        materialSubCategoryLbl.setText("Material Sub Category:");
        
        
        jLabel20.setFont(UIConstant.labelFont);
        jLabel20.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
//        jLabel20.setText("NET:");
        jLabel20.setText("Stone Tare Time:");
        
        //firstWt.setFont(UIConstant.labeltextFont);

        stoneTareWt.setFont(UIConstant.labeltextFont);
        stoneTareWt.setForeground(UIConstant.labeltextFontColor);
     
        stoneTareTime.setFont(UIConstant.labeltextFontTemp);
        stoneTareTime.setForeground(UIConstant.labeltextFontColor);
        
        panel1.setBackground(new java.awt.Color(0, 0, 0));

        labelWeighment.setFont(UIConstant.headingFont);
        labelWeighment.setForeground(new java.awt.Color(255, 255, 255));
        labelWeighment.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
//        labelWeighment.setText("26100");
        javax.swing.GroupLayout panel1Layout = new javax.swing.GroupLayout(panel1);
        panel1.setLayout(panel1Layout);
        panel1Layout.setHorizontalGroup(
            panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(labelWeighment, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 322, Short.MAX_VALUE)
        );
        panel1Layout.setVerticalGroup(
            panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(labelWeighment, javax.swing.GroupLayout.DEFAULT_SIZE, 88, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap()
  
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                    		.addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(stoneTareWt, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel20, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(stoneTareTime, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(45, 45, 45))
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                    		.addComponent(stoneNetWtlbl, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    		.addComponent(stoneNetWtText, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, 0))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(panel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(panel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 2, Short.MAX_VALUE)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(stoneTareWt, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel20, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(stoneTareTime, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 2, Short.MAX_VALUE)
                
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    		 .addComponent(stoneNetWtlbl, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                             .addComponent(stoneNetWtText, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
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

        digitalClock.setFont(UIConstant.textFont); // NOI18N

        digitalClock.setForeground(UIConstant.textFontColor);
        digitalClock.setFont(new java.awt.Font("Segoe UI", 1, 36));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(489, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addComponent(clearButton, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(29, 29, 29)
                        .addComponent(manualButton, javax.swing.GroupLayout.PREFERRED_SIZE, 166, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(saveButton, javax.swing.GroupLayout.PREFERRED_SIZE, 221, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(423, 423, 423))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addComponent(digitalClock, javax.swing.GroupLayout.PREFERRED_SIZE, 164, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(81, 81, 81))))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(208, 208, 208)
                        .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(33, 33, 33)
                        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(blocking_reason, javax.swing.GroupLayout.PREFERRED_SIZE, 1316, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(blocking_reason, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(26, 26, 26)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(52, 52, 52)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(saveButton, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(manualButton, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(clearButton, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 146, Short.MAX_VALUE)
                .addComponent(digitalClock, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(35, 35, 35))
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

    

	private void manualButtonActionPerformed(java.awt.event.ActionEvent evt) {                                             
        manualButtonAction();
    }                                            

    private void button1ActionPerformed(java.awt.event.ActionEvent evt) {                                        
    	GateInDao.forceSignut(TokenManager.userId,TokenManager.srcType,Integer.toString(TokenManager.systemId));
    	this.dispose();
        new LoginWindow().setVisible(true);

    }                                       

    private void carryingTransporterActionPerformed(java.awt.event.ActionEvent evt) {                                                    
        
    }                                                   

    private void carryingTransporterKeyPressed(java.awt.event.KeyEvent evt) {                                               
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            setWhiteBackColor();
            coalTransporter.requestFocusInWindow();
        }
    }                                              

    private void coalTransporterActionPerformed(java.awt.event.ActionEvent evt) {                                                
//          if(coalTransporter.getSelectedIndex() != 0){
//                bedNo.removeAllItems();
//                bedNo.addItem(new ComboItem(0, "Select"));
//                DropDownValues.setBedList(bedNo,coalTransporter.getSelectedIndex());
//          }else{
//              bedNo.removeAllItems();
//              bedNo.addItem(new ComboItem(0, "Select"));
//          }
    }                                               

    private void coalTransporterKeyPressed(java.awt.event.KeyEvent evt) {                                           
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            setWhiteBackColor();
            bedNo.requestFocusInWindow();
        }
    }                                          

    private void clearButtonActionPerformed(java.awt.event.ActionEvent evt) {                                            
        clearAction();
    }                                           

    private void bedNoKeyPressed(java.awt.event.KeyEvent evt) {                                 
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            setWhiteBackColor();
//            materialSubCategory.requestFocusInWindow();
            lrNo.requestFocusInWindow();
           // setFocusOnButton();
        }
    }                                

    private void manualButtonKeyPressed(java.awt.event.KeyEvent evt) {                                        
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            manualButtonAction();
        }
    }                                       

    private void clearButtonFocusGained(java.awt.event.FocusEvent evt) {                                        
        setWhiteBackColor();        // TODO add your handling code here:
    }                                       

    private void manualButtonFocusGained(java.awt.event.FocusEvent evt) {                                         
        setWhiteBackColor();        // TODO add your handling code here:
    }                                        

    private void saveButtonKeyPressed(java.awt.event.KeyEvent evt) {                                      
        saveButtonAction();        // TODO add your handling code here:
    }                                     

    private void saveButtonFocusGained(java.awt.event.FocusEvent evt) {                                       
        setWhiteBackColor();        // TODO add your handling code here:
    }                                      

    private void saveButtonActionPerformed(java.awt.event.ActionEvent evt) {                                           
        saveButtonAction();
    }                                          

    private void bedNoActionPerformed(java.awt.event.ActionEvent evt) {                                      
        // TODO add your handling code here:
    }                                     

    private void lrNoFocusGained(java.awt.event.FocusEvent evt) {                                 
           lrNo.setBackground(UIConstant.focusPanelColor);
    }                                

    private void lrNoFocusLost(java.awt.event.FocusEvent evt) {                               
           lrNo.setBackground(UIConstant.PanelWhite);
    }                              

    private void lrNoKeyPressed(java.awt.event.KeyEvent evt) {                                
          if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            setWhiteBackColor();
            setFocusOnButton();
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
            java.util.logging.Logger.getLogger(StoneWeighmentGross.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(StoneWeighmentGross.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(StoneWeighmentGross.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(StoneWeighmentGross.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    new StoneWeighmentGross().setVisible(true);
                } catch (IOException ex) {
                    Logger.getLogger(StoneWeighmentGross.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
    }
    // Variables declaration - do not modify                     
    private javax.swing.JComboBox bedNo;
    private javax.swing.JLabel blocking_reason;
    private java.awt.Button button1;
    private javax.swing.JComboBox carryingTransporter;
    private javax.swing.JButton clearButton;
    private javax.swing.JComboBox coalTransporter;
    public static javax.swing.JLabel digitalClock;
    public static javax.swing.JLabel stoneTareWt;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel  tprIdLabel;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JLabel labelWeighment;
    private javax.swing.JTextField lrNo;
    private javax.swing.JButton manualButton;
    public static javax.swing.JLabel stoneTareTime;
    private java.awt.Panel panel1;
    private javax.swing.JButton saveButton;
    public static javax.swing.JLabel username;
    private javax.swing.JComboBox vehicleName;
    private javax.swing.JLabel stoneTareLabel;
    private javax.swing.JLabel stoneTareText;
    private javax.swing.JLabel stoneNetWtText;
    private javax.swing.JLabel stoneNetWtlbl;
    private javax.swing.JComboBox materialSubCategory;
    private javax.swing.JLabel materialSubCategoryLbl;
    // End of variables declaration                   
	private Timer timer = null;

    private void setWhiteBackColor() {
        vehicleName.setBackground(UIConstant.PanelWhite);
        lrNo.setBackground(UIConstant.PanelWhite);
    }

    private void clearAction() {
        Connection conn = null;
        boolean destroyIt = false;
        try {
            conn = DBConnectionPool.getConnectionFromPoolNonWeb();
            setWhiteBackColor();
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
        GateInDao.stopClearScreenTimer(timer);
        tprIdLabel.setText("");
        stoneNetWtText.setText("");
        bedNo.setSelectedIndex(0);
        carryingTransporter.setSelectedIndex(0);
        //materialSubCategory.setSelectedIndex(0);
        coalTransporter.setSelectedIndex(0);
        blocking_reason.setText("");
        stoneTareTime.setText("");
        stoneTareWt.setText("");
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
//        Wb_Net_Wt = Misc.getUndefDouble();
        lrNo.setText("");
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
            carryingTransporter.requestFocusInWindow();
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
            if (!vehicleName.isEnabled()) {
                setWhiteBackColor();
                carryingTransporter.requestFocusInWindow();
                carryingTransporter.setBackground(UIConstant.focusPanelColor);
            }      else if (vehicleName.getItemCount() == 0 && Utils.isNull(vehicleName.getEditor().getItem().toString())) {
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
                        vehicleName.addItem("");
                        return;
                    } else if (responseVehicleDialog == contiNue) {
                        try {
                            GateInDao.InsertNewVehicle(conn, vehName, TokenManager.userId);
                            setTPRecord(vehName);
                            setWhiteBackColor();
                            carryingTransporter.requestFocusInWindow();
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
                        carryingTransporter.requestFocusInWindow();
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
            }  else if (carryingTransporter.getSelectedIndex() == 0) {//mines.getSelectedIndex() == 0) {
                JOptionPane.showMessageDialog(null, "Please Select Carrying Transporter");
                carryingTransporter.requestFocusInWindow();
                return;
            } else if (coalTransporter.getSelectedIndex() == 0) {//mines.getSelectedIndex() == 0) {
                JOptionPane.showMessageDialog(null, "Please Select Coal Transporter");
                coalTransporter.requestFocusInWindow();
                return;
            }  else if (bedNo.getSelectedIndex() == 0) {//mines.getSelectedIndex() == 0) {
                JOptionPane.showMessageDialog(null, "Please Select Bed");
                bedNo.requestFocusInWindow();
                return;
            } else if (Utils.isNull(lrNo.getText())) {
                JOptionPane.showMessageDialog(null, "Please Enter LrNo");
                lrNo.requestFocusInWindow();
                return;
            } 
            else {
            	boolean isLrNoExist = GateInDao.isStoneLrNoExist(conn,tprRecord.getTprId(),lrNo.getText(),TokenManager.materialCat);
                if(isLrNoExist){
                    JOptionPane.showMessageDialog(null, "Duplicate LrNo");
                	return;
                }
            	
                captureWeight = Misc.getParamAsDouble(labelWeighment.getText());
                if(captureWeight < TokenManager.min_weight || captureWeight > TokenManager.max_weight){
//                if(captureWeight < 15000.0 || captureWeight > 50000.0){
        			JOptionPane.showMessageDialog(null, "Captured Weight is not in limits (15.00-50.00 MT).Please capture properly");
        			return;
        		}else{
        			captureWeight = captureWeight/1000;
        		}
//                int responseVehicleDialog = JOptionPane.showConfirmDialog(this, "Vehicle Name: " + vehicleName.getSelectedItem().toString() + "\nTransporter: " + carryingTransporter.getSelectedItem().toString() + "\nCaptured Weight: " + Misc.printDouble(captureWeight/1000), UIConstant.dialogTitle, JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
                String[] options = {"Yes", "No"};

                int responseVehicleDialog = JOptionPane.showOptionDialog(new javax.swing.JFrame(),
        				"Vehicle Name: " + vehicleName.getSelectedItem().toString() + "\nTransporter: " + carryingTransporter.getSelectedItem().toString() + "\nCaptured Weight: " + Misc.printDouble(captureWeight,false),
                        "Confirm",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.INFORMATION_MESSAGE,
                        null, //do not use a custom Icon
                        options, //the titles of buttons
                        options[1]);
              System.out.print("##### Confirmation Value :#####" + responseVehicleDialog);
                
                System.out.print("##### Confirmation Value :#####" + responseVehicleDialog);
                if (responseVehicleDialog == 1 || responseVehicleDialog == -1) {
                    return;
                } else {
                    try {
                    	
                        boolean isInsert = false;
                        boolean isUpdateTpr = false;
                        int stepId = Misc.getUndefInt();

                        Triple<TPRecord, Integer, Boolean> tprTriplet = TPRInformation.getLatestNonWeb(conn, token.getVehicleId(), null, vehicleName.getSelectedItem().toString(), TokenManager.currWorkStationType, TokenManager.materialCat);//(conn, vehicleId, data, vehicleName, TokenManager.createNewTPR, workStationType);
        				TPRecord tpr = tprTriplet != null ? tprTriplet.first : null;
        				boolean repeatProcess = false;
        				if(tpr != null && TPRInformation.isGreaterThanEqualsProcessed(tpr, TokenManager.currWorkStationType, TokenManager.materialCat)/*tpr.getTprId() != tprRecord.getTprId()*/){
        					repeatProcess = true;
        					tprRecord = tpr;
        				}else{
        					repeatProcess = false;
        				}
        				
        				if(!repeatProcess)
        					isUpdateTpr = updateTPR(conn);

                        stepId = InsertTPRStep(conn, false,repeatProcess);
                        if (stepId != Misc.getUndefInt()) {
                            InsertQCDetatl(conn, stepId);
//                        InsertTPRQuestionDetails(stepId);
                        }
                        GateInDao.insertReadings(conn, tprRecord.getTprId(), readings);
                        conn.commit();
                        if (true) {
                        	if(repeatProcess){
    	    					JOptionPane.showMessageDialog(null, "This Step has already been completed, Please Confirm next step from control room");
    	    				}else
    	    					JOptionPane.showMessageDialog(null, "Detail Saved");
                            
                        	if(TokenManager.Weightment_Printer_Connected == 1){
                            	new StoneGrossSlip(this, true, tprRecord).setVisible(true);
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
            int stepId = InsertTPRStep(conn, true,false);

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
            tprRecord.setCarryingTransporterId(DropDownValues.getComboSelectedVal(carryingTransporter));
            tprRecord.setTransporterId(DropDownValues.getComboSelectedVal(coalTransporter));
            int bedAssigned = DropDownValues.getComboSelectedVal(bedNo);
            if (bedAssigned != Misc.getUndefInt()) {
                tprRecord.setBedAssigned(bedNo.getSelectedItem().toString());
            }
//            tprRecord.setMaterial_sub_cat_id(DropDownValues.getComboSelectedVal(materialSubCategory));
            tprRecord.setLrNo(lrNo.getText());
            tprRecord.setMplRefDoc(bedAssigned);
            tprRecord.setLoadGross(captureWeight);
            tprRecord.setLoadTare(Misc.getParamAsDouble(stoneTareWt.getText()));
            tprRecord.setUpdatedOn(new Date());
            tprRecord.setPreStepType(TokenManager.currWorkStationType);
            tprRecord.setNextStepType(TokenManager.nextWorkStationType);
            tprRecord.setEarliestLoadWbOutEntry(entryTime);
            tprRecord.setUpdatedBy(TokenManager.userId);
            tprRecord.setLoadWbOutName(TokenManager.userName);
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
        TPRInformation.insertUpdateTpr(conn, tprRecord);
        if (tprBlockManager != null) {
            tprBlockManager.setTprBlockStatus(conn, tprRecord.getTprId(), TokenManager.userId);
        }
        isUpdate = true;
        return isUpdate;
    }


    private int InsertTPRStep(Connection conn, boolean isDeny, boolean repeatProcess) throws Exception {
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
            	tpStep.setSaveStatus(isDeny ? TPStep.REQUEST_OVERRIDE : TPStep.SAVE_AND_CONTINUE);
            else
            	tpStep.setSaveStatus(isDeny ? TPStep.REQUEST_OVERRIDE : TPStep.SAVE_AND_CONTINUE);
            RFIDMasterDao.insert(conn, tpStep,false);
            RFIDMasterDao.insert(conn, tpStep,true);
    
        } else {
            long currTimeServerMillis = System.currentTimeMillis();
            tpStep.setExitTime(new Date(currTimeServerMillis));
            tpStep.setUpdatedOn(new Date(currTimeServerMillis));

            tpStep.setSaveStatus(isDeny ? TPStep.REQUEST_OVERRIDE : TPStep.SAVE_AND_CONTINUE);
            tpStep.setHasValidRf(isTagRead ? 1 : 0);
            tpStep.setMaterialCat(TokenManager.materialCat);
            RFIDMasterDao.update(conn, tpStep,false);
            RFIDMasterDao.update(conn, tpStep,true);
    
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
//          private void calculateNetWt() {
//            if (tprRecord != null && tprRecord.getLoadTare() != Misc.getUndefDouble() && !Utils.isNull(labelWeighment.getText())) {
//                double mplGross = Double.valueOf(labelWeighment.getText());
//                if (!Misc.isUndef(mplGross)) {
//                	if(mplGross > 5000.0)
//                		mplGross = (mplGross/1000);
//                	System.out.print("  calculateNetWt() : Stone Tare: "+tprRecord.getLoadTare()+" Stone Gross: "+mplGross);
//                    	Wb_Net_Wt = mplGross - tprRecord.getLoadTare();
//            		
//                }
//            }
  //      }
    
    public void scheduleClearScreenTimer(){
     	 try{
     		 timer = new Timer();
     		 timer.schedule( new TimerTask() {
     			 public void run() {
     				 System.out.println("Auto Refresh Screen");
     				 clearAction();
     				// timer.cancel();
     			 }
     		 }, TokenManager.screenClearInterval * 1000);
     	 }catch (Exception e) {
     		e.printStackTrace();
     	}
      } 
    
}
