package org.scratch.microwebserver.messagebinder;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.widget.Toast;

public class DispatchingClient
{
	private final Messenger mMessenger=new Messenger(new IncomingHandler());
	private Messenger mService=null;
	private final String group_alias;
	
	private final Context context;
	private final Map<String,ServiceCaller> callers = new HashMap<String,ServiceCaller>();
	private final Map<String,ServiceCaller> pendingCallers = new HashMap<String,ServiceCaller>();
	
	boolean mIsBound=false;

	public DispatchingClient(final Context context,final String group_alias) throws ServiceConnectionException
	{
		// TODO: check if Context was set !
		this.context=context;
		this.group_alias=group_alias;
		bindService();
	}

	private void bindService() throws ServiceConnectionException
	{
		// Establish a connection with the service. We use an explicit
		// class name because there is no reason to be able to let other
		// applications replace our component.

		//TODO: check flags !!!
		if(context.bindService(IntentFactory.createIntent(),mConnection,Context.BIND_AUTO_CREATE))
			mIsBound=true;
		else
			throw new ServiceConnectionException("could not bind service via "+IntentFactory.createIntent());
	}

	void doUnbindService()
	{
		if(mIsBound)
		{
			// If we have received the service, and hence registered with
			// it, then now is the time to unregister.
			if(mService!=null)
			{
				try
				{
					Message msg=Message.obtain(null,MessageTypes.MSG_UNREGISTER_SERVICE.ordinal());
					msg.replyTo=mMessenger;
					mService.send(msg);
				}
				catch(RemoteException e)
				{
					// There is nothing special we need to do if the service
					// has crashed.
				}
			}

			// Detach our existing connection.
			context.unbindService(mConnection);
			mIsBound=false;
		}
	}

	public final Messenger getMessenger()
	{
		return mService;
	}

	public final Messenger getReplyMessenger()
	{
		return mMessenger;
	}

	public final String getPackageName()
	{
		return context.getPackageName();
	}

	private ServiceConnection mConnection=new ServiceConnection()
	{
		public void onServiceConnected(ComponentName className,IBinder service)
		{
			// This is called when the connection with the service has been
			// established, giving us the service object we can use to
			// interact with the service. We are communicating with our
			// service through an IDL interface, so get a client-side
			// representation of that from the raw service object.
			DispatchingClient.this.mService=new Messenger(service);
			// As part of the sample, tell the user what happened.
			Toast.makeText(DispatchingClient.this.context,"remote service connected",Toast.LENGTH_SHORT).show();
			
			Iterator<String> scnamei = pendingCallers.keySet().iterator();
			while(scnamei.hasNext())
			{
				String name = scnamei.next();
				ServiceCaller sc = pendingCallers.remove(name);
				
				try
				{
					int sid=ServiceManager.registerService(DispatchingClient.this,sc.getMethods(),
							sc.getPossibleInputMimetypes(),sc.getOutputMimeType(),
							group_alias,name);
					
					callers.put(name,sc);
				}
				catch(RemoteException re)
				{
					re.printStackTrace();
					//TODO: handle ! Best would be to inform "creator context" over some sort of callback or listener
				}
			}
		}

		public void onServiceDisconnected(ComponentName className)
		{
			// This is called when the connection with the service has been
			// unexpectedly disconnected -- that is, its process crashed.
			mService=null;

			// As part of the sample, tell the user what happened.
			// Toast.makeText(Binding.this,
			// R.string.remote_service_disconnected,
			// Toast.LENGTH_SHORT).show();
		}
	};

	class IncomingHandler extends Handler
	{
		@Override
		public void handleMessage(Message msg)
		{
			Bundle mb=msg.getData();
			
			if(msg.what==MessageTypes.MSG_INVOKE_SERVICE.ordinal())
			{
				Message repl = new Message();
				
				if(mb.containsKey(InvocationFieldTypes.INVOKE_URI.getFieldName()))
				{
					String[] uri = mb.getStringArray(InvocationFieldTypes.INVOKE_URI.getFieldName());
					
					//why this second check ?!?
					if(uri.length>=2 && group_alias.equals(uri[0]))
					{
						if(callers.containsKey(uri[1]))
						{							
							try
							{
								byte[] pd = mb.getByteArray(InvocationFieldTypes.INVOKE_POSTDATA.getFieldName());								
								byte[] res =callers.get(uri[1]).callService(mb.getInt(InvocationFieldTypes.INVOKE_METHOD.getFieldName()),mb.getString(InvocationFieldTypes.INVOKE_MIME.getFieldName()),uri,pd);
								
								repl.what=MessageTypes.MSG_SERVICE_REPLY.ordinal();
								Bundle b=new Bundle();
								b.putByteArray(ReplyFieldTypes.RESULTDATA.getFieldName(),res);
								
								repl.arg1=msg.arg1;
								repl.setData(b);
								repl.arg2=(int)(System.currentTimeMillis()/1000);
							}
							catch(ServiceCallException e)
							{
								//SET ERROR REPLY !!!
								e.printStackTrace();
							}
						}
						else
						{
							System.err.println(uri[1]+" NOT IN CALLERS !");
							
							//SET ERROR REPLY !!!
						}
						
					}
					else
					{
						//SET ERROR REPLY !!!
					}
					
				}
				else
				{
					//SET ERROR REPLY !!!
				}
				
				try
				{
					mService.send(repl);
				}
				catch(RemoteException e)
				{
					//BAD ! BAD ! BAD !
					e.printStackTrace();
				}
			}
			else
			{
				//should not happen at all (the dispatcher should take care !!!!)
				super.handleMessage(msg);
			}
		}
	}
	
	
	public synchronized void registerCaller(String name,ServiceCaller sc) throws RegisterServiceException
	{
		if(!mIsBound || mService==null)
		{
			pendingCallers.put(name,sc);
			return;
		}
		
		// We want to monitor the service for as long as we are
		// connected to it.
		try
		{
			int sid=ServiceManager.registerService(DispatchingClient.this,sc.getMethods(),
					sc.getPossibleInputMimetypes(),sc.getOutputMimeType(),
					group_alias,name);
			
			if(sid==-1)
			{
				throw new RegisterServiceException("could not register service : dispatcher is NULL");	//TODO: be more descriptive ;)
			}
			
			callers.put(name,sc);
		}
		catch(RemoteException e)
		{
			// In this case the service has crashed before we could even
			// do anything with it; we can count on soon being
			// disconnected (and then reconnected if it can be restarted)
			// so there is no need to do anything here.

			throw new RegisterServiceException("could not register service : "+e.getMessage());
		}

	}

}
