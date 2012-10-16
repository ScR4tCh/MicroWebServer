package org.scratch.microwebserver.messagebinder;

public interface ServiceCaller
{
	//plain bytes ! ;)
	public byte[] callService(String[] uri,byte[] postdata) throws ServiceCallException;

	public String[] getPossibleInputMimetypes();
	public String getOutputMimeType();
}
