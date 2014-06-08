package net.stormdev.bukkitmods.ultimatepluginupdater.generalDownload;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import com.google.common.io.Files;

public class UrlData {
	public static byte[] getData(URL url){
		ByteArrayOutputStream bais = new ByteArrayOutputStream();
		InputStream is = null;
		try {
		  is = url.openStream ();
		  byte[] byteChunk = new byte[4096]; // Or whatever size you want to read in at a time.
		  int n;

		  while ( (n = is.read(byteChunk)) > 0 ) {
		    bais.write(byteChunk, 0, n);
		  }
		}
		catch (IOException e) {
		  System.err.printf ("Failed while reading bytes from %s: %s", url.toExternalForm(), e.getMessage());
		  e.printStackTrace ();
		  // Perform any other exception handling that's appropriate.
		}
		finally {
		  if (is != null) { try {
			is.close();
		} catch (IOException e) {
			return new byte[]{};
		} }
		}
		return bais.toByteArray();
	}
	
	public static byte[] getData(File f){
		try {
			return Files.toByteArray(f);
		} catch (IOException e) {
			e.printStackTrace();
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
		return !isEqual(url, local);
	}
}
