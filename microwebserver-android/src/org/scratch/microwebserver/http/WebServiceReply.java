package org.scratch.microwebserver.http;

public class WebServiceReply
{
	protected String mime;
	protected long date;
	protected long length;
	
	protected StringBuffer data;

	public final String getMime()
	{
		return mime;
	}

	public final void setMime(String mime)
	{
		this.mime=mime;
	}

	public final long getDate()
	{
		return date;
	}

	public final void setDate(long date)
	{
		this.date=date;
	}

	public final long getLength()
	{
		return length;
	}

	public final void setLength(long length)
	{
		this.length=length;
	}

	public final StringBuffer getData()
	{
		return data;
	}

	public final void setData(StringBuffer data)
	{
		this.data=data;
	}
	
	
}
