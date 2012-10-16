/**
 * Android Micro Web Server
 * Copyright (C) 2011  ScR4tCh
 * Contact scr4tch@scr4tch.org

 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the Lesser GNU General Public License for more details.

 * You should have received a copy of the GNU Lesser General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package org.scratch.microwebserver;

import java.util.Arrays;
import java.util.Vector;

import org.scratch.microwebserver.MicrowebserverService.MicrowebserverServiceBinder;
import org.scratch.microwebserver.http.WebService;
import org.scratch.microwebserver.properties.PropertyNames;

import com.viewpagerindicator.TitlePageIndicator;
import com.viewpagerindicator.TitlePageIndicator.IndicatorStyle;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;

import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcelable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;
import android.widget.ImageView;
import android.widget.TextView;

public class MicrowebserverActivity extends FragmentActivity implements OnClickListener,ServiceListener
{
    private ServiceConnection sconn;
    private MicrowebserverServiceBinder binder;

    private static Context stcont;
    
    private ViewPager pager;
    private ImageView statusImage;
    private TextView statusText,socketInfo;
    
    //view pager views
    //logs (main)
    private ListView logList;
    //services
    private ListView serviceList;
    
    
    private LogEntryAdapter lea;
    private ServiceAdapter sea;
        
	@Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        stcont = getApplicationContext();
        
        logList = new ListView(this);
        logList.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        
        serviceList = new ListView(this);

        connectService();
    }
    
    protected void connectService()
	{
    	Intent serviceIntent=new Intent(this.getApplicationContext(),MicrowebserverService.class);
		
		sconn=new ServiceConnection()
		{

			@Override
			public void onServiceConnected(ComponentName name,IBinder service)
			{
				if(name.getClassName().equals("org.scratch.microwebserver.MicrowebserverService"))
				{
					binder=((MicrowebserverServiceBinder)service);
										
					//global adapter ;)
					lea = binder.getLogEntryAdapter();					
					
					binder.registerServiceListener(MicrowebserverActivity.this);
					
					setContentView(R.layout.main);

					
					statusImage=(ImageView)findViewById(R.id.StatusImage);
					statusImage.setOnClickListener(MicrowebserverActivity.this);
					statusText=(TextView)findViewById(R.id.statusText);
					socketInfo=(TextView)findViewById(R.id.socketInfo);
					//logList=(ListView)findViewById(R.id.logList);
					logList.setAdapter(lea);
					
					runOnUiThread(
									new Runnable()
									{
										public void run()
										{
											logList.setSelection(lea.getCount());
										}
									}
								);
					
					Vector<WebService> wss = new Vector<WebService>();
					//add remote AND local (TODO !)
					wss.addAll(binder.getRegisteredRemoteWebServices());
					
					sea = new ServiceAdapter(getPackageManager(),wss);
					serviceList.setAdapter(sea);
					
					//pager
					pager = (ViewPager)findViewById(R.id.pager);
					pager.setAdapter(new APagerAdapter());
					
					//Bind the title indicator to the adapter
					TitlePageIndicator indicator = (TitlePageIndicator)findViewById(R.id.pagertitles);
					
					final float density = getResources().getDisplayMetrics().density;
					
					TypedValue tv = new TypedValue();
					
					
					try
					{
						//fgcol (line/footer)
						getApplicationContext().getTheme().resolveAttribute(android.R.attr.colorActivatedHighlight, tv, true);
						indicator.setFooterColor(getResources().getColor(tv.resourceId));
						//logList.getDivider().setColorFilter(getResources().getColor(tv.resourceId),Mode.MULTIPLY);
					}
					catch(Resources.NotFoundException rnfe){}
			        
			        indicator.setFooterLineHeight(1 * density); //1dp
			        indicator.setFooterIndicatorHeight(2 * density); //2dp
			        indicator.setFooterIndicatorStyle(IndicatorStyle.Underline);
			        
			        //textcol
			        TypedValue tv2 = new TypedValue();
			        
			        try
			        {
			        	getApplicationContext().getTheme().resolveAttribute(android.R.attr.colorActivatedHighlight, tv2, true);
			        	indicator.setTextColor(getResources().getColor(tv2.resourceId));
			        }
					catch(Resources.NotFoundException rnfe){}
			        
					
			        //selectedtextcol
			        TypedValue tv3 = new TypedValue();
			        getApplicationContext().getTheme().resolveAttribute(android.R.attr.colorForeground, tv3, true);
			        indicator.setSelectedColor(getResources().getColor(tv3.resourceId));
			        
			        indicator.setSelectedBold(true);
			        
					indicator.setViewPager(pager);
					
					
					if(binder.isServerUp())
			    	{
			    		runOnUiThread(
								new Runnable()
								{
									public void run()
									{
										statusText.setText("Started");
										socketInfo.setText(Arrays.toString(binder.getListeningAdresses().toArray()));
										statusImage.setImageResource(R.drawable.indicator_started);
									}
								}
						);
			    	}
			    	else
			    	{
			    		runOnUiThread(
								new Runnable()
								{
									public void run()
									{
										statusText.setText("Stopped");
										socketInfo.setText("");
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
		
		startService(serviceIntent);

		if(!bindService(serviceIntent,sconn,BIND_AUTO_CREATE))
		{
			Log.e(PropertyNames.LOGGERNAME.toString(),"ARGH !!!!");
			// TOOOOOOOAST ! or a dialog ... whatever this SHOULD not happen at
			// all ...
		}
		else
		{

		}
	}
    
    @Override
    public void onResume()
    {
    	if(sconn==null)
    		connectService();
    	
    	super.onResume();
    }
    
    @Override
    public void onDestroy()
    {
    	if(sconn!=null && binder!=null)
    	{
    		binder.unregisterServiceListener(this);
    		unbindService(sconn);
    	}
    	
    	super.onDestroy();
    }

	@Override
	public void onClick(View v)
	{
		if(v.equals(statusImage) )
		{	
			if(!binder.isServerUp())
			{
				binder.startServer();
			}
			else
			{
				binder.stopServer();
			}
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater=getMenuInflater();
		inflater.inflate(R.menu.menu,menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		// Handle item selection
		switch(item.getItemId())
		{
			case R.id.menu_settings:
				Intent si = new Intent(getApplicationContext(),MicroWebServerSettingsActivity.class);
				si.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivityForResult(si,0);
				
				return true;
				
			case R.id.menu_logs:
				Intent li = new Intent(getApplicationContext(),LogSettingsActivity.class);
				li.putExtra(LogSettingsActivity.MINLEVEL,lea.getMinLevel());
				li.putExtra(LogSettingsActivity.MAXLEVEL,lea.getMaxLevel());
				li.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivityForResult(li,LogSettingsActivity.SET);
				
				return true;
		}

		return false;
	}

	@Override
	public void log(final LogEntry le)
	{
		if(lea!=null)
		{
			this.runOnUiThread(
					new Runnable()
					{
						public void run()
						{
							lea.log(le);
							//logList.requestLayout();
							//lea.informObservers();
						}
					}
			);
			
		}
	}
	
	 public static Context getAppContext()
	 {
	        return stcont;
	  }

	@Override
	public void startUp(boolean started)
	{
		if(!started)
		{
			runOnUiThread(new Runnable()
						  {
								public void run()
								{
									statusText.setText("Starting server ...");
									socketInfo.setText("");
									statusImage.setClickable(false);
									statusImage.setImageResource(R.drawable.indicator_idle);
								}
						  }
						);
		}
		else if(started)
		{
			runOnUiThread(new Runnable()
						  {
								public void run()
								{
									statusText.setText("Started");
									socketInfo.setText(Arrays.toString(binder.getListeningAdresses().toArray()));
									statusImage.setClickable(true);
									statusImage.setImageResource(R.drawable.indicator_started);
								}
						  }
						);
		}
	}

	@Override
	public void shutDown(boolean shutDown)
	{
		System.err.println("SERVER SHUTDOWN EVENT "+shutDown);
		
		if(!shutDown)
		{
			runOnUiThread(new Runnable()
						  {
								public void run()
								{
									statusText.setText("Stopping server ...");
									socketInfo.setText("");
									statusImage.setClickable(false);
									statusImage.setImageResource(R.drawable.indicator_idle);
								}
						  }
						);
		}
		else if(shutDown)
		{
			runOnUiThread(new Runnable()
						  {
								public void run()
								{
									statusText.setText("Stopped");
									socketInfo.setText("");
									statusImage.setClickable(true);
									statusImage.setImageResource(R.drawable.indicator_stopped);
								}
						 }
		);
		}
	}
	
	@Override
	public void recreate(boolean b,final Vector<String> addr)
	{
		
		if(b)
		{
			runOnUiThread(
					new Runnable()
					{
						public void run()
						{
							statusText.setText("Rebinding ...");
							socketInfo.setText("");
							statusImage.setClickable(false);
							statusImage.setImageResource(R.drawable.indicator_idle);
						}
					}
				);
		}
		else
		{
			runOnUiThread(
					new Runnable()
					{
						public void run()
						{
							statusText.setText("Started");
							socketInfo.setText(Arrays.toString(addr.toArray()));
							statusImage.setImageResource(R.drawable.indicator_started);
							statusImage.setClickable(true);
						}
					}
				);
		}
	}
	
	private class APagerAdapter extends PagerAdapter
	{

		@Override
		public int getCount()
		{
			return 2;
		}
		
		@Override
        public Object instantiateItem(View collection, int position)
		{
                switch(position)
                {
                	case 0:	 ((ViewPager) collection).addView(logList,0);
                			 return logList;

                	case 1:	 ((ViewPager) collection).addView(serviceList,0);
       			             return serviceList;
			                 
			        default: System.err.println("WAHT THE FUCK ???");
			        		 break;

                }
                
                return null;
        }


		  @Override
          public boolean isViewFromObject(View view, Object object)
		  {
			  return view.equals(object);
//                  return (view instanceof TextView)||view.equals(logList);
          }
		  
		  @Override
		  public void destroyItem(View collection, int position, Object view)
		  {
		        ((ViewPager) collection).removeView((View) view);
		  }


          
      /**
       * Called when the a change in the shown pages has been completed.  At this
       * point you must ensure that all of the pages have actually been added or
       * removed from the container as appropriate.
       * @param container The containing View which is displaying this adapter's
       * page views.
       */
          @Override
          public void finishUpdate(View arg0)
          {
        	  
          }
          

          @Override
          public void restoreState(Parcelable arg0, ClassLoader arg1)
          {
        	  
          }

          @Override
          public Parcelable saveState()
          {
                  return null;
          }

          @Override
          public void startUpdate(View arg0)
          {
        	  
          }
          
          @Override
          public CharSequence getPageTitle(int position)
          {
              switch(position)
              {
            	  case 0:	return "Logs";
            	  case 1:	return "Services";
              }
              
              return null;
          }

	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{	
		if(resultCode==MicroWebServerSettingsActivity.OK)
		{
			if(binder.isServerUp())
				binder.restartServer();
		}
		
		if(requestCode==LogSettingsActivity.SET && resultCode==LogSettingsActivity.SET)
		{	

			int minlevel = data.getIntExtra(LogSettingsActivity.MINLEVEL,-1);
			int maxlevel = data.getIntExtra(LogSettingsActivity.MAXLEVEL,-1);
			
			System.err.println("loglevel filter "+minlevel+" -> "+maxlevel);
			
			
			lea.filter(minlevel,maxlevel);
		}
	}

	@Override
	public void webServiceAdded(final WebService service)
	{
		this.runOnUiThread(
				new Runnable()
				{
					public void run()
					{
						sea.addService(service);
					}
				}
		);
	}
	
	@Override
	public void webServiceRemoved(final WebService service)
	{
		this.runOnUiThread(
				new Runnable()
				{
					public void run()
					{
						sea.removeService(service);
					}
				}
		);
	}


}