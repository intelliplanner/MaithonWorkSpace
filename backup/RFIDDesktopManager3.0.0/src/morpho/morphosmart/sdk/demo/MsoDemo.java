package morpho.morphosmart.sdk.demo;

import java.awt.Cursor;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

import com.ipssi.rfid.ui.DriverRegistrationWindow;
import com.scl.loadlibrary.BioMatricBean;

import morpho.morphosmart.sdk.api.IMsoSecu;
import morpho.morphosmart.sdk.api.MorphoCallbackCommand;
import morpho.morphosmart.sdk.api.MorphoDatabase;
import morpho.morphosmart.sdk.api.MorphoDevice;
import morpho.morphosmart.sdk.api.MorphoFAR;
import morpho.morphosmart.sdk.api.MorphoSmartSDK;
import morpho.morphosmart.sdk.api.MorphoTemplateList;
import morpho.morphosmart.sdk.demo.dialog.DialogEnableBioDataEncryption;
import morpho.morphosmart.sdk.demo.dialog.DialogReadMsoConf;
import morpho.morphosmart.sdk.demo.dialog.DialogResultWindow;
import morpho.morphosmart.sdk.demo.dialog.DialogUtils;
import morpho.morphosmart.sdk.demo.ennum.EnumTabIndex;
import morpho.morphosmart.sdk.demo.trt.ErrorsMgt;
import morpho.morphosmart.sdk.demo.trt.FilesMgt;
import morpho.morphosmart.sdk.demo.trt.ImageLoader;
import morpho.morphosmart.sdk.demo.trt.MorphoInfo;
import morpho.morphosmart.sdk.demo.trt.SecurityMgt;
import morpho.msosecu.sdk.api.MsoSecu;

/**
 * @author Vi$ky
 *
 */
public class MsoDemo extends JDialog{

	private static final long serialVersionUID = 1L;
	// left and right parts of the MSO_Demo app
	protected PanelBasicBiometricOperation leftPanel;
	protected PanelDatabaseMgt rightPanel;
	private byte[] hostCertificate;
	private boolean isDataEncryption = false;

	private boolean OFFERED_SECURITY = false;
	private boolean TUNNELING = false;
	public boolean isBaseAvailable = false;
	// MorphoSmart SDK
	private MorphoDevice mDevice;
	private MorphoDatabase mDatabase;
	public static String MORPHOSMART_SDK_JAVA_VERSION = "V6.13.2.0";

	// SDK variables
	public int wakeUpMode = 0;
	public int callbackMask = MorphoCallbackCommand.MORPHO_CALLBACK_IMAGE_CMD.swigValue() | MorphoCallbackCommand.MORPHO_CALLBACK_ENROLLMENT_CMD.swigValue() | MorphoCallbackCommand.MORPHO_CALLBACK_COMMAND_CMD.swigValue() | MorphoCallbackCommand.MORPHO_CALLBACK_CODEQUALITY.swigValue() | MorphoCallbackCommand.MORPHO_CALLBACK_DETECTQUALITY.swigValue();

	JMenuItem miReadMsoConfiguration = new JMenuItem("Read MSO Configuration");
	JMenuItem miLoadPkcs = new JMenuItem("Load Pkcs12");
	JMenuItem miStoreCertificate = new JMenuItem("Store Certificate");
	JMenuItem miReadCertificate = new JMenuItem("Read Certificate");
	JMenuItem miVerifySign = new JMenuItem("Verify Sign X9.84");
	JMenuItem miExtractSignerId = new JMenuItem("Extract SignerId X984");
	JMenuItem miLoadKsUnsecure = new JMenuItem("Load Ks in Unsecure Mode");
	JMenuItem miLoadKsSymmetric = new JMenuItem("Load Ks in Symmetric Secure Mode");
	JMenuItem miLoadKsAsymmetric = new JMenuItem("Load Ks in Asymmetric Secure Mode");
	JMenuItem miGetKs = new JMenuItem("Get Ks KCV");
	JMenuItem miEnableBiometricData = new JMenuItem("Enable Biometric Data Encryption");

	JMenuItem miReset = new JMenuItem("Reset");
	JMenuItem miLoad = new JMenuItem("Load");
	JMenuItem miSaveAs = new JMenuItem("Save as");
	public String serialNumber = "";

	public InputStream load(String path) {
		InputStream input = MsoDemo.class.getResourceAsStream(path);
		if (input == null)
			input = MsoDemo.class.getResourceAsStream("/" + path);
		return input;
	}

	/**
	 * Create the frame.
	 * 
	 * @param mDevice
	 * @param sn
	 * @param mDatabase
	 * @param hostCertificate
	 * @param isTUNNELING
	 * @param isOFFERED_SECURITY
	 */
	
	public MsoDemo(MorphoDevice mDevice, MorphoDatabase mDatabase, boolean isBaseAvailable) {
		this.mDevice = mDevice;
		this.mDatabase = mDatabase;
		this.isBaseAvailable = isBaseAvailable;
		updateDBInformation();
	}
	
	public MsoDemo(DriverRegistrationWindow driverReg,MorphoDevice mDevice, String sn, MorphoDatabase mDatabase, boolean isBaseAvailable, byte[] hostCertificate, boolean isOFFERED_SECURITY, boolean isTUNNELING) {
		super(driverReg,true);
		this.mDevice = mDevice;
		this.mDatabase = mDatabase;
		this.serialNumber = sn;
		this.isBaseAvailable = isBaseAvailable;
		this.setHostCertificate(hostCertificate);
		this.setTUNNELING(isTUNNELING);
		this.setOFFERED_SECURITY(isOFFERED_SECURITY);

		setIconImage(ImageLoader.load("MSO_Demo.png"));
		setTitle("Morpho MSO Demo Java " + MORPHOSMART_SDK_JAVA_VERSION);
//		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		if (System.getProperty("os.name").startsWith("Windows")) {
			setSize(405, 720);//setSize(790, 750);
		} else {
			setSize(900, 720);
		}
		
		setLocationRelativeTo(driverReg);
//		this.setLocation(500, 200);
		setResizable(false);
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		// Security menu, this menu is used only in Windows OS
		if (System.getProperty("os.name").startsWith("Windows")) {
			JMenu mnSecurity = new JMenu("Security");
			menuBar.add(mnSecurity);
			mnSecurity.add(miReadMsoConfiguration);
			mnSecurity.add(miLoadPkcs);
			mnSecurity.add(miStoreCertificate);
			mnSecurity.add(miReadCertificate);
			mnSecurity.addSeparator(); // Separator
			mnSecurity.add(miVerifySign);
			mnSecurity.add(miExtractSignerId);
			mnSecurity.addSeparator(); // Separator
			mnSecurity.add(miLoadKsUnsecure);
			mnSecurity.add(miLoadKsAsymmetric);
			mnSecurity.add(miLoadKsSymmetric);
			mnSecurity.add(miGetKs);
			mnSecurity.add(miEnableBiometricData);
		}

		// Tools menu
		JMenu mnTools = new JMenu("Tools");
		menuBar.add(mnTools);
		menuBar.setVisible(false);
		JMenu mnToolsSubmenu = new JMenu("FVP MMI File");
		mnTools.add(mnToolsSubmenu);

		mnToolsSubmenu.add(miSaveAs);

		mnToolsSubmenu.add(miLoad);

		mnToolsSubmenu.add(miReset);

		JPanel contentPane = new JPanel();
		
		contentPane.setLayout(new GridLayout(1, 2));
		setContentPane(contentPane);

		// Add panels
		leftPanel = new PanelBasicBiometricOperation(this);
		rightPanel = new PanelDatabaseMgt(this);
		contentPane.add(leftPanel);
//		contentPane.add(rightPanel);

		// close action
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent we) {
				closeDevice();
				//System.exit(0);
			}
		});

		enableDataBaseFunction(isBaseAvailable);
		updateDataBaseInformation();
		initSensorWinPosition();

		miReadMsoConfiguration.addActionListener(miReadMsoConfigurationAL);
		miLoadPkcs.addActionListener(miLoadPkcsAL);
		miStoreCertificate.addActionListener(miStoreCertificateAL);
		miReadCertificate.addActionListener(miReadCertificateAL);
		miVerifySign.addActionListener(miVerifySignAL);
		miExtractSignerId.addActionListener(miExtractSignerIdAL);
		miLoadKsUnsecure.addActionListener(miLoadKsUnsecureAL);
		miLoadKsSymmetric.addActionListener(miLoadKsSymmetricAL);
		miLoadKsAsymmetric.addActionListener(miLoadKsAsymmetricAL);
		miGetKs.addActionListener(miGetKsAL);
		miEnableBiometricData.addActionListener(miEnableBiometricDataAL);

		miSaveAs.addActionListener(miSaveAsAL);
		miReset.addActionListener(miResetAL);
		miLoad.addActionListener(miLoadAL);

		if (isTUNNELING() || isOFFERED_SECURITY()) {
			// device is secure: loading Ks using unsecure mode will be
			// forbidden
			miLoadKsUnsecure.setEnabled(false);
		} else {
			// device is not secure: Ks cannot be loaded using asymmetric secure
			// mode
			miLoadKsAsymmetric.setEnabled(false);
		}

		FilesMgt.checkFFDLogParam(sn);
	
	}


	private ActionListener miSaveAsAL = new ActionListener() {

		@Override
		public void actionPerformed(ActionEvent arg0) {
			ArrayList<Byte> mmifile = new ArrayList<Byte>();
			int ret = mDevice.getConfigParam(MorphoSmartSDK.CONFIG_UI_CONFIG_TAG, mmifile);

			String result = "FVP MMI File";
			String message1 = "";
			String message2 = "";
			String message3 = "";

			if (ret != 0) {
				message1 = "Failure";
				message2 = "Unable to save MMI file.";
				message3 = ErrorsMgt.convertSDKError(ret);
			} else {
				byte[] data = new byte[mmifile.size()];
				for (int i = 0; i < mmifile.size(); ++i) {
					data[i] = mmifile.get(i);
				}
				if (FilesMgt.saveMMIFile(MsoDemo.this, "UIConfig.csv", data)) {
					message1 = "Success";
					message2 = "File UIConfig.csv is saved";
				}
			}
			new DialogResultWindow(MsoDemo.this, result, message1, message2, message3, "").setVisible(true);
		}
	};

	private ActionListener miResetAL = new ActionListener() {

		@Override
		public void actionPerformed(ActionEvent arg0) {
			byte[] paramValue = { 1 };
			int ret = mDevice.setConfigParam(MorphoSmartSDK.CONFIG_UI_CONFIG_TAG, paramValue);
			if (ret == MorphoSmartSDK.MORPHO_OK) {
				new DialogResultWindow(MsoDemo.this, "FVP MMI File", "Reset Success", "", "To apply the reset, restart FVP, please", "").setVisible(true);
			} else {
				new DialogResultWindow(MsoDemo.this, "FVP MMI File", "Reset Error", "", ErrorsMgt.convertSDKError(ret), "").setVisible(true);
			}
		}
	};

	private ActionListener miLoadAL = new ActionListener() {

		@Override
		public void actionPerformed(ActionEvent arg0) {
			byte[] mmiFile = FilesMgt.readMMIFile(MsoDemo.this);
			if (mmiFile != null) {
				if (mmiFile.length >= 4 * 1024) {
					new DialogResultWindow(MsoDemo.this, "FVP MMI File Error", "The file size is too high", "File size:", mmiFile.length + " Bytes (Max : 4 KB)", "").setVisible(true);
				} else {
					int ret = mDevice.setConfigParam(MorphoSmartSDK.CONFIG_UI_CONFIG_TAG, mmiFile);
					if (ret == MorphoSmartSDK.MORPHO_OK) {
						new DialogResultWindow(MsoDemo.this, "FVP MMI File", "Success", "MMI File is loaded", "To apply the update, restart FVP, please", "").setVisible(true);
					} else {
						new DialogResultWindow(MsoDemo.this, "FVP MMI File", "Failure", "MMI File is not loaded.", ErrorsMgt.convertSDKError(ret), "").setVisible(true);
					}
				}
			} else {
				new DialogResultWindow(MsoDemo.this, "FVP MMI File Error", "aborted by user", "", "", "").setVisible(true);
			}
		}
	};

	private ActionListener miReadMsoConfigurationAL = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			String[] secuSerialNumber = { "" };
			byte[] secuConfig = { 0 };
			MorphoFAR[] secuFar = { MorphoFAR.MORPHO_FAR_0 };
			long[] secuMinMSL = { 0 };
			int ret = mDevice.getSecuConfig(secuSerialNumber, secuConfig, secuFar, secuMinMSL);
			if (ret == MorphoSmartSDK.MORPHO_OK) {
				DialogReadMsoConf dialog = new DialogReadMsoConf(MsoDemo.this);
				dialog.setMsoSerialNumber(secuSerialNumber[0]);
				dialog.setMaxFAFR(secuFar[0].toString());
				String msl = "";
				switch((int)secuMinMSL[0])
				{
					case MorphoSmartSDK.MULTIMODAL_SECURITY_STANDARD: msl = "Standard"; break;
					case MorphoSmartSDK.MULTIMODAL_SECURITY_MEDIUM: msl = "Medium"; break;
					case MorphoSmartSDK.MULTIMODAL_SECURITY_HIGH: msl = "High"; break;										
				}
				dialog.setMinimumMultimodalSecurity(msl);
				dialog.setIsProtectedWithSignature(((secuConfig[0] & MorphoSmartSDK.SECU_SIGNED_DOWNLOAD) != 0 ? true : false));
				dialog.setIsTunneling(((secuConfig[0] & MorphoSmartSDK.SECU_TUNNELING) != 0 ? true : false));
				dialog.setIsTunneling(((secuConfig[0] & MorphoSmartSDK.SECU_TUNNELING) != 0 ? true : false));
				dialog.setIsOfferedSecurity(((secuConfig[0] & MorphoSmartSDK.SECU_OFFERED_SECURITY) != 0 ? true : false));
				dialog.setIsAcceptOnlyPkSigned(((secuConfig[0] & MorphoSmartSDK.SECU_PK_ONLY_SIGNED) != 0 ? true : false));
				dialog.setIsExportScore(((secuConfig[0] & MorphoSmartSDK.SECU_NEVER_EXPORT_SCORE) == 0 ? true : false));
				// show dialog
				dialog.fillTable();
				dialog.setVisible(true);
			}
		}
	};

	private ActionListener miLoadPkcsAL = new ActionListener() {

		@Override
		public void actionPerformed(ActionEvent arg0) {
			byte[] pkcs12 = FilesMgt.readPKCS12File(MsoDemo.this);
			boolean isFileOK = false;
			if (pkcs12 != null) {
				byte[] antireplayCounter = FilesMgt.getAntireplayCounterFromFile(MsoDemo.this);
				if (antireplayCounter != null) {
					isFileOK = true;
					int ac = ByteBuffer.wrap(antireplayCounter).order(ByteOrder.LITTLE_ENDIAN).getInt();

					int ret = 0;

					if (MsoDemo.this.isTUNNELING()) {
						ret = SecurityMgt.tunnelingClose(MsoDemo.this.mDevice);
					} else if (MsoDemo.this.isOFFERED_SECURITY()) {
						ret = SecurityMgt.offeredSecuClose(MsoDemo.this.mDevice);
					}

					if (ret != MorphoSmartSDK.MORPHO_OK) {
						DialogUtils.showErrorMessage("Load PKCS#12", "Error while cutting encryption");
					} else {
						ret = SecurityMgt.secuStoPkcs12(MsoDemo.this.mDevice, pkcs12, ac);
						if (ret != MorphoSmartSDK.MORPHO_OK) {
							String result = "Load PKCS12 Failed";
							String message1 = "";
							String message2 = "";
							String message3 = "";

							if (ret == MorphoSmartSDK.MORPHOERR_SECU) {
								message1 = "Security failure (could be antireplay or crypto error)";
							} else {
								message1 = "An error occured while calling";
								message2 = "MorphoDevice.secuStoPkcs12() function";
								message3 = ErrorsMgt.convertSDKError(ret);
							}
							new DialogResultWindow(MsoDemo.this, result, message1, message2, message3, "").setVisible(true);
						} else {
							new DialogResultWindow(MsoDemo.this, "Load PKCS12 OK", "", "", "", "").setVisible(true);
						}

						IMsoSecu msoSecu = new MsoSecu();

						if (MsoDemo.this.isTUNNELING()) {
							ret = SecurityMgt.tunnelingOpen(MsoDemo.this.mDevice, msoSecu, MsoDemo.this.hostCertificate);
						} else if (MsoDemo.this.isOFFERED_SECURITY()) {
							ret = SecurityMgt.offeredSecuOpen(MsoDemo.this.mDevice, msoSecu);
						}
					}
				}
			}

			if (!isFileOK) {
				DialogUtils.showErrorMessage("Load PKCS#12", "Error while getting pkcs12 files");
			}
		}
	};

	private ActionListener miStoreCertificateAL = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			byte[] certificate = FilesMgt.readCertificateFile(MsoDemo.this);
			if (certificate != null) {
				String result = "";
				String message1 = "";
				String message2 = "";
				String message3 = "";

				int ret = SecurityMgt.secuStoCertif(mDevice, certificate);
				if (ret != MorphoSmartSDK.MORPHO_OK) {
					result = "Store certificate Failed";

					if (ret == MorphoSmartSDK.MORPHOERR_CERTIF_UNKNOW) {
						message1 = "Signature Certificat Inconnue";
					} else {
						message1 = "An error occured while calling";
						message2 = "MorphoDevice.secuStoCertificate() function";
						message3 = ErrorsMgt.convertSDKError(ret);
					}
					new DialogResultWindow(MsoDemo.this, result, message1, message2, message3, "").setVisible(true);
				} else {
					new DialogResultWindow(MsoDemo.this, "Store Certificate OK", "", "", "", "").setVisible(true);
				}
			} else {
				DialogUtils.showErrorMessage("Store Certificate", "Error while getting certificate");
			}
		}
	};

	private ActionListener miReadCertificateAL = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			ArrayList<Byte> certificate = new ArrayList<Byte>();
			int ret = SecurityMgt.secuReadCertificate(mDevice, (short) 0, certificate);
			boolean isSave = false;
			if (ret == MorphoSmartSDK.MORPHO_OK) {
				byte[] data = new byte[certificate.size()];
				for (int i = 0; i < certificate.size(); ++i) {
					data[i] = certificate.get(i);
				}
				isSave = FilesMgt.saveCertificateFile(MsoDemo.this, "mso_certif.der", data);

				short index = 1;
				while (isSave) {
					ret = SecurityMgt.secuReadCertificate(mDevice, index, certificate);
					if (ret == MorphoSmartSDK.MORPHO_OK) {
						data = new byte[certificate.size()];
						for (int i = 0; i < certificate.size(); ++i) {
							data[i] = certificate.get(i);
						}
						isSave = FilesMgt.saveCertificateFile(MsoDemo.this, "certif_path num " + index + ".der", data);
					} else {
						break;
					}

					index++;
				}
			}

			String result = "";
			String message1 = "";
			String message2 = "";
			String message3 = "";

			if (ret != MorphoSmartSDK.MORPHO_OK && !isSave) {
				result = "Read certificate Failed";
				message1 = "An error occured while calling";
				message2 = "MorphoDevice.secuReadCertificate()";
				message3 = ErrorsMgt.convertSDKError(ret);

			} else {
				result = "Read Certificate OK";
			}

			new DialogResultWindow(MsoDemo.this, result, message1, message2, message3, "").setVisible(true);

		}
	};

	private ActionListener miVerifySignAL = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			UserData tkbData = FilesMgt.readTKBFile(MsoDemo.this);

			if (tkbData == null) {
				DialogUtils.showErrorMessage("Verify Sign X9.84", "Get tkb file aborted. Press OK to continue...");
			} else {
				if (tkbData.getNbFinger() == 0) {
					DialogUtils.showErrorMessage("Verify Sign X9.84", "Error while extracting data.\nFile may be corrupted.");
				} else {
					MorphoTemplateList mTemplateList = new MorphoTemplateList();
					IMsoSecu verifSignRSA = new MsoSecu();
					int[] result = { 0 };

					int ret = SecurityMgt.verifSignX984(mTemplateList, tkbData.getPkX984TemplateData(), tkbData.getPkX984Certificate(), verifSignRSA, result);

					if (ret != MorphoSmartSDK.MORPHO_OK) {
						DialogUtils.showErrorMessage("Verify Sign X9.84", "An error occured while calling MorphoTemplateList.VerifSignX984() function", ret, 0);
					} else {
						if (result[0] == 1) {
							DialogUtils.showInfoMessage("Verify Sign X9.84", "The X984 is valid");
						} else {
							DialogUtils.showInfoMessage("Verify Sign X9.84", "The X984 is not valid");
						}
					}
				}
			}
		}
	};

	private ActionListener miExtractSignerIdAL = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			UserData tkbData = FilesMgt.readTKBFile(MsoDemo.this);

			if (tkbData == null) {
				DialogUtils.showErrorMessage("Extract SignerId X984", "Get tkb file aborted. Press OK to continue...");
			} else {
				if (tkbData.getNbFinger() == 0) {
					DialogUtils.showErrorMessage("Extract SignerId X984", "Error while extracting data.\nFile may be corrupted.");
				} else {
					MorphoTemplateList mTemplateList = new MorphoTemplateList();

					ArrayList<Byte> signerId = new ArrayList<Byte>();
					int ret = mTemplateList.extractSignerIdX984(tkbData.getPkX984TemplateData(), signerId);

					if (ret != MorphoSmartSDK.MORPHO_OK) {
						DialogUtils.showErrorMessage("Extract SignerId X984", "An error occured while calling MorphoTemplateList.extractSignerIdX984() function", ret, 0);
					} else {
						String signerIdMessage = "Certificate identifier : ";

						byte[] data = new byte[signerId.size()];
						for (int i = 0; i < signerId.size(); ++i) {
							data[i] = signerId.get(i);
						}

						signerIdMessage += byteArrayToHex(data);

						DialogUtils.showInfoMessage("Extract SignerId X984", signerIdMessage);
					}
				}
			}
		}
	};

	public static String byteArrayToHex(byte[] a) {
		StringBuilder sb = new StringBuilder(a.length * 2);
		for (byte b : a)
			sb.append(String.format("%02x ", b & 0xff));
		return sb.toString().toUpperCase();
	}

	private ActionListener miLoadKsUnsecureAL = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			byte[] ks = FilesMgt.readKSFile(MsoDemo.this);
			if (ks != null) {
				waitCursor();
				int ret = mDevice.loadKs(ks);
				defaultCursor();
				if (ret != MorphoSmartSDK.MORPHO_OK) {
					DialogUtils.showErrorMessage("Load Ks in Unsecure Mode", "An error occured while calling MorphoDevice.loadKs() function", ret, mDevice.getInternalError());
				} else {
					new DialogResultWindow(MsoDemo.this, "Load Ks", "Successful", "", "", "").setVisible(true);
				}
			} else {
				DialogUtils.showInfoMessage("Load Ks in Unsecure Mode", "Error while getting ks file");
			}
		}
	};

	private ActionListener miLoadKsSymmetricAL = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			byte[] currentks = FilesMgt.readKSFile(MsoDemo.this);
			byte[] newks = null;

			IMsoSecu msoSecu = new MsoSecu();

			String result = "Load Ks";
			String message1 = "";

			int ret = 0;
			if (currentks != null) {
				if (currentks.length != MsoSecu.TRIPLE_DES_KEY_LENGTH) {
					message1 = "error: key is not 192 bits long";
					ret = -1;
				}
			} else {
				ret = -1;
				message1 = "Error while getting ks file";
			}

			if (ret != -1) {
				newks = FilesMgt.readKSFile(MsoDemo.this);

				if (newks != null) {
					if (newks.length != MsoSecu.TRIPLE_DES_KEY_LENGTH) {
						message1 = "error: key is not 192 bits long";
						ret = -1;
					}
				} else {
					ret = -1;
					message1 = "Error while getting ks file";
				}
			}

			if (ret != -1) {
				// we actually have to encrypt [current-ks||new-ks] with current
				// ks
				byte[] currentNewKs = new byte[currentks.length + newks.length];

				for (int i = 0; i < currentks.length; ++i) {
					currentNewKs[i] = currentks[i];
				}

				for (int i = currentks.length; i < currentks.length + newks.length; ++i) {
					currentNewKs[i] = newks[i - currentks.length];
				}

				byte[] iv = new byte[MsoSecu.DES_BLOCK_LENGTH];
				byte[] out = new byte[currentNewKs.length];
				ret = msoSecu.encrypt3DesCbcNopad(currentks, currentNewKs, currentNewKs.length, iv, out);

				if (ret != 0) {
					message1 = "encrypt3DesCbcNopad() error";
				} else {
					// prepare buffer to send:
					// [iv][3DES-CBC(current-ks,[current-ks||new-ks])]
					int kslength = MsoSecu.DES_BLOCK_LENGTH + out.length;
					byte[] ks = new byte[kslength];

					for (int i = 0; i < MsoSecu.DES_BLOCK_LENGTH; ++i) {
						ks[i] = iv[i];
					}

					for (int i = MsoSecu.DES_BLOCK_LENGTH; i < kslength; ++i) {
						ks[i] = out[i - MsoSecu.DES_BLOCK_LENGTH];
					}
					waitCursor();
					ret = mDevice.loadKsSecurely(ks);
					defaultCursor();
					if (ret != 0) {
						message1 = "MorphoDevice.loadKsSecurely() error";
					} else {
						message1 = "Successful";
					}
				}
			}

			new DialogResultWindow(MsoDemo.this, result, message1, "", "", "").setVisible(true);
		}
	};

	private ActionListener miLoadKsAsymmetricAL = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			byte[] ks = FilesMgt.readKSFile(MsoDemo.this);
			byte[] hostKey = null;
			byte[] hostDer = null;

			IMsoSecu msoSecu = new MsoSecu();

			String result = "Load Ks";
			String message1 = "Failed";
			String message2 = "";
			String message3 = "";
			int ret = 0;

			if (ks == null) {
				ret = -1;
				message2 = "Error while getting ks file";
			} else {
				hostKey = FilesMgt.readHostKeyFile(MsoDemo.this);

				if (hostKey == null) {
					ret = -1;
					message2 = "Error while getting host certificate (key) file";
				} else {
					hostDer = FilesMgt.readHostDerFile(MsoDemo.this);
					if (hostDer == null) {
						ret = -1;
						message2 = "Error while getting host certificate (der) file";
					}
				}
			}

			if (ret == 0) {
				ArrayList<Byte> kscrypt = new ArrayList<Byte>();
				if (hostCertificate == null) {
					ArrayList<Byte> certif = new ArrayList<Byte>();
					ret = SecurityMgt.secuReadCertificate(mDevice, (short) 0, certif);

					if (ret != MorphoSmartSDK.MORPHO_OK) {
						DialogUtils.showErrorMessage(result, "Cannot read MSO certificate (error " + ErrorsMgt.convertSDKError(ret) + ")");
						return;
					}

					hostCertificate = new byte[certif.size()];
					for (int i = 0; i < certif.size(); i++) {
						hostCertificate[i] = certif.get(i);
					}
				}

				ret = msoSecu.rsaEncrypt(hostCertificate, ks, kscrypt);

				if (ret != 0) {
					message2 = "MsoSecu.rsaEncrypt error";
				} else {
					byte[] kscryptdata = new byte[kscrypt.size()];
					for (int i = 0; i < kscrypt.size(); ++i) {
						kscryptdata[i] = kscrypt.get(i);
					}
					ArrayList<Byte> signature = new ArrayList<Byte>();
					ret = msoSecu.signRSA(hostKey, kscryptdata, signature);
					if (ret != 0) {
						message2 = "MsoSecu.signRSA error";
					} else {
						byte[] signatureData = new byte[signature.size()];
						for (int i = 0; i < signature.size(); ++i) {
							signatureData[i] = signature.get(i);
						}

						waitCursor();
						ret = mDevice.loadKsSecurely(kscryptdata, signatureData, hostDer);
						defaultCursor();

						if (ret != 0) {
							message2 = "MorphoDevice::loadKsSecurely() error";
							message3 = ErrorsMgt.convertSDKError(ret);
						} else {
							message1 = "Successful";
						}
					}
				}
			}

			new DialogResultWindow(MsoDemo.this, result, message1, message2, message3, "").setVisible(true);
		}
	};

	private ActionListener miGetKsAL = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			short keyID = MorphoSmartSDK.ID_KS;
			byte[] kCV = { 0, 0, 0 };
			int ret = mDevice.getKCV(keyID, null, kCV);
			if (ret == MorphoSmartSDK.MORPHO_OK) {
				DialogUtils.showInfoMessage("Get Ks KCV", "Ks KCV :" + byteArrayToHex(kCV));
			} else {
				DialogUtils.showErrorMessage("Get Ks KCV", "An error occured while calling MorphoDevice.getKCV() function", ret, mDevice.getInternalError());
			}
		}
	};

	private ActionListener miEnableBiometricDataAL = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			if (!MsoDemo.this.isDataEncryption) {
				new DialogEnableBioDataEncryption(MsoDemo.this).setVisible(true);
				if (MsoDemo.this.isDataEncryption) {
					miEnableBiometricData.setText("Disable Biometric Data Encryption");
				}
			} else {
				MsoDemo.this.setDataEncryption(false);
				mDevice.enableDataEncryption(false, null);
				miEnableBiometricData.setText("Enable Biometric Data Encryption");
			}
		}
	};

	public MsoDemo getMsoDemo() {
		return this;
	}

	public MorphoDevice getMorphoDeviceInstance() {
		return mDevice;
	}

	public MorphoDatabase getMorphoDatabaseInstance() {
		return mDatabase;
	}

	/**
	 * Close Communication
	 */
	public void closeDevice() {
		if (mDevice != null) {
			mDevice.cancelLiveAcquisition();
			mDevice.closeDevice();
		}
	}

	public PanelDatabaseMgt getPanelDatabaseMgtInstance() {
		return rightPanel;
	}

	public PanelBasicBiometricOperation getPanelBasicBiometricOperationInstance() {
		return leftPanel;
	}

	/**
	 * 
	 * @param isBaseAvailable
	 */
	public void enableDataBaseFunction(boolean isBaseAvailable) {
		if (isBaseAvailable) {
			rightPanel.btnDestroyBase.setEnabled(true);
			rightPanel.btnCreateBase.setEnabled(false);
			rightPanel.lblBaseStatus.setIcon(rightPanel.iconGreen);
			rightPanel.btnAddUser.setEnabled(true);
			rightPanel.cbNoCheck.setEnabled(true);
		} else {
			rightPanel.btnCreateBase.setEnabled(true);
			rightPanel.btnAddUser.setEnabled(false);
			rightPanel.btnDestroyBase.setEnabled(false);
			rightPanel.btnRemoveAll.setEnabled(false);
			rightPanel.btnRemoveUser.setEnabled(false);
			rightPanel.btnUpdateUser.setEnabled(false);
			rightPanel.btnIdentifyMatch.setEnabled(false);
			rightPanel.cbNoCheck.setEnabled(false);
			rightPanel.lblBaseStatus.setIcon(rightPanel.iconRed);
		}

		leftPanel.AddTabsEnrollIdentify(isBaseAvailable);
	}

	/**
	 *
	 */
	public void updateDataBaseInformation() {
		rightPanel.tabDbInfo.setLblEncryptedBb("N/A");
		rightPanel.tabDbInfo.setLblMaxNbOfRecords(0);
		rightPanel.tabDbInfo.setLblNbOfFingersPerRecord(0);
		rightPanel.tabDbInfo.setLblCurrentNbOfRecords(0);

		int[] nbUsedRecord = { 0 };
		int ret = mDatabase.getNbUsedRecord(nbUsedRecord);

		if (ret == MorphoSmartSDK.MORPHO_OK) {
			rightPanel.tabDbInfo.setLblCurrentNbOfRecords(nbUsedRecord[0]);
			if (nbUsedRecord[0] == 0) {
				rightPanel.lblBaseStatus.setIcon(rightPanel.iconYellow);
			} else {
				rightPanel.lblBaseStatus.setIcon(rightPanel.iconGreen);
			}

			int[] nbTotalRecord = { 0 };
			short[] nbFinger = { 0 };

			mDatabase.getNbFinger(nbFinger);

			mDatabase.getNbTotalRecord(nbTotalRecord);

			rightPanel.tabDbInfo.setLblMaxNbOfRecords(nbTotalRecord[0]);
			rightPanel.tabDbInfo.setLblNbOfFingersPerRecord((int) nbFinger[0]);

			short[] encryptDB = { 0 };
			ret = mDatabase.getDbEncryptionStatus(encryptDB);

			if (ret == MorphoSmartSDK.MORPHO_OK) {
				rightPanel.tabDbInfo.setLblEncryptedBb((encryptDB[0] == 1 ? "Yes" : "No"));
			} else {
				rightPanel.tabDbInfo.setLblEncryptedBb("N/A");
			}
		}
	}

	/**
	 * getSecurityLevel
	 * 
	 * @param function
	 *            : used for FVP , 1 = case enroll , getImage and Capture; 0 :
	 *            case Verify and identify
	 * @return SecurityLevel
	 */
	public int getSecurityLevel(EnumTabIndex function) {
		int securityLevel = 0;

		String strSecurityLevel = (String) rightPanel.tabBioSettings.comboSecurityLevel.getSelectedItem();

		if (MorphoInfo.isFVP) {
			if (function == EnumTabIndex.TAB_CAPTURE || function == EnumTabIndex.TAB_ENROL || function == EnumTabIndex.TAB_FINGERPTINT_IMAGE) {
				securityLevel = MorphoSmartSDK.FFD_SECURITY_LEVEL_DEFAULT_HOST;
			} else {
				if (strSecurityLevel.equals("Standard")) {
					securityLevel = MorphoSmartSDK.MULTIMODAL_SECURITY_STANDARD;
				} else if (strSecurityLevel.equals("Medium")) {
					securityLevel = MorphoSmartSDK.MULTIMODAL_SECURITY_MEDIUM;
				} else if (strSecurityLevel.equals("High")) {
					securityLevel = MorphoSmartSDK.MULTIMODAL_SECURITY_HIGH;
				}
			}
		} else {
			if (strSecurityLevel.equals("Low")) {
				securityLevel = MorphoSmartSDK.FFD_SECURITY_LEVEL_LOW_HOST;
			} else if (strSecurityLevel.equals("Medium")) {
				securityLevel = MorphoSmartSDK.FFD_SECURITY_LEVEL_MEDIUM_HOST;
			} else if (strSecurityLevel.equals("High")) {
				securityLevel = MorphoSmartSDK.FFD_SECURITY_LEVEL_HIGH_HOST;
			} else if (strSecurityLevel.equals("Default")) {
				securityLevel = MorphoSmartSDK.FFD_SECURITY_LEVEL_DEFAULT_HOST;
			}
		}

		return securityLevel;
	}

	/**
	 * 
	 * @param strSecurityLevel
	 */
	public void setSecurityLevel(String strSecurityLevel) {
		int securityLevel = 0;

		if (MorphoInfo.isFVP) {
			if (strSecurityLevel.equals("Standard")) {
				securityLevel = MorphoSmartSDK.MULTIMODAL_SECURITY_STANDARD;
			} else if (strSecurityLevel.equals("Medium")) {
				securityLevel = MorphoSmartSDK.MULTIMODAL_SECURITY_MEDIUM;
			} else if (strSecurityLevel.equals("High")) {
				securityLevel = MorphoSmartSDK.MULTIMODAL_SECURITY_HIGH;
			}

		} else {
			if (strSecurityLevel.equals("Low")) {
				securityLevel = MorphoSmartSDK.FFD_SECURITY_LEVEL_LOW_HOST;
			} else if (strSecurityLevel.equals("Medium")) {
				securityLevel = MorphoSmartSDK.FFD_SECURITY_LEVEL_MEDIUM_HOST;
			} else if (strSecurityLevel.equals("High")) {
				securityLevel = MorphoSmartSDK.FFD_SECURITY_LEVEL_HIGH_HOST;
			} else if (strSecurityLevel.equals("Default")) {
				securityLevel = MorphoSmartSDK.FFD_SECURITY_LEVEL_DEFAULT_HOST;
			}
		}

		mDevice.setSecurityLevel(securityLevel);
	}

	/**
	 * 
	 * @param strategyAcquisitionMode
	 */
	public void setStrategyAcquisitionMode(short strategyAcquisitionMode) {
		int ret = mDevice.setStrategyAcquisitionMode(strategyAcquisitionMode);
		if (ret != 0)
			DialogUtils.showErrorMessage("MSO_Demo", "An error occured while calling MorphoDevice.setStrategyAcquisitionMode() function", ret, mDevice.getInternalError());
	}

	/**
	 *
	 */
	public void initSensorWinPosition() {
		int[] paramValue = { 0 };
		int ret = mDevice.getConfigParam(MorphoSmartSDK.CONFIG_SENSOR_WIN_POSITION_TAG, paramValue);
		if (ret == MorphoSmartSDK.MORPHO_OK) {
			if (paramValue[0] < 4) {
				rightPanel.tabOptions.comboSensorWindowPosition.setSelectedIndex(paramValue[0]);
			}
		}
	}

	/**
	 *
	 */
	public void disableAllButons() {
		rightPanel.disableAllButons();
		leftPanel.disableAllButons();
	}

	/**
	 *
	 */
	public void enableAllButtons() {
		rightPanel.enableAllButtons();
		leftPanel.enableAllButtons();
	}

	public byte[] getHostCertificate() {
		return hostCertificate;
	}

	public void setHostCertificate(byte[] hostCertificate) {
		this.hostCertificate = hostCertificate;
	}

	public boolean isDataEncryption() {
		return isDataEncryption;
	}

	public void setDataEncryption(boolean isDataEncryption) {
		this.isDataEncryption = isDataEncryption;
	}

	public void waitCursor() {
		this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
	}

	public void defaultCursor() {
		this.setCursor(Cursor.getDefaultCursor());
	}

	public boolean isOFFERED_SECURITY() {
		return OFFERED_SECURITY;
	}

	public void setOFFERED_SECURITY(boolean isOFFERED_SECURITY) {
		OFFERED_SECURITY = isOFFERED_SECURITY;
	}

	public boolean isTUNNELING() {
		return TUNNELING;
	}

	public void setTUNNELING(boolean isTUNNELING) {
		TUNNELING = isTUNNELING;
	}
	public void updateDBInformation() {
		int[] nbUsedRecord = { 0 };
		int ret = mDatabase.getNbUsedRecord(nbUsedRecord);
		if (ret == MorphoSmartSDK.MORPHO_OK) {
			int[] nbTotalRecord = { 0 };
			short[] nbFinger = { 0 };
			mDatabase.getNbFinger(nbFinger);
			mDatabase.getNbTotalRecord(nbTotalRecord);
		}
	}
	
}
