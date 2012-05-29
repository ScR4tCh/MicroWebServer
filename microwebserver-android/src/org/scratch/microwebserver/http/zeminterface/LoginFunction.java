/**
 * Android Micro Web Server
 * Copyright (C) 2011  ScR4tCh
 * Contact scr4tch@scr4tch.org

 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the Lesser GNU General Public License for more details.

 * You should have received a copy of the GNU Lesser General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package org.scratch.microwebserver.http.zeminterface;

import java.sql.SQLException;
import java.util.Map;

import org.scratch.microwebserver.http.WebConnection;

import net.zeminvaders.lang.Interpreter;
import net.zeminvaders.lang.SourcePosition;
import net.zeminvaders.lang.runtime.ZemObject;

public class LoginFunction extends MicroWebServerFunction
{
	private WebConnection wb;
	
	private String username=null;
	private String password=null;
	
	private int errorcode=0;
	private String errormessage="";

	public LoginFunction(WebConnection wb)
	{
		this.wb=wb;
		
		if(wb.hasPostData())
		{
			String mime = wb.getRequestMimeType();
			
			if(mime.equals("application/x-www-form-urlencoded"))
			{
				String formdata="";
				
				try
				{
					if(wb.getPostDataLength()==-1)
					{
						errorcode=401;
						errormessage="invalid content length";
					}
					else
					{
						char[] rb = new char[(int)wb.getPostDataLength()];
						wb.getPostData().read(rb);
						formdata=new String(rb);
						
						
						Map<String,String> pd = WebConnection.getFormData(formdata);
						
						if(pd.containsKey("username") && pd.containsKey("password"))
						{
							this.username=pd.get("username");
							this.password=pd.get("password");
							
						}
					}
				}
				catch(Exception e)
				{
					errorcode=500;
					errormessage=e.getMessage();
				}
				
			}
			else if(mime.equals("application/json"))
			{
				//TODO:...
			}
			else
			{
				errorcode=415;
				errormessage="Unsupported data "+mime;
			}
		}
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
		if(username!=null && password!=null)
		{
			try
			{
				String token = wb.getDatabase().generateToken(username,password);
				wb.addCookieCrumb("TOKEN",token);
				wb.appendReplyCookieCrumb("TOKEN",token);
			}
			catch(SQLException e)
			{
				throw new ZHTMLException(500,"unable to log in : \""+e.getMessage()+"\"");
			}
		}
		else
		{
			if(errorcode>0)
			{
				throw new ZHTMLException(errorcode,errormessage);
			}
			else
				throw new ZHTMLException(401,"invalid login data");
		}
		
		return null;
	}

	@Override
	public int getParameterCount()
	{
		return 0;
	}

	@Override
	public String getParameterName(int index)
	{
		// TODO Auto-generated method stub
		return null;
	}

}
