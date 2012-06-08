package org.scratch.microwebserver.users;

public class TableRights
{
	//31 possible "fields"
	public static final int SELECT=1;
	public static final int INSERT=2;
	public static final int UPDATE=4;
	public static final int DELETE=8;
	public static final int CREATE=16;
	public static final int ALTER=32;
	public static final int DROP=64;
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
