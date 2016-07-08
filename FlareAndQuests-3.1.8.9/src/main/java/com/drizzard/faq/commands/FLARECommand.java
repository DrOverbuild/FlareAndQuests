package com.drizzard.faq.commands;

import com.drizzard.faq.FlareAndQuests;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Created by jasper on 6/28/16.
 */
public class FLARECommand extends BasePluginCommand {
	public FLARECommand(FlareAndQuests plugin) {
		super(plugin);
	}

	@Override
	public boolean executeCommand(CommandSender sender, String[] args) {
		if (args[0].equalsIgnoreCase("list")) {
			list(sender, "Flares");
			return true;
		} else if (args.length < 2) {
		} else if (args[0].equalsIgnoreCase("delete")) {
			if (!getConf().config.contains("Flares." + args[1]))
				sender.sendMessage(DR + "There is no flare named " + R + args[1]);
			else {
				getConf().config.set("Flares." + args[1], null);
				getConf().save();
				sender.sendMessage(G + "Successfully deleted " + Y + args[1]);
			}
			return true;
		} else if (args.length < 3) {
		} else if (args[0].equalsIgnoreCase("give")) {
			if (!getConf().config.contains("Flares." + args[1])) {
				sender.sendMessage(DR + "There is no flare of the name " + R + args[1]);
				return true;
			}

			Player target = Bukkit.getPlayer(args[2]);
			if (target == null) {

				OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[2]);

				if (offlinePlayer != null) {
					sender.sendMessage(R + args[2] + DR + " is currently not online, but we will try to give him a flare when he joins.");
					plugin.getPlayerData().config.set("players." + offlinePlayer.getUniqueId().toString() + ".flare", args[1]);
					plugin.getPlayerData().save();
				} else {
					sender.sendMessage(DR + "Could not find" + R + args[2]);
				}
			} else {
				target.getInventory().addItem(getConf().config.getItemStack("Flares." + args[1] + ".Activate"));
				target.updateInventory();
				sender.sendMessage(G + "Gave the flare " + Y + args[1] + G + " to " + Y + args[2]);
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

		if (args.length < 2) { // Handling too few arguments.
			return false;
		} else if (args[0].equalsIgnoreCase("create")) {
			if (playerHasNamedItem(p)) {
				if (getConf().config.contains("Flares." + args[1]))
					p.sendMessage(DR + "There is already a flare named " + R + args[1]);
				else {
					getConf().config.set("Flares." + args[1] + ".Activate", p.getItemInHand());
					getConf().save();
					//player.getInventory().remove(player.getItemInHand());
					p.getInventory().setItem(p.getInventory().getHeldItemSlot(), null);
					p.updateInventory();
					p.sendMessage(G + "Successfully created a flare named " + Y + args[1]);
					p.sendMessage(G + "Next step: set the items using " + Y + "/flare setinventory " + args[1]);
				}
			}
		} else if (args[0].equalsIgnoreCase("setinventory")) {
			if (!getConf().config.contains("Flares." + args[1])) {
				sender.sendMessage(DR + "There is no flare of the name " + R + args[1]);
			}

			getPlugin().openFlareInventory(p, args[1]);
		} else {
			return false;
		}

		return true;
	}

	@Override
	public void sendHelp(CommandSender sender) {
		sender.sendMessage(BEG + "/flare create <name> " + SEP + "Creates a flare.");
		sender.sendMessage(BEG + "/flare delete <name> " + SEP + "Deletes a flare.");
		sender.sendMessage(BEG + "/flare setinventory <name> " + SEP + "Sets the chest inventory.");
		sender.sendMessage(BEG + "/flare give <name> <player> " + SEP + "Gives a player a flare.");
		sender.sendMessage(BEG + "/flare list " + SEP + "Lists all flares.");
	}
}
