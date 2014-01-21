package net.stormdev.bukkitmods.ultimatepluginupdater.main;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class UPUEventListener implements Listener {
	public UPUEventListener(){
	}
    @EventHandler (priority = EventPriority.LOWEST)
    void manageRegistrations(AsyncPlayerChatEvent event){
    	if(!main.plugin.pluginRegistrations.containsKey(event.getPlayer().getName())){
    		return;
    	}
    	String inputText = ChatColor.stripColor(event.getMessage());
    	main.plugin.pluginRegistrations.get(event.getPlayer().getName()).recievedInput(inputText);
    	event.setMessage("");
    	event.setCancelled(true);
    	return;
    }
}
