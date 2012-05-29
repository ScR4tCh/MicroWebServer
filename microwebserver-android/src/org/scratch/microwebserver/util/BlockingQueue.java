/**
 * Android Micro Web Server
 * Copyright (C) 2011  ScR4tCh
 * Contact scr4tch@scr4tch.org

 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the Lesser GNU General Public License for more details.

 * You should have received a copy of the GNU Lesser General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
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