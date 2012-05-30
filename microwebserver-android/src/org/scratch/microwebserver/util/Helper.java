/**
 * Android Micro Web Server
 * Copyright (C) 2011  ScR4tCh
 * Contact scr4tch@scr4tch.org

 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the Lesser GNU General Public License for more details.

 * You should have received a copy of the GNU Lesser General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package org.scratch.microwebserver.util;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class Helper
{
	private static SimpleDateFormat formatter = new SimpleDateFormat("dd-MMM-yy HH:mm");
	
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

	//Apache code  https://www.apache.org/licenses/LICENSE-2.0
	//taken from http://stackoverflow.com/questions/813710/java-1-6-determine-symbolic-links
	public static boolean isSymlink(File file) throws IOException
	{
		  if (file == null)
			  throw new NullPointerException("File must not be null");
		  
		  File canon;
		  
		  if (file.getParent() == null)
		  {
		    canon = file;
		  }
		  else
		  {
		    File canonDir = file.getParentFile().getCanonicalFile();
		    canon = new File(canonDir, file.getName());
		  }
		  
		  return !canon.getCanonicalFile().equals(canon.getAbsoluteFile());
		}

	public static String fileSizeFormat(long length)
	{
		return fileSizeFormat(length,0);
	}
	
	private static String fileSizeFormat(long length,int lev)
	{
		if(length>1024 && lev<6)
		{
			return fileSizeFormat(length/1024,lev+1);
		}
		else
		{
			switch(lev)
			{
				case 0:	return ""+length;
				case 1: return length+"K";
				case 2: return length+"M";
				case 3: return length+"G";
				case 4: return length+"T";
				default: return length+"E";
			}
		}
	}

	public static String stdDateFormat(long t)
	{
		return formatter.format(t);
	}


}
