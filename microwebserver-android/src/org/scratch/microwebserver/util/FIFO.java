/**
 * Android Micro Web Server
 * Copyright (C) 2011  ScR4tCh
 * Contact scr4tch@scr4tch.org

 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the Lesser GNU General Public License for more details.

 * You should have received a copy of the GNU Lesser General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package org.scratch.microwebserver.util;

public class FIFO
{
    private FIFOnode head;
    private FIFOnode last;
    private Object lockOnLast;
    private int size=0;

    public FIFO()
    {
        head = last = new FIFOnode(null, null);
        lockOnLast = new Object();
    }

    public void push(Object x)
    {
        FIFOnode newNode = new FIFOnode(x, null);

        synchronized (lockOnLast)
        {
            last.next = newNode;
            last = newNode;
            size++;
        }

    }

    public synchronized Object pop()
    {
        Object x = null;

        FIFOnode first = head.next;

        if (first != null)
        {
            x = first.value;
            head = first;
            size--;
        }

        return x;
    }

    public synchronized int size()
    {
        return size;
    }
}

final class FIFOnode
{
    Object value;
    FIFOnode next;

    FIFOnode(Object x, FIFOnode n)
    {
        value = x;
        next = n;
    }
}