package com.ipssi.rfid.ui;

import java.sql.Connection;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.LayoutStyle.ComponentPlacement;

import morpho.morphosmart.sdk.demo.trt.ImageLoader;

import com.ipssi.fingerprint.utils.SynServiceHandler;
import com.ipssi.fingerprint.utils.SyncFingerPrintDeviceHelper;
import com.ipssi.gen.exception.GenericException;
import com.ipssi.gen.utils.DBConnectionPool;
import com.ipssi.gen.utils.Misc;
import com.ipssi.rfid.processor.TokenManager;
import com.scl.loadlibrary.LoadLibrary;
import com.scl.loadlibrary.MorphoSmartFunctions;

public class FingerPrintSyncDialog extends JDialog {

	private static final long serialVersionUID = 1L;
	// UI variables
	JLabel statusOpen = new JLabel(": ");
	JLabel msoDeviceId = new JLabel(": ");
	JLabel msoDeviceCapacity = new JLabel(": ");
	JLabel msoDeviceEnroll = new JLabel(": ");
	JLabel syncStatus = new JLabel(": ");
	JLabel lblCommunicationInitialisation = new JLabel("Total Enroll");
	JLabel lblOpen = new JLabel("Connection Status");
	JLabel lblMsoConfiguration = new JLabel("MSO Device Id");
	JLabel lblTheCertification = new JLabel("MSO Device Capacity");
	SyncFingerPrintDeviceHelper syncService = null;
    boolean isMorphoExist = false;
    boolean isSyncServiceRunning = false;

	public FingerPrintSyncDialog() {
		setModal(true);
		setIconImage(ImageLoader.load("MSO_Demo.png"));
		setModalExclusionType(ModalExclusionType.APPLICATION_EXCLUDE);
		setTitle("Initialisation, please wait...");
		setResizable(false);
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
//		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setSize(450, 240);
		setLocationRelativeTo(null);

		JLabel lblMsoAnalysePlease = new JLabel("MSO analyse, please wait.................");

		JLabel lblTheDatabaseConfiguration = new JLabel("Sync Status");

		GroupLayout groupLayout = new GroupLayout(getContentPane());
		groupLayout.setHorizontalGroup(groupLayout.createParallelGroup(Alignment.LEADING).addGroup(
				Alignment.TRAILING,
				groupLayout
						.createSequentialGroup()
						.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
								.addGroup(groupLayout.createSequentialGroup()
										.addContainerGap()
										.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
												.addComponent(lblCommunicationInitialisation)
												.addComponent(lblOpen)
												.addComponent(lblMsoConfiguration)
												.addComponent(lblTheCertification)))
												.addGroup(groupLayout.createSequentialGroup()
														.addGap(10)
														.addComponent(lblTheDatabaseConfiguration))
										.addGroup(groupLayout.createSequentialGroup().addGap(10).addComponent(lblMsoAnalysePlease))).addPreferredGap(ComponentPlacement.RELATED, 28, Short.MAX_VALUE)
						.addGroup(groupLayout.createParallelGroup(Alignment.LEADING, false)
								.addGroup(groupLayout.createSequentialGroup()
										.addComponent(syncStatus)
										.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
										.addGroup(Alignment.TRAILING, groupLayout.createSequentialGroup()
												.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
														.addComponent(msoDeviceEnroll).addComponent(msoDeviceCapacity)
														.addComponent(msoDeviceId).addComponent(statusOpen)).addGap(151)))));
		groupLayout.setVerticalGroup(groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
						.addContainerGap()
						.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
								.addGroup(groupLayout.createSequentialGroup()
										.addComponent(statusOpen).addGap(18)
										.addComponent(msoDeviceId).addGap(18)
										.addComponent(msoDeviceCapacity).addGap(18)
										.addComponent(msoDeviceEnroll))
										.addGroup(groupLayout.createSequentialGroup()
												.addComponent(lblMsoAnalysePlease).addGap(18)
												.addComponent(lblOpen)
												.addGap(18)
												.addComponent(lblMsoConfiguration)
												.addGap(18)
												.addComponent(lblTheCertification)
												.addGap(18)
												.addComponent(lblCommunicationInitialisation)))
												.addGap(18)
												.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
														.addComponent(lblTheDatabaseConfiguration)
														.addComponent(syncStatus)).addContainerGap(27, Short.MAX_VALUE)));
		getContentPane().setLayout(groupLayout);
		pack();
		isMorphoExist  = TokenManager.morphoDeviceExist == 1 && (TokenManager.useSDK() ?  MorphoSmartFunctions.getMorpho().isConnected() : LoadLibrary.isMorphoConnected() );
		start();
		setVisible(true);
	}
	public void start(){
		if(isMorphoExist){
    		new Thread(new Runnable() {
    			@Override
    			public void run() {
    				Connection conn = null;
    				try{
    					conn = DBConnectionPool.getConnectionFromPoolNonWeb();
    					setStatusOpen("OK");
    					SyncFingerPrintDeviceHelper.loadInFingerPrintDeviceSDK(conn, false, new SynServiceHandler() {
    						@Override
    						public void onChange(boolean onChange) {
    							isSyncServiceRunning = onChange;
    							if(!isSyncServiceRunning){
    								close();
    							}
    							/*else{
    								enrollCount = MorphoSmartFunctions.getTotalEnrolled();
    								setMsoDeviceEnroll(!Misc.isUndef(enrollCount) ? enrollCount+"" : "");
    							}*/
    						}
    						@Override
    						public void notifyText(String msg) {
    							setSyncStatus(msg);
    						}
							@Override
							public void setDeviceId(String deviceId) {
								// TODO Auto-generated method stub
								setMsoDeviceId(deviceId);
							}
							@Override
							public void setCapacity(int capacity) {
								// TODO Auto-generated method stub
								setMsoDeviceCapacity(!Misc.isUndef(capacity) ? capacity+"" : "");
							}
							@Override
							public void setEnrolled(int enrolled) {
								// TODO Auto-generated method stub
								setMsoDeviceEnroll(!Misc.isUndef(enrolled) ? enrolled+"" : "");
							}
							@Override
							public void clearingData() {
								// TODO Auto-generated method stub
								
							}
							@Override
							public void init(String deviceId, int capacity, int enrolled) {
								// TODO Auto-generated method stub
								
							}
    					},false);
    					
    				}catch(Exception ex){
    					ex.printStackTrace();
    				}finally{
    					try {
    						DBConnectionPool.returnConnectionToPoolNonWeb(conn);
    					} catch (GenericException e) {
    						e.printStackTrace();
    					}
    				}
    			}
    		}).start();
    	
		}else{
			this.dispose();
		}
	}
	private void close(){
		this.dispose();
	}
	//
	// Setters-------------------------------------------------------------------
	//

	public void setOpen(String open) {
		this.lblOpen.setText(open);
		this.repaint();
	}

	public void setStatusOpen(String statusOpen) {
		this.statusOpen.setText(": " + statusOpen);
	}

	public void setMsoDeviceId(String msoDeviceId) {
		this.msoDeviceId.setText(": " + msoDeviceId);
	}

	public void setMsoDeviceCapacity(String msoDeviceCapacity) {
		this.msoDeviceCapacity.setText(": " + msoDeviceCapacity);
	}

	public void setCommunicationInitialisation(String securityCom) {
		this.lblCommunicationInitialisation.setText(securityCom);
	}

	public void setMsoDeviceEnroll(String msoDeviceEnroll) {
		this.msoDeviceEnroll.setText(": " + msoDeviceEnroll);
	}

	public void setSyncStatus(String syncText) {
		this.syncStatus.setText(": " + syncText);
	}

	public void setCertification(String certification) {
		this.lblTheCertification.setText(certification);
	}
}
