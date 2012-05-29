package org.scratch.microwebserver.http.zeminterface;

import org.scratch.microwebserver.http.WebConnection;

import net.zeminvaders.lang.Interpreter;
import net.zeminvaders.lang.SourcePosition;
import net.zeminvaders.lang.runtime.ZemObject;

public class RedirectFunction extends MicroWebServerFunction
{
	private WebConnection wb;
	
	public RedirectFunction(WebConnection wb)
	{
		this.wb=wb;
	}

	
	@Override
	public ZemObject eval(Interpreter interpreter,SourcePosition pos) throws ZHTMLException
	{
		String location = interpreter.getVariable("location", pos).toZString().toString();
        
		wb.setExtra("Location",location);
		wb.setStatusCode(307); //307 Temporary Redirect
		
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
		return "location";
	}
	
}
