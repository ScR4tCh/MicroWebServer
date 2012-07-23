/**
 * Android Micro Web Server
 * Copyright (C) 2011  ScR4tCh
 * Contact scr4tch@scr4tch.org

 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the Lesser GNU General Public License for more details.

 * You should have received a copy of the GNU Lesser General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package org.scratch.microwebserver.http.z3minterface;

import java.io.BufferedReader;

import org.scratch.microwebserver.data.DBManager;
import org.scratch.microwebserver.http.WebConnection;

import net.zeminvaders.lang.Interpreter;
import net.zeminvaders.lang.SourcePosition;
import net.zeminvaders.lang.runtime.Function;
import net.zeminvaders.lang.runtime.ZemObject;

public abstract class MicroWebServerFunction extends Function
{
	protected WebConnection wb;
	protected BufferedReader postdata;
	protected String[] getrequest;
	protected String mimetype;
	protected DBManager database;
	
	public MicroWebServerFunction()
	{
		super();
	}
	
	@Override
	public abstract ZemObject eval(Interpreter interpreter,SourcePosition pos) throws ZHTMLException;

	@Override
	public ZemObject getDefaultValue(int index)
	{
		return null;
	}

	@Override
	public abstract int getParameterCount();

	@Override
	public abstract String getParameterName(int index);
	
	public void setWebConnection(WebConnection wb)
	{
		this.postdata=wb.getPostData();
		this.mimetype=wb.getRequestMimeType();
		this.getrequest=wb.getGetRequest();
		this.database=wb.getDatabase();
	}

}
