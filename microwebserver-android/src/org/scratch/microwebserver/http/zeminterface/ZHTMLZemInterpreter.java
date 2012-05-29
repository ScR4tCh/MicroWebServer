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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.scratch.fork.SJSONArray;
import org.json.scratch.fork.SJSONException;
import org.json.scratch.fork.SJSONObject;
import org.json.scratch.fork.SJSONTokener;
import org.scratch.microwebserver.http.BasicWebService;
import org.scratch.microwebserver.http.WebConnection;


import net.zeminvaders.lang.Interpreter;
import net.zeminvaders.lang.SourcePosition;
import net.zeminvaders.lang.TooFewArgumentsException;
import net.zeminvaders.lang.runtime.Dictionary;
import net.zeminvaders.lang.runtime.Function;
import net.zeminvaders.lang.runtime.ZemArray;
import net.zeminvaders.lang.runtime.ZemBoolean;
import net.zeminvaders.lang.runtime.ZemNumber;
import net.zeminvaders.lang.runtime.ZemObject;
import net.zeminvaders.lang.runtime.ZemString;

public class ZHTMLZemInterpreter extends Interpreter
{
	private WebConnection wb;
	
	public ZHTMLZemInterpreter(WebConnection wb)
	{
		super();
		
		symbolTable.put("include", new IncludeFunction());
		
		setWebConnection(wb);
	}
	
	protected void setWebConnection(WebConnection wb)
	{
		this.wb=wb;
		
		if(wb.getRequestMimeType()!=null && wb.getRequestMimeType().equals(BasicWebService.MIME_JSON))
			symbolTable.put("jsoninput",wrapJson(wb.getPostData()));
		else
			symbolTable.put("jsoninput",new Dictionary());
		
		symbolTable.put("mimetype", new ZemString(this.wb.getRequestMimeType()));
		
		List<ZemObject> elements = new ArrayList<ZemObject>();
		String[] getp = wb.getGetRequest();
		for(int i=0;i<getp.length;i++)
			elements.add(new ZemString(getp[i]));
		
		symbolTable.put("getPath", new ZemArray(elements));
		symbolTable.put("getTime", new GetTimeFunction());
		symbolTable.put("getUptime", new GetUptimeFunction(this.wb.getServer()));
		symbolTable.put("getCookieCrumb",new GetCookieFunction(this.wb));
		symbolTable.put("getGetValue",new GetValueFunction(this.wb));
		symbolTable.put("hasPostData",ZemBoolean.valueOf(this.wb.hasPostData()));
		symbolTable.put("hasGetData",ZemBoolean.valueOf(this.wb.hasGetData()));
		symbolTable.put("loggedIn",new Gatekeeper(this.wb));
		symbolTable.put("logIn",new LoginFunction(this.wb));
		symbolTable.put("logOut",new LogoutFunction(this.wb));
		symbolTable.put("isSet",new IsSetFunction());
		symbolTable.put("redirect",new RedirectFunction(this.wb));
	}
	
	private ZemObject wrapJson(BufferedReader postData)
	{
		if(postData!=null)
		{
			try
			{
				StringBuffer rb=new StringBuffer();
				String l;
				while((l=postData.readLine())!=null)
					rb.append(l);
				
				SJSONTokener jt = new SJSONTokener(rb.toString());
				
				try
				{
					return wrapJsonObject(new SJSONObject(jt));
				}
				catch(SJSONException je)
				{
					try
					{
						return wrapJsonArray(new SJSONArray(jt));
					}catch(SJSONException je2)
					 {
						//TODO: output ?!? log ?!?
					 }
				}
				
				
			}catch(Exception e)
			 {
				//TODO: output ?!? log ?!?
			 }
		}
		return Interpreter.NULL;
	}
	
	private Dictionary wrapJsonObject(SJSONObject json) throws SJSONException
	{
		Dictionary ret = new Dictionary();
		if(json!=null)
		{
			while(json.keys().hasNext())
			{
				String s = json.keys().next().toString();
				ZemString zs = new ZemString(s);
				
				Object data = json.get(s);
				
				if(data instanceof SJSONObject)
					ret.set(zs,wrapJsonObject((SJSONObject)data));
				else if(data instanceof SJSONArray)
					ret.set(zs,wrapJsonArray((SJSONArray)data));
				else if(data instanceof Number)
					ret.set(zs,new ZemNumber(data.toString()));
				else if(data instanceof Boolean)
					ret.set(zs,new ZemBoolean(json.getBoolean(s)));
				else if(data instanceof String)
					ret.set(zs,new ZemString(data.toString()));
				else
					System.out.println("undefined content in JSONObject ..."); //should not happen
				
			}
		}
		return ret;
	}
	
	private ZemArray wrapJsonArray(SJSONArray ja) throws SJSONException
	{
		ZemArray ret = new ZemArray();
		
		if(ja!=null)
		{
			for(int i=0;i<ja.length();i++)
			{
				Object data = ja.get(i);
				
				if(data instanceof SJSONObject)
					ret.push(wrapJsonObject((SJSONObject)data));
				else if(data instanceof SJSONArray)
					ret.push(wrapJsonArray((SJSONArray)data));
				else if(data instanceof Number)
					ret.push(new ZemNumber(data.toString()));
				else if(data instanceof Boolean)
					ret.push(new ZemBoolean(ja.getBoolean(i)));
				else if(data instanceof String)
					ret.push(new ZemString(data.toString()));
				else
					System.out.println("undefined content in JSONObject ..."); //should not happen
			}
		}
		
		return ret;
	}

	public ZemObject callFunction(String functionName, List<ZemObject> args, SourcePosition pos)
	{
        Function function = (Function) symbolTable.get(functionName);
        
        if(function instanceof MicroWebServerFunction)
        {
        	MicroWebServerFunction mwsfunc = (MicroWebServerFunction)function;
        	mwsfunc.setWebConnection(wb);
        }
        
        Map<String, ZemObject> savedSymbolTable =new HashMap<String, ZemObject>(symbolTable);
        // Setup symbolTable for function
        int noMissingArgs = 0;
        int noRequiredArgs = 0;
        for (int paramIndex = 0;paramIndex < function.getParameterCount(); paramIndex++)
        {
            String parameterName = function.getParameterName(paramIndex);
            ZemObject value = function.getDefaultValue(paramIndex);
            
            if (value == null)
            {
                noRequiredArgs++;
            }
            
            if (paramIndex < args.size())
            {
                // Value provided in function call overrides the default value
                value = args.get(paramIndex);
            }
            
            if (value == null)
            {
                noMissingArgs++;
            }
            
            setVariable(parameterName, value);
        }
        if (noMissingArgs > 0)
        {
            throw new TooFewArgumentsException(functionName, noRequiredArgs,args.size(), pos);
        }
        
        ZemObject ret = null;
        
        ret = function.eval(this, pos);
        // Restore symbolTable
        symbolTable = savedSymbolTable;

        return ret;
	}
}
