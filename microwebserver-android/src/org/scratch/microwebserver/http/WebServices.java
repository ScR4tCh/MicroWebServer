/**
 * Android Micro Web Server
 * Copyright (C) 2011  ScR4tCh
 * Contact scr4tch@scr4tch.org

 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the Lesser GNU General Public License for more details.

 * You should have received a copy of the GNU Lesser General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package org.scratch.microwebserver.http;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.scratch.microwebserver.data.DatabaseManagerException;
import org.scratch.microwebserver.http.services.GetUsersService;
import org.scratch.microwebserver.http.services.LogInService;
import org.scratch.microwebserver.http.services.LogOutService;
import org.scratch.microwebserver.messagebinder.MethodTypes;


public class WebServices
{
	public static final int METHOD_GET=MethodTypes.GET.ordinal();
	public static final int METHOD_POST=MethodTypes.POST.ordinal();
	public static final int METHOD_HEAD=2;
	
	private static WebServices instance;
	
	private Map<String,Map<String,WebService>> services = new HashMap<String,Map<String,WebService>>();
	
	private WebServices()
	{
		//init default services (FIXME: move to Server class !!!)
		
		Map<String,WebService> m = new HashMap<String,WebService>();
		WebService w0;
		try
		{
			w0=new LogInService();
			WebService w2 = new LogOutService();
			WebService w3 = new GetUsersService();
			
			m.put(w0.getUri(),w0);
			m.put(w2.getUri(),w2);
			m.put(w3.getUri(),w3);
		}
		catch(DatabaseManagerException e)
		{
			//TODO: log !
			e.printStackTrace();
		}
		
		services.put("server",m);
	}
		
	public static final WebServices getInstance()
	{
		if(instance==null)
			instance=new WebServices();
			
		return instance;
	}
	
	//for now as simple as possible
	public void registerService(String serviceurl,String p,WebService ws)
	{
		if(!services.containsKey(serviceurl))
		{
			services.put(serviceurl,new HashMap<String,WebService>());
		}
		
			services.get(serviceurl).put(p,ws);
	}

	public boolean exists(String string)
	{		
		//should not be null !
		String[] p;
		
		if(string.startsWith("/"))
			p = string.substring(1).split("/");
		else
			p = string.split("/");
		
		if(p.length>=2 && services.containsKey(p[0]))
		{
			if(services.get(p[0]).containsKey(p[1]))
			{
				return true;
			}
		}
				
		return false;
	}
	
	public WebServiceReply invoke(String p,int method,String mime,WebConnection wb) throws WebServiceException
	{
		System.err.println("invoking service: "+p+" with mime: "+mime);
		//log ?!?
		
		if(!exists(p))
		{
			throw new WebServiceException(404,"Not found");
		}
		else
		{
			String[] pp;
				
			if(p.startsWith("/"))
				pp = p.substring(1).split("/");
			else
				pp = p.split("/");
				
			WebService service = services.get(pp[0]).get(pp[1]);
				
			if(!service.acceptsMethod(method))
				throw new WebServiceException(405,"Method not allowed !");
				
			if(method==METHOD_POST && !service.acceptsMime(mime))
				throw new WebServiceException(425,"Unsupported Media Type !");
			
						
			
			String[] ppc = new String[pp.length/*-1*/];
			
			//TODO: before, we dropped the "group" using arraycopy(pp,1,ppc,0,ppc.length), check what would be better ...
			System.arraycopy(pp,0,ppc,0,ppc.length);
			return service.invoke(ppc,method,mime,wb);
		}
	}
	
	public void dumpServices()
	{
		Iterator<String> servicesIt = services.keySet().iterator();
		while(servicesIt.hasNext())
		{
			String sv = servicesIt.next();
			System.err.println(sv+"\n========================");
			
			Iterator svsi = services.get(sv).keySet().iterator();
			while(svsi.hasNext())
			{
				System.err.println("\t> "+svsi.next());
			}
		}
	}

	public static void replace(final WebServices services2)
	{
		instance=services2;
	}
}
