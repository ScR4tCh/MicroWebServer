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
	private final Messenger msgs;
	private final Set<String> inmimetypes;
	private final String outmimetype;
	private final boolean post;
	private final String groupalias;
	private final String name;
	private final IncomingHandler handler;
	
	private volatile WebServiceReply wsr;
	
	
	public Object replyLock = new Object();
	
	public RemoteWebService(IncomingHandler handler, Messenger msgs,String[] inmimetypes,String outmimetype,boolean post,String groupalias,String name)
	{
		//TODO: check if the groupalias is acceptable (at least, there should be only ONE per connecting app !)
		
		this.msgs=msgs;
		this.inmimetypes=new HashSet<String>(Arrays.asList(inmimetypes));
		this.outmimetype=outmimetype;
		this.post=post;
		this.groupalias=groupalias;
		this.name=name;
		
		this.handler=handler;
	}

	@Override
	public String getUri()
	{
		//return "groupalias", too ?
		return name;
	}

	@Override
	public WebServiceReply invoke(String[] ppc,int method,String mime,WebConnection wb) throws WebServiceException
	{
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
		
		//better do it threaded ?
		Message im = new Message();
		handler.registerPendingReply(msgs,hashCode());
		im.what=MessageTypes.MSG_INVOKE_SERVICE.ordinal();
		im.setData(data);
		
		try
		{
			msgs.send(im);
			try
			{
				replyLock.wait(2000);
			}
			catch(InterruptedException e)
			{
				// TODO: Handle !
				e.printStackTrace();
			} //wait two seconds
			
		}
		catch(RemoteException e)
		{
			//TODO: HANDLE !
		}
		
		
		// TODO Auto-generated method stub
		return wsr;
	}
	
	public void setReply(WebServiceReply wsr)
	{
		this.wsr=wsr;
	}

	@Override
	public boolean acceptsMethod(int method)
	{
		if(post && method==WebServices.METHOD_POST)
			return true;
		else if(!post && method==WebServices.METHOD_POST)
			return false;
		
		//always accept GET ?
		
		return true;
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

}
