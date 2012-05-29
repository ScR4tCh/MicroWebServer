package org.scratch.microwebserver.http;

public interface WebConnectionListener
{
	public static final int LOGLEVEL_DEBUG=0;
	public static final int LOGLEVEL_INFO=1;
	public static final int LOGLEVEL_NORMAL=2;
	public static final int LOGLEVEL_WARN=3;
	public static final int LOGLEVEL_ERROR=4;
	
	public void log(int lev,String client,String request,String msg);
}
