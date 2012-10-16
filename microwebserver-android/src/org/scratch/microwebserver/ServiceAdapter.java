package org.scratch.microwebserver;

import java.util.Vector;

import org.scratch.microwebserver.http.WebService;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

public class ServiceAdapter implements ListAdapter
{
	private Vector<WebService> registeredServices = new Vector<WebService>();
	
	private PackageManager pm;
	private Vector<DataSetObserver> observers = new Vector<DataSetObserver>();
	
	
	public ServiceAdapter(PackageManager pm,Vector<WebService> registeredServices)
	{
		this.pm=pm;
		this.registeredServices.addAll(registeredServices);
	}
	
	public ServiceAdapter(PackageManager pm)
	{
		this.pm=pm;
	}
	
	
	@Override
	public int getCount()
	{
		return registeredServices.size();
	}
	
	@Override
	public Object getItem(int arg0)
	{
		return registeredServices.elementAt(arg0);
	}
	
	@Override
	public long getItemId(int position)
	{
		return position;
	}
	
	@Override
	public int getItemViewType(int position)
	{
		return R.layout.serviceitem;
	}
	
	@Override
	public View getView(int position,View convertView,ViewGroup parent)
	{
		 View v = convertView;
	        
	        if (v == null)
	        {
	            LayoutInflater vi = (LayoutInflater)parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	            v = vi.inflate(R.layout.serviceitem, null);
	        }
	        
	        ImageView appIcon = (ImageView) v.findViewById(R.id.appIcon);
        	
    	 	TextView serviceTx = (TextView) v.findViewById(R.id.serviceTx);
            TextView appTx = (TextView) v.findViewById(R.id.appTx);
            TextView descTx = (TextView) v.findViewById(R.id.descTx);
            
            CheckBox enableCk = (CheckBox) v.findViewById(R.id.enableCk);
	        
	        WebService ws = registeredServices.elementAt(position);
	        
	        if(ws instanceof RemoteWebService)
	        {
	        	
		        final RemoteWebService rws = (RemoteWebService)ws;
		               
		        if (rws != null)
		        {
		        		 
		                if (rws.getUri() != null)
		                {
		                	serviceTx.setText(rws.getUri());
		                }
		                
		                if(rws.isPermitted())
		                {
		                	enableCk.setChecked(true);
		                }
		                else
		                {
		                	enableCk.setChecked(false);
		                }
		                
		                enableCk.setOnCheckedChangeListener(new OnCheckedChangeListener()
						{
							@Override
							public void onCheckedChanged(CompoundButton buttonView,boolean isChecked)
							{
								rws.permit(isChecked);
							}
						});
		                
		                ApplicationInfo appinfo=null;
						try
						{
							appinfo=pm.getApplicationInfo(rws.getPackageName(),0);
							System.err.println(rws.getPackageName());
						}
						catch(NameNotFoundException e)
						{
							//HANDLE!
							e.printStackTrace();
						}
		                
		                if (appinfo != null)
		                {
		                	appTx.setText(pm.getApplicationLabel(appinfo));
		                	appIcon.setImageDrawable(pm.getApplicationIcon(appinfo));
		                }
		                else
		                {
		                	//SET SOME SORT OF DEFAULT ;)
		                }
		                
		                //dec is empty for now !
		                
		        }
	        }
	        else
	        {
	        	//local services ....
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
		return false;
	}
	
	@Override
	public boolean isEmpty()
	{
		return registeredServices.size()==0;
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
		return false;
	}

	public void addService(WebService service)
	{
		registeredServices.add(service);
		informObservers();
	}
	
	public void removeService(WebService service)
	{
		registeredServices.remove(service);
		informObservers();
	}
	
	public void informObservers()
	{
		for(int i=0;i<observers.size();i++)
				observers.elementAt(i).onChanged();
	}
}
