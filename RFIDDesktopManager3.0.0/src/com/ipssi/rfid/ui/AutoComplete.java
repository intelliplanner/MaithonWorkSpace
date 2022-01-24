/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ipssi.rfid.ui;

import java.awt.Color;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.plaf.ComboBoxUI;
import javax.swing.plaf.basic.BasicArrowButton;
import javax.swing.plaf.basic.BasicComboBoxUI;

import com.ipssi.gen.utils.DBConnectionPool;
import com.ipssi.gen.utils.Misc;
import com.ipssi.rfid.beans.ComboItem;
import com.ipssi.rfid.database.DropDownValues;
import com.ipssi.rfid.processor.TokenManager;
import com.ipssi.rfid.processor.Utils;

/**
 *
 * @author Vi$ky
 */
public class AutoComplete {

    public final JTextField tf;
    private ComboKeyEvent keyEvent = null;
//    private Connection conn = null;

    public AutoComplete(final JComboBox vehicle) {
//        this.conn = conn;
//        vehicle.setEditable(false);
        vehicle.setUI(ColorArrowUI.createUI(vehicle));

        tf = (JTextField) vehicle.getEditor().getEditorComponent();
        tf.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                String text = tf.getText() + (Character.isLetterOrDigit(e.getKeyChar()) || e.getKeyChar() == '_' ? e.getKeyChar() : "");
                if (text.length() <= 3) {
                    setModel(vehicle, new DefaultComboBoxModel(), tf.getText());
                    vehicle.hidePopup();
                } else {
                    DefaultComboBoxModel m = getSuggestedModel(text);
                    if (m.getSize() == 0 || hide_flag) {
                        vehicle.hidePopup();
                        hide_flag = false;
                    } else {
                        setModel(vehicle, m, tf.getText());
                        vehicle.showPopup();
                    }
                }
            }

            @Override
            public void keyPressed(KeyEvent e) {
                String text = tf.getText();
                int code = e.getKeyCode();
                if (code == KeyEvent.VK_ENTER) {
//                    JOptionPane.showMessageDialog(null, tf.getText());
                    int val = getValue(vehicle);
//                    if (Misc.isUndef(val)) {
//                        tf.setBackground(Color.RED);
//                    } else {
//                        tf.setBackground(Color.GREEN);
//                    }
                    vehicle.hidePopup();
                    hide_flag = true;
                } else if (code == KeyEvent.VK_ESCAPE) {
                    hide_flag = true;
                } else if (code == KeyEvent.VK_RIGHT) {
                } else {
                    tf.setBackground(Color.WHITE);
                }
                if (keyEvent != null) {
                    keyEvent.onKeyPress(e);
                }
            }
        });
//        tf.setText("");
        
    }

    public int getValue(JComboBox vehicle) {
        return DropDownValues.getComboSelectedVal(vehicle);
    }
    private boolean hide_flag = false;

    private void setModel(JComboBox vehicle, DefaultComboBoxModel mdl, String str) {
        vehicle.setModel(mdl);
        vehicle.setSelectedIndex(-1);
        tf.setText(str);
    }

//    public void setVehicleList(JComboBox combo, String text) {
//        ArrayList<ComboItem> transporterList = getVehicleList(text);
//        for (int i = 0; i < transporterList.size(); i++) {
//            combo.addItem(transporterList.get(i));
//            combo.setSelectedIndex(0);
//        }
//    }
    private ArrayList<ComboItem> getVehicleList(Connection conn, String text) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        ArrayList<ComboItem> vehList = new ArrayList<ComboItem>();
        String query = " select vehicle.id,vehicle.std_name from vehicle join "
                + " (select distinct(vehicle.id) vehicle_id from vehicle "
                + " left outer join port_nodes custleaf on (custleaf.id = vehicle.customer_id) "
                + " left outer join vehicle_access_groups on (vehicle_access_groups.vehicle_id = vehicle.id) "
                + " left outer join port_nodes leaf on (leaf.id = vehicle_access_groups.port_node_id) "
                + " join port_nodes anc  on (anc.id in (" + TokenManager.portNodeId + ") and ((anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) "
                + " or  (anc.lhs_number <= custleaf.lhs_number and anc.rhs_number >= custleaf.rhs_number))) ) vi on vi.vehicle_id = vehicle.id "
                + " where status in (1) and vehicle.std_name like '%" + text + "%'";
        try {
            ps = conn.prepareStatement(query);
            rs = ps.executeQuery();
            while (rs.next()) {
                vehList.add(new ComboItem(Misc.getRsetInt(rs, 1), rs.getString(2)));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (ps != null) {
                    ps.close();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return vehList;
    }

    private DefaultComboBoxModel getSuggestedModel(String text) {
        DefaultComboBoxModel m = new DefaultComboBoxModel();
        Connection conn = null;
        boolean destroyIt = false;
        try {
            conn = DBConnectionPool.getConnectionFromPoolNonWeb();
            java.util.List<ComboItem> list = getVehicleList(conn, text);
            for (int i = 0, is = list == null ? 0 : list.size(); i < is; i++) {
                ComboItem item = list.get(i);
                if (item != null && !Utils.isNull(item.getLabel()) && item.getLabel().toUpperCase().contains(text.toUpperCase())) {
                    m.addElement(item);
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
        return m;
    }

   

    public static interface ComboKeyEvent {
        void onKeyPress(KeyEvent e);
    }
    public ComboKeyEvent getKeyEvent() {
	return keyEvent;
    }
    public void setKeyEvent(ComboKeyEvent keyEvent) {
		this.keyEvent = keyEvent;
	}
    public String getText() {
        return tf.getText();
    }
    
    static class ColorArrowUI extends BasicComboBoxUI{
        public static ComboBoxUI createUI(JComponent c) {
	        return new ColorArrowUI();
	    }
          @Override 
	    protected JButton createArrowButton() {
	    	BasicArrowButton retval = new BasicArrowButton(
		            BasicArrowButton.SOUTH,
		            Color.cyan, Color.magenta,
		            Color.yellow, Color.blue);
                               
	    	retval.setVisible(false);
	    	retval.setEnabled(false);
	    	return retval;
	    }
    }
    
    
   
}
