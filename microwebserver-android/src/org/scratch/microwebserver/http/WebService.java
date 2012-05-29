package org.scratch.microwebserver.http;

public interface WebService
{
	public String getUri();
	public WebServiceReply invoke(String[] ppc,int method, String mime, WebConnection wb) throws WebServiceException;
	public boolean acceptsMethod(int method);
	public boolean acceptsMime(String mime);
	public String getMime();
}
