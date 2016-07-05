package com.drizzard.faq.commands;

import com.drizzard.faq.Config;
import com.drizzard.faq.FlareAndQuests;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Created by jasper on 6/28/16.
 */
public abstract class BasePluginCommand implements CommandExecutor {
	FlareAndQuests plugin;

	public static final String DR = ChatColor.DARK_RED + "";
	public static final String R = ChatColor.RED + "";
	public static final String G = ChatColor.GRAY + "";
	public static final String Y = ChatColor.YELLOW + "";

	public static final String SEP = ChatColor.WHITE + "- " + ChatColor.GRAY;
	public static final String BEG = ChatColor.GRAY + "- " + ChatColor.AQUA;

	public BasePluginCommand(FlareAndQuests plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
		if (!sender.hasPermission("faq." + command.getName().toLowerCase())) {
			sender.sendMessage(R + "You need the permission 'faq." + command.getName().toLowerCase() + "'.");
			return true;
		}

		if (args.length < 1) {
			sendHelp(sender);
			return true;
		}

		getConf().load();

		if (!executeCommand(sender, args)) {
			sender.sendMessage(DR + "Invalid syntax or missing arguments. Type /" + command.getName() + " to view help.");
		}

		return true;
	}

	public FlareAndQuests getPlugin() {
		return plugin;
	}

	public Config getConf(){
		return getPlugin().getConf();
	}

	public Config getTrans(){
		return getPlugin().getTrans();
	}

	public abstract boolean executeCommand(CommandSender sender, String[] args);

	public abstract void sendHelp(CommandSender sender);

	public void list(CommandSender sender, String listWhat) {
		String msg = "";
		getConf().load();
//		if(a) sec = "Quests";
//		else if(b) sec = "Flares";
//		else sec = "Witems";
		if (getConf().config.contains(listWhat)){
			for (String key : getConf().config.getConfigurationSection(listWhat).getKeys(false)) {
				msg += (msg.equals("") ? "" : G + ", ") + Y + key;
			}
		}
		sender.sendMessage(G + listWhat + ": " + (msg.equals("") ? Y + "Nothing to list for " + listWhat + "." : msg));
	}

	public boolean playerHasNamedItem(Player p) {
		if (p.getItemInHand() == null || p.getItemInHand().getType() == Material.AIR) {
			p.sendMessage(DR + "You must hold an item in your hand.");
			return false;
		}

		if (!p.getItemInHand().hasItemMeta()) {
			p.sendMessage(DR + "You cannot use an unnamed/unlored item.");
			return false;
		}

		return true;
	}
}
