package org.scratch.microwebserver;

import org.scratch.microwebserver.http.WebConnection;

public class RemoteServiceCall
{
	public final long t;
	public final RemoteWebService service;
	public final WebConnection wb;
	
	public RemoteServiceCall(final long t,final RemoteWebService service,final WebConnection wb)
	{
		this.t=t;
		this.service=service;
		this.wb=wb;
	}
	
}
