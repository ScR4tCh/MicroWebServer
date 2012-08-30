/**
 * Android Micro Web Server
 * Copyright (C) 2011  ScR4tCh
 * Contact scr4tch@scr4tch.org

 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the Lesser GNU General Public License for more details.

 * You should have received a copy of the GNU Lesser General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package org.scratch.microwebserver;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.scratch.microwebserver.data.DBManager;
import org.scratch.microwebserver.data.DatabaseManagerException;
import org.scratch.microwebserver.http.MicroWebServer;
import org.scratch.microwebserver.http.MicroWebServerListener;
import org.scratch.microwebserver.http.WebService;
import org.scratch.microwebserver.http.WebServices;
import org.scratch.microwebserver.messagebinder.MessageTypes;
import org.scratch.microwebserver.properties.PropertyNames;
import org.scratch.microwebserver.properties.ServerProperties;
import org.scratch.microwebserver.util.MimeDetector;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
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
	
	private ConnectionChangeReceiver networkChangedReceiver;
	
	private Vector<ServiceListener> listeners = new Vector<ServiceListener>();
	
	private long startTime=0;
	
	private Vector<String> listeningAdresses = new Vector<String>();
	private LogEntryAdapter lea;
	
	private WakeLock wakeLock;
	
	public class ConnectionChangeReceiver extends BroadcastReceiver
	{
		  @Override
		  public void onReceive( Context context, Intent intent )
		  {
		    
		    if(server!=null && server.isOnline())
		    {
		    	System.err.println("CONNECTION HAS CHANGED !");
		    	System.err.println("SERVER UP, REBIND PORTS !");
		    	
		    	listeningAdresses = new Vector<String>();
		    	Enumeration<NetworkInterface> en;
		    	
				try
				{
					en=NetworkInterface.getNetworkInterfaces();
								
			    	while( en.hasMoreElements())
			    	{
			    		NetworkInterface intf = en.nextElement();
			    	    for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();)
			    	    {
			    	    	InetAddress inetAddress = enumIpAddr.nextElement();
			    	    	if (!inetAddress.isLoopbackAddress())
			    	    		listeningAdresses.add(inetAddress.getHostAddress());
			    	    }
			    	}
			    	
			    	if(listeningAdresses.size()>0) //should always be true !
			    	{
				    	try
						{
							server.recreateSockets(listeningAdresses);
						}
						catch(IOException e)
						{
							//TODO: log !
							e.printStackTrace();
						}
			    	}
			    	else
			    	{
			    		System.err.println("NO ADDRESSES, REBIND FAILED !!!!");
			    		//TODO: log !
			    	}
				}
				catch(SocketException e1)
				{
					e1.printStackTrace();
					//TODO: log !
			    }
			}
			
		    
		  }
	}
	
	protected class MicrowebserverServiceBinder extends Binder
	{
		public void startServer()
		{	
			startUp(false);
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
				startUp(true);
				
				//wake lock  --- configurable ?
				wakeLock = ((PowerManager)getSystemService(Context.POWER_SERVICE)).newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"microwebserver wakelock");
				wakeLock.acquire();
				
				//aquire wifi lock (if configured)
				
				createNotification("serving ...");
			}
			catch(Exception e)
			{
				if(server!=null)
				{
					log(server.fetchPreLogs());
					server.shutdown();
				}
				
				log(System.currentTimeMillis(),MicroWebServerListener.LOGLEVEL_ERROR,"","","Failed to start server:\n"+e.getMessage());
				shutDown(true);
			}
		}
		
		public void stopServer()
		{
			if(server!=null)
			{
				server.shutdown();
				
				try
				{
					DBManager.getInstance(ServerProperties.getInstance().getString(PropertyNames.DATABASES_PATH.toString())).close();
				}
				catch(SQLException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				catch(DatabaseManagerException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				server.removeMicroWebServerListener(MicrowebserverService.this);
				
				if(wakeLock.isHeld())
					wakeLock.release();
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
		Context ctx = this.getApplicationContext();
		Intent intent = new Intent(ctx, MicrowebserverActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
		
		//Intent nIntent=new Intent(getBaseContext(),MicrowebserverActivity.class);
		//nIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

		//notificationIntent=PendingIntent.getActivity(this,0,nIntent,PendingIntent.FLAG_UPDATE_CURRENT);
		notificationIntent=PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_UPDATE_CURRENT);
		notificationBuilder.setTicker("service started").setSmallIcon(R.drawable.ic_launcher).setOngoing(true).setContentTitle("MicroWebServer").setContentText(msg).setContentIntent(notificationIntent);
		
		mNotificationManager.notify(APPICON_ID,notificationBuilder.getNotification());
	}
	
	public void onCreate()
	{
		super.onCreate();
		networkChangedReceiver = new ConnectionChangeReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(android.net.ConnectivityManager.CONNECTIVITY_ACTION);

		registerReceiver(networkChangedReceiver, filter);

		
		startTime=System.currentTimeMillis();
		
		mNotificationManager=(NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
		notificationBuilder = new NotificationCompat.Builder(this);
		notificationBuilder.setOngoing(true);
		notificationBuilder.setAutoCancel(false);
		
		lea=new LogEntryAdapter(startTime, 86400000); //24h history
		
		startForeground(APPICON_ID,notificationBuilder.getNotification());
		
		log(System.currentTimeMillis(),LOGLEVEL_INFO,"","","Service started");
	}
	
	public void onDestroy()
	{
		log(System.currentTimeMillis(),LOGLEVEL_INFO,"","","Service destroyed");
		unregisterReceiver(networkChangedReceiver);
		super.onDestroy();
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
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		 // We want this service to continue running until it is explicitly
		 // stopped, so return sticky.
		 return START_STICKY;
	}

	@Override
	public void startUp(boolean started)
	{
		log(System.currentTimeMillis(),LOGLEVEL_INFO,"","","Server startup");
		
		for(int i=0;i<listeners.size();i++)
			listeners.elementAt(i).startUp(started);
	}

	@Override
	public void shutDown(boolean shutDown)
	{
		log(System.currentTimeMillis(),LOGLEVEL_INFO,"","","Server shutdown");
		
		for(int i=0;i<listeners.size();i++)
			listeners.elementAt(i).shutDown(shutDown);
	}

	@Override
	public void recreate(boolean b,Vector<String> addr)
	{
		for(int i=0;i<listeners.size();i++)
			listeners.elementAt(i).recreate(b,addr);
	}

	@Override
	public void serviceAdded(WebService service)
	{
		for(int i=0;i<listeners.size();i++)
			listeners.elementAt(i).webServiceAdded(service);
	}

	
	
	
	//messenger
	
	private Map<Messenger,Set<RemoteWebService>> remoteServices = new HashMap<Messenger,Set<RemoteWebService>>();
	private final Messenger mMessenger = new Messenger(new IncomingHandler());
	
	class IncomingHandler extends Handler
	{
			private final Map<Messenger,Integer> resultsPending = new HashMap<Messenger,Integer>();
	        @Override
	        public void handleMessage(Message msg)
	        {
	        	if(msg.what==MessageTypes.MSG_REGISTER_SERVICE.ordinal())
	        	{
	        		
	        			/**
	                	 * The following attrs must be passed :
	                	 * 
	                	 * String[] input_mime - (accepted mimetypes)
	                	 * String output_mime  - (output mimetype)
	                	 * boolean postdata	   - (is postdata accepted ?)
	                	 * String group_alias  - "servicegroup" alias i.e. http://<serveraddr>/group_alias/....
	                	 * String service_name - service name i.e. http://<serveraddr>/group_alias/servicename
	                	 */
	                	
	                	Bundle b = msg.getData();
	                	
	                	if(b.containsKey("input_mime") && b.containsKey("output_mime") && b.containsKey("postdata") && b.containsKey("ppc"))
	                	{
	                		String[] input_mime = b.getStringArray("input_mime");
	                		String output_mime = b.getString("output_mime");
	                		boolean postdata = b.getBoolean("postdata");
	                		String group_alias = b.getString("group_alias");
	                		String service_name = b.getString("service_name");
	                		
	                		RemoteWebService rws = new RemoteWebService(this,mMessenger,input_mime,output_mime,postdata,group_alias,service_name);
	                		
	                		if(!remoteServices.containsKey(msg.replyTo))
	                		{
	                			remoteServices.put(msg.replyTo,new HashSet<RemoteWebService>());
	                		}
	                		
	                		WebServices.getInstance().registerService(group_alias,rws.getUri(),rws);
	                	}
	                	else
	                	{
	                		//handle ...
	                	}
	        	}
	        	else if(msg.what==MessageTypes.MSG_SERVICE_REPLY.ordinal())
	        	{
	        		
	        	}
	        	else
	        	{
	        		super.handleMessage(msg);
	        	}
	        }

			public void registerPendingReply(Messenger msgs,int hashCode)
			{
			}
	 }
}
