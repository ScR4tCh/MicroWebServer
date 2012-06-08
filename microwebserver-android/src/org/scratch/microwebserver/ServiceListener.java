package org.scratch.microwebserver;

public interface ServiceListener
{
	public void log(LogEntry le);
	public void startUp(boolean started);
	public void shutDown(boolean shutDown);
}
