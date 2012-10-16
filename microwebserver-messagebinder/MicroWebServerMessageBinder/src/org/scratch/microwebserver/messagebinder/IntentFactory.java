package org.scratch.microwebserver.messagebinder;

import android.content.ComponentName;
import android.content.Intent;

public class IntentFactory
{
	public static final String INTENT_PACKAGE="org.scratch.microwebserver";
	public static final String SERVICE="org.scratch.microwebserver.RemoteDispatcherService";
	
	public static final Intent createIntent()
	{
		final Intent i = new Intent(INTENT_PACKAGE);                
    	i.setComponent(new  ComponentName(INTENT_PACKAGE,SERVICE));
    	
    	return i;
	}
}
