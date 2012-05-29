package org.scratch.microwebserver.http.zeminterface;

import java.sql.SQLException;

import org.scratch.microwebserver.http.WebConnection;

import net.zeminvaders.lang.Interpreter;
import net.zeminvaders.lang.SourcePosition;
import net.zeminvaders.lang.runtime.ZemObject;

public class LogoutFunction extends MicroWebServerFunction
{
	private WebConnection wb;

	public LogoutFunction(WebConnection wb)
	{
		this.wb=wb;
	}

	@Override
	public ZemObject eval(Interpreter interpreter,SourcePosition pos)throws ZHTMLException
	{
		if(wb.getCookieCrumb("TOKEN")!=null)
		{
			try
			{
				wb.getDatabase().invalidateToken(wb.getCookieCrumb("TOKEN").toString());
			}
			catch(SQLException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		//redirect
		wb.setExtra("Location","/");
		wb.setExtra("Cache-Control","no-cache");
		wb.setStatusCode(307);
		
		return null;
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
