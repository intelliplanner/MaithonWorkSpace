package com.ipssi.rfid.ui;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ButtonGroup;
import javax.swing.JOptionPane;

import com.ipssi.common.ds.rule.GpsPlusViolations;
import com.ipssi.common.ds.rule.ResultEnum;
import com.ipssi.gen.utils.CacheTrack;
import com.ipssi.gen.utils.DBConnectionPool;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.Pair;
import com.ipssi.gen.utils.Triple;
import com.ipssi.rfid.beans.BlockingInstruction;
import com.ipssi.rfid.beans.RFIDHolder;
import com.ipssi.rfid.beans.TPRQCDetail;
import com.ipssi.rfid.beans.TPRecord;
import com.ipssi.rfid.beans.TPSQuestionDetail;
import com.ipssi.rfid.beans.TPStep;
import com.ipssi.rfid.beans.Token;
import com.ipssi.rfid.constant.Status;
import com.ipssi.rfid.constant.Type;
import com.ipssi.rfid.constant.UIConstant;
import com.ipssi.rfid.database.DropDownValues;
import com.ipssi.rfid.database.GateInDao;
import com.ipssi.rfid.db.RFIDMasterDao;
import com.ipssi.rfid.integration.Barrier;
import com.ipssi.rfid.integration.Clock;
import com.ipssi.rfid.processor.TPRBlockManager;
import com.ipssi.rfid.processor.TPRBlockStatusHelper;
import com.ipssi.rfid.processor.TPRInformation;
import com.ipssi.rfid.processor.TPRUtils;
import com.ipssi.rfid.processor.TokenManager;
import com.ipssi.rfid.processor.Utils;
import com.ipssi.rfid.readers.RFIDDataHandler;
import com.ipssi.rfid.readers.RFIDDataProcessor;
import com.ipssi.rfid.readers.TAGListener;
import com.ipssi.rfid.ui.AutoCompleteCombo.ComboKeyEvent;

public class CoalYardOutWindow extends javax.swing.JFrame {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	UIConstant ApplicationConstant;
    //flags
    private int contiNue = 1;
    private int reEnter = 0;
    
    private ButtonGroup gpsOk = new ButtonGroup();
    
    private RFIDDataHandler rfidHandler = null;
    private int readerId = 0;
    
    
    private Date entryTime = null;
    private Date exitTime = null;
    
    private TPRecord tprRecord = null;
    private GpsPlusViolations gpv = null;
    private RFIDHolder data = null;
    private Token token = null;
    private TPStep tpStep = null;
    private TPRQCDetail tprQcDetail = null;
    
    private boolean isVehicleExist = false;
    private boolean vehicleBlackListed = false;
    private boolean isTagRead = false;
    private boolean isRequestOverride = false;
    
    private int isGpsOk = Misc.getUndefInt();
    private int qc_status = Misc.getUndefInt();
    private BlockingInstruction blockInstructionGps = null;
	private TPRBlockManager tprBlockManager = null;
	private boolean isManual = false;
	private Pair<Long, String> pairVal=null;
    
    /**
     * Creates new form CoalYardOutWindow
     */
    public CoalYardOutWindow() throws IOException {
        initComponents();
//        gpsLocation.setText("1234012345678901234567890");
//        gpsTime.setText("asdfghjkl;wertyuiod");
        this.setExtendedState(this.getExtendedState()
                | this.MAXIMIZED_BOTH);
        this.setTitle(UIConstant.formTitle);
        Clock.startClock("YardOut");
        //Clock.startClock("YardIn");
		Connection conn = null;
		boolean destroyIt = false;
		try{
			conn = DBConnectionPool.getConnectionFromPoolNonWeb();
			BlockingInstruction bIns = new BlockingInstruction();
    		bIns.setType(com.ipssi.rfid.constant.Type.BlockingInstruction.BLOCK_DUETO_GPS);
    		ArrayList<BlockingInstruction> list = (ArrayList<BlockingInstruction>) RFIDMasterDao.getList(conn, bIns, null);
    		if(list != null && list.size() > 0)
    			blockInstructionGps = list.get(0);
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
        getFocus();
        setHideComponentPermanently(false);
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

    private void setHideComponentPermanently(boolean isTrue) {
    	jLabel9.setVisible(isTrue);
    	gpsRepaired.setVisible(isTrue);
	}

	public void start() throws IOException {

        if (rfidHandler == null) {
            rfidHandler = new RFIDDataHandler(1000, readerId, TokenManager.currWorkStationType, TokenManager.currWorkStationId,TokenManager.userId);
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
                    //Object[] options = {" Cancel ", "  Update  "};
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
        jLabel6 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        tprIdLabel = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        username = new javax.swing.JLabel();
        button1 = new java.awt.Button();
        jLabel8 = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        vehicleName = new AutoCompleteCombo();
        jLabel7 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        gpsRepaired = new javax.swing.JLabel();
        overrides = new javax.swing.JLabel();
//        jPanel3 = new javax.swing.JPanel();
//        jLabel11 = new javax.swing.JLabel();
        iiaReceipts = new javax.swing.JTextField();
//        jLabel12 = new javax.swing.JLabel();
        gpsOK = new javax.swing.JLabel();
        transporter = new javax.swing.JLabel();
        mines = new javax.swing.JLabel();
        grade = new javax.swing.JLabel();
        qcDone = new javax.swing.JComboBox();
        jLabel15 = new javax.swing.JLabel();
        challanNo = new javax.swing.JLabel();
        supplier = new javax.swing.JLabel();
        SaveAndOpenGate = new javax.swing.JButton();
        manualEntryButton = new javax.swing.JButton();
        clear = new javax.swing.JButton();
        digitalClock = new javax.swing.JLabel();
        blocking_reason = new javax.swing.JLabel();
        
        gpsLocationLabel = new javax.swing.JLabel();
        gpsTime = new javax.swing.JLabel();
        gpsTimeLabel = new javax.swing.JLabel();
        gpsLocation = new javax.swing.JLabel();
        
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        this.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
            	GateInDao.forceSignut(TokenManager.userId,TokenManager.srcType,Integer.toString(TokenManager.systemId));
            }});
        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        jPanel2.setBackground(java.awt.SystemColor.controlLtHighlight);

        jLabel6.setFont(ApplicationConstant.subHeadingFont);
        jLabel6.setText("Yard Out");
        
        tprIdLabel.setFont(new java.awt.Font("Segoe UI", 1, 14)); 
//        tprIdLabel.setText("TPR-ID:1234567890");
        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/ipssi/rfid/ui/cover_01_right_top.gif"))); // NOI18N

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

        jLabel8.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/ipssi/rfid/ui/intelliplanner.png"))); // NOI18N

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel14, javax.swing.GroupLayout.PREFERRED_SIZE, 207, javax.swing.GroupLayout.PREFERRED_SIZE)
                    
                    .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 247, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(50, 50, 50)
                 .addComponent(tprIdLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 256, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(80, 80, 80)
                .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 201, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(username, javax.swing.GroupLayout.PREFERRED_SIZE, 108, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(button1, javax.swing.GroupLayout.PREFERRED_SIZE, 92, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 171, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(107, 107, 107))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(username, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(button1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel8, javax.swing.GroupLayout.DEFAULT_SIZE, 52, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel14)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                .addComponent(tprIdLabel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        jPanel4.setBackground(new java.awt.Color(255, 255, 255));

        jLabel2.setFont(ApplicationConstant.labelFont);
        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel2.setText("Vehicle:");

        jLabel3.setFont(ApplicationConstant.labelFont);
        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel3.setText("Overrides?");

        jLabel4.setFont(ApplicationConstant.labelFont);
        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel4.setText("Transporter:");

        jLabel5.setFont(ApplicationConstant.labelFont);
        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel5.setText("Mines:");

        /*vehicleName.setEditable(false);
        vehicleName.setTextBackground(new java.awt.Color(255, 255, 255));
        vehicleName.setFont(ApplicationConstant.vehicleLabel);
        vehicleName.setText("NO VEHICLE DETECTED");
        vehicleName.setForeground(UIConstant.textFontColor);
        vehicleName.setBorder(null);
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
            public void keyReleased(java.awt.event.KeyEvent evt) {
                vehicleNameKeyReleased(evt);
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
        jLabel7.setFont(ApplicationConstant.labelFont);
        jLabel7.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel7.setText("Grade:");

        jLabel9.setFont(ApplicationConstant.labelFont);
        jLabel9.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel9.setText("GPS Repair Needed:");

        jLabel10.setFont(ApplicationConstant.labelFont);
        jLabel10.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel10.setText("QC Done:");
        jLabel10.setVisible(false);

        gpsRepaired.setForeground(ApplicationConstant.textFontColor);
        gpsRepaired.setFont(ApplicationConstant.textFont);

        overrides.setFont(UIConstant.textFont);
        overrides.setForeground(UIConstant.textFontColor);
        
        gpsLocationLabel.setFont(UIConstant.labelFont);
        gpsLocationLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        gpsLocationLabel.setText("GPS Location:");

        gpsLocation.setFont(UIConstant.textFont);
        gpsLocation.setForeground(UIConstant.textFontColor);
        
        gpsTimeLabel.setFont(UIConstant.labelFont);
        gpsTimeLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        gpsTimeLabel.setText("GPS Time:");

        gpsTime.setFont(UIConstant.textFont);
        gpsTime.setForeground(UIConstant.textFontColor);
        

        

//        jPanel3.setBackground(new java.awt.Color(255, 255, 255));

//        jLabel11.setFont(ApplicationConstant.labelFont);
//        jLabel11.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
//        jLabel11.setText("<html>IIA receipt #:</html>");
//        jLabel11.setVisible(false);

        iiaReceipts.setFont(ApplicationConstant.textFont);
        iiaReceipts.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        iiaReceipts.setEnabled(false);
        iiaReceipts.setVisible(false);
        iiaReceipts.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                iiaReceiptsMouseClicked(evt);
            }
        });
        iiaReceipts.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                iiaReceiptsActionPerformed(evt);
            }
        });
        iiaReceipts.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                iiaReceiptsFocusGained(evt);
            }
        });
        iiaReceipts.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                iiaReceiptsKeyPressed(evt);
            }
        });

//        jLabel12.setFont(ApplicationConstant.labelFont);
//        jLabel12.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
//        jLabel12.setText("GPS Ok:");
//        jLabel12.setVisible(false);

//        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
//        jPanel3.setLayout(jPanel3Layout);
//        jPanel3Layout.setHorizontalGroup(
//            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
//            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
//                .addGap(0, 0, Short.MAX_VALUE)
//                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
//                    .addComponent(jLabel11, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 116, Short.MAX_VALUE)
//                    .addComponent(jLabel12, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
//                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
//                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
//                    .addComponent(iiaReceipts, javax.swing.GroupLayout.DEFAULT_SIZE, 215, Short.MAX_VALUE)
//                    .addComponent(gpsOK, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
//                .addContainerGap())
//        );
//        jPanel3Layout.setVerticalGroup(
//            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
//            .addGroup(jPanel3Layout.createSequentialGroup()
//                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
//                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
//                    .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
//                    .addComponent(iiaReceipts, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
//                .addGap(37, 37, 37)
//                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
//                    .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
//                    .addComponent(gpsOK, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
//                .addGap(31, 31, 31))
//        );

        transporter.setForeground(ApplicationConstant.textFontColor);
        transporter.setFont(ApplicationConstant.textFont);

        mines.setForeground(ApplicationConstant.textFontColor);
        mines.setFont(ApplicationConstant.textFont);

        grade.setForeground(ApplicationConstant.textFontColor);
        grade.setFont(ApplicationConstant.textFont);

        qcDone.setFont(UIConstant.textFont);
        qcDone.setVisible(false);
        qcDone.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Select", "Yes", "No" }));
        qcDone.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                qcDoneItemStateChanged(evt);
            }
        });
        qcDone.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                qcDoneActionPerformed(evt);
            }
        });
        qcDone.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                qcDoneFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                qcDoneFocusLost(evt);
            }
        });
        qcDone.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                qcDoneKeyPressed(evt);
            }
        });

        jLabel15.setFont(UIConstant.labelFont);
        jLabel15.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel15.setText("Challan #:");

        challanNo.setFont(UIConstant.textFont); // NOI18N

        challanNo.setForeground(UIConstant.textFontColor);

        supplier.setFont(UIConstant.labelFont);
        supplier.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        supplier.setText("Supplier:");
        supplier.setVisible(false);

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        		.addComponent(gpsLocationLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        		.addComponent(gpsTimeLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel9, javax.swing.GroupLayout.DEFAULT_SIZE, 170, Short.MAX_VALUE))
                            
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addGap(3, 3, 3)
                                .addComponent(gpsRepaired, javax.swing.GroupLayout.PREFERRED_SIZE, 218, javax.swing.GroupLayout.PREFERRED_SIZE))
                           
                                .addGroup(jPanel4Layout.createSequentialGroup()
                                .addGap(3, 3, 3)
                                .addComponent(gpsLocation, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE))
                                
                                .addGroup(jPanel4Layout.createSequentialGroup()
                                .addGap(3, 3, 3)
                                .addComponent(gpsTime, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE))
//                                
                             
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(qcDone, javax.swing.GroupLayout.PREFERRED_SIZE, 214, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(39, 39, 39))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel7, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel4, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(3, 3, 3)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(overrides, javax.swing.GroupLayout.DEFAULT_SIZE, 215, Short.MAX_VALUE)
                                .addComponent(transporter, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(grade, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addComponent(vehicleName, javax.swing.GroupLayout.PREFERRED_SIZE, 257, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(44, 44, 44)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
//                    .addGroup(jPanel4Layout.createSequentialGroup()
//                        .addGap(27, 27, 27)
//                        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(supplier, javax.swing.GroupLayout.DEFAULT_SIZE, 141, Short.MAX_VALUE)
                            .addComponent(jLabel15, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 141, Short.MAX_VALUE)
                            .addComponent(jLabel5, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(challanNo, javax.swing.GroupLayout.DEFAULT_SIZE, 311, Short.MAX_VALUE)
                            .addComponent(mines, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addGap(360, 360, 360))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, 40, Short.MAX_VALUE)
                    .addComponent(vehicleName))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, 30, Short.MAX_VALUE)
                        .addComponent(overrides, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(supplier, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(transporter, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(mines, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel7, javax.swing.GroupLayout.DEFAULT_SIZE, 30, Short.MAX_VALUE)
                            .addComponent(grade, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(qcDone, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(3, 3, 3)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel9, javax.swing.GroupLayout.DEFAULT_SIZE, 30, Short.MAX_VALUE)
                            .addComponent(gpsRepaired, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(3, 3, 3)
                        
                     .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(gpsLocationLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 30, Short.MAX_VALUE)
                            .addComponent(gpsLocation, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(3, 3, 3)
                     .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(gpsTimeLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 30, Short.MAX_VALUE)
                            .addComponent(gpsTime, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(3, 3, 3))

                        
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel15, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(challanNo, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
//                        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, 127, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(112, 112, 112))))
        );

        SaveAndOpenGate.setFont(ApplicationConstant.buttonFont);
        SaveAndOpenGate.setText("Save And Open Gate");
        SaveAndOpenGate.setEnabled(false);
        SaveAndOpenGate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SaveAndOpenGateActionPerformed(evt);
            }
        });
        SaveAndOpenGate.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                SaveAndOpenGateKeyPressed(evt);
            }
        });

        manualEntryButton.setFont(ApplicationConstant.buttonFont);
        manualEntryButton.setText("Manual Entry");
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

        clear.setFont(ApplicationConstant.buttonFont);
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

        digitalClock.setFont(UIConstant.textFont); // NOI18N

        digitalClock.setForeground(UIConstant.textFontColor);
        digitalClock.setFont(new java.awt.Font("Segoe UI", 1, 36));

        blocking_reason.setFont(UIConstant.textFont);
        blocking_reason.setForeground(UIConstant.noActionPanelColor);
        blocking_reason.setMinimumSize(null);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(275, 275, 275)
                        .addComponent(clear, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(53, 53, 53)
                        .addComponent(manualEntryButton, javax.swing.GroupLayout.PREFERRED_SIZE, 195, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(55, 55, 55)
                        .addComponent(SaveAndOpenGate, javax.swing.GroupLayout.PREFERRED_SIZE, 221, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(223, 223, 223)
                        .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(blocking_reason, javax.swing.GroupLayout.PREFERRED_SIZE, 1305, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(digitalClock, javax.swing.GroupLayout.PREFERRED_SIZE, 182, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(140, 140, 140))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(blocking_reason, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, 265, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(19, 19, 19)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(SaveAndOpenGate, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(manualEntryButton, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(clear, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 33, Short.MAX_VALUE)
                .addComponent(digitalClock, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
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
    }// </editor-fold>//GEN-END:initComponents

    public void setTPRecord(Connection conn, TPRecord tpr) throws IOException {
        System.out.println("######### Yard Out setTPRecord  ########");
        try {
            tprRecord = tpr;
            if (tprRecord != null) {
                if (true) {//blockStatus == Status.VALIDATION.NO_ISSUE) {// TPR Valid
                	toggleVehicle(false);
                	isTagRead = token != null ? token.isReadFromTag() : false;
                    if (token == null && tprRecord.getEarliestUnloadYardOutEntry() != null) {
                        entryTime = tprRecord.getEarliestUnloadYardOutEntry();
                    } else if (token != null && tprRecord.getEarliestUnloadYardOutEntry() == null) {
                        if (token.getLastSeen() != Misc.getUndefInt()) {
                            entryTime = new Date(token.getLastSeen());
                        } else {
                            entryTime = new Date();
                        }
                    } else if (token != null && tprRecord.getEarliestUnloadYardOutEntry() != null) {
                        if (token.getLastSeen() > Utils.getDateTimeLong(tprRecord.getEarliestUnloadYardOutEntry())) {
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
                    tprIdLabel.setText("TPR-ID:"+tprRecord.getTprId());
                    String minesName = DropDownValues.getMines(tprRecord.getMinesId(), conn);
                    String gradeName = DropDownValues.getGrade(tprRecord.getMaterialGradeId(), conn);
                    String transporterName = DropDownValues.getTransporter(tprRecord.getTransporterId(), conn);
                    vehicleName.setText(tprRecord.getVehicleId(), tprRecord.getVehicleName());
                    transporter.setText(transporterName);
                    mines.setText(minesName);
                    grade.setText(gradeName);
                    challanNo.setText(tprRecord.getChallanNo());
                    if (tprRecord.getTprId() != Misc.getUndefInt()) {
                        gpv = GpsPlusViolations.getGpsPlusViolatins(conn, tprRecord.getVehicleId(), tprRecord.getTprId(), System.currentTimeMillis(), tprRecord.getMinesId(), tprRecord.getChallanDate() == null ? System.currentTimeMillis() : tprRecord.getChallanDate().getTime());
                    } else {
                        gpv = GpsPlusViolations.getGpsPlusViolatins(conn, tprRecord.getVehicleId(), tprRecord.getTprId(), System.currentTimeMillis(), Misc.getUndefInt(), System.currentTimeMillis());
                    }
                 /*  
                   Pair<ResultEnum, String> gpsRepair = gpv.getGpsRepairNeeded(conn);
                    if (gpsRepair.first == ResultEnum.GREEN) {
                        gpsRepaired.setForeground(UIConstant.PanelDarkGreen);
                        gpsRepaired.setText(gpsRepair.second);
                        isGpsOk = UIConstant.YES;
                    } else if (gpsRepair.first == ResultEnum.RED) {
                        gpsRepaired.setForeground(UIConstant.noActionPanelColor);
                        gpsRepaired.setText(gpsRepair.second);
                        isGpsOk = UIConstant.NO;
                        setQuetionsBlocking(Status.TPRQuestion.gpsOk, UIConstant.NO);

                    } else {
                        gpsRepaired.setForeground(UIConstant.PanelYellow);
                        gpsRepaired.setText(gpsRepair.second);
                        isGpsOk = UIConstant.NC;
                    }
                   */
                    Pair<Integer, String> supplierDetails = TPRUtils.getSupplierFromDo(conn, tprRecord.getDoId());
                    if(!Misc.isUndef(supplierDetails.first)){
                    	supplier.setText(supplierDetails.second);
                    }
                } 
                
            	pairVal = GpsPlusViolations.getLatestLocation(conn, tprRecord.getVehicleId());
                if(pairVal != null){
                	String location = pairVal.second == null ? "" : pairVal.second;
                	gpsTime.setText(GateInDao.getLongToDatetime(pairVal.first));
                	gpsLocation.setText(GateInDao.getString(location, 29));
                }
			
                setBlockingStatus();
                SaveAndOpenGate.setEnabled(true);
                SaveAndOpenGate.requestFocusInWindow();
                
                try{
					Thread.sleep(TokenManager.save_timing);
					quickCreateAndOpen();
				}catch(Exception e){
					e.printStackTrace();
				}
                
            } else {
                /*if (!Utils.isNull(vehicle_name) && !(vehicleName.getText().trim()).equalsIgnoreCase("NO VEHICLE DETECTED")) {
                 JOptionPane.showMessageDialog(null, "Invalid Vehicle Go to Registration");
                 } else if (Utils.isNull(vehicle_name)) {
                 JOptionPane.showMessageDialog(null, "Invalid Vehicle Go to Registration");
                 }*/
                JOptionPane.showMessageDialog(null, "Invalid Vehicle Go to Registration");
                isVehicleExist = false;
            }
        } catch(Exception ex){
    		ex.printStackTrace();
    	}
    }

    private void button1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button1ActionPerformed
        stopRfid();
    	GateInDao.forceSignut(TokenManager.userId,TokenManager.srcType,Integer.toString(TokenManager.systemId));
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
    private void setTPRecord(String vehicleName) throws IOException {
        if (rfidHandler != null) {
            rfidHandler.getTprecord(vehicleName);
        }
    }
    private void vehicleNameKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_vehicleNameKeyReleased
        // TODO add your handling code here:
    }//GEN-LAST:event_vehicleNameKeyReleased

    private void iiaReceiptsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_iiaReceiptsMouseClicked
    }//GEN-LAST:event_iiaReceiptsMouseClicked

    private void iiaReceiptsFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_iiaReceiptsFocusGained
        setWhiteBackColor();
        iiaReceipts.requestFocusInWindow();
        iiaReceipts.setBackground(UIConstant.focusPanelColor);
    }//GEN-LAST:event_iiaReceiptsFocusGained

    private void iiaReceiptsKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_iiaReceiptsKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            setWhiteBackColor();
            SaveAndOpenGate.requestFocusInWindow();
        }
    }//GEN-LAST:event_iiaReceiptsKeyPressed

    private void qcDoneActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_qcDoneActionPerformed
        if (qcDone.getSelectedIndex() == 1) {
            iiaReceipts.setEnabled(true);
            setWhiteBackColor();
        } else {
            iiaReceipts.setEnabled(false);
            setWhiteBackColor();
        }
    }//GEN-LAST:event_qcDoneActionPerformed

    private void qcDoneFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_qcDoneFocusGained
        iiaReceipts.setEnabled(false);
        setWhiteBackColor();
    }//GEN-LAST:event_qcDoneFocusGained

    private void qcDoneKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_qcDoneKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            if (iiaReceipts.isEnabled()) {
                iiaReceipts.requestFocusInWindow();
                iiaReceipts.setBackground(UIConstant.focusPanelColor);
            } else {
                SaveAndOpenGate.requestFocusInWindow();
            }
        }
    }//GEN-LAST:event_qcDoneKeyPressed

    private void SaveAndOpenGateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SaveAndOpenGateActionPerformed
        quickCreateAndOpen();
    }//GEN-LAST:event_SaveAndOpenGateActionPerformed

    private void SaveAndOpenGateKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_SaveAndOpenGateKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            quickCreateAndOpen();
        }
    }//GEN-LAST:event_SaveAndOpenGateKeyPressed

    private void manualEntryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_manualEntryActionPerformed
        manualEntryAction();
    }//GEN-LAST:event_manualEntryActionPerformed

    private void clearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearActionPerformed
        clearAction();
    }//GEN-LAST:event_clearActionPerformed

    private void clearKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_clearKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            clearAction();
        }
    }//GEN-LAST:event_clearKeyPressed

    private void qcDoneItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_qcDoneItemStateChanged
    }//GEN-LAST:event_qcDoneItemStateChanged

    private void qcDoneFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_qcDoneFocusLost
//        if (qcDone.getSelectedIndex() == 1) {
//            // iiaReceipt.enable();
//            iiaReceipts.setFocusable(true);
//            iiaReceipts.setEditable(true);
//            //iiaReceipts.setEnabled(true);
//            iiaReceipts.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
//            //iiaReceipt.getText();
//            setWhiteBackColor();
//            iiaReceipts.requestFocusInWindow();
//            iiaReceipts.setBackground(UIConstant.focusPanelColor);
//            // iiaReceipt.setEnabled(true);
//        } else {
//            iiaReceipts.setFocusable(false);
//            iiaReceipts.setEditable(false);
//            iiaReceipts.setBorder(null);
//            // iiaReceipts.setEnabled(false);
//            //iiaReceipt.getText();
//            setWhiteBackColor();
//            SaveAndOpenGate.requestFocusInWindow();
//            //iiaReceipts.setBackground(UIConstant.PanelWhite);
//            // iiaReceipt.setEnabled(true);
//        }
    }//GEN-LAST:event_qcDoneFocusLost

    private void manualEntryKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_manualEntryKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            manualEntryAction();
        }
    }//GEN-LAST:event_manualEntryKeyPressed

    private void vehicleNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_vehicleNameActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_vehicleNameActionPerformed

    private void iiaReceiptsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_iiaReceiptsActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_iiaReceiptsActionPerformed

    private void vehicleNameFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_vehicleNameFocusLost
        // TODO add your handling code here:
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
            java.util.logging.Logger.getLogger(CoalYardOutWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(CoalYardOutWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(CoalYardOutWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(CoalYardOutWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    new CoalYardOutWindow().setVisible(true);
                } catch (IOException ex) {
                    Logger.getLogger(CoalYardOutWindow.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton SaveAndOpenGate;
    private javax.swing.JLabel blocking_reason;
    private java.awt.Button button1;
    public static javax.swing.JLabel challanNo;
    private javax.swing.JButton clear;
    private javax.swing.JButton manualEntryButton;
    public static javax.swing.JLabel digitalClock;
    private javax.swing.JLabel gpsOK;
    private javax.swing.JLabel gpsRepaired;
    private javax.swing.JLabel grade;
    private javax.swing.JTextField iiaReceipts;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
//    private javax.swing.JLabel jLabel11;
//    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel tprIdLabel;
    
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel supplier;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
//    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JLabel mines;
    private javax.swing.JLabel overrides;
    private javax.swing.JComboBox qcDone;
    private javax.swing.JLabel transporter;
    public static javax.swing.JLabel username;
    private AutoCompleteCombo vehicleName;
    private javax.swing.JLabel gpsTimeLabel;
    private javax.swing.JLabel gpsLocationLabel;
    private javax.swing.JLabel gpsLocation;
    private javax.swing.JLabel gpsTime;

    private void setWhiteBackColor() {
        vehicleName.setBackground(UIConstant.PanelWhite);
        iiaReceipts.setBackground(UIConstant.PanelWhite);
    }

    void stopRfid() {
        if (rfidHandler != null) {
            rfidHandler.stop();
        }
    }

    private void getFocus() {
        if (vehicleName.isTextEditable()) {
            setWhiteBackColor();
            vehicleName.requestFocusInWindow();
            vehicleName.setTextBackground(UIConstant.focusPanelColor);
        } else {
            setWhiteBackColor();
            qcDone.requestFocusInWindow();

            //   challanCollectedPanel.setBackground(UIConstant.focusPanelColor);
            // challanCollectedPanel.requestFocusInWindow();
            //grade.setBackground(UIConstant.focusPanelColor);
        }
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
    		else if (tprRecord == null) {
    			if (vehicleName.isTextEditable() && Utils.isNull(vehicleName.getText())) {
    				JOptionPane.showMessageDialog(null, "Please Enter Vehicle");
    			}
    			setWhiteBackColor();
    			return;
    		} /*else if (qcDone.getSelectedIndex() == 0) {
    			JOptionPane.showMessageDialog(null, "Please Select QC Done");
    			setWhiteBackColor();
    			qcDone.requestFocusInWindow();
    			//  vehicleName.setTextBackground(UIConstant.focusPanelColor);
    			return;
    		} else if (iiaReceipts.isEnabled() && Utils.isNull(iiaReceipts.getText())) {
    			JOptionPane.showMessageDialog(null, "Please Enter IIA Reciept No");
    			setWhiteBackColor();
    			iiaReceipts.requestFocusInWindow();
    			iiaReceipts.setBackground(UIConstant.focusPanelColor);
    			return;
    		} */else {
    			Triple<TPRecord, Integer, Boolean> tprTriplet = TPRInformation.getLatestNonWeb(conn, token.getVehicleId(), null, vehicleName.getText(), TokenManager.currWorkStationType, TokenManager.materialCat);//(conn, vehicleId, data, vehicleName, TokenManager.createNewTPR, workStationType);
				TPRecord tpr = tprTriplet != null ? tprTriplet.first : null;
				boolean repeatProcess = false;
				if(tpr != null && TPRInformation.isGreaterThanEqualsProcessed(tpr, TokenManager.currWorkStationType, TokenManager.materialCat)/*tpr.getTprId() != tprRecord.getTprId()*/){
					repeatProcess = true;
					tprRecord = tpr;
				}else{
					repeatProcess = false;
				}
				if(!repeatProcess)
					updateTPR(conn);
    			int stepId = InsertTPRStep(conn,false,repeatProcess);
    			if (stepId != Misc.getUndefInt()) {
    				InsertTPRQuestionDetails(conn, stepId);
    				//InsertQCDetail(conn, stepId);
    			}
    			conn.commit();
    			if (true) {
//    				JOptionPane.showMessageDialog(null, "Detail Saved");
    				
    				Barrier.openEntryGate();
    				//TokenManager.returnToken(conn, token);
    				clearInputs(conn, false);
    				getFocus();
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
    private void clearInputs(Connection conn, boolean clearToken) {
    	if(clearToken){
    		TokenManager.clearWorkstation();
    	}else{
    		if(token != null)
    			TokenManager.returnToken(conn, token);
    	}
    	pairVal = null;
    	tprIdLabel.setText("");
        vehicleName.setText("");
        overrides.setText("");
        transporter.setText("");
        mines.setText("");
        grade.setText("");
        gpsOk.clearSelection();
        iiaReceipts.setText("");
        iiaReceipts.setEnabled(false);
        challanNo.setText("");
        blocking_reason.setText("");
        qcDone.setSelectedIndex(0);
        gpsRepaired.setText("");
        SaveAndOpenGate.setText("Save And Open Gate");
        SaveAndOpenGate.setEnabled(false);
        if(isManual)
        	manualEntryButton.setEnabled(true);
        entryTime = null;
        exitTime = null;
        
        tprRecord = null;
        tprBlockManager = null;
        gpv = null;
        data = null;
        token = null;
        tpStep = null;
        tprQcDetail = null;
        
        isVehicleExist = false;
        vehicleBlackListed = false;
        isTagRead = false;
        isRequestOverride = false;
        
        isGpsOk = Misc.getUndefInt();
        qc_status = Misc.getUndefInt();
        toggleVehicle(false);
        enableDenyEntry(false);
        overrides.setText("");
        blocking_reason.setText("");
        
        gpsLocation.setText("");
        gpsTime.setText("");
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
    private void InsertQCDetail(Connection conn, int stepId) throws Exception {
        tprQcDetail = new TPRQCDetail();
        if (qcDone.getSelectedIndex() == 2) {
            tprQcDetail.setIiaReceipt_no(iiaReceipts.getText());
            tprQcDetail.setStatus(qcDone.getSelectedIndex());
        }
        tprQcDetail.setTprId(tprRecord.getTprId());
        tprQcDetail.setTpsId(stepId);
        tprQcDetail.setUpdatedBy(TokenManager.userId);
        tprQcDetail.setCreatedOn(new Date());
        tprQcDetail.setUpdatedOn(new Date());
        RFIDMasterDao.insert(conn, tprQcDetail,false);
        RFIDMasterDao.insert(conn, tprQcDetail,true);
    }

    private void updateTPR(Connection conn) throws Exception {
        updateTPR(conn, false);
    }

    private void updateTPR(Connection conn, boolean isDeny) throws Exception {
        if (!isDeny) {
            tprRecord.setPreStepType(TokenManager.currWorkStationType);
            tprRecord.setNextStepType(TokenManager.nextWorkStationType);
            tprRecord.setPreStepDate(new Date());
            tprRecord.setUpdatedBy(TokenManager.userId);
            tprRecord.setUpdatedOn(new Date());
            
            tprRecord.setLatestUnloadYardOutExit(new Date());
            if (tprRecord.getComboStart() == null) {
                tprRecord.setComboStart(new Date());
            }
            tprRecord.setComboEnd(new Date());
            if (TokenManager.closeTPR) {
                tprRecord.setTprStatus(Status.TPR.CLOSE);
                if (token.getEpcId() != null && token.getEpcId().length() >= 20 && rfidHandler != null) {
                    rfidHandler.clearData(Utils.HexStringToByteArray(token.getEpcId()), 5);
                }
            }
        }
        tprRecord.setEarliestUnloadYardOutEntry(entryTime);
        tprRecord.setUnloadYardOutName(TokenManager.userName);
        TPRInformation.insertUpdateTpr(conn, tprRecord);
        if(tprBlockManager != null)
        	tprBlockManager.setTprBlockStatus(conn, tprRecord.getTprId(),TokenManager.userId);
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
           // tpStep.setCoadYardOutGpsLocation(GateInDao.removeSpecialChar(gpsLocation.getText()));
            tpStep.setGpsLocation((gpsLocation.getText() != null && gpsLocation.getText().trim().length() > 0)? GateInDao.removeSpecialChar(gpsLocation.getText()) : "");
            tpStep.setGpsRecordTime((pairVal != null && !Misc.isUndef(pairVal.first))? new Date(pairVal.first) : null);
            tpStep.setUpdatedOn(new Date());
            if (qcDone.getSelectedIndex() == 1) {
                tpStep.setIiaReceiptNo(iiaReceipts.getText());
            }
            if(repeatProcess)
            	tpStep.setSaveStatus(TPStep.REPEAT_PROCESS);
            else
            tpStep.setSaveStatus(isDeny ? TPStep.REQUEST_OVERRIDE : TPStep.SAVE_AND_CONTINUE);
            tpStep.setHasValidRf(isTagRead ? 1 : 0);
            tpStep.setMaterialCat(TokenManager.materialCat);
            RFIDMasterDao.insert(conn, tpStep,false);
            RFIDMasterDao.insert(conn, tpStep,true);
        } else {
            long currTimeServerMillis = System.currentTimeMillis();
            tpStep.setExitTime(new Date(currTimeServerMillis));
            tpStep.setUpdatedOn(new Date(currTimeServerMillis));
            if (qcDone.getSelectedIndex() == 1) {
                tpStep.setIiaReceiptNo(iiaReceipts.getText());
            }
            if(repeatProcess)
            	tpStep.setSaveStatus(TPStep.REPEAT_PROCESS);
            else
            	tpStep.setSaveStatus(isDeny ? TPStep.REQUEST_OVERRIDE : TPStep.SAVE_AND_CONTINUE);
            tpStep.setHasValidRf(isTagRead ? 1 : 0);
            tpStep.setMaterialCat(TokenManager.materialCat);
            RFIDMasterDao.update(conn, tpStep,false);
            RFIDMasterDao.update(conn, tpStep,true);
        }
        return tpStep.getId();
    }

    private boolean InsertTPRQuestionDetails(Connection invConn1, int stepId) throws Exception {
        HashMap<Integer, Integer> quesAnsList = getQuestionIdList();
        boolean isInsert = false;
        for (Map.Entry<Integer, Integer> entry : quesAnsList.entrySet()) {
            Integer questionId = entry.getKey();
            Integer answerId = entry.getValue();
            GateInDao.updateTPRQuestion(invConn1, tprRecord.getTprId(), TokenManager.currWorkStationType, questionId, answerId, TokenManager.userId);
        }
        return isInsert;
    }

    private HashMap<Integer, Integer> getQuestionIdList() {
        HashMap<Integer, Integer> quesAnsList = new HashMap<Integer, Integer>();
        quesAnsList.put(Status.TPRQuestion.gpsOk, isGpsOk);
        return quesAnsList;
    }

    private void requestOverrideAction() {
        Connection conn = null;
        boolean destroyIt = false;
    	try {
    		conn = DBConnectionPool.getConnectionFromPoolNonWeb();
            updateTPR(conn, true);
            int stepId = InsertTPRStep(conn,true,false);
            //InsertTPRQuestionDetails(conn, stepId);
            conn.commit();
            clearInputs(conn, false);
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
    private void clearAction() {
    	Connection conn = null;
    	boolean destroyIt = false;
    	try{
    		conn = DBConnectionPool.getConnectionFromPoolNonWeb();
    		setWhiteBackColor();
    		clearInputs(conn, true);
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

    private void enableDenyEntry(boolean show) {
        if (show || vehicleBlackListed) {
            /*manualEntryButton.setEnabled(true);
            quickCreate.setEnabled(false);*/
        	isRequestOverride = true;
        	overrides.setText("BLOCKED");
        	SaveAndOpenGate.setText("Request Override");
        } else {
        	isRequestOverride = false;
        	overrides.setText("NOT_BLOCKED");
        	SaveAndOpenGate.setText("Save And Open Gate");
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
    				if (qc_status == 0) {
    					SaveAndOpenGate.requestFocusInWindow();
    				} else {
    					qcDone.requestFocusInWindow();
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
                SaveAndOpenGate.requestFocusInWindow();
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
