package com.drizzard.faq.commands;

import com.drizzard.faq.FlareAndQuests;
import com.drizzard.faq.util.ItemStacks;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.Arrays;
import java.util.Set;

/**
 * Created by jasper on 8/4/16.
 */
public class MMCommand extends BasePluginCommand {
	public MMCommand(FlareAndQuests plugin) {
		super(plugin);
	}

	@Override
	public boolean executeCommand(CommandSender sender, String[] args) {
		if (args[0].equalsIgnoreCase("list")) {
			list(sender, "MysteryMobs");
			return true;
		} else if (args.length < 2) {
		} else if (args[0].equalsIgnoreCase("delete")) {
			if (!getConf().config.contains("MysteryMobs." + args[1]))
				sender.sendMessage(DR + "There is no Mystery Mob named " + R + args[1]);
			else {
				getConf().config.set("MysteryMobs." + args[1], null);
				getConf().save();
				sender.sendMessage(G + "Successfully deleted " + Y + args[1]);
			}
			return true;
		} else if (args.length < 4) {
		} else if (args[0].equalsIgnoreCase("give")) {
			if (!getConf().config.contains("MysteryMobs." + args[2])) {
				sender.sendMessage(DR + "There is no mystery mob of the name " + R + args[2]);
				return true;
			}

			Player target = Bukkit.getPlayer(args[1]);
			if (target == null) {
				sender.sendMessage(DR + "Could not find" + R + args[1]);
			} else {
				int numberOfItems = 1;
				if (args.length > 3) {
					try {
						numberOfItems = Integer.parseInt(args[3]);
					} catch (NumberFormatException e) {
					}
				}

				for (int i = 0; i < numberOfItems; i++) {
					target.getInventory().addItem(getConf().config.getItemStack("MysteryMobs." + args[2] + ".Activate"));
				}
				target.updateInventory();

				sender.sendMessage(G + "Gave " + numberOfItems + " Mystery Mob" + (numberOfItems == 1 ? "" : "s") + " with the name " + Y + args[2] + G + " to " + Y + args[1]);
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

		if (args.length < 2) {
		} else if (args[0].equalsIgnoreCase("create")) {

			if (playerHasNamedItem(p)) {
				if (getConf().config.contains("MysteryMobs." + args[1])) {
					p.sendMessage(DR + "There is already a mystery mob named " + R + args[1]);
				} else {
					getConf().config.set("MysteryMobs." + args[1] + ".Activate", p.getItemInHand());
					getConf().save();
					p.getInventory().setItem(p.getInventory().getHeldItemSlot(), null);
					p.updateInventory();

					plugin.openMysteryMobInventory(p, args[1]);
				}
			}

			return true;
		} else if (args[0].equalsIgnoreCase("edit")) {

			if (getConf().config.contains("MysteryMobs." + args[1])) {
				plugin.openMysteryMobInventory(p, args[1]);
			} else {
				p.sendMessage(DR + "There are no mystery mobs named " + args[1]);

			}

			return true;
		}

		return false;
	}


	@Override
	public void sendHelp(CommandSender sender) {
		sender.sendMessage(BEG + "/mm create <name> " + SEP + "Creates a Mystery Mob.");
		sender.sendMessage(BEG + "/mm delete <name> " + SEP + "Deletes a Mystery Mob.");
		sender.sendMessage(BEG + "/mm give <player> <name> <amount>" + SEP + "Gives a player a rank quest.");
		sender.sendMessage(BEG + "/mm edit <name>" + SEP + "Edits a Mystery Mob.");
		sender.sendMessage(BEG + "/mm list " + SEP + "Lists all Mystery Mobs.");
	}
}
