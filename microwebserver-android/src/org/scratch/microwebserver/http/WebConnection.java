/**
 * Android Micro Web Server
 * Copyright (C) 2011  ScR4tCh
 * Contact scr4tch@scr4tch.org
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the Lesser GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 *
 * A very minimalistic "webserver" that is able to process
 * head,get and post commands received by any client.
 * 
 * 
 * TODO: implement the "WebServices" class to be able to define external request handlers (defined as xml maybe ...)!
 */

package org.scratch.microwebserver.http;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.FileNameMap;
import java.net.Socket;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import net.zeminvaders.lang.runtime.ZemObject;

import org.scratch.microwebserver.data.Cache;
import org.scratch.microwebserver.data.DBManager;
import org.scratch.microwebserver.data.DatabaseManagerException;
import org.scratch.microwebserver.http.zeminterface.ZHTMLException;
import org.scratch.microwebserver.properties.PropertyNames;
import org.scratch.microwebserver.properties.ServerProperties;
import org.scratch.microwebserver.util.Helper;
import org.scratch.microwebserver.util.MimeDetector;
import org.scratch.microwebserver.util.MimeType;

public class WebConnection
{
	private static final String IDENTIFIER="ScR4tCh MicroWebServer Android";
	
	//consts
	private static final String HTTPID="HTTP/1.1 ";
	private static final String HEAD_SERVER="Server: ";
	private static final String HEAD_DATE="Date: ";
	private static final String HEAD_CONNECTION="Connection: ";
	private static final String HEAD_CONTENTTYPE="Content-Type: ";
	private static final String HEAD_CONTENTLENGTH="Content-Length: ";
	private static final String HEAD_CONTENTRANGE="Content-Range: ";
	private static final String HEAD_SETCOOKIE="Set-Cookie: ";
	private static final String SLASH="/";
	private static final String EOL=(char)13+""+(char)10;

	//"read" ahead
	private static final int BBUFFERSIZE=16384;
	
	//socket timeout
	private static final int SOCKETTIMEOUT=5000; //eight seconds ... (best with telnet ;) )
	private static final int KEEPALIVETIMEOUT=15000; //eight seconds ... (best with telnet ;) )
	
	//Connection value
	private static final String CONN_CLOSE="close";
	private static final String CONN_KEEP="keep-alive";
	
	
	//new page setup ...
	private String request;
	private String[] urlReq;
	private String mimetype=null;
	private long length=-1;
	//private boolean post=false;
	private Map<String,String> cookie = new HashMap<String,String>();
	private Map<String,String> setCookie = new HashMap<String,String>();
	private Map<String,String> headerFields = new HashMap<String,String>();
	private int statuscode=200;
	
	//request type
	int rt;
	
	//request params
	private String[] params;
	
	private Vector<String> header = new Vector<String>();
	
	//connection specific
	private MicroWebServer server;
	protected Socket sock;
	private BufferedReader in;
	private Map<String,String> getData = new HashMap<String,String>();
	private OutputStream out;
	private StringBuffer outBuffer = new StringBuffer();
	private DBManager database;
	private WebServices services = WebServices.getInstance();
	
	//reply specific
	private SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z");
	
	//range header support
	private long byteFrom=-1;
	private long byteTo=-1;
	
	private volatile boolean connected=false;
	
	private MimeDetector mimeDetect;
	
	//keep-alive
	private boolean keep_alive = false;
	private volatile boolean processing=false;
	
	
	public WebConnection(final Socket sock,final MicroWebServer server)throws IOException
	{

		try
		{
			database=DBManager.getInstance(ServerProperties.getInstance().getString(PropertyNames.DATABASES_PATH.toString()));
		}
		catch(DatabaseManagerException e)
		{
			// TODO log: WARN !
			
			e.printStackTrace();
		}
					
		this.server=server;
		this.mimeDetect=server.getMimeDetector();
		this.sock=sock;
		this.sock.setSoTimeout(SOCKETTIMEOUT);
		in=new BufferedReader(new InputStreamReader(sock.getInputStream()));
		out=new BufferedOutputStream(sock.getOutputStream());
		
		log(MicroWebServerListener.LOGLEVEL_DEBUG,"connection from: "+sock.getInetAddress());
		
		connected=true;

	}

	public synchronized void process() throws IOException
	{
		boolean first=true;
		
		String buffer;
		
		//read http header
		while((buffer=in.readLine())!=null && buffer.length()>0 && connected && !processing)
		{
			if(first)
			{
				params=buffer.split("\\s");
		
				request = params[1];
				
				log(MicroWebServerListener.LOGLEVEL_DEBUG,"request: "+Arrays.asList(params));
				
				int urlei = params[1].indexOf('?');
				if(urlei>0)
				{
					getData=getFormData(params[1].substring(urlei+1,params[1].length()));
					params[1]=params[1].substring(0,urlei);
				}
				
				urlReq=params[1].split(SLASH);
				first=false;
				
				if(params[0].toLowerCase().equals("post"))
				{
					rt=WebServices.METHOD_POST;
					//post=true;
				}
				else if(params[0].toLowerCase().equals("get"))
				{
					rt=WebServices.METHOD_GET;
				}
				else if(params[0].toLowerCase().equals("head"))
				{
					rt=WebServices.METHOD_HEAD;
				}
				continue;
			}
			
			if(buffer.trim().length()==0)
			{
				processing=true;
				break;
			}	
			
			if(buffer.toLowerCase().startsWith("cookie:"))
			{
				processCookies(buffer.substring(buffer.indexOf(':')+1,buffer.length()).trim());
			}
			else if(rt==WebServices.METHOD_POST && buffer.toLowerCase().startsWith("content-type:"))
			{
				mimetype=buffer.substring(buffer.indexOf(':')+1,buffer.length()).trim();
			}
			else if(rt==WebServices.METHOD_POST && buffer.toLowerCase().startsWith("content-length:"))
			{
				length=Long.valueOf(buffer.substring(buffer.indexOf(':')+1,buffer.length()).trim()).longValue();
			}
			else if(buffer.toLowerCase().startsWith("connection:"))
			{
				String c = buffer.substring(buffer.indexOf(':')+1,buffer.length()).trim().toLowerCase();
				if(c.equals(CONN_KEEP))
				{
					keep_alive=true;
					sock.setSoTimeout(KEEPALIVETIMEOUT);
				}
			}
			else if(rt==WebServices.METHOD_GET && buffer.toLowerCase().startsWith("range:"))
			{
				String br = buffer.substring(buffer.indexOf(':')+1).trim();
				String[]brc = br.split("=");
				if(brc.length==2)
				{
					if(brc[0].equals("bytes"))
					{
						try
						{
							String[] brr = brc[1].split("-");
							if(brc[1].startsWith("-"))
							{
								byteTo=Long.parseLong(brr[1]);
							}
							else if(brc[1].endsWith("-"))
							{
								byteFrom=Long.parseLong(brr[0]);
							}
							else
							{
								byteFrom=Long.parseLong(brr[0]);
								byteTo=Long.parseLong(brr[1]);
							}
							
							log(MicroWebServerListener.LOGLEVEL_DEBUG,"byte range acceptable_: "+byteFrom+" TO "+byteTo);
							
						}catch(NumberFormatException nfe)
						 {
							byteFrom=-1;
							byteTo=-1;
							log(MicroWebServerListener.LOGLEVEL_NORMAL,"ibvalid byte range: "+buffer);
						 }
					}
					else
					{
						log(MicroWebServerListener.LOGLEVEL_NORMAL,"unknown range: "+buffer);
					}
				}
				else
				{
					log(MicroWebServerListener.LOGLEVEL_NORMAL,"unacceptable byte range: "+buffer);
				}
				
			}
			
			
			header.add(buffer);
		}
		
		log(MicroWebServerListener.LOGLEVEL_DEBUG,"HEADER:\n"+headOut(header));
		
		File f = new File(ServerProperties.getInstance().getString(PropertyNames.SERVER_ROOT.toString())+URLDecoder.decode(params[1],"UTF-8"));
		
		if(f.exists() && f.isDirectory())
		{
			String fp = f.getPath();
			if(!fp.endsWith(SLASH))
				fp+=SLASH;
			
			File possIndex;
			
			if((possIndex=new File(fp+ServerProperties.getInstance().getString(PropertyNames.INDEX_NAME.toString())+".html")).exists())
			{
				f=possIndex;
			}			
			else if((possIndex=new File(fp+ServerProperties.getInstance().getString(PropertyNames.INDEX_NAME.toString())+".htm")).exists())
			{
				f=possIndex;
			}
			else if((possIndex=new File(fp+ServerProperties.getInstance().getString(PropertyNames.INDEX_NAME.toString())+".zhtml")).exists())
			{
				f=possIndex;
			}
			else
			{
				if(!ServerProperties.getInstance().getBoolean(PropertyNames.ALLOW_DIRLIST.toString()))
				{
					log(MicroWebServerListener.LOGLEVEL_NORMAL,"dir listing requested, but not allowed");
					reply(403,"text/html","utf-8",constructErrorPage(403,"The server does not allow access, perhaps you must authenticate ..."));
					return;
				}
				else
				{
					if(Helper.isSymlink(f) && !ServerProperties.getInstance().getBoolean(PropertyNames.FOLLOW_SYMLINKS.toString()))
					{
						//403 ?
					}
					else
					{
						StringBuffer list = doDirList(f);
						
						byte[] sb = list.toString().getBytes();
						
						// do listing !
						sendOK(sb,null,true);
												
						return;
					}
				}
			}
		}
		
		//TODO: check for .htaccess if parsing htaccess is enabled !
		
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
				
				//sendBuffer.append(""+EOL);
				
				try
				{
					long t0 = System.currentTimeMillis();
					ZemObject res=null;
					sendBuffer.append(ZHTMProcessor.process(this,sb,res));
					long t = System.currentTimeMillis()-t0;
					
					log(MicroWebServerListener.LOGLEVEL_INFO,"ZemScript executing "+f+" took "+t+"ms"+((res==null)?"":" result="+res.toString()));
					
				}catch(ZHTMLException e)
				 {
					log(MicroWebServerListener.LOGLEVEL_WARN,e.getReplyCode()+" "+getCodeDescription(e.getReplyCode())+" "+e.getMessage());
					reply(e.getReplyCode(),"text/html","utf-8","<html><head><title>ERROR</title></head><body><h1>"+e.getReplyCode()+" "+getCodeDescription(e.getReplyCode())+"</h1>"+e.getMessage().replace("\n","<br/>")+"</body></html>");
					return;
				 }
				
				//create header
				
				send(HTTPID+statuscode);
				send(HEAD_SERVER+IDENTIFIER);
				send(HEAD_DATE+dateFormat.format(new Date(System.currentTimeMillis())));
				send(HEAD_CONNECTION+(keep_alive?CONN_KEEP:CONN_CLOSE));
				
				byte[] sbb = sendBuffer.toString().getBytes();
				
				send(HEAD_CONTENTLENGTH+sbb.length);
				sendExtraHeaderFields();
				
					
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
						
					send(HEAD_SETCOOKIE+cookie);
				}
				send(""); //end header
	
				sendBuffer(sbb);
			}
			else
			{
				//TODO: check for range header
				serveFile(f,byteFrom,byteTo);
			}
			
			close(false);
		}
		else
		{
			if(services.exists(params[1]))
			{
				try
				{
					WebServiceReply ret = services.invoke(params[1],rt/*post?WebServices.METHOD_POST:WebServices.METHOD_GET*/,mimetype,this);
					
					
					if(ret==null || (ret!=null && ret.getData()==null))
						sendOK(0,new StringBuffer(),null,true);
					else
						sendOK(ret.getLength(),ret.getData(),ret.getMime(),true);
				}
				catch(WebServiceException wse)
				{
					//wse.printStackTrace();
					//TODO: auto generate Error pages ! [OR: read from FS -> config "page overrides"]
					log(MicroWebServerListener.LOGLEVEL_WARN,wse.getReplyCode()+" "+getCodeDescription(wse.getReplyCode())+" "+params[1]);
					reply(wse.getReplyCode(),"text/html","utf-8","");
				}
			}
			else
			{
				//last try ... does a cached file exist ?
				if(Cache.getInstance().has(params[1]))
				{
					//TODO: check for range header
					serveFile(Cache.getInstance().getCached(params[1]),byteFrom,byteTo);
					close(false);
				}
				else
				{
					log(MicroWebServerListener.LOGLEVEL_NORMAL,"404 NOT FOUND : "+params[1]);
					reply(404,"text/html","utf-8",constructErrorPage(404,"The requested page or service was not found on this server"));
				}
			}
		}
	}
	
	private String headOut(Vector<String> header)
	{
		StringBuffer ret = new StringBuffer();
		
		for(int i=0;i<header.size();i++)
			ret.append(header.elementAt(i)+"\n");
		
		return ret.toString();
	}

	private synchronized void sendOK(byte[] data,String mime,boolean close) throws IOException
	{
		send(HTTPID+200);
		send(HEAD_SERVER+IDENTIFIER);
		send(HEAD_DATE+dateFormat.format(new Date(System.currentTimeMillis())));
		send(HEAD_CONNECTION+(keep_alive?CONN_KEEP:CONN_CLOSE));
		
		if(mime!=null)
			send(HEAD_CONTENTTYPE+mime);
		else
			send(HEAD_CONTENTTYPE+"text/html");
		
		send(HEAD_CONTENTLENGTH+data.length);
		
		sendExtraHeaderFields();
		
		send("");
		
		if(data!=null && rt!=WebServices.METHOD_HEAD)
			send(data);
		
		if(close)
			close(false);
	}
	
	private synchronized void sendOK(long l,StringBuffer data,String mime,boolean close) throws IOException
	{
		send(HTTPID+200);
		send(HEAD_SERVER+IDENTIFIER);
		send(HEAD_DATE+dateFormat.format(new Date(System.currentTimeMillis())));
		send(HEAD_CONNECTION+(keep_alive?CONN_KEEP:CONN_CLOSE));
		
		if(mime!=null)
			send(HEAD_CONTENTTYPE+mime);
		else
			send(HEAD_CONTENTTYPE+"text/html");
		
		send(HEAD_CONTENTLENGTH+l);
		
		sendExtraHeaderFields();
		
		send("");
		
		if(data!=null && rt!=WebServices.METHOD_HEAD)
			send(data.toString());
		
		if(close)
			close(false);
	}
	
	private String constructErrorPage(int errno,String errdetails)
	{
		String ret="<html><head><title>";
		
		ret+=errno+" "+getCodeDescription(errno)+"</title></head><body><h1>";
		ret+=errno+" "+getCodeDescription(errno)+"</h1>"+errdetails+"</body></html>";
		
		return ret;
	}
	
	private synchronized void serveFile(File f,long from,long to) throws IOException
	{
		//System.err.println("REQUEST STD FILE ! "+params[1]);
		
		if(from!=-1 && to!=-1)
		{
			if(from<0 || to<0)
				throw new IOException("invalid range given, must be >0");
			
			if(from>to)
				throw new IOException("lower range boundary must not be bigger than upper !");
		}
		
						
//		String mime = getMimeType("file://"+f.getAbsolutePath());
		String mime = getMimeType(f.getAbsolutePath());
				
		byte[] fbuffer = new byte[BBUFFERSIZE];
		BufferedInputStream fin = new BufferedInputStream(new FileInputStream(f));
		
		long realsize=f.length();
		
		if(from==-1)
			from=0;
		
		if(to==-1)
			to=realsize;
		
		
		long rd=0;
		
		if(from>0)
		{
			fin.skip(from);
			rd=from;
		}
				
		long avail=to-from;
		
		boolean range = (from>0 || to<realsize);
		
		log(MicroWebServerListener.LOGLEVEL_DEBUG,"RANGE: "+range+"-> "+rd+"-"+(rd+avail)+"/"+realsize+" | "+to);
		
		
		//HTTP PART
		if(!range)
		{
			send(HTTPID+"200");
		}
		else
		{
			send(HTTPID+"206");
			send("Cache-Control: no-cache");
			send("Expires: "+dateFormat.format(new Date(System.currentTimeMillis()+72000)));
		}
		
		send(HEAD_SERVER+IDENTIFIER);
		
		if(mime!=null)
			send(HEAD_CONTENTTYPE+mime);
		else
			send(HEAD_CONTENTTYPE+"application/octet-stream");	//?!?
		
		send(HEAD_DATE+dateFormat.format(new Date(System.currentTimeMillis())));
		
		if(!range)
			send(HEAD_CONTENTLENGTH+avail);
		else
			send(HEAD_CONTENTRANGE+"bytes "+rd+"-"+(rd+avail)+"/"+realsize);
		
		send("");
		
		out.write(outBuffer.toString().getBytes());
		
		if(rt!=WebServices.METHOD_HEAD)
		{
			log(MicroWebServerListener.LOGLEVEL_DEBUG,"SERVE FILE REPL:\n"+outBuffer.toString());
			
			//"STREAM"
			outBuffer=new StringBuffer();
			
			int r=0;
			
			while((r=fin.read(fbuffer))>0)
			{
				rd+=r;
				
				if(to!=avail && rd>to)
				{
					out.write(fbuffer,0,(int)(rd-to));
					out.flush();
				}
				else
				{
					out.write(fbuffer,0,r);
					out.flush();
				}
			}
			fin.close();
		}
		
	}

	private synchronized StringBuffer doDirList(File f)
	{
		StringBuffer dl = new StringBuffer();
		
		dl.append("<html>\n\t<head><title>Index of "+params[1]+"</title></head>\n\t<body>\n");
		
		Vector<File> files = new Vector<File>(Arrays.asList(f.listFiles()));
		
		
		log(MicroWebServerListener.LOGLEVEL_DEBUG,"create dir listing: "+f.getAbsolutePath()+"    "+files.size()+" files");
		
		Collections.sort(files,new Comparator<File>()
								{
									@Override
									public int compare(File one,File two)
									{
										if(one.isDirectory() && !two.isDirectory())
										{
											return -1;
										}
										else if(!one.isDirectory() && two.isDirectory())
										{
											return 1;
										}
										else
										{
											return one.compareTo(two);
										}
									}
									
								});
		
		dl.append("\t\t<h1>Index of "+params[1]+"</h1>\n\t\t<pre><hr/>\n");
		
		for(int i=0;i<files.size();i++)
		{	
			
			String ico="";
			
			if(files.elementAt(i).isDirectory())
				ico=ServerProperties.getInstance().getString(PropertyNames.DEFAULT_FOLDER_ICON.toString());
			else
				ico=ServerProperties.getInstance().getString(PropertyNames.DEFAULT_FILE_ICON.toString());
			
			String shrtn = files.elementAt(i).getName();
			
			if(files.elementAt(i).isDirectory())
				shrtn+=SLASH;
			
			if(shrtn.length()>23)
			{
				shrtn = shrtn.substring(0,20)+"..>";
			}
			
			String space="";
			if(shrtn.length()<26)
			{
				for(int l=shrtn.length();l<27;l++)
					space+=" ";
			}
			
			try
			{
				String furl;
				String fl;
				
				if(files.elementAt(i).isDirectory())
				{
					fl="-";
					furl=params[1]+URLEncoder.encode(files.elementAt(i).getName(),"UTF-8")+SLASH;
				}
				else
				{
					fl=Helper.fileSizeFormat(files.elementAt(i).length());
					furl=params[1]+URLEncoder.encode(files.elementAt(i).getName(),"UTF-8");
				}
				
				dl.append("<image src=\""+ico+"\">  <a alt=\""+files.elementAt(i).getName()+"\" href=\""+furl+"\">"+shrtn+"</a>"+space+"   "+Helper.stdDateFormat(files.elementAt(i).lastModified())+"   "+fl+"\n");
			}
			catch(UnsupportedEncodingException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
				
			
			
		}
		
		dl.append("\t\t<hr/></pre>\n\t<p>"+IDENTIFIER+"</p></body>\n</html>\n");
		
		return dl;
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
	
	private void sendBuffer(byte[] sb)
	{
		outBuffer.append(new String(sb));
	}
			
	private void send(String command) throws IOException
	{
		outBuffer.append(command+EOL);
	}
	
	private void send(byte[] b) throws IOException
	{
		//FIXME: not that good ....
		outBuffer.append(new String(b));
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
	
	private String getMimeType(String fileUrl)
	{

		Collection<MimeType> mts = new HashSet<MimeType>();
		try
		{
			mts=mimeDetect.getMimeTypesFile(fileUrl);
		}
		catch(UnsupportedOperationException e)
		{
			e.printStackTrace();
		}
		catch(IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(mts.size()>0)
		{
			return mts.toArray()[0].toString();
		}
		else
		{
			    FileNameMap fileNameMap = URLConnection.getFileNameMap();
			    String type = fileNameMap.getContentTypeFor("file://"+fileUrl);
			    return type;
		}
	}
		
	//simpe reply
	protected synchronized void reply(int no,String content,String charset,String message) throws IOException
	{
		outBuffer=new StringBuffer();
		send(HTTPID+no+" "+getCodeDescription(no));
		send("Cache-Control: no-cache");
		send(HEAD_SERVER+IDENTIFIER);
		send(HEAD_CONTENTTYPE+content+"; charset="+charset);
		send("");
		if(message!=null)
			send(message);
		
		if(no>=299)
			close(true);
		else
			close(false);
	}
	
	public synchronized void close(boolean force) throws IOException
	{
		out.write(outBuffer.toString().getBytes());
		out.flush();
		
		processing=false;
		
		if(!keep_alive || force)
		{
			out.close();
			in.close();
			connected=false;
			sock.close();
			server.removeConnection(this);
		}
	}
	
	public final MicroWebServer getServer()
	{
		return server;
	}
	
	public boolean hasPostData()
	{
		//return post;
		return rt==WebServices.METHOD_POST;
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
			case 200:return "OK";
			case 400:return "Bad Request!";
			case 401:return "Unauthorized!";
			case 403:return "Forbidden!";
			case 404:return "Not Found!";
			case 411:return "Length Required";
			case 415:return "Unsupported Media Type";
			case 501:return "Unknown";
			case 500:return "Internal Server Error";
			default: return "Undefined Error";

		}
	}

	public void appendReplyCookieCrumb(String key,String value)
	{
		setCookie.put(key,value);
	}
	
	private void sendExtraHeaderFields() throws IOException
	{
		Iterator<String> ki = headerFields.keySet().iterator();
		while(ki.hasNext())
		{
			String k = ki.next();
			send(k+": "+headerFields.get(k));
		}
	}

	public Object getCookieCrumb(String string)
	{
		return cookie.get(string);
	}

	public void setHeaderField(String key,String value)
	{
		headerFields.put(key,value);
	}

	public void setStatusCode(int i)
	{
		statuscode=i;
	}

	public String getGetValue(String key)
	{
		return getData.get(key);
	}
	
	protected void log(int lev,String msg)
	{
		server.log(System.currentTimeMillis(),lev,this,msg);
	}
	
	public String getRequestingAddr()
	{
		return sock.getInetAddress().toString();
	}
	
	public String getRequest()
	{
		return request;
	}
	
	protected void setKeepAlive(boolean b)
	{
		keep_alive=b;
	}

	//reset values for "kept connection" !
	protected void reset()
	{
		request=null;
		urlReq=null;
		mimetype=null;
		length=-1;
		outBuffer=new StringBuffer();
		cookie.clear();
		setCookie.clear();
		headerFields.clear();
		statuscode=200;
		
		header.clear();
	}
}
