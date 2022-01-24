/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ipssi.rfid.ui;

import com.ipssi.fingerprint.utils.SynServiceHandler;
import com.ipssi.fingerprint.utils.SyncFingerPrintDeviceHelper;
import com.ipssi.gen.exception.GenericException;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import morpho.morphosmart.sdk.api.MorphoSmartSDK;
import morpho.morphosmart.sdk.demo.MsoConnection;
import morpho.morphosmart.sdk.demo.constant.MorphoConstant;

import com.ipssi.gen.utils.DBConnectionPool;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.Pair;
import com.ipssi.rfid.beans.TPRecord;
import com.ipssi.rfid.beans.TPSQuestionDetail;
import com.ipssi.rfid.beans.Token;
import com.ipssi.rfid.camera.WebcamExecutable;
import com.ipssi.rfid.constant.Results;
import com.ipssi.rfid.constant.Status;
import com.ipssi.rfid.constant.UIConstant;
import com.ipssi.rfid.constant.Status.TPRQuestion;
import com.ipssi.rfid.database.GateInDao;
import com.ipssi.rfid.db.RFIDMasterDao;
import com.ipssi.rfid.processor.TPRBlockManager;
import com.ipssi.rfid.processor.TokenManager;
import com.ipssi.rfid.processor.Utils;
import com.ipssi.rfid.readers.UIHandler;
import com.scl.loadlibrary.BioMatricBean;
import com.scl.loadlibrary.CapturePanel;
import com.scl.loadlibrary.DriverBean;
import com.scl.loadlibrary.FingerPrintAction;
import com.scl.loadlibrary.LoadLibrary;
import com.scl.loadlibrary.MorphoSmartFunctions;

/**
 *
 * @author Vi$ky
 */
public class DriverRegistrationWindow extends javax.swing.JDialog {

    UIConstant ApplicationConstant;
    ButtonGroup driverType = new ButtonGroup();
    ButtonGroup hmv = new ButtonGroup();
    ButtonGroup lmv = new ButtonGroup();
    private BufferedImage bi = null;
    public static BioMatricBean biometricBean = null;
    private boolean isEnterPressedOnDLNo = false;
    private int isSave = 1;// for Save ,0 for not save,2 for update
    private int isUpdate = 2;
    private int tprId = Misc.getUndefInt();
    public int driverId = Misc.getUndefInt();
    private String vehicleName = "";
    private UIHandler handler = null;
    private TPRecord tpRecord = null;
    private int userId = Misc.getUndefInt();
    private ArrayList<Integer> deleteDriverList = null;
    private ArrayList<byte[]> fingerTemplateList = null;
    private ArrayList<byte[]> fingerImageList = null;
    TPRBlockManager tprBlockManager = null;
    boolean isMorphoExist = false;
    public DriverRegistrationWindow(java.awt.Frame parent, boolean modal, UIHandler handler, TPRecord tpRecord, TPRBlockManager tprBlockManager, int userId) {
    	super(parent, modal);
    	initComponents();
    	this.setLocation(210,-2);
    	isMorphoExist  = TokenManager.morphoDeviceExist == 1 && (TokenManager.useSDK() ?  MorphoSmartFunctions.getMorpho().isConnected() : LoadLibrary.isMorphoConnected() );
    	this.handler = handler;
    	this.tpRecord = tpRecord;
    	this.tprBlockManager = tprBlockManager;
    	this.tprId = tpRecord != null ? tpRecord.getTprId() : Misc.getUndefInt();
    	this.driverId = tpRecord != null ? tpRecord.getDriverId() : Misc.getUndefInt();
    	driverType.add(driver);
    	driverType.add(helper);
    	driverType.add(supervisor);
    	driverType.add(other);
    	lmv.add(lmvYes);
    	lmv.add(lmvNo);
    	lmv.add(lmvNc);
    	hmv.add(hmvYes);
    	hmv.add(hmvNo);
    	hmv.add(hmvNC);
    	setValues();
    	if(!Misc.isUndef(driverId)){
    		Connection conn = null;
    		boolean destroyIt = false;
    		try{
    			clearInputs();
    			conn = DBConnectionPool.getConnectionFromPoolNonWeb();
    			biometricBean = GateInDao.getDriverDetails(conn, driverId+"", "driverId");
    			if (biometricBean != null && !Misc.isUndef(biometricBean.getDriverId())) {
    				showExistingDriverDetails();
    				if (biometricBean.getIsfingerCaptured() == 1) {
    					biometricBean.setIsSave(2);
    				}

    			} else {
    				clearInputs();
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
    		gatePassId.setEditable(false);
    		clearButton.setVisible(false);
    	}
    	driverTypePanel.requestFocusInWindow();
    	driverTypePanel.setBackground(UIConstant.focusPanelColor);
    	
    	/*new Thread(new Runnable() {
		@Override
		public void run() {
			try{
				FingerPrintSyncDialog fSyncDialog = new FingerPrintSyncDialog();
				fSyncDialog.setVisible(true);
				fSyncDialog.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				fSyncDialog.start();
			}catch(Exception ex){
				ex.printStackTrace();
			}
		}
    	}).start();*/
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
        jLabel6 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        mobile1 = new javax.swing.JTextField();
        fatherName = new javax.swing.JTextField();
        Address = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        name = new javax.swing.JTextField();
        adharNo = new javax.swing.JTextField();
        dl = new javax.swing.JTextField();
        panNo = new javax.swing.JTextField();
        jLabel19 = new javax.swing.JLabel();
        capturePhoto = new javax.swing.JButton();
        photo = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel21 = new javax.swing.JLabel();
        hmvPanel = new javax.swing.JPanel();
        hmvNo = new javax.swing.JCheckBox();
        hmvYes = new javax.swing.JCheckBox();
        hmvNC = new javax.swing.JCheckBox();
        dob = new com.toedter.calendar.JDateChooser();
        jButton1 = new javax.swing.JButton();
        jLabel20 = new javax.swing.JLabel();
        gatePassId = new javax.swing.JTextField();
        jButton2 = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        mobile2 = new javax.swing.JTextField();
        voterId = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        captureFinger = new javax.swing.JButton();
        jLabel24 = new javax.swing.JLabel();
        lmvPanel = new javax.swing.JPanel();
        lmvNo = new javax.swing.JCheckBox();
        lmvYes = new javax.swing.JCheckBox();
        lmvNc = new javax.swing.JCheckBox();
        dlExpiryDate = new com.toedter.calendar.JDateChooser();
        clearButton = new javax.swing.JButton();
        Save = new javax.swing.JButton();
        jPanel5 = new javax.swing.JPanel();
        jLabel15 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        jLabel28 = new javax.swing.JLabel();
        isDriverExist = new javax.swing.JLabel();
        isFingerExist = new javax.swing.JLabel();
        isFingerCaptured = new javax.swing.JLabel();
        isFingerVarified = new javax.swing.JLabel();
        challanInfo = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        driverTypePanel = new javax.swing.JPanel();
        helper = new javax.swing.JCheckBox();
        driver = new javax.swing.JCheckBox();
        supervisor = new javax.swing.JCheckBox();
        other = new javax.swing.JCheckBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel2.setBackground(java.awt.SystemColor.controlLtHighlight);

        jLabel6.setFont(ApplicationConstant.subHeadingFont);
        jLabel6.setText("Driver/Helper");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 207, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(226, 226, 226)
                .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 201, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 304, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 6, Short.MAX_VALUE))
        );

        jPanel1.add(jPanel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 0, -1, -1));

        jPanel3.setBackground(new java.awt.Color(255, 255, 255));
        jPanel3.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel2.setFont(ApplicationConstant.labelFont);
        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel2.setText("Father's Name* :");
        jLabel2.setMaximumSize(new java.awt.Dimension(163, 163));
        jPanel3.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(23, 116, 163, 30));

        jLabel3.setFont(ApplicationConstant.labelFont);
        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel3.setText("Address* :");
        jLabel3.setMaximumSize(new java.awt.Dimension(163, 163));
        jPanel3.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(23, 152, 163, 30));

        jLabel4.setFont(ApplicationConstant.labelFont);
        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel4.setText("Mobile 1* :");
        jLabel4.setMaximumSize(new java.awt.Dimension(163, 163));
        jPanel3.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(23, 188, 163, 30));

        jLabel5.setFont(ApplicationConstant.labelFont);
        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel5.setText("DOB* :");
        jLabel5.setMaximumSize(new java.awt.Dimension(163, 163));
        jPanel3.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(23, 224, 163, 30));

        mobile1.setFont(ApplicationConstant.textFont);
        mobile1.setForeground(UIConstant.textFontColor);
        mobile1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        mobile1.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                mobile1FocusGained(evt);
            }
        });
        mobile1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                mobile1KeyPressed(evt);
            }
        });
        jPanel3.add(mobile1, new org.netbeans.lib.awtextra.AbsoluteConstraints(189, 188, 215, 30));

        fatherName.setFont(ApplicationConstant.textFont);
        fatherName.setForeground(UIConstant.textFontColor);
        fatherName.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        fatherName.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fatherNameActionPerformed(evt);
            }
        });
        fatherName.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                fatherNameFocusGained(evt);
            }
        });
        fatherName.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                fatherNameKeyPressed(evt);
            }
        });
        jPanel3.add(fatherName, new org.netbeans.lib.awtextra.AbsoluteConstraints(189, 116, 215, 30));

        Address.setFont(ApplicationConstant.textFont);
        Address.setForeground(UIConstant.textFontColor);
        Address.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        Address.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                AddressFocusGained(evt);
            }
        });
        Address.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                AddressKeyPressed(evt);
            }
        });
        jPanel3.add(Address, new org.netbeans.lib.awtextra.AbsoluteConstraints(189, 152, 215, 30));

        jLabel7.setFont(ApplicationConstant.labelFont);
        jLabel7.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel7.setText("Name* :");
        jLabel7.setMaximumSize(new java.awt.Dimension(163, 163));
        jPanel3.add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(23, 80, 163, 30));

        jLabel8.setFont(ApplicationConstant.labelFont);
        jLabel8.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel8.setText("Aadhaar:");
        jLabel8.setMaximumSize(new java.awt.Dimension(163, 163));
        jPanel3.add(jLabel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(23, 260, 163, 30));

        jLabel9.setFont(ApplicationConstant.labelFont);
        jLabel9.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel9.setText("DL No:");
        jLabel9.setMaximumSize(new java.awt.Dimension(163, 163));
        jPanel3.add(jLabel9, new org.netbeans.lib.awtextra.AbsoluteConstraints(23, 47, 163, 30));

        name.setFont(ApplicationConstant.textFont);
        name.setForeground(UIConstant.textFontColor);
        name.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        name.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nameActionPerformed(evt);
            }
        });
        name.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                nameFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                nameFocusLost(evt);
            }
        });
        name.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                nameKeyPressed(evt);
            }
        });
        jPanel3.add(name, new org.netbeans.lib.awtextra.AbsoluteConstraints(189, 80, 215, 30));

        adharNo.setFont(ApplicationConstant.textFont);
        adharNo.setForeground(UIConstant.textFontColor);
        adharNo.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        adharNo.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                adharNoFocusGained(evt);
            }
        });
        adharNo.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                adharNoKeyPressed(evt);
            }
        });
        jPanel3.add(adharNo, new org.netbeans.lib.awtextra.AbsoluteConstraints(189, 260, 215, 30));

        dl.setFont(ApplicationConstant.textFont);
        dl.setForeground(UIConstant.textFontColor);
        dl.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        dl.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                dlFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                dlFocusLost(evt);
            }
        });
        dl.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                dlKeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                dlKeyReleased(evt);
            }
        });
        jPanel3.add(dl, new org.netbeans.lib.awtextra.AbsoluteConstraints(189, 47, 215, 30));

        panNo.setFont(ApplicationConstant.textFont);
        panNo.setForeground(UIConstant.textFontColor);
        panNo.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        panNo.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                panNoFocusGained(evt);
            }
        });
        panNo.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                panNoKeyPressed(evt);
            }
        });
        jPanel3.add(panNo, new org.netbeans.lib.awtextra.AbsoluteConstraints(189, 296, 215, 30));

        jLabel19.setFont(ApplicationConstant.labelFont);
        jLabel19.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel19.setText("PAN Card:");
        jLabel19.setMaximumSize(new java.awt.Dimension(163, 163));
        jPanel3.add(jLabel19, new org.netbeans.lib.awtextra.AbsoluteConstraints(23, 296, 163, 30));

        capturePhoto.setText("Capture");
        capturePhoto.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                capturePhotoMouseClicked(evt);
            }
        });
        capturePhoto.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                capturePhotoActionPerformed(evt);
            }
        });
        capturePhoto.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                capturePhotoKeyPressed(evt);
            }
        });
        jPanel3.add(capturePhoto, new org.netbeans.lib.awtextra.AbsoluteConstraints(187, 538, 170, 30));

        photo.setText("                       Photo");
        photo.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        photo.setFocusable(false);
        jPanel3.add(photo, new org.netbeans.lib.awtextra.AbsoluteConstraints(187, 368, 170, 164));

        jLabel11.setFont(ApplicationConstant.labelFont);
        jLabel11.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel11.setText("Photo:");
        jPanel3.add(jLabel11, new org.netbeans.lib.awtextra.AbsoluteConstraints(23, 368, 124, 30));

        jLabel21.setFont(ApplicationConstant.labelFont);
        jLabel21.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel21.setText("HMV:");
        jLabel21.setMaximumSize(new java.awt.Dimension(163, 163));
        jPanel3.add(jLabel21, new org.netbeans.lib.awtextra.AbsoluteConstraints(23, 332, 163, 30));

        hmvPanel.setBackground(new java.awt.Color(255, 255, 255));
        hmvPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                hmvPanelMouseClicked(evt);
            }
        });
        hmvPanel.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                hmvPanelFocusGained(evt);
            }
        });
        hmvPanel.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                hmvPanelKeyPressed(evt);
            }
        });

        hmvNo.setText("No");
        hmvNo.setFocusable(false);
        hmvNo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                hmvNoActionPerformed(evt);
            }
        });
        hmvNo.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                hmvNoKeyPressed(evt);
            }
        });

        hmvYes.setText("Yes");
        hmvYes.setFocusable(false);
        hmvYes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                hmvYesActionPerformed(evt);
            }
        });

        hmvNC.setText("NC");
        hmvNC.setFocusable(false);
        hmvNC.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                hmvNCActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout hmvPanelLayout = new javax.swing.GroupLayout(hmvPanel);
        hmvPanel.setLayout(hmvPanelLayout);
        hmvPanelLayout.setHorizontalGroup(
            hmvPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, hmvPanelLayout.createSequentialGroup()
                .addComponent(hmvYes)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(hmvNo)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(hmvNC, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        hmvPanelLayout.setVerticalGroup(
            hmvPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(hmvPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(hmvNo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(hmvYes, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(hmvNC, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jPanel3.add(hmvPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(192, 332, -1, -1));

        dob.setBackground(new java.awt.Color(255, 255, 255));
        dob.setDateFormatString("dd/MM/yyyy");
        dob.setFont(UIConstant.textFont);
        dob.setForeground(UIConstant.textFontColor);
        jPanel3.add(dob, new org.netbeans.lib.awtextra.AbsoluteConstraints(189, 224, 215, 30));

        jButton1.setBackground(new java.awt.Color(255, 255, 255));
        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/ipssi/rfid/ui/Search.png"))); // NOI18N
        jButton1.setBorder(null);
        jButton1.setFocusable(false);
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        jPanel3.add(jButton1, new org.netbeans.lib.awtextra.AbsoluteConstraints(410, 47, -1, 30));

        jLabel20.setFont(ApplicationConstant.labelFont);
        jLabel20.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel20.setText("Gate Pass ID:");
        jLabel20.setMaximumSize(new java.awt.Dimension(163, 163));
        jPanel3.add(jLabel20, new org.netbeans.lib.awtextra.AbsoluteConstraints(23, 11, 163, 30));

        gatePassId.setFont(ApplicationConstant.textFont);
        gatePassId.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        gatePassId.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                gatePassIdActionPerformed(evt);
            }
        });
        gatePassId.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                gatePassIdFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                gatePassIdFocusLost(evt);
            }
        });
        gatePassId.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                gatePassIdKeyPressed(evt);
            }
        });
        jPanel3.add(gatePassId, new org.netbeans.lib.awtextra.AbsoluteConstraints(189, 11, 215, 30));

        jButton2.setBackground(new java.awt.Color(255, 255, 255));
        jButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/ipssi/rfid/ui/Search.png"))); // NOI18N
        jButton2.setBorder(null);
        jButton2.setFocusable(false);
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });
        jPanel3.add(jButton2, new org.netbeans.lib.awtextra.AbsoluteConstraints(410, 11, -1, 30));

        jPanel1.add(jPanel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 103, -1, -1));

        jPanel4.setBackground(new java.awt.Color(255, 255, 255));
        jPanel4.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        mobile2.setFont(ApplicationConstant.textFont);
        mobile2.setForeground(UIConstant.textFontColor);
        mobile2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        mobile2.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                mobile2FocusGained(evt);
            }
        });
        mobile2.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                mobile2KeyPressed(evt);
            }
        });
        jPanel4.add(mobile2, new org.netbeans.lib.awtextra.AbsoluteConstraints(166, 56, 215, 30));

        voterId.setFont(ApplicationConstant.textFont);
        voterId.setForeground(UIConstant.textFontColor);
        voterId.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        voterId.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                voterIdFocusGained(evt);
            }
        });
        voterId.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                voterIdKeyPressed(evt);
            }
        });
        jPanel4.add(voterId, new org.netbeans.lib.awtextra.AbsoluteConstraints(166, 128, 215, 30));

        jLabel12.setFont(ApplicationConstant.labelFont);
        jLabel12.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel12.setText("Mobile 2:");
        jPanel4.add(jLabel12, new org.netbeans.lib.awtextra.AbsoluteConstraints(18, 56, 144, 30));

        jLabel13.setFont(ApplicationConstant.labelFont);
        jLabel13.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel13.setText("DL Expiry Date* :");
        jPanel4.add(jLabel13, new org.netbeans.lib.awtextra.AbsoluteConstraints(18, 92, 144, 30));

        jLabel14.setFont(ApplicationConstant.labelFont);
        jLabel14.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel14.setText("LMV:");
        jPanel4.add(jLabel14, new org.netbeans.lib.awtextra.AbsoluteConstraints(18, 206, 144, 30));

        captureFinger.setFont(UIConstant.textFont);
        captureFinger.setText("Capture Fingure Print");
        captureFinger.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                captureFingerMouseClicked(evt);
            }
        });
        captureFinger.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                captureFingerActionPerformed(evt);
            }
        });
        captureFinger.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                captureFingerKeyPressed(evt);
            }
        });
        jPanel4.add(captureFinger, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 299, 238, 43));

        jLabel24.setFont(ApplicationConstant.labelFont);
        jLabel24.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel24.setText("Voter Id:");
        jPanel4.add(jLabel24, new org.netbeans.lib.awtextra.AbsoluteConstraints(18, 128, 144, 30));

        lmvPanel.setBackground(new java.awt.Color(255, 255, 255));
        lmvPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lmvPanelMouseClicked(evt);
            }
        });
        lmvPanel.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                lmvPanelKeyPressed(evt);
            }
        });

        lmvNo.setText("No");
        lmvNo.setFocusable(false);
        lmvNo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                lmvNoActionPerformed(evt);
            }
        });
        lmvNo.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                lmvNoKeyPressed(evt);
            }
        });

        lmvYes.setText("Yes");
        lmvYes.setFocusable(false);
        lmvYes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                lmvYesActionPerformed(evt);
            }
        });

        lmvNc.setText("NC");
        lmvNc.setFocusable(false);
        lmvNc.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                lmvNcActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout lmvPanelLayout = new javax.swing.GroupLayout(lmvPanel);
        lmvPanel.setLayout(lmvPanelLayout);
        lmvPanelLayout.setHorizontalGroup(
            lmvPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, lmvPanelLayout.createSequentialGroup()
                .addComponent(lmvYes)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lmvNo, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lmvNc)
                .addContainerGap())
        );
        lmvPanelLayout.setVerticalGroup(
            lmvPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(lmvPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(lmvNo, javax.swing.GroupLayout.DEFAULT_SIZE, 25, Short.MAX_VALUE)
                .addComponent(lmvYes, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(lmvNc))
        );

        jPanel4.add(lmvPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(166, 206, -1, -1));

        dlExpiryDate.setBackground(new java.awt.Color(255, 255, 255));
        dlExpiryDate.setDateFormatString("dd/MM/yyyy");
        dlExpiryDate.setFont(UIConstant.textFont);
        dlExpiryDate.setForeground(UIConstant.textFontColor);
        dlExpiryDate.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                dlExpiryDateFocusGained(evt);
            }
        });
        dlExpiryDate.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                dlExpiryDateKeyPressed(evt);
            }
        });
        jPanel4.add(dlExpiryDate, new org.netbeans.lib.awtextra.AbsoluteConstraints(166, 92, 215, 30));

        jPanel1.add(jPanel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(473, 228, -1, -1));

        clearButton.setFont(ApplicationConstant.buttonFont);
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
        jPanel1.add(clearButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(439, 677, 116, 40));

        Save.setFont(ApplicationConstant.buttonFont);
        Save.setText("Save");
        Save.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SaveActionPerformed(evt);
            }
        });
        Save.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                SaveKeyPressed(evt);
            }
        });
        jPanel1.add(Save, new org.netbeans.lib.awtextra.AbsoluteConstraints(605, 677, 143, 40));

        jPanel5.setBackground(new java.awt.Color(255, 255, 255));
        jPanel5.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel15.setVisible(true);
        jLabel15.setFont(ApplicationConstant.labelFont);
        jLabel15.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel15.setText("Driver Exist:");
        jPanel5.add(jLabel15, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 2, 124, 25));

        jLabel16.setVisible(true);
        jLabel16.setFont(ApplicationConstant.labelFont);
        jLabel16.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel16.setText("Finger Exist:");
        jPanel5.add(jLabel16, new org.netbeans.lib.awtextra.AbsoluteConstraints(19, 29, 125, 25));

        jLabel17.setVisible(true);
        jLabel17.setFont(ApplicationConstant.labelFont);
        jLabel17.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel17.setText("Finger Captured:");
        jPanel5.add(jLabel17, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 56, 134, 25));

        jLabel18.setVisible(true);
        jLabel18.setFont(ApplicationConstant.labelFont);
        jLabel18.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel18.setText("Finger Identify:");
        jPanel5.add(jLabel18, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 83, 132, 23));

        jLabel28.setVisible(false);
        jLabel28.setFont(ApplicationConstant.labelFont);
        jLabel28.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel28.setText(" Challan Info:");
        jPanel5.add(jLabel28, new org.netbeans.lib.awtextra.AbsoluteConstraints(28, 108, 114, 25));

        isDriverExist.setFont(UIConstant.textFont);
        isDriverExist.setVisible(true);
        isDriverExist.setForeground(UIConstant.textFontColor);
        jPanel5.add(isDriverExist, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 2, 87, 25));

        isFingerExist.setVisible(true);
        isFingerExist.setFont(UIConstant.textFont);
        isFingerExist.setForeground(UIConstant.textFontColor);
        jPanel5.add(isFingerExist, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 29, 87, 25));

        isFingerCaptured.setVisible(true);
        isFingerCaptured.setFont(UIConstant.textFont);
        isFingerCaptured.setForeground(UIConstant.textFontColor);
        jPanel5.add(isFingerCaptured, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 56, 87, 25));

        isFingerVarified.setVisible(true);
        isFingerVarified.setFont(UIConstant.textFont);
        isFingerVarified.setForeground(UIConstant.textFontColor);
        jPanel5.add(isFingerVarified, new org.netbeans.lib.awtextra.AbsoluteConstraints(148, 83, 89, 23));

        challanInfo.setVisible(false);
        challanInfo.setFont(UIConstant.textFont);
        challanInfo.setForeground(UIConstant.textFontColor);
        jPanel5.add(challanInfo, new org.netbeans.lib.awtextra.AbsoluteConstraints(148, 108, 89, 25));

        jPanel1.add(jPanel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(461, 58, -1, -1));

        jLabel10.setFont(ApplicationConstant.labelFont);
        jLabel10.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel10.setText("Type:");
        jPanel1.add(jLabel10, new org.netbeans.lib.awtextra.AbsoluteConstraints(38, 62, 144, 30));

        driverTypePanel.setBackground(new java.awt.Color(255, 255, 255));
        driverTypePanel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                driverTypePanelMouseClicked(evt);
            }
        });
        driverTypePanel.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                driverTypePanelKeyPressed(evt);
            }
        });

        helper.setText("Helper");
        helper.setFocusable(false);
        helper.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                helperActionPerformed(evt);
            }
        });
        helper.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                helperKeyPressed(evt);
            }
        });

        driver.setText("Driver");
        driver.setFocusable(false);
        driver.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                driverActionPerformed(evt);
            }
        });

        supervisor.setText("Supervisor");
        supervisor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                supervisorActionPerformed(evt);
            }
        });

        other.setText("Other");
        other.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                otherActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout driverTypePanelLayout = new javax.swing.GroupLayout(driverTypePanel);
        driverTypePanel.setLayout(driverTypePanelLayout);
        driverTypePanelLayout.setHorizontalGroup(
            driverTypePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, driverTypePanelLayout.createSequentialGroup()
                .addComponent(driver)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(helper)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(supervisor)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(other, javax.swing.GroupLayout.DEFAULT_SIZE, 58, Short.MAX_VALUE)
                .addContainerGap())
        );
        driverTypePanelLayout.setVerticalGroup(
            driverTypePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(driverTypePanelLayout.createSequentialGroup()
                .addGroup(driverTypePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(other, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 31, Short.MAX_VALUE)
                    .addComponent(driver, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(helper, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(supervisor, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(0, 0, Short.MAX_VALUE))
        );

        jPanel1.add(driverTypePanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(186, 62, -1, 30));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 958, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void mobile1FocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_mobile1FocusGained
        setPanelWhite();
        mobile1.setBackground(UIConstant.focusPanelColor);
    }//GEN-LAST:event_mobile1FocusGained

    private void mobile1KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_mobile1KeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            dob.requestFocusInWindow();
        }
    }//GEN-LAST:event_mobile1KeyPressed

    private void fatherNameFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_fatherNameFocusGained
        setPanelWhite();
        fatherName.setBackground(UIConstant.focusPanelColor);
    }//GEN-LAST:event_fatherNameFocusGained

    private void fatherNameKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_fatherNameKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            Address.requestFocusInWindow();
        }
    }//GEN-LAST:event_fatherNameKeyPressed

    private void AddressFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_AddressFocusGained
        setPanelWhite();
        Address.setBackground(UIConstant.focusPanelColor);
    }//GEN-LAST:event_AddressFocusGained

    private void AddressKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_AddressKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            mobile1.requestFocusInWindow();
        }
    }//GEN-LAST:event_AddressKeyPressed

    private void nameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nameActionPerformed
    }//GEN-LAST:event_nameActionPerformed

    private void nameFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_nameFocusGained
        setPanelWhite();
        name.setBackground(UIConstant.focusPanelColor);
    }//GEN-LAST:event_nameFocusGained

    private void nameKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_nameKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            setPanelWhite();
            fatherName.requestFocusInWindow();
        }
    }//GEN-LAST:event_nameKeyPressed

    private void adharNoFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_adharNoFocusGained
        setPanelWhite();
        adharNo.setBackground(UIConstant.focusPanelColor);
    }//GEN-LAST:event_adharNoFocusGained

    private void adharNoKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_adharNoKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            panNo.requestFocusInWindow();
        }
    }//GEN-LAST:event_adharNoKeyPressed

    private void dlFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_dlFocusGained
        setPanelWhite();
        dl.setBackground(UIConstant.focusPanelColor);
    }//GEN-LAST:event_dlFocusGained

    private void dlKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_dlKeyPressed
    	if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            if (biometricBean == null) {
                eventOnDlNo();
            } else {
                name.setBackground(UIConstant.focusPanelColor);
                name.requestFocusInWindow();
            }
        }
    }//GEN-LAST:event_dlKeyPressed
    private void eventOnDlNo() {
    	biometricBean = null;
    	setWhiteBackground();
    	Connection conn = null;
    	boolean destroyIt = false;
    	try{
    		conn = DBConnectionPool.getConnectionFromPoolNonWeb();
    		clearInputs();
    		if (Utils.isNull(dl.getText())) {
    			JOptionPane.showMessageDialog(null, "Please Enter Driver Lisence No!!!");
    			setWhiteBackground();
    			dl.setBackground(UIConstant.focusPanelColor);
    			dl.requestFocusInWindow();
    			return;
    		}
    		isEnterPressedOnDLNo = true;
    		biometricBean = GateInDao.getDriverDetails(conn, dl.getText().trim(), "dlNo");
    		if (biometricBean != null && biometricBean.getDriverId() != Misc.getUndefInt()) {
    			showExistingDriverDetails();
    			if (biometricBean.getIsfingerCaptured() == 1) {
    				biometricBean.setIsSave(2);
    			}

    		} else {
    			clearInputs();
    		}
    		name.requestFocusInWindow();
    		name.setBackground(UIConstant.focusPanelColor);
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
 
    private void panNoFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_panNoFocusGained
        setPanelWhite();
        panNo.setBackground(UIConstant.focusPanelColor);
    }//GEN-LAST:event_panNoFocusGained

    private void panNoKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_panNoKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            hmvPanel.setBackground(UIConstant.focusPanelColor);
            hmvPanel.requestFocusInWindow();
        }
    }//GEN-LAST:event_panNoKeyPressed

    private void helperActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_helperActionPerformed
        setPanelWhite();
//        dl.requestFocusInWindow();
//        dl.setBackground(UIConstant.focusPanelColor);
    }//GEN-LAST:event_helperActionPerformed

    private void helperKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_helperKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_helperKeyPressed

    private void driverActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_driverActionPerformed
        setPanelWhite();
//        dl.requestFocusInWindow();
//        dl.setBackground(UIConstant.focusPanelColor);
    }//GEN-LAST:event_driverActionPerformed

    private void driverTypePanelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_driverTypePanelMouseClicked
        setPanelWhite();
        driverTypePanel.requestFocusInWindow();
        driverTypePanel.setBackground(UIConstant.focusPanelColor);
    }//GEN-LAST:event_driverTypePanelMouseClicked

    private void driverTypePanelKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_driverTypePanelKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            driverTypePanel.setBackground(Color.WHITE);
            gatePassId.requestFocusInWindow();
        } else if (evt.getKeyCode() == KeyEvent.VK_1 || evt.getKeyCode() == KeyEvent.VK_NUMPAD1) {
//            driverTypePanel.setBackground(Color.WHITE);
            driver.setSelected(true);
//            dl.requestFocusInWindow();
        } else if (evt.getKeyCode() == KeyEvent.VK_2 || evt.getKeyCode() == KeyEvent.VK_NUMPAD2) {
//            driverTypePanel.setBackground(Color.WHITE);
//            dl.requestFocusInWindow();
            helper.setSelected(true);
        } else if (evt.getKeyCode() == KeyEvent.VK_3 || evt.getKeyCode() == KeyEvent.VK_NUMPAD3) {
//            driverTypePanel.setBackground(Color.WHITE);
//            dl.requestFocusInWindow();
            supervisor.setSelected(true);
        } else if (evt.getKeyCode() == KeyEvent.VK_4 || evt.getKeyCode() == KeyEvent.VK_NUMPAD4) {
//            driverTypePanel.setBackground(Color.WHITE);
//            dl.requestFocusInWindow();
            other.setSelected(true);
        }
    }//GEN-LAST:event_driverTypePanelKeyPressed

    private void mobile2FocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_mobile2FocusGained
        setPanelWhite();
        mobile2.setBackground(UIConstant.focusPanelColor);
    }//GEN-LAST:event_mobile2FocusGained

    private void mobile2KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_mobile2KeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            setPanelWhite();
            dlExpiryDate.requestFocusInWindow();
        }
    }//GEN-LAST:event_mobile2KeyPressed

    private void voterIdFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_voterIdFocusGained
        setPanelWhite();
        voterId.setBackground(UIConstant.focusPanelColor);
    }//GEN-LAST:event_voterIdFocusGained

    private void voterIdKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_voterIdKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            setPanelWhite();
            lmvPanel.requestFocusInWindow();
            lmvPanel.setBackground(UIConstant.focusPanelColor);
        }
    }//GEN-LAST:event_voterIdKeyPressed

    private void captureFingerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_captureFingerActionPerformed
        fingerCapturedAction();
    }//GEN-LAST:event_captureFingerActionPerformed

    private void lmvNoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_lmvNoActionPerformed
        setPanelWhite();
        capturePhoto.requestFocusInWindow();
    }//GEN-LAST:event_lmvNoActionPerformed

    private void lmvNoKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_lmvNoKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_lmvNoKeyPressed

    private void lmvYesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_lmvYesActionPerformed
        setPanelWhite();
        capturePhoto.requestFocusInWindow();
        // challanInfoPanel.setBackground(Color.WHITE);
    }//GEN-LAST:event_lmvYesActionPerformed

    private void lmvPanelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lmvPanelMouseClicked
        setPanelWhite();
        lmvPanel.requestFocusInWindow();
        lmvPanel.setBackground(UIConstant.focusPanelColor);
    }//GEN-LAST:event_lmvPanelMouseClicked

    private void lmvPanelKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_lmvPanelKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            setPanelWhite();
            capturePhoto.requestFocusInWindow();
        } else if (evt.getKeyCode() == KeyEvent.VK_1) {
            setPanelWhite();
            lmvYes.setSelected(true);
            capturePhoto.requestFocusInWindow();
        } else if (evt.getKeyCode() == KeyEvent.VK_2) {
            setPanelWhite();
            lmvNo.setSelected(true);
            capturePhoto.requestFocusInWindow();
        } else if (evt.getKeyCode() == KeyEvent.VK_3) {
            setPanelWhite();
            lmvNc.setSelected(true);
            capturePhoto.requestFocusInWindow();
        }
    }//GEN-LAST:event_lmvPanelKeyPressed

    private void dlExpiryDateFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_dlExpiryDateFocusGained
        setPanelWhite();
    }//GEN-LAST:event_dlExpiryDateFocusGained

    private void dlExpiryDateKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_dlExpiryDateKeyPressed
        voterId.requestFocusInWindow();
    }//GEN-LAST:event_dlExpiryDateKeyPressed

    private void capturePhotoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_capturePhotoActionPerformed
        photoCaptureAction();
        captureFinger.requestFocusInWindow();
    }//GEN-LAST:event_capturePhotoActionPerformed

    private void clearButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearButtonActionPerformed
        biometricBean = null;
        isEnterPressedOnDLNo = false;
        gatePassId.setText("");
        dl.setText("");
        driverType.clearSelection();
        clearInputs();
    }//GEN-LAST:event_clearButtonActionPerformed

    private void SaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SaveActionPerformed
        saveAction();
    }//GEN-LAST:event_SaveActionPerformed

    private void fatherNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fatherNameActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_fatherNameActionPerformed

    private void hmvNoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hmvNoActionPerformed
        setPanelWhite();
        mobile2.requestFocusInWindow();
        mobile2.setBackground(UIConstant.focusPanelColor);
    }//GEN-LAST:event_hmvNoActionPerformed

    private void hmvNoKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_hmvNoKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_hmvNoKeyPressed

    private void hmvYesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hmvYesActionPerformed
        setPanelWhite();
        mobile2.requestFocusInWindow();
        mobile2.setBackground(UIConstant.focusPanelColor);
    }//GEN-LAST:event_hmvYesActionPerformed

    private void hmvPanelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_hmvPanelMouseClicked
        setPanelWhite();
        hmvPanel.setBackground(UIConstant.focusPanelColor);
        hmvPanel.requestFocusInWindow();
    }//GEN-LAST:event_hmvPanelMouseClicked

    private void hmvPanelKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_hmvPanelKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            setPanelWhite();
            mobile2.requestFocusInWindow();
            mobile2.setBackground(UIConstant.focusPanelColor);

        } else if (evt.getKeyCode() == KeyEvent.VK_1) {
            setPanelWhite();
            hmvYes.setSelected(true);
            mobile2.setBackground(UIConstant.focusPanelColor);
            mobile2.requestFocusInWindow();

        } else if (evt.getKeyCode() == KeyEvent.VK_2) {
            setPanelWhite();
            hmvNo.setSelected(true);
            mobile2.setBackground(UIConstant.focusPanelColor);
            mobile2.requestFocusInWindow();
        } else if (evt.getKeyCode() == KeyEvent.VK_3) {
            setPanelWhite();
            hmvNC.setSelected(true);
            mobile2.setBackground(UIConstant.focusPanelColor);
            mobile2.requestFocusInWindow();
        }
    }//GEN-LAST:event_hmvPanelKeyPressed

    private void hmvPanelFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_hmvPanelFocusGained
        setPanelWhite();
        hmvPanel.setBackground(UIConstant.focusPanelColor);
    }//GEN-LAST:event_hmvPanelFocusGained

    private void nameFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_nameFocusLost
        // TODO add your handling code here:
        setPanelWhite();
    }//GEN-LAST:event_nameFocusLost

    private void hmvNCActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hmvNCActionPerformed
        setPanelWhite();
        mobile2.requestFocusInWindow();
        mobile2.setBackground(UIConstant.focusPanelColor);
    }//GEN-LAST:event_hmvNCActionPerformed

    private void lmvNcActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_lmvNcActionPerformed
        setPanelWhite();
        capturePhoto.requestFocusInWindow();
    }//GEN-LAST:event_lmvNcActionPerformed

    private void capturePhotoKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_capturePhotoKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            setPanelWhite();
            photoCaptureAction();
            captureFinger.requestFocusInWindow();
        }
    }//GEN-LAST:event_capturePhotoKeyPressed

    private void capturePhotoMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_capturePhotoMouseClicked
        setPanelWhite();

    }//GEN-LAST:event_capturePhotoMouseClicked

    private void captureFingerKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_captureFingerKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            setPanelWhite();
            fingerCapturedAction();
//            Save.requestFocusInWindow();
        }
    }//GEN-LAST:event_captureFingerKeyPressed

    private void captureFingerMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_captureFingerMouseClicked
        setPanelWhite();
    }//GEN-LAST:event_captureFingerMouseClicked

    private void dlFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_dlFocusLost
//        eventOnDlNo();
    }//GEN-LAST:event_dlFocusLost

    private void supervisorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_supervisorActionPerformed
        setPanelWhite();
//        dl.requestFocusInWindow();
//        dl.setBackground(UIConstant.focusPanelColor);
    }//GEN-LAST:event_supervisorActionPerformed

    private void otherActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_otherActionPerformed
        setPanelWhite();
//        dl.requestFocusInWindow();
//        dl.setBackground(UIConstant.focusPanelColor);
    }//GEN-LAST:event_otherActionPerformed

    private void SaveKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_SaveKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            saveAction();
        }
    }//GEN-LAST:event_SaveKeyPressed

    private void clearButtonKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_clearButtonKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
          biometricBean = null;
            isEnterPressedOnDLNo = false;
            dl.setText("");
            clearInputs();
        }
    }//GEN-LAST:event_clearButtonKeyPressed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed

        if (biometricBean == null) {
            eventOnDlNo();
        } else {
            name.setBackground(UIConstant.focusPanelColor);
            name.requestFocusInWindow();
        }

    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        eventOngatePassId();
    }//GEN-LAST:event_jButton2ActionPerformed

    private void gatePassIdKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_gatePassIdKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {

            eventOngatePassId();

        }
    }//GEN-LAST:event_gatePassIdKeyPressed

    private void gatePassIdFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_gatePassIdFocusLost
        // TODO add your handling code here:
    }//GEN-LAST:event_gatePassIdFocusLost

    private void gatePassIdFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_gatePassIdFocusGained
        setPanelWhite();
        gatePassId.setBackground(UIConstant.focusPanelColor);
    }//GEN-LAST:event_gatePassIdFocusGained

    private void gatePassIdActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_gatePassIdActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_gatePassIdActionPerformed

    private void dlKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_dlKeyReleased
        // TODO add your handling code here:
    }//GEN-LAST:event_dlKeyReleased

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
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
//                DriverRegistrationWindow dialog = new DriverRegistrationWindow(new javax.swing.JFrame(), true,null,null);
//                dialog.addWindowListener(new java.awt.event.WindowAdapter() {
//                    @Override
//                    public void windowClosing(java.awt.event.WindowEvent e) {
//                        System.exit(0);
//                    }
//                });
//                dialog.setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField Address;
    private javax.swing.JButton Save;
    private javax.swing.JTextField adharNo;
    private javax.swing.JButton captureFinger;
    private javax.swing.JButton capturePhoto;
    private javax.swing.JLabel challanInfo;
    private javax.swing.JButton clearButton;
    public static javax.swing.JTextField dl;
    private com.toedter.calendar.JDateChooser dlExpiryDate;
    private com.toedter.calendar.JDateChooser dob;
    private javax.swing.JCheckBox driver;
    private javax.swing.JLabel isFingerCaptured;
    private javax.swing.JPanel driverTypePanel;
    private javax.swing.JTextField fatherName;
    public static javax.swing.JTextField gatePassId;
    private javax.swing.JCheckBox helper;
    private javax.swing.JCheckBox hmvNC;
    private javax.swing.JCheckBox hmvNo;
    private javax.swing.JPanel hmvPanel;
    private javax.swing.JCheckBox hmvYes;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
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
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel28;
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
    private javax.swing.JCheckBox lmvNc;
    private javax.swing.JCheckBox lmvNo;
    private javax.swing.JPanel lmvPanel;
    private javax.swing.JCheckBox lmvYes;
    private javax.swing.JTextField mobile1;
    private javax.swing.JTextField mobile2;
    private javax.swing.JLabel isFingerVarified;
    public static javax.swing.JTextField name;
    private javax.swing.JCheckBox other;
    private javax.swing.JTextField panNo;
    private javax.swing.JLabel photo;
    public static javax.swing.JLabel isDriverExist;
    private javax.swing.JCheckBox supervisor;
    private javax.swing.JLabel isFingerExist;
    private javax.swing.JTextField voterId;
    // End of variables declaration//GEN-END:variables

    private void setPanelWhite() {
        driverTypePanel.setBackground(UIConstant.PanelWhite);
        gatePassId.setBackground(UIConstant.PanelWhite);
        lmvPanel.setBackground(UIConstant.PanelWhite);
        hmvPanel.setBackground(UIConstant.PanelWhite);
        name.setBackground(UIConstant.PanelWhite);
        fatherName.setBackground(UIConstant.PanelWhite);
        Address.setBackground(UIConstant.PanelWhite);
        mobile1.setBackground(UIConstant.PanelWhite);
        dl.setBackground(UIConstant.PanelWhite);
        adharNo.setBackground(UIConstant.PanelWhite);
        panNo.setBackground(UIConstant.PanelWhite);
        mobile2.setBackground(UIConstant.PanelWhite);
        voterId.setBackground(UIConstant.PanelWhite);
    }

    private void clearInputs() {
        name.setText("");

        dob.setDate(null);
        fatherName.setText("");
        Address.setText("");
        mobile1.setText("");

        dlExpiryDate.setDate(null);
        adharNo.setText("");
        panNo.setText("");
        mobile2.setText("");
        voterId.setText("");
        photo.setIcon(null);
        photo.setText("                       Photo");

        lmv.clearSelection();
        hmv.clearSelection();
        bi = null;
//        rfTag.setText("");
//        vehicleInfo.setText("");
        biometricBean = null;
        deleteDriverList = null;
        fingerTemplateList = null;
        fingerImageList = null;
        biometricBean = null;
        isFingerCaptured.setText("");
        isDriverExist.setText("");
        isFingerExist.setText("");
        isFingerCaptured.setText("");
        isFingerVarified.setText("");
    }

    private void setWhiteBackground() {
        driverTypePanel.setBackground(UIConstant.PanelWhite);
        name.setBackground(UIConstant.PanelWhite);
        fatherName.setBackground(UIConstant.PanelWhite);
        Address.setBackground(UIConstant.PanelWhite);
        mobile1.setBackground(UIConstant.PanelWhite);
        dl.setBackground(UIConstant.PanelWhite);
        adharNo.setBackground(UIConstant.PanelWhite);
        panNo.setBackground(UIConstant.PanelWhite);
        mobile2.setBackground(UIConstant.PanelWhite);
        voterId.setBackground(UIConstant.PanelWhite);
        lmvPanel.setBackground(UIConstant.PanelWhite);
        hmvPanel.setBackground(UIConstant.PanelWhite);
    }

    public void showExistingDriverDetails() {
    	if(biometricBean == null)
    		return;
        gatePassId.setText(Misc.getPrintableInt(biometricBean.getDriverId()));
        name.setText(biometricBean.getDriverName());
        if (biometricBean.getDriverDob() != null) {
            dob.setDate(biometricBean.getDriverDob());
        }
        isFingerCaptured.setText("Yes");
        fatherName.setText(biometricBean.getInfo1());
        Address.setText(biometricBean.getDriverAddressOne());
        mobile1.setText(biometricBean.getDriverMobileOne());
        dl.setText(biometricBean.getDriverDlNumber());
        if (biometricBean.getDlExpiryDate() != null) {
            dlExpiryDate.setDate(biometricBean.getDlExpiryDate());
        }
        adharNo.setText(biometricBean.getProvidedUid());
        panNo.setText(biometricBean.getDriverUid());
        mobile2.setText(biometricBean.getDriverMobileTwo());
        voterId.setText(biometricBean.getInfo4());


        if (biometricBean.getPhoto() != null) {
            photo.setText("");
            bi = FingerPrintAction.byteArrayToImage(biometricBean.getPhoto());
            photo.setIcon(new ImageIcon(bi));
        }
        if (biometricBean.getType() == 1) {
            driverType.clearSelection();
            driver.setSelected(true);
        } else if (biometricBean.getType() == 2) {
            driverType.clearSelection();
            helper.setSelected(true);
        } else if (biometricBean.getType() == 3) {
            driverType.clearSelection();
            supervisor.setSelected(true);
        } else {
            driverType.clearSelection();
            other.setSelected(true);
        }

        if (biometricBean.getLovField2() != Misc.getUndefInt()) {
            if (biometricBean.getLovField2() == 1) {
                hmvYes.setSelected(true);
            } else if (biometricBean.getLovField2() == 2) {
                hmvNo.setSelected(true);
            } else {
                hmvNC.setSelected(true);
            }
        }
        if (biometricBean.getLovField3() != Misc.getUndefInt()) {
            if (biometricBean.getLovField3() == 1) {
                lmvYes.setSelected(true);
            } else if (biometricBean.getLovField3() == 2) {
                lmvNo.setSelected(true);
            } else {
                lmvNc.setSelected(true);
            }
        }
        updateBlockStatus();
        isEnterPressedOnDLNo = true;
    }
    private void updateBlockStatus(){
    	int isFingerCapturedAtGate = Misc.getUndefInt();
    	int isFingerMatchedAtGate = Misc.getUndefInt();
    	int isDriverExist_ = Misc.getUndefInt();
    	int isFingerExist_ = Misc.getUndefInt();
    	if(tpRecord != null && tprBlockManager != null && !Misc.isUndef(tpRecord.getTprId())){
        	isFingerCapturedAtGate = tprBlockManager.isBlockForInstruction(com.ipssi.rfid.constant.Type.BlockingInstruction.BLOCK_DUETO_FINGER_NOT_VERIFIED) ? UIConstant.NO : UIConstant.YES;
        	isFingerMatchedAtGate = tprBlockManager.isBlockForInstruction(com.ipssi.rfid.constant.Type.BlockingInstruction.BLOCK_DUETO_FINGER_NOT_CAPTURED) ? UIConstant.NO : UIConstant.YES;
            isDriverExist_ = tprBlockManager.isBlockForInstruction(com.ipssi.rfid.constant.Type.BlockingInstruction.BLOCK_DUETO_DRIVER_NOT_EXIST) ? UIConstant.NO : UIConstant.YES;
            isFingerExist_ = tprBlockManager.isBlockForInstruction(com.ipssi.rfid.constant.Type.BlockingInstruction.BLOCK_DUETO_FINGER_NOT_EXIST) ? UIConstant.NO : UIConstant.YES;
        }else{
        	isDriverExist_ = biometricBean.getStatus() == 10 ?  UIConstant.NO :  UIConstant.YES;
            isFingerExist_ = biometricBean.getCaptureFirstTemplate1() == null && biometricBean.getCaptureSecondTemplate2() == null ?  UIConstant.NO :  UIConstant.YES;
        }
    	if(isFingerCapturedAtGate == UIConstant.NO)
    		isFingerCaptured.setForeground(Color.RED);
        if(isFingerMatchedAtGate == UIConstant.NO)
        	isFingerVarified.setForeground(Color.RED);
        if(isDriverExist_ == UIConstant.NO)
        	isDriverExist.setForeground(Color.RED);
        if(isFingerExist_ == UIConstant.NO)
        	isFingerExist.setForeground(Color.RED);
        isFingerCaptured.setText(isFingerCapturedAtGate == UIConstant.YES ? "Yes" : (isFingerCapturedAtGate == UIConstant.NO ? "No" : "Na"));
        isFingerVarified.setText(isFingerMatchedAtGate == UIConstant.YES ? "Yes" : (isFingerMatchedAtGate == UIConstant.NO ? "No" : "Na"));
    	isDriverExist.setText(isDriverExist_ == UIConstant.YES ? "Yes" : (isDriverExist_ == UIConstant.NO ? "No" : "Na"));
        isFingerExist.setText(isFingerExist_ == UIConstant.YES ? "Yes" : (isFingerExist_ == UIConstant.NO ? "No" : "Na"));
    }
    private void setValues() {
        if (tpRecord != null && Utils.isNull(tpRecord.getDlNo())) {
            dl.setText(tpRecord.getDlNo());
            dl.requestFocusInWindow();
            driverId = tpRecord.getDriverId();
        }
    }

   private void saveAction() {
        Pattern pattern1 = Pattern.compile("\\d{10}");
        Matcher matcher1 = pattern1.matcher(mobile1.getText());
        Matcher matcher2 = pattern1.matcher(mobile2.getText());
        Pattern pattern2 = Pattern.compile("\\d{12}");
        Matcher matcher3 = pattern2.matcher(adharNo.getText());
        setWhiteBackground();
        Connection conn = null;
        boolean destroyIt = false;
        StringBuilder error = new StringBuilder();
        try{
        	conn = DBConnectionPool.getConnectionFromPoolNonWeb();
        	if (!driver.isSelected() && !helper.isSelected() && !supervisor.isSelected() && !other.isSelected()) {
        		JOptionPane.showMessageDialog(null, " Please Select Driver Type !!!");
        		driverTypePanel.setBackground(UIConstant.focusPanelColor);
        		driverTypePanel.requestFocusInWindow();
        		return;
        	} else if (dob.getDate() == null) {
        		JOptionPane.showMessageDialog(null, " Please Enter Date of Birth !!!");
        		dob.requestFocusInWindow();
        		return;
        	} else if (Utils.isNull(name.getText())) {
        		JOptionPane.showMessageDialog(null, " Please Enter Name !!!");
        		name.setBackground(UIConstant.focusPanelColor);
        		name.requestFocusInWindow();
        		return;
        	} else if (!Utils.isNull(adharNo.getText()) && !matcher3.matches() && !helper.isSelected()) {
        		JOptionPane.showMessageDialog(null, "Please Enter 12 Digit Aadhaar Number !!!");
        		adharNo.setBackground(UIConstant.focusPanelColor);
        		adharNo.requestFocusInWindow();
        		return;
        	} else if (Utils.isNull(fatherName.getText())) {
        		JOptionPane.showMessageDialog(null, " Please Enter father Name!!!");
        		fatherName.setBackground(UIConstant.focusPanelColor);
        		fatherName.requestFocusInWindow();
        		return;
        	} else if (Utils.isNull(Address.getText())) {
        		JOptionPane.showMessageDialog(null, " Please Enter Address !!!");
        		Address.setBackground(UIConstant.focusPanelColor);
        		Address.requestFocusInWindow();
        		return;
        	} else if (!Utils.isPhoneNumberValidate(mobile1.getText())) {
        		JOptionPane.showMessageDialog(null, " Incorrect Phone Number !!!");
        		mobile1.setBackground(UIConstant.focusPanelColor);
        		mobile1.requestFocusInWindow();
        		return;
        	} else if (!Utils.isNull(mobile2.getText()) && !Utils.isPhoneNumberValidate(mobile2.getText())) {
        		JOptionPane.showMessageDialog(null, " Incorrect Phone Number !!!");
        		mobile2.setBackground(UIConstant.focusPanelColor);
        		mobile2.requestFocusInWindow();
        		return;
        	} else if (Utils.isNull(dl.getText()) && !helper.isSelected()) {
        		JOptionPane.showMessageDialog(null, " Please Enter DL No. !!!");
        		dl.setBackground(UIConstant.focusPanelColor);
        		dl.requestFocusInWindow();
        		return;
        	} else if (!hmvYes.isSelected() && !hmvNo.isSelected() && !hmvNC.isSelected() && !helper.isSelected()) {
        		JOptionPane.showMessageDialog(null, " Please Select HMV !!!");
        		hmvPanel.setBackground(UIConstant.focusPanelColor);
        		hmvPanel.requestFocusInWindow();
        		return;
        	} else if (dlExpiryDate.getDate() == null && !helper.isSelected()) {
        		JOptionPane.showMessageDialog(null, " Please Select DL Expiry Date !!!");
        		//name.setBackground(UIConstant.focusPanelColor);
        		dlExpiryDate.requestFocusInWindow();
        		return;
        	} else if (!lmvYes.isSelected() && !lmvNo.isSelected() && !lmvNc.isSelected() && !helper.isSelected()) {
        		JOptionPane.showMessageDialog(null, " Please Select LMV !!!");
        		lmvPanel.setBackground(UIConstant.focusPanelColor);
        		lmvPanel.requestFocusInWindow();
        		return;
        	} else if (photo.getIcon() == null) {
        		JOptionPane.showMessageDialog(null, "Please Capture Photo!!!");
        		capturePhoto.requestFocusInWindow();
        		return;
        	} else if (fingerTemplateList != null && fingerTemplateList.size() < 2){
        		JOptionPane.showMessageDialog(null, "Please Take Minimum Two Finger Print !!!");
        		captureFinger.requestFocusInWindow();
        		return;
        	}
        	else {
        		//            if (biometricBean.getIsSave() == isSave || biometricBean.getIsSave() == isUpdate) {
        		boolean isNewDriver = biometricBean == null || Misc.isUndef(biometricBean.getDriverId());
        		if(error != null)
        			error.append("\nisNewDriver : "+isNewDriver);
        		if (driver.isSelected()) {
        			biometricBean.setType(1);
        		} else if (helper.isSelected()) {
        			biometricBean.setType(2);
        		} else if (supervisor.isSelected()) {
        			biometricBean.setType(3);
        		} else {
        			biometricBean.setType(4);
        		}
        		if (hmvYes.isSelected()) {
        			biometricBean.setLovField2(1);
        		} else if (hmvNo.isSelected()) {
        			biometricBean.setLovField2(2);
        		} else {
        			biometricBean.setLovField2(3);
        		}
        		if (lmvYes.isSelected()) {
        			biometricBean.setLovField3(1);
        		} else if (lmvNo.isSelected()) {
        			
        			biometricBean.setLovField3(2);
        		} else {
        			biometricBean.setLovField3(3);
        		}
        		biometricBean.setInfo3(vehicleName);
        		biometricBean.setDriverDlNumber(dl.getText());
        		biometricBean.setDriverName(name.getText());
        		biometricBean.setInfo1(fatherName.getText());
        		biometricBean.setDriverAddressOne(Address.getText());
        		biometricBean.setDriverMobileOne(mobile1.getText());
        		biometricBean.setDriverDob(dob.getDate());
        		biometricBean.setProvidedUid(adharNo.getText());
        		biometricBean.setDriverUid(panNo.getText());
        		biometricBean.setDriverMobileTwo(mobile2.getText());
        		biometricBean.setDlExpiryDate(dlExpiryDate.getDate());
        		biometricBean.setInfo4(voterId.getText());
        		biometricBean.setStatus(1);
        		biometricBean.setIsfingerCaptured(1);
        		if (bi != null) {
        			byte[] photoTemplate = FingerPrintAction.bufferImageToBytes(bi);
        			biometricBean.setPhoto(photoTemplate);
        		}
        		boolean updateDriverFingureOne =fingerTemplateList != null && fingerTemplateList.size() > 0;
        		boolean updateDriverFingureTwo =fingerTemplateList != null && fingerTemplateList.size() > 1;
        		if(updateDriverFingureOne)
        			biometricBean.setCaptureFirstTemplate1(fingerTemplateList.get(0));
        		if(updateDriverFingureTwo)
        			biometricBean.setCaptureSecondTemplate2(fingerTemplateList.get(1));
        		
        		boolean isInserted = false;
        		if(isNewDriver){
        			isInserted = GateInDao.insertDriverDetail(conn, biometricBean);
        		}else{
        			isInserted = GateInDao.updateDriverDetail(conn, biometricBean);
        		}
        		if(error != null){
        			error.append(" Register for driver(id) : "+biometricBean.getDriverId());
        		}
        		if(error != null){
        			error.append("driver save status : "+ isInserted);
        			System.out.println(error.toString());
        		}
        		
        		if(isInserted && fingerTemplateList != null && fingerTemplateList.size() > 0){
        			MorphoSmartFunctions morpho = null;
        			synchronized (MorphoSmartFunctions.lock) {
        				morpho = MorphoSmartFunctions.getMorpho();
        				for(int i=0,is = deleteDriverList == null ? 0 : deleteDriverList.size() ; i <is; i++){
                			FingerPrintAction.deleteDriverFingerFromDB(conn, deleteDriverList.get(i), error);
                		}
        				for(int i=0,is = deleteDriverList == null ? 0 : deleteDriverList.size() ; i <is; i++){
        					if(!TokenManager.checkSyncReg){
        						if(TokenManager.useSDK()){
        							morpho.remove(deleteDriverList.get(i));
        						}else{
        							FingerPrintAction.deleteAllDriverFinger(deleteDriverList.get(i),error);
        						}
        					}
        				}
        				int[] enrollStatusList =   SyncFingerPrintDeviceHelper.getEmptyIntArray(10);
        				int enrollResult = Misc.getUndefInt();
        				if(!TokenManager.checkSyncReg)
        					enrollResult = morpho.enroll(biometricBean.getDriverId()+"", fingerTemplateList);
        				if(enrollResult != MorphoSmartSDK.MORPHOERR_PROTOCOLE || TokenManager.checkSyncReg){
        					enrollStatusList[0] = enrollResult;
        					GateInDao.updateDriverFingure(conn, biometricBean);
        					//SyncFingerPrintDeviceHelper.updateDriverDetails(conn,biometricBean.getDriverId(), morpho.getMorphoDBDeviceId(conn),DriverBean.INSERT,1, Misc.getUndefInt(),null,enrollStatusList);
        				}	
        			}
        			GateInDao.updateTPRQuestion(conn, tprId, TokenManager.currWorkStationType, Status.TPRQuestion.isFingerCaptured, Results.Questions.YES, userId);
        			GateInDao.updateTPRQuestion(conn, tprId, TokenManager.currWorkStationType, Status.TPRQuestion.isFingerVerified, Results.Questions.YES, userId);
        			GateInDao.updateTPRQuestion(conn, tprId, TokenManager.currWorkStationType, Status.TPRQuestion.isDriverExist, Results.Questions.YES, userId);
        			GateInDao.updateTPRQuestion(conn, tprId, TokenManager.currWorkStationType, Status.TPRQuestion.isFingerExist, Results.Questions.YES, userId);
        			if(tprBlockManager != null){
        				tprBlockManager.addQuestions(new TPSQuestionDetail(TPRQuestion.isFingerCaptured, Results.Questions.YES));
        				tprBlockManager.addQuestions(new TPSQuestionDetail(TPRQuestion.isFingerVerified, Results.Questions.YES));
        				tprBlockManager.addQuestions(new TPSQuestionDetail(TPRQuestion.isDriverExist, Results.Questions.YES));
        				tprBlockManager.addQuestions(new TPSQuestionDetail(TPRQuestion.isFingerExist, Results.Questions.YES));
        				
        			}
        			if(handler != null){
        				handler.updateVehicleBlockStatus(conn, tpRecord != null ? tpRecord.getVehicleId() : Misc.getUndefInt(), tpRecord);
        			}
        			
        		}
        		/*else {
        			conn.commit();
        			JOptionPane.showMessageDialog(null, " Detail Saved!!!");
        			clearInputs();
        			this.dispose();
//        			JOptionPane.showMessageDialog(null, " Some Exception occurs !!!!.Please try again");
        		}*/
        		conn.commit();
    			JOptionPane.showMessageDialog(null, " Detail Saved!!!");
    			clearInputs();
    			this.dispose();
        		//            } 
        	/*else {
                JOptionPane.showMessageDialog(null, " Detail Not Saved!!!");
            }*/
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

     private void fingerCapturedAction() {
        if (Utils.isNull(dl.getText()) && !helper.isSelected()) {
            JOptionPane.showMessageDialog(null, "Please Enter Driver Lisence No!!!");
            setWhiteBackground();
            dl.setBackground(UIConstant.focusPanelColor);
            dl.requestFocusInWindow();
            return;
        } else if (!isEnterPressedOnDLNo && !helper.isSelected()) {
            JOptionPane.showMessageDialog(null, "Please Search Driver by Driver Lisence No then take finger Print!!!");
            setWhiteBackground();
            dl.setBackground(UIConstant.focusPanelColor);
            dl.requestFocusInWindow();
            return;
        } else if (Utils.isNull(name.getText())) {
            JOptionPane.showMessageDialog(null, "Please Enter Name!!!");
            setWhiteBackground();
            name.setBackground(UIConstant.focusPanelColor);
            name.requestFocusInWindow();
            return;
        }
        if(biometricBean == null)
        	biometricBean = new BioMatricBean();
        	deleteDriverList = new ArrayList<Integer>();
        	fingerTemplateList = new ArrayList<byte[]>();
        	fingerImageList = new ArrayList<byte[]>();
        if (driver.isSelected()) {
			biometricBean.setType(1);
		} else if (helper.isSelected()) {
			biometricBean.setType(2);
		} else if (supervisor.isSelected()) {
			biometricBean.setType(3);
		} else {
			biometricBean.setType(4);
		}
		if (hmvYes.isSelected()) {
			biometricBean.setLovField2(1);
		} else if (hmvNo.isSelected()) {
			biometricBean.setLovField2(2);
		} else {
			biometricBean.setLovField2(3);
		}
		if (lmvYes.isSelected()) {
			biometricBean.setLovField3(1);
		} else if (lmvNo.isSelected()) {
			biometricBean.setLovField3(2);
		} else {
			biometricBean.setLovField3(3);
		}
		biometricBean.setInfo3(vehicleName);
		biometricBean.setDriverDlNumber(dl.getText());
		biometricBean.setDriverName(name.getText());
		biometricBean.setInfo1(fatherName.getText());
		biometricBean.setDriverAddressOne(Address.getText());
		biometricBean.setDriverMobileOne(mobile1.getText());
		biometricBean.setDriverDob(dob.getDate());
		biometricBean.setProvidedUid(adharNo.getText());
		biometricBean.setDriverUid(panNo.getText());
		biometricBean.setDriverMobileTwo(mobile2.getText());
		biometricBean.setDlExpiryDate(dlExpiryDate.getDate());
		biometricBean.setInfo4(voterId.getText());
		showExistingDriverDetails();
		Save.requestFocusInWindow();
		if(!VehicleRegistrationWindow.isFingerSyncRunning){
			if(!TokenManager.useSDK()){
				new FingerPrintDialog(this, true, biometricBean, deleteDriverList, fingerTemplateList , fingerImageList).setVisible(true);
			}else{
				new CapturePanel(this, true, biometricBean, deleteDriverList, fingerTemplateList , fingerImageList).setVisible(true);
				/*MsoConnection mscOb = new MsoConnection(this);
			  MorphoConstant.DO_ENROLL = true;
			  mscOb.btnOkActionPerformed(this,false,true);*/
			}
		}else{
			JOptionPane.showMessageDialog(null, "Finger print sync service running.please try again.");
		}
		
  }

    private void photoCaptureAction() {
        bi = new WebcamExecutable().getImage();
        photo.setText("");
        if (bi != null) {
            photo.setIcon(new ImageIcon(bi));
        }
    }

    private void eventOngatePassId() {
    	biometricBean = null;
    	setWhiteBackground();
    	Connection conn = null;
    	boolean destroyIt = false;
    	try{
    		conn = DBConnectionPool.getConnectionFromPoolNonWeb();
    		clearInputs();
    		if (Utils.isNull(gatePassId.getText())) {
    			JOptionPane.showMessageDialog(null, "Please Enter Driver Gate Pass Id!!!");
    			setWhiteBackground();
    			gatePassId.setBackground(UIConstant.focusPanelColor);
    			gatePassId.requestFocusInWindow();
    			return;
    		}
    		isEnterPressedOnDLNo = true;
    		biometricBean = GateInDao.getDriverDetails(conn, gatePassId.getText().trim(), "driverId");
    		if (biometricBean != null && biometricBean.getDriverId() != Misc.getUndefInt()) {
    			showExistingDriverDetails();
    			if (biometricBean.getIsfingerCaptured() == 1) {
    				biometricBean.setIsSave(2);
    			}
    		} else {
    			biometricBean = null;
    			gatePassId.setText("");
    			dl.setText("");
    			clearInputs();
    		}
    		dl.requestFocusInWindow();
    		dl.setBackground(UIConstant.focusPanelColor);
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
