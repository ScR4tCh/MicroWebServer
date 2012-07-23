/**
 * Android Micro Web Server
 * Copyright (C) 2011  ScR4tCh
 * Contact scr4tch@scr4tch.org

 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the Lesser GNU General Public License for more details.

 * You should have received a copy of the GNU Lesser General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package org.scratch.microwebserver.http.z3minterface;


import java.sql.SQLException;

import org.scratch.microwebserver.data.DBManager;
import org.scratch.microwebserver.data.DatabaseRightsException;
import org.scratch.microwebserver.data.DatabaseUserException;
import org.scratch.microwebserver.data.NoSuchDatabaseException;
import org.scratch.microwebserver.http.WebConnection;

import net.zeminvaders.lang.Interpreter;
import net.zeminvaders.lang.SourcePosition;
import net.zeminvaders.lang.runtime.ZemNULL;
import net.zeminvaders.lang.runtime.ZemObject;

public class CreateDBConnectionFunction extends MicroWebServerFunction
{	
	private static final String EXT="Database connection failed: ";
	
	private DBManager dbm;
	
	public CreateDBConnectionFunction(WebConnection wb)
	{
		dbm=wb.getDatabase();
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
		String db=null;
		
		ZemObject zo=interpreter.getVariable("database", pos);
		
		if( zo!=null && !(zo instanceof ZemNULL) )
			db = zo.toZString().toString();
		
		//FIXME: should test for NULL !
		String user = interpreter.getVariable("username", pos).toZString().toString();
		String pw = interpreter.getVariable("password", pos).toZString().toString();
		
		try
		{
			return new Z3mDBConnObject(dbm.openDatabaseConnection(db,user,DBManager.encodePW(pw)));
		}
		catch(NoSuchDatabaseException e)
		{
			throw new ZHTMLException(500,EXT+e.getMessage());
		}
		catch(DatabaseRightsException e)
		{
			throw new ZHTMLException(500,EXT+e.getMessage());
		}
		catch(DatabaseUserException e)
		{
			throw new ZHTMLException(500,EXT+e.getMessage());
		}
		catch(SQLException e)
		{
			throw new ZHTMLException(500,EXT+e.getMessage());
		}
				
	}

	@Override
	public int getParameterCount()
	{
		return 3;
	}

	@Override
	public String getParameterName(int index)
	{
		if(index==0)
			return "database";
		else if(index==1)
			return "username";
		else
			return "password";
	}

}
