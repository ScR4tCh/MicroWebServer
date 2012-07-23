/**
 * Android Micro Web Server
 * Copyright (C) 2011  ScR4tCh
 * Contact scr4tch@scr4tch.org

 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the Lesser GNU General Public License for more details.

 * You should have received a copy of the GNU Lesser General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package org.scratch.microwebserver.http;

import java.io.IOException;
import java.sql.SQLException;

import org.scratch.microwebserver.data.DBManager;
import org.scratch.microwebserver.data.DatabaseManagerException;
import org.scratch.microwebserver.properties.PropertyNames;
import org.scratch.microwebserver.properties.ServerProperties;


public abstract class BasicWebService implements WebService
{
	public static final String MIME_HTML="text/html";
	public static final String MIME_JSON="application/json";
	public static final String MIME_XML="application/xml";	
	public static final String MIME_URLE="application/x-www-form-urlencoded";
	
	
	protected DBManager database;
	
	
	public BasicWebService() throws DatabaseManagerException
	{
		database=DBManager.getInstance(ServerProperties.getInstance().getString(PropertyNames.DATABASES_PATH.toString()));
	}
	
	//for basic auth !
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
