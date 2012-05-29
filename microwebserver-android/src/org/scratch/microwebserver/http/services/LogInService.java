package org.scratch.microwebserver.http.services;

import java.sql.SQLException;

import org.json.scratch.fork.SJSONException;
import org.json.scratch.fork.SJSONObject;
import org.scratch.microwebserver.http.BasicWebService;
import org.scratch.microwebserver.http.WebConnection;
import org.scratch.microwebserver.http.WebServiceException;
import org.scratch.microwebserver.http.WebServiceReply;
import org.scratch.microwebserver.http.WebServices;


public class LogInService extends BasicWebService
{

	@Override
	public boolean acceptsMethod(int method)
	{
		if(method==WebServices.METHOD_POST)
			return true;
		
		return false;
	}

	@Override
	public boolean acceptsMime(String mime)
	{
		if(mime!=null && mime.equals(MIME_JSON))
			return true;
		
		return false;
	}

	@Override
	public String getMime()
	{
		return MIME_JSON;
	}

	@Override
	public WebServiceReply invoke(String[] ppc,int method,String mime,WebConnection wb) throws WebServiceException
	{
		try
		{
			SJSONObject authobj=new SJSONObject(readStream(wb).toString());
			if(authobj.get("username")!=null && authobj.get("password")!=null)
			{
				String token=database.generateToken(authobj.getString("username"),authobj.getString("password"));
	
				if(token!=null)
				{
					SJSONObject ret=new SJSONObject();
					ret.put("token",token);
					
					WebServiceReply rret = new WebServiceReply();
					rret.setData(new StringBuffer(ret.toString()));
					rret.setLength(ret.toString().getBytes().length);
					rret.setMime(MIME_JSON);
					rret.setDate(System.currentTimeMillis());
					
					return rret;
				}
				else
				{
					throw new WebServiceException(401,"Unauthorized!");
				}
			}
			else
			{
				throw new WebServiceException(400,"Bad Request!");
			}
		}catch(SJSONException je)
		 {
			throw new WebServiceException(400,"Bad Request!");
		 }
		catch(SQLException e)
		{
			throw new WebServiceException(500,"Internal Server Error!");
		}
	}

	@Override
	public String getUri()
	{
		return "logIn";
	}

}
