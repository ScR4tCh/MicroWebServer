package org.scratch.microwebserver.data;

public class DatabaseAlreadyExistsException extends DatabaseManagerException
{

	public DatabaseAlreadyExistsException(String string)
	{
		super(string);
	}

	private static final long serialVersionUID=5692323830745507039L;

}
