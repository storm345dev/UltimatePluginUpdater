package net.stormdev.bukkitmods.ultimatepluginupdater.main;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import net.stormdev.bukkitmods.ultimatepluginupdater.utils.FileGetter;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;

public class UpdateableManager {
	public static ArrayList<Updateable> updateables = new ArrayList<Updateable>();
    private static File folder = null;
    private static Boolean needReload = false;
    private static Boolean runningUpdates = false;
    public static int checked = 0;
    public static Thread updater = null;
	public static void load(File softwareFolder){
		updateables = new ArrayList<Updateable>();
    	folder = softwareFolder;
    	if(folder.isDirectory()){
    		File dir = folder;
    		File[] files = dir.listFiles();
    		for(File file:files){
    			if(file.getName().toLowerCase().endsWith(".updateable")){
    				Object obj = ObjectLoader.load(file);
    				try {
						Updateable updateable = (Updateable) obj;
						updateables.add(updateable);
						main.logger.info("Successfully loaded updateable "+updateable.getFileName()+"!");
					} catch (Exception e) {
						main.logger.info("Failed to load updateable "+file.getName()+", invalid?");
					}
    			}
    		}
    	}
    }
    public static void save(){
    	for(Updateable updateable: updateables){
    		String fileName = updateable.getFileName();
    		String path = folder.getAbsolutePath() + File.separator + fileName + ".updateable";
    		File file = new File(path);
    		if(!file.exists() || file.length() < 1){
    			try {
					file.createNewFile();
				} catch (IOException e) {
				}
    		}
    		ObjectLoader.save(updateable, file);
    	}
    	return;
    }
    public static void save(Updateable updateable){
    	String fileName = updateable.getFileName();
		String path = folder.getAbsolutePath() + File.separator + fileName + ".updateable";
		File file = new File(path);
		if(!file.exists() || file.length() < 1){
			try {
				file.createNewFile();
			} catch (IOException e) {
			}
		}
		ObjectLoader.save(updateable, file);
		return;
    }
    public static void registerUpdateable(String pluginName, String fileName, String slug){
    	Updateable updateable = new Updateable(pluginName, fileName, slug);
    	updateables.add(updateable);
    	save();
    }
    public static URL checkForUpdate(Updateable updateable){
    	try {
    		URL last = null;
			try {
				 last = FileGetter.getLatestPluginFileURL(updateable);
			} catch (Exception e) {
				main.logger.info("Connect Exception: "+e.getMessage());
				main.logger.info("plugin: "+updateable.getPluginName());
				main.logger.info("slug: "+updateable.getSlug());
				main.logger.info("Can't connect to dev.bukkit.org, Bukkit overloaded, or plugin removed?");
				return null;
			}
			String old = last.toExternalForm().toLowerCase();
			String local = updateable.getOldUrl();
			if(!old.equalsIgnoreCase(local)){
				//Update
				return last;
			}
		} catch (Exception e) {
			//No update
			return null;
		}
    	//No update
    	return null;
    }
    public static void checkAndRunUpdate(final Updateable updateable, final Boolean reload){
    	try {
    		Boolean log = main.config.getBoolean("general.updater.logChecks");
    		final URL update = checkForUpdate(updateable);
			if(update == null){
				if(log){
					main.logger.info(main.colors.getInfo()+updateable.getPluginName() + " is up-to-date!");
				}
				checked++;
				checkDone(false);
				return;
			}
			if(!main.plugin.updaterEnabled){
				main.logger.info(main.colors.getInfo()+"Found new update for: "+updateable.getPluginName() + " - Not downloaded; Reason: Updater disabled");
				checked++;
				checkDone(false);
				return;
			}
			if(log){
				main.logger.info(main.colors.getInfo()+"Found new update for: "+updateable.getPluginName() + " - Downloading...");
			}
			String fileName = update.getFile();
			int z = fileName.lastIndexOf("/");
			fileName = fileName.substring(z+1);
			String path = "";
			Boolean zip = false;
    		if(fileName.toLowerCase().endsWith(".jar")){
    			if(!main.useUpdateFolder){
    				path = main.plugin.getDataFolder().getParentFile().getAbsolutePath() + File.separator + updateable.getFileName() + ".jar";
    			}
    			else{
    				path = Bukkit.getUpdateFolderFile().getAbsolutePath() + File.separator + updateable.getFileName() + ".jar";
    			}
    		}
    		else if(fileName.toLowerCase().endsWith(".zip")){
    		    zip = true;	
    		}
    		else{
    			String ext = fileName.toLowerCase().substring(fileName.toLowerCase().lastIndexOf(Pattern.quote(".")));
    			path = main.plugin.getDataFolder().getParentFile().getAbsolutePath() + File.separator + "pluginFiles" + File.separator + updateable.getFileName() + ext;
    		}
    		File file = null;
    		if(!zip){
    		file = new File(path);
			if(!file.exists() || file.length() < 1){
    			try {
					file.createNewFile();
				} catch (IOException e) {
				}
			}
			main.logger.info("Saving update to "+file.getAbsolutePath());
    		}
    		if(reload){
				save();
			}
			    	 Boolean toReload = true;
			if(!zip){
				boolean success = false;
				int downloadAttempts = 0;
				
				while(downloadAttempts < 5){
			    	try {
						double length = (update.openConnection().getContentLength()/1024)+1; //In KB
						String name = updateable.pluginName;
						System.out.println("Started updating "+name+"...");
						System.out.println("Length: "+length+"KB");
						//URLConnection connection = update.openConnection();
						/*
						InputStream inUp = new BufferedInputStream(update.openStream());
						 ByteArrayOutputStream outUp = new ByteArrayOutputStream();
						 byte[] buf = new byte[1024]; //1024 bytes = 1KB
						 int n = 0;
						 int comp = -1; 
						 int off = 0;
						 int prevPercent = -1;
						 while (-1!=(n=inUp.read(buf)))
						 {
							 comp = comp + 1;
							 int percent = 0;
							 if(comp >= 0 ){
								//progress must be in %
								 percent = (int) ((Double.parseDouble(""+comp)/Double.parseDouble(""+length))*100);
							 }
							if(percent % 20 == 0){
							if(percent != prevPercent){
							System.out.println(name+"(" + percent + "%)");
							prevPercent = percent;
							}
							}
						    outUp.write(buf, off, n);
						    off = off+n;
						 }
						 outUp.close();
						 inUp.close();
						 */
						 InputStream inUp = new BufferedInputStream(update.openStream());
						 ByteArrayOutputStream outUp = new ByteArrayOutputStream();
						 byte[] buf = new byte[1024]; //1024 bytes = 1KB
						 int n = 0;
						 int comp = -1; 
						 int prevPercent = -1;
						 while (-1!=(n=inUp.read(buf)))
						 {
							 comp = comp + 1;
							 int percent = 0;
							 if(comp >= 0 ){
								//progress must be in %
								 percent = (int) ((Double.parseDouble(""+comp)/Double.parseDouble(""+length))*100);
							 }
							if(percent % 20 == 0){
							if(percent != prevPercent){
							System.out.println(name+"(" + percent + "%)");
							prevPercent = percent;
							}
							}
						    outUp.write(buf, 0, n);
						 }
						 outUp.close();
						 inUp.close();
						 System.out.println("Downloaded content! Saving...");
						 byte[] responseUp = outUp.toByteArray();
						 file.getParentFile().mkdirs();
						 FileOutputStream fos = new FileOutputStream(file);
						     fos.write(responseUp);
						     fos.flush();
						     fos.close();
						 System.out.println("Update complete!");
						 String oldUrl = update.toExternalForm().toLowerCase();
						 updateable.setOldUrl(oldUrl);
						 //done;
						 if(reload){
							toReload = false;
							reload();
						 }
						 else{
							toReload = true;
							needReload = true;
						 }
					} catch (IOException e) {
						downloadAttempts++;
						continue;
					}
			    	success = true;
			    	break;
				}
				if(!success || downloadAttempts >= 5){
					main.logger.info(main.colors.getError()+"Update failed for: "+updateable.getPluginName()+"!");
					return;
				}
			}
			else{
				//Is a .zip
				
				
				boolean success = false;
				int downloadAttempts = 0;
				
				while(downloadAttempts < 5){		
					try {
						File tmp = File.createTempFile("tmpUltimatePluginUpdater"+new Random().nextInt(10), ".zip");
						double length = (update.openConnection().getContentLength()/1024)+1; //In KB
						String name = updateable.pluginName;
						System.out.println("Started downloading zip for "+name+"...");
						System.out.println("Length: "+length+"KB");
						//URLConnection connection = update.openConnection();
						InputStream inUp = new BufferedInputStream(update.openStream());
						 ByteArrayOutputStream outUp = new ByteArrayOutputStream();
						 byte[] buf = new byte[1024]; //1024 bytes = 1KB
						 int n = 0;
						 int comp = -1; 
						 int prevPercent = -1;
						 while (-1!=(n=inUp.read(buf)))
						 {
							 comp = comp + 1;
							 int percent = 0;
							 if(comp >= 0 ){
								//progress must be in %
								 percent = (int) ((Double.parseDouble(""+comp)/Double.parseDouble(""+length))*100);
							 }
							if(percent % 20 == 0){
							if(percent != prevPercent){
							System.out.println(name+"(" + percent + "%)");
							prevPercent = percent;
							}
							}
						    outUp.write(buf, 0, n);
						 }
						 outUp.close();
						 inUp.close();
						 byte[] responseUp = outUp.toByteArray();
						 tmp.getParentFile().mkdirs();
						 if(tmp.length() < 1 || !tmp.exists()){
							 tmp.createNewFile();
						 }
						 FileOutputStream fos = new FileOutputStream(tmp);
						     fos.write(responseUp);
						     fos.flush();
						     fos.close();
						 System.out.println("Downloaded zip!");
						 updateables.remove(updateable);
							String oldUrl = update.toExternalForm().toLowerCase();
							updateable.setOldUrl(oldUrl);
							updateables.add(updateable);
						 ZipInputStream zis = 
						    		new ZipInputStream(new FileInputStream(tmp));
						    	//get the zipped file list entry
						    	ZipEntry ze = zis.getNextEntry();
						    	byte[] buffer = new byte[1024];
						    	while(ze!=null){
						 
						    	   String fn = ze.getName();
						    	   Boolean plugin = false;
						    	   String plfName = updateable.fileName;
						    	   Boolean logIt = main.config.getBoolean("general.updater.logChecks");
						    	   if(logIt){
						    	   main.logger.info("Found file: "+fn);
						    	   }
						           if(fn.equalsIgnoreCase(plfName+".jar") || fn.equalsIgnoreCase(plfName.replaceAll(" ", "")) || fn.equalsIgnoreCase(plfName.replaceAll(" ", "-"))){
						        	   plugin = true;
						           }
						           if(plugin){
						        	   //This is the file
						        	   if(!main.useUpdateFolder){
						        		   path = main.plugin.getDataFolder().getParentFile().getAbsolutePath() + File.separator + updateable.getFileName() + ".jar";
						        	   }
						        	   else{
						        		   path = Bukkit.getUpdateFolderFile().getAbsolutePath() + File.separator + updateable.getFileName() + ".jar";
						        	   }
						        	   File newFile = new File(path);
						        	   newFile.getParentFile().mkdirs();
						        	   if(!newFile.exists() || newFile.length() < 1){
						        		   newFile.createNewFile();
						        	   }
						        	   main.logger.info("Saving jar to: "+newFile.getAbsolutePath());
						        	   FileOutputStream ffos = new FileOutputStream(newFile);             
						               int len;
						               while ((len = zis.read(buffer)) > 0) {
						          		ffos.write(buffer, 0, len);
						               }
						               ffos.close(); 
						           }
						           else {
						        	   if(!ze.isDirectory()){ //Isn't a directory
						        		   //Look for similar config in plugin's data folder and if so copy values across
						        		   String nn = fn;
						        		   if(fn.contains(".")){
						        			   nn = fn.substring(fn.lastIndexOf("."));
						        		   }
						        	       File similar = new File(main.plugin.getDataFolder().getParentFile() + File.separator + updateable.getPluginName() + File.separator + fn);
						        	       Boolean yaml = true;
						        	       if(!nn.equalsIgnoreCase(".yaml") && !nn.equalsIgnoreCase(".yml")){
						        	    	   yaml = false;
						        	       }
						        	       Boolean valid = true;
						        	       if(yaml){
						        	    	   if(logIt){
							        	    	   main.logger.info("Yaml file found!");
										       }
						        	       YamlConfiguration prev = new YamlConfiguration();
						        	       if(similar.exists() && similar.length() > 0){
						        	    	   try {
												prev.load(similar);
											} catch (Exception e) {
												//Not right file or not yaml
											}
						        	       }
						        	       YamlConfiguration fromZip = new YamlConfiguration();	  
						        	       File temp = File.createTempFile(fn, ".tmp", new File(main.plugin.getDataFolder().getParentFile() + File.separator + updateable.getPluginName()));
							        	   temp.getParentFile().mkdirs();
							        	   if(!temp.exists() || temp.length() < 1){
							        		   temp.createNewFile();
							        	   }
							        	   FileOutputStream ffos = new FileOutputStream(temp);             
							               int len;
							               while ((len = zis.read(buffer)) > 0) {
							          		ffos.write(buffer, 0, len);
							               }
							               ffos.close(); 
							               try {
											fromZip.load(temp);
										} catch (Exception e) {
											valid = false;
										}
							               temp.delete(); 
							               if(valid){
							            	   //Copy values and save to correct location
							            	   if(logIt){
							        	    	   main.logger.info("Copying previous yaml values!");
										       }
							            	   Set<String> keys = prev.getKeys(true);
							            	   for(String key:keys){
							            		   fromZip.set(key, prev.get(key));
							            	   }
							            	   similar.getParentFile().mkdirs();
							            	   if(!similar.exists() || similar.length() < 1){
							            		   similar.createNewFile();
							            	   }
							            	   main.logger.info("Saving "+fn+" to: "+similar.getAbsolutePath());
							            	   fromZip.save(similar);
							               }
							               else{
							            	   main.logger.info(main.colors.getError()+"INVALID YAML FILE");
							               }
						        	       } //End if yaml
							               else{
							            	   main.logger.info("Saving "+fn+" to: "+similar.getAbsolutePath());
								        	   similar.getParentFile().mkdirs();
								        	   if(!similar.exists() || similar.length() < 1){
								        		   similar.createNewFile();
								        	   }
								        	   FileOutputStream ffoss = new FileOutputStream(similar);             
								               int len2;
								               while ((len2 = zis.read(buffer)) > 0) {
								          		ffoss.write(buffer, 0, len2);
								               }
								               ffoss.close(); 
							               }
						        	   }
						           }
						           ze = zis.getNextEntry();
						    	}
						    	System.out.println("Zip extract finished!");
						        zis.closeEntry();
						    	zis.close();
						    	tmp.delete();
						 //done;
						 if(reload){
							toReload = false;
							save(updateable);
							reload();
						 }
						 else{
							toReload = true;
							needReload = true;
						 }
					} catch (Exception e) {
						downloadAttempts++;
						continue;
					}
					success = true;
					break;
				}
    			if(!success || downloadAttempts >= 5){
					main.logger.info(main.colors.getError()+"Update failed for: "+updateable.getPluginName()+"!");
					return;
    			}
    		}
					save(updateable);
			    	checked++;
			    	checkDone(toReload);
			    	return;
		} catch (Exception e) {
			checked++;
			checkDone(false);
			return;
		}
    }
    public static void checkAll(){
    	updater = new Thread(){ //ONE updater thread which is NOT linked to the Bukkit scheduler
    		public void run(){
    			checked = 0;
    			runningUpdates = true;
    			@SuppressWarnings("unchecked")
				ArrayList<Updateable> ups = (ArrayList<Updateable>) updateables.clone();
    			for(Updateable updateable:ups){
    				if(main.config.getBoolean("general.updater.logChecks")){
    					System.out.println("Checking plugin ("+(checked+1)+"/"+ups.size()+")...");
    				}
    				checkAndRunUpdate(updateable, false);
    			}
    			//Finished checking all plugins for updates
    			System.gc();
    			System.out.println("Updater terminated!");
    			return;
    		}
    	};
    	updater.setDaemon(true); //Close when server dies
    	updater.start();
    	System.gc();
    	return;
    }
    public static void stopUpdater(){
    	if(updater != null){
    		updater.interrupt(); //Attempt to stop it
    	}
    }
    public static void checkDone(Boolean reloadit){
    	if(!needReload && reloadit){
    	needReload = reloadit;
    	}
    	if(runningUpdates){
    		final int toCheck = updateables.size();
    		if(checked < toCheck){
				return;
			}
    		runningUpdates = false;
    		if(main.config.getBoolean("general.updater.logChecks") && !needReload){
    			main.logger.info("All plugins up to date!");
    		}
    		if(main.config.getBoolean("general.updater.logChecks") && needReload){
    			main.logger.info("Finished checking for updates!");
    		}
    		System.gc();
			if(needReload){
				main.logger.info("Updated, restarting server!");
				save(); //If you don't do... recursive download
				reload();
				return;
			}
			else{
				//Finished update chunk
			}
			System.gc();
			return;
    	}
    	return;
    }
    public static void reload(){
    	try {
			needReload = false;
		} catch (Exception e) {
		}
    	Bukkit.getServer().getScheduler().runTask(main.plugin, new Runnable(){

			public void run() {
				main.plugin.getServer().reload();
				return;
			}});
    	return;
    }
    public static void remove(Updateable updateable){
    	updateables.remove(updateable);
    	try {
			File f = new File(folder.getAbsolutePath() + File.separator + updateable.getFileName() + ".updateable");
			f.delete();
		} catch (Exception e) {
			main.logger.info("Unable to delete updateable? Not exist?");
			//Not there to be deleted
		}
    	return;
    }
}
