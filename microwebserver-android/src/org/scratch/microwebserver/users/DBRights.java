package org.scratch.microwebserver.users;

public class DBRights
{
	//31 possible "fields"
	public static final int SELECT=1;
	public static final int INSERT=2;
	public static final int UPDATE=4;
	public static final int DELETE=8;
	public static final int CREATE_TABLE=16;
	public static final int ALTER_TABLE=32;
	public static final int DROP_TABLE=64;
	public static final int GRANT=128;
	public static final int ADMIN=256;
	//..
	
	public static boolean hasRight(int bitmask,int right)
	{
		if((bitmask & right) == right)
		{
			return true;
		}
		else
		{
			return false;
		}
	}
}
