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

import org.scratch.microwebserver.properties.PropertyNames;

import android.util.Log;

public class WebConnectionsList implements Runnable
{
	private static volatile int workerId=0;
	private int myId;
	private boolean running=true;
	private MicroWebServer server;
	
	public WebConnectionsList(MicroWebServer server)
	{
		workerId++;
		myId=workerId;
		this.server=server;
	}
	
	//TODO: implement std http commands (HEAD,GET,POST)
	public void run()
	{
		while(running)
		{
			WebConnection conn;
			if((conn=server.getNextConnection())!=null)
			{
				//Logger.getLogger("carma.remote.webworker["+myId+"]").info("processing "+conn.toString());
				
				try
				{
					if(!conn.isConnected())
					{
						server.removeConnection(conn);
						//Logger.getLogger("carma.remote.webworker["+myId+"]").info("removed connection "+conn.toString()+" due to disconnect");
						continue;
					}
					else
					{
						conn.process();
					}
				}catch (IOException e)
				 {
					Log.w(PropertyNames.LOGGERNAME,"[WORKER:"+myId+"]connection error: "+e.getMessage());
					//we do not score, just break !!!
					server.removeConnection(conn);
					continue;
				 }
			}
			
			//cut off some load
			try
			{
				Thread.sleep(20);
			}catch(InterruptedException ie)
			 {
				
			 }
		}
	}
}
