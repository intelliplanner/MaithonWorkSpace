/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ipssi.rfid.connection;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.ipssi.gen.utils.Misc;
import com.ipssi.rfid.constant.Type;

/**
 *
 * @author Vi$ky
 */
public class ConfigUtility {

    String path = "C:" + File.separator + "ipssi" + File.separator + "properties" + File.separator + "new_conn.property";
    String screenListPath = "C:" + File.separator + "ipssi" + File.separator + "properties" + File.separator + "screen_list.property";
    String rfidConfigPath = "C:" + File.separator + "ipssi" + File.separator + "properties" + File.separator + "RFIDConfig.property";
    String barrierConfigPath = "C:" + File.separator + "ipssi" + File.separator + "properties" + File.separator + "barrier.property";
    String weighBridgeConfigPath = "C:" + File.separator + "ipssi" + File.separator + "properties" + File.separator + "weighBridge.property";
    private static String WorkStationpath = "C:" + File.separator + "ipssi" + File.separator + "properties" + File.separator;
    private File configFile = new File(path);
    private File logFile = new File(path);
    private File screenListFile = new File(screenListPath);
    private File rfidConfigFile = new File(rfidConfigPath);
    private File barrierConfigFile = new File(barrierConfigPath);
    private File weighBridgeConfigFile = new File(weighBridgeConfigPath);
    private static File workStationConfigFile = new File(WorkStationpath);
    //private Properties configProps;

    public void loadProperlies() throws FileNotFoundException, IOException {
        Properties defaultProps = new Properties();
        // sets default properties
        defaultProps.setProperty("desktop.DBConn.userName", "root");
        defaultProps.setProperty("desktop.DBConn.password", "root");
        defaultProps.setProperty("desktop.DBConn.host", "localhost");
        defaultProps.setProperty("desktop.DBConn.port", "3306");
        defaultProps.setProperty("desktop.DBConn.Database", "ipssi");
        defaultProps.setProperty("desktop.DBConn.maxConnection", "4");

        //        defaultProps.setProperty("desktop.DBConn.userName", "service");
        //        defaultProps.setProperty("desktop.DBConn.password", "service123!");
        //        defaultProps.setProperty("desktop.DBConn.host", "203.197.197.17");
        //        defaultProps.setProperty("desktop.DBConn.port", "3306");
        //        defaultProps.setProperty("desktop.DBConn.Database", "ipssi_stag");
        //configProps = new Properties(defaultProps);
        // loads properties from file
        if (!configFile.exists()) {
            File f = new File(path);
            f.getParentFile().mkdirs();
            f.createNewFile();
            FileOutputStream cfos = new FileOutputStream(path);
            defaultProps.store(cfos, "Desktop Application");
            cfos.close();
        }

    }

    public boolean setScreenList(Properties prop) throws IOException {
        boolean Success = false;
        if (!screenListFile.exists()) {
            File f = new File(screenListPath);
            f.getParentFile().mkdirs();
            f.createNewFile();
        }
        FileOutputStream cfos = new FileOutputStream(screenListPath);
        prop.store(cfos, "Selected Screen List For this System");
        Success = true;
        cfos.close();
        return Success;
    }

    public Properties loadScreenList() throws FileNotFoundException, IOException {
        Properties screenList = null;
        if (screenListFile.exists()) {
            screenList = new Properties();
            InputStream inputStream = new FileInputStream(screenListPath);
            screenList.load(inputStream);
            inputStream.close();
        }
        return screenList;
    }

    public Properties getWeighBridgeConfiguration() {
        Properties config = null;
        if (barrierConfigFile.exists()) {
            InputStream inputStream = null;
            try {
                config = new Properties();
                inputStream = new FileInputStream(weighBridgeConfigPath);
                config.load(inputStream);
                inputStream.close();
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
        }
        return config;
    }

    public Properties getBarrierConfiguration() {
        Properties config = null;
        if (barrierConfigFile.exists()) {
            InputStream inputStream = null;
            try {
                config = new Properties();
                inputStream = new FileInputStream(barrierConfigPath);
                config.load(inputStream);
                inputStream.close();
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
        }
        return config;
    }

    public Properties getReaderConfiguration() {
        Properties config = null;
        if (rfidConfigFile.exists()) {
            InputStream inputStream = null;
            try {
                config = new Properties();
                inputStream = new FileInputStream(rfidConfigPath);
                config.load(inputStream);
                inputStream.close();
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
        }
        return config;
    }

    public static void loadWorkStationProperties(String suffix, int type) {
        try {
            Properties defaultProps = new Properties();
            // sets default properties
            int prev = Misc.getUndefInt();
            int next = Misc.getUndefInt();
            if (type > Type.WorkStationType.GATE_IN_TYPE) {
                prev = type - 1;
            }
            if (type < Type.WorkStationType.GATE_OUT_TYPE) {
                next = type + 1;
            }
            defaultProps.setProperty("WORK_STATION_TYPE", type + "");
            defaultProps.setProperty("WORK_STATION_ID", "1");
            defaultProps.setProperty("NEXT_WORK_STATION_TYPE", Misc.isUndef(next) ? "" : next + "");
            defaultProps.setProperty("PREV_WORK_STATION_TYPE", Misc.isUndef(prev) ? "" : prev + "");
            defaultProps.setProperty("MIN_TOKEN_GAP", "1800");
            defaultProps.setProperty("MORPHO_DEVICE_EXIST", "1");
            defaultProps.setProperty("MORPHO_API_TYPE", "0");
            defaultProps.setProperty("PRINTER_CONNECTED", "0");
            defaultProps.setProperty("WEIGHMENT_PRINTER_CONNECTED", "1");
            defaultProps.setProperty("REFRESH_INTERVAL", "10");
            defaultProps.setProperty("CREATE_NEW_TRIP", Type.WorkStationType.GATE_IN_TYPE == type ? "1" : "0");
            defaultProps.setProperty("CLOSE_TRIP", Type.WorkStationType.GATE_OUT_TYPE == type ? "1" : "0");
            defaultProps.setProperty("PORT_NODE_ID", "463");
            defaultProps.setProperty("MAXIMUM_TARE_DAYS", "0");
            defaultProps.setProperty("WEIGHT", "");
            workStationConfigFile = new File(WorkStationpath + suffix + "_configuration.property");
            // loads properties from file
            if (!workStationConfigFile.exists()) {
                File f = new File(WorkStationpath + suffix + "_configuration.property");
                f.getParentFile().mkdirs();
                f.createNewFile();
                FileOutputStream cfos = new FileOutputStream(WorkStationpath + suffix + "_configuration.property");
                defaultProps.store(cfos, Type.WorkStationType.getString(type));
                cfos.close();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static Properties getWorkStationConfiguration(String suffix, int type) throws FileNotFoundException, IOException {
        Properties config = null;
        workStationConfigFile = new File(WorkStationpath + suffix + "_configuration.property");
        if (workStationConfigFile.exists()) {
            InputStream inputStream = null;
            try {
                config = new Properties();
                inputStream = new FileInputStream(WorkStationpath + suffix + "_configuration.property");
                config.load(inputStream);
                inputStream.close();
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
            loadWorkStationProperties(suffix, type);
        }
        return config;
    }
    public static Properties getSystemConfiguration() throws FileNotFoundException, IOException {
        Properties config = null;
        workStationConfigFile = new File(WorkStationpath+"system_configuration.property");
        if (workStationConfigFile.exists()) {
            InputStream inputStream = null;
            try {
                config = new Properties();
                inputStream = new FileInputStream(WorkStationpath+"system_configuration.property");
                config.load(inputStream);
                inputStream.close();
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
        	loadSystemProperties();
        }
        return config;
    }
    public static void loadSystemProperties() {
        try {
            Properties defaultProps = new Properties();
            // sets default properties
            defaultProps.setProperty("SYNC_CLOCK", "1");
            defaultProps.setProperty("SYSTEM_DATE_FORMAT", "");
            defaultProps.setProperty("DEBUG", "0");
            defaultProps.setProperty("CLOCK_SYNC_FREQ", "");

            workStationConfigFile = new File(WorkStationpath+"system_configuration.property");
            // loads properties from file
            if (!workStationConfigFile.exists()) {
                File f = new File(WorkStationpath+"system_configuration.property");
                f.getParentFile().mkdirs();
                f.createNewFile();
                FileOutputStream cfos = new FileOutputStream(WorkStationpath+"system_configuration.property");
                defaultProps.store(cfos, "system_configuration");
                cfos.close();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
