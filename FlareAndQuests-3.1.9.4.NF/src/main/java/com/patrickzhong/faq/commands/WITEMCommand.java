package com.patrickzhong.faq.commands;

import com.patrickzhong.faq.FlareAndQuests;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

/**
 * Created by jasper on 6/28/16.
 */
public class WITEMCommand extends BasicPluginCommand {


	public WITEMCommand(FlareAndQuests plugin) {
		super(plugin);
	}

	@Override
	public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
		return false;
	}
}
