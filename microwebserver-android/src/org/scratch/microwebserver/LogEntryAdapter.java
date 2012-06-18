/**
 * Android Micro Web Server
 * Copyright (C) 2011  ScR4tCh
 * Contact scr4tch@scr4tch.org

 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the Lesser GNU General Public License for more details.

 * You should have received a copy of the GNU Lesser General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package org.scratch.microwebserver;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

import org.scratch.microwebserver.data.DBManager;
import org.scratch.microwebserver.data.DatabaseManagerException;
import org.scratch.microwebserver.http.MicroWebServerListener;
import org.scratch.microwebserver.properties.PropertyNames;
import org.scratch.microwebserver.properties.ServerProperties;

import android.content.Context;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

public class LogEntryAdapter implements ListAdapter
{
	private int minlevel=-1,maxlevel=-1;
	private long maxhistory;
	private long createTime;
		
	private Vector<LogEntry> logs;
	private Vector<LogEntry> workingSet;
	
	private Vector<DataSetObserver> observers = new Vector<DataSetObserver>();
	
	/**
	 * @param logfile	the logfile to read from (this is an gzipped file !)
	 * @param refreshRate	the refresh rate the logfile should be polled for logs (-1 for direct update)
	 * @param maxhistory  the maximum "backward history" in milliseconds [UTC]
	 */
	public LogEntryAdapter(long createTime,long maxhistory)
	{
		this.createTime=createTime;
		this.maxhistory=maxhistory;
				
		readLogs();
					
	}
	
	private void readLogs()
	{
		
		try
		{
			logs=DBManager.getInstance(ServerProperties.getInstance().getString(PropertyNames.DATABASES_PATH.toString())).getLogs(createTime,maxhistory,minlevel,maxlevel);
			workingSet = new Vector<LogEntry>(logs);
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
		
	}
	
	public void filter(long maxhistory)
	{	
		this.maxhistory=maxhistory;
		
		readLogs();
		
		informObservers();
	}
	
	public void filter(int min,int max)
	{
		
		if(this.minlevel==min && this.maxlevel==max)
			return;
		
		this.minlevel=min;
		this.maxlevel=max;
		
		if(minlevel==-1 && maxlevel==-1)
		{
			workingSet=new Vector<LogEntry>(logs);
		}
		else
		{
			workingSet = new Vector<LogEntry>();
			
			for(int i=0;i<logs.size();i++)
			{
				int l = logs.elementAt(i).getLevel();
				if(minlevel!=-1 && maxlevel==-1)
				{
					if(l>=minlevel)
						workingSet.add(logs.elementAt(i));
				}
				else if(minlevel==-1 && maxlevel!=-1)
				{
					if(l<=maxlevel)
						workingSet.add(logs.elementAt(i));
				}
				else
				{
					if(l>=minlevel && l<=maxlevel)
						workingSet.add(logs.elementAt(i));
				}
			}
		}
		
		for(int i=0;i<observers.size();i++)
			observers.elementAt(i).onInvalidated();
		
	}
	
	private boolean checkLevel(LogEntry le)
	{
		if((le.getLevel()>=minlevel||minlevel==-1) && (le.getLevel()<=maxlevel||maxlevel==-1))
			return true;
		
		return false;
	}
	
	private boolean checkT(LogEntry le)
	{
		//TODO: "filter time"
		return true;
	}

	public void log(LogEntry le)
	{
		
		try
		{
			DBManager.getInstance(ServerProperties.getInstance().getString(PropertyNames.DATABASES_PATH.toString())).log(le);
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
		
		
		logs.add(le);
		
		if(checkLevel(le) && checkT(le))
		{
			workingSet.add(le);
			informObservers();
		}
	}
	

	public void informObservers()
	{
		for(int i=0;i<observers.size();i++)
				observers.elementAt(i).onChanged();
	}
	
	@Override
	public int getCount()
	{
		return workingSet.size();
	}
	

	@Override
	public Object getItem(int position)
	{
		return workingSet.elementAt(position);
	}

	@Override
	public long getItemId(int position)
	{
		return position;
	}

	@Override
	public int getItemViewType(int position)
	{
		return R.layout.logentry;
	}

	@Override
	public View getView(int position,View convertView,ViewGroup parent)
	{
		 View v = convertView;
	        
	        if (v == null)
	        {
	            LayoutInflater vi = (LayoutInflater)parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	            v = vi.inflate(R.layout.logentry, null);
	        }
	        	        
	        LogEntry le = workingSet.get(position);
	        
	        if (le != null)
	        {
	        	 	TextView logdate = (TextView) v.findViewById(R.id.logDate);
	                TextView logtext = (TextView) v.findViewById(R.id.logtext);
	                TextView logclient = (TextView) v.findViewById(R.id.logClient);
	                
	                ImageView level = (ImageView) v.findViewById(R.id.levelIco);
	                
	                if (logdate != null)
	                {
	                	logdate.setText(SimpleDateFormat.getInstance().format(new Date(le.getT())));
	                }
	                
	                if (logclient != null)
	                {
	                	logclient.setText(le.getRemoteAddress());
	                }
	                
	                if (logtext != null)
	                {
	                	logtext.setText(le.getMessage());
	                }       
	                
	                if(level!=null)
	                {
	                	
	                	switch(le.getLevel())
	                	{
	                		default:	break;
	                		case MicroWebServerListener.LOGLEVEL_DEBUG:	level.setImageResource(R.drawable.bug_icon);
	                													level.setColorFilter(0xFF0000FF,android.graphics.PorterDuff.Mode.MULTIPLY);
	                													logtext.setTextColor(0xFF0000FF);
	                													break;
	                		
	                		case MicroWebServerListener.LOGLEVEL_INFO:	level.setImageResource(R.drawable.info_icon);
												                		level.setColorFilter(0xFFFFFFFF,android.graphics.PorterDuff.Mode.MULTIPLY);
												                		logtext.setTextColor(0xFFFFFFFF);
																		break;
	                		
	                		case MicroWebServerListener.LOGLEVEL_NORMAL:	level.setImageResource(R.drawable.checkmark_icon);
												                		level.setColorFilter(0xFF00FF00,android.graphics.PorterDuff.Mode.MULTIPLY);
												                		logtext.setTextColor(0xFF00FF00);
																		break;
	                		
	                		case MicroWebServerListener.LOGLEVEL_WARN:	level.setImageResource(R.drawable.attention_icon);
												                		level.setColorFilter(0xFFFF9900,android.graphics.PorterDuff.Mode.MULTIPLY);
												                		logtext.setTextColor(0xFFFF9900);
																		break;
	                		
	                		
	                		case MicroWebServerListener.LOGLEVEL_ERROR:	level.setImageResource(R.drawable.delete_icon);
												                		level.setColorFilter(0xFFFF0000,android.graphics.PorterDuff.Mode.MULTIPLY);
												                		logtext.setTextColor(0xFFFF0000);
																		break;
	                			
	                	}
	                	
	                }           
	                
	        }
	        
	        return v;
	}

	@Override
	public int getViewTypeCount()
	{
		return 1;
	}

	@Override
	public boolean hasStableIds()
	{
		return true;
	}

	@Override
	public boolean isEmpty()
	{
		return workingSet.size()==0;
	}

	@Override
	public void registerDataSetObserver(DataSetObserver observer)
	{
		observers.add(observer);
	}

	@Override
	public void unregisterDataSetObserver(DataSetObserver observer)
	{
		observers.remove(observer);
	}

	@Override
	public boolean areAllItemsEnabled()
	{
		return false;
	}

	@Override
	public boolean isEnabled(int position)
	{
		//TODO: additional log info ?!?
		return false;
	}

	public int getMinLevel()
	{
		return minlevel;
	}

	public int getMaxLevel()
	{
		return maxlevel;
	}
}
