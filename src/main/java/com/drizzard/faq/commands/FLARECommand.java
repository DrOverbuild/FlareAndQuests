package com.drizzard.faq.commands;

import com.drizzard.faq.FlareAndQuests;
import com.drizzard.faq.util.ItemStacks;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

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
            if (!getConf().config.contains("Flares." + args[2])) {
                sender.sendMessage(DR + "There is no flare of the name " + R + args[2]);
                return true;
            }

            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[1]);

                if (offlinePlayer != null) {
                    sender.sendMessage(R + args[1] + DR + " is currently not online, but we will try to give him a flare when he joins.");
                    plugin.getPlayerData().config.set("players." + offlinePlayer.getUniqueId().toString() + ".flare", args[2]);
                    plugin.getPlayerData().save();
                } else {
                    sender.sendMessage(DR + "Could not find" + R + args[1]);
                }
            } else {
                int numberOfItems = 1;
                if (args.length > 3) {
                    try {
                        numberOfItems = Integer.parseInt(args[3]);
                    } catch (NumberFormatException e) {
                    }
                }

                for (int i = 0; i < numberOfItems; i++) {
                    target.getInventory().addItem(getConf().config.getItemStack("Flares." + args[2] + ".Activate"));
                }
                target.updateInventory();

                sender.sendMessage(G + "Gave " + numberOfItems + " flare"+ (numberOfItems == 1 ? "" : "s") +" with the name " + Y + args[2] + G + " to " + Y + args[1]);
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
        if (args[0].equalsIgnoreCase("wand")) {
            ItemStack i = ItemStacks.generateStack(Material.IRON_AXE, ChatColor.AQUA + "" + ChatColor.BOLD + "FAQ Region Selector");

            p.getInventory().addItem(i);
            p.updateInventory();
        } else if (args.length < 2) { // Handling too few arguments.
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
                    p.sendMessage(G + "Next step: select a region using " + Y + "/flare wand" + G + " and " + Y + "/flare setregion " + args[1]);
                }
            }
        } else if (args[0].equalsIgnoreCase("setinventory")) {
            if (!getConf().config.contains("Flares." + args[1]))
                sender.sendMessage(DR + "There is no flare of the name " + R + args[1]);
            else
            	getPlugin().openFlareInventory(p, args[1]);
        } else if (args[0].equalsIgnoreCase("setregion")) {
        	if (!getConf().config.contains("Flares." + args[1]))
                sender.sendMessage(DR + "There is no flare of the name " + R + args[1]);
        	else {
        		Location f = plugin.left.get(p);
	            Location s = plugin.right.get(p);
	            if (f == null || s == null)
	                p.sendMessage(DR + "Please select a region first");
	            else {
	                getConf().config.set("Flares." + args[1] + ".First", f);
	                getConf().config.set("Flares." + args[1] + ".Second", s);
	                getConf().save();
	                p.sendMessage(G + "Successfully set the region for " + Y + args[1]);
	
	                if (plugin.serverHasFactions()) {
	                    p.sendMessage(G + "Please be aware that Flares will use the warzone instead of region if Factions is installed.");
	                }
	
	                p.sendMessage(G + "Next step: set the items using " + Y + "/flare setinventory " + args[1]);
	            }
        	}
        } else {
            return false;
        }

        return true;
    }

    @Override
    public void sendHelp(CommandSender sender) {
        sender.sendMessage(BEG + "/flare create <name> " + SEP + "Creates a flare.");
        sender.sendMessage(BEG + "/flare delete <name> " + SEP + "Deletes a flare.");
        if (!plugin.serverHasFactions()) {
            sender.sendMessage(BEG + "/flare wand " + SEP + "Gives you a selection wand.");
            sender.sendMessage(BEG + "/flare setregion <name> " + SEP + "Sets the region to your selection.");
        }
        sender.sendMessage(BEG + "/flare setinventory <name> " + SEP + "Sets the chest inventory.");
        sender.sendMessage(BEG + "/flare give <player> <name> <amount>" + SEP + "Gives a player a flare.");
        sender.sendMessage(BEG + "/flare list " + SEP + "Lists all flares.");
    }
}
