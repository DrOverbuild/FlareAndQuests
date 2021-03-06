package com.drizzard.faq.listeners;

import com.drizzard.faq.FlareAndQuests;
import com.drizzard.faq.nms.anvil.AnvilSender;
import com.drizzard.faq.util.Group;
import com.drizzard.faq.util.ItemStacks;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by jasper on 8/10/16.
 * <p>
 * Handles all inventory events.
 */
public class InventoryListener implements Listener {
	public static final String G = ChatColor.GRAY + "";
	public static final String Y = ChatColor.YELLOW + "";

	FlareAndQuests plugin;

	public InventoryListener(FlareAndQuests plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onClose(InventoryCloseEvent ev) {
		String invTitle = ev.getInventory().getTitle();

		String activity = "";
		String configKey = "";
		if (invTitle.contains("Set Voucher Items For ")) {
			activity = "Quests";
			configKey = "Rewards";
		} else if (invTitle.contains("Set Flare Items For")) {
			activity = "Flares";
			configKey = "Contents";
		} else if (invTitle.contains("Select Spawners For")) {
			activity = "MysteryMobs";
		}

		if (activity.equals("Quests") || activity.equals("Flares")) {
			plugin.getConf().load();
			List<ItemStack> items = new ArrayList<ItemStack>();
			for (ItemStack i : ev.getInventory().getContents()) {
				if (i != null && i.getType() != Material.AIR) {
					if(ItemStacks.itemHasGlowEffect(i)){
						items.add(ItemStacks.addGlowEffect(i));
					}else {
						items.add(i);
					}
				}
			}
			String name = invTitle.substring(invTitle.indexOf("For ") + 4);
			plugin.getConf().config.set(activity + "." + name + "." + configKey, items);

			plugin.getConf().save();

			if (activity.equals("Quests")) {
				ev.getPlayer().sendMessage(G + "Successfully set the reward items for " + Y + name);
				ev.getPlayer().sendMessage(G + "Next step: add reward commands using " + Y + "/rq addvcommand " + name + " <message>");
			} else {
				ev.getPlayer().sendMessage(G + "Successfully set the inventory of " + Y + name);
				ev.getPlayer().sendMessage(G + "You're all done setting up the flare " + Y + name + G + "!");
			}
		} else if (activity.equals("MysteryMobs")) {
			// Just tell user we saved their data without actually saving their
			// data.
			//
			// JK, the data is saved when a change is made. See
			// spawnersSelected(InventoryClickEvent).
			String name = invTitle.substring(invTitle.indexOf("For ") + 4);
			ev.getPlayer().sendMessage(G + "Successfully set the spawners for " + Y + name);
		} else if (ev.getInventory() instanceof AnvilInventory && plugin.getFlareAnvils().containsKey(ev.getInventory())) {
			AnvilInventory inv = (AnvilInventory) ev.getInventory();
			inv.setContents(new ItemStack[2]);
			plugin.getFlareAnvils().remove(ev.getInventory());
		} else if (ev.getInventory() instanceof AnvilInventory && plugin.getMmAnvils().containsKey(ev.getInventory())) {
			AnvilInventory inv = (AnvilInventory) ev.getInventory();
			inv.setContents(new ItemStack[2]);
			plugin.getMmAnvils().remove(ev.getInventory());
		} else if (ev.getInventory().getHolder() instanceof Chest) {
			boolean inventoryIsEmpty = true;
			for (ItemStack i : ev.getInventory().getContents()) {
				if (i != null && !i.getType().equals(Material.AIR)) {
					inventoryIsEmpty = false;
					break;
				}
			}

			if (inventoryIsEmpty) {
				Chest chest = (Chest) ev.getInventory().getHolder();
				BukkitTask task = plugin.getPartTimers().remove(chest.getBlock());
				if (task != null) {
					task.cancel();
					chest.getBlock().setType(Material.AIR);
				}
			}
		}
	}

	@EventHandler
	public void onInvClick(InventoryClickEvent ev) {
		if (ev.getInventory().getTitle().contains("Set Flare Items For ")) {
			flareItemsModified(ev);
		} else if (ev.getInventory().getTitle().contains("Select Spawners For ")) {
			spawnersSelected(ev);
		} else if (ev.getInventory() instanceof AnvilInventory) {
			// We run both of these methods because they make the check if the
			// anvil is for configuration locationChecksOut the methods.
			itemChanceSet(ev);
			spawnerChanceSet(ev);
		}
	}

	/**
	 * Called by onInvClick(InventoryClickEvent) when the inventory name
	 * contains "Set Flare Items For", meaning the clicked inventory is for the
	 * flare configuration stage.
	 */
	private void flareItemsModified(final InventoryClickEvent ev) {
		ItemStack cursor = ev.getCursor();
		ItemStack clicked = ev.getCurrentItem();
		boolean clBAD = clicked == null || clicked.getType() == Material.AIR;
		boolean cuBAD = cursor == null || cursor.getType() == Material.AIR;

		int slot = ev.getRawSlot();

		final ItemStack target;
		if (ev.getClick() == ClickType.SHIFT_LEFT && slot >= 27 && !clBAD) {
			target = clicked;
		} else if (slot < 27 && !cuBAD) {
			target = cursor;
		} else {
			target = null;
		}

		if (target != null) {
			final ItemStack targetClone = target.clone();
			String t = ev.getInventory().getTitle();
			final String name = t.substring(t.indexOf("For ") + 4);

			new BukkitRunnable() {
				public void run() {
					ItemMeta im = targetClone.getItemMeta();
					List<String> lore = im.getLore();
					double chance = 100.0;
					if (lore != null) {
						String str = ChatColor.stripColor(lore.get(lore.size() - 1));
						try {
							chance = Double.parseDouble(str);
							lore.remove(lore.size() - 1);
							im.setLore(lore);
							targetClone.setItemMeta(im);
						} catch (Exception e) {
							chance = 100;
						}
					}

					ItemStack tag = new ItemStack(Material.NAME_TAG);
					im = tag.getItemMeta();
					im.setDisplayName(chance + "");
					tag.setItemMeta(im);

					AnvilSender.send(ev.getWhoClicked(), "Set Chance", tag);

					plugin.getFlareAnvils().put(ev.getWhoClicked().getOpenInventory().getTopInventory(), new Group<ItemStack, String>(targetClone, name));
				}
			}.runTaskLater(plugin, 1);
		}
	}

	/**
	 * Called by onInvClick(InventoryClickEvent) when the inventory name
	 * contains "Select Spawners For", meaning the clicked inventory is for the
	 * spawner selecton when configuring mystery mobs.
	 */
	private void spawnersSelected(final InventoryClickEvent ev) {
		ev.setCancelled(true);

		if(!ev.getClick().equals(ClickType.LEFT) && !ev.getClick().equals(ClickType.RIGHT)){
			return;
		}

		if (ev.getClickedInventory() != null && ev.getClickedInventory().getHolder() == null) {
			plugin.getMysteryMobSpawners().load();
			plugin.getConf().load();

			final String title = ev.getClickedInventory().getTitle();
			final String name = title.substring(title.indexOf("For ") + 4);

			if (ev.getCurrentItem().getType().equals(Material.STAINED_GLASS_PANE)) {
				String entityType = ev.getCurrentItem().getItemMeta().getLore().get(0);
				String displayName = plugin.getMysteryMobSpawners().config.getString("spawners." + entityType.toLowerCase() + ".display_name");
				ItemStack replaceWith = ItemStacks.generateStack(Material.MOB_SPAWNER, displayName, 1, (short) 0, Arrays.asList(entityType));
				ev.getClickedInventory().setItem(ev.getSlot(), replaceWith);
				((Player) ev.getWhoClicked()).updateInventory();

				plugin.getConf().config.set("MysteryMobs." + name + ".spawners." + entityType.toLowerCase(), null);
				plugin.getConf().save();
			} else if (ev.getCurrentItem().getType().equals(Material.MOB_SPAWNER)) {

				new BukkitRunnable(){

					@Override
					public void run() {
						String entityType = ev.getCurrentItem().getItemMeta().getLore().get(0);
						ItemStack item = ItemStacks.generateStack(Material.MOB_SPAWNER, "100.0", 1, (short) 0, Arrays.asList(entityType));
						AnvilSender.send(ev.getWhoClicked(), "Set Chance For Spawner", item);

						plugin.getMmAnvils().put(ev.getWhoClicked().getOpenInventory().getTopInventory(), name);
					}
				}.runTaskLater(plugin, 1);
			}
		}
	}

	/**
	 * Called by onInvClick(InventoryClickEvent) when the inventory is an anvil
	 * inventory. This method checks if the inventory is for setting a spawner
	 * chance.
	 */
	private void spawnerChanceSet(InventoryClickEvent ev){
		ItemStack clicked = ev.getCurrentItem();

		if (clicked == null || ev.getRawSlot() > 3) {
			return;
		}

		if (clicked.getType().equals(Material.AIR)) {
			return;
		}

		String mysteryMob = plugin.getMmAnvils().remove(ev.getInventory());
		if (mysteryMob == null) {
			return;
		}

		ev.setCancelled(true);

		String newChance;
		if(clicked.getItemMeta().hasDisplayName()){
			newChance = clicked.getItemMeta().getDisplayName();
		}else{
			newChance = "100";
		}

		// Ensure the new chance is an actual number
		try{
			Double.parseDouble(newChance);
		}catch (NumberFormatException e){
			return;
		}

		String entityType = clicked.getItemMeta().getLore().get(0);

		plugin.getConf().config.set("MysteryMobs." + mysteryMob + ".spawners." + entityType.toLowerCase(), newChance);
		plugin.getConf().save();

		ev.getInventory().setContents(new ItemStack[2]);

		ev.getWhoClicked().closeInventory();
		plugin.openMysteryMobInventory((Player) ev.getWhoClicked(), mysteryMob);
	}

	/**
	 * Called by onInvClick(InventoryClickEvent) when the inventory is an anvil
	 * inventory. This method checks if the inventory is for setting a flare
	 * item chance.
	 */
	private void itemChanceSet(InventoryClickEvent ev) {
		ItemStack clicked = ev.getCurrentItem();

		if (clicked == null || ev.getRawSlot() > 3) {
			return;
		}

		if (clicked.getType().equals(Material.AIR)) {
			return;
		}

		Group<ItemStack, String> g = plugin.getFlareAnvils().remove(ev.getInventory());
		if (g == null) {
			return;
		}

		ev.setCancelled(true);

		ItemStack target = g.a;
		String name = g.b;

		ItemMeta im = target.getItemMeta();
		List<String> lore = im.getLore();
		if (lore == null) {
			lore = new ArrayList<String>();
		}

		if (clicked.hasItemMeta() && clicked.getItemMeta().hasDisplayName()) {
			lore.add(clicked.getItemMeta().getDisplayName());
		} else {
			lore.add("100");
		}

		im.setLore(lore);
		target.setItemMeta(im);

		plugin.getConf().load();

		if(ItemStacks.itemHasGlowEffect(target)){
			target = ItemStacks.addGlowEffect(target);
		}

		List<ItemStack> items = (List<ItemStack>) plugin.getConf().config.getList("Flares." + name + ".Contents", new ArrayList<ItemStack>());
		items.add(target);
		plugin.getConf().config.set("Flares." + name + ".Contents", items);
		plugin.getConf().save();

		ev.getInventory().setContents(new ItemStack[2]);

		plugin.openFlareInventory((Player) ev.getWhoClicked(), name);
	}
}
