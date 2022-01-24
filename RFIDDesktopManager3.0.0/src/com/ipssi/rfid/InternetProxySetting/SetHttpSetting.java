/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ipssi.rfid.InternetProxySetting;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.swing.JOptionPane;

/**
 *
 * @author Vi$ky
 */
public class SetHttpSetting {
     private File HttpconfigFile = new File("HttpconfigFile.properties");
    private Properties configProps;
    private Properties httpconfigProps;
    public  void saveHttpProperties(String Host,String Port,String userName,String password) throws FileNotFoundException,IOException {
        configProps = new Properties();
        FileOutputStream cfos = new FileOutputStream(HttpconfigFile);
        configProps.setProperty("proxySet", "true");
        configProps.setProperty("java.net.useSystemProxies", "true");
        configProps.setProperty("socksProxyHost",Host);
        configProps.setProperty("socksProxyPort", Port);
        configProps.setProperty("java.net.socks.username", userName);
        configProps.setProperty("java.net.socks.password", password);
        configProps.store(cfos, "Http Properties file generated from Java program");
        cfos.close();
    }
     public Properties loadHttpProperlies() throws FileNotFoundException,IOException {
            httpconfigProps = new Properties();   
        // loads properties from file
        if (HttpconfigFile.exists()) {
            InputStream inputStream = new FileInputStream(HttpconfigFile);
            httpconfigProps.load(inputStream);
            inputStream.close();
        }
        return httpconfigProps;
    }
     
     public  void setServerProxy(){
        if (HttpconfigFile.exists()) {
                InputStream inputStream = null;
                try {
                    httpconfigProps = new Properties();
                    inputStream = new FileInputStream(HttpconfigFile);
                    httpconfigProps.load(inputStream);
                    String useSystemProxies = httpconfigProps.getProperty("java.net.useSystemProxies");
                    String proxySet = httpconfigProps.getProperty("proxySet");
                    String proxy_host = httpconfigProps.getProperty("socksProxyHost");
                    String proxy_port = httpconfigProps.getProperty("socksProxyPort");
                    String proxy_username = httpconfigProps.getProperty("java.net.socks.username");
                    String proxy_password = httpconfigProps.getProperty("java.net.socks.password");
                    Properties systemProperties = System.getProperties();
                    
                    systemProperties.setProperty("proxySet", proxySet);
                    systemProperties.setProperty("java.net.useSystemProxies", useSystemProxies);
                    systemProperties.setProperty("http.proxyHost", proxy_host);
                    systemProperties.setProperty("http.proxyPort", proxy_port);
                    systemProperties.setProperty("http.proxyUser", proxy_username);
                    systemProperties.setProperty("http.proxyPassword", proxy_password);
                } catch (FileNotFoundException ex) {
                    ex.printStackTrace();
                } catch (IOException ex) {
                	ex.printStackTrace();
                } finally {
                    try {
                        inputStream.close();
                    } catch (IOException ex) {
                         ex.printStackTrace();
                    }
                }
            } else {
                JOptionPane.showMessageDialog(null,
                        "Property not Saved !!!");
            }
     }
    
}
