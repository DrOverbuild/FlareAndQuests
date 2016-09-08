package com.drizzard.faq.util;

import com.drizzard.faq.nms.NMSHelper;
import com.sun.org.apache.xpath.internal.operations.Bool;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * Created by jasper on 6/28/16.
 */
public class ItemStacks {
	public static ItemStack generateStack(Material material, String displayName, int amount, short data, List<String> lore) {
		ItemStack stack = new ItemStack(material, amount, data);
		ItemMeta stackMeta = stack.getItemMeta();
		stackMeta.setDisplayName(displayName);
		stackMeta.setLore(lore);
		stack.setItemMeta(stackMeta);

		return stack;
	}

	public static ItemStack generateStack(Material material, String displayName) {
		return ItemStacks.generateStack(material, displayName, 1, (short) 0, null);
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

	public static ItemStack addGlowEffect(ItemStack itemStack) {
		final String version = NMSHelper.getNMSVersion();

		if (version.equals("v1_7_R4")) {

			// Ew... reflection...
			try {
				Class<?> nmsItemStackClass = Class.forName("net.minecraft.server." + version + ".ItemStack");
				Class<?> craftItemStackClass = Class.forName("org.bukkit.craftbukkit." + version + ".inventory.CraftItemStack");
				Class<?> nbtTagCompoundClass = Class.forName("net.minecraft.server." + version + ".NBTTagCompound");
				Class<?> nbtTagListClass = Class.forName("net.minecraft.server." + version + ".NBTTagList");
				Class<?> nbtBaseClass = Class.forName("net.minecraft.server." + version + ".NBTBase");

				Object nmsStack = craftItemStackClass.getMethod("asNMSCopy", ItemStack.class).invoke(null, itemStack);
				Object tag = null;
				Object ench = nbtTagListClass.getConstructor().newInstance();

				Boolean hasTag = (Boolean) nmsItemStackClass.getMethod("hasTag").invoke(nmsStack);
				if (!hasTag) {
					tag = nbtTagCompoundClass.getConstructor().newInstance();
					nmsItemStackClass.getMethod("setTag", nbtTagCompoundClass).invoke(nmsStack, tag);
				}

				if (tag == null) {
					tag = nmsItemStackClass.getMethod("getTag").invoke(nmsStack);
				}

				nbtTagCompoundClass.getMethod("set", String.class, nbtBaseClass).invoke(tag, "ench", ench);
				nmsItemStackClass.getMethod("setTag", nbtTagCompoundClass).invoke(nmsStack, tag);

				return (ItemStack) craftItemStackClass.getMethod("asCraftMirror", nmsItemStackClass).invoke(null, nmsStack);
			} catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException
					| InvocationTargetException | ClassCastException | InstantiationException e) {
				e.printStackTrace();
				return null;
			}

//          This throws a ClassDefNotFound exception when loading the ItemStacks class.
//
//			net.minecraft.server.v1_7_R4.ItemStack nmsStack = org.bukkit.craftbukkit.v1_7_R4.inventory.CraftItemStack.asNMSCopy(itemStack);
//			net.minecraft.server.v1_7_R4.NBTTagCompound tag = null;
//			if (!nmsStack.hasTag()) {
//				tag = new net.minecraft.server.v1_7_R4.NBTTagCompound();
//				nmsStack.setTag(tag);
//			}
//			if (tag == null) tag = nmsStack.getTag();
//			net.minecraft.server.v1_7_R4.NBTTagList ench = new net.minecraft.server.v1_7_R4.NBTTagList();
//			tag.set("ench", ench);
//			nmsStack.setTag(tag);
//			return org.bukkit.craftbukkit.v1_7_R4.inventory.CraftItemStack.asCraftMirror(nmsStack);
		} else {
			ItemMeta meta = itemStack.getItemMeta();
			meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
			itemStack.setItemMeta(meta);
			itemStack.addUnsafeEnchantment(Enchantment.getById(0), 1);
			return itemStack;
		}
	}

	public static boolean itemHasGlowEffect(ItemStack itemStack) {
		final String version = NMSHelper.getNMSVersion();

		// Have I told you how much I hate reflection?
		try {
			Class<?> nmsItemStackClass = Class.forName("net.minecraft.server." + version + ".ItemStack");
			Class<?> craftItemStackClass = Class.forName("org.bukkit.craftbukkit." + version + ".inventory.CraftItemStack");
			Class<?> nbtTagCompoundClass = Class.forName("net.minecraft.server." + version + ".NBTTagCompound");
			Class<?> nbtTagListClass = Class.forName("net.minecraft.server." + version + ".NBTTagList");
			Class<?> nbtBaseClass = Class.forName("net.minecraft.server." + version + ".NBTBase");

			Object nmsStack = craftItemStackClass.getMethod("asNMSCopy", ItemStack.class).invoke(null, itemStack);
			Object tag;
			Object ench;

			Boolean hasTag = (Boolean) nmsItemStackClass.getMethod("hasTag").invoke(nmsStack);
			if (!hasTag) {
				return false;
			}

			tag = nmsItemStackClass.getMethod("getTag").invoke(nmsStack);

			Boolean hasEnch = (Boolean) nbtTagCompoundClass.getMethod("hasKey", String.class).invoke(tag, "ench");
			if (!hasEnch) {
				return false;
			}

			ench = nbtTagCompoundClass.getMethod("getList", String.class, int.class).invoke(tag, "ench", 0);
			Boolean enchIsEmpty = (Boolean) nbtTagListClass.getMethod("isEmpty").invoke(ench);
			return  enchIsEmpty;

//			net.minecraft.server.v1_8_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(itemStack);
//			NBTTagCompound tag = nmsItem.getTag();
//			NBTTagList ench = tag.getList("ench", 0);
//			return ench.isEmpty();
		} catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException
				| InvocationTargetException | ClassCastException e) {
			e.printStackTrace();
			return false;
		}
	}
}

