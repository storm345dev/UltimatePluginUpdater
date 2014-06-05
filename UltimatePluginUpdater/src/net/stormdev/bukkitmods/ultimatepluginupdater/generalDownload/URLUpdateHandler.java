package net.stormdev.bukkitmods.ultimatepluginupdater.generalDownload;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import net.stormdev.bukkitmods.ultimatepluginupdater.main.ObjectLoader;
import net.stormdev.bukkitmods.ultimatepluginupdater.main.UpdateableManager;
import net.stormdev.bukkitmods.ultimatepluginupdater.main.main;

import org.bukkit.Bukkit;

public class URLUpdateHandler {
	
	private List<URLUpdateable> registered = new ArrayList<URLUpdateable>();
	private static boolean runningUpdates = false;
	private File saveFileFolder;
	
	public URLUpdateHandler(){
		saveFileFolder = new File(main.plugin.getDataFolder()+File.separator+"mySoftware"+File.separator+"urlUpdates");
		saveFileFolder.mkdirs();
	}
	
	public List<URLUpdateable> getAll(){
		return new ArrayList<URLUpdateable>(registered);
	}
	
	public boolean unregister(String url){
		for(URLUpdateable check:new ArrayList<URLUpdateable>(registered)){
			if(check.getRemoteURL().equalsIgnoreCase(url)){
				registered.remove(check);
				if(!check.getSaveFile().delete()){
					check.getSaveFile().deleteOnExit();
				}
				return true;
			}
		}
		return false;
	}
	
	public boolean registerNew(File local, URL url){
		if(!(local.exists() && local.length() > 1)){
			return false;
		}
		if(!local.getName().toLowerCase().endsWith(".jar")){
			return false;
		}
		String name = local.getName().toLowerCase().replaceAll(Pattern.quote(".jar"), "");
		String path = local.getAbsolutePath();
		String remote = url.toString();
		File saveLoc = new File(saveFileFolder+File.separator+name+".urlUpdateable");
		
		final URLUpdateable newUpdat = new URLUpdateable(path, remote, saveLoc);
		
		Bukkit.getScheduler().runTaskAsynchronously(main.plugin, new Runnable(){

			public void run() {
				registered.add(newUpdat);
				save(newUpdat);
				return;
			}});
		
		return true;
	}
	
	public void load(){
		File[] contents = saveFileFolder.listFiles();
		for(File file:contents){
			if(file.getName().toLowerCase().endsWith(".urlUpdateable")){
				Object obj = ObjectLoader.load(file);
				try {
					URLUpdateable updateable = (URLUpdateable) obj;
					registered.add(updateable);
					main.logger.info("Successfully loaded url updateable "+file.getName()+"!");
				} catch (Exception e) {
					main.logger.info("Failed to load url updateable "+file.getName()+", invalid?");
				}
			}
		}
	}
	
	public void save(URLUpdateable updat){
		File saveFile = updat.getSaveFile();
		saveFile.getParentFile().mkdirs();
		if(!saveFile.exists()){
			try {
				saveFile.createNewFile();
			} catch (IOException e) {
				main.logger.info("Failed to save url updateable: "+e.getLocalizedMessage());
				return;
			}
		}
		
		ObjectLoader.save(updat, saveFile);
	}
	
	public void callCheck(){
		Runnable run = new Runnable(){

			public void run() {
				if(runningUpdates){
					return;
				}
				
				runningUpdates = true;
				checkAll();
				runningUpdates = false;
				return;
			}
		
		};
		
		execAsync(run);
	}
	
	private synchronized void checkAll(){
		for(URLUpdateable updat:registered){
			check(updat);
		}
	}
	
	public void check(URLUpdateable update){
		String remote = update.getRemoteURL();
		String local = update.getLocalPath();
		boolean toUpdate;
		try {
			toUpdate = UrlData.shouldUpdate(local, remote);
		} catch (MalformedURLException e) {
			main.logger.info("Error in updateable, malformed URL: "+remote);
			return;
		}
		if(toUpdate){
			String jarname = null;
			main.logger.info("Starting update of "+remote);
			File f = new File(local);
			if(!f.exists()){
				try {
					File fr = new File(new URL(remote).toURI());
					jarname = fr.getName();
				} catch (Exception e) {
					main.logger.info("Error in updateable: "+e.getLocalizedMessage());
					return;
				}
			}
			else {
				jarname = f.getName();
			}
			
			if(jarname == null){
				main.logger.info("Error determining jar name for update!");
			}
			
			try {
				downloadUpdate(jarname, new URL(remote));
			} catch (MalformedURLException e) {
				main.logger.info("Error in updateable, malformed URL: "+remote);
				return;
			}
		}
	}
	
	private void downloadUpdate(String jarName, URL remote){
		File file = new File(Bukkit.getUpdateFolderFile()+File.separator+jarName);
		UpdateableManager.doDownload(remote, file);
	}
	
	private void execAsync(Runnable run){
		if(!Bukkit.isPrimaryThread()){
			run.run();
		}
		else {
			Bukkit.getScheduler().runTaskAsynchronously(main.plugin, run);
		}
	}
}
