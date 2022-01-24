package com.ipssi.timerTask;

import java.util.Timer;
import java.util.TimerTask;

public class TimerTest{
	Timer timer = null;
	 public void scheduleClearScreenTimer(long time){
		 try{
		 timer = new Timer();
			 timer.schedule( new TimerTask() {
		            public void run() {
		                System.out.println(" Event run");
		                timer.cancel();
 		            }
		         }, time);
		 }catch (Exception e) {
			e.printStackTrace();
		}
	 }
	 public void stopClearScreenTimer(){
		 try {
			 if(timer != null)
				 timer.cancel();	
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	 }
	
	  public static void main(String[] args) {
		  TimerTest o =new TimerTest();
		  o.scheduleClearScreenTimer(3 *1000);
		  //o.stopClearScreenTimer();
	  }
	
}
