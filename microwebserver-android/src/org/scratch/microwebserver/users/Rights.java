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
