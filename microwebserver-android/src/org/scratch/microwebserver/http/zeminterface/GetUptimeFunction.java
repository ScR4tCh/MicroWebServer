package org.scratch.microwebserver.http.zeminterface;

import org.scratch.microwebserver.http.MicroWebServer;

import net.zeminvaders.lang.Interpreter;
import net.zeminvaders.lang.SourcePosition;
import net.zeminvaders.lang.runtime.ZemNumber;
import net.zeminvaders.lang.runtime.ZemObject;

public class GetUptimeFunction extends MicroWebServerFunction
{	
	private MicroWebServer mws;
	
	public GetUptimeFunction(MicroWebServer mws)
	{
		this.mws=mws;
	}

	@Override
	public ZemObject eval(Interpreter interpreter,SourcePosition pos) throws ZHTMLException
	{
		return new ZemNumber(mws.getUptime()/1000/1000);
	}

	@Override
	public int getParameterCount()
	{
		return 0;
	}

	@Override
	public String getParameterName(int index)
	{
		return null;
	}

}
