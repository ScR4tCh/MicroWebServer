package org.scratch.microwebserver.http.zeminterface;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.scratch.microwebserver.http.ZHTMProcessor;
import org.scratch.microwebserver.properties.PropertyNames;
import org.scratch.microwebserver.properties.ServerProperties;


import net.zeminvaders.lang.Interpreter;
import net.zeminvaders.lang.SourcePosition;
import net.zeminvaders.lang.runtime.ZemIncludeString;
import net.zeminvaders.lang.runtime.ZemObject;

public class IncludeFunction extends MicroWebServerFunction
{

	@Override
	public ZemObject eval(Interpreter interpreter,SourcePosition pos) throws ZHTMLException
	{
		String script = interpreter.getVariable("script", pos).toZString().toString();
        if(!script.startsWith("/"))
        	script="/"+script;
		
		File f = new File(ServerProperties.getInstance().getString(PropertyNames.SERVER_ROOT)+script);
		
		if(f.getName().toLowerCase().endsWith(".zhtml"))
		{
			
			String sb = "";
			
			try
			{
				BufferedReader bin = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
				String b;
				while((b=bin.readLine())!=null)
					sb+=b;		
				bin.close();
				
				return new ZemIncludeString(ZHTMProcessor.process((ZHTMLZemInterpreter)interpreter,wb,sb));
			}catch(IOException ioe){ throw new ZHTMLException(500,ioe.getMessage());}
		}
		
		return Interpreter.NULL;
	}

	@Override
	public int getParameterCount()
	{
		return 1;
	}

	@Override
	public String getParameterName(int index)
	{
		return "script";
	}
	
}
