package com.patrickzhong.faq.commands;

import com.patrickzhong.faq.FlareAndQuests;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jasper on 6/28/16.
 */
public class WITEMCommand extends BasePluginCommand {
	public WITEMCommand(FlareAndQuests plugin) {
		super(plugin);
	}

	@Override
	public boolean executeCommand(CommandSender sender, String[] args) {
		if (args[0].equalsIgnoreCase("list")) {
			list(sender, "Witems");
			return true;
		}else if (args.length < 2) {
		} else if (args[0].equalsIgnoreCase("delete")) {
			if (!getConf().config.contains("Witems." + args[1]))
				sender.sendMessage(DR + "There is no witem named " + R + args[1]);
			else {
				getConf().config.set("Witems." + args[1], null);
				getConf().save();
				sender.sendMessage(G + "Successfully deleted " + Y + args[1]);
			}
			return true;
		} else if (args[0].equalsIgnoreCase("listcommands")) {
			if (!getConf().config.contains("Witems." + args[1]))
				sender.sendMessage(DR + "Could not find " + R + args[1]);
			else {
				String msg = "";
				getConf().load();

				List<String> cmds = getConf().config.getStringList("Witems." + args[1] + ".Commands");
				if (cmds == null)
					cmds = new ArrayList<String>();

				for (String command : cmds)
					msg += (msg.equals("") ? "" : G + ", ") + Y + ChatColor.translateAlternateColorCodes('&', command);
				sender.sendMessage(ChatColor.GRAY + "Commands: " + msg);
			}
			return true;
		}else if (args.length < 3) {
		} else if (args[0].equalsIgnoreCase("addcommand")) {
			List<String> cmds = getConf().config.getStringList("Witems." + args[1] + ".Commands");
			if (cmds == null)
				cmds = new ArrayList<String>();
			String newCMD = "";
			for (int i = 2; i < args.length; i++)
				newCMD += (i > 2 ? " " : "") + args[i];
			cmds.add(newCMD);
			getConf().config.set("Witems." + args[1] + ".Commands", cmds);
			getConf().save();
			sender.sendMessage(G + "Successfully added " + Y + "/" + ChatColor.translateAlternateColorCodes('&', newCMD) + G + " to the witem " + Y + args[1]);
			sender.sendMessage(G + "You're all done setting up the witem " + Y + args[1] + G + "!");
			return true;
		} else if (args[0].equalsIgnoreCase("delcommand")) {
			List<String> cmds = getConf().config.getStringList("Witems." + args[1] + ".Commands");
			if (cmds == null)
				cmds = new ArrayList<String>();
			String newCMD = "";
			for (int i = 2; i < args.length; i++)
				newCMD += (i > 2 ? " " : "") + args[i];
			if (cmds.remove(newCMD)) {
				getConf().config.set("Witems." + args[1] + ".Commands", cmds);
				getConf().save();
				sender.sendMessage(G + "Successfully removed " + Y + "/" + ChatColor.translateAlternateColorCodes('&', newCMD) + G + " from the witem " + Y + args[1]);
			} else {
				sender.sendMessage(DR + "Could not find the command " + R + "/" + ChatColor.translateAlternateColorCodes('&', newCMD));
			}
			return true;
		} else if (args[0].equalsIgnoreCase("give")) {
			Player target = Bukkit.getPlayer(args[2]);
			if (target == null) {
				sender.sendMessage(DR + "Could not find " + R + args[2]);
			} else {
				target.getInventory().addItem(getConf().config.getItemStack("Witems." + args[1] + ".Activate"));
				target.updateInventory();
				sender.sendMessage(G + "Gave the witem " + Y + args[1] + G + " to " + Y + args[2]);
			}
			return true;
		}

		Player p;
		if (sender instanceof Player) {
			p = (Player) sender;
		} else {
			sender.sendMessage("You must be a player!");
			return true;
		}

		// Commands that must be executed by player

		if (args.length < 2) {
			return false;
		} else if (args[0].equalsIgnoreCase("create")) {
			if (playerHasNamedItem(p)) {
				if (getConf().config.contains("Witems." + args[1]))
					p.sendMessage(DR + "There is already a witem named " + R + args[1]);
				else {
					getConf().config.set("Witems." + args[1] + ".Activate", p.getItemInHand());
					getConf().save();
					//player.getInventory().remove(player.getItemInHand());
					p.getInventory().setItem(p.getInventory().getHeldItemSlot(), null);
					p.updateInventory();
					p.sendMessage(G + "Successfully created a witem named " + Y + args[1]);
					p.sendMessage(G + "Next step: select a region using " + Y + "/rq wand" + G + " and " + Y + "/witem setregion " + args[1]);
				}
			}
		} else if (!getConf().config.contains("Witems." + args[1])) {
			sender.sendMessage(DR + "There is no witem of the name " + R + args[1]);
		} else if (args[0].equalsIgnoreCase("setregion")) {
			Location f = plugin.left.get(p);
			Location s = plugin.right.get(p);
			if (f == null || s == null)
				p.sendMessage(DR + "Please select a region first");
			else {
				getConf().config.set("Witems." + args[1] + ".First", f);
				getConf().config.set("Witems." + args[1] + ".Second", s);
				getConf().save();
				p.sendMessage(G + "Successfully set the region for " + Y + args[1]);
				p.sendMessage(G + "Next step: add a command using " + Y + "/witem addcommand " + args[1] + " <command>");
			}
		}

		return true;
	}

	@Override
	public void sendHelp(CommandSender sender) {
		sender.sendMessage(BEG + "/witem create <name> " + SEP + "Creates a witem.");
		sender.sendMessage(BEG + "/witem delete <name> " + SEP + "Deletes a witem.");
		sender.sendMessage(BEG + "/witem setregion <name> " + SEP + "Sets the region to your selection.");
		sender.sendMessage(BEG + "/witem addcommand <name> <command> " + SEP + "Adds a command.");
		sender.sendMessage(BEG + "/witem listcommands <name> " + SEP + "Lists all commands.");
		sender.sendMessage(BEG + "/witem delcommand <name> <command> " + SEP + "Deletes a command.");
		sender.sendMessage(BEG + "/witem give <name> <player> " + SEP + "Gives a player a witem.");
		sender.sendMessage(BEG + "/witem list " + SEP + "Lists all witems.");
	}
}