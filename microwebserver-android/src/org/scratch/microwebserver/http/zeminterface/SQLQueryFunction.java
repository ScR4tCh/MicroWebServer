/**
 * Android Micro Web Server
 * Copyright (C) 2011  ScR4tCh
 * Contact scr4tch@scr4tch.org

 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the Lesser GNU General Public License for more details.

 * You should have received a copy of the GNU Lesser General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package org.scratch.microwebserver.http.zeminterface;

import java.io.IOException;

import java.sql.Blob;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.Vector;

import org.scratch.microwebserver.util.Base64;

import net.zeminvaders.lang.Interpreter;
import net.zeminvaders.lang.SourcePosition;
import net.zeminvaders.lang.ZemException;
import net.zeminvaders.lang.runtime.Dictionary;
import net.zeminvaders.lang.runtime.ZemArray;
import net.zeminvaders.lang.runtime.ZemBoolean;
import net.zeminvaders.lang.runtime.ZemNumber;
import net.zeminvaders.lang.runtime.ZemObject;
import net.zeminvaders.lang.runtime.ZemString;

public class SQLQueryFunction extends MicroWebServerFunction
{	
	
	public SQLQueryFunction()
	{
	}

	@Override
	public int compareTo(ZemObject o)
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public ZemObject eval(Interpreter interpreter,SourcePosition pos)
	{
		String query = interpreter.getVariable("query", pos).toZString().toString();
				
		ZemObject zdbc = interpreter.getVariable("dbconnection", pos);
		
		if(!(zdbc instanceof ZemDBConnObject))
			throw new ZemException("dbconnection must be a valid DatabaseConnection !");
		
		if(query!=null)
		{
			ZemArray ret = new ZemArray();
			
			try
			{
				Vector<TreeMap<String,Object>> res = ((ZemDBConnObject)zdbc).getDatabaseConnection().doQuery(query);
				
				for(int i=0;i<res.size();i++)
				{
					Dictionary dict = new Dictionary();
					
					TreeMap<String,Object> tmc = res.elementAt(i);
					
					Iterator<String> si = tmc.keySet().iterator();
					while(si.hasNext())
					{
						String key = si.next();
						Object val = tmc.get(key);
						
						if(val==null)
							dict.set(new ZemString(key), null);
						else if(val instanceof Number)
							dict.set(new ZemString(key),new ZemNumber(val.toString()));
						else if(val instanceof Boolean)
							dict.set(new ZemString(key),new ZemBoolean(new ZemBoolean(((Boolean)val)).booleanValue()));
						else if(val instanceof CharSequence)
							dict.set(new ZemString(key),new ZemString(val.toString()));
						else if(val instanceof Blob)
							dict.set(new ZemString(key),new ZemString(Base64.encodeInputStream(((Blob)val).getBinaryStream())));
						else //should be a BLOB
							System.err.println("WTF ?");
					}
					
					ret.push(dict);
				}
				
				return ret;
			}
			catch(SQLException e)
			{
				throw new ZHTMLException(500,"DB Query failed : "+e.getMessage());
			}
			catch(IOException e)
			{
				throw new ZHTMLException(500,"Reading Blob failed : "+e.getMessage());
			}
		}
		
		throw new ZHTMLException(500,"Invalid Query");
	}

	@Override
	public int getParameterCount()
	{
		return 2;
	}

	@Override
	public String getParameterName(int index)
	{
		if(index==0)
			return "dbconnection";
		else
			return "query";
	}

}
