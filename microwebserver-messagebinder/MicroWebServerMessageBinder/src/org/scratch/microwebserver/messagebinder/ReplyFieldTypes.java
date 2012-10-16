package org.scratch.microwebserver.messagebinder;

public enum ReplyFieldTypes
{
	RESULTDATA("result");
	
	private String fieldname;
	
	private ReplyFieldTypes(String fieldname)
	{
		this.fieldname=fieldname;
	}
	
	public String getFieldName()
	{
		return fieldname;
	}
}
