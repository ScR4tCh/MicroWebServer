/**
 * Android Micro Web Server
 * Copyright (C) 2011  ScR4tCh
 * Contact scr4tch@scr4tch.org

 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the Lesser GNU General Public License for more details.

 * You should have received a copy of the GNU Lesser General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package org.scratch.microwebserver.http;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Vector;

import org.scratch.microwebserver.properties.PropertyNames;
import org.scratch.microwebserver.properties.ServerProperties;


public class MicroWebServer implements Runnable
{
	private Thread webworker;
	private volatile boolean accept=true;
	
	private Vector<WebConnection> connections=new Vector<WebConnection>();
	private ServerSocket ssock;
	
	private int currentConnection=0;
	
	private long startTime=0;
	
	//listen !
	private Vector<WebConnectionListener> wbls = new Vector<WebConnectionListener>();
	
	public MicroWebServer() throws IOException
	{	
		ssock=new ServerSocket(ServerProperties.getInstance().getInt(PropertyNames.SERVER_PORT));
		
		webworker = new Thread(this);
		webworker.start();
		
		
		int workers = ServerProperties.getInstance().getInt(PropertyNames.SERVER_WORKERS);
		if(workers<1)
			workers=1;
		
		for(int i=0;i<workers;i++)
		{
			WebConnectionsList slist = new WebConnectionsList(this);
			Thread list=new Thread(slist);
			list.start();
		}
		
		startTime=System.nanoTime();
	}
	
	
	public void run()
	{
		while(accept && webworker.isAlive())
		{
			try
			{
				WebConnection conn = new WebConnection(ssock.accept(),this);
				connections.add(conn);
				
			} catch (IOException e)
			  {
				// TODO: LOG !
				e.printStackTrace();
			  }
		}
	}
	
	public WebConnection getNextConnection()
	{
		if(currentConnection>=connections.size())
			currentConnection=0;
		
		if(connections.size()==0)
			return null;
		
		return connections.elementAt(currentConnection++);
	}
	
	public void removeConnection(WebConnection c)
	{
		//Logger.getLogger("carma.remote").info("removing web connection "+c.toString());
		connections.remove(c);
	}
	
	public boolean isOnline()
	{
		return accept;
	}

	public void shutdown()
	{
		accept=false;
		try
		{
			try
			{
				ssock.close();
			}
			catch(IOException e)
			{
				//just don't care !
			}
			webworker.join();
		}
		catch(InterruptedException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public long getUptime()
	{
		return (System.nanoTime()-startTime);
	}
	
	public void log(int level,WebConnection wc,String msg)
	{
		for(int i=0;i<wbls.size();i++)
			wbls.elementAt(i).log(level,wc.getRequestingAddr(),wc.getRequest(),msg);
	}
	
	public void addWebConnectionListener(WebConnectionListener wcl)
	{
		wbls.add(wcl);
	}
	
	public void removeWebConnectionListener(WebConnectionListener wcl)
	{
		wbls.add(wcl);
	}
}
