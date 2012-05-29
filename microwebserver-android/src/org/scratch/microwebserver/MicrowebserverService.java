package org.scratch.microwebserver;

import java.io.IOException;

import org.scratch.microwebserver.http.MicroWebServer;
import org.scratch.microwebserver.http.WebConnectionListener;
import org.scratch.microwebserver.properties.PropertyNames;

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
	
	protected class MicrowebserverServiceBinder extends Binder
	{
		public boolean startServer()
		{
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
		}

		public boolean isServerUp()
		{
			if(server!=null)
				return server.isOnline();
			
			return false;
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
		
		mNotificationManager=(NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
		notificationBuilder = new NotificationCompat.Builder(this);
	}
	
	@Override
	public IBinder onBind(Intent i)
	{
		// TODO Auto-generated method stub
		return binder;
	}

	@Override
	public void log(int lev,String client,String request,String msg)
	{
		// TODO Auto-generated method stub
		
	}

}
