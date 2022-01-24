package com.ipssi.rfid.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;

import com.ipssi.gen.utils.Misc;
import com.ipssi.rfid.processor.TokenManager;

public class ConfirmDialog extends javax.swing.JDialog {

	public static final int OK = 0;
	public static final int CANCEL = 1;
	private final JPanel contentPanel = new JPanel();
	private JLabel label = null;
	public int result =  CANCEL;
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			/*ConfirmDialog dialog = new ConfirmDialog(null,"test");
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);*/
			
			for (int i = 0; i < 20; i++) {
				
				
				TokenManager.lastStationCount = (++TokenManager.lastStationCount) % TokenManager.noOfStation;
				String nextStationLabel = " "+ (TokenManager.nextStationNumber + TokenManager.lastStationCount);
				System.out.println(nextStationLabel);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static int showDialog(JFrame parent,String msg) {
		try {
			ConfirmDialog dialog = new ConfirmDialog(parent,msg);
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
			System.out.println(dialog.result);
			return dialog.result;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return Misc.getUndefInt();
	}

	/**
	 * Create the dialog.
	 */
	public ConfirmDialog(JFrame parent,String message) {
	  	super(parent, true);
		setBounds(100, 100, 200, 200);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setLayout(new FlowLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.LINE_START);
		{
			JLabel label = new JLabel(message);
			contentPanel.add(label);
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.setActionCommand("OK");
				okButton.addActionListener(new java.awt.event.ActionListener() {
		            public void actionPerformed(java.awt.event.ActionEvent evt) {
		                //clearButtonActionPerformed(evt);
		            	result=OK;
		            	close();
		            }
		        });
				okButton.addKeyListener(new java.awt.event.KeyAdapter() {
		            public void keyPressed(java.awt.event.KeyEvent evt) {
		            	if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
		            		result=OK;	
		            		close();
		            	}
		            }
		        });
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.addActionListener(new java.awt.event.ActionListener() {
		            public void actionPerformed(java.awt.event.ActionEvent evt) {
		                //clearButtonActionPerformed(evt);
		            	result=CANCEL;	
	            		close();
		            }
		        });
				cancelButton.addKeyListener(new java.awt.event.KeyAdapter() {
		            public void keyPressed(java.awt.event.KeyEvent evt) {
		            	if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
		            		result=CANCEL;	
		            		close();
		            	}
		            }
		        });
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
	}
	private void close(){
		this.dispose();
	}
}
