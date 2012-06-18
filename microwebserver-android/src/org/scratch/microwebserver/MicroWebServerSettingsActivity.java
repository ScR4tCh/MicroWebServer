package org.scratch.microwebserver;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.scratch.filedialog.FileChooserDialog;
import org.scratch.microwebserver.properties.PropertyChecks;
import org.scratch.microwebserver.properties.PropertyDatatype;
import org.scratch.microwebserver.properties.PropertyDependency;
import org.scratch.microwebserver.properties.PropertyNames;
import org.scratch.microwebserver.properties.ServerProperties;
import org.scratch.microwebserver.util.Helper;


import android.app.Activity;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView.OnEditorActionListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

/**
 * OK dude, this could become part of a separate lib for "generic settings" ;)
 * @author scratch
 *
 */

public class MicroWebServerSettingsActivity extends Activity implements OnCheckedChangeListener,OnItemSelectedListener,OnEditorActionListener,OnClickListener
{	
	protected static final int OK=101;

	private boolean changed=false;
	
	private Map<Integer,TextView> textwidgets = new HashMap<Integer,TextView>();
	
	private Map<PropertyNames,View> views = new HashMap<PropertyNames,View>();
	private Map<PropertyNames,View> editViews = new HashMap<PropertyNames,View>();	//may differ ...
	private Map<PropertyNames,Vector<PropertyNames>> depend = new HashMap<PropertyNames,Vector<PropertyNames>>();
	
	private Button applyBt;
	
	@Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.settings);
        
        
        LinearLayout l = (LinearLayout)findViewById(R.id.settingscontent);
        
        applyBt=(Button)findViewById(R.id.applyBt);
        applyBt.setOnClickListener(this);
        
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
        									cb.setOnCheckedChangeListener(this);
        				
        									cb.setLayoutParams(wp);
        									l.addView(cb);
        									
        									views.put(pn[i],cb);
        									editViews.put(pn[i],cb);
        									        									
        									break;
        			
        			
        			case INT:				LinearLayout tln = new LinearLayout(getApplicationContext());
											tln.setOrientation(LinearLayout.VERTICAL);
											
											TextView tvn = new TextView(getApplicationContext());
											tvn.setText(getDescrStr(pn[i].name()));
											tln.addView(tvn);
											
											EditText np = new EditText(getApplicationContext());
											np.setInputType(InputType.TYPE_CLASS_NUMBER);
											np.setText(""+props.getInt(pn[i].toString()));
											tln.addView(np);
																					
											tln.setLayoutParams(wp);
											l.addView(tln);
											
											views.put(pn[i],tln);
											editViews.put(pn[i],np);		
        				
        									break;
        									
					case FLOAT:				break;
        			
        			
        			case STRING:case PASSWORD:			
        									LinearLayout tl = new LinearLayout(getApplicationContext());
        									tl.setOrientation(LinearLayout.VERTICAL);
        									
        									TextView tv = new TextView(getApplicationContext());
        									tv.setText(getDescrStr(pn[i].name()));
        									tl.addView(tv);
        									
        									EditText te = new EditText(getApplicationContext());
        									te.setText(props.getString(pn[i].toString()));
        									tl.addView(te);
        									
        									if(pn[i].getType()==PropertyDatatype.PASSWORD)
        										te.setTransformationMethod(PasswordTransformationMethod.getInstance());

        									
        									tl.setLayoutParams(wp);
        									l.addView(tl);
        									
        									te.setOnEditorActionListener(this);
        									
        									views.put(pn[i],tl);
        									editViews.put(pn[i],te);
        									
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
		        									
		        									views.put(pn[i],tls);
		        									editViews.put(pn[i],sp);
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
											
											
											final int o = pn[i].ordinal();
											
											textwidgets.put(o,tef);
											tef.setOnEditorActionListener(this);
											
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
											
											
											
											RelativeLayout.LayoutParams lp2 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
											lp2.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, tlfh.getId());
											lp2.addRule(RelativeLayout.ALIGN_TOP, tlfh.getId());
											
	
											RelativeLayout.LayoutParams lp1 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
											lp1.addRule(RelativeLayout.ALIGN_LEFT, tlfh.getId());
											lp1.addRule(RelativeLayout.LEFT_OF, sbt.getId());
											
											
											sbt.setLayoutParams(lp2);
											tef.setLayoutParams(lp1);
											
											tlfh.addView(sbt);
											tlfh.addView(tef);
											
											tlf.addView(tlfh);
											tlf.setLayoutParams(wp);
											l.addView(tlf);
											
											views.put(pn[i],tlf);
        									editViews.put(pn[i],tef);
																						
											break;
        		}
        		
        	}
        }
        
        // crawl views to add some padding and margin
        Iterator<View> vi = views.values().iterator();
        while(vi.hasNext())
        {
        	View vii = vi.next();
        	if(! (vii instanceof CheckBox) )
        		vii.setPadding(5,5,5,5);
        }
        
        //"crawl" edit views to map dependencies
        Iterator<PropertyNames> ip = editViews.keySet().iterator();
        while(ip.hasNext())
        {
        	PropertyNames ipn = ip.next();
        	//View v = editViews.get(ipn);
        	
        	if(ipn.getDependencies().size()>0)
        	{
        		Set<PropertyDependency> pds = ipn.getDependencies();
        		Iterator<PropertyDependency> pdsi = pds.iterator();
        		
        		while(pdsi.hasNext())
        		{
        			PropertyDependency pd = pdsi.next();
        			if(!depend.containsKey(pd.getPropertyName()))
        			{
        				Vector<PropertyNames> vp = new Vector<PropertyNames>();
        				vp.add(ipn);
        				depend.put(pd.getPropertyName(),vp);
        			}
        			else
        			{
        				depend.get(pd.getPropertyName()).add(ipn);
        			}
        				
        		}
        	}
        }
        
        //initial depen check
        validateDependencies(new Vector<PropertyNames>(views.keySet()));
        
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

	@Override
	public boolean onEditorAction(TextView textview,int i,KeyEvent keyevent)
	{
//		if(i==EditorInfo.IME_ACTION_DONE)
//		{
//			PropertyNames p;
//			if(editViews.containsValue(textview))
//			{
//				p=Helper.getKeyByValue(editViews,textview);
//				if(depend.containsKey(p))
//				{
//					validateDependencies(depend.get(p));
//				}
//			}
//		}
//		
		return false;
	}
	

	@Override
	public void onItemSelected(AdapterView<?> arg0,View arg1,int arg2,long arg3)
	{
//		PropertyNames p;
//		if(editViews.containsValue(arg0))
//		{
//			p=Helper.getKeyByValue(editViews,arg0);
//			if(depend.containsKey(p))
//			{
//				validateDependencies(depend.get(p));
//			}
//		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0)
	{
//		PropertyNames p;
//		if(editViews.containsValue(arg0))
//		{
//			p=Helper.getKeyByValue(editViews,arg0);
//			if(depend.containsKey(p))
//			{
//				validateDependencies(depend.get(p));
//			}
//		}
	}
	
	@Override
	public void onCheckedChanged(CompoundButton buttonView,boolean isChecked)
	{
		PropertyNames p;
		if(editViews.containsValue(buttonView))
		{
			p=Helper.getKeyByValue(editViews,buttonView);
			if(depend.containsKey(p))
			{
				System.out.println("DEPENDENCY -> "+p.toString());
				validateDependencies(depend.get(p));
			}
		}
	}
	
	private void validateDependencies(Vector<PropertyNames> pdv)
	{
		
		for(int i=0;i<pdv.size();i++)
		{
			System.err.println("has dependency->"+pdv.elementAt(i).toString());
			
			View v = views.get(pdv.elementAt(i));
			
			Set<PropertyDependency> pds = pdv.elementAt(i).getDependencies();
			
			Iterator<PropertyDependency> dpi = pds.iterator();
			boolean enable=true;
			while(dpi.hasNext())
			{
				PropertyDependency pd = dpi.next();
				
				View editv = editViews.get(pd.getPropertyName());
				
				Object test;
				
				if(editv instanceof EditText)
					if( (((EditText)editv).getInputType()&InputType.TYPE_CLASS_NUMBER)==InputType.TYPE_CLASS_NUMBER)
						try{test=Integer.valueOf(((TextView)editv).getText().toString());}catch(NumberFormatException nfe){test=null;}
					else
						test=((TextView)editv).getText().toString();
				else if(editv instanceof CheckBox)
					test=new Boolean(((CheckBox)editv).isChecked());
				else
					test=null;
				
				if(!pd.dependencyMet(test))
				{
					enable=false;
					break;
				}
			}
			
			final boolean eb = enable;
			final View vv = v;
			
			System.err.println(pdv.elementAt(i)+" view "+(eb?"enabled":"disabled"));
			
			runOnUiThread(new Runnable(){public void run(){setViewEnabled(vv,eb);}});
		}
	}
	
	private void setViewEnabled(final View v,boolean state)
	{
		if(v==null || (v!=null && textwidgets.containsValue(v)) )
		{
			return;
		}
		
		v.setEnabled(state);
		if(v instanceof Button)
			v.setClickable(state);
		
		if(v instanceof ViewGroup)
		{
			for (int i = 0; i <= ((ViewGroup)v).getChildCount(); i++)
			{
				setViewEnabled(((ViewGroup)v).getChildAt(i),state);
			}
		}
	}

	@Override
	public void onClick(View v)
	{
		if(v.equals(applyBt))
		{
			//possebility checks
			if(doPossebilityChecks())
			{
				//save changes !
				ServerProperties props = ServerProperties.getInstance();
				
				Iterator<PropertyNames> pnn = editViews.keySet().iterator();
				while(pnn.hasNext())
				{
					PropertyNames pn=pnn.next();
					View vv = editViews.get(pn);
					
					if(vv instanceof EditText)
						props.addProperty(pn.toString(),((EditText)vv).getText().toString());
					else if(vv instanceof CheckBox)
						props.addProperty(pn.toString(),((CheckBox)vv).isChecked());
					//TODO: what about other property types ?
				}
				
				try
				{
					props.writeProperties();
				}
				catch(IOException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				setResult(OK);
				finish();
			}
		}
	}
	
	private boolean doPossebilityChecks()
	{
		boolean ret=true;
		
		Iterator<PropertyNames> pnn = views.keySet().iterator();
		while(pnn.hasNext())
		{
			PropertyNames pn = pnn.next();
			if(pn.getChecks().size()>0)
			{
				Iterator<PropertyChecks> pcs = pn.getChecks().iterator();
				while(pcs.hasNext())
				{
					View editv = editViews.get(pn);
					
					Object test;
					
					if(editv instanceof EditText)
						if( (((EditText)editv).getInputType()&InputType.TYPE_CLASS_NUMBER)==InputType.TYPE_CLASS_NUMBER)
							try{test=Integer.valueOf(((TextView)editv).getText().toString());}catch(NumberFormatException nfe){test=null;}
						else
							test=((TextView)editv).getText().toString();
					else if(editv instanceof CheckBox)
						test=new Boolean(((CheckBox)editv).isChecked());
					else
						test=null;
					
					final PropertyChecks pc = pcs.next();
					
					if(!pc.dependencyMet(test))
					{
						//draw border around view !
						final View v = views.get(pn);
						
						
						runOnUiThread(new Runnable()
									  {
										public void run()
										{
											v.setBackgroundResource(R.drawable.redrect);
											(Toast.makeText(getApplicationContext(),pc.getHint(),Toast.LENGTH_SHORT)).show();
										}
									  });
						
						ret=false;
					}
					else
					{
						final View v = views.get(pn);
						
						runOnUiThread(new Runnable()
						{
							public void run()
							{
								v.setVisibility(View.GONE);
							}
						});
					}
				}
			}
			else
			{
				final View v = views.get(pn);
				
				runOnUiThread(new Runnable()
				{
					public void run()
					{
						v.setVisibility(View.GONE);
					}
				});
			}
		}
		
		return ret;
	}

}
