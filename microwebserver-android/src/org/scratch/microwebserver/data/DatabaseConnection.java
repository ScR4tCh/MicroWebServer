package org.scratch.microwebserver.data;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;

import org.scratch.microwebserver.users.DBRights;

public class DatabaseConnection
{
	private Connection conn;
	private Statement stmt;
	
	private String dbname;
	protected String user;
	protected String pwHash;
	private DBManager dbminst;
	
	private Set<String> tables = new HashSet<String>();
	
	private volatile boolean closed=false;
	
	protected DatabaseConnection(String user,String pwhash,Statement stmt) //for sys DB connection !
	{
		this.user=user;
		this.stmt=stmt;
		
		try
		{
			updateTableIndex();
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
	}
	
	public DatabaseConnection(DBManager dbminst,String user,String databasepath,String database)
	{
		if(dbminst==null)
			throw new IllegalArgumentException("Database manager instance must not be NULL !");
		
		this.user=user;
		this.pwHash=pwHash;
		this.dbminst=dbminst;
		this.dbname=database;
		
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
	
	public Vector<TreeMap<String,Object>> doQuery(String q) throws SQLException,DatabaseManagerException
	{
		Vector<TreeMap<String, Object>> ret = new Vector<TreeMap<String,Object>>();
		
		if(!simpleSQLSyntaxCheck(q,user))
			throw new DatabaseRightsException("you do not have sufficient rights for this query"); // TODO: add message !!!
		
		if(  q.toLowerCase().startsWith("grant") || q.toLowerCase().startsWith("revoke") || q.toLowerCase().equals("list tables") )
		{
					//TODO: intercept some "unsupported" sqlite "features"	
					//--> every functions implies that the user has the sufficient rights (checked with simpleSQLSyntaxCheck)
			
					if(q.toLowerCase().equals("list tables"))
					{
						Vector<String> cn = new Vector<String>();
						cn.add("table");
						
						long j=0;
						
						Iterator<String> rs=tables.iterator();
						
						while(rs.hasNext())
						{
							TreeMap<String,Object> tm = new TreeMap<String,Object>();
							
							tm.put("*",j);
							tm.put("table",rs.next());
							
							ret.add(tm);
							j++;
						}
						
						return ret;
					}
					else if(q.toLowerCase().startsWith("grant"))
					{
						System.err.println("NOT YET IMPLEMENTED");
						return ret;
					}
					else if(q.toLowerCase().startsWith("revoke"))
					{
						System.err.println("NOT YET IMPLEMENTED");
						return ret;
					}
					else
					{
						//should not happen
						throw new SQLException("Your statement conatins errors.");
					}
		}
		
		
		System.err.println("do query: \""+q+"\"");
		
		ResultSet rs = stmt.executeQuery(q);
		
		if(q.toLowerCase().startsWith("create table") || q.toLowerCase().startsWith("drop table"))
			updateTableIndex();

		
		int cc = rs.getMetaData().getColumnCount();
		
		Vector<String> cn = new Vector<String>();
		for(int i=0;i<cc;i++)
		{
			cn.add(rs.getMetaData().getColumnName(i+1));
		}
		
		
		long j=0;
		
		while(rs.next())
		{
			TreeMap<String,Object> tm = new TreeMap<String,Object>();
			
			for(int i=-1;i<cc;i++)
			{
				if(i==-1)
					tm.put("*",j);
				else
					tm.put(cn.elementAt(i),rs.getObject(i+1));
			}
			
			ret.add(tm);
			j++;
		}
		
		return ret;
	}
	
	
	//FIXME: A real SQL Parser would be much better here, so find a lightweight one !!!
	//		 It's not event safe !!! users could easily "inject" higher privileged commands
	//		 into allowed ones to bypass security checks !!!
	protected boolean simpleSQLSyntaxCheck(String query,String user) throws SQLException, DatabaseUserException
	{
		if(closed)
			throw new SQLException("Database connection closed !");
		
		String[] q=query.toLowerCase().split(";");
		
		for(int i=0;i<q.length;i++)
		{
		
			if(q[i].startsWith("select"))
			{
				return dbminst.checkDbRights(dbname,user,DBRights.SELECT);
			}
			else if(q[i].startsWith("insert into"))
			{
				return dbminst.checkDbRights(dbname,user,DBRights.INSERT);
			}
			else if(q[i].startsWith("update"))
			{
				return dbminst.checkDbRights(dbname,user,DBRights.UPDATE);
			}
			else if(q[i].startsWith("delete from")||q[i].startsWith("truncate table"))
			{
				return dbminst.checkDbRights(dbname,user,DBRights.DELETE);
			}
			else if(q[i].startsWith("drop table"))
			{
				return dbminst.checkDbRights(dbname,user,DBRights.DROP_TABLE);
			}
			else if(q[i].startsWith("alter table")||q[i].startsWith("rename"))
			{
				return dbminst.checkDbRights(dbname,user,DBRights.ALTER_TABLE);
			}
			else if(q[i].startsWith("grant")||q[i].startsWith("revoke"))
			{
				return dbminst.checkDbRights(dbname,user,DBRights.GRANT);
			}
			
			//dbmanager functions !
			else if(q[i].startsWith("create table")||q[i].startsWith("create database"))
			{
				String[] tokens = q[i].split("\\s+");
				
				//remember , everything's lowercase here !!!
				if(tokens[1].equals("table"))
				{
					return dbminst.checkDbRights(null,user,DBRights.CREATE_TABLE)||dbminst.checkDbRights(null,user,DBRights.ADMIN);
				}
				else if(tokens[1].equals("database"))	//kidcode again ;)
				{
					if(tokens.length==3 || (tokens.length==6 && tokens[2].equals("if") && tokens[3].equals("not") && tokens[4].equals("exists")) )
					{
						return true;
					}
					
					return false;
				}
				
				return false;
			}
			else if(q[i].startsWith("drop database"))
			{
				String[] tokens = q[i].split("\\s+");
				
				if(tokens.length==3 && tokens[1].equals("database"))	//kidcode again ;)
				{
					return true;
				}
				
				return false;
			}
		}
		
		return false;
		
	}

	public synchronized void close()
	{
		closed=true;
	}
	
	protected synchronized boolean isClosed()
	{
		return closed;
	}
	
}
