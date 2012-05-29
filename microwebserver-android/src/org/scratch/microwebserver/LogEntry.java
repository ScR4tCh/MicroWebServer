package org.scratch.microwebserver;

public class LogEntry
{
	private long t;
	private int level;
	private String message;
	private String request;
	private String remoteAddress;
	
	public LogEntry(long t,int level,String message,String request,String remoteAddress)
	{
		this.t=t;
		this.level=level;
		this.message=message;
		this.request=request;
		this.remoteAddress=remoteAddress;
	}
	
	public long getT()
	{
		return t;
	}

	public int getLevel()
	{
		return level;
	}

	public String getMessage()
	{
		return message;
	}

	public String getRequest()
	{
		return request;
	}

	public String getRemoteAddress()
	{
		return remoteAddress;
	}
}
