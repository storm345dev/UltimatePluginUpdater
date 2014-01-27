package net.stormdev.bukkitmods.ultimatepluginupdater.main;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;

public class GetStringFromUrl {
	public static Object[] getTextLines(URL webAddress) throws Exception{
		ArrayList<String> result = new ArrayList<String>();
		URL url = webAddress;
		InputStream in = null;
		in = new BufferedInputStream(url.openStream());
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));   				
		String line;
		while((line = reader.readLine()) != null){
			result.add(line);
		}		
	    return result.toArray();
	}
	

}
