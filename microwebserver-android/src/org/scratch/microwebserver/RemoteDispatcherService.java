package org.scratch.microwebserver;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.scratch.microwebserver.messagebinder.MessageTypes;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

public class RemoteDispatcherService extends Service
{
		//messenger
	
		private static final Messenger mMessenger = new Messenger(new IncomingHandler());
		private static Messenger serverMessenger;
		
		//just disptach !
		static class IncomingHandler extends Handler
		{
				private final Map<Messenger,Set<Integer>> remoteClients = new HashMap<Messenger,Set<Integer>>();
				
		        @Override
		        public void handleMessage(Message msg)
		        {
		        	if(msg.what==MessageTypes.MSG_SERVER_HELLO.ordinal())
		        	{	
		        		if(serverMessenger==null) // there can only be on mcLeod
		        		{
		        			serverMessenger=msg.replyTo;
		        		}
		        		else
		        		{
		        			//just drop ! or notify sender ??
		        			super.handleMessage(msg);
		        		}
		        	}
		        	else if(msg.what==MessageTypes.MSG_SERVER_BYE.ordinal() && msg.replyTo.equals(serverMessenger))
		        	{
		        		serverMessenger=null;
		        		super.handleMessage(msg);	//drop !
		        	}
		        	else if(msg.what==MessageTypes.MSG_REGISTER_SERVICE.ordinal())
		        	{
		        		if(!remoteClients.containsKey(msg.replyTo))
		        			remoteClients.put(msg.replyTo,new HashSet<Integer>());	//TODO: arg1 should be some kind of "unique" identifier (perhaps some sort of digest hashing ;) )

		        		remoteClients.get(msg.replyTo).add(msg.arg1);
		        		dispatch(msg);
		        	}
		        	else if(msg.what==MessageTypes.MSG_UNREGISTER_SERVICE.ordinal())
		        	{
		        		if(remoteClients.containsKey(msg.replyTo))
		        		{
		        			remoteClients.get(msg.replyTo).remove(msg.arg1);
		        			if(remoteClients.get(msg.replyTo).size()==0)
		        				remoteClients.remove(msg.replyTo);
		        		}
		        		
		        		dispatch(msg);
		        	}
		        	else
		        	{
		        		dispatch(msg);
		        	}
		        }
		        
		        private synchronized void dispatch(final Message msg)
		        {
		        	
		        	if(serverMessenger!=null && msg.replyTo==null || (msg.replyTo!=null && !msg.replyTo.equals(serverMessenger)) )
	        		{
						try
						{
							//always reply to the dispatcher (me ^^)
				        	msg.replyTo=mMessenger;
							serverMessenger.send(msg);
						}
						catch(RemoteException e)
						{
							// HANDLE !!!
							// inform remote callers that the server is not running !
							e.printStackTrace();
						}
	        		}
	        		else if(serverMessenger!=null && msg.replyTo!=null && msg.replyTo.equals(serverMessenger))
	        		{
	        			//always reply to the dispatcher (me ^^)
			        	msg.replyTo=mMessenger;
			        	
	        			Iterator<Messenger> msgi = remoteClients.keySet().iterator();
	        			while(msgi.hasNext())
	        			{
	        				try
	        				{
	        					msgi.next().send(msg);
	        				}catch(RemoteException re)
	        				 {
	        					re.printStackTrace();
	        					//TODO: HANDLE ! (remove Messegner or score ???)
	        				 }
	        			}
	        		}
	        		else
	        		{
	        			//HANDLE !!!
	        			super.handleMessage(msg); //drop !
	        		}
		        }

		 }

		@Override
		public IBinder onBind(Intent intent)
		{
			return mMessenger.getBinder();
		}
}
