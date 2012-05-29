/**
 * Android Micro Web Server
 * Copyright (C) 2011  ScR4tCh
 * Contact scr4tch@scr4tch.org

 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the Lesser GNU General Public License for more details.

 * You should have received a copy of the GNU Lesser General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
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

