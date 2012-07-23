package org.scratch.microwebserver.data;

public class NoSuchDatabaseException extends DatabaseManagerException
{
	private static final long serialVersionUID=-5669061701679172237L;

	public NoSuchDatabaseException(String db)
	{
		super("no such database: "+db);
	}
}
