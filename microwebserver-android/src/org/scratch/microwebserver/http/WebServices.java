package org.scratch.microwebserver.http;

import java.util.HashMap;
import java.util.Map;

import org.scratch.microwebserver.http.services.GetUsersService;
import org.scratch.microwebserver.http.services.LogInService;
import org.scratch.microwebserver.http.services.LogOutService;


public class WebServices
{
	public static final int METHOD_GET=0;
	public static final int METHOD_POST=1;
	
	private static WebServices instance;
	
	private Map<String,Map<String,WebService>> services = new HashMap<String,Map<String,WebService>>();
	
	private WebServices()
	{
		//init default services (FIXME: move to Server class !!!)
		
		Map<String,WebService> m = new HashMap<String,WebService>();
		WebService w0 = new LogInService();
		WebService w2 = new LogOutService();
		WebService w3 = new GetUsersService();
		
		m.put(w0.getUri(),w0);
		m.put(w2.getUri(),w2);
		m.put(w3.getUri(),w3);
		
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
		if(services.containsKey(serviceurl))
		{
			services.get(serviceurl).put(p,ws);
		}
		else
		{
			Map<String,WebService> sm = new HashMap<String,WebService>();
			sm.put(p,ws);
			services.put(serviceurl,sm);
		}
	}

	public boolean exists(String string)
	{
		//should not be null !
		String[] p;
		
		if(string.startsWith("/"))
			p = string.substring(1).split("/");
		else
			p = string.split("/");
		
		if(services.containsKey(p[0]))
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
		//System.err.println("invoking service: "+p+" with mime: "+mime);
		//log ?!?
		
		if(!exists(p))
			throw new WebServiceException(404,"Not found");
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
			
						
			if(pp.length>2)
			{
				String[] ppc = new String[pp.length-2];
				System.arraycopy(pp,2,ppc,0,ppc.length);
				return service.invoke(ppc,method,mime,wb);
			}
			else
			{
				return service.invoke(null,method,mime,wb);
			}
		}
		
	}
}
