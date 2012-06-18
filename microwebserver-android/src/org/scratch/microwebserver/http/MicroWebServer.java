/**
 * Android Micro Web Server
 * Copyright (C) 2011  ScR4tCh
 * Contact scr4tch@scr4tch.org

 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the Lesser GNU General Public License for more details.

 * You should have received a copy of the GNU Lesser General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package org.scratch.microwebserver.http;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Vector;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;

import org.scratch.microwebserver.LogEntry;
import org.scratch.microwebserver.RootCommands;
import org.scratch.microwebserver.properties.PropertyNames;
import org.scratch.microwebserver.properties.ServerProperties;
import org.scratch.microwebserver.util.MimeDetector;


public class MicroWebServer implements Runnable
{
	private Thread webworkerStd,webworkerSSL;
	private volatile boolean accept=true;
	
	private Vector<WebConnection> connections=new Vector<WebConnection>();
	private ServerSocket ssock;
	private SSLServerSocket sslssock;
	
	private int currentConnection=0;
	
	private long startTime=0;
	
	private MicroWebServerExecutor executor;
	
	private MimeDetector mimeDetect;
	
	private String sslmsgs="";
	
	//listen !
	private Vector<MicroWebServerListener> wbls = new Vector<MicroWebServerListener>();
	private Vector<LogEntry> preLogs = new Vector<LogEntry>();
	
	public MicroWebServer() throws IOException
	{
		//no listener will notice ...
		startUp(false);
		
		try
		{
			mimeDetect = new MimeDetector(ServerProperties.getInstance().getString(PropertyNames.MAGICMIME_FILE.toString()));
		}catch(Exception e){/*TODO: LOG !*/}
		
		ssock=new ServerSocket(ServerProperties.getInstance().getInt(PropertyNames.SERVER_PORT.toString()));
		
		if(ServerProperties.getInstance().getBoolean(PropertyNames.SERVER_PORTREDIRECT80.toString()))
			RootCommands.execute("iptables -t nat -A PREROUTING -p tcp --dport 80 -j REDIRECT --to-port "+ServerProperties.getInstance().getInt(PropertyNames.SERVER_PORT.toString()),null,null);
		
		executor = new MicroWebServerExecutor();
		
		webworkerStd = new Thread(this);
		webworkerStd.start();
		
		if(ServerProperties.getInstance().getBoolean(PropertyNames.SERVER_SSLENABLE.toString()))
		{
			try
			{
				sslssock=setupSSLServerSocket(ServerProperties.getInstance().getInt(PropertyNames.SERVER_PORTSSL.toString()),new FileInputStream(ServerProperties.getInstance().getString(PropertyNames.SERVER_KEYSTORE.toString())), ServerProperties.getInstance().getString(PropertyNames.SSL_KEYSTOREPASS.toString()));
				if(sslssock!=null)
				{
					if(ServerProperties.getInstance().getBoolean(PropertyNames.SERVER_PORTREDIRECT443.toString()))
						RootCommands.execute("iptables -t nat -A PREROUTING -p tcp --dport 443 -j REDIRECT --to-port "+ServerProperties.getInstance().getInt(PropertyNames.SERVER_PORTSSL.toString()),null,null);
					
					
					webworkerSSL = new Thread(this);
					webworkerSSL.start();
				}
				else
				{
					log(System.currentTimeMillis(),MicroWebServerListener.LOGLEVEL_ERROR,null,sslmsgs);
				}
			}catch(Exception e)
			 {
				log(System.currentTimeMillis(),MicroWebServerListener.LOGLEVEL_ERROR,null,"Setting up SSL failed: "+e.getMessage());
			 }
		}		
		
		startTime=System.currentTimeMillis();
		
		//no listener will notice ...
		startUp(true);
	}
	
	public synchronized Vector<LogEntry> fetchPreLogs()
	{
		Vector<LogEntry> ret = new Vector<LogEntry>(preLogs);
		preLogs.clear();
		
		return ret;
	}
	
	
	public void run()
	{
		if(Thread.currentThread().equals(webworkerStd))
		{
			while(accept && webworkerStd.isAlive())
			{
				try
				{
					WebConnection conn = new WebConnection(ssock.accept(),this);
					if(conn!=null)
					{
						connections.add(conn);
						executor.runTask(new MicroWebServerExecutor.WorkerRunnable(this,conn));
					}
					
				} catch (IOException e)
				  {
					// TODO: LOG !
					e.printStackTrace();
				  }
			}
		}
		else if(Thread.currentThread().equals(webworkerSSL))
		{
			while(accept && webworkerStd.isAlive())
			{
				try
				{
					WebConnection conn = new WebConnection(sslssock.accept(),this);
					if(conn!=null)
					{
						connections.add(conn);
						executor.runTask(new MicroWebServerExecutor.WorkerRunnable(this,conn));
					}
					
				} catch (IOException e)
				  {
					// TODO: LOG !
					e.printStackTrace();
				  }
			}
		}
	}
	
	public WebConnection getNextConnection()
	{
		if(currentConnection>=connections.size())
			currentConnection=0;
		
		if(connections.size()==0)
			return null;
		
		return connections.elementAt(currentConnection++);
	}
	
	public void removeConnection(WebConnection c)
	{
		connections.remove(c);	
	}
	
	public boolean isOnline()
	{
		return accept;
	}

	public void shutdown()
	{
		shutDown(false);
		
		accept=false;
		try
		{
			executor.shutDown();
			
			try
			{
				if(ssock!=null)
					ssock.close();
				
				if(sslssock!=null)
					sslssock.close();
			}
			catch(Exception e)
			{
				//just don't care !!!
			}
			
			if(webworkerStd!=null)
				webworkerStd.join();
			
			if(webworkerSSL!=null)
				webworkerSSL.join();
			
			//remove redirects (if applied)
			if(ServerProperties.getInstance().getBoolean(PropertyNames.SERVER_PORTREDIRECT80.toString()))
			{
				deleteRules(ServerProperties.getInstance().getInt(PropertyNames.SERVER_PORT.toString()));
			}
			
			if(ServerProperties.getInstance().getBoolean(PropertyNames.SERVER_PORTREDIRECT443.toString()))
			{
				deleteRules(ServerProperties.getInstance().getInt(PropertyNames.SERVER_PORTSSL.toString()));
			}
			
		}
		catch(InterruptedException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		shutDown(true);
		
	}
	
	private void deleteRules(int port)
	{

		boolean remove=true;
		
		while(remove)
		{
			StringBuffer outb = new StringBuffer();
			RootCommands.execute("iptables -t nat --line-numbers -n -L|grep redir|awk -F\\  '{print $1,$11}'|grep "+port,outb,null);
			
			if(outb.length()==0)
				remove=false;
			else
			{
				remove=RootCommands.execute("iptables -t nat -D PREROUTING 1",null,null);
			}
			
		}
	}

	public long getUptime()
	{
		return (System.currentTimeMillis()-startTime);
	}
	
	protected void log(long t,int level,WebConnection wc,String msg)
	{
		if(wbls.size()==0)
			preLogs .add(new LogEntry(t,level,msg,"",""));
		
		for(int i=0;i<wbls.size();i++)
			wbls.elementAt(i).log(t,level,wc.getRequestingAddr(),wc.getRequest(),msg);
	}
	
	private void startUp(boolean b)
	{
		for(int i=0;i<wbls.size();i++)
			wbls.elementAt(i).startUp(b);
	}
	
	private void shutDown(boolean b)
	{
		for(int i=0;i<wbls.size();i++)
		{
			wbls.elementAt(i).shutDown(b);
		}
	}
	
	public void addMicroWebServerListener(MicroWebServerListener wcl)
	{
		wbls.add(wcl);
	}
	
	public void removeMicroWebServerListener(MicroWebServerListener wcl)
	{
		wbls.add(wcl);
	}


	public MimeDetector getMimeDetector()
	{
		return mimeDetect;
	}
	
	public void setMimeDetector(MimeDetector mimeDetect)
	{
		this.mimeDetect=mimeDetect;
	}
	
	private SSLServerSocket setupSSLServerSocket(int port,final InputStream is,String pw)
	{
		
		try
		{
			SSLContext sslContext = SSLContext.getInstance( "TLS" );
			
			KeyManagerFactory km = KeyManagerFactory.getInstance("X509");
			
			KeyStore ks = KeyStore.getInstance("PKCS12");
			
			ks.load(is,pw.toCharArray());
			km.init(ks, PropertyNames.SSL_KEYSTOREPASS.toString().toCharArray());
			
			sslContext.init(km.getKeyManagers(), null, null);
			
			SSLServerSocketFactory f = sslContext.getServerSocketFactory();
			
			SSLServerSocket ss = (SSLServerSocket) f.createServerSocket(port);
			
			return ss;
			
		}
		catch (UnrecoverableKeyException e)
		{
			sslmsgs+=e.getClass().getSimpleName()+": "+e.getMessage()+"\n";
			//e.printStackTrace();
		}
		catch (KeyManagementException e)
		{
			sslmsgs+=e.getClass().getSimpleName()+": "+e.getMessage()+"\n";
			//e.printStackTrace();
		}
		catch (NoSuchAlgorithmException e)
		{
			sslmsgs+=e.getClass().getSimpleName()+": "+e.getMessage()+"\n";
			//e.printStackTrace();
		}
		catch (KeyStoreException e)
		{
			sslmsgs+=e.getClass().getSimpleName()+": "+e.getMessage()+"\n";
			//e.printStackTrace();
		}
		catch (CertificateException e)
		{
			sslmsgs+=e.getClass().getSimpleName()+": "+e.getMessage()+"\n";
			//e.printStackTrace();
		}
		catch (FileNotFoundException e)
		{
			sslmsgs+=e.getClass().getSimpleName()+": "+e.getMessage()+"\n";
			//e.printStackTrace();
		}
		catch (IOException e)
		{
			sslmsgs+=e.getClass().getSimpleName()+": "+e.getMessage()+"\n";
			//e.printStackTrace();
		}
		
		return null;
	}

}
