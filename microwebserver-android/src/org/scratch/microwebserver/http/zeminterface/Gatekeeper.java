package org.scratch.microwebserver.http.zeminterface;

import java.sql.SQLException;

import org.scratch.microwebserver.data.DBManager;
import org.scratch.microwebserver.http.WebConnection;

import net.zeminvaders.lang.Interpreter;
import net.zeminvaders.lang.SourcePosition;
import net.zeminvaders.lang.runtime.ZemBoolean;
import net.zeminvaders.lang.runtime.ZemObject;

public class Gatekeeper extends MicroWebServerFunction
{
	private WebConnection wb;
	
	
	public Gatekeeper(WebConnection wb)
	{
		this.wb=wb;
	}

	private boolean checkToken(String token,DBManager database)
	{
		//System.err.println("GATEKEEPER -> TOKEN:"+token);
		
		if(token==null)
			return false;
			
		try
		{
			if(database.checkTokenValid(token))
			{
				if(!database.checkTokenExpired(token))
				{
					return true;
				}
			}
		}catch(SQLException se)
		 {
			return false;
		 }
		
		return false;
		
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
		return new ZemBoolean(checkToken(wb.getCookie().get("TOKEN"),wb.getDatabase()));
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
