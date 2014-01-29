package net.stormdev.bukkitmods.ultimatepluginupdater.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import net.stormdev.bukkitmods.ultimatepluginupdater.main.Updateable;
import net.stormdev.bukkitmods.ultimatepluginupdater.main.UpdateableManager;
import net.stormdev.bukkitmods.ultimatepluginupdater.main.main;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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
		int connectErrors = 0;
		URL url = null;
		
		while(connectErrors < 5){
			try {
				url = getLatestPluginFileURLOnce(updateable);
			} catch (IOException e) {
				if(connectErrors >= 5){
					throw e;
				}
				connectErrors++;
				continue;
			}
			break;
		}
		
		return url;
	}
	private static URL getLatestPluginFileURLOnce(Updateable updateable) throws IOException {
		int connectErrors = 0;
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
		
		String response = "";
		String agent = "UltimatePluginUpdater/v"+main.plugin.getDescription().getVersion() + "(By StormDev)";
		
		while(connectErrors < 5){
			HttpsURLConnection conn;
			try {
				conn = (HttpsURLConnection) search.openConnection();
			} catch (IOException e2) {
				connectErrors++;
				continue;
			}
			conn.addRequestProperty("User-Agent", agent);
			conn.setConnectTimeout(1500);
	        conn.setReadTimeout(1500);
	        try {
				conn.connect();
			} catch (Exception e1) {
				connectErrors++;
				continue;
			}
			final BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
	        response = reader.readLine();
	        reader.close();
	        try {
				conn.getInputStream().close(); //Close if connection kept alive
			} catch (Exception e) {
				connectErrors++;
				continue;
			}
	        break;
		}
		
		if(connectErrors >= 5 || response.length() < 1){
			main.logger.info(ChatColor.RED+"Unable to connect to the CurseForge API! Is curseforge.com offline or just busy? ("+pluginName+")");
			return null;
		}
		connectErrors = 0;
		
        long id = search(possible_slugs, pluginName, response);
        if(id < 0){
        	String response2 = "";
        	HttpsURLConnection conn2 = null;
        	
        	while(connectErrors < 5){
        		try {
					conn2 = (HttpsURLConnection) search2.openConnection();
				} catch (IOException e1) {
					connectErrors++;
					continue;
				}
        		conn2.addRequestProperty("User-Agent", agent);
        		conn2.setConnectTimeout(1500);
    	        conn2.setReadTimeout(1500);
    	        try {
					conn2.connect();
					final BufferedReader reader2 = new BufferedReader(new InputStreamReader(conn2.getInputStream()));
					response2 = reader2.readLine();
					reader2.close();
				} catch (Exception e) {
					connectErrors++;
					continue;
				}
                break;
        	}
        	if(connectErrors >= 5 || response2.length() < 1){
        		main.logger.info(ChatColor.RED+"Unable to connect to the CurseForge API! Is curseforge.com offline or just busy? ("+pluginName+")");
    			return null;
        	}
        	
            id = search(possible_slugs, pluginName, response2);
            if(id < 0){
            	main.logger.info("Unable to find "+pluginName+", unregistering..., please reregister manually!");
    			UpdateableManager.remove(updateable);
    			UpdateableManager.save();
    			return null;
            }
            try {
            	if(conn2 != null)
            		conn2.getInputStream().close();
			} catch (Exception e) {
			}
        }
        connectErrors = 0;
        
        long projectId = id;
        String fileReq = "https://api.curseforge.com/servermods/files?projectIds="+projectId;
        
        String response3 = "";
        HttpsURLConnection conn3 = null;
        
        while(connectErrors < 5){
        	try {
				conn3 = (HttpsURLConnection) new URL(fileReq).openConnection();
			} catch (IOException e1) {
				connectErrors++;
    			continue;
			}
            conn3.addRequestProperty("User-Agent", agent);
            conn3.setConnectTimeout(2000);
            conn3.setReadTimeout(2000);
            BufferedReader reader3;
    		try {
    			reader3 = new BufferedReader(new InputStreamReader(conn3.getInputStream()));
    		} catch (IOException e) {
    			connectErrors++;
    			continue;
    		}
            response3 = reader3.readLine();
            break;
        }
        
        if(conn3 == null || connectErrors >= 5 || response3.length() < 1){
        	main.logger.info(ChatColor.RED+"Unable to connect to the CurseForge API! Is curseforge.com offline or just busy? ("+pluginName+")");
			return null;
        }
        
        connectErrors = 0;
        
        // Parse the array of files from the query's response
        JSONArray array = (JSONArray) JSONValue.parse(response3);
        String downloadUrl = "";
        if (array.size() > 0) {
            // Get the newest file's details
            JSONObject latest = (JSONObject) array.get(array.size() - 1);
            downloadUrl = (String) latest.get("downloadUrl");
            if(main.strictVersioning){
            	String gameVersion = (String) latest.get("gameVersion");
                int start = gameVersion.indexOf(" ");
                if(start > 4){
                	start = 0;
                }
                int end = gameVersion.indexOf("-");
                if(start < 1 && end < 1){
                	start = 0; end = gameVersion.length();
                }
                String currentVersion = Bukkit.getBukkitVersion();
                int currentStart = 0;
                int currentEnd = currentVersion.indexOf("-");
                if(start >= 0 && end > 0 && currentStart >= 0 && currentEnd > 0){
                	String pluginVersion = gameVersion.substring(start, end).trim();
                	String gameVer = currentVersion.substring(currentStart, currentEnd).trim();
                	main.logger.info("Plugin: "+pluginVersion+" Server: "+gameVer);
                	
                	if(compareVersions(pluginVersion, gameVer) > 0){
                		main.logger.info("Plugin "+pluginName+" is newer than server - Not downloaded!");
                	}
                }
            }
        } else {
        	main.logger.info("Unable to find "+pluginName+", unregistering..., please reregister manually!");
			UpdateableManager.remove(updateable);
			UpdateableManager.save();
			return null;
        }
        url = new URL(downloadUrl);
        try {
			conn3.getInputStream().close();
		} catch (Exception e) {
		}
		return url;
	}
	
	//Returns -1 if str1 < str2, 0 is they are equal and +1 if str1 > str2
	private static int compareVersions(String str1, String str2){
		String[] vals1 = str1.split("\\.");
		String[] vals2 = str2.split("\\.");
		int i=0;
		while(i<vals1.length && i<vals2.length && vals1[i].equals(vals2[i])) {
		  i++;
		}

		if (i<vals1.length && i<vals2.length) {
		    int diff = Integer.valueOf(vals1[i]).compareTo(Integer.valueOf(vals2[i]));
		    return Integer.signum(diff);
		}

		return Integer.signum(vals1.length - vals2.length);
	}
}
