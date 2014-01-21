package net.stormdev.bukkitmods.ultimatepluginupdater.utils;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Pattern;

import net.stormdev.bukkitmods.ultimatepluginupdater.main.ObjectLoader;
import net.stormdev.bukkitmods.ultimatepluginupdater.main.Updateable;
import net.stormdev.bukkitmods.ultimatepluginupdater.main.UpdateableManager;
import net.stormdev.bukkitmods.ultimatepluginupdater.main.main;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class PluginRegistration {
	public String pluginName = "";
	public String fileName = "";
	public String url = "";
	public String playername = "";
	public int stage = 0;
	Boolean complete = false;
	public CommandSender sender = null;
	public Boolean player = false;
	public Boolean install = false;
	public PluginRegistration(String pluginName, String senderName, CommandSender sender, Boolean install){
		this.pluginName = pluginName;
		this.sender = sender;
		this.install = install;
		this.playername = sender.getName();
		if(sender instanceof Player){
			player = true;
		}
		CommandSender p = getPlayer();
		main.plugin.pluginRegistrations.put(playername, this);
		if(p != null){
			p.sendMessage(main.colors.getInfo() + "Please type the fileName of the plugin.jar into the chat!");
		}
		stage = 0;
	}
	public CommandSender getPlayer(){
		CommandSender p = null;
		if(player){
		p = main.plugin.getServer().getPlayer(playername);
		}
		else{
			return sender;
		}
		if(p == null){
			main.plugin.pluginRegistrations.remove(playername);
			return null;
		}
		return p;
	}
    public void recievedInput(String inputText){
    	if(getPlayer() == null){
    		main.plugin.pluginRegistrations.remove(playername);
			return;
    	}
    	if(stage == 0){
    		this.fileName = inputText;
    		stage = 1;
    		main.plugin.pluginRegistrations.put(playername, this);
    		CommandSender p = getPlayer();
    		if(p != null){
    			p.sendMessage(main.colors.getInfo() + "Please Bukkit Name / Slug of the plugin in the chat!");
    		}
    	}
    	else if(stage == 1){
    		this.url = inputText;
    		validate();
    		main.plugin.pluginRegistrations.remove(playername);
    	}
    	return;
    }
    public void validate(){
    	//TODO Validate and register
    	CommandSender p = getPlayer();
    	if(p != null){
    	String pluginName = this.pluginName;
    	if(this.fileName.toLowerCase().endsWith(".jar")){
    		try {
				this.fileName = this.fileName.replaceAll(this.fileName.substring(this.fileName.lastIndexOf(".")), "");
			} catch (Exception e) {
				main.logger.info("An error occured: Tripped by filename including extension-Handler failed");
				//WTF HAPPENED
			}
    	}
		final String fileName = this.fileName;
		Boolean valid = true;
		Boolean validPlugin = false;
		if(!install){
		Plugin[] plugins = main.plugin.getServer().getPluginManager().getPlugins().clone();
		for(Plugin plugin:plugins){
			if(plugin.getName().equalsIgnoreCase(pluginName)){
				validPlugin = true;
				pluginName = plugin.getName();
			}
		}
		if(!validPlugin){
			valid = false;
		}
		}
		final Boolean v = valid;
		final CommandSender pl = p;
		final String uri = this.url;
		final String pn = pluginName;
		main.plugin.getServer().getScheduler().runTaskAsynchronously(main.plugin, new Runnable(){

			@SuppressWarnings("unchecked")
			public void run() {
				Boolean valid = v;
				CommandSender p = pl;
				String pluginName = pn;
				String url = uri;
				if(valid){
					try {
						p.sendMessage(main.colors.getInfo()+"Validating...");
						FileGetter.getLatestPluginFileURL(new Updateable(pluginName, fileName, url));
					    //If no error, the plugin is on Bukkit.org, if not - it isn't
					} catch (IOException e) {
						valid = false;
					}
				}
				if(valid){ //Plugin is on bukkit.org - Info above is correct
					UpdateableManager.registerUpdateable(pluginName, fileName, url); //Let the plugin know to update it
					p.sendMessage(main.colors.getSuccess()+"Successfully registered plugin!");
					if(install){
						ArrayList<String> configuredPlugins = new ArrayList<String>();
						File cfgPlugins = new File(main.plugin.getDataFolder().getAbsolutePath() + File.separator
								+ "cfgPlugins.data"); //Tells the plugin what local files have already been auto-configured
						if (cfgPlugins.exists() == false
								|| cfgPlugins.length() < 1) {
							main.plugin.getDataFolder().mkdirs();
							try {
								cfgPlugins.createNewFile();
							} catch (IOException e) {
							}
						}
						else{
							configuredPlugins = (ArrayList<String>) ObjectLoader.load(cfgPlugins);
						}
						configuredPlugins.add(pluginName.toLowerCase());
						ObjectLoader.save(configuredPlugins, cfgPlugins);	
					}
				}
				else{
					p.sendMessage(main.colors.getError()+"Invalid plugin registration, you made a mistake somewhere...");
					return;
				}
			}});
    	}
    }
}
