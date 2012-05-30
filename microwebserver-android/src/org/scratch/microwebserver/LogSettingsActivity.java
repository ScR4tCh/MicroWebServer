package org.scratch.microwebserver;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Spinner;

public class LogSettingsActivity extends Activity implements OnItemSelectedListener
{
	private Spinner minLevelSpin,maxLevelSpin;
	
	private int minLevel,maxLevel;
	
	@Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.logsettings);
        
        minLevelSpin=(Spinner)findViewById(R.id.minlevel_spin);
        //minLevelSpin.setAdapter();
        minLevelSpin.setOnItemSelectedListener(this);
        
        maxLevelSpin=(Spinner)findViewById(R.id.maxlevel_spin);
        //maxLevelSpin.setAdapter();
        maxLevelSpin.setOnItemSelectedListener(this);
        
    }

	@Override
	public void onItemSelected(AdapterView<?> parent,View view,int pos,long id)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent)
	{
		// TODO Auto-generated method stub
		
	}
}
