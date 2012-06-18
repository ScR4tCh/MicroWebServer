/**
 * Android Micro Web Server
 * Copyright (C) 2011  ScR4tCh
 * Contact scr4tch@scr4tch.org

 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the Lesser GNU General Public License for more details.

 * You should have received a copy of the GNU Lesser General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package org.scratch.microwebserver.data;

import java.io.File;
import java.io.FilenameFilter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.scratch.microwebserver.LogEntry;
import org.scratch.microwebserver.properties.PropertyNames;
import org.scratch.microwebserver.properties.ServerProperties;
import org.scratch.microwebserver.users.Rights;


public class DBManager
{
	private static DBManager instance;
	private Connection conn;
	private Statement stmt;
	private ResultSet result;
	
	protected static final String JDBC_DRIVER="com.lemadi.storage.database.sqldroid.SqldroidDriver";
	protected static final String CONNECT_URI="jdbc:sqldroid:";
	
	private String databasepath;
	
	private static final Object instanceLock = new Object();
	
	private ConcurrentMap<Integer,ConcurrentMap<String,Vector<DatabaseConnection>>> dbConnections = new ConcurrentHashMap<Integer,ConcurrentMap<String,Vector<DatabaseConnection>>>();
	
	private Set<String> databases = new HashSet<String>();
	
	protected DBManager(String databasepath) throws DatabaseManagerException
	{
		File dbpf = new File(databasepath);
	
		if(!dbpf.exists())
			dbpf.mkdirs();
		
		if(!dbpf.isDirectory())
			throw new DatabaseManagerException(databasepath+" is not a directory");
		
		this.databasepath=databasepath;
		
		try
		{
			Class.forName(JDBC_DRIVER);
			conn = DriverManager.getConnection(CONNECT_URI+databasepath+File.separator+"microwebserver.sqlite");
		}catch(Exception e)
		 {
			e.printStackTrace();
		 }
		
	   
		try
		{
			stmt = conn.createStatement();

			//NOTE: SQLITE ONLY (NOT REALLY SAFE)
			stmt.execute("PRAGMA synchronous = 0");
			stmt.execute("CREATE TABLE IF NOT EXISTS logs		(id INTEGER PRIMARY KEY AUTOINCREMENT,time INTEGER,level INTEGER,request TEXT,remoteaddress VARCHAR[256],message TEXT NOT NULL)");
			
			stmt.execute("CREATE TABLE IF NOT EXISTS users 		(id INTEGER PRIMARY KEY AUTOINCREMENT,username VARCHAR[64] NOT NULL,password CHAR[32] NOT NULL)");
			stmt.execute("CREATE TABLE IF NOT EXISTS dbsecurity (database VARCHAR[512] NOT NULL,user INTEGER,dbrights INTEGER)");
			stmt.execute("CREATE TABLE IF NOT EXISTS tokens 	(userid INTEGER PRIMARY KEY,expire LONG,token CHAR[32])"); //general session creation ?!?
			
			//SET default root user and pw !
			fillStdData();
			
		}catch (SQLException sqle)
		 {
			sqle.printStackTrace();
		 }
			
		//TODO: list databases
		String[] dbf = dbpf.list(new FilenameFilter()
		{
			
			@Override
			public boolean accept(File file,String s)
			{
				if(s.toLowerCase().endsWith(".sqlite"))
					return true;
				
				return false;
			}
		});
		
		for(int i=0;i<dbf.length;i++)
		{
			databases.add(dbf[i].substring(0,dbf[i].indexOf(".sqlite")));
		}
	}
	
	private void fillStdData() throws SQLException
	{
		stmt.execute("INSERT INTO users (username,password) VALUES (\"root\",\""+encodePW("42")+"\")");
	}
	
	public DatabaseConnection openDatabaseConnection(String database,String user,String pwhash) throws NoSuchDatabaseException,DatabaseRightsException,DatabaseUserException, SQLException
	{
		if(!databases.contains(database))
			throw new NoSuchDatabaseException(database);
		
		int userid;
		
		if((userid = checkUserCredentials(user,pwhash))==-1)
			throw new DatabaseUserException();
		
		if(!checkDbRights(database,user))
			throw new DatabaseRightsException();
		
		if(dbConnections.containsKey(userid))
		{
			if(dbConnections.get(userid).containsKey(database))
			{
				if(dbConnections.get(userid).get(database).size()>0)
				{
					//get most idle or so ...or just one per user ? (sqlite normally should open just one connection at time ...)
					return dbConnections.get(userid).get(database).firstElement();
				}
				else
				{
					DatabaseConnection dbconn = new DatabaseConnection(databasepath,database);
					dbConnections.get(userid).get(database).add(dbconn);
					return dbconn;
				}
			}
			else
			{
				DatabaseConnection dbconn = new DatabaseConnection(databasepath,database);
				
				Vector<DatabaseConnection> vcns = new Vector<DatabaseConnection>();
				vcns.add(dbconn);
				
				dbConnections.get(userid).put(database,vcns);
				
				return dbconn;
			}
		}
		
		
		DatabaseConnection dbconn = new DatabaseConnection(databasepath,database);
		ConcurrentMap<String,Vector<DatabaseConnection>> cns = new ConcurrentHashMap<String,Vector<DatabaseConnection>>();
		
		Vector<DatabaseConnection> vcns = new Vector<DatabaseConnection>();
		vcns.add(dbconn);
		cns.put(database,vcns);
		
		dbConnections.put(userid,cns);
		return dbconn;
	}
	
	private boolean checkDbRights(String database,String user) throws SQLException
	{
		ResultSet rs = stmt.executeQuery("SELECT dbrights FROM dbsecurity WHERE user='"+user+"'");
		
		if(rs.next())
			return true;
		
		return false;
	}

	private int checkUserCredentials(String user,String pwhash) throws SQLException
	{
		// TODO Auto-generated method stub
		ResultSet rs=stmt.executeQuery("SELECT id FROM users WHERE username='"+user+"' AND password='"+pwhash+"'");
		
		if(rs.next())
			return rs.getInt("id");
		
		return -1;
	}
	
	public static String encodePW(String what)
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
			if(result.getString("password").equals(encodePW(password)))
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
			String ntoken=encodePW(result.getString("username")+result.getString("password")+System.currentTimeMillis());
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
			
			if(result.getString("password").equals(encodePW(password)) && Rights.hasRight(result.getInt("rights"),Rights.WEBACCESS))
			{
				//TODO: update token expiration
				int userid=result.getInt("id");
				String token=encodePW(username+password+System.currentTimeMillis());
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
	
	public static DBManager getInstance(String database) throws DatabaseManagerException
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
	
	public void log(LogEntry e) throws SQLException
	{
		String insert = "INSERT INTO logs (time,level,request,remoteaddress,message) VALUES ("+e.getT()+","+e.getLevel()+",\""+e.getRequest()+"\",\""+e.getRemoteAddress()+"\",\""+e.getMessage()+"\")";
		stmt.execute(insert);
	}
	
	public Vector<LogEntry> getLogs(long now,long history,int minlevel,int maxlevel) throws SQLException
	{
		Vector<LogEntry> ret = new Vector<LogEntry>();
		
		String levelsort="";
		
		if(minlevel == -1 && maxlevel!=-1)
			levelsort = "AND level < "+maxlevel;
		else if(minlevel != -1 && maxlevel==-1)
			levelsort = "AND level > "+minlevel;
		else if(minlevel!=-1 && maxlevel!=-1)
			levelsort = "AND level BETWEEN "+minlevel+" AND "+maxlevel;
		
		String q = "SELECT time,level,request,remoteaddress,message FROM logs WHERE time>="+(now-history)+levelsort+" ORDER BY time"; 
		ResultSet rs = stmt.executeQuery(q);
		
		while(rs.next())
		{
			ret.add(new LogEntry(rs.getLong("time"),rs.getInt("level"),rs.getString("message"),rs.getString("request"),rs.getString("remoteaddress")));
		}
		
		return ret;
	}

}

