package com.patrickzhong.main;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.bukkit.configuration.file.YamlConfiguration;
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
	
}
