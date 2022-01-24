/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ipssi.rfid.ui;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.ipssi.gen.utils.Misc;
import com.ipssi.rfid.database.GateInDao;
import com.ipssi.rfid.processor.TokenManager;
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
   
    
    private String removeSpecialChar(String str){
    	if(str == null || str.length() == 0)
    		return str;
    		
    	str = str.replaceAll("/", "").replaceAll("'", "");
    	
    	return str;
    } 

    
    
    public static void main(String s[]){
        TestClass t =new TestClass();
//        int val  = t.getIntFromDouble(doub); 
//        System.out.println("value :" +val);
//        double gross = 24.85;
//        double tare = 10.15;
//        double d = GateInDao.calculateNetWt(gross, tare);
//        System.out.println(Misc.getPrintableDouble(d));
//        String str = "Hello'test/123/wq";
//        str = t.removeSpecialChar(str);
        
//        String str1 = "TM %                      (ARB)";
//        str1 = str1.replaceAll("\\s","");
//        System.out.println(str1.equalsIgnoreCase("TM%(ARB)"));
//        
        try { 
        String gpsViolations = GateInDao.sendDataOnServer(TokenManager.URL,TokenManager.ACTION,"TEST");
        JSONArray jsonArr = new JSONArray(gpsViolations);
        if (jsonArr.length() > 0) {
        	for (int i = 0; i < jsonArr.length(); i++) {
        		JSONObject jobj = jsonArr.getJSONObject(i);
        		System.out.println("GPS_VIOLATIONS:"+jobj.get("GPS_VIOLATIONS"));
        		System.out.println("GPS_TIME:"+jobj.get("GPS_TIME"));
        		System.out.println("GPS_LOCATION:"+jobj.get("GPS_LOCATION"));
			}
				
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//        String c = jobj.getString(gpsViolations); 
        
        
        //String str[] = str1.split(" ");
        
        //System.out.println(str[0].equalsIgnoreCase("TM"));

   }
    
}
