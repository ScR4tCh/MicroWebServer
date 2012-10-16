package org.scratch.microwebserver;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.scratch.microwebserver.MicrowebserverService.IncomingHandler;
import org.scratch.microwebserver.http.WebConnection;
import org.scratch.microwebserver.http.WebService;
import org.scratch.microwebserver.http.WebServiceException;
import org.scratch.microwebserver.http.WebServiceReply;
import org.scratch.microwebserver.http.WebServices;
import org.scratch.microwebserver.messagebinder.MessageTypes;

import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

public class RemoteWebService implements WebService
{
	private final Messenger remmsgs;
	private final Messenger srvmsgs;
	private final String packageName;
	private final Set<String> inmimetypes;
	private final String outmimetype;
	private final int methods;
	private final String groupalias;
	private final String name;
	private final IncomingHandler handler;
	
	//is the webserver permitted to invoke this service ???
	//NOTE : do we need a better security management here ?!?
	private boolean permit = false;
	
	private volatile WebServiceReply wsr;
	
		
	public RemoteWebService(String packageName,IncomingHandler incomingHandler, Messenger remmsgs,Messenger srvmsgs,String[] inmimetypes,String outmimetype,int methods,String groupalias,String name)
	{
		//TODO: check if the groupalias is acceptable (at least, there should be only ONE per connecting app !)
		//		 perhaps it will be a bit "dirty" to use full classname ut this would be relatively straight down the android road ;)
		this.packageName=packageName;
		
		this.remmsgs=remmsgs;
		this.srvmsgs=srvmsgs;
		this.inmimetypes=new HashSet<String>(Arrays.asList(inmimetypes));
		this.outmimetype=outmimetype;
		this.methods=methods;
		this.groupalias=groupalias;
		this.name=name;
		
		this.handler=incomingHandler;
	}
	
	public void permit(boolean b)
	{
		this.permit=b;
	}
	
	public String getPackageName()
	{
		return packageName;
	}

	@Override
	public String getUri()
	{
		//return "groupalias", too ?No need for it now ,but check it later !!h!!!
		return name;
	}

	@Override
	public WebServiceReply invoke(String[] ppc,int method,String mime,WebConnection wb) throws WebServiceException
	{
		if(!permit)
			throw new WebServiceException(403,"Not Permitted !");
		
		wsr=null;
		
		Bundle data = new Bundle();
		data.putStringArray("ppc",ppc);
		
		if(method==WebServices.METHOD_POST)
			data.putBoolean("post",true);
		else
			data.putBoolean("post",false);
		
		data.putString("mime",mime);
		if(method==WebServices.METHOD_POST)
		{
			try
			{
			 ByteArrayOutputStream baos = new ByteArrayOutputStream();
			 byte[] buffer = new byte[1024]; // Experiment with this value
			 int bytesRead;

			 while ((bytesRead = wb.getRawPostData().read(buffer)) != -1)
			 {
			    baos.write(buffer, 0, bytesRead);
			 }
			
			 //assume that android does some optimization when marshalling ...
			 data.putByteArray("postdata",baos.toByteArray());
			}catch(IOException ioe){/*TODO: HANDLE !*/}
		}
		
		//TODO:set cookies / session data ???
		
		Message im = new Message();
		
		int l= (int)(System.currentTimeMillis()/1000);
		
		final RemoteServiceCall call = new RemoteServiceCall(l,this,wb);
		
		im.arg1=l;
		handler.registerPendingReply(call);
		im.what=MessageTypes.MSG_INVOKE_SERVICE.ordinal();
		im.setData(data);
		im.replyTo=srvmsgs;
		
		try
		{
			remmsgs.send(im);
			try
			{
				System.err.println("WAITING : "+getUri());
				System.err.println(Thread.currentThread().toString());
				
				synchronized(Thread.currentThread())
				{
					Thread.currentThread().wait(2000);	//max 2 seconds :: TODO: make configurable !
					if(wsr==null)
					{
						throw new WebServiceException(500,"Timeout for service "+getUri());
					}
				}
			}
			catch(InterruptedException e)
			{
				System.err.println("ABORTED WAIT : "+getUri());
				handler.unregisterPendingReply(call);
				throw new WebServiceException(500,"Timeout for service "+getUri());
				
			} //wait two seconds
			
		}
		catch(RemoteException e)
		{
			//TODO: HANDLE !
		}
		System.err.println("POSSIBLY POSITIVE : "+getUri());
		
		// TODO Auto-generated method stub
		return wsr;
	}
	
	protected void setReply(WebServiceReply wsr)
	{
		this.wsr=wsr;
	}
	
	@Override
	public boolean acceptsMethod(int method)
	{
		return (methods&method)==method;
	}

	@Override
	public boolean acceptsMime(String mime)
	{
		return inmimetypes.contains(mime);
	}

	@Override
	public String getMime()
	{
		return outmimetype;
	}

	public boolean isPermitted()
	{
		return permit;
	}

}
