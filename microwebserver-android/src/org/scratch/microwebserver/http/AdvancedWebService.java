package org.scratch.microwebserver.http;

import java.util.Map;

public interface AdvancedWebService extends WebService
{
	public Map<String,Param> getPrerequesites();
}
