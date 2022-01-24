package com.scl.loadlibrary;

import java.awt.Color;
import java.awt.Component;
import java.awt.Frame;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import ch.qos.logback.classic.pattern.Util;

import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.Pair;
import com.ipssi.gen.utils.Triple;
import com.ipssi.rfid.constant.UIConstant;
import com.ipssi.rfid.processor.Utils;
import com.ipssi.rfid.ui.DriverRegistrationWindow;


import morpho.morphosmart.sdk.api.MorphoImage;
import morpho.morphosmart.sdk.api.MorphoSmartSDK;
import morpho.morphosmart.sdk.demo.dialog.DialogResultWindow;
import morpho.morphosmart.sdk.demo.ennum.EnumMoveFinger;
import morpho.morphosmart.sdk.demo.trt.ErrorsMgt;
import morpho.morphosmart.sdk.demo.trt.ImageLoader;
import morpho.morphosmart.sdk.demo.trt.MorphoEventHandler;

public class CapturePanel extends javax.swing.JDialog implements MorphoActionI{

	private static final long serialVersionUID = 1L;

	private Icon iconArrowUp = new ImageIcon(ImageLoader.load("arrow_up.png"));
	private Icon iconArrowRight = new ImageIcon(ImageLoader.load("arrow_right.png"));
	private Icon iconArrowDown = new ImageIcon(ImageLoader.load("arrow_down.png"));
	private Icon iconArrowLeft = new ImageIcon(ImageLoader.load("arrow_left.png"));
	private Icon gifCapture = new ImageIcon(ImageLoader.load("capture.gif"));
	private Icon gifMaillage = new ImageIcon(ImageLoader.load("maillage.gif"));
	private ImageIcon imgFvpOk = new ImageIcon(ImageLoader.load("ok.png"));
	private ImageIcon imgFvpKo = new ImageIcon(ImageLoader.load("ko.png"));
	private JPanel fingerPrintPanelCap = new JPanel(); 
	private JPanel fingerPrintPanel = null; 
	private JLabel lblFinger1step1 = new JLabel("");
	private JLabel lblFinger1step2 = new JLabel("");
	private JLabel lblFinger1step3 = new JLabel("");
	private JLabel lblFinger2step1 = new JLabel("");
	private JLabel lblFinger2step2 = new JLabel("");
	private JLabel lblFinger2step3 = new JLabel("");
	private JLabel lblDetectedQuality = new JLabel("...");
	private JLabel[][] lblFinger = { { lblFinger1step1, lblFinger1step2, lblFinger1step3 }, { lblFinger2step1, lblFinger2step2, lblFinger2step3 } };
	// Borders
	private Border lowerBevelBorder = BorderFactory.createBevelBorder(BevelBorder.LOWERED);
	private Border greenLine = BorderFactory.createLineBorder(Color.GREEN);
	private Border orangeLine = BorderFactory.createLineBorder(Color.ORANGE);
	private BioMatricBean biometricBean = null;
	private List<String> deviceIdList = null;
	private ArrayList<Integer> deleteDriverList = null;
	private ArrayList<byte[]> fingerTemplateList = null;
	private ArrayList<byte[]> fingerImageList = null;
	private DriverRegistrationWindow parent = null;

	public static javax.swing.JLabel photo;

	// labels for fingerprint
	private JLabel lblCurrentImageInfo;
	private JLabel lblSteps;
	private JLabel lblScore;
	private JLabel lblInstruction;
	private JProgressBar progressBar;
	/**
	 * Create the panel.
	 * 
	 * @param panelBasicBiometricOperation
	 */
	public CapturePanel(DriverRegistrationWindow parent, boolean modal, BioMatricBean biometricBean, ArrayList<Integer> deleteDriverList, ArrayList<byte[]> fingerTemplateList, ArrayList<byte[]> fingerImageList) {
		super(parent, modal);
		initComponents();
		this.setLocation(500, 200);
		this.parent = parent;
		this.biometricBean = biometricBean;
		this.deleteDriverList = deleteDriverList;
		this.fingerTemplateList = fingerTemplateList;
		this.fingerImageList = fingerImageList;
		Thread t = new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					startFingerPrint();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});
		t.start();
	}

	private void startFingerPrint() {
		// TODO Auto-generated method stub
		try{
			boolean deviceConnected = MorphoSmartFunctions.getMorpho().isConnected();
        	System.out.println("deviceConnected" + deviceConnected);
        	if (!deviceConnected) {
        		return;
        	}
        	MorphoSmartFunctions morpho = null;
        	synchronized (MorphoSmartFunctions.lock) {
        		morpho = MorphoSmartFunctions.getMorpho();
        		Triple<Integer, byte[], byte[]> ret = morpho.capture(this);
        		if(ret != null && ret.first == MorphoSmartSDK.MORPHO_OK && ret.second != null && ret.third !=null){
        			ArrayList<byte[]> fingerPrintTemplateList = new ArrayList<byte[]>();
        			fingerPrintTemplateList.add(ret.second);
        			fingerPrintTemplateList.add(ret.third);
        			Pair<String, String> matchResult = morpho.identifyUser(biometricBean.getDriverId() + "", fingerPrintTemplateList);
        			boolean fingerOneConflict = false;
        			boolean fingerTwoConflict = false;
        			int fingerOneConflictId = Misc.getUndefInt();
        			int fingerTwoConflictId = Misc.getUndefInt();
        			ArrayList<Integer> tempDeletedId = new ArrayList<Integer>();
        			String conflictMessage = null;
        			fingerOneConflictId = matchResult != null ? Misc.getParamAsInt(matchResult.first) : Misc.getUndefInt();
        			fingerTwoConflictId = matchResult != null ? Misc.getParamAsInt(matchResult.second) : Misc.getUndefInt();
        			fingerOneConflict = !Misc.isUndef(fingerOneConflictId) && biometricBean.getDriverId() != fingerOneConflictId;
        			fingerTwoConflict = !Misc.isUndef(fingerTwoConflictId) && biometricBean.getDriverId() != fingerTwoConflictId;
        			if(fingerOneConflict){
        				conflictMessage = "Finger One conflicting with driver ID : "+ matchResult.first;
        				tempDeletedId.add(fingerOneConflictId);
        			}
        			if(fingerTwoConflict){
        				conflictMessage = conflictMessage == null ? "" : conflictMessage+" and \n";
        				if(fingerOneConflict){
        					if(fingerOneConflictId != fingerTwoConflictId){
        						conflictMessage = "Finger Two conflicting with driver ID : "+ matchResult.second+"\n";
        						tempDeletedId.add(fingerTwoConflictId);
        					}else{
        						conflictMessage = "Both Fingers conflicting with Gate PASS ID : "+ matchResult.second + "\n";
        					}
        				}else{
        					tempDeletedId.add(fingerTwoConflictId);
        				}
        			}
        			if(fingerOneConflict || fingerTwoConflict){
        				if(conflictMessage == null)
        					conflictMessage = "";
        				conflictMessage += "\nDo you want to continue with ?\nDriver ID: " + biometricBean.getDriverId() +"\nDriver Name: " + biometricBean.getDriverName() + "\nDriver DL No: " + biometricBean.getDriverDlNumber();
        				int responseVehicleDialog = JOptionPane.showConfirmDialog(this, conflictMessage, UIConstant.dialogTitle, JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
        				System.out.print("##### Confirmation Value :#####" + responseVehicleDialog);
        				if (responseVehicleDialog == 1 || responseVehicleDialog == -1) {
        					return;
        				} else {
        					for (int i = 0,is = tempDeletedId == null ? 0 : tempDeletedId.size(); i < is; i++) {
        						if(i==0)
        							deleteDriverList.clear();
        						deleteDriverList.add(tempDeletedId.get(i));
        					}
        					fingerTemplateList.clear();
        					fingerTemplateList.add(ret.second);
        					fingerTemplateList.add(ret.third);
        				}
        			}else{
        				fingerTemplateList.clear();
        				fingerTemplateList.add(ret.second);
        				fingerTemplateList.add(ret.third);
        			}
        			this.dispose();
        		}else{
        			int captureResult = ret != null ? ret.first : Misc.getUndefInt();
        			String message1 = "Capture Failed";
					String message2 = "";
					String message3 = "";
					String message4 = "";
					switch (captureResult) {
					case MorphoSmartSDK.MORPHOERR_NO_HIT:
						message2 = "Bad Capture Sequence.";
						break;
					case MorphoSmartSDK.MORPHOERR_CMDE_ABORTED:
						message2 = "Command aborted by user.";
						break;
					case MorphoSmartSDK.MORPHOERR_TIMEOUT:
						message2 = "Timeout has expired.";
						message3 = "Command aborted.";
						break;
					case MorphoSmartSDK.MORPHOERR_FFD:
						message2 = "False finger detected !!!";
						break;
					case MorphoSmartSDK.MORPHOERR_MOIST_FINGER:
						message2 = "Finger too moist !!!";
						break;
					case MorphoSmartSDK.MORPHOERR_BADPARAMETER:
						message2 = "One or more input parameters are out of range";
						break;
					case MorphoSmartSDK.MORPHOERR_LICENSE_MISSING:
						message2 = "A required license is missing.";
						break;
					case MorphoSmartSDK.MORPHOERR_ADVANCED_SECURITY_LEVEL_NOT_AVAILABLE:
						message2 = "Cannot make a multimodal template compatible with advanced security levels.";
						break;
					case MorphoSmartSDK.MORPHOERR_ADVANCED_SECURITY_LEVEL_MISMATCH:
						message2 = "Failed to make a multimodal template compatible with advanced security levels.";
						break;
					case MorphoSmartSDK.MORPHOERR_BAD_FINAL_FINGER_PRINT_QUALITY:
						message2 = "Failed to reach the fingerprint quality threshold.";
						break;
					default:
						message2 = "An error occured while calling";
						message3 = "MorphoDevice.capture() function";
						message4 = ErrorsMgt.convertSDKError(captureResult);
						break;
					}
					if(Misc.isUndef(captureResult))
						JOptionPane.showMessageDialog(null, "Unable to process request please try again later.");
					else
						new DialogResultWindow(this, message1, message2, message3, message4, "").setVisible(true);
					this.dispose();
        		}
        		System.out.println(ret);
        	}
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	@SuppressWarnings("unchecked")
	private void initComponents() {

		setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
		JPanel panelFinger1 = new JPanel();
		fingerPrintPanel = new JPanel();
		photo = new javax.swing.JLabel();
		photo.setIcon(null);
		photo.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

		lblCurrentImageInfo = new JLabel("");
		lblCurrentImageInfo.setVisible(false);
		lblCurrentImageInfo.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
		lblInstruction = new JLabel("", SwingConstants.CENTER);
		lblInstruction.setBorder(BorderFactory.createLineBorder(Color.GRAY));
		lblSteps = new JLabel("", SwingConstants.CENTER);
		lblSteps.setBorder(BorderFactory.createLineBorder(Color.GRAY));
		lblSteps.setOpaque(true);
		lblSteps.setBackground(Color.WHITE);
		lblScore = new JLabel("", SwingConstants.CENTER);
		lblScore.setBorder(BorderFactory.createLineBorder(Color.GRAY));

		progressBar = new JProgressBar();
		progressBar.setOrientation(SwingConstants.VERTICAL);
		progressBar.setMaximum(150);

		fingerPrintPanel.setBorder(new TitledBorder(null, "Current Capture Info", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		
//		fingerPrintPanel.setBackground(java.awt.SystemColor.controlLtHighlight);
		
		
		
		
		panelFinger1.setBorder(new TitledBorder(null, "Finger #1", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		lblFinger1step1.setBorder(lowerBevelBorder);
		lblFinger1step2.setBorder(lowerBevelBorder);
		lblFinger1step3.setBorder(lowerBevelBorder);
		JPanel panelFinger2 = new JPanel();
		panelFinger2.setBorder(new TitledBorder(null, "Finger #2", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		lblFinger2step1.setBorder(lowerBevelBorder);
		lblFinger2step2.setBorder(lowerBevelBorder);
		lblFinger2step3.setBorder(lowerBevelBorder);

		lblDetectedQuality.setBorder(lowerBevelBorder);

		GroupLayout groupLayout = new GroupLayout(fingerPrintPanelCap);
		groupLayout.setHorizontalGroup(groupLayout.createParallelGroup(Alignment.LEADING)
				.addComponent(panelFinger1, GroupLayout.DEFAULT_SIZE, 390, Short.MAX_VALUE)
				.addComponent(panelFinger2, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 390, Short.MAX_VALUE)
				.addGroup(Alignment.TRAILING, groupLayout.createSequentialGroup().addContainerGap()
						.addComponent(lblDetectedQuality, GroupLayout.DEFAULT_SIZE, 370, Short.MAX_VALUE)
						.addContainerGap())
				.addComponent(fingerPrintPanel, GroupLayout.DEFAULT_SIZE, 390, Short.MAX_VALUE));
		groupLayout.setVerticalGroup(groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
						.addComponent(panelFinger1, GroupLayout.PREFERRED_SIZE, 150, GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addComponent(panelFinger2, GroupLayout.PREFERRED_SIZE, 150, GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addComponent(lblDetectedQuality)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addComponent(fingerPrintPanel, GroupLayout.PREFERRED_SIZE, 190, GroupLayout.PREFERRED_SIZE)
						.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
						);
		groupLayout.linkSize(SwingConstants.VERTICAL, new Component[] { panelFinger1, panelFinger2 });
		GroupLayout gl_panelFinger2 = new GroupLayout(panelFinger2);
		gl_panelFinger2.setHorizontalGroup(gl_panelFinger2.createParallelGroup(Alignment.LEADING).addGroup(gl_panelFinger2.createSequentialGroup().addContainerGap().addComponent(lblFinger2step1, GroupLayout.PREFERRED_SIZE, 100, GroupLayout.PREFERRED_SIZE).addGap(18).addComponent(lblFinger2step2, GroupLayout.PREFERRED_SIZE, 100, GroupLayout.PREFERRED_SIZE).addGap(18).addComponent(lblFinger2step3, GroupLayout.PREFERRED_SIZE, 100, GroupLayout.PREFERRED_SIZE).addContainerGap(32, Short.MAX_VALUE)));
		gl_panelFinger2.setVerticalGroup(gl_panelFinger2.createParallelGroup(Alignment.LEADING).addGroup(
				gl_panelFinger2.createSequentialGroup().addGroup(gl_panelFinger2.createParallelGroup(Alignment.LEADING).addComponent(lblFinger2step1, GroupLayout.PREFERRED_SIZE, 115, GroupLayout.PREFERRED_SIZE).addComponent(lblFinger2step2, GroupLayout.PREFERRED_SIZE, 115, GroupLayout.PREFERRED_SIZE).addComponent(lblFinger2step3, GroupLayout.PREFERRED_SIZE, 115, GroupLayout.PREFERRED_SIZE)).addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
		panelFinger2.setLayout(gl_panelFinger2);

		GroupLayout gl_panelFinger1 = new GroupLayout(panelFinger1);
		gl_panelFinger1.setHorizontalGroup(gl_panelFinger1.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panelFinger1.createSequentialGroup()
						.addContainerGap()
						.addComponent(lblFinger1step1, GroupLayout.PREFERRED_SIZE, 100, GroupLayout.PREFERRED_SIZE)
						.addGap(18).addComponent(lblFinger1step2, GroupLayout.PREFERRED_SIZE, 100, GroupLayout.PREFERRED_SIZE)
						.addGap(18).addComponent(lblFinger1step3, GroupLayout.PREFERRED_SIZE, 100, GroupLayout.PREFERRED_SIZE)
						.addContainerGap(23, Short.MAX_VALUE)));
		gl_panelFinger1.setVerticalGroup(gl_panelFinger1.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panelFinger1.createSequentialGroup()
						.addGroup(gl_panelFinger1.createParallelGroup(Alignment.LEADING)
								.addComponent(lblFinger1step1, GroupLayout.PREFERRED_SIZE, 115, GroupLayout.PREFERRED_SIZE)
								.addComponent(lblFinger1step2, GroupLayout.PREFERRED_SIZE, 115, GroupLayout.PREFERRED_SIZE)
								.addComponent(lblFinger1step3, GroupLayout.PREFERRED_SIZE, 115, GroupLayout.PREFERRED_SIZE)
								)
						.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
					    );
		panelFinger1.setLayout(gl_panelFinger1);

		javax.swing.GroupLayout panel1Layout = new javax.swing.GroupLayout(fingerPrintPanel);
		fingerPrintPanel.setLayout(panel1Layout);
		panel1Layout.setHorizontalGroup(
				panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(panel1Layout.createSequentialGroup()
						.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addComponent(photo, javax.swing.GroupLayout.PREFERRED_SIZE, 158, javax.swing.GroupLayout.PREFERRED_SIZE)
						.addGroup(panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
								.addComponent(lblScore, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
								.addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
								.addGroup(panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
										.addComponent(lblInstruction, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
										.addComponent(lblSteps, javax.swing.GroupLayout.DEFAULT_SIZE, 180, Short.MAX_VALUE))
										.addGap(10, 10, 10))
										.addComponent(lblCurrentImageInfo, javax.swing.GroupLayout.DEFAULT_SIZE, 180, Short.MAX_VALUE)
				);
		panel1Layout.setVerticalGroup(
				panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(panel1Layout.createSequentialGroup()
						.addGroup(panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
								.addGroup(panel1Layout.createSequentialGroup()
										.addGroup(panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
												.addComponent(lblSteps, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
												.addGap(2,2,2)
												.addComponent(lblInstruction, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
												.addGroup(panel1Layout.createSequentialGroup()
														.addComponent(lblScore, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
														.addGap(2,2,2)
														.addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)))
														.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
														.addComponent(lblCurrentImageInfo, javax.swing.GroupLayout.DEFAULT_SIZE, 30, Short.MAX_VALUE)
														.addContainerGap())
					.addComponent(photo, javax.swing.GroupLayout.PREFERRED_SIZE, 165, javax.swing.GroupLayout.PREFERRED_SIZE)
				);



		fingerPrintPanelCap.setLayout(groupLayout);
		add(fingerPrintPanelCap);
		pack();
	}
	public void setDetectedQuality(short detectedQuality) {
		this.lblDetectedQuality.setText("DETECTED QUALITY : " + detectedQuality);
	}

	/**
	 * Set the live image to the appropriate Label based on fingerNumber and
	 * step
	 * 
	 * @param morphoImage
	 * @param fingerNumber
	 * @param step
	 */
	@Override
	public void setLiveStepImage(MorphoImage morphoImage, short fingerNumber, short step) {
		if (fingerNumber == -1 || step == -1) {
			return;
		}
		int row = fingerNumber - 1;
		int col = step - 1;
		JLabel label = lblFinger[row][col];

		BufferedImage bufferedImage = MorphoEventHandler.toBufferedImage(morphoImage.getImage(), morphoImage.getImageHeader().getNbCol(), morphoImage.getImageHeader().getNbRow());

		ImageIcon image = new ImageIcon(MorphoEventHandler.resizeImage(bufferedImage, label.getWidth(), label.getHeight()));

		label.setIcon(image);
		label.setBorder(BorderFactory.createCompoundBorder(orangeLine, lowerBevelBorder));
	}

	/**
	 * Colorify the done captures
	 * 
	 * @param fingerNumber
	 * @param step
	 */
	@Override
	public void setBorderColorGreen(short fingerNumber, short step) {
		if (fingerNumber == -1 || step == -1) {
			return;
		}
		lblFinger[fingerNumber - 1][step - 1].setBorder(BorderFactory.createCompoundBorder(greenLine, lowerBevelBorder));
	}

	/**
	 * Clear JLabels for further uses
	 */
	public void clearLive() {
		int rows = 2;
		int cols = 3;
		for (int col = 0; col < cols; ++col) {
			for (int row = 0; row < rows; ++row) {
				lblFinger[row][col].setIcon(null);
				lblFinger[row][col].setBorder(lowerBevelBorder);
			}
		}
		//panelBasicBiometricOperation.reinitUI();
		this.lblDetectedQuality.setText("...");
	}
	@Override
	public void setCodeQuality(short codeQuality) {
		this.lblDetectedQuality.setText("CODED QUALITY : " + codeQuality);
	}
	public static void main(String[] str){
		try{
			/*JFrame jf = new JFrame();
			jf.add(new CapturePanel());
			jf.setVisible(true);*/

		}catch(Exception ex){
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
		Icon iconOK = new ImageIcon(imgFvpOk.getImage().getScaledInstance(photo.getWidth(), photo.getHeight(), Image.SCALE_AREA_AVERAGING));
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
		BufferedImage bufferedImage = MorphoEventHandler.toBufferedImage(morphoImage.getImage(), morphoImage.getImageHeader().getNbCol(), morphoImage.getImageHeader().getNbRow());
		ImageIcon image = new ImageIcon(MorphoEventHandler.resizeImage(bufferedImage, photo.getWidth(), photo.getHeight()));
		photo.setIcon(image);
	}

	@Override
	public void setCurrentImageInfo(int nbCol, int nbRow, int resX, int resY, int bitPerPixel) {
		// TODO Auto-generated method stub
		lblCurrentImageInfo.setText("Size: " + nbCol + "*" + nbRow + " pix, Res: " + resX + "*" + resY + " dpi, " + bitPerPixel + " bits/pixels");
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

}
