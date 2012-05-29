/**
 * Android Micro Web Server
 * Copyright (C) 2011  ScR4tCh
 * Contact scr4tch@scr4tch.org

 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the Lesser GNU General Public License for more details.

 * You should have received a copy of the GNU Lesser General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package org.scratch.microwebserver.util.io;

import java.io.InputStream;

//Taken from http://javatechniques.com/blog/faster-deep-copies-of-java-objects/

/**
 * ByteArrayInputStream implementation that does not synchronize methods.
 */
public class FastByteArrayInputStream extends InputStream
{
	/**
	 * Our byte buffer
	 */
	protected byte[] buf=null;

	/**
	 * Number of bytes that we can read from the buffer
	 */
	protected int count=0;

	/**
	 * Number of bytes that have been read from the buffer
	 */
	protected int pos=0;

	public FastByteArrayInputStream(byte[] buf,int count)
	{
		this.buf=buf;
		this.count=count;
	}

	public final int available()
	{
		return count-pos;
	}

	public final int read()
	{
		return (pos<count) ? (buf[pos++]&0xff) : -1;
	}

	public final int read(byte[] b,int off,int len)
	{
		if(pos>=count)
			return -1;

		if((pos+len)>count)
			len=(count-pos);

		System.arraycopy(buf,pos,b,off,len);
		pos+=len;
		return len;
	}

	public final long skip(long n)
	{
		if((pos+n)>count)
			n=count-pos;
		if(n<0)
			return 0;
		pos+=n;
		return n;
	}

}
