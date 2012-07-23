/**
 * Android Micro Web Server
 * Copyright (C) 2011  ScR4tCh
 * Contact scr4tch@scr4tch.org

 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the Lesser GNU General Public License for more details.

 * You should have received a copy of the GNU Lesser General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package org.scratch.microwebserver.http.z3minterface;

import net.zeminvaders.lang.ZemException;

public class ZHTMLException extends ZemException
{

	private static final long serialVersionUID=8116607640025895140L;
	
	private int replycode=500;
		
	public ZHTMLException(int replycode,String message)
	{
		super(message);
		this.replycode=replycode;
	}
	
	public int getReplyCode()
	{
		return replycode;
	}

}
