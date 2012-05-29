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
