/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.scl.loadlibrary;

import com.ipssi.fingerprint.utils.SyncFingerPrintDeviceHelper;
import com.ipssi.gen.utils.DBConnectionPool;
import com.ipssi.gen.utils.Misc;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.imageio.ImageIO;

import morpho.morphosmart.sdk.api.IMorphoEventHandler;
import morpho.morphosmart.sdk.api.MorphoCallbackCommand;
import morpho.morphosmart.sdk.api.MorphoCompressAlgo;
import morpho.morphosmart.sdk.api.MorphoDevice;
import morpho.morphosmart.sdk.api.MorphoFVPTemplateType;
import morpho.morphosmart.sdk.api.MorphoTemplateList;
import morpho.morphosmart.sdk.api.MorphoTemplateType;
import morpho.morphosmart.sdk.api.MorphoUser;
import morpho.morphosmart.sdk.demo.ennum.Coder;
import morpho.morphosmart.sdk.demo.ennum.DetectionMode;
import morpho.morphosmart.sdk.demo.trt.MorphoEventHandler;

/**
 * 
 * @author Vi$ky
 */
public class FingerPrintAction {

	private LoadLibrary auth = null;
	private boolean isDriverLoaded = false;

	public static void deleteAllDriverFinger(int userId, StringBuilder error) {
		try {
			synchronized (LoadLibrary.lock) {
				LoadLibrary auth = LoadLibrary.getBiometricDevice();
				for (int i = 1; i <= 10; i++) {
					int status = auth.deleteUserById(userId + "_" + i);
					if(error != null)
						error.append("\ndelete driver_finger_id:" + userId + "_" + i + ", status:" + status);
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static void deleteAllCurrentUserId(LoadLibrary auth, List<String> currentUserIdListofDevice) {
		if (auth == null)
			return;
		Iterator<String> myListIterator = currentUserIdListofDevice.iterator();
		while (myListIterator.hasNext()) {
			String userId = myListIterator.next();
			auth = LoadLibrary.getBiometricDevice();
			auth.deleteUserById(userId);
		}
	}

	public static int getNewDriverId(Connection invConn) throws Exception {
		System.out.println("####### FingerPrintAction: getNewDriverId() ########");
		int driverId = Misc.getUndefInt();
		int parameterIndex = 1;
		PreparedStatement ps = null;
		ResultSet rs = null;
		int driverType = 1;
		int status = 0;
		String query = "insert into driver_details (type, status) values(?,?)";
		try {
			ps = invConn.prepareStatement(query);
			ps.setInt(parameterIndex++, driverType);
			ps.setInt(parameterIndex++, status);
			ps.executeUpdate();
			rs = ps.getGeneratedKeys();
			if (rs.next()) {
				driverId = rs.getInt(1);
				System.out.println("Generated Driver ID: " + Integer.toString(driverId));
			}
			rs.close();
			ps.close();

		} catch (Exception ex) {
			ex.printStackTrace();
			throw ex;
		}
		return driverId;
	}

	public static BufferedImage byteArrayToImage(byte[] bytes) {
		BufferedImage bufferedImage = null;
		try {
			if (bytes != null) {
				InputStream inputStream = new ByteArrayInputStream(bytes);
				bufferedImage = ImageIO.read(inputStream);
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return bufferedImage;
	}

	public static BufferedImage changeSizeOfImage(BufferedImage img) {
		int w = img.getWidth();
		int h = img.getHeight();
		int newWidth = 75;
		int newHeight = 117;
		BufferedImage dimg = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = dimg.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g.drawImage(img, 0, 0, newWidth, newHeight, 0, 0, w, h, null);
		g.dispose();
		return dimg;
	}

	public static void setFingerImage(int i, byte[] capturedImage, BioMatricBean biomatricBean) {
		try {
			if (i == 1) {// getCapture_First_Finger_Image1
				biomatricBean.setCaptureFirstFingerImage1(capturedImage);
			} else if (i == 2) {
				biomatricBean.setCaptureSecondFingerImage2(capturedImage);
			} else if (i == 3) {
				biomatricBean.setCaptureThirdFingerImage3(capturedImage);
			} else if (i == 4) {
				biomatricBean.setCaptureFourthFingerImage4(capturedImage);
			} else if (i == 5) {
				biomatricBean.setCaptureFivethFingerImage5(capturedImage);
			} else if (i == 6) {
				biomatricBean.setCaptureSixthFingerImage6(capturedImage);
			} else if (i == 7) {
				biomatricBean.setCaptureSeventhFingerImage7(capturedImage);
			} else if (i == 8) {
				biomatricBean.setCaptureEighthFingerImage8(capturedImage);
			} else if (i == 9) {
				biomatricBean.setCaptureNinthFingerImage9(capturedImage);
			} else if (i == 10) {
				biomatricBean.setCaptureTenthFingerImage10(capturedImage);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static byte[] getProcessedImage(LoadLibrary auth) {
		byte[] retval = null;
		try {
			byte[] rawImageData = new byte[102400];
			byte[] capturedImageWithISO = auth.getCapturedImage();
			System.arraycopy(capturedImageWithISO, 46, rawImageData, 0, 102400);
			retval = auth.createImage(rawImageData, rawImageData.length);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return retval;
	}

	public static void createImageFromByte(LoadLibrary auth, int i, byte[] capturedImageWithISO, BioMatricBean biomatricBean) {
		if (auth == null)
			return;
		byte[] rawImageData = new byte[102400];
		System.arraycopy(capturedImageWithISO, 46, rawImageData, 0, 102400);
		try {
			if (i == 1) {// getCapture_First_Finger_Image1
				biomatricBean.setCaptureFirstFingerImage1(auth.createImage(rawImageData, rawImageData.length));
			} else if (i == 2) {
				biomatricBean.setCaptureSecondFingerImage2(auth.createImage(rawImageData, rawImageData.length));
			} else if (i == 3) {
				biomatricBean.setCaptureThirdFingerImage3(auth.createImage(rawImageData, rawImageData.length));
			} else if (i == 4) {
				biomatricBean.setCaptureFourthFingerImage4(auth.createImage(rawImageData, rawImageData.length));
			} else if (i == 5) {
				biomatricBean.setCaptureFivethFingerImage5(auth.createImage(rawImageData, rawImageData.length));
			} else if (i == 6) {
				biomatricBean.setCaptureSixthFingerImage6(auth.createImage(rawImageData, rawImageData.length));
			} else if (i == 7) {
				biomatricBean.setCaptureSeventhFingerImage7(auth.createImage(rawImageData, rawImageData.length));
			} else if (i == 8) {
				biomatricBean.setCaptureEighthFingerImage8(auth.createImage(rawImageData, rawImageData.length));
			} else if (i == 9) {
				biomatricBean.setCaptureNinthFingerImage9(auth.createImage(rawImageData, rawImageData.length));
			} else if (i == 10) {
				biomatricBean.setCaptureTenthFingerImage10(auth.createImage(rawImageData, rawImageData.length));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void deleteDriverFingerFromDB(Connection conn, int driverId, StringBuilder error) throws Exception{
		System.out.println("### FingerPrintAction deleteDriverFromDB() ##");
		System.out.println("### driverId :" + driverId + " ##");
		PreparedStatement ps = null;
		ResultSet rs = null;
		String DELETE_DRIVER = " update driver_details set capture_template_first = null ,capture_template_second = null, capture_template_third = null, capture_template_fourth = null,capture_template_fifth = null,capture_template_sixth = null , capture_template_seventh = null, capture_template_eighth = null,capture_template_ninth = null ,capture_template_tenth = null, first_finger_template = null, second_finger_template = null, third_finger_template = null, fourth_finger_template = null , fifth_finger_template = null, sixth_finger_template = null ,seventh_finger_template = null , eight_finger_template = null, ninth_finger_template = null, tenth_finger_template = null, template_updated_on = now() where id = ? ";
		try {
			ps = conn.prepareStatement(DELETE_DRIVER);
			Misc.setParamInt(ps, driverId, 1);
			ps.execute();
			SyncFingerPrintDeviceHelper.updateDriverSyncStatus(conn, driverId, DriverBean.DELETE, 0,Misc.getUndefInt(), error);
			if(error != null)
				error.append("\ndeleteDriverFromDB driver_id:" + driverId);
		} catch (Exception ex) {
			ex.printStackTrace();
			throw ex;
		} finally {
			try {
				if (rs != null) {
					rs.close();
				}
				if (ps != null) {
					ps.close();
				}
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
	}

	public static void deleteLastAutoIdFromDB(Connection conn, String autoId) throws Exception {
		System.out.println("### FingerPrintAction deleteLastAutoIdFromDB() ##");
		PreparedStatement ps = null;
		ResultSet rs = null;
		String DELETE_DRIVER = " delete from driver_details where id = ? ";
		try {
			ps = conn.prepareStatement(DELETE_DRIVER);
			Misc.setParamInt(ps, autoId, 1);

			ps.execute();
		} catch (Exception ex) {
			ex.printStackTrace();
			throw ex;
		} finally {
			try {
				if (rs != null) {
					rs.close();
				}
				if (ps != null) {
					ps.close();
				}
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
	}

	public static void setFingerTemplateToBean(int i, byte[] captureTemplate, BioMatricBean biomatricBeans) {
		switch (i) {
		case 1:
			biomatricBeans.setCaptureFirstTemplate1(captureTemplate);
			break;
		case 2:
			biomatricBeans.setCaptureSecondTemplate2(captureTemplate);
			break;
		case 3:
			biomatricBeans.setCaptureThirdTemplate3(captureTemplate);
			break;
		case 4:
			biomatricBeans.setCaptureFourthTemplate4(captureTemplate);
			break;
		case 5:
			biomatricBeans.setCaptureFivethTemplate5(captureTemplate);
			break;
		case 6:
			biomatricBeans.setCaptureSixthTemplate6(captureTemplate);
			break;
		case 7:
			biomatricBeans.setCaptureSeventhTemplate7(captureTemplate);
			break;
		case 8:
			biomatricBeans.setCaptureEighthTemplate8(captureTemplate);
			break;
		case 9:
			biomatricBeans.setCaptureNinthTemplate9(captureTemplate);
			break;
		case 10:
			biomatricBeans.setCaptureTenthTemplate10(captureTemplate);
			break;
		default:
			break;
		}
	}

	public static int checkAuthentication(LoadLibrary auth, String driverId) {
		int enrollmentId = Misc.getUndefInt();
		String newFingerId = driverId;
		String driver_name = "";
		String driver_DL = "";
		if (auth == null)
			return enrollmentId;
		try {
			boolean deviceConnected = auth.isDeviceConnected();
			System.out.println("deviceConnected" + deviceConnected);
			if (!deviceConnected) {
				return enrollmentId;
			}
			enrollmentId = auth.captureFPData(30);
			// LoggerNew.Write(enrollmentId);
			byte[] captureTemplate = auth.getCapturedTemplate();
			enrollmentId = auth.enrollUser(newFingerId, driver_name, driver_DL, captureTemplate, captureTemplate.length);
		} catch (UnsatisfiedLinkError e) {
			e.printStackTrace();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return enrollmentId;
	}

	public static byte[] bufferImageToBytes(BufferedImage bi) {
		byte[] imageInByte = null;
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(bi, "jpg", baos);
			baos.flush();
			imageInByte = baos.toByteArray();
			baos.close();

		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
		return imageInByte;
	}

	public static int checkDriverExistance(LoadLibrary auth, String newFingerId, String driver_name, String driver_DL, int i, BioMatricBean biomatricBean)
			throws FileNotFoundException, IOException {
		int enrollmentId = Misc.getUndefInt();
		try {
			if (auth == null)
				return enrollmentId;
			boolean deviceConnected = auth.isDeviceConnected();
			System.out.println("deviceConnected" + deviceConnected);
			if (!deviceConnected) {
				enrollmentId = -1;
				return enrollmentId;
			}

			enrollmentId = auth.captureFPData(30);
			System.out.println("" + enrollmentId);
			byte[] captureTemplate = auth.getCapturedTemplate();
			enrollmentId = auth.enrollUser(newFingerId, driver_name, driver_DL, captureTemplate, captureTemplate.length);
			if (enrollmentId == 0) {
				FingerPrintAction.setFingerTemplateToBean(i, captureTemplate, biomatricBean);
				byte[] capturedImageWithISO = auth.getCapturedImage();
				FingerPrintAction.createImageFromByte(auth, i, capturedImageWithISO, biomatricBean);
			}
		} catch (UnsatisfiedLinkError e) {
			e.printStackTrace();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return enrollmentId;
	}

	public static int enrollUser(String id, String firstName, String lastName, byte[] templateData, int length) {
		int retval = Misc.getUndefInt();
		try {
			synchronized (LoadLibrary.lock) {
				LoadLibrary auth = LoadLibrary.getBiometricDevice();
				retval = auth.enrollUser(id, firstName, lastName, templateData, length);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		System.out.println("enroll user result : " + retval);
		return retval;
	}
	public static void deleteDriverId(int driverId) throws Exception {
		Connection conn = null;
		boolean destroyIt = false;
		try {
			conn = DBConnectionPool.getConnectionFromPoolNonWeb();
			deleteLastAutoIdFromDB(conn, Integer.toString(driverId));
			conn.commit();
		} catch (Exception ex) {
			destroyIt = true;
			ex.printStackTrace();
			throw ex;
		} finally {
			try {
				DBConnectionPool.returnConnectionToPoolNonWeb(conn, destroyIt);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}
	public static int getDriverId() throws Exception {
		Connection conn = null;
		boolean destroyIt = false;
		int driverId = Misc.getUndefInt();
		try {
			conn = DBConnectionPool.getConnectionFromPoolNonWeb();
			driverId = FingerPrintAction.getNewDriverId(conn);
			conn.commit();
		} catch (Exception ex) {
			destroyIt = true;
			ex.printStackTrace();
			throw ex;
		} finally {
			try {
				DBConnectionPool.returnConnectionToPoolNonWeb(conn, destroyIt);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return driverId;
	}
	public static byte[] toByteArray(ArrayList<Byte> dataTemplate) {
		if(dataTemplate == null || dataTemplate.size() <= 0)
			return null;
		final int n = dataTemplate.size();
		byte ret[] = new byte[n];
		for (int i = 0; i < n; i++) {
			ret[i] = dataTemplate.get(i);
		}
		return ret;
	}
	
	
	public static int enrollUser(MorphoDevice mDevice){
		/*short advancedSecurityLevelsRequired = (short) 0;
		short acquisitionThreshold = 100;// default 0
		MorphoCompressAlgo compressAlgo = MorphoCompressAlgo.MORPHO_NO_COMPRESS;
		short compressRate = 0;
		short exportMinutiae = 0;
		short fingerNumber = (short) 2;
		MorphoTemplateType fpTemplateType = MorphoTemplateType.MORPHO_NO_PK_FP;
		MorphoFVPTemplateType fvpTemplateType = MorphoFVPTemplateType.MORPHO_NO_PK_FVP;
		int callbackCmd = MorphoCallbackCommand.MORPHO_CALLBACK_IMAGE_CMD.swigValue() | MorphoCallbackCommand.MORPHO_CALLBACK_ENROLLMENT_CMD.swigValue() | MorphoCallbackCommand.MORPHO_CALLBACK_COMMAND_CMD.swigValue() | MorphoCallbackCommand.MORPHO_CALLBACK_CODEQUALITY.swigValue() | MorphoCallbackCommand.MORPHO_CALLBACK_DETECTQUALITY.swigValue();
		short saveRecord = (short) 1;
		IMorphoEventHandler mEventHandlerCallback = new MorphoEventHandler(null);
		int coderChoice = 0;
		// detectModeChoice
		int detectModeChoice = DetectionMode.MORPHO_ENROLL_DETECT_MODE.getValue();
		MorphoTemplateList templateList = new MorphoTemplateList();
		int timeout = 60*1000;
		MorphoUser user = new MorphoUser();
		String userID = "";
		String firstName = "";
		String lastName = "";
		int ret = 0;
		ret = user.enroll(timeout, acquisitionThreshold, advancedSecurityLevelsRequired,
				compressAlgo, compressRate, exportMinutiae, fingerNumber, 
				fpTemplateType, fvpTemplateType, saveRecord, callbackCmd,
				mEventHandlerCallback, coderChoice, detectModeChoice, templateList);*/
		return Misc.getUndefInt(); 
	}
	public static void main(String[] ar) {
		FingerPrintAction.deleteAllDriverFinger(3799, null);
	}

}
