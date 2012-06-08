package org.scratch.microwebserver.http;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

//THX FOR THE CODE : http://konstantin.filtschew.de/blog/2009/06/14/mit-threadpoolexecutor-arbeit-unter-java-effizient-parallelisieren/

public class MicroWebServerExecutor
{
	
	//TODO: make these values configurable
	
	//Parallel running Threads(Executor) on System
    int corePoolSize = 10;
 
    //Maximum Threads allowed in Pool
    int maxPoolSize = 20;
 
    //Keep alive time for waiting threads for jobs(Runnable)
    long keepAliveTime = 10;
 
    //This is the one who manages and start the work
    ThreadPoolExecutor threadPool = null;
 
    //Working queue for jobs (Runnable). We add them finally here
    final ArrayBlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<Runnable>(5);
 
    public MicroWebServerExecutor()
    {
        threadPool = new ThreadPoolExecutor(corePoolSize, maxPoolSize,keepAliveTime, TimeUnit.SECONDS, workQueue);
    }
 
    /**
     * Here we add our jobs to working queue
     *
     * @param task a Runnable task
     */
    public void runTask(Runnable task)
    { 
        threadPool.execute(task);
        System.out.println("Tasks in workQueue.." + workQueue.size());
    }
 
    /**
     * Shutdown the Threadpool if it's finished
     */
    public void shutDown()
    {
        threadPool.shutdown();
    }
    
    protected static class WorkerRunnable implements Runnable
    {
        private MicroWebServer server;
        private WebConnection conn;
       
        public WorkerRunnable(MicroWebServer server,WebConnection conn)
        {
        	this.server=server;
            this.conn = conn;
        }
       
        @Override
        public void run()
        {
			try
			{
				while(conn.isConnected())
				{
					conn.process();
					conn.reset();
				}
				
				conn.close();
			}catch(SocketTimeoutException ste)
			 {
				try
				{
					conn.log(MicroWebServerListener.LOGLEVEL_NORMAL,"connection timeout: "+conn.sock.getInetAddress());
					conn.setKeepAlive(false);
					conn.close();
				}
				catch(IOException e)
				{
					e.printStackTrace();
				}
				 server.removeConnection(conn);
			 }
			 catch (IOException e)
			 {
				conn.log(MicroWebServerListener.LOGLEVEL_WARN,"I/O Failure: "+e.getMessage());
			 }
			
			server.removeConnection(conn);
        }
    }
}
