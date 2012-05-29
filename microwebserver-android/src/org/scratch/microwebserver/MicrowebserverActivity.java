/**
 * Android Micro Web Server
 * Copyright (C) 2011  ScR4tCh
 * Contact scr4tch@scr4tch.org

 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the Lesser GNU General Public License for more details.

 * You should have received a copy of the GNU Lesser General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package org.scratch.microwebserver;

import org.scratch.microwebserver.MicrowebserverService.MicrowebserverServiceBinder;
import org.scratch.microwebserver.properties.PropertyNames;
import org.scratch.microwebserver.properties.ServerProperties;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ImageView;
import android.widget.TextView;

public class MicrowebserverActivity extends Activity implements OnClickListener
{
    private ServiceConnection sconn;
    private MicrowebserverServiceBinder binder;

    
    private ImageView statusImage;
    private TextView statusText;
    private ListView logList;
    
    //private LogEntryAdapter lea = new LogEntryAdapter(ServerProperties.getInstance().getString(PropertyNames.LOGGERNAME)+".log.gz", 0, 0);
    
	@Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        connectService();
    }
    
    protected void connectService()
	{
		Intent serviceIntent=new Intent(this.getApplicationContext(),MicrowebserverService.class);
		startService(serviceIntent);

		sconn=new ServiceConnection()
		{

			@Override
			public void onServiceConnected(ComponentName name,IBinder service)
			{
				if(name.getClassName().equals("org.scratch.microwebserver.MicrowebserverService"))
				{
					binder=((MicrowebserverServiceBinder)service);
					
					setContentView(R.layout.main);
					statusImage=(ImageView)findViewById(R.id.StatusImage);
					statusImage.setOnClickListener(MicrowebserverActivity.this);
					statusText=(TextView)findViewById(R.id.statusText);
					logList=(ListView)findViewById(R.id.logList);
					//add adapter to lw !
					
					if(binder.isServerUp())
			    	{
			    		statusImage.post(
								new Runnable()
								{
									public void run()
									{
										statusText.setText("Started");
										statusImage.setImageResource(R.drawable.indicator_started);
									}
								}
						);
			    	}
			    	else
			    	{
			    		statusImage.post(
								new Runnable()
								{
									public void run()
									{
										statusText.setText("Stopped");
										statusImage.setImageResource(R.drawable.indicator_stopped);
									}
								}
						);
			    	}
				}
			}

			@Override
			public void onServiceDisconnected(ComponentName name)
			{
				if(name.getClassName().equals("comtec.tool.wifilogger.WifiLoggerService"))
				{
					// TODO: implement, do stuff here ?
				}
			}

		};

		if(!bindService(serviceIntent,sconn,BIND_AUTO_CREATE))
		{
			Log.e(PropertyNames.LOGGERNAME,"ARGH !!!!");
			// TOOOOOOOAST ! or a dialog ... whatever this SHOULD not happen at
			// all ...
		}
		else
		{

		}
	}
    
    @Override
    public void onDestroy()
    {
    	if(sconn!=null)
    		unbindService(sconn);
    	super.onDestroy();
    }

	@Override
	public void onClick(View v)
	{
		if(v.equals(statusImage) )
		{	
			if(!binder.isServerUp())
			{
				if(binder.startServer())
				{
					statusImage.post(
										new Runnable()
										{
											public void run()
											{
												statusText.setText("Started");
												statusImage.setImageResource(R.drawable.indicator_started);
											}
										}
									);
				}
			}
			else
			{
				binder.stopServer();
				statusText.setText("Stopped");
				statusImage.setImageResource(R.drawable.indicator_stopped);
			}
		}
	}

}