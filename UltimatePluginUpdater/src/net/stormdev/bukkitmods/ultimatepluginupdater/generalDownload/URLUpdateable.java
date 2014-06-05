package net.stormdev.bukkitmods.ultimatepluginupdater.generalDownload;

import java.io.File;
import java.io.Serializable;

public class URLUpdateable implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private String local;
	private String remote;
	private File saveLoc;
	
	public URLUpdateable(String localPath, String remoteURL, File saveLoc){
		this.local = localPath;
		this.remote = remoteURL;
		this.saveLoc = saveLoc;
	}
	
	public File getSaveFile(){
		return saveLoc;
	}
	
	public String getLocalPath(){
		return local;
	}
	
	public String getRemoteURL(){
		return remote;
	}
}
