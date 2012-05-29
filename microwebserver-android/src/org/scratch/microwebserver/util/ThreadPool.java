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