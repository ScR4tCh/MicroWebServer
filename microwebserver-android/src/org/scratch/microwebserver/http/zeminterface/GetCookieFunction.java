package org.scratch.microwebserver.http.zeminterface;

import java.util.Map;

import org.scratch.microwebserver.http.WebConnection;

import net.zeminvaders.lang.Interpreter;
import net.zeminvaders.lang.SourcePosition;
import net.zeminvaders.lang.runtime.ZemObject;
import net.zeminvaders.lang.runtime.ZemString;

public class GetCookieFunction extends MicroWebServerFunction
{
	private Map<String,String> cookie;
	
	public GetCookieFunction(WebConnection wb)
	{
		cookie=wb.getCookie();
	}

	@Override
	public int compareTo(ZemObject o)
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public ZemObject eval(Interpreter interpreter,SourcePosition pos)
	{
		String key = interpreter.getVariable("key", pos).toZString().toString();
		String value = cookie.get(key);
		if(value!=null)
			return new ZemString(cookie.get(key));
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
