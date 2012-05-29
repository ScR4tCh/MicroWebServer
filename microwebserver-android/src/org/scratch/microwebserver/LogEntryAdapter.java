/**
 * Android Micro Web Server
 * Copyright (C) 2011  ScR4tCh
 * Contact scr4tch@scr4tch.org

 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the Lesser GNU General Public License for more details.

 * You should have received a copy of the GNU Lesser General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package org.scratch.microwebserver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.Iterator;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.zip.GZIPInputStream;

import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;

public class LogEntryAdapter implements ListAdapter
{
	private int minlevel=-1,maxlevel=-1;
	private long bwhistory;
	private long created;
	
	private String logfile;
	private PrintWriter logout;
	
	private ConcurrentMap<Long,ConcurrentMap<Integer,Vector<LogEntry>>> logs = new ConcurrentHashMap<Long,ConcurrentMap<Integer,Vector<LogEntry>>>();
	private Vector<LogEntry> wLogs = new Vector<LogEntry>();
	
	private Vector<DataSetObserver> observers = new Vector<DataSetObserver>();
	
	/**
	 * @param logfile	the logfile to read from (this is an gzipped file !)
	 * @param refreshRate	the refresh rate the logfile should be polled for logs (-1 for direct update)
	 * @param maxhistory  the maximum "backward history" in milliseconds [UTC]
	 */
	public LogEntryAdapter(String logfile,long refreshRate,long maxhistory)
	{
		created=System.nanoTime()/1000;
		bwhistory=created-maxhistory;
		
		this.logfile=logfile;
		
		File fi = new File(logfile);
		if(fi.exists())
		{
			try
			{
				BufferedReader bin = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(fi))));
			
				String line;
				/**
				 * LOG FORMAT:
				 * 
				 * time(ms)|level|msg|addr|req
				 * 
				 */
				while((line=bin.readLine())!=null)
				{
					String[] cols = line.split("\\|");
					if(cols.length>=5) //>= ??? possible extra info at the future ???
					{
						long t = Long.parseLong(cols[0]);
						
						//skip stuff we don't care about !!!
						if(t<bwhistory)
							continue;
						
						int l = Integer.parseInt(cols[1]);
						
						if(!logs.containsKey(t))
						{
							ConcurrentMap<Integer,Vector<LogEntry>> c = new ConcurrentHashMap<Integer,Vector<LogEntry>>();
							logs.put(t,c);
						}
						
						if(!logs.get(t).containsKey(l))
						{
							logs.get(t).put(l,new Vector<LogEntry>());
						}
						
						LogEntry le = new LogEntry(t,l,cols[2],cols[4],cols[3]);
						
						wLogs.add(le);
						logs.get(t).get(l).add(le);
					}
				}
			}
			catch(IOException ioe)
			{
				
			}
		}
			
	}
	
	public void log(LogEntry le)
	{
		//FIXME: consider time sorts !
		wLogs.add(le);		
	}
	
	private void informObservers(boolean justChange)
	{
		for(int i=0;i<observers.size();i++)
			if(justChange)
				observers.elementAt(i).onChanged();
			else
				observers.elementAt(i).onInvalidated();
	}
	
	public void filter(int minlevel,int maxlevel)
	{
		this.minlevel=minlevel;
		this.maxlevel=maxlevel;
		
		//TODO: rebuild wLogs !
		
		informObservers(false);
	}
	
	@Override
	public int getCount()
	{
		return wLogs.size();
	}
	

	@Override
	public Object getItem(int position)
	{
		return wLogs.elementAt(position);
	}

	@Override
	public long getItemId(int position)
	{
		return position;
	}

	@Override
	public int getItemViewType(int position)
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position,View convertView,ViewGroup parent)
	{
		// TODO Auto-generated method stub
		return null;
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
		return wLogs.size()==0;
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

	
}
