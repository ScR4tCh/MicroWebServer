/**
 * Android Micro Web Server
 * Copyright (C) 2011  ScR4tCh
 * Contact scr4tch@scr4tch.org

 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the Lesser GNU General Public License for more details.

 * You should have received a copy of the GNU Lesser General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package org.scratch.microwebserver.users;

public class Rights
{
	//31 possible "fields"
	public static final int WEBACCESS=1;
	public static final int RIGHT2=2;
	public static final int RIGHT3=4;
	public static final int RIGHT4=8;
	public static final int RIGHT5=16;
	public static final int RIGHT6=32;
	public static final int RIGHT7=64;
	public static final int RIGHT8=128;
	public static final int RIGHT9=256;
	public static final int RIGHT10=512;
	//..
	
	public static boolean hasRight(int bitmask,int right)
	{
		if((bitmask & right) == right)
		{
			return true; 
		}
		
		return false;
	}
}
