package net.stormdev.bukkitmods.ultimatepluginupdater.main;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import net.stormdev.bukkitmods.ultimatepluginupdater.utils.FileGetter;
import net.stormdev.bukkitmods.ultimatepluginupdater.utils.PluginRegistration;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class main extends JavaPlugin implements CommandExecutor {
	public YamlConfiguration lang = new YamlConfiguration();
	public static main plugin;
	public static FileConfiguration config = new YamlConfiguration();
	public static Colors colors; 
	public static CustomLogger logger = null;
	public static BukkitTask checker = null;
	public Boolean updaterEnabled = true;
	public ArrayList<String> configuredPlugins = new ArrayList<String>();
	public static boolean strictVersioning = false;
	public static boolean useUpdateFolder = true;
	public HashMap<String, PluginRegistration> pluginRegistrations = new HashMap<String, PluginRegistration>();
	@SuppressWarnings("unchecked")
	public void onEnable(){
		plugin = this;
		config = getConfig();
		logger = new CustomLogger(getServer().getConsoleSender(), getLogger());
		if (new File(getDataFolder().getAbsolutePath() + File.separator
				+ "config.yml").exists() == false
				|| new File(getDataFolder().getAbsolutePath() + File.separator
						+ "config.yml").length() < 1) {
			getDataFolder().mkdirs();
			File configFile = new File(getDataFolder().getAbsolutePath()
					+ File.separator + "config.yml");
			try {
				configFile.createNewFile();
			} catch (IOException e) {
			}
		}
        try {
        	//Setup the config
        	if (!config.contains("general.logger.colour")) {
				config.set("general.logger.colour", true);
        	}
        	if (!config.contains("general.updater.enable")) {
				config.set("general.updater.enable", true);
        	}
        	if (!config.contains("general.updater.logChecks")) {
				config.set("general.updater.logChecks", false);
        	}
        	if (!config.contains("general.updater.strictVersioning")) {
				config.set("general.updater.strictVersioning", false);
        	}
        	else{ //Value already set - NOT first run
            	this.updaterEnabled = config.getBoolean("general.updater.enable");
            }
        	if (!config.contains("general.updater.useUpdateFolder")) {
				config.set("general.updater.useUpdateFolder", true);
        	}
        	//Setup the colour scheme
        	if (!config.contains("colorScheme.success")) {
				config.set("colorScheme.success", "&a");
			}
			if (!config.contains("colorScheme.error")) {
				config.set("colorScheme.error", "&c");
			}
			if (!config.contains("colorScheme.info")) {
				config.set("colorScheme.info", "&6");
			}
			if (!config.contains("colorScheme.title")) {
				config.set("colorScheme.title", "&3");
			}
			if (!config.contains("colorScheme.tp")) {
				config.set("colorScheme.tp", "&d");
			}
        } catch(Exception e){
        }
		saveConfig();
		useUpdateFolder = config.getBoolean("general.updater.useUpdateFolder");
		strictVersioning = config.getBoolean("general.updater.strictVersioning");
		//Load the colour scheme
		colors = new Colors(config.getString("colorScheme.success"),
				config.getString("colorScheme.error"),
				config.getString("colorScheme.info"),
				config.getString("colorScheme.title"),
				config.getString("colorScheme.title"));
		logger.info("Config loaded!");
		File softwareFolder = new File(getDataFolder().getAbsolutePath() + File.separator
				+ "mySoftware");
		softwareFolder.mkdirs();
		UpdateableManager.load(softwareFolder); //Load the .updateable files from the folder
		//Schedule checker
		
		if(checker == null){
			checker = getServer().getScheduler().runTaskTimerAsynchronously(plugin, new Runnable(){

				public void run() {
					main.logger.info("Starting plugin updater...");
					UpdateableManager.checkAll();
					main.logger.info("Plugin updater started!");
					return;
				}}, 20l, 36000l);
		}
		for(Object k:getDescription().getCommands().keySet()){
			getCommand((String) k).setExecutor(this);
		}
		final File cfgPlugins = new File(getDataFolder().getAbsolutePath() + File.separator
				+ "cfgPlugins.data"); //Tells the plugin what local files have already been auto-configured
		Boolean save = false;
		if (cfgPlugins.exists() == false
				|| cfgPlugins.length() < 1) {
			getDataFolder().mkdirs();
			try {
				cfgPlugins.createNewFile();
			} catch (IOException e) {
			}
			save = true;
		}
		else{
			configuredPlugins = (ArrayList<String>) ObjectLoader.load(cfgPlugins);
		}
		getServer().getScheduler().runTaskAsynchronously(this, new BukkitRunnable(){

			public void run() {
				boolean save = false;
				for(Plugin pl:getServer().getPluginManager().getPlugins()){
					if(!configuredPlugins.contains(pl.getName().toLowerCase())){
						newPlugin(pl);
						configuredPlugins.add(pl.getName().toLowerCase());
						save = true;
					}
				}
				if(save){
				    ObjectLoader.save(configuredPlugins, cfgPlugins);	
				}
				return;
			}});
		if(save){
		    ObjectLoader.save(configuredPlugins, cfgPlugins);	
		}
		getServer().getPluginManager().registerEvents(new UPUEventListener(), this);
		logger.info("UltimatePluginUpdater v"+plugin.getDescription().getVersion()+" has been enabled!");
	}
	
	public void onDisable(){
		checker.cancel();
		checker = null;
		logger.info("UltimatePluginUpdater has been disabled!");
	}
	
	@SuppressWarnings("unused")
	private void copy(InputStream in, File file) {
		try {
			OutputStream out = new FileOutputStream(file);
			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
				// System.out.write(buf, 0, len);
			}
			out.close();
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static String colorise(String prefix) {
		 return ChatColor.translateAlternateColorCodes('&', prefix);
	}
	public boolean onCommand(CommandSender sender, Command cmd, String alias,
			String[] args) {
		if(cmd.getName().equalsIgnoreCase("upu")){
			if(args.length > 0){
				if(args[0].equalsIgnoreCase("check")){
					sender.sendMessage(main.colors.getInfo()+"Initiated update check!");
					main.logger.info(sender.getName()+" initiated an update check!");
					UpdateableManager.checkAll();
					return true;
				}
				else if(args[0].equalsIgnoreCase("update")){
					if(args.length < 2){
						return false;
					}
					String pluginName = "";
					for(int i = 1;i<args.length;i++){
						if(pluginName == ""){
							pluginName = args[i];
						}
						else{
						    pluginName = pluginName + " " + args[i];
						}
					}
					String pname = pluginName;
					Updateable updat = null;
					@SuppressWarnings("unchecked")
					ArrayList<Updateable> updats = (ArrayList<Updateable>) UpdateableManager.updateables.clone();
				    for(Updateable up:updats){
				    	if(up.getPluginName().replaceAll(" ", "-").equalsIgnoreCase(pname)){
				    		updat = up;
				    	}
				    }
				    if(updat == null){
				    	sender.sendMessage(main.colors.getError()+"Plugin not registered!");
				    	return true;
				    }
				    else{
				    	UpdateableManager.updateables.remove(updat);
				    	updat.setOldUrl("NULL");
				    	UpdateableManager.updateables.add(updat);
				    	sender.sendMessage(main.colors.getSuccess()+"Plugin will be updated soon!");
				    	return true;
				    }
				}
				else if(args[0].equalsIgnoreCase("unregister")){
					if(args.length < 2){
						return false;
					}
					String pluginName = null;
					for(int i = 1;i<args.length;i++){
						if(pluginName == null){
							pluginName = args[i];
						}
						else{
						    pluginName = pluginName + " " + args[i];
						}
					}
					String pname = pluginName;
					Updateable updat = null;
					@SuppressWarnings("unchecked")
					ArrayList<Updateable> updats = (ArrayList<Updateable>) UpdateableManager.updateables.clone();
				    for(Updateable up:updats){
				    	if(up.getPluginName().replaceAll(" ", "-").equalsIgnoreCase(pname)){
				    		updat = up;
				    	}
				    	else if(up.getPluginName().equalsIgnoreCase(pname)){
				    		updat = up;
				    	}
				    }
				    if(updat == null){
				    	sender.sendMessage(main.colors.getError()+"Plugin not registered! (" + pname+")");
				    	return true;
				    }
				    else{
				    	UpdateableManager.remove(updat);
						UpdateableManager.save();
						sender.sendMessage(main.colors.getSuccess()+"Plugin successfully unregistered from UltimatePluginUpdater!");
				    	return true;
				    }
				}
				else if(args[0].equalsIgnoreCase("register") || args[0].equalsIgnoreCase("install")){
					if(args.length < 2){
						return false;
					}
					if(!(sender instanceof Player)){
						sender.sendMessage(main.colors.getError()+"Players only!");
						return true;
					}
					String pluginName = "";
					for(int i = 1;i<args.length;i++){
						if(pluginName == ""){
							pluginName = args[i];
						}
						else{
						    pluginName = pluginName + " " + args[i];
						}
					}
					Boolean install = false;
					if(args[0].equalsIgnoreCase("install")){
						install = true;
					}
					new PluginRegistration(pluginName, sender.getName(), sender, install);
					return true;
				}
			}
			@SuppressWarnings("unchecked")
			ArrayList<Updateable> updats = (ArrayList<Updateable>) UpdateableManager.updateables.clone();
			String names = "";
			for(Updateable u:updats){	
				String name = u.pluginName;
				if(names != ""){
				names = names + ", ";
				}
				names = names + name;
			}
			sender.sendMessage(main.colors.getTitle()+"My Registered Plugins: ("+updats.size()+")");
			sender.sendMessage(main.colors.getInfo()+names);
			return true;
		}
		return false;
	}
	public void newPlugin(Plugin pl){
		String pluginName = pl.getName();
		String fileName = pluginName;
		Boolean valid = true;
		String slug = pluginName;
		if(valid){ //Url is formed correctly
			try {
				URL v = FileGetter.getLatestPluginFileURL(new Updateable(pluginName, fileName, slug));
			    if(v == null){
			    	valid = false;
			    }
				//If no error, the plugin is on Bukkit.org, if not - it isn't
			} catch (IOException e) {
				valid = false;
			}
		}
		if(valid){ //Plugin is on bukkit.org - Info above is correct
			UpdateableManager.registerUpdateable(pluginName, fileName, slug); //Let the plugin know to update it
			main.logger.info("Found and registered "+pluginName);
		}
		return;
	}
}
