package com.ipssi.rfid.integration;

import java.io.File;
import java.io.OutputStream;

import javax.imageio.ImageIO;

import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream;

import java.awt.image.BufferedImage;

import uk.co.mmscomputing.device.scanner.Scanner;
import uk.co.mmscomputing.device.scanner.ScannerDevice;
import uk.co.mmscomputing.device.scanner.ScannerListener;
import uk.co.mmscomputing.device.scanner.ScannerIOException;
import uk.co.mmscomputing.device.scanner.ScannerIOMetadata;

public class DocScanner implements ScannerListener{

  static DocScanner app;  

  Scanner scanner;

  public DocScanner(String[] argv)throws ScannerIOException{
    scanner=Scanner.getDevice();
    scanner.addListener(this);    
    scanner.acquire();
  }

  public void update(ScannerIOMetadata.Type type, ScannerIOMetadata metadata){
    if(type.equals(ScannerIOMetadata.ACQUIRED)){
      BufferedImage image=metadata.getImage();
      System.out.println("Have an image now!");
      try{
    	  ByteOutputStream out = new ByteOutputStream();
    	  ImageIO.write(image, "png", out);
    	  byte[] data = out.getBytes();
      }catch(Exception e){
        e.printStackTrace();
      }
    }else if(type.equals(ScannerIOMetadata.NEGOTIATE)){
      ScannerDevice device=metadata.getDevice();
      try{
//        device.setShowUserInterface(true);
//        device.setShowProgressBar(true);
//        device.setResolution(100);
      }catch(Exception e){
        e.printStackTrace();
      }
    }else if(type.equals(ScannerIOMetadata.STATECHANGE)){
      System.err.println(metadata.getStateStr());
      if(metadata.isFinished()){
        System.exit(0);
      }
    }else if(type.equals(ScannerIOMetadata.EXCEPTION)){
      metadata.getException().printStackTrace();
    }
  }

  public static void main(String[] argv){
    try{
      app=new DocScanner(argv);
    }catch(Exception e){
      e.printStackTrace();
    }
  }
}

