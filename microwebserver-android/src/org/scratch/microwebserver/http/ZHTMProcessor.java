package org.scratch.microwebserver.http;

import java.io.StringWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.scratch.microwebserver.http.zeminterface.ZHTMLException;
import org.scratch.microwebserver.http.zeminterface.ZHTMLZemInterpreter;


import net.zeminvaders.lang.Interpreter;
import net.zeminvaders.lang.runtime.ZemObject;

/**
 * Very basic PHP-like scripting interface based upon ZemScript
 * @author scratch
 *
 */


public class ZHTMProcessor
{
	public static String process(ZHTMLZemInterpreter interpreter,WebConnection wb,String s) throws ZHTMLException
	{
//		System.err.println("-----------------------------------------------------------");
//		System.err.println(s);
//		System.err.println("-----------------------------------------------------------");
		
		Pattern ps = Pattern.compile("(<\\?zem){1}");
		Matcher ms = ps.matcher(s);
		
		StringBuffer html = new StringBuffer();
		int i=0;
		
		while(ms.find())
		{
			html.append(s.substring(i,ms.start()));
			i=s.indexOf("?>",ms.start())+2;
			String script=s.substring(ms.start()+7,i-2);
			html.append(processScript(script,interpreter));
		}
			
		if(i==0)
			return s;
		else if(i<s.length()-1)
			html.append(s.substring(i,s.length()));
		
		return html.toString();
	}
	
	public static String process(WebConnection wb,String s) throws ZHTMLException
	{
		ZHTMLZemInterpreter interpreter = new ZHTMLZemInterpreter(wb);
		return process(interpreter,wb,s);
	}

	private static String processScript(final String script,final Interpreter interpreter) throws ZHTMLException
	{
		
		StringWriter s = new StringWriter();
		interpreter.setOutput(s);
		
		try
		{
			ZemObject r = interpreter.eval(script);
			
			//if resulting object comes from an include function ... append !
//			if(r instanceof ZemIncludeString)
//				s.append(r.toString());
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new ZHTMLException(500,"Unknown Server Error \n"+e.getClass().getSimpleName()+" : "+e.getMessage());
		}
		
		String rs = interpreter.getOut().toString();
		
		//destroy active interpreter output
		interpreter.destroyOut(s);
				
		return rs;
	}

}
