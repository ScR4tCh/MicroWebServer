package org.scratch.microwebserver.http;

import java.io.IOException;
import java.sql.SQLException;

import org.scratch.microwebserver.data.DBManager;
import org.scratch.microwebserver.properties.PropertyNames;
import org.scratch.microwebserver.properties.ServerProperties;


public abstract class BasicWebService implements WebService
{
	public static final String MIME_HTML="text/html";
	public static final String MIME_JSON="application/json";
	public static final String MIME_XML="application/xml";	
	public static final String MIME_URLE="application/x-www-form-urlencoded";
	
	
	protected DBManager database = DBManager.getInstance(ServerProperties.getInstance().getString(PropertyNames.DATABASE_URL));
	
	protected boolean checkToken(String token)
	{
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
	
	
	
	protected StringBuffer readStream(WebConnection wb) throws WebServiceException
	{
		StringBuffer dataBuffer = new StringBuffer();		
		
		if(wb.getPostDataLength()<=0)
		{
			throw new WebServiceException(411,"Length Required!");
		}
		
		try
		{
			char[] rb = new char[(int)wb.getPostDataLength()];
			wb.getPostData().read(rb);
			dataBuffer.append(new String(rb));
		}
		catch(IOException e)
		{
			throw new WebServiceException(500,"Internal Server Error!");
		}

		return dataBuffer;
	}
	
	public abstract String getUri();
	public abstract WebServiceReply invoke(String[] ppc,int method, String mime, WebConnection wb) throws WebServiceException;
	public abstract boolean acceptsMethod(int method);
	public abstract boolean acceptsMime(String mime);
	public abstract String getMime();

}
