package com.ipssi.rfid.ui;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JOptionPane;

import com.ipssi.common.ds.rule.GpsPlusViolations;
import com.ipssi.gen.utils.CacheTrack;
import com.ipssi.gen.utils.DBConnectionPool;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.Pair;
import com.ipssi.rfid.beans.RFIDHolder;
import com.ipssi.rfid.beans.TPRecord;
import com.ipssi.rfid.beans.TPSQuestionDetail;
import com.ipssi.rfid.beans.TPStep;
import com.ipssi.rfid.beans.Token;
import com.ipssi.rfid.constant.Status;
import com.ipssi.rfid.constant.Type;
import com.ipssi.rfid.constant.Type.WorkStationType;
import com.ipssi.rfid.constant.UIConstant;
import com.ipssi.rfid.database.DropDownValues;
import com.ipssi.rfid.database.GateInDao;
import com.ipssi.rfid.db.RFIDMasterDao;
import com.ipssi.rfid.integration.Barrier;
import com.ipssi.rfid.integration.Clock;
import com.ipssi.rfid.processor.TPRBlockManager;
import com.ipssi.rfid.processor.TPRBlockStatusHelper;
import com.ipssi.rfid.processor.TPRInformation;
import com.ipssi.rfid.processor.TokenManager;
import com.ipssi.rfid.processor.Utils;
import com.ipssi.rfid.readers.RFIDDataHandler;
import com.ipssi.rfid.readers.TAGListener;
import com.ipssi.rfid.ui.AutoCompleteCombo.ComboKeyEvent;

public class CoalGateOutWindow extends javax.swing.JFrame {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	UIConstant ApplicationConstant;

    private int contiNue = 1;
    private int reEnter = 0;
    
    private RFIDDataHandler rfidHandler;
    private int readerId = 0;
    
    private TPRecord tprRecord = null;
    private Date entryTime = null;
    private Token token = null;
    private TPStep tpStep = null;
    
    private boolean isRequestOverride = false;
    private boolean isVehicleExist = false;
    private boolean vehicleBlackListed = false;
    private boolean isTagRead =false;

	private TPRBlockManager tprBlockManager = null;

	private boolean isManual = false;
    
    public CoalGateOutWindow() throws IOException {
        initComponents();
//        gpsLocation.setText("12345678901234567890123456789012345678901234567890");
//      gpsTime.setText("asdfghjkl;wertyuiod");
        this.setExtendedState(this.getExtendedState()
                | this.MAXIMIZED_BOTH);
        this.setTitle(ApplicationConstant.formTitle);

        Clock.startClock("GateOut");
        getFocus();
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
					clearInputs(conn, clearToken);
				}

				@Override
				public int mergeData(long sessionId, String epc, RFIDHolder rfidHolder) {
					// TODO Auto-generated method stub
					return 0;
				}
            });
        }
        rfidHandler.start();
    }

    private void setTPRecord(Connection conn, String vehicleName) throws IOException {
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
        System.out.println("######### Gate Out setTPRecord  ########");
        try {
        	tprRecord = tpr;
        	if (tprRecord != null) {
        		if (true) {//blockStatus == Status.VALIDATION.NO_ISSUE) {// TPR Valid
        			toggleVehicle(false);
        			isTagRead = token != null ? token.isReadFromTag() : false;
        			if (token == null && tprRecord.getEarliestUnloadGateOutEntry() != null) {
        				entryTime = tprRecord.getEarliestUnloadGateOutEntry();
        			} else if (token != null && tprRecord.getEarliestUnloadGateOutEntry() == null) {
        				if (token.getLastSeen() != Misc.getUndefInt()) {
        					entryTime = new Date(token.getLastSeen());
        				} else {
        					entryTime = new Date();
        				}
        			} else if (token != null && tprRecord.getEarliestUnloadGateOutEntry() != null) {
        				if (token.getLastSeen() > Utils.getDateTimeLong(tprRecord.getEarliestUnloadGateOutEntry())) {
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
        			transporter.setText(DropDownValues.getTransporter(tprRecord.getTransporterId(), conn));
        			purpose.setText(com.ipssi.rfid.constant.Type.TPRMATERIAL.getStr(tprRecord.getMaterialCat()));
        		} 
        		
        		 Pair<Long, String> pairVal = GpsPlusViolations.getLatestLocation(conn, tprRecord.getVehicleId());
                 if(pairVal != null){
                 	String location = pairVal.second == null ? "" : pairVal.second;
                 	gpsTime.setText(GateInDao.getLongToDatetime(pairVal.first));
                 	gpsLocation.setText(GateInDao.getString(location, 40));
                 }
                 
        		setBlockingStatus();
        		saveAndOpen.setEnabled(true);
        		saveAndOpen.requestFocusInWindow();
        	} else {
        		JOptionPane.showMessageDialog(null, "Invalid Vehicle Go to Registration");
        		isVehicleExist = false;
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
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        jPanel5 = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        vehicleName = new AutoCompleteCombo();
        transporter = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        overrides = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        challanNo = new javax.swing.JTextField();
        purpose = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        gpsLocation = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        gpsTime = new javax.swing.JLabel();
        saveAndOpen = new javax.swing.JButton();
        manualEntryButton = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        username = new javax.swing.JLabel();
        button1 = new java.awt.Button();
        jLabel4 = new javax.swing.JLabel();
        digitalClock = new javax.swing.JLabel();
        blocking_reason = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        jPanel4.setBackground(new java.awt.Color(255, 255, 255));

        jPanel5.setBackground(new java.awt.Color(255, 255, 255));

        jLabel7.setFont(ApplicationConstant.labelFont);
        jLabel7.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel7.setText("Vehicle:");

        jLabel8.setFont(ApplicationConstant.labelFont);
        jLabel8.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel8.setText("Transporter:");

        /*vehicleName.setFont(UIConstant.vehicleLabel);
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
        });*/
        vehicleName.setFont(UIConstant.textFont);
        vehicleName.setMaximumRowCount(10);
        vehicleName.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Select" }));
        vehicleName.setTextBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 0));
//        vehicleName.setEnabled(false);
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
        transporter.setFont(UIConstant.textFont);
        transporter.setForeground(UIConstant.textFontColor);

        jLabel5.setFont(ApplicationConstant.labelFont);
        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel5.setText("Override?:");

        overrides.setFont(UIConstant.textFont);
        overrides.setForeground(UIConstant.textFontColor);
        
      
        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
                jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel5Layout.createSequentialGroup()
                    .addGap(10, 10, 10)
                    .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel5Layout.createSequentialGroup()
                            .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 158, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(10, 10, 10)
                            .addComponent(transporter, javax.swing.GroupLayout.PREFERRED_SIZE, 215, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(jPanel5Layout.createSequentialGroup()
                            .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 158, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(10, 10, 10)
                            .addComponent(vehicleName, javax.swing.GroupLayout.PREFERRED_SIZE, 259, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(jPanel5Layout.createSequentialGroup()
                            .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 162, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(6, 6, 6)
                            .addComponent(overrides, javax.swing.GroupLayout.PREFERRED_SIZE, 215, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            );
            jPanel5Layout.setVerticalGroup(
                jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel5Layout.createSequentialGroup()
                    .addGap(20, 20, 20)
                    .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(vehicleName, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGap(6, 6, 6)
                    .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(overrides, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGap(6, 6, 6)
                    .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(transporter, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addContainerGap(47, Short.MAX_VALUE))
            );
        jPanel3.setBackground(new java.awt.Color(255, 255, 255));

        jLabel2.setFont(ApplicationConstant.labelFont);
        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel2.setText("Purpose:");

        jLabel3.setFont(ApplicationConstant.labelFont);
        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel3.setText("Challan #:");

        challanNo.setFont(UIConstant.textFont);
        challanNo.setForeground(UIConstant.textFontColor);
        challanNo.setEditable(false);
        challanNo.setBackground(new java.awt.Color(255, 255, 255));
        challanNo.setBorder(null);
        challanNo.setFocusable(false);

        purpose.setFont(UIConstant.textFont);
        purpose.setForeground(UIConstant.textFontColor);

        jLabel9.setFont(ApplicationConstant.labelFont);
        jLabel9.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel9.setText("GPS Location:");

        gpsLocation.setFont(UIConstant.textFont);
        gpsLocation.setForeground(UIConstant.textFontColor);

        jLabel10.setFont(ApplicationConstant.labelFont);
        jLabel10.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel10.setText("GPS Time:");
        
        gpsTime.setFont(UIConstant.textFont);
        gpsTime.setForeground(UIConstant.textFontColor);
        
        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
                jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel3Layout.createSequentialGroup()
                    .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel3Layout.createSequentialGroup()
                            .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 137, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(6, 6, 6)
                            .addComponent(purpose, javax.swing.GroupLayout.PREFERRED_SIZE, 215, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(jPanel3Layout.createSequentialGroup()
                            .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 137, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(6, 6, 6)
                            .addComponent(challanNo, javax.swing.GroupLayout.PREFERRED_SIZE, 215, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(jPanel3Layout.createSequentialGroup()
                            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(jLabel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jLabel9, javax.swing.GroupLayout.DEFAULT_SIZE, 135, Short.MAX_VALUE))
                            .addGap(6, 6, 6)
                            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(gpsTime, javax.swing.GroupLayout.PREFERRED_SIZE, 230, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(gpsLocation, javax.swing.GroupLayout.PREFERRED_SIZE, 424, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addContainerGap(21, Short.MAX_VALUE))
            );
            jPanel3Layout.setVerticalGroup(
                jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel3Layout.createSequentialGroup()
                    .addContainerGap(36, Short.MAX_VALUE)
                    .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(purpose, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGap(6, 6, 6)
                    .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(challanNo, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGap(6, 6, 6)
                    .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(gpsLocation, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(gpsTime, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGap(33, 33, 33))
            );

        saveAndOpen.setFont(ApplicationConstant.buttonFont);
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

        manualEntryButton.setFont(ApplicationConstant.buttonFont);
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

        jButton1.setFont(ApplicationConstant.buttonFont);
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

        jPanel2.setBackground(java.awt.SystemColor.controlLtHighlight);

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/ipssi/rfid/ui/intelliplanner.png"))); // NOI18N

        jLabel6.setFont(ApplicationConstant.subHeadingFont);
        jLabel6.setText("Coal Gate Out");

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

        jLabel4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/ipssi/rfid/ui/cover_01_right_top.gif"))); // NOI18N

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(400, 400, 400)
                .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 201, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(username, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(button1, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(1, 1, 1)
                .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel6, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(username, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(button1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );

        digitalClock.setFont(UIConstant.textFont); // NOI18N

        digitalClock.setForeground(UIConstant.textFontColor);
        digitalClock.setFont(new java.awt.Font("Segoe UI", 1, 36)); // NOI18N

        blocking_reason.setFont(UIConstant.textFont);
        blocking_reason.setForeground(UIConstant.noActionPanelColor);

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
                jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel4Layout.createSequentialGroup()
                    .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel4Layout.createSequentialGroup()
                            .addGap(200, 200, 200)
                            .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(jPanel4Layout.createSequentialGroup()
                            .addGap(10, 10, 10)
//                            .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
//                        .addGroup(jPanel4Layout.createSequentialGroup()
//                            .addGap(10, 10, 10)
                            .addComponent(blocking_reason, javax.swing.GroupLayout.PREFERRED_SIZE, 1242, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(jPanel4Layout.createSequentialGroup()
                            .addGap(351, 351, 351)
                            .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(39, 39, 39)
                            .addComponent(manualEntryButton, javax.swing.GroupLayout.PREFERRED_SIZE, 166, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(35, 35, 35)
                            .addComponent(saveAndOpen, javax.swing.GroupLayout.PREFERRED_SIZE, 221, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGap(0, 14, Short.MAX_VALUE)))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(digitalClock, javax.swing.GroupLayout.PREFERRED_SIZE, 179, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(51, 51, 51))
                 .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            );
            jPanel4Layout.setVerticalGroup(
                jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel4Layout.createSequentialGroup()
                    .addGap(2, 2, 2)
                    .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(2, 2, 2)
                    .addComponent(blocking_reason, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel4Layout.createSequentialGroup()
                            .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                            .addGap(0, 37, Short.MAX_VALUE)
                            .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(18, 18, Short.MAX_VALUE)))
                    .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(manualEntryButton, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(saveAndOpen, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 382, Short.MAX_VALUE)
                    .addComponent(digitalClock, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap())
            );


        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, 1356, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
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

    private void vehicleNameMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_vehicleNameMouseClicked
        if (vehicleName.isTextEditable()) {
            setWhiteBackColor();
            vehicleName.requestFocusInWindow();
            vehicleName.setTextBackground(UIConstant.focusPanelColor);
        }
    }//GEN-LAST:event_vehicleNameMouseClicked

    private void vehicleNameFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_vehicleNameFocusGained
        setWhiteBackColor();
        vehicleName.requestFocusInWindow();
        vehicleName.setTextBackground(UIConstant.focusPanelColor);
    }//GEN-LAST:event_vehicleNameFocusGained

    private void vehicleNameKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_vehicleNameKeyPressed
      
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            vehicleNameAction();
        }

    }//GEN-LAST:event_vehicleNameKeyPressed

    private void saveAndOpenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveAndOpenActionPerformed
        quickCreateAndSave();
    }//GEN-LAST:event_saveAndOpenActionPerformed

    private void saveAndOpenKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_saveAndOpenKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            quickCreateAndSave();
        }
    }//GEN-LAST:event_saveAndOpenKeyPressed

    private void manualEntryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_manualEntryActionPerformed
        // TODO add your handling code here:
        manualEntryAction();
    }//GEN-LAST:event_manualEntryActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        clearAction();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton1KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jButton1KeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            clearAction();
        }
    }//GEN-LAST:event_jButton1KeyPressed

    private void manualEntryKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_manualEntryKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            manualEntryAction();
        }
    }//GEN-LAST:event_manualEntryKeyPressed

    private void vehicleNameFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_vehicleNameFocusLost
//       if(vehicleName.isTextEditable() && !Utils.isNull(vehicleName.getText()) && !vehicleName.getText().equalsIgnoreCase("NO VEHICLE DETECTED")){
//            vehicleNameAction();
//        }
    }//GEN-LAST:event_vehicleNameFocusLost

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
            java.util.logging.Logger.getLogger(CoalGateOutWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(CoalGateOutWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(CoalGateOutWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(CoalGateOutWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    new CoalGateOutWindow().setVisible(true);
                } catch (IOException ex) {
                    Logger.getLogger(CoalGateOutWindow.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel blocking_reason;
    private java.awt.Button button1;
    private javax.swing.JTextField challanNo;
    private javax.swing.JButton manualEntryButton;
    public static javax.swing.JLabel digitalClock;
    private javax.swing.JLabel gpsLocation;
    private javax.swing.JLabel gpsTime;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JLabel overrides;
    private javax.swing.JLabel purpose;
    private javax.swing.JButton saveAndOpen;
    private javax.swing.JLabel transporter;
    public static javax.swing.JLabel username;
    private AutoCompleteCombo vehicleName;
    // End of variables declaration//GEN-END:variables
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
    		vehicleName.setBorder(null);
    		vehicleName.setTextBackground(Color.WHITE);
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
    private void manualEntryAction() {
    	Connection conn = null;
    	boolean destroyIt = false;
    	try{
    		conn = DBConnectionPool.getConnectionFromPoolNonWeb();
        	clearInputs(conn, false);
        	if (!vehicleName.isTextEditable()) {
                toggleVehicle(true);
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
    private void clearInputs(Connection conn, boolean clearToken) {
    		if(clearToken){
    			TokenManager.clearWorkstation();
    		}else{
    			if(token != null)
    				TokenManager.returnToken(conn, token);
    		}
    		vehicleName.setText("");

    		transporter.setText("");
    		purpose.setText("");
    		challanNo.setText("");
    		overrides.setText("");
    		blocking_reason.setText("");
    		saveAndOpen.setEnabled(false);
    		if(isManual)
    			manualEntryButton.setEnabled(true);

    		tprRecord = null;
    		tprBlockManager = null;
    		entryTime = null;
    		token = null;
    		tpStep = null;
    		isRequestOverride = false;
    		isVehicleExist = false;
    		vehicleBlackListed = false;
    		isTagRead =false;
    		toggleVehicle(false);
    		enableDenyEntry(false);
    		overrides.setText("");
    		gpsLocation.setText("");
    		gpsTime.setText("");
    		
    }
    private void updateTPR(Connection conn) throws Exception {
        updateTPR(conn, false);
    }

    private void updateTPR(Connection conn, boolean isDeny) throws Exception {
        if (!isDeny) {
            tprRecord.setPreStepType(WorkStationType.GATE_OUT_TYPE);
            tprRecord.setPreStepDate(new Date());
            if (TokenManager.closeTPR) {
                tprRecord.setTprStatus(Status.TPR.CLOSE);
                if (token.getEpcId() != null && token.getEpcId().length() >= 20 && rfidHandler != null) {
                    rfidHandler.clearData(Utils.HexStringToByteArray(token.getEpcId()), 5);
                }
            }
            tprRecord.setUpdatedBy(TokenManager.userId);
            tprRecord.setUpdatedOn(new Date());
            tprRecord.setPrevTpStep(TokenManager.currWorkStationId);
            tprRecord.setPreStepType(TokenManager.currWorkStationType);
            tprRecord.setNextStepType(Misc.getUndefInt());
            if(tprRecord.getMaterialCat() == com.ipssi.rfid.constant.Type.TPRMATERIAL.FLYASH || tprRecord.getMaterialCat() == com.ipssi.rfid.constant.Type.TPRMATERIAL.STONE || tprRecord.getMaterialCat() == com.ipssi.rfid.constant.Type.TPRMATERIAL.OTHERS){
            	tprRecord.setLatestLoadGateOutExit(new Date());
            }else{
            	tprRecord.setLatestUnloadGateOutExit(new Date());
            }
            tprRecord.setComboEnd(new Date());
            if (tprRecord.getComboStart() == null) {
                tprRecord.setComboStart(new Date());
            }
            tprRecord.setComboEnd(new Date());
        }

        if(tprRecord.getMaterialCat() == com.ipssi.rfid.constant.Type.TPRMATERIAL.FLYASH || tprRecord.getMaterialCat() == com.ipssi.rfid.constant.Type.TPRMATERIAL.STONE || tprRecord.getMaterialCat() == com.ipssi.rfid.constant.Type.TPRMATERIAL.OTHERS){
        	tprRecord.setEarliestLoadGateOutEntry(entryTime);
        }else{
        	tprRecord.setEarliestUnloadGateOutEntry(entryTime);
        }
        
        if (tprRecord.getComboStart() == null) {
            tprRecord.setComboStart(new Date());
        }
        tprRecord.setUnloadGateOutName(TokenManager.userName);
        TPRInformation.insertUpdateTpr(conn, tprRecord);
        if(tprBlockManager != null)
        	tprBlockManager.setTprBlockStatus(conn, tprRecord.getTprId(),TokenManager.userId);
    }

    private int InsertTPRStep(Connection conn,boolean isDeny) throws Exception {
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
            RFIDMasterDao.insert(conn, tpStep,false);
            RFIDMasterDao.insert(conn, tpStep,true);
        } else {
            long currTimeServerMillis = System.currentTimeMillis();
            tpStep.setExitTime(new Date(currTimeServerMillis));
            tpStep.setUpdatedOn(new Date(currTimeServerMillis));
            tpStep.setHasValidRf(isTagRead ? 1 : 0);
            tpStep.setMaterialCat(TokenManager.materialCat);
            tpStep.setSaveStatus(isDeny ? TPStep.REQUEST_OVERRIDE : TPStep.SAVE_AND_CONTINUE);
            RFIDMasterDao.update(conn, tpStep,false);
            RFIDMasterDao.update(conn, tpStep,true);
        }

        return tpStep.getId();
    }

    private void setWhiteBackColor() {
        vehicleName.setBackground(UIConstant.PanelWhite);
        challanNo.setBackground(UIConstant.PanelWhite);
//        reasonAllowExit.setBackground(UIConstant.PanelWhite);
    }

    private void changeToQuickCreateMode() {
        vehicleName.setEditable(true);
        vehicleName.setFocusable(true);
        vehicleName.requestFocusInWindow();
        vehicleName.setTextBackground(UIConstant.focusPanelColor);
        vehicleName.setText("");
        vehicleName.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
    }

    private void getFocus() {
        if (vehicleName.isTextEditable()) {
            setWhiteBackColor();
            vehicleName.requestFocusInWindow();
            vehicleName.setTextBackground(UIConstant.focusPanelColor);
        } else {
            setWhiteBackColor();
        }
    }

    private void quickCreateAndSave() {
    	Connection conn = null;
    	boolean destroyIt = false;
    	try{
    		conn = DBConnectionPool.getConnectionFromPoolNonWeb();
    		setWhiteBackColor();
    		if(isRequestOverride){
    			requestOverrideAction();
    			return;
    		}
    		else if (tprRecord == null) {
    			if (vehicleName.isTextEditable() && Utils.isNull(vehicleName.getText())) {
    				JOptionPane.showMessageDialog(null, "Please Enter Vehicle");
    			}

    			setWhiteBackColor();
    			changeToQuickCreateMode();
    			return;
    		} 
    		else {
    			updateTPR(conn);
    			InsertTPRStep(conn,false);
    			conn.commit();
    			JOptionPane.showMessageDialog(null, "Detail Saved");
    			Barrier.openEntryGate();
    			clearInputs(conn, false);
    			getFocus();
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

    private void stopRfid() {
        if (rfidHandler != null) {
            rfidHandler.stop();
        }
    }

    private void requestOverrideAction() {
        Connection conn = null;
        boolean destroyIt = false;
    	try {
    		conn = DBConnectionPool.getConnectionFromPoolNonWeb();
            updateTPR(conn, true);
            InsertTPRStep(conn,true);
            //TPRBlockStatusHelper.allowCurrentStep(conn, tprRecord.getVehicleId(), tprRecord, TokenManager.currWorkStationId, TokenManager.currWorkStationType, TokenManager.userId, true, true);
            conn.commit();
            clearInputs(conn,false);
            getFocus();
        } catch(Exception ex){
    		ex.printStackTrace();
    	}finally{
    		try{
    			DBConnectionPool.returnConnectionToPoolNonWeb(conn, destroyIt);
    		}catch(Exception ex){
    			ex.printStackTrace();
    		}
    	}
    }

    private void clearAction() {
    	Connection conn = null;
    	boolean destroyIt = false;
    	try{
    		conn = DBConnectionPool.getConnectionFromPoolNonWeb();
    		clearInputs(conn, true);
    		getFocus();
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

    private void vehicleNameAction() {
    	int vehId = Misc.getUndefInt();
    	Connection conn = null;
    	boolean destroyIt = false;
    	try{
    		conn = DBConnectionPool.getConnectionFromPoolNonWeb();
    		if (Utils.isNull(vehicleName.getText())) {
    			JOptionPane.showMessageDialog(null, "Please Enter Vehicle !!!");
    			return;
    		} else {
    			vehicleName.setText(CacheTrack.standardizeName(vehicleName.getText()));
    			Pair<Integer, String> vehPair = TPRInformation.getVehicle(conn, null, vehicleName.getText());
    			if (vehPair != null) {
    				vehId = vehPair.first;
    			}
    			if (vehId != Misc.getUndefInt()) {
    				isVehicleExist = true;
    			}
    			if (!isVehicleExist) {
    				JOptionPane.showMessageDialog(null, "Invalid Vehicle,please enter valid vehicle");
    				/*Object[] options = {"  Re-Enter  ", "  Continue  "};
    				String msg = "Invalid Vehicle Please Go to Registration Office";
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
    				}*/
    			} else {
    				setWhiteBackColor();
    				setTPRecord(conn, vehicleName.getText());
    			}
    		}
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
}