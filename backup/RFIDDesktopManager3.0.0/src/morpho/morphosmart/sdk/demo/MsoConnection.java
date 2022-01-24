package morpho.morphosmart.sdk.demo;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Dialog.ModalExclusionType;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import com.ipssi.rfid.ui.DriverRegistrationWindow;
import com.scl.loadlibrary.BioMatricBean;

import morpho.morphosmart.sdk.api.IMsoSecu;
import morpho.morphosmart.sdk.api.MorphoDatabase;
import morpho.morphosmart.sdk.api.MorphoDevice;
import morpho.morphosmart.sdk.api.MorphoFAR;
import morpho.morphosmart.sdk.api.MorphoSmartSDK;
import morpho.morphosmart.sdk.demo.MsoDemo;
import morpho.morphosmart.sdk.demo.constant.MorphoConstant;
import morpho.morphosmart.sdk.demo.dialog.DialogCreateDB;
import morpho.morphosmart.sdk.demo.dialog.DialogUtils;
import morpho.morphosmart.sdk.demo.trt.ImageLoader;
import morpho.morphosmart.sdk.demo.trt.MorphoInfo;
import morpho.morphosmart.sdk.demo.trt.SecurityMgt;
import morpho.msosecu.sdk.api.MsoSecu;

public class MsoConnection extends JDialog {

	private static final long serialVersionUID = 1L;
	// UI variables
	private JPanel contentPane;
	private JTabbedPane tabbedPane;
	private JLabel lblMsoName = new JLabel("Device Name");
	private JLabel lblSerialPort = new JLabel("Serial Port");
	private JLabel lblBaudRate = new JLabel("Baud Rate");
	private JComboBox comboSerialPort;
	private JComboBox comboBaudRate;
	private JComboBox comboDeviceListUSB = new JComboBox();
	// variables
	private List<String> listDeviceNames = new ArrayList<String>();
	// MorphoSmart SDK
	private MorphoDevice mDevice = null;
	private MorphoDatabase mDatabase = null;
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
				MsoConnection mscOb = new MsoConnection(null);
				mscOb.btnOkActionPerformed(null,false,true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public MsoConnection(DriverRegistrationWindow driverReg) {
		setIconImage(ImageLoader.load("MSO_Demo.png"));
		setModalExclusionType(ModalExclusionType.APPLICATION_EXCLUDE);
		setTitle("Morpho MSO Demo Java " + MsoDemo.MORPHOSMART_SDK_JAVA_VERSION);
		setResizable(false);
//		setDefaultCloseOperation(JDialog.EXIT_ON_CLOSE);
		setSize(400, 300);
		setLocationRelativeTo(driverReg);

		// Loading MorphoSDK and MSOSECU libraries
		try {
			System.loadLibrary("MorphoSmartSDKJavaWrapper");//MSOSECUJavaWrapper
			if (System.getProperty("os.name").startsWith("Windows")) {
				// load MSOSECU library on Windows ONLY
				System.loadLibrary("MSOSECUJavaWrapper");
			}
		} catch (UnsatisfiedLinkError e) {
			e.printStackTrace();
			DialogUtils.showErrorMessage("Load Library Error", "Native code library failed to load." + " See the chapter on Dynamic Linking Problems in the SWIG Java documentation for help.\nError: " + e.getMessage());
			dispose();
			System.exit(1);
		}

		// fill ComboBoxes with values
		Integer[] baudRate = { 50, 75, 100, 110, 150, 200, 220, 300, 400, 440, 600, 880, 1200, 1440, 2000, 2400, 3600, 4000, 4800, 7200, 8000, 9600, 14400, 16000, 19200, 28800, 38400, 56000, 57600, 76800, 115200, 153600, 230400, 460800, 921600 };
		comboBaudRate = new JComboBox(baudRate);
		comboBaudRate.setSelectedItem(115200);
		String[] serialPort = new String[256];
		for (int i = 0; i < 256; i++)
			serialPort[i] = "COM" + (i + 1);
		comboSerialPort = new JComboBox(serialPort);

		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new BorderLayout(0, 0));

		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		contentPane.add(tabbedPane, BorderLayout.CENTER);

		JPanel panelButtons = new JPanel();
		contentPane.add(panelButtons, BorderLayout.SOUTH);
		panelButtons.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

		JButton btnOk = new JButton("   OK   ");
		// give the focus to the OK button
		getRootPane().setDefaultButton(btnOk);
		btnOk.requestFocus();
		// btnOk action
		btnOk.addActionListener(btnOkAction);
		panelButtons.add(btnOk);

		JButton btnCancel = new JButton(" Cancel ");
		btnCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// Close connection JFrame
				mDevice.closeDevice();
				dispose();
			}
		});
		panelButtons.add(btnCancel);

		JPanel panelRS232 = new JPanel();
		panelRS232.setBorder(new TitledBorder(null, "Serial Port Properties", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		tabbedPane.addTab("RS232", panelRS232);

		GroupLayout gl_panelRS232 = new GroupLayout(panelRS232);
		gl_panelRS232.setHorizontalGroup(gl_panelRS232.createParallelGroup(Alignment.LEADING).addGroup(
				gl_panelRS232.createSequentialGroup().addGap(58).addGroup(gl_panelRS232.createParallelGroup(Alignment.LEADING).addComponent(lblSerialPort).addComponent(comboSerialPort, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)).addGap(71).addGroup(gl_panelRS232.createParallelGroup(Alignment.LEADING).addComponent(comboBaudRate, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE).addComponent(lblBaudRate))
						.addContainerGap(133, Short.MAX_VALUE)));
		gl_panelRS232.setVerticalGroup(gl_panelRS232.createParallelGroup(Alignment.LEADING).addGroup(
				gl_panelRS232.createSequentialGroup().addGap(55).addGroup(gl_panelRS232.createParallelGroup(Alignment.BASELINE).addComponent(lblSerialPort).addComponent(lblBaudRate)).addPreferredGap(ComponentPlacement.RELATED)
						.addGroup(gl_panelRS232.createParallelGroup(Alignment.BASELINE).addComponent(comboSerialPort, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE).addComponent(comboBaudRate, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)).addContainerGap(106, Short.MAX_VALUE)));
		panelRS232.setLayout(gl_panelRS232);

		JPanel panelUSB = new JPanel();
		panelUSB.setBorder(new TitledBorder(null, "MSO Serial Number", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		tabbedPane.addTab("USB", panelUSB);
		lblMsoName.setHorizontalAlignment(SwingConstants.CENTER);
		lblMsoName.setFont(new Font("Tahoma", Font.BOLD, 14));

		GroupLayout gl_panelUSB = new GroupLayout(panelUSB);
		gl_panelUSB.setHorizontalGroup(gl_panelUSB.createParallelGroup(Alignment.LEADING).addGroup(gl_panelUSB.createSequentialGroup().addGap(48).addGroup(gl_panelUSB.createParallelGroup(Alignment.LEADING).addComponent(lblMsoName).addComponent(comboDeviceListUSB, GroupLayout.PREFERRED_SIZE, 258, GroupLayout.PREFERRED_SIZE)).addContainerGap(61, Short.MAX_VALUE)));
		gl_panelUSB.setVerticalGroup(gl_panelUSB.createParallelGroup(Alignment.LEADING).addGroup(gl_panelUSB.createSequentialGroup().addGap(61).addComponent(lblMsoName).addGap(18).addComponent(comboDeviceListUSB, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE).addContainerGap(62, Short.MAX_VALUE)));
		gl_panelUSB.linkSize(SwingConstants.HORIZONTAL, new Component[] { comboDeviceListUSB, lblMsoName });
		panelUSB.setLayout(gl_panelUSB);

		// By default select USB tab
		tabbedPane.setSelectedIndex(1);

		// close action
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent we) {
				mDevice.closeDevice();
				//System.exit(0);
			}
		});

		// get USB devices
		mDevice = new MorphoDevice();
		long[] nbUsbDevice = { 0 };
		int ret = mDevice.initUsbDevicesNameEnum(nbUsbDevice);
		if (ret == 0) {
			if (nbUsbDevice[0] > 0) {
				for (int i = 0; i < nbUsbDevice[0]; ++i) {
					comboDeviceListUSB.addItem(mDevice.getUsbDeviceName(i));
					listDeviceNames.add(mDevice.getUsbDevicePropertie(i));
				}
				// initaliser le nom du device
				lblMsoName.setText(listDeviceNames.get(comboDeviceListUSB.getSelectedIndex()));
			} else {
				DialogUtils.showWarningMessage("Warning", "No device detected");
			}
		} else {
			DialogUtils.showErrorMessage("Error", "morphoDevice Error");
		}

		// Associate actions
		comboDeviceListUSB.addActionListener(comboDeviceListUSBAction);
	}

	//
	// Listeners-------------------------------------------------------------------------------------
	//
	MsoInit msoInit = new MsoInit();
	private ActionListener btnOkAction = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent event) {
			// Launch MSO initialization
			new Thread(new Runnable() {
				@Override
				public void run() {
					//msoInit.setVisible(true);
					msoInit.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				}
			}).start();

			new Thread(new Runnable() {

				@Override
				public void run() {
					int ret = 0;

					// RS232
					if (tabbedPane.getSelectedIndex() == 0) {
						int baudRate = Integer.parseInt(comboBaudRate.getSelectedItem().toString());
						short serialPort = (short) (comboSerialPort.getSelectedIndex() + 1);
						msoInit.setOpen("Open RS232 COM" + serialPort);
						ret = mDevice.openDevice(serialPort, baudRate);
					}
					// USB
					else {
						msoInit.setOpen("Open USB");
						// open connection with selected device
						String serialNumber = (String) comboDeviceListUSB.getSelectedItem();
						ret = mDevice.openUsbDevice(serialNumber, 30);
					}

					short isOfferedSecurity = 0;
					short isTunneling = 0;
					byte[] hostCertificate = null;

					setVisible(false);
					if (ret == MorphoSmartSDK.MORPHO_OK) {
						msoInit.setStatusOpen("OK");
						// Setup security
						String[] secuSerialNumber = { "" };
						byte[] secuConfig = { 0 };
						MorphoFAR[] secuFar = { MorphoFAR.MORPHO_FAR_0 };
						long[] secuMinMSL = { 0 };
						ret = mDevice.getSecuConfig(secuSerialNumber, secuConfig, secuFar, secuMinMSL);

						isOfferedSecurity = (short) (((short) secuConfig[0]) & MorphoSmartSDK.SECU_OFFERED_SECURITY);
						isTunneling = (short) (((short) secuConfig[0]) & MorphoSmartSDK.SECU_TUNNELING);

						if (!System.getProperty("os.name").startsWith("Windows")) {
							// linux is not supported with secured devices
							if (isOfferedSecurity != 0 || isTunneling != 0) {
								DialogUtils.showErrorMessage("MSO Connection", "Secured devices are not supported on Linux");
								dispose();
								System.exit(1);
							}
						}

						IMsoSecu msoSecu = new MsoSecu();

						if (isTunneling != 0) {
							msoInit.setCertification("Host Certification");
							msoInit.setCommunicationInitialisation("Tunneling communication initialisation");
							msoInit.setStatusMsoConfig("Tunneling");
							msoInit.setStatusSecurityCom("please wait a few seconds...");

							if (isMultiProtectStarted()) {
								int q = DialogUtils.showQuestionMessage("MSO Connection", "The service \"Sagem Licence Service\" is not compatible with products with Tunneling configuration ,\n it may cause problems in the enrollment process. \nPlease stop this service before using MSO_Demo.\n\nContinue anyway ?");
								if (q == JOptionPane.NO_OPTION) {
									dispose();
									msoInit.dispose();
									return;
								}
							}

							ArrayList<Byte> certifHost = new ArrayList<Byte>();
							ret = msoSecu.getHostCertif(certifHost);

							if (ret != MorphoSmartSDK.MORPHO_OK) {
								DialogUtils.showErrorMessage("Connection", "Cannot read host certificate (error " + ret + ").\nPlease check that:\n1/ You have generated the HOST keys (with the MKMS or other)\n2/ You have copied host.der (HOST public certificate), host.key (HOST private key),\n ca.crt (CA public key that can be found in the MKMS station) in the directory .\\openssl\\keys.");
								dispose();
								msoInit.dispose();
								return;
							} else {
								msoInit.setStatusMsoCertif("OK");
								hostCertificate = new byte[certifHost.size()];
								for (int i = 0; i < certifHost.size(); i++) {
									hostCertificate[i] = certifHost.get(i);
								}

								ret = SecurityMgt.tunnelingOpen(mDevice, msoSecu, hostCertificate);
								if (ret != MorphoSmartSDK.MORPHO_OK) {
									DialogUtils.showErrorMessage("Connection", "Security Tunneling Initialization failed.\nPlease check that:\n1/ You have generated the HOST keys (with the MKMS or other)\n2/ You have copied host.der (HOST public certificate), host.key (HOST private key), ca.crt (CA public key that can be found in the MKMS station) in the directory .\\openssl\\keys.");
									dispose();
									msoInit.dispose();
									return;
								} else {
									msoInit.setStatusSecurityCom("OK");
								}
							}
						} else if (isOfferedSecurity != 0) {
							msoInit.setCommunicationInitialisation("Offered communication initialisation");
							msoInit.setStatusMsoConfig("Offered");
							msoInit.setCertification("The MSO Certification");

							ret = SecurityMgt.offeredSecuOpen(mDevice, msoSecu);
							if (ret != MorphoSmartSDK.MORPHO_OK) {
								DialogUtils.showErrorMessage("Connection", "Offered Security Initialization failed", ret, mDevice.getInternalError());
								int q = DialogUtils.showQuestionMessage("Connection", "The MorphoSmart Certificate has not been authenticated.\nPlease check that you have copied ca.crt (CA public key that can be found in the MKMS station) in the directory .\\openssl\\keys.\nContinue anyway ?");
								if (q == JOptionPane.NO_OPTION) {
									dispose();
									msoInit.dispose();
									return;
								}
							} else {
								msoInit.setStatusMsoCertif("OK");
								msoInit.setStatusSecurityCom("OK");
							}
						}

						ArrayList<String> productName = new ArrayList<String>();
						ret = mDevice.getDescriptorBin((short) MorphoSmartSDK.BINDESC_PRODUCT_NAME, productName);

						if (ret == MorphoSmartSDK.MORPHO_OK && productName.size() > 0) {
							// The device is FVP ?
							if (productName.get(0).endsWith("FINGER VP")) {
								MorphoInfo.isFVP = true;
							}
						} else {
							DialogUtils.showErrorMessage("Connection", "An error occured while calling MorphoDevice.getDescriptorBin() function", ret, mDevice.getInternalError());
						}

						boolean isBaseAvailable = false;
						mDatabase = new MorphoDatabase();
						ret = mDevice.getDatabase((short) 0, "", mDatabase);
						if (ret != MorphoSmartSDK.MORPHO_OK && ret != MorphoSmartSDK.MORPHOERR_BASE_NOT_FOUND) {
							DialogUtils.showErrorMessage("Connection", "An error occured while calling MorphoDevice.getDatabase() function", ret, mDevice.getInternalError());
						}

						int[] nbUsedRecord = { 0 };
						ret = mDatabase.getNbUsedRecord(nbUsedRecord);

						if (ret == MorphoSmartSDK.MORPHOERR_BASE_NOT_FOUND) {
							msoInit.setStatusDatabaseConf("no detected database");
							ret = DialogUtils.showQuestionMessage("Connection", "There is currently no internal database available in the MorphoSmartSDK.\nWould you like to create one?");
							if (ret == JOptionPane.YES_OPTION) {
								DialogCreateDB dialogCreateDB = new DialogCreateDB(MsoConnection.this);
								dialogCreateDB.setDevice(mDevice);
								dialogCreateDB.setDatabase(mDatabase);
								dialogCreateDB.setVisible(true);
								if (dialogCreateDB.getSdkError() == 0) {
									isBaseAvailable = true;
									msoInit.setStatusDatabaseConf("OK");
								}
							}
						} else if (ret != MorphoSmartSDK.MORPHO_OK) {
							DialogUtils.showErrorMessage("Connection", "An error occured while calling MorphoDatabase.getNbUsedRecord() function", ret, mDevice.getInternalError());
						} else {
							isBaseAvailable = true;
							msoInit.setStatusDatabaseConf("OK");
						}

						// Close connection JFrame
						dispose();
						// Launch MSO Demo
						boolean offeredSecurity = (isOfferedSecurity != 0) ? true : false;
						boolean tunneling = (isTunneling != 0) ? true : false;
						MsoDemo msoDemoFrame = new MsoDemo(null, mDevice, (String) comboDeviceListUSB.getSelectedItem(), mDatabase, isBaseAvailable, hostCertificate, offeredSecurity, tunneling);
						msoDemoFrame.setVisible(true);
					} else {
						DialogUtils.showErrorMessage("Connection Error", "The connection with the MSO failed. Please check the device is correctly plugged.");
						dispose();
					}
					msoInit.dispose();
				}
			}).start();
		}
	};

	
	private ActionListener comboDeviceListUSBAction = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			JComboBox cb = (JComboBox) e.getSource();
			int index = cb.getSelectedIndex();
			lblMsoName.setText(listDeviceNames.get(index));
		}
	};

	private boolean isMultiProtectStarted() {
		if (!System.getProperty("os.name").startsWith("Windows")) {
			return false;
		}

		boolean ret = false;
		try {
			String process1 = "SagemSecurite_Licence_Protection.exe";
			String process2 = "SagemSecurite_License_Protection.exe";

			File file = File.createTempFile("realhowto", ".vbs");
			file.deleteOnExit();
			FileWriter fw = new java.io.FileWriter(file);

			String vbs = "Set WshShell = WScript.CreateObject(\"WScript.Shell\")\n" + "Set locator = CreateObject(\"WbemScripting.SWbemLocator\")\n" + "Set service = locator.ConnectServer()\n" + "Set processes = service.ExecQuery _\n" + " (\"select * from Win32_Process where name='" + process1 + "' or name='" + process2 + "'\")\n" + "For Each process in processes\n" + "wscript.echo process.Name \n" + "Next\n" + "Set WSHShell = Nothing\n";

			fw.write(vbs);
			fw.close();
			Process p = Runtime.getRuntime().exec("cscript //NoLogo " + file.getPath());
			BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line;
			line = input.readLine();
			if (line != null) {
				if (line.equals(process1) || line.equals(process2)) {
					ret = true;
				}
			}
			input.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
	}
	
	public void btnOkActionPerformed(final DriverRegistrationWindow driverReg,
			boolean enrollTabVisibility, boolean identifyTabVisibility) {

		// Launch MSO initialization
		
		 MorphoConstant.TAB_ENROLL_VISIBLE = enrollTabVisibility;
		 MorphoConstant.TAB_IDENTIFY_VISIBLE = identifyTabVisibility;
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				//msoInit.setVisible(true);
				msoInit.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			}
		}).start();

		new Thread(new Runnable() {

			@Override
			public void run() {
				int ret = 0;

				// RS232
				if (tabbedPane.getSelectedIndex() == 0) {
					int baudRate = Integer.parseInt(comboBaudRate.getSelectedItem().toString());
					short serialPort = (short) (comboSerialPort.getSelectedIndex() + 1);
					msoInit.setOpen("Open RS232 COM" + serialPort);
					ret = mDevice.openDevice(serialPort, baudRate);
				}
				// USB
				else {
					msoInit.setOpen("Open USB");
					// open connection with selected device
					String serialNumber = (String) comboDeviceListUSB.getSelectedItem();
					ret = mDevice.openUsbDevice(serialNumber, 30);
				}

				short isOfferedSecurity = 0;
				short isTunneling = 0;
				byte[] hostCertificate = null;

				setVisible(false);
				if (ret == MorphoSmartSDK.MORPHO_OK) {
					msoInit.setStatusOpen("OK");
					// Setup security
					String[] secuSerialNumber = { "" };
					byte[] secuConfig = { 0 };
					MorphoFAR[] secuFar = { MorphoFAR.MORPHO_FAR_0 };
					long[] secuMinMSL = { 0 };
					ret = mDevice.getSecuConfig(secuSerialNumber, secuConfig, secuFar, secuMinMSL);

					isOfferedSecurity = (short) (((short) secuConfig[0]) & MorphoSmartSDK.SECU_OFFERED_SECURITY);
					isTunneling = (short) (((short) secuConfig[0]) & MorphoSmartSDK.SECU_TUNNELING);

					if (!System.getProperty("os.name").startsWith("Windows")) {
						// linux is not supported with secured devices
						if (isOfferedSecurity != 0 || isTunneling != 0) {
							DialogUtils.showErrorMessage("MSO Connection", "Secured devices are not supported on Linux");
							dispose();
							System.exit(1);
						}
					}

					IMsoSecu msoSecu = new MsoSecu();

					if (isTunneling != 0) {
						msoInit.setCertification("Host Certification");
						msoInit.setCommunicationInitialisation("Tunneling communication initialisation");
						msoInit.setStatusMsoConfig("Tunneling");
						msoInit.setStatusSecurityCom("please wait a few seconds...");

						if (isMultiProtectStarted()) {
							int q = DialogUtils.showQuestionMessage("MSO Connection", "The service \"Sagem Licence Service\" is not compatible with products with Tunneling configuration ,\n it may cause problems in the enrollment process. \nPlease stop this service before using MSO_Demo.\n\nContinue anyway ?");
							if (q == JOptionPane.NO_OPTION) {
								dispose();
								msoInit.dispose();
								return;
							}
						}

						ArrayList<Byte> certifHost = new ArrayList<Byte>();
						ret = msoSecu.getHostCertif(certifHost);

						if (ret != MorphoSmartSDK.MORPHO_OK) {
							DialogUtils.showErrorMessage("Connection", "Cannot read host certificate (error " + ret + ").\nPlease check that:\n1/ You have generated the HOST keys (with the MKMS or other)\n2/ You have copied host.der (HOST public certificate), host.key (HOST private key),\n ca.crt (CA public key that can be found in the MKMS station) in the directory .\\openssl\\keys.");
							dispose();
							msoInit.dispose();
							return;
						} else {
							msoInit.setStatusMsoCertif("OK");
							hostCertificate = new byte[certifHost.size()];
							for (int i = 0; i < certifHost.size(); i++) {
								hostCertificate[i] = certifHost.get(i);
							}

							ret = SecurityMgt.tunnelingOpen(mDevice, msoSecu, hostCertificate);
							if (ret != MorphoSmartSDK.MORPHO_OK) {
								DialogUtils.showErrorMessage("Connection", "Security Tunneling Initialization failed.\nPlease check that:\n1/ You have generated the HOST keys (with the MKMS or other)\n2/ You have copied host.der (HOST public certificate), host.key (HOST private key), ca.crt (CA public key that can be found in the MKMS station) in the directory .\\openssl\\keys.");
								dispose();
								msoInit.dispose();
								return;
							} else {
								msoInit.setStatusSecurityCom("OK");
							}
						}
					} else if (isOfferedSecurity != 0) {
						msoInit.setCommunicationInitialisation("Offered communication initialisation");
						msoInit.setStatusMsoConfig("Offered");
						msoInit.setCertification("The MSO Certification");

						ret = SecurityMgt.offeredSecuOpen(mDevice, msoSecu);
						if (ret != MorphoSmartSDK.MORPHO_OK) {
							DialogUtils.showErrorMessage("Connection", "Offered Security Initialization failed", ret, mDevice.getInternalError());
							int q = DialogUtils.showQuestionMessage("Connection", "The MorphoSmart Certificate has not been authenticated.\nPlease check that you have copied ca.crt (CA public key that can be found in the MKMS station) in the directory .\\openssl\\keys.\nContinue anyway ?");
							if (q == JOptionPane.NO_OPTION) {
								dispose();
								msoInit.dispose();
								return;
							}
						} else {
							msoInit.setStatusMsoCertif("OK");
							msoInit.setStatusSecurityCom("OK");
						}
					}

					ArrayList<String> productName = new ArrayList<String>();
					ret = mDevice.getDescriptorBin((short) MorphoSmartSDK.BINDESC_PRODUCT_NAME, productName);

					if (ret == MorphoSmartSDK.MORPHO_OK && productName.size() > 0) {
						// The device is FVP ?
						if (productName.get(0).endsWith("FINGER VP")) {
							MorphoInfo.isFVP = true;
						}
					} else {
						DialogUtils.showErrorMessage("Connection", "An error occured while calling MorphoDevice.getDescriptorBin() function", ret, mDevice.getInternalError());
					}

					boolean isBaseAvailable = false;
					mDatabase = new MorphoDatabase();
					ret = mDevice.getDatabase((short) 0, "", mDatabase);
					if (ret != MorphoSmartSDK.MORPHO_OK && ret != MorphoSmartSDK.MORPHOERR_BASE_NOT_FOUND) {
						DialogUtils.showErrorMessage("Connection", "An error occured while calling MorphoDevice.getDatabase() function", ret, mDevice.getInternalError());
					}

					int[] nbUsedRecord = { 0 };
					ret = mDatabase.getNbUsedRecord(nbUsedRecord);

					if (ret == MorphoSmartSDK.MORPHOERR_BASE_NOT_FOUND) {
						msoInit.setStatusDatabaseConf("no detected database");
						ret = DialogUtils.showQuestionMessage("Connection", "There is currently no internal database available in the MorphoSmartSDK.\nWould you like to create one?");
						if (ret == JOptionPane.YES_OPTION) {
							DialogCreateDB dialogCreateDB = new DialogCreateDB(MsoConnection.this);
							dialogCreateDB.setDevice(mDevice);
							dialogCreateDB.setDatabase(mDatabase);
							dialogCreateDB.setVisible(true);
							if (dialogCreateDB.getSdkError() == 0) {
								isBaseAvailable = true;
								msoInit.setStatusDatabaseConf("OK");
							}
						}
					} else if (ret != MorphoSmartSDK.MORPHO_OK) {
						DialogUtils.showErrorMessage("Connection", "An error occured while calling MorphoDatabase.getNbUsedRecord() function", ret, mDevice.getInternalError());
					} else {
						isBaseAvailable = true;
						msoInit.setStatusDatabaseConf("OK");
					}

					// Close connection JFrame
					dispose();
					// Launch MSO Demo
					boolean offeredSecurity = (isOfferedSecurity != 0) ? true : false;
					boolean tunneling = (isTunneling != 0) ? true : false;
					new MsoDemo(driverReg,mDevice, (String) comboDeviceListUSB.getSelectedItem(), mDatabase, isBaseAvailable, hostCertificate, offeredSecurity, tunneling).setVisible(true);
				
				} else {
					DialogUtils.showErrorMessage("Connection Error", "The connection with the MSO failed. Please check the device is correctly plugged.");
					dispose();
				}
				msoInit.dispose();
			}
		}).start();
		
	}

	
	
	
}
