package org.scratch.microwebserver.http.services;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.Map;

import org.json.scratch.fork.SJSONArray;
import org.json.scratch.fork.SJSONException;
import org.json.scratch.fork.SJSONObject;
import org.scratch.microwebserver.http.BasicWebService;
import org.scratch.microwebserver.http.WebConnection;
import org.scratch.microwebserver.http.WebServiceException;
import org.scratch.microwebserver.http.WebServiceReply;
import org.scratch.microwebserver.http.WebServices;



public class GetUsersService extends BasicWebService
{

	@Override
	public boolean acceptsMethod(int method)
	{
		if(method==WebServices.METHOD_POST || method==WebServices.METHOD_GET)
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
	public WebServiceReply invoke(String[] ppc,int method,String mime,WebConnection wb) throws WebServiceException
	{
		if(method==WebServices.METHOD_GET)
		{
			if(checkToken(wb.getCookie().get("TOKEN")))
			{
				try
				{
					return getUsers();
				}
				catch(SJSONException e)
				{
					throw new WebServiceException(400,"Bad Request!");
				}
			}
			else
			{
				throw new WebServiceException(401,"Unauthorized!");
			}
		}
		else if(method==WebServices.METHOD_POST)
		{
		
			StringBuffer dataBuffer = readStream(wb);
			
			SJSONObject json;
			try
			{
				json=new SJSONObject(dataBuffer.toString());
			
				
				if(json.has("token") && checkToken(json.getString("token")))
				{
					return getUsers();
				}
				else
				{
					throw new WebServiceException(401,"Unauthorized!");
				}
			}
			catch(SJSONException e)
			{
				throw new WebServiceException(400,"Bad Request!");
			}
		}
		else
		{
			throw new WebServiceException(405,"Method not Allowed !");
		}
	}
	
	private WebServiceReply getUsers() throws SJSONException, WebServiceException
	{
		WebServiceReply rret = new WebServiceReply();
		
		Map<Integer, String> result;
		try
		{
			result=database.getUsers();
			Iterator<Integer> ri = result.keySet().iterator();
			SJSONArray ret = new SJSONArray();
			
			while(ri.hasNext())
			{
				SJSONObject djo = new SJSONObject();
				Object uid = ri.next();
				djo.put("id",uid);
				djo.put("name",result.get(uid));
				
				//get further info ...
				// online ?
				// lastonline ?
				// session ?
				// lastsessio ?
				
				ret.put(djo);
			}
			
			rret.setData(new StringBuffer(ret.toString()));
			rret.setMime(MIME_JSON);
			rret.setLength(ret.toString().getBytes().length);
			rret.setDate(System.currentTimeMillis());
			
			return rret;
		}
		catch(SQLException e)
		{
			e.printStackTrace();
			throw new  WebServiceException(500,"Internal Server Error ! "+e.getMessage() );
		}
		
	}

	@Override
	public String getMime()
	{
		return MIME_JSON;
	}

	@Override
	public String getUri()
	{
		return "getUsers";
	}

}
