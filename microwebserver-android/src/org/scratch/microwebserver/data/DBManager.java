/**
 * Android Micro Web Server
 * Copyright (C) 2011  ScR4tCh
 * Contact scr4tch@scr4tch.org

 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the Lesser GNU General Public License for more details.

 * You should have received a copy of the GNU Lesser General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package org.scratch.microwebserver.data;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;

import org.scratch.microwebserver.properties.PropertyNames;
import org.scratch.microwebserver.properties.ServerProperties;
import org.scratch.microwebserver.users.Rights;


public class DBManager
{
	private static DBManager instance;
	private Connection conn;
	private Statement stmt;
	private ResultSet result;
	
	
	private static final String JDBC_DRIVER="com.lemadi.storage.database.sqldroid.SqldroidDriver";
	private static final String CONNECT_URI="jdbc:sqldroid:";
	
	private static final Object instanceLock = new Object();
	
	private Set<String> tables = new HashSet<String>();
	
	protected DBManager(String database)
	{
		try
		{
			Class.forName(JDBC_DRIVER);
			conn = DriverManager.getConnection(CONNECT_URI+database);
		}catch(Exception e)
		 {
			e.printStackTrace();
		 }
		
	   
		try
		{
			stmt = conn.createStatement();

			//NOTE: SQLITE ONLY (NOT REALLY SAFE)
			stmt.execute("PRAGMA synchronous = 0");
			
			stmt.execute("CREATE TABLE IF NOT EXISTS users (id INTEGER PRIMARY KEY AUTOINCREMENT,username VARCHAR[64] NOT NULL,password CHAR[32] NOT NULL,rights INTEGER)");
			stmt.execute("CREATE TABLE IF NOT EXISTS tablesecurity (tablename VARCHAR[256] NOT NULL,user INTEGER,dbrights INTEGER)");
			stmt.execute("CREATE TABLE IF NOT EXISTS tokens (userid INTEGER PRIMARY KEY,expire LONG,token CHAR[32])");
			
			//NOTE: SQLITE ONLY
			result=stmt.executeQuery("SELECT name FROM sqlite_master WHERE type='table'");
			while(result.next())
			{
				tables.add(result.getString("name"));
			}
			
			
			//TEST ONLY !
			fillTestData();
			
		}catch (SQLException sqle)
		 {
			sqle.printStackTrace();
		 }
			
		
	}
	
	private void fillTestData() throws SQLException
	{
		int rights1=0;
		rights1|=Rights.WEBACCESS;
		stmt.execute("INSERT INTO users      (username,password,rights) 			  VALUES (\"root\",\""+encode("42")+"\","+rights1+")");
	}
	
	@SuppressWarnings("unused")
	private boolean checkTableUserRight(int user,String table,Statement st) throws SQLException
	{
		if(!tables.contains(table))
		{
			return false;
		}
		else
		{
			return false;
		}
	}
	
	public Vector<TreeMap<String,Object>> doQuery(String q) throws SQLException
	{
		Vector<TreeMap<String, Object>> ret = new Vector<TreeMap<String,Object>>();
		
		ResultSet rs = stmt.executeQuery(q);
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
	
	private String encode(String what)
	{
		MessageDigest digest;
		try
		{
			digest=MessageDigest.getInstance("MD5");
			digest.update(what.getBytes());
			byte[] hash=digest.digest();
	
			StringBuffer hexString=new StringBuffer();
			for(int i=0;i<hash.length;i++)
			{
				hexString.append(Integer.toHexString(0xFF&hash[i]));
			}

			return hexString.toString();
		} catch(NoSuchAlgorithmException nsae)
		  {
			nsae.printStackTrace();
			return null;
		  }	
	}

	//returns user id !
	public int login(String username,String password) throws SQLException
	{	
		//check if username exists and has the right password
		result=stmt.executeQuery("SELECT password,id FROM users WHERE username=\""+username+"\"");
		if(result.next())
		{
			if(result.getString("password").equals(encode(password)))
			{
				return result.getInt("id");
			}		
		}
		
		return -1;
	}
	
	//TODO: test this ! it's a bit ... dirty ...
	public boolean checkTokenValid(String token) throws SQLException
	{
		result=stmt.executeQuery("SELECT token FROM tokens WHERE token=\""+token+"\"");
		if(result.next())
		{
			return true;
		}
		
		return false;
	}
	
	//let a token expire imidiatly
	public void invalidateToken(String token) throws SQLException
	{
		result=stmt.executeQuery("SELECT token,userid FROM tokens WHERE token=\""+token+"\"");
		if(result.next())
		{
			int userid=result.getInt("userid");
			stmt.execute("UPDATE tokens SET expire=0 WHERE userid="+userid);
		}
	}
	
	//update a token
	public String updateToken(String token) throws SQLException
	{
		result=stmt.executeQuery("SELECT token,userid FROM tokens WHERE token=\""+token+"\"");
		if(result.next())
		{
			//update token expiration
			int userid=result.getInt("userid");
			result=stmt.executeQuery("SELECT username,password FROM users WHERE id="+userid);
			String ntoken=encode(result.getString("username")+result.getString("password")+System.currentTimeMillis());
			long exp=System.currentTimeMillis()+ServerProperties.getInstance().getLong(PropertyNames.TOKEN_EXPIRATION.toString());
			stmt.execute("UPDATE tokens SET token=\""+ntoken+"\",expire="+exp+" WHERE userid="+userid);
				
			return ntoken;
		}
		
		return null;
	}
	
	//checks wether a given token is expired or not
	public boolean checkTokenExpired(String token) throws SQLException
	{
		result=stmt.executeQuery("SELECT expire FROM tokens WHERE token=\""+token+"\"");
		if(result.next())
		{
			if(result.getLong("expire")>System.currentTimeMillis())
				return false;
		}
		
		return true;
	}
	
	//returns a token if username and password are correct, returns null if not
	public String generateToken(String username,String password) throws SQLException
	{
		result=stmt.executeQuery("SELECT password,rights,id FROM users WHERE username=\""+username+"\"");
		if(result.next())
		{
			System.err.println(result.getInt("rights"));
			
			if(result.getString("password").equals(encode(password)) && Rights.hasRight(result.getInt("rights"),Rights.WEBACCESS))
			{
				//TODO: update token expiration
				int userid=result.getInt("id");
				String token=encode(username+password+System.currentTimeMillis());
				long exp=System.currentTimeMillis()+ServerProperties.getInstance().getLong(PropertyNames.TOKEN_EXPIRATION.toString());
				
				result=stmt.executeQuery("SELECT userid FROM tokens WHERE userid=\""+userid+"\"");
				if(result.next())
					stmt.execute("UPDATE tokens SET token=\""+token+"\",expire="+exp+" WHERE userid="+userid);
				else
					stmt.execute("INSERT INTO tokens (token,expire,userid) VALUES (\""+token+"\","+exp+","+userid+")");
				
				return token;
			}		
		}
		
		return null;
	}
	
	
	public Map<Integer,String> getUsers() throws SQLException
	{
		Map<Integer,String> ret = new HashMap<Integer,String>();
		
		String query = "SELECT id,username FROM users ORDER BY id";
		
		//System.out.println(query);
		ResultSet set=stmt.executeQuery(query);
						
		while(set.next())
		{
			ret.put(set.getInt("id"),set.getString("username"));
		}
		
		
		return ret;
	}
	
	public String[] getAuthSettings(int id) throws SQLException
	{
		String[] ret = new String[2];
		
		String query = "SELECT username,password FROM pushauth where id="+id;
		
		ResultSet set=stmt.executeQuery(query);
		if(set.next())
		{
			ret[0]=set.getString("username");
			ret[1]=set.getString("password");	
		}
		
		return ret;
	}
	
	public int getRights(String token) throws SQLException
	{
		int ret = 0;
		
		String query="SELECT rights FROM users WHERE id=(SELECT userid FROM tokens WHERE token=\""+token+"\")";
		result=stmt.executeQuery(query);
		if(result.next())
		{
			ret=result.getInt("rights");
		}
		
		return ret;
	}
	
	public int addUser(String username,String hash) throws SQLException
	{
		String insert = "INSERT INTO users (username,password) VALUES (\""+username+"\",\""+hash+"\")";
		stmt.execute(insert);
		
		String query = "SELECT id FROM users WHERE username=\""+username+"\" AND password=\""+hash+"\"";
		result=stmt.executeQuery(query);
		if(result.next())
		{
			return result.getInt("id");
		}
		
		return -1;
	}
	
	
	/*public void commit() throws SQLException
	{
		conn.commit();
	}*/
	
	public static DBManager getInstance(String database)
	{
		synchronized(instanceLock)
		{
			if(instance==null)
				instance=new DBManager(database);
		}
		
		return instance;
	}
	
	public void close() throws SQLException
	{
		synchronized(instanceLock)
		{	
			conn.close();
			instance=null;
		}
	}

	public int getRights(int userId) throws SQLException
	{
		int ret = 0;
		
		String query="SELECT rights FROM users WHERE id="+userId;
		result=stmt.executeQuery(query);
		if(result.next())
		{
			ret=result.getInt("rights");
		}
		
		return ret;
	}

}

