package com.ipssi.rfid.ui;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import com.ipssi.gen.utils.DBConnectionPool;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.Pair;
import com.ipssi.gen.utils.Triple;
import com.ipssi.rfid.constant.UIConstant;
import com.ipssi.rfid.database.GateInDao;
import com.ipssi.rfid.processor.Utils;
import com.scl.loadlibrary.BioMatricBean;
import com.scl.loadlibrary.BioMatricException;
import com.scl.loadlibrary.FingerPrintAction;
import com.scl.loadlibrary.LoadLibrary;

public class FingerPrintDialog extends javax.swing.JDialog {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int recapture = 0;
    private int next = 1;
    private int cancelWindow = -1;
    private int saveNow = 2;
    private int totalFingerPrints = 10;
    private String driverId = "";
    private String driverName = "";
    private String driverDl = "";
    private Object[] option1 = {"  Recapture  ", "  Next  ", "  Save Now  "};
    private String msg1 = "Do you want to  capture next finger click to Next ?";
    private Object[] options2 = {"  Yes  ", "  No  "};
    private String msg2 = "Do you want to  Save Data ?";
    private boolean isNewDriverId = false;
    private int responseConfirmDialogue = Misc.getUndefInt();
    private int responseConfirmDialogueForSaveData = Misc.getUndefInt();
    private String newFingerID = "";
    private BioMatricBean biometricBean = null;
    private List<String> deviceIdList = null;
    private ArrayList<Integer> deleteDriverList = null;
    private ArrayList<byte[]> fingerTemplateList = null;
    private ArrayList<byte[]> fingerImageList = null;
    private DriverRegistrationWindow parent = null;

    public FingerPrintDialog(DriverRegistrationWindow parent, boolean modal, BioMatricBean biometricBean, ArrayList<Integer> deleteDriverList, ArrayList<byte[]> fingerTemplateList, ArrayList<byte[]> fingerImageList) {
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
		        	startFingerPrintNew();
		        } catch (Exception ex) {
		            ex.printStackTrace();
		        }
			}
		});
        t.start();
    }
    
	private void startFingerPrintNew(){
		StringBuilder error = new StringBuilder();
        removeAllLabelIcon();
        Connection conn = null;
        boolean destroyIt = false;
        try{
        	conn = DBConnectionPool.getConnectionFromPoolNonWeb();
        synchronized (LoadLibrary.lock) {
        	LoadLibrary auth = LoadLibrary.getBiometricDevice();
        	boolean Success = false;
        	int fSeq = 1;
        	while (fSeq <= totalFingerPrints) {
        		Triple<Integer, byte[], byte[]> response = captureFingurePrint(conn, auth, fSeq, deleteDriverList,error);
        		int enroll_Id = (Integer) response.first;//checkDriverExistance(auth, driverName, driverDl, fSeq);
        		if(error != null){
        			error.append("\nfinger enrollment : "+ enroll_Id);
        		}
        		if (enroll_Id == 0) {
        			fingerTemplateList.add(response.second);
        			fingerImageList.add(response.third);
        			setFingerImage(response.third, fSeq);
        			responseConfirmDialogue = ConfirmationDialog.getDialogBox(new javax.swing.JFrame(), true, option1, msg1);
        			if (responseConfirmDialogue == next) {
        				Success = true;
        			} else if (responseConfirmDialogue == recapture) {
        				fingerTemplateList.remove(fSeq-1);
            			fingerImageList.remove(fSeq-1);
        				removePhotofromLabel(fSeq);
        					continue;
        			} else if (responseConfirmDialogue == cancelWindow) {
        				responseConfirmDialogueForSaveData = ConfirmationDialog.getDialogBox(new javax.swing.JFrame(), true, options2, msg2);
        				if (responseConfirmDialogueForSaveData == 0) {
        					if (biometricBean.getCaptureFirstTemplate1() == null || biometricBean.getCaptureSecondTemplate2() == null) {
        						biometricBean.setIsSave(0);// 0 not to save
        					} else {
        						if (isNewDriverId) {
        							biometricBean.setIsSave(1);// 1 for save 2 for update
        						} else {
        							biometricBean.setIsSave(2);
        						}
        					}
        					if(error != null)
        						System.out.println(error.toString());
        					this.dispose();
        					break;
        				} else {
        					removeAllLabelIcon();
        					biometricBean.setIsSave(0);
        					if(error != null)
        						System.out.println(error.toString());
        					this.dispose();
        				}
        				break;
        			} else if (responseConfirmDialogue == saveNow) {
        				if (isNewDriverId) {
        					biometricBean.setIsSave(1);// 1 for save
        				} else {
        					biometricBean.setIsSave(2);
        				}
        				if (biometricBean.getCaptureFirstTemplate1() == null || biometricBean.getCaptureSecondTemplate2() == null) {
        					biometricBean.setIsSave(0);
        				}
        				if(error != null)
    						System.out.println(error.toString());
        				this.dispose();
        				break;
        			}
        		} else if (enroll_Id == -19 || enroll_Id == -17) {
        			responseConfirmDialogue = ConfirmationDialog.getDialogBox(new javax.swing.JFrame(), true, option1, msg1);
        			System.out.println("responseConfirmDialogue : " + responseConfirmDialogue);
        			if (responseConfirmDialogue == next || responseConfirmDialogue == recapture) {
        				continue;
        			} else if (responseConfirmDialogue == cancelWindow || responseConfirmDialogue == saveNow) {
        				responseConfirmDialogueForSaveData = ConfirmationDialog.getDialogBox(new javax.swing.JFrame(), true, options2, msg2);
        				if (responseConfirmDialogueForSaveData == 0 && fSeq > 2) {
        					if (isNewDriverId) {
        						biometricBean.setIsSave(1);// 1 for save
        					} else {
        						biometricBean.setIsSave(2);
        					}
        					if(error != null)
        						System.out.println(error.toString());
        					this.dispose();
        					break;
        				} else {
        					removeAllLabelIcon();
        					biometricBean.setIsSave(0);
        					String errorMessage = BioMatricException.getException(enroll_Id);
        					System.out.println("\n Error Message: " + errorMessage + "Morpho Error Code is " + enroll_Id
        							+ "Finger Print less than 2");
        					if(error != null)
        						System.out.println(error.toString());
        					this.dispose();
        				}
        			}
        		} else {
        			String errorMessage = BioMatricException.getException(enroll_Id);
        			if (errorMessage.equalsIgnoreCase("") || errorMessage.length() < 0) {
        				errorMessage = "Check Log File";
        			}
        			JOptionPane.showMessageDialog(null, errorMessage);
        			removeAllLabelIcon();
        			System.out.println(" Error Message: " + errorMessage + " Error Code is " + enroll_Id);
        			if(error != null)
						System.out.println(error.toString());
        			this.dispose();
        			break;
        		}
        		if (Success) {
        			fSeq++;

        		}
        	}
        	if (fSeq > 10) {
        		if (isNewDriverId) {
        			biometricBean.setIsSave(1);// 1 for save
        		} else {
        			biometricBean.setIsSave(2);
        		}
        		this.dispose();
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
    private void StartFingerPrint() throws IOException {
        removeAllLabelIcon();
        synchronized (LoadLibrary.lock) {
        	LoadLibrary auth = LoadLibrary.getBiometricDevice();
        	if (deviceIdList != null) {
        		FingerPrintAction.deleteAllCurrentUserId(auth,deviceIdList);
        	}
        	boolean Success = false;
        	deviceIdList = new ArrayList<String>();
        	int fSeq = 1;
        	while (fSeq <= totalFingerPrints) {
        		int enroll_Id = checkDriverExistance(auth, driverName, driverDl, fSeq);
        		if (enroll_Id == 0) {
        			boolean isExist = deviceIdList.contains(newFingerID);
        			if (!isExist) {
        				deviceIdList.add(newFingerID);
        			}
        			attachPhotoToLabel(biometricBean, fSeq);
        			responseConfirmDialogue = ConfirmationDialog.getDialogBox(new javax.swing.JFrame(), true, option1, msg1);
        			System.out.println("test : " + responseConfirmDialogue);
        			if (responseConfirmDialogue == next) {
        				Success = true;
        			} else if (responseConfirmDialogue == recapture) {
        				removePhotofromLabel(fSeq);

        				int deleteUserResult = auth.deleteUserById(newFingerID);
        				if (deleteUserResult == 0) {
        					continue;
        				}
        			} else if (responseConfirmDialogue == cancelWindow) {
        				responseConfirmDialogueForSaveData = ConfirmationDialog.getDialogBox(new javax.swing.JFrame(), true, options2, msg2);
        				if (responseConfirmDialogueForSaveData == 0) {
        					if (biometricBean.getCaptureFirstTemplate1() == null || biometricBean.getCaptureSecondTemplate2() == null) {
        						FingerPrintAction.deleteAllCurrentUserId(auth, deviceIdList);
        						biometricBean.setIsSave(0);// 0 not to save
        					} else {
        						if (isNewDriverId) {
        							biometricBean.setIsSave(1);// 1 for save 2 for update
        							biometricBean.setDriverId(Integer.parseInt(driverId));
        						} else {
        							biometricBean.setIsSave(2);
        						}
        					}

        					this.dispose();
        					break;
        				} else {

        					removeAllLabelIcon();
        					biometricBean.setIsSave(0);
        					FingerPrintAction.deleteAllCurrentUserId(auth, deviceIdList);
        					this.dispose();
        				}
        				break;
        			} else if (responseConfirmDialogue == saveNow) {
        				if (isNewDriverId) {
        					biometricBean.setIsSave(1);// 1 for save
        					biometricBean.setDriverId(Integer.parseInt(driverId));
        				} else {
        					biometricBean.setIsSave(2);
        				}
        				if (biometricBean.getCaptureFirstTemplate1() == null || biometricBean.getCaptureSecondTemplate2() == null) {
        					FingerPrintAction.deleteAllCurrentUserId(auth, deviceIdList);
        					biometricBean.setIsSave(0);
        				}
        				this.dispose();
        				break;
        			}
        		} else if (enroll_Id == -19 || enroll_Id == -17) {
        			responseConfirmDialogue = ConfirmationDialog.getDialogBox(new javax.swing.JFrame(), true, option1, msg1);
        			System.out.println("responseConfirmDialogue : " + responseConfirmDialogue);
        			if (responseConfirmDialogue == next || responseConfirmDialogue == recapture) {
        				continue;
        			} else if (responseConfirmDialogue == cancelWindow || responseConfirmDialogue == saveNow) {
        				responseConfirmDialogueForSaveData = ConfirmationDialog.getDialogBox(new javax.swing.JFrame(), true, options2, msg2);
        				if (responseConfirmDialogueForSaveData == 0 && fSeq > 2) {
        					if (isNewDriverId) {
        						biometricBean.setIsSave(1);// 1 for save
        					} else {
        						biometricBean.setIsSave(2);
        					}
        					this.dispose();
        					break;
        				} else {
        					removeAllLabelIcon();
        					biometricBean.setIsSave(0);
        					FingerPrintAction.deleteAllCurrentUserId(auth, deviceIdList);
        					String errorMessage = BioMatricException.getException(enroll_Id);
        					System.out.println("\n Error Message: " + errorMessage + "Morpho Error Code is " + enroll_Id
        							+ "Finger Print less than 2");
        					this.dispose();
        				}
        			}
        		} else {
        			String errorMessage = BioMatricException.getException(enroll_Id);
        			if (errorMessage.equalsIgnoreCase("") || errorMessage.length() < 0) {
        				errorMessage = "Check Log File";
        			}
        			JOptionPane.showMessageDialog(null, errorMessage);
        			removeAllLabelIcon();
        			FingerPrintAction.deleteAllCurrentUserId(auth, deviceIdList);
        			System.out.println(" Error Message: " + errorMessage + " Error Code is " + enroll_Id);
        			this.dispose();
        			break;
        		}
        		if (Success) {
        			fSeq++;

        		}
        	}
        	if (fSeq > 10) {
        		if (isNewDriverId) {
        			biometricBean.setIsSave(1);// 1 for save
        		} else {
        			biometricBean.setIsSave(2);
        		}
        		this.dispose();
        	}
        }
    }

    private static void removeAllLabelIcon() {
        if (finger1.getIcon() != null) {
            finger1.setIcon(null);
            finger1.setText("Finger Print 1");
            finger1.revalidate();
        }
        if (finger2.getIcon() != null) {
            finger2.setIcon(null);
            finger2.setText("Finger Print 2");
            finger2.revalidate();
        }
        if (finger3.getIcon() != null) {
            finger3.setIcon(null);
            finger3.setText("Finger Print 3");
            finger3.revalidate();
        }
        if (finger4.getIcon() != null) {
            finger4.setIcon(null);
            finger4.setText("Finger Print 4");
            finger4.revalidate();
        }
        if (finger5.getIcon() != null) {
            finger5.setIcon(null);
            finger5.setText("Finger Print 5");
            finger5.revalidate();
        }
        if (finger6.getIcon() != null) {
            finger6.setIcon(null);
            finger6.setText("Finger Print 6");
            finger6.revalidate();
        }
        if (finger7.getIcon() != null) {
            finger7.setIcon(null);
            finger7.setText("Finger Print 7");
            finger7.revalidate();
        }
        if (finger8.getIcon() != null) {
            finger8.setIcon(null);
            finger8.setText("Finger Print 8");
            finger8.revalidate();
        }
        if (finger9.getIcon() != null) {
            finger9.setIcon(null);
            finger9.setText("Finger Print 9");
            finger9.revalidate();
        }
        if (finger10.getIcon() != null) {
            finger10.setIcon(null);
            finger10.setText("Finger Print 10");
            finger10.revalidate();
        }
    }
    @SuppressWarnings("unchecked")
    private void initComponents() {
        jPanel1 = new javax.swing.JPanel();
        finger1 = new javax.swing.JLabel();
        finger2 = new javax.swing.JLabel();
        finger3 = new javax.swing.JLabel();
        finger4 = new javax.swing.JLabel();
        finger5 = new javax.swing.JLabel();
        finger6 = new javax.swing.JLabel();
        finger7 = new javax.swing.JLabel();
        finger8 = new javax.swing.JLabel();
        finger9 = new javax.swing.JLabel();
        finger10 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        finger1.setText("     FINGER1");

        finger2.setText("     FINGER2");

        finger3.setText("     FINGER3");

        finger4.setText("     FINGER4");

        finger5.setText("     FINGER5");

        finger6.setText("     FINGER6");

        finger7.setText("     FINGER7");

        finger8.setText("     FINGER8");

        finger9.setText("     FINGER9");

        finger10.setText("     FINGER10");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(finger1, javax.swing.GroupLayout.DEFAULT_SIZE, 80, Short.MAX_VALUE)
                    .addComponent(finger6, javax.swing.GroupLayout.DEFAULT_SIZE, 80, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(finger2, javax.swing.GroupLayout.DEFAULT_SIZE, 80, Short.MAX_VALUE)
                    .addComponent(finger7, javax.swing.GroupLayout.DEFAULT_SIZE, 80, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(finger3, javax.swing.GroupLayout.DEFAULT_SIZE, 80, Short.MAX_VALUE)
                    .addComponent(finger8, javax.swing.GroupLayout.DEFAULT_SIZE, 80, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(finger4, javax.swing.GroupLayout.DEFAULT_SIZE, 80, Short.MAX_VALUE)
                    .addComponent(finger9, javax.swing.GroupLayout.DEFAULT_SIZE, 80, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(finger5, javax.swing.GroupLayout.DEFAULT_SIZE, 80, Short.MAX_VALUE)
                    .addComponent(finger10, javax.swing.GroupLayout.DEFAULT_SIZE, 80, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(finger2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(finger3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(finger1, javax.swing.GroupLayout.DEFAULT_SIZE, 125, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(finger5, javax.swing.GroupLayout.DEFAULT_SIZE, 125, Short.MAX_VALUE)
                        .addComponent(finger4, javax.swing.GroupLayout.DEFAULT_SIZE, 125, Short.MAX_VALUE)))
                .addGap(10, 10, 10)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(finger9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(finger6, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 125, Short.MAX_VALUE)
                    .addComponent(finger10, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 125, Short.MAX_VALUE)
                    .addComponent(finger8, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 125, Short.MAX_VALUE)
                    .addComponent(finger7, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 125, Short.MAX_VALUE)))
        );

        finger1.getAccessibleContext().setAccessibleName("");
        finger2.getAccessibleContext().setAccessibleName("");
        finger3.getAccessibleContext().setAccessibleName("");
        finger4.getAccessibleContext().setAccessibleName("");
        finger5.getAccessibleContext().setAccessibleName("");
        finger6.getAccessibleContext().setAccessibleName("");
        finger7.getAccessibleContext().setAccessibleName("");
        finger8.getAccessibleContext().setAccessibleName("");
        finger9.getAccessibleContext().setAccessibleName("");
        finger10.getAccessibleContext().setAccessibleName("");

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

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
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
    }
    private static javax.swing.JLabel finger1;
    private static javax.swing.JLabel finger10;
    private static javax.swing.JLabel finger2;
    private static javax.swing.JLabel finger3;
    private static javax.swing.JLabel finger4;
    private static javax.swing.JLabel finger5;
    private static javax.swing.JLabel finger6;
    private static javax.swing.JLabel finger7;
    private static javax.swing.JLabel finger8;
    private static javax.swing.JLabel finger9;
    private javax.swing.JPanel jPanel1;
    // End of variables declaration//GEN-END:variables

    private void setFingerImage(byte[] image, int i){
        BufferedImage getNewBufferedImage = FingerPrintAction.byteArrayToImage(image);
        BufferedImage myBufferedImage = FingerPrintAction.changeSizeOfImage(getNewBufferedImage);
        if (i == 1) {
            finger1.setText("");
            if (myBufferedImage != null) {
                finger1.setIcon(new ImageIcon(myBufferedImage));
            }
        } else if (i == 2) {
            finger2.setText("");
            if (myBufferedImage != null) {
                finger2.setIcon(new ImageIcon(myBufferedImage));
            }
        } else if (i == 3) {
            finger3.setText("");
            if (myBufferedImage != null) {
                finger3.setIcon(new ImageIcon(myBufferedImage));
            }
        } else if (i == 4) {
            finger4.setText("");
            if (myBufferedImage != null) {
                finger4.setIcon(new ImageIcon(myBufferedImage));
            }
        } else if (i == 5) {
            finger5.setText("");
            if (myBufferedImage != null) {
                finger5.setIcon(new ImageIcon(myBufferedImage));
            }
        } else if (i == 6) {
            finger6.setText("");
            if (myBufferedImage != null) {
                finger6.setIcon(new ImageIcon(myBufferedImage));
            }
        } else if (i == 7) {
            finger7.setText("");
            if (myBufferedImage != null) {
                finger7.setIcon(new ImageIcon(myBufferedImage));
            }
        } else if (i == 8) {
            finger8.setText("");
            if (myBufferedImage != null) {
                finger8.setIcon(new ImageIcon(myBufferedImage));
            }
        } else if (i == 9) {
            finger9.setText("");
            if (myBufferedImage != null) {
                finger9.setIcon(new ImageIcon(myBufferedImage));
            }
        } else if (i == 10) {
            finger10.setText("");
            if (myBufferedImage != null) {
                finger10.setIcon(new ImageIcon(myBufferedImage));
            }
        }
    }
    private void attachPhotoToLabel(BioMatricBean bioMatricBean, int i) {
        if (i == 1) {
            finger1.setText("");
            BufferedImage getNewBufferedImage = FingerPrintAction.byteArrayToImage(bioMatricBean.getCaptureFirstFingerImage1());
            BufferedImage myBufferedImage = FingerPrintAction.changeSizeOfImage(getNewBufferedImage);
            if (myBufferedImage != null) {
                finger1.setIcon(new ImageIcon(myBufferedImage));
            }
        } else if (i == 2) {
            finger2.setText("");
            BufferedImage getNewBufferedImage = FingerPrintAction.byteArrayToImage(bioMatricBean.getCaptureSecondFingerImage2());
            BufferedImage myBufferedImage = FingerPrintAction.changeSizeOfImage(getNewBufferedImage);
            if (myBufferedImage != null) {
                finger2.setIcon(new ImageIcon(myBufferedImage));
            }
        } else if (i == 3) {
            finger3.setText("");
            BufferedImage getNewBufferedImage = FingerPrintAction.byteArrayToImage(bioMatricBean.getCaptureThirdFingerImage3());
            BufferedImage myBufferedImage = FingerPrintAction.changeSizeOfImage(getNewBufferedImage);
            if (myBufferedImage != null) {
                finger3.setIcon(new ImageIcon(myBufferedImage));
            }
        } else if (i == 4) {
            finger4.setText("");
            BufferedImage getNewBufferedImage = FingerPrintAction.byteArrayToImage(bioMatricBean.getCaptureFourthFingerImage4());
            BufferedImage myBufferedImage = FingerPrintAction.changeSizeOfImage(getNewBufferedImage);
            if (myBufferedImage != null) {
                finger4.setIcon(new ImageIcon(myBufferedImage));
            }
        } else if (i == 5) {
            finger5.setText("");
            BufferedImage getNewBufferedImage = FingerPrintAction.byteArrayToImage(bioMatricBean.getCaptureFivethFingerImage5());
            BufferedImage myBufferedImage = FingerPrintAction.changeSizeOfImage(getNewBufferedImage);
            if (myBufferedImage != null) {
                finger5.setIcon(new ImageIcon(myBufferedImage));
            }
        } else if (i == 6) {
            finger6.setText("");
            BufferedImage getNewBufferedImage = FingerPrintAction.byteArrayToImage(bioMatricBean.getCaptureSixthFingerImage6());
            BufferedImage myBufferedImage = FingerPrintAction.changeSizeOfImage(getNewBufferedImage);
            if (myBufferedImage != null) {
                finger6.setIcon(new ImageIcon(myBufferedImage));
            }
        } else if (i == 7) {
            finger7.setText("");
            BufferedImage getNewBufferedImage = FingerPrintAction.byteArrayToImage(bioMatricBean.getCaptureSeventhFingerImage7());
            BufferedImage myBufferedImage = FingerPrintAction.changeSizeOfImage(getNewBufferedImage);
            if (myBufferedImage != null) {
                finger7.setIcon(new ImageIcon(myBufferedImage));
            }
        } else if (i == 8) {
            finger8.setText("");
            BufferedImage getNewBufferedImage = FingerPrintAction.byteArrayToImage(bioMatricBean.getCaptureEighthFingerImage8());
            BufferedImage myBufferedImage = FingerPrintAction.changeSizeOfImage(getNewBufferedImage);
            if (myBufferedImage != null) {
                finger8.setIcon(new ImageIcon(myBufferedImage));
            }
        } else if (i == 9) {
            finger9.setText("");
            BufferedImage getNewBufferedImage = FingerPrintAction.byteArrayToImage(bioMatricBean.getCaptureNinthFingerImage9());
            BufferedImage myBufferedImage = FingerPrintAction.changeSizeOfImage(getNewBufferedImage);
            if (myBufferedImage != null) {
                finger9.setIcon(new ImageIcon(myBufferedImage));
            }
        } else if (i == 10) {
            finger10.setText("");
            BufferedImage getNewBufferedImage = FingerPrintAction.byteArrayToImage(bioMatricBean.getCaptureTenthFingerImage10());
            BufferedImage myBufferedImage = FingerPrintAction.changeSizeOfImage(getNewBufferedImage);
            if (myBufferedImage != null) {
                finger10.setIcon(new ImageIcon(myBufferedImage));
            }
        }
    }

    private void removePhotofromLabel(int i) {
        switch (i) {
            case 1:
                finger1.setIcon(null);
                break;
            case 2:
                finger2.setIcon(null);
                break;
            case 3:
                finger3.setIcon(null);
                break;
            case 4:
                finger4.setIcon(null);
                break;
            case 5:
                finger5.setIcon(null);
                break;
            case 6:
                finger6.setIcon(null);
                break;
            case 7:
                finger7.setIcon(null);
                break;
            case 8:
                finger8.setIcon(null);
                break;
            case 9:
                finger9.setIcon(null);
                break;
            case 10:
                finger10.setIcon(null);
                break;
            default:
                break;
        } 
    }
    
    private Triple<Integer, byte[], byte[]> captureFingurePrint(Connection conn, LoadLibrary auth, int seq , ArrayList<Integer> deleteDriverList,StringBuilder error) {
        Integer enrollmentId = Misc.getUndefInt();
        int identifyStatus = Misc.getUndefInt();
        byte[] capturedImage = null;
        byte[] captureTemplate = null;
        int currentDriverId = biometricBean != null ? biometricBean.getDriverId() : Misc.getUndefInt();
        try {
        	boolean deviceConnected = auth.isDeviceConnected();
        	if(error != null){
        		error.append("\n@@@Start captureFingurePrint@@@");
        		error.append("\ndeviceConnected : " + deviceConnected);
        	}
//        	System.out.println("deviceConnected" + deviceConnected);
        	if (!deviceConnected) {
        		enrollmentId = -1;
        		return new Triple<Integer, byte[], byte[]>(enrollmentId, captureTemplate, capturedImage);
        	}
        	//check finger
        	identifyStatus = auth.identify(20);
        	if(error != null){
        		error.append("\nidentifyStatus : " + identifyStatus);
        	}
        	if (identifyStatus == 0) {
        		String userId = auth.getUserId();
        		String[] arg = userId.split("_");
        		int detectedDriverId = arg != null && arg.length > 0 ? Misc.getParamAsInt(arg[0]) : Misc.getUndefInt();
        		if(error != null){
            		error.append("\ndetectedDriverId : " + userId);
            		error.append("\ncurrentDriverId : " + currentDriverId);
            	}
        		if(detectedDriverId != currentDriverId){
        			BioMatricBean driverData = GateInDao.getDriverDetails(conn, arg[0], "driverId");
        			//auth.deleteUserById(userId);
        			int responseVehicleDialog = JOptionPane.showConfirmDialog(this, "Finger Already Enrolled Do you want to continue with ?\nDriver ID: " + driverData.getDriverId() +"\nDriver Name: " + driverData.getDriverName() + "\nDriver DL No: " + driverData.getDriverDlNumber(), UIConstant.dialogTitle, JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
        			
        			System.out.print("##### Confirmation Value :#####" + responseVehicleDialog);
        			if (responseVehicleDialog == 1 || responseVehicleDialog == -1) {
        				boolean found = false;
        				for(int i=0,is=deleteDriverList == null ? 0 : deleteDriverList.size();i<is;i++){
        					found = detectedDriverId == deleteDriverList.get(i);
        					if(found)
        						break;
        				}
        				if(!found)
        					deleteDriverList.add(detectedDriverId);
        			} else {
        				if(driverData != null && !Misc.isUndef(driverData.getDriverId())){
        					Utils.copy(driverData, biometricBean);
        					if(parent != null){
        						parent.showExistingDriverDetails();
        					}
        				}
        				
        			}
        			if(error != null){
                		error.append("\nDriver finger exist but not match with current");
                		error.append("\nContinue with current : " + !(responseVehicleDialog == 1 || responseVehicleDialog == -1));
                		error.append("\nfinal Driver DriverId : " + biometricBean.getDriverId());
                	}
        		}
        	}else{
        		
        	}
        	JOptionPane.showMessageDialog(null, "**Please, Put same finger on reader once again**");
        	if(error != null){
        		error.append("\nEnroll Varified Finger");
        	}
        	enrollmentId = auth.captureFPData(30);
        	if(error != null){
        		error.append("\ncaptureFPData : "+enrollmentId);
        	}
//        	System.out.println("captureFPData : " + enrollmentId);
        	if (enrollmentId == 0) {
        		captureTemplate = auth.getCapturedTemplate();
        	} else {
        		return new Triple<Integer, byte[], byte[]>(enrollmentId, captureTemplate, capturedImage);
        	}
        	if (enrollmentId == 0) {
        		//FingerPrintAction.setFingerTemplateToBean(seq, captureTemplate, biometricBean);
        		capturedImage = FingerPrintAction.getProcessedImage(auth);
        		if(error != null){
            		error.append("\ncapturedImage : "+capturedImage);
            	}
        		//FingerPrintAction.createImageFromByte(auth, seq, capturedImageWithISO, biometricBean);
        	}
        } catch (UnsatisfiedLinkError e) {
            e.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new Triple<Integer, byte[], byte[]>(enrollmentId, captureTemplate, capturedImage);
    }
    
    private int checkDriverExistance(LoadLibrary auth, String driver_name, String driver_DL, int i) {
        int enrollmentId = Misc.getUndefInt();
        int captureFPData = Misc.getUndefInt();
        Connection conn = null;
        boolean destroyIt = false;
        try {
        	conn = DBConnectionPool.getConnectionFromPoolNonWeb();
        	boolean deviceConnected = auth.isDeviceConnected();
        	System.out.println("deviceConnected" + deviceConnected);
        	if (!deviceConnected) {
        		enrollmentId = -1;
        		return enrollmentId;
        	}
        	if (Utils.isNull(driverId)) {
        		if (biometricBean != null && biometricBean.getDriverId() != Misc.getUndefInt()) {
        			driverId = Integer.toString(biometricBean.getDriverId());
        			System.out.println("Old Driver ID " + driverId);
        			isNewDriverId = false;
        		} else {
        			driverId = Integer.toString(FingerPrintAction.getNewDriverId(conn));
        			System.out.println("New Driver ID " + driverId);
        			isNewDriverId = true;
        		}
        	}
        	if (driverId == null || driverId.equalsIgnoreCase("")) {
        		JOptionPane.showMessageDialog(null, "Please Check Internet Or Save HTTP Setting");
        	}
        	newFingerID = driverId + "_" + i;
        	System.out.println("newFingerID : " + newFingerID);
        	captureFPData = auth.captureFPData(30);
        	System.out.println("captureFPData : " + captureFPData);

        	byte[] captureTemplate = null;
        	if (captureFPData == 0) {
        		captureTemplate = auth.getCapturedTemplate();
        		enrollmentId = auth.enrollUser(newFingerID, driver_name, driver_DL, captureTemplate, captureTemplate.length);
        	} else {
        		return captureFPData;
        	}
        	if (enrollmentId == 0) {
        		FingerPrintAction.setFingerTemplateToBean(i, captureTemplate, biometricBean);
        		byte[] capturedImageWithISO = auth.getCapturedImage();
        		FingerPrintAction.createImageFromByte(auth, i, capturedImageWithISO, biometricBean);
        	}
        } catch (UnsatisfiedLinkError e) {
            e.printStackTrace();
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
        return enrollmentId;
    }
}