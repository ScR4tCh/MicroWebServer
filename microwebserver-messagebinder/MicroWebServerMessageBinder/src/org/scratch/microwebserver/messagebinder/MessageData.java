package org.scratch.microwebserver.messagebinder;

public enum MessageData
{
	PACKAGE_NAME("package_name"),SUPPORTED_METHODS("methods"),INPUT_MIMETYPES("input_mime"),OUTPUT_MIMETYPE("output_mimetype"),SERVICEGROUP_ALIAS("group_alias"),SERVICE_NAME("service_name");
	
	private String fieldname;
	
	private MessageData(String fieldname)
	{
		this.fieldname=fieldname;
	}
	
	public String getFieldName()
	{
		return fieldname;
	}
}
