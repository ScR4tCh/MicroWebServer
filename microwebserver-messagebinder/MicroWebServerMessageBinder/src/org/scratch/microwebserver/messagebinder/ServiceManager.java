package org.scratch.microwebserver.messagebinder;

import android.os.Bundle;
import android.os.Message;
import android.os.RemoteException;

public class ServiceManager
{
	public static int registerService(DispatchingClient dp,boolean postdata,String[] input_mime,String output_mime,String group_alias,String service_name) throws RemoteException
	{
		//TODO: encode groupname/servicename to fit "url" std
		
		Message msg = new Message();
		
		msg.what=MessageTypes.MSG_REGISTER_SERVICE.ordinal();
        msg.replyTo = dp.getReplyMessenger();
        
        Bundle data = new Bundle();
        data.putBoolean("postdata",postdata);
        data.putStringArray("input_mime",input_mime);
        data.putString("output_mime",output_mime);
        data.putString("group_alias",group_alias);
        data.putString("service_name",service_name);
        data.putString("package_name",dp.getPackageName());
        msg.setData(data);
        
        dp.getMessenger().send(msg);
        
        //TODO: use a better hashing !!!
        return (group_alias+service_name).hashCode();
	}
}
