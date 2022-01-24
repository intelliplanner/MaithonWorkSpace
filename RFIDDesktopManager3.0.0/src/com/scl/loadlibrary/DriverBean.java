package com.scl.loadlibrary;

import java.util.ArrayList;
import java.util.Date;

import com.ipssi.gen.utils.Misc;

public class DriverBean {
	public static final int INSERT = 0;
	public static final int UPDATE = 1;
	public static final int DELETE = 2;
    public int id = Misc.getUndefInt();
    public String driver_name;
    public String dl_no;
    public String first_name;
    public String last_name;
    public int status;
    public String driver_std_name;
    public ArrayList <byte[]> capture_template;
    public Date created_on;
    public int task = INSERT;
    public long readTimestamp = Misc.getUndefInt();
}
