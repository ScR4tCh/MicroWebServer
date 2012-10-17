package org.scratch.microwebserver.messagebinder;

public enum InvocationFieldTypes
{
	INVOKE_URI("invocation_uri"),INVOKE_POSTDATA("invocation_postdata"), INVOKE_METHOD("invocation_method"), INVOKE_MIME("invocation_mime");
	
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
