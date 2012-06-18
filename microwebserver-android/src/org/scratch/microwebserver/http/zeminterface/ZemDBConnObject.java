package org.scratch.microwebserver.http.zeminterface;

import org.scratch.microwebserver.data.DatabaseConnection;

import net.zeminvaders.lang.runtime.ZemObject;

public class ZemDBConnObject extends ZemObject
{
	private DatabaseConnection dbc;
	
	public ZemDBConnObject(DatabaseConnection dbc)
	{
		this.dbc=dbc;
	}
	
	public DatabaseConnection getDatabaseConnection()
	{
		return dbc;
	}
	
	//TODO: check !
	@Override
	public int compareTo(ZemObject another)
	{
		if(another instanceof ZemDBConnObject)
		{
			DatabaseConnection adbc= ((ZemDBConnObject)another).dbc;
			
			if(adbc.equals(dbc))
				return 1;
			else
				return -1;
		}
			
		return 0;
	}

}
