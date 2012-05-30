/**
 * Android Micro Web Server
 * Copyright (C) 2011  ScR4tCh
 * Contact scr4tch@scr4tch.org

 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the Lesser GNU General Public License for more details.

 * You should have received a copy of the GNU Lesser General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package org.scratch.microwebserver.http;

public interface WebConnectionListener
{
	public static final int LOGLEVEL_DEBUG=0;
	public static final int LOGLEVEL_INFO=1;
	public static final int LOGLEVEL_NORMAL=2;
	public static final int LOGLEVEL_WARN=3;
	public static final int LOGLEVEL_ERROR=4;
	
	public void log(long t,int lev,String client,String request,String msg);
}
