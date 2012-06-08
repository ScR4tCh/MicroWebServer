package org.scratch.microwebserver;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

public class LogSettingsActivity extends Activity implements OnItemSelectedListener
{
	private Spinner minLevelSpin,maxLevelSpin;
	
	public static final String MINLEVEL="MIN_LEVEL";
	public static final String MAXLEVEL="MAX_LEVEL";
	
	private int minLevel,maxLevel;
	
	@Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.logsettings);
        
        
        Intent i = getIntent();
        
        minLevel=i.getIntExtra(MINLEVEL,0);
        maxLevel=i.getIntExtra(MAXLEVEL,0);
        
        minLevelSpin=(Spinner)findViewById(R.id.minlevel_spin);
        ArrayAdapter<CharSequence> adapterMin = ArrayAdapter.createFromResource(this, R.array.loglevels, android.R.layout.simple_spinner_item);
        adapterMin.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        minLevelSpin.setAdapter(adapterMin);
        minLevelSpin.setSelection(minLevel);
        minLevelSpin.setOnItemSelectedListener(this);
        
        maxLevelSpin=(Spinner)findViewById(R.id.maxlevel_spin);
        ArrayAdapter<CharSequence> adapterMax = ArrayAdapter.createFromResource(this, R.array.loglevels, android.R.layout.simple_spinner_item);
        adapterMax.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        maxLevelSpin.setAdapter(adapterMax);
        maxLevelSpin.setSelection(maxLevel);
        maxLevelSpin.setOnItemSelectedListener(this);
        
    }

	@Override
	public void onItemSelected(AdapterView<?> parent,View view,int pos,long id)
	{
		if(parent.equals(minLevelSpin))
			minLevel=pos;
		
		if(parent.equals(maxLevelSpin))
			maxLevel=pos;
	
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent)
	{
		// TODO Auto-generated method stub
		
	}

}
