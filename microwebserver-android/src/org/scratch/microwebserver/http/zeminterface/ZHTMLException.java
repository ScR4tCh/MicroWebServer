package org.scratch.microwebserver.http.zeminterface;

import net.zeminvaders.lang.ZemException;

public class ZHTMLException extends ZemException
{

	private static final long serialVersionUID=8116607640025895140L;
	
	private int replycode=500;
		
	public ZHTMLException(int replycode,String message)
	{
		super(message);
		this.replycode=replycode;
	}
	
	public int getReplyCode()
	{
		return replycode;
	}

}
