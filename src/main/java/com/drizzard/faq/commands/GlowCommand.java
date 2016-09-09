package com.drizzard.faq.commands;

import com.drizzard.faq.util.ItemStacks;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;

/**
 * Created by jasper on 8/28/16.
 */
public class GlowCommand implements CommandExecutor {
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) {
		if (!sender.hasPermission("faq." + "glow") && !sender.hasPermission("faq.*")) {
			sender.sendMessage(BasePluginCommand.DR + "You need the permission " +
					BasePluginCommand.R + "faq.glow" + BasePluginCommand.DR +
					" or " + BasePluginCommand.R + " faq.* " + BasePluginCommand.DR + ".");
			return true;
		}

		if(!(sender instanceof Player)){
			sender.sendMessage("You must be a player.");
			return true;
		}

		Player player = (Player) sender;

		if(player.getItemInHand() == null || player.getItemInHand().getType().equals(Material.AIR)){
			player.sendMessage(BasePluginCommand.R + "You must have an item in your hand");
			return true;
		}

		if(ItemStacks.itemHasGlowEffect(player.getItemInHand())){
			player.sendMessage(BasePluginCommand.G + "Item is already glowing (Item has empty \"ench\" NBT tag)");
		}else if (player.getItemInHand().getItemMeta().hasItemFlag(ItemFlag.HIDE_ENCHANTS)){
			player.sendMessage(BasePluginCommand.G + "Item is already glowing (Item has HIDE_ENCHANTS item flag)");
		}else {
			player.setItemInHand(ItemStacks.addGlowEffect(player.getItemInHand()));
			player.sendMessage(BasePluginCommand.G + "Applied glow effect to the item in your hand");
		}
		return true;
	}
}
