package org.scratch.microwebserver.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class DynDNSUpdater
{
	/**
	 * HOSTERS
	 * =======
	 * 
	 * should work:
	 * 
	 * members.dyndns.org
	 * dynupdate.no-ip.com
	 * 
	 * 
	 */
	
	public static boolean performUpdate(String hoster,String credentials64,String alias,String ip,String useragent,String response) throws IOException
	{
		// Connect to DynDNS
		URL url = new URL("https://"+hoster+"/nic/update?hostname=" + alias + "&myip=" + ip);
		HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
		connection.setRequestMethod("GET");
		connection.setRequestProperty("User-Agent", useragent);
		connection.setRequestProperty("Authorization", "Basic " + credentials64);

		// Execute GET
		int responseCode = connection.getResponseCode();		
		
		// Print feedback
		String line;
		StringBuffer respBuf = new StringBuffer();
		BufferedReader buff = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		while((line = buff.readLine())!= null)
		{
			//process results !
			respBuf.append(line);
		}
		
		connection.disconnect();
		
		line = respBuf.toString();
		if(response!=null)
			response=new String(line);
		
		if(line.startsWith("good") || line.startsWith("nochg"))
			return true;
			
		return false;
	}
}
