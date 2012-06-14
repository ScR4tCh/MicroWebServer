/**
 * Android Micro Web Server
 * Copyright (C) 2011  ScR4tCh
 * Contact scr4tch@scr4tch.org

 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the Lesser GNU General Public License for more details.

 * You should have received a copy of the GNU Lesser General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package org.scratch.microwebserver.http.zeminterface;

import org.scratch.microwebserver.http.MicroWebServer;

import net.zeminvaders.lang.Interpreter;
import net.zeminvaders.lang.SourcePosition;
import net.zeminvaders.lang.runtime.ZemNumber;
import net.zeminvaders.lang.runtime.ZemObject;

public class GetUptimeFunction extends MicroWebServerFunction
{	
	private MicroWebServer mws;
	
	public GetUptimeFunction(MicroWebServer mws)
	{
		this.mws=mws;
	}

	@Override
	public ZemObject eval(Interpreter interpreter,SourcePosition pos) throws ZHTMLException
	{
		return new ZemNumber(mws.getUptime());
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
