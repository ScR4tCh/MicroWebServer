package com.example.microwebserverremoteservicetest;


import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.scratch.microwebserver.messagebinder.DispatchingClient;
import org.scratch.microwebserver.messagebinder.MethodTypes;
import org.scratch.microwebserver.messagebinder.RegisterServiceException;
import org.scratch.microwebserver.messagebinder.ServiceCallException;
import org.scratch.microwebserver.messagebinder.ServiceCaller;
import org.scratch.microwebserver.messagebinder.ServiceConnectionException;

import android.os.Bundle;
import android.app.Activity;
import android.widget.TextView;

public class MainActivity extends Activity
{
	private DispatchingClient dpc;
	
	private String outmime = "application/json";
	
	TextView tv;
	
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv = (TextView)findViewById(R.id.text); 
         
        try
		{
        	dpc = new DispatchingClient(this,"test");
			dpc.registerCaller("settext",new ServiceCaller()
			{
				
				@Override
				public String[] getPossibleInputMimetypes()
				{
					return new String[]{"application/json","application/x-www-form-urlencoded"};
				}
				
				@Override
				public String getOutputMimeType()
				{
					return outmime;
				}
				
				public int getMethods()
				{
					return MethodTypes.POST.ordinal()|MethodTypes.GET.ordinal();
				}
				
				@Override
				public byte[] callService(int method,String mime,String[] uri,byte[] postdata) throws ServiceCallException
				{					
					if(method==MethodTypes.POST.ordinal() && postdata!=null)
					{
						if(mime.equals("application/json"))
						{
							try
							{
								final JSONObject jo=new JSONObject(new JSONTokener(new String(postdata)));
								
								System.err.println("GOT DATA : "+jo);
								
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
									return (new String("{\"HEUREKA\":true,\"text\":\""+jo.getString("text")+"\"}")).getBytes();
								}
							}
							catch(JSONException e)
							{
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						else if(mime.equals("application/x-www-form-urlencoded"))
						{				
							String text="";
							
							String[] kva = new String(postdata).split("&");
							for(int i=0;i<kva.length;i++)
							{
								String[] kv = kva[i].split("=");
								if(kv.length==2 && kv[0].equals("text"))
									text=kv[1];
							}
							
							final String t = text;
							
							runOnUiThread(new Runnable()
							{
									public void run()
									{
										tv.setText(t);
									}	
									
							});
							
							String r = "<HTML><HEAD><TITLE>SET TEXT</TITLE></HEAD><BODY>";
							
							r+="<form action=\"test/settext\" method=\"post\"><input name=\"text\" type=\"text\" value=\""+text+"\"size=\"30\"/><br/><input type=\"submit\" name=\"submit\"/></form>";
							
							r+="</BODY></HTML>";
							
							outmime="text/html";
							
							return r.getBytes();
						}
						else
							return null;
					}
					else if(method==MethodTypes.GET.ordinal())
					{
						String r = "<HTML><HEAD><TITLE>SET TEXT</TITLE></HEAD><BODY>";
						
						r+="<form action=\"test/settext\" method=\"post\"><input name=\"text\" type=\"text\" size=\"30\"/><br/><input type=\"submit\" name=\"submit\"/></form>";
						
						r+="</BODY></HTML>";
						
						outmime="text/html";
						
						return r.getBytes();
					}
					else
					{
						System.err.println("POSTDATA IS NULL !");
					}
					
					return null;
				}
			});
		}
		catch(RegisterServiceException e)
		{
			e.printStackTrace();
		}
        catch(ServiceConnectionException sce)
        {
        	sce.printStackTrace();
        }
    }
   
}
