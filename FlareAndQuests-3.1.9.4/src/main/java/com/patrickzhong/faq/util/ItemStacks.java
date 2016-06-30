package com.patrickzhong.faq.util;

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
}

