package com.drizzard.faq.listeners;

import com.drizzard.faq.Flare;
import com.drizzard.faq.FlareAndQuests;
import com.drizzard.faq.util.SoundUtil;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by jasper on 8/12/16.
 * Handles all events under org.bukkit.event.entity
 */
public class EntityListener implements Listener {
	FlareAndQuests plugin;

	public EntityListener(FlareAndQuests plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onEntityChangeBlock(EntityChangeBlockEvent ev){
		if(ev.getEntityType().equals(EntityType.FALLING_BLOCK)){
			Flare flare = plugin.getFallingFlares().remove(ev.getEntity().getUniqueId());
			if(flare != null){
				// Next two lines are here because some plugins like to break
				// the landed block...
				ev.setCancelled(true);
				ev.getEntity().remove();

				ev.getBlock().setType(Material.AIR); // <- May not be necessary
				flare.doLightningAnimation(ev.getBlock());
			}
		}
	}

	@EventHandler
	public void onItemSpawn(ItemSpawnEvent ev){
		List<Entity> ents = ev.getEntity().getNearbyEntities(2, 2, 2);
		for(Entity e : ents){
			Flare flare = plugin.getFallingFlares().remove(e.getUniqueId());
			if(flare != null){
				ev.getEntity().remove();
				ev.getLocation().getBlock().getRelative(BlockFace.UP).setType(Material.AIR); // <- May not be necessary
				flare.doLightningAnimation(ev.getLocation().getBlock().getRelative(BlockFace.UP));
				return;
			}
		}
	}
}