/**
 * Android Micro Web Server
 * Copyright (C) 2011  ScR4tCh
 * Contact scr4tch@scr4tch.org

 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the Lesser GNU General Public License for more details.

 * You should have received a copy of the GNU Lesser General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package org.scratch.microwebserver;

public class LogEntry
{
	private long t;
	private int level;
	private String message;
	private String request;
	private String remoteAddress;
	
	public LogEntry(long t,int level,String message,String request,String remoteAddress)
	{
		this.t=t;
		this.level=level;
		this.message=message;
		this.request=request;
		this.remoteAddress=remoteAddress;
	}
	
	public long getT()
	{
		return t;
	}

	public int getLevel()
	{
		return level;
	}

	public String getMessage()
	{
		return message;
	}

	public String getRequest()
	{
		return request;
	}

	public String getRemoteAddress()
	{
		return remoteAddress;
	}
}
