package org.scratch.microwebserver;

import java.util.Vector;

import org.scratch.microwebserver.http.WebService;

public interface ServiceListener
{
	public void log(LogEntry le);
	public void startUp(boolean started);
	public void shutDown(boolean shutDown);
	public void recreate(boolean b,Vector<String> addr);
	public void webServiceAdded(WebService service);
}
