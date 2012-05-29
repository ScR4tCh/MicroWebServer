/**
 * Android Micro Web Server
 * Copyright (C) 2011  ScR4tCh
 * Contact scr4tch@scr4tch.org

 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the Lesser GNU General Public License for more details.

 * You should have received a copy of the GNU Lesser General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package org.scratch.microwebserver.util;

import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class Helper
{
	public static String dumpMap(Map m,String keyDescriptor,String valueDescriptor)
	{
		String s="";
		
		return s;
	}
	
	public static boolean isNumeric(String str)  
	{  
	  try  
	  {  
	    double d = Double.parseDouble(str);  
	  }  
	  catch(NumberFormatException nfe)  
	  {  
	    return false;  
	  }  
	  return true;  
	}
	
	public static boolean isInt(String str)  
	{  
	  try  
	  {  
	    Integer i = Integer.parseInt(str);  
	  }  
	  catch(NumberFormatException nfe)  
	  {  
	    return false;  
	  }  
	  return true;  
	}
	
	/**
	 * THX TO http://stackoverflow.com/a/2904266
	 */
	public static <T, E> Set<T> getKeysByValue(Map<T, E> map, E value)
	{
	     Set<T> keys = new HashSet<T>();
	     for (Entry<T, E> entry : map.entrySet())
	     {
	         if (value.equals(entry.getValue()))
	         {
	             keys.add(entry.getKey());
	         }
	     }
	     return keys;
	}



}
