package org.scratch.microwebserver.http.zeminterface;

import org.scratch.microwebserver.http.WebConnection;

import net.zeminvaders.lang.Interpreter;
import net.zeminvaders.lang.SourcePosition;
import net.zeminvaders.lang.runtime.ZemObject;
import net.zeminvaders.lang.runtime.ZemString;

public class GetValueFunction extends MicroWebServerFunction
{
	private WebConnection wb;
	
	public GetValueFunction(final WebConnection wb)
	{
		this.wb=wb;
	}

	@Override
	public ZemObject eval(Interpreter interpreter,SourcePosition pos) throws ZHTMLException
	{
		String key = interpreter.getVariable("key", pos).toZString().toString();
		String value = wb.getGetValue(key);
		if(value!=null)
			return new ZemString(value);
		else
			return Interpreter.NULL;
	}

	@Override
	public int getParameterCount()
	{
		return 1;
	}

	@Override
	public String getParameterName(int index)
	{
		return "key";
	}

}
