package org.scratch.microwebserver;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

public class LogSettingsActivity extends Activity implements OnItemSelectedListener,OnClickListener
{
	private Spinner minLevelSpin,maxLevelSpin;
	private Button applyBt;
	
	public static final String MINLEVEL="MIN_LEVEL";
	public static final String MAXLEVEL="MAX_LEVEL";

	protected static final int SET=2212;
	
	private int minLevel=-1,maxLevel=-1;
	
	@Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.logsettings);
        
        
        Intent i = getIntent();
        
        minLevel=i.getIntExtra(MINLEVEL,-1);
        maxLevel=i.getIntExtra(MAXLEVEL,-1);
        
        minLevelSpin=(Spinner)findViewById(R.id.minlevel_spin);
        ArrayAdapter<CharSequence> adapterMin = ArrayAdapter.createFromResource(this, R.array.loglevels, android.R.layout.simple_spinner_item);
        adapterMin.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        minLevelSpin.setAdapter(adapterMin);
        minLevelSpin.setSelection(minLevel+1);
        minLevelSpin.setOnItemSelectedListener(this);
        
        maxLevelSpin=(Spinner)findViewById(R.id.maxlevel_spin);
        ArrayAdapter<CharSequence> adapterMax = ArrayAdapter.createFromResource(this, R.array.loglevels, android.R.layout.simple_spinner_item);
        adapterMax.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        maxLevelSpin.setAdapter(adapterMax);
        maxLevelSpin.setSelection(maxLevel+1);
        maxLevelSpin.setOnItemSelectedListener(this);
        
        applyBt=(Button)findViewById(R.id.button1);
        applyBt.setOnClickListener(this);
        
    }

	@Override
	public void onItemSelected(AdapterView<?> parent,View view,int pos,long id)
	{
		if(parent.equals(minLevelSpin))
			minLevel=pos-1;
		
		if(parent.equals(maxLevelSpin))
			maxLevel=pos-1;
	
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent)
	{
		// TODO Auto-generated method stub
		
	}
	
	private void result()
	{
		Intent it = new Intent();
		it.putExtra(MINLEVEL,minLevel);
		it.putExtra(MAXLEVEL,maxLevel);
		
		setResult(SET,it);
		
		finish();
	}

	@Override
	public void onClick(View view)
	{
		if(view.equals(applyBt))
			result();
	}

}
