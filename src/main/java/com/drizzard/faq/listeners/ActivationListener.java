package com.drizzard.faq.listeners;

import com.drizzard.faq.Flare;
import com.drizzard.faq.FlareAndQuests;
import com.drizzard.faq.RankQuest;
import com.drizzard.faq.util.Group;
import com.drizzard.faq.util.ItemStacks;
import com.drizzard.faq.util.SoundUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Created by jasper on 8/10/16.
 * <p>
 * Handles the interact event and activates an activity if needed.
 * <p>
 * The activations of all the activities are separated into their own methods.
 */
public class ActivationListener implements Listener {

	public static final String G = ChatColor.GRAY + "";
	public static final String Y = ChatColor.YELLOW + "";

	FlareAndQuests plugin;

	public ActivationListener(FlareAndQuests plugin) {
		this.plugin = plugin;
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onClick(PlayerInteractEvent ev) {
		String disp = ItemStacks.getDisplayName(ev.getItem());

		/*if (ev.getClickedBlock() != null) {
			BukkitTask t = partTimers.remove(ev.getClickedBlock());
			if (t != null)
				t.cancel();
		}*/

		if (disp.equals("FAQ Region Selector")) {
			ev.setCancelled(true);
			if (ev.getAction() == Action.LEFT_CLICK_BLOCK) {
				Location loc = ev.getClickedBlock().getLocation();
				plugin.left.put(ev.getPlayer(), loc);
				ev.getPlayer().sendMessage(G + "Set first position to " + Y + loc.getBlockX() + G + ", " + Y + loc.getBlockY() + G + ", " + Y + loc.getBlockZ() + G + " in " + Y + loc.getWorld().getName());
			} else if (ev.getAction() == Action.RIGHT_CLICK_BLOCK) {
				Location loc = ev.getClickedBlock().getLocation();
				plugin.right.put(ev.getPlayer(), loc);
				ev.getPlayer().sendMessage(G + "Set second position to " + Y + loc.getBlockX() + G + ", " + Y + loc.getBlockY() + G + ", " + Y + loc.getBlockZ() + G + " in " + Y + loc.getWorld().getName());
			}
		} else {
			if (ev.getAction().equals(Action.LEFT_CLICK_AIR) || ev.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
				return;
			}

			if (ev.getItem() != null) {
				plugin.getConf().load();
				if (plugin.getConf().config.contains("Flares")) {
					for (String key : plugin.getConf().config.getConfigurationSection("Flares").getKeys(false)) {
						if (ItemStacks.stackIsSimilar(ev.getItem(), plugin.getConf().config.getItemStack("Flares." + key + ".Activate"), false)) {

							// Matched to flare
							ev.setCancelled(true);

							activateFlare(ev.getPlayer(), key, ev.getItem());

							return;
						}
					}
				}

				if (plugin.getConf().config.contains("Witems")) {
					for (String key : plugin.getConf().config.getConfigurationSection("Witems").getKeys(false)) {
						if (ItemStacks.stackIsSimilar(ev.getItem(), plugin.getConf().config.getItemStack("Witems." + key + ".Activate"), false)) {
							//Matched to Witem
							ev.setCancelled(true);

							activateWitem(ev.getPlayer(), key, ev.getItem());

							return;
						}
					}
				}

				if (plugin.getConf().config.contains("Quests")) {
					for (String key : plugin.getConf().config.getConfigurationSection("Quests").getKeys(false)) {
						ItemStack citem = plugin.getConf().config.getItemStack("Quests." + key + ".Activate");

						if (ItemStacks.stackIsSimilar(ev.getItem(), citem, false)) {
							// Matched to Rank Quest
							ev.setCancelled(true);

							activateRankQuest(ev.getPlayer(), key, ev.getItem());

							return;
						} else if (ItemStacks.stackIsSimilar(ev.getItem(), plugin.getConf().config.getItemStack("Quests." + key + ".Voucher"), false)) {
							// Matched to Voucher
							ev.setCancelled(true);

							activateRankQuestVoucher(ev.getPlayer(), key, ev.getItem());

							return;
						}
					}
				}

				if (plugin.getConf().config.contains("MysteryMobs")) {
					for (String key : plugin.getConf().config.getConfigurationSection("MysteryMobs").getKeys(false)) {
						if (ItemStacks.stackIsSimilar(ev.getItem(), plugin.getConf().config.getItemStack("MysteryMobs." + key + ".Activate"), false)) {
							// Matched to Mystery Mob
							plugin.getLogger().info("Player is using mystery mob");

							ev.setCancelled(true);

							activateMysteryMob(ev.getPlayer(), key, ev.getItem());
						}
					}
				}
			}
		}
	}

	private void activateFlare(Player player, String key, ItemStack itemStack) {
		if (plugin.playerIsActive(player)) {
			player.sendMessage(plugin.getTrans().format("other-function-active.flare", null, player));
			return;
		}

		int minPlayers = plugin.getConf().config.getInt("minimum-players.flare");
		if (plugin.getServer().getOnlinePlayers().size() < minPlayers) {
			player.sendMessage(plugin.getTrans().format("not-enough-players", new Group<>("min-online", minPlayers + "")));
			return;
		}
		World world = ((Location)plugin.getConf().config.get("Flares." + key + ".First")).getWorld();
		
		List<String> usableWorlds = plugin.getConf().config.getStringList("always-usable-worlds");
		
		if (!usableWorlds.contains(world.getName()) && !plugin.inside(player.getLocation(), (Location) plugin.getConf().config.get("Flares." + key + ".First"), (Location) plugin.getConf().config.get("Flares." + key + ".Second"))) {
			if (plugin.serverHasFactions()) {
				String[] message = plugin.getTrans().format("not-in-warzone", player.getLocation(), player);
				player.sendMessage(message);
			} else {
				player.sendMessage(plugin.getTrans().format("not-in-region", null, player));
			}
			return;
		} else {
			Flare.activateFlare(itemStack, player, plugin, key);
		}
	}

	private void activateWitem(Player player, String key, ItemStack itemStack) {
		
		int minPlayers = plugin.getConf().config.getInt("minimum-players.witem");
		
		if (plugin.getServer().getOnlinePlayers().size() < minPlayers) {
			player.sendMessage(plugin.getTrans().format("not-enough-players", new Group<>("min-online", minPlayers + "")));
			return;
		}

		if (plugin.playerIsActive(player)) {
			player.sendMessage(plugin.getTrans().format("other-function-active.witem", null, player));
			return;
		}

		World world = ((Location)plugin.getConf().config.get("Witems." + key + ".First")).getWorld();
		
		List<String> usableWorlds = plugin.getConf().config.getStringList("always-usable-worlds");
		
		if (!usableWorlds.contains(world.getName()) && !plugin.inside(player.getLocation(), (Location) plugin.getConf().config.get("Witems." + key + ".First"), (Location) plugin.getConf().config.get("Witems." + key + ".Second"))) {
			if (plugin.serverHasFactions()) {
				String[] message = plugin.getTrans().format("not-in-warzone", player.getLocation(), player);
				player.sendMessage(message);
			} else {
				player.sendMessage(plugin.getTrans().format("not-in-region", null, player));
			}
			return;
			//player.sendMessage(DR+"You must be plugin.inside the proper region!");
		}

		if (itemStack.getAmount() == 1) {
			player.getInventory().setItem(player.getInventory().getHeldItemSlot(), null);
		} else {
			itemStack.setAmount(itemStack.getAmount() - 1);
		}

		player.updateInventory();

		SoundUtil.playWitemUseSound(plugin, player);

		if (plugin.getConf().config.contains("Witems." + key + ".Commands")) {
			for (String str : plugin.getConf().config.getStringList("Witems." + key + ".Commands")) {
				plugin.getServer().dispatchCommand(Bukkit.getConsoleSender(), ChatColor.translateAlternateColorCodes('&', str).replace("{player}", player.getName()));
			}
		}
	}

	private void activateRankQuest(Player player, String key, ItemStack itemStack) {
		int minPlayers = plugin.getConf().config.getInt("minimum-players.rq");
		if (plugin.getServer().getOnlinePlayers().size() < minPlayers) {
			player.sendMessage(plugin.getTrans().format("not-enough-players", new Group<>("min-online", minPlayers + "")));
			return;
		}

		if (plugin.playerIsActive(player)) {
			player.sendMessage(plugin.getTrans().format("other-function-active.rq", null, player));
			return;
		}

		World world = ((Location)plugin.getConf().config.get("Quests." + key + ".First")).getWorld();
		
		List<String> usableWorlds = plugin.getConf().config.getStringList("always-usable-worlds");
		
		if (itemStack.getAmount() > 1) {
			
			player.sendMessage(plugin.getTrans().format("rq.activation-error.stacked-rq", null, player));
		
		} else if (plugin.getQIP().containsKey(player)) {
		
			player.sendMessage(plugin.getTrans().format("rq.activation-error.currently-doing-quest", null, player));
		
		} else if (!usableWorlds.contains(world.getName()) && !plugin.inside(player.getLocation(), (Location) plugin.getConf().config.get("Quests." + key + ".First"), (Location) plugin.getConf().config.get("Quests." + key + ".Second"))) {
			
			if (plugin.serverHasFactions()) {
				String[] message = plugin.getTrans().format("not-in-warzone", null, player);
				player.sendMessage(message);
			} else {
				player.sendMessage(plugin.getTrans().format("not-in-region", null, player));
			}

			//player.sendMessage(DR+"You must be plugin.inside the proper region!");
		} else if (plugin.getDeathsLeft().containsKey(player)) {
			player.sendMessage(plugin.getTrans().format("rq.activation-error.in-keep-inv", null, player));
		} else {
			plugin.getQIP().put(player, new RankQuest(player.getInventory().getHeldItemSlot(), player, plugin.getConf().config.getInt("Quests." + key + ".Duration"), plugin, key));
		}
	}

	private void activateRankQuestVoucher(Player player, String key, ItemStack itemStack) {
		PlayerInventory inv = player.getInventory();

		if (itemStack.getAmount() == 1) {
			inv.setItem(inv.getHeldItemSlot(), null);
		} else {
			itemStack.setAmount(itemStack.getAmount() - 1);
		}

		for (ItemStack i : (List<ItemStack>) plugin.getConf().config.getList("Quests." + key + ".Rewards", new ArrayList<ItemStack>())) {
			inv.addItem(i);
		}

		player.updateInventory();
		if (plugin.getConf().config.contains("Quests." + key + ".Commands")) {
			for (String str : plugin.getConf().config.getStringList("Quests." + key + ".Commands")) {
				plugin.getServer().dispatchCommand(Bukkit.getConsoleSender(), ChatColor.translateAlternateColorCodes('&', str).replace("{player}", player.getName()));
			}
		}
	}
	
	private void activateMysteryMob(Player player, String key, ItemStack itemStack){
		if (plugin.playerIsActive(player)) {
			player.sendMessage(plugin.getTrans().format("other-function-active.mm", null, player));
			return;
		}

		if (itemStack.getAmount() == 1) {
			player.getInventory().setItem(player.getInventory().getHeldItemSlot(), null);
		} else {
			itemStack.setAmount(itemStack.getAmount() - 1);
		}

		player.updateInventory();

		SoundUtil.playMMUseSound(plugin, player);

		// TODO: Activate MM
		String chosenSpawner = null;
		List<String> allSpawners = new ArrayList<>();
		allSpawners.addAll(plugin.getConf().config.getConfigurationSection("MysteryMobs." + key + ".spawners").getKeys(false));
		Random random = new Random();

		while(chosenSpawner == null){
			int index = random.nextInt(allSpawners.size());
			double chance = random.nextDouble() * 100d;
			double selectedSpawnerChance;

			try{
				selectedSpawnerChance = Double.valueOf(plugin.getConf().config.getString("MysteryMobs." + key + ".spawners." + allSpawners.get(index)));
			}catch (NumberFormatException e){
				selectedSpawnerChance = 50;
			}

			if(chance < selectedSpawnerChance){
				chosenSpawner = allSpawners.get(index);
			}
		}


		player.getInventory().addItem(ItemStacks.generateStack(Material.MOB_SPAWNER,
				plugin.getMysteryMobSpawners().config.getString("spawners." + chosenSpawner + ".display_name"),
				1, (short) 0, Arrays.asList(chosenSpawner.toUpperCase())));

		player.sendMessage(plugin.getTrans().format("mm-item-use", player.getLocation(), player, new Group<>("selectedspawner",
				plugin.getMysteryMobSpawners().config.getString("spawners." + chosenSpawner + ".display_name"))));
	}


}
