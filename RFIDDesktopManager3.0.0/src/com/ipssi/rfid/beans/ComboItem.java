/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ipssi.rfid.beans;

/**
 *
 * @author Vi$ky
 */
public class ComboItem {
int value;
    String label;
  
       public ComboItem(int value, String label) {
        this.value = value;
        this.label = label;
    }

//    public ComboItem(Object first, Object second) {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//    }
    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String Label) {
        this.label = Label;
    }
    
    @Override
    public String toString() {
        return label;
    }
}
