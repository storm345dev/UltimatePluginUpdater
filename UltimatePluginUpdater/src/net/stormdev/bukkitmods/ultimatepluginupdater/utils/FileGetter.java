package net.stormdev.bukkitmods.ultimatepluginupdater.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import net.stormdev.bukkitmods.ultimatepluginupdater.main.Updateable;
import net.stormdev.bukkitmods.ultimatepluginupdater.main.UpdateableManager;
import net.stormdev.bukkitmods.ultimatepluginupdater.main.main;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class FileGetter {
	public static long search(String[] possible_slugs, String pluginName, String response){
		Boolean found = false;
        long id = -1;
        JSONArray array = (JSONArray) JSONValue.parse(response);
        if (array.size() > 0) {
            // Get the newest file's details
        	for(Object o:array){
        		if(!found){
        		JSONObject latest = (JSONObject) o;

                // Get the values
                long tid = (Long) latest.get("id");
                String name = (String) latest.get("name");
                String slug = (String) latest.get("slug");
                String stage = (String) latest.get("stage");	
                if(!(stage == "deleted") || (stage == "planning")){
                	//It is a valid bukkit item
                	Boolean is = false;
                	if(name.equalsIgnoreCase(pluginName)){
                		is = true;
                	}
                	for(String s:possible_slugs){
                		if(slug.equalsIgnoreCase(s)){
                			if(!is){
                				is = true;
                			}
                		}
                	}
                	if(is){
                		id = tid;
                		found = true;
                	}
                }
        		}
        	}
        }
        return id;
	}
	public static URL getLatestPluginFileURL(Updateable updateable) throws IOException {
		URL url = null;
		String pluginName = updateable.getSlug();
		String a = pluginName.toLowerCase();
		String b = a.replaceAll(" ", "");
		String c = a.replaceAll(" ", "-");
		String d = a.replaceAll(" ", "_");
		String[] possible_slugs = new String[]{a,b,c,d};
		String query = "https://api.curseforge.com/servermods/projects?search="+b; //Search without spaces
		String query2 = "https://api.curseforge.com/servermods/projects?search="+c; //Search with dashes (If spaces nto found)
		URL search = new URL(query);
		URL search2 = new URL(query2);
		URLConnection conn = search.openConnection();
		String agent = "UltimatePluginUpdater/v"+main.plugin.getDescription().getVersion() + "(By StormDev)";
		conn.addRequestProperty("User-Agent", agent);
		final BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String response = reader.readLine();
        reader.close();
        try {
			conn.getInputStream().close(); //Close if connection kept alive
		} catch (Exception e) {
		}
        long id = search(possible_slugs, pluginName, response);
        if(id < 0){
        	URLConnection conn2 = search2.openConnection();
    		conn2.addRequestProperty("User-Agent", agent);
    		final BufferedReader reader2 = new BufferedReader(new InputStreamReader(conn2.getInputStream()));
            String response2 = reader2.readLine();
            reader2.close();
            id = search(possible_slugs, pluginName, response2);
            if(id < 0){
            	main.logger.info("Unable to find "+pluginName+", unregistering..., please reregister manually!");
    			UpdateableManager.remove(updateable);
    			UpdateableManager.save();
    			return null;
            }
        }
        long projectId = id;
        String fileReq = "https://api.curseforge.com/servermods/files?projectIds="+projectId;
        URLConnection conn3 = new URL(fileReq).openConnection();
        conn3.addRequestProperty("User-Agent", agent);
        final BufferedReader reader3 = new BufferedReader(new InputStreamReader(conn3.getInputStream()));
        String response3 = reader3.readLine();
        // Parse the array of files from the query's response
        JSONArray array = (JSONArray) JSONValue.parse(response3);
        String downloadUrl = "";
        if (array.size() > 0) {
            // Get the newest file's details
            JSONObject latest = (JSONObject) array.get(array.size() - 1);
            downloadUrl = (String) latest.get("downloadUrl");
        } else {
        	main.logger.info("Unable to find "+pluginName+", unregistering..., please reregister manually!");
			UpdateableManager.remove(updateable);
			UpdateableManager.save();
			return null;
        }
        url = new URL(downloadUrl);
		return url;
	}
}
