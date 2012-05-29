package org.scratch.microwebserver.http;

public class WebServiceException extends Exception
{
	private static final long serialVersionUID=-785426629377842368L;
	
	private int replycode;
	
	public WebServiceException(int replycode,String message)
	{
		super(message);
		this.replycode=replycode;
	}
	
	public int getReplyCode()
	{
		return replycode;
	}
}
