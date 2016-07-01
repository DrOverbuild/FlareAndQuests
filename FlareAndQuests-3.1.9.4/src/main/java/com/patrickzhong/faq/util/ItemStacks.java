package com.patrickzhong.faq.util;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

/**
 * Created by jasper on 6/28/16.
 */
public class ItemStacks {
	public static ItemStack generateStack(Material material, String displayName, int amount, short data, List<String> lore){
		ItemStack stack = new ItemStack(material,amount, data);
		ItemMeta stackMeta = stack.getItemMeta();
		stackMeta.setDisplayName(displayName);
		stackMeta.setLore(lore);
		stack.setItemMeta(stackMeta);

		return stack;
	}

	public static ItemStack generateStack(Material material, String displayName){
		return ItemStacks.generateStack(material, displayName, 1, (short)0, null);
	}

	public static String getDisplayName(ItemStack i) {
		try {
			String disp = ChatColor.stripColor(i.getItemMeta().getDisplayName());
			return disp == null ? "" : disp;
		} catch (Exception e) {
			return "";
		}
	}

	public static boolean stackIsSimilar(ItemStack larger, ItemStack smaller, boolean disp) {
		try {
			ItemMeta l = larger.getItemMeta();
			ItemMeta s = smaller.getItemMeta();
			return larger.getType() == smaller.getType() &&
					larger.getData().equals(smaller.getData()) &&
					((!l.hasDisplayName() && !s.hasDisplayName()) || (((disp && l.getDisplayName().contains(s.getDisplayName())) || (!disp && l.getDisplayName().equals(s.getDisplayName()))))) &&
					((!l.hasLore() && !s.hasLore()) || (l.getLore().equals(s.getLore())));
		} catch (Exception e) {
			return false;
		}
	}
}

