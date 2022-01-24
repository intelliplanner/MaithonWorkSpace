package com.ipssi.rfid.ui;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.sql.Connection;
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
import com.ipssi.rfid.beans.BedAssignmentDetails;
import com.ipssi.rfid.beans.BedDetails;
import com.ipssi.rfid.beans.BlockingInstruction;
import com.ipssi.rfid.beans.ComboItem;
import com.ipssi.rfid.beans.HopperDetails;
import com.ipssi.rfid.beans.RFIDHolder;
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
import com.ipssi.rfid.integration.WaveFormPlayer;
import com.ipssi.rfid.processor.TPRBlockManager;
import com.ipssi.rfid.processor.TPRInformation;
import com.ipssi.rfid.processor.TPRUtils;
import com.ipssi.rfid.processor.TokenManager;
import com.ipssi.rfid.processor.Utils;
import com.ipssi.rfid.readers.RFIDDataHandler;
import com.ipssi.rfid.readers.TAGListener;
import com.ipssi.rfid.ui.AutoCompleteCombo.ComboKeyEvent;

public class CoalYardInWindow extends javax.swing.JFrame {

	private static final long serialVersionUID = 1L;

	UIConstant ApplicationConstant;
	//flags
	private int contiNue = 1;
	private int reEnter = 0;

	private int readerId = 0;

	// private ButtonGroup gpsRepaired = new ButtonGroup();
	private ButtonGroup getGPSFixed = new ButtonGroup();
	private ButtonGroup informedIIA = new ButtonGroup();

	private RFIDDataHandler rfidHandler = null;


	private TPRecord tprRecord = null;
	private RFIDHolder data = null;
	private Date entryTime = null;
	private Date exitTime = null;
	private TPStep tpStep = null;
	Token token = null;

	private int isGpsRepaired = Misc.getUndefInt();
	private int qcMark = Misc.getUndefInt();
	private GpsPlusViolations gpv = null;

	private boolean isTagRead = false;
	private boolean vehicleBlackListed = false;
	private boolean isVehicleExist = false;
	private boolean isRequestOverride = false;
	private BlockingInstruction blockInstructionInformedGps = null;
	private TPRBlockManager tprBlockManager = null;

	private boolean isManual = false;

	public CoalYardInWindow() throws IOException {
		initComponents();

//        gpsLocation.setText("12345678901234567890123456789012345678901234567890");
//        gpsTime.setText("asdfghjkl;wertyuiod");
		this.setExtendedState(this.getExtendedState()
				| this.MAXIMIZED_BOTH);
		this.setTitle(ApplicationConstant.formTitle);
		myComponent();
		Clock.startClock("YardIn");
		Connection conn = null;
		boolean destroyIt = false;
		try{
			conn = DBConnectionPool.getConnectionFromPoolNonWeb();
			/*BlockingInstruction bIns = new BlockingInstruction();
    		bIns.setType(com.ipssi.rfid.constant.Type.BlockingInstruction.BLOCK_DUETO_NOT_INFORMED_GPS_VENDOR);
    		ArrayList<BlockingInstruction> list = (ArrayList<BlockingInstruction>) RFIDMasterDao.getList(conn, bIns, null);
    		if(list != null && list.size() > 0)
    			blockInstructionInformedGps = list.get(0);*/
			setBedList(conn);
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

	private void setTPRecord(String vehicleName) throws IOException {
		if (rfidHandler != null) {
			rfidHandler.getTprecord(vehicleName);
		}
	}

	public void setTPRecord(Connection conn, TPRecord tpr) throws IOException {
		System.out.println("######### Yard In setTPRecord  ########");
		try {
			tprRecord = tpr;
			if (tprRecord != null) {
				if (true) {//blockStatus == Status.VALIDATION.NO_ISSUE) {// TPR Valid
					toggleVehicle(false);
					
					isTagRead = token != null ? token.isReadFromTag() : false;
					if (token == null && tprRecord.getEarliestUnloadYardInEntry() != null) {
						entryTime = tprRecord.getEarliestUnloadYardInEntry();
					} else if (token != null && tprRecord.getEarliestUnloadYardInEntry() == null) {
						if (token.getLastSeen() != Misc.getUndefInt()) {
							entryTime = new Date(token.getLastSeen());
						} else {
							entryTime = new Date();
						}
					} else if (token != null && tprRecord.getEarliestUnloadYardInEntry() != null) {
						if (token.getLastSeen() > Utils.getDateTimeLong(tprRecord.getEarliestUnloadYardInEntry())) {
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
					lblChallanNoVal.setText(tprRecord.getChallanNo());
					mines.setText(DropDownValues.getMines(tprRecord.getMinesId(), conn));
					transporter.setText(DropDownValues.getTransporter(tprRecord.getTransporterId(), conn));
					grade.setText(DropDownValues.getGrade(tprRecord.getMaterialGradeId(), conn));
					ArrayList<Integer> soundList = new ArrayList<Integer>();
					if (tprRecord.getTprId() != Misc.getUndefInt()) {
						gpv = GpsPlusViolations.getGpsPlusViolatins(conn, tprRecord.getVehicleId(), tprRecord.getTprId(), System.currentTimeMillis(), tprRecord.getMinesId(), tprRecord.getChallanDate() == null ? System.currentTimeMillis() : tprRecord.getChallanDate().getTime());
					} else {
						gpv = GpsPlusViolations.getGpsPlusViolatins(conn, tprRecord.getVehicleId(), tprRecord.getTprId(), System.currentTimeMillis(), Misc.getUndefInt(), System.currentTimeMillis());
					}
					double unloadGross = tprRecord.getUnloadGross();
		    		if(unloadGross > 5000.0)
		    			unloadGross = (unloadGross/1000);
		    		double partyGross = tprRecord.getLoadGross();
		    		if(partyGross > 5000.0)
		    			partyGross = (partyGross/1000);
		    		Pair<ResultEnum, String> getMarkQC = gpv.getMarkForQC(conn, unloadGross, partyGross);
//					Pair<ResultEnum, String> getMarkQC = gpv.getMarkForQC(conn, tprRecord.getUnloadGross(), tprRecord.getLoadGross());
					if (getMarkQC.first == ResultEnum.GREEN) {
						qcRequired.setForeground(UIConstant.PanelDarkGreen);
						qcRequired.setText(getMarkQC.second);
						qcMark = UIConstant.NO;
						changeIIAInformed(false);
					} else if (getMarkQC.first == ResultEnum.RED) {
						qcRequired.setForeground(UIConstant.noActionPanelColor);
						qcRequired.setText(getMarkQC.second);
						qcMark = UIConstant.YES;
						changeIIAInformed(true);
					} else {
						qcRequired.setForeground(UIConstant.PanelYellow);
						qcRequired.setText(getMarkQC.second);
						qcMark = UIConstant.NC;
					}
					int selectedtransporter = tprRecord != null ? tprRecord.getTransporterId() : Misc.getUndefInt();
					int selectedMines = tprRecord != null ? tprRecord.getMinesId() : Misc.getUndefInt();
					int selectedGrade = tprRecord != null ? tprRecord.getMaterialGradeId() : Misc.getUndefInt();
					int selectedDO = tprRecord != null ? tprRecord.getDoId() : Misc.getUndefInt();
					Pair<Integer, String> bedAssign = TPRUtils.getBedAllignment(conn, selectedtransporter, selectedMines, selectedGrade, selectedDO);
					if(bedAssign != null)
						DropDownValues.setComboItem(bedAssigned, bedAssign.first);
					Pair<ResultEnum, String> gpsRepair = gpv.getGpsRepairNeeded(conn);
					if (gpsRepair.first == ResultEnum.GREEN) {
						gpsRepaired.setForeground(UIConstant.PanelDarkGreen);
						gpsRepaired.setText(gpsRepair.second);
						isGpsRepaired = UIConstant.NO;
						changeGPSFixedPanel(false);
					} else if (gpsRepair.first == ResultEnum.RED) {
						gpsRepaired.setForeground(UIConstant.noActionPanelColor);
						gpsRepaired.setText(gpsRepair.second);
						isGpsRepaired = UIConstant.YES;
						changeGPSFixedPanel(true);
						soundList.add(Status.TPRQuestion.barrierGps);
					} else {
						isGpsRepaired = UIConstant.NC;
						gpsRepaired.setForeground(UIConstant.PanelYellow);
						gpsRepaired.setText(gpsRepair.second);

					}
					setBlockingStatus();
					 Pair<Long, String> pairVal = GpsPlusViolations.getLatestLocation(conn, tprRecord.getVehicleId());
		                if(pairVal != null){
		                	String location = pairVal.second == null ? "" : pairVal.second;
		                	gpsTime.setText(GateInDao.getLongToDatetime(pairVal.first));
		                	gpsLocation.setText(GateInDao.getString(location, 40));
		                }
					
					//setBedList(conn);
					if(!vehicleBlackListed)
						WaveFormPlayer.playSoundSequence(soundList);
					quickCreate.setEnabled(true);
				} 
			} else {
				JOptionPane.showMessageDialog(null, "Invalid Vehicle Go to Registration");
				isVehicleExist = false;
				token = null;
				setWhiteBackground();
				getFocus();
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
		jPanel4 = new javax.swing.JPanel();
		jLabel2 = new javax.swing.JLabel();
		jLabel3 = new javax.swing.JLabel();
		jLabel4 = new javax.swing.JLabel();
		jLabel5 = new javax.swing.JLabel();
		vehicleName = new AutoCompleteCombo();
		jLabel7 = new javax.swing.JLabel();
		jLabel8 = new javax.swing.JLabel();
		overrides = new javax.swing.JLabel();
		transporter = new javax.swing.JLabel();
		mines = new javax.swing.JLabel();
		grade = new javax.swing.JLabel();
		bedAssigned = new javax.swing.JComboBox();
		jPanel2 = new javax.swing.JPanel();
		jLabel1 = new javax.swing.JLabel();
		jLabel6 = new javax.swing.JLabel();
		username = new javax.swing.JLabel();
		button1 = new java.awt.Button();
		jLabel12 = new javax.swing.JLabel();
		jPanel3 = new javax.swing.JPanel();
		informedIIALabel = new javax.swing.JLabel();
		getGPSFixedLabel = new javax.swing.JLabel();
		informedIIAPanel = new javax.swing.JPanel();
		informedIIANo = new javax.swing.JCheckBox();
		informedIIAYes = new javax.swing.JCheckBox();
		informedIIANC = new javax.swing.JCheckBox();
		getGPSFixedPanel = new javax.swing.JPanel();
		informedGpsVendorNo = new javax.swing.JCheckBox();
		informedGpsVendorYes = new javax.swing.JCheckBox();
		informedGpsVendorNC = new javax.swing.JCheckBox();
		jLabel10 = new javax.swing.JLabel();
		jLabel9 = new javax.swing.JLabel();
		qcRequired = new javax.swing.JLabel();
		
		gpsRepaired = new javax.swing.JLabel();
		jButton1 = new javax.swing.JButton();
		manualEntryButton = new javax.swing.JButton();
		quickCreate = new javax.swing.JButton();
		digitalClock = new javax.swing.JLabel();
		blocking_reason = new javax.swing.JLabel();
		lblChallanNo = new javax.swing.JLabel();
		lblChallanNoVal = new javax.swing.JLabel();

		jLabel11 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        gpsLocation = new javax.swing.JLabel();
        gpsTime = new javax.swing.JLabel();
        
		setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

		jPanel1.setBackground(new java.awt.Color(255, 255, 255));

		jPanel4.setBackground(new java.awt.Color(255, 255, 255));

		jLabel2.setFont(ApplicationConstant.labelFont);
		jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
		jLabel2.setText("Vehicle:");

		jLabel3.setFont(ApplicationConstant.labelFont);
		jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
		jLabel3.setText("Overrides?:");

		jLabel4.setFont(ApplicationConstant.labelFont);
		jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
		jLabel4.setText("Transporter:");

		jLabel5.setFont(ApplicationConstant.labelFont);
		jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
		jLabel5.setText("Mines:");

		/*vehicleName.setFont(UIConstant.vehicleLabel);
        vehicleName.setForeground(UIConstant.textFontColor);
        vehicleName.setEditable(false);
        vehicleName.setTextBackground(new java.awt.Color(255, 255, 255));
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
		jLabel7.setFont(ApplicationConstant.labelFont);
		jLabel7.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
		jLabel7.setText("Grade:");
		
		lblChallanNo.setFont(ApplicationConstant.labelFont);
		lblChallanNo.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
		lblChallanNo.setText("Challan No:");
		
		lblChallanNoVal.setFont(UIConstant.textFont);
		lblChallanNoVal.setForeground(UIConstant.textFontColor);
//		lblChallanNoVal.setText("Test Challan No");//comment this

		jLabel8.setFont(ApplicationConstant.labelFont);
		jLabel8.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
		jLabel8.setText("Bed Assigned:");

		overrides.setFont(UIConstant.textFont);
		overrides.setForeground(UIConstant.textFontColor);

		transporter.setFont(UIConstant.textFont);
		transporter.setForeground(UIConstant.textFontColor);
		
		mines.setFont(UIConstant.textFont);
		mines.setForeground(UIConstant.textFontColor);

		grade.setFont(UIConstant.textFont);
		grade.setForeground(UIConstant.textFontColor);

		bedAssigned.setFont(UIConstant.textFont);
		bedAssigned.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Select" }));
		bedAssigned.addMouseListener(new java.awt.event.MouseAdapter() {
			public void mouseClicked(java.awt.event.MouseEvent evt) {
				bedAssignedMouseClicked(evt);
			}
		});
		bedAssigned.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				bedAssignedActionPerformed(evt);
			}
		});
		bedAssigned.addFocusListener(new java.awt.event.FocusAdapter() {
			public void focusLost(java.awt.event.FocusEvent evt) {
				bedAssignedFocusLost(evt);
			}
		});
		bedAssigned.addKeyListener(new java.awt.event.KeyAdapter() {
			public void keyPressed(java.awt.event.KeyEvent evt) {
				bedAssignedKeyPressed(evt);
			}
		});

		javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
		jPanel4.setLayout(jPanel4Layout);
		jPanel4Layout.setHorizontalGroup(
				jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(jPanel4Layout.createSequentialGroup()
						.addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
								.addComponent(jLabel8, javax.swing.GroupLayout.DEFAULT_SIZE, 177, Short.MAX_VALUE)
								.addComponent(jLabel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(lblChallanNo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(jLabel3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(jLabel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(jLabel4, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
								.addGap(3, 3, 3)
								.addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
										.addComponent(bedAssigned, javax.swing.GroupLayout.PREFERRED_SIZE, 218, javax.swing.GroupLayout.PREFERRED_SIZE)
										.addComponent(overrides, javax.swing.GroupLayout.PREFERRED_SIZE, 215, javax.swing.GroupLayout.PREFERRED_SIZE)
										.addComponent(vehicleName, javax.swing.GroupLayout.PREFERRED_SIZE, 256, javax.swing.GroupLayout.PREFERRED_SIZE)
										.addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
												.addComponent(mines, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 215, Short.MAX_VALUE)
												.addComponent(transporter, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
												.addComponent(grade, javax.swing.GroupLayout.PREFERRED_SIZE, 215, javax.swing.GroupLayout.PREFERRED_SIZE)
												.addComponent(lblChallanNoVal, javax.swing.GroupLayout.PREFERRED_SIZE, 215, javax.swing.GroupLayout.PREFERRED_SIZE)
												)
												.addGap(16, 16, 16))
				);
		jPanel4Layout.setVerticalGroup(
				jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(jPanel4Layout.createSequentialGroup()
						.addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
								.addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, 40, Short.MAX_VALUE)
								.addComponent(vehicleName))
								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
								.addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
										.addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, 30, Short.MAX_VALUE)
										.addComponent(overrides, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
												.addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, 30, Short.MAX_VALUE)
												.addComponent(transporter, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
												.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
												.addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
														.addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, 30, Short.MAX_VALUE)
														.addComponent(mines, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
														.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
														.addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
																.addComponent(jLabel7, javax.swing.GroupLayout.DEFAULT_SIZE, 30, Short.MAX_VALUE)
																.addComponent(grade, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
																.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																.addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
																.addComponent(lblChallanNo, javax.swing.GroupLayout.DEFAULT_SIZE, 30, Short.MAX_VALUE)
																.addComponent(lblChallanNoVal, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
																.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																.addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
																		.addComponent(jLabel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
																		.addComponent(bedAssigned, javax.swing.GroupLayout.DEFAULT_SIZE, 30, Short.MAX_VALUE)))
				);

		jPanel2.setBackground(java.awt.SystemColor.controlLtHighlight);

		jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/ipssi/rfid/ui/intelliplanner.png"))); // NOI18N

		jLabel6.setFont(ApplicationConstant.subHeadingFont);
		jLabel6.setText("Yard In");

		username.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);

		button1.setBackground(java.awt.SystemColor.controlLtHighlight);
		button1.setFocusable(false);
		button1.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
		button1.setForeground(new java.awt.Color(0, 102, 153));
		button1.setLabel("Sign Out");
		button1.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				button1ActionPerformed(evt);
			}
		});

		jLabel12.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/ipssi/rfid/ui/cover_01_right_top.gif"))); // NOI18N

		javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
		jPanel2.setLayout(jPanel2Layout);
		jPanel2Layout.setHorizontalGroup(
				jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(jPanel2Layout.createSequentialGroup()
						.addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)
						.addGap(419, 419, 419)
						.addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 201, javax.swing.GroupLayout.PREFERRED_SIZE)
						.addGap(173, 173, 173)
						.addComponent(username, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(button1, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(jLabel12, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
				);
		jPanel2Layout.setVerticalGroup(
				jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addComponent(jLabel6, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(jLabel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(jLabel12, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
						.addContainerGap()
						.addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
								.addComponent(username, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(button1, javax.swing.GroupLayout.DEFAULT_SIZE, 36, Short.MAX_VALUE))
								.addContainerGap())
				);

		jPanel3.setBackground(new java.awt.Color(255, 255, 255));

		informedIIALabel.setFont(ApplicationConstant.labelFont);
		//informedIIALabel.setVisible(false);
		informedIIALabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
		informedIIALabel.setText("<html>Have you<br>Informed IIA?</html>");

		getGPSFixedLabel.setFont(ApplicationConstant.labelFont);
		getGPSFixedLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
		getGPSFixedLabel.setText("<html>Have you informed<br>Driver to get GPS Fixed?</html>");

		//informedIIAPanel.setVisible(false);
		informedIIAPanel.setBackground(new java.awt.Color(255, 255, 255));
		informedIIAPanel.addMouseListener(new java.awt.event.MouseAdapter() {
			public void mouseClicked(java.awt.event.MouseEvent evt) {
				informedIIAPanelMouseClicked(evt);
			}
		});
		informedIIAPanel.addKeyListener(new java.awt.event.KeyAdapter() {
			public void keyPressed(java.awt.event.KeyEvent evt) {
				informedIIAPanelKeyPressed(evt);
			}
		});

		informedIIANo.setText("No");
		informedIIANo.setFocusable(false);
		informedIIANo.setOpaque(false);
		informedIIANo.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				informedIIANoActionPerformed(evt);
			}
		});
		informedIIANo.addKeyListener(new java.awt.event.KeyAdapter() {
			public void keyPressed(java.awt.event.KeyEvent evt) {
				informedIIANoKeyPressed(evt);
			}
		});

		informedIIAYes.setText("Yes");
		informedIIAYes.setFocusable(false);
		informedIIAYes.setOpaque(false);
		informedIIAYes.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				informedIIAYesActionPerformed(evt);
			}
		});

		informedIIANC.setText("NC");
		informedIIANC.setFocusable(false);
		informedIIANC.setOpaque(false);
		informedIIANC.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				informedIIANCActionPerformed(evt);
			}
		});

		javax.swing.GroupLayout informedIIAPanelLayout = new javax.swing.GroupLayout(informedIIAPanel);
		informedIIAPanel.setLayout(informedIIAPanelLayout);
		informedIIAPanelLayout.setHorizontalGroup(
				informedIIAPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(javax.swing.GroupLayout.Alignment.TRAILING, informedIIAPanelLayout.createSequentialGroup()
						.addComponent(informedIIAYes)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(informedIIANo, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(informedIIANC, javax.swing.GroupLayout.DEFAULT_SIZE, 45, Short.MAX_VALUE))
				);
		informedIIAPanelLayout.setVerticalGroup(
				informedIIAPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(informedIIAPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
						.addComponent(informedIIANo)
						.addComponent(informedIIAYes, javax.swing.GroupLayout.DEFAULT_SIZE, 25, Short.MAX_VALUE)
						.addComponent(informedIIANC))
				);

		getGPSFixedPanel.setBackground(new java.awt.Color(255, 255, 255));
		getGPSFixedPanel.addMouseListener(new java.awt.event.MouseAdapter() {
			public void mouseClicked(java.awt.event.MouseEvent evt) {
				getGPSFixedPanelMouseClicked(evt);
			}
		});
		getGPSFixedPanel.addKeyListener(new java.awt.event.KeyAdapter() {
			public void keyPressed(java.awt.event.KeyEvent evt) {
				getGPSFixedPanelKeyPressed(evt);
			}
		});

		informedGpsVendorNo.setText("No");
		informedGpsVendorNo.setFocusable(false);
		informedGpsVendorNo.setOpaque(false);
		informedGpsVendorNo.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				getGPSFixedNoActionPerformed(evt);
			}
		});
		informedGpsVendorNo.addKeyListener(new java.awt.event.KeyAdapter() {
			public void keyPressed(java.awt.event.KeyEvent evt) {
				getGPSFixedNoKeyPressed(evt);
			}
		});

		informedGpsVendorYes.setText("Yes");
		informedGpsVendorYes.setFocusable(false);
		informedGpsVendorYes.setOpaque(false);
		informedGpsVendorYes.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				getGPSFixedYesActionPerformed(evt);
			}
		});

		informedGpsVendorNC.setText("NC");
		informedGpsVendorNC.setFocusable(false);
		informedGpsVendorNC.setOpaque(false);
		informedGpsVendorNC.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				getGPSFixedNCActionPerformed(evt);
			}
		});

		javax.swing.GroupLayout getGPSFixedPanelLayout = new javax.swing.GroupLayout(getGPSFixedPanel);
		getGPSFixedPanel.setLayout(getGPSFixedPanelLayout);
		getGPSFixedPanelLayout.setHorizontalGroup(
				getGPSFixedPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(javax.swing.GroupLayout.Alignment.TRAILING, getGPSFixedPanelLayout.createSequentialGroup()
						.addComponent(informedGpsVendorYes)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
						.addComponent(informedGpsVendorNo)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
						.addComponent(informedGpsVendorNC, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
				);
		getGPSFixedPanelLayout.setVerticalGroup(
				getGPSFixedPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(getGPSFixedPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
						.addComponent(informedGpsVendorNo)
						.addComponent(informedGpsVendorYes, javax.swing.GroupLayout.DEFAULT_SIZE, 25, Short.MAX_VALUE)
						.addComponent(informedGpsVendorNC))
				);

		jLabel10.setFont(ApplicationConstant.labelFont);
		jLabel10.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
		jLabel10.setText("QC Required:");

		jLabel9.setFont(ApplicationConstant.labelFont);
		jLabel9.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
		jLabel9.setText("GPS Repair Needed:");

		qcRequired.setFont(UIConstant.textFont);
		qcRequired.setForeground(UIConstant.textFontColor);

		gpsRepaired.setFont(UIConstant.textFont);
		gpsRepaired.setForeground(UIConstant.textFontColor);

		javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
		jPanel3.setLayout(jPanel3Layout);
		jPanel3Layout.setHorizontalGroup(
				jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(jPanel3Layout.createSequentialGroup()
						.addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
								.addComponent(jLabel9, javax.swing.GroupLayout.DEFAULT_SIZE, 164, Short.MAX_VALUE)
								.addComponent(jLabel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
								.addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
										.addComponent(qcRequired, javax.swing.GroupLayout.PREFERRED_SIZE, 215, javax.swing.GroupLayout.PREFERRED_SIZE)
										.addComponent(gpsRepaired, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 215, javax.swing.GroupLayout.PREFERRED_SIZE))
										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
										.addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
												.addComponent(getGPSFixedLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 219, javax.swing.GroupLayout.PREFERRED_SIZE)
												.addComponent(informedIIALabel, javax.swing.GroupLayout.PREFERRED_SIZE, 219, javax.swing.GroupLayout.PREFERRED_SIZE))
												.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
												.addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
														.addComponent(informedIIAPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
														.addComponent(getGPSFixedPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
														.addGap(16, 16, 16))
				);
		jPanel3Layout.setVerticalGroup(
				jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
						.addGap(0, 0, Short.MAX_VALUE)
						.addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
								.addComponent(getGPSFixedLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE)
								.addGroup(jPanel3Layout.createSequentialGroup()
										.addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
												.addComponent(informedIIALabel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
												.addGroup(jPanel3Layout.createSequentialGroup()
														.addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
																.addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
																		.addComponent(qcRequired, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
																		.addComponent(jLabel10, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 30, Short.MAX_VALUE))
																		.addGroup(jPanel3Layout.createSequentialGroup()
																				.addGap(10, 10, 10)
																				.addComponent(informedIIAPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
																				.addGap(7, 7, 7)))
																				.addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
																						.addGroup(jPanel3Layout.createSequentialGroup()
																								.addGap(34, 34, 34)
																								.addComponent(getGPSFixedPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
																								.addGap(14, 14, 14))
																								.addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
																										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																										.addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
																												.addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
																												.addComponent(gpsRepaired, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
																												.addGap(17, 17, 17)))))
																												.addGap(87, 87, 87))
				);

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

		quickCreate.setFont(ApplicationConstant.buttonFont);
		quickCreate.setText("Save And Open Gate");
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

		digitalClock.setFont(UIConstant.textFont); // NOI18N

		digitalClock.setForeground(UIConstant.textFontColor);
		digitalClock.setFont(new java.awt.Font("Segoe UI", 1, 36)); // NOI18N

		blocking_reason.setFont(UIConstant.textFont);
		blocking_reason.setForeground(UIConstant.noActionPanelColor);
		blocking_reason.setMinimumSize(null);

		 jLabel11.setFont(ApplicationConstant.labelFont);
	     jLabel11.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
	     jLabel11.setText("GPS Location:");

	     jLabel13.setFont(ApplicationConstant.labelFont);
	     jLabel13.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
	     jLabel13.setText("GPS Time:");

	     gpsLocation.setFont(UIConstant.textFont);
	     gpsLocation.setForeground(UIConstant.textFontColor);

	     gpsTime.setFont(UIConstant.textFont);
	     gpsTime.setForeground(UIConstant.textFontColor);
		
		javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
		jPanel1.setLayout(jPanel1Layout);
		jPanel1Layout.setHorizontalGroup(
				jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addGroup(jPanel1Layout.createSequentialGroup()
						.addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
								.addGroup(jPanel1Layout.createSequentialGroup()
										.addGap(308, 308, 308)
										.addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
										.addGap(55, 55, 55)
										.addComponent(manualEntryButton, javax.swing.GroupLayout.PREFERRED_SIZE, 177, javax.swing.GroupLayout.PREFERRED_SIZE)
										.addGap(42, 42, 42)
										.addComponent(quickCreate, javax.swing.GroupLayout.PREFERRED_SIZE, 221, javax.swing.GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
										.addComponent(digitalClock, javax.swing.GroupLayout.PREFERRED_SIZE, 179, javax.swing.GroupLayout.PREFERRED_SIZE))
										.addGroup(jPanel1Layout.createSequentialGroup()
												.addGap(252, 252, 252)
												.addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
														.addGroup(jPanel1Layout.createSequentialGroup()
																.addGap(10, 10, 10)
																.addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
																.addGroup(jPanel1Layout.createSequentialGroup()
										                                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
										                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
										                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
										                                    .addComponent(jLabel11, javax.swing.GroupLayout.DEFAULT_SIZE, 162, Short.MAX_VALUE)
										                                    .addComponent(jLabel13, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
										                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										                                .addComponent(gpsTime, javax.swing.GroupLayout.PREFERRED_SIZE, 231, javax.swing.GroupLayout.PREFERRED_SIZE)))
										                        .addGap(0, 0, Short.MAX_VALUE)))
										                .addContainerGap())
										            .addGroup(jPanel1Layout.createSequentialGroup()
										                .addContainerGap()
										                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
										                    .addComponent(blocking_reason, javax.swing.GroupLayout.PREFERRED_SIZE, 1234, javax.swing.GroupLayout.PREFERRED_SIZE)
										                    .addGroup(jPanel1Layout.createSequentialGroup()
										                        .addGap(0, 862, Short.MAX_VALUE)
										                        .addComponent(gpsLocation, javax.swing.GroupLayout.PREFERRED_SIZE, 415, javax.swing.GroupLayout.PREFERRED_SIZE)))
										                .addContainerGap(65, Short.MAX_VALUE))
				);
		jPanel1Layout.setVerticalGroup(
				jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(jPanel1Layout.createSequentialGroup()
						.addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
						.addComponent(blocking_reason, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
						
						  .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(63, 63, 63)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(gpsLocation, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(gpsTime, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                
						
						.addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, 184, javax.swing.GroupLayout.PREFERRED_SIZE)
						.addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
								.addGroup(jPanel1Layout.createSequentialGroup()
										.addGap(29, 29, 29)
										.addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
												.addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
												.addComponent(manualEntryButton, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
												.addComponent(quickCreate, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE))
												.addContainerGap(22, Short.MAX_VALUE))
												.addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
														.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
														.addComponent(digitalClock, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
														.addGap(20, 20, 20))))
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

	private void vehicleNameMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_vehicleNameMouseClicked
		setWhiteBackground();
		vehicleName.setTextBackground(UIConstant.focusPanelColor);
		vehicleName.requestFocusInWindow();
	}//GEN-LAST:event_vehicleNameMouseClicked

	private void vehicleNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_vehicleNameActionPerformed
		// TODO add your handling code here:
	}//GEN-LAST:event_vehicleNameActionPerformed

	private void vehicleNameKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_vehicleNameKeyPressed


		if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
			vehicleNameAction();
		}
	}//GEN-LAST:event_vehicleNameKeyPressed

	private void button1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button1ActionPerformed
		stopRfid();
		this.dispose();

		try {
			new LoginWindow().setVisible(true);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}//GEN-LAST:event_button1ActionPerformed

	private void informedIIANoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_informedIIANoActionPerformed
		setWhiteBackground();
		if (getGPSFixedPanel.isVisible()) {
			getGPSFixedPanel.setBackground(UIConstant.focusPanelColor);
			getGPSFixedPanel.requestFocusInWindow();
		} else
			quickCreate.requestFocusInWindow();
	}//GEN-LAST:event_informedIIANoActionPerformed

	private void informedIIANoKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_informedIIANoKeyPressed
	}//GEN-LAST:event_informedIIANoKeyPressed

	private void informedIIAYesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_informedIIAYesActionPerformed
		setWhiteBackground();
		if (getGPSFixedPanel.isVisible()) {
			getGPSFixedPanel.setBackground(UIConstant.focusPanelColor);
			getGPSFixedPanel.requestFocusInWindow();
		} else {
			quickCreate.requestFocusInWindow();
		}

	}//GEN-LAST:event_informedIIAYesActionPerformed

	private void informedIIANCActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_informedIIANCActionPerformed
		setWhiteBackground();
		if (getGPSFixedPanel.isVisible()) {
			getGPSFixedPanel.setBackground(UIConstant.focusPanelColor);
			getGPSFixedPanel.requestFocusInWindow();
		} else {
			quickCreate.requestFocusInWindow();
		}
	}//GEN-LAST:event_informedIIANCActionPerformed

	private void informedIIAPanelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_informedIIAPanelMouseClicked
		setWhiteBackground();
		informedIIAPanel.setBackground(UIConstant.focusPanelColor);
		informedIIAPanel.requestFocusInWindow();
	}//GEN-LAST:event_informedIIAPanelMouseClicked

	private void informedIIAPanelKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_informedIIAPanelKeyPressed
		if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
			setWhiteBackground();
			if (getGPSFixedPanel.isVisible()) {
				getGPSFixedPanel.requestFocusInWindow();
				getGPSFixedPanel.setBackground(UIConstant.focusPanelColor);
			} else {
				quickCreate.requestFocusInWindow();
			}
		} else if (evt.getKeyCode() == KeyEvent.VK_1 || evt.getKeyCode() == KeyEvent.VK_NUMPAD1) {
			informedIIAYes.setSelected(true);
			setWhiteBackground();

			if (getGPSFixedPanel.isVisible()) {
				getGPSFixedPanel.requestFocusInWindow();
				getGPSFixedPanel.setBackground(UIConstant.focusPanelColor);
			} else {
				quickCreate.requestFocusInWindow();
			}

		} else if (evt.getKeyCode() == KeyEvent.VK_2 || evt.getKeyCode() == KeyEvent.VK_NUMPAD2) {
			informedIIANo.setSelected(true);
			setWhiteBackground();

			if (getGPSFixedPanel.isVisible()) {
				getGPSFixedPanel.requestFocusInWindow();
				getGPSFixedPanel.setBackground(UIConstant.focusPanelColor);
			} else {
				quickCreate.requestFocusInWindow();
			}

		} else if (evt.getKeyCode() == KeyEvent.VK_3 || evt.getKeyCode() == KeyEvent.VK_NUMPAD3) {
			informedIIANC.setSelected(true);
			setWhiteBackground();

			if (getGPSFixedPanel.isVisible()) {
				getGPSFixedPanel.requestFocusInWindow();
				getGPSFixedPanel.setBackground(UIConstant.focusPanelColor);
			} else {
				quickCreate.requestFocusInWindow();
			}

		}
	}//GEN-LAST:event_informedIIAPanelKeyPressed

	private void getGPSFixedNoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_getGPSFixedNoActionPerformed
		setWhiteBackground();
		setQuetionsBlocking(Status.TPRQuestion.haveYouInformedGpsVendor, UIConstant.NO);
		/*if(TPRBlockStatusHelper.useForBlocking(Status.TPRQuestion.haveYouInformedGpsVendor)){
        	enableDenyEntry(true);
        	blocking_reason.setText(RFIDDataProcessor.getTprBlockStatus(tprRecord, blockInstructionInformedGps, true));
		}*/
		quickCreate.requestFocusInWindow();
	}//GEN-LAST:event_getGPSFixedNoActionPerformed

	private void getGPSFixedNoKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_getGPSFixedNoKeyPressed
		// TODO add your handling code here:
	}//GEN-LAST:event_getGPSFixedNoKeyPressed

	private void getGPSFixedYesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_getGPSFixedYesActionPerformed
		setWhiteBackground();
		setQuetionsBlocking(Status.TPRQuestion.haveYouInformedGpsVendor, UIConstant.YES);
		/*if(TPRBlockStatusHelper.useForBlocking(Status.TPRQuestion.haveYouInformedGpsVendor)){
        	enableDenyEntry(false);
        	blocking_reason.setText(RFIDDataProcessor.getTprBlockStatus(tprRecord, blockInstructionInformedGps, false));
		}*/
		quickCreate.requestFocusInWindow();
	}//GEN-LAST:event_getGPSFixedYesActionPerformed

	private void getGPSFixedNCActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_getGPSFixedNCActionPerformed
		setWhiteBackground();
		setQuetionsBlocking(Status.TPRQuestion.haveYouInformedGpsVendor, UIConstant.NC);
		/*if(TPRBlockStatusHelper.useForBlocking(Status.TPRQuestion.haveYouInformedGpsVendor)){
        	enableDenyEntry(false);
        	blocking_reason.setText(RFIDDataProcessor.getTprBlockStatus(tprRecord, blockInstructionInformedGps, false));
		}*/
		quickCreate.requestFocusInWindow();
	}//GEN-LAST:event_getGPSFixedNCActionPerformed

	private void getGPSFixedPanelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_getGPSFixedPanelMouseClicked
		setWhiteBackground();
		getGPSFixedPanel.setBackground(UIConstant.focusPanelColor);
		getGPSFixedPanel.requestFocusInWindow();
	}//GEN-LAST:event_getGPSFixedPanelMouseClicked

	private void getGPSFixedPanelKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_getGPSFixedPanelKeyPressed
		if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
			setWhiteBackground();
			quickCreate.requestFocusInWindow();
			//getGPSFixedPanel.setBackground(UIConstant.focusPanelColor);
		} else if (evt.getKeyCode() == KeyEvent.VK_1 || evt.getKeyCode() == KeyEvent.VK_NUMPAD1) {
			informedGpsVendorYes.setSelected(true);
			setQuetionsBlocking(Status.TPRQuestion.haveYouInformedGpsVendor, UIConstant.YES);
			/*if(TPRBlockStatusHelper.useForBlocking(Status.TPRQuestion.haveYouInformedGpsVendor)){
	        	enableDenyEntry(false);
	        	blocking_reason.setText(RFIDDataProcessor.getTprBlockStatus(tprRecord, blockInstructionInformedGps, false));
			}*/
			setWhiteBackground();
			quickCreate.requestFocusInWindow();
			//  getGPSFixedPanel.setBackground(UIConstant.focusPanelColor);
		} else if (evt.getKeyCode() == KeyEvent.VK_2 || evt.getKeyCode() == KeyEvent.VK_NUMPAD2) {
			informedGpsVendorNo.setSelected(true);
			setQuetionsBlocking(Status.TPRQuestion.haveYouInformedGpsVendor, UIConstant.NO);
			/*if(TPRBlockStatusHelper.useForBlocking(Status.TPRQuestion.haveYouInformedGpsVendor)){
	        	enableDenyEntry(true);
	        	blocking_reason.setText(RFIDDataProcessor.getTprBlockStatus(tprRecord, blockInstructionInformedGps, true));
			}*/
			setWhiteBackground();
			quickCreate.requestFocusInWindow();
			//   getGPSFixedPanel.setBackground(UIConstant.focusPanelColor);
		} else if (evt.getKeyCode() == KeyEvent.VK_3 || evt.getKeyCode() == KeyEvent.VK_NUMPAD3) {
			informedGpsVendorNC.setSelected(true);
			setQuetionsBlocking(Status.TPRQuestion.haveYouInformedGpsVendor, UIConstant.NC);
			/*if(TPRBlockStatusHelper.useForBlocking(Status.TPRQuestion.haveYouInformedGpsVendor)){
	        	enableDenyEntry(false);
	        	blocking_reason.setText(RFIDDataProcessor.getTprBlockStatus(tprRecord, blockInstructionInformedGps, false));
			}*/
			setWhiteBackground();
			quickCreate.requestFocusInWindow();
			//   getGPSFixedPanel.setBackground(UIConstant.focusPanelColor);
		}
	}//GEN-LAST:event_getGPSFixedPanelKeyPressed

	private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
		//        tpStepBean = null;
		clearAction();
	}//GEN-LAST:event_jButton1ActionPerformed

	private void jButton1KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jButton1KeyPressed
		if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
			clearAction();
		}
	}//GEN-LAST:event_jButton1KeyPressed

	private void manualEntryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_manualEntryActionPerformed
		manualEntryAction();
	}//GEN-LAST:event_manualEntryActionPerformed

	private void quickCreateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_quickCreateActionPerformed
		quickCreateAndOpen();
	}//GEN-LAST:event_quickCreateActionPerformed

	private void quickCreateKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_quickCreateKeyPressed
		if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
			quickCreateAndOpen();
		}
	}//GEN-LAST:event_quickCreateKeyPressed

	private void bedAssignedMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bedAssignedMouseClicked
		setWhiteBackground();
		bedAssigned.requestFocusInWindow();
		//        bedAssigned.setBackground(UIConstant.focusPanelColor);
	}//GEN-LAST:event_bedAssignedMouseClicked

	private void bedAssignedKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_bedAssignedKeyPressed
		if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
			setWhiteBackground();
			if (informedIIAPanel.isVisible()) {
				informedIIAPanel.requestFocusInWindow();
				informedIIAPanel.setBackground(UIConstant.focusPanelColor);
			} else if (getGPSFixedPanel.isVisible()) {
				getGPSFixedPanel.requestFocusInWindow();
				getGPSFixedPanel.setBackground(UIConstant.focusPanelColor);
			}else {
				quickCreate.requestFocusInWindow();
			}
		}
	}//GEN-LAST:event_bedAssignedKeyPressed

	private void manualEntryKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_denyEntryKeyPressed
		if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
			manualEntryAction();
		}
	}//GEN-LAST:event_denyEntryKeyPressed

	private void bedAssignedActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bedAssignedActionPerformed
		// TODO add your handling code here:
	}//GEN-LAST:event_bedAssignedActionPerformed

	private void bedAssignedFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_bedAssignedFocusLost
	}//GEN-LAST:event_bedAssignedFocusLost

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
			java.util.logging.Logger.getLogger(CoalYardInWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		} catch (InstantiationException ex) {
			java.util.logging.Logger.getLogger(CoalYardInWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		} catch (IllegalAccessException ex) {
			java.util.logging.Logger.getLogger(CoalYardInWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		} catch (javax.swing.UnsupportedLookAndFeelException ex) {
			java.util.logging.Logger.getLogger(CoalYardInWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		}
		//</editor-fold>

		/* Create and display the form */
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					new CoalYardInWindow().setVisible(true);
				} catch (IOException ex) {
					Logger.getLogger(CoalYardInWindow.class.getName()).log(Level.SEVERE, null, ex);
				}
			}
		});
	}
	// Variables declaration - do not modify//GEN-BEGIN:variables
	private javax.swing.JComboBox bedAssigned;
	private javax.swing.JLabel blocking_reason;
	private java.awt.Button button1;
	private javax.swing.JButton manualEntryButton;
	public static javax.swing.JLabel digitalClock;
	private javax.swing.JLabel getGPSFixedLabel;
	private javax.swing.JCheckBox informedGpsVendorNC;
	private javax.swing.JCheckBox informedGpsVendorNo;
	private javax.swing.JPanel getGPSFixedPanel;
	private javax.swing.JCheckBox informedGpsVendorYes;
	private javax.swing.JLabel gpsLocation;
    private javax.swing.JLabel gpsTime;
	private javax.swing.JLabel gpsRepaired;
	private javax.swing.JLabel grade;
	private javax.swing.JLabel informedIIALabel;
	private javax.swing.JCheckBox informedIIANC;
	private javax.swing.JCheckBox informedIIANo;
	private javax.swing.JPanel informedIIAPanel;
	private javax.swing.JCheckBox informedIIAYes;
	private javax.swing.JButton jButton1;
	private javax.swing.JLabel jLabel1;
	private javax.swing.JLabel jLabel10;
	private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
	private javax.swing.JLabel jLabel2;
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
	private javax.swing.JLabel mines;
	private javax.swing.JLabel overrides;
	private javax.swing.JLabel qcRequired;
	private javax.swing.JButton quickCreate;
	private javax.swing.JLabel transporter;
	public static javax.swing.JLabel username;
	private AutoCompleteCombo vehicleName;
	private javax.swing.JLabel lblChallanNo;
	private javax.swing.JLabel lblChallanNoVal;
	// End of variables declaration//GEN-END:variables

	private void updateTPR(Connection conn) throws Exception {
		updateTPR(conn, false);
	}

	private void updateTPR(Connection conn, boolean isDeny) throws Exception {
		//tprRecord.setVehicleName(vehicleName.getText());
		if(!isDeny){
			tprRecord.setMarkForQC(qcMark);
			tprRecord.setMarkForGPS(isGpsRepaired);
			tprRecord.setBedAssigned(bedAssigned.getSelectedItem().toString());
			tprRecord.setPreStepType(TokenManager.currWorkStationType);
			tprRecord.setNextStepType(TokenManager.nextWorkStationType);
			tprRecord.setPreStepDate(new Date());
			tprRecord.setUpdatedBy(TokenManager.userId);
			tprRecord.setUpdatedOn(new Date());
			tprRecord.setComboEnd(new Date());
			if (TokenManager.closeTPR) {
				tprRecord.setTprStatus(Status.TPR.CLOSE);
				if (token.getEpcId() != null && token.getEpcId().length() >= 20 && rfidHandler != null) {
					rfidHandler.clearData(Utils.HexStringToByteArray(token.getEpcId()), 5);
				}
			}
			tprRecord.setLatestUnloadYardInExit(new Date());
		}
		if (tprRecord.getComboStart() == null) {
			tprRecord.setComboStart(new Date());
		}
		tprRecord.setEarliestUnloadYardInEntry(entryTime);
		tprRecord.setUnloadYardInName(TokenManager.userName);
		TPRInformation.insertUpdateTpr(conn, tprRecord);
		if(tprBlockManager != null)
        	tprBlockManager.setTprBlockStatus(conn, tprRecord.getTprId(),TokenManager.userId);
	}

	private int InsertTPRStep(Connection conn,boolean isDeny) throws Exception {
		long currTimeServerMillis = System.currentTimeMillis();
		if (tpStep == null || Misc.isUndef(tpStep.getId())) {
			System.out.println("[Manual Creted TpStep]");
			tpStep = new TPStep();
			tpStep.setEntryTime(entryTime);
			tpStep.setExitTime(new Date(currTimeServerMillis));
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
			tpStep.setExitTime(new Date(currTimeServerMillis));
			tpStep.setUpdatedOn(new Date(currTimeServerMillis));
			tpStep.setHasValidRf(isTagRead ? 1 : 0);
			tpStep.setSaveStatus(isDeny ? TPStep.REQUEST_OVERRIDE : TPStep.SAVE_AND_CONTINUE);
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

		if (informedIIAYes.isSelected()) {
			quesAnsList.put(Status.TPRQuestion.haveYouInformedIIA, UIConstant.YES);
		} else if (informedIIANo.isSelected()) {
			quesAnsList.put(Status.TPRQuestion.haveYouInformedIIA, UIConstant.NO);
		} else if (informedIIANC.isSelected()) {
			quesAnsList.put(Status.TPRQuestion.haveYouInformedIIA, UIConstant.NC);
		} else {
			quesAnsList.put(Status.TPRQuestion.haveYouInformedIIA, Misc.getUndefInt());
		}
		if (informedGpsVendorYes.isSelected()) {
			quesAnsList.put(Status.TPRQuestion.haveYouInformedGpsVendor, UIConstant.YES);
		} else if (informedGpsVendorNo.isSelected()) {
			quesAnsList.put(Status.TPRQuestion.haveYouInformedGpsVendor, UIConstant.NO);
		} else if (informedGpsVendorNC.isSelected()) {
			quesAnsList.put(Status.TPRQuestion.haveYouInformedGpsVendor, UIConstant.NC);
		} else {
			quesAnsList.put(Status.TPRQuestion.haveYouInformedGpsVendor, Misc.getUndefInt());
		}
		return quesAnsList;
	}

	private void clearInputs(Connection conn, boolean clearToken) {
		if(clearToken){
			TokenManager.clearWorkstation();
		}else{
			if(token != null)
				TokenManager.returnToken(conn, token);
		}
		vehicleName.setText("");
		overrides.setText("");
		mines.setText("");
		lblChallanNoVal.setText("");
		grade.setText("");
		transporter.setText("");
		bedAssigned.setSelectedIndex(0);
		qcRequired.setText("");
		gpsRepaired.setText("");;
		informedIIA.clearSelection();
		getGPSFixed.clearSelection();
		blocking_reason.setText("");
		changeIIAInformed(true);
		changeGPSFixedPanel(true);

		quickCreate.setText("Save And Open Gate");
		quickCreate.setEnabled(false);
		if(isManual)
			manualEntryButton.setEnabled(true);
		
		gpsRepaired.setText("");
		tprRecord = null;
		tprBlockManager = null;
		data = null;
		entryTime = null;
		exitTime = null;
		tpStep = null;
		token = null;
		
		gpsLocation.setText("");
		gpsTime.setText("");

		isGpsRepaired = Misc.getUndefInt();
		qcMark = Misc.getUndefInt();
		gpv = null;

		isTagRead = false;
		vehicleBlackListed = false;
		isVehicleExist = false;
		isRequestOverride = false;
		//bedAssigned.removeAllItems();
		toggleVehicle(false);
		enableDenyEntry(false);
		overrides.setText("");
		setBedList(conn);
	}

	private void getFocus() {
		if (vehicleName.isTextEditable()) {
			setWhiteBackground();
			vehicleName.requestFocusInWindow();
			vehicleName.setTextBackground(UIConstant.focusPanelColor);
		} else {
			bedAssigned.requestFocusInWindow();
			//            bedAssigned.setBackground(UIConstant.focusPanelColor);
		}
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
	private void setWhiteBackground() {
		vehicleName.setBackground(UIConstant.PanelWhite);
		informedIIAPanel.setBackground(UIConstant.PanelWhite);
		getGPSFixedPanel.setBackground(UIConstant.PanelWhite);
		//        bedAssigned.setBackground(UIConstant.PanelWhite);
	}



	//    private void setTransporterList() {
	//        ArrayList<String> transporterList = DropDownValues.getTranporterList(conn);
	//        for (int i = 0; i < transporterList.size(); i++) {
	//            transporter.addItem(transporterList.get(i));
	//        }
	//    }
	//    private void setMinesList() {
	//        ArrayList<String> minesList = DropDownValues.getMinesList(conn);
	//        for (int i = 0; i < minesList.size(); i++) {
	//            mines.addItem(minesList.get(i));
	//        }
	//    }
	//    private void setGardeList() {
	//        ArrayList<String> gradeList = DropDownValues.getGradeList(conn);
	//        for (int i = 0; i < gradeList.size(); i++) {
	//            grade.addItem(gradeList.get(i));
	//        }
	//    }
	private void myComponent() {
		getGPSFixed.add(informedGpsVendorNo);
		getGPSFixed.add(informedGpsVendorYes);
		getGPSFixed.add(informedGpsVendorNC);
		informedIIA.add(informedIIANo);
		informedIIA.add(informedIIAYes);
		informedIIA.add(informedIIANC);
	}

	private void quickCreateAndOpen() {
		Connection conn = null;
		boolean destroyIt = false;
		try{
			conn = DBConnectionPool.getConnectionFromPoolNonWeb();
			//playBedAssignmentSound(conn);
			if(isRequestOverride){
				requestOverrideAction();
				return;
			}
			if (tprRecord == null) {
				if (vehicleName.isTextEditable() && Utils.isNull(vehicleName.getText())) {
					JOptionPane.showMessageDialog(null, "Please Enter Vehicle");
				}
				setWhiteBackground();
				return;
			} else if (qcMark == UIConstant.YES && !informedIIAYes.isSelected() && !informedIIANo.isSelected() && !informedIIANC.isSelected()) {
				JOptionPane.showMessageDialog(null, "Please Informed to IIA Or Ipssi");
				setWhiteBackground();
				informedIIAPanel.setBackground(UIConstant.focusPanelColor);
				informedIIAPanel.requestFocusInWindow();
			} else if (isGpsRepaired == UIConstant.YES && !informedGpsVendorYes.isSelected() && !informedGpsVendorNo.isSelected() && !informedGpsVendorNC.isSelected()) {
				JOptionPane.showMessageDialog(null, "Please Informed Driver to Fixed GPS");
				setWhiteBackground();
				getGPSFixedPanel.setBackground(UIConstant.focusPanelColor);
				getGPSFixedPanel.requestFocusInWindow();
			} else {
				updateTPR(conn);
				int stepId = InsertTPRStep(conn,false);
				if (stepId != Misc.getUndefInt()) {
					InsertTPRQuestionDetails(conn, stepId);
				}
				conn.commit();
				if (true) {
					JOptionPane.showMessageDialog(null, "Detail Saved");
					if(!vehicleBlackListed)
						playBedAssignmentSound(conn);
					Barrier.openEntryGate();
					clearInputs(conn,false);
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
			int stepId = InsertTPRStep(conn,true);
			InsertTPRQuestionDetails(conn, stepId);
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
			clearInputs(conn,false);
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
			setWhiteBackground();
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
			isRequestOverride = true;
			overrides.setText("BLOCKED");
			quickCreate.setText("Request Override");
		} else {
			isRequestOverride = false;
			overrides.setText("NOT_BLOCKED");
			quickCreate.setText("Save And Open Gate");
		}
	}

	private void setBedList(Connection conn) {
		try{
			bedAssigned.removeAllItems();
			bedAssigned.addItem(new ComboItem(Misc.getUndefInt(), "select"));
			ArrayList<Pair<Integer, String>> bedList = TPRUtils.getBedList(conn, Misc.getUndefInt(),Misc.getUndefInt(), Misc.getUndefInt(), Misc.getUndefInt());//TPRUtils.getBedList(conn, tprRecord != null ? tprRecord.getTransporterId() : Misc.getUndefInt(), tprRecord != null ? tprRecord.getMinesId() : Misc.getUndefInt(), tprRecord != null ? tprRecord.getMaterialGradeId() : Misc.getUndefInt()); 
			for (int i = 0,is=bedList == null ? 0 : bedList.size() ; i < is; i++) {
				bedAssigned.addItem(new ComboItem(bedList.get(i).first, bedList.get(i).second));
				
			}
			bedAssigned.setSelectedIndex(0);
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}

	private void changeIIAInformed(boolean isVal) {
		informedIIALabel.setVisible(isVal);
		informedIIAPanel.setVisible(isVal);
	}

	private void changeGPSFixedPanel(boolean isVal) {
		getGPSFixedPanel.setVisible(isVal);
		getGPSFixedLabel.setVisible(isVal);
	}

	private void playBedAssignmentSound(Connection conn) {
		if(conn == null)
			return;
		try{
			ArrayList<String> instructionList = new ArrayList<String>(); 
			BedAssignmentDetails bed = (BedAssignmentDetails) RFIDMasterDao.get(conn, BedAssignmentDetails.class, DropDownValues.getComboSelectedVal(bedAssigned));
			ArrayList<String> soundList = new ArrayList<String>();
			String preFix = "H";
			String se = "Se";
			String ya = "Ya";
			boolean isBed = false;
			if(bed != null){
				BedDetails bedDetails = Misc.isUndef(bed.getCurr_bed_module()) ? null  : (BedDetails) RFIDMasterDao.get(conn, BedDetails.class, bed.getCurr_bed_module());
				HopperDetails hopperOneSt = Misc.isUndef(bed.getCurr_bed_module()) ? null  : (HopperDetails) RFIDMasterDao.get(conn, HopperDetails.class, bed.getCurrStartHopperNo());
				HopperDetails hopperOneEn = Misc.isUndef(bed.getCurr_bed_module()) ? null : (HopperDetails) RFIDMasterDao.get(conn, HopperDetails.class, bed.getCurrEndHopperNo());
				HopperDetails hopperTwoSt = Misc.isUndef(bed.getCurr_bed_module()) ? null  : (HopperDetails) RFIDMasterDao.get(conn, HopperDetails.class, bed.getHopperTwoStart());
				HopperDetails hopperTwoEn = Misc.isUndef(bed.getCurr_bed_module()) ? null : (HopperDetails) RFIDMasterDao.get(conn, HopperDetails.class, bed.getHopperTwoEnd());
				if(bedDetails != null && bedDetails.getWaveSrc() != null && bedDetails.getWaveSrc().length() > 0){
					instructionList.add("Bed");
					instructionList.add(bedDetails.getWaveSrc());
				}
				if(hopperOneSt != null && hopperOneSt.getWaveSrc() != null && hopperOneSt.getWaveSrc().length() > 0){
					if(instructionList.size() > 0)
						instructionList.add(ya);
					instructionList.add("Hopper");
					instructionList.add(hopperOneSt.getWaveSrc());
				}
				if(hopperOneEn != null && hopperOneEn.getWaveSrc() != null && hopperOneEn.getWaveSrc().length() > 0){
					if(!Misc.isUndef(bed.getCurrStartHopperNo()))
						instructionList.add(se);
					instructionList.add(hopperOneEn.getWaveSrc());
				}
				if(hopperTwoSt != null && hopperTwoSt.getWaveSrc() != null && hopperTwoSt.getWaveSrc().length() > 0){
					if(!Misc.isUndef(bed.getCurrStartHopperNo()))
						instructionList.add(ya);
					instructionList.add("Hopper");
					instructionList.add(hopperTwoSt.getWaveSrc());
				}
				if(hopperTwoEn != null && hopperTwoEn.getWaveSrc() != null && hopperTwoEn.getWaveSrc().length() > 0){
					if(!Misc.isUndef(bed.getHopperTwoStart()))
						instructionList.add(se);
					instructionList.add(hopperTwoEn.getWaveSrc());
				}
				
				if(instructionList.size() > 0)
					instructionList.add("Record_0059");
				WaveFormPlayer.playSoundSequenceStr(instructionList);
				System.out.println(instructionList.toString());
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
		/*
    	switch (selectedIndex) {
            case 1:
                WaveFormPlayer.playSoundIn(Status.TPRQuestion.bedOne);
                break;
            case 2:
                WaveFormPlayer.playSoundIn(Status.TPRQuestion.bedTwo);
                break;
            case 3:
                WaveFormPlayer.playSoundIn(Status.TPRQuestion.bedThree);
                break;
            case 4:
                WaveFormPlayer.playSoundIn(Status.TPRQuestion.bedFour);
                break;
            case 5:
                WaveFormPlayer.playSoundIn(Status.TPRQuestion.bedFive);
                break;
            case 6:
                WaveFormPlayer.playSoundIn(Status.TPRQuestion.bedSix);
                break;
            case 7:
                WaveFormPlayer.playSoundIn(Status.TPRQuestion.hopperOne);
                break;
            case 8:
                WaveFormPlayer.playSoundIn(Status.TPRQuestion.hopperTwo);
                break;
            case 9:
                WaveFormPlayer.playSoundIn(Status.TPRQuestion.hopperThree);
                break;
            case 10:
                WaveFormPlayer.playSoundIn(Status.TPRQuestion.hopperFour);
                break;
            case 11:
                WaveFormPlayer.playSoundIn(Status.TPRQuestion.hopperFive);
                break;
            case 12:
                WaveFormPlayer.playSoundIn(Status.TPRQuestion.hopperSix);
                break;
            default:
                break;


        }*/
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
					int responseVehicleDialog = ConfirmationDialog.getDialogBox(new javax.swing.JFrame(), true, options, msg);
					if (responseVehicleDialog == reEnter) {
						setWhiteBackground();
						vehicleName.setText("");
						vehicleName.setTextBackground(UIConstant.focusPanelColor);
						return;
					} else if (responseVehicleDialog == contiNue) {
						setWhiteBackground();
						vehicleName.setText("");
						vehicleName.setTextBackground(UIConstant.focusPanelColor);
						return;
					}
				*/
				} else {
					setWhiteBackground();
					bedAssigned.requestFocusInWindow();
					setTPRecord(vehicleName.getText());
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