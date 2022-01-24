/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ipssi.rfid.ui;

import com.ipssi.common.ds.rule.GpsPlusViolations;
import com.ipssi.common.ds.rule.ResultEnum;
import com.ipssi.fingerprint.utils.SynServiceHandler;
import com.ipssi.gen.utils.CacheTrack;
import com.ipssi.gen.utils.DBConnectionPool;
import com.ipssi.rfid.constant.UIConstant;
import com.ipssi.rfid.processor.Utils;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.Pair;
import com.ipssi.gen.utils.Triple;
import com.ipssi.rfid.beans.Biometric;
import com.ipssi.rfid.beans.BlockingInstruction;
import com.ipssi.rfid.beans.ComboItem;
import com.ipssi.rfid.beans.RFIDHolder;
import com.ipssi.rfid.beans.RegistrationStatus;
import com.ipssi.rfid.beans.TPRecord;
import com.ipssi.rfid.beans.TPSQuestionDetail;
import com.ipssi.rfid.beans.TPStep;
import com.ipssi.rfid.beans.Token;
import com.ipssi.rfid.beans.Vehicle;
import com.ipssi.rfid.constant.Constant;
import com.ipssi.rfid.constant.Results;
import com.ipssi.rfid.constant.Status;
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
import com.ipssi.rfid.processor.TPRBlockStatusHelper;
import com.ipssi.rfid.processor.TPRInformation;
import com.ipssi.rfid.processor.TPRUtils;
import com.ipssi.rfid.processor.TokenManager;
import com.ipssi.rfid.readers.RFIDDataHandler;
import com.ipssi.rfid.readers.RFIDDataProcessor;
import com.ipssi.rfid.readers.TAGListener;
import static com.ipssi.rfid.ui.GateInFlyAshWindow.transporter;
import static com.ipssi.rfid.ui.GateInFlyAshWindow.dlNo;
import static com.ipssi.rfid.ui.GateInFlyAshWindow.helperOk; //import static com.ipssi.rfid.ui.GateInFlyAshWindow.isFingerCaptured;
import static com.ipssi.rfid.ui.GateInFlyAshWindow.isFingerVerified;
import static com.ipssi.rfid.ui.GateInFlyAshWindow.isTpRecordValid;
import static com.ipssi.rfid.ui.GateInFlyAshWindow.photo;
import com.scl.loadlibrary.DriverDetail;
import com.scl.loadlibrary.FingurePrintHandler;
import com.scl.loadlibrary.FingurePrintService;
import com.scl.loadlibrary.LoadLibrary;
import com.scl.loadlibrary.MorphoActionI;
import com.scl.loadlibrary.MorphoSmartFunctions;

import java.awt.Color;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
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
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;

import morpho.morphosmart.sdk.api.MorphoImage;
import morpho.morphosmart.sdk.demo.ennum.EnumMoveFinger;
import morpho.morphosmart.sdk.demo.trt.ImageLoader;
import morpho.morphosmart.sdk.demo.trt.MorphoEventHandler;

/**
 * 
 * @author Vi$ky
 */
public class GateInFlyAshWindow extends javax.swing.JFrame implements
		MorphoActionI {

	// private javax.swing.ButtonGroup buttonGroup1;
	private ButtonGroup sealOk = new ButtonGroup();
	private ButtonGroup tarpaulin = new ButtonGroup();
	private ButtonGroup numberVisible = new ButtonGroup();
	private ButtonGroup tailLightOk = new ButtonGroup();
	private ButtonGroup sideMirrorOk = new ButtonGroup();
	private ButtonGroup leftSideIndicator = new ButtonGroup();
	private ButtonGroup seatBelt = new ButtonGroup();
	private ButtonGroup headLightOk = new ButtonGroup();
	private ButtonGroup reverseHornOk = new ButtonGroup();
	private ButtonGroup breathlizerOk = new ButtonGroup();
	public static ButtonGroup helperOk = new ButtonGroup();
	private ButtonGroup rightSideIndicator = new ButtonGroup();
	public static boolean isTpRecordValid = false;
	public static String currentVehicleName = "";
	private int responseVehicleDialog = Misc.getUndefInt();
	private SyncFingerPrint fingerPrintSyncService = null;
	int contiNue = 1;
	int reEnter = 0;
	private static String instructions = "";
	public static int userBy = Misc.getUndefInt();
	private int readerId = 0;
	private int readerType = com.ipssi.rfid.constant.Type.Reader.IN;
	private Date entryTime = null;
	boolean isVehicleExist = false;

	public static boolean isFingerVerified = false;
	private boolean isDriverExist = false;
	public static boolean isFingerCaptured = false;
	private boolean isFingerExist = false;
	private boolean isDriverBlacklisted = false;

	private boolean isAutoFingerCaptureStart = false;
	private RFIDDataHandler rfidHandler = null;
	private TPRecord tprRecord = null;
	public static Biometric driverInformation = null;
	// private Connection conn = null;
	// private Connection dbConnectionRInFID = null;
	private Token token = null;
	private RegistrationStatus regisBean = null;
	// AutoComplete auto_combo = null;
	private boolean isSeviceRunning = false;
	private boolean isMorphoExist = false;
	private boolean isDataSyncRunning = false;
	private boolean vehicleBlackListed = false;
	private boolean isTagRead = false;
	private FingurePrintService fingurePrintCaptureService = null;
	private boolean isRequestOverride = false;
	private TPStep tpStep = null;
	// private Connection morphoSyncConn = null;
	// private Connection fingurePrintConn = null;
	private int fitnessOk = Misc.getUndefInt();
	private int roadPermitOk = Misc.getUndefInt();
	private int insuranceOk = Misc.getUndefInt();
	private int polutionOk = Misc.getUndefInt();
	private boolean isPaperOk = false;
	private boolean doCheck = false;
	private int driverSrc = Misc.getUndefInt();
	private AutoComplete auto_complete = null;
	private BlockingInstruction blockInstructionBreathelizer = null;
	private TPRBlockManager tprBlockManager = null;
	private GpsPlusViolations gpv = null;
	private boolean isFingerPrintCaptureRunning = false;
	private int isNewVehicle = Misc.getUndefInt();
	private int captureCount = 1;
	private String gpsStat = "";
	private byte[] debugTemplate = null;
	/**
	 * Creates new form GateInFlyAshWindow
	 */
	private Icon iconArrowUp = new ImageIcon(ImageLoader.load("arrow_up.png"));
	private Icon iconArrowRight = new ImageIcon(ImageLoader
			.load("arrow_right.png"));
	private Icon iconArrowDown = new ImageIcon(ImageLoader
			.load("arrow_down.png"));
	private Icon iconArrowLeft = new ImageIcon(ImageLoader
			.load("arrow_left.png"));
	private Icon gifCapture = new ImageIcon(ImageLoader.load("capture.gif"));
	private Icon gifMaillage = new ImageIcon(ImageLoader.load("maillage.gif"));
	private ImageIcon imgFvpOk = new ImageIcon(ImageLoader.load("ok.png"));
	private ImageIcon imgFvpKo = new ImageIcon(ImageLoader.load("ko.png"));
	private boolean isManualEntry = false;
	private boolean manualCaptureFinger = false;

	public GateInFlyAshWindow() throws IOException {
		Connection conn = null;
		boolean destroyIt = false;

		try {
			initComponents();
			// gpsLocation.setText("123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890");
			// gpsTime.setText("kjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjs");
			// driverHours.setText("srdfghjkl");
			this
					.setExtendedState(this.getExtendedState()
							| this.MAXIMIZED_BOTH);
			this.setTitle(UIConstant.formTitle);
			myComponent();

			conn = DBConnectionPool.getConnectionFromPoolNonWeb();
			DropDownValues.setTransporterList(transporter, conn,
					TokenManager.materialCat);
			getFocus();
			auto_complete = new AutoComplete(vehicleName);
			auto_complete.setKeyEvent(new AutoComplete.ComboKeyEvent() {
				@Override
				public void onKeyPress(KeyEvent evt) {
					if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
						vehicleNameAction();
					}
				}
			});
			isMorphoExist = TokenManager.morphoDeviceExist == 1
					&& (TokenManager.useSDK() ? MorphoSmartFunctions
							.getMorpho().isConnected() : LoadLibrary
							.isMorphoConnected());
			changeDriverPanel(isMorphoExist);
			setFingerPrintPanel(false);
			System.out.println("######### Gate IN Start  ########");

			start();
			Clock.startClock("FlyAshIn");
			if (TokenManager.isManualEntry
					.containsKey(TokenManager.currWorkStationId)) {
				int val = TokenManager.isManualEntry
						.get(TokenManager.currWorkStationId);
				if (val == 1) {
					isManualEntry = true;
				} else {
					isManualEntry = false;
					manualEntryButton.setEnabled(false);
				}
			} else {
				isManualEntry = false;
				manualEntryButton.setEnabled(false);
			}
			com.ipssi.rfid.beans.BlockingInstruction bIns = new com.ipssi.rfid.beans.BlockingInstruction();
			bIns
					.setType(com.ipssi.rfid.constant.Type.BlockingInstruction.BLOCK_DUETO_DRUNCK);
			ArrayList<com.ipssi.rfid.beans.BlockingInstruction> list = (ArrayList<com.ipssi.rfid.beans.BlockingInstruction>) RFIDMasterDao
					.getList(conn, bIns, null);
			if (list != null && list.size() > 0) {
				blockInstructionBreathelizer = list.get(0);
			}
			if (isMorphoExist) {
				syncFingerPrintDataFromServer();
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

	public void start() throws IOException {
		if (rfidHandler == null) {
			rfidHandler = new RFIDDataHandler(1000, readerId,
					TokenManager.currWorkStationType,
					TokenManager.currWorkStationId, TokenManager.userId);
			rfidHandler.setTagListener(new TAGListener() {
				@Override
				public void manageTag(Connection conn, Token _token,
						TPRecord tpr, TPStep tps,
						TPRBlockManager _tprBlockManager) {
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
					// vehicleName.addItem("");
				}

				@Override
				public int promptMessage(String message, Object[] options) {
					return ConfirmationDialog.getDialogBox(
							new javax.swing.JFrame(), true, options, message);
				}

				@Override
				public void clear(boolean clearToken, Connection conn) {
					// TODO Auto-generated method stub
					clearInput(clearToken, conn);
				}

				@Override
				public int mergeData(long sessionId, String epc,
						RFIDHolder rfidHolder) {
					// TODO Auto-generated method stub
					return 0;
				}
			});
		}
		rfidHandler.start();
		if (fingurePrintCaptureService == null) {
			fingurePrintCaptureService = new FingurePrintService(1000,
					TokenManager.currWorkStationType,
					TokenManager.currWorkStationId, TokenManager.userId);
			fingurePrintCaptureService.setListener(new FingurePrintHandler() {
				@Override
				public void showMessage(String message) {
					// TODO Auto-generated method stub
					fingerInstruction.setText("No Match Found");
					WaveFormPlayer
							.playSoundIn(Status.TPRQuestion.fingerNotMatch);
					/*
					 * driverId.setEditable(true); driverId.setFocusable(true);
					 * // driverId.requestFocusInWindow();
					 * driverId.setBorder(javax
					 * .swing.BorderFactory.createLineBorder(new
					 * java.awt.Color(0, 0, 0))); dlNo.setEditable(true);
					 * dlNo.setFocusable(true); //
					 * CoalGateInWindow.dlNo.requestFocusInWindow();
					 * dlNo.setBorder
					 * (javax.swing.BorderFactory.createLineBorder(new
					 * java.awt.Color(0, 0, 0)));
					 */
					disableFingurePrintCapture();
				}

				@Override
				public int promptMessage(String message, Object[] options) {
					// TODO Auto-generated method stub
					return 0;
				}

				@Override
				public void onChange(Connection conn, Biometric driverBean,
						boolean fingerVerified, boolean fingerCaptured) {
					populateDriver(conn, driverBean, fingerVerified,
							fingerCaptured);
				}

				@Override
				public void statusChange(boolean status) {
					changeFingerButtonText(status);
				}

				@Override
				public void setDebugTemplate(byte[] data) {
					// TODO Auto-generated method stub
					debugTemplate  = data; 	
				}
			}, this);
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
			// Triple<Token, TPRecord, Integer> tpRecord =
			// rfidHandler.getTprecord(conn, vehicleName);
			// if (tpRecord != null) {
			// token = tpRecord.first;
			// setTPRecord(tpRecord.second, tpRecord.third);
			// }
		}
	}

	public void setTPRecord(Connection conn, TPRecord tpr) throws IOException {
		System.out.println("######### Gate IN setTPRecord  ########");
		try {
			tprRecord = tpr;
			if (tprRecord != null) {
				if (true) {// blockStatus == Status.VALIDATION.NO_ISSUE) {// TPR
					// Valid
					toggleVehicle(false);
					isTpRecordValid = true;
					isVehicleExist = true;
					isNewVehicle = Status.VEHICLE.EXISTING_RF;
					isTagRead = token != null ? token.isReadFromTag() : false;
					fitnessOk = TPRUtils.isVehicleDocumentComplete(conn,
							tprRecord.getVehicleId(),
							Status.TPRQuestion.isFitnessOk,
							TokenManager.fitnessExpiaryThreshold);
					roadPermitOk = TPRUtils.isVehicleDocumentComplete(conn,
							tprRecord.getVehicleId(),
							Status.TPRQuestion.isRoadPermitOk,
							TokenManager.roadPermitExpiaryThreshold);
					insuranceOk = TPRUtils.isVehicleDocumentComplete(conn,
							tprRecord.getVehicleId(),
							Status.TPRQuestion.isInsuranceOk,
							TokenManager.insauranceExpiaryThreshold);
					polutionOk = TPRUtils.isVehicleDocumentComplete(conn,
							tprRecord.getVehicleId(),
							Status.TPRQuestion.isPolutionOk,
							TokenManager.polutionExpiaryThreshold);
					isPaperOk = tprBlockManager == null
							|| ((!tprBlockManager
									.useForBlocking(Status.TPRQuestion.isFitnessOk) || fitnessOk == Results.Questions.YES)
									&& (!tprBlockManager
											.useForBlocking(Status.TPRQuestion.isRoadPermitOk) || roadPermitOk == Results.Questions.YES)
									&& (!tprBlockManager
											.useForBlocking(Status.TPRQuestion.isInsuranceOk) || insuranceOk == Results.Questions.YES) && (!tprBlockManager
									.useForBlocking(Status.TPRQuestion.isPolutionOk) || polutionOk == Results.Questions.YES));
					if (isPaperOk) {
						paperValid.setForeground(UIConstant.textFontColor);
						paperValid.setText("Yes");
					} else {
						paperValid.setForeground(Color.RED);
						paperValid.setText("No");
					}
					if (token == null
							&& tprRecord.getEarliestUnloadGateInEntry() != null) {
						entryTime = tprRecord.getEarliestUnloadGateInEntry();
					} else if (token != null
							&& tprRecord.getEarliestUnloadGateInEntry() == null) {
						if (token.getLastSeen() != Misc.getUndefInt()) {
							entryTime = new Date(token.getLastSeen());
						} else {
							entryTime = new Date();
						}
					} else if (token != null
							&& tprRecord.getEarliestUnloadGateInEntry() != null) {
						if (token.getLastSeen() > Utils
								.getDateTimeLong(tprRecord
										.getEarliestUnloadGateInEntry())) {
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

					if (tprRecord.getTransporterId() != Misc.getUndefInt()
							&& tprRecord.getTransporterId() != 0) {
						DropDownValues.setComboItem(transporter, tprRecord
								.getTransporterId());
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

					Pair<Long, String> pairVal = GpsPlusViolations
							.getLatestLocation(conn, tprRecord.getVehicleId());
					if (pairVal != null) {
						String location = pairVal.second == null ? ""
								: pairVal.second;
						gpsTime.setText(GateInDao
								.getLongToDatetime(pairVal.first));
						gpsLocation.setText(GateInDao.getString(location, 40));
					}
					if (gpv == null) {
						gpv = GpsPlusViolations.getGpsPlusViolatins(conn,
								tprRecord.getVehicleId(), tprRecord.getTprId(),
								System.currentTimeMillis(), Misc.getUndefInt(),
								System.currentTimeMillis());
					}
					Pair<ResultEnum, String> gpsSafetyViolation = gpv
							.getSafetyViolations(conn);
					if (gpsSafetyViolation.first == ResultEnum.GREEN) {
						gpsStat = "Ok";
					} else if (gpsSafetyViolation.first == ResultEnum.RED) {
						gpsStat = "Not Ok";
					} else {
						gpsStat = "Ok";
					}

					setBlockingStatus();
					saveButton.setEnabled(true);
				} else {
					/*
					 * if (!Utils.isNull(vehicle_name) &&
					 * !(vehicleName.getText()
					 * .trim()).equalsIgnoreCase("NO VEHICLE DETECTED")) {
					 * JOptionPane.showMessageDialog(null,
					 * "Invalid Vehicle Go to Registration"); } else if
					 * (Utils.isNull(vehicle_name)) {
					 * JOptionPane.showMessageDialog(null,
					 * "Invalid Vehicle Go to Registration"); }
					 */
					System.out.println("Error No TPR found");
					// JOptionPane.showMessageDialog(null,
					// "Invalid Vehicle Go to Registration");
					isTpRecordValid = false;
					isVehicleExist = false;
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void setQuetionsBlocking(int questionId, int answerId) {
		if (Misc.isUndef(questionId)) {
			return;
		}
		if (tprBlockManager != null) {
			TPSQuestionDetail tpsQuestionBean = new TPSQuestionDetail();
			tpsQuestionBean.setQuestionId(questionId);
			tpsQuestionBean.setAnswerId(answerId);
			tprBlockManager.addQuestions(tpsQuestionBean);
			setBlockingStatus();
		}
	}

	// public void setTPRecord(TPRecord tpr, int statusCode) throws IOException
	// {
	// System.out.println("######### Gate IN setTPRecord  ########");
	// Connection conn = null;
	// boolean destroyIt = false;
	// try {
	// conn = DBConnectionPool.getConnectionFromPoolNonWeb();
	// tprRecord = tpr;
	// if (tprRecord != null) {
	// if (true) {//blockStatus == Status.VALIDATION.NO_ISSUE) {// TPR Valid
	// toggleVehicle(false);
	// isTpRecordValid = true;
	// isVehicleExist = true;
	// isTagRead = token != null ? token.isReadFromTag() : false;
	// fitnessOk = TPRUtils.isVehicleDocumentComplete(conn,
	// tprRecord.getVehicleId(), Status.TPRQuestion.isFitnessOk,
	// TokenManager.fitnessExpiaryThreshold);
	// roadPermitOk = TPRUtils.isVehicleDocumentComplete(conn,
	// tprRecord.getVehicleId(), Status.TPRQuestion.isRoadPermitOk,
	// TokenManager.roadPermitExpiaryThreshold);
	// insuranceOk = TPRUtils.isVehicleDocumentComplete(conn,
	// tprRecord.getVehicleId(), Status.TPRQuestion.isInsuranceOk,
	// TokenManager.insauranceExpiaryThreshold);
	// polutionOk = TPRUtils.isVehicleDocumentComplete(conn,
	// tprRecord.getVehicleId(), Status.TPRQuestion.isPolutionOk,
	// TokenManager.polutionExpiaryThreshold);
	// isPaperOk = fitnessOk == Results.Questions.YES && roadPermitOk ==
	// Results.Questions.YES && insuranceOk == Results.Questions.YES &&
	// polutionOk == Results.Questions.YES;
	// if (isPaperOk) {
	// paperValid.setText("Yes");
	// } else {
	// paperValid.setText("No");
	// }
	// if (token == null && tprRecord.getEarliestUnloadGateInEntry() != null) {
	// entryTime = tprRecord.getEarliestUnloadGateInEntry();
	// } else if (token != null && tprRecord.getEarliestUnloadGateInEntry() ==
	// null) {
	// if (token.getLastSeen() != Misc.getUndefInt()) {
	// entryTime = new Date(token.getLastSeen());
	// } else {
	// entryTime = new Date();
	// }
	// } else if (token != null && tprRecord.getEarliestUnloadGateInEntry() !=
	// null) {
	// if (token.getLastSeen() >
	// Utils.getDateTimeLong(tprRecord.getEarliestUnloadGateInEntry())) {
	// if (token.getLastSeen() != Misc.getUndefInt()) {
	// entryTime = new Date(token.getLastSeen());
	// } else {
	// entryTime = new Date();
	// }
	// System.out.println("token " + entryTime);
	// } else {
	// entryTime = new Date();
	// }
	// } else {
	// entryTime = new Date();
	// }
	// if (Utils.isNull(tprRecord.getVehicleName())) {
	// vehicleName.removeAllItems();
	// vehicleName.addItem(tprRecord.getVehicleName());
	// } else {
	// vehicleName.removeAllItems();
	// vehicleName.addItem(tprRecord.getVehicleName());
	// }
	//
	// if (tprRecord.getTransporterId() != Misc.getUndefInt() &&
	// tprRecord.getTransporterId() != 0) {
	// DropDownValues.setComboItem(transporter, tprRecord.getTransporterId());
	// seatBeltPanel.requestFocusInWindow();
	// seatBeltPanel.setBackground(UIConstant.focusPanelColor);
	// playSeatBeltWormVoice();
	// } else {
	// transporter.setSelectedIndex(0);
	// transporter.setFocusable(true);
	// transporter.setEnabled(true);
	// transporter.requestFocusInWindow();
	// setWhiteBackColor();
	// }
	//
	// }
	// saveButton.setEnabled(true);
	// } else {
	// /*
	// * if (!Utils.isNull(vehicle_name) &&
	// !(vehicleName.getText().trim()).equalsIgnoreCase("NO VEHICLE DETECTED"))
	// { JOptionPane.showMessageDialog(null,
	// * "Invalid Vehicle Go to Registration"); } else if
	// (Utils.isNull(vehicle_name)) { JOptionPane.showMessageDialog(null,
	// "Invalid Vehicle Go to Registration"); }
	// */
	// JOptionPane.showMessageDialog(null,
	// "Invalid Vehicle Go to Registration");
	// isTpRecordValid = false;
	// isVehicleExist = false;
	// }
	// } catch (Exception ex) {
	// ex.printStackTrace();
	// }
	// }
	private void getFocus() {
		if (vehicleName.isEditable()) {
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
	// <editor-fold defaultstate="collapsed" desc="Generated Code">
	private void initComponents() {

		jPanel1 = new javax.swing.JPanel();
		jPanel3 = new javax.swing.JPanel();
		jLabel1 = new javax.swing.JLabel();
		jLabel7 = new javax.swing.JLabel();
		jLabel2 = new javax.swing.JLabel();
		username = new javax.swing.JLabel();
		button1 = new java.awt.Button();
		jPanel4 = new javax.swing.JPanel();
		jLabel4 = new javax.swing.JLabel();
		jLabel84 = new javax.swing.JLabel();
		transporter = new javax.swing.JComboBox();
		paperValid = new javax.swing.JLabel();
		paperValid.setFont(UIConstant.textFont);
		paperValid.setForeground(UIConstant.textFontColor);
		jLabel15 = new javax.swing.JLabel();
		jLabel11 = new javax.swing.JLabel();
		gpsTime = new javax.swing.JLabel();
		gpsLocation = new javax.swing.JLabel();
		jPanel2 = new javax.swing.JPanel();
		jLabel85 = new javax.swing.JLabel();
		jLabel87 = new javax.swing.JLabel();
		jLabel86 = new javax.swing.JLabel();
		jLabel89 = new javax.swing.JLabel();
		jLabel97 = new javax.swing.JLabel();
		sideMirrorPanel = new javax.swing.JPanel();
		sideMirrorNo = new javax.swing.JCheckBox();
		sideMirrorYes = new javax.swing.JCheckBox();
		sideMirrorNC = new javax.swing.JCheckBox();
		reverseHornPanel = new javax.swing.JPanel();
		reverseHornYes = new javax.swing.JCheckBox();
		reverseHornNo = new javax.swing.JCheckBox();
		reverseHornNC = new javax.swing.JCheckBox();
		tailLightPanel = new javax.swing.JPanel();
		tailLightNo = new javax.swing.JCheckBox();
		tailLightYes = new javax.swing.JCheckBox();
		tailLightNC = new javax.swing.JCheckBox();
		numberVisiblePanel = new javax.swing.JPanel();
		numberVisibleNo = new javax.swing.JCheckBox();
		numberVisibleYes = new javax.swing.JCheckBox();
		numberVisibleNC = new javax.swing.JCheckBox();
		leftSideIndicatorPanel = new javax.swing.JPanel();
		leftSideIndicatorNo = new javax.swing.JCheckBox();
		leftSideIndicatorYes = new javax.swing.JCheckBox();
		leftSideIndicatorNC = new javax.swing.JCheckBox();
		seatBeltPanel = new javax.swing.JPanel();
		seatBeltNo = new javax.swing.JCheckBox();
		seatBeltYes = new javax.swing.JCheckBox();
		seatBeltNC = new javax.swing.JCheckBox();
		jLabel6 = new javax.swing.JLabel();
		jLabel8 = new javax.swing.JLabel();
		jPanel5 = new javax.swing.JPanel();
		jPanel6 = new javax.swing.JPanel();
		jLabel88 = new javax.swing.JLabel();
		headLightPanel = new javax.swing.JPanel();
		headLightNo = new javax.swing.JCheckBox();
		headLightYes = new javax.swing.JCheckBox();
		headLightNC = new javax.swing.JCheckBox();
		jLabel9 = new javax.swing.JLabel();
		jLabel10 = new javax.swing.JLabel();
		rightSideIndicatorPanel = new javax.swing.JPanel();
		rightSideIndicatorYes = new javax.swing.JCheckBox();
		rightSideIndicatorNo = new javax.swing.JCheckBox();
		rightSideIndicatorNC = new javax.swing.JCheckBox();
		tarpaulinPanel = new javax.swing.JPanel();
		tarpaulinNo = new javax.swing.JCheckBox();
		tarpaulinYes = new javax.swing.JCheckBox();
		tarpaulinNC = new javax.swing.JCheckBox();
		sealOkPanel = new javax.swing.JPanel();
		sealYes = new javax.swing.JCheckBox();
		sealNo = new javax.swing.JCheckBox();
		sealNC = new javax.swing.JCheckBox();
		jPanel7 = new javax.swing.JPanel();
		jLabel23 = new javax.swing.JLabel();
		jLabel24 = new javax.swing.JLabel();
		jLabel25 = new javax.swing.JLabel();
		jLabel26 = new javax.swing.JLabel();
		filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 20),
				new java.awt.Dimension(0, 20),
				new java.awt.Dimension(32767, 20));
		breathlizerOkPanel = new javax.swing.JPanel();
		breathlizerNo = new javax.swing.JCheckBox();
		breathlizerYes = new javax.swing.JCheckBox();
		breathlizerNC = new javax.swing.JCheckBox();
		HelperPanel = new javax.swing.JPanel();
		helperNo = new javax.swing.JCheckBox();
		helperYes = new javax.swing.JCheckBox();
		dlNo = new javax.swing.JTextField();
		driverName = new javax.swing.JTextField();
		jLabel27 = new javax.swing.JLabel();
		driverId = new javax.swing.JTextField();
		clearButton = new javax.swing.JButton();
		manualEntryButton = new javax.swing.JButton();
		saveButton = new javax.swing.JButton();
		photo = new javax.swing.JLabel();
		jPanel8 = new javax.swing.JPanel();
		jLabel3 = new javax.swing.JLabel();
		jLabel5 = new javax.swing.JLabel();
		overrides = new javax.swing.JTextField();
		captureButton = new javax.swing.JButton();
		fingerInstruction = new javax.swing.JLabel();
		vehicleName = new javax.swing.JComboBox();
		jLabel12 = new javax.swing.JLabel();
		driverHours = new javax.swing.JLabel();
		filler2 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0),
				new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 0));
		filler3 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0),
				new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 0));
		blocking_reason = new javax.swing.JLabel();
		digitalClock = new javax.swing.JLabel();
		fingerPrintPanel = new java.awt.Panel();
		lblScore = new javax.swing.JLabel();
		lblSteps = new javax.swing.JLabel();
		lblInstruction = new javax.swing.JLabel();
		lblCurrentImageInfo = new javax.swing.JLabel();
		progressBar = new javax.swing.JProgressBar();
		progressBar.setOrientation(SwingConstants.VERTICAL);
		progressBar.setMaximum(150);
		manualSync = new javax.swing.JButton();

		setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

		jPanel1.setBackground(new java.awt.Color(255, 255, 255));

		jPanel3.setBackground(new java.awt.Color(255, 255, 255));

		jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource(
				"/com/ipssi/rfid/ui/intelliplanner.png"))); // NOI18N

		jLabel7.setFont(UIConstant.subHeadingFont);
		jLabel7.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
		jLabel7.setText("Gate In:Controlled Fly-ash Vehicle Gate Entry");

		jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource(
				"/com/ipssi/rfid/ui/cover_01_right_top.gif"))); // NOI18N

		username.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);

		button1.setBackground(new java.awt.Color(255, 255, 255));
		button1.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
		button1.setForeground(new java.awt.Color(0, 102, 153));
		button1.setLabel("Sign Out");
		button1.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				button1ActionPerformed(evt);
			}
		});

		javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(
				jPanel3);
		jPanel3.setLayout(jPanel3Layout);
		jPanel3Layout
				.setHorizontalGroup(jPanel3Layout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								jPanel3Layout
										.createSequentialGroup()
										.addComponent(
												jLabel1,
												javax.swing.GroupLayout.PREFERRED_SIZE,
												232,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addGap(133, 133, 133)
										.addComponent(
												jLabel7,
												javax.swing.GroupLayout.PREFERRED_SIZE,
												495,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(
												username,
												javax.swing.GroupLayout.PREFERRED_SIZE,
												258,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(
												button1,
												javax.swing.GroupLayout.PREFERRED_SIZE,
												83,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(
												jLabel2,
												javax.swing.GroupLayout.PREFERRED_SIZE,
												273,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addContainerGap()));
		jPanel3Layout
				.setVerticalGroup(jPanel3Layout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addComponent(jLabel1,
								javax.swing.GroupLayout.DEFAULT_SIZE,
								javax.swing.GroupLayout.DEFAULT_SIZE,
								Short.MAX_VALUE)
						.addComponent(jLabel2,
								javax.swing.GroupLayout.DEFAULT_SIZE,
								javax.swing.GroupLayout.DEFAULT_SIZE,
								Short.MAX_VALUE)
						.addGroup(
								jPanel3Layout
										.createSequentialGroup()
										.addGroup(
												jPanel3Layout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.LEADING)
														.addGroup(
																javax.swing.GroupLayout.Alignment.TRAILING,
																jPanel3Layout
																		.createSequentialGroup()
																		.addContainerGap()
																		.addGroup(
																				jPanel3Layout
																						.createParallelGroup(
																								javax.swing.GroupLayout.Alignment.TRAILING)
																						.addComponent(
																								username,
																								javax.swing.GroupLayout.DEFAULT_SIZE,
																								javax.swing.GroupLayout.DEFAULT_SIZE,
																								Short.MAX_VALUE)
																						.addComponent(
																								button1,
																								javax.swing.GroupLayout.DEFAULT_SIZE,
																								33,
																								Short.MAX_VALUE)))
														.addComponent(
																jLabel7,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																Short.MAX_VALUE))
										.addContainerGap()));

		jPanel4.setBackground(new java.awt.Color(255, 255, 255));

		jLabel4.setFont(UIConstant.labelFont);
		jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
		jLabel4.setText("Transporter:");

		jLabel84.setFont(UIConstant.labelFont);
		jLabel84.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
		jLabel84.setText("Paper Validity:");

		transporter.addItem(new ComboItem(0, "Select"));
		transporter.setFont(UIConstant.textFont);
		transporter.setBorder(javax.swing.BorderFactory.createLineBorder(
				new java.awt.Color(0, 0, 0), 0));
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

		jLabel15.setFont(UIConstant.labelFont);
		jLabel15.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
		jLabel15.setText("GPS Location:");

		jLabel11.setFont(UIConstant.labelFont);
		jLabel11.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
		jLabel11.setText("GPS Time:");

		gpsTime.setFont(UIConstant.textFont);
		gpsTime.setForeground(UIConstant.textFontColor);

		gpsLocation.setFont(UIConstant.textFont); // NOI18N

		gpsLocation.setForeground(UIConstant.textFontColor);

		javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(
				jPanel4);
		jPanel4.setLayout(jPanel4Layout);
		jPanel4Layout
				.setHorizontalGroup(jPanel4Layout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								jPanel4Layout
										.createSequentialGroup()
										.addContainerGap()
										.addGroup(
												jPanel4Layout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.LEADING)
														.addComponent(
																jLabel84,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																126,
																Short.MAX_VALUE)
														.addComponent(
																jLabel4,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																Short.MAX_VALUE))
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addGroup(
												jPanel4Layout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.LEADING)
														.addComponent(
																transporter,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																219,
																javax.swing.GroupLayout.PREFERRED_SIZE)
														.addComponent(
																paperValid,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																219,
																javax.swing.GroupLayout.PREFERRED_SIZE))
										.addGap(110, 110, 110)
										.addGroup(
												jPanel4Layout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.LEADING)
														.addGroup(
																jPanel4Layout
																		.createSequentialGroup()
																		.addComponent(
																				jLabel11,
																				javax.swing.GroupLayout.PREFERRED_SIZE,
																				153,
																				javax.swing.GroupLayout.PREFERRED_SIZE)
																		.addPreferredGap(
																				javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																		.addComponent(
																				gpsTime,
																				javax.swing.GroupLayout.PREFERRED_SIZE,
																				258,
																				javax.swing.GroupLayout.PREFERRED_SIZE))
														.addGroup(
																jPanel4Layout
																		.createSequentialGroup()
																		.addComponent(
																				jLabel15,
																				javax.swing.GroupLayout.PREFERRED_SIZE,
																				152,
																				javax.swing.GroupLayout.PREFERRED_SIZE)
																		.addPreferredGap(
																				javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																		.addComponent(
																				gpsLocation,
																				javax.swing.GroupLayout.PREFERRED_SIZE,
																				459,
																				javax.swing.GroupLayout.PREFERRED_SIZE)))
										.addGap(27, 27, 27)));
		jPanel4Layout
				.setVerticalGroup(jPanel4Layout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								jPanel4Layout
										.createSequentialGroup()
										.addGap(3, 3, 3)
										.addGroup(
												jPanel4Layout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.LEADING)
														.addGroup(
																jPanel4Layout
																		.createSequentialGroup()
																		.addGroup(
																				jPanel4Layout
																						.createParallelGroup(
																								javax.swing.GroupLayout.Alignment.LEADING)
																						.addComponent(
																								jLabel4,
																								javax.swing.GroupLayout.PREFERRED_SIZE,
																								30,
																								javax.swing.GroupLayout.PREFERRED_SIZE)
																						.addGroup(
																								javax.swing.GroupLayout.Alignment.TRAILING,
																								jPanel4Layout
																										.createParallelGroup(
																												javax.swing.GroupLayout.Alignment.BASELINE)
																										.addComponent(
																												transporter,
																												javax.swing.GroupLayout.PREFERRED_SIZE,
																												30,
																												javax.swing.GroupLayout.PREFERRED_SIZE)
																										.addComponent(
																												jLabel15,
																												javax.swing.GroupLayout.PREFERRED_SIZE,
																												30,
																												javax.swing.GroupLayout.PREFERRED_SIZE)))
																		.addGap(
																				0,
																				0,
																				Short.MAX_VALUE))
														.addComponent(
																gpsLocation,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																Short.MAX_VALUE))
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addGroup(
												jPanel4Layout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.LEADING)
														.addComponent(
																jLabel84,
																javax.swing.GroupLayout.Alignment.TRAILING,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																30,
																javax.swing.GroupLayout.PREFERRED_SIZE)
														.addComponent(
																paperValid,
																javax.swing.GroupLayout.Alignment.TRAILING,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																30,
																javax.swing.GroupLayout.PREFERRED_SIZE)
														.addComponent(
																jLabel11,
																javax.swing.GroupLayout.Alignment.TRAILING,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																30,
																javax.swing.GroupLayout.PREFERRED_SIZE)
														.addComponent(
																gpsTime,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																30,
																javax.swing.GroupLayout.PREFERRED_SIZE))
										.addContainerGap()));

		jPanel2.setBackground(new java.awt.Color(255, 255, 255));

		jLabel85.setFont(UIConstant.labelFont);
		jLabel85.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
		jLabel85.setText("Reverse Horn?:");

		jLabel87.setFont(UIConstant.labelFont);
		jLabel87.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
		jLabel87.setText("Tail Light?:");

		jLabel86.setFont(UIConstant.labelFont);
		jLabel86.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
		jLabel86.setText("Seat Belt Worn?:");

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

		javax.swing.GroupLayout sideMirrorPanelLayout = new javax.swing.GroupLayout(
				sideMirrorPanel);
		sideMirrorPanel.setLayout(sideMirrorPanelLayout);
		sideMirrorPanelLayout
				.setHorizontalGroup(sideMirrorPanelLayout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								javax.swing.GroupLayout.Alignment.TRAILING,
								sideMirrorPanelLayout
										.createSequentialGroup()
										.addComponent(
												sideMirrorYes,
												javax.swing.GroupLayout.PREFERRED_SIZE,
												47,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(
												sideMirrorNo,
												javax.swing.GroupLayout.PREFERRED_SIZE,
												45,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(
												sideMirrorNC,
												javax.swing.GroupLayout.PREFERRED_SIZE,
												53,
												javax.swing.GroupLayout.PREFERRED_SIZE)));
		sideMirrorPanelLayout
				.setVerticalGroup(sideMirrorPanelLayout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								sideMirrorPanelLayout
										.createSequentialGroup()
										.addGroup(
												sideMirrorPanelLayout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.BASELINE)
														.addComponent(
																sideMirrorNo,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																30,
																javax.swing.GroupLayout.PREFERRED_SIZE)
														.addComponent(
																sideMirrorYes,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																30,
																javax.swing.GroupLayout.PREFERRED_SIZE)
														.addComponent(
																sideMirrorNC,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																30,
																javax.swing.GroupLayout.PREFERRED_SIZE))
										.addGap(0, 0, Short.MAX_VALUE)));

		reverseHornPanel.setBackground(java.awt.SystemColor.controlLtHighlight);
		reverseHornPanel.setName("reverseHornPanel"); // NOI18N
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

		reverseHornYes.setBackground(new java.awt.Color(255, 255, 255));
		reverseHornYes.setText("Yes");
		reverseHornYes.setFocusPainted(false);
		reverseHornYes.setFocusable(false);
		reverseHornYes.setOpaque(false);
		reverseHornYes.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				reverseHornYesActionPerformed(evt);
			}
		});
		reverseHornYes.addKeyListener(new java.awt.event.KeyAdapter() {
			public void keyPressed(java.awt.event.KeyEvent evt) {
				reverseHornYesKeyPressed(evt);
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
		reverseHornNo.addKeyListener(new java.awt.event.KeyAdapter() {
			public void keyPressed(java.awt.event.KeyEvent evt) {
				reverseHornNoKeyPressed(evt);
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
		reverseHornNC.addKeyListener(new java.awt.event.KeyAdapter() {
			public void keyPressed(java.awt.event.KeyEvent evt) {
				reverseHornNCKeyPressed(evt);
			}
		});

		javax.swing.GroupLayout reverseHornPanelLayout = new javax.swing.GroupLayout(
				reverseHornPanel);
		reverseHornPanel.setLayout(reverseHornPanelLayout);
		reverseHornPanelLayout
				.setHorizontalGroup(reverseHornPanelLayout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								reverseHornPanelLayout
										.createSequentialGroup()
										.addComponent(
												reverseHornYes,
												javax.swing.GroupLayout.PREFERRED_SIZE,
												47,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(
												reverseHornNo,
												javax.swing.GroupLayout.PREFERRED_SIZE,
												44,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(reverseHornNC)
										.addContainerGap(15, Short.MAX_VALUE)));
		reverseHornPanelLayout
				.setVerticalGroup(reverseHornPanelLayout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								reverseHornPanelLayout
										.createParallelGroup(
												javax.swing.GroupLayout.Alignment.BASELINE)
										.addComponent(
												reverseHornYes,
												javax.swing.GroupLayout.PREFERRED_SIZE,
												30,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addComponent(
												reverseHornNo,
												javax.swing.GroupLayout.PREFERRED_SIZE,
												30,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addComponent(
												reverseHornNC,
												javax.swing.GroupLayout.PREFERRED_SIZE,
												30,
												javax.swing.GroupLayout.PREFERRED_SIZE)));

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

		javax.swing.GroupLayout tailLightPanelLayout = new javax.swing.GroupLayout(
				tailLightPanel);
		tailLightPanel.setLayout(tailLightPanelLayout);
		tailLightPanelLayout
				.setHorizontalGroup(tailLightPanelLayout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								javax.swing.GroupLayout.Alignment.TRAILING,
								tailLightPanelLayout
										.createSequentialGroup()
										.addComponent(
												tailLightYes,
												javax.swing.GroupLayout.PREFERRED_SIZE,
												47,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(
												tailLightNo,
												javax.swing.GroupLayout.PREFERRED_SIZE,
												44,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.RELATED,
												javax.swing.GroupLayout.DEFAULT_SIZE,
												Short.MAX_VALUE).addComponent(
												tailLightNC).addGap(16, 16, 16)));
		tailLightPanelLayout
				.setVerticalGroup(tailLightPanelLayout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								javax.swing.GroupLayout.Alignment.TRAILING,
								tailLightPanelLayout
										.createSequentialGroup()
										.addGap(0, 0, Short.MAX_VALUE)
										.addGroup(
												tailLightPanelLayout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.LEADING,
																false)
														.addComponent(
																tailLightNo,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																30,
																Short.MAX_VALUE)
														.addComponent(
																tailLightNC,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																Short.MAX_VALUE)
														.addComponent(
																tailLightYes,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																Short.MAX_VALUE))));

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

		javax.swing.GroupLayout numberVisiblePanelLayout = new javax.swing.GroupLayout(
				numberVisiblePanel);
		numberVisiblePanel.setLayout(numberVisiblePanelLayout);
		numberVisiblePanelLayout
				.setHorizontalGroup(numberVisiblePanelLayout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								javax.swing.GroupLayout.Alignment.TRAILING,
								numberVisiblePanelLayout
										.createSequentialGroup()
										.addComponent(
												numberVisibleYes,
												javax.swing.GroupLayout.PREFERRED_SIZE,
												47,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(
												numberVisibleNo,
												javax.swing.GroupLayout.PREFERRED_SIZE,
												44,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(
												numberVisibleNC,
												javax.swing.GroupLayout.DEFAULT_SIZE,
												javax.swing.GroupLayout.DEFAULT_SIZE,
												Short.MAX_VALUE)));
		numberVisiblePanelLayout
				.setVerticalGroup(numberVisiblePanelLayout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								numberVisiblePanelLayout
										.createSequentialGroup()
										.addGroup(
												numberVisiblePanelLayout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.BASELINE)
														.addComponent(
																numberVisibleNo,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																30,
																javax.swing.GroupLayout.PREFERRED_SIZE)
														.addComponent(
																numberVisibleYes,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																30,
																javax.swing.GroupLayout.PREFERRED_SIZE))
										.addGap(0, 0, Short.MAX_VALUE))
						.addComponent(numberVisibleNC,
								javax.swing.GroupLayout.DEFAULT_SIZE,
								javax.swing.GroupLayout.DEFAULT_SIZE,
								Short.MAX_VALUE));

		leftSideIndicatorPanel.setBackground(new java.awt.Color(255, 255, 255));
		leftSideIndicatorPanel.setName("numberVisiblePanel"); // NOI18N
		leftSideIndicatorPanel.setPreferredSize(new java.awt.Dimension(94, 30));
		leftSideIndicatorPanel
				.addMouseListener(new java.awt.event.MouseAdapter() {
					public void mouseClicked(java.awt.event.MouseEvent evt) {
						leftSideIndicatorPanelMouseClicked(evt);
					}
				});
		leftSideIndicatorPanel
				.addFocusListener(new java.awt.event.FocusAdapter() {
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
		leftSideIndicatorNo
				.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent evt) {
						leftSideIndicatorNoActionPerformed(evt);
					}
				});

		leftSideIndicatorYes.setBackground(new java.awt.Color(255, 255, 255));
		leftSideIndicatorYes.setText("Yes");
		leftSideIndicatorYes.setFocusable(false);
		leftSideIndicatorYes.setOpaque(false);
		leftSideIndicatorYes
				.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent evt) {
						leftSideIndicatorYesActionPerformed(evt);
					}
				});

		leftSideIndicatorNC.setBackground(new java.awt.Color(255, 255, 255));
		leftSideIndicatorNC.setText("NC");
		leftSideIndicatorNC.setFocusable(false);
		leftSideIndicatorNC.setOpaque(false);
		leftSideIndicatorNC
				.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent evt) {
						leftSideIndicatorNCActionPerformed(evt);
					}
				});

		javax.swing.GroupLayout leftSideIndicatorPanelLayout = new javax.swing.GroupLayout(
				leftSideIndicatorPanel);
		leftSideIndicatorPanel.setLayout(leftSideIndicatorPanelLayout);
		leftSideIndicatorPanelLayout
				.setHorizontalGroup(leftSideIndicatorPanelLayout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								javax.swing.GroupLayout.Alignment.TRAILING,
								leftSideIndicatorPanelLayout
										.createSequentialGroup()
										.addComponent(
												leftSideIndicatorYes,
												javax.swing.GroupLayout.PREFERRED_SIZE,
												47,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(
												leftSideIndicatorNo,
												javax.swing.GroupLayout.PREFERRED_SIZE,
												44,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(
												leftSideIndicatorNC,
												javax.swing.GroupLayout.DEFAULT_SIZE,
												47, Short.MAX_VALUE)
										.addContainerGap()));
		leftSideIndicatorPanelLayout
				.setVerticalGroup(leftSideIndicatorPanelLayout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addComponent(leftSideIndicatorNC,
								javax.swing.GroupLayout.Alignment.TRAILING,
								javax.swing.GroupLayout.DEFAULT_SIZE, 30,
								Short.MAX_VALUE).addComponent(
								leftSideIndicatorYes,
								javax.swing.GroupLayout.DEFAULT_SIZE,
								javax.swing.GroupLayout.DEFAULT_SIZE,
								Short.MAX_VALUE).addComponent(
								leftSideIndicatorNo,
								javax.swing.GroupLayout.Alignment.TRAILING,
								javax.swing.GroupLayout.DEFAULT_SIZE,
								javax.swing.GroupLayout.DEFAULT_SIZE,
								Short.MAX_VALUE));

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

		seatBeltNC.setBackground(new java.awt.Color(255, 255, 255));
		seatBeltNC.setText("NC");
		seatBeltNC.setFocusable(false);
		seatBeltNC.setOpaque(false);
		seatBeltNC.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				seatBeltNCActionPerformed(evt);
			}
		});

		javax.swing.GroupLayout seatBeltPanelLayout = new javax.swing.GroupLayout(
				seatBeltPanel);
		seatBeltPanel.setLayout(seatBeltPanelLayout);
		seatBeltPanelLayout
				.setHorizontalGroup(seatBeltPanelLayout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								javax.swing.GroupLayout.Alignment.TRAILING,
								seatBeltPanelLayout
										.createSequentialGroup()
										.addComponent(
												seatBeltYes,
												javax.swing.GroupLayout.PREFERRED_SIZE,
												47,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(
												seatBeltNo,
												javax.swing.GroupLayout.PREFERRED_SIZE,
												44,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(
												seatBeltNC,
												javax.swing.GroupLayout.DEFAULT_SIZE,
												47, Short.MAX_VALUE)
										.addContainerGap()));
		seatBeltPanelLayout.setVerticalGroup(seatBeltPanelLayout
				.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(
						seatBeltPanelLayout.createParallelGroup(
								javax.swing.GroupLayout.Alignment.BASELINE)
								.addComponent(seatBeltNo,
										javax.swing.GroupLayout.PREFERRED_SIZE,
										30,
										javax.swing.GroupLayout.PREFERRED_SIZE)
								.addComponent(seatBeltYes,
										javax.swing.GroupLayout.PREFERRED_SIZE,
										30,
										javax.swing.GroupLayout.PREFERRED_SIZE)
								.addComponent(seatBeltNC,
										javax.swing.GroupLayout.DEFAULT_SIZE,
										javax.swing.GroupLayout.DEFAULT_SIZE,
										Short.MAX_VALUE)));

		jLabel6.setFont(UIConstant.labelFont);
		jLabel6.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
		jLabel6.setText("Tarpaulin Ok:");

		jLabel8.setFont(UIConstant.labelFont);
		jLabel8.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
		jLabel8.setText("Seal Ok:");

		jPanel5.setBackground(new java.awt.Color(255, 255, 255));

		javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(
				jPanel5);
		jPanel5.setLayout(jPanel5Layout);
		jPanel5Layout.setHorizontalGroup(jPanel5Layout.createParallelGroup(
				javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 6,
				Short.MAX_VALUE));
		jPanel5Layout.setVerticalGroup(jPanel5Layout.createParallelGroup(
				javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 0,
				Short.MAX_VALUE));

		jPanel6.setBackground(new java.awt.Color(255, 255, 255));

		javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(
				jPanel6);
		jPanel6.setLayout(jPanel6Layout);
		jPanel6Layout.setHorizontalGroup(jPanel6Layout.createParallelGroup(
				javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 0,
				Short.MAX_VALUE));
		jPanel6Layout.setVerticalGroup(jPanel6Layout.createParallelGroup(
				javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 0,
				Short.MAX_VALUE));

		jLabel88.setFont(UIConstant.labelFont);
		jLabel88.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
		jLabel88.setText("Head light?:");

		headLightPanel.setBackground(new java.awt.Color(255, 255, 255));
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
		headLightNo.addKeyListener(new java.awt.event.KeyAdapter() {
			public void keyPressed(java.awt.event.KeyEvent evt) {
				headLightNoKeyPressed(evt);
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
		headLightYes.addKeyListener(new java.awt.event.KeyAdapter() {
			public void keyPressed(java.awt.event.KeyEvent evt) {
				headLightYesKeyPressed(evt);
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

		javax.swing.GroupLayout headLightPanelLayout = new javax.swing.GroupLayout(
				headLightPanel);
		headLightPanel.setLayout(headLightPanelLayout);
		headLightPanelLayout
				.setHorizontalGroup(headLightPanelLayout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								javax.swing.GroupLayout.Alignment.TRAILING,
								headLightPanelLayout
										.createSequentialGroup()
										.addComponent(
												headLightYes,
												javax.swing.GroupLayout.PREFERRED_SIZE,
												47,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(
												headLightNo,
												javax.swing.GroupLayout.PREFERRED_SIZE,
												44,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(headLightNC).addGap(25,
												25, 25)));
		headLightPanelLayout
				.setVerticalGroup(headLightPanelLayout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								headLightPanelLayout
										.createParallelGroup(
												javax.swing.GroupLayout.Alignment.BASELINE)
										.addComponent(
												headLightNo,
												javax.swing.GroupLayout.PREFERRED_SIZE,
												30,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addComponent(
												headLightYes,
												javax.swing.GroupLayout.PREFERRED_SIZE,
												30,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addComponent(
												headLightNC,
												javax.swing.GroupLayout.PREFERRED_SIZE,
												30,
												javax.swing.GroupLayout.PREFERRED_SIZE)));

		jLabel9.setBackground(new java.awt.Color(255, 255, 255));
		jLabel9.setFont(UIConstant.labelFont);
		jLabel9.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
		jLabel9.setText("Left Indicator?:");

		jLabel10.setBackground(new java.awt.Color(255, 255, 255));
		jLabel10.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
		jLabel10.setFont(UIConstant.labelFont);
		jLabel10.setText("Right Indicator?:");

		rightSideIndicatorPanel
				.setBackground(new java.awt.Color(255, 255, 255));
		rightSideIndicatorPanel
				.addMouseListener(new java.awt.event.MouseAdapter() {
					public void mouseClicked(java.awt.event.MouseEvent evt) {
						rightSideIndicatorPanelMouseClicked(evt);
					}
				});
		rightSideIndicatorPanel
				.addFocusListener(new java.awt.event.FocusAdapter() {
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
		rightSideIndicatorYes
				.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent evt) {
						rightSideIndicatorYesActionPerformed(evt);
					}
				});

		rightSideIndicatorNo.setBackground(new java.awt.Color(255, 255, 255));
		rightSideIndicatorNo.setText("No");
		rightSideIndicatorNo.setOpaque(false);
		rightSideIndicatorNo.setFocusable(false);
		rightSideIndicatorNo
				.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent evt) {
						rightSideIndicatorNoActionPerformed(evt);
					}
				});

		rightSideIndicatorNC.setBackground(new java.awt.Color(255, 255, 255));
		rightSideIndicatorNC.setText("NC");
		rightSideIndicatorNC.setOpaque(false);
		rightSideIndicatorNC.setFocusable(false);
		rightSideIndicatorNC
				.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent evt) {
						rightSideIndicatorNCActionPerformed(evt);
					}
				});

		javax.swing.GroupLayout rightSideIndicatorPanelLayout = new javax.swing.GroupLayout(
				rightSideIndicatorPanel);
		rightSideIndicatorPanel.setLayout(rightSideIndicatorPanelLayout);
		rightSideIndicatorPanelLayout
				.setHorizontalGroup(rightSideIndicatorPanelLayout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								rightSideIndicatorPanelLayout
										.createSequentialGroup()
										.addComponent(
												rightSideIndicatorYes,
												javax.swing.GroupLayout.PREFERRED_SIZE,
												47,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(
												rightSideIndicatorNo,
												javax.swing.GroupLayout.PREFERRED_SIZE,
												44,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(
												rightSideIndicatorNC,
												javax.swing.GroupLayout.DEFAULT_SIZE,
												javax.swing.GroupLayout.DEFAULT_SIZE,
												Short.MAX_VALUE)
										.addContainerGap()));
		rightSideIndicatorPanelLayout
				.setVerticalGroup(rightSideIndicatorPanelLayout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								rightSideIndicatorPanelLayout
										.createParallelGroup(
												javax.swing.GroupLayout.Alignment.BASELINE)
										.addComponent(
												rightSideIndicatorYes,
												javax.swing.GroupLayout.PREFERRED_SIZE,
												30,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addComponent(
												rightSideIndicatorNo,
												javax.swing.GroupLayout.PREFERRED_SIZE,
												30,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addComponent(
												rightSideIndicatorNC,
												javax.swing.GroupLayout.PREFERRED_SIZE,
												30,
												javax.swing.GroupLayout.PREFERRED_SIZE)));

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

		javax.swing.GroupLayout tarpaulinPanelLayout = new javax.swing.GroupLayout(
				tarpaulinPanel);
		tarpaulinPanel.setLayout(tarpaulinPanelLayout);
		tarpaulinPanelLayout
				.setHorizontalGroup(tarpaulinPanelLayout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								javax.swing.GroupLayout.Alignment.TRAILING,
								tarpaulinPanelLayout
										.createSequentialGroup()
										.addComponent(
												tarpaulinYes,
												javax.swing.GroupLayout.PREFERRED_SIZE,
												47,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(
												tarpaulinNo,
												javax.swing.GroupLayout.PREFERRED_SIZE,
												44,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(
												tarpaulinNC,
												javax.swing.GroupLayout.DEFAULT_SIZE,
												javax.swing.GroupLayout.DEFAULT_SIZE,
												Short.MAX_VALUE)));
		tarpaulinPanelLayout.setVerticalGroup(tarpaulinPanelLayout
				.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(
						tarpaulinPanelLayout.createParallelGroup(
								javax.swing.GroupLayout.Alignment.BASELINE)
								.addComponent(tarpaulinNo,
										javax.swing.GroupLayout.PREFERRED_SIZE,
										30,
										javax.swing.GroupLayout.PREFERRED_SIZE)
								.addComponent(tarpaulinYes,
										javax.swing.GroupLayout.PREFERRED_SIZE,
										30,
										javax.swing.GroupLayout.PREFERRED_SIZE)
								.addComponent(tarpaulinNC)));

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

		javax.swing.GroupLayout sealOkPanelLayout = new javax.swing.GroupLayout(
				sealOkPanel);
		sealOkPanel.setLayout(sealOkPanelLayout);
		sealOkPanelLayout
				.setHorizontalGroup(sealOkPanelLayout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								sealOkPanelLayout
										.createSequentialGroup()
										.addComponent(
												sealYes,
												javax.swing.GroupLayout.PREFERRED_SIZE,
												47,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(
												sealNo,
												javax.swing.GroupLayout.PREFERRED_SIZE,
												44,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(
												sealNC,
												javax.swing.GroupLayout.DEFAULT_SIZE,
												57, Short.MAX_VALUE)));
		sealOkPanelLayout
				.setVerticalGroup(sealOkPanelLayout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								sealOkPanelLayout
										.createParallelGroup(
												javax.swing.GroupLayout.Alignment.BASELINE)
										.addComponent(
												sealYes,
												javax.swing.GroupLayout.PREFERRED_SIZE,
												30,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addComponent(
												sealNo,
												javax.swing.GroupLayout.PREFERRED_SIZE,
												30,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addComponent(
												sealNC,
												javax.swing.GroupLayout.PREFERRED_SIZE,
												30,
												javax.swing.GroupLayout.PREFERRED_SIZE)));

		javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(
				jPanel2);
		jPanel2.setLayout(jPanel2Layout);
		jPanel2Layout
				.setHorizontalGroup(jPanel2Layout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								jPanel2Layout
										.createSequentialGroup()
										.addGap(10, 10, 10)
										.addGroup(
												jPanel2Layout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.LEADING,
																false)
														.addComponent(
																jLabel6,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																Short.MAX_VALUE)
														.addComponent(
																jLabel8,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																Short.MAX_VALUE)
														.addComponent(
																jLabel86,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																130,
																Short.MAX_VALUE))
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addGroup(
												jPanel2Layout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.LEADING)
														.addGroup(
																jPanel2Layout
																		.createSequentialGroup()
																		.addGroup(
																				jPanel2Layout
																						.createParallelGroup(
																								javax.swing.GroupLayout.Alignment.LEADING)
																						.addComponent(
																								sealOkPanel,
																								javax.swing.GroupLayout.DEFAULT_SIZE,
																								javax.swing.GroupLayout.DEFAULT_SIZE,
																								Short.MAX_VALUE)
																						.addComponent(
																								tarpaulinPanel,
																								javax.swing.GroupLayout.DEFAULT_SIZE,
																								javax.swing.GroupLayout.DEFAULT_SIZE,
																								Short.MAX_VALUE))
																		.addPreferredGap(
																				javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																		.addComponent(
																				jPanel5,
																				javax.swing.GroupLayout.PREFERRED_SIZE,
																				javax.swing.GroupLayout.DEFAULT_SIZE,
																				javax.swing.GroupLayout.PREFERRED_SIZE)
																		.addPreferredGap(
																				javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																		.addComponent(
																				jPanel6,
																				javax.swing.GroupLayout.PREFERRED_SIZE,
																				javax.swing.GroupLayout.DEFAULT_SIZE,
																				javax.swing.GroupLayout.PREFERRED_SIZE))
														.addComponent(
																seatBeltPanel,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE))
										.addGap(31, 31, 31)
										.addGroup(
												jPanel2Layout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.LEADING,
																false)
														.addComponent(
																jLabel97,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																140,
																Short.MAX_VALUE)
														.addComponent(
																jLabel89,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																Short.MAX_VALUE))
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addGroup(
												jPanel2Layout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.LEADING,
																false)
														.addComponent(
																numberVisiblePanel,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																145,
																Short.MAX_VALUE)
														.addComponent(
																sideMirrorPanel,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																Short.MAX_VALUE))
										.addGap(12, 12, 12)
										.addGroup(
												jPanel2Layout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.TRAILING,
																false)
														.addComponent(
																jLabel85,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																160,
																Short.MAX_VALUE)
														.addComponent(
																jLabel87,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																Short.MAX_VALUE)
														.addComponent(
																jLabel88,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																Short.MAX_VALUE))
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addGroup(
												jPanel2Layout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.LEADING)
														.addComponent(
																headLightPanel,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																145,
																javax.swing.GroupLayout.PREFERRED_SIZE)
														.addGroup(
																jPanel2Layout
																		.createSequentialGroup()
																		.addGroup(
																				jPanel2Layout
																						.createParallelGroup(
																								javax.swing.GroupLayout.Alignment.LEADING)
																						.addComponent(
																								reverseHornPanel,
																								javax.swing.GroupLayout.PREFERRED_SIZE,
																								javax.swing.GroupLayout.DEFAULT_SIZE,
																								javax.swing.GroupLayout.PREFERRED_SIZE)
																						.addComponent(
																								tailLightPanel,
																								javax.swing.GroupLayout.PREFERRED_SIZE,
																								145,
																								javax.swing.GroupLayout.PREFERRED_SIZE))
																		.addGap(
																				40,
																				40,
																				40)
																		.addGroup(
																				jPanel2Layout
																						.createParallelGroup(
																								javax.swing.GroupLayout.Alignment.LEADING,
																								false)
																						.addComponent(
																								jLabel9,
																								javax.swing.GroupLayout.DEFAULT_SIZE,
																								javax.swing.GroupLayout.DEFAULT_SIZE,
																								Short.MAX_VALUE)
																						.addComponent(
																								jLabel10,
																								javax.swing.GroupLayout.DEFAULT_SIZE,
																								125,
																								Short.MAX_VALUE))
																		.addGroup(
																				jPanel2Layout
																						.createParallelGroup(
																								javax.swing.GroupLayout.Alignment.LEADING)
																						.addGroup(
																								jPanel2Layout
																										.createSequentialGroup()
																										.addGap(
																												6,
																												6,
																												6)
																										.addComponent(
																												leftSideIndicatorPanel,
																												javax.swing.GroupLayout.PREFERRED_SIZE,
																												144,
																												javax.swing.GroupLayout.PREFERRED_SIZE))
																						.addGroup(
																								jPanel2Layout
																										.createSequentialGroup()
																										.addPreferredGap(
																												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																										.addComponent(
																												rightSideIndicatorPanel,
																												javax.swing.GroupLayout.DEFAULT_SIZE,
																												javax.swing.GroupLayout.DEFAULT_SIZE,
																												Short.MAX_VALUE)))))
										.addGap(47, 47, 47)));
		jPanel2Layout
				.setVerticalGroup(jPanel2Layout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								jPanel2Layout
										.createSequentialGroup()
										.addGroup(
												jPanel2Layout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.LEADING)
														.addGroup(
																jPanel2Layout
																		.createSequentialGroup()
																		.addGap(
																				2,
																				2,
																				2)
																		.addGroup(
																				jPanel2Layout
																						.createParallelGroup(
																								javax.swing.GroupLayout.Alignment.LEADING,
																								false)
																						.addGroup(
																								jPanel2Layout
																										.createSequentialGroup()
																										.addGroup(
																												jPanel2Layout
																														.createParallelGroup(
																																javax.swing.GroupLayout.Alignment.LEADING)
																														.addComponent(
																																jLabel97,
																																javax.swing.GroupLayout.PREFERRED_SIZE,
																																30,
																																javax.swing.GroupLayout.PREFERRED_SIZE)
																														.addComponent(
																																numberVisiblePanel,
																																javax.swing.GroupLayout.PREFERRED_SIZE,
																																javax.swing.GroupLayout.DEFAULT_SIZE,
																																javax.swing.GroupLayout.PREFERRED_SIZE))
																										.addPreferredGap(
																												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																										.addGroup(
																												jPanel2Layout
																														.createParallelGroup(
																																javax.swing.GroupLayout.Alignment.LEADING,
																																false)
																														.addComponent(
																																sideMirrorPanel,
																																javax.swing.GroupLayout.PREFERRED_SIZE,
																																javax.swing.GroupLayout.DEFAULT_SIZE,
																																javax.swing.GroupLayout.PREFERRED_SIZE)
																														.addComponent(
																																jLabel89,
																																javax.swing.GroupLayout.PREFERRED_SIZE,
																																30,
																																javax.swing.GroupLayout.PREFERRED_SIZE)))
																						.addGroup(
																								jPanel2Layout
																										.createSequentialGroup()
																										.addGroup(
																												jPanel2Layout
																														.createParallelGroup(
																																javax.swing.GroupLayout.Alignment.TRAILING)
																														.addComponent(
																																jLabel86,
																																javax.swing.GroupLayout.PREFERRED_SIZE,
																																30,
																																javax.swing.GroupLayout.PREFERRED_SIZE)
																														.addComponent(
																																seatBeltPanel,
																																javax.swing.GroupLayout.PREFERRED_SIZE,
																																javax.swing.GroupLayout.DEFAULT_SIZE,
																																javax.swing.GroupLayout.PREFERRED_SIZE))
																										.addPreferredGap(
																												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																										.addGroup(
																												jPanel2Layout
																														.createParallelGroup(
																																javax.swing.GroupLayout.Alignment.LEADING)
																														.addComponent(
																																jLabel6,
																																javax.swing.GroupLayout.DEFAULT_SIZE,
																																javax.swing.GroupLayout.DEFAULT_SIZE,
																																Short.MAX_VALUE)
																														.addComponent(
																																jPanel6,
																																javax.swing.GroupLayout.DEFAULT_SIZE,
																																javax.swing.GroupLayout.DEFAULT_SIZE,
																																Short.MAX_VALUE)
																														.addGroup(
																																jPanel2Layout
																																		.createSequentialGroup()
																																		.addComponent(
																																				tarpaulinPanel,
																																				javax.swing.GroupLayout.PREFERRED_SIZE,
																																				javax.swing.GroupLayout.DEFAULT_SIZE,
																																				javax.swing.GroupLayout.PREFERRED_SIZE)
																																		.addGap(
																																				0,
																																				0,
																																				Short.MAX_VALUE)))))
																		.addPreferredGap(
																				javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																		.addGroup(
																				jPanel2Layout
																						.createParallelGroup(
																								javax.swing.GroupLayout.Alignment.LEADING,
																								false)
																						.addComponent(
																								jLabel8,
																								javax.swing.GroupLayout.DEFAULT_SIZE,
																								javax.swing.GroupLayout.DEFAULT_SIZE,
																								Short.MAX_VALUE)
																						.addComponent(
																								jPanel5,
																								javax.swing.GroupLayout.DEFAULT_SIZE,
																								javax.swing.GroupLayout.DEFAULT_SIZE,
																								Short.MAX_VALUE)
																						.addComponent(
																								sealOkPanel,
																								javax.swing.GroupLayout.DEFAULT_SIZE,
																								javax.swing.GroupLayout.DEFAULT_SIZE,
																								Short.MAX_VALUE)))
														.addGroup(
																jPanel2Layout
																		.createSequentialGroup()
																		.addGroup(
																				jPanel2Layout
																						.createParallelGroup(
																								javax.swing.GroupLayout.Alignment.LEADING,
																								false)
																						.addComponent(
																								jLabel85,
																								javax.swing.GroupLayout.DEFAULT_SIZE,
																								javax.swing.GroupLayout.DEFAULT_SIZE,
																								Short.MAX_VALUE)
																						.addComponent(
																								reverseHornPanel,
																								javax.swing.GroupLayout.DEFAULT_SIZE,
																								javax.swing.GroupLayout.DEFAULT_SIZE,
																								Short.MAX_VALUE)
																						.addComponent(
																								jLabel9,
																								javax.swing.GroupLayout.DEFAULT_SIZE,
																								javax.swing.GroupLayout.DEFAULT_SIZE,
																								Short.MAX_VALUE)
																						.addGroup(
																								jPanel2Layout
																										.createSequentialGroup()
																										.addComponent(
																												leftSideIndicatorPanel,
																												javax.swing.GroupLayout.PREFERRED_SIZE,
																												javax.swing.GroupLayout.DEFAULT_SIZE,
																												javax.swing.GroupLayout.PREFERRED_SIZE)
																										.addGap(
																												2,
																												2,
																												2)))
																		.addPreferredGap(
																				javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																		.addGroup(
																				jPanel2Layout
																						.createParallelGroup(
																								javax.swing.GroupLayout.Alignment.LEADING)
																						.addGroup(
																								jPanel2Layout
																										.createParallelGroup(
																												javax.swing.GroupLayout.Alignment.LEADING,
																												false)
																										.addComponent(
																												tailLightPanel,
																												javax.swing.GroupLayout.DEFAULT_SIZE,
																												javax.swing.GroupLayout.DEFAULT_SIZE,
																												Short.MAX_VALUE)
																										.addComponent(
																												jLabel87,
																												javax.swing.GroupLayout.DEFAULT_SIZE,
																												javax.swing.GroupLayout.DEFAULT_SIZE,
																												Short.MAX_VALUE)
																										.addComponent(
																												jLabel10,
																												javax.swing.GroupLayout.DEFAULT_SIZE,
																												javax.swing.GroupLayout.DEFAULT_SIZE,
																												Short.MAX_VALUE))
																						.addComponent(
																								rightSideIndicatorPanel,
																								javax.swing.GroupLayout.PREFERRED_SIZE,
																								javax.swing.GroupLayout.DEFAULT_SIZE,
																								javax.swing.GroupLayout.PREFERRED_SIZE))
																		.addPreferredGap(
																				javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																		.addGroup(
																				jPanel2Layout
																						.createParallelGroup(
																								javax.swing.GroupLayout.Alignment.LEADING)
																						.addComponent(
																								jLabel88,
																								javax.swing.GroupLayout.PREFERRED_SIZE,
																								30,
																								javax.swing.GroupLayout.PREFERRED_SIZE)
																						.addComponent(
																								headLightPanel,
																								javax.swing.GroupLayout.DEFAULT_SIZE,
																								javax.swing.GroupLayout.DEFAULT_SIZE,
																								Short.MAX_VALUE))))
										.addGap(0, 0, 0)));

		headLightPanel.getAccessibleContext().setAccessibleDescription("");

		jPanel7.setBackground(java.awt.SystemColor.controlLtHighlight);
		jPanel7.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

		jLabel23.setFont(UIConstant.labelFont);
		jLabel23.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
		jLabel23.setText("DL#:");
		jPanel7.add(jLabel23,
				new org.netbeans.lib.awtextra.AbsoluteConstraints(47, 73, 150,
						30));

		jLabel24.setFont(UIConstant.labelFont);
		jLabel24.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
		jLabel24.setText("Driver:");
		jPanel7.add(jLabel24,
				new org.netbeans.lib.awtextra.AbsoluteConstraints(47, 106, 150,
						30));

		jLabel25.setFont(UIConstant.labelFont);
		jLabel25.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
		jLabel25.setText("Not Driven By Helper?:");
		jPanel7.add(jLabel25,
				new org.netbeans.lib.awtextra.AbsoluteConstraints(-14, 139,
						210, 28));

		jLabel26.setFont(UIConstant.labelFont);
		jLabel26.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
		jLabel26.setText("Breathlyzer Pass?:");
		jPanel7.add(jLabel26,
				new org.netbeans.lib.awtextra.AbsoluteConstraints(21, 5, 170,
						30));
		jPanel7.add(filler1, new org.netbeans.lib.awtextra.AbsoluteConstraints(
				422, 0, -1, 180));

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

		javax.swing.GroupLayout breathlizerOkPanelLayout = new javax.swing.GroupLayout(
				breathlizerOkPanel);
		breathlizerOkPanel.setLayout(breathlizerOkPanelLayout);
		breathlizerOkPanelLayout
				.setHorizontalGroup(breathlizerOkPanelLayout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								javax.swing.GroupLayout.Alignment.TRAILING,
								breathlizerOkPanelLayout
										.createSequentialGroup()
										.addComponent(
												breathlizerYes,
												javax.swing.GroupLayout.PREFERRED_SIZE,
												47,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(
												breathlizerNo,
												javax.swing.GroupLayout.PREFERRED_SIZE,
												48,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(
												breathlizerNC,
												javax.swing.GroupLayout.DEFAULT_SIZE,
												43, Short.MAX_VALUE)
										.addContainerGap()));
		breathlizerOkPanelLayout
				.setVerticalGroup(breathlizerOkPanelLayout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								breathlizerOkPanelLayout
										.createParallelGroup(
												javax.swing.GroupLayout.Alignment.BASELINE)
										.addComponent(
												breathlizerNo,
												javax.swing.GroupLayout.PREFERRED_SIZE,
												30,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addComponent(
												breathlizerYes,
												javax.swing.GroupLayout.PREFERRED_SIZE,
												30,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addComponent(
												breathlizerNC,
												javax.swing.GroupLayout.PREFERRED_SIZE,
												30,
												javax.swing.GroupLayout.PREFERRED_SIZE)));

		jPanel7.add(breathlizerOkPanel,
				new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 10, -1,
						-1));

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

		javax.swing.GroupLayout HelperPanelLayout = new javax.swing.GroupLayout(
				HelperPanel);
		HelperPanel.setLayout(HelperPanelLayout);
		HelperPanelLayout
				.setHorizontalGroup(HelperPanelLayout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								javax.swing.GroupLayout.Alignment.TRAILING,
								HelperPanelLayout
										.createSequentialGroup()
										.addComponent(
												helperYes,
												javax.swing.GroupLayout.DEFAULT_SIZE,
												63, Short.MAX_VALUE)
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(helperNo).addGap(8, 8, 8)));
		HelperPanelLayout
				.setVerticalGroup(HelperPanelLayout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								HelperPanelLayout
										.createParallelGroup(
												javax.swing.GroupLayout.Alignment.BASELINE)
										.addComponent(
												helperNo,
												javax.swing.GroupLayout.PREFERRED_SIZE,
												30,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addComponent(
												helperYes,
												javax.swing.GroupLayout.PREFERRED_SIZE,
												30,
												javax.swing.GroupLayout.PREFERRED_SIZE)));

		jPanel7.add(HelperPanel,
				new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 140,
						110, -1));

		dlNo.setFont(UIConstant.textFont);
		dlNo.setForeground(UIConstant.textFontColor);
		dlNo.setBorder(null);
		dlNo.setFocusable(false);
		dlNo.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				dlNoActionPerformed(evt);
			}
		});
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
		jPanel7.add(dlNo, new org.netbeans.lib.awtextra.AbsoluteConstraints(
				200, 80, 215, 30));

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
		jPanel7.add(driverName,
				new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 110,
						215, 30));

		jLabel27.setFont(UIConstant.labelFont);
		jLabel27.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
		jLabel27.setText("Gate Pass Id:");
		jPanel7.add(jLabel27,
				new org.netbeans.lib.awtextra.AbsoluteConstraints(49, 40, 150,
						31));

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

			public void keyReleased(java.awt.event.KeyEvent evt) {
				driverIdKeyReleased(evt);
			}
		});
		jPanel7.add(driverId,
				new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 40, 215,
						31));

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

		manualEntryButton.setFont(UIConstant.buttonFont);
		manualEntryButton.setText("Manual Entry");
		manualEntryButton
				.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent evt) {
						manualEntryButtonActionPerformed(evt);
					}
				});
		manualEntryButton.addFocusListener(new java.awt.event.FocusAdapter() {
			public void focusGained(java.awt.event.FocusEvent evt) {
				manualEntryButtonFocusGained(evt);
			}
		});
		manualEntryButton.addKeyListener(new java.awt.event.KeyAdapter() {
			public void keyPressed(java.awt.event.KeyEvent evt) {
				manualEntryButtonKeyPressed(evt);
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

		photo.setBorder(javax.swing.BorderFactory
				.createLineBorder(new java.awt.Color(0, 0, 0)));

		jPanel8.setBackground(new java.awt.Color(255, 255, 255));
		jPanel8.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

		jLabel3.setFont(UIConstant.vehicleLabel);
		jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
		jLabel3.setText("Vehicle:");
		jPanel8.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(
				0, 4, 150, 50));

		jLabel5.setFont(UIConstant.labelFont);
		jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
		jLabel5.setText("Override?:");
		jPanel8.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(
				22, 53, 128, 32));

		overrides.setEditable(false);
		overrides.setBackground(new java.awt.Color(255, 255, 255));
		overrides.setBorder(null);
		overrides.setFocusable(false);
		overrides.setFont(UIConstant.textFont);
		overrides.setForeground(UIConstant.textFontColor);
		jPanel8.add(overrides,
				new org.netbeans.lib.awtextra.AbsoluteConstraints(154, 56, 213,
						31));

		captureButton.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
		captureButton.setText("Capture Finger Print >>>");
		captureButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				captureButtonActionPerformed(evt);
			}
		});
		captureButton.addFocusListener(new java.awt.event.FocusAdapter() {
			public void focusGained(java.awt.event.FocusEvent evt) {
				captureButtonFocusGained(evt);
			}
		});
		captureButton.addKeyListener(new java.awt.event.KeyAdapter() {
			public void keyPressed(java.awt.event.KeyEvent evt) {
				captureButtonKeyPressed(evt);
			}
		});
		jPanel8.add(captureButton,
				new org.netbeans.lib.awtextra.AbsoluteConstraints(547, 3, 237,
						45));

		fingerInstruction.setFont(new java.awt.Font("Segoe UI", 1, 14));
		jPanel8.add(fingerInstruction,
				new org.netbeans.lib.awtextra.AbsoluteConstraints(832, 3, 178,
						47));

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
		jPanel8.add(vehicleName,
				new org.netbeans.lib.awtextra.AbsoluteConstraints(154, 5, 259,
						45));

		jLabel12.setFont(UIConstant.labelFont);
		jLabel12.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
		jLabel12.setText("Driving Status:");
		jPanel8.add(jLabel12,
				new org.netbeans.lib.awtextra.AbsoluteConstraints(488, 56, 147,
						28));

		driverHours.setFont(UIConstant.textFont); // NOI18N

		driverHours.setForeground(UIConstant.textFontColor);

		jPanel8.add(driverHours,
				new org.netbeans.lib.awtextra.AbsoluteConstraints(641, 54, 192,
						30));

		filler2.setBorder(javax.swing.BorderFactory.createLineBorder(
				new java.awt.Color(0, 0, 0), 2));
		filler2.setFocusCycleRoot(true);

		filler3.setBorder(javax.swing.BorderFactory.createLineBorder(
				new java.awt.Color(0, 0, 0), 2));

		digitalClock.setFont(UIConstant.textFont); // NOI18N

		digitalClock.setForeground(UIConstant.textFontColor);
		digitalClock.setFont(new java.awt.Font("Segoe UI", 1, 36));

		lblScore.setText("1");

		lblSteps.setText("1");

		lblCurrentImageInfo.setText("1");

		manualSync.setText("Sync");

		javax.swing.GroupLayout fingerPrintPanelLayout = new javax.swing.GroupLayout(
				fingerPrintPanel);
		fingerPrintPanel.setLayout(fingerPrintPanelLayout);
		fingerPrintPanelLayout
				.setHorizontalGroup(fingerPrintPanelLayout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								fingerPrintPanelLayout
										.createSequentialGroup()
										.addGroup(
												fingerPrintPanelLayout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.LEADING)
														.addComponent(
																lblScore,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																48,
																javax.swing.GroupLayout.PREFERRED_SIZE)
														.addComponent(
																progressBar,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																48,
																javax.swing.GroupLayout.PREFERRED_SIZE))
										.addGroup(
												fingerPrintPanelLayout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.LEADING)
														.addGroup(
																fingerPrintPanelLayout
																		.createSequentialGroup()
																		.addGap(
																				8,
																				8,
																				8)
																		.addComponent(
																				lblInstruction,
																				javax.swing.GroupLayout.PREFERRED_SIZE,
																				150,
																				javax.swing.GroupLayout.PREFERRED_SIZE))
														.addGroup(
																fingerPrintPanelLayout
																		.createSequentialGroup()
																		.addPreferredGap(
																				javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																		.addComponent(
																				lblSteps,
																				javax.swing.GroupLayout.PREFERRED_SIZE,
																				154,
																				javax.swing.GroupLayout.PREFERRED_SIZE)))
										.addGap(18, 18, 18)
										.addComponent(
												manualSync,
												javax.swing.GroupLayout.PREFERRED_SIZE,
												90,
												javax.swing.GroupLayout.PREFERRED_SIZE))
						.addComponent(lblCurrentImageInfo,
								javax.swing.GroupLayout.PREFERRED_SIZE, 220,
								javax.swing.GroupLayout.PREFERRED_SIZE));
		fingerPrintPanelLayout
				.setVerticalGroup(fingerPrintPanelLayout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								fingerPrintPanelLayout
										.createSequentialGroup()
										.addGroup(
												fingerPrintPanelLayout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.LEADING)
														.addGroup(
																fingerPrintPanelLayout
																		.createSequentialGroup()
																		.addComponent(
																				lblScore,
																				javax.swing.GroupLayout.PREFERRED_SIZE,
																				34,
																				javax.swing.GroupLayout.PREFERRED_SIZE)
																		.addGap(
																				6,
																				6,
																				6)
																		.addComponent(
																				progressBar,
																				javax.swing.GroupLayout.PREFERRED_SIZE,
																				127,
																				javax.swing.GroupLayout.PREFERRED_SIZE))
														.addGroup(
																fingerPrintPanelLayout
																		.createSequentialGroup()
																		.addGroup(
																				fingerPrintPanelLayout
																						.createParallelGroup(
																								javax.swing.GroupLayout.Alignment.TRAILING,
																								false)
																						.addComponent(
																								lblSteps,
																								javax.swing.GroupLayout.DEFAULT_SIZE,
																								javax.swing.GroupLayout.DEFAULT_SIZE,
																								Short.MAX_VALUE)
																						.addGroup(
																								fingerPrintPanelLayout
																										.createSequentialGroup()
																										.addGap(
																												40,
																												40,
																												40)
																										.addComponent(
																												manualSync,
																												javax.swing.GroupLayout.PREFERRED_SIZE,
																												55,
																												javax.swing.GroupLayout.PREFERRED_SIZE)))
																		.addGap(
																				5,
																				5,
																				5)
																		.addComponent(
																				lblInstruction,
																				javax.swing.GroupLayout.PREFERRED_SIZE,
																				60,
																				javax.swing.GroupLayout.PREFERRED_SIZE)))
										.addGap(6, 6, 6)
										.addComponent(
												lblCurrentImageInfo,
												javax.swing.GroupLayout.PREFERRED_SIZE,
												28,
												javax.swing.GroupLayout.PREFERRED_SIZE)));

		javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(
				jPanel1);
		jPanel1.setLayout(jPanel1Layout);
		jPanel1Layout
				.setHorizontalGroup(jPanel1Layout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addComponent(jPanel3,
								javax.swing.GroupLayout.PREFERRED_SIZE,
								javax.swing.GroupLayout.DEFAULT_SIZE,
								javax.swing.GroupLayout.PREFERRED_SIZE)
						.addGroup(
								jPanel1Layout.createSequentialGroup().addGap(
										30, 30, 30).addComponent(
										blocking_reason,
										javax.swing.GroupLayout.PREFERRED_SIZE,
										1451,
										javax.swing.GroupLayout.PREFERRED_SIZE))
						.addGroup(
								jPanel1Layout.createSequentialGroup().addGap(
										129, 129, 129).addComponent(jPanel8,
										javax.swing.GroupLayout.PREFERRED_SIZE,
										1381,
										javax.swing.GroupLayout.PREFERRED_SIZE))
						.addGroup(
								jPanel1Layout.createSequentialGroup().addGap(
										145, 145, 145).addComponent(jPanel4,
										javax.swing.GroupLayout.PREFERRED_SIZE,
										javax.swing.GroupLayout.DEFAULT_SIZE,
										javax.swing.GroupLayout.PREFERRED_SIZE))
						.addGroup(
								jPanel1Layout.createSequentialGroup().addGap(
										128, 128, 128).addComponent(filler3,
										javax.swing.GroupLayout.PREFERRED_SIZE,
										1130,
										javax.swing.GroupLayout.PREFERRED_SIZE))
						.addGroup(
								jPanel1Layout.createSequentialGroup().addGap(
										30, 30, 30).addComponent(jPanel2,
										javax.swing.GroupLayout.PREFERRED_SIZE,
										javax.swing.GroupLayout.DEFAULT_SIZE,
										javax.swing.GroupLayout.PREFERRED_SIZE))
						.addGroup(
								jPanel1Layout.createSequentialGroup().addGap(
										124, 124, 124).addComponent(filler2,
										javax.swing.GroupLayout.PREFERRED_SIZE,
										1130,
										javax.swing.GroupLayout.PREFERRED_SIZE))
						.addGroup(
								jPanel1Layout
										.createSequentialGroup()
										.addGap(239, 239, 239)
										.addComponent(
												jPanel7,
												javax.swing.GroupLayout.PREFERRED_SIZE,
												490,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addGap(10, 10, 10)
										.addComponent(
												photo,
												javax.swing.GroupLayout.PREFERRED_SIZE,
												158,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addGap(12, 12, 12)
										.addComponent(
												fingerPrintPanel,
												javax.swing.GroupLayout.PREFERRED_SIZE,
												javax.swing.GroupLayout.DEFAULT_SIZE,
												javax.swing.GroupLayout.PREFERRED_SIZE))
						.addGroup(
								jPanel1Layout
										.createSequentialGroup()
										.addGap(240, 240, 240)
										.addComponent(
												clearButton,
												javax.swing.GroupLayout.PREFERRED_SIZE,
												131,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addGap(41, 41, 41)
										.addComponent(
												manualEntryButton,
												javax.swing.GroupLayout.PREFERRED_SIZE,
												241,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addGap(37, 37, 37)
										.addComponent(
												saveButton,
												javax.swing.GroupLayout.PREFERRED_SIZE,
												231,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addGap(172, 172, 172)
										.addComponent(
												digitalClock,
												javax.swing.GroupLayout.PREFERRED_SIZE,
												200,
												javax.swing.GroupLayout.PREFERRED_SIZE)));
		jPanel1Layout
				.setVerticalGroup(jPanel1Layout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								jPanel1Layout
										.createSequentialGroup()
										.addComponent(
												jPanel3,
												javax.swing.GroupLayout.PREFERRED_SIZE,
												javax.swing.GroupLayout.DEFAULT_SIZE,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addGap(11, 11, 11)
										.addComponent(
												blocking_reason,
												javax.swing.GroupLayout.PREFERRED_SIZE,
												32,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addGap(3, 3, 3)
										.addComponent(
												jPanel8,
												javax.swing.GroupLayout.PREFERRED_SIZE,
												90,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addGap(1, 1, 1)
										.addComponent(
												jPanel4,
												javax.swing.GroupLayout.PREFERRED_SIZE,
												javax.swing.GroupLayout.DEFAULT_SIZE,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addGap(6, 6, 6)
										.addComponent(
												filler3,
												javax.swing.GroupLayout.PREFERRED_SIZE,
												1,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addGap(6, 6, 6)
										.addComponent(
												jPanel2,
												javax.swing.GroupLayout.PREFERRED_SIZE,
												javax.swing.GroupLayout.DEFAULT_SIZE,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addGap(6, 6, 6)
										.addComponent(
												filler2,
												javax.swing.GroupLayout.PREFERRED_SIZE,
												1,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addGap(4, 4, 4)
										.addGroup(
												jPanel1Layout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.LEADING)
														.addComponent(
																jPanel7,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																170,
																javax.swing.GroupLayout.PREFERRED_SIZE)
														.addComponent(
																photo,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																165,
																javax.swing.GroupLayout.PREFERRED_SIZE)
														.addComponent(
																fingerPrintPanel,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE))
										.addGap(12, 12, 12)
										.addGroup(
												jPanel1Layout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.LEADING)
														.addComponent(
																clearButton,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																41,
																javax.swing.GroupLayout.PREFERRED_SIZE)
														.addComponent(
																manualEntryButton,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																44,
																javax.swing.GroupLayout.PREFERRED_SIZE)
														.addComponent(
																saveButton,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																44,
																javax.swing.GroupLayout.PREFERRED_SIZE)
														.addGroup(
																jPanel1Layout
																		.createSequentialGroup()
																		.addGap(
																				18,
																				18,
																				18)
																		.addComponent(
																				digitalClock,
																				javax.swing.GroupLayout.PREFERRED_SIZE,
																				50,
																				javax.swing.GroupLayout.PREFERRED_SIZE)))));

		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(
				getContentPane());
		getContentPane().setLayout(layout);
		layout.setHorizontalGroup(layout.createParallelGroup(
				javax.swing.GroupLayout.Alignment.LEADING).addGroup(
				layout.createSequentialGroup().addComponent(jPanel1,
						javax.swing.GroupLayout.PREFERRED_SIZE, 1369,
						javax.swing.GroupLayout.PREFERRED_SIZE).addGap(0, 0,
						Short.MAX_VALUE)));
		layout.setVerticalGroup(layout.createParallelGroup(
				javax.swing.GroupLayout.Alignment.LEADING).addComponent(
				jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE,
				javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE));

		pack();
	}// </editor-fold>

	private void myComponent() {
		sealOk.add(sealNC);
		sealOk.add(sealNo);
		sealOk.add(sealYes);

		sealNC.setVisible(false);
		sealNo.setVisible(false);
		sealYes.setVisible(false);

		tarpaulin.add(tarpaulinNC);
		tarpaulin.add(tarpaulinNo);
		tarpaulin.add(tarpaulinYes);

		tarpaulinNC.setVisible(false);
		tarpaulinYes.setVisible(false);
		tarpaulinNo.setVisible(false);

		jLabel6.setVisible(false);
		jLabel8.setVisible(false);

		numberVisible.add(numberVisibleYes);
		numberVisible.add(numberVisibleNo);
		numberVisible.add(numberVisibleNC);
		tailLightOk.add(tailLightYes);
		tailLightOk.add(tailLightNo);
		tailLightOk.add(tailLightNC);

		headLightOk.add(headLightNC);
		headLightOk.add(headLightYes);
		headLightOk.add(headLightNo);

		sideMirrorOk.add(sideMirrorNo);
		sideMirrorOk.add(sideMirrorYes);
		sideMirrorOk.add(sideMirrorNC);
		leftSideIndicator.add(leftSideIndicatorNo);
		leftSideIndicator.add(leftSideIndicatorYes);
		leftSideIndicator.add(leftSideIndicatorNC);
		rightSideIndicator.add(rightSideIndicatorNC);
		rightSideIndicator.add(rightSideIndicatorYes);
		rightSideIndicator.add(rightSideIndicatorNo);

		seatBelt.add(seatBeltNo);
		seatBelt.add(seatBeltYes);
		seatBelt.add(seatBeltNC);
		breathlizerOk.add(breathlizerYes);
		breathlizerOk.add(breathlizerNo);
		breathlizerOk.add(breathlizerNC);
		helperOk.add(helperNo);
		helperOk.add(helperYes);

		reverseHornOk.add(reverseHornNC);
		reverseHornOk.add(reverseHornNo);
		reverseHornOk.add(reverseHornYes);

	}

	private void saveButtonActionPerformed(java.awt.event.ActionEvent evt) {
		quickCreateAndSave();
	}

	private void clearButtonActionPerformed(java.awt.event.ActionEvent evt) {
		clearAction();
	}

	private void reverseHornYesActionPerformed(java.awt.event.ActionEvent evt) {
		// TODO add your handling code here:
		reverseHornAction();
		if (doCheck) {
			checkQuestions();
		}
	}

	private void reverseHornNoActionPerformed(java.awt.event.ActionEvent evt) {
		reverseHornAction();
		if (doCheck) {
			checkQuestions();
		}
	}

	private void tailLightYesActionPerformed(java.awt.event.ActionEvent evt) {
		tailLightAction();
		if (doCheck) {
			checkQuestions();
		}
	}

	private void tailLightNoActionPerformed(java.awt.event.ActionEvent evt) {
		tailLightAction();
		if (doCheck) {
			checkQuestions();
		}
	}

	private void headLightYesActionPerformed(java.awt.event.ActionEvent evt) {
		headLightAction();
		if (doCheck) {
			checkQuestions();
		}
	}

	private void headLightNoActionPerformed(java.awt.event.ActionEvent evt) {
		// TODO add your handling code here:
		headLightAction();
		if (doCheck) {
			checkQuestions();
		}
	}

	private void sideMirrorYesActionPerformed(java.awt.event.ActionEvent evt) {
		sideMirrorAction();
		if (doCheck) {
			checkQuestions();
		}
	}

	private void sideMirrorNoActionPerformed(java.awt.event.ActionEvent evt) {
		sideMirrorAction();
		if (doCheck) {
			checkQuestions();
		}
	}

	private void reverseHornPanelKeyPressed(java.awt.event.KeyEvent evt) {
		if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
			// setWhiteBackColor();
			tailLightPanel.requestFocusInWindow();
			// tailLightPanel.setBackground(UIConstant.focusPanelColor);
			if (doCheck) {
				checkQuestions();
			}
		} else if (evt.getKeyCode() == KeyEvent.VK_1
				|| evt.getKeyCode() == KeyEvent.VK_NUMPAD1) {
			reverseHornYes.setSelected(true);
			// setWhiteBackColor();
			tailLightPanel.requestFocusInWindow();
			// tailLightPanel.setBackground(UIConstant.focusPanelColor);
			if (doCheck) {
				checkQuestions();
			}
		} else if (evt.getKeyCode() == KeyEvent.VK_2
				|| evt.getKeyCode() == KeyEvent.VK_NUMPAD2) {
			reverseHornNo.setSelected(true);
			// setWhiteBackColor();
			tailLightPanel.requestFocusInWindow();
			// tailLightPanel.setBackground(UIConstant.focusPanelColor);
			if (doCheck) {
				checkQuestions();
			}
		} else if (evt.getKeyCode() == KeyEvent.VK_3
				|| evt.getKeyCode() == KeyEvent.VK_NUMPAD3) {
			reverseHornNC.setSelected(true);
			// setWhiteBackColor();
			tailLightPanel.requestFocusInWindow();
			// tailLightPanel.setBackground(UIConstant.focusPanelColor);
			if (doCheck) {
				checkQuestions();
			}
		}

	}

	private void headLightPanelKeyPressed(java.awt.event.KeyEvent evt) {
		if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
			// setWhiteBackColor();
			leftSideIndicatorPanel.requestFocusInWindow();
			// leftSideIndicatorPanel.setBackground(UIConstant.focusPanelColor);
			if (doCheck) {
				checkQuestions();
			}
		} else if (evt.getKeyCode() == KeyEvent.VK_1
				|| evt.getKeyCode() == KeyEvent.VK_NUMPAD1) {
			headLightYes.setSelected(true);
			// setWhiteBackColor();
			leftSideIndicatorPanel.requestFocusInWindow();
			// leftSideIndicatorPanel.setBackground(UIConstant.focusPanelColor);
			if (doCheck) {
				checkQuestions();
			}
		} else if (evt.getKeyCode() == KeyEvent.VK_2
				|| evt.getKeyCode() == KeyEvent.VK_NUMPAD2) {
			headLightNo.setSelected(true);
			// setWhiteBackColor();
			leftSideIndicatorPanel.requestFocusInWindow();
			// leftSideIndicatorPanel.setBackground(UIConstant.focusPanelColor);
			if (doCheck) {
				checkQuestions();
			}
		} else if (evt.getKeyCode() == KeyEvent.VK_3
				|| evt.getKeyCode() == KeyEvent.VK_NUMPAD3) {
			headLightNC.setSelected(true);
			// setWhiteBackColor();
			leftSideIndicatorPanel.requestFocusInWindow();
			// leftSideIndicatorPanel.setBackground(UIConstant.focusPanelColor);
			if (doCheck) {
				checkQuestions();
			}
		}
	}

	private void numberVisibleNoActionPerformed(java.awt.event.ActionEvent evt) {
		numberVisibleAction();
		if (doCheck) {
			checkQuestions();
		}
	}

	private void numberVisibleYesActionPerformed(java.awt.event.ActionEvent evt) {
		numberVisibleAction();
		if (doCheck) {
			checkQuestions();
		}
	}

	private void numberVisiblePanelKeyPressed(java.awt.event.KeyEvent evt) {
		if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
			// setWhiteBackColor();
			sideMirrorPanel.requestFocusInWindow();
			// sideMirrorPanel.setBackground(UIConstant.focusPanelColor);
			if (doCheck) {
				checkQuestions();
			}

		} else if (evt.getKeyCode() == KeyEvent.VK_1
				|| evt.getKeyCode() == KeyEvent.VK_NUMPAD1) {
			numberVisibleYes.setSelected(true);
			// setWhiteBackColor();
			// sideMirrorPanel.setBackground(UIConstant.focusPanelColor);
			sideMirrorPanel.requestFocusInWindow();
			if (doCheck) {
				checkQuestions();
			}
		} else if (evt.getKeyCode() == KeyEvent.VK_2
				|| evt.getKeyCode() == KeyEvent.VK_NUMPAD2) {
			numberVisibleNo.setSelected(true);
			// setWhiteBackColor();
			// sideMirrorPanel.setBackground(UIConstant.focusPanelColor);
			sideMirrorPanel.requestFocusInWindow();
			if (doCheck) {
				checkQuestions();
			}
		} else if (evt.getKeyCode() == KeyEvent.VK_3
				|| evt.getKeyCode() == KeyEvent.VK_NUMPAD3) {
			numberVisibleNC.setSelected(true);
			// setWhiteBackColor();
			// sideMirrorPanel.setBackground(UIConstant.focusPanelColor);
			sideMirrorPanel.requestFocusInWindow();
			if (doCheck) {
				checkQuestions();
			}
		}
	}

	private void headLightYesKeyPressed(java.awt.event.KeyEvent evt) {
		// TODO add your handling code here:
	}

	private void headLightNoKeyPressed(java.awt.event.KeyEvent evt) {
		// TODO add your handling code here:
	}

	private void leftSideIndicatorNoActionPerformed(
			java.awt.event.ActionEvent evt) {
		leftSideIndicatorAction();
		if (doCheck) {
			checkQuestions();
		}
	}

	private void leftSideIndicatorYesActionPerformed(
			java.awt.event.ActionEvent evt) {
		leftSideIndicatorAction();
		if (doCheck) {
			checkQuestions();
		}
	}

	private void leftSideIndicatorAction() {
		rightSideIndicatorPanel.requestFocusInWindow();
		// rightSideIndicatorPanel.setBackground(UIConstant.focusPanelColor);
	}

	private void leftSideIndicatorPanelKeyPressed(java.awt.event.KeyEvent evt) {
		if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
			// setWhiteBackColor();
			rightSideIndicatorPanel.requestFocusInWindow();
			// rightSideIndicatorPanel.setBackground(UIConstant.focusPanelColor);
			if (doCheck) {
				checkQuestions();
			}
		} else if (evt.getKeyCode() == KeyEvent.VK_1
				|| evt.getKeyCode() == KeyEvent.VK_NUMPAD1) {
			leftSideIndicatorYes.setSelected(true);
			// setWhiteBackColor();
			rightSideIndicatorPanel.requestFocusInWindow();
			// rightSideIndicatorPanel.setBackground(UIConstant.focusPanelColor);
			if (doCheck) {
				checkQuestions();
			}
		} else if (evt.getKeyCode() == KeyEvent.VK_2
				|| evt.getKeyCode() == KeyEvent.VK_NUMPAD2) {
			leftSideIndicatorNo.setSelected(true);
			// setWhiteBackColor();
			rightSideIndicatorPanel.requestFocusInWindow();
			// rightSideIndicatorPanel.setBackground(UIConstant.focusPanelColor);
			if (doCheck) {
				checkQuestions();
			}
		} else if (evt.getKeyCode() == KeyEvent.VK_3
				|| evt.getKeyCode() == KeyEvent.VK_NUMPAD3) {
			leftSideIndicatorNC.setSelected(true);
			// setWhiteBackColor();
			rightSideIndicatorPanel.requestFocusInWindow();
			// rightSideIndicatorPanel.setBackground(UIConstant.focusPanelColor);
			if (doCheck) {
				checkQuestions();
			}
		}

	}

	private void seatBeltNoActionPerformed(java.awt.event.ActionEvent evt) {
		seatBeltAction();
		// if (!Utils.isNull(vehicleName.getSelectedItem().toString())
		// && !(vehicleName.getSelectedItem().toString().trim())
		// .equalsIgnoreCase("") && !isSeviceRunning) {
		// fingurePrintCaptureService.start();
		// isSeviceRunning = true;
		// }
		startFingerCaptureService();
	}

	private void seatBeltYesActionPerformed(java.awt.event.ActionEvent evt) {
		seatBeltAction();
		// if (!Utils.isNull(vehicleName.getSelectedItem().toString())
		// && !(vehicleName.getSelectedItem().toString().trim())
		// .equalsIgnoreCase("") && !isSeviceRunning) {
		// fingurePrintCaptureService.start();
		// isSeviceRunning = true;
		// }
		startFingerCaptureService();
	}

	private void seatBeltAction() {
		// setWhiteBackColor();
		// tarpaulinPanel.setBackground(UIConstant.focusPanelColor);
		numberVisiblePanel.requestFocusInWindow();
	}

	private void seatBeltPanelKeyPressed(java.awt.event.KeyEvent evt) {

		if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
			// setWhiteBackColor();
			seatBeltAction();
			// tarpaulinPanel.setBackground(UIConstant.focusPanelColor);
			if (doCheck) {
				checkQuestions();
			}
			// if (!Utils.isNull(vehicleName.getSelectedItem().toString())
			// && !(vehicleName.getSelectedItem().toString().trim())
			// .equalsIgnoreCase("") && !isSeviceRunning
			// && isMorphoExist) {
			// fingurePrintCaptureService.start();
			// isSeviceRunning = true;
			// }
			startFingerCaptureService();
		} else if (evt.getKeyCode() == KeyEvent.VK_1
				|| evt.getKeyCode() == KeyEvent.VK_NUMPAD1) {
			// setWhiteBackColor();
			seatBeltYes.setSelected(true);
			seatBeltAction();
			// tarpaulinPanel.setBackground(UIConstant.focusPanelColor);
			if (doCheck) {
				checkQuestions();
			}
			// if (!Utils.isNull(vehicleName.getSelectedItem().toString())
			// && !(vehicleName.getSelectedItem().toString().trim())
			// .equalsIgnoreCase("") && !isSeviceRunning
			// && isMorphoExist) {
			// fingurePrintCaptureService.start();
			// isSeviceRunning = true;
			// }
			startFingerCaptureService();
		} else if (evt.getKeyCode() == KeyEvent.VK_2
				|| evt.getKeyCode() == KeyEvent.VK_NUMPAD2) {
			// setWhiteBackColor();
			seatBeltNo.setSelected(true);
			seatBeltAction();
			// tarpaulinPanel.setBackground(UIConstant.focusPanelColor);
			if (doCheck) {
				checkQuestions();
			}
			// if (!Utils.isNull(vehicleName.getSelectedItem().toString())
			// && !(vehicleName.getSelectedItem().toString().trim())
			// .equalsIgnoreCase("") && !isSeviceRunning
			// && isMorphoExist) {
			// fingurePrintCaptureService.start();
			// isSeviceRunning = true;
			// }
			startFingerCaptureService();
		} else if (evt.getKeyCode() == KeyEvent.VK_3
				|| evt.getKeyCode() == KeyEvent.VK_NUMPAD3) {
			// setWhiteBackColor();
			seatBeltNC.setSelected(true);
			seatBeltAction();
			// tarpaulinPanel.setBackground(UIConstant.focusPanelColor);
			if (doCheck) {
				checkQuestions();
			}
			// if (!Utils.isNull(vehicleName.getSelectedItem().toString())
			// && !(vehicleName.getSelectedItem().toString().trim())
			// .equalsIgnoreCase("") && !isSeviceRunning
			// && isMorphoExist) {
			// fingurePrintCaptureService.start();
			// isSeviceRunning = true;
			// }
			startFingerCaptureService();
		}
	}

	private void breathlizerNoActionPerformed(java.awt.event.ActionEvent evt) {
		setWhiteBackColor();
		setQuetionsBlocking(Status.TPRQuestion.breathLyzerOk, UIConstant.NO);
		/*
		 * if(tprBlockManager != null &&
		 * tprBlockManager.useForBlocking(Status.TPRQuestion.breathLyzerOk) &&
		 * tprRecord != null){ enableDenyEntry(true);
		 * 
		 * 
		 * 
		 * 
		 * 
		 * blocking_reason.setText(RFIDDataProcessor.getTprBlockStatus(tprRecord,
		 * blockInstructionBreathelizer, true)); }
		 */
		checkQuestions();
	}

	private void breathlizerYesActionPerformed(java.awt.event.ActionEvent evt) {
		setWhiteBackColor();
		setQuetionsBlocking(Status.TPRQuestion.breathLyzerOk, UIConstant.YES);
		/*
		 * if(tprBlockManager != null &&
		 * tprBlockManager.useForBlocking(Status.TPRQuestion.breathLyzerOk)){
		 * enableDenyEntry(false);
		 * blocking_reason.setText(RFIDDataProcessor.getTprBlockStatus
		 * (tprRecord, blockInstructionBreathelizer, false)); }
		 */
		checkQuestions();
	}

	private void breathlizerOkPanelKeyPressed(java.awt.event.KeyEvent evt) {
		if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
			if (!breathlizerNo.isSelected() && !breathlizerYes.isSelected()
					&& !breathlizerNC.isSelected()) {
				breathlizerOkPanel.setBackground(UIConstant.focusPanelColor);
			} else {
				breathlizerOkPanel.setBackground(UIConstant.PanelWhite);
			}
			setWhiteBackColor();

			saveButton.requestFocusInWindow();
			checkQuestions();
		} else if (evt.getKeyCode() == KeyEvent.VK_1
				|| evt.getKeyCode() == KeyEvent.VK_NUMPAD1) {
			breathlizerYes.setSelected(true);
			breathlizerOkPanel.setBackground(Color.WHITE);
			setWhiteBackColor();
			setQuetionsBlocking(Status.TPRQuestion.breathLyzerOk,
					UIConstant.YES);
			checkQuestions();
		} else if (evt.getKeyCode() == KeyEvent.VK_2
				|| evt.getKeyCode() == KeyEvent.VK_NUMPAD2) {
			breathlizerNo.setSelected(true);
			setQuetionsBlocking(Status.TPRQuestion.breathLyzerOk, UIConstant.NO);
			checkQuestions();
		} else if (evt.getKeyCode() == KeyEvent.VK_3
				|| evt.getKeyCode() == KeyEvent.VK_NUMPAD3) {
			breathlizerNC.setSelected(true);
			breathlizerOkPanel.setBackground(Color.WHITE);

			setQuetionsBlocking(Status.TPRQuestion.breathLyzerOk, UIConstant.NC);
			checkQuestions();
		}
	}

	private void tailLightPanelKeyPressed(java.awt.event.KeyEvent evt) {
		if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
			// setWhiteBackColor();
			headLightPanel.requestFocusInWindow();
			// headLightPanel.setBackground(UIConstant.focusPanelColor);
			if (doCheck) {
				checkQuestions();
			}
		} else if (evt.getKeyCode() == KeyEvent.VK_1
				|| evt.getKeyCode() == KeyEvent.VK_NUMPAD1) {
			tailLightYes.setSelected(true);
			// setWhiteBackColor();
			headLightPanel.requestFocusInWindow();
			// headLightPanel.setBackground(UIConstant.focusPanelColor);
			if (doCheck) {
				checkQuestions();
			}
		} else if (evt.getKeyCode() == KeyEvent.VK_2
				|| evt.getKeyCode() == KeyEvent.VK_NUMPAD2) {
			tailLightNo.setSelected(true);
			// setWhiteBackColor();
			headLightPanel.requestFocusInWindow();
			// headLightPanel.setBackground(UIConstant.focusPanelColor);
			if (doCheck) {
				checkQuestions();
			}
		} else if (evt.getKeyCode() == KeyEvent.VK_3
				|| evt.getKeyCode() == KeyEvent.VK_NUMPAD3) {
			tailLightNC.setSelected(true);
			// setWhiteBackColor();
			headLightPanel.requestFocusInWindow();
			// headLightPanel.setBackground(UIConstant.focusPanelColor);
			if (doCheck) {
				checkQuestions();
			}
		}
	}

	private void sideMirrorPanelKeyPressed(java.awt.event.KeyEvent evt) {
		if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
			// setWhiteBackColor();
			reverseHornPanel.requestFocusInWindow();
			// reverseHornPanel.setBackground(UIConstant.focusPanelColor);
			if (doCheck) {
				checkQuestions();
			}
		} else if (evt.getKeyCode() == KeyEvent.VK_1
				|| evt.getKeyCode() == KeyEvent.VK_NUMPAD1) {
			sideMirrorYes.setSelected(true);
			// setWhiteBackColor();
			reverseHornPanel.requestFocusInWindow();
			// reverseHornPanel.setBackground(UIConstant.focusPanelColor);
			if (doCheck) {
				checkQuestions();
			}
		} else if (evt.getKeyCode() == KeyEvent.VK_2
				|| evt.getKeyCode() == KeyEvent.VK_NUMPAD2) {
			sideMirrorNo.setSelected(true);
			// setWhiteBackColor();
			reverseHornPanel.requestFocusInWindow();
			// reverseHornPanel.setBackground(UIConstant.focusPanelColor);
			if (doCheck) {
				checkQuestions();
			}
		} else if (evt.getKeyCode() == KeyEvent.VK_3
				|| evt.getKeyCode() == KeyEvent.VK_NUMPAD3) {
			sideMirrorNC.setSelected(true);
			// setWhiteBackColor();
			reverseHornPanel.requestFocusInWindow();
			// reverseHornPanel.setBackground(UIConstant.focusPanelColor);
			if (doCheck) {
				checkQuestions();
			}
		}
	}

	private void reverseHornPanelMouseClicked(java.awt.event.MouseEvent evt) {
		setWhiteBackColor();
		reverseHornPanel.requestFocusInWindow();
		reverseHornPanel.setBackground(UIConstant.focusPanelColor);
	}

	private void headLightPanelMouseClicked(java.awt.event.MouseEvent evt) {
		// sealOkPanel.setBackgroHeadlightPanel.PanelWhite);
		// tarpaulinPaHeadlightPanelusInWindow();
		setWhiteBackColor();
		headLightPanel.setBackground(UIConstant.focusPanelColor);
		headLightPanel.requestFocusInWindow();
	}

	private void numberVisiblePanelMouseClicked(java.awt.event.MouseEvent evt) {
		numberVisiblePanel.requestFocusInWindow();
		numberVisiblePanel.setBackground(UIConstant.focusPanelColor);
		setWhiteBackColor();
	}

	private void tailLightPanelMouseClicked(java.awt.event.MouseEvent evt) {
		setWhiteBackColor();
		tailLightPanel.requestFocusInWindow();
		tailLightPanel.setBackground(UIConstant.focusPanelColor);
	}

	private void sideMirrorPanelMouseClicked(java.awt.event.MouseEvent evt) {
		setWhiteBackColor();
		sideMirrorPanel.requestFocusInWindow();
		sideMirrorPanel.setBackground(UIConstant.focusPanelColor);
	}

	private void leftSideIndicatorPanelMouseClicked(
			java.awt.event.MouseEvent evt) {
		setWhiteBackColor();
		leftSideIndicatorPanel.requestFocusInWindow();
		leftSideIndicatorPanel.setBackground(UIConstant.focusPanelColor);
	}

	private void seatBeltPanelMouseClicked(java.awt.event.MouseEvent evt) {
		setWhiteBackColor();
		breathlizerOkPanel.requestFocusInWindow();
		breathlizerOkPanel.setBackground(UIConstant.focusPanelColor);
	}

	private void breathlizerOkPanelMouseClicked(java.awt.event.MouseEvent evt) {
		setWhiteBackColor();
		breathlizerOkPanel.requestFocusInWindow();
		breathlizerOkPanel.setBackground(UIConstant.focusPanelColor);
	}

	private void helperNoActionPerformed(java.awt.event.ActionEvent evt) {
		// TODO add your handling code here:
	}

	private void helperYesActionPerformed(java.awt.event.ActionEvent evt) {
		// TODO add your handling code here:
	}

	private void HelperPanelMouseClicked(java.awt.event.MouseEvent evt) {
		// TODO add your handling code here:
	}

	private void HelperPanelKeyPressed(java.awt.event.KeyEvent evt) {
		// TODO add your handling code here:
	}

	private void captureButtonActionPerformed(java.awt.event.ActionEvent evt) {
		if (captureButton.getText().startsWith("Cancel")) {
			if (fingurePrintCaptureService != null) {
				fingurePrintCaptureService.stop();
				if (!isDataSyncRunning)
					MorphoSmartFunctions.getMorpho().cancelAllCurrentTask();

				disableFingurePrintCapture();
			}
		} else {
			if (captureCount < 3) {
				startFingerCaptureService(true);
			} else {
				captureButton.setVisible(false);
				driverId.requestFocusInWindow();
			}
			// captureFingurePrint();
		}
	}

	private void startFingerCaptureService() {
		startFingerCaptureService(false);
	}

	private void startFingerCaptureService(boolean isManual) {
		if ((vehicleName.getSelectedItem().toString().trim())
				.equalsIgnoreCase("")) {
			JOptionPane.showMessageDialog(null, "Please Enter Vehicle Name");
			return;
		} else if (Utils.isNull(vehicleName.getSelectedItem().toString())) {
			JOptionPane.showMessageDialog(null, "Please Enter Vehicle Name");
			return;
		} else if (tprRecord == null) {
			JOptionPane.showMessageDialog(null,
					"Please Enter valid Vehicle Name");
			return;
		} else if (isFingerPrintCaptureRunning || isDataSyncRunning
				|| !isMorphoExist) {
			return;
		} else if (!isAutoFingerCaptureStart || isManual) {
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
				isFingerPrintCaptureRunning = true;
				isAutoFingerCaptureStart = true;
				// captureFingurePrint();
				captureCount++;
			} catch (Exception ex) {
				Logger.getLogger(CoalGateInWindow.class.getName()).log(
						Level.SEVERE, null, ex);
			}
		}
	}

	private void button1ActionPerformed(java.awt.event.ActionEvent evt) {
		stopRfid();
		if (fingerPrintSyncService != null) {
			fingerPrintSyncService.stop();
		}
		this.dispose();

		new LoginWindow().setVisible(true);

	}

	void stopRfid() {
		if (rfidHandler != null) {
			rfidHandler.stop();
		}
	}

	private void manualEntryButtonActionPerformed(java.awt.event.ActionEvent evt) {
		manualEntryAction();
	}

	private void dlNoKeyPressed(java.awt.event.KeyEvent evt) {
		Connection conn = null;
		boolean destroyIt = false;
		try {// GEN-FIRST:event_dlNoKeyPressed
			conn = DBConnectionPool.getConnectionFromPoolNonWeb();
			if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
				System.out.println("Enter Keyed");
				if (Utils.isNull(dlNo.getText())) {
					System.out.println("Enter Keyed null ");
					JOptionPane.showMessageDialog(null,
							"Please Enter Driver Lisence Number !!!");
					return;
				} else {
					System.out.println("Enter Keyed not null");
					driverName.setText("");
					photo.setIcon(null);
					helperOk.clearSelection();
					try {
						// driverInformation =
						// GateInDao.getDriverDetailByDL(dlNo.getText());
						driverInformation = GateInDao.getDriverDetailByDL(conn,
								dlNo.getText(), "DLNo");
						if (driverInformation != null) {
							driverInformation.setIsFingerCaptured(0);
							// changeQuickCreateToOpenGate();
							System.out
									.println("driverInformation is not null ");
							setDriverHour(conn, driverInformation
									.getDriver_id());
							populateDriver(conn, driverInformation, false,
									false);
							// DriverDetail.showDriverDetail(driverInformation);
							driverSrc = Constant.DRIVER_SOURCE_DL_NO;
							/*
							 * if (manualEntryButton.isEnabled()) {
							 * manualEntryButton.requestFocusInWindow(); } else
							 * { quickCreate.requestFocusInWindow(); }
							 */
							setFocusOnButton();
						} else {
							System.out
									.println("driverInformation is not null ");
							String msg = "Driver Detail Not Exist, Do you want to Re-Enter?";
							Object[] options = { "  Yes  ", "  No  " };

							// JOptionPane.showMessageDialog(null,
							// "Driver not Exist please enter driver name");
							int responseVehicleDialog = ConfirmationDialog
									.getDialogBox(new java.awt.Frame(), true,
											options, msg);
							System.out.print("responseVehicleDialog : "
									+ responseVehicleDialog);
							if (responseVehicleDialog == 1) {
								isFingerCaptured = false;
								// isFingerVerified = false;
								// JOptionPane.showMessageDialog(null,
								// "responseVehicleDialog == 1: " +
								// responseVehicleDialog);
								System.out.print("responseVehicleDialog == 1: "
										+ responseVehicleDialog);
								System.out.print("driverName : " + driverName);
								dlNo.setFocusable(false);
								driverName.setFocusable(true);
								driverName.setEditable(true);

								driverName.setBorder(javax.swing.BorderFactory
										.createLineBorder(new java.awt.Color(0,
												0, 0)));
								driverName.requestFocusInWindow();
								dlNo.setFocusable(true);
							} else {
								System.out
										.print("responseVehicleDialog not 1: "
												+ responseVehicleDialog);
								dlNo.requestFocusInWindow();

							}

						}

					} catch (IOException ex) {
						ex.printStackTrace();
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
	}

	private void dlNoKeyReleased(java.awt.event.KeyEvent evt) {
		// TODO add your handling code here:
	}

	private void dlNoFocusGained(java.awt.event.FocusEvent evt) {
		setWhiteBackColor();
		dlNo.setBackground(UIConstant.focusPanelColor);
		driverName.setEditable(false);
		driverName.setFocusable(false);
		driverName.setBorder(null);
		driverName.setText("");
	}

	private void dlNoFocusLost(java.awt.event.FocusEvent evt) {
		dlNo.setBackground(UIConstant.PanelWhite);
	}

	private void sideMirrorNCActionPerformed(java.awt.event.ActionEvent evt) {
		sideMirrorAction();
		if (doCheck) {
			checkQuestions();
		}
	}

	private void sideMirrorAction() {
		// setWhiteBackColor();
		// reverseHornPanel.setBackground(UIConstant.focusPanelColor);
		reverseHornPanel.requestFocusInWindow();
	}

	private void reverseHornNCActionPerformed(java.awt.event.ActionEvent evt) {
		reverseHornAction();
		if (doCheck) {
			checkQuestions();
		}
	}

	private void reverseHornAction() {
		// tailLightPanel.setBackground(UIConstant.focusPanelColor);
		tailLightPanel.requestFocusInWindow();
	}

	private void headLightNCActionPerformed(java.awt.event.ActionEvent evt) {
		headLightAction();
		if (doCheck) {
			checkQuestions();
		}
	}

	private void headLightAction() {
		// setWhiteBackColor();
		// leftSideIndicatorPanel.setBackground(UIConstant.focusPanelColor);
		leftSideIndicatorPanel.requestFocusInWindow();
	}

	private void numberVisibleNCActionPerformed(java.awt.event.ActionEvent evt) {
		numberVisibleAction();
		if (doCheck) {
			checkQuestions();
		}
	}

	private void numberVisibleAction() {
		// setWhiteBackColor();
		sideMirrorPanel.requestFocusInWindow();
		// sideMirrorPanel.setBackground(UIConstant.focusPanelColor);
	}

	private void tailLightNCActionPerformed(java.awt.event.ActionEvent evt) {
		tailLightAction();
		if (doCheck) {
			checkQuestions();
		}
	}

	private void tailLightAction() {
		// setWhiteBackColor();
		// headLightPanel.setBackground(UIConstant.focusPanelColor);
		headLightPanel.requestFocusInWindow();
	}

	private void leftSideIndicatorNCActionPerformed(
			java.awt.event.ActionEvent evt) {
		leftSideIndicatorAction();
		if (doCheck) {
			checkQuestions();
		}
	}

	private void seatBeltNCActionPerformed(java.awt.event.ActionEvent evt) {
		seatBeltAction();
		if (!Utils.isNull(vehicleName.getSelectedItem().toString())
				&& !(vehicleName.getSelectedItem().toString().trim())
						.equalsIgnoreCase("") && !isSeviceRunning) {
			fingurePrintCaptureService.start();
			isSeviceRunning = true;
		}
	}

	private void driverNameKeyPressed(java.awt.event.KeyEvent evt) {

		if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
			Connection conn = null;
			boolean destroyIt = false;
			try {
				conn = DBConnectionPool.getConnectionFromPoolNonWeb();
				if (Utils.isNull(driverName.getText())) {
					JOptionPane.showMessageDialog(null,
							"Please Enter Driver Name !!!");
					return;
				} else {
					try {
						driverInformation = GateInDao.insertDriverDetailByDL(
								conn, dlNo.getText(), driverName.getText(),
								tprRecord.getVehicleId());
						if (driverInformation != null) {
							driverInformation.setIsFingerCaptured(0);
							populateDriver(conn, driverInformation, false,
									false);
							// DriverDetail.showDriverDetail(driverInformation);
							driverSrc = Constant.DRIVER_SOURCE_NEW;
						} else {
							isFingerCaptured = false;
						}
						saveButton.requestFocusInWindow();
					} catch (IOException ex) {
						Logger.getLogger(CoalGateInWindow.class.getName()).log(
								Level.SEVERE, null, ex);
					} catch (Exception ex) {
						Logger.getLogger(CoalGateInWindow.class.getName()).log(
								Level.SEVERE, null, ex);
					}
				}
			} catch (Exception ex) {
				ex.printStackTrace();
				destroyIt = true;
			} finally {
				try {
					DBConnectionPool.returnConnectionToPoolNonWeb(conn,
							destroyIt);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}

		}
	}

	private void driverNameFocusGained(java.awt.event.FocusEvent evt) {
		setWhiteBackColor();
		driverName.setBackground(UIConstant.focusPanelColor);
	}

	private void reverseHornPanelFocusLost(java.awt.event.FocusEvent evt) {
		setWhiteBackColor();
		if (doCheck) {
			checkQuestions();
		}
	}

	private void reverseHornPanelFocusGained(java.awt.event.FocusEvent evt) {
		WaveFormPlayer.playSoundIn(Status.TPRQuestion.reverseHornOk);
		setWhiteBackColor();
		reverseHornPanel.setBackground(UIConstant.focusPanelColor);
	}

	private void headLightPanelFocusGained(java.awt.event.FocusEvent evt) {
		WaveFormPlayer.playSoundIn(Status.TPRQuestion.headLightOk);
		setWhiteBackColor();
		headLightPanel.setBackground(UIConstant.focusPanelColor);
	}

	private void headLightPanelFocusLost(java.awt.event.FocusEvent evt) {
		setWhiteBackColor();
		if (doCheck) {
			checkQuestions();
		}
	}

	private void numberVisiblePanelFocusGained(java.awt.event.FocusEvent evt) {
		setWhiteBackColor();
		numberVisiblePanel.setBackground(UIConstant.focusPanelColor);
	}

	private void numberVisiblePanelFocusLost(java.awt.event.FocusEvent evt) {
		setWhiteBackColor();
		if (doCheck) {
			checkQuestions();
		}
	}

	private void tailLightPanelFocusGained(java.awt.event.FocusEvent evt) {
		WaveFormPlayer.playSoundIn(Status.TPRQuestion.pushBrake);
		setWhiteBackColor();
		tailLightPanel.setBackground(UIConstant.focusPanelColor);
	}

	private void tailLightPanelFocusLost(java.awt.event.FocusEvent evt) {
		setWhiteBackColor();
		if (doCheck) {
			checkQuestions();
		}
	}

	private void sideMirrorPanelFocusGained(java.awt.event.FocusEvent evt) {
		setWhiteBackColor();
		sideMirrorPanel.setBackground(UIConstant.focusPanelColor);
	}

	private void sideMirrorPanelFocusLost(java.awt.event.FocusEvent evt) {
		setWhiteBackColor();
		if (doCheck) {
			checkQuestions();
		}
	}

	private void leftSideIndicatorPanelFocusGained(java.awt.event.FocusEvent evt) {
		WaveFormPlayer.playSoundIn(Status.TPRQuestion.leftSideIndicator);
		setWhiteBackColor();
		leftSideIndicatorPanel.setBackground(UIConstant.focusPanelColor);
	}

	private void seatBeltPanelFocusGained(java.awt.event.FocusEvent evt) {
		setWhiteBackColor();
		seatBeltPanel.setBackground(UIConstant.focusPanelColor);
	}

	private void seatBeltPanelFocusLost(java.awt.event.FocusEvent evt) {
		setWhiteBackColor();
	}

	private void leftSideIndicatorPanelFocusLost(java.awt.event.FocusEvent evt) {
		setWhiteBackColor();
		if (doCheck) {
			checkQuestions();
		}
	}

	private void breathlizerOkPanelFocusGained(java.awt.event.FocusEvent evt) {
		setWhiteBackColor();
		breathlizerOkPanel.setBackground(UIConstant.focusPanelColor);
	}

	private void breathlizerOkPanelFocusLost(java.awt.event.FocusEvent evt) {
		setWhiteBackColor();
		checkQuestions();
	}

	private void transporterKeyPressed(java.awt.event.KeyEvent evt) {
		if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
			setWhiteBackColor();
			seatBeltPanel.requestFocusInWindow();
			seatBeltPanel.setBackground(UIConstant.focusPanelColor);
			playSeatBeltWormVoice();
			// if (!isSeviceRunning && isMorphoExist) {
			// fingurePrintCaptureService.start();
			// isSeviceRunning = true;
			// }
			startFingerCaptureService();
		}
	}

	private void transporterActionPerformed(java.awt.event.ActionEvent evt) {
		// TODO add your handling code here:
	}

	// TODO add your handling code here:
	private void transporterMousePressed(java.awt.event.MouseEvent evt) {
	}

	private void transporterMouseClicked(java.awt.event.MouseEvent evt) {

		setWhiteBackColor();
		// transporter.setBackground(UIConstant.focusPanelColor);
		// sealOkPanel.setBackground(UIConstant.PanelWhite);
		// tarpaulinPanel.setBackground(UIConstant.PanelWhite);
		// numberVisiblePanel.setBackground(UIConstant.PanelWhite);
		// tailLightPanel.setBackground(UIConstant.PanelWhite);
		// sideMirrorPanel.setBackground(UIConstant.PanelWhite);
		// sideIndicatorPanel.setBackground(UIConstant.PanelWhite);
		// seatBeltPanel.setBackground(UIConstant.PanelWhite);
		// headLightPanel.setBackground(UIConstant.PanelWhite);
		// reverseHornPanel.setBackground(UIConstant.PanelWhite);
		// breathlizerOkPanel.setBackground(UIConstant.PanelWhite);
	}

	private void reverseHornYesKeyPressed(java.awt.event.KeyEvent evt) {
		// TODO add your handling code here:
	}

	private void reverseHornNoKeyPressed(java.awt.event.KeyEvent evt) {
		// TODO add your handling code here:
	}

	private void reverseHornNCKeyPressed(java.awt.event.KeyEvent evt) {
		// TODO add your handling code here:
	}

	private void driverIdFocusGained(java.awt.event.FocusEvent evt) {
		setWhiteBackColor();
		driverId.setBackground(UIConstant.focusPanelColor);
	}

	private void driverIdFocusLost(java.awt.event.FocusEvent evt) {
		setWhiteBackColor();
	}

	private void driverIdKeyPressed(java.awt.event.KeyEvent evt) {
		Connection conn = null;
		boolean destroyIt = false;
		try {
			conn = DBConnectionPool.getConnectionFromPoolNonWeb();
			if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
				if (Utils.isNull(driverId.getText())) {
					JOptionPane.showMessageDialog(null,
							"Please Enter Driver Id !!!");
					return;
				} else {
					driverName.setText("");
					photo.setIcon(null);
					dlNo.setText("");
					helperOk.clearSelection();
					try {
						driverInformation = GateInDao.getDriverDetailByDL(conn,
								driverId.getText(), "DriverId");
						if (driverInformation != null) {
							driverInformation.setIsFingerCaptured(0);
							// changeQuickCreateToOpenGate();
							setDriverHour(conn, driverInformation
									.getDriver_id());
							populateDriver(conn, driverInformation, false,
									false);
							// DriverDetail.showDriverDetail(driverInformation);
							driverSrc = Constant.DRIVER_SOURCE_GATE_PASS_ID;
							/*
							 * if (manualEntryButton.isEnabled()) {
							 * manualEntryButton.requestFocusInWindow(); } else
							 * { quickCreate.requestFocusInWindow(); }
							 */
							setFocusOnButton();
							driverName.setText(driverInformation
									.getDriver_name());
						} else {
							String msg = "Driver Detail Not Exist, Do you want to Re-Enter?";
							Object[] options = { "  Yes  ", "  No  " };// new
							// javax.swing.JFrame()
							// JOptionPane.showMessageDialog(null,
							// "Driver not Exist please enter driver name");
							int responseVehicleDialog = ConfirmationDialog
									.getDialogBox(new java.awt.Frame(), true,
											options, msg);
							System.out.print("responseVehicleDialog : "
									+ responseVehicleDialog);
							if (responseVehicleDialog == 1) {
								isFingerCaptured = false;
								isFingerVerified = false;
								driverId.setFocusable(false);
								dlNo.setFocusable(true);
								dlNo.setEditable(true);

								dlNo.setBorder(javax.swing.BorderFactory
										.createLineBorder(new java.awt.Color(0,
												0, 0)));
								dlNo.requestFocusInWindow();
								// dlNo.setFocusable(true);
								driverId.setText("");
								// driverId.setBackground(UIConstant.PanelWhite);
							} else {
								System.out
										.print("responseVehicleDialog not 1: "
												+ responseVehicleDialog);
								driverId.requestFocusInWindow();
							}
						}

					} catch (IOException ex) {
						Logger.getLogger(CoalGateInWindow.class.getName()).log(
								Level.SEVERE, null, ex);
					} catch (Exception ex) {
						Logger.getLogger(CoalGateInWindow.class.getName()).log(
								Level.SEVERE, null, ex);
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

	private void driverIdKeyReleased(java.awt.event.KeyEvent evt) {
		// TODO add your handling code here:
	}

	private void dlNoActionPerformed(java.awt.event.ActionEvent evt) {
		// TODO add your handling code here:
	}

	private void captureButtonKeyPressed(java.awt.event.KeyEvent evt) {
		if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
			captureButtonAction();
		}
	}

	private void captureButtonFocusGained(java.awt.event.FocusEvent evt) {
		setWhiteBackColor();
	}

	private void clearButtonFocusGained(java.awt.event.FocusEvent evt) {
		setWhiteBackColor();
	}

	private void manualEntryButtonFocusGained(java.awt.event.FocusEvent evt) {
		setWhiteBackColor();
	}

	private void saveButtonFocusGained(java.awt.event.FocusEvent evt) {
		setWhiteBackColor();
	}

	private void driverIdActionPerformed(java.awt.event.ActionEvent evt) {
		// TODO add your handling code here:
	}

	private void clearButtonKeyPressed(java.awt.event.KeyEvent evt) {
		if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
			clearAction();
		}
	}

	private void saveButtonKeyPressed(java.awt.event.KeyEvent evt) {
		if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
			quickCreateAndSave();
		}
	}

	private void rightSideIndicatorYesActionPerformed(
			java.awt.event.ActionEvent evt) {
		rightSideIndicatorAction();
		if (doCheck) {
			checkQuestions();
		}
	}

	private void rightSideIndicatorAction() {
		// setWhiteBackColor();
		// breathlizerOkPanel.setBackground(UIConstant.focusPanelColor);
		breathlizerOkPanel.requestFocusInWindow();
	}

	private void rightSideIndicatorNoActionPerformed(
			java.awt.event.ActionEvent evt) {
		rightSideIndicatorAction();
		if (doCheck) {
			checkQuestions();
		}
	}

	private void rightSideIndicatorNCActionPerformed(
			java.awt.event.ActionEvent evt) {
		rightSideIndicatorAction();
		if (doCheck) {
			checkQuestions();
		}
	}

	private void rightSideIndicatorPanelMouseClicked(
			java.awt.event.MouseEvent evt) {
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
	}

	private void rightSideIndicatorPanelFocusGained(
			java.awt.event.FocusEvent evt) {
		WaveFormPlayer.playSoundIn(Status.TPRQuestion.rightSideIndicator);
		setWhiteBackColor();
		rightSideIndicatorPanel.setBackground(UIConstant.focusPanelColor);
	}

	private void rightSideIndicatorPanelFocusLost(java.awt.event.FocusEvent evt) {
		WaveFormPlayer.playSoundIn(0);
		setWhiteBackColor();
		if (doCheck) {
			checkQuestions();
		}
	}

	private void rightSideIndicatorPanelKeyPressed(java.awt.event.KeyEvent evt) {
		if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
			// setWhiteBackColor();
			breathlizerOkPanel.requestFocusInWindow();
			// breathlizerOkPanel.setBackground(UIConstant.focusPanelColor);
			if (doCheck) {
				checkQuestions();
			}
		} else if (evt.getKeyCode() == KeyEvent.VK_1
				|| evt.getKeyCode() == KeyEvent.VK_NUMPAD1) {
			rightSideIndicatorYes.setSelected(true);
			// setWhiteBackColor();
			breathlizerOkPanel.requestFocusInWindow();
			// breathlizerOkPanel.setBackground(UIConstant.focusPanelColor);
			if (doCheck) {
				checkQuestions();
			}
		} else if (evt.getKeyCode() == KeyEvent.VK_2
				|| evt.getKeyCode() == KeyEvent.VK_NUMPAD2) {
			rightSideIndicatorNo.setSelected(true);
			// setWhiteBackColor();
			breathlizerOkPanel.requestFocusInWindow();
			// breathlizerOkPanel.setBackground(UIConstant.focusPanelColor);
			if (doCheck) {
				checkQuestions();
			}
		} else if (evt.getKeyCode() == KeyEvent.VK_3
				|| evt.getKeyCode() == KeyEvent.VK_NUMPAD3) {
			rightSideIndicatorNC.setSelected(true);
			// setWhiteBackColor();
			breathlizerOkPanel.requestFocusInWindow();
			// breathlizerOkPanel.setBackground(UIConstant.focusPanelColor);
			if (doCheck) {
				checkQuestions();
			}
		}
	}

	private void tarpaulinNoActionPerformed(java.awt.event.ActionEvent evt) {
		setWhiteBackColor();
		sealOkPanel.setBackground(UIConstant.focusPanelColor);
		sealOkPanel.requestFocusInWindow();

	}

	private void tarpaulinNoKeyPressed(java.awt.event.KeyEvent evt) {
		// TODO add your handling code here:
	}

	private void tarpaulinYesActionPerformed(java.awt.event.ActionEvent evt) {
		tarpaulinAction();
		if (doCheck) {
			checkQuestions();
		}
	}

	private void tarpaulinAction() {
		// setWhiteBackColor();
		// sealOkPanel.setBackground(UIConstant.focusPanelColor);
		sealOkPanel.requestFocusInWindow();

	}

	private void tarpaulinYesKeyPressed(java.awt.event.KeyEvent evt) {
		// TODO add your handling code here:
	}

	private void tarpaulinNCActionPerformed(java.awt.event.ActionEvent evt) {
		setWhiteBackColor();
		sealOkPanel.setBackground(UIConstant.focusPanelColor);
		sealOkPanel.requestFocusInWindow();
	}

	private void tarpaulinPanelMouseClicked(java.awt.event.MouseEvent evt) {
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
	}

	private void tarpaulinPanelFocusGained(java.awt.event.FocusEvent evt) {
		setWhiteBackColor();
		tarpaulinPanel.setBackground(UIConstant.focusPanelColor);
	}

	private void tarpaulinPanelFocusLost(java.awt.event.FocusEvent evt) {
		setWhiteBackColor();
		if (doCheck) {
			checkQuestions();
		}
	}

	private void tarpaulinPanelKeyPressed(java.awt.event.KeyEvent evt) {
		if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
			sealOkPanel.requestFocusInWindow();
			// sealOkPanel.setBackground(UIConstant.focusPanelColor);
			if (doCheck) {
				checkQuestions();
			}
		} else if (evt.getKeyCode() == KeyEvent.VK_1
				|| evt.getKeyCode() == KeyEvent.VK_NUMPAD1) {
			tarpaulinYes.setSelected(true);
			// tarpaulinPanel.setBackground(Color.WHITE);
			// sealOkPanel.setBackground(UIConstant.focusPanelColor);
			sealOkPanel.requestFocusInWindow();
			if (doCheck) {
				checkQuestions();
			}
		} else if (evt.getKeyCode() == KeyEvent.VK_2
				|| evt.getKeyCode() == KeyEvent.VK_NUMPAD2) {
			// tarpaulinPanel.setBackground(Color.WHITE);
			tarpaulinNo.setSelected(true);
			// sealOkPanel.setBackground(UIConstant.focusPanelColor);
			sealOkPanel.requestFocusInWindow();
			if (doCheck) {
				checkQuestions();
			}
		} else if (evt.getKeyCode() == KeyEvent.VK_3
				|| evt.getKeyCode() == KeyEvent.VK_NUMPAD3) {
			// tarpaulinPanel.setBackground(Color.WHITE);
			tarpaulinNC.setSelected(true);
			// sealOkPanel.setBackground(UIConstant.focusPanelColor);
			sealOkPanel.requestFocusInWindow();
			if (doCheck) {
				checkQuestions();
			}
		}

	}

	private void sealYesActionPerformed(java.awt.event.ActionEvent evt) {
		sealOkAction();
		if (doCheck) {
			checkQuestions();
		}
	}

	private void sealOkAction() {
		// TODO add your handling code here:
		// setWhiteBackColor();
		// numberVisiblePanel.setBackground(UIConstant.focusPanelColor);
		numberVisiblePanel.requestFocusInWindow();
	}

	private void sealNoActionPerformed(java.awt.event.ActionEvent evt) {
		sealOkAction();
		if (doCheck) {
			checkQuestions();
		}
	}

	private void sealNCActionPerformed(java.awt.event.ActionEvent evt) {
		sealOkAction();
		if (doCheck) {
			checkQuestions();
		}
	}

	private void sealOkPanelMouseClicked(java.awt.event.MouseEvent evt) {
		// setWhiteBackColor();
		sealOkPanel.requestFocusInWindow();
		// sealOkPanel.setBackground(UIConstant.focusPanelColor);
	}

	private void sealOkPanelFocusGained(java.awt.event.FocusEvent evt) {
		setWhiteBackColor();
		sealOkPanel.setBackground(UIConstant.focusPanelColor);
	}

	private void sealOkPanelFocusLost(java.awt.event.FocusEvent evt) {
		setWhiteBackColor();
		if (doCheck) {
			checkQuestions();
		}

	}

	private void sealOkPanelKeyPressed(java.awt.event.KeyEvent evt) {
		if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
			// setWhiteBackColor();
			numberVisiblePanel.requestFocusInWindow();
			// numberVisiblePanel.setBackground(UIConstant.focusPanelColor);
			if (doCheck) {
				checkQuestions();
			}
		} else if (evt.getKeyCode() == KeyEvent.VK_1
				|| evt.getKeyCode() == KeyEvent.VK_NUMPAD1) {
			// setWhiteBackColor();
			sealYes.setSelected(true);
			// numberVisiblePanel.setBackground(UIConstant.focusPanelColor);
			numberVisiblePanel.requestFocusInWindow();
			if (doCheck) {
				checkQuestions();
			}
		} else if (evt.getKeyCode() == KeyEvent.VK_2
				|| evt.getKeyCode() == KeyEvent.VK_NUMPAD2) {
			// setWhiteBackColor();
			sealNo.setSelected(true);
			// numberVisiblePanel.setBackground(UIConstant.focusPanelColor);
			numberVisiblePanel.requestFocusInWindow();
			if (doCheck) {
				checkQuestions();
			}
		} else if (evt.getKeyCode() == KeyEvent.VK_3
				|| evt.getKeyCode() == KeyEvent.VK_NUMPAD3) {
			// setWhiteBackColor();
			sealNC.setSelected(true);
			// numberVisiblePanel.setBackground(UIConstant.focusPanelColor);
			numberVisiblePanel.requestFocusInWindow();
			if (doCheck) {
				checkQuestions();
			}
		}
	}

	private void transporterFocusLost(java.awt.event.FocusEvent evt) {
	}

	private void breathlizerNCActionPerformed(java.awt.event.ActionEvent evt) {
		setWhiteBackColor();
		/*
		 * if
		 * (TPRBlockStatusHelper.useForBlocking(Status.TPRQuestion.breathLyzerOk
		 * )) { enableDenyEntry(false); }
		 */
		setQuetionsBlocking(Status.TPRQuestion.breathLyzerOk, UIConstant.NO);
		checkQuestions();
	}

	private void manualEntryButtonKeyPressed(java.awt.event.KeyEvent evt) {
		if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
			manualEntryAction();
		} // TODO add your handling code here:
	}

	private void transporterFocusGained(java.awt.event.FocusEvent evt) {
		setWhiteBackColor();
	}

	private void vehicleNameFocusGained(java.awt.event.FocusEvent evt) {
		setWhiteBackColor();
		vehicleName.setBackground(UIConstant.focusPanelColor);
	}

	private void vehicleNameKeyPressed(java.awt.event.KeyEvent evt) {
		if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
			vehicleNameAction();
		}
	}

	void clearInput(boolean clearToken, Connection conn) {
		changeIntoDefault(clearToken, conn);
	}

	// void clearInput() {
	// setWhiteBackColor();
	// driverId.setText("");
	// dlNo.setEditable(false);
	// dlNo.setFocusable(false);
	// dlNo.setBorder(null);
	//
	// driverName.setEditable(false);
	// driverName.setFocusable(false);
	// driverName.setBorder(null);
	// captureButton.setVisible(true);
	// vehicleName.setFocusable(false);
	// vehicleName.setEditable(false);
	// // vehicleName.setBackground(UIConstant.PanelWhite);
	// vehicleName.setBorder(null);
	// // vehicleName.setText("NO VEHICLE DETECTED");
	// overrides.setText("");
	// transporter.setFocusable(false);
	// transporter.setEnabled(false);
	// transporter.setSelectedIndex(0);
	// driverName.setText("");
	// dlNo.setText("");
	// photo.setIcon(null);
	//
	// reverseHornOk.clearSelection();
	// headLightOk.clearSelection();
	// numberVisible.clearSelection();
	// tailLightOk.clearSelection();
	// sideMirrorOk.clearSelection();
	// leftSideIndicator.clearSelection();
	// rightSideIndicator.clearSelection();
	// seatBelt.clearSelection();
	// reverseHornOk.clearSelection();
	// breathlizerOk.clearSelection();
	// tarpaulin.clearSelection();
	// sealOk.clearSelection();
	// helperOk.clearSelection();
	// transporter.setFocusable(false);
	// transporter.setEnabled(false);
	// quickCreate.setText("Quick Create");
	// }
	/**
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String args[]) {
		/* Set the Nimbus look and feel */
		// <editor-fold defaultstate="collapsed"
		// desc=" Look and feel setting code (optional) ">
		/*
		 * If Nimbus (introduced in Java SE 6) is not available, stay with the
		 * default look and feel. For details see
		 * http://download.oracle.com/javase
		 * /tutorial/uiswing/lookandfeel/plaf.html
		 */
		try {
			for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager
					.getInstalledLookAndFeels()) {
				if ("Nimbus".equals(info.getName())) {
					javax.swing.UIManager.setLookAndFeel(info.getClassName());
					break;
				}
			}
		} catch (ClassNotFoundException ex) {
			java.util.logging.Logger.getLogger(
					GateInFlyAshWindow.class.getName()).log(
					java.util.logging.Level.SEVERE, null, ex);
		} catch (InstantiationException ex) {
			java.util.logging.Logger.getLogger(
					GateInFlyAshWindow.class.getName()).log(
					java.util.logging.Level.SEVERE, null, ex);
		} catch (IllegalAccessException ex) {
			java.util.logging.Logger.getLogger(
					GateInFlyAshWindow.class.getName()).log(
					java.util.logging.Level.SEVERE, null, ex);
		} catch (javax.swing.UnsupportedLookAndFeelException ex) {
			java.util.logging.Logger.getLogger(
					GateInFlyAshWindow.class.getName()).log(
					java.util.logging.Level.SEVERE, null, ex);
		}
		// </editor-fold>

		/* Create and display the form */
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					new GateInFlyAshWindow().setVisible(true);
				} catch (IOException ex) {
					Logger.getLogger(GateInFlyAshWindow.class.getName()).log(
							Level.SEVERE, null, ex);
				}
			}
		});
	}

	// Variables declaration - do not modify
	private javax.swing.JPanel HelperPanel;
	private javax.swing.JLabel blocking_reason;
	private javax.swing.JCheckBox breathlizerNC;
	private javax.swing.JCheckBox breathlizerNo;
	private javax.swing.JPanel breathlizerOkPanel;
	private javax.swing.JCheckBox breathlizerYes;
	private java.awt.Button button1;
	public static javax.swing.JButton captureButton;
	private javax.swing.JButton clearButton;
	public static javax.swing.JLabel digitalClock;
	public static javax.swing.JTextField dlNo;
	private javax.swing.JLabel driverHours;
	public static javax.swing.JTextField driverId;
	public static javax.swing.JTextField driverName;
	private javax.swing.Box.Filler filler1;
	private javax.swing.Box.Filler filler2;
	private javax.swing.Box.Filler filler3;
	private javax.swing.JLabel fingerInstruction;
	private java.awt.Panel fingerPrintPanel;
	private javax.swing.JLabel gpsLocation;
	private javax.swing.JLabel gpsTime;
	private javax.swing.JCheckBox headLightNC;
	private javax.swing.JCheckBox headLightNo;
	private javax.swing.JPanel headLightPanel;
	private javax.swing.JCheckBox headLightYes;
	public static javax.swing.JCheckBox helperNo;
	public static javax.swing.JCheckBox helperYes;
	private javax.swing.JLabel jLabel1;
	private javax.swing.JLabel jLabel10;
	private javax.swing.JLabel jLabel11;
	private javax.swing.JLabel jLabel12;
	private javax.swing.JLabel jLabel15;
	private javax.swing.JLabel jLabel2;
	private javax.swing.JLabel jLabel23;
	private javax.swing.JLabel jLabel24;
	private javax.swing.JLabel jLabel25;
	private javax.swing.JLabel jLabel26;
	private javax.swing.JLabel jLabel27;
	private javax.swing.JLabel jLabel3;
	private javax.swing.JLabel jLabel4;
	private javax.swing.JLabel jLabel5;
	private javax.swing.JLabel jLabel6;
	private javax.swing.JLabel jLabel7;
	private javax.swing.JLabel jLabel8;
	private javax.swing.JLabel jLabel84;
	private javax.swing.JLabel jLabel85;
	private javax.swing.JLabel jLabel86;
	private javax.swing.JLabel jLabel87;
	private javax.swing.JLabel jLabel88;
	private javax.swing.JLabel jLabel89;
	private javax.swing.JLabel jLabel9;
	private javax.swing.JLabel jLabel97;
	private javax.swing.JPanel jPanel1;
	private javax.swing.JPanel jPanel2;
	private javax.swing.JPanel jPanel3;
	private javax.swing.JPanel jPanel4;
	private javax.swing.JPanel jPanel5;
	private javax.swing.JPanel jPanel6;
	private javax.swing.JPanel jPanel7;
	private javax.swing.JPanel jPanel8;
	private javax.swing.JLabel lblCurrentImageInfo;
	private javax.swing.JLabel lblInstruction;
	private javax.swing.JLabel lblScore;
	private javax.swing.JLabel lblSteps;
	private javax.swing.JCheckBox leftSideIndicatorNC;
	private javax.swing.JCheckBox leftSideIndicatorNo;
	private javax.swing.JPanel leftSideIndicatorPanel;
	private javax.swing.JCheckBox leftSideIndicatorYes;
	private javax.swing.JButton manualEntryButton;
	private javax.swing.JButton manualSync;
	private javax.swing.JCheckBox numberVisibleNC;
	private javax.swing.JCheckBox numberVisibleNo;
	private javax.swing.JPanel numberVisiblePanel;
	private javax.swing.JCheckBox numberVisibleYes;
	private javax.swing.JTextField overrides;
	public static javax.swing.JLabel paperValid;
	public static javax.swing.JLabel photo;
	private javax.swing.JProgressBar progressBar;
	private javax.swing.JCheckBox reverseHornNC;
	private javax.swing.JCheckBox reverseHornNo;
	private javax.swing.JPanel reverseHornPanel;
	private javax.swing.JCheckBox reverseHornYes;
	private javax.swing.JCheckBox rightSideIndicatorNC;
	private javax.swing.JCheckBox rightSideIndicatorNo;
	private javax.swing.JPanel rightSideIndicatorPanel;
	private javax.swing.JCheckBox rightSideIndicatorYes;
	public static javax.swing.JButton saveButton;
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
	private javax.swing.JCheckBox tailLightNC;
	private javax.swing.JCheckBox tailLightNo;
	private javax.swing.JPanel tailLightPanel;
	private javax.swing.JCheckBox tailLightYes;
	private javax.swing.JCheckBox tarpaulinNC;
	private javax.swing.JCheckBox tarpaulinNo;
	private javax.swing.JPanel tarpaulinPanel;
	private javax.swing.JCheckBox tarpaulinYes;
	public static javax.swing.JComboBox transporter;
	public static javax.swing.JLabel username;
	private javax.swing.JComboBox vehicleName;
	private Vehicle vehicleDetail = null;
	

	// End of variables declaration

	private void changeToCreateMode() {
		setWhiteBackColor();
		toggleVehicle(true);
	}

	private void updateTPR(Connection conn, int nextWorkStation)
			throws Exception {
		updateTPR(conn, nextWorkStation, false);
	}

	private void updateTPR(Connection conn, int nextWorkStation, boolean isDeny)
			throws Exception {
		java.util.Date curr = new java.util.Date();
		if (!isDeny) {
			// tprRecord.setVehicleName(vehicleName.getText());
			tprRecord.setDriverName(driverInformation.getDriver_name());
			tprRecord.setDlNo(driverInformation.getDl_no());
			tprRecord.setDriverId(driverInformation.getDriver_id());
			tprRecord.setPreStepType(TokenManager.currWorkStationType);
			tprRecord.setPrevTpStep(TokenManager.currWorkStationId);
			tprRecord.setPreStepDate(curr);
			tprRecord.setUpdatedBy(TokenManager.userId);
			tprRecord.setUpdatedOn(curr);
			tprRecord.setLoadGateInName(TokenManager.userName);
			if (transporter.isEnabled()) {
				tprRecord.setTransporterId(DropDownValues
						.getComboSelectedVal(transporter));
			}
			tprRecord.setNextStepType(nextWorkStation);
			tprRecord.setComboEnd(new Date());
			if (TokenManager.closeTPR) {
				tprRecord.setTprStatus(Status.TPR.CLOSE);
				if (token.getEpcId() != null && token.getEpcId().length() >= 20
						&& rfidHandler != null) {
					rfidHandler.clearData(Utils.HexStringToByteArray(token
							.getEpcId()), 5);
				}
			}

			tprRecord.setLatestLoadGateInExit(new Date());
			tprRecord.setDriverSrc(driverSrc);
		}

		if (tprRecord.getComboStart() == null) {
			tprRecord.setComboStart(curr);
		}
		tprRecord.setIsNewVehicle(isNewVehicle);
		tprRecord.setEarliestLoadGateInEntry(entryTime);

		TPRInformation.insertUpdateTpr(conn, tprRecord);
		if (tprBlockManager != null) {
			tprBlockManager.setTprBlockStatus(conn, tprRecord.getTprId(),
					TokenManager.userId);
		}
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
			// tpStep.setHasValidRf(isTagRead ? 1 : 0);
			// RFIDMasterDao.insert(conn, tpStep);
			tpStep.setHasValidRf(isTagRead ? 1 : 0);
			tpStep.setMaterialCat(TokenManager.materialCat);
			tpStep.setSaveStatus(isDeny ? TPStep.REQUEST_OVERRIDE
					: TPStep.SAVE_AND_CONTINUE);
			RFIDMasterDao.insert(conn, tpStep, false);
			RFIDMasterDao.insert(conn, tpStep, true);

		} else {
			// tpStep.setTprId(tprRecord.getTprId());
			long currTimeServerMillis = System.currentTimeMillis();
			tpStep.setExitTime(new Date(currTimeServerMillis));
			tpStep.setUpdatedOn(new Date(currTimeServerMillis));
			tpStep.setHasValidRf(isTagRead ? 1 : 0);
			tpStep.setMaterialCat(TokenManager.materialCat);
			tpStep.setSaveStatus(isDeny ? TPStep.REQUEST_OVERRIDE
					: TPStep.SAVE_AND_CONTINUE);
			RFIDMasterDao.update(conn, tpStep, false);
			RFIDMasterDao.update(conn, tpStep, true);
		}

		return tpStep.getId();
	}

	private void insertRegistrationStatus(Connection invConn) {/*
																 * regisBean =
																 * new
																 * RegistrationStatus
																 * (); if
																 * (isTagRead) {
																 * regisBean
																 * .setTag_info
																 * (1); } else {
																 * regisBean
																 * .setTag_info
																 * (0); }
																 * 
																 * if
																 * (isVehicleExist
																 * ) {
																 * regisBean.
																 * setVehicle_info
																 * (1); } else {
																 * regisBean
																 * .setVehicle_info
																 * (0); } if
																 * (isFingerCaptured
																 * ) {
																 * regisBean.
																 * setDriver_info
																 * (1); } else {
																 * regisBean
																 * .setDriver_info
																 * (0); }
																 * regisBean
																 * .setTpr_id
																 * (tprRecord
																 * .getTprId());
																 * if
																 * (Utils.isNull
																 * (tprRecord.
																 * getChallanNo
																 * ())) {
																 * regisBean.
																 * setChallan_record_info
																 * (0); } else {
																 * regisBean.
																 * setChallan_record_info
																 * (1); }
																 * 
																 * regisBean.
																 * setMultiple_tpr_info
																 * (0);
																 * regisBean
																 * .setDriver_id
																 * (
																 * driverInformation
																 * .
																 * getDriver_id(
																 * ));
																 * regisBean.
																 * setVehicle_id
																 * (tprRecord.
																 * getVehicleId
																 * ());
																 * 
																 * GateInDao.
																 * InsertIntoRegistrationStatus
																 * (invConn,
																 * regisBean);
																 */

	}

	private String getInstruction(Connection conn) {
		StringBuilder instructionString = null;
		if (tprBlockManager != null
				&& tprBlockManager
						.useForBlocking(Status.TPRQuestion.isFingerCaptured)
				&& !isFingerCaptured) {
			if (instructionString == null) {
				instructionString = new StringBuilder();
			}
			instructionString.append("Finger Not Captured.");
		}
		if (tprBlockManager != null
				&& tprBlockManager
						.useForBlocking(Status.TPRQuestion.isFingerVerified)
				&& isFingerCaptured && !isFingerVerified) {
			if (instructionString == null) {
				instructionString = new StringBuilder();
			} else {
				instructionString.append("\n");
			}
			instructionString.append("Finger Not Verified.");
		}
		if (tprBlockManager != null
				&& tprBlockManager
						.useForBlocking(Status.TPRQuestion.isDriverExist)
				&& !isDriverExist) {
			if (instructionString == null)
				instructionString = new StringBuilder();
			else
				instructionString.append("\n");
			instructionString.append("Driver Not Reg.");
		}
		if (tprBlockManager != null
				&& tprBlockManager
						.useForBlocking(Status.TPRQuestion.isFingerExist)
				&& !isFingerExist) {
			if (instructionString == null)
				instructionString = new StringBuilder();
			else
				instructionString.append("\n");
			instructionString.append("Driver Finger Not Reg.");
		}
		if (tprBlockManager != null
				&& tprBlockManager
						.useForBlocking(Status.TPRQuestion.isDriverBlacklisted)
				&& isDriverBlacklisted) {
			if (instructionString == null)
				instructionString = new StringBuilder();
			else
				instructionString.append("\n");
			instructionString.append("Driver Blacklisted.");
		}
		if (tprBlockManager != null
				&& tprBlockManager.useForBlocking(Status.TPRQuestion.isTagRead)
				&& !isTagRead) {
			if (instructionString == null) {
				instructionString = new StringBuilder();
			} else {
				instructionString.append("\n");
			}
			instructionString.append("RF Not Working.");
		}
		if (tprBlockManager != null
				&& tprBlockManager
						.useForBlocking(Status.TPRQuestion.isVehicleExist)
				&& !isVehicleExist) {
			if (instructionString == null)
				instructionString = new StringBuilder();
			else
				instructionString.append("\n");
			instructionString.append("Vehicle Not Exist.");
		}
		if (tprBlockManager != null
				&& tprBlockManager
						.useForBlocking(Status.TPRQuestion.breathLyzerOk)
				&& breathlizerNo.isSelected()) {
			if (instructionString == null) {
				instructionString = new StringBuilder();
			} else {
				instructionString.append("\n");
			}
			instructionString.append("Driver fails Breathlyzer Test.");
		}
		if (tprBlockManager != null
				&& tprBlockManager
						.useForBlocking(Status.TPRQuestion.isChallanExist)
				&& Utils.isNull(tprRecord.getChallanNo())) {
			if (instructionString == null) {
				instructionString = new StringBuilder();
			} else {
				instructionString.append("\n");
			}
			instructionString.append("Challan Data Missing.");
		}
		if (tprBlockManager != null
				&& (tprBlockManager
						.useForBlocking(Status.TPRQuestion.isFitnessOk)
						|| tprBlockManager
								.useForBlocking(Status.TPRQuestion.isRoadPermitOk)
						|| tprBlockManager
								.useForBlocking(Status.TPRQuestion.isInsuranceOk) || tprBlockManager
						.useForBlocking(Status.TPRQuestion.isPolutionOk))
				&& !isPaperOk) {
			if (instructionString == null) {
				instructionString = new StringBuilder();
			} else {
				instructionString.append("\n");
			}
			instructionString.append("Paper Not Valid.");
		}
		return instructionString == null || instructionString.length() == 0 ? null
				: instructionString.toString() + "\n GOTO Registration.";
	}

	private void changeQuickCreateToOpenGate() {
		System.out.println("text Change");
		if (tprRecord != null) {
			saveButton.setText("Save And Open Gate");
		} else {
			saveButton.setText("Quick Create");
		}
	}

	void setWhiteBackColor() {
		vehicleName.setBackground(UIConstant.PanelWhite);
		seatBeltPanel.setBackground(UIConstant.PanelWhite);
		tarpaulinPanel.setBackground(UIConstant.PanelWhite);
		sealOkPanel.setBackground(UIConstant.PanelWhite);
		numberVisiblePanel.setBackground(UIConstant.PanelWhite);
		sideMirrorPanel.setBackground(UIConstant.PanelWhite);
		reverseHornPanel.setBackground(UIConstant.PanelWhite);
		tailLightPanel.setBackground(UIConstant.PanelWhite);
		headLightPanel.setBackground(UIConstant.PanelWhite);
		leftSideIndicatorPanel.setBackground(UIConstant.PanelWhite);
		rightSideIndicatorPanel.setBackground(UIConstant.PanelWhite);
		breathlizerOkPanel.setBackground(UIConstant.PanelWhite);

		dlNo.setBackground(UIConstant.PanelWhite);
		driverName.setBackground(UIConstant.PanelWhite);
		driverId.setBackground(UIConstant.PanelWhite);
	}

	// private void initConnection() {
	// try {
	// conn = DBConnectionPool.getConnectionFromPoolNonWeb();
	// dbConnectionRFID = DBConnectionPool.getConnectionFromPoolNonWeb();
	// fingurePrintConn = DBConnectionPool.getConnectionFromPoolNonWeb();
	// morphoSyncConn = DBConnectionPool.getConnectionFromPoolNonWeb();
	// } catch (GenericException ex) {
	// // Logger.getLogger(GateIn1st.class.getName()).log(Level.SEVERE, null,
	// ex);
	// ex.printStackTrace();
	// }
	// }
	// private void insertRegistrationStatus() {
	// regisBean = new RegistrationStatus();
	// // regisBean.setTag_info(isTagRead);
	// if (isVehicleExist) {
	// regisBean.setVehicle_info(1);
	// } else {
	// regisBean.setVehicle_info(0);
	// }
	// if (isFingerCaptured) {
	// regisBean.setDriver_info(1);
	// } else {
	// regisBean.setDriver_info(0);
	// }
	// regisBean.setTpr_id(tprRecord.getTprId());
	// if (Utils.isNull(tprRecord.getChallanNo())) {
	// regisBean.setChallan_record_info(0);
	// } else {
	// regisBean.setChallan_record_info(1);
	// }
	//
	// regisBean.setMultiple_tpr_info(0);
	// regisBean.setDriver_id(driverInformation.getDriver_id());
	// regisBean.setVehicle_id(tprRecord.getVehicleId());
	// GateInDao.InsertIntoRegistrationStatus(conn, regisBean);
	//
	// }
	// private void quickCreateAndSave() {
	// if (vehicleName.getSelectedText().length() != 0) {
	// // if (!vehicleName.isEditable()) {
	// // changeToCreateMode();
	// // vehicleName.requestFocusInWindow();
	// // }
	// } else if (transporter.isEnabled() && transporter.getSelectedIndex() ==
	// 0) {
	// JOptionPane.showMessageDialog(null, "Please Select Transporter");
	// setWhiteBackColor();
	// transporter.requestFocusInWindow();
	// return;
	// } else {
	// if (!isTpRecordValid) {
	// JOptionPane.showMessageDialog(null,
	// "Vehicle Not Exist Please go for Registration");
	// return;
	// } else if (!reverseHornNo.isSelected() && !reverseHornYes.isSelected()) {
	// JOptionPane.showMessageDialog(null,
	// "Please Give Answer to All Question");
	// setWhiteBackColor();
	// reverseHornOkPanel.setBackground(UIConstant.focusPanelColor);
	// reverseHornOkPanel.requestFocusInWindow();
	// } else if (!headLightNo.isSelected() && !headLightYes.isSelected()) {
	// JOptionPane.showMessageDialog(null,
	// "Please Give Answer to All Question");
	// headLightOkPanel.requestFocusInWindow();
	// } else if (!numberVisibleNo.isSelected() &&
	// !numberVisibleYes.isSelected()) {
	// JOptionPane.showMessageDialog(null,
	// "Please Give Answer to All Question");
	// setWhiteBackColor();
	// numberVisiblePanel.setBackground(UIConstant.focusPanelColor);
	// numberVisiblePanel.requestFocusInWindow();
	// } else if (!tailLightNo.isSelected() && !tailLightYes.isSelected()) {
	// JOptionPane.showMessageDialog(null,
	// "Please Give Answer to All Question");
	// setWhiteBackColor();
	// tailLightPanel.setBackground(UIConstant.focusPanelColor);
	// tailLightPanel.requestFocusInWindow();
	// } else if (!sideMirrorNo.isSelected() && !sideMirrorYes.isSelected()) {
	// JOptionPane.showMessageDialog(null,
	// "Please Give Answer to All Question");
	// setWhiteBackColor();
	// sideMirrorPanel.setBackground(UIConstant.focusPanelColor);
	// sideMirrorPanel.requestFocusInWindow();
	// } else if (!sideIndicatorNo.isSelected() &&
	// !sideIndicatorYes.isSelected()) {
	// JOptionPane.showMessageDialog(null,
	// "Please Give Answer to All Question");
	// setWhiteBackColor();
	// leftSideIndicatorPanel.setBackground(UIConstant.focusPanelColor);
	// leftSideIndicatorPanel.requestFocusInWindow();
	// } else if (!seatBeltNo.isSelected() && !seatBeltYes.isSelected()) {
	// JOptionPane.showMessageDialog(null,
	// "Please Give Answer to All Question");
	// setWhiteBackColor();
	// seatBeltPanel.setBackground(UIConstant.focusPanelColor);
	// seatBeltPanel.requestFocusInWindow();
	// } else if (!headLightNo.isSelected() && !headLightYes.isSelected()) {
	// JOptionPane.showMessageDialog(null,
	// "Please Give Answer to All Question");
	// setWhiteBackColor();
	// } else if (!reverseHornNo.isSelected() && !reverseHornYes.isSelected()) {
	// JOptionPane.showMessageDialog(null,
	// "Please Give Answer to All Question");
	// setWhiteBackColor();
	// } else if (!breathlizerNo.isSelected() && !breathlizerYes.isSelected()) {
	// JOptionPane.showMessageDialog(null,
	// "Please Give Answer to All Question");
	// setWhiteBackColor();
	// breathlizerOkPanel.setBackground(UIConstant.focusPanelColor);
	// breathlizerOkPanel.requestFocusInWindow();
	// } else if (captureButton.isVisible() && driverInformation == null) {
	// JOptionPane.showMessageDialog(null, "Please Captured Finger Print");
	// return;
	// } else {
	// String instruction = getInstruction();
	//
	// try {
	// // System.out.println("vehicleName : " + vehicleName.getText());
	// System.out.println("vehicle_Id: " + tprRecord.getVehicleId());
	// updateTPR();
	// insertRegistrationStatus();
	// int stepId = InsertTPRStep();
	// boolean isInsert = false;
	// if (stepId != Misc.getUndefInt()) {
	// isInsert = InsertTPRQuestionDetails(stepId);
	// }
	// conn.commit();
	// if (isInsert) {
	// JOptionPane.showMessageDialog(null, "Detail Saved");
	// TokenManager.returnToken(conn, token);
	// closeConnection();
	// token = null;
	// isTpRecordValid = false;
	// isVehicleExist = false;
	// isFingerCaptured = false;
	// isFingerVerified = false;
	// clearInput();
	// getFocus();
	// } else {
	// JOptionPane.showMessageDialog(null, "Detail Not Saved");
	// }
	// if (!Utils.isNull(instruction) && isInsert) {
	// JOptionPane.showMessageDialog(null, instruction);
	// }
	// } catch (Exception ex) {
	// ex.printStackTrace();
	// System.out.println(ex);
	// }
	// }
	// }
	// }
	private void quickCreateAndSave() {
		Connection conn = null;
		boolean destroyIt = false;
		try {
			conn = DBConnectionPool.getConnectionFromPoolNonWeb();
			if (isRequestOverride) {
				requestOverrideAction();
				return;
			}
			if (vehicleName.isEnabled()
					&& Utils.isNull(vehicleName.getSelectedItem().toString())) {
				JOptionPane
						.showMessageDialog(null, "Please Enter Vehicle Name");
				vehicleName.requestFocusInWindow();
				return;
			} else if (transporter.isEnabled()
					&& transporter.getSelectedItem().toString()
							.equalsIgnoreCase("Select")) {
				JOptionPane
						.showMessageDialog(null, "Please Select Transporter");
				setWhiteBackColor();
				transporter.requestFocusInWindow();
				return;
			} else {
				if (!isTpRecordValid) {
					JOptionPane.showMessageDialog(null,
							"Vehicle Not Exist Please go for Registration");
					return;
				} else if (!seatBeltNo.isSelected()
						&& !seatBeltYes.isSelected()
						&& !seatBeltNC.isSelected()) {
					JOptionPane.showMessageDialog(null,
							"Please Give Answer to All Question");
					setWhiteBackColor();
					seatBeltPanel.setBackground(UIConstant.focusPanelColor);
					seatBeltPanel.requestFocusInWindow();
				} else if (!numberVisibleNo.isSelected()
						&& !numberVisibleYes.isSelected()
						&& !numberVisibleNC.isSelected()) {
					JOptionPane.showMessageDialog(null,
							"Please Give Answer to All Question");
					setWhiteBackColor();
					numberVisiblePanel
							.setBackground(UIConstant.focusPanelColor);
					numberVisiblePanel.requestFocusInWindow();
				} else if (!sideMirrorNo.isSelected()
						&& !sideMirrorYes.isSelected()
						&& !sideMirrorNC.isSelected()) {
					JOptionPane.showMessageDialog(null,
							"Please Give Answer to All Question");
					setWhiteBackColor();
					sideMirrorPanel.setBackground(UIConstant.focusPanelColor);
					sideMirrorPanel.requestFocusInWindow();
				} else if (!reverseHornNo.isSelected()
						&& !reverseHornYes.isSelected()
						&& !reverseHornNC.isSelected()) {
					JOptionPane.showMessageDialog(null,
							"Please Give Answer to All Question");
					setWhiteBackColor();
					reverseHornPanel.setBackground(UIConstant.focusPanelColor);
					reverseHornPanel.requestFocusInWindow();
				} else if (!tailLightNo.isSelected()
						&& !tailLightYes.isSelected()
						&& !tailLightNC.isSelected()) {
					JOptionPane.showMessageDialog(null,
							"Please Give Answer to All Question");
					setWhiteBackColor();
					tailLightPanel.setBackground(UIConstant.focusPanelColor);
					tailLightPanel.requestFocusInWindow();
				} else if (!headLightNo.isSelected()
						&& !headLightYes.isSelected()
						&& !headLightNC.isSelected()) {
					JOptionPane.showMessageDialog(null,
							"Please Give Answer to All Question");
					setWhiteBackColor();
					headLightPanel.setBackground(UIConstant.focusPanelColor);
					headLightPanel.requestFocusInWindow();
				} else if (!leftSideIndicatorNo.isSelected()
						&& !leftSideIndicatorYes.isSelected()
						&& !leftSideIndicatorNC.isSelected()) {
					JOptionPane.showMessageDialog(null,
							"Please Give Answer to All Question");
					setWhiteBackColor();
					leftSideIndicatorPanel
							.setBackground(UIConstant.focusPanelColor);
					leftSideIndicatorPanel.requestFocusInWindow();
				} else if (!rightSideIndicatorNo.isSelected()
						&& !rightSideIndicatorYes.isSelected()
						&& !rightSideIndicatorNC.isSelected()) {
					JOptionPane.showMessageDialog(null,
							"Please Give Answer to All Question");
					setWhiteBackColor();
					rightSideIndicatorPanel
							.setBackground(UIConstant.focusPanelColor);
					rightSideIndicatorPanel.requestFocusInWindow();
				} else if (!breathlizerNo.isSelected()
						&& !breathlizerYes.isSelected()
						&& !breathlizerNC.isSelected()) {
					JOptionPane.showMessageDialog(null,
							"Please Give Answer to All Question");
					setWhiteBackColor();
					breathlizerOkPanel
							.setBackground(UIConstant.focusPanelColor);
					breathlizerOkPanel.requestFocusInWindow();
				} else if (captureButton.isVisible()
						&& driverInformation == null) {
					JOptionPane.showMessageDialog(null,
							"Please Fill Driver Details ");
					if (driverId.isEditable()) {
						driverId.requestFocusInWindow();
					} else if (dlNo.isEditable()) {
						dlNo.requestFocusInWindow();
					} else if (driverName.isEditable()) {
						driverName.requestFocusInWindow();
					}
					return;
				} else if (Utils.isNull(driverName.getText())
						|| Utils.isNull(dlNo.getText())) {
					JOptionPane.showMessageDialog(null,
							"Please Fill Driver Details ");
					if (driverId.isEditable()) {
						driverId.requestFocusInWindow();
					} else if (dlNo.isEditable()) {
						dlNo.requestFocusInWindow();
					} else if (driverName.isEditable()) {
						driverName.requestFocusInWindow();
					}
					return;
				} else {
					System.out
							.println("########### Quick Create And Save Start  ##########");
					String instruction = getInstruction(conn);

					try {
						System.out.println("vehicleName : "
								+ vehicleName.getSelectedItem().toString());
						System.out.println("vehicle_Id: "
								+ tprRecord.getVehicleId());
						int nextWorkstationType = instruction == null ? TokenManager.nextWorkStationType
								: com.ipssi.rfid.constant.Type.WorkStationType.REGISTRATION;

						if (nextWorkstationType != WorkStationType.REGISTRATION) {
							vehicleDetail = (Vehicle) RFIDMasterDao.get(conn,
									Vehicle.class, tprRecord.getVehicleId());
							long total_days = DropDownValues
									.getDifferenceBwDate(vehicleDetail
											.getFlyashTareTime());
							if (total_days > TokenManager.maxTareDays) {
								nextWorkstationType = WorkStationType.FLY_ASH_TARE_WT_TYPE;
							}
						}

						updateTPR(conn, nextWorkstationType);
						// insertRegistrationStatus(conn);
						if(debugTemplate != null){
	    					GateInDao.insertTemplate(conn,tprRecord.getTprId(),debugTemplate);
	    				}
						int stepId = InsertTPRStep(conn, false);
						if (!Misc.isUndef(stepId)) {
							InsertTPRQuestionDetails(conn, stepId);
						}
						// TPRBlockStatusHelper.allowCurrentStep(conn, tprRecord
						// != null ? tprRecord.getVehicleId() :
						// Misc.getUndefInt(), tprRecord, Misc.getUndefInt(),
						// TokenManager.currWorkStationType,
						// TokenManager.userId, true, true);
						if (tprBlockManager != null) {
							updateCurrentBlocking(conn);
							tprBlockManager.calculateBlocking(conn);
							setBlockingStatus();
							tprBlockManager.setTprBlockStatus(conn, tprRecord
									.getTprId(), TokenManager.userId);
						}
						conn.commit();
						if (true) {
							System.out
									.println("########### Print Started  ##########");

							System.out.println("IsVehicleExist: "
									+ GateInDao.getValue(isVehicleExist)
									+ " isFingerCaptured: "
									+ ""
									+ GateInDao.getValue(isFingerCaptured)
									+ " isFingerVerified: "
									+ GateInDao.getValue(isFingerVerified)
									+ " isChallanExist"
									+ " "
									+ GateInDao.getValue(!Utils
											.isNull(tprRecord.getChallanNo()))
									+ " isTpRecordValid: " + ""
									+ GateInDao.getValue(isTpRecordValid)
									+ " isTagRead: "
									+ GateInDao.getValue(isTagRead));
							System.out
									.println("## GO To "
											+ com.ipssi.rfid.constant.Type.WorkStationType
													.getString(nextWorkstationType)
											+ " ##");
							// if(TokenManager.Printer_Connected == 1 ){
							DocPrinter.print(tprRecord.getVehicleName(),
									new Date(), tprRecord.getTprId(),
									DropDownValues.getTransporter(tprRecord
											.getTransporterId(), conn),
									tprRecord.getDriverName(), WorkStationType
											.getString(tprRecord
													.getNextStepType()),
									isVehicleExist, isTagRead,
									isFingerCaptured, isFingerVerified, !Utils
											.isNull(tprRecord.getChallanNo()),
									TokenManager.Printer_Connected, null,
									nextWorkstationType, isPaperOk,
									isDriverExist, isFingerExist,
									isDriverBlacklisted, false, gpsStat);

							JOptionPane.showMessageDialog(null, "Detail Saved");
							Barrier.openEntryGate();
							WaveFormPlayer
									.playSoundIn(Status.TPRQuestion.saveDetail);
							fingurePrintCaptureService.stop();
							clearInput(false, conn);
							getFocus();
							System.out
									.println("########### Successfully Saved  ##########");

						}
						if (!Utils.isNull(instruction)) {
							JOptionPane.showMessageDialog(null, instruction);
						} else {
							JOptionPane
									.showMessageDialog(
											null,
											"Go to "
													+ com.ipssi.rfid.constant.Type.WorkStationType
															.getString(nextWorkstationType));
						}
					} catch (Exception ex) {
						JOptionPane.showMessageDialog(null, ex.getMessage());
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

	private void updateCurrentBlocking(Connection conn) {
		if (tprBlockManager == null)
			return;
		HashMap<Integer, Integer> quesAnsList = getQuestionIdList(conn);
		for (Map.Entry<Integer, Integer> entry : quesAnsList.entrySet()) {
			Integer questionId = entry.getKey();
			Integer answerId = entry.getValue();
			setQuestionsBlocking(questionId, answerId);
		}
	}

	private void setQuestionsBlocking(int questionId, int answerId) {
		if (Misc.isUndef(questionId))
			return;
		if (tprBlockManager != null) {
			TPSQuestionDetail tpsQuestionBean = new TPSQuestionDetail();
			tpsQuestionBean.setQuestionId(questionId);
			tpsQuestionBean.setAnswerId(answerId);
			tprBlockManager.addQuestions(tpsQuestionBean);
			setBlockingStatus();
		}
	}

	private HashMap<Integer, Integer> getQuestionIdList(Connection conn) {
		HashMap<Integer, Integer> quesAnsList = new HashMap<Integer, Integer>();

		if (sealYes.isSelected()) {
			quesAnsList.put(Status.TPRQuestion.sealOk, UIConstant.YES);
		} else if (sealNo.isSelected()) {
			quesAnsList.put(Status.TPRQuestion.sealOk, UIConstant.NO);
		} else if (sealNC.isSelected()) {
			quesAnsList.put(Status.TPRQuestion.sealOk, UIConstant.NC);
		} else {
			quesAnsList.put(Status.TPRQuestion.sealOk, Misc.getUndefInt());
		}

		if (tarpaulinYes.isSelected()) {
			quesAnsList.put(Status.TPRQuestion.tarpaulinOk, UIConstant.YES);
		} else if (tarpaulinNo.isSelected()) {
			quesAnsList.put(Status.TPRQuestion.tarpaulinOk, UIConstant.NO);
		} else if (tarpaulinNC.isSelected()) {
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
			quesAnsList.put(Status.TPRQuestion.numberVisible, Misc
					.getUndefInt());
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
			quesAnsList.put(Status.TPRQuestion.leftSideIndicator,
					UIConstant.YES);
		} else if (leftSideIndicatorNo.isSelected()) {
			quesAnsList
					.put(Status.TPRQuestion.leftSideIndicator, UIConstant.NO);
		} else if (leftSideIndicatorNC.isSelected()) {
			quesAnsList
					.put(Status.TPRQuestion.leftSideIndicator, UIConstant.NC);
		} else {
			quesAnsList.put(Status.TPRQuestion.leftSideIndicator, Misc
					.getUndefInt());
		}

		if (seatBeltYes.isSelected()) {
			quesAnsList.put(Status.TPRQuestion.seatBeltWorm, UIConstant.YES);
		} else if (seatBeltNo.isSelected()) {
			quesAnsList.put(Status.TPRQuestion.seatBeltWorm, UIConstant.NO);
		} else if (seatBeltNC.isSelected()) {
			quesAnsList.put(Status.TPRQuestion.seatBeltWorm, UIConstant.NC);
		} else {
			quesAnsList
					.put(Status.TPRQuestion.seatBeltWorm, Misc.getUndefInt());
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
			quesAnsList.put(Status.TPRQuestion.reverseHornOk, Misc
					.getUndefInt());
		}

		if (breathlizerYes.isSelected()) {
			quesAnsList.put(Status.TPRQuestion.breathLyzerOk, UIConstant.YES);
		} else if (breathlizerNo.isSelected()) {
			quesAnsList.put(Status.TPRQuestion.breathLyzerOk, UIConstant.NO);
		} else if (breathlizerNC.isSelected()) {
			quesAnsList.put(Status.TPRQuestion.breathLyzerOk, UIConstant.NC);
		} else {
			quesAnsList.put(Status.TPRQuestion.breathLyzerOk, Misc
					.getUndefInt());
		}

		if (helperYes.isSelected()) {
			quesAnsList.put(Status.TPRQuestion.drivenByHelper, UIConstant.YES);
		} else if (helperNo.isSelected()) {
			quesAnsList.put(Status.TPRQuestion.drivenByHelper, UIConstant.NO);
		} else {
			quesAnsList.put(Status.TPRQuestion.drivenByHelper, Misc
					.getUndefInt());
		}

		if (rightSideIndicatorYes.isSelected()) {
			quesAnsList.put(Status.TPRQuestion.rightSideIndicator,
					UIConstant.YES);
		} else if (rightSideIndicatorNo.isSelected()) {
			quesAnsList.put(Status.TPRQuestion.rightSideIndicator,
					UIConstant.NO);
		} else if (rightSideIndicatorNC.isSelected()) {
			quesAnsList.put(Status.TPRQuestion.rightSideIndicator,
					UIConstant.NC);
		} else {
			quesAnsList.put(Status.TPRQuestion.rightSideIndicator, Misc
					.getUndefInt());
		}
		int blockStatus = Misc.getUndefInt();
		if (tprBlockManager != null)
			blockStatus = tprBlockManager.getBlockStatus();
		if (blockStatus != UIConstant.BLOCKED) {
			quesAnsList.put(Status.TPRQuestion.isTagRead,
					isTagRead ? Results.Questions.YES : Results.Questions.NO);
			// if(isFingerCaptured)
			quesAnsList.put(Status.TPRQuestion.isFingerVerified,
					isFingerVerified ? Results.Questions.YES
							: Results.Questions.NO);
			quesAnsList.put(Status.TPRQuestion.isFingerCaptured,
					isFingerCaptured ? Results.Questions.YES
							: Results.Questions.NO);
			quesAnsList.put(Status.TPRQuestion.isVehicleExist,
					isVehicleExist ? Results.Questions.YES
							: Results.Questions.NO);
			quesAnsList.put(Status.TPRQuestion.isDriverExist,
					isDriverExist ? Results.Questions.YES
							: Results.Questions.NO);
			quesAnsList.put(Status.TPRQuestion.isFingerExist,
					isFingerExist ? Results.Questions.YES
							: Results.Questions.NO);
			quesAnsList.put(Status.TPRQuestion.isDriverBlacklisted,
					isDriverBlacklisted ? UIConstant.NO : UIConstant.YES);

			quesAnsList.put(Status.TPRQuestion.isChallanExist, !Utils
					.isNull(tprRecord.getChallanNo()) ? Results.Questions.YES
					: Results.Questions.NO);
			if (!Misc.isUndef(fitnessOk))
				quesAnsList.put(Status.TPRQuestion.isFitnessOk, fitnessOk);
			if (!Misc.isUndef(roadPermitOk))
				quesAnsList
						.put(Status.TPRQuestion.isRoadPermitOk, roadPermitOk);
			if (!Misc.isUndef(insuranceOk))
				quesAnsList.put(Status.TPRQuestion.isInsuranceOk, insuranceOk);
			if (!Misc.isUndef(polutionOk))
				quesAnsList.put(Status.TPRQuestion.isPolutionOk, polutionOk);

			/*
			 * quesAnsList.put(Status.TPRQuestion.isDriverExist, isDriverExist ?
			 * Results.Questions.YES : Results.Questions.NO);
			 * quesAnsList.put(Status.TPRQuestion.isFingerExist, isFingerExist ?
			 * Results.Questions.YES : Results.Questions.NO);
			 * quesAnsList.put(Status.TPRQuestion.isDriverBlacklisted,
			 * isDriverBlacklisted ? UIConstant.NO : UIConstant.YES);
			 */

			/*
			 * Triple<Integer, Long, Long> driverBlockStatus =
			 * TPRUtils.getDriverBlockStatus(conn, tprRecord.getDriverId());
			 * if(driverBlockStatus != null){ long curr =
			 * System.currentTimeMillis(); boolean isBlock =
			 * !Misc.isUndef(driverBlockStatus.first) &&
			 * !Misc.isUndef(driverBlockStatus.second) && (curr >=
			 * driverBlockStatus.second && (curr <= driverBlockStatus.third ||
			 * Misc.isUndef(driverBlockStatus.third)));
			 * 
			 * }
			 */
		}
		return quesAnsList;
	}

	private boolean InsertTPRQuestionDetails(Connection conn, int stepId)
			throws Exception {
		HashMap<Integer, Integer> quesAnsList = getQuestionIdList();
		boolean isInsert = true;
		for (Map.Entry<Integer, Integer> entry : quesAnsList.entrySet()) {
			Integer questionId = entry.getKey();
			Integer answerId = entry.getValue();
			GateInDao.updateTPRQuestion(conn, tprRecord.getTprId(),
					TokenManager.currWorkStationType, questionId, answerId,
					TokenManager.userId);
		}
		return isInsert;
	}

	private HashMap<Integer, Integer> getQuestionIdList() {
		HashMap<Integer, Integer> quesAnsList = new HashMap<Integer, Integer>();

		// if (sealYes.isSelected()) {
		// quesAnsList.put(Status.TPRQuestion.sealOk, UIConstant.YES);
		// } else if (sealNo.isSelected()) {
		// quesAnsList.put(Status.TPRQuestion.sealOk, UIConstant.NO);
		// } else {
		// quesAnsList.put(Status.TPRQuestion.sealOk, UIConstant.NOSELECTED);
		// }
		// if (tarpaulinYes.isSelected()) {
		// quesAnsList.put(Status.TPRQuestion.tarpaulinOk, UIConstant.YES);
		// } else if (tarpaulinNo.isSelected()) {
		// quesAnsList.put(Status.TPRQuestion.tarpaulinOk, UIConstant.NO);
		// } else {
		// quesAnsList.put(Status.TPRQuestion.tarpaulinOk,
		// UIConstant.NOSELECTED);
		// }
		if (numberVisibleYes.isSelected()) {
			quesAnsList.put(Status.TPRQuestion.numberVisible, UIConstant.YES);
		} else if (numberVisibleNo.isSelected()) {
			quesAnsList.put(Status.TPRQuestion.numberVisible, UIConstant.NO);
		} else {
			quesAnsList.put(Status.TPRQuestion.numberVisible,
					UIConstant.NOSELECTED);
		}
		if (tailLightYes.isSelected()) {
			quesAnsList.put(Status.TPRQuestion.tailLightOk, UIConstant.YES);
		} else if (tailLightNo.isSelected()) {
			quesAnsList.put(Status.TPRQuestion.tailLightOk, UIConstant.NO);
		} else {
			quesAnsList.put(Status.TPRQuestion.tailLightOk,
					UIConstant.NOSELECTED);
		}
		if (sideMirrorYes.isSelected()) {
			quesAnsList.put(Status.TPRQuestion.sideMirror, UIConstant.YES);
		} else if (sideMirrorNo.isSelected()) {
			quesAnsList.put(Status.TPRQuestion.sideMirror, UIConstant.NO);
		} else {
			quesAnsList.put(Status.TPRQuestion.sideMirror,
					UIConstant.NOSELECTED);
		}
		if (leftSideIndicatorYes.isSelected()) {
			quesAnsList.put(Status.TPRQuestion.leftSideIndicator,
					UIConstant.YES);
		} else if (leftSideIndicatorNo.isSelected()) {
			quesAnsList
					.put(Status.TPRQuestion.leftSideIndicator, UIConstant.NO);
		} else {
			quesAnsList.put(Status.TPRQuestion.leftSideIndicator,
					UIConstant.NOSELECTED);
		}
		if (seatBeltYes.isSelected()) {
			quesAnsList.put(Status.TPRQuestion.seatBeltWorm, UIConstant.YES);
		} else if (seatBeltNo.isSelected()) {
			quesAnsList.put(Status.TPRQuestion.seatBeltWorm, UIConstant.NO);
		} else {
			quesAnsList.put(Status.TPRQuestion.seatBeltWorm,
					UIConstant.NOSELECTED);
		}
		if (headLightYes.isSelected()) {
			quesAnsList.put(Status.TPRQuestion.headLightOk, UIConstant.YES);
		} else if (headLightNo.isSelected()) {
			quesAnsList.put(Status.TPRQuestion.headLightOk, UIConstant.NO);
		} else {
			quesAnsList.put(Status.TPRQuestion.headLightOk,
					UIConstant.NOSELECTED);
		}
		if (reverseHornYes.isSelected()) {
			quesAnsList.put(Status.TPRQuestion.reverseHornOk, UIConstant.YES);
		} else if (reverseHornNo.isSelected()) {
			quesAnsList.put(Status.TPRQuestion.reverseHornOk, UIConstant.NO);
		} else {
			quesAnsList.put(Status.TPRQuestion.reverseHornOk,
					UIConstant.NOSELECTED);
		}
		if (breathlizerYes.isSelected()) {
			quesAnsList.put(Status.TPRQuestion.breathLyzerOk, UIConstant.YES);
		} else if (breathlizerNo.isSelected()) {
			quesAnsList.put(Status.TPRQuestion.breathLyzerOk, UIConstant.NO);
		} else {
			quesAnsList.put(Status.TPRQuestion.breathLyzerOk,
					UIConstant.NOSELECTED);
		}

		if (helperYes.isSelected()) {
			quesAnsList.put(Status.TPRQuestion.drivenByHelper, UIConstant.YES);
		} else if (helperNo.isSelected()) {
			quesAnsList.put(Status.TPRQuestion.drivenByHelper, UIConstant.NO);
		} else {
			quesAnsList.put(Status.TPRQuestion.drivenByHelper,
					UIConstant.NOSELECTED);
		}
		if (rightSideIndicatorYes.isSelected()) {
			quesAnsList.put(12, UIConstant.YES);
		} else if (leftSideIndicatorNo.isSelected()) {
			quesAnsList.put(12, UIConstant.NO);
		} else {
			quesAnsList.put(12, UIConstant.NOSELECTED);
		}
		quesAnsList.put(Status.TPRQuestion.isTagRead,
				isTagRead ? Results.Questions.YES : Results.Questions.NO);
		if (isFingerCaptured) {
			quesAnsList.put(Status.TPRQuestion.isFingerVerified,
					isFingerVerified ? Results.Questions.YES
							: Results.Questions.NO);
		}
		quesAnsList
				.put(Status.TPRQuestion.isFingerCaptured,
						isFingerCaptured ? Results.Questions.YES
								: Results.Questions.NO);
		quesAnsList.put(Status.TPRQuestion.isVehicleExist,
				isVehicleExist ? Results.Questions.YES : Results.Questions.NO);
		quesAnsList.put(Status.TPRQuestion.isChallanExist, !Utils
				.isNull(tprRecord.getChallanNo()) ? Results.Questions.YES
				: Results.Questions.NO);

		if (!Misc.isUndef(fitnessOk)) {
			quesAnsList.put(Status.TPRQuestion.isFitnessOk, fitnessOk);
		}
		if (!Misc.isUndef(roadPermitOk)) {
			quesAnsList.put(Status.TPRQuestion.isRoadPermitOk, roadPermitOk);
		}
		if (!Misc.isUndef(insuranceOk)) {
			quesAnsList.put(Status.TPRQuestion.isInsuranceOk, insuranceOk);
		}
		if (!Misc.isUndef(polutionOk)) {
			quesAnsList.put(Status.TPRQuestion.isPolutionOk, polutionOk);
		}

		return quesAnsList;
	}

	private void requestOverrideAction() {
		Connection conn = null;
		boolean destroyIt = false;
		try {
			conn = DBConnectionPool.getConnectionFromPoolNonWeb();
			updateTPR(conn,
					com.ipssi.rfid.constant.Type.WorkStationType.REGISTRATION,
					true);
			int stepId = InsertTPRStep(conn, true);
			if (!Misc.isUndef(stepId)) {
				InsertTPRQuestionDetails(conn, stepId);
			}
			// TPRBlockStatusHelper.allowCurrentStep(conn,
			// tprRecord.getVehicleId(), tprRecord,
			// TokenManager.currWorkStationId, TokenManager.currWorkStationType,
			// TokenManager.userId, true, true);
			conn.commit();
			fingurePrintCaptureService.stop();
			clearInput(false, conn);
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

	private void manualEntryAction() {
		Connection conn = null;
		boolean destroyIt = false;
		try {
			conn = DBConnectionPool.getConnectionFromPoolNonWeb();
			clearInput(false, conn);
			if (!vehicleName.isEditable()) {
				changeToCreateMode();
				vehicleName.requestFocusInWindow();
			}
			manualEntryButton.setEnabled(false);
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

	private void captureButtonAction() {
		setWhiteBackColor();
		if (vehicleName.getSelectedItem().toString().length() != 0) {
			JOptionPane.showMessageDialog(null, "Please Enter Vehicle Name");
			return;
		} else if (tprRecord == null) {
			JOptionPane.showMessageDialog(null,
					"Please Enter valid Vehicle Name");
			return;
		} else {
			try {
				driverId.setText("");
				dlNo.setText("");
				driverName.setText("");
				helperOk.clearSelection();
				photo.setIcon(null);
				isFingerCaptured = false;
				isFingerVerified = false;
				// DriverDetailFlyAsh.checkDriverExistance(conn);
				captureFingurePrint();
			} catch (Exception ex) {
				Logger.getLogger(GateInFlyAshWindow.class.getName()).log(
						Level.SEVERE, null, ex);
			}
		}
	}

	private void clearAction() {
		Connection conn = null;
		boolean destroyIt = false;
		try {
			conn = DBConnectionPool.getConnectionFromPoolNonWeb();
			clearInput(true, conn);
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

	private void vehicleNameAction() {
		Connection conn = null;
		boolean destroyIt = false;
		try {
			conn = DBConnectionPool.getConnectionFromPoolNonWeb();
			int vehId = Misc.getUndefInt();
			Pair<Integer, String> vehPair = null;
			if (!vehicleName.isEditable()) {
				setWhiteBackColor();
				sealOkPanel.requestFocusInWindow();
				sealOkPanel.setBackground(UIConstant.focusPanelColor);
			} else if (vehicleName.getItemCount() == 0
					&& Utils.isNull(vehicleName.getEditor().getItem()
							.toString())) {
				JOptionPane.showMessageDialog(null, "Please Enter Vehicle !!!");
				return;
				// vehicleName.setBackground(UIConstant.noActionPanelColor);
			} else {

				String vehName = null;
				try {
					isVehicleExist = false;
					isNewVehicle = Status.VEHICLE.EXISTING_MANUAL;
					String std_name = null;
					if (vehicleName.isEditable()
							|| vehicleName.getItemCount() == 0) {
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
					Object[] options = { "  Re-Enter  ", "  Continue  " };
					String msg = " Vehicle Not Exist ";
					int responseVehicleDialog = ConfirmationDialog
							.getDialogBox(new java.awt.Frame(), true, options,
									msg);
					if (responseVehicleDialog == reEnter) {
						vehicleName.setSelectedItem("");
						return;
					} else if (responseVehicleDialog == contiNue) {
						try {
							GateInDao.InsertNewVehicle(conn, vehName,
									TokenManager.userId);
							setTPRecord(vehName);
							setWhiteBackColor();
							transporter.setFocusable(true);
							transporter.setSelectedIndex(0);
							transporter.requestFocusInWindow();
							transporter.setEnabled(true);
							/*
							 * if (!isSeviceRunning && isMorphoExist) {
							 * fingurePrintCaptureService.start();
							 * isSeviceRunning = true; }
							 */
							startFingerCaptureService();
							isNewVehicle = Status.VEHICLE.NEW_MANUAL;
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
						isNewVehicle = Status.VEHICLE.EXISTING_MANUAL;
						/*
						 * if (!isSeviceRunning && isMorphoExist) {
						 * fingurePrintCaptureService.start(); isSeviceRunning =
						 * true; }
						 */
						if (tprRecord != null) {
							startFingerCaptureService();
							if (transporter.isFocusable()) {
								transporter.requestFocusInWindow();
							} else {
								seatBeltPanel
										.setBackground(UIConstant.focusPanelColor);
								seatBeltPanel.requestFocusInWindow();
							}
						}
						// transporter.requestFocusInWindow();
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

	private void populateDriver(Connection conn, Biometric driverBean,
			boolean fingerVerified, boolean fingerCaptured) {
		// TODO Auto-generated method stub
		driverInformation = driverBean;
		dlNo.setText(driverBean.getDl_no());
		driverId.setText(Misc.getPrintableInt(driverBean.getDriver_id()));
		driverName.setText(driverBean.getDriver_name());
		if (driverBean.getPhoto() != null) {
			BufferedImage image = DriverDetail.byteArrayToImage(driverBean
					.getPhoto());
			photo.setIcon(new ImageIcon(image));
		}
		if (driverBean.getDriver_type() == 1) {
			helperNo.setSelected(true);
		} else {
			helperYes.setSelected(true);
			JOptionPane
					.showMessageDialog(null,
							"Please Inform to Driver to take his Seat and then Re-Authenticate !!!");
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
		Triple<Integer, Long, Long> driverBlockStatus = TPRUtils
				.getDriverBlockStatus(conn, driverBean.getDriver_id());
		if (driverBlockStatus != null) {
			long curr = System.currentTimeMillis();
			isDriverBlacklisted = driverBlockStatus.first == Status.ACTIVE
					&& !Misc.isUndef(driverBlockStatus.second)
					&& (curr >= driverBlockStatus.second && (curr <= driverBlockStatus.third || Misc
							.isUndef(driverBlockStatus.third)));
		}
		setDriverHour(conn, driverBean.getDriver_id());
	}

	private void disableFingurePrintCapture() {
		driverId.setEditable(true);
		driverId.setFocusable(true);
		driverId.requestFocusInWindow();
		driverId.setBorder(javax.swing.BorderFactory
				.createLineBorder(new java.awt.Color(0, 0, 0)));
		dlNo.setEditable(true);
		dlNo.setFocusable(true);
		dlNo.setBorder(javax.swing.BorderFactory
				.createLineBorder(new java.awt.Color(0, 0, 0)));
		// captureButton.setVisible(false);
		captureButton.setVisible(true);
	}

	private void setDriverHour(Connection conn, int driver_ID) {
		Pair<ResultEnum, String> driverWorkInfo;

		try {
			if (gpv == null) {
				if (tprRecord.getTprId() != Misc.getUndefInt()) {
					gpv = GpsPlusViolations.getGpsPlusViolatins(conn, tprRecord
							.getVehicleId(), tprRecord.getTprId(), System
							.currentTimeMillis(), tprRecord.getMinesId(),
							tprRecord.getChallanDate() == null ? System
									.currentTimeMillis() : tprRecord
									.getChallanDate().getTime());
				} else {
					gpv = GpsPlusViolations.getGpsPlusViolatins(conn, tprRecord
							.getVehicleId(), tprRecord.getTprId(), System
							.currentTimeMillis(), Misc.getUndefInt(), System
							.currentTimeMillis());
				}
			}
			driverWorkInfo = gpv.getDriverHours(conn, driver_ID);
			if (driverWorkInfo.first == ResultEnum.GREEN) {
				driverHours.setText(driverWorkInfo.second);
				driverHours.setForeground(UIConstant.PanelDarkGreen);
			} else if (driverWorkInfo.first == ResultEnum.RED) {
				driverHours.setText(driverWorkInfo.second);
				driverHours.setForeground(UIConstant.noActionPanelColor);
			} else {
				driverHours.setText(driverWorkInfo.second);
				driverHours.setForeground(UIConstant.PanelYellow);
			}
		} catch (Exception ex) {
			Logger.getLogger(CoalGateInWindow.class.getName()).log(
					Level.SEVERE, null, ex);
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

	private void playSeatBeltWormVoice() {
		WaveFormPlayer.playSoundIn(Status.TPRQuestion.seatBeltWorm);
	}

	private void changeDriverPanel(boolean isManual) {
		if (!isMorphoExist || isManual) {
			driverId.setEditable(true);
			driverId.setFocusable(true);
			driverId.setBorder(javax.swing.BorderFactory
					.createLineBorder(new java.awt.Color(0, 0, 0)));
			dlNo.setEditable(true);
			dlNo.setFocusable(true);
			dlNo.setBorder(javax.swing.BorderFactory
					.createLineBorder(new java.awt.Color(0, 0, 0)));
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
			// vehicleName.addItem("");
		}
		// transporter.setFocusable(true);
		// transporter.setEnabled(true);
	}

	private void captureFingurePrint() {
		Connection conn = null;
		boolean destroyIt = false;
		try {
			conn = DBConnectionPool.getConnectionFromPoolNonWeb();
			Triple<Biometric, Boolean, Boolean> captureResult = null;
			String msg = "User Not Exist, Do you want to  capture again ?";
			Object[] options = { "  Recapture  ", "  Continue  " };
			boolean continueCapture = true;
			while (continueCapture) {
				setFingerPrintPanel(true);
				captureResult = TokenManager.useSDK() ? DriverDetail
						.captureFingurePrintSDK(this) : DriverDetail
						.captureFingurePrint(this);
				if (captureResult == null) {
					JOptionPane
							.showMessageDialog(null,
									"Device is not Connected, Please Restart Application and Unplugged Device !!!");
					setFingerPrintPanel(false);
					disableFingurePrintCapture();
					break;
				} else if (captureResult.first == null) {
					int confirmationVal = ConfirmationDialog.getDialogBox(
							new javax.swing.JFrame(), true, options, msg);
					if (confirmationVal == 0) {
						WaveFormPlayer
								.playSoundOut(Status.TPRQuestion.tryAgainFinger);
						continue;
					} else if (confirmationVal == 1) {
						WaveFormPlayer
								.playSoundOut(Status.TPRQuestion.fingerNotMatch);
						setFingerPrintPanel(false);
						disableFingurePrintCapture();
						break;
					}
				} else {
					setFingerPrintPanel(false);
					populateDriver(conn, captureResult.first,
							captureResult.second, captureResult.third);
					break;
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

	private void changeIntoDefault(boolean clearToken, Connection conn) {
		if (clearToken) {
			TokenManager.clearWorkstation();
		} else {
			if (token != null) {
				TokenManager.returnToken(conn, token);
			}
		}
		// services
		fingurePrintCaptureService.stop();
		isSeviceRunning = false;
		isFingerPrintCaptureRunning = false;
		isAutoFingerCaptureStart = false;
		token = null;
		tprRecord = null;
		tpStep = null;
		driverInformation = null;
		gpv = null;
		entryTime = null;
		// questions
		fitnessOk = Misc.getUndefInt();
		roadPermitOk = Misc.getUndefInt();
		insuranceOk = Misc.getUndefInt();
		polutionOk = Misc.getUndefInt();
		driverSrc = Misc.getUndefInt();

		// flags
		captureCount = 1;
		isDriverExist = false;
		isFingerExist = false;
		isFingerVerified = false;
		isFingerCaptured = false;
		isDriverBlacklisted = false;
		debugTemplate = null;
		// isMorphoExist = false;
		vehicleBlackListed = false;
		isPaperOk = false;
		doCheck = false;
		isTagRead = false;
		isVehicleExist = false;
		isTpRecordValid = false;
		isRequestOverride = false;
		manualCaptureFinger = false;
		gpsLocation.setText("");
		gpsTime.setText("");

		// reset ui
		paperValid.setText("");
		isSeviceRunning = false;
		vehicleBlackListed = false;

		isNewVehicle = Misc.getUndefInt();

		vehicleName.setEnabled(false);
		vehicleName.setEditable(false);
		vehicleName.removeAllItems();
		vehicleName.setFocusable(false);
		// vehicleName.addItem("");
		driverHours.setText("");
		transporter.setFocusable(false);
		transporter.setEnabled(false);
		transporter.setSelectedIndex(0);
		setWhiteBackColor();
		driverSrc = Misc.getUndefInt();
		photo.setIcon(null);
		blocking_reason.setText("");

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
		transporter.setFocusable(false);
		transporter.setEnabled(false);
		saveButton.setText("Save And Open Gate");
		paperValid.setText("");
		saveButton.setEnabled(false);
		if (isManualEntry)
			manualEntryButton.setEnabled(true);
		enableDenyEntry(false);
		overrides.setText("");

		tprBlockManager = null;

		clearDriver();
		if (!isFingerPrintCaptureRunning)
			MorphoSmartFunctions.getMorpho().cancelAllCurrentTask();
		// ChangeOnOpenGate(true);
	}

	private void ChangeOnOpenGate(boolean isDataSyncRunning) {
		if (!isDataSyncRunning) {
			driverId.setText("");
			driverId.setEditable(true);
			driverId.setFocusable(true);
			driverId.setBorder(javax.swing.BorderFactory
					.createLineBorder(new java.awt.Color(0, 0, 0)));
			driverId.setBackground(UIConstant.PanelWhite);
			dlNo.setText("");
			dlNo.setEditable(true);
			dlNo.setFocusable(true);
			dlNo.setBorder(javax.swing.BorderFactory
					.createLineBorder(new java.awt.Color(0, 0, 0)));
			dlNo.setBackground(UIConstant.PanelWhite);
			driverName.setText("");
			driverName.setEditable(false);
			driverName.setFocusable(false);
			driverName.setBackground(UIConstant.PanelWhite);
			driverName.setBorder(null);
			// dlNo.setEditable(false);
			// dlNo.setFocusable(false);
			// dlNo.setBorder(null);
			// captureButton.setVisible(false);

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
	}

	private void checkQuestions() {
		doCheck = true;
		setWhiteBackColor();
		if (!seatBeltNo.isSelected() && !seatBeltYes.isSelected()
				&& !seatBeltNC.isSelected()) {
			// JOptionPane.showMessageDialog(null,
			// "Please Give Answer to All Question");
			seatBeltPanel.setBackground(UIConstant.focusPanelColor);
			seatBeltPanel.requestFocusInWindow();
			return;
		}
		// else if (!tarpaulinNo.isSelected() && !tarpaulinYes.isSelected() &&
		// !tarpaulinNC.isSelected()) {
		// // JOptionPane.showMessageDialog(null,
		// "Please Give Answer to All Question");
		// tarpaulinPanel.setBackground(UIConstant.focusPanelColor);
		// tarpaulinPanel.requestFocusInWindow();
		// return;
		// } else if (!sealNo.isSelected() && !sealYes.isSelected() &&
		// !sealNC.isSelected()) {
		// // JOptionPane.showMessageDialog(null,
		// "Please Give Answer to All Question");
		// sealOkPanel.setBackground(UIConstant.focusPanelColor);
		// sealOkPanel.requestFocusInWindow();
		// return;
		// }
		else if (!numberVisibleNo.isSelected()
				&& !numberVisibleYes.isSelected()
				&& !numberVisibleNC.isSelected()) {
			// JOptionPane.showMessageDialog(null,
			// "Please Give Answer to All Question");
			numberVisiblePanel.setBackground(UIConstant.focusPanelColor);
			numberVisiblePanel.requestFocusInWindow();
			return;
		} else if (!sideMirrorNo.isSelected() && !sideMirrorYes.isSelected()
				&& !sideMirrorNC.isSelected()) {
			// JOptionPane.showMessageDialog(null,
			// "Please Give Answer to All Question");
			sideMirrorPanel.setBackground(UIConstant.focusPanelColor);
			sideMirrorPanel.requestFocusInWindow();
			return;
		} else if (!reverseHornNo.isSelected() && !reverseHornYes.isSelected()
				&& !reverseHornNC.isSelected()) {
			// JOptionPane.showMessageDialog(null,
			// "Please Give Answer to All Question");
			reverseHornPanel.setBackground(UIConstant.focusPanelColor);
			reverseHornPanel.requestFocusInWindow();
			return;
		} else if (!tailLightNo.isSelected() && !tailLightYes.isSelected()
				&& !tailLightNC.isSelected()) {
			// JOptionPane.showMessageDialog(null,
			// "Please Give Answer to All Question");
			tailLightPanel.setBackground(UIConstant.focusPanelColor);
			tailLightPanel.requestFocusInWindow();
			return;
		} else if (!headLightNo.isSelected() && !headLightYes.isSelected()
				&& !headLightNC.isSelected()) {
			// JOptionPane.showMessageDialog(null,
			// "Please Give Answer to All Question");
			headLightPanel.setBackground(UIConstant.focusPanelColor);
			headLightPanel.requestFocusInWindow();
			return;
		} else if (!leftSideIndicatorNo.isSelected()
				&& !leftSideIndicatorYes.isSelected()
				&& !leftSideIndicatorNC.isSelected()) {
			// JOptionPane.showMessageDialog(null,
			// "Please Give Answer to All Question");
			leftSideIndicatorPanel.setBackground(UIConstant.focusPanelColor);
			leftSideIndicatorPanel.requestFocusInWindow();
			return;
		} else if (!rightSideIndicatorNo.isSelected()
				&& !rightSideIndicatorYes.isSelected()
				&& !rightSideIndicatorNC.isSelected()) {
			// JOptionPane.showMessageDialog(null,
			// "Please Give Answer to All Question");
			rightSideIndicatorPanel.setBackground(UIConstant.focusPanelColor);
			rightSideIndicatorPanel.requestFocusInWindow();
			return;
		} else if (!breathlizerNo.isSelected() && !breathlizerYes.isSelected()
				&& !breathlizerNC.isSelected()) {
			// JOptionPane.showMessageDialog(null,
			// "Please Give Answer to All Question");
			breathlizerOkPanel.setBackground(UIConstant.focusPanelColor);
			breathlizerOkPanel.requestFocusInWindow();
			return;
		} else if (driverId.isEditable() && driverId.isFocusable()
				&& Utils.isNull(driverId.getText())) {
			driverId.setBackground(UIConstant.focusPanelColor);
			driverId.requestFocusInWindow();
			return;
		} else {
			setFocusOnButton();
		}
	}

	private void syncFingerPrintDataFromServer() {
		if (fingerPrintSyncService == null) {
			System.out.println("TokenManager.currWorkStationId : "
					+ TokenManager.currWorkStationId);
			fingerPrintSyncService = new SyncFingerPrint();
			// isDataSyncRunning = true;
			fingerPrintSyncService.setHandler(new SynServiceHandler() {
				@Override
				public void onChange(boolean onChange) {
					if (onChange) {
						fingerInstruction.setText("Sync Service Running");
					} else {
						fingerInstruction.setText("");
					}
					isDataSyncRunning = onChange;
					changeDriverPanel(onChange);
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

	private void setFocusOnButton() {
		if (saveButton.isEnabled()) {
			saveButton.requestFocusInWindow();
		} else if (manualEntryButton.isEnabled()) {
			manualEntryButton.requestFocusInWindow();
		} else {
			clearButton.requestFocusInWindow();
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
		Icon iconOK = new ImageIcon(imgFvpOk.getImage()
				.getScaledInstance(photo.getWidth(), photo.getHeight(),
						Image.SCALE_AREA_AVERAGING));
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
		// if(!isFingerPrintCaptureRunning)
		if (!isFingerPrintCaptureRunning || !manualCaptureFinger)
			return;
		BufferedImage bufferedImage = MorphoEventHandler.toBufferedImage(
				morphoImage.getImage(),
				morphoImage.getImageHeader().getNbCol(), morphoImage
						.getImageHeader().getNbRow());
		ImageIcon image = new ImageIcon(MorphoEventHandler.resizeImage(
				bufferedImage, photo.getWidth(), photo.getHeight()));
		photo.setIcon(image);
	}

	@Override
	public void setCurrentImageInfo(int nbCol, int nbRow, int resX, int resY,
			int bitPerPixel) {
		// TODO Auto-generated method stub
		lblCurrentImageInfo.setText("Size: " + nbCol + "*" + nbRow
				+ " pix, Res: " + resX + "*" + resY + " dpi, " + bitPerPixel
				+ " bits/pixels");
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

	private void setFingerPrintPanel(boolean status) {
		lblScore.setText("");
		progressBar.setValue(0);
		lblSteps.setText("");
		lblInstruction.setText("");
		lblCurrentImageInfo.setText("");
		fingerPrintPanel.setVisible(status);
		manualCaptureFinger = status;
		isFingerPrintCaptureRunning = status;
		if(debugTemplate != null)
			photo.setIcon(null);
	}

	@Override
	public void setBorderColorGreen(short fingerNumber, short step) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setLiveStepImage(MorphoImage morphoImage, short fingerNumber,
			short step) {
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

	private void clearDriver() {
		if (!isMorphoExist || isFingerPrintCaptureRunning) {
			driverId.setText("");
			driverId.setEditable(true);
			driverId.setFocusable(true);
			driverId.setBorder(javax.swing.BorderFactory
					.createLineBorder(new java.awt.Color(0, 0, 0)));
			driverId.setBackground(UIConstant.PanelWhite);
			dlNo.setText("");
			dlNo.setEditable(true);
			dlNo.setFocusable(true);
			dlNo.setBorder(javax.swing.BorderFactory
					.createLineBorder(new java.awt.Color(0, 0, 0)));
			dlNo.setBackground(UIConstant.PanelWhite);
			driverName.setText("");
			driverName.setEditable(false);
			driverName.setFocusable(false);
			driverName.setBackground(UIConstant.PanelWhite);
			driverName.setBorder(null);
			// dlNo.setEditable(false);
			// dlNo.setFocusable(false);
			// dlNo.setBorder(null);
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
}
