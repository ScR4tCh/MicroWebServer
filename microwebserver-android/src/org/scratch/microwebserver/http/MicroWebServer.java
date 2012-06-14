/**
 * Android Micro Web Server
 * Copyright (C) 2011  ScR4tCh
 * Contact scr4tch@scr4tch.org

 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the Lesser GNU General Public License for more details.

 * You should have received a copy of the GNU Lesser General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package org.scratch.microwebserver.http;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.Vector;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.TrustManagerFactory;

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
	
	//listen !
	private Vector<MicroWebServerListener> wbls = new Vector<MicroWebServerListener>();
	
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
				FileInputStream fis = new FileInputStream(ServerProperties.getInstance().getString(PropertyNames.SERVER_SSLCERT.toString()));
				BufferedInputStream bis = new BufferedInputStream(fis);
				CertificateFactory cf;
				
				KeyStore ks = KeyStore.getInstance("BKS");
				ks.load(null,null);
				
				KeyManagerFactory kmf =  KeyManagerFactory.getInstance("X509");
				kmf.init(ks, null);
				
				cf=CertificateFactory.getInstance( "X.509" );
				Certificate cert = null;
				
				while ( bis.available() > 0 )
				{
				 cert = cf.generateCertificate( bis );
				 ks.setCertificateEntry( "SGCert", cert );
				}
				
				ks.setCertificateEntry( "SGCert", cert );
				
				TrustManagerFactory tmf = TrustManagerFactory.getInstance( "X509" );
				tmf.init( ks );
							
				SSLContext sslContext = SSLContext.getInstance( "TLS" );
				sslContext.init( kmf.getKeyManagers(), tmf.getTrustManagers(),null );
							
				SSLServerSocketFactory sf = sslContext.getServerSocketFactory();
				sslssock = (SSLServerSocket)sf.createServerSocket( ServerProperties.getInstance().getInt(PropertyNames.SERVER_PORTSSL.toString()) );
				
				
				webworkerSSL = new Thread(this);
				webworkerSSL.start();
				
			}
			catch(CertificateException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			catch(KeyStoreException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			catch(NoSuchAlgorithmException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			catch(KeyManagementException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			catch(IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			catch(UnrecoverableKeyException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
		//int workers = ServerProperties.getInstance().getInt(PropertyNames.SERVER_WORKERS);
		//if(workers<1)
		//	workers=1;
		
//		for(int i=0;i<workers;i++)
//		{
//			WebConnectionsList slist = new WebConnectionsList(this);
//			Thread list=new Thread(slist);
//			list.start();
//		}
		
		
		startTime=System.currentTimeMillis();
		
		//no listener will notice ...
		startUp(true);
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
		//Logger.getLogger("carma.remote").info("removing web connection "+c.toString());
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
			wbls.elementAt(i).shutDown(b);
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
}
