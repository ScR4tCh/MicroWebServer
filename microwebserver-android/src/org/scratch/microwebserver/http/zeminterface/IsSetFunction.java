package org.scratch.microwebserver.http.zeminterface;

import net.zeminvaders.lang.Interpreter;
import net.zeminvaders.lang.SourcePosition;
import net.zeminvaders.lang.UnsetVariableException;
import net.zeminvaders.lang.runtime.ZemBoolean;
import net.zeminvaders.lang.runtime.ZemObject;

public class IsSetFunction extends MicroWebServerFunction
{

	@Override
	public ZemObject eval(Interpreter interpreter,SourcePosition pos)throws ZHTMLException
	{
		try
		{
			ZemObject zo = interpreter.getVariable("object", pos);
			return ZemBoolean.valueOf(!zo.equals(Interpreter.NULL));
		}catch(UnsetVariableException uve){return new ZemBoolean(false);}
	}

	@Override
	public int getParameterCount()
	{
		return 1;
	}

	@Override
	public String getParameterName(int index)
	{
		return "object";
	}

}
