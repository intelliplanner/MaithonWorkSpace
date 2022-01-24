/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ipssi.rfid.integration;

import com.ipssi.rfid.ui.CoalGateInWindow;
import static com.ipssi.rfid.ui.CoalGateInWindow.digitalClock;
import com.ipssi.rfid.ui.CoalGateOutWindow;
import com.ipssi.rfid.ui.CoalWeighBridgeInWindow;
import com.ipssi.rfid.ui.CoalWeighBridgeOutWindow;
import com.ipssi.rfid.ui.CoalYardInWindow;
import com.ipssi.rfid.ui.CoalYardOutWindow;
import com.ipssi.rfid.ui.FlyashWeighmentGross;
import com.ipssi.rfid.ui.FlyashWeighmentTare;
import com.ipssi.rfid.ui.GateInFlyAshWindow;
import com.ipssi.rfid.ui.StoneWeighmentGross;
import com.ipssi.rfid.ui.StoneWeighmentTare;
import com.ipssi.rfid.ui.Weighment1st;
import com.ipssi.rfid.ui.Weighment2nd;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author Vi$ky
 */
public class Clock {
    private static int currentSecond;
    private static Calendar calendar;
    
    
     private final static SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
     public static void startClock(final String workStationType) {
        
        resetClock();
        System.out.print("###startClock()  Worker Scheduler  Start ###");
        ScheduledExecutorService worker  = Executors.newScheduledThreadPool(1);
       
        worker.scheduleAtFixedRate(new Runnable() {
            public void run() {
                if (currentSecond == 60) {
                    resetClock();
                }
                if(workStationType.equalsIgnoreCase("GateIn")){
                    resetClock();
                    CoalGateInWindow.digitalClock.setText(String.format("%s:%02d", sdf.format(calendar.getTime()), currentSecond));
                }
                else if(workStationType.equalsIgnoreCase("GateOut")){
                    resetClock();
                    CoalGateOutWindow.digitalClock.setText(String.format("%s:%02d", sdf.format(calendar.getTime()), currentSecond));
                }
                else if(workStationType.equalsIgnoreCase("WbIn")){
                    resetClock();
                    CoalWeighBridgeInWindow.digitalClock.setText(String.format("%s:%02d", sdf.format(calendar.getTime()), currentSecond));
                }
                else  if(workStationType.equalsIgnoreCase("WbOut")){
                    resetClock();
                    CoalWeighBridgeOutWindow.digitalClock.setText(String.format("%s:%02d", sdf.format(calendar.getTime()), currentSecond));
                }
                else if(workStationType.equalsIgnoreCase("YardIn")){
                     resetClock();
                    CoalYardInWindow.digitalClock.setText(String.format("%s:%02d", sdf.format(calendar.getTime()), currentSecond));
                }
                else if(workStationType.equalsIgnoreCase("YardOut")){
                    resetClock();
                    CoalYardOutWindow.digitalClock.setText(String.format("%s:%02d", sdf.format(calendar.getTime()), currentSecond));
                }
                else if(workStationType.equalsIgnoreCase("FlyAshIn")){
                    resetClock();
                    GateInFlyAshWindow.digitalClock.setText(String.format("%s:%02d", sdf.format(calendar.getTime()), currentSecond));
                }
                else if(workStationType.equalsIgnoreCase("FlyAshWeighmentGross")){
                    resetClock();
                    FlyashWeighmentGross.digitalClock.setText(String.format("%s:%02d", sdf.format(calendar.getTime()), currentSecond));
                }
                else if(workStationType.equalsIgnoreCase("FlyAshWeighmentTare")){
                    resetClock();
                    FlyashWeighmentTare.digitalClock.setText(String.format("%s:%02d", sdf.format(calendar.getTime()), currentSecond));
                }
                else if(workStationType.equalsIgnoreCase("StoneWeighmentGross")){
                    resetClock();
                    StoneWeighmentGross.digitalClock.setText(String.format("%s:%02d", sdf.format(calendar.getTime()), currentSecond));
                }
                else if(workStationType.equalsIgnoreCase("StoneWeighmentTare")){
                    resetClock();
                    StoneWeighmentTare.digitalClock.setText(String.format("%s:%02d", sdf.format(calendar.getTime()), currentSecond));
                }
                else if(workStationType.equalsIgnoreCase("FirstWeighment")){
                    resetClock();
                    Weighment1st.digitalClock.setText(String.format("%s:%02d", sdf.format(calendar.getTime()), currentSecond));
                }
                else if(workStationType.equalsIgnoreCase("SecondWeighment")){
                    resetClock();
                    Weighment2nd.digitalClock.setText(String.format("%s:%02d", sdf.format(calendar.getTime()), currentSecond));
                }
                
                currentSecond++;
            }
        }, 0, 1000, TimeUnit.MILLISECONDS);
    }
    public static void resetClock() {
       
        calendar = Calendar.getInstance();
        currentSecond = calendar.get(Calendar.SECOND);
    }
}
