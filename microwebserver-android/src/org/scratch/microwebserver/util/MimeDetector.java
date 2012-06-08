package org.scratch.microwebserver.util;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class MimeDetector
{

	private ConcurrentMap<String,SortedSet<String>> mimeTypes = new ConcurrentHashMap<String,SortedSet<String>>();
	private List<MagicMimeEntry> mMagicMimeEntries=new ArrayList<MagicMimeEntry>();

	public MimeDetector(String mimemagicfile) throws FileNotFoundException
	{
		File f = new File(mimemagicfile);
		initMagicRules(new BufferedInputStream(new FileInputStream(f)));
	}
	
	public MimeDetector(InputStream is)
	{
		initMagicRules(is);
	}

	/**
	 * Get the mime types that may be contained in the data array.
	 * 
	 * @param data
	 *            . The byte array that contains data we want to detect mime
	 *            types from.
	 * @return the mime types.
	 * @throws MimeException
	 *             if for instance we try to match beyond the end of the data.
	 */
	public Collection<MimeType> getMimeTypesByteArray(final byte[] data)
			throws UnsupportedOperationException
	{
		Collection<MimeType> mimeTypes=new LinkedHashSet<MimeType>();
		int len=mMagicMimeEntries.size();
		try
		{
			for(int i=0;i<len;i++)
			{
				MagicMimeEntry me=mMagicMimeEntries.get(i);
				MagicMimeEntry matchingMagicMimeEntry=me.getMatch(data);
				if(matchingMagicMimeEntry!=null)
				{
					mimeTypes.add(matchingMagicMimeEntry.getMimeType());
				}
			}
		}
		catch(Exception e)
		{
		}
		return mimeTypes;
	}
	
	public Collection<MimeType> getMimeTypesFile(String file) throws UnsupportedOperationException,IOException
	{
		File f = new File(file);	//check for existance ?
		return getMimeTypesInputStream(new BufferedInputStream(new FileInputStream(f)));
	}

	/**
	 * Get the mime types of the data in the specified {@link InputStream}.
	 * Therefore, the <code>InputStream</code> must support mark and reset (see
	 * {@link InputStream#markSupported()}). If it does not support mark and
	 * reset, an {@link MimeException} is thrown.
	 * 
	 * @param in
	 *            the stream from which to read the data.
	 * @return the mime types.
	 * @throws MimeException
	 *             if the specified <code>InputStream</code> does not support
	 *             mark and reset (see {@link InputStream#markSupported()}).
	 */
	public Collection<MimeType> getMimeTypesInputStream(final InputStream in)throws UnsupportedOperationException
	{
		Collection<MimeType> mimeTypes=new LinkedHashSet<MimeType>();
		int len=mMagicMimeEntries.size();
		try
		{
			for(int i=0;i<len;i++)
			{
				MagicMimeEntry me=mMagicMimeEntries.get(i);
				MagicMimeEntry matchingMagicMimeEntry=me.getMatch(in);
				if(matchingMagicMimeEntry!=null)
				{
					mimeTypes.add(matchingMagicMimeEntry.getMimeType());
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return mimeTypes;
	}

	/*
	 * This loads the magic.mime file rules into the internal parse tree in the
	 * following order 1. From any magic.mime that can be located on the
	 * classpath 2. From any magic.mime file that can be located using the
	 * environment variable MAGIC 3. From any magic.mime located in the users
	 * home directory ~/.magic.mime file if the MAGIC environment variable is
	 * not set 4. From the locations defined in the magicMimeFileLocations and
	 * the order defined 5. From the internally defined magic.mime file ONLY if
	 * we are unable to locate any of the files in steps 2 - 5 above Thanks go
	 * to Simon Pepping for his bug report
	 */
	private void initMagicRules(InputStream is)
	{
			if(is!=null)
			{
				try
				{
					parse(new InputStreamReader(is));
					is.close();
				}
				catch(Exception ex)
				{
				}
			}
	}

	// Parse the magic.mime file
	private void parse(final Reader r) throws IOException
	{
		// long start=System.currentTimeMillis();

		BufferedReader br=new BufferedReader(r);
		String line;
		List<String> sequence=new ArrayList<String>();

		long lineNumber=0;
		line=br.readLine();
		if(line!=null)
			++lineNumber;
		while(true)
		{
			if(line==null)
			{
				break;
			}
			line=line.trim();
			if(line.length()==0||line.charAt(0)=='#')
			{
				line=br.readLine();
				if(line!=null)
					++lineNumber;
				continue;
			}
			sequence.add(line);

			// read the following lines until a line does not begin with '>' or
			// EOF
			while(true)
			{
				line=br.readLine();
				if(line!=null)
					++lineNumber;
				if(line==null)
				{
					addEntry(lineNumber,sequence);
					sequence.clear();
					break;
				}
				line=line.trim();
				if(line.length()==0||line.charAt(0)=='#')
				{
					continue;
				}
				if(line.charAt(0)!='>')
				{
					addEntry(lineNumber,sequence);
					sequence.clear();
					break;
				}
				sequence.add(line);
			}

		}
		if(!sequence.isEmpty())
		{
			addEntry(lineNumber,sequence);
		}

	}

	private void addEntry(final long lineNumber,final List<String> aStringArray)
	{
		MagicMimeEntry magicEntry=new MagicMimeEntry(aStringArray);
		mMagicMimeEntries.add(magicEntry);
		// Add this to the list of known mime types as well
		if(magicEntry.getMimeType()!=null)
		{
			addKnownMimeType(magicEntry.getMimeType());
		}

	}

	private void addKnownMimeType(final MimeType mimeType)
	{
		addKnownMimeType(mimeType.toString());
	}

	private void addKnownMimeType(final String mimeType)
	{
		try
		{

			String key=getMediaType(mimeType);
			SortedSet<String> s=mimeTypes.get(key);
			if(s==null)
			{
				s=new TreeSet<String>();
			}
			s.add(getSubType(mimeType));
			mimeTypes.put(key,s);
		}
		catch(MimeException ignore)
		{
			// A couple of entries in the magic mime file don't follow the rules
			// so ignore them
		}
	}
	
	public static String getMediaType(final String mimeType) throws MimeException
	{
		return new MimeType(mimeType).getMediaType();
	}
	
	public static String getSubType(final String mimeType) throws MimeException
	{
		return new MimeType(mimeType).getSubType();
	}
}
