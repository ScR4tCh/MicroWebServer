package org.scratch.microwebserver.messagebinder;

import android.os.Bundle;
import android.os.Message;
import android.os.RemoteException;

public class ServiceManager
{
	public static int registerService(DispatchingClient dp,int methods,String[] input_mime,String output_mime,String group_alias,String service_name) throws RemoteException
	{
		 if(dp==null || dp.getMessenger()==null)
	        	return -1;
		
		//TODO: encode groupname/servicename to fit "url" std
		
		Message msg = new Message();
		
		msg.what=MessageTypes.MSG_REGISTER_SERVICE.ordinal();
        msg.replyTo = dp.getReplyMessenger();
        
        Bundle data = new Bundle();
        data.putInt(MessageData.SUPPORTED_METHODS.getFieldName(),methods);
        data.putStringArray(MessageData.INPUT_MIMETYPES.getFieldName(),input_mime);
        data.putString(MessageData.OUTPUT_MIMETYPE.getFieldName(),output_mime);
        data.putString(MessageData.SERVICEGROUP_ALIAS.getFieldName(),group_alias);
        data.putString(MessageData.SERVICE_NAME.getFieldName(),service_name);
        data.putString(MessageData.PACKAGE_NAME.getFieldName(),dp.getPackageName());
        msg.setData(data);
        
        dp.getMessenger().send(msg);
        
        //TODO: use a better hashing !!!
        return (group_alias+service_name).hashCode();
	}
}
