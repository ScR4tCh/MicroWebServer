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