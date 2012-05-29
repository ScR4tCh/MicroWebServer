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
