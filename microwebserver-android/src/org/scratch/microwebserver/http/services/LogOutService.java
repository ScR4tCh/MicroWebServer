package org.scratch.microwebserver.http.services;

import java.sql.SQLException;

import org.json.scratch.fork.SJSONException;
import org.json.scratch.fork.SJSONObject;
import org.scratch.microwebserver.http.BasicWebService;
import org.scratch.microwebserver.http.WebConnection;
import org.scratch.microwebserver.http.WebServiceException;
import org.scratch.microwebserver.http.WebServiceReply;
import org.scratch.microwebserver.http.WebServices;


public class LogOutService extends BasicWebService
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
			SJSONObject json=new SJSONObject(readStream(wb).toString());
			if(json.has("token"))
			{
				String token=json.getString("token");
				
				database.invalidateToken(token);	
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
		
		WebServiceReply rret = new WebServiceReply();
		rret.setMime(MIME_JSON);
		rret.setDate(System.currentTimeMillis());
		
		return rret;
	}

	@Override
	public String getUri()
	{
		return "logOut";
	}

}
