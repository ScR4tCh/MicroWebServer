/**
 * Android Micro Web Server
 * Copyright (C) 2011  ScR4tCh
 * Contact scr4tch@scr4tch.org

 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the Lesser GNU General Public License for more details.

 * You should have received a copy of the GNU Lesser General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package org.scratch.microwebserver.http.zeminterface;

import java.text.SimpleDateFormat;

import net.zeminvaders.lang.Interpreter;
import net.zeminvaders.lang.SourcePosition;
import net.zeminvaders.lang.runtime.ZemObject;
import net.zeminvaders.lang.runtime.ZemString;

public class DateFormatFunction extends MicroWebServerFunction
{	
	public DateFormatFunction()
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
		String format = interpreter.getVariable("format", pos).toZString().toString();
		long millis = interpreter.getVariable("millis",pos).toNumber(pos).longValue();
		
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		
		return new ZemString(sdf.format(millis));
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
			return "millis";
		else
			return "format";
	}

}
