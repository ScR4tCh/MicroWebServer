package org.scratch.microwebserver.data;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.scratch.microwebserver.properties.ServerProperties;
import org.scratch.microwebserver.util.Base64;

public class Cache
{
	private Map<String,File> cached;
	private Vector<String> writeList;
	private static Cache instance;
	
	private String p = ServerProperties.getRoot()+File.separator+"cache";
	
	private static volatile boolean instancing=false;
	
	protected Cache()
	{
		instancing=true;
		cached=new HashMap<String,File>();
		writeList=new Vector<String>();
		populateCacheData();
	}
	
	private void populateCacheData()
	{
		File cpf = new File(p);
		
		if(cpf.exists())
		{
			File[] files = cpf.listFiles();
			for(int i=0;i<files.length;i++)
			{
				try
				{
					String realname=new String(Base64.decode(files[i].getName()));
					cached.put(realname,files[i]);
				}catch(Exception e)
				 {
					//TODO: LOG !!!
					e.printStackTrace();
				 }
			}
		}
		else
		{
			cpf.mkdirs();
		}
	}
	
	public boolean has(String file)
	{
		return cached.containsKey(file);
	}
	
	public File getCached(String file)
	{
		return cached.get(file);
	}
	
	public void cache(String id,byte[] data) throws IOException
	{
		if(writeList.contains(id))	//work with semaphores !
			return;
		
		writeList.add(id);
		File f = new File(p+File.separator+Base64.encodeBytes(id.getBytes()));
		FileOutputStream fos = new FileOutputStream(p+File.separator+Base64.encodeBytes(id.getBytes()));
		fos.write(data);
		fos.close();
		
		cached.put(id,f);
		
		writeList.remove(id);
	}
	
	public synchronized static Cache getInstance()
	{
		synchronized(Cache.class)
		{
			if(instance==null)
			{
				if(!instancing)
					instance = new Cache();
			}
		}
		
		return instance;
	}
}
