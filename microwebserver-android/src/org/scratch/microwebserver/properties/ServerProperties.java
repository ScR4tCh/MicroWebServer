/**
 * Android Micro Web Server
 * Copyright (C) 2011  ScR4tCh
 * Contact scr4tch@scr4tch.org

 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the Lesser GNU General Public License for more details.

 * You should have received a copy of the GNU Lesser General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
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

import org.scratch.microwebserver.R;
import org.scratch.microwebserver.util.AndroidImageResolver;

import android.os.Environment;
import android.util.Log;

public class ServerProperties
{	
	
	private static final String ROOT=Environment.getExternalStorageDirectory().getAbsolutePath()+File.separator+"microwebserver";
	
	private static final String PROP_FILE=ROOT+File.separator+"server.properties";
	
	private static ServerProperties instance;
		
	private Map<String,Object> properties=new HashMap<String,Object>();
	
	private static volatile boolean instancing=false;
	
	protected ServerProperties()
	{
		instancing=true;
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
		
		properties.put(PropertyNames.ALLOW_DIRLIST,true);
		properties.put(PropertyNames.PROCESS_HTACCESS,false);
		properties.put(PropertyNames.FOLLOW_SYMLINKS,true);
		properties.put(PropertyNames.CACHE_PATH,"cached");

		try
		{
			properties.put(PropertyNames.DEFAULT_FOLDER_ICON,AndroidImageResolver.resolveCachedAndroidImage(R.drawable.deffolder));
			properties.put(PropertyNames.DEFAULT_FILE_ICON,AndroidImageResolver.resolveCachedAndroidImage(R.drawable.deffile));
			
			//test only !
			//properties.put(PropertyNames.DEFAULT_FOLDER_ICON,AndroidImageResolver.resolveCachedAndroidImage(android.R.drawable.ic_menu_add));
			//properties.put(PropertyNames.DEFAULT_FILE_ICON,AndroidImageResolver.resolveCachedAndroidImage(android.R.drawable.ic_menu_save));
		}
		catch(IOException e1)
		{
			e1.printStackTrace();
			properties.put(PropertyNames.DEFAULT_FOLDER_ICON,ROOT+File.separator+"icons"+File.separator+"folder.png");
			properties.put(PropertyNames.DEFAULT_FILE_ICON,ROOT+File.separator+"icons"+File.separator+"file.png");
		}
		
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
	
	public synchronized static ServerProperties getInstance()
	{
		if(instance==null)
		{
			synchronized(ServerProperties.class)
			{
				if(!instancing)
					instance=new ServerProperties();
			}
		}
		
		return instance;
	}
	
	public static String getRoot()
	{
		return ROOT;
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
