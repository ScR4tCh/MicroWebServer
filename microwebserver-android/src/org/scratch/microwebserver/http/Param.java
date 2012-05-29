package org.scratch.microwebserver.http;

public class Param
{
	public static final int TYPE_BOOLEAN=0;
	public static final int TYPE_INT=1;
	public static final int TYPE_LONG=2;
	public static final int TYPE_FLOAT=3;
	public static final int TYPE_DOUBLE=4;
	public static final int TYPE_STRING=5;
	
	
	private boolean required;
	private int dataType;
	
	public Param(boolean required,int dataType)
	{
		this.required=required;
		this.dataType=dataType;
	}

	public final boolean isRequired()
	{
		return required;
	}

	public final int getDataType()
	{
		return dataType;
	}
	
}
