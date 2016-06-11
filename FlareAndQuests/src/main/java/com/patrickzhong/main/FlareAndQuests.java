package com.patrickzhong.main;

import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class FlareAndQuests extends JavaPlugin implements Listener {
	
	Config conf;
	
	String DR = ChatColor.DARK_RED+"";
	String R = ChatColor.RED+"";
	String G = ChatColor.GRAY+"";
	String Y = ChatColor.YELLOW+"";
	
	public void onEnable(){
		this.getServer().getPluginManager().registerEvents(this, this);
		HashMap<String, Object> defs = new HashMap<String, Object>();
		defs.put("Broadcast", "&e{player} &7has started a rank quest!");
		conf = new Config(this, defs);
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
		boolean a = cmd.getName().equalsIgnoreCase("rq");
		boolean b = cmd.getName().equalsIgnoreCase("flare");
		
		if(a || b){
			if(!(sender instanceof Player))
				sender.sendMessage(DR+"You must be a player.");
			else if(args.length < 2) // TODO help page
				sender.sendMessage(DR+"Missing arguments.");
			else if(!sender.hasPermission(cmd.getName().toLowerCase()+"."+args[0].toLowerCase()) && !sender.hasPermission("rq.*"))
				sender.sendMessage(DR+"You need the permission "+R+"rq."+args[0].toLowerCase());
			else {
				conf.load();
				if(args[0].equalsIgnoreCase("create")){
					if(a){
						// Successful CREATE (RQ)
					}
					else if(b){
						// Successful CREATE (FLARE)
					}
				}
				else {
					if(!conf.config.contains("Quests."+args[1].toLowerCase()))
						sender.sendMessage(DR+"There is no rank quest of the name "+R+args[1].toLowerCase());
					else if(a){
						if(args[0].equalsIgnoreCase("addvoucher")){
							// Successful ADDVOUCHER (RQ)
						}
						else if(args[0].equalsIgnoreCase("addvitems")){
							// Successful ADDVITEMS (RQ)
						}
						else if(args[0].equalsIgnoreCase("addvcommand")){
							if(args.length < 3)
								sender.sendMessage(DR+"Missing arguments.");
							else {
								// Successful ADDVCOMMAND (RQ)
							}
						}
					}
					else if(b && args[0].equalsIgnoreCase("addinventory")){
						// Successful ADDINVENTORY (FLARE)
					}
				}
			}
		}
		
		return true;
	}
	
	private String disp(ItemStack i){
		try {
			return ChatColor.stripColor(i.getItemMeta().getDisplayName());
		}
		catch (Exception e){
			return "";
		}
	}
	
	@EventHandler
	public void onClick(PlayerInteractEvent ev){
		if(disp(ev.getItem()).contains("Rank Quest")){
			new RankQuest(ev.getItem(), ev.getPlayer(), 30, this);
		}
	}

}
