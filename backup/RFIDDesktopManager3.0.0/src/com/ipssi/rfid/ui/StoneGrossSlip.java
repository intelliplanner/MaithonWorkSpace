/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ipssi.rfid.ui;

import com.ipssi.gen.utils.DBConnectionPool;
import com.ipssi.gen.utils.Misc;
import com.ipssi.rfid.beans.TPRecord;
import com.ipssi.rfid.constant.UIConstant;
import com.ipssi.rfid.database.DropDownValues;
import com.ipssi.rfid.database.GateInDao;
import com.ipssi.rfid.db.RFIDMasterDao;
import com.ipssi.rfid.processor.TokenManager;
import com.ipssi.rfid.processor.Utils;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.print.PrinterException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

/**
 *
 * @author Vi$ky
 */
public class StoneGrossSlip extends javax.swing.JDialog {

    /**
     * Creates new form FlyAshGrossSlip
     */
    TPRecord tpRecord = null;
    private String supplier = "MPL";
    private String dispatchPlace = "";
    private String tprId = "";
    private String landingArea = "";
    private String challanNo = "";
    private String lrNo = "";
    private String partyName = "";
    private String grossWts = "";
    private String tareWts = "";
    private String netWts = "";
    private String gateInTime = "";
    private String grossTime = "";
//  private String tareTime = "";
    private String vehicleNo = "";
//  private String vehicleType = "";
    private String lrDate = "";
    private String product = "";
//    private String gateEntryUser = "";
//    private String firstWbUse = "";
//    private String secondWbUse = "";
    private String checkedBy = "";
    private String grade = "";
    private String consignee = "";
    private String challanDate = "";
    private String expectedDate = "";
    private String coalTransporter = "";
    private String carryingTranporter;
    private String preparedBy = "";

    public StoneGrossSlip(java.awt.Frame parent, boolean modal, TPRecord tpRecord) {
        super(parent, modal);
        initComponents();
        textPane.setBackground(Color.WHITE);
        this.tpRecord = tpRecord;
         
        center();
        initializeVariables();
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                printData();
            }
        });
        print();
    }

    private void center() {
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension us = getSize();
        int x = (screen.width - us.width) / 2;
        int y = (screen.height - us.height) / 2;
        setLocation(x, y);
    }

    void printData() {

        StyledDocument doc = textPane.getStyledDocument();
        Style def = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);
        Style regular = doc.addStyle("regular", def);

        // Create an italic style
        Style italic = doc.addStyle("italic", regular);
        StyleConstants.setItalic(italic, true);

        // Create a bold style
        Style bold = doc.addStyle("bold", regular);
        StyleConstants.setBold(bold, true);

        // Create a small style
        Style small = doc.addStyle("small", bold);
        StyleConstants.setFontSize(small, 10);
//StyleConstants.setAlignment(small, StyleConstants.ALIGN_CENTER);
        Style medium = doc.addStyle("small", regular);
        StyleConstants.setFontSize(medium, 10);
//        StyleConstants.setAlignment(medium, StyleConstants.ALIGN_CENTER);
        // Create a large style
        Style large = doc.addStyle("large", bold);
        StyleConstants.setFontSize(large, 12);
//        StyleConstants.setAlignment(large, StyleConstants.ALIGN_CENTER);

        Style label = doc.addStyle("large", bold);
        StyleConstants.setFontSize(label, 10);
//                                StyleConstants.setAlignment(label, StyleConstants.ALIGN_RIGHT);
        Style text = doc.addStyle("large", regular);
        StyleConstants.setFontSize(text, 10);
        // Create a superscript style
        Style superscript = doc.addStyle("superscript", regular);
        StyleConstants.setSuperscript(superscript, true);
        // Create a highlight style
        Style highlight = doc.addStyle("highlight", regular);
        StyleConstants.setBackground(highlight, Color.yellow);
        try {

            // Original Print
            doc.setLogicalStyle(8, regular);
            doc.insertString(0, "                     MAITHON POWER LIMITED\n", large);
            doc.insertString(doc.getLength(), "       VILL: DAMBHUI, PO: BARBINDIA, THANA: NIRSA DHANBAD-828205 DHANBAD\n", medium);
            doc.insertString(doc.getLength(), "                   CHALLAN CUM WEIGHMENT SLIP(ORIGINAL)\n", small);
            doc.insertString(doc.getLength(), "---------------------------------"
                    + "----------------------------------------------------\n", regular);
            doc.insertString(doc.getLength(), getLabelString("Consignor:", 16), label);
            doc.insertString(doc.getLength(), getString("Maithon Power Ltd.", 20), text);
            doc.insertString(doc.getLength(), getLabelString("Challan#:", 16), label);
            doc.insertString(doc.getLength(), Misc.getPrintableString(challanNo) + "\n", text);

            doc.insertString(doc.getLength(), getLabelString("TIN:", 16), label);
            doc.insertString(doc.getLength(), getString(/*dispatchPlace*/"20192005195", 20), text);
            doc.insertString(doc.getLength(), getLabelString("Challan Date:", 16), label);
            doc.insertString(doc.getLength(), Misc.getPrintableString(challanDate) + "\n", text);

            doc.insertString(doc.getLength(), getLabelString("CST:", 16), label);
            doc.insertString(doc.getLength(), getString("20192005195", 20), text);
            doc.insertString(doc.getLength(), getLabelString("Truck#:", 16), label);
            doc.insertString(doc.getLength(), Misc.getPrintableString(vehicleNo) + "\n", text);

            doc.insertString(doc.getLength(), "-------------------------"
                    + "------------------------------------------------------------\n", regular);


            doc.insertString(doc.getLength(), getLabelString("Coal Transporter:", 21), label);
            doc.insertString(doc.getLength(), getString(coalTransporter,26), text);
            doc.insertString(doc.getLength(), getLabelString("LR#:", 5), label);
            doc.insertString(doc.getLength(), Misc.getPrintableString(lrNo) + "\n", text);

            doc.insertString(doc.getLength(), getLabelString("Carrying Transporter:", 16), label);
            doc.insertString(doc.getLength(), Misc.getPrintableString(carryingTranporter) + "\n", text);

            doc.insertString(doc.getLength(), getLabelString("Value of Goods:", 21), label);
            doc.insertString(doc.getLength(), getString("NIL", 11), label);
            doc.insertString(doc.getLength(), getLabelString("Category:", 20), label);
            doc.insertString(doc.getLength(), "Normal Goods" + "\n", label);

            doc.insertString(doc.getLength(), getLabelString("Expected Date of Delivery:", 21), label);
            doc.insertString(doc.getLength(),  Misc.getPrintableString(expectedDate) + "\n", label);

            doc.insertString(doc.getLength(), "-------------------------"
                    + "------------------------------------------------------------\n", regular);

            doc.insertString(doc.getLength(), getLabelString("Description of Goods", 22), label);
            doc.insertString(doc.getLength(), getString("", 13), text);
            doc.insertString(doc.getLength(), getLabelString("Wt Gross(MT):", 17), label);
            doc.insertString(doc.getLength(), Misc.getPrintableString(grossWts) + "\n", text);

            doc.insertString(doc.getLength(), getLabelString("", 9), label);
            doc.insertString(doc.getLength(), getString("Stone", 26), text);
            doc.insertString(doc.getLength(), getLabelString("Wt Tare(MT):", 17), label);
            doc.insertString(doc.getLength(), Misc.getPrintableString(tareWts) + "\n", text);

            doc.insertString(doc.getLength(), getLabelString(" ", 16), label);
            doc.insertString(doc.getLength(), getString(" ", 20), text);
            doc.insertString(doc.getLength(), getLabelString("Wt Net(MT):", 16), label);
            doc.insertString(doc.getLength(), Misc.getPrintableString(netWts) + "\n", text);

            doc.insertString(doc.getLength(), getLabelString("WB Date & Time:", 16), label);
            doc.insertString(doc.getLength(), getString(grossTime, 20), text);

            doc.insertString(doc.getLength(), "--------------------------------------"
                    + "------------------------------------------------\n", regular);

            //            doc.insertString(doc.getLength(), getString(gateEntryUser, 19), text);
            doc.insertString(doc.getLength(), getString(preparedBy, 40), text);
//            doc.insertString(doc.getLength(), getString(secondWbUse, 18), text);
            doc.insertString(doc.getLength(), Misc.getPrintableString(checkedBy) + "\n", text);

            doc.insertString(doc.getLength(), getString("  Prepared By", 34), label);
//            doc.insertString(doc.getLength(), getLabelString("Gross WB User", 20), label);
//            doc.insertString(doc.getLength(), getLabelString("Tare WB User", 20), label);
            doc.insertString(doc.getLength(), getLabelString("Checked By", 16) + "\n\n", label);

            doc.insertString(doc.getLength(), ".........................................."
                    + "...............................................\n\n", regular);

// Duplicate Print

            doc.setLogicalStyle(8, regular);
            doc.insertString(doc.getLength(), "                     MAITHON POWER LIMITED\n", large);
            doc.insertString(doc.getLength(), "       VILL: DAMBHUI, PO: BARBINDIA, THANA: NIRSA DHANBAD-828205 DHANBAD\n", medium);
            doc.insertString(doc.getLength(), "                   CHALLAN CUM WEIGHMENT SLIP(DUPLICATE)\n", small);
            doc.insertString(doc.getLength(), "---------------------------------"
                    + "----------------------------------------------------\n", regular);
            doc.insertString(doc.getLength(), getLabelString("Consignor:", 16), label);
            doc.insertString(doc.getLength(), getString("Maithon Power Ltd.", 20), text);
            doc.insertString(doc.getLength(), getLabelString("Challan#:", 16), label);
            doc.insertString(doc.getLength(), Misc.getPrintableString(challanNo) + "\n", text);

            doc.insertString(doc.getLength(), getLabelString("TIN:", 16), label);
            doc.insertString(doc.getLength(), getString(/*dispatchPlace*/"20192005195", 20), text);
            doc.insertString(doc.getLength(), getLabelString("Challan Date:", 16), label);
            doc.insertString(doc.getLength(), Misc.getPrintableString(challanDate) + "\n", text);

            doc.insertString(doc.getLength(), getLabelString("CST:", 16), label);
            doc.insertString(doc.getLength(), getString("20192005195", 20), text);
            doc.insertString(doc.getLength(), getLabelString("Truck#:", 16), label);
            doc.insertString(doc.getLength(), Misc.getPrintableString(vehicleNo) + "\n", text);

            doc.insertString(doc.getLength(), "-------------------------"
                    + "------------------------------------------------------------\n", regular);


//            doc.insertString(doc.getLength(), getLabelString("Coal Transporter:", 21), label);
//            doc.insertString(doc.getLength(), Misc.getPrintableString(coalTransporter) + "\n", text);

            doc.insertString(doc.getLength(), getLabelString("Coal Transporter:", 21), label);
            doc.insertString(doc.getLength(), getString(coalTransporter,26), text);
            doc.insertString(doc.getLength(), getLabelString("LR#:", 5), label);
            doc.insertString(doc.getLength(), Misc.getPrintableString(lrNo) + "\n", text);
            
            doc.insertString(doc.getLength(), getLabelString("Carrying Transporter:", 16), label);
            doc.insertString(doc.getLength(), Misc.getPrintableString(carryingTranporter) + "\n", text);

            doc.insertString(doc.getLength(), getLabelString("Value of Goods:", 21), label);
            doc.insertString(doc.getLength(), getString("NIL", 11), label);
            doc.insertString(doc.getLength(), getLabelString("Category:", 20), label);
            doc.insertString(doc.getLength(), "Normal Goods" + "\n", label);

            doc.insertString(doc.getLength(), getLabelString("Expected Date of Delivery:", 21), label);
            doc.insertString(doc.getLength(),  Misc.getPrintableString(expectedDate) + "\n", label);

            doc.insertString(doc.getLength(), "-------------------------"
                    + "------------------------------------------------------------\n", regular);

            doc.insertString(doc.getLength(), getLabelString("Description of Goods", 22), label);
            doc.insertString(doc.getLength(), getString("", 13), text);
            doc.insertString(doc.getLength(), getLabelString("Wt Gross(MT):", 17), label);
            doc.insertString(doc.getLength(), Misc.getPrintableString(grossWts) + "\n", text);

            doc.insertString(doc.getLength(), getLabelString("", 9), label);
            doc.insertString(doc.getLength(), getString("Stone", 27), text);
            doc.insertString(doc.getLength(), getLabelString("Wt Tare(MT):", 16), label);
            doc.insertString(doc.getLength(), Misc.getPrintableString(tareWts) + "\n", text);

            doc.insertString(doc.getLength(), getLabelString("", 16), label);
            doc.insertString(doc.getLength(), getString(" ", 20), text);
            doc.insertString(doc.getLength(), getLabelString("Wt Net(MT):", 16), label);
            doc.insertString(doc.getLength(), Misc.getPrintableString(netWts) + "\n", text);

            doc.insertString(doc.getLength(), getLabelString("WB Date & Time:", 16), label);
            doc.insertString(doc.getLength(), getString(grossTime, 20), text);

            doc.insertString(doc.getLength(), "--------------------------------------"
                    + "------------------------------------------------\n", regular);

            //            doc.insertString(doc.getLength(), getString(gateEntryUser, 19), text);
            doc.insertString(doc.getLength(), getString(preparedBy, 40), text);
//            doc.insertString(doc.getLength(), getString(secondWbUse, 18), text);
            doc.insertString(doc.getLength(), Misc.getPrintableString(checkedBy) + "\n", text);

            doc.insertString(doc.getLength(), getString("  Prepared By", 34), label);
//            doc.insertString(doc.getLength(), getLabelString("Gross WB User", 20), label);
//            doc.insertString(doc.getLength(), getLabelString("Tare WB User", 20), label);
            doc.insertString(doc.getLength(), getLabelString("Checked By", 16) + "\n", label);


        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

        private static String getString(String str1, int defaultLength) {
        if (Utils.isNull(str1)) {
            str1 = "";
        }
        int strLen = str1.length();
        if(strLen > defaultLength )
            str1 = str1.substring(0, defaultLength);
        System.out.println(str1 + " new Length: " + str1.length());
        int diffLength = defaultLength - strLen;
        String str2 = " ";
        String str3 = "";
        for (int i = 0, is = str1 == null ? 0 : diffLength; i < is; i++) {
            str3 += str2;
        }
        
        str1 += str3;
        
        return str1;
    }

    private String getLabelString(String str1, int defaultLength) {
        if (Utils.isNull(str1)) {
            str1 = "";
        }
        int strLen = str1.length();
        System.out.println(str1 + " Length: " + strLen);
        int diffLength = defaultLength - strLen;
        String str2 = " ";
        String str3 = "";
        for (int i = 0, is = str1 == null ? 0 : diffLength; i < is; i++) {
            str3 += str2;
        }
        str3 = str3 + str1;

        return str3;
    }

    private void print() {
        try {
            textPane.setContentType("text/plain");
            boolean done = textPane.print();
            if (done) {
                System.out.println("Printing is done");
            } else {
                System.out.println("Error while printing");
            }
        } catch (PrinterException ex) {
            Logger.getLogger(PrintData.class.getName()).log(Level.SEVERE, null, ex);
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

        jScrollPane1 = new javax.swing.JScrollPane();
        textPane = new javax.swing.JTextPane();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        textPane.setEditable(false);
        jScrollPane1.setViewportView(textPane);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 605, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 638, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        pack();
    }// </editor-fold>                        

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
            java.util.logging.Logger.getLogger(FlyAshGrossSlip.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(FlyAshGrossSlip.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(FlyAshGrossSlip.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(FlyAshGrossSlip.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                StoneGrossSlip dialog = new StoneGrossSlip(new javax.swing.JFrame(), true, null);
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
                dialog.setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify                     
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextPane textPane;
    // End of variables declaration                   

    private void initializeVariables() {
        if (tpRecord != null) {
            Connection conn = null;
            boolean destroyIt = false;
            try {
                conn = DBConnectionPool.getConnectionFromPoolNonWeb();
                tpRecord = (TPRecord) RFIDMasterDao.get(conn, TPRecord.class, tpRecord.getTprId());
                supplier = "MPL";
//                Pair<Integer, String> pairVal = TPRUtils.getSupplierFromDo(conn, tpRecord.getDoId());
//                supplier = pairVal.first != Misc.getUndefInt() ? pairVal.second : "";
                if (tpRecord.getEarliestUnloadGateInEntry() != null) {
                    gateInTime = UIConstant.displayFormat.format(tpRecord.getEarliestUnloadGateInEntry());
                }
                tprId = Misc.getPrintableInt(tpRecord.getTprId());
                dispatchPlace = tpRecord.getConsigneeAddress();

                if (tpRecord.getEarliestLoadWbInEntry() != null) {//setLatestLoadWbOutExit
                    grossTime = UIConstant.displayFormat.format(tpRecord.getEarliestLoadWbInEntry());
                }
//                if (tpRecord.getLatestUnloadWbOutExit() != null) { //
//                    tareTime = UIConstant.displayFormat.format(tpRecord.getLatestUnloadWbOutExit());
//                }
                landingArea = tpRecord.getConsignorAddress();

                coalTransporter = DropDownValues.getTransporter(tpRecord.getTransporterId(), conn);
                carryingTranporter = DropDownValues.getTransporter(tpRecord.getCarryingTransporterId(), conn);

                vehicleNo = tpRecord.getVehicleName();
//      vehicleType = getVehicleType(conn, tpRecord.getVehicleId());
                challanNo = tpRecord.getChallanNo();
                lrNo = tpRecord.getLrNo();

                if (tpRecord.getLrDate() != null) {
                    lrDate = UIConstant.slipFormat.format(tpRecord.getLrDate());
                }
                if (tpRecord.getComboStart() != null) {
                    challanDate = UIConstant.slipFormat.format(tpRecord.getComboStart());
                    expectedDate = UIConstant.slipFormat.format(GateInDao.getExpectedDate(tpRecord.getComboStart()));
                }
                
                
                
//                product = "";
//                product = com.ipssi.rfid.constant.Type.TPRMATERIAL.getStr(TokenManager.materialCat);//TokenManager.materialCat == 1 ? "STONE" : TokenManager.materialCat == 2 ? "FLYASH" : TokenManager.materialCat == 3 ? "OTHERS" : "COAL";
//                grade = DropDownValues.getGrade(tpRecord.getMaterialGradeId(), conn);
//                product = product + " " + grade;

                partyName = tpRecord.getConsigneeName();

                consignee = Misc.getPrintableString(tpRecord.getConsigneeName()) + ", " + Misc.getPrintableString(tpRecord.getConsigneeAddress());

                tareWts = Misc.getPrintableDouble(tpRecord.getLoadTare());
                grossWts = Misc.getPrintableDouble(tpRecord.getLoadGross());
                netWts =  Misc.getPrintableDouble(GateInDao.calculateNetWt(tpRecord.getLoadGross(), tpRecord.getLoadTare()));
                
//                gateEntryUser = DropDownValues.getUser(conn, tpRecord.getTprId(), com.ipssi.rfid.constant.Type.WorkStationType.GATE_IN_TYPE);
//                firstWbUse = DropDownValues.getUser(conn, tpRecord.getTprId(), com.ipssi.rfid.constant.Type.WorkStationType.WEIGH_BRIDGE_IN_TYPE);
                preparedBy = TokenManager.userName;
                checkedBy = "";

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
    }
//    private String getVehicleType(Connection conn, int vehicleId) {
//        PreparedStatement ps = null;
//        String vehicleType = "";
//        ResultSet rs = null;
//        int parameterIndex = 1;
//        java.sql.Date now = new java.sql.Date((new java.util.Date()).getTime());
//        String query = "Select vehicle_type.type FROM vehicle_type JOIN vehicle ON (vehicle_type.id = vehicle.id) where vehicle.id = ?";
//        try {
//            ps = conn.prepareStatement(query);
//            ps.setInt(parameterIndex++, vehicleId);
//            rs = ps.executeQuery();
//            while (rs.next()) {
//                vehicleType = rs.getString(1);
//            }
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        } finally {
//            try {
//                if (ps != null) {
//                    ps.close();
//                }
//                if (rs != null) {
//                    rs.close();
//                }
//            } catch (SQLException ex) {
//                Logger.getLogger(FlyashWeighmentTare.class.getName()).log(Level.SEVERE, null, ex);
//            }
//        }
//        return vehicleType;
//
//    }
}
