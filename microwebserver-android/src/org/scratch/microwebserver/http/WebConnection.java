/**
 * Android Micro Web Server
 * Copyright (C) 2011  ScR4tCh
 * Contact scr4tch@scr4tch.org

 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the Lesser GNU General Public License for more details.

 * You should have received a copy of the GNU Lesser General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
/**
 * @author scratch
 * 
 * A very minimalistic "webserver" that is able to process
 * get and post commands received by any client.
 * This implementation is totally stateless and does not provide
 * connection "keep-alive" functionality.
 * After each request/reply circle, the connection is cancelled !
 * 
 * TODO: implement the "WebServices" class to be able to define external request handlers (defined as xml maybe ...)!
 */

package org.scratch.microwebserver.http;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.FileNameMap;
import java.net.Socket;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import org.scratch.microwebserver.data.DBManager;
import org.scratch.microwebserver.http.zeminterface.ZHTMLException;
import org.scratch.microwebserver.properties.PropertyNames;
import org.scratch.microwebserver.properties.ServerProperties;


public class WebConnection
{
	private static final String EOL=(char)13+""+(char)10;
	
	//TODO: auto generate Error pages !
	private static final String P403="<HTML><HEAD><TITLE>403 Forbidden</TITLE></HEAD><BODY><H1>403 Forbidden</H1>The server does not allow acccess, perhaps you must authenticate ...</BODY></HTML>";
	private static final String P404="<HTML><HEAD><TITLE>404 Not Found</TITLE></HEAD><BODY><H1>404 Not Found</H1>The requested page or service was not found on this server</BODY></HTML>";

	//new page setup ...
	private String request;
	private String[] urlReq;
	private String mimetype=null;
	private long length=-1;
	private boolean post=false;
	private Map<String,String> cookie = new HashMap<String,String>();
	private Map<String,String> setCookie = new HashMap<String,String>();
	private Map<String,String> extras = new HashMap<String,String>();
	private int statuscode=200;
	
	//request params
	private String[] params;
	
	private Vector<String> header = new Vector<String>();
	
	//connection specific
	private MicroWebServer server;
	private Socket sock;
	private BufferedReader in;
	private Map<String,String> getData = new HashMap<String,String>();
	private OutputStream out;
	private StringBuffer outBuffer = new StringBuffer();
	private DBManager database=DBManager.getInstance(ServerProperties.getInstance().getString(PropertyNames.DATABASE_URL));
	private WebServices services = WebServices.getInstance();
	
	
	private volatile boolean connected=false;
		
	public WebConnection(final Socket sock,final MicroWebServer server)throws IOException
	{
		this.server=server;
		this.sock=sock;
		this.sock.setSoTimeout(5000);
		in=new BufferedReader(new InputStreamReader(sock.getInputStream()));
		out=sock.getOutputStream();
		
		connected=true;

	}

	public void process() throws IOException
	{
		boolean first=true;
		post=false;
		
		String buffer;
		
		//read http header
		while((buffer=in.readLine())!=null && buffer.length()>0 && connected)
		{
			if(first)
			{
				params=buffer.split("\\s");
		
				request = params[1];
				
				int urlei = params[1].indexOf('?');
				if(urlei>0)
				{
					getData=getFormData(params[1].substring(urlei+1,params[1].length()));
					params[1]=params[1].substring(0,urlei);
				}
				
				urlReq=params[1].split("/");
				first=false;
				
				if(params[0].toLowerCase().equals("post"))
				{
					post=true;
				}
			}
			
			
			
			if(buffer.length()==0)
				break;
			
			if(buffer.toLowerCase().startsWith("cookie:"))
			{
				processCookies(buffer.substring(buffer.indexOf(':')+1,buffer.length()).trim());
			}
			else if(post && buffer.toLowerCase().startsWith("content-type:"))
			{
				mimetype=buffer.substring(buffer.indexOf(':')+1,buffer.length()).trim();
			}
			else if(post && buffer.toLowerCase().startsWith("content-length:"))
			{
				length=Long.valueOf(buffer.substring(buffer.indexOf(':')+1,buffer.length()).trim()).longValue();
			}
			
			
			header.add(buffer);
		}		
		
		File f = new File(ServerProperties.getInstance().getString(PropertyNames.SERVER_ROOT)+params[1]);
		
		if(f.exists() && f.isDirectory())
		{
			String fp = f.getPath();
			if(!fp.endsWith("/"))
				fp=fp+"/";
			
			File possIndex;
			
			if((possIndex=new File(fp+"index.html")).exists())
			{
				f=possIndex;
			}			
			else if((possIndex=new File(fp+"index.htm")).exists())
			{
				f=possIndex;
			}
			else if((possIndex=new File(fp+"index.zhtml")).exists())
			{
				f=possIndex;
			}
			else
			{
				reply(403,"Forbidden","text/html","utf-8",P403);
				return;
			}
		}
		
		if(f.exists() && f.isFile())
		{
			StringBuffer sendBuffer = new StringBuffer();
			
			
						
			if(f.getName().toLowerCase().endsWith(".zhtml"))
			{
				
				
				String sb = "";
				BufferedReader bin = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
				String b;
				while((b=bin.readLine())!=null)
					sb+=b+"\n";		
				bin.close();
				
				sendBuffer.append(""+EOL);
				
				try
				{
					sendBuffer.append(ZHTMProcessor.process(this,sb));
				}catch(ZHTMLException e)
				 {
					reply(e.getReplyCode(),e.getMessage(),"text/html","utf-8","<html><head><title>ERROR</title></head><body><h1>"+e.getReplyCode()+" "+getCodeDescription(e.getReplyCode())+"</h1>"+e.getMessage().replace("\n","<br/>")+"</body></html>");
					return;
				 }
				
				send("HTTP/1.1 "+statuscode);
				sendExtras();
				send("Server: scr4tch micro web server");
				
				
				if(statuscode>=200 && statuscode<300)
				{
					send("Content-Type: text/html");
					
					if(setCookie.size()>0)
					{
						String cookie ="";
						Iterator<String> cookieIt = setCookie.keySet().iterator();
						while(cookieIt.hasNext())
						{
							String k = cookieIt.next();
							cookie+=k+"="+setCookie.get(k);
							
							if(cookieIt.hasNext())
								cookie+=";";
						}
						
						send("Set-Cookie: "+cookie);
					}
					
	
					send(sendBuffer.toString());
				}
			}
			else
			{
				//System.err.println("REQUEST STD FILE ! "+params[1]);
				
				send("HTTP/1.1 200");
				send("Server: scr4tch micro web server");
				send("Content-Type: "+getMimeType("file://"+params[1]));
				byte[] fbuffer = new byte[4096];
				InputStream fin = new FileInputStream(f);
				int r;
				send("Content-Length: "+fin.available());
				send("");
				out.write(outBuffer.toString().getBytes());
				
				outBuffer=new StringBuffer();
				
				while((r=fin.read(fbuffer))>0)
				{
					out.write(fbuffer,0,r);
				}
				fin.close();
			}
			
			close();
		}
		else
		{
			if(services.exists(params[1]))
			{
				try
				{
					WebServiceReply ret = services.invoke(params[1],post?WebServices.METHOD_POST:WebServices.METHOD_GET,mimetype,this);
					send("HTTP/1.1 200");
					send("Server: scr4tch micro web server");
					send("Content-Type: "+ret.getMime());
					send("Content-Length: "+ret.getLength());
					send("");
					
					if(ret!=null && ret.getData()!=null)
						send(ret.getData().toString());
					
					close();
				}
				catch(WebServiceException wse)
				{
					//wse.printStackTrace();
					//TODO: auto generate Error pages ! [OR: read from FS -> config "page overrides"]
					reply(wse.getReplyCode(),wse.getMessage(),"text/html","utf-8","");
				}
			}
			else
			{
				log(WebConnectionListener.LOGLEVEL_NORMAL,"404 NOT FOUND : "+params[1]);
				reply(404,"Not Found","text/html","utf-8",P404);
			}
		}
	}

	private void processCookies(String cookie)
	{
		if(cookie==null)
			return;
		
		String[] crumbs = cookie.split(";");
		
		for(int i=0;i<crumbs.length;i++)
		{
			String[] crumb=crumbs[i].trim().split("=");
			if(crumb.length==2)
			{
				this.cookie.put(crumb[0],crumb[1]);
			}
		}
		
	}
			
	private void send(String command) throws IOException
	{
		outBuffer.append(command+EOL);
	}		

	public BufferedReader getPostData()
	{
		return in;
	}
	
	public String getRequestMimeType()
	{
		return mimetype;
	}
	
	public String[] getGetRequest()
	{
		return urlReq;
	}
	
	public Map<String,String> getCookie()
	{
		return cookie;
	}
	
	public void addCookieCrumb(String key,Object value)
	{
		cookie.put(key,value.toString());
	}
	
	public DBManager getDatabase()
	{
		return database;
	}
	
	public static String getMimeType(String fileUrl) throws java.io.IOException
	{
	    FileNameMap fileNameMap = URLConnection.getFileNameMap();
	    String type = fileNameMap.getContentTypeFor(fileUrl);

	    return type;
	}
	
	//simpe reply
	protected void reply(int no,String id,String content,String charset,String message) throws IOException
	{
		outBuffer=new StringBuffer();
		send("HTTP/1.1 "+no+" "+id);
		send("Cache-Control: no-cache");
		send("Server: scr4tch micro web server");
		send("Content-Type: "+content+"; charset="+charset);
		send("");
		send(message);
		close();
	}
	
	public void close() throws IOException
	{
		out.write(outBuffer.toString().getBytes());
		out.flush();
		out.close();
		in.close();
		connected=false;
		sock.close();
		server.removeConnection(this);
	}
	
	public final MicroWebServer getServer()
	{
		return server;
	}
	
	public boolean hasPostData()
	{
		return post;
	}
	
	public boolean hasGetData()
	{
		return getData.size()>0;
	}
	
	public long getPostDataLength()
	{
		return length;
	}
	
	public boolean isConnected()
	{
		return connected;
	}
	
	public static Map<String,String> getFormData(String inpt)
	{
		String[] pp = inpt.split("&");
		
		Map<String,String> res = new HashMap<String,String>();
		for(int i=0;i<pp.length;i++)
		{
			String[] ps = pp[i].split("=");
			if(ps.length==2)
			{
				res.put(ps[0],ps[1]);
			}
		}
		
		return res;
	}
	
	public static String getCodeDescription(int code)
	{
		switch(code)
		{
			case 400:return "Bad Request";
			case 401:return "Unauthorized";
			case 403:return "Forbidden";
			case 404:return "Not Found";
			case 411:return "Length Required";
			case 415:return "Unsupported Media Type";
			case 501:return "Unknown";
			case 500:return "Internal Server Error";
			default: return "Unknwon";

		}
	}

	public void appendReplyCookieCrumb(String key,String value)
	{
		setCookie.put(key,value);
	}
	
	private void sendExtras() throws IOException
	{
		Iterator<String> ki = extras.keySet().iterator();
		while(ki.hasNext())
		{
			String k = ki.next();
			send(k+": "+extras.get(k));
		}
	}

	public Object getCookieCrumb(String string)
	{
		return cookie.get(string);
	}

	public void setExtra(String key,String value)
	{
		extras.put(key,value);
	}

	public void setStatusCode(int i)
	{
		statuscode=i;
	}

	public String getGetValue(String key)
	{
		return getData.get(key);
	}
	
	private void log(int lev,String msg)
	{
		server.log(lev,this,msg);
	}
	
	public String getRequestingAddr()
	{
		return sock.getInetAddress().toString();
	}
	
	public String getRequest()
	{
		return request;
	}
}
