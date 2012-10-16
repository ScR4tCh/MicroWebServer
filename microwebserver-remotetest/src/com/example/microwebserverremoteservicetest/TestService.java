package com.example.microwebserverremoteservicetest;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

public class TestService extends Service
{
	private final IBinder binder=new TestBinder();
	private Watcher watcher;
	
	protected class TestBinder extends Binder
	{
		public void setWatcher(Watcher w)
		{
			TestService.this.watcher=w;
		}
	}
	
	@Override
	public IBinder onBind(Intent intent)
	{
		return binder;
	}
	
	

}
