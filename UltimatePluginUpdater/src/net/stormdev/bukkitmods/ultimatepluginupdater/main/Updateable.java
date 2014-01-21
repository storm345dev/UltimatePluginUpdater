package net.stormdev.bukkitmods.ultimatepluginupdater.main;

import java.io.Serializable;
import java.net.URL;

public class Updateable implements Serializable {
	private static final long serialVersionUID = 1906965187595531169L;
	public String slug = "";
	public String oldUrl = "";
	public String fileName = "";
	public String pluginName = "";
    public Updateable(String pluginName, String fileName, String slug){
    	this.slug = slug;
    	this.fileName = fileName;
    	this.pluginName = pluginName;
    }
    public String getSlug(){
    	return this.slug;
    }
    public String getOldUrl(){
    	return this.oldUrl;
    }
    public void setOldUrl(String oldUrl){
    	this.oldUrl = oldUrl;
    	return;
    }
    public void setSlug(String slug){
    	this.slug = slug;
    	return;
    }
    public String getFileName(){
    	return this.fileName;
    }
    public void setFileName(String fileName){
    	this.fileName = fileName;
    }
    public String getPluginName(){
    	return this.pluginName;
    }
    public void setPluginName(String pluginName){
    	this.pluginName = pluginName;
    }
}
