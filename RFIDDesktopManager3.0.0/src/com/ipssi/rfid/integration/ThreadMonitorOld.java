package com.ipssi.rfid.integration;

import com.ipssi.rfid.ui.DisconnectionDialog;

 /**
  * Monitors a thread, interrupting it if it reaches the specified timeout.
  * <p>
  * This works by sleeping until the specified timeout amount and then
  * interrupting the thread being monitored. If the thread being monitored
  * completes its work before being interrupted, it should <code>interrupt()</code>
  * the <i>monitor</i> thread.
  * </p>
  * 
  * <pre>
  *       long timeoutInMillis = 1000;
  *       try {
  *           Thread monitor = ThreadMonitor.start(timeoutInMillis);
  *           // do some work here
  *           ThreadMonitor.stop(monitor);
  *       } catch (InterruptedException e) {
  *           // timed amount was reached
  *       }
  * </pre>
  */
 
 class ThreadMonitorOld implements Runnable {
     private final Thread thread;
     private final long timeout;
     private final InterruptListener listener;
 
     public static Thread start(final long timeout,final InterruptListener _listener) throws Exception {
    	 System.out.println("[ThreadMonitor]:"+timeout);
         return start(Thread.currentThread(), timeout, _listener);	 	
     }
 
     public static Thread start(final Thread thread, final long timeout,final InterruptListener _listener) throws Exception{
         Thread monitor = null;
         if (timeout > 0) {
             final ThreadMonitorOld timout = new ThreadMonitorOld(thread, timeout,_listener);
             monitor = new Thread(timout, ThreadMonitorOld.class.getSimpleName());
             monitor.setDaemon(true);
             monitor.start();
         }
         return monitor;	 	
     }

     public static void stop(final Thread thread) {
         if (thread != null) {
             thread.interrupt();
         }	
     }

     private ThreadMonitorOld(final Thread thread, final long timeout,final InterruptListener _listener) {
         this.thread = thread;
         this.timeout = timeout;
         this.listener = _listener;
     }
 	 	
     public void run() {
         try {
			Thread.sleep(timeout);
			thread.interrupt();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
     }
     static DisconnectionDialog disconnectionDialog = null;
     public static void main(String[] arg){
    	 long timeoutInMillis = 1000;
    	 Thread monitor = null; 
    	 disconnectionDialog = new DisconnectionDialog("Weigh Bridge disConnected");
    	 while(true){
    		 try {
    			 ThreadMonitorOld.stop(monitor);
    			 monitor = ThreadMonitorOld.start(timeoutInMillis,null);
    			 long sleepTime = Math.round(Math.random()*2000);
    			 Thread.sleep(sleepTime);
    			 if(disconnectionDialog != null)
    				 disconnectionDialog.setVisible(false);
    		 } catch (Exception e) {
    			 java.awt.EventQueue.invokeLater(new Runnable() {
    				 public void run() {
    					 disconnectionDialog.setVisible(true);
    				 }
    			 });
    		 }
    	 }
    	 
     }
 }
 
 
 
 