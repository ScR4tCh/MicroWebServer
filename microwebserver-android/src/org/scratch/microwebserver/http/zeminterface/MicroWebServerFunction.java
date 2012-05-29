package org.scratch.microwebserver.http.zeminterface;

import java.io.BufferedReader;

import org.scratch.microwebserver.data.DBManager;
import org.scratch.microwebserver.http.WebConnection;

import net.zeminvaders.lang.Interpreter;
import net.zeminvaders.lang.SourcePosition;
import net.zeminvaders.lang.runtime.Function;
import net.zeminvaders.lang.runtime.ZemObject;

public abstract class MicroWebServerFunction extends Function
{
	protected WebConnection wb;
	protected BufferedReader postdata;
	protected String[] getrequest;
	protected String mimetype;
	protected DBManager database;
	
	public MicroWebServerFunction()
	{
		super();
	}
	
	@Override
	public abstract ZemObject eval(Interpreter interpreter,SourcePosition pos) throws ZHTMLException;

	@Override
	public ZemObject getDefaultValue(int index)
	{
		return null;
	}

	@Override
	public abstract int getParameterCount();

	@Override
	public abstract String getParameterName(int index);
	
	public void setWebConnection(WebConnection wb)
	{
		this.postdata=wb.getPostData();
		this.mimetype=wb.getRequestMimeType();
		this.getrequest=wb.getGetRequest();
		this.database=wb.getDatabase();
	}

}
