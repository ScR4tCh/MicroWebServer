package org.scratch.microwebserver;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.View;
import android.view.ViewGroup;

public class LogCursorAdapter extends CursorAdapter
{

	public LogCursorAdapter(Context context,Cursor c)
	{
		super(context,c,false);
	}

	@Override
	public void bindView(View arg0,Context arg1,Cursor arg2)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public View newView(Context arg0,Cursor arg1,ViewGroup arg2)
	{
		// TODO Auto-generated method stub
		return null;
	}

}
