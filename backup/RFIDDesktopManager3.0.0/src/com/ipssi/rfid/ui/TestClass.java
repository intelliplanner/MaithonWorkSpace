/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ipssi.rfid.ui;

import com.ipssi.gen.utils.Misc;
import com.ipssi.rfid.database.GateInDao;
import com.ipssi.rfid.processor.Utils;

/**
 *
 * @author Vi$ky
 */
public class TestClass {
    static String doub = "";
    
    private int getIntFromDouble(String val){
         int intVal = Misc.getUndefInt();
        
         if(!Utils.isNull(val)){
             Double d = new Double(val);
             intVal = d.intValue();
         }
        return intVal;
     }
   
    
    public static void main(String s[]){
        TestClass t =new TestClass();
        int val  = t.getIntFromDouble(doub); 
        System.out.println("value :" +val);
        double gross = 24.85;
        double tare = 10.15;
        double d = GateInDao.calculateNetWt(gross, tare);
        System.out.println(Misc.getPrintableDouble(d));
        
    }
    
}
