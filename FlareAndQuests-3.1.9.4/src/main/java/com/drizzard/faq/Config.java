package com.drizzard.faq;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class Config {
	
	File configFile;
	public YamlConfiguration config;

	public Config(Plugin plugin, HashMap<String, Object> defaults, String name){
		
		if(!plugin.getDataFolder().exists())
			plugin.getDataFolder().mkdir();
		configFile = new File(plugin.getDataFolder(), name+".yml");
		if(!configFile.exists()){
			try {
				configFile.createNewFile();
			} catch (IOException e) {
			}
		}
		load();
		if(defaults != null){
			for(String path : defaults.keySet())
				config.addDefault(path, defaults.get(path));
			config.options().copyDefaults(true);
			save();
		}
	}
	
	public void save(){
		try {
			config.save(configFile);
		}
		catch (Exception e){
		}
	}
	
	public void load(){
		config = YamlConfiguration.loadConfiguration(configFile);
	}

	public String[] format(String messageKey, Location loc, Player player){
		return format(messageKey, loc, player, null);
	}

	public String[] format(String messageKey, Player player){
		return format(messageKey, null, player, null);
	}

	public String[] format(String messageKey, Location loc, Player player, String left){
		String message = config.getString(messageKey, "none");
		if(message.equals("none")){
			return new String[]{};
		}

		String[] lines = message.split("\\|");

		for(int i = 0; i < lines.length; i++){
			lines[i] = formatLine(lines[i], loc, player, left);
			if(i > 0){
				lines[i] = ChatColor.getLastColors(lines[i-1]) + lines[i];
			}
		}

		return lines;
	}

	public static String formatLine(String message, Location loc, Player player, String left){
		message = ChatColor.translateAlternateColorCodes('&', message);
		if(loc != null) {
			message = message.replace("{x}", loc.getBlockX() + "");
			message = message.replace("{y}", loc.getBlockY() + "");
			message = message.replace("{z}", loc.getBlockZ() + "");
		}

		if(player != null){
			message = message.replace("{player}", player.getName());
		}

		if(left != null){
			message = message.replace("{left", left);
		}

		return message;
	}
}
