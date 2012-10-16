package org.scratch.microwebserver.messagebinder;

public class ServiceCallException extends Exception
{

	private static final long serialVersionUID=-7325901183707465244L;

	private final String packagename;
	private final String service;
	
	public ServiceCallException(String packagename,String service,String msg)
	{
		super(msg);
		
		this.packagename=packagename;
		this.service=service;
	}

	public String getPackagename()
	{
		return packagename;
	}

	public String getService()
	{
		return service;
	}
	
	
}
