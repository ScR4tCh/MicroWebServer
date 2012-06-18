package org.scratch.microwebserver.data;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;

public class DatabaseConnection
{
	private Connection conn;
	private Statement stmt;
	
	private Set<String> tables = new HashSet<String>();
	
	public DatabaseConnection(String databasepath,String database)
	{
		try
		{
			Class.forName(DBManager.JDBC_DRIVER);
			conn = DriverManager.getConnection(DBManager.CONNECT_URI+databasepath+File.separator+database+".sqlite");
			stmt = conn.createStatement();
			
			 updateTableIndex();
		}catch(Exception e)
		 {
			e.printStackTrace();
		 }
		
	}
	
	private void updateTableIndex() throws SQLException
	{
		//NOTE: SQLITE ONLY
		ResultSet rs=stmt.executeQuery("SELECT name FROM sqlite_master WHERE type='table'");
		while(rs.next())
		{
			tables.add(rs.getString("name"));
		}
	}
	
	public Vector<TreeMap<String,Object>> doQuery(String q) throws SQLException
	{
		Vector<TreeMap<String, Object>> ret = new Vector<TreeMap<String,Object>>();
		
		ResultSet rs = stmt.executeQuery(q);
		
		if(q.toLowerCase().startsWith("create"))
			 updateTableIndex();
		
		int cc = rs.getMetaData().getColumnCount();
		
		Vector<String> cn = new Vector<String>();
		for(int i=0;i<cc;i++)
		{
			cn.add(rs.getMetaData().getColumnName(i));
		}
		
		
		long j=1;
		
		while(rs.next())
		{
			TreeMap<String,Object> tm = new TreeMap<String,Object>();
			
			for(int i=-1;i<cc;i++)
			{
				if(i==-1)
					tm.put("*",j);
				else
					tm.put(cn.elementAt(i),rs.getObject(i));
			}
			
			ret.add(tm);
			j++;
		}
		
		return ret;
	}
}
