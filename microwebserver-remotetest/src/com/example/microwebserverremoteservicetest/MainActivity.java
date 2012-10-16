package com.example.microwebserverremoteservicetest;


import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.scratch.microwebserver.messagebinder.DispatchingClient;
import org.scratch.microwebserver.messagebinder.ServiceCallException;
import org.scratch.microwebserver.messagebinder.ServiceCaller;

import android.os.Bundle;
import android.app.Activity;
import android.widget.TextView;

public class MainActivity extends Activity
{
	private DispatchingClient dpc;
	
	TextView tv;
	
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv = (TextView)findViewById(R.id.text); 
        
        dpc = new DispatchingClient(this,"test");
        
        
        dpc.registerCaller("settext",new ServiceCaller()
		{
			
			@Override
			public String[] getPossibleInputMimetypes()
			{
				return new String[]{"application/json"};
			}
			
			@Override
			public String getOutputMimeType()
			{
				return "application/json";
			}
			
			@Override
			public byte[] callService(String[] uri,byte[] postdata) throws ServiceCallException
			{
				if(postdata!=null)
				{
					try
					{
						final JSONObject jo=new JSONObject(new JSONTokener(new String(postdata)));
						if(jo.has("text"))
						{
							runOnUiThread(new Runnable()
										  {
												public void run()
												{
													try
													{
														tv.setText(jo.getString("text"));
													}
													catch(JSONException e)
													{
														tv.setText("ERROR: "+e.getMessage());
													}
												}
										  });
						}
					}
					catch(JSONException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				}
				
				return null;
			}
		});
    }
   
}
