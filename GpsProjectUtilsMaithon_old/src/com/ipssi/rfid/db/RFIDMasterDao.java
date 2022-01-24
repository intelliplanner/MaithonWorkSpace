package com.ipssi.rfid.db;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import com.ipssi.gen.utils.DBConnectionPool;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.Pair;
import com.ipssi.rfid.beans.TPRecord;
import com.ipssi.rfid.db.Table.Column;
import com.ipssi.rfid.db.Table.GENRATED;
import com.ipssi.rfid.db.Table.GenratedTime;
import com.ipssi.rfid.db.Table.JOIN;
import com.ipssi.rfid.db.Table.KEY;
import com.ipssi.rfid.db.Table.PRIMARY_KEY;


public class RFIDMasterDao {
	public static final int INSERT = 0;
	public static final int UPDATE = 1;
	public static final int DELETE = 2;
	public static final int SELECT = 3;
	
	private static HashMap<Class,ArrayList<DatabaseColumn>> classFieldMap = new HashMap<Class, ArrayList<DatabaseColumn>>();
	public static boolean executeQuery(Connection conn, String query) throws Exception{
		boolean retval = false;
		PreparedStatement ps = null;
		try{//generic_params
			if(query != null && query.length() > 0){
				ps = conn.prepareStatement(query);
				ps.executeUpdate();
				retval = true;
			}
		}catch(Exception ex){
			ex.printStackTrace();
			throw ex;
		}finally{
			if(ps != null)
				ps.close();
		}
		return retval;
	}
	public static int getRowCount(Connection conn, String query) throws Exception{
		int retval = 0;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try{//generic_params
			if(query != null && query.length() > 0){
				ps = conn.prepareStatement(query);
				rs = ps.executeQuery();
				while(rs.next()){
					retval++;
				}
			}
		}catch(Exception ex){
			ex.printStackTrace();
			throw ex;
		}finally{
			if(ps != null)
				ps.close();
		}
		return retval;
	}
	public static Object get(Connection conn, Class<?> objType , int id) throws Exception{
		Object dataBean = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		Pair<StringBuilder,ArrayList<DatabaseColumn>> queryPair = null;
		ArrayList<Object> retval = null;
		Object obj = null;
		try{
			obj = objType.newInstance();
			setPrimaryValue(obj, id);
			queryPair = getGeneralQuery(SELECT, obj,false, null);
			if(queryPair != null && queryPair.first != null){
				ps = conn.prepareStatement(queryPair.first.toString());
				rs = ps.executeQuery();
				if (rs.next()){
				    if(retval == null)
				    	retval = new ArrayList<Object>();
				    dataBean = obj.getClass().newInstance();
					if(queryPair.second != null && queryPair.second.size() > 0){
						 for(DatabaseColumn dc : queryPair.second){
							 setRsetValue(rs,dc,dataBean);
						 }
					}
				}
				//retval = true;
			}
		}catch(Exception ex){
			ex.printStackTrace();
			throw ex;
		}finally{
			if(ps != null)
				ps.close();
			if(rs != null)
				rs.close();
		}
		return dataBean;
	}
	public static void main(String[] args) {
		Connection conn = null;
		boolean destroyIt = false;
		try {
			conn = DBConnectionPool.getConnectionFromPoolNonWeb();
			/*TPRecord tpr1 = new TPRecord();
			tpr1.setVehicleId(21141);
			tpr1.setTransporterId(12);
			tpr1.setTprStatus(TPR.OPEN);
			tpr1.setTprCreateDate(new Date());
			insert(conn,tpr1);
			TPRecord tpr2 = new TPRecord();
			tpr2.setVehicleId(21141);
			tpr2.setTprStatus(TPR.OPEN);
			List tprList = select(conn,tpr2);
			TPRecord tpr3 = null;
			if(tprList != null && tprList.size() > 0){
				tpr3 = (TPRecord)tprList.get(0);
			}
			if(tpr3 != null){
				tpr3.setTprStatus(TPR.CLOSE);
				update(conn, tpr3);
				
			}*/
			System.out.println(new Date(1457768651000l));
			System.out.println(new Date(1457720051000l));
			System.out.println(System.currentTimeMillis()+",1457768651000l,1457720051000l");
			System.out.println(new Date().getTime());
			ArrayList<DatabaseColumn> t = getClassDatabaseColumn(TPRecord.class);
			ArrayList<DatabaseColumn> t2 = getClassDatabaseColumn(TPRecord.class);
			if(true)
				return;
			
			ArrayList<TPRecord> list = new ArrayList<TPRecord>();
			TPRecord tpr1 = (TPRecord) get(conn, TPRecord.class, 1);
			TPRecord tpr2 = (TPRecord) get(conn, TPRecord.class, 16);
			TPRecord tpr3 = (TPRecord) get(conn, TPRecord.class, 3);
			list.add(tpr1);
			list.add(tpr2);
			list.add(tpr3);
			//insertList(conn, list);
			for(TPRecord tp : list){
				System.out.println(tp == null ? Misc.getUndefInt() : tp.getTprId());
			}
			System.out.println();
			/*Object someObject = tpr1.getVehicleId();
			//System.out.println("test=" + tpr1.nextStep+",Hash="+hash);
			for (Field field : tpr1.getClass().getDeclaredFields()) {
			    field.setAccessible(true); // You might want to set modifier to public first.
			    Object value = field.get(tpr1); 
			    if (value != null) {	
			    	int hash1 = System.identityHashCode(value);
			    	//System.out.println(field.getName() + "=" + value+",Hash="+hash1);
			    }
			}*/
		} catch (Exception e) {
			destroyIt = true;
		} finally {
			try {
				DBConnectionPool.returnConnectionToPoolNonWeb(conn, destroyIt);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
	}
	private static StringBuilder getGeneralQuery(int type, String tableName,ArrayList<DatabaseColumn> cols,ArrayList<DatabaseColumn> clauses){
		StringBuilder retval = null;
		StringBuilder colStr = null;
		StringBuilder valueStr = null;
		StringBuilder whereStr = null;
		try{
			if(tableName != null && tableName.length() > 0 && cols != null && cols.size() > 0){
				if(type == INSERT){
					for(DatabaseColumn dc : cols){
						if(dc.isAuto())
							continue;
						if(retval == null){
							retval = new StringBuilder("");
							colStr = new StringBuilder("");
							valueStr = new StringBuilder("");
							retval.append("insert into ").append(tableName).append(" ");
						}
						else {
							colStr.append(", ");
							valueStr.append(", ");
						}
						colStr.append(dc.getColName());
						valueStr.append(dc.getColVal());
					}
					if(retval != null && colStr != null && valueStr != null){
						retval.append("(").append(colStr.toString()).append(") values (").append(valueStr).append(")");
					}
				}
				else if(type == UPDATE){
					for(DatabaseColumn dc : cols){
						if(retval == null){
							retval = new StringBuilder("");
							colStr = new StringBuilder("");
							retval.append("update ").append(tableName).append(" set ");
						}
						else {
							colStr.append(", ");
						}

						colStr.append(dc.getColName()).append(" = ").append(dc.getColVal());
					}
					if(clauses != null  && clauses.size() > 0){
						for(DatabaseColumn dc : clauses){
							if(whereStr == null){
								whereStr = new StringBuilder("");
								whereStr.append(" where ");
							}
							else {
								whereStr.append(" and ");
							}
							whereStr.append(dc.getColName()).append(" = ").append(dc.getColVal());
						}
					}
					if(retval != null && colStr != null){
						retval.append(colStr.toString());
						if(whereStr != null)
							retval.append(whereStr.toString());
					}
				}else if(type == SELECT){
					for(DatabaseColumn dc : cols){
						if(retval == null){
							retval = new StringBuilder("");
							colStr = new StringBuilder("");
							retval.append("select ");
						}
						else {
							colStr.append(", ");
						}

						colStr.append(tableName).append(".").append(dc.getColName());;
					}
					if(clauses != null  && clauses.size() > 0){
						for(DatabaseColumn dc : clauses){
							if(whereStr == null){
								whereStr = new StringBuilder("");
								whereStr.append(" where ");
							}
							else {
								whereStr.append(" and ");
							}
							whereStr.append(dc.getColName()).append(" = ").append(dc.getColVal());
						}
					}
					if(retval != null && colStr != null){
						retval.append(colStr.toString()).append(" from ").append(tableName).append(" ");
						if(whereStr != null)
							retval.append(whereStr.toString());
					}
				}
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return retval;

	}
	private static Pair<StringBuilder, ArrayList<DatabaseColumn>> getGeneralQuery(int type, Object obj) throws IllegalArgumentException, IllegalAccessException{
		return getGeneralQuery(type, obj, false,null);
	}
	private static Pair<StringBuilder, ArrayList<DatabaseColumn>> getGeneralQuery(int type, Object obj, boolean isApprvd) throws IllegalArgumentException, IllegalAccessException{
		return getGeneralQuery(type, obj, isApprvd,null);
	}
	private static Pair<StringBuilder, ArrayList<DatabaseColumn>> getGeneralQuery(int type, Object obj,boolean isApprvd,Criteria criteria) throws IllegalArgumentException, IllegalAccessException{
		StringBuilder query = new StringBuilder();
		StringBuilder colStr = new StringBuilder();
		StringBuilder valueStr = new StringBuilder();
		StringBuilder fromStr = new StringBuilder();
		StringBuilder whereStr = new StringBuilder();
		String tableName = null;
		Class<?> base = null;
		ArrayList<DatabaseColumn> columns = new ArrayList<DatabaseColumn>();
//		try{
		if(obj != null){
			base = obj.getClass();
			if (base.isAnnotationPresent(Table.class)) {
				Annotation annotation = base.getAnnotation(Table.class);
				Table table  = (Table) annotation;
				if(table != null)
					tableName = isApprvd ? table.value()+"_apprvd" :  table.value();
				
			}
			getGeneralQuery(type, obj, query, colStr, valueStr, fromStr, whereStr, columns, isApprvd);
			if( colStr != null && colStr.length() > 0 ){
				if(type == INSERT)
					query.append("insert into ").append(tableName).append(" (").append(colStr.toString()).append(") values (").append(valueStr).append(")");
				else if(type == UPDATE){
					query.append("update ").append(tableName).append(" set ").append(colStr.toString());
					if(whereStr != null)
						query.append(" where ").append(whereStr.toString());
				}else if(type == SELECT){
					query.append("select ").append(colStr.toString()).append(" from ").append(tableName).append(" ").append(fromStr.toString());
					if(whereStr != null && whereStr.toString().length() > 0){
						query.append(" where ").append(whereStr.toString());
					}
					if(criteria != null ){
						if(criteria.getWhrClause() != null ){
							if(whereStr != null && whereStr.toString().length() > 0)
								query.append(" and ");
							else
								query.append(" where ");
							query.append(criteria.getWhrClause().toString());
						}
						if(criteria.getOrderByClause() != null && criteria.getOrderByClause().toString() != null && criteria.getOrderByClause().length() > 0){
							query.append(" order by ").append(criteria.getOrderByClause().toString());
							if(criteria.isDesc())
								query.append(" desc ");
							else
								query.append(" asc ");
							if(!Misc.isUndef(criteria.getLimit()))
								query.append(" limit ").append(criteria.getLimit());
						}
					}
				}
				System.out.println("[RFIDMaster Query]:"+query.toString());
			}
		}
		/*}catch(Exception ex){
			ex.printStackTrace();
		}*/
		return new Pair<StringBuilder, ArrayList<DatabaseColumn>>(query, columns);
	}
	private static void getGeneralQuery(int type, Object obj,StringBuilder query,StringBuilder colStr,StringBuilder valueStr,StringBuilder fromStr,StringBuilder whereStr,ArrayList<DatabaseColumn> columns ,boolean isApprvd) throws IllegalArgumentException, IllegalAccessException{
		String tableName = null;
		Class<?> base = null;
		int fieldType = 0;
		DatabaseColumn dc = null;
//		try{
			if(obj != null){
				base = obj.getClass();
				if (base.isAnnotationPresent(Table.class)) {
					Annotation annotation = base.getAnnotation(Table.class);
					Table table  = (Table) annotation;
					if(table != null)
						tableName = table.value();
				}
				for(Field field : base.getDeclaredFields()){
					fieldType = DatabaseColumn.STRING;
					if (field.getType().isAssignableFrom(Integer.TYPE)) {
						fieldType = DatabaseColumn.INTEGER;
					}else if (field.getType().isAssignableFrom(String.class)) {
						fieldType = DatabaseColumn.STRING;
					}else if (field.getType().isAssignableFrom(Date.class)) {
						fieldType = DatabaseColumn.DATE;
					}else if (field.getType().isAssignableFrom(Long.TYPE)) {
						fieldType = DatabaseColumn.INTEGER;
					}else if (field.getType().isAssignableFrom(Float.TYPE)) {
						fieldType = DatabaseColumn.DOUBLE;
					}else if (field.getType().isAssignableFrom(Double.TYPE)) {
						fieldType = DatabaseColumn.DOUBLE;
					}else if (field.getType().isAssignableFrom(Boolean.TYPE)) {
						fieldType = DatabaseColumn.BOOLEAN;
					}
					else if(type == SELECT){
						if(field.isAnnotationPresent(JOIN.class) ) {
							Annotation annotation = field.getAnnotation(JOIN.class);
							JOIN join = (JOIN) annotation;
							field.setAccessible(true);
							Object child = field.get(obj);
							if(fromStr != null)
								fromStr.append(" join ").append(join.entity())
								.append(" on ").append(" (")
								.append(tableName).append(".").append(join.parentCol())
								.append("=")
								.append(join.entity()).append(".").append(join.childCol())
								.append(") ");
							getGeneralQuery(fieldType, child, query, colStr, valueStr, fromStr, whereStr, columns,false);
							continue;
						}
					}
					if (field.isAnnotationPresent(Column.class) ) {
						Annotation annotation = field.getAnnotation(Column.class);
						Column col = (Column) annotation;
						field.setAccessible(true);
						dc = new DatabaseColumn(fieldType, col.value(), field.get(obj));
						dc.setField(field);
						if (field.isAnnotationPresent(KEY.class)) {
							dc.setKey(true);
						}
						if (field.isAnnotationPresent(GENRATED.class)) {
							dc.setAuto(true);
						}
						
						if(type == INSERT){
							if (field.isAnnotationPresent(GenratedTime.class)) {
								dc.setGeneratedTime(true);
							}
							if(dc.isAuto() && !isApprvd){
								if(columns == null)
									columns = new ArrayList<DatabaseColumn>();
								columns.add(dc);
								continue;
							}
							if(query == null){
								query = new StringBuilder("");
								colStr = new StringBuilder("");
								valueStr = new StringBuilder("");
								query.append("insert into ").append(tableName).append(" ");
							}
							else if(colStr.length() > 0) {
								colStr.append(", ");
								valueStr.append(", ");
							}
							colStr.append(dc.getColName());
							valueStr.append(dc.getColVal());
						}
						else if(type == UPDATE){
							if(!dc.isNull() && dc.isKey()){
								if(whereStr == null ){
									whereStr = new StringBuilder("");
									whereStr.append(" where ");
								}
								else if(whereStr.length() > 0) {
									whereStr.append(" and ");
								}
								whereStr.append(dc.getColName()).append(" = ").append(dc.getColVal());
								continue;
							}
							if(query == null){
								query = new StringBuilder("");
								colStr = new StringBuilder("");
								query.append("update ").append(tableName).append(" set ");
							}
							else if(colStr.length() > 0) {
								colStr.append(", ");
							}
							colStr.append(dc.getColName()).append(" = ").append(dc.getColVal());
							
			
						}else if(type == SELECT){
							if(columns == null)
								columns = new ArrayList<DatabaseColumn>();
							columns.add(dc);
							if(query == null){
								query = new StringBuilder("");
								colStr = new StringBuilder("");
								fromStr = new StringBuilder(" from ").append(tableName).append(" ");
								query.append("select ");
							}
							else if(colStr.length() > 0) {
								colStr.append(", ");
							}
							colStr.append(tableName).append(".").append(dc.getColName());;
							if(!dc.isNull()){
								if(whereStr == null){
									whereStr = new StringBuilder("");
									whereStr.append(" where ");
								}
								else if(whereStr.length() > 0){
									whereStr.append(" and ");
								}
								whereStr.append(tableName).append(".").append(dc.getColName()).append("=").append(dc.getColVal());
							}
						}
					}
				}
			}
		/*}catch(Exception ex){
			ex.printStackTrace();
		}*/
		//return new Pair<StringBuilder, ArrayList<DatabaseColumn>>(query, columns);
	}
    public static ArrayList<Object> select(Connection conn, Object obj) throws Exception{
    	return select(conn, obj,null); 
    }
    public static ArrayList<?> getList(Connection conn, Object obj,Criteria criteria) throws Exception{
		Object dataBean = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		Pair<StringBuilder,ArrayList<DatabaseColumn>> queryPair = null;
		ArrayList<Object> retval = null;
		try{
			queryPair = getGeneralQuery(SELECT, obj,false, criteria);
			if(queryPair != null && queryPair.first != null){
				ps = conn.prepareStatement(queryPair.first.toString());
				rs = ps.executeQuery();
				while (rs.next()){
				    if(retval == null)
				    	retval = new ArrayList<Object>();
				    dataBean = obj.getClass().newInstance();
					if(queryPair.second != null && queryPair.second.size() > 0){
						 for(DatabaseColumn dc : queryPair.second){
							 setRsetValue(rs,dc,dataBean);
						 }
					}
					retval.add(dataBean);
				}
				//retval = true;
			}
		}catch(Exception ex){
			ex.printStackTrace();
			throw ex;
		}finally{
			if(ps != null)
				ps.close();
			if(rs != null)
				rs.close();
		}
		return retval;
	}
	public static ArrayList<Object> select(Connection conn, Object obj,Criteria criteria) throws Exception{
		Object dataBean = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		Pair<StringBuilder,ArrayList<DatabaseColumn>> queryPair = null;
		ArrayList<Object> retval = null;
		try{
			queryPair = getGeneralQuery(SELECT, obj, false, criteria);
			if(queryPair != null && queryPair.first != null){
				ps = conn.prepareStatement(queryPair.first.toString());
				rs = ps.executeQuery();
				while (rs.next()){
				    if(retval == null)
				    	retval = new ArrayList<Object>();
				    dataBean = obj.getClass().newInstance();
					if(queryPair.second != null && queryPair.second.size() > 0){
						 for(DatabaseColumn dc : queryPair.second){
							 setRsetValue(rs,dc,dataBean);
						 }
					}
					retval.add(dataBean);
				}
				//retval = true;
			}
		}catch(Exception ex){
			ex.printStackTrace();
			throw ex;
		}finally{
			if(ps != null)
				ps.close();
			if(rs != null)
				rs.close();
		}
		return retval;
	}
	private static void setRsetValue(ResultSet rs,DatabaseColumn dc,Object parent){
//		try{
			Field field = dc.getField();
			if (field.getType().isAssignableFrom(Integer.TYPE) || 
				field.getType().isAssignableFrom(String.class) || 
				field.getType().isAssignableFrom(Date.class) ||
				field.getType().isAssignableFrom(Long.TYPE) ||
				field.getType().isAssignableFrom(Float.TYPE) ||
				field.getType().isAssignableFrom(Double.TYPE) ||
				field.getType().isAssignableFrom(Boolean.TYPE)){
				dc.setValue(parent,rs);
			}
		/*}catch(Exception ex){
			ex.printStackTrace();
		}*/
	}
	public static boolean insertList(Connection conn, ArrayList<?> list) throws Exception{
		return insertList(conn, list, false);
	}
	public static boolean insertList(Connection conn, ArrayList<?> list, boolean isApprvd) throws Exception{
		boolean retval = false;
		PreparedStatement ps = null;
		ResultSet rs = null;
		Pair<StringBuilder,ArrayList<DatabaseColumn>> queryPair = null;
		try{
			for(int i=0,is=list == null ? 0 : list.size();i<is;i++){
				queryPair = getGeneralQuery(INSERT, list.get(i),isApprvd);
				if(queryPair == null || queryPair.first == null)
					continue;
				if(ps == null)
					ps = conn.prepareStatement(queryPair.first.toString());
				ps.addBatch(queryPair.first.toString());
			}
			if(ps != null){
				ps.executeBatch();
				retval = true;
			}
		}catch(Exception ex){
			ex.printStackTrace();
			throw ex;
		}finally{
			if(ps != null)
				ps.close();
			if(rs != null)
				rs.close();
		}
		return retval;
	}
	public static boolean insert(Connection conn, Object obj) throws Exception{
		return insert(conn, obj, false);
	}
	public static boolean insert(Connection conn, Object obj, boolean isApprvd) throws Exception{
		boolean retval = false;
		PreparedStatement ps = null;
		ResultSet rs = null;
		Pair<StringBuilder,ArrayList<DatabaseColumn>> queryPair = null;
		try{
			queryPair = getGeneralQuery(INSERT, obj, isApprvd);
			if(queryPair != null && queryPair.first != null){
				ps = conn.prepareStatement(queryPair.first.toString());
				ps.executeUpdate();
				rs = ps.getGeneratedKeys();
				if (rs.next()){
					if(queryPair.second != null && queryPair.second.size() > 0){
						queryPair.second.get(0).setColVal(Misc.getRsetInt(rs, 1));
						queryPair.second.get(0).setValue(obj);
					}
				}
				retval = true;
			}
		}catch(Exception ex){
			ex.printStackTrace();
			throw ex;
		}finally{
			if(ps != null)
				ps.close();
			if(rs != null)
				rs.close();
		}
		return retval;
	}
	public static boolean update(Connection conn, Object obj) throws Exception{
		return update(conn, obj, false);
	}
	public static boolean update(Connection conn, Object obj, boolean isApprvd) throws Exception{
		boolean retval = false;
		PreparedStatement ps = null;
		Pair<StringBuilder,ArrayList<DatabaseColumn>> queryPair = null;
		try{//generic_params
			queryPair = getGeneralQuery(UPDATE, obj, isApprvd);
			if(queryPair != null && queryPair.first != null){
				ps = conn.prepareStatement(queryPair.first.toString());
				ps.executeUpdate();
				retval = true;
			}
		}catch(Exception ex){
			ex.printStackTrace();
			throw ex;
		}finally{
			if(ps != null)
				ps.close();
		}
		return retval;
	}
	public static ArrayList<DatabaseColumn> getDatabaseColumns(Object obj) throws IllegalArgumentException, IllegalAccessException{
		ArrayList<DatabaseColumn> retval = null;
		DatabaseColumn dc = null;
		String tableName = null;
		Class<?> base = null;
//		try{
			if(obj != null){
				base = obj.getClass();
				if (base.isAnnotationPresent(Table.class)) {
					Annotation annotation = base.getAnnotation(Table.class);
					Table table  = (Table) annotation;
					if(table != null)
						tableName = table.value();
				}
				for(Field field : base.getDeclaredFields()){
					int type = DatabaseColumn.STRING;
					if (field.getType().isAssignableFrom(Integer.TYPE)) {
						type = DatabaseColumn.INTEGER;
					}else if (field.getType().isAssignableFrom(String.class)) {
						type = DatabaseColumn.STRING;
					}else if (field.getType().isAssignableFrom(Date.class)) {
						type = DatabaseColumn.DATE;
					}else if (field.getType().isAssignableFrom(Long.TYPE)) {
						type = DatabaseColumn.INTEGER;
					}else if (field.getType().isAssignableFrom(Float.TYPE)) {
						type = DatabaseColumn.DOUBLE;
					}else if (field.getType().isAssignableFrom(Double.TYPE)) {
						type = DatabaseColumn.DOUBLE;
					}
					if (field.isAnnotationPresent(Column.class)) {
						Annotation annotation = field.getAnnotation(Column.class);
						Column col = (Column) annotation;
						field.setAccessible(true);
						dc = new DatabaseColumn(type, col.value(), field.get(obj));
						if (field.isAnnotationPresent(KEY.class)) {
							dc.setKey(true);
						}
						if (field.isAnnotationPresent(GENRATED.class)) {
							dc.setAuto(true);
						}
					}
					if(retval == null)
						retval = new ArrayList<DatabaseColumn>();
					if(dc != null)
						retval.add(dc);
				}
			}
	/*	}catch(Exception ex){
			ex.printStackTrace();
		}*/
		return retval;
	}
	private static void setPrimaryValue(Object obj,int key) throws IllegalArgumentException, IllegalAccessException{
//		try{
			if(obj == null || Misc.isUndef(key))
				return;
			Class<?> base = obj.getClass();
			Field primary = null;
			for(Field field : base.getDeclaredFields()){
				/*if(field.isAnnotationPresent(KEY.class)){
					primary = field;
				}*/
				if (field.isAnnotationPresent(PRIMARY_KEY.class)) {
					primary = field;
					break;
				}
			}
			if(primary != null){
				primary.setAccessible(true);
				primary.set(obj, key);
			}
		/*}catch(Exception ex){
			ex.printStackTrace();
		}*/
	}
	public static ArrayList<DatabaseColumn> getClassDatabaseColumn(Class base){
		if(base == null)
			return null;
//		try{
			if(!classFieldMap.containsKey(base)){
				String tableName = null;
				int fieldType = DatabaseColumn.STRING;
				ArrayList<DatabaseColumn> colList = null;
				if (base.isAnnotationPresent(Table.class)) {
					Annotation annotation = base.getAnnotation(Table.class);
					Table table  = (Table) annotation;
					if(table != null)
						tableName = table.value();
				}
				for(Field field : base.getDeclaredFields()){
					fieldType = DatabaseColumn.STRING;
					if (field.getType().isAssignableFrom(Integer.TYPE)) {
						fieldType = DatabaseColumn.INTEGER;
					}else if (field.getType().isAssignableFrom(String.class)) {
						fieldType = DatabaseColumn.STRING;
					}else if (field.getType().isAssignableFrom(Date.class)) {
						fieldType = DatabaseColumn.DATE;
					}else if (field.getType().isAssignableFrom(Long.TYPE)) {
						fieldType = DatabaseColumn.INTEGER;
					}else if (field.getType().isAssignableFrom(Float.TYPE)) {
						fieldType = DatabaseColumn.DOUBLE;
					}else if (field.getType().isAssignableFrom(Double.TYPE)) {
						fieldType = DatabaseColumn.DOUBLE;
					}else if (field.getType().isAssignableFrom(Boolean.TYPE)) {
						fieldType = DatabaseColumn.BOOLEAN;
					}
					DatabaseColumn dc = null;
					if (field.isAnnotationPresent(Column.class) ) {
						if(colList == null)
							colList = new ArrayList<DatabaseColumn>();
						Annotation annotation = field.getAnnotation(Column.class);
						Column col = (Column) annotation;
						field.setAccessible(true);
						dc = new DatabaseColumn(fieldType, col.value(), null);
						dc.setField(field);
						if (field.isAnnotationPresent(KEY.class)) {
							dc.setKey(true);
						}
						if (field.isAnnotationPresent(GENRATED.class)) {
							dc.setAuto(true);
						}
						colList.add(dc);
					}
					
				}
				classFieldMap.put(base, colList);
			}
			return classFieldMap.get(base);
		/*}catch(Exception ex){
			ex.printStackTrace();
		}*/
		//return null;
	}
	
}
