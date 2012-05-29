package org.scratch.microwebserver.util;

import java.util.Vector;


/*
 * Taken from http://tutorials.jenkov.com/java-concurrency/blocking-queues.html
 */

public class BlockingQueue<T>
{

	  private Vector<T> queue = new Vector<T>();
	  private int  limit = 10;

	  public BlockingQueue(int limit)
	  {
	    this.limit = limit;
	  }


	  public synchronized void enqueue(T item) throws InterruptedException
	  {
	    while(this.queue.size() == this.limit) 
	    {
	      wait();
	    }
	    
	    if(this.queue.size() == 0)
	    {
	      notifyAll();
	    }
	    
	    this.queue.add(item);
	  }


	  public synchronized T dequeue() throws InterruptedException
	  {
	    while(this.queue.size() == 0)
	    {
	      wait();
	    }
	    
	    if(this.queue.size() == this.limit)
	    {
	      notifyAll();
	    }

	    return this.queue.remove(0);
	  }

	}