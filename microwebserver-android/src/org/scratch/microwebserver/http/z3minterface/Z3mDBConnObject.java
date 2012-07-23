package org.scratch.microwebserver.http.z3minterface;

import org.scratch.microwebserver.data.DatabaseConnection;

import net.zeminvaders.lang.runtime.ZemObject;

public class Z3mDBConnObject extends ZemObject
{
	private DatabaseConnection dbc;
	
	public Z3mDBConnObject(DatabaseConnection dbc)
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
		if(another instanceof Z3mDBConnObject)
		{
			DatabaseConnection adbc= ((Z3mDBConnObject)another).dbc;
			
			if(adbc.equals(dbc))
				return 1;
			else
				return -1;
		}
			
		return 0;
	}

}
