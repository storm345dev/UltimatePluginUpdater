package net.stormdev.bukkitmods.ultimatepluginupdater.generalDownload;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import com.google.common.io.Files;

public class UrlData {
	public static byte[] getData(URL url){
		File f;
		try {
		  f = new File(url.toURI());
		} catch(URISyntaxException e) {
		  f = new File(url.getPath());
		}
		return getData(f);
	}
	
	public static byte[] getData(File f){
		try {
			return Files.toByteArray(f);
		} catch (IOException e) {
			return null;
		}
	}
	
	public static boolean isEqual(URL updateURL, File current){
		byte[] remote = getData(updateURL);
		byte[] local = getData(current);
		
		return FileComparator.areSame(remote, local);
	}
	
	public static boolean shouldUpdate(String currentFilePath, String updateURL) throws MalformedURLException{
		URL url = new URL(updateURL);
		File local = new File(currentFilePath);
		if(!local.exists()){
			return true;
		}
		return isEqual(url, local);
	}
}
