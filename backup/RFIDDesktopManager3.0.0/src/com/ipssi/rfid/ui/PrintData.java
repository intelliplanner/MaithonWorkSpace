/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ipssi.rfid.ui;

import com.ipssi.gen.utils.DBConnectionPool;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.Pair;
import com.ipssi.gen.utils.DimConfigInfo.ExprHelper.CalcFunctionEnum;
import com.ipssi.rfid.beans.TPRecord;
import com.ipssi.rfid.constant.UIConstant;
import com.ipssi.rfid.database.DropDownValues;
import com.ipssi.rfid.database.GateInDao;
import com.ipssi.rfid.processor.TPRUtils;
import com.ipssi.rfid.processor.TokenManager;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.print.PrinterException;
import java.sql.Connection;
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
public class PrintData extends javax.swing.JDialog {
//    static int defaultLength = 25;

    TPRecord tpRecord = null;
    private String supplier = "";
    private String delayHour = "";
    private String gateEntryUser = "";
    private String firstWbUse = "";
    private String secondWbUse = "";
    private String checkedBy = "";
    private String mines = "";
    private String tranporter = "";
    private String vehicleNo = "";
    private String lrDate = "";
    private String challanDate = "";
    private String roadPermit = "";
    private String product = "";
    private String netWt = "";
    private String acceptWt = "";
    private String gateInTime = "";
    private String grossTime = "";
    private String tareTime = "";
    private String mineralPermit = "";
    private String lrNo = "";
    private String challanNo = "";
    private String supplierTare = "";
    private String supplierGross = "";
    private String receiveTare = "";
    private String supplierNet = "";
    private String tprId = "";
    private String shortWt = "";
    private String receiveGross = "";
    private String supplierNetWt = "";
//    private String netWts = "";
//    private String shortWts = "";
    
    private String grade =  "";
    
    /**
     * Creates new form PrintData
     */
//     public PrintData(java.awt.Frame parent, boolean modal, TPRecord tpRecord, String supplierNetWt , String shortWts, String acceptedWt) {
    public PrintData(java.awt.Frame parent, boolean modal, TPRecord tpRecord, String supplierNetWt , String shortWts) {
        super(parent, modal);
        initComponents();
        textPane.setBackground(Color.WHITE);
        this.tpRecord = tpRecord;
//        this.shortWts = shortWts;
        this.supplierNetWt = supplierNetWt;
//        this.acceptedWt = acceptedWt;
//        initializeVariables();
        center();
        initializeVariables();
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                printData();
            }
        });
        //print();
        
    }

    protected void center() {
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
        Style small = doc.addStyle("small", regular);
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
            doc.setLogicalStyle(8, regular);
            doc.insertString(0, "                     MAITHON POWER LIMITED\n", large);
            doc.insertString(doc.getLength(), "       VILL: DAMBHUI, PO: BARBINDIA, THANA: NIRSA DHANBAD-828205 DHANBAD\n", medium);
            doc.insertString(doc.getLength(), "                      WEIGHMENT SLIP - Inward Goods\n", small);
            doc.insertString(doc.getLength(), "---------------------------------"
                    + "----------------------------------------------------\n", regular);
            doc.insertString(doc.getLength(), getLabelString("Supplier: ", 22), label);
            doc.insertString(doc.getLength(), getString(supplier, 18), text);
            doc.insertString(doc.getLength(), getLabelString("GateIn Time: ", 18), label);
            doc.insertString(doc.getLength(), gateInTime + "\n", text);

            doc.insertString(doc.getLength(), getLabelString("Mines: ", 22), label);
            doc.insertString(doc.getLength(), getString(mines, 18), text);
            doc.insertString(doc.getLength(), getLabelString("Gross Wt Time: ", 18), label);
            doc.insertString(doc.getLength(), grossTime + "\n", text);

            doc.insertString(doc.getLength(), getLabelString("TPR Id: ", 22), label);
            doc.insertString(doc.getLength(), getString(tprId, 18), text);
            doc.insertString(doc.getLength(), getLabelString("Tare Wt Time: ", 18), label);
            doc.insertString(doc.getLength(), tareTime + "\n", text);


//            doc.insertString(doc.getLength(), getLabelString("Gate Entry Date: ",60), label);
//            doc.insertString(doc.getLength(), entryDate + "\n", text);

            doc.insertString(doc.getLength(), "-------------------------"
                    + "------------------------------------------------------------\n", regular);
            doc.insertString(doc.getLength(), getLabelString("Transporter: ", 22), label);
            doc.insertString(doc.getLength(), getString(tranporter, 18), text);
            doc.insertString(doc.getLength(), getLabelString("Vehicle#: ", 18), label);
            doc.insertString(doc.getLength(), vehicleNo + "\n", text);


            doc.insertString(doc.getLength(), getLabelString("Challan#: ", 22), label);
            doc.insertString(doc.getLength(), getString(challanNo, 18), text);
            doc.insertString(doc.getLength(), getLabelString("Challan Date: ", 18), label);
            doc.insertString(doc.getLength(), challanDate + "\n", text);


            doc.insertString(doc.getLength(), getLabelString("LR#: ", 22), label);
            doc.insertString(doc.getLength(), getString(lrNo, 18), text);
            doc.insertString(doc.getLength(), getLabelString("LR Date: ", 18), label);
            doc.insertString(doc.getLength(), lrDate + "\n", text);

            doc.insertString(doc.getLength(), getLabelString("Road Permit#: ", 22), label);
            doc.insertString(doc.getLength(), getString(roadPermit, 18), text);
            doc.insertString(doc.getLength(), getLabelString("Mineral Permit#: ", 18), label);
            doc.insertString(doc.getLength(), mineralPermit + "\n", text);

          
//            doc.insertString(doc.getLength(), getLabelString("Challan Wt: ",60), label);
//            doc.insertString(doc.getLength(), challanWt + "\n", text);


            doc.insertString(doc.getLength(), "-------------------------------------"
                    + "------------------------------------------------\n", regular);

            doc.insertString(doc.getLength(), getLabelString("Product: ", 22), label);
            doc.insertString(doc.getLength(), getString(product, 18) + "\n", text);

            doc.insertString(doc.getLength(), getLabelString("Supplier Tare(MT): ", 22), label);
            doc.insertString(doc.getLength(), getString(supplierTare, 14), text);
            doc.insertString(doc.getLength(), getLabelString("Received Gross(MT): ", 22), label);
            doc.insertString(doc.getLength(), receiveGross + "\n", text);

            doc.insertString(doc.getLength(), getLabelString("Supplier Gross(MT): ", 22), label);
            doc.insertString(doc.getLength(), getString(supplierGross, 14), text);
            doc.insertString(doc.getLength(), getLabelString("Received Tare(MT): ", 22), label);
            doc.insertString(doc.getLength(), receiveTare + "\n", text);

//            doc.insertString(doc.getLength(), getLabelString("------------------\n",60), regular);
            doc.insertString(doc.getLength(), getLabelString("Supplier Net(MT): ", 22), label);
            doc.insertString(doc.getLength(), getString(supplierNet, 18), text);
            doc.insertString(doc.getLength(), getLabelString("Net Wt(MT): ", 18), label);
            doc.insertString(doc.getLength(), netWt + "\n", text);

            doc.insertString(doc.getLength(), getLabelString("Short Wt(MT): ", 22), label);
            doc.insertString(doc.getLength(), getString(shortWt, 18), text);
            doc.insertString(doc.getLength(), getLabelString("Accepted Wt(MT): ", 18), label);
            doc.insertString(doc.getLength(), acceptWt + "\n", text);

            doc.insertString(doc.getLength(), "--------------------------------------"
                    + "------------------------------------------------\n", regular);

            doc.insertString(doc.getLength(), getString(gateEntryUser,19), text);
            doc.insertString(doc.getLength(), getString(firstWbUse, 21), text);
            doc.insertString(doc.getLength(), getString(secondWbUse, 18), text);
            doc.insertString(doc.getLength(), getString(checkedBy, 16) + "\n", text);

            doc.insertString(doc.getLength(), "Gate In User", label);
            doc.insertString(doc.getLength(), getLabelString("Gross WB User", 20), label);
            doc.insertString(doc.getLength(), getLabelString("Tare WB User", 20), label);
            doc.insertString(doc.getLength(), getLabelString("Checked By", 16) + "\n", label);

//            doc.insertString(doc.getLength(), "Total Delay Hours: ", text);
//            doc.insertString(doc.getLength(), getString(delayHour, 25), text);
            print();
        } catch (BadLocationException e) {
            e.printStackTrace(); //To change body of catch statement use File | Settings | File Templates.
        }finally{
        	this.dispose();
        }
    }

    private String getString(String str1, int defaultLength) {
        int strLen = str1.length();
        int diffLength = defaultLength - strLen;
        String str2 = " ";
        String str3 = "";
        for (int i = 0, is = str1 == null ? 0 : diffLength; i < is; i++) {
            str3 += str2;
        }
        str1 += str3;
        System.out.print("New Length " + str1.length());
        return str1;
    }

    private String getLabelString(String str1, int defaultLength) {
        int strLen = str1.length();
        int diffLength = defaultLength - strLen;
        String str2 = " ";
        String str3 = "";
        for (int i = 0, is = str1 == null ? 0 : diffLength; i < is; i++) {
            str3 += str2;
        }
        str3 = str3 + str1;
        System.out.print("New Length " + str3.length());
        return str3;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panel1 = new java.awt.Panel();
        jScrollPane1 = new javax.swing.JScrollPane();
        textPane = new javax.swing.JTextPane();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        textPane.setEditable(false);
        jScrollPane1.setViewportView(textPane);

        javax.swing.GroupLayout panel1Layout = new javax.swing.GroupLayout(panel1);
        panel1.setLayout(panel1Layout);
        panel1Layout.setHorizontalGroup(
            panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 625, Short.MAX_VALUE)
                .addContainerGap())
        );
        panel1Layout.setVerticalGroup(
            panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 433, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(88, 88, 88))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(panel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panel1, javax.swing.GroupLayout.PREFERRED_SIZE, 450, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

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
            java.util.logging.Logger.getLogger(PrintData.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(PrintData.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(PrintData.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(PrintData.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                PrintData dialog = new PrintData(new javax.swing.JFrame(), true, null,"","");
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

    void print() {
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
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane1;
    private java.awt.Panel panel1;
    private javax.swing.JTextPane textPane;
    // End of variables declaration//GEN-END:variables

 private void initializeVariables() {
        if (tpRecord != null) {
            Connection conn = null;
            boolean destroyIt = false;
            try {
                conn = DBConnectionPool.getConnectionFromPoolNonWeb();
                
                Pair<Integer, String> pairVal = TPRUtils.getSupplierFromDo(conn, tpRecord.getDoId());
                supplier = pairVal.first != Misc.getUndefInt() ? pairVal.second : "";
                
                if(tpRecord.getEarliestUnloadGateInEntry() != null){
                gateInTime =  UIConstant.displayFormat.format(tpRecord.getEarliestUnloadGateInEntry());
                }
                mines =  DropDownValues.getMines(tpRecord.getMinesId(), conn);;
                tprId = Misc.getPrintableInt(tpRecord.getTprId());
                if(tpRecord.getLatestUnloadWbInExit() != null){
                grossTime = UIConstant.displayFormat.format(tpRecord.getLatestUnloadWbInExit());
                }
                if(tpRecord.getLatestUnloadWbOutExit() != null){
                tareTime = UIConstant.displayFormat.format(tpRecord.getLatestUnloadWbOutExit());
                }
                tranporter =  DropDownValues.getTransporter(tpRecord.getTransporterId(), conn);
                vehicleNo = tpRecord.getVehicleName();
                challanNo = tpRecord.getChallanNo();
                lrNo = tpRecord.getLrNo();
                roadPermit = tpRecord.getDispatchPermitNo();
                mineralPermit = tpRecord.getMaterialDescription();
                if(tpRecord.getChallanDate() != null){
                challanDate = UIConstant.slipFormat.format(tpRecord.getChallanDate());
                }
                if(tpRecord.getLrDate() != null){
                lrDate =  UIConstant.slipFormat.format(tpRecord.getLrDate());
                }
//                product = "";
                product  = TokenManager.materialCat == 1 ? "STONE" : TokenManager.materialCat == 2 ? "FLYASH" :   TokenManager.materialCat == 3 ? "OTHERS": "COAL";
                grade =DropDownValues.getGrade(tpRecord.getMaterialGradeId(), conn);
                product = product + " " + grade;
                supplierTare = Misc.getPrintableDouble(tpRecord.getLoadTare());
                supplierGross = Misc.getPrintableDouble(tpRecord.getLoadGross());
                
                supplierNet = supplierNetWt;
//                shortWt =  shortWts;
                
                receiveGross = Misc.getPrintableDouble(tpRecord.getUnloadGross());
                receiveTare = Misc.getPrintableDouble(tpRecord.getUnloadTare());
                double calNetWt =GateInDao.calculateNetWt(tpRecord.getUnloadGross(), tpRecord.getUnloadTare()); 
                netWt =  Misc.getPrintableDouble(calNetWt);
                
                double calTotalShort =GateInDao.calculateTotalShort(Misc.getParamAsDouble(supplierNetWt), calNetWt);
                shortWt = Misc.getPrintableDouble(calTotalShort);
                
                double acceptedWt =  GateInDao.calculateAcceptedNetWt(Misc.getParamAsDouble(supplierNetWt), calNetWt);
                acceptWt = Misc.getPrintableDouble(acceptedWt);
                        
                gateEntryUser = DropDownValues.getUser(conn, tpRecord.getTprId(), com.ipssi.rfid.constant.Type.WorkStationType.GATE_IN_TYPE);
                firstWbUse =  DropDownValues.getUser(conn, tpRecord.getTprId(), com.ipssi.rfid.constant.Type.WorkStationType.WEIGH_BRIDGE_IN_TYPE);;
                secondWbUse =  TokenManager.userName;
                checkedBy =  "";
                
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
//        else{
//         supplierNet = supplierNetWt;
//                shortWt =  shortWts;
//        }
    }
    
    
  
}
