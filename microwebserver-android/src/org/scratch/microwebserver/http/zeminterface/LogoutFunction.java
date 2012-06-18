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

import org.scratch.microwebserver.http.WebConnection;

import net.zeminvaders.lang.Interpreter;
import net.zeminvaders.lang.SourcePosition;
import net.zeminvaders.lang.runtime.ZemObject;

public class LogoutFunction extends MicroWebServerFunction
{
	private WebConnection wb;

	public LogoutFunction(WebConnection wb)
	{
		this.wb=wb;
	}

	@Override
	public ZemObject eval(Interpreter interpreter,SourcePosition pos)throws ZHTMLException
	{
		if(wb.getCookieCrumb("TOKEN")!=null)
		{
			try
			{
				wb.getDatabase().invalidateToken(wb.getCookieCrumb("TOKEN").toString());
			}
			catch(SQLException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		//redirect
		wb.setHeaderField("Location","/");
		wb.setHeaderField("Cache-Control","no-cache");
		wb.setStatusCode(307);
		
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
		return null;
	}

}
