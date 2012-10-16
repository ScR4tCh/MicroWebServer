/**
 * Android Micro Web Server
 * Copyright (C) 2011  ScR4tCh
 * Contact scr4tch@scr4tch.org

 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the Lesser GNU General Public License for more details.

 * You should have received a copy of the GNU Lesser General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package org.scratch.microwebserver.http;

import java.io.Serializable;

public class WebServiceReply implements Serializable
{
	private static final long serialVersionUID=-2868172616086413054L;
	
	protected String mime;
	protected long date;
	protected long length;
	
	protected byte[] data;

	public final String getMime()
	{
		return mime;
	}

	public final void setMime(String mime)
	{
		this.mime=mime;
	}

	public final long getDate()
	{
		return date;
	}

	public final void setDate(long date)
	{
		this.date=date;
	}

	public final long getLength()
	{
		return length;
	}

	public final void setLength(long length)
	{
		this.length=length;
	}

	public final byte[] getData()
	{
		return data;
	}

	public final void setData(byte[] data)
	{
		if(data!=null)
			length=data.length;
		this.data=data;
	}
	
	
}
