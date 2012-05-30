/**
 * Android Micro Web Server
 * Copyright (C) 2011  ScR4tCh
 * Contact scr4tch@scr4tch.org

 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the Lesser GNU General Public License for more details.

 * You should have received a copy of the GNU Lesser General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package org.scratch.microwebserver;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Vector;

import org.scratch.microwebserver.http.MicroWebServer;
import org.scratch.microwebserver.http.WebConnectionListener;
import org.scratch.microwebserver.properties.PropertyNames;
import org.scratch.microwebserver.properties.ServerProperties;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class MicrowebserverService extends Service implements WebConnectionListener
{
	// "tray" icon
	private NotificationManager mNotificationManager;
	private NotificationCompat.Builder notificationBuilder;
	private PendingIntent notificationIntent;
	private int APPICON_ID=2112;
	
	private MicroWebServer server;
	private final IBinder binder=new MicrowebserverServiceBinder();
	
	private String logfile = PropertyNames.LOGGERNAME+".log";
	private PrintWriter logWriter;
	
	private Vector<ServiceListener> listeners = new Vector<ServiceListener>();
	
	private long startTime=0;
	
	protected class MicrowebserverServiceBinder extends Binder
	{
		public boolean startServer()
		{
			try
			{
				logWriter = new PrintWriter(new OutputStreamWriter(new FileOutputStream(ServerProperties.getRoot()+File.separator+logfile,true)));
			}
			catch(FileNotFoundException e1)
			{
				e1.printStackTrace();
			}
			
			try
			{
				server=new MicroWebServer();
				server.addWebConnectionListener(MicrowebserverService.this);
				createNotification("serving ...");
				return true;
			}
			catch(IOException e)
			{
				Log.e(PropertyNames.LOGGERNAME,"Could not start server: "+e.getMessage());
				return false;
			}
		}
		
		public void stopServer()
		{
			if(server!=null)
			{
				server.shutdown();
				server.removeWebConnectionListener(MicrowebserverService.this);
			}
			
			mNotificationManager.cancel(APPICON_ID);
			
			logWriter.close();
		}

		public boolean isServerUp()
		{
			if(server!=null)
				return server.isOnline();
			
			return false;
		}

		public String getLogFile()
		{
			return ServerProperties.getRoot()+File.separator+logfile;
		}
		
		public void registerServiceListener(ServiceListener sl)
		{
			listeners.add(sl);
		}
		
		public void unregisterServiceListener(ServiceListener sl)
		{
			listeners.remove(sl);
		}

		public long getServerStartTime()
		{
			return MicrowebserverService.this.startTime;
		}
	}
	
	private void createNotification(final String msg)
	{
		Intent nIntent=new Intent(getBaseContext(),MicrowebserverActivity.class);
		nIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

		notificationIntent=PendingIntent.getActivity(this,0,nIntent,PendingIntent.FLAG_UPDATE_CURRENT);
		
		notificationBuilder.setTicker("service started").setSmallIcon(R.drawable.ic_launcher).setOngoing(true).setContentTitle("MicroWebServer").setContentText(msg).setContentIntent(notificationIntent);
		
		mNotificationManager.notify(APPICON_ID,notificationBuilder.getNotification());
	}
	
	public void onCreate()
	{
		super.onCreate();
		
		startTime=System.currentTimeMillis();
		
		mNotificationManager=(NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
		notificationBuilder = new NotificationCompat.Builder(this);
	}
	
	@Override
	public IBinder onBind(Intent i)
	{
		return binder;
	}

	@Override
	public void log(long t,int lev,String client,String request,String msg)
	{
		/**
		 * LOG FORMAT:
		 * 
		 * time(ms)|level|msg|addr|req
		 * 
		 */
		logWriter.println(t+"|"+lev+"|"+msg+"|"+client+"|"+request);
		if(logWriter.checkError())
		{
			LogEntry le = new LogEntry(System.currentTimeMillis(),WebConnectionListener.LOGLEVEL_ERROR,"there was an error while writing to the logfile","","SERVER");
			for(int i=0;i<listeners.size();i++)
				listeners.elementAt(i).log(le);
		}
		
		LogEntry le = new LogEntry(t,lev,msg,request,client);
		for(int i=0;i<listeners.size();i++)
			listeners.elementAt(i).log(le);
	}

}
