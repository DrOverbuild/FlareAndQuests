package com.drizzard.faq;

import com.drizzard.faq.util.ActionBar;
import com.drizzard.faq.util.SoundUtil;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Flare {

//	boolean manual = false;
//	double vel = 0.0;

	FlareAndQuests plugin;
	Random rand = new Random();
	String name;
	Player activator;

	public Flare(Player activator, ItemStack is, FlareAndQuests plugin, String name) {
		this.plugin = plugin;
		this.name = name;
		this.activator = activator;

		double r = plugin.conf.config.getDouble("Flare Drop Radius");
		int minFree = plugin.getConf().config.getInt("min-free-above-blocks", 20);

		// The location at which the flare will start falling
		Location flareSpawn = null;

		int count = 0;
		int max = plugin.conf.config.getInt("Flare Max Tries");

		while (!plugin.inside(flareSpawn, (Location) plugin.getConf().config.get("Flares." + name + ".First"),
				(Location) plugin.getConf().config.get("Flares." + name + ".Second"))) {
			double randX = (rand.nextDouble() * r * 2) - r;
			double randZ = (rand.nextDouble() * r * 2) - r;

			flareSpawn = activator.getLocation().add(randX, Math.abs((double) minFree), randZ);

			count++;
			if (count >= max) {
				activator.sendMessage(plugin.getTrans().format("Flare Drop Failed Message", null, activator));
				activator.getInventory().addItem(is);
				return;
			}
		}

		FallingBlock block = activator.getWorld().spawnFallingBlock(flareSpawn, Material.SAND, (byte) 0);
		block.setCustomName(activator.getName() + "-" + name);
		block.setCustomNameVisible(false);
		block.setHurtEntities(false);
		plugin.getFallingFlares().put(block.getUniqueId(), this);
	}

	public Random getRand() {
		return rand;
	}

	public String getName() {
		return name;
	}

	public Player getActivator() {
		return activator;
	}

	public void fillFlareChest(final Block chestBlock){
		chestBlock.setType(Material.CHEST);

		Chest c = (Chest) chestBlock.getState();
		plugin.getConf().load();
		List<ItemStack> list = (List<ItemStack>) plugin.getConf().config.getList("Flares." + this.getName() + ".Contents", new ArrayList<ItemStack>());
		ItemStack[] conts = new ItemStack[27];
		int count = 0;
		Random rand = new Random();

		int min = plugin.getConf().config.getInt("Minimum Flare Contents");
		int max = plugin.getConf().config.getInt("Maximum Flare Contents");

		if (list.size() < min)
			min = list.size();
		if (list.size() < max)
			max = list.size();

		int target = (int) (Math.random() * (max - min)) + min;

		for (int i = 0; i < list.size(); i++) {
			ItemStack is = list.get(i);
			double percent = 100;
			if (is != null && is.hasItemMeta() && is.getItemMeta().hasLore()) {
				ItemMeta im = is.getItemMeta();
				List<String> lore = im.getLore();
				percent = Double.parseDouble(ChatColor.stripColor(lore.remove(lore.size() - 1)));
				im.setLore(lore);
				is.setItemMeta(im);
			}
			if (rand.nextDouble() * 100 < percent) {
				conts[count] = is;
				count++;
			}

			if (count >= target)
				break;
		}

		while (count < target) {
			conts[count] = list.get((int) (Math.random() * list.size()));
			count++;
		}

		c.getBlockInventory().setContents(conts);
		c.update(true);

		SoundUtil.playChestArrivalSound(plugin, this.getActivator());

		getActivator().sendMessage(plugin.getTrans().format("Flare Arrived Message", chestBlock.getLocation(), getActivator()));

		plugin.getPartTimers().put(chestBlock, new BukkitRunnable() {
			public void run() {
				plugin.spawnParticle(plugin.getConf().config.getInt("Flare Chest Particle ID"),
						chestBlock.getLocation().clone().add(0.5, 0.5, 0.5), 1, 1, 1, 5,
						chestBlock.getWorld().getEntitiesByClass(Player.class));
			}
		}.runTaskTimer(plugin, 0, 1));


	}

	public void doLightningAnimation(final Block chestBlock){
		new BukkitRunnable(){
			int i = 2;

			@Override
			public void run() {
				chestBlock.getWorld().strikeLightningEffect(chestBlock.getLocation());

				if(i <= 1){
					fillFlareChest(chestBlock);
					this.cancel();
				}

				i--;
			}
		}.runTaskTimer(plugin, 0, 10);
	}

//	public Flare(ItemStack is, final Player player, final FlareAndQuests plugin, final String name) {
//
//		double r = plugin.conf.config.getDouble("Flare Drop Radius");
//		Location loc = player.getLocation().add(0, 5, 0);
//		Location rloc = randomLoc(loc, r);
//		int count = 0;
//		int max = plugin.conf.config.getInt("Flare Max Tries");
//
//
//		while (!plugin.isWarzone(rloc)) {
//			rloc = randomLoc(loc, r);
//			count++;
//			if (count >= max) {
//				player.sendMessage(plugin.getTrans().format("Flare Drop Failed Message", null, player));
//				player.getInventory().addItem(is);
//				return;
//			}
//		}
//
//		player.sendMessage(plugin.getTrans().format("Flare Arrived Message", rloc, player));
//
//		final ArmorStand armor = (ArmorStand) loc.getWorld().spawnEntity(rloc.clone().subtract(0, 1.5, 0), EntityType.ARMOR_STAND);
//		armor.setGravity(true);
//		armor.setVisible(false);
//		armor.setHelmet(new ItemStack(Material.CHEST));
//		armor.setHeadPose(new EulerAngle(0, Math.PI, 0));
//
//		new BukkitRunnable() {
//			public void run() {
//
//				if (manual) {
//					Location head = armor.getLocation().add(0, 1.5, 0);
//					if (head.getBlock().getType().isSolid()) {
//						armor.remove();
//						final Block b = head.add(0, 1, 0).getBlock();
//						b.setType(Material.CHEST);
//						Chest c = (Chest) b.getState();
//						plugin.conf.load();
//						List<ItemStack> list = (List<ItemStack>) plugin.conf.config.getList("Flares." + name + ".Contents", new ArrayList<ItemStack>());
//						ItemStack[] conts = new ItemStack[27];
//						int count = 0;
//
//						int min = plugin.conf.config.getInt("Minimum Flare Contents");
//						int max = plugin.conf.config.getInt("Maximum Flare Contents");
//
//						if (list.size() < min)
//							min = list.size();
//						if (list.size() < max)
//							max = list.size();
//
//						int target = (int) (Math.random() * (max - min)) + min;
//
//						for (int i = 0; i < list.size(); i++) {
//							ItemStack is = list.get(i);
//							double percent = 100;
//							if (is != null && is.hasItemMeta() && is.getItemMeta().hasLore()) {
//								ItemMeta im = is.getItemMeta();
//								List<String> lore = im.getLore();
//								percent = Double.parseDouble(ChatColor.stripColor(lore.remove(lore.size() - 1)));
//								im.setLore(lore);
//								is.setItemMeta(im);
//							}
//							if (rand.nextDouble() * 100 < percent) {
//								conts[count] = is;
//								count++;
//							}
//
//							if (count >= target)
//								break;
//						}
//
//						while (count < target) {
//							conts[count] = list.get((int) (Math.random() * list.size()));
//							count++;
//						}
//
//						c.getBlockInventory().setContents(conts);
//						c.update(true);
//
//						plugin.partTimers.put(b, new BukkitRunnable() {
//							public void run() {
//								plugin.spawnParticle(plugin.conf.config.getInt("Flare Chest Particle ID"), b.getLocation().clone().add(0.5, 0.5, 0.5), 1, 1, 1, 5, b.getWorld().getEntitiesByClass(Player.class));
//							}
//						}.runTaskTimer(plugin, 0, 1));
//
//						SoundUtil.playChestArrivalSound(plugin, player);
//
//						this.cancel();
//					} else
//						armor.teleport(armor.getLocation().add(0, 5 * vel, 0));
//				} else if (armor.getLocation().subtract(0, 0.1, 0).getBlock().getType().isSolid()) {
//					double v = armor.getVelocity().getY();
//					manual = true;
//					armor.setGravity(false);
//					vel = v;
//				} else if (armor.getVelocity().getY() == 0) {
//					manual = true;
//					armor.setGravity(false);
//					vel = -0.0784000015258789;
//				}
//			}
//		}.runTaskTimer(plugin, 10, 1);
//	}

	public static void activateFlare(final ItemStack is, final Player player, final FlareAndQuests plugin, final String name) {
		plugin.getConf().load();

		if (plugin.playerFlares.containsKey(player)) {
			player.sendMessage(plugin.getTrans().format("Flare In Use", null, player));
			return;
		}

		double r = plugin.conf.config.getDouble("Flare Drop Radius");

		// TODO: Check if there is enough space to spawn flare

		String[] message = plugin.getTrans().format("Flare Broadcast", player.getLocation(), player);
		for (Entity ent : player.getNearbyEntities(r, r, r)) {
			if (ent instanceof Player && ent.getLocation().distance(player.getLocation()) <= r) {
				ent.sendMessage(message);
			}
		}
		player.sendMessage(message);

		if (is.getAmount() == 1) {
			player.getInventory().setItem(player.getInventory().getHeldItemSlot(), null);
		} else {
			is.setAmount(is.getAmount() - 1);
		}

		player.updateInventory();

		SoundUtil.playFlareUseSound(plugin, player);

		final int delay = plugin.getConf().config.getInt("Flare Arrival Delay", 0);

		if (delay <= 0) {
//			new Flare(is, player, plugin, name);
			new Flare(player, is, plugin, name);
		} else {
			plugin.playerFlares.put(player, name);

			final String message2 = ChatColor.translateAlternateColorCodes('&', plugin.getTrans().config.getString("Flare Arriving In Action Bar Message"));

			new BukkitRunnable() {
				int secondsLeft = delay;

				public void run() {
					ActionBar.sendActionBar(player, message2.replace("{time}", secondsLeft + ""));
					if (secondsLeft <= 0) {
						plugin.playerFlares.remove(player);
						new Flare(player, is, plugin, name);
//						new Flare(is, player, plugin, name);
						this.cancel();
						return;
					}
					secondsLeft--;
				}
			}.runTaskTimer(plugin, 0, 20);
		}
	}

	private Location randomLoc(Location loc, double r) {
		int sx; //   Variables
		int lx; //   For
		int xz; //   Warzone
		int lz; //   Boundaries

		double dX = (Math.random() * 2 - 1) * r;
		double dZ = (Math.random() * 2 - 1) * Math.sqrt(Math.pow(r, 2) - Math.pow(dX, 2));
		double dY = Math.random() * Math.sqrt(Math.pow(r, 2) - Math.pow(dX, 2) - Math.pow(dZ, 2));

		return new Location(loc.getWorld(), (int) (dX + loc.getX()) + 0.5, dY + loc.getY(), (int) (dZ + loc.getZ()) + 0.5);
	}
}
