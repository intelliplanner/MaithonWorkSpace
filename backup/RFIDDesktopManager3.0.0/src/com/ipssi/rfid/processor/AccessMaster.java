package com.ipssi.rfid.processor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import com.ipssi.gen.utils.Cache;
import com.ipssi.gen.utils.DBConnectionPool;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.PrivInfo;
import com.ipssi.gen.utils.User;


public class AccessMaster {
	public static boolean isEditable(Connection conn,int portNodeId,int userId,String tag){
		boolean retval = false;
        User user;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {

        	Cache cache = Cache.getCacheInstance(conn);
        	user = new User(userId,conn,cache);
        	PrivInfo.TagInfo rwTagInfo = cache.getPrivId(tag);
        	int _notReadOnly = rwTagInfo == null ? Misc.getUndefInt() : rwTagInfo.m_write; 
        	retval = user.isPrivAvailable(conn, _notReadOnly, Misc.getUndefInt(), Misc.getUndefInt(), portNodeId, true, Misc.getUndefInt(), Misc.getUndefInt(), Misc.getUndefInt(), Misc.getUndefInt(), false, null);
        } catch (Exception e) {
              e.printStackTrace();
        }
		return retval;
	}
	public static ArrayList<Integer> getPrivList(Connection conn,int userId){
		ArrayList<Integer> retval = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
        	//check if it is super user
        	ps = conn.prepareStatement("select role_privs.priv_id from users join user_roles on (user_roles.user_1_id=users.id)  join role_privs on (user_roles.role_id = role_privs.role_id) where users.id=?");
        	Misc.setParamInt(ps, userId, 1);
        	rs = ps.executeQuery();
        	while(rs.next()){
        		if(retval == null)
        			retval = new ArrayList<Integer>();
        		retval.add(Misc.getRsetInt(rs, 1));
        	}
        } catch (Exception e) {
              e.printStackTrace();
        }
		return retval;
	}
	public static boolean isSuperUser(Connection conn, int userId) {
		if(userId == 1)
			return true;
		boolean retval = false;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
        	//check if it is super user
        	ps = conn.prepareStatement("select user_roles.role_id from users join user_roles on (user_roles.user_1_id=users.id)  where user_roles.role_id=1 and users.id=?");
        	Misc.setParamInt(ps, userId, 1);
        	rs = ps.executeQuery();
        	if(rs.next()){
        		return true;
        	}
        } catch (Exception e) {
              e.printStackTrace();
        }
		return retval;
	}
}
