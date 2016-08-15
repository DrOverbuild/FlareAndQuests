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
	FlareAndQuests plugin;
	Random rand = new Random();
	String name;
	Player activator;

	/**
	 * The location at which the flare will start falling
	 */
	Location flareSpawn;

	public Flare(Player activator, FlareAndQuests plugin, String name) {
		this.plugin = plugin;
		this.name = name;
		this.activator = activator;
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

	public Location getFlareSpawn() {
		return flareSpawn;
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

	public void dropFlare(){
		FallingBlock block = activator.getWorld().spawnFallingBlock(flareSpawn, Material.SAND, (byte) 0);
		block.setHurtEntities(false);
		plugin.getFallingFlares().put(block.getUniqueId(), this);
	}

	public void fillFlareChest(final Block chestBlock){
		chestBlock.setType(Material.CHEST);

		Chest c = (Chest) chestBlock.getState();
		plugin.getConf().load();
		List<ItemStack> list = (List<ItemStack>) plugin.getConf().config.getList("Flares." + this.getName() + ".Contents", new ArrayList<ItemStack>());
		ItemStack[] conts = new ItemStack[27];
		int count = 0;
		Random rand = new Random();

		int min = plugin.getConf().config.getInt("flare.min-contents");
		int max = plugin.getConf().config.getInt("flare.max-contents");

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

		getActivator().sendMessage(plugin.getTrans().format("Flare Arrived Message", chestBlock.getLocation(), getActivator()));

		plugin.getPartTimers().put(chestBlock, new BukkitRunnable() {
			public void run() {
				plugin.spawnParticle(plugin.getConf().config.getInt("flare.chest-particle-id"),
						chestBlock.getLocation().clone().add(0.5, 0.5, 0.5), 1, 1, 1, 5,
						chestBlock.getWorld().getEntitiesByClass(Player.class));
			}
		}.runTaskTimer(plugin, 0, 1));


	}

	public void setFlareSpawn(){
		double r = plugin.conf.config.getDouble("flare.drop-radius");
		int minFree = plugin.getConf().config.getInt("flare.min-free-above-blocks", 20);

		int count = 0;
		int max = plugin.conf.config.getInt("flare.max-tries");

		while (!plugin.inside(flareSpawn, (Location) plugin.getConf().config.get("Flares." + name + ".First"),
				(Location) plugin.getConf().config.get("Flares." + name + ".Second"))) {
			double randX = (rand.nextDouble() * r * 2) - r;
			double randZ = (rand.nextDouble() * r * 2) - r;

			flareSpawn = activator.getLocation().add(randX, Math.abs((double) minFree), randZ);

			count++;
			if (count >= max) {
				activator.sendMessage(plugin.getTrans().format("Flare Drop Failed Message", null, activator));
				flareSpawn = null;
				return;
			}
		}
	}

	public static void activateFlare(final ItemStack is, final Player player, final FlareAndQuests plugin, final String name) {
		plugin.getConf().load();

		if (plugin.playerFlares.containsKey(player)) {
			player.sendMessage(plugin.getTrans().format("Flare In Use", null, player));
			return;
		}

		double r = plugin.conf.config.getDouble("flare.drop-radius");

		// TODO: Check if there is enough space to spawn flare

		final Flare flare = new Flare(player, plugin, name);

		flare.setFlareSpawn();

		if(flare.getFlareSpawn() == null){
			return;
		}

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

		final int delay = plugin.getConf().config.getInt("flare.arrival-delay", 0);

		if (delay <= 0) {
			flare.dropFlare();
		} else {
			plugin.playerFlares.put(player, name);

			final String message2 = ChatColor.translateAlternateColorCodes('&', plugin.getTrans().config.getString("Flare Arriving In Action Bar Message"));

			new BukkitRunnable() {
				int secondsLeft = delay;

				public void run() {
					ActionBar.sendActionBar(player, message2.replace("{time}", secondsLeft + ""));
					if (secondsLeft <= 0) {
						plugin.playerFlares.remove(player);
						flare.dropFlare();
						this.cancel();
						return;
					}
					secondsLeft--;
				}
			}.runTaskTimer(plugin, 0, 20);
		}
	}
}
