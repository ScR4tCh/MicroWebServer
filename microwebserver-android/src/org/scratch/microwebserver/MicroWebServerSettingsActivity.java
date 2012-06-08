package org.scratch.microwebserver;

import java.util.HashMap;
import java.util.Map;

import org.scratch.filedialog.FileChooserDialog;
import org.scratch.microwebserver.properties.PropertyNames;
import org.scratch.microwebserver.properties.ServerProperties;


import android.app.Activity;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.text.InputType;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

public class MicroWebServerSettingsActivity extends Activity
{	
	private boolean changed=false;
	
	private Map<Integer,TextView> textwidgets = new HashMap<Integer,TextView>();
	
	@Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.settings);
        
        
        LinearLayout l = (LinearLayout)findViewById(R.id.settingscontent);
        
        final PropertyNames[] pn = PropertyNames.values();
        
        ServerProperties props = ServerProperties.getInstance();
        
        LinearLayout.LayoutParams wp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        
        for(int i=0;i<pn.length;i++)
        {
        	if(pn[i].isConfigurable())
        	{
        		switch(pn[i].getType())
        		{
        			case BOOLEAN:			CheckBox cb = new CheckBox(getApplicationContext());
        									cb.setText(getDescrStr(pn[i].name()));
        									cb.setChecked(props.getBoolean(pn[i].toString()));
        				
        									cb.setLayoutParams(wp);
        									l.addView(cb);
        									
        									break;
        			
        			
        			case INT:				
        				
        				break;
        			
        			
        			case FLOAT:				
        				
        				break;
        			
        			
        			case STRING:			LinearLayout tl = new LinearLayout(getApplicationContext());
        									tl.setOrientation(LinearLayout.VERTICAL);
        									
        									TextView tv = new TextView(getApplicationContext());
        									tv.setText(getDescrStr(pn[i].name()));
        									tl.addView(tv);
        									
        									EditText te = new EditText(getApplicationContext());
        									te.setText(props.getString(pn[i].toString()));
        									tl.addView(te);
        									
        									tl.setLayoutParams(wp);
        									l.addView(tl);
        				
        									break;
        			
        			
        			case MIXED:				
        				
        				break;
        			
        			
        			case SELECTION:			int arrayid = getResources().getIdentifier(getDescrStr(pn[i].name()),"array",this.getApplicationContext().getPackageName());
        				
        									if(arrayid!=0)
        									{
        										LinearLayout tls = new LinearLayout(getApplicationContext());
            									tls.setOrientation(LinearLayout.VERTICAL);
            									
            									TextView tvs = new TextView(getApplicationContext());
            									tvs.setText(getDescrStr(pn[i].name()));
            									tls.addView(tvs);
            									
	        									Spinner sp = new Spinner(getApplicationContext());
	        									ArrayAdapter<CharSequence> spa = ArrayAdapter.createFromResource(this, arrayid, android.R.layout.simple_spinner_item);
	        									sp.setAdapter(spa);
	        									
	        									int spp = spa.getPosition(props.getString(pn[i].toString()));

	        									if(spp>=0)
	        									{
		        									//set the default according to value
		        									sp.setSelection(spp);
	
		        									tls.addView(sp);
	        									}
	        									
        									}
        				
        									break;
        				
        			case FILE:case FOLDER:  LinearLayout tlf = new LinearLayout(getApplicationContext());
											tlf.setOrientation(LinearLayout.VERTICAL);
											
											TextView tvf = new TextView(getApplicationContext());
											tvf.setText(getDescrStr(pn[i].name()));
											tlf.addView(tvf);
											
											RelativeLayout tlfh = new RelativeLayout(getApplicationContext());
											tlfh.setLayoutParams(wp);
											
											EditText tef = new EditText(getApplicationContext());
											tef.setEnabled(false);
											tef.setInputType(InputType.TYPE_NULL);
											tef.setText(props.getString(pn[i].toString()));
											tef.setId(tef.hashCode());
											RelativeLayout.LayoutParams lp1 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.MATCH_PARENT);
											lp1.addRule(RelativeLayout.ALIGN_LEFT, tef.getId());
											tef.setLayoutParams(lp1);
											
											
											tlfh.addView(tef);
											
											final int o = pn[i].ordinal();
											
											textwidgets.put(o,tef);
											
											Button sbt = new Button(getApplicationContext());
											sbt.setId(sbt.hashCode());
											sbt.setText("...");
											sbt.setOnClickListener(		new OnClickListener()
																		{
																			
																			@Override
																			public void onClick(View v)
																			{
//																				Intent intent = new Intent(getBaseContext(), FileDialog.class);
//																                intent.putExtra(FileDialog.START_PATH, Environment.getExternalStorageDirectory().getAbsolutePath());
//																                intent.putExtra(FileDialog.SELECTION_MODE, SelectionMode.MODE_OPEN);
////																                //can user select directories or not
//																                intent.putExtra(FileDialog.ONLY_SELECT_DIR, true);
//																                intent.putExtra(FileDialog.FILE_DRAWABLE, R.drawable.file);
//																                intent.putExtra(FileDialog.FOLDER_DRAWABLE, R.drawable.file);
////																                
//																                startActivityForResult(intent, o);
																				
																				
																				Intent intent = new Intent(getBaseContext(), FileChooserDialog.class);
																                intent.putExtra(FileChooserDialog.START_PATH, Environment.getExternalStorageDirectory().getAbsolutePath());
																                //intent.putExtra(FileDialog.SELECT_MODE, SelectionMode.MODE_OPEN);
//																                //can user select directories or not
																                intent.putExtra(FileChooserDialog.VIEW_MODE, FileChooserDialog.SELECT_FOLDER);
																                intent.putExtra(FileChooserDialog.FILE_DRAWABLE, BitmapFactory.decodeResource(getResources(),R.drawable.deffile));
																                intent.putExtra(FileChooserDialog.FOLDER_DRAWABLE, BitmapFactory.decodeResource(getResources(),R.drawable.deffolder));
//																                
																                startActivityForResult(intent, o);
																				
																			}
																		}
																   );
											
											tlfh.addView(sbt);
											
											RelativeLayout.LayoutParams lp2 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
											lp2.addRule(RelativeLayout.RIGHT_OF, tef.getId());

											
											sbt.setLayoutParams(lp2);
											
											
											tlf.addView(tlfh);
											tlf.setLayoutParams(wp);
											l.addView(tlf);
											
											break;
        		}
        	}
        }        
        
    }
	
	@Override
	public void onBackPressed()
	{
		if(changed)
		{
			//restartDialog
		}
		
		super.onBackPressed();
	}
	
	private String getDescrStr(String id)
	{
		int resId = getResources().getIdentifier(id,"string",this.getApplicationContext().getPackageName());
		
		if(resId==0)
			return id;
		
		return getString(resId);
	}
	
	 public synchronized void onActivityResult(final int requestCode, int resultCode, final Intent data)
	 {

             if (resultCode == Activity.RESULT_OK)
             {
            	 	if(textwidgets.containsKey(requestCode))
            	 	{
            	 		textwidgets.get(requestCode).post
            	 						(
	            	 						new Runnable()
	            	 						{
	            	 							public void run()
	            	 							{
	            	 								textwidgets.get(requestCode).setText(data.getStringExtra(FileChooserDialog.RESULT_PATH));
	            	 							}
	            	 						}
            	 						);
            	 	}
             }
             else if (resultCode == Activity.RESULT_CANCELED)
             {
            	 //just do nothing !
             }

     }
	
}
