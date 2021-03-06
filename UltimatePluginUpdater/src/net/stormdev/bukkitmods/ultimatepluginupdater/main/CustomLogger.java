package net.stormdev.bukkitmods.ultimatepluginupdater.main;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;

public class CustomLogger {
	public Boolean coloured = true;
	ConsoleCommandSender console = null;
	Logger logger = null;
	public CustomLogger(ConsoleCommandSender console, Logger logger){
		try {
			coloured = main.config.getBoolean("general.logger.colour");
		} catch (Exception e) {
			coloured = false;
		}
		this.console = console;
		this.logger = logger;
	}
	public void setColoured(Boolean coloured){
		this.coloured = coloured;
	}
	public String getMsg(String raw){
		String colour = main.colorise(raw);
		if(!coloured){
			return ChatColor.stripColor(colour);
		}
		return colour;
	}
	public void log(String message, Level level){
		print(message);
		log(level);
	}
	public void defaultLog(String msg, Level level){
		logger.log(level, msg);
	}
	public void log(Level level){
		logger.log(level, "");
	}
	public void error(Exception e){
		print(main.colors.getError()+e.getLocalizedMessage());
		e.printStackTrace();
	}
	public void error(String msg, Exception e){
		print(main.colors.getError()+msg);
		e.printStackTrace();
	}
	public void info(String message){
		print(main.colors.getInfo()+message);
	}
	public void print(String message){
		if(coloured){
		console.sendMessage(main.colors.getTitle()+"[UltimatePluginUpdater] "+ChatColor.RESET+main.colors.getInfo()+getMsg(message));
		}
		else{
			logger.info(getMsg(message));
		}
		return;
	}
}
