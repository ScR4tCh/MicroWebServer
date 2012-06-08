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

import com.viewpagerindicator.TitlePageIndicator;
import com.viewpagerindicator.TitlePageIndicator.IndicatorStyle;

import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
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
    private TextView statusText;
    
    //view pager views
    private ListView logList;
    
    private LogEntryAdapter lea;
    
    private ProgressDialog pd;
    
	@Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        stcont = getApplicationContext();
        
        logList = new ListView(this);
        logList.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);

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
					
					runOnUiThread(new Runnable(){public void run(){pd = ProgressDialog.show(MicrowebserverActivity.this, "Parsing Logs..", "stand by", true, false);}});
					lea = new LogEntryAdapter(binder.getLogFile(),binder.getServerStartTime(), 86400000); //24h history
					runOnUiThread(new Runnable(){public void run(){pd.dismiss();}});
					
					
					binder.registerServiceListener(MicrowebserverActivity.this);
					
					setContentView(R.layout.main);

					
					statusImage=(ImageView)findViewById(R.id.StatusImage);
					statusImage.setOnClickListener(MicrowebserverActivity.this);
					statusText=(TextView)findViewById(R.id.statusText);
					//logList=(ListView)findViewById(R.id.logList);
					logList.setAdapter(lea);
					
					logList.post(
									new Runnable()
									{
										public void run()
										{
											logList.setSelection(lea.getCount());
										}
									}
								);
					
					//pager
					pager = (ViewPager)findViewById(R.id.pager);
					pager.setAdapter(new APagerAdapter());
					
					//Bind the title indicator to the adapter
					TitlePageIndicator indicator = (TitlePageIndicator)findViewById(R.id.pagertitles);
					
					final float density = getResources().getDisplayMetrics().density;
					
					TypedValue tv = new TypedValue();
					
					//fgcol (line/footer)
					getApplicationContext().getTheme().resolveAttribute(android.R.attr.colorActivatedHighlight, tv, true);
			        indicator.setFooterColor(getResources().getColor(tv.resourceId));
			        
			        
			        indicator.setFooterLineHeight(1 * density); //1dp
			        indicator.setFooterIndicatorHeight(2 * density); //2dp
			        indicator.setFooterIndicatorStyle(IndicatorStyle.Underline);
			        
			        //textcol
			        TypedValue tv2 = new TypedValue();
			        getApplicationContext().getTheme().resolveAttribute(android.R.attr.colorActivatedHighlight, tv2, true);
			        indicator.setTextColor(getResources().getColor(tv2.resourceId));
			        
			        //selectedtextcol
			        TypedValue tv3 = new TypedValue();
			        getApplicationContext().getTheme().resolveAttribute(android.R.attr.colorForeground, tv3, true);
			        indicator.setSelectedColor(getResources().getColor(tv3.resourceId));
			        
			        indicator.setSelectedBold(true);
			        
					indicator.setViewPager(pager);
					
					
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
    	if(sconn!=null)
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
				startActivity(si);
				
				return true;
				
			case R.id.menu_logs:
				Intent li = new Intent(getApplicationContext(),LogSettingsActivity.class);
				li.putExtra(LogSettingsActivity.MINLEVEL,0);
				li.putExtra(LogSettingsActivity.MAXLEVEL,4);
				li.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(li);
				
				return true;
		}

		return false;
	}

	@Override
	public void log(LogEntry le)
	{
		if(lea!=null)
		{
			lea.log(le);
			
			this.runOnUiThread(
					new Runnable()
					{
						public void run()
						{
							lea.informObservers();
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
			pd = ProgressDialog.show(this, "Starting Server..", "stand by", true, false);
		else if(started && pd!=null)
			pd.dismiss();
	}

	@Override
	public void shutDown(boolean shutDown)
	{
		System.err.println("SERVER SHUTDOWN EVENT "+shutDown);
		
		if(!shutDown)
			runOnUiThread(new Runnable(){public void run(){pd = ProgressDialog.show(MicrowebserverActivity.this, "Stopping Server..", "stand by", true, false);}});
		else if(shutDown && pd!=null)
			pd.dismiss();
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

                	case 1:	 TextView tv = new TextView(getApplicationContext());
			                 tv.setText("WIP " + position);
			                 tv.setTextSize(30);
			                    
			                 ((ViewPager) collection).addView(tv,0);
			                    
			                 return tv;
			                 
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
            	  case 1:	return "Stuff";
              }
              
              return null;
          }

	}


}