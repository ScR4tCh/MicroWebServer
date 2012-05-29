package org.scratch.microwebserver.util;

import java.util.Vector;

/**
 * ThreadPool reference implementation based upon http://tutorials.jenkov.com/java-concurrency/thread-pools.html
 */

public class ThreadPool
{
	  private BlockingQueue<Runnable> taskQueue = null;
	  private Vector<PoolThread> threads = new Vector<PoolThread>();
	  private boolean isStopped = false;

	  public ThreadPool(int noOfThreads, int maxNoOfTasks)
	  {
	    taskQueue = new BlockingQueue<Runnable>(maxNoOfTasks);

	    for(int i=0; i<noOfThreads; i++)
	    {
	      threads.add(new PoolThread(taskQueue));
	    }
	    
	    for(int i=0;i<threads.size();i++)
	    {
	      threads.elementAt(i).start();
	    }
	  }

	  public synchronized void execute(Runnable task) throws InterruptedException
	  {
	    if(this.isStopped)
	    	throw new IllegalStateException("ThreadPool is stopped");

	    this.taskQueue.enqueue(task);
	  }

	  public synchronized void stop()
	  {
	    this.isStopped = true;
	    
	    for(int i=0;i<threads.size();i++)
	    {
	    	threads.elementAt(i).stopThread();
	    }
	  }

	}