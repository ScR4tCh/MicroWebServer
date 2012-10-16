package org.scratch.microwebserver.messagebinder;

public enum InvocationFieldTypes
{
	INVOKE_URI("invocation_uri"),INVOKE_POSTDATA("invocation_postdata");
	
	private String fieldname;
	
	private InvocationFieldTypes(String fieldname)
	{
		this.fieldname=fieldname;
	}
	
	public String getFieldName()
	{
		return fieldname;
	}
}
