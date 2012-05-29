/**
 * Android Micro Web Server
 * Copyright (C) 2011  ScR4tCh
 * Contact scr4tch@scr4tch.org

 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the Lesser GNU General Public License for more details.

 * You should have received a copy of the GNU Lesser General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
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
