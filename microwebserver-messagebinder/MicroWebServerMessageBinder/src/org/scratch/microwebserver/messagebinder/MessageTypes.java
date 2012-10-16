package org.scratch.microwebserver.messagebinder;

public enum MessageTypes
{
	MSG_SERVER_HELLO("SERVER SAYS HELLO"),
	MSG_SERVER_BYE("SERVER SAYS GOODBYE"),
	MSG_REGISTER_SERVICE("REGISTER A SERVICE"),
	MSG_REGISTER_SERVICE_REPLY("REPLY TO A SERVICE REGISTRATION"),
	MSG_INVOKE_SERVICE("INVOKE A SERVICE"),
	MSG_SERVICE_REPLY("SERVICE SENDS REPLY"),
	MSG_UNREGISTER_SERVICE("UNREGISTER A SERVICE"),
	MSG_UNREGISTER_SERVICE_REPLY("REPLY TO A SERVICE 'DEREGISTRATION'");
	
	private String str;
	
	private MessageTypes(String str)
	{
		this.str=str;
	}
	
	public String toString()
	{
		return str;
	}
	
	public static final String getMessageType(int mto)
	{
		if(mto<0 || mto>values().length)
			return null;
		
		return MessageTypes.values()[mto].toString();
	}
}
