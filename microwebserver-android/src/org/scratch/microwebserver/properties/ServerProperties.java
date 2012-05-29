package org.scratch.microwebserver.properties;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import android.os.Environment;
import android.util.Log;

public class ServerProperties
{	
	
	private static final String ROOT=Environment.getExternalStorageDirectory().getAbsolutePath()+File.separator+"microwebserver";
	
	private static final String PROP_FILE=ROOT+File.separator+"server.properties";
	
	private static ServerProperties instance;
		
	private Map<String,Object> properties=new HashMap<String,Object>();
	
	protected ServerProperties()
	{
		//TODO: check if appdata dir on SD exists if NOT, create it !!!
		if(!(new File(ROOT)).exists())
			(new File(ROOT)).mkdirs();
		
		//fill with std props			
		properties.put(PropertyNames.DATABASE_TYPE,"sqlite"); //TODO: this parameter should define the database type ! 
		properties.put(PropertyNames.DATABASE_URL, ROOT+File.separator+"serverdata.sqlite"); // sqlite database file
		
		properties.put(PropertyNames.SERVER_PORT, 8080); // webserver port
		properties.put(PropertyNames.SERVER_WORKERS,1); //number of webserver workers
		properties.put(PropertyNames.SERVER_ROOT,ROOT+File.separator+"webroot"); //webroot
				
		properties.put(PropertyNames.TOKEN_EXPIRATION,7200000); //how long should a webservice token be valid (std: 2 hours)
		
		try
		{
			readProperties();
		}
		catch(IOException ioe)
		{
			Log.w(PropertyNames.LOGGERNAME,"could not read properties file: "+ioe.getMessage());
			try
			{
				writeProperties();
			}
			catch(IOException e)
			{
				Log.w(PropertyNames.LOGGERNAME,"could not write new properties file: "+e.getMessage());
			}
		}
	}
	
	public static ServerProperties getInstance()
	{
		if(instance==null)
			instance=new ServerProperties();
		
		return instance;
	}
	
	
	public void addProperty(String name,Object value)
	{
		properties.put(name, value);
	}
	
	public Object getProperty(String key)
	{
		return properties.get(key);
	}
	
	public String getString(String key)
	{
		return properties.get(key).toString();
	}
	
	public int getInt(String key)
	{
		return Integer.valueOf(properties.get(key).toString()).intValue();
	}
	
	public long getLong(String key)
	{
		return Long.valueOf(properties.get(key).toString()).longValue();
	}
	
	public float getFloat(String key)
	{
		return Float.valueOf(properties.get(key).toString()).floatValue();
	}
	
	public double getDouble(String key)
	{
		return Double.valueOf(properties.get(key).toString()).doubleValue();
	}
	
	public boolean getBoolean(String key)
	{
		return Boolean.valueOf(properties.get(key).toString()).booleanValue();
	}
	
	public void writeProperties() throws IOException
	{
		Properties p = new Properties();
		
		for(String key:properties.keySet())
		{
			p.put(key, properties.get(key).toString());
		}
		
		DateFormat dateFormat = new SimpleDateFormat("MM.dd.yyyy HH:mm:ss");
        Date date = new Date(System.currentTimeMillis());
		
		p.store(new FileOutputStream(PROP_FILE), "modified on "+dateFormat.format(date));
	}
	
	public void readProperties() throws IOException
	{
		Properties p = new Properties();
		p.load(new FileInputStream(PROP_FILE));
		for (Object ko:p.keySet())
		{
			properties.put(ko.toString(), p.get(ko));
		}
	}

}
