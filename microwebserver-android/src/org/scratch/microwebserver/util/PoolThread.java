package org.scratch.microwebserver.util;

/**
 * Based upon PoolThread taken from http://tutorials.jenkov.com/java-concurrency/thread-pools.html
 */

public class PoolThread extends Thread
{

	  private BlockingQueue<Runnable> taskQueue = null;
	  private boolean       isStopped = false;

	  public PoolThread(BlockingQueue<Runnable> queue)
	  {
	    taskQueue = queue;
	  }

	  public void run()
	  {
	    while(!isStopped())
	    {
	      try
	      {
	        Runnable runnable = taskQueue.dequeue();
	        runnable.run();
	      }catch(Exception e)
	       {
	    	e.printStackTrace();
	        //log or otherwise report exception,
	        //but keep pool thread alive.
	       }
	    }
	  }

	  public synchronized void stopThread()
	  {
	    isStopped = true;
	    this.interrupt(); //break pool thread out of dequeue() call.
	  }

	  public synchronized boolean isStopped()
	  {
	    return isStopped;
	  }
}

