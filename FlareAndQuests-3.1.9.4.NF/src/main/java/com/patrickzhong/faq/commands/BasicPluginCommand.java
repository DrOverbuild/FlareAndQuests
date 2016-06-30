package com.patrickzhong.faq.commands;

import com.patrickzhong.faq.FlareAndQuests;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

/**
 * Created by jasper on 6/28/16.
 */
public class BasicPluginCommand implements CommandExecutor {
	FlareAndQuests plugin;

	public static final String DR = ChatColor.DARK_RED+"";
	public static final String R = ChatColor.RED+"";
	public static final String G = ChatColor.GRAY+"";
	public static final String Y = ChatColor.YELLOW+"";

	public BasicPluginCommand(FlareAndQuests plugin) {
		this.plugin = plugin;
	}

	public FlareAndQuests getPlugin() {
		return plugin;
	}

	@Override
	public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
		commandSender.sendMessage("This command has not been implemented yet.");
		return true;
	}
}
