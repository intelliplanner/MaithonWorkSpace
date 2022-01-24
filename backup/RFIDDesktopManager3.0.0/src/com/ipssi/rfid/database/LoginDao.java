/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ipssi.rfid.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.ipssi.gen.exception.GenericException;
import com.ipssi.gen.utils.DBConnectionPool;
import com.ipssi.gen.utils.Pair;
//import com.ipssi.rfid.connection.Log_File;
import com.ipssi.gen.utils.Misc;

/**
 *
 * @author Vi$ky
 */
public class LoginDao {
    public static Pair<Integer, String>  Login(Connection conn, String username1, char[] password1) throws GenericException {
        ResultSet rs = null;
        PreparedStatement ps = null;
        int userId = Misc.getUndefInt();
        String name = null;
        try {
            ps = conn.prepareStatement("select id,name from users where username=? and password=? and isactive=1");
            ps.setString(1, username1);
            ps.setString(2, new String(password1));
            rs = ps.executeQuery();
            if(rs.next()) {
                userId = rs.getInt(1);
                name = rs.getString(2);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
        	try {
                if (rs != null) {
                    rs.close();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        	try {
                if (ps != null) {
                    ps.close();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        return new Pair<Integer, String>(userId, name);
    }
}
