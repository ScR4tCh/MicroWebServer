/**
 * Android Micro Web Server
 * Copyright (C) 2011  ScR4tCh
 * Contact scr4tch@scr4tch.org

 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the Lesser GNU General Public License for more details.

 * You should have received a copy of the GNU Lesser General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package org.scratch.microwebserver;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Vector;

import org.scratch.microwebserver.http.MicroWebServer;
import org.scratch.microwebserver.http.MicroWebServerListener;
import org.scratch.microwebserver.util.MimeDetector;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

public class MicrowebserverService extends Service implements MicroWebServerListener
{
	// "tray" icon
	private NotificationManager mNotificationManager;
	private NotificationCompat.Builder notificationBuilder;
	private PendingIntent notificationIntent;
	private int APPICON_ID=2112;
	
	private MicroWebServer server;
	private final IBinder binder=new MicrowebserverServiceBinder();
	
	
	private Vector<ServiceListener> listeners = new Vector<ServiceListener>();
	
	private long startTime=0;
	
	private Vector<String> listeningAdresses = new Vector<String>();
	private LogEntryAdapter lea;
	
	protected class MicrowebserverServiceBinder extends Binder
	{
		public boolean startServer()
		{	
			try
			{
				server=new MicroWebServer();
				server.addMicroWebServerListener(MicrowebserverService.this);
				
				server.setMimeDetector(new MimeDetector(getResources().openRawResource(R.raw.magic)));
				
				listeningAdresses.clear();
				
				try
				{
					for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();)
					{
						NetworkInterface intf = en.nextElement();
						for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();)
						{
							InetAddress inetAddress = enumIpAddr.nextElement();
							if (!inetAddress.isLoopbackAddress())
							{
				                    listeningAdresses.add(inetAddress.getHostAddress());
				            }
				        }
					}
				}
				catch (SocketException ex)
				{
					if(server!=null)
					{
						log(server.fetchPreLogs());
						server.shutdown();
					}
					
					log(System.currentTimeMillis(),MicroWebServerListener.LOGLEVEL_ERROR,"","","Failed to start server:\n"+ex.getMessage());
				}
				
				log(server.fetchPreLogs());
				createNotification("serving ...");
				return true;
			}
			catch(Exception e)
			{
				if(server!=null)
				{
					log(server.fetchPreLogs());
					server.shutdown();
				}
				
				log(System.currentTimeMillis(),MicroWebServerListener.LOGLEVEL_ERROR,"","","Failed to start server:\n"+e.getMessage());
				
				return false;
			}
		}
		
		public void stopServer()
		{
			if(server!=null)
			{
				server.shutdown();
				server.removeMicroWebServerListener(MicrowebserverService.this);
			}
			
			mNotificationManager.cancel(APPICON_ID);
		}
		
		//TODO: Test behavior !
		public void restartServer()
		{
			stopServer();
			startServer();
		}
		
		public Vector<String> getListeningAdresses()
		{
			return listeningAdresses;
		}

		public boolean isServerUp()
		{
			if(server!=null)
				return server.isOnline();
			
			return false;
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

		public LogEntryAdapter getLogEntryAdapter()
		{
			return lea;
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
		
		lea=new LogEntryAdapter(startTime, 86400000); //24h history
	}
	
	@Override
	public IBinder onBind(Intent i)
	{
		return binder;
	}

	@Override
	public void log(long t,int lev,String client,String request,String msg)
	{
		LogEntry le = new LogEntry(t,lev,msg,request,client);
		for(int i=0;i<listeners.size();i++)
			listeners.elementAt(i).log(le);
	}
	
	public void log(Vector<LogEntry> vl)
	{
		for(int l=0;l<vl.size();l++)
		{
			LogEntry le = vl.elementAt(l);
			for(int i=0;i<listeners.size();i++)
				listeners.elementAt(i).log(le);
		}
	}

	@Override
	public void startUp(boolean started)
	{
		for(int i=0;i<listeners.size();i++)
			listeners.elementAt(i).startUp(started);
	}

	@Override
	public void shutDown(boolean shutDown)
	{
		for(int i=0;i<listeners.size();i++)
			listeners.elementAt(i).shutDown(shutDown);
	}

}
