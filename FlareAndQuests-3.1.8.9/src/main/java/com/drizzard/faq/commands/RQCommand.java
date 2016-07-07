package com.drizzard.faq.commands;

import com.drizzard.faq.FlareAndQuests;
import com.drizzard.faq.util.ItemStacks;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jasper on 6/28/16.
 */
public class RQCommand extends BasePluginCommand {
	public RQCommand(FlareAndQuests plugin) {
		super(plugin);
	}

	@Override
	public boolean executeCommand(CommandSender sender, String[] args) {

		if (args[0].equalsIgnoreCase("list")) {
			list(sender, "Quests");
			return true;
		} else if (args.length < 2) {
		} else if (args[0].equalsIgnoreCase("delete")) {
			if (!getConf().config.contains("Quests." + args[1]))
				sender.sendMessage(DR + "There is no quest named " + R + args[1]);
			else {
				getConf().config.set("Quests." + args[1], null);
				getConf().save();
				sender.sendMessage(G + "Successfully deleted " + Y + args[1]);
			}
			return true;
		} else if (args[0].equalsIgnoreCase("listvcommands")) {
			if (!getConf().config.contains("Quests." + args[1]))
				sender.sendMessage(DR + "Could not find " + R + args[1]);
			else {
				String msg = "";
				getConf().load();

				List<String> cmds = getConf().config.getStringList("Quests." + args[1] + ".Commands");
				if (cmds == null)
					cmds = new ArrayList<String>();

				for (String command : cmds)
					msg += (msg.equals("") ? "" : G + ", ") + Y + ChatColor.translateAlternateColorCodes('&', command);
				sender.sendMessage(ChatColor.GRAY + "Commands: " + msg);
			}
			return true;
		} else if (args.length < 3) {
		} else if (args[0].equalsIgnoreCase("give")) {
			if (!getConf().config.contains("Quests." + args[1])) {
				sender.sendMessage(DR + "There is no quest of the name " + R + args[1]);
			}

			Player target = Bukkit.getPlayer(args[2]);
			if (target == null)
				sender.sendMessage(DR + "Could not find " + R + args[2]);
			else {
				target.getInventory().addItem(getConf().config.getItemStack("Quests." + args[1] + ".Activate"));
				target.updateInventory();
				sender.sendMessage(G + "Gave the quest " + Y + args[1] + G + " to " + Y + args[2]);
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

		if (args[0].equalsIgnoreCase("wand")) {
			ItemStack i = ItemStacks.generateStack(Material.IRON_AXE, ChatColor.AQUA + "" + ChatColor.BOLD + "Rank Quest Region Selector");

			p.getInventory().addItem(i);
			p.updateInventory();
		} else if (args.length < 2) { // Handling too few arguments.
			return false;
		} else if (args[0].equalsIgnoreCase("create")) {
			if (playerHasNamedItem(p)) {
				if (getPlugin().getConf().config.contains("Quests." + args[1]))
					p.sendMessage(DR + "There is already a quest named " + R + args[1]);
				else {
					getConf().config.set("Quests." + args[1] + ".Activate", p.getItemInHand());
					getConf().save();
					//player.getInventory().remove(player.getItemInHand());
					p.getInventory().setItem(p.getInventory().getHeldItemSlot(), null);
					p.updateInventory();
					p.sendMessage(G + "Successfully created a rank quest named " + Y + args[1]);
					p.sendMessage(G + "Next step: select a region using " + Y + "/rq wand" + G + " and " + Y + "/rq setregion " + args[1]);
				}
			}
		} else if (!getConf().config.contains("Quests." + args[1])) {
			sender.sendMessage(DR + "There is no quest of the name " + R + args[1]);
		} else if (args[0].equalsIgnoreCase("setvoucher")) {
			if (p.getItemInHand() == null)
				p.sendMessage(DR + "You must hold an item in your hand.");
			else {
				getConf().config.set("Quests." + args[1] + ".Voucher", p.getItemInHand());
				getConf().save();
				p.getInventory().remove(p.getItemInHand());
				p.updateInventory();
				p.sendMessage(G + "Successfully set the voucher for " + Y + args[1]);
				p.sendMessage(G + "Next step: set the rewards using " + Y + "/rq setvitems " + args[1]);
			}
		} else if (args[0].equalsIgnoreCase("setvitems")) {
			List<ItemStack> items = (List<ItemStack>) getConf().config.getList("Quests." + args[1] + ".Rewards", new ArrayList<ItemStack>());
			Inventory inv = Bukkit.createInventory(p, 36, "Set Voucher Items For " + args[1]);
			for (int i = 0; i < items.size(); i++)
				inv.setItem(i, items.get(i));
			p.openInventory(inv);
		} else if (args[0].equalsIgnoreCase("setregion")) {
			Location f = getPlugin().left.get(p);
			Location s = getPlugin().right.get(p);
			if (f == null || s == null)
				p.sendMessage(DR + "Please select a region first");
			else {
				getConf().config.set("Quests." + args[1] + ".First", f);
				getConf().config.set("Quests." + args[1] + ".Second", s);
				getConf().save();
				p.sendMessage(G + "Successfully set the region for " + Y + args[1]);
				//38.47
				p.sendMessage(G + "Next step: set the duration using " + Y + "/rq settime " + args[1] + " <seconds>");
			}
		} else if (args.length < 3) {
			return false;
		} else if (args[0].equalsIgnoreCase("settime")) {
			int seconds = Integer.parseInt(args[2]);

			getConf().config.set("Quests." + args[1] + ".Duration", seconds);
			getConf().save();

			p.sendMessage(G + "Successfully set the duration of " + Y + args[1] + G + " to " + Y + seconds + G + " seconds.");
			p.sendMessage(G + "Next step: set the voucher item using " + Y + "/rq setvoucher " + args[1]);
		} else if (args[0].equalsIgnoreCase("addvcommand")) {
			List<String> cmds = getConf().config.getStringList("Quests." + args[1] + ".Commands");
			if (cmds == null)
				cmds = new ArrayList<String>();
			String newCMD = "";
			for (int i = 2; i < args.length; i++)
				newCMD += (i > 2 ? " " : "") + args[i];
			cmds.add(newCMD);
			getConf().config.set("Quests." + args[1] + ".Commands", cmds);
			getConf().save();
			p.sendMessage(G + "Successfully added " + Y + "/" + ChatColor.translateAlternateColorCodes('&', newCMD) + G + " to the quest " + Y + args[1]);
			p.sendMessage(G + "You're all done setting up the rank quest " + Y + args[1] + G + "!");
		} else if (args[0].equalsIgnoreCase("delvcommand")) {
			List<String> cmds = getConf().config.getStringList("Quests." + args[1] + ".Commands");
			if (cmds == null)
				cmds = new ArrayList<String>();
			String newCMD = "";
			for (int i = 2; i < args.length; i++)
				newCMD += (i > 2 ? " " : "") + args[i];
			if (cmds.remove(newCMD)) {
				getConf().config.set("Quests." + args[1] + ".Commands", cmds);
				getConf().save();
				p.sendMessage(G + "Successfully removed " + Y + "/" + ChatColor.translateAlternateColorCodes('&', newCMD) + G + " from the quest " + Y + args[1]);
			} else {
				p.sendMessage(DR + "Could not find the command " + R + "/" + ChatColor.translateAlternateColorCodes('&', newCMD));
			}
		}else{
			return false;
		}

		return true;
	}

	@Override
	public void sendHelp(CommandSender sender) {
		sender.sendMessage(BEG + "/rq create <name> " + SEP + "Creates a rank quest.");
		sender.sendMessage(BEG + "/rq delete <name> " + SEP + "Deletes a rank quest.");
		sender.sendMessage(BEG + "/rq wand " + SEP + "Gives you a selection wand.");
		sender.sendMessage(BEG + "/rq setregion <name> " + SEP + "Sets the region to your selection.");
		sender.sendMessage(BEG + "/rq setvoucher <name> " + SEP + "Sets the voucher.");
		sender.sendMessage(BEG + "/rq settime <name> <seconds> " + SEP + "Sets the duration.");
		sender.sendMessage(BEG + "/rq setvitems <name> " + SEP + "Sets the reward items.");
		sender.sendMessage(BEG + "/rq addvcommand <name> <command> " + SEP + "Adds a command.");
		sender.sendMessage(BEG + "/rq listvcommands <name> " + SEP + "Lists all commands.");
		sender.sendMessage(BEG + "/rq delvcommand <name> <command> " + SEP + "Deletes a command.");
		sender.sendMessage(BEG + "/rq give <name> <player> " + SEP + "Gives a player a rank quest.");
		sender.sendMessage(BEG + "/rq list " + SEP + "Lists all rank quests.");
	}
}
